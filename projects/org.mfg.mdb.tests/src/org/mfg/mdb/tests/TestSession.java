package org.mfg.mdb.tests;

import java.io.File;
import java.io.IOException;

import org.mfg.mdb.runtime.SessionMode;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;

public class TestSession extends JUnitTestsMDBSession {

	public TestSession(String sessionName, File root, SessionMode mode)
			throws IOException {
		super(sessionName, root, mode);
		setDebug(AllTests.DEBUG);
	}

	public TestSession(String sessionName, File root) throws IOException {
		super(sessionName, root);
		setDebug(AllTests.DEBUG);
	}

}
