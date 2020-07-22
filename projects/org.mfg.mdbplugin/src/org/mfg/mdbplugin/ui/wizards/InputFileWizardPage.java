package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.util.Comparator;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider;
import org.mfg.mdbplugin.ui.ConnectionsLabelProvider;
import org.mfg.mdbplugin.ui.UIUtils;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.viewers.Viewer;

public class InputFileWizardPage extends WizardPage {
	private static class Sorter extends ViewerSorter {
		public Sorter() {
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {			
			return 0;
		}
	}

	private TreeViewer treeViewer;
	File _selectedFile;

	/**
	 * Create the wizard.
	 */
	public InputFileWizardPage(File selectedFile) {
		super("wizardPage");
		setTitle("Export MDB Rows");
		setDescription("Export MDB rows to other MDB file");
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
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		ScrolledComposite scrolledComposite = new ScrolledComposite(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		treeViewer = new TreeViewer(scrolledComposite, SWT.BORDER);
		treeViewer.setSorter(new Sorter());
		Tree tree = treeViewer.getTree();
		tree.setLinesVisible(true);
		scrolledComposite.setContent(tree);
		scrolledComposite
				.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		treeViewer.setContentProvider(new ConnectionsContentProvider());
		treeViewer.setLabelProvider(new ConnectionsLabelProvider());

		afterCreateWidgets();
	}

	private void afterCreateWidgets() {
		setErrorMessage(null);

		treeViewer.setInput(ConnectionsContentProvider.MDB_CONNECTIONS_ROOT);
		if (_selectedFile != null && MDBPlugin.isMDBFile(_selectedFile)) {
			UIUtils.selectAndExpand(treeViewer, _selectedFile,
					new Comparator<Object>() {

						@Override
						public int compare(Object o1, Object o2) {
							return _selectedFile.equals(o2) ? 0 : -1;
						}
					});
		} else {
			setErrorMessage("Select a file to export");
		}
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object first = ((StructuredSelection) event.getSelection())
						.getFirstElement();
				File sel = first != null && first instanceof File ? (File) first
						: null;
				if (sel != null) {
					_selectedFile = sel;
				}
				if (sel != null && MDBPlugin.isMDBFile(sel)) {
					setErrorMessage(null);
				} else {
					setErrorMessage("Invalid selection");
				}
			}
		});
	}

	public File getSelectedFile() {
		return _selectedFile;
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
