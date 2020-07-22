package org.mfg.mdb.compiler.internal;

import java.util.ArrayList;
import java.util.List;

import org.mfg.mdb.compiler.ICompilerExtension;
import org.mfg.mdb.compiler.IExtensionTargets;

/**
 * A simple "map" of extensions and its targets.
 * 
 * @see IExtensionTargets
 * @author arian
 * 
 */
public class ExtensionRegistry {
	private List<ICompilerExtension> extensions;

	/**
	 * The constructor.
	 */
	public ExtensionRegistry() {
		extensions = new ArrayList<>();
	}

	/**
	 * Register an extension.
	 * 
	 * @param extension
	 */
	public void register(ICompilerExtension extension) {
		extensions.add(extension);
	}

	/**
	 * Find all the extensions with the given <code>target</code>.
	 * 
	 * @see IExtensionTargets
	 * @param target
	 *            The target of the extensions.
	 * @return The list of extensions.
	 */
	public List<ICompilerExtension> find(String target) {
		List<ICompilerExtension> list = new ArrayList<>();
		for (ICompilerExtension e : extensions) {
			if (e.getTarget().equals(target)) {
				list.add(e);
			}
		}
		return list;
	}
}
