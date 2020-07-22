package org.mfg.mdbplugin.jobs;

import java.io.File;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.jobs.DiffComputer.DiffRecord;
import org.mfg.mdbplugin.ui.editors.DiffEditor;
import org.mfg.mdbplugin.ui.editors.DiffEditorInput;
import org.mfg.mdbplugin.xmdb.XCursor;

public class DiffJob extends Job {

	File _leftFile;
	File _rightFile;
	Table _table;

	public DiffJob(Table table, File leftFile, File rightFile) {
		super("Diff " + leftFile.getName() + " " + rightFile.getName());
		this._table = table;
		this._leftFile = leftFile;
		this._rightFile = rightFile;

	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			XCursor cur = new XCursor(_table, _leftFile);
			Object[] left = new Object[(int) cur.size()];

			int i = 0;
			while (cur.next()) {
				left[i] = cur.getRecord();
				i++;
			}
			cur.close();

			cur = new XCursor(_table, _rightFile);
			Object[] right = new Object[(int) cur.size()];

			i = 0;
			while (cur.next()) {
				right[i] = cur.getRecord();
				i++;
			}
			cur.close();

			DiffComputer diff = new DiffComputer();

			final List<DiffRecord> diffResult = diff.diff(left, right);

			// for (DiffRecord r : diffResult) {
			// out.println(r);
			// }

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.openEditor(
										new DiffEditorInput(_table, _leftFile,
												_rightFile, diffResult),
										DiffEditor.EDITOR_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});

			return Status.OK_STATUS;
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}
	}

}
