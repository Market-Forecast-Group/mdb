package org.mfg.mdbplugin.ui;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;

public abstract class DoubleClickEditingSupport extends EditingSupport {

	private Object lastSel;
	protected boolean doubleClick;

	public DoubleClickEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		Object sel = ((StructuredSelection) getViewer().getSelection())
				.getFirstElement();
		boolean res = false;
		if (lastSel != null && lastSel == sel) {
			res = true;
		}
		lastSel = sel;
		return res;
	}

}
