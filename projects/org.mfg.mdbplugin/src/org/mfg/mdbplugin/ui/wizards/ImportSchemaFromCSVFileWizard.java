package org.mfg.mdbplugin.ui.wizards;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdbplugin.jobs.ImportCSVSchemaJob;

public class ImportSchemaFromCSVFileWizard extends Wizard implements
		IImportWizard {

	private InputCSVFile_Schema_WizardPage inputPage;
	private WorkspaceSchemaWizardPage outputPage;
	private IFile iniFile;

	public ImportSchemaFromCSVFileWizard() {
		setWindowTitle("Import Schema from CSV File");
	}

	@Override
	public void addPages() {
		inputPage = new InputCSVFile_Schema_WizardPage();
		outputPage = new WorkspaceSchemaWizardPage(iniFile);
		addPage(inputPage);
		addPage(outputPage);
	}

	@Override
	public boolean performFinish() {
		IFile file = outputPage.getSelectedSchemaFile();
		List<Column> cols = inputPage.getSelectedColumns();
		String name = outputPage.getTableName();
		ImportCSVSchemaJob job = new ImportCSVSchemaJob(file, cols, name);
		job.schedule();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object sel = ((StructuredSelection) selection).getFirstElement();
		if (sel instanceof IFile) {
			iniFile = (IFile) sel;
		}
	}

}
