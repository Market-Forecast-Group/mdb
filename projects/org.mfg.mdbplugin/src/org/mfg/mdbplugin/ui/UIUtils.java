package org.mfg.mdbplugin.ui;

import java.util.Comparator;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

public class UIUtils {
	public static void selectAndExpand(TreeViewer viewer, Object key,
			Comparator<Object> comp) {
		selectAndExpand(viewer, viewer.getTree().getItems(), key, comp);
	}

	private static boolean selectAndExpand(TreeViewer viewer, TreeItem[] items,
			Object key, Comparator<Object> comp) {
		for (TreeItem i : items) {
			Object data = i.getData();
			if (data == null) {
				continue;
			}
			viewer.expandToLevel(data, 1);
			if (comp.compare(key, data) == 0) {
				viewer.setSelection(new StructuredSelection(data));
				return true;
			}
			boolean res = selectAndExpand(viewer, i.getItems(), key, comp);
			if (res) {
				return true;
			}
			viewer.collapseToLevel(data, 1);
		}
		return false;
	}
}
