package org.mfg.mdbplugin.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdbplugin.MDBPlugin;

@Deprecated
public class CopyMDBLibIntoProject extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) currentSelection;
			if (!sel.isEmpty()) {
				Object elem = sel.getFirstElement();

				if (elem instanceof IJavaElement) {
					IProject project = ((IJavaElement) elem).getResource()
							.getProject();
					elem = project;
					elem = JavaCore.create(project);
				}

				if (elem instanceof IJavaProject) {
					IJavaProject project = (IJavaProject) elem;
					MDBPlugin.copyLibToProjectAndSetClasspath(project);
				} else {
					MessageDialog.openInformation(
							HandlerUtil.getActiveShell(event),
							"Copy MDB Library",
							"You are not selecting a project or a resource.");
				}
			}
		}
		return null;
	}

}
