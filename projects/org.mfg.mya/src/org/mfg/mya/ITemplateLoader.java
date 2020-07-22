package org.mfg.mya;

import java.io.InputStream;

/**
 * <p>
 * Interface to abstract the template loading. A template is loaded when the
 * with instruction:
 * </p>
 * 
 * <pre>
 * #mya:import &lt;template-name&gt;
 * </pre>
 * <p>
 * A loader implementation build an input stream for the given template name.
 * </p>
 * 
 * @author arian
 *
 */
public interface ITemplateLoader {

	/**
	 * Build an input stream for the given template name.
	 * 
	 * @param name
	 *            The name of the template.
	 * @return An input stream with the template identified with that name.
	 */
	public InputStream load(String name);
}
