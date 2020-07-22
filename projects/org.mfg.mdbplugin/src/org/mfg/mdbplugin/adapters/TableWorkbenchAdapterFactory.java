package org.mfg.mdbplugin.adapters;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.wb.swt.ResourceManager;
import org.mfg.mdb.compiler.Table;

public class TableWorkbenchAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(final Object adaptableObject, Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class
				&& adaptableObject instanceof Table) {
			return new IWorkbenchAdapter() {

				@Override
				public Object getParent(Object o) {
					return null;
				}

				@Override
				public String getLabel(Object o) {
					return ((Table) adaptableObject).getName();
				}

				@Override
				public ImageDescriptor getImageDescriptor(Object object) {
					return ResourceManager.getPluginImageDescriptor(
							"org.eclipse.ui",
							"/icons/full/obj16/generic_element.gif");
				}

				@Override
				public Object[] getChildren(Object o) {
					return null;
				}
			};
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

}
