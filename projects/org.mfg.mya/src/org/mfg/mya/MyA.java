package org.mfg.mya;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Main class to run MyA templates.
 * </p>
 * <p>
 * Create an instance of MyA to run the templates. To run a template you have to
 * give the template source (or input) using one of the "input" methods and the
 * output destination, using one of the "output" methods. Also you should
 * provide a loader, to load sub templates.
 * </p>
 * 
 * @author arian
 */
public class MyA {
	private static final String MYA_IMPORT_INSTR = "#mya:import ";
	private static final String SPECIAL_HIDDEN_CODE = "#_special_#_code_!!";
	private InputStream _template;
	private OutputStream _output;
	private ITemplateLoader _loader;
	private String _printExpr;

	/**
	 * Create an MyA instance. By default is created a
	 * {@link DefaultTemplateLoader} with the caller class.
	 */
	public MyA() {
		_printExpr = "print(@);";
		try {
			// get a default loader with the caller class.
			_loader = new DefaultTemplateLoader(Class.forName(new Exception()
					.getStackTrace()[1].getClassName()));
		} catch (Exception e) {
			// caller class hack failed
		}
	}

	/**
	 * Use the given stream as input.
	 * 
	 * @param input
	 *            The stream with the input.
	 */
	public void inputStream(InputStream input) {
		_template = input;
	}

	/**
	 * Use the given file as input.
	 * 
	 * @param input
	 *            The file as input.
	 * @throws FileNotFoundException
	 *             If the file is not found.
	 */
	public void inputFile(File input) throws FileNotFoundException {
		_template = new FileInputStream(input);
	}

	/**
	 * Set the given text as input.
	 * 
	 * @param input
	 *            The text as input.
	 */
	public void inputText(String input) {
		_template = new ByteArrayInputStream(input.getBytes());
	}

	/**
	 * Use as input a resource loaded with the current loader (
	 * {@link #loader(ITemplateLoader)}).
	 * 
	 * @param resourseName
	 *            The name of the resource as input.
	 */
	public void inputResource(String resourseName) {
		inputStream(_loader.load(resourseName));
	}

	/**
	 * Run the template and append the result to the given string builder.
	 * 
	 * @param output
	 *            String builder where the text is appended.
	 * @throws IOException
	 *             If there is any error.
	 */
	public void outputBuilder(StringBuilder output) throws IOException {
		try (ByteArrayOutputStream array = new ByteArrayOutputStream()) {
			outputStream(array);
			output.append(array.toString());
		}
	}

