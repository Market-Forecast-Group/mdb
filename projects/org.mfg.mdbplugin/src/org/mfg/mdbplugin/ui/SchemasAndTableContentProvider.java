package org.mfg.mdbplugin.ui;

import org.eclipse.core.resources.IFile;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdbplugin.MDBPlugin;

public class SchemasAndTableContentProvider extends SchemasContentProvider {
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFile) {
			IFile f = (IFile) parentElement;
			if (f.getFileExtension().equals("schema-json")) {
				try {
					Schema schema = MDBPlugin.readSchemaEditorFile(f);
					return schema.toArray();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.getChildren(parentElement);
	}
}
