package org.mfg.mya;

import java.io.InputStream;

/**
 * Default implementation of {@link ITemplateLoader}. The templates are loaded
 * from the resources of a given class, with the
 * {@link Class#getResourceAsStream(String)}.
 * 
 * @author arian
 *
 */
public class DefaultTemplateLoader implements ITemplateLoader {
	private Class<?> _resourcesClass;

	/**
	 * Create a default template loader.
	 * 
	 * @param resourcesClass
	 *            The class used to load the resources.
	 * @see Class#getResourceAsStream(String)
	 */
	public DefaultTemplateLoader(Class<?> resourcesClass) {
		_resourcesClass = resourcesClass;
	}

	/**
	 * The class used to load the resources as streams.
	 * 
	 * @return The class used to load the resources.
	 */
	public Class<?> getResourcesClass() {
		return _resourcesClass;
	}

	/**
	 * Load the template from the resources of the {@link #getResourcesClass()}.
	 * Is the same of:
	 * 
	 * <pre>
	 * getResourcesClass().getResourceAsStream(name);
	 * </pre>
	 * 
	 * @param name
	 *            The name of the resource/template.
	 */
	@Override
	public InputStream load(String name) {
		return _resourcesClass.getResourceAsStream(name);
	}

}
