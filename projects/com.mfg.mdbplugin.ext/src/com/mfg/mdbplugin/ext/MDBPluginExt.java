package com.mfg.mdbplugin.ext;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MDBPluginExt extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.mfg.mdbplugin.ext"; //$NON-NLS-1$

	// The shared instance
	private static MDBPluginExt plugin;
	
	/**
	 * The constructor
	 */
	public MDBPluginExt() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MDBPluginExt getDefault() {
		return plugin;
	}

}
