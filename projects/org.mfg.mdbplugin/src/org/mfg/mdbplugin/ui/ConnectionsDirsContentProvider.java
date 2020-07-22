package org.mfg.mdbplugin.ui;

import java.io.File;
import java.util.List;

public class ConnectionsDirsContentProvider extends ConnectionsContentProvider {
	@Override
	protected void addFile(List<File> list, File file) {
		if (file.isDirectory()) {
			super.addFile(list, file);
		}
	}
}
