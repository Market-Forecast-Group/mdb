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

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

/**
 * @author arian
 * 
 */
public class MDBClasspathContainer implements IClasspathContainer {
	public static final String ID = "org.mfg.mdbplugin.classpathcontainer";
	private static IClasspathEntry[] entries;

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		if (entries == null) {
			Bundle bundle = Platform.getBundle("org.mfg.mdb.runtime");
			try {
				File path = FileLocator.getBundleFile(bundle);
				out.println("mdb runtime jar at: " + path.getAbsolutePath());
				IClasspathEntry entry = JavaCore.newLibraryEntry(
						Path.fromOSString(path.getAbsolutePath()), null,
						null);
				entries = new IClasspathEntry[] { entry };
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	@Override
	public String getDescription() {
		return "MDB Runtime Library";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	@Override
	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	@Override
	public IPath getPath() {
		return Path.fromPortableString(ID);
	}

}
