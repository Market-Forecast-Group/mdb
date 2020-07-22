package org.mfg.mdbplugin.ui.views;

import java.io.File;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.ui.ConnectionsContentProvider;
import org.mfg.mdbplugin.ui.editors.MDBFileEditor;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;

public class MDBConnectionsView extends CommonNavigator {
	
	public static final Object VIEW_ID = "org.mfg.mdbplugin.ui.views.navigator";

	@Override
	public void init(IViewSite aSite, IMemento aMemento)
			throws PartInitException {
		super.init(aSite, aMemento);
	}

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);

		getCommonViewer().addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection sel = (StructuredSelection) event
						.getSelection();
				if (sel.getFirstElement() instanceof File) {
					File f = (File) sel.getFirstElement();
					if (MDBPlugin.isMDBFile(f)) {
						try {
							PlatformUI
									.getWorkbench()
									.getActiveWorkbenchWindow()
									.getActivePage()
									.openEditor(new MDBFileEditorInput(f),
											MDBFileEditor.EDITOR_ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	@Override
	protected Object getInitialInput() {
		return ConnectionsContentProvider.MDB_CONNECTIONS_ROOT;
	}
}
