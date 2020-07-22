/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mya;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * <p>
 * Helper class you can use in the MyA templates to generate the text to files
 * or any other output stream.
 * </p>
 * <p>
 * To use this class in a template, you can change the default "print()" method
 * by "printer.print()", something like this:
 * </p>
 * 
 * <pre>
 * #mya:print printer.print(@)
 * &#47;&#47;printer = new Printer(output);
 * Hello world!
 * </pre>
 * <p>
 * The main feature of this class is the
 * {@linkplain #userText(String, String, String)} method. With it you can
 * preserve some code in the replacing file, making it easy to customize the
 * generated text.
 * </p>
 * 
 * @author arian
 * 
 */
public class Printer implements Closeable {

	/**
	 * Camel case the given text. For example "person" is converted to "Person".
	 * 
	 * @param str
	 *            Text to convert to camel-case.
	 * @return The camel case representation of the given string.
	 */
	public static String camelCase(String str) {
		String name2 = str.substring(0, 1).toUpperCase()
				+ str.substring(1, str.length());
		return name2;
	}

	/**
	 * Just like {@link String#toUpperCase()}.
	 * 
	 * @param str
	 *            String to upper case.
	 * @return The upper case representation of the given string.
	 */
	public static String upperCase(String str) {
		return str.toUpperCase();
	}

	/**
	 * Just like {@link String#toLowerCase()}.
	 * 
	 * @param str
	 *            String to lower case.
	 * @return The lower case representation of the given string.
	 */
	public static String lowerCase(String str) {
		return str.toLowerCase();
	}

	/**
	 * Escape the escape char "\".
	 * 
	 * @param str
	 *            String to escape.
	 * @return The escaped string.
	 */
	public static String escape(String str) {
		return str.replace("\"", "\\\"");
	}

	private final BufferedWriter writer;
	private InputStream replacingStream;
	private String replacedText;

	/**
	 * Create a printer.
	 * 
	 * @param output
	 *            The output stream.
	 */
	public Printer(OutputStream output) {
		this.writer = new BufferedWriter(new OutputStreamWriter(output));
	}

	/**
	 * Create a printer.
	 * 
	 * @param output
	 *            The output stream.
	 * @param replacing
	 *            The input stream of the content to replace. This is used by
	 *            the {@link #userText(String, String, String)} method.
	 */
	public Printer(OutputStream output, InputStream replacing) {
		this(output);
		this.replacingStream = replacing;
	}

	/**
	 * Create a printer.
	 * 
	 * @param output
	 *            The output stream.
	 * @param replacing
	 *            The file to replace. This is used by the
	 *            {@link #userText(String, String, String)} method.
	 * @throws FileNotFoundException
	 *             If the file is not found.
	 */
	@SuppressWarnings("resource")
	public Printer(OutputStream output, File replacing)
			throws FileNotFoundException {
		this(output, replacing == null || !replacing.exists() ? null
				: new FileInputStream(replacing));
	}

	/**
	 * Print the string. This is the method you should use in the template
	 * instruction:
	 * 
	 * <pre>
	 * #mya:print printer.print(@);
	 * </pre>
	 * 
	 * @param str
	 *            The string to print.
	 * @throws IOException
	 *             If there is an error.
	 */
	public void print(String str) throws IOException {
		writer.write(str);
	}

	/**
	 * The text to be replaced. Used by
	 * {@link #userText(String, String, String)}.
	 * 
	 * @return The replacing text.
	 */
	public String getReplacedText() {
		if (replacingStream == null) {
			return "";
		}
		if (replacedText == null) {
			StringBuilder b = new StringBuilder();
			BufferedReader r = new BufferedReader(new InputStreamReader(
					replacingStream));
			try {
				String l;
				while ((l = r.readLine()) != null) {
					b.append(l + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			replacedText = b.toString();
		}
		return replacedText;
	}

	/**
	 * Use this method to preserve certain code in the replacing text. All text
	 * between the tags "openTag" and "closeTag" wil be kept intact. If those
	 * tags do not exist, then the default text is printed.
	 * 
	 * @param openTag
	 *            Open tag.
	 * @param closeTag
	 *            Close tag.
	 * @param defaultText
	 *            Default content if there is not any open-close tag.
	 * @throws IOException
	 *             If there is any error.
	 */
	public void userText(String openTag, String closeTag, String defaultText)
			throws IOException {
		String userText = defaultText;

		String text = getReplacedText();
		int start = text.indexOf(openTag);
		if (start >= 0) {
			int end = text.indexOf(closeTag);
			if (end > start) {
				userText = text.substring(start + openTag.length(), end);
			}
		}

		print(openTag);
		print(userText);
		print(closeTag);
	}

	/**
	 * Closes the emitter. This flush/close the internal writer.
	 * 
	 * @throws IOException
	 *             I/O error.
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}
}
