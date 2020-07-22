package org.mfg.mdbplugin.ui.editors;

import java.util.Arrays;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdbplugin.jobs.DiffComputer.DiffRecord;

public class DiffEditorPage extends FormPage {
	private Text leftText;
	private Text rightText;
	private TableViewer rightViewer;
	private TableViewer leftViewer;

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public DiffEditorPage(String id, String title) {
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
	public DiffEditorPage(FormEditor editor, String id, String title) {
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
		form.setText("MDB Diff");
		Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		leftText = managedForm.getToolkit().createText(
				managedForm.getForm().getBody(), "New Text", SWT.READ_ONLY);
		leftText.setText("");
		leftText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		rightText = managedForm.getToolkit().createText(
				managedForm.getForm().getBody(), "New Text", SWT.READ_ONLY);
		rightText.setText("");
		rightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		leftViewer = new TableViewer(managedForm.getForm().getBody(),
				SWT.BORDER | SWT.FULL_SELECTION);
		Table table = leftViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().paintBordersFor(table);
		leftViewer.setContentProvider(new ArrayContentProvider());

		rightViewer = new TableViewer(managedForm.getForm().getBody(),
				SWT.BORDER | SWT.FULL_SELECTION);
		Table table_1 = rightViewer.getTable();
		table_1.setLinesVisible(true);
		table_1.setHeaderVisible(true);
		table_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().paintBordersFor(table_1);
		rightViewer.setContentProvider(new ArrayContentProvider());

		afterCreateWidgets();
	}

	private void afterCreateWidgets() {
		org.mfg.mdb.compiler.Table table = getEditorInput().getTable();

		// left table

		TableViewerColumn c = new TableViewerColumn(leftViewer, SWT.None);
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DiffRecord rec = (DiffRecord) element;
				if (rec._left == null) {
					return "";
				}
				return getRecordType(rec);
			}

			@Override
			public Color getBackground(Object element) {
				return getRecordBg(element, super.getBackground(element));
			}

			@Override
			public Color getForeground(Object element) {
				return getRecordFg(element, super.getForeground(element));
			}
		});
		c.getColumn().setText("M");

		int i = 0;
		for (Column col : table) {
			c = new TableViewerColumn(leftViewer, SWT.None);
			final int j = i;
			c.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Object e2 = ((DiffRecord) element)._left;
					return getRecordText(j, e2);
				}

				@Override
				public Color getBackground(Object element) {
					return getRecordBg(element, super.getBackground(element));
				}

				@Override
				public Color getForeground(Object element) {
					return getRecordFg(element, super.getForeground(element));
				}
			});
			c.getColumn().setText(col.getName());
			i++;
		}

		// right table

		c = new TableViewerColumn(rightViewer, SWT.None);
		c.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DiffRecord rec = (DiffRecord) element;
				if (rec._right == null) {
					return "";
				}
				return getRecordType(rec);
			}

			@Override
			public Color getBackground(Object element) {
				return getRecordBg(element, super.getBackground(element));
			}

			@Override
			public Color getForeground(Object element) {
				return getRecordFg(element, super.getForeground(element));
			}
		});
		c.getColumn().setText("M");

		i = 0;
		for (Column col : table) {
			c = new TableViewerColumn(rightViewer, SWT.None);
			final int j = i;
			c.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					Object e2 = ((DiffRecord) element)._right;
					return getRecordText(j, e2);
				}

				@Override
				public Color getBackground(Object element) {
					return getRecordBg(element, super.getBackground(element));
				}
			});
			c.getColumn().setText(col.getName());
			i++;
		}

		for (TableViewer viewer : new TableViewer[] { leftViewer, rightViewer }) {
			for (TableColumn col : viewer.getTable().getColumns()) {
				col.pack();
			}
			viewer.setInput(getEditorInput().getDiff());
		}

		leftText.setText(getEditorInput().getLeftFile().getAbsolutePath());
		rightText.setText(getEditorInput().getRightFile().getAbsolutePath());

	}

	protected static String getRecordType(DiffRecord rec) {
		switch (rec._type) {
		case DiffRecord.ADDED:
			return "+";
		case DiffRecord.CHANGED:
			return "*";
		case DiffRecord.DELETED:
			return "-";
		case DiffRecord.EQUAL:
			return "=";
		default:
			break;
		}
		return null;
	}

	@Override
	public DiffEditorInput getEditorInput() {
		return (DiffEditorInput) super.getEditorInput();
	}

	static String getRecordText(final int j, Object element) {
		if (element == null) {
			return null;
		}

		Object value = ((Object[]) element)[j];
		if (value instanceof Object[]) {
			return Arrays.toString((Object[]) value);
		}

		return value == null ? "" : value.toString();
	}

	static Color getRecordBg(Object element, Color def) {
		DiffRecord rec = (DiffRecord) element;
		return rec._type == DiffRecord.EQUAL ? def : Display.getDefault()
				.getSystemColor(SWT.COLOR_RED);
	}

	protected static Color getRecordFg(Object element, Color def) {
		DiffRecord rec = (DiffRecord) element;
		return rec._type == DiffRecord.EQUAL ? def : Display.getDefault()
				.getSystemColor(SWT.COLOR_WHITE);
	}

}
