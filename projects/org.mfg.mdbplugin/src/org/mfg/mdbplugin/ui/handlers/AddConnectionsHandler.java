package org.mfg.mdbplugin.ui.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdbplugin.MDBPlugin;

public class AddConnectionsHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		DirectoryDialog dialog = new DirectoryDialog(shell);
		String path = dialog.open();
		Object reveal = null;
		if (path != null) {
			File f = new File(path);
			reveal = f.getAbsolutePath();
			addConnections(f);
		} 
		MDBPlugin.refreshConnectionViews(reveal);

		return null;
	}

	private void addConnections(File f) {
		if (f.isDirectory()) {
			if (MDBPlugin.isDBRoot(f)) {
				MDBPlugin.getDefault().addConnectionPath(f.getAbsolutePath());
				return;
			}
			File[] list = f.listFiles();
			for (File file : list) {
				addConnections(file);
			}
		}
	}

}
