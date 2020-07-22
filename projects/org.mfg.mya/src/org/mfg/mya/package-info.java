/**
 * <p>MyA is a simple template engine created by the MFG team to be used in the MDB compiler.</p>
 * 
 * <b>Index</b>
 * <ul>
 * 	<li><a href="#example1">Example 1: Print Hello message</a></li>
 * 	<li><a href="#example2">Example 2: Print Hello message for a list of names</a></li>
 * 	<li><a href="#example3">Example 3: Put the generated code in a context</a></li>
 * 	<li><a href="#example4">Example 4: More about the "//", "%" and "@" symbols</a></li>
 * 	<li><a href="#example5">Example 5: Split the templates</a></li>	
 * 	<li><a href="#example6">Example 6: Using the <code>org.mfg.mya.Printer</code> class</a></li>
 * </ul>
 * 
 * 
 * <p>The main feature of MyA is its simplicity and productivity. First thing, is that you don't need to learn a new template language,
 * MyA uses Java as the "action language", and you can debug your templates as easy as debug your own Java project, since MyA compiles a template into a Java code.</p>
 * <p>MyA has as input a template (text, text file, etc..) and transform it into Java instructions that can be executed to generate the final code.</p>
 * <p>Let's see some examples:</p>
 * <h3 id="example1">Example 1: Print Hello message.</h3>
 * 
 * <p>Step 1: Write your template:</p>
 * 
 * <pre>
 * Hello $name$!
 * </pre>
 * 
 * <p>Step 2: Pass the template as input to MyA, and get a Java like program.</p>
 
 * <p>The template can be stored in a file or in general passed as an input stream to {@link org.mfg.mya.MyA}. You can see MyA has helpful "input" and "output" methods.
 * Look this is a way to run the hello world template:
 * </p>
 * <pre>
 * public class Main {
 * 	public static void main(String[] args) {
 * 		MyA mya = new MyA();
 * 		mya.inputText("Hello $name$");
 * 		mya.outputStream(System.out);
 * 	}
 * }
 * </pre>
 * 
 * <p>
 * Then execute that class and you get:
 * </p>
 * 
 * <pre>
 * print("Hello " + name + "!\n");
 * </pre>
 * 
 * <p>Step 3: That code can be used by a Java program to print a hello message with a name stored in the variable "name". As in many other templates, 
 * the "$" symbol is used to enclose an expression, it means, a code that is used to query the data model and concatenate the result with the text of the template.</p>
 * <p>In this case we use the <code>$name$</code> expression, then look how a variable "name" is concatenated with the text of the template in the resultant code.</p>
 * 
 * <p>At a first glance this can look a bit complicated, since the majority of the template engines uses a two-step approach, but in
 * MyA we decided to introduce a middle step because we gain in flexibility and, at the end, in legibility.</p>
 * <p>If you love compiler-compiler like ANTLR or Cup, then you will love MyA, since it uses a similar
 * model, it generates a code generator.</p>
 * 
 * <h3 id="example2">Example 2: Print Hello message for a list of names.</h3>
 * 
 * <p>Similar to the previous example, but now, given an array of names, it prints a hello message for each one of them. This is the template:</p>
 * 
 * <pre>
 * &#47;&#47;for (String name : names) {
 * Hello $name$!
 * &#47;&#47;} 
 * </pre>
 * 
 * <p>And this is the generated code:</p>
 * 
 * <pre>
 * for (String name : names) {
 * print("Hello " + name + "!\n");
 * }
 * </pre>
 * 
 * <p>Here we use the symbol "&#47;&#47;" at the beginning of the line because we want to write the code to iterate the array of names. It means,
 * that code will be executed as Java statements and it is not concatenated with any text of the template.</p>
 * 
 * <h3 id="example3">Example 3: Put the generated code in a context.</h3>
 * 
 * <p>In the previous examples what you get are some lines of a Java code like language that you can use somewhere, but yet they have some requirements, because in the code is used a "print" 
 * method that is defined by no one.</p>
 * <p>Suppose what you want to do is just to append the text of the template into a {@link java.lang.StringBuilder}. The options you have, for now, is to create your own "print" method and pass the string to the string builder:
 * 
 * <pre>
 * &#47;&#47;private StringBuilder builder = new StringBuilder();
 * &#47;&#47; 
 * &#47;&#47;private void print(String str) {
 * &#47;&#47;	builder.append(str);
 * &#47;&#47;}
 * &#47;&#47;
 * &#47;&#47;private void printList() {
 * &#47;&#47;
 * &lt;html&gt;
 * 	&lt;body&gt;
 * 		&lt;ul&gt;
 * &#47;&#47;
 * &#47;&#47;	for (String name : names) {
 * 			&lt;li&gt;Hello $name$!&lt;&#47;li&gt;
 * &#47;&#47;	}
 * &#47;&#47;
 * 		&lt;&#47;ul&gt;
 * 	&lt;&#47;body&gt;
 * &lt;&#47;html&gt;
 * &#47;&#47;
 * &#47;&#47;	response(printer.builder.toString())
 * &#47;&#47;}
 * </pre>
 * And the result is:
 * <pre>
 * private StringBuilder builder = new StringBuilder();
 * 
 * private void print(String str) {
 * 	builder.append(str);
 * }
 *
 * private void printList() {
 * 
 * print("&lt;html&gt;\n");
 * print("	&lt;body&gt;\n");
 * print("		&lt;ul&gt;\n");
 * 
 * 	for (String name : names) {
 * print("			&lt;li&gt;Hello " + name + "!&lt;&#47;li&gt;\n");
 * 	}
 * 
 * print(" 		&lt;&#47;ul&gt;\n");
 * print(" 	&lt;&#47;body&gt;\n");
 * print("&lt;&#47;html&gt;\n");
 * 
 * 	response(printer.builder);
 * }
 * </pre>
 * 
 * <p>Ok the template is different now, it is a about to generate an HTML document that is passed to a "response" method at the end. But the main question is that
 * we had to create a"print" method to bypass the string to the builder. However, we are not forced to do it, there is a way to say to MyA that we want to change 
 * the "print" sentences to something like "builder.append()".</p>
 * <p>To do it we use the "#" symbol. When you use the "#" at the beginning of a line, it means it is a template comment and MyA ignores it,
 * but also you can use "#" to give some instructions to MyA. In this case, we can use the instruction <code>#mya:print builder.append(@);</code>, 
 * where symbol "@" will be replaced by the string in question.</p>
 * <p>So, a better template is:</p>
 * <pre>
 * #mya:print builder.append(@);
 * &#47;&#47;StringBuilder builder = new StringBuilder();
 * &#47;&#47;
 * &lt;html&gt;
 * 	&lt;body&gt;
 * 		&lt;ul&gt;
 * &#47;&#47;
 * &#47;&#47;for (String name : names) {
 * 			&lt;li&gt;Hello $name$!&lt;&#47;li&gt;
 * &#47;&#47;}
 * &#47;&#47;
 * 		&lt;&#47;ul&gt;
 * 	&lt;&#47;body&gt;
 * &lt;&#47;html&gt;
 * &#47;&#47;
 * &#47;&#47;response(builder.toString());
 * </pre>
 * <p>And the generated code is:</p>
 * <pre>
 * StringBuilder builder = new StringBuilder();
 * 
 * builder.append("&lt;html&gt;").append("\\n");
 * builder.append("	&lt;body&gt;").append("\\n");
 * builder.append("		&lt;ul&gt;").append("\\n");
 * 
 * for (String name : names) {
 * builder.append("			&lt;li&gt;Hello " + name + "!&lt;&#47;li&gt;").append("\\n");
 * }
 * 
 * builder.append(" 		&lt;&#47;ul&gt;").append("\\n");
 * builder.append(" 	&lt;&#47;body&gt;").append("\\n");
 * builder.append("&lt;&#47;html&gt;").append("\\n");
 * 
 * response(printer.builder);
 * </pre>
 * 
 * 
 * 
 * <h3 id="example4">Example 4: More about the "//", "%" and "@" symbols</h3>
 * 
 * <p>
 * We saw the "//" symbol is used to start a line of action code. Look in this template we print a good morning message in Italian or in English, 
 * in dependence of the default locale:
 * </p>
 * 
 * <pre>
 * &#47;&#47;if (Locale.getDefault() == Locale.ITALIA) {
 * Buongiorno
 * &#47;&#47;} else {
 * Good morning
 * &#47;&#47;}
 * </pre>
 * 
 * <p>
 * That is compiled into:
 * </p>
 * 
 * <pre>
 * if (Locale.getDefault() == Locale.ITALIA) {
 * print("Buongiorno\n");
 * } else {
 * print("Good morning\n");
 * }
 * </pre>
 * 
 * <p>
 * But we can simplify that template if we write in the same line action code together with text code:
 * </p>
 * 
 * <pre>
 * Buongiorno//if (Locale.getDefault() == Locale.ITALIA) %
 * Good morning//else %
 * </pre>
 * 
 * <p>
 * It is compiled into:
 * </p>
 * 
 * <pre>
 * if (Locale.getDefault() == Locale.ITALIA) print("Buongiorno\n");
 * else print("Good morning\n");
 * </pre>
 * 
 * <p>
 * Do you see what's happening? The action code is written as usual, but it replaces the symbol "%" by the text written the "//".
 * The same happens if you use the "@" symbol, but instead of print a line (a text with a "\n" at the end), it prints just the text, for example:
 * If you want to print all numbers from 0 to 99 in this way "(0)(1)(2)...(99)" you can do a template like: 
 * </p>
 * 
 * <pre>
 * ($n$)//for (int n = 0; n &lt; 100; n++) @
 * </pre>
 * 
 * It compiles to:
 * 
 * <pre>
 * for (int n = 0; n &lt; 100; n++) print("(" + n + ")");
 * </pre>
 * 
 * <h3 id="example5">Example 5: Split the templates</h3>
 * 
 * <p>If you template is too big you can split it in files. For example if you have a big template "main.mya", 
 * you can split it in 3 files like "part1.mya", "part2.mya" and "part3.mya", and then write in "main.mya"</p>
 * 
 * <pre>
 * #mya:import part1.mya
 * #mya:import part2.mya
 * #mya:import part3.mya
 * </pre>
 * 
 * <p>
 * By default, the MyA instance to import the templates uses the class loader, but it can be changed with the method {@link org.mfg.mya.MyA#loader(ITemplateLoader)}. 
 * </p>
 * <h3 id="example6">Example 6: Using the <code>org.mfg.mya.Printer</code> class</h3>
 * 
 * <p>
 * In previous examples, by default, the text is passed to a <code>print()</code> method, or you can redefine it 
 * to pass the text to a <code>StringBuilder.append()</code> method. 
 * That is a nice feature of MyA, it gives to you all the control. 
 * But there is a built-in class in MyA special for text generation. It's name is {@linkplain org.mfg.mya.Printer} 
 * What makes this class special is that it comes with some helper methods. 
 * The must useful of these methods is {@linkplain org.mfg.mya.Printer#userText(String, String, String)}.    
 * </p>
 * <p>
 * In the majority of the cases, like in other template engines, the text is written into a file. Then, with the <code>userText()</code> method, you can preserve part
 * of that "overriding" file. In the case of MDB, where the generated files are Java source files, we use <code>userText()</code> to preserve part of the class it overrides. 
 * In this way, the programmer can add methods or variables to the generated classes, and keep them intact when the class is re-generated.
 * </p>
 * <p>
 * Let's do this. We want to generate a class but we want to keep open that class for certain customization. We can write the following MyA template:
 * </p>
 * <pre>
 * class Person extends $baseClassName$ {
 * }
 * </pre>
 * <p>
 * That template programmer uses a <code>baseClassName</code> variable to customize the inheritance of the generated class Person. This is fine, but we can do it in other way:
 * </p>
 * <pre>
 * #mya:print printer.print(@)
 * &#47;&#47;printer = new Printer(outputFile);
 * class Person
 * &#47;&#47;printer.userText(
 * &#47;&#47;	"&#47;* begin-inheritance *&#47;", 
 * &#47;&#47;	"&#47;* end-inheritance *&#47;", 
 * &#47;&#47;	"&#47;* you can write any code here *&#47;")
 * {
 * }
 * </pre>
 * That template generates this class:
 * <pre>
 * class Person
 * &#47;* begin-inheritance *&#47;
 * &#47;* you can write any code here *&#47;
 * &#47;* end-inheritance *&#47;
 * {
 * }
 * </pre>
 * Now, if the developer who are using the class <code>Person</code> wants to change the inheritance of that class, 
 * he can write between the tags "begin-inheritance" and "end-inheritance" the "extends" or "implements" clauses. Like this:
 * <pre>
 * class Person
 * &#47;* begin-inheritance *&#47;
 * 	<b>extends Animal implements Cloneable</b>
 * &#47;* end-inheritance *&#47;
 * {
 * }
 * </pre>
 * <p>As you can see, there is more freedom to customize the template, in this case, you can write an "implements" clause. But the most important thing, 
 * is that here the customization is in the opposite direction. This means, that the customization parameters, or values, are taken from the file will be replaced, 
 * but not from the data model of the template.</p>
 * <p>
 * Other advantage, in the case of the generation of programming code, is that you can use the IDE assist features to write and validate your customization code. 
 * Also in many cases it reduce the complexity of the hierarchies of the target projects, because the classes are open to customization without the need of
 * object oriented techniques like method overriding.
 * </p>
 */
package org.mfg.mya;

