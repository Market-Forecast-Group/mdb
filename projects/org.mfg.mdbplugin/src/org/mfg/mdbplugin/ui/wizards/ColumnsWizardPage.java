package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdbplugin.MDBPlugin;

public class ColumnsWizardPage extends WizardPage {

	private File _file;
	private CheckboxTableViewer _viewer;
	private org.mfg.mdb.compiler.Table tableDef;

	/**
	 * Create the wizard.
	 */
	public ColumnsWizardPage() {
		super("wizardPage");
		setTitle("Columns");
		setDescription("Check the columns to export.");
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

		_viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER
				| SWT.FULL_SELECTION);
		Table table = _viewer.getTable();
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(_viewer,
				SWT.NONE);
		tableViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				return ((Column) element).getName();
			}
		});
		TableColumn tblclmnColumn = tableViewerColumn_1.getColumn();
		tblclmnColumn.setWidth(100);
		tblclmnColumn.setText("Column");
		_viewer.setContentProvider(new ArrayContentProvider());

		aftwerCreateWidgets();
	}

	private void aftwerCreateWidgets() {
		if (_file != null) {
			updateViewer();
		}
	}

	public void setFile(File file) {
		this._file = file;
		if (_viewer != null) {
			updateViewer();
		}
	}

	private void updateViewer() {
		try {
			tableDef = MDBPlugin.getTableDef(_file);
			List<Column> list = new ArrayList<>();
			for (Column c : tableDef) {
				if (!c.isVirtual()) {
					list.add(c);
				}
			}
			_viewer.setInput(list);
			_viewer.setAllChecked(true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<Column> getSelectedColumns() {
		List<Column> list = new ArrayList<>();
		for (Column c : tableDef) {
			if (_viewer.getChecked(c)) {
				list.add(c);
			}
		}
		return list;
	}

}
