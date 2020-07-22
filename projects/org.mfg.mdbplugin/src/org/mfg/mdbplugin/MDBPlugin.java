package org.mfg.mdbplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.runtime.FilesTable;
import org.mfg.mdbplugin.jobs.CompileJob;
import org.mfg.mdbplugin.ui.UIUtils;
import org.mfg.mdbplugin.ui.editors.MDBFileEditor;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;
import org.mfg.mdbplugin.ui.editors.MDBSchemaEditor;
import org.mfg.mdbplugin.ui.views.MDBConnectionsView;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MDBPlugin extends AbstractUIPlugin {

	private static final String CONNECTION_PATHS_KEY = "connectionPaths";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.mfg.mdbplugin"; //$NON-NLS-1$

	// The shared instance
	private static MDBPlugin plugin;

	private ICompilerExtension[] compilerExtensions;

	private LinkedHashSet<String> connectionPaths;

	/**
	 * The constructor
	 */
	public MDBPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		connectionPaths = new LinkedHashSet<>();
		getPreferenceStore().setDefault(CONNECTION_PATHS_KEY, "");
		String str = getPreferenceStore().getString(CONNECTION_PATHS_KEY);

		String[] paths = str.split(";");
		for (String path : paths) {
			if (path.trim().length() > 0) {
				connectionPaths.add(path);
			}
		}
	}

	public String[] getConnectionPaths() {
		return connectionPaths.toArray(new String[connectionPaths.size()]);
	}

	public void addConnectionPath(String connectionPath) {
		connectionPaths.add(connectionPath);
	}

	public void removeConnectionPath(String connectionPath) {
		connectionPaths.remove(connectionPath);

		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] refs = activePage.getEditorReferences();
		List<IEditorReference> list = new ArrayList<>();

		for (IEditorReference ref : refs) {
			try {
				if (ref.getEditorInput() instanceof MDBFileEditorInput) {
					MDBFileEditorInput input = (MDBFileEditorInput) ref
							.getEditorInput();
					if (input.getFile().getAbsolutePath()
							.startsWith(connectionPath)) {
						list.add(ref);
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		activePage.closeEditors(
				list.toArray(new IEditorReference[list.size()]), false);
	}

	public static List<MDBSchemaEditor> findSchemaEditors(IFile file) {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] refs = activePage.getEditorReferences();
		List<MDBSchemaEditor> list = new ArrayList<>();
		for (IEditorReference ref : refs) {
			try {
				if (ref.getId().equals(MDBSchemaEditor.EDITOR_ID)) {
					MDBSchemaEditor editor = (MDBSchemaEditor) ref
							.getEditor(true);
					if (editor != null
							&& editor.getEditorInput().getFile().equals(file)) {
						list.add(editor);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static void closeEditors(File file) {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] refs = activePage.getEditorReferences();
		List<IEditorReference> list = new ArrayList<>();
		for (IEditorReference ref : refs) {
			try {
				if (ref.getEditorInput() instanceof MDBFileEditorInput) {
					MDBFileEditorInput input = (MDBFileEditorInput) ref
							.getEditorInput();
					if (input.getFile().equals(file)) {
						list.add(ref);
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		activePage.closeEditors(
				list.toArray(new IEditorReference[list.size()]), false);
	}

	public ICompilerExtension[] getCompilerExtensions() {
		if (compilerExtensions == null) {
			List<ICompilerExtension> list = new ArrayList<>();
			IConfigurationElement[] elems = Platform.getExtensionRegistry()
					.getConfigurationElementsFor("org.mfg.mdbplugin.emitter");
			for (IConfigurationElement elem : elems) {
				ICompilerExtension ext;
				try {
					ext = (ICompilerExtension) elem
							.createExecutableExtension("factory");
					list.add(ext);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			compilerExtensions = list.toArray(new ICompilerExtension[list
					.size()]);
		}
		return compilerExtensions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		StringBuilder sb = new StringBuilder();
		for (String path : connectionPaths) {
			if (sb.length() > 0) {
				sb.append(";");
			}
			sb.append(path);
		}
		getPreferenceStore().setValue(CONNECTION_PATHS_KEY, sb.toString());
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static MDBPlugin getDefault() {
		return plugin;
	}

	/**
	 * @param file
	 * @return
	 * @throws CoreException
	 * @throws JSONException
	 */
	public static Schema readSchemaEditorFile(IFile file) throws JSONException,
			CoreException {
		return new Schema(new JSONObject(new JSONTokener(new InputStreamReader(
				file.getContents()))));
	}

	public static Schema readMetadataSchemaFile(File file)
			throws JSONException, IOException {
		try (FileReader fileReader = new FileReader(file)) {
			return new Schema(new JSONObject(new JSONTokener(fileReader)));
		}
	}

	public static void compile(final IFile file, Schema info) {
		Job job = new CompileJob(file, info);
		job.setRule(file);
		job.schedule();

		job = new Job("Refresh Project") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					file.getProject().refreshLocal(IResource.DEPTH_INFINITE,
							monitor);
				} catch (CoreException e) {
					e.printStackTrace();
					return ValidationStatus.error(e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Deprecated
	public static void copyLibToProjectAndSetClasspath(IJavaProject project) {
		try (InputStream bundleLibStream = FileLocator.openStream(MDBPlugin
				.getDefault().getBundle(), new Path("lib/mdb.jar"), false)) {

			IFolder libsFolder = project.getProject().getFolder("lib");

			if (!libsFolder.exists()) {
				libsFolder.create(true, true, null);
			}

			IFile dstFile = libsFolder.getFile("mdb.jar");

			if (dstFile.exists()) {
				dstFile.setContents(bundleLibStream, true, false, null);
			} else {
				dstFile.create(bundleLibStream, true, null);
			}

			IClasspathEntry[] entries = project.getRawClasspath();

			for (IClasspathEntry entry : entries) {
				if (entry.getPath().equals(dstFile.getFullPath())) {
					return;
				}
			}
			IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];

			System.arraycopy(entries, 0, newEntries, 0, entries.length);

			// newEntries[entries.length] = JavaCore.newContainerEntry(mdbPath);
			newEntries[entries.length] = JavaCore.newLibraryEntry(
					dstFile.getFullPath(), null, null);
			project.setRawClasspath(newEntries, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Refresh all connections view.
	 * 
	 * @param revealObject
	 *            Reveal the object if not null. When a wizard creates a new
	 *            file, to reveal it.
	 */
	public static void refreshConnectionViews(Object revealObject) {
		IViewReference[] refs = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getViewReferences();
		for (IViewReference ref : refs) {
			if (ref.getId().equals(MDBConnectionsView.VIEW_ID)) {
				IWorkbenchPart view = ref.getPart(true);
				if (view != null) {
					MDBConnectionsView view2 = (MDBConnectionsView) view;
					view2.getCommonViewer().refresh();
					if (revealObject != null) {
						UIUtils.selectAndExpand(view2.getCommonViewer(),
								revealObject, new Comparator<Object>() {

									@Override
									public int compare(Object o1, Object o2) {
										return o1 == null || o2 == null ? -1
												: o1.toString().compareTo(
														o2.toString());
									}
								});
					}
				}
			}
		}
	}

	public static void refreshMDBEditors() {
		IEditorReference[] refs = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (IEditorReference ref : refs) {
			if (ref.getId().equals(MDBFileEditor.EDITOR_ID)) {
				IWorkbenchPart editor = ref.getPart(true);
				if (editor != null) {
					MDBFileEditor editor2 = (MDBFileEditor) editor;
					editor2.refresh();
				}
			}
		}
	}

	public static void openMDBEditor(File file) {
		try {
			PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.openEditor(new MDBFileEditorInput(file),
							MDBFileEditor.EDITOR_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	public static File getRootFile(File file) {
		if (file.isDirectory()
				&& new File(file, "/.metadata/schema.json").exists()) {
			return file;
		}
		File parent = file.getParentFile();
		if (parent != null) {
			return getRootFile(parent);
		}
		return null;
	}

	private static Map<File, FilesTable> _filesTableMap = new HashMap<>();

	@SuppressWarnings("resource")
	public static FilesTable getFilesTable(File dbRoot) throws IOException {
		if (!_filesTableMap.containsKey(dbRoot)) {
			FilesTable ftable = new FilesTable(dbRoot.toPath());
			_filesTableMap.put(dbRoot, ftable);
		}
		return _filesTableMap.get(dbRoot);
	}

	public static void forgetFilesTable(File dbRoot) {
		_filesTableMap.remove(dbRoot);
	}

	@SuppressWarnings("resource")
	public static Table getTableDef(File file) throws IOException,
			JSONException {
		if (isPartialBackupFile(file)) {
			File root = getPartialBackupFileRoot(file);
			if (root == null) {
				return null;
			}
			java.nio.file.Path bkPath = root.toPath();
			java.nio.file.Path backupsPath = bkPath.getParent();
			String dbname = backupsPath.getName(backupsPath.getNameCount() - 1)
					.toString().substring(1);
			java.nio.file.Path dbRoot = backupsPath.resolveSibling(dbname);
			java.nio.file.Path rel = bkPath.relativize(file.toPath());
			java.nio.file.Path dbFile = dbRoot.resolve(rel);
			return getTableDef(dbFile.toFile());
		}

		File root = getRootFile(file);
		if (root != null) {
			FilesTable filesTable = getFilesTable(root);
			String tableId = filesTable.lookupTableId(file.toPath());
			Schema schema = readMetadataSchemaFile(new File(root,
					".metadata/schema.json"));
			for (Table t : schema) {
				if (t.getUUID().toString().equals(tableId)) {
					return t;
				}
			}
		}
		return null;
	}

	public static File getArrayFile(File file) throws IOException {
		File root = getRootFile(file);
		if (root != null) {
			String path = file.getAbsolutePath().substring(
					root.getAbsolutePath().length());
			File mdFile = new File(root, ".metadata/" + path + ".metadata");
			if (mdFile.exists()) {
				Properties props = new Properties();
				try (FileInputStream in = new FileInputStream(mdFile)) {
					props.load(in);
					in.close();
					String arrayFilename = (String) props.get("array-file");
					if (arrayFilename != null) {
						return new File(file.getParentFile(), arrayFilename);
					}
				}
			}
		}
		return null;
	}

	public static boolean isMDBFile(File f) {
		try {
			Table table = getTableDef(f);
			return table != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isPartialBackupFile(File file) {
		return file.isFile() && isPartialBackupFile2(file);
	}

	private static boolean isPartialBackupFile2(File file) {
		if (file.getParentFile() == null) {
			return false;
		}
		if (Files.exists(file.toPath().resolveSibling("partial"))
				&& Files.exists(file.toPath().resolveSibling(
						"version.properties"))) {
			return true;
		}
		return isPartialBackupFile2(file.getParentFile());
	}

	public static boolean isBackupFile(File file) {
		return isBackupFile2(file);
	}

	private static boolean isBackupFile2(File file) {
		if (file == null || file.getParentFile() == null
				|| file.equals(file.getParentFile())) {
			return false;
		}
		if (Files.exists(file.toPath().resolveSibling("version.properties"))) {
			return true;
		}
		return isBackupFile2(file.getParentFile());
	}

	private static File getPartialBackupFileRoot(File file) {
		if (file == null || file.equals(file.getParentFile())) {
			return null;
		}
		if (file.isDirectory()
				&& Files.exists(file.toPath().resolve("partial"))
				&& Files.exists(file.toPath().resolve("version.properties"))) {
			return file;
		}
		return getPartialBackupFileRoot(file.getParentFile());
	}

	public static boolean isDBRoot(File file) {
		return new File(file, ".metadata/schema.json").exists()
				&& new File(file, ".metadata/signatures.properties").exists();
	}

	@SuppressWarnings("resource")
	public static void deleteFile(File file) throws IOException {
		new File(file.getPath() + ".array").delete();
		file.delete();
		FilesTable ftable = getFilesTable(MDBPlugin.getRootFile(file));
		ftable.delete(file.toPath());
		ftable.flush();
	}

	@SuppressWarnings({ "resource" })
	public static void renameFile(File file, String name) throws IOException,
			PartInitException {

		File newFile = new File(file.getParentFile(), name);
		file.renameTo(newFile);
		File arrayFile = new File(file.getPath() + ".array");
		if (arrayFile.exists()) {
			arrayFile.renameTo(new File(newFile.getPath() + ".array"));
		}
		FilesTable ftable = getFilesTable(getRootFile(file));
		String id = ftable.lookupTableId(file.toPath());
		ftable.delete(file.toPath());
		ftable.update(newFile, id);
		ftable.flush();

		refreshConnectionViews(null);

		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] refs = activePage.getEditorReferences();
		List<IEditorReference> list = new ArrayList<>();
		for (IEditorReference ref : refs) {
			try {
				if (ref.getEditorInput() instanceof MDBFileEditorInput) {
					MDBFileEditorInput input = (MDBFileEditorInput) ref
							.getEditorInput();
					if (input.getFile().equals(file)) {
						list.add(ref);
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		for (IEditorReference ref : list) {
			MDBFileEditorInput input = (MDBFileEditorInput) ref
					.getEditorInput();
			input.setFile(newFile);
			MDBFileEditor editor = (MDBFileEditor) ref.getEditor(false);
			if (editor != null) {
				editor.setInput(input);
			}
			input.setFile(newFile);
		}
	}

}
