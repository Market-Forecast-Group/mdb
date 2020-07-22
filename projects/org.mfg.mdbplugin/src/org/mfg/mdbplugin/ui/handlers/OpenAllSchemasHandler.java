package org.mfg.mdbplugin.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.mfg.mdbplugin.ui.editors.MDBSchemaEditor;

public class OpenAllSchemasHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		for (IProject proj : ws.getRoot().getProjects()) {
			IJavaProject javaProj = JavaCore.create(proj);
			if (javaProj != null) {
				try {
					for (IPackageFragmentRoot root : javaProj
							.getPackageFragmentRoots()) {
						IResource resource = root.getResource();
						if (resource != null) {
							resource.accept(new IResourceVisitor() {

								@Override
								public boolean visit(IResource res)
										throws CoreException {
									if (res instanceof IFile
											&& res.getFileExtension().equals(
													"schema-json")) {
										PlatformUI
												.getWorkbench()
												.getActiveWorkbenchWindow()
												.getActivePage()
												.openEditor(
														new FileEditorInput(
																(IFile) res),
														MDBSchemaEditor.EDITOR_ID);
									}
									return true;
								}
							});
						}
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return null;
	}

}
