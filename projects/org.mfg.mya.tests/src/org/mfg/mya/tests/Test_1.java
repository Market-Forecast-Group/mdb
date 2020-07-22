package org.mfg.mya.tests;

import java.io.IOException;

import org.junit.Test;
import org.mfg.mya.MyA;

public class Test_1 {

	@Test
	public void test_fileTemplate() throws IOException {
		MyA mya = new MyA();
		mya.inputStream(getClass().getResourceAsStream("Test_1.mya"));
		mya.outputStream(System.out);
	}

	@Test
	public void test_symbols() {
		// nothing yet
	}

}
