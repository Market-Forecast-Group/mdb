package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Type;

import com.csvreader.CsvReader;

public class InputCSVFile_Schema_WizardPage extends WizardPage {
	private Text fileText;
	private Table table;
	private CheckboxTableViewer viewer;

	/**
	 * Create the wizard.
	 */
	public InputCSVFile_Schema_WizardPage() {
		super("wizardPage");
		setTitle("CSV File");
		setDescription("Select CSV file.");
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

		fileText = new Text(container, SWT.BORDER);
		fileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				textModified();
			}
		});
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				browseFile();
			}
		});
		btnNewButton.setText("Browse...");

		table = new Table(container, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		viewer = new CheckboxTableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
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
		TableColumn tblclmnColumn = tableViewerColumn.getColumn();
		tblclmnColumn.setWidth(184);
		tblclmnColumn.setText("Column");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(viewer,
				SWT.NONE);
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
		TableColumn tblclmnInferredType = tableViewerColumn_1.getColumn();
		tblclmnInferredType.setWidth(100);
		tblclmnInferredType.setText("Inferred Type");
		viewer.setContentProvider(new ArrayContentProvider());

		textModified();
	}

	public List<Column> getSelectedColumns() {
		List<Column> list = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Column> input = (List<Column>) viewer.getInput();
		for (Column c : input) {
			if (viewer.getChecked(c)) {
				list.add(c);
			}
		}
		return list;
	}

	void textModified() {
		setErrorMessage(null);
		String path = fileText.getText();
		File f = new File(path);
		if (f.exists()) {
			setSelectedFile(f);
		} else {
			setErrorMessage("Selected file does not exist.");
		}
	}

	private void setSelectedFile(File file) {
		setErrorMessage(null);

		List<Column> list = new ArrayList<>();

		try (FileReader fileReader = new FileReader(file)) {
			CsvReader reader = new CsvReader(fileReader);
			reader.readHeaders();
			String[] headers = reader.getHeaders();

			String[] record = null;
			if (reader.readRecord()) {
				record = reader.getValues();
			}

			for (int i = 0; i < headers.length; i++) {
				String name = headers[i];
				Type type = Type.LONG;
				if (record != null) {
					type = inferType(record[i]);
				}
				Column col = new Column(name, type, Order.NONE, false, "", 0,
						UUID.randomUUID());
				list.add(col);
			}

			reader.close();

			viewer.setInput(list);
			viewer.setCheckedElements(list.toArray());

		} catch (IOException e) {
			e.printStackTrace();
			setErrorMessage(e.getMessage());
		}
	}

	private static Type inferType(String str) {
		try {
			Long.parseLong(str);
			return Type.LONG;
		} catch (Exception e) {
			// nothing
		}

		try {
			Double.parseDouble(str);
			return Type.DOUBLE;
		} catch (Exception e) {
			// nothing
		}

		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return Type.BOOLEAN;
		}

		// TODO: missing arrays.

		return Type.STRING;
	}

	protected void browseFile() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterExtensions(new String[] { "*.csv" });
		String path = dialog.open();
		if (path != null) {
			fileText.setText(path);
		}
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
