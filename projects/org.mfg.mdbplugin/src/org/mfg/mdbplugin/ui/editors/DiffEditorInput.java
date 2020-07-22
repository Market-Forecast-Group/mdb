package org.mfg.mdbplugin.ui.editors;

import java.io.File;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.jobs.DiffComputer.DiffRecord;

public class DiffEditorInput implements IEditorInput {

	private Table _table;
	private File _leftFile;
	private File _rightFile;
	private List<DiffRecord> _diff;

	public DiffEditorInput(Table table, File leftFile, File rightFile,
			List<DiffRecord> diff) {
		super();
		this._table = table;
		this._leftFile = leftFile;
		this._rightFile = rightFile;
		this._diff = diff;
	}

	public Table getTable() {
		return _table;
	}

	public File getLeftFile() {
		return _leftFile;
	}

	public File getRightFile() {
		return _rightFile;
	}

	public List<DiffRecord> getDiff() {
		return _diff;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Diff: " + _leftFile.getName() + " - " + _rightFile.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return getName();
	}

}
