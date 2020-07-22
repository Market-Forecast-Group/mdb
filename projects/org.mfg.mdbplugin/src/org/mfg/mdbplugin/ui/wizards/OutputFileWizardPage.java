package org.mfg.mdbplugin.ui.wizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OutputFileWizardPage extends WizardPage {
	private Text fileText;
	private File _sourceFile;
	private File selectedFile;
	private File selectedArrayFile;

	/**
	 * Create the wizard.
	 */
	public OutputFileWizardPage() {
		super("wizardPage");
		setTitle("Ouput File");
		setDescription("Select output file.");
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

		Label lblOutputFile = new Label(container, SWT.NONE);
		lblOutputFile.setText("Output file name:");

		Composite composite = new Composite(container, SWT.BORDER);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		composite.setLayout(new GridLayout(1, false));

		fileText = new Text(composite, SWT.BORDER);
		fileText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				textModified();
			}
		});
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		afterCreateWidgets();
	}

	protected void textModified() {
		setErrorMessage(null);
		if (fileText.getText().trim().length() > 0) {
			selectedFile = new File(_sourceFile.getParentFile(),
					fileText.getText());
			validate();
		} else {
			setErrorMessage("Invalid file name.");
		}
	}

	private void afterCreateWidgets() {
		setErrorMessage(null);
		if (selectedFile != null) {
			setSelectedFile(selectedFile);
		}
	}

	public void setSourceFile(File sourceFile) {
		this._sourceFile = sourceFile;
		String name = sourceFile.getName();
		String[] split = name.split("\\.");
		String ext = name.contains(".") ? split[split.length - 1] : "";
		String head = name.substring(0, name.length() - ext.length() - 1);
		File f = new File(sourceFile.getParentFile(), head + " (Exported).mdb");
		int i = 1;
		while (f.exists()) {
			f = new File(sourceFile.getParentFile(), head + " (Exported " + i
					+ ").mdb");
			i++;
		}
		setSelectedFile(f);
	}

	private void setSelectedFile(File f) {
		selectedFile = f;
		if (f.getAbsolutePath().endsWith(".mdb")) {
			selectedArrayFile = new File(f.getAbsolutePath().replace(".mdb",
					".amdb"));
		} else {
			selectedArrayFile = new File(f.getAbsolutePath() + ".amdb");
		}

		setErrorMessage(null);
		validate();
		if (fileText != null) {
			fileText.setText(selectedFile.getName());
		}
	}

	private void validate() {
		if (selectedFile.exists()) {
			setErrorMessage("Output file exists.");
		}
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public File getSelectedArrayFile() {
		return selectedArrayFile;
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}
}
