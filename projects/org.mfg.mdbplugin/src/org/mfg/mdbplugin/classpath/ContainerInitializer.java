/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdbplugin.classpath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author arian
 * 
 */
public class ContainerInitializer extends ClasspathContainerInitializer {

	@Override
	public void initialize(IPath path, IJavaProject project)
			throws CoreException {
		MDBClasspathContainer container = new MDBClasspathContainer();
		JavaCore.setClasspathContainer(path, new IJavaProject[] { project },
				new IClasspathContainer[] { container }, null);
	}

}