	/**
	 * Run the template and write the result to the file.
	 * 
	 * @param output
	 *            The file where the text is written.
	 * @throws IOException
	 *             If there is any error.
	 */
	public void outputFile(File output) throws IOException {
		output.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(output)) {
			outputStream(fos);
		}
	}

	/**
	 * Run the template and return the result.
	 * 
	 * @return The result of the template run.
	 * @throws IOException
	 *             If there is an error.
	 */
	public String outputText() throws IOException {
		try (ByteArrayOutputStream array = new ByteArrayOutputStream()) {
			outputStream(array);
			return array.toString();
		}
	}

	/**
	 * Run the template and write the result in the given output stream.
	 * 
	 * @param output
	 *            The output stream.
	 * @throws IOException
	 *             If there is any error.
	 */
	public void outputStream(OutputStream output) throws IOException {
		_output = output;
		compile();
	}

	/**
	 * Set the loader used by the <code>#mya:import</code> instruction.
	 * 
	 * @param loader
	 *            The loader to use.
	 * 
	 * @see ITemplateLoader
	 */
	public void loader(ITemplateLoader loader) {
		if (loader == null) {
			throw new InvalidParameterException(
					"Invalid argument, null not allowed.");
		}
		_loader = loader;
	}

	/**
	 * Use the given class loader of the given class to load the sub-templates
	 * as resources.
	 * 
	 * @param resourcesClass
	 *            The class used to load the sub-templates.
	 * 
	 * @see #loader(ITemplateLoader)
	 * @see DefaultTemplateLoader
	 * @see Class#getResourceAsStream(String)
	 */
	public void loaderDefault(Class<?> resourcesClass) {
		loader(new DefaultTemplateLoader(resourcesClass));
	}

	private void compile() throws IOException {
		if (_template == null) {
			throw new InvalidParameterException(
					"Missing template. Please use one of the MyA.input() methods.");
		}

		String inputText = expandInput(_template);

		try (BufferedReader reader = new BufferedReader(new StringReader(
				inputText));
				BufferedWriter fileWriter = new BufferedWriter(
						new OutputStreamWriter(_output))) {

			StringBuilder strBuilder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				String xline = parseLine(line);
				if (xline != null) {
					if (strBuilder.length() > 0) {
						strBuilder.append("\n");
					}
					strBuilder.append(xline);
				}
			}

			String result = strBuilder.toString();

			fileWriter.write(result);
		}
	}

	/**
	 * Build the input with the multiple input files. The input file may contain
	 * #mya:import instructions used to include other file. This operation
	 * should be made before to process the template.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	private String expandInput(InputStream input) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				input))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimLine = line.trim();
				if (trimLine.startsWith(MYA_IMPORT_INSTR)) {
					// import other file
					String name = trimLine.substring(MYA_IMPORT_INSTR.length())
							.trim();
					// out.println("Expanding " + name);
					String src = expandInput(_loader.load(name));
					// TODO: the indentation is not working really
					int i = line.indexOf(MYA_IMPORT_INSTR);
					String indent = line.substring(0, i);
					src = indent + src.replace("\n", "\n" + indent);
					sb.append(src + "\n");
				} else {
					sb.append(line + "\n");
				}
			}
		}

		return sb.toString();
	}

	private String parseLine(String line) {
		String trimLine = line.trim();
		if (trimLine.startsWith("#")) {
			if (trimLine.startsWith("#mya:print ")) {
				_printExpr = trimLine.substring("#mya:print ".length());
			}
			return null;
		}

		int arianIndex = -1;

		for (int i = line.length() - 1; i > 0; i--) {
			if (line.charAt(i) == '/' && line.charAt(i - 1) == '/') {
				arianIndex = i - 1;
				break;
			}
		}

		String maria;
		String arian;

		if (arianIndex != -1) {
			maria = line.substring(0, arianIndex);
			arian = line.substring(arianIndex + 2);
		} else {
			maria = line;
			arian = "%";
		}

		String xline = expandLine(maria, arian);

		return xline;
	}

	private String expandLine(String maria, String arian) {
		String maria2 = maria.replace("\"", "\\\\\"").replace("''", "\"")
				.replace("/#", "//");
		StringBuilder mariaStr = new StringBuilder();
		List<String> chunks = new ArrayList<>();
		boolean open = false;
		for (int i = 0; i < maria2.length(); i++) {
			char ch = maria2.charAt(i);
			if (ch == '$') {
				if (open) {
					chunks.add(mariaStr.toString());
				} else {
					chunks.add("\"" + mariaStr.toString() + "\"");
				}
				mariaStr = new StringBuilder();
				open = !open;
			} else {
				mariaStr.append(ch);
			}
		}
		chunks.add("\"" + mariaStr.toString() + "\"");

		mariaStr = new StringBuilder();
		for (int i = 0; i < chunks.size(); i++) {
			String str = chunks.get(i);
			if (!str.equals("\"\"")) {
				if (i > 0) {
					mariaStr.append(" + ");
				}
				mariaStr.append(str);
			}
		}
		String expr = mariaStr.toString();
		String expr_nl;
		if (expr.length() == 0) {
			expr_nl = "\"\\\\n\"";
		} else {
			if (expr.endsWith("\"")) {
				expr_nl = expr.substring(0, expr.length() - 1) + "\\\\n\"";
			} else {
				expr_nl = expr + " + \"\\\\n\"";
			}
		}

		String xmaria = _printExpr.replace("@", expr);
		String xmaria_nl = _printExpr.replace("@", expr_nl);
		String xline;
		String arian2 = arian.replace("\\@", SPECIAL_HIDDEN_CODE);
		if (arian2.contains("%")) {
			xline = arian2.replaceAll("%", xmaria_nl);
		} else {
			xline = arian2.replaceAll("@", xmaria);
		}
		xline = xline.replaceAll(SPECIAL_HIDDEN_CODE, "@");
		return xline;
	}
}
