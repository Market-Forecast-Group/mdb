package org.mfg.mdbplugin.ui.wizards;

import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.SchemasContentProvider;
import org.mfg.mdbplugin.ui.UIUtils;

public class WorkspaceSchemaWizardPage extends WizardPage {
	private Text nameText;
	private TreeViewer viewer;
	private IFile selectedSchemaFile;
	private Schema selectedSchema;
	IFile _initFile;

	/**
	 * Create the wizard.
	 * 
	 * @param selectedFile
	 */
	public WorkspaceSchemaWizardPage(IFile initFile) {
		super("wizardPage");
		setTitle("Workspace Schema");
		setDescription("Select a workspace schema and new table name");
		this._initFile = initFile;
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblTableName = new Label(container, SWT.NONE);
		lblTableName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblTableName.setText("Table name:");

		nameText = new Text(container, SWT.BORDER);
		nameText.setText("Table1");
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				validate();
			}
		});
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Label lblWorkspaceSchema = new Label(container, SWT.NONE);
		lblWorkspaceSchema.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		lblWorkspaceSchema.setText("Workspace schema:");

		viewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setContentProvider(new SchemasContentProvider());

		afterCreateWidgets();
	}

	private void afterCreateWidgets() {
		viewer.setInput(ResourcesPlugin.getWorkspace());
		UIUtils.selectAndExpand(viewer, "no-key", new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				if (o2 instanceof IFile) {
					return _initFile == null || _initFile.equals(o2) ? 0 : -1;
				}
				return -1;
			}
		});
		viewer.expandAll();
	}

	private void checkSelection() {
		if (getErrorMessage() == null) {
			Object sel = ((StructuredSelection) viewer.getSelection())
					.getFirstElement();
			if (sel != null && sel instanceof IFile) {
				IFile f = (IFile) sel;
				if (f.isSynchronized(IResource.DEPTH_ONE)) {
					try {
						selectedSchema = MDBPlugin.readSchemaEditorFile(f);
						if (checkNameNotUsed(f)) {
							selectedSchemaFile = f;
						}
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					setErrorMessage("Resource out of synchronization.");
					return;
				}
			}
			setErrorMessage("The selected object is not an MDB schema.");
		}
	}

	/**
	 * @param file
	 */
	private boolean checkNameNotUsed(IFile file) {
		if (selectedSchema != null) {
			for (Table t : selectedSchema) {
				if (t.getName().equals(getTableName())) {
					setErrorMessage("That table name exists.");
					return false;
				}
			}
		}
		return true;
	}

	public String getTableName() {
		return nameText.getText();
	}

	public IFile getSelectedSchemaFile() {
		return selectedSchemaFile;
	}

	protected void validate() {
		setErrorMessage(null);
		if (checkNameSyntax()) {
			checkSelection();
		}
	}

	private boolean checkNameSyntax() {
		String str = nameText.getText();
		for (char c : str.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				setErrorMessage("Invalid table name, it should be a valid Java identifier.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
