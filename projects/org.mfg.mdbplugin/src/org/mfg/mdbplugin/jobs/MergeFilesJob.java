package org.mfg.mdbplugin.jobs;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Order;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.xmdb.XAppender;
import org.mfg.mdbplugin.xmdb.XCursor;

public class MergeFilesJob extends Job {

	private Table _table;
	private File[] _files;
	File _output;
	private Column sortingColumn;

	public MergeFilesJob(Table table, File[] input, File output) {
		super("Merge files " + table.getName());
		this._table = table;
		this._files = input;
		this._output = output;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			// find column to do sorting
			sortingColumn = null;
			for (Column col : _table) {
				if (!col.isVirtual()) {
					if (!col.getOrder().isNone()) {
						sortingColumn = col;
						break;
					}
				}
			}

			XCursor[] cursors = new XCursor[_files.length];
			int len = 0;
			for (int i = 0; i < cursors.length; i++) {
				File f = _files[i];
				XCursor c = new XCursor(_table, f);
				cursors[i] = c;
				len += c.size();
			}

			monitor.beginTask("Merge files", len);

			XAppender app = new XAppender(_output, _table);

			Object[][] records = new Object[cursors.length][];

			while (true) {
				// read next records
				for (int i = 0; i < cursors.length; i++) {
					XCursor cur = cursors[i];
					if (records[i] == null) {
						if (cur.next()) {
							records[i] = cur.getRecord();
						}
					}
				}

				// continue if there is any available record
				int count = 0;
				for (Object[] rec : records) {
					if (rec != null) {
						count++;
					}
				}
				if (count == 0) {
					break; // stop loop
				}

				// find the next record to add
				Object[] rec = popRecord(records);
				if (rec != null) {
					monitor.worked(1);
					app.append(rec);
				}
			}

			app.close();
			for (XCursor cur : cursors) {
				cur.close();
			}

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MDBPlugin.openMDBEditor(_output);
					MDBPlugin.refreshConnectionViews(_output);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}

		return Status.OK_STATUS;
	}

	@SuppressWarnings("null")
	private Object[] popRecord(Object[][] records) {
		Object[] result = null;
		int colIdx = _table.indexOf(sortingColumn);
		Object minValue = null;
		for (Object[] rec : records) {
			if (rec != null) {
				if (sortingColumn == null) {
					result = rec;
					break;
				}
				Object v = rec[colIdx];
				if (result == null) {
					minValue = v;
					result = rec;
				} else {
					if (sortingColumn.getOrder() == Order.ASCENDING) {
						if (((Number) v).doubleValue() < ((Number) minValue)
								.doubleValue()) {
							minValue = v;
							result = rec;
						}
					} else {
						// descending
						if (((Number) v).doubleValue() > ((Number) minValue)
								.doubleValue()) {
							minValue = v;
							result = rec;
						}
					}
				}
			}
		}
		if (result != null) {
			int i = Arrays.asList(records).indexOf(result);
			records[i] = null;
		}
		return result;
	}
}
