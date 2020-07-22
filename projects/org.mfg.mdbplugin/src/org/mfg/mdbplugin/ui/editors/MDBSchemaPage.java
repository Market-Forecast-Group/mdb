/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdbplugin.ui.editors;

import java.util.Collections;
import java.util.UUID;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wb.swt.ResourceManager;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Type;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.DoubleClickEditingSupport;

/**
 * @author arian
 * 
 */
@SuppressWarnings("boxing")
public class MDBSchemaPage extends FormPage {
	Table _table;
	Table _table_1;
	private boolean _dirty = false;
	TableViewer _tablesViewer;
	TableViewer _columnsViewer;
	ImageHyperlink _removeTableBtn;
	ImageHyperlink _addColumnBtn;
	ImageHyperlink _removeColumnBtn;

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public MDBSchemaPage(String id, String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 * 
	 * @param editor
	 * @param id
	 * @param title
	 * @wbp.parser.constructor
	 * @wbp.eval.method.parameter id "Some id"
	 * @wbp.eval.method.parameter title "Some title"
	 */
	public MDBSchemaPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 * 
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("MDB Schema");
		Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(new GridLayout(1, false));

		createActions();

		SashForm sashForm = new SashForm(managedForm.getForm().getBody(),
				SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		managedForm.getToolkit().adapt(sashForm);
		managedForm.getToolkit().paintBordersFor(sashForm);

		@SuppressWarnings("static-access")
		Section sctnTables = managedForm.getToolkit().createSection(sashForm,
				Section.TWISTIE | Section.TITLE_BAR);
		managedForm.getToolkit().paintBordersFor(sctnTables);
		sctnTables.setText("Tables");
		sctnTables.setExpanded(true);

		Composite composite = managedForm.getToolkit().createComposite(
				sctnTables, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnTables.setClient(composite);
		composite.setLayout(new GridLayout(1, false));

		_tablesViewer = new TableViewer(composite, SWT.FULL_SELECTION);
		_tablesViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						boolean empty = _tablesViewer.getSelection().isEmpty();
						_removeTableBtn.setEnabled(!empty);
						_addColumnBtn.setEnabled(!empty);
						if (!empty) {
							_columnsViewer
									.setInput(((StructuredSelection) _tablesViewer
											.getSelection()).getFirstElement());
						}
					}
				});
		_table = _tablesViewer.getTable();
		_table.setHeaderVisible(true);
		_table.setLinesVisible(true);
		_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().paintBordersFor(_table);

		TableViewerColumn tableViewerColumn_4 = new TableViewerColumn(
				_tablesViewer, SWT.NONE);
		tableViewerColumn_4.setEditingSupport(new DoubleClickEditingSupport(
				_tablesViewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(_table);
			}

			@Override
			protected Object getValue(Object element) {
				return ((org.mfg.mdb.compiler.Table) element).getName();
			}

			@Override
			protected void setValue(Object element, Object value) {
				org.mfg.mdb.compiler.Table t = (org.mfg.mdb.compiler.Table) element;
				if (!t.getName().equals(value)) {
					t.setName((String) value);
					_tablesViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn_4.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((org.mfg.mdb.compiler.Table) element).getName();
			}
		});
		TableColumn tblclmnName_1 = tableViewerColumn_4.getColumn();
		tblclmnName_1.setWidth(220);
		tblclmnName_1.setText("Table Name");
		_tablesViewer.setContentProvider(new ArrayContentProvider());

		Composite composite_1 = managedForm.getToolkit().createComposite(
				composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_1);

