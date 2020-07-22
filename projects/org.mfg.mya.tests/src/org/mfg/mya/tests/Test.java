package org.mfg.mya.tests;

import java.io.IOException;

import org.mfg.mya.MyA;

public class Test {
	public static void main(String[] args) throws IOException {
		MyA mya = new MyA();
		//mya.inputResource("template2");
		mya.inputText("#mya:import HelloWorld.mya");
		mya.outputStream(System.out);
	}
}
