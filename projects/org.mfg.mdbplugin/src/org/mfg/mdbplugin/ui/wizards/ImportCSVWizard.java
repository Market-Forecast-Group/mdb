package org.mfg.mdbplugin.ui.wizards;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.jobs.ImportCSVJob;
import org.mfg.mdbplugin.ui.editors.MDBFileEditor;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;

public class ImportCSVWizard extends Wizard implements IImportWizard {

	ColumnsLinkingWizardPage linkPage;
	OutputFileInDbLocationWizardPage outputPage;
	private InputCSVFileWizardPage inputPage;
	private SchemaWizardPage schemaPage;
	private File selectedFile;

	public ImportCSVWizard() {
		setWindowTitle("Import CSV File");
	}

	@Override
	public void addPages() {
		inputPage = new InputCSVFileWizardPage() {
			@Override
			protected void setSelectedFile(File file) {
				super.setSelectedFile(file);
				outputPage.setInitalName(file.getName());
			}

			@Override
			protected void setHeaders(String[] headers) {
				super.setHeaders(headers);
				linkPage.setSelectedCSVColumns(headers);
			}
		};
		schemaPage = new SchemaWizardPage(selectedFile) {
			@Override
			public void setSelectedTable(Table table) {
				super.setSelectedTable(table);
				linkPage.setSelectedTable(table);
				outputPage.setSelectedTable(table);
			}
		};
		linkPage = new ColumnsLinkingWizardPage();
		outputPage = new OutputFileInDbLocationWizardPage(selectedFile);

		addPage(inputPage);
		addPage(schemaPage);
		addPage(linkPage);
		addPage(outputPage);
	}

	@Override
	public boolean performFinish() {
		ImportCSVJob job = new ImportCSVJob(inputPage.getSelectedFile(),
				schemaPage.getSelectedTable(), linkPage.getLinks(),
				outputPage.getSelectedFile());
		job.schedule();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object first = selection.getFirstElement();
		if (first instanceof File) {
			selectedFile = (File) first;
		} else if (first instanceof String
				&& MDBPlugin.isDBRoot(new File((String) first))) {
			selectedFile = new File((String) first);
		} else {
			IEditorPart editor = workbench.getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
			if (editor != null && editor instanceof MDBFileEditor) {
				MDBFileEditor mdbEditor = (MDBFileEditor) editor;
				MDBFileEditorInput input = (MDBFileEditorInput) mdbEditor
						.getEditorInput();
				selectedFile = input.getFile();
			}
		}
	}
}
