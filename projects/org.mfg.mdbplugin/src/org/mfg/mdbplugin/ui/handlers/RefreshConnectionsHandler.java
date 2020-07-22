package org.mfg.mdbplugin.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.mfg.mdbplugin.ui.views.MDBConnectionsView;

public class RefreshConnectionsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MDBConnectionsView view = (MDBConnectionsView) HandlerUtil
				.getActivePart(event);
		view.getCommonViewer().refresh();
		return null;
	}

}
