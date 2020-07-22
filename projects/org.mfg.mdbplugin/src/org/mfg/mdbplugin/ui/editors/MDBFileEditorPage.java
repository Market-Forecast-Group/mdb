package org.mfg.mdbplugin.ui.editors;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.wb.swt.ResourceManager;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Type;
import org.mfg.mdbplugin.ui.CopyStructuredSelectionAction;
import org.mfg.mdbplugin.ui.DoubleClickEditingSupport;

public class MDBFileEditorPage extends FormPage {
	private Table _table;
	TableViewer tableViewer;
	private Text fromText;
	private Text toText;
	private Label pageLabel;

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public MDBFileEditorPage(String id, String title) {
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
	public MDBFileEditorPage(FormEditor editor, String id, String title) {
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
		form.setText("Data");
		Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);

		initActions();
		managedForm.getForm().getBody().setLayout(new GridLayout(1, false));

		Composite composite_1 = managedForm.getToolkit().createComposite(
				managedForm.getForm().getBody(), SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		managedForm.getToolkit().paintBordersFor(composite_1);
		composite_1.setLayout(new GridLayout(8, false));

		Label lblStart = managedForm.getToolkit().createLabel(composite_1,
				"From", SWT.NONE);
		lblStart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblStart.setBounds(0, 0, 70, 17);

		fromText = managedForm.getToolkit().createText(composite_1, "New Text",
				SWT.NONE);
		GridData gd_fromText = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_fromText.widthHint = 80;
		fromText.setLayoutData(gd_fromText);
		fromText.setText("0");

		Label lblStop = managedForm.getToolkit().createLabel(composite_1, "To",
				SWT.NONE);

		toText = managedForm.getToolkit().createText(composite_1, "New Text",
				SWT.NONE);
		GridData gd_toText = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_toText.widthHint = 80;
		toText.setLayoutData(gd_toText);
		toText.setText("100");

		Button btnLoad = managedForm.getToolkit().createButton(composite_1,
				"Load", SWT.NONE);
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateTable(false);
			}
		});

		pageLabel = managedForm.getToolkit().createLabel(composite_1, "0/100",
				SWT.CENTER);
		pageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Button btnPrevPage = managedForm.getToolkit().createButton(composite_1,
				"Prev Page", SWT.NONE);
		btnPrevPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				loadPrevPage();
			}
		});
		btnPrevPage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		Button btnNextPage = managedForm.getToolkit().createButton(composite_1,
				"Next Page", SWT.NONE);
		btnNextPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				loadNextPage();
			}
		});
		btnNextPage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));

		Composite composite_2 = managedForm.getToolkit().createComposite(
				managedForm.getForm().getBody(), SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_2);
		composite_2.setLayout(new GridLayout(1, false));

		ImageHyperlink mghprlnkAddRow = managedForm.getToolkit()
				.createImageHyperlink(composite_2, SWT.NONE);
		mghprlnkAddRow.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				appendRecord();
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
		mghprlnkAddRow.setImage(ResourceManager.getPluginImage(
				"org.eclipse.ui", "/icons/full/obj16/add_obj.gif"));
		managedForm.getToolkit().paintBordersFor(mghprlnkAddRow);
		mghprlnkAddRow.setText("Append");

		Composite composite = managedForm.getToolkit().createComposite(
				managedForm.getForm().getBody(), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		composite.setLayout(new GridLayout(1, false));
		managedForm.getToolkit().paintBordersFor(composite);

		tableViewer = new TableViewer(composite, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.MULTI);
		_table = tableViewer.getTable();
		_table.setHeaderVisible(true);
		_table.setLinesVisible(true);
		_table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().paintBordersFor(_table);
		tableViewer.setContentProvider(new ArrayContentProvider());

		afterCreateWidgets();
	}

	void appendRecord() {
		try {
			getEditorInput().addRecord();

			long start = Long.parseLong(fromText.getText());
			long stop = Long.parseLong(toText.getText());
			long size = getEditorInput().size();
			long len = stop - start;
			start = size - len;
			stop = size - 1;
			if (start < 0) {
				start = 0;
				stop = start + len;
			}
			fromText.setText(start + "");
			toText.setText(stop + "");
			updateTable(false);
		} catch (IOException e) {
			handleError(e);
		}
	}

	private void initActions() {
		IToolBarManager manager = getManagedForm().getForm().getForm()
				.getToolBarManager();
		manager.add(new Action("Import...", ResourceManager
				.getPluginImageDescriptor("org.eclipse.ui",
						"/icons/full/etool16/import_wiz.gif")) {
			@Override
			public void run() {
				importWizard();
			}
		});
		manager.add(new Action("Export...", ResourceManager
				.getPluginImageDescriptor("org.eclipse.ui",
						"/icons/full/etool16/export_wiz.gif")) {
			@Override
			public void run() {
				exportWizard();
			}
		});
		manager.update(true);
		IActionBars actionBars = getEditorSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
				new CopyStructuredSelectionAction());
		actionBars.updateActionBars();
	}

	protected void exportWizard() {
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand("org.eclipse.ui.file.export", null);
		} catch (Exception e) {
			handleError(e);
		}
	}

	protected void importWizard() {
		IHandlerService handlerService = (IHandlerService) getSite()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand("org.eclipse.ui.file.import", null);
		} catch (Exception e) {
			handleError(e);
		}
	}

	protected void loadNextPage() {
		try {
			long start = Long.parseLong(fromText.getText());
			long stop = Long.parseLong(toText.getText());
			long len = stop - start;
			if (len < 0) {
				len = 0;
			}
			long size = getEditorInput().size();
			stop += len;
			start += len;
			if (start < size) {
				fromText.setText(start + "");
				toText.setText(stop + "");
				updateTable(false);
			}
		} catch (Exception e) {
			handleError(e);
		}
	}

	protected void loadPrevPage() {
		long start = Long.parseLong(fromText.getText());
		long stop = Long.parseLong(toText.getText());
		long len = stop - start;
		if (len < 0) {
			len = 0;
		}
		start -= len;
		stop -= len;
		if (start < 0) {
			start = 0;
			stop = len;
		}
		fromText.setText(start + "");
		toText.setText(stop + "");
		updateTable(false);
	}

	private void afterCreateWidgets() {
		getEditorSite().setSelectionProvider(tableViewer);

		org.mfg.mdb.compiler.Table table = getEditorInput().getTable();

		int i = 0;
		for (final Column col : table) {
			TableViewerColumn c = new TableViewerColumn(tableViewer, SWT.None);
			final int j = i;
			c.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Object value = ((Object[]) element)[j];
					if (value instanceof Object[]) {
						return super.getText(Arrays.toString((Object[]) value));
					}
					return super.getText(value);
				}
			});
			c.setEditingSupport(new DoubleClickEditingSupport(tableViewer) {

				@Override
				protected void setValue(Object element, Object strValue) {
					int colIndex = col.getIndex();
					Type colType = col.getType();
					Object value = colType.parseString((String) strValue);

					Object[] rec = (Object[]) element;
					@SuppressWarnings("boxing")
					int pos = (Integer) rec[rec.length - 1];
					try {
						MDBFileEditorInput input = getEditorInput();
						input.writeValue(pos, col, value);
						Object value2 = input.readValue(pos, colIndex);
						Assert.isTrue(value == value2 || value.equals(value2),
								"For some reason the DB was not updated. Possible bug.");
						rec[colIndex] = value;
						tableViewer.refresh(rec);
					} catch (IOException e) {
						handleError(e);
					}
				}

				@Override
				protected Object getValue(Object element) {
					Object val = ((Object[]) element)[col.getIndex()];
					return val == null ? "" : val.toString();
				}

				@Override
				protected CellEditor getCellEditor(Object element) {
					return new TextCellEditor(tableViewer.getTable());
				}

				@Override
				protected boolean canEdit(Object element) {
					boolean b = !col.getType().isArray();
					return b && super.canEdit(element);
				}
			});
			c.getColumn().setText(col.getName());
			i++;
		}

		updateTable(true);
	}

	public void updateTable(boolean pack) {
		long start = Long.parseLong(fromText.getText());
		long stop = Long.parseLong(toText.getText());
		if (stop >= start) {
			try {
				pageLabel.setText(start + "-" + stop + "/"
						+ getEditorInput().size());
				tableViewer.setInput(getEditorInput().readData(start, stop));
			} catch (IOException e) {
				handleError(e);
			}
		}
		if (pack) {
			for (TableColumn c : tableViewer.getTable().getColumns()) {
				c.pack();
			}
		}
	}

	@Override
	public MDBFileEditorInput getEditorInput() {
		return (MDBFileEditorInput) super.getEditorInput();
	}

	public Object[][] getSelectedRows() {
		StructuredSelection sel = (StructuredSelection) tableViewer
				.getSelection();
		if (sel.isEmpty()) {
			return null;
		}
		Object[] arr = sel.toArray();
		Object[][] res = new Object[arr.length][];
		for (int i = 0; i < arr.length; i++) {
			res[i] = (Object[]) arr[i];
		}
		return res;
	}

	void handleError(Exception e) {
		e.printStackTrace();
		MessageDialog.openConfirm(getEditorSite().getShell(), "Error", e
				.getClass().getSimpleName() + ": " + e.getMessage());
	}
}
