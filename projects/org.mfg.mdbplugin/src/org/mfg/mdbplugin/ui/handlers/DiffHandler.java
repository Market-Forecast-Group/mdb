package org.mfg.mdbplugin.ui.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.jobs.DiffJob;

public class DiffHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection sel = (StructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		Shell shell = HandlerUtil.getActiveShell(event);

		Table table = null;

		Object[] arr = sel.toArray();

		File[] files = new File[arr.length];

		if (files.length != 2) {
			MessageDialog
					.openError(shell, "Wrong Number of Files",
							"Diff should be between only two files, not more, not less.");
			return null;
		}

		int i = 0;
		for (Object obj : arr) {
			File f = files[i] = (File) obj;
			if (f.isDirectory()) {
				MessageDialog.openError(shell, "Invalid Diff",
						"Cannot compare directories.");
				return null;
			}
			try {
				Table t = MDBPlugin.getTableDef(f);

				if (t == null) {
					throw new IllegalArgumentException();
				} else if (table == null) {
					table = t;
				} else if (table.getUUID().equals(t.getUUID())) {
					files[i] = f;
				} else {
					MessageDialog.openError(shell, "Wrong Selection",
							"Cannot compare files with different shemas.");
					return null;
				}

			} catch (Exception e) {
				MessageDialog.openError(shell, "Metadata Error",
						"File " + f.getName() + " is not a valid MDB file");
				return null;
			}
			i++;
		}

		DiffJob job = new DiffJob(table, files[0], files[1]);
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();

		return null;
	}

}
