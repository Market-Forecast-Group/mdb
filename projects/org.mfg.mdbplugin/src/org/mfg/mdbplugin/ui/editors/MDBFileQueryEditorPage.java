package org.mfg.mdbplugin.ui.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
import org.eclipse.wb.swt.ResourceManager;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdbplugin.xmdb.XQuery;
import org.mfg.mdbplugin.xmdb.XQuery.SelectColumn;

public class MDBFileQueryEditorPage extends FormPage {
	private Table _table_1;
	CheckboxTableViewer _columnsViewer;
	ArrayList<SelectColumn> _select;
	TableViewer _resultViewer;
	private ImageHyperlink _btnRemove;
	private StyledText _filterText;
	private Button _filterCheck;
	Button _btnExecute;

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public MDBFileQueryEditorPage(String id, String title) {
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
	public MDBFileQueryEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 * 
	 * @param managedForm
	 */
	@SuppressWarnings("unused")
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.setText("Query");
		Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		_filterCheck = managedForm.getToolkit().createButton(
				managedForm.getForm().getBody(), "Filter", SWT.CHECK);
		_filterCheck.setSelection(true);

		Label lblColumns = managedForm.getToolkit().createLabel(
				managedForm.getForm().getBody(), "Columns", SWT.NONE);

		_filterText = new StyledText(managedForm.getForm().getBody(),
				SWT.BORDER);
		GridData gd_filterText = new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1);
		gd_filterText.heightHint = 150;
		_filterText.setLayoutData(gd_filterText);
		managedForm.getToolkit().adapt(_filterText);
		managedForm.getToolkit().paintBordersFor(_filterText);

