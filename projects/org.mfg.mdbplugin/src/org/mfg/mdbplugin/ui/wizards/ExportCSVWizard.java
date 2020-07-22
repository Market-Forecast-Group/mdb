package org.mfg.mdbplugin.ui.wizards;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.jobs.ExportCSVJob;
import org.mfg.mdbplugin.ui.editors.MDBFileEditor;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;

public class ExportCSVWizard extends Wizard implements IExportWizard {
	private File selectedFile;
	InputFileWizardPage filePage;
	RowsWizardPage rangePage;
	private OutputCSVFileWizardPage outputPage;
	private Object[][] selectedRows;
	ColumnsWizardPage columnsPage;

	public ExportCSVWizard() {
		setWindowTitle("Export MDB to CSV");
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
						columnsPage.setFile(file);
					}
				}
			}
		};
		rangePage = new RowsWizardPage(selectedRows);
		columnsPage = new ColumnsWizardPage();
		outputPage = new OutputCSVFileWizardPage();

		addPage(filePage);
		addPage(rangePage);
		addPage(columnsPage);
		addPage(outputPage);
	}

	@Override
	public boolean performFinish() {
		if (outputPage.getSelectedFile().exists()) {
			if (!MessageDialog.openConfirm(getShell(), "Override",
					"Do you want to override "
							+ outputPage.getSelectedFile().getName() + "?")) {
				return false;
			}
		}
		ExportCSVJob job = new ExportCSVJob(filePage.getSelectedFile(),
				outputPage.getSelectedFile(), rangePage.getStart(),
				rangePage.getStop(), selectedRows,
				columnsPage.getSelectedColumns());
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
