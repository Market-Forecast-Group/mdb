package org.mfg.mdbplugin.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.part.FileEditorInput;
import org.mfg.mdbplugin.ui.editors.MDBSchemaEditor;

public class MDBSchemasView extends CommonNavigator {
	public final String VIEW_ID = "org.mfg.mdbplugin.ui.views.schema";

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		getCommonViewer().addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection) event
						.getSelection();
				if (sel.getFirstElement() instanceof IFile) {
					IFile f = (IFile) sel.getFirstElement();
					if (f.getFileExtension().equals("schema-json")) {
						try {
							getSite()
									.getWorkbenchWindow()
									.getActivePage()
									.openEditor(new FileEditorInput(f),
											MDBSchemaEditor.EDITOR_ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		getCommonViewer().expandAll();
	}

	@Override
	protected Object getInitialInput() {
		return ResourcesPlugin.getWorkspace();
	}
}
