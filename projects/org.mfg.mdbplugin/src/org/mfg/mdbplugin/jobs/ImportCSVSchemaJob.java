package org.mfg.mdbplugin.jobs;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.editors.MDBSchemaEditor;

public class ImportCSVSchemaJob extends WorkbenchJob {

	private IFile _schemaFile;
	private List<Column> _columns;
	private String _name;

	public ImportCSVSchemaJob(IFile schemaFile, List<Column> columns,
			String name) {
		super("Import table schema from CSV");
		this._schemaFile = schemaFile;
		this._columns = columns;
		this._name = name;
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		try {
			List<MDBSchemaEditor> editors = MDBPlugin
					.findSchemaEditors(_schemaFile);
			Table t = new Table(_name, UUID.randomUUID());
			t.addAll(_columns);

			for (MDBSchemaEditor editor : editors) {
				editor.getMainPage().addTable(t);
			}
			IWorkbenchPage activePage = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			if (editors.isEmpty()) {
				Schema schema = MDBPlugin.readSchemaEditorFile(_schemaFile);
				schema.add(t);
				_schemaFile.setContents(new ByteArrayInputStream(schema
						.toJSONString().getBytes()), true, false, monitor);
			} else {
				activePage.activate(editors.get(0));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

}
