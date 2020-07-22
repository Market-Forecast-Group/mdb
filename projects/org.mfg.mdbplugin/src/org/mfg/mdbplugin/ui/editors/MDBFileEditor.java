package org.mfg.mdbplugin.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

public class MDBFileEditor extends FormEditor {

	public static final String EDITOR_ID = "org.mfg.mdbplugin.ui.editors.mdbfile";

	@Override
	protected void addPages() {
		try {
			addPage(new MDBFileEditorPage(this, "data", "Data"));
			addPage(new MDBFileQueryEditorPage(this, "query", "Query"));
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}

	public void refresh() {
		if (!pages.isEmpty()) {
			MDBFileEditorPage page = (MDBFileEditorPage) pages.get(0);
			page.updateTable(true);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// nothing
	}

	@Override
	public void doSaveAs() {
		// nothing
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public Object[][] getSelectedRows() {
		MDBFileEditorPage page = (MDBFileEditorPage) pages.get(0);
		return page.getSelectedRows();
	}

}
