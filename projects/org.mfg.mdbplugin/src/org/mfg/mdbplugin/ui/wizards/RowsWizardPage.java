package org.mfg.mdbplugin.ui.wizards;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.xmdb.XCursor;

public class RowsWizardPage extends WizardPage {
	@SuppressWarnings("unused")
	private DataBindingContext m_bindingContext;
	private Text text;
	private Text text_1;

	private long _start;
	private long _stop;
	long fileLen;
	private RowsWizardPage self = this;
	private File _selectedFile;
	private Object[][] _selection;

	/**
	 * Create the wizard.
	 */
	public RowsWizardPage(Object[][] selection) {
		super("wizardPage");
		setTitle("Select Rows Range");
		setDescription("Select the range of rows to export. Indices are inclusive.");
		this._selection = selection;
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

		headerLabel = new Label(container, SWT.NONE);
		headerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		headerLabel.setText("File ... has .. number of rows");

		btnChooseExportRange = new Button(container, SWT.RADIO);
		btnChooseExportRange.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		btnChooseExportRange.setText("Choose the export range");

		Label lblStartIndex = new Label(container, SWT.NONE);
		lblStartIndex.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblStartIndex.setText("From");

		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblStopIndex = new Label(container, SWT.NONE);
		lblStopIndex.setText("To");

		text_1 = new Text(container, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false,
				2, 1));

		btnSelectAll = new Button(composite, SWT.NONE);
		btnSelectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStart(0);
				setStop(fileLen - 1);
			}
		});
		btnSelectAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnSelectAll.setText("Select All");

		btnSelectFromBeginning = new Button(composite, SWT.NONE);
		btnSelectFromBeginning.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStart(fileLen / 2);
			}
		});
		btnSelectFromBeginning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		btnSelectFromBeginning.setText("Select From Middle");

		btnSelectToThe = new Button(composite, SWT.NONE);
		btnSelectToThe.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setStop(fileLen / 2);
			}
		});
		btnSelectToThe.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		btnSelectToThe.setText("Select To the Middle");

		btnExportSelectedRows = new Button(container, SWT.RADIO);
		btnExportSelectedRows.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 2, 1));
		btnExportSelectedRows.setText("Export selected rows in active editor");

		afterCreateWidgets();
		m_bindingContext = initDataBindings();
	}

	public void setFile(File file) {
		_selectedFile = file;
		if (headerLabel != null) {
			try {
				Table table = MDBPlugin.getTableDef(file);
				if (table == null) {
					setErrorMessage("File " + file.getName() + " is not valid");
				} else {
					setErrorMessage(null);
					XCursor cursor = new XCursor(table, file);
					fileLen = cursor.size();
					headerLabel.setText("File \"" + file.getName() + "\" has "
							+ fileLen + " rows.");
					cursor.close();
					setStart(0);
					setStop(fileLen - 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				setErrorMessage("File " + file.getName()
						+ " is not a valid MDB file.");
			}
		}
	}

	@Override
	public void setErrorMessage(String newMessage) {
		super.setErrorMessage(newMessage);
		setPageComplete(newMessage == null);
	}

	protected void afterCreateWidgets() {
		if (_selectedFile != null) {
			setFile(_selectedFile);
		}
		btnChooseExportRange.setSelection(_selection == null);
		btnExportSelectedRows
				.setSelection(!btnChooseExportRange.getSelection());
	}

	public long getStart() {
		return _start;
	}

	public void setStart(long start) {
		if (getErrorMessage() != null && getErrorMessage().startsWith("Start")) {
			setErrorMessage(null);
		}
		if (start < 0) {
			setErrorMessage("Start position should be >= 0.");
		}
		if (start >= fileLen) {
			setErrorMessage("Start position should be < " + fileLen + ".");
		}
		if (start > _stop) {
			setErrorMessage("Start position should be lower or equal than stop position.");
		}
		this._start = start;
		firePropertyChange("start");
	}

	public long getStop() {
		return _stop;
	}

	public void setStop(long stop) {
		if (getErrorMessage() != null && getErrorMessage().startsWith("Stop")) {
			setErrorMessage(null);
		}
		if (stop < 0) {
			setErrorMessage("Stop position should be >= 0.");
		}
		if (stop >= fileLen) {
			setErrorMessage("Stop position should be < " + fileLen + ".");
		}
		if (stop < _start) {
			setErrorMessage("Stop position should be bigger or equal than start position.");
		}
		_stop = stop;
		firePropertyChange("stop");
	}

	private transient final PropertyChangeSupport support = new PropertyChangeSupport(
			this);
	private Label headerLabel;
	private Composite composite;
	private Button btnSelectAll;
	private Button btnSelectFromBeginning;
	private Button btnSelectToThe;
	private Button btnChooseExportRange;
	private Button btnExportSelectedRows;

	public void addPropertyChangeListener(PropertyChangeListener l) {
		support.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		support.removePropertyChangeListener(l);
	}

	public void addPropertyChangeListener(String property,
			PropertyChangeListener l) {
		support.addPropertyChangeListener(property, l);
	}

	public void removePropertyChangeListener(String property,
			PropertyChangeListener l) {
		support.removePropertyChangeListener(property, l);
	}

	public void firePropertyChange(String property) {
		support.firePropertyChange(property, true, false);
	}

	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue observeTextTextObserveWidget = WidgetProperties.text(
				SWT.Modify).observe(text);
		IObservableValue startSelfObserveValue = BeanProperties.value("start")
				.observe(self);
		bindingContext.bindValue(observeTextTextObserveWidget,
				startSelfObserveValue, null, null);
		//
		IObservableValue observeTextText_1ObserveWidget = WidgetProperties
				.text(SWT.Modify).observe(text_1);
		IObservableValue stopSelfObserveValue = BeanProperties.value("stop")
				.observe(self);
		bindingContext.bindValue(observeTextText_1ObserveWidget,
				stopSelfObserveValue, null, null);
		//
		return bindingContext;
	}
}
