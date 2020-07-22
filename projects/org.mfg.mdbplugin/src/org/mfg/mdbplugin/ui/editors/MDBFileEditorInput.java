package org.mfg.mdbplugin.ui.editors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdbplugin.MDBPlugin;
import org.mfg.mdbplugin.adapters.MDBFileEditorInputFactory;
import org.mfg.mdbplugin.xmdb.XAppender;
import org.mfg.mdbplugin.xmdb.XCursor;

public class MDBFileEditorInput implements IEditorInput, IPersistableElement {

	private File _file;
	private Table _table;

	public MDBFileEditorInput(File file) {
		setFile(file);
	}

	public File getFile() {
		return _file;
	}

	public void setFile(File file) {
		this._file = file;
		try {
			if (file.exists()) {
				_table = MDBPlugin.getTableDef(file);
				if (_table == null) {
					throw new RuntimeException(
							"Cannot find the table definition for file " + file);
				}
			} else {
				_table = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Table getTable() {
		return _table;
	}

	@SuppressWarnings("boxing")
	public Object[][] readData(long start, long end) throws IOException {
		XCursor c = createCursor();
		int len = (int) Math.min(end - start, (int) c.size() - start);
		Object[][] data = new Object[len][];
		c.position(start);
		for (int i = 0; i < data.length && i <= end; i++) {
			c.next();
			Object[] r = c.getRecord();

			// create an augmented record with record position at the end.
			Object[] r2 = new Object[r.length + 1];
			System.arraycopy(r, 0, r2, 0, r.length);
			r2[r.length] = i;

			data[i] = r2;
		}
		c.close();
		return data;
	}

	public Object readValue(long row, int col) throws IOException {
		XCursor cursor = createCursor();
		cursor.position(row);
		cursor.next();
		Object[] rec = cursor.getRecord();
		cursor.close();
		Object value = rec[col];
		return value;
	}

	public void writeValue(long row, Column col, Object value)
			throws IOException {
		XAppender app = new XAppender(_file, _table);
		app.update(row, col.getIndex(), value);
		app.close();
	}

	public void addRecord() throws IOException {
		Object[] rec = new Object[_table.size()];
		for (Column col : _table) {
			rec[col.getIndex()] = col.getType().createDefault();
		}
		XAppender app = new XAppender(_file, _table);
		app.append(rec);
		app.close();
	}

	public XCursor createCursor() throws FileNotFoundException {
		return new XCursor(_table, _file);
	}

	public long size() throws IOException {
		XCursor c = createCursor();
		long size = c.size();
		c.close();
		return size;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return _file.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return _file.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return _file.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof MDBFileEditorInput) {
			return ((MDBFileEditorInput) obj).getFile().equals(_file);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return _file.hashCode();
	}

	@Override
	public void saveState(IMemento memento) {
		memento.putString("filepath", _file.getAbsolutePath());
	}

	@Override
	public String getFactoryId() {
		return MDBFileEditorInputFactory.ID;
	}
}
