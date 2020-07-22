package org.mfg.mdbplugin.ui.wizards;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.jobs.ExportMDBRowsJob;
import org.mfg.mdbplugin.ui.editors.MDBFileEditor;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;

public class ExportRowsWizard extends Wizard implements IExportWizard {
	private File selectedFile;
	InputFileWizardPage filePage;
	RowsWizardPage rangePage;
	OutputFileWizardPage targetPage;
	private Object[][] selectedRows;

	public ExportRowsWizard() {
		setWindowTitle("Export MDB Rows");
	}

	@Override
	public void addPages() {
		filePage = new InputFileWizardPage(selectedFile) {
			@Override
			public void setPageComplete(boolean complete) {
				super.setPageComplete(complete);
				if (complete) {
					File file = filePage.getSelectedFile();
					if (file != null) {
						rangePage.setFile(file);
						targetPage.setSourceFile(file);
					}
				}
			}
		};
		rangePage = new RowsWizardPage(selectedRows);
		targetPage = new OutputFileWizardPage();

		addPage(filePage);
		addPage(rangePage);
		addPage(targetPage);
	}

	@Override
	public boolean performFinish() {
		ExportMDBRowsJob job = new ExportMDBRowsJob(filePage.getSelectedFile(),
				targetPage.getSelectedFile(), rangePage.getStart(),
				rangePage.getStop(), selectedRows);
		job.schedule();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object first = selection.getFirstElement();
		if (first instanceof File) {
			File file = (File) first;
			if (MDBPlugin.isMDBFile(file)) {
				selectedFile = file;
			}
		} else {
			IEditorPart editor = workbench.getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
			if (editor != null && editor instanceof MDBFileEditor) {
				MDBFileEditor mdbEditor = (MDBFileEditor) editor;
				MDBFileEditorInput input = (MDBFileEditorInput) mdbEditor
						.getEditorInput();
				selectedFile = input.getFile();
				selectedRows = mdbEditor.getSelectedRows();
			}
		}
	}

}
