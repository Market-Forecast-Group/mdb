package org.mfg.mdbplugin.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.BackupVersion;
import org.mfg.mdbplugin.MDBPlugin;

public class ConnectionsContentProvider implements ITreeContentProvider {
	public static final String MDB_CONNECTIONS_ROOT = "org.mfg.mdbplugin.ui.views.connections";

	public static class BackupsNode {
		private File _dbFile;

		public BackupsNode(File dbFile) {
			super();
			_dbFile = dbFile;
		}

		public File getDbFile() {
			return _dbFile;
		}

		@Override
		public String toString() {
			return _dbFile.getAbsolutePath();
		}
	}

	public static class FilesNode {
		private String _connection;

		public FilesNode(String connection) {
			_connection = connection;
		}

		public String getConnection() {
			return _connection;
		}
	}

	@Override
	public void dispose() {
		// nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == MDB_CONNECTIONS_ROOT) {
			return MDBPlugin.getDefault().getConnectionPaths();
		}

		if (parentElement instanceof String) {
			String conn = (String) parentElement;
			return new Object[] { new FilesNode(conn),
					new BackupsNode(new File(conn)) };
		}

		if (parentElement instanceof FilesNode) {
			return getChildren(new File(
					((FilesNode) parentElement).getConnection()));
		}

		if (parentElement instanceof BackupsNode) {
			File f = ((BackupsNode) parentElement).getDbFile();
			try {
				List<BackupVersion> versions = MDBSession.getBackups(f
						.toPath());
				
				Collections.sort(versions);
				Collections.reverse(versions);
				
				return versions.toArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (parentElement instanceof BackupVersion) {
			BackupVersion version = (BackupVersion) parentElement;
			if (version.isPartial()) {
				try {
					Properties props = MDBSession
							.readBackupProperties(version);
					String fname = props
							.getProperty(MDBSession.KEY_VERSION_PARTIAL_BACKUP);
					File f = version.getPath().resolve(fname).toFile();
					return new Object[] { f };
				} catch (IOException e) {
					e.printStackTrace();
					return new Object[0];
				}
			}

			File f = version.getPath().toFile();
			return getChildren(f);
		}

		if (parentElement instanceof File) {
			File[] files = ((File) parentElement).listFiles();
			if (files != null) {
				List<File> list = new ArrayList<>();
				for (File f : files) {
					if (f.getName().startsWith(".")) {
						continue;
					}
					if (f.isDirectory()) {
						if (f.listFiles().length == 0) {
							continue;
						}
					} else {
						if (!MDBPlugin.isMDBFile(f)) {
							continue;
						}
					}
					addFile(list, f);
				}
				return list.toArray();
			}
		}
		return null;
	}

	@SuppressWarnings("static-method")
	protected void addFile(List<File> list, File f) {
		list.add(f);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

}
