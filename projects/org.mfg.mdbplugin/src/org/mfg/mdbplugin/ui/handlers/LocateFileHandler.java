package org.mfg.mdbplugin.ui.handlers;

import static java.lang.System.out;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 
 * @author arian
 *
 */
public class LocateFileHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		IResource res = null;
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) activePart).getEditorInput();
			if (input instanceof FileEditorInput) {
				res = ((FileEditorInput) input).getFile();
			}
		} else if (sel instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) sel).getFirstElement();

			if (obj instanceof IResource) {
				res = (IResource) obj;
			} else if (obj instanceof IJavaElement) {
				res = ((IJavaElement) obj).getResource();
			} else {
				Object adapter = Platform.getAdapterManager().getAdapter(obj,
						IResource.class);
				if (adapter != null) {
					res = (IResource) adapter;
				}
			}
		}
		if (res != null) {
			File file = res.getLocation().toFile();
			try {
				if (!file.isDirectory()) {
					file = file.getParentFile();
				}
				out.println("Open file " + file);
				Desktop.getDesktop().open(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
