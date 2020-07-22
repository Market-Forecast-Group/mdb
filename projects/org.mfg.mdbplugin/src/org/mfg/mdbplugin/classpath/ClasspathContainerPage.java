package org.mfg.mdbplugin.classpath;

import static java.lang.System.out;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ClasspathContainerPage extends WizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {

	private IJavaProject _project;
	private IClasspathEntry[] _entries;

	public ClasspathContainerPage() {
		super("mdbRuntimeLib", "MDB Runtime Library", null);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));

		Label lblMdbRuntimeLibrary = new Label(container, SWT.NONE);
		lblMdbRuntimeLibrary.setText("MDB Runtime Library");

		StyledText styledText = new StyledText(container, SWT.BORDER);
		styledText.setTopMargin(5);
		styledText.setRightMargin(5);
		styledText.setLeftMargin(5);
		styledText.setEditable(false);
		styledText
				.setText("\nThis library is to be used with Java projects.\n\nIn case of PDE plugin projects, you should add this plugin dependecy:\n\norg.mfg.mdb.runtime");
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
	}

	@Override
	public boolean finish() {
		for (IClasspathEntry entry : _entries) {
			String entryPath = entry.getPath().toString();
			out.println("Entry path: " + entryPath);
			if (entryPath.equals(MDBClasspathContainer.ID)) {
				out.println("The mdb runtime is already there");
				return true;
			}
		}
		out.println("ClasspathContainerPage.finish()");
		IClasspathEntry[] newEntries = new IClasspathEntry[_entries.length + 1];
		System.arraycopy(_entries, 0, newEntries, 0, _entries.length);
		newEntries[_entries.length] = JavaCore.newContainerEntry(Path
				.fromPortableString(MDBClasspathContainer.ID));
		// newEntries[_entries.length] = JavaCore.newLibraryEntry(dstFile,
		// null,null);
		try {
			_project.setRawClasspath(newEntries, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
			MessageDialog
					.openError(getShell(), "Set Classpath", e.getMessage());
		}
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {
		out.println("ClasspathContainerPage.getSelection()");
		return null;
	}

	@Override
	public void setSelection(IClasspathEntry containerEntry) {
		out.println("ClasspathContainerPage.setSelection(" + containerEntry
				+ ")");
	}

	@Override
	public void initialize(IJavaProject project,
			IClasspathEntry[] currentEntries) {
		_project = project;
		_entries = currentEntries;
	}
}
