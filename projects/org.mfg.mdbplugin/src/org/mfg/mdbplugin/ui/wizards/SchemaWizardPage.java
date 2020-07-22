package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.util.Comparator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider;
import org.mfg.mdbplugin.ui.ConnectionsLabelProvider;
import org.mfg.mdbplugin.ui.SchemasInConnectionsContentProvider;
import org.mfg.mdbplugin.ui.UIUtils;

public class SchemaWizardPage extends WizardPage {
	private static class Sorter extends ViewerSorter {
		public Sorter() {
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return e1 == null || e2 == null ? -1 : e1.toString().compareTo(
					e2.toString());
		}
	}

	private TreeViewer viewer;
	private Table selectedTable;
	private File _workbenchSelection;

	/**
	 * Create the wizard.
	 * 
	 * @param selectedFile
	 */
	public SchemaWizardPage(File workbenchSelection) {
		super("wizardPage");
		setTitle("Schema");
		setDescription("Select the MDB schema.");
		this._workbenchSelection = workbenchSelection;
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
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledComposite scrolledComposite = new ScrolledComposite(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		viewer = new TreeViewer(scrolledComposite, SWT.BORDER);
		viewer.setSorter(new Sorter());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		Tree tree = viewer.getTree();
		scrolledComposite.setContent(tree);
		scrolledComposite
				.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		viewer.setContentProvider(new SchemasInConnectionsContentProvider());

		afterCreateWidgets();
	}

	private void afterCreateWidgets() {
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
				new ConnectionsLabelProvider()));
		viewer.setInput(ConnectionsContentProvider.MDB_CONNECTIONS_ROOT);
		if (_workbenchSelection != null) {
			if (_workbenchSelection.isDirectory()) {
				File root = MDBPlugin.getRootFile(_workbenchSelection);
				UIUtils.selectAndExpand(viewer, root, new Comparator<Object>() {

					@Override
					public int compare(Object o1, Object o2) {
						return o1.toString().compareTo(o2.toString());
					}
				});
			} else {
				try {
					final Table t = MDBPlugin.getTableDef(_workbenchSelection);
					UIUtils.selectAndExpand(viewer, t,
							new Comparator<Object>() {

								@Override
								public int compare(Object o1, Object o2) {
									if (o2 instanceof Table) {
										int cmp = ((Table) o2).getUUID()
												.equals(t.getUUID()) ? 0 : -1;
										return cmp;
									}
									return -1;
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		validate();
	}

	void validate() {
		setErrorMessage("Invalid selection.");
		Object sel = ((StructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (sel instanceof Table) {
			setErrorMessage(null);
			setSelectedTable((Table) sel);
		}
	}

	public Table getSelectedTable() {
		return selectedTable;
	}

	public void setSelectedTable(Table table) {
		selectedTable = table;
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
