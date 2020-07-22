package org.mfg.mdbplugin.ui.handlers;

import java.io.File;
import java.io.IOException;

import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdbplugin.MDBPlugin;

public class MakeOneFileBackupHandler extends MakeBackupHandler {

	@Override
	protected String getCommentDialogTitle() {
		return "Backup File";
	}

	@Override
	protected void makeBackup(File file, String comment) throws IOException {
		MDBSession.backupFile(MDBPlugin.getRootFile(file), file,
				comment);
	}
}
