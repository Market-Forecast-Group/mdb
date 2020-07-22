package org.mfg.mdbplugin.ui.wizards;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.csvreader.CsvReader;

public class InputCSVFileWizardPage extends WizardPage {
	private Text fileText;
	private Table table;
	private File selectedFile;
	private TableViewer viewer;
	private String[] _headers;

	/**
	 * Create the wizard.
	 */
	public InputCSVFileWizardPage() {
		super("wizardPage");
		setTitle("CSV File");
		setDescription("Select CSV file to import.");
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(2, false));

		Label lblCsvFile = new Label(container, SWT.NONE);
		lblCsvFile.setText("CSV file path:");
		new Label(container, SWT.NONE);

		fileText = new Text(container, SWT.BORDER);
		fileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				textModified();
			}
		});
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				browseFile();
			}
		});
		btnBrowse.setText("Browse...");

		Label lblShortPreview = new Label(container, SWT.NONE);
		lblShortPreview.setText("Short preview:");
		new Label(container, SWT.NONE);

		Composite grpPreview = new Composite(container, SWT.NONE);
		grpPreview.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_grpPreview = new GridData(SWT.FILL, SWT.FILL, false, true,
				2, 1);
		gd_grpPreview.heightHint = 100;
		grpPreview.setLayoutData(gd_grpPreview);

		viewer = new TableViewer(grpPreview, SWT.BORDER | SWT.FULL_SELECTION);
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());

		textModified();
	}

	public File getSelectedFile() {
		return selectedFile;
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

	protected void setSelectedFile(File file) {
		setErrorMessage(null);
		selectedFile = file;
		try (FileReader fileReader = new FileReader(file);) {
			CsvReader reader = new CsvReader(fileReader);
			reader.readHeaders();
			setHeaders(reader.getHeaders());
			for (TableColumn col : viewer.getTable().getColumns()) {
				col.dispose();
			}
			for (int i = 0; i < _headers.length; i++) {
				TableViewerColumn col = new TableViewerColumn(viewer, SWT.None);
				String name = _headers[i];
				col.getColumn().setText(name);
				final int n = i;
				col.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						String[] r = (String[]) element;
						return super.getText(r[n]);
					}
				});
			}

			List<String[]> list = new ArrayList<>();
			for (int i = 0; i < 50 && reader.readRecord(); i++) {
				list.add(reader.getValues());
			}

			viewer.setInput(list);
			reader.close();
			viewer.getTable().setRedraw(false);
			for (TableColumn col : viewer.getTable().getColumns()) {
				col.pack();
				col.setWidth(col.getWidth() + 10);
			}
			viewer.getTable().setRedraw(true);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			setErrorMessage(e.getMessage());
		}
	}

	protected void setHeaders(String[] headers) {
		this._headers = headers;
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
