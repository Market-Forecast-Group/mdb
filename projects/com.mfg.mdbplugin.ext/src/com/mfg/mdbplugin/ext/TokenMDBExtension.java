package com.mfg.mdbplugin.ext;

import java.io.InputStream;

import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.Table;

public class TokenMDBExtension implements ICompilerExtension {

	@Override
	public String getTarget() {
		return TARGET_MDB_CLASS_APPEND_METHOD;
	}

	@Override
	public String executeExtension(InputStream replacedFile, final Table table) {
		return "			_session.modified();\n";
	}

}
