package org.mfg.mdbplugin.jobs;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.Type;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.wizards.ColumnsLinkingWizardPage.ColumnLink;
import org.mfg.mdbplugin.xmdb.XAppender;

import com.csvreader.CsvReader;

public class ImportCSVJob extends Job {

	private File _input;
	private Table _table;
	private List<ColumnLink> _links;
	File _output;

	public ImportCSVJob(File input, Table table, List<ColumnLink> links,
			File output) {
		super("Import CSV file " + input.getName());
		this._input = input;
		this._table = table;
		this._links = links;
		this._output = output;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			_output.createNewFile();

			XAppender app = new XAppender(_output, _table);

			try (FileReader fileReader = new FileReader(_input)) {
				CsvReader reader = new CsvReader(fileReader);
				reader.readHeaders();
				Object[] mdbRec = new Object[_table.size()];
				while (reader.readRecord()) {
					for (int i = 0; i < _table.size(); i++) {
						Column mdbCol = _table.get(i);
						if (!mdbCol.isVirtual()) {
							String csvCol = findCsvColumnFor(mdbCol);
							String csvValue = reader.get(csvCol);
							Type type = mdbCol.getType();
							if (type.isArray() && type != Type.STRING) {
								Type elemType = type.getElementType();
								if (csvValue.trim().equals("[]")) {
									mdbRec[i] = new Object[0];
								} else {
									String[] arr = csvValue.substring(1,
											csvValue.length() - 1).split(",");
									Object[] mdbArr = new Object[arr.length];
									for (int j = 0; j < arr.length; j++) {
										String csvElem = arr[j].trim();
										Object elem = computeSimpleMdbValue(
												elemType, csvElem);
										mdbArr[j] = elem;
									}
									mdbRec[i] = mdbArr;
								}

							} else {
								mdbRec[i] = computeSimpleMdbValue(type,
										csvValue);
							}
						}
					}
					app.append(mdbRec);
				}
				app.close();
				reader.close();
			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MDBPlugin.openMDBEditor(_output);
				}
			});
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		} finally {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MDBPlugin.refreshConnectionViews(_output);
				}
			});
		}
	}

	@SuppressWarnings("boxing")
	private static Object computeSimpleMdbValue(Type type, String csvValue) {
		Object mdbValue = 0;
		switch (type) {
		case BOOLEAN:
			mdbValue = Boolean.parseBoolean(csvValue);
			break;
		case BYTE:
			mdbValue = Byte.parseByte(csvValue);
			break;
		case DOUBLE:
			mdbValue = Double.parseDouble(csvValue);
			break;
		case FLOAT:
			mdbValue = Float.parseFloat(csvValue);
			break;
		case INTEGER:
			mdbValue = Integer.parseInt(csvValue);
			break;
		case LONG:
			mdbValue = Long.parseLong(csvValue);
			break;
		case SHORT:
			mdbValue = Short.parseShort(csvValue);
			break;
		case STRING:
			mdbValue = csvValue == ColumnLink.DEFAULT_VALUE ? "" : csvValue;
			break;
		case ARRAY_BOOLEAN:
		case ARRAY_BYTE:
		case ARRAY_DOUBLE:
		case ARRAY_FLOAT:
		case ARRAY_INTEGER:
		case ARRAY_LONG:
		case ARRAY_SHORT:
			// arrays are not supported yet
			mdbValue = null;
			break;
		}
		return mdbValue;
	}

	private String findCsvColumnFor(Column mdbCol) {
		for (ColumnLink link : _links) {
			if (mdbCol.getUUID().equals(link.mdbColumn.getUUID())) {
				return link.csvColumn;
			}
		}
		throw new RuntimeException("Something wrong! There is not column "
				+ mdbCol.getName() + " in the MDB/CSV columns linking.");
	}
}
