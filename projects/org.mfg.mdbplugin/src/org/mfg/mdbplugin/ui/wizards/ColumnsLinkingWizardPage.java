package org.mfg.mdbplugin.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;
import org.mfg.mdb.compiler.Column;

@SuppressWarnings("boxing")
public class ColumnsLinkingWizardPage extends WizardPage {

	public static class ColumnLink {
		public static final String DEFAULT_VALUE = "-- default --";

		public Column mdbColumn;
		public String csvColumn;
	}

	private Table _table;
	private org.mfg.mdb.compiler.Table _selectedTable;
	TableViewer viewer;
	String[] csvColumns;
	private TableViewerColumn tableViewerColumn_1;
	private Label tableLabel;

	private ArrayList<ColumnLink> links;

	/**
	 * Create the wizard.
	 */
	public ColumnsLinkingWizardPage() {
		super("wizardPage");
		setTitle("Columns Linking");
		setDescription("Link the MDB column with the CSV column.");
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

		tableLabel = new Label(container, SWT.NONE);
		tableLabel.setFont(SWTResourceManager.getFont("Ubuntu", 11, SWT.BOLD));
		tableLabel
				.setText("Table ...................................................");

		viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		_table = viewer.getTable();
		_table.setLinesVisible(true);
		_table.setHeaderVisible(true);
		_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((ColumnLink) element).mdbColumn.getName();
			}
		});
		TableColumn tblclmnMdbColumn = tableViewerColumn.getColumn();
		tblclmnMdbColumn.setWidth(182);
		tblclmnMdbColumn.setText("MDB Column");

		tableViewerColumn_1 = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn_1.setEditingSupport(getEditingSupport());
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((ColumnLink) element).csvColumn;
			}
		});
		TableColumn tblclmnCsvColumn = tableViewerColumn_1.getColumn();
		tblclmnCsvColumn.setWidth(100);
		tblclmnCsvColumn.setText("CSV Column");
		viewer.setContentProvider(new ArrayContentProvider());

		afterCreateWidgets();
	}

	private EditingSupport getEditingSupport() {
		return new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				ComboBoxCellEditor editor = new ComboBoxCellEditor(
						viewer.getTable(), csvColumns, SWT.READ_ONLY);
				return editor;
			}

			@Override
			protected Object getValue(Object element) {
				return Arrays.asList(csvColumns).indexOf(
						((ColumnLink) element).csvColumn);
			}

			@Override
			protected void setValue(Object element, Object value) {
				((ColumnLink) element).csvColumn = csvColumns[(Integer) value];
				viewer.refresh();
			}
		};
	}

	private void afterCreateWidgets() {
		if (_selectedTable != null && csvColumns != null) {
			updateContent();
		}
	}

	public void setSelectedTable(org.mfg.mdb.compiler.Table table) {
		_selectedTable = table;
		if (viewer != null) {
			updateContent();
		}
	}

	public void setSelectedCSVColumns(String[] columns) {
		csvColumns = columns;
		String[] aux = new String[csvColumns.length + 1];
		System.arraycopy(csvColumns, 0, aux, 0, csvColumns.length);
		aux[aux.length - 1] = ColumnLink.DEFAULT_VALUE;
		csvColumns = aux;
		if (viewer != null && _selectedTable != null) {
			updateContent();
		}
	}

	private void updateContent() {
		tableLabel.setText(_selectedTable.getName());

		links = new ArrayList<>();
		int i = 0;
		for (Column col : _selectedTable) {
			if (!col.isVirtual()) {
				ColumnLink link = new ColumnLink();
				link.mdbColumn = col;
				link.csvColumn = csvColumns[i % (csvColumns.length - 1)];
				links.add(link);
				i++;
			}
		}
		tableViewerColumn_1.setEditingSupport(getEditingSupport());
		viewer.setInput(links);
	}

	public ArrayList<ColumnLink> getLinks() {
		return links;
	}

}
