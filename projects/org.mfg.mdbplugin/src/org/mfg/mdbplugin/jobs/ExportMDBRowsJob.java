package org.mfg.mdbplugin.jobs;

import java.io.File;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.xmdb.XAppender;
import org.mfg.mdbplugin.xmdb.XCursor;

public class ExportMDBRowsJob extends Job {

	private File _inputFile;
	File _outputFile;
	private long _start;
	private long _stop;
	private Object[][] _selectedRows;

	public ExportMDBRowsJob(File inputFile, File outputFile, long start,
			long stop, Object[][] selectedRows) {
		super("Export rows to " + outputFile);
		this._inputFile = inputFile;
		this._outputFile = outputFile;
		this._start = start;
		this._stop = stop;
		this._selectedRows = selectedRows;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			monitor.beginTask("Export MDB rows", (int) (_stop - _start));

			Table table = MDBPlugin.getTableDef(_inputFile);

			XAppender app = new XAppender(_outputFile, table);

			if (_selectedRows == null) {
				XCursor cursor = new XCursor(table, _inputFile);
				cursor.position(_start);
				while (cursor.position() <= _stop && cursor.next()) {
					app.append(cursor.getRecord());
					monitor.worked(1);
				}
				cursor.close();
			} else {
				for (Object[] record : _selectedRows) {
					app.append(record);
					monitor.worked(1);
				}
			}
			app.close();

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MDBPlugin.refreshConnectionViews(_outputFile);
					MDBPlugin.openMDBEditor(_outputFile);
				}
			});

			monitor.done();

			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}
	}
}
