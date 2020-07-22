package org.mfg.mdbplugin.ui.editors;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.json.JSONObject;
import org.mfg.mdb.compiler.Schema;
import org.mfg.mdbplugin.MDBPlugin;

public class MDBSchemaEditor extends FormEditor implements
		IResourceChangeListener {

	public static final String EDITOR_ID = "org.mfg.mdbplugin.schemaEditor";

	private Schema _schema;
	private MDBSchemaPage mainPage;

	public MDBSchemaEditor() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setPartName(input.getName());

		readSchema();

		ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
				IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * 
	 */
	private void readSchema() {
		try {
			IFile file = getEditorInput().getFile();
			_schema = MDBPlugin.readSchemaEditorFile(file);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the schema
	 */
	public Schema getSchema() {
		return _schema;
	}

	public MDBSchemaPage getMainPage() {
		return mainPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		try {
			mainPage = new MDBSchemaPage(this, "schema", "Schema");
			mainPage.addPropertyListener(new IPropertyListener() {

				@SuppressWarnings("synthetic-access")
				@Override
				public void propertyChanged(Object source, int propId) {
					handlePropertyChange(propId);
				}
			});
			addPage(mainPage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileEditorInput getEditorInput() {
		return (FileEditorInput) super.getEditorInput();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IFile file = getEditorInput().getFile();
		try {
			file.setPersistentProperty(new QualifiedName("org.mfg.mdbplugin",
					"selectedTable"), Integer.toString(mainPage
					.getSelectedTable()));
			file.setPersistentProperty(new QualifiedName("org.mfg.mdbplugin",
					"selectedColumn"), Integer.toString(mainPage
					.getSelectedColumn()));
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

		try {
			final String str = _schema.toJSONString();
			file.setContents(new ByteArrayInputStream(new JSONObject(str)
					.toString(4).getBytes()), false, false, null);

			mainPage.setDirty(false);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFocus() {
		mainPage.setFocus();
	}

	@Override
	public void doSaveAs() {
		// nothing
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta evtDelta = event.getDelta();
		if (evtDelta != null) {
			try {
				evtDelta.accept(new IResourceDeltaVisitor() {

					@Override
					public boolean visit(IResourceDelta delta)
							throws CoreException {
						IFile file = getEditorInput().getFile();

						IPath movedToPath = delta.getMovedToPath();
						if (movedToPath != null
								&& delta.getResource().equals(file)) {
							inputMovedTo(movedToPath);
						} else {
							if (delta.getResource().equals(file)) {
								int kind = delta.getKind();
								switch (kind) {
								case IResourceDelta.CONTENT:
									inputContentChanged();
									break;
								case IResourceDelta.REMOVED:
									inputRemoved();
									break;
								default:
									break;
								}
							}
						}
						return true;
					}
				});
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
	 */
	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		Display.getDefault().asyncExec(new Runnable() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() {
				setPartName(getEditorInput().getFile().getFullPath()
						.removeFileExtension().lastSegment());
			}
		});
	}

	protected void inputContentChanged() {
		readSchema();
		mainPage.refresh();
	}

	protected void inputMovedTo(IPath movedToPath) {
		IFile newFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(movedToPath);
		FileEditorInput input = new FileEditorInput(newFile);
		setInput(input);
		readSchema();
		mainPage.refresh();
	}

	protected void inputRemoved() {
		close(false);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
}
