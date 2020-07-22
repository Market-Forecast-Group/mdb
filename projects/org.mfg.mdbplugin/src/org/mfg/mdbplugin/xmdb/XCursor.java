package org.mfg.mdbplugin.xmdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.Type;

@SuppressWarnings("boxing")
public class XCursor {
	private Table _table;
	private Object[] record;
	private RandomAccessFile _raf;
	private FileChannel _channel;
	private ByteBuffer _buff;
	private RandomAccessFile _arrayRaf;
	private FileChannel _arrayChannel;
	private boolean _evalVirtualColumns;
	private ScriptEngine _engine;
	private HashMap<Column, String> _formulaMap;

	public XCursor(Table table, File file) throws FileNotFoundException {
		super();
		this._table = table;
		this._raf = new RandomAccessFile(file, "r");
		_channel = _raf.getChannel();
		_buff = ByteBuffer.allocate(table.getRowSize());
		if (table.hasArray()) {
			File arrayFile = new File(file.getPath() + ".array");
			_arrayRaf = new RandomAccessFile(arrayFile, "r");
			_arrayChannel = _arrayRaf.getChannel();
		}
		_evalVirtualColumns = table.hasVirtualColumns();

		if (_evalVirtualColumns) {
			ScriptEngineManager manager = new ScriptEngineManager();
			_engine = manager.getEngineByName("JavaScript");

			_formulaMap = new HashMap<>();
			for (Column c : table) {
				if (c.isVirtual()) {
					String formula = c.getFormula();
					formula = expandFormula(table, formula);
					_formulaMap.put(c, formula);
				}
			}
		}
	}

	public static String expandFormula(Table table, String formula) {
		String expFormula = formula;
		for (Column c2 : table) {
			expFormula = expFormula.replace("$$." + c2.getName(), "__rec__"
					+ c2.getName());
		}
		return expFormula;
	}

	public boolean isEvalVirtualColumns() {
		return _evalVirtualColumns;
	}

	public void setEvalVirtualColumns(boolean evalVirtualColumns) {
		this._evalVirtualColumns = evalVirtualColumns;
	}

	public void position(long pos) throws IOException {
		_channel.position(pos * _table.getRowSize());
	}

	public long position() throws IOException {
		return _channel.position() / _table.getRowSize();
	}

	public boolean next() throws IOException {
		if (_channel.position() == _channel.size()) {
			return false;
		}
		record = new Object[_table.size()];
		_buff.rewind();
		_channel.read(_buff);
		_buff.rewind();
		int i = 0;
		for (Column c : _table) {
			if (!c.isVirtual()) {
				Type type = c.getType();
				if (type.isArray()) {
					long start = _buff.getLong();
					int len = _buff.getInt();
					ByteBuffer abuff = ByteBuffer.allocate(len);
					_arrayChannel.read(abuff, start);
					if (type == Type.STRING) {
						record[i] = new String(abuff.array());
					} else {
						Type elemType = type.getElementType();
						int elemSize = elemType.getSize();
						int arrLen = len / elemSize;
						Object[] arr = new Object[arrLen];

						abuff.rewind();
						for (int j = 0; j < arrLen; j++) {
							arr[j] = elemType.readValue(abuff);
						}
						record[i] = arr;
					}
				} else {
					record[i] = type.readValue(_buff);
				}
			}
			i++;
		}

		if (_evalVirtualColumns) {
			SimpleScriptContext ctx = new SimpleScriptContext();
			ctx.setAttribute("$pos$", position() - 1,
					ScriptContext.ENGINE_SCOPE);
			_engine.setContext(ctx);

			i = 0;
			for (Column c : _table) {
				if (!c.isVirtual()) {
					ctx.setAttribute("__rec__" + c.getName(), record[i],
							ScriptContext.ENGINE_SCOPE);
				}
				i++;
			}

			i = 0;
			for (Column c : _table) {
				if (c.isVirtual()) {
					if (_evalVirtualColumns) {
						String formula = _formulaMap.get(c);
						try {
							Object res = _engine.eval(formula);
							record[i] = res;
						} catch (ScriptException e) {
							e.printStackTrace();
							_evalVirtualColumns = false;
						}
					}
				}
				i++;
			}
		}
		return true;
	}

	public long size() throws IOException {
		return _channel.size() / _table.getRowSize();
	}

	public void close() throws IOException {
		_raf.close();
		if (_arrayRaf != null) {
			_arrayRaf.close();
		}
	}

	public Object[] getRecord() {
		return record;
	}

	public Table getTable() {
		return _table;
	}

}