		ImageHyperlink mghprlnkAddTable = managedForm.getToolkit()
				.createImageHyperlink(composite_1, SWT.NONE);
		mghprlnkAddTable.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				addTable();
			}

			@Override
			public void linkEntered(HyperlinkEvent e) {
				// nothing
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
				// nothing
			}
		});
		mghprlnkAddTable.setImage(ResourceManager.getPluginImage(
				"org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
		managedForm.getToolkit().paintBordersFor(mghprlnkAddTable);
		mghprlnkAddTable.setText("Add Table");

		_removeTableBtn = managedForm.getToolkit().createImageHyperlink(
				composite_1, SWT.NONE);
		_removeTableBtn.setEnabled(false);
		_removeTableBtn.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				removeTable();
			}

			@Override
			public void linkEntered(HyperlinkEvent e) {
				// nothing
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
				// nothing
			}
		});
		_removeTableBtn.setImage(ResourceManager.getPluginImage(
				"org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		managedForm.getToolkit().paintBordersFor(_removeTableBtn);
		_removeTableBtn.setText("Remove Table");

		@SuppressWarnings("static-access")
		Section sctnColumns = managedForm.getToolkit().createSection(sashForm,
				Section.TWISTIE | Section.TITLE_BAR);
		sctnColumns.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		managedForm.getToolkit().paintBordersFor(sctnColumns);
		sctnColumns.setText("Columns");
		sctnColumns.setExpanded(true);

		Composite composite_2 = managedForm.getToolkit().createComposite(
				sctnColumns, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_2);
		sctnColumns.setClient(composite_2);
		composite_2.setLayout(new GridLayout(1, false));

		_columnsViewer = new TableViewer(composite_2, SWT.FULL_SELECTION);
		_columnsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						boolean empty = _columnsViewer.getSelection().isEmpty();
						_removeColumnBtn.setEnabled(!empty);
					}
				});
		_table_1 = _columnsViewer.getTable();
		_table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		_table_1.setLinesVisible(true);
		_table_1.setHeaderVisible(true);
		managedForm.getToolkit().paintBordersFor(_table_1);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn.setEditingSupport(new DoubleClickEditingSupport(
				_columnsViewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(_table_1);
			}

			@Override
			protected Object getValue(Object element) {
				return ((Column) element).getName();
			}

			@Override
			protected void setValue(Object element, Object value) {
				Column c = (Column) element;
				if (!c.getName().equals(value)) {
					c.setName((String) value);
					_columnsViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((Column) element).getName();
			}
		});
		TableColumn tblclmnName = tableViewerColumn.getColumn();
		tblclmnName.setWidth(214);
		tblclmnName.setText("Column Name");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn_1.setEditingSupport(new DoubleClickEditingSupport(
				_columnsViewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				String[] items = new String[Type.values().length];
				for (int i = 0; i < items.length; i++) {
					items[i] = Type.values()[i].name();
				}
				ComboBoxCellEditor combo = new ComboBoxCellEditor(_table_1,
						items);
				combo.setValue(((Column) element).getType().ordinal());
				return combo;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Column) element).getType().ordinal();
			}

			@Override
			protected void setValue(Object element, Object value) {
				Column c = (Column) element;
				if (!value.equals(c.getType().ordinal())) {
					c.setType(Type.values()[(Integer) value]);
					_columnsViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((Column) element).getType().toString();
			}
		});
		TableColumn tblclmnType = tableViewerColumn_1.getColumn();
		tblclmnType.setWidth(100);
		tblclmnType.setText("Type");

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn_2.setEditingSupport(new DoubleClickEditingSupport(
				_columnsViewer) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				String[] items = new String[Order.values().length];
				for (int i = 0; i < items.length; i++) {
					items[i] = Order.values()[i].name();
				}
				ComboBoxCellEditor combo = new ComboBoxCellEditor(_table_1,
						items);
				combo.setValue(((Column) element).getOrder().ordinal());
				return combo;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Column) element).getOrder().ordinal();
			}

			@Override
			protected void setValue(Object element, Object value) {
				Column c = (Column) element;
				if (!value.equals(c.getOrder().ordinal())) {
					c.setOrder(Order.values()[(Integer) value]);
					_columnsViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn_2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				Order order = ((Column) element).getOrder();
				return order == Order.NONE ? "" : order.toString();
			}
		});
		TableColumn tblclmnOrder = tableViewerColumn_2.getColumn();
		tblclmnOrder.setWidth(100);
		tblclmnOrder.setText("Order");

		TableViewerColumn tableViewerColumn_3 = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn_3.setEditingSupport(new DoubleClickEditingSupport(
				_columnsViewer) {
			@Override
			protected CellEditor getCellEditor(Object element) {
				String[] items = { "Yes", "No" };
				ComboBoxCellEditor combo = new ComboBoxCellEditor(_table_1,
						items);
				combo.setValue(((Column) element).isVirtual() ? 0 : 1);
				return combo;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Column) element).isVirtual() ? 0 : 1;
			}

			@Override
			protected void setValue(Object element, Object value) {
				Column c = (Column) element;
				if (!value.equals(c.isVirtual() ? 0 : 1)) {
					c.setVirtual((Integer) value == 0);
					_columnsViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((Column) element).isVirtual() ? "Yes" : "No";
			}
		});
		TableColumn tblclmnVirtual = tableViewerColumn_3.getColumn();
		tblclmnVirtual.setWidth(100);
		tblclmnVirtual.setText("Virtual");

		TableViewerColumn tableViewerColumn_5 = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn_5.setEditingSupport(new DoubleClickEditingSupport(
				_columnsViewer) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(_table_1);
			}

			@Override
			protected Object getValue(Object element) {
				return ((Column) element).getFormula();
			}

			@Override
			protected void setValue(Object element, Object value) {
				Column c = (Column) element;
				if (!value.equals(c.getFormula())) {
					c.setFormula((String) value);
					_columnsViewer.refresh();
					setDirty(true);
				}
			}
		});
		tableViewerColumn_5.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				String formula = ((Column) element).getFormula();
				return formula == null ? "" : formula.toString();
			}
		});
		TableColumn tblclmnFormula = tableViewerColumn_5.getColumn();
		tblclmnFormula.setWidth(100);
		tblclmnFormula.setText("Formula");
		_columnsViewer.setContentProvider(new ArrayContentProvider());

		Composite composite_3 = managedForm.getToolkit().createComposite(
				composite_2, SWT.NONE);
		composite_3.setLayout(new GridLayout(2, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_3);

		_addColumnBtn = managedForm.getToolkit().createImageHyperlink(
				composite_3, SWT.NONE);
		_addColumnBtn.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				addColumn();
			}

			@Override
			public void linkEntered(HyperlinkEvent e) {
				// nothing
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
				// nothing
			}
		});
		_addColumnBtn.setEnabled(false);
		_addColumnBtn.setImage(ResourceManager.getPluginImage("org.eclipse.ui",
				"/icons/full/obj16/add_obj.gif"));
		managedForm.getToolkit().paintBordersFor(_addColumnBtn);
		_addColumnBtn.setText("Add Column");

		_removeColumnBtn = managedForm.getToolkit().createImageHyperlink(
				composite_3, SWT.NONE);
		_removeColumnBtn.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				removeColumn();
			}

			@Override
			public void linkEntered(HyperlinkEvent e) {
				// nothing
			}

			@Override
			public void linkExited(HyperlinkEvent e) {
				// nothing
			}
		});
		_removeColumnBtn.setEnabled(false);
		_removeColumnBtn.setImage(ResourceManager.getPluginImage(
				"org.eclipse.ui", "/icons/full/obj16/delete_obj.gif"));
		managedForm.getToolkit().paintBordersFor(_removeColumnBtn);
		_removeColumnBtn.setText("Remove Column");
		sashForm.setWeights(new int[] { 1, 1 });

		afterCreateWidgets();
	}

	private void createActions() {
		IToolBarManager manager = getManagedForm().getForm()
				.getToolBarManager();
		manager.add(new Action("Compile", ResourceManager
				.getPluginImageDescriptor("org.mfg.mdbplugin",
						"/icons/run_co.gif")) {
			@Override
			public void run() {
				compile();
			}
		});
		manager.update(true);
	}

	protected void compile() {
		final IFile file = getEditor().getEditorInput().getFile();
		Schema schema = getEditor().getSchema();
		MDBPlugin.compile(file, schema);
	}

	protected void removeColumn() {
		if (MessageDialog.openConfirm(getSite().getShell(), "Delete Table",
				"Are you sure do you want to remove the table")) {
			StructuredSelection sel = (StructuredSelection) _tablesViewer
					.getSelection();
			org.mfg.mdb.compiler.Table table = (org.mfg.mdb.compiler.Table) sel
					.getFirstElement();
			Object col = ((StructuredSelection) _columnsViewer.getSelection())
					.getFirstElement();
			table.remove(col);
			_columnsViewer.refresh();
			setDirty(true);
		}
	}

	protected void addColumn() {
		final org.mfg.mdb.compiler.Table table = (org.mfg.mdb.compiler.Table) ((StructuredSelection) _tablesViewer
				.getSelection()).getFirstElement();

		InputDialog dlg = new InputDialog(getSite().getShell(), "New Column",
				"Enter the column name", "Column" + (table.size() + 1),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						for (Column c : table) {
							if (newText.equals(c.getName())) {
								return "That name is used by other column.";
							}
						}
						return null;
					}
				});
		if (dlg.open() == Window.OK) {
			Column col = new Column(dlg.getValue(), Type.LONG, Order.NONE,
					false, "", 0, UUID.randomUUID());
			table.add(col);
			_columnsViewer.refresh();
			setDirty(true);
		}
	}

	protected void removeTable() {
		if (MessageDialog.openConfirm(getSite().getShell(), "Delete Table",
				"Are you sure do you want to remove the table")) {
			StructuredSelection sel = (StructuredSelection) _tablesViewer
					.getSelection();
			int i = 0;
			for (Object t : getSchema()) {
				if (t == sel.getFirstElement()) {
					break;
				}
				i++;
			}
			getSchema().remove(i);
			_tablesViewer.setSelection(StructuredSelection.EMPTY);
			_tablesViewer.refresh();
			_columnsViewer.setInput(Collections.EMPTY_LIST);
			setDirty(true);
		}
	}

	private void afterCreateWidgets() {
		refresh();
	}

	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				IFile file = ((FileEditorInput) getEditorInput()).getFile();
				int itable = 0;
				int icol = 0;

				_tablesViewer.setInput(Collections.EMPTY_LIST);
				_columnsViewer.setInput(Collections.EMPTY_LIST);

				if (file != null && file.exists()) {
					try {
						String selectedTable = file
								.getPersistentProperty(new QualifiedName(
										"org.mfg.mdbplugin", "selectedTable"));
						String selectedCol = file
								.getPersistentProperty(new QualifiedName(
										"org.mfg.mdbplugin", "selectedColumn"));
						if (selectedCol != null && selectedTable != null) {
							itable = Integer.parseInt(selectedTable);
							icol = Integer.parseInt(selectedCol);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Schema schema = getSchema();
				_tablesViewer.setInput(schema);
				if (!schema.isEmpty()) {
					itable = Math.min(schema.size() - 1, itable);
					_tablesViewer.getTable().setSelection(itable);
					org.mfg.mdb.compiler.Table table = schema.get(itable);
					_columnsViewer.setInput(table);
					_removeTableBtn.setEnabled(true);
					if (!table.isEmpty()) {
						icol = Math.min(table.size() - 1, icol);
						_columnsViewer.getTable().setSelection(icol);
						_removeColumnBtn.setEnabled(true);
					}
				}
			}
		});

	}

	public int getSelectedTable() {
		return _tablesViewer.getTable().getSelectionIndex();
	}

	public int getSelectedColumn() {
		return _columnsViewer.getTable().getSelectionIndex();
	}

	protected void addTable() {
		InputDialog dlg = new InputDialog(getSite().getShell(), "New Table",
				"Enter the table name", "Table" + (getSchema().size() + 1),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						for (org.mfg.mdb.compiler.Table t : getSchema()) {
							if (newText.equals(t.getName())) {
								return "That name is used by other table.";
							}
						}
						return null;
					}
				});
		if (dlg.open() == Window.OK) {
			org.mfg.mdb.compiler.Table table = new org.mfg.mdb.compiler.Table(
					dlg.getValue(), UUID.randomUUID());
			addTable(table);
		}
	}

	public void addTable(org.mfg.mdb.compiler.Table table) {
		getSchema().add(table);
		_tablesViewer.refresh();
		_tablesViewer.setSelection(new StructuredSelection(table));
		setDirty(true);
	}

	public Schema getSchema() {
		return getEditor().getSchema();
	}

	@Override
	public MDBSchemaEditor getEditor() {
		return (MDBSchemaEditor) super.getEditor();
	}

	@Override
	public void setFocus() {
		_tablesViewer.getTable().setFocus();
	}

	@Override
	public boolean isDirty() {
		return _dirty;
	}

	public void setDirty(boolean dirty) {
		if (this._dirty != dirty) {
			this._dirty = dirty;
			firePropertyChange(PROP_DIRTY);
		}
	}

}
