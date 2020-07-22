package org.mfg.mdbplugin.ui.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.runtime.BackupVersion;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.views.MDBConnectionsView;

public class RestoreVersionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection sel = (StructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		for (Object obj : sel.toArray()) {
			BackupVersion version = (BackupVersion) obj;
			try {

				if (MessageDialog.openConfirm(
						shell,
						"Restore",
						"Do you want to restore to the version "
								+ version.getDate() + " - "
								+ version.getComment())) {
					MDBPlugin.forgetFilesTable(version.getDbRoot().toFile());
					MDBSession.restore(version.getDbRoot().toFile(), version);
					MDBConnectionsView view = (MDBConnectionsView) HandlerUtil
							.getActivePart(event);
					view.getCommonViewer().refresh();
				}
			} catch (IOException e) {
				e.printStackTrace();
				ErrorDialog.openError(shell, "Error", e.getMessage(),
						ValidationStatus.error(e.getMessage(), e));
			}
		}

		MDBPlugin.refreshMDBEditors();
		return null;
	}

}
