package org.mfg.mdbplugin.ui.wizards;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.Type;

public class NewSchemaWizard extends BasicNewResourceWizard {

	private WizardNewFileCreationPage newPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		newPage = new WizardNewFileCreationPage(
				"New MFG Database Schema Wizard", currentSelection);
		newPage.setTitle("MFG Database Schema");
		newPage.setDescription("Create a MFG Database Schema.");
		newPage.setAllowExistingResources(false);
		newPage.setFileExtension("schema-json");
		newPage.setFileName("Database1");
	}

	@Override
	public boolean performFinish() {
		final IFile file = newPage.createNewFile();

		if (file == null) {
			return false;
		}

		try {
			Schema schema = new Schema();
			schema.setBufferSize(100);
			Table table = new Table("Table1", UUID.randomUUID());
			schema.add(table);
			table.add(new Column("Column1", Type.LONG, Order.NONE, false, "", 0, UUID.randomUUID()));

			file.setContents(new ByteArrayInputStream(schema.toJSONString()
					.getBytes()), IResource.FORCE, null);

			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}
			});

		} catch (CoreException e1) {
			e1.printStackTrace();
			newPage.setErrorMessage(e1.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public void addPages() {
		addPage(newPage);
	}

}
