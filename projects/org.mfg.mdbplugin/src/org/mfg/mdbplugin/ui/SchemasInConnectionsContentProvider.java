package org.mfg.mdbplugin.ui;

import java.io.File;

import org.mfg.mdb.compiler.Schema;
import org.mfg.mdbplugin.MDBPlugin;

public class SchemasInConnectionsContentProvider extends
		ConnectionsContentProvider {
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement != MDB_CONNECTIONS_ROOT
				&& parentElement instanceof String) {
			String conn = (String) parentElement;
			try {
				Schema schema = MDBPlugin.readMetadataSchemaFile(new File(conn,
						".metadata/schema.json"));
				return schema.toArray();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return super.getChildren(parentElement);
	}
}
