package org.mfg.mdbplugin.ui.handlers;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.views.MDBConnectionsView;

public class MakeBackupHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection sel = (StructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		boolean someBackupFiles = false;
		InputDialog dlg = new InputDialog(shell, getCommentDialogTitle(),
				"Enter a backup comment:", "by "
						+ System.getProperty("user.name"),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						return null;
					}
				});
		if (dlg.open() == Window.OK) {
			for (Object obj : sel.toArray()) {
				try {
					File file = obj instanceof String ? new File((String) obj)
							: (File) obj;
					boolean isBackupFile = MDBPlugin.isBackupFile(file);
					someBackupFiles = someBackupFiles || isBackupFile;
					if (!isBackupFile) {
						String comment = dlg.getValue();
						makeBackup(file, comment);
					}
				} catch (IOException e) {
					e.printStackTrace();
					ErrorDialog.openError(shell, "Error", e.getMessage(),
							ValidationStatus.error(e.getMessage(), e));
				}
			}

			if (someBackupFiles) {
				MessageDialog
						.openInformation(shell, getCommentDialogTitle(),
								"Some selected files was ignored because you cannot backup a backup's file.");
			}
			MDBConnectionsView view = (MDBConnectionsView) HandlerUtil
					.getActivePart(event);
			view.getCommonViewer().refresh();
		}
		return null;
	}

	@SuppressWarnings("static-method")
	protected String getCommentDialogTitle() {
		return "Bakcup Database";
	}

	@SuppressWarnings("static-method")
	protected void makeBackup(File file, String comment) throws IOException {
		MDBSession.backup(file, comment);
	}
}
