package org.mfg.mdbplugin.adapters;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.mfg.mdbplugin.ui.editors.MDBFileEditorInput;

public class MDBFileEditorInputFactory implements IElementFactory {
	public static final String ID = "org.mfg.mdbplugin.adapters.mdbfileinputfactory";

	@Override
	public IAdaptable createElement(IMemento memento) {
		String path = memento.getString("filepath");
		File file = new File(path);
		return new MDBFileEditorInput(file);
	}

}
