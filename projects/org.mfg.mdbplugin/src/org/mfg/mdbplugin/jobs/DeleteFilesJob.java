package org.mfg.mdbplugin.jobs;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.mfg.mdbplugin.MDBPlugin;

public class DeleteFilesJob extends Job {

	File[] _files;

	public DeleteFilesJob(File... files) {
		super("Delete files");
		this._files = files;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Delete files", _files.length);
		for (File f : _files) {			
			try {
				MDBPlugin.deleteFile(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
			monitor.worked(1);

		}
		monitor.done();
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				for (File f : _files) {
					MDBPlugin.closeEditors(f);
				}
			}

		});
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MDBPlugin.refreshConnectionViews(null);
			}

		});

		return Status.OK_STATUS;
	}
}
