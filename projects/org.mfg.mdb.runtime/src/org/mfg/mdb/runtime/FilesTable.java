package org.mfg.mdb.runtime;

import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to keep updated the "files.table" file of the session. This file
 * contains a list of all the MDB files of the database and the table definition
 * ID of the file.
 * 
 * @author arian
 * 
 */
public class FilesTable implements Closeable {
	private Path _path;
	private Map<String, String> _map;
	private Path _dbPath;
	private BufferedWriter _writer;
	private boolean _rewrite;

	/**
	 * The constructor.
	 * 
	 * @param dbPath
	 *            The database path.
	 * @throws IOException
	 *             If there is any problem accessing the files.
	 */
	public FilesTable(Path dbPath) throws IOException {
		super();
		_dbPath = dbPath;
		_path = dbPath.resolve(".metadata/files.table");
		_map = new HashMap<>();
		_rewrite = false;

		if (Files.exists(_path)) {
			try (BufferedReader reader = Files.newBufferedReader(_path,
					Charset.defaultCharset())) {
				while (true) {
					String relPath = reader.readLine();
					if (relPath == null) {
						break;
					}
					String id = reader.readLine();
					if (id == null) {
						err.println("Wrong \"files.table\" format.");
						break;
					}
					_map.put(relPath, id);
				}
			}
		} else {
			Files.createFile(_path);
		}
		_writer = Files.newBufferedWriter(_path, Charset.defaultCharset(),
				StandardOpenOption.APPEND);
	}

	/**
	 * Update the file meta-data.
	 * 
	 * @param mdbFile
	 *            The MDB file.
	 * @param tableId
	 *            The table definition ID.
	 * @throws IOException
	 *             If there is a problem writing to file.
	 */
	public synchronized void update(File mdbFile, String tableId)
			throws IOException {
		String relPath = _dbPath.relativize(mdbFile.toPath()).toString();

		if (!_map.containsKey(relPath)) {
			_map.put(relPath, tableId);
			_writer.write(relPath);
			_writer.write("\n");
			_writer.write(tableId);
			_writer.write("\n");
			_writer.flush();
		}
	}

	/**
	 * Delete the file from the table.
	 * 
	 * @param mdbFile
	 *            The MDB file to delete.
	 */
	public void delete(Path mdbFile) {
		String relPath = _dbPath.relativize(mdbFile).toString();
		if (_map.containsKey(relPath)) {
			_map.remove(relPath);
			_rewrite = true;
		}
	}

	/**
	 * Get the table definition ID of the given file.
	 * 
	 * @param file
	 *            The file to lookup.
	 * @return The table definition ID or <tt>null</tt> if the file is not an
	 *         MDB file.
	 */
	public String lookupTableId(Path file) {
		String relPath = _dbPath.relativize(file).toString();
		return _map.get(relPath);
	}

	@Override
	public void close() throws IOException {
		_writer.close();
		rewrite();
	}

	private void rewrite() throws IOException {
		if (_rewrite) {
			try (BufferedWriter writer = Files.newBufferedWriter(_path,
					Charset.defaultCharset())) {
				for (String relPath : _map.keySet()) {
					String tableId = _map.get(relPath);
					writer.write(relPath);
					writer.write("\n");
					writer.write(tableId);
					writer.write("\n");
				}
			}
		}
		_rewrite = false;
	}

	/**
	 * Write changes.
	 * 
	 * @throws IOException
	 *             If there is a problem writing to file.
	 */
	public void flush() throws IOException {
		_writer.flush();
		rewrite();
	}
}
