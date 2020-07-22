package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.util.Comparator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider;
import org.mfg.mdbplugin.ui.ConnectionsDirsContentProvider;
import org.mfg.mdbplugin.ui.ConnectionsLabelProvider;
import org.mfg.mdbplugin.ui.UIUtils;

public class OutputFileInDbLocationWizardPage extends WizardPage {
	private static class Sorter extends ViewerSorter {
		public Sorter() {
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return e1 == null || e2 == null ? -1 : e1.toString().compareTo(
					e2.toString());
		}
	}

	private Text fileText;
	private TreeViewer viewer;
	File _selectedFile;
	private Table selectedTable;

	/**
	 * Create the wizard.
	 * 
	 * @param selectedFile2
	 */
	public OutputFileInDbLocationWizardPage(File selectedFile) {
		super("wizardPage");
		setTitle("Output Location");
		setDescription("Select an output location in a database");
		this._selectedFile = selectedFile;
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
		container.setLayout(new GridLayout(1, false));

		Label lblFileNameLike = new Label(container, SWT.NONE);
		lblFileNameLike
				.setText("File name, like \"prices.mdb\" or just \"prices\", it will add the \".mdb\" extension");

		fileText = new Text(container, SWT.BORDER);
		fileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		viewer = new TreeViewer(container, SWT.BORDER);
		viewer.setSorter(new Sorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validateSelection();
			}
		});
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.setContentProvider(new ConnectionsDirsContentProvider());
		viewer.setLabelProvider(new ConnectionsLabelProvider());

		afterCreateWidgets();
	}

	private void afterCreateWidgets() {
		viewer.setInput(ConnectionsContentProvider.MDB_CONNECTIONS_ROOT);
		viewer.expandAll();
		if (_selectedFile != null) {
			UIUtils.selectAndExpand(viewer, _selectedFile,
					new Comparator<Object>() {

						@Override
						public int compare(Object o1, Object o2) {
							if (_selectedFile.isDirectory()) {
								return _selectedFile.getAbsolutePath()
										.compareTo(o2.toString());
							}
							File p = _selectedFile.getParentFile();
							return p == null ? -1 : p.getAbsolutePath()
									.compareTo(o2.toString());
						}
					});
		}
	}

	public void setInitalName(String initalName) {
		fileText.setText(initalName.replace(".csv", "") + ".mdb");
	}

	void update() {
		setErrorMessage(null);
		String name = fileText.getText();
		if (name.trim().length() == 0 || name.contains("/")
				|| name.contains("\\")) {
			setErrorMessage("Invalid file name.");
		} else {
			if (!name.endsWith(".mdb")) {
				name += ".mdb";
			}
			File file = new File(getSelectedLocation(), name);
			if (file.exists()) {
				setErrorMessage("That file exists.");
			} else {
				setSelectedFile(file);
			}
		}
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}

	private String getSelectedLocation() {
		Object sel = ((StructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (sel instanceof File) {
			return ((File) sel).getAbsolutePath();
		}
		return (String) sel;
	}

	public File getSelectedFile() {
		return _selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this._selectedFile = selectedFile;
	}

	void validateSelection() {
		if (selectedTable == null) {
			return;
		}

		setErrorMessage(null);
		Object sel = ((StructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (sel == null) {
			setErrorMessage("No location selected");
		} else {
			File root;
			try {
				if (sel instanceof File) {
					File file = (File) sel;
					root = MDBPlugin.getRootFile(file);
				} else {
					root = new File((String) sel);
				}
				Schema schema = MDBPlugin.readMetadataSchemaFile(new File(root,
						".metadata/schema.json"));
				boolean valid = false;
				for (Table t : schema) {
					if (t.getUUID().equals(selectedTable.getUUID())) {
						valid = true;
						break;
					}
				}

				if (!valid) {
					setErrorMessage("The selected database does not contains the table definition \""
							+ selectedTable.getName() + "\".");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			if (getErrorMessage() == null) {
				update();
			}
		}
	}

	public void setSelectedTable(Table table) {
		selectedTable = table;
		if (viewer != null && !viewer.getSelection().isEmpty()
				&& selectedTable != null) {
			validateSelection();
		}
	}

}
