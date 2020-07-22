package org.mfg.mdbplugin.jobs;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.Type;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.xmdb.XCursor;

import com.csvreader.CsvWriter;

public class ExportCSVJob extends Job {

	private File _inputFile;
	private File _outputFile;
	private long _start;
	private long _stop;
	private Object[][] _selectedRows;
	private List<Column> _columns;

	public ExportCSVJob(File inputFile, File outputFile, long start, long stop,
			Object[][] selectedRows, List<Column> columns) {
		super("Export rows to " + outputFile);
		_inputFile = inputFile;
		_outputFile = outputFile;
		_start = start;
		_stop = stop;
		_selectedRows = selectedRows;
		_columns = columns;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask("Export CSV file", (int) (_stop - _start));

			Table table = MDBPlugin.getTableDef(_inputFile);

			CsvWriter writer = new CsvWriter(this._outputFile.getAbsolutePath());

			for (Column c : _columns) {
				writer.write(c.getName());
			}
			writer.endRecord();

			if (_selectedRows == null) {
				XCursor cursor = new XCursor(table, _inputFile);
				cursor.position(_start);
				while (cursor.position() <= _stop && cursor.next()) {
					Object[] record = cursor.getRecord();
					for (int i = 0; i < record.length; i++) {
						Column col = table.get(i);
						Type type = col.getType();
						if (acceptCol(col)) {
							String str;
							Object value = record[i];
							if (type.isArray() && type != Type.STRING) {
								str = Arrays.toString((Object[]) value);
							} else {
								str = value.toString();
							}
							writer.write(str);
							monitor.worked(1);
						}
					}
					writer.endRecord();
				}
				writer.close();
				cursor.close();
			} else {
				for (Object[] record : _selectedRows) {
					for (int i = 0; i < record.length; i++) {
						Column col = table.get(i);
						if (acceptCol(col)) {
							writer.write(record[i].toString());
							monitor.worked(1);
						}
					}
					writer.endRecord();
				}
			}
			writer.close();
			monitor.done();
			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}
	}

	private boolean acceptCol(Column col) {
		if (col.isVirtual()) {
			return false;
		}
		for (Column c : _columns) {
			if (c.getName().equals(col.getName())) {
				return true;
			}
		}
		return false;
	}
}
