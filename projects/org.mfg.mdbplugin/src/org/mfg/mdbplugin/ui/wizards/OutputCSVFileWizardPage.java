package org.mfg.mdbplugin.ui.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;

public class OutputCSVFileWizardPage extends WizardPage {
	private Text fileText;
	private File selectedFile;

	/**
	 * Create the wizard.
	 */
	public OutputCSVFileWizardPage() {
		super("wizardPage");
		setTitle("Output File");
		setDescription("Selected output file.");
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

		Label lblCsvFileName = new Label(container, SWT.NONE);
		lblCsvFileName.setText("CSV file path:");
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

		textModified();
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	void browseFile() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setFilterExtensions(new String[] { "*.csv" });
		dialog.setOverwrite(false);
		String path = dialog.open();
		if (path != null) {
			if (!path.toLowerCase().endsWith(".csv")) {
				path = path + ".csv";
			}
			fileText.setText(path);
		}
	}

	void textModified() {
		setErrorMessage(null);
		if (fileText.getText().trim().length() > 0) {
			selectedFile = new File(fileText.getText());
			validate();
		} else {
			setErrorMessage("Invalid file name.");
		}
	}

	private void validate() {
		if (selectedFile.getParentFile() != null
				&& !selectedFile.getParentFile().exists()) {
			setErrorMessage("Invalid location, parent file does not exist.");
		}
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