		Composite composite = new Composite(managedForm.getForm().getBody(),
				SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 3));
		managedForm.getToolkit().adapt(composite);
		managedForm.getToolkit().paintBordersFor(composite);

		final Table table = new Table(composite, SWT.BORDER | SWT.SINGLE
				| SWT.CHECK);
		_columnsViewer = new CheckboxTableViewer(table);
		_columnsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						columnsSelectionChanged();
					}
				});
		GridData gd_table = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_table.widthHint = 350;
		table.setLayoutData(gd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		managedForm.getToolkit().paintBordersFor(table);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn.setEditingSupport(new EditingSupport(_columnsViewer) {

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(table);
			}

			@Override
			protected Object getValue(Object element) {
				return ((SelectColumn) element).name;
			}

			@Override
			protected void setValue(Object element, Object value) {
				((SelectColumn) element).name = (String) value;
				_columnsViewer.refresh(element);
			}
		});
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((SelectColumn) element).name;
			}
		});
		TableColumn tblclmnName = tableViewerColumn.getColumn();
		tblclmnName.setWidth(100);
		tblclmnName.setText("Name");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				_columnsViewer, SWT.NONE);
		tableViewerColumn_1
				.setEditingSupport(new EditingSupport(_columnsViewer) {

					@Override
					protected boolean canEdit(Object element) {
						return true;
					}

					@Override
					protected CellEditor getCellEditor(Object element) {
						return new TextCellEditor(table);
					}

					@Override
					protected Object getValue(Object element) {
						return ((SelectColumn) element).formula;
					}

					@Override
					protected void setValue(Object element, Object value) {
						((SelectColumn) element).formula = (String) value;
						_columnsViewer.refresh(element);
					}
				});
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((SelectColumn) element).formula;
			}
		});
		TableColumn tblclmnFormula = tableViewerColumn_1.getColumn();
		tblclmnFormula.setWidth(100);
		tblclmnFormula.setText("Formula");
		_columnsViewer.setContentProvider(new ArrayContentProvider());

		Composite composite_1 = managedForm.getToolkit().createComposite(
				composite, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_1);

		ImageHyperlink mghprlnkAdd = managedForm.getToolkit()
				.createImageHyperlink(composite_1, SWT.NONE);
		mghprlnkAdd.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				addSelectColumn();
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
		mghprlnkAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		mghprlnkAdd.setImage(ResourceManager.getPluginImage("org.eclipse.ui",
				"/icons/full/obj16/add_obj.gif"));
		managedForm.getToolkit().paintBordersFor(mghprlnkAdd);
		mghprlnkAdd.setText("Add");

		_btnRemove = managedForm.getToolkit().createImageHyperlink(composite_1,
				SWT.NONE);
		_btnRemove.setEnabled(false);
		_btnRemove.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				removeSelectColumn();
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
		_btnRemove.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		_btnRemove.setImage(ResourceManager.getPluginImage("org.eclipse.ui",
				"/icons/full/obj16/delete_obj.gif"));
		managedForm.getToolkit().paintBordersFor(_btnRemove);
		_btnRemove.setText("Remove");

		_btnExecute = new Button(managedForm.getForm().getBody(), SWT.NONE);
		_btnExecute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				executeQuery();
			}
		});
		_btnExecute.setText("Execute");

		_resultViewer = new TableViewer(managedForm.getForm().getBody(),
				SWT.BORDER | SWT.FULL_SELECTION);
		_table_1 = _resultViewer.getTable();
		_table_1.setLinesVisible(true);
		_table_1.setHeaderVisible(true);
		_table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		managedForm.getToolkit().paintBordersFor(_table_1);
		_resultViewer.setContentProvider(new ArrayContentProvider());

		afterCreateWidgets();
	}

	protected void columnsSelectionChanged() {
		_btnRemove.setEnabled(!_columnsViewer.getSelection().isEmpty());
	}

	protected void removeSelectColumn() {
		StructuredSelection sel = (StructuredSelection) _columnsViewer
				.getSelection();
		if (!sel.isEmpty()) {
			_select.remove(sel.getFirstElement());
			_columnsViewer.refresh();
		}
	}

	protected void addSelectColumn() {
		SelectColumn c = new SelectColumn();
		c.name = "col" + _select.size();
		c.formula = "$pos$";
		_select.add(c);
		_columnsViewer.refresh();
		_columnsViewer.setChecked(c, true);
		_columnsViewer.setSelection(new StructuredSelection(c));
	}

	protected void executeQuery() {
		_btnExecute.setEnabled(false);
		List<String> formulas = new ArrayList<>();

		for (TableColumn col : _resultViewer.getTable().getColumns()) {
			col.dispose();
		}

		int i = 0;
		for (SelectColumn scol : _select) {
			if (_columnsViewer.getChecked(scol)) {
				formulas.add(scol.formula);
				TableViewerColumn c = new TableViewerColumn(_resultViewer,
						SWT.None);
				final int j = i;
				c.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Object value = ((Object[]) element)[j];
						if (value instanceof Object[]) {
							return super.getText(Arrays
									.toString((Object[]) value));
						}
						return super.getText(value);
					}
				});
				c.getColumn().setText(scol.name);
				c.getColumn().pack();
			}
			i++;
		}

		final String filter = _filterText.getText();
		final boolean filterEnabled = _filterCheck.getSelection();

		Job job = new Job("Query") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				List<Object[]> data = Collections.emptyList();

				try {
					XQuery query = new XQuery(getEditorInput().createCursor(),
							_select, filterEnabled
									&& filter.trim().length() > 0 ? filter
									: null);
					query.setLimit(100);
					data = query.selectAll();
					query.close();
				} catch (Exception e) {
					status = ValidationStatus.error(e.getMessage(), e);
				}

				final List<Object[]> data2 = data;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						_resultViewer.setInput(data2);
						_btnExecute.setEnabled(true);
					}
				});

				return status;
			}
		};
		job.schedule();

	}

	private void afterCreateWidgets() {
		org.mfg.mdb.compiler.Table table = getEditorInput().getTable();

		_select = new ArrayList<>();

		for (Column col : table) {
			SelectColumn scol = new SelectColumn();
			scol.name = col.getName();
			if (col.isVirtual()) {
				scol.formula = col.getFormula();
			} else {

				scol.formula = "$$." + scol.name;
			}
			_select.add(scol);
		}

		_columnsViewer.setInput(_select);
		_columnsViewer.setAllChecked(true);
	}

	@Override
	public MDBFileEditorInput getEditorInput() {
		return (MDBFileEditorInput) super.getEditorInput();
	}
}
