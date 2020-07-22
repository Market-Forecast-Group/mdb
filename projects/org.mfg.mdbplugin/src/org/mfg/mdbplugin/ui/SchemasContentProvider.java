package org.mfg.mdbplugin.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.validation.ValidationStatus;
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SchemasContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// nothing
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// nothing
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkspace) {
			IProject[] projects = ((IWorkspace) parentElement).getRoot()
					.getProjects();
			List<Object> list = new ArrayList<>();
			for (IProject p : projects) {
				Object[] children = getChildren(p);
				if (children != null && children.length > 0) {
					list.add(p);
				}
			}
			return list.toArray();
		}
		if (parentElement instanceof IProject) {
			IProject proj = (IProject) parentElement;
			final List<Object> list = new ArrayList<>();

			final IJavaProject javaProj = JavaCore.create(proj);
			if (javaProj != null) {
				try {
					try {
						IPackageFragmentRoot[] roots = javaProj
								.getPackageFragmentRoots();
						for (IPackageFragmentRoot root : roots) {
							IResource resource = root.getResource();
							if (resource != null) {
								resource.accept(new IResourceVisitor() {

									@Override
									public boolean visit(IResource res)
											throws CoreException {
										if (res instanceof IFile
												&& res.getFileExtension()
														.equals("schema-json")) {
											list.add(res);
										}
										return true;
									}
								});
							}
						}
					} catch (JavaModelException e) {
						// ignore it
					}
				} catch (Exception e) {
					e.printStackTrace();
					ResourcesPlugin.getPlugin().getLog()
							.log(ValidationStatus.error(e.getMessage(), e));
				}
			}

			return list.toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

}
