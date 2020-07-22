package com.mfg.mdbplugin.ext;

import java.io.IOException;
import java.io.InputStream;

import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.Table;
import org.mfg.mya.MyA;

/**
 * @author arian
 *
 */
public class TokenMDBSessionExtension implements ICompilerExtension {

	@Override
	public String getTarget() {
		return TARGET_SESSION_CLASS;
	}

	@Override
	public String executeExtension(InputStream replacedFile, Table table) {
		StringBuilder sb = new StringBuilder();
		sb.append("	private long modificationToken = 0;\n");
		sb.append("\n");
		sb.append("	public void modified() {\n");
		sb.append("		modificationToken++;\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	public long getModificatonToken() {\n");
		sb.append("		return modificationToken;\n");
		sb.append("	}\n");
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		// Just write to std the code to print
		MyA mya = new MyA();
		mya.inputResource("token_mdb_session.mya");
		mya.outputStream(System.out);
	}

}
