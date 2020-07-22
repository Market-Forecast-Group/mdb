package org.mfg.mdbplugin.ui;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ConnectionsSorter extends ViewerSorter {

	public ConnectionsSorter() {
	}

	public ConnectionsSorter(Collator aCollator) {
		super(aCollator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return 0;
	}

}
