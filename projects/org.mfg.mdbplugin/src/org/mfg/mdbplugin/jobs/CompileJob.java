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
package org.mfg.mdbplugin.jobs;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Compiler;
import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;

/**
 * @author arian
 * 
 */
public class CompileJob extends Job {

	private final IFile _file;
	private final Schema _schema;

	public CompileJob(IFile file, Schema schema) {
		super("Compile MDB " + schema.getName());
		this._file = file;
		this._schema = schema;
	}

	private Compiler buildCompiler() {
		String schemaName = _file.getName().replace(
				"." + _file.getFileExtension(), "");
		String srcDir = "";
		String pkg = "";

		IProject proj = _file.getProject();
		try {
			if (proj.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
				IJavaProject javaProj = JavaCore.create(proj);

				// set src and package name

				for (IPackageFragmentRoot pkgFragment : javaProj
						.getPackageFragmentRoots()) {
					if (pkgFragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
						if (pkgFragment.getPath().isPrefixOf(
								_file.getFullPath())) {
							int n = pkgFragment.getPath()
									.matchingFirstSegments(_file.getFullPath());
							String pkgStr = _file.getFullPath()
									.removeFirstSegments(n)
									.removeLastSegments(1).toPortableString()
									.replace('/', '.');
							pkg = pkgStr;
							srcDir = pkgFragment.getResource().getLocation()
									.toPortableString();
							_schema.setSource(srcDir);
							break;
						}
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		int buffSize = _schema.getBufferSize();

		final Compiler compiler = new Compiler(schemaName, srcDir, pkg,
				buffSize);

		ICompilerExtension[] extensions = MDBPlugin.getDefault()
				.getCompilerExtensions();
		for (ICompilerExtension ext : extensions) {
			compiler.extension(ext);
		}

		for (final Table t : _schema) {
			compiler.table(t.getName(), t.getUUID());
			for (final Column col : t) {
				compiler.column(col.getName(), col.getType(), col.getOrder(),
						col.isVirtual(), col.getFormula(), col.getUUID());
			}
		}

		return compiler;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			Compiler c = buildCompiler();
			c.compile();
		} catch (Exception e) {
			e.printStackTrace();
			return ValidationStatus.error(e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}
}
