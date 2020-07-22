package org.mfg.mdbplugin.ui;

import java.io.File;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.ResourceManager;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.runtime.BackupVersion;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider.BackupsNode;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider.FilesNode;

public class ConnectionsLabelProvider extends LabelProvider implements
		IStyledLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof String || element instanceof BackupVersion) {
			return ResourceManager.getPluginImage(MDBPlugin.PLUGIN_ID,
					"icons/db.png");
		}

		if (element instanceof File) {
			File f = (File) element;
			if (f.isDirectory()) {
				return ResourceManager.getPluginImage(MDBPlugin.PLUGIN_ID,
						"icons/fldr_obj.gif");
			} else if (MDBPlugin.isPartialBackupFile(f)
					|| MDBPlugin.isMDBFile(f)) {
				return ResourceManager.getPluginImage(MDBPlugin.PLUGIN_ID,
						"icons/table.gif");
			}
		}

		if (element instanceof Table) {
			return ResourceManager.getPluginImage("org.eclipse.ui",
					"/icons/full/obj16/generic_element.gif");
		}
		if (element instanceof BackupsNode) {
			return ResourceManager.getPluginImage(MDBPlugin.PLUGIN_ID,
					"icons/backup.png");
		}
		return ResourceManager.getPluginImage("org.eclipse.ui",
				"/icons/full/obj16/generic_elements.gif");
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof String) {
			return new StyledString(element.toString());
		}
		if (element instanceof File) {
			File f = (File) element;
			if (f.isFile()) {
				try {
					Table t = MDBPlugin.getTableDef(f);
					StyledString s = new StyledString();
					s.append(f.getName());
					if (t == null) { // partial files does not have metadata
						s.append(" - ?", StyledString.QUALIFIER_STYLER);
					} else {
						s.append(" - " + t.getName(),
								StyledString.QUALIFIER_STYLER);
					}
					return s;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return new StyledString(f.getName());
		}
		if (element instanceof Table) {
			return new StyledString(((Table) element).getName());
		}

		if (element instanceof BackupVersion) {
			BackupVersion version = (BackupVersion) element;
			String name = (version.isPartial() ? "(P)" : "(T)") + " "
					+ version.getDate().toString();

			StyledString ss = new StyledString(name);
			if (version.getComment().length() > 0) {
				ss.append(" - " + version.getComment(),
						StyledString.QUALIFIER_STYLER);
			}
			return ss;
		}

		if (element instanceof BackupsNode) {
			return new StyledString("Backups");
		}

		if (element instanceof FilesNode) {
			return new StyledString("Files");
		}
		return new StyledString(element.toString());
	}

	@Override
	public String getText(Object element) {
		return getStyledText(element).toString();
	}
}
