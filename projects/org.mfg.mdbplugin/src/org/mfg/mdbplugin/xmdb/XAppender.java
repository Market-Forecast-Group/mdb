package org.mfg.mdbplugin.xmdb;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.mfg.mdb.compiler.Column;
import org.mfg.mdb.compiler.Table;
import org.mfg.mdb.compiler.Type;
import org.mfg.mdb.runtime.FilesTable;
import org.mfg.mdbplugin.MDBPlugin;

public class XAppender {
	private Table _table;
	private RandomAccessFile raf;
	private FileChannel channel;
	private ByteBuffer buff;
	private RandomAccessFile arrayRaf;
	private FileChannel arrayChannel;

	@SuppressWarnings("resource")
	public XAppender(File file, Table table) throws IOException {
		this._table = table;
		file.getParentFile().mkdirs();
		file.createNewFile();
		raf = new RandomAccessFile(file, "rw");
		channel = raf.getChannel();
		channel.position(channel.size());
		buff = ByteBuffer.allocate(table.getRowSize());
		File arrayFile = null;
		if (table.hasArray()) {
			arrayFile = new File(file.getPath() + ".array");
			arrayFile.getParentFile().mkdirs();
			arrayFile.createNewFile();
			arrayRaf = new RandomAccessFile(arrayFile, "rw");
			arrayChannel = arrayRaf.getChannel();
			arrayChannel.position(arrayChannel.size());
		}
		File root = MDBPlugin.getRootFile(file);
		FilesTable ftable = MDBPlugin.getFilesTable(root);
		ftable.update(file, table.getUUID().toString());
	}

	public void append(Object[] record) throws IOException {
		int i = 0;
		buff.rewind();
		for (Column c : _table) {
			if (!c.isVirtual()) {
				Type type = c.getType();
				if (type == Type.STRING) {
					String str = (String) record[i];
					buff.putLong(arrayChannel.position());
					buff.putInt(str.length());
					ByteBuffer abuff = ByteBuffer.wrap(str.getBytes());
					arrayChannel.write(abuff);
				} else if (type.isArray()) {
					Type elemType = type.getElementType();
					int elemSize = elemType.getSize();

					Object[] arr = (Object[]) record[i];
					int bufLen = arr.length * elemSize;

					buff.putLong(arrayChannel.position());
					buff.putInt(bufLen);

					ByteBuffer abuff = ByteBuffer.allocate(bufLen);
					for (int j = 0; j < arr.length; j++) {
						Object value = arr[j];
						elemType.writeValue(abuff, value);
					}
					abuff.rewind();
					arrayChannel.write(abuff);
				} else {
					type.writeValue(buff, record[i]);
				}
			}
			i++;
		}
		buff.rewind();
		channel.write(buff);
	}

	public void update(long row, int col, Object value) throws IOException {
		Type type = _table.get(col).getType();
		int colOffs = 0;
		for (int i = 0; i < col; i++) {
			colOffs += _table.get(i).getType().getSize();
		}
		ByteBuffer buf = ByteBuffer.allocate(type.getSize());
		buf.rewind();
		type.writeValue(buf, value);
		buf.rewind();
		long pos = row * _table.getRowSize();

		channel.write(buf, pos + colOffs);
	}

	public void close() throws IOException {
		raf.close();
		if (arrayRaf != null) {
			arrayRaf.close();
		}
	}

}
