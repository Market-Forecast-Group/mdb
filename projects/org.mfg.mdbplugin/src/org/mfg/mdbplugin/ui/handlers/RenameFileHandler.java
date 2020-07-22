package org.mfg.mdbplugin.ui.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdbplugin.MDBPlugin;

public class RenameFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final File f = (File) ((StructuredSelection) HandlerUtil
				.getCurrentSelection(event)).getFirstElement();
		InputDialog dialog = new InputDialog(HandlerUtil.getActiveShell(event),
				"Rename File", "Enter new name", f.getName(),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						String text = newText;
						if (!text.contains(".")) {
							text += ".mdb";
						}
						if (new File(f.getParentFile(), text).exists()) {
							return "File exists. Select other name.";
						}
						return null;
					}
				});
		if (dialog.open() == Window.OK) {
			String name = dialog.getValue();
			if (!name.contains(".")) {
				name += ".mdb";
			}
			try {
				MDBPlugin.renameFile(f, name);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
