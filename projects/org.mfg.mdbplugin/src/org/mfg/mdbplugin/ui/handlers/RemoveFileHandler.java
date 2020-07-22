package org.mfg.mdbplugin.ui.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.BackupVersion;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.jobs.DeleteFilesJob;
import org.mfg.mdbplugin.ui.views.MDBConnectionsView;

public class RemoveFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection sel = (StructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		if (!sel.isEmpty()
				&& MessageDialog
						.openQuestion(HandlerUtil.getActiveShell(event),
								"Delete",
								"Are you sure do you want to delete these files from your physical storage?")) {
			Object[] arr = sel.toArray();
			List<File> files = new ArrayList<>();
			for (int i = 0; i < arr.length; i++) {
				Object e = arr[i];
				if (e instanceof BackupVersion) {
					MDBSession.delete(((BackupVersion) e).getPath()
							.toFile());
				} else if (e instanceof String) {
					MDBPlugin.getDefault().removeConnectionPath((String) e);
				} else if (e instanceof File) {
					files.add((File) e);
				}
			}
			if (files.isEmpty()) {
				MDBConnectionsView view = (MDBConnectionsView) HandlerUtil
						.getActivePart(event);
				view.getCommonViewer().refresh();
			} else {
				new DeleteFilesJob(files.toArray(new File[files.size()]))
						.schedule();
			}
		}
		return null;
	}
}
