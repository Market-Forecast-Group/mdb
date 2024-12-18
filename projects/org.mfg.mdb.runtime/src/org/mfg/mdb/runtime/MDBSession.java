/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package org.mfg.mdb.runtime;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Base class for all the MDB sessions. Also it contains methods to manage MDB
 * backups.
 * 
 * @author arian
 * 
 */
public abstract class MDBSession {
	private static final int DEFERRED_CURSORS_THRESHOLD = 1024;

	/**
	 * The key for partial backup version.
	 */
	public static final String KEY_VERSION_PARTIAL_BACKUP = "Partial-Backup";
	protected final boolean _memory;
	protected Map<File, MDB<?>> _cache;
	protected boolean _open;
	private final File _root;
	private File _metadataDir;
	private final String _sessionName;
	private final Properties _properties;
	private final SessionMode _mode;
	private final List<ICursor<?>> _deferredCursors;
	private File _backupDir;
	private File _propsFile;
	private boolean _debug;
	protected final ReadLock _readLock;
	protected final WriteLock _writeLock;
	private Condition _allCursorsClosedCond;

	private long _closeTimeoutMillis;

	private int _openCursors;

	private Map<String, String> _signatures;

	private String _jsonSchema;

	private FilesTable _filesTable;

	/**
	 * Create a new session.
	 * 
	 * @param sessionName
	 *            Session name, used only as a name.
	 * @param root
	 *            The database folder.
	 * @param mode
	 *            Session mode used to assert a correct use of the session.
	 * @param signatures
	 *            The signature of the schema, used to validate if the database
	 *            is readable by this session. This information is generated by
	 *            the compiler.
	 * @param jsonSchema
	 *            The schema in JSON format. It is generated by the compiler and
	 *            saved into the database, then external tools can read the
	 *            files without create a session.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files. If
	 *             there is any problem writing/reading session files.
	 */
	public MDBSession(final String sessionName, final File root,
			final SessionMode mode, Map<String, String> signatures,
			String jsonSchema) throws IOException {
		_debug = false;
		_memory = mode == SessionMode.MEMORY;
		_signatures = signatures;
		_jsonSchema = jsonSchema;

		if (mode == SessionMode.READ_ONLY) {
			if (!root.exists()) {
				throw new IOException(
						"Session mode READ_ONY requires a previous database exists");
			}
		} else if (mode == SessionMode.READ_WRITE) {
			root.mkdirs();
			if (!root.exists()) {
				throw new IOException("Fail create root dir at " + root);
			}
		}

		_sessionName = sessionName;
		_root = root;
		_backupDir = _root.toPath().resolveSibling("~" + root.getName())
				.toFile();
		_mode = mode;
		_cache = new HashMap<>();
		_open = true;
		_properties = new Properties();

		_deferredCursors = new ArrayList<>();

		if (!_memory) {
			initProperties();
			initMetadata();
		}
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		_readLock = lock.readLock();
		_writeLock = lock.writeLock();
		_allCursorsClosedCond = _writeLock.newCondition();

		_closeTimeoutMillis = 3_000;

		_openCursors = 0;
	}

	private void initProperties() throws FileNotFoundException, IOException {
		_propsFile = new File(_root, "session.properties");
		if (_propsFile.exists()) {
			try (FileInputStream is = new FileInputStream(_propsFile)) {
				_properties.load(is);
			}
		}
	}

	private void initMetadata() throws IOException {
		_metadataDir = new File(_root, ".metadata");
		if (!_metadataDir.exists()) {
			if (!_metadataDir.mkdirs()) {
				throw new IOException("Cannot create metadata dir at "
						+ _metadataDir);
			}
		}

		checkCompatibility(_signatures);
		Path schemaFile = getMetadataDir().toPath().resolve("schema.json");
		Files.write(schemaFile, _jsonSchema.getBytes());

		_filesTable = new FilesTable(_root.toPath());
	}

	/**
	 * If the session is in debug mode.
	 * 
	 * @return <code>true</code> if it is debugging is on.
	 */
	public boolean isDebug() {
		return _debug;
	}

	/**
	 * Set debug mode.
	 * 
	 * @param debug
	 *            Debug flag.
	 */
	public void setDebug(boolean debug) {
		_debug = debug;
	}

	/**
	 * Subclasses can override this method to initialize some fields looking
	 * into the {@link #getProperties()} values.
	 */
	protected void readProperties() {
		// nothing by default
	}

	/**
	 * Check if the current MDB generated classes are compatible with the
	 * database. Maybe it is an old database but the current schema is other.
	 * 
	 * @param signatures
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	protected void checkCompatibility(Map<String, String> signatures)
			throws IOException {
		Properties props = new Properties();
		File file = new File(_metadataDir, "signatures.properties");
		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file)) {
				props.load(in);
				in.close();
				for (Object tableId : props.keySet()) {
					if (signatures.containsKey(tableId)) {
						String newSignature = signatures.get(tableId);
						Object oldSignature = props.get(tableId);
						if (!newSignature.equals(oldSignature)) {
							throw new IOException("Wrong signature for table "
									+ tableId);
						}
					}
				}
			}
		}
		props.clear();
		for (String key : signatures.keySet()) {
			props.put(key, signatures.get(key));
		}
		try (FileOutputStream output = new FileOutputStream(file)) {
			props.store(output, null);
		}
	}

	protected void createFileMetadata(File mainFile, String tableId)
			throws IOException {
		_filesTable.update(mainFile, tableId);
	}

	/**
	 * Create a new session with READ_WRITE mode.
	 * 
	 * @param sessionName
	 *            Session name.
	 * @param root
	 *            The database folder.
	 * @param metadata
	 *            Session metadata.
	 * @param jsonSchema
	 *            JSON text representation of the schema.
	 * @see SessionMode#READ_WRITE
	 * @throws IOException
	 *             If there is any problem writing/reading the session files. If
	 *             there is any error writing/reading session files.
	 */
	public MDBSession(final String sessionName, final File root,
			Map<String, String> metadata, String jsonSchema) throws IOException {
		this(sessionName, root, SessionMode.READ_WRITE, metadata, jsonSchema);
	}

	/**
	 * This method is called by MDB generated classes. Do not call it yourself.
	 * 
	 * @param mdb
	 *            MDB file of the appender.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public void appenderRequested(final MDB<?> mdb) throws IOException {
		if (_mode == SessionMode.READ_ONLY) {
			throw new IOException(
					"Session mode READ_ONLY violation. Appender requested for "
							+ mdb.getFile() + ".");
		}
	}

	/**
	 * This method is called by MDB generated classes. Do not call it yourself.
	 * 
	 * @param cursor
	 *            The cursor.
	 * 
	 */
	public void cursorCreated(final ICursor<?> cursor) {
		if (_debug) {
			out.println("Cursor created " + cursor + " - "
					+ cursor.getMDB().getFile());
		}
		_writeLock.lock();
		_openCursors++;
		try {
			int size = _deferredCursors.size();
			if (size > DEFERRED_CURSORS_THRESHOLD) {

				List<ICursor<?>> list = new ArrayList<>();
				for (ICursor<?> c : _deferredCursors) {
					if (c != null && !c.isOpen()) {
						list.add(c);
					}
				}
				_deferredCursors.removeAll(list);
				if (_debug) {
					out.println("Reduce list of deferred cursors from " + size
							+ " to " + _deferredCursors.size());
				}
			}
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * This method is called by MDB generated classes. Do not call it yourself.
	 * 
	 * @param cursor
	 *            The cursor.
	 * 
	 */
	public void cursorClosed(ICursor<?> cursor) {
		if (_debug) {
			out.println("Cursor closed " + cursor + " - "
					+ cursor.getMDB().getFile());
		}
		_writeLock.lock();
		_openCursors--;
		assert _openCursors >= 0;
		try {
			_allCursorsClosedCond.signalAll();
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Defer a cursor. Deferred cursors are closed automatically before to close
	 * the session. This is useful for cursors you pretend to use in the whole
	 * session live. However please take a look to the methods
	 * {@link MDB#thread_cursor()} and {@link MDB#thread_randomCursor()},
	 * probably a reusable thread cursor is what you need.
	 * 
	 * @see MDB#thread_cursor()
	 * @see MDB#thread_randomCursor()
	 * 
	 * @param cursor
	 *            The cursor to close automatically at the end of the session.
	 */
	public void defer(final ICursor<?> cursor) {
		if (cursor == null) {
			throw new IllegalArgumentException("Cannot defer a null cursor.");
		}

		_writeLock.lock();
		try {
			_deferredCursors.add(cursor);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * <p>
	 * Disconnect the MDB file from the session. When you disconnect an MDB
	 * file, the appender and cursors are closed, so all the resources
	 * associated to that file are disposed.
	 * </p>
	 * <p>
	 * This method is useful for those databases with a big number of files,
	 * maybe at certain moment the application needs to read some information of
	 * from the files but later only a small portion of them is accessed, in
	 * this case the application can disconnect the not needed files.
	 * </p>
	 * 
	 * @param mdb
	 *            MDB file to disconnect.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 * @throws TimeoutException
	 *             This exception is sent if the session is busy. Check there is
	 *             not any open cursor before to call this method.
	 */
	public void disconnect(MDB<?> mdb) throws IOException, TimeoutException {
		_writeLock.lock();
		try {
			mdb.closeAppender();
			closeCursors(mdb);
			_cache.remove(mdb);
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Close the cursors.
	 * 
	 * @param mdb
	 *            The owner of the cursors, or <code>null</code> to close all
	 *            the cursors of the session.
	 * @throws TimeoutException
	 *             This exception is sent if the session is busy. Check there is
	 *             not any open cursor before to call this method.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	void closeCursors(MDB<?> mdb) throws TimeoutException, IOException {
		// close the deferred cursors
		for (ICursor<?> cur : _deferredCursors) {
			if (cur != null) {
				if (mdb == null || cur.getMDB() == mdb) {
					// we take the risk to close the cursor
					// and we hope the user will not it use
					// again.
					cur.close();
				}
			}
		}
		if (_closeTimeoutMillis > 0) {
			long t = currentTimeMillis();
			if (_debug) {
				out.println("Waiting for cursors to close the session...");
			}

			// wait for the reading threads to close the cursors
			// this "waiting" help to applications
			// where is difficult to synchronize the reading threads
			// with the one who closes the session.
			int count;
			while (true) {
				if (mdb == null) {
					count = getOpenCursorCount();
				} else {
					count = mdb.getOpenCursorCount();
				}
				if (count == 0) {
					break;
				}

				assert count >= 0 : "Bug: negative number of open cursors ("
						+ count + ")";

				try {
					if (_debug) {
						out.println(this + " await...");
					}
					_allCursorsClosedCond.await(10, TimeUnit.MILLISECONDS);
					if (currentTimeMillis() - t > _closeTimeoutMillis) {
						if (_debug) {
							for (MDB<?> mdb2 : _cache.values()) {
								int count2 = mdb2.getOpenCursorCount();
								if (count2 != 0) {
									out.println(mdb2 + " open cursors: "
											+ count2 + " - " + mdb2.getFile());
								}
							}
						}
						throw new TimeoutException("Waiting for " + count
								+ " cursors to close the session.");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (_debug) {
				out.println(this + " All cursors are closed, waiting time: "
						+ (currentTimeMillis() - t) + " - " + getRoot());
			}
		}

	}

	/**
	 * The session mode.
	 * 
	 * @return the mode
	 */
	public SessionMode getMode() {
		return _mode;
	}

	/**
	 * <p>
	 * The close timeout milliseconds. When the session is closing it waits for
	 * all the cursors to close, at certain time, if yet there are an open
	 * cursor then a {@link TimeoutException} is sent. By default the timeout is
	 * set to 3000 milliseconds.
	 * </p>
	 * <p>
	 * If you want to make the session to force the close operation, then set a
	 * timeout value below or equal to 0.
	 * </p>
	 * 
	 * @return The session close timeout. It is below or equal to 0 if the
	 *         session is set to force the close.
	 */
	public long getCloseTimeoutMillis() {
		return _closeTimeoutMillis;
	}

	/**
	 * <p>
	 * Set the close timeout in milliseconds.
	 * </p>
	 * <p>
	 * When the session is closing it waits for all the cursors to close, after
	 * certain time, if yet there are an open cursor then a
	 * {@link TimeoutException} is sent. By default the timeout is set to 3000
	 * milliseconds.
	 * </p>
	 * <p>
	 * If you want to make the session to force the close operation, then set a
	 * timeout value below or equal to 0.
	 * </p>
	 * 
	 * @param closeTimeoutMillis
	 *            The timeout, below or equal to 0 if you want to force the
	 *            close.
	 */
	public void setCloseTimeoutMillis(long closeTimeoutMillis) {
		_closeTimeoutMillis = closeTimeoutMillis;
	}

	/**
	 * The properties of this database. These properties are an alternative way
	 * to save descriptive data in your database.
	 * 
	 * @return The properties.
	 */
	public Properties getProperties() {
		return _properties;
	}

	/**
	 * Persist the properties.
	 * 
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 * 
	 */
	public void saveProperties() throws IOException {
		if (!_memory) {
			_writeLock.lock();
			try {
				final File file = new File(_root, "session.properties");
				file.getParentFile().mkdirs();
				file.createNewFile();
				try (FileOutputStream os = new FileOutputStream(file)) {
					_properties.store(os, "MDB Session properties");
				}
			} finally {
				_writeLock.unlock();
			}
		}
	}

	/**
	 * If the session is open.
	 * 
	 * @return <code>true</code> if it is open.
	 */
	public boolean isOpen() {
		return _open;
	}

	/**
	 * Get the number of open cursors in this session.
	 * 
	 * @return The cursor count.
	 */
	public int getOpenCursorCount() {
		return _openCursors;
	}

	/**
	 * The number of open appenders in this session.
	 * 
	 * @return The open appender count.
	 */
	public int getOpenAppenderCount() {
		_readLock.lock();
		try {
			int count = 0;
			final Collection<MDB<?>> list = getCache().values();

			for (final MDB<?> mdb : list) {
				count += mdb.isAppenderOpen() ? 1 : 0;
			}

			return count;
		} finally {
			_readLock.unlock();
		}
	}

	/**
	 * The root of the database.
	 * 
	 * @return The database folder.
	 */
	public File getRoot() {
		return _root;
	}

	/**
	 * The meta-data folder.
	 * 
	 * @return The meta-data directory.
	 */
	public File getMetadataDir() {
		return _metadataDir;
	}

	/**
	 * Get a file relative to the root of the database.
	 * 
	 * @param relPath
	 *            The relative path of the file.
	 * @return The file at the given <code>relPath</code>.
	 */
	public File getFile(final String relPath) {
		return new File(_root, relPath);
	}

	/**
	 * The name of the session.
	 * 
	 * @return The session name.
	 */
	public String getSessionName() {
		return _sessionName;
	}

	/**
	 * Close the session. Do not use closed sessions.
	 * 
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 * @throws TimeoutException
	 *             This exception is sent if the session is busy. Check there is
	 *             not any open cursor before to call this method.
	 */
	public void close() throws IOException, TimeoutException {
		close(true);
	}

	/**
	 * Visitor to copy a tree of files.
	 * 
	 * @author arian
	 * 
	 */
	static class CopyVisitor extends SimpleFileVisitor<Path> {
		private Path _from;
		private Path _to;

		public CopyVisitor(Path from, Path to) {
			super();
			_from = from;
			_to = to;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Path rel = _from.relativize(file);
			Path target = _to.resolve(rel);
			Files.createDirectories(target.getParent());
			Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
			return FileVisitResult.CONTINUE;
		}
	}

	/**
	 * Backup the current state of the database.
	 * 
	 * @param comment
	 *            A description of this backup.
	 * @return The backup reference. Use it to restore the backup.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public BackupVersion backup(String comment) throws IOException {
		if (_memory)
			throw new UnsupportedOperationException(
					"Backup of memory databases are not implemented yet.");

		_writeLock.lock();
		try {
			flush();
			saveProperties();

			BackupVersion version = backup(_root, comment);

			return version;
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Backup the file of the given MDB instance.
	 * 
	 * @param mdb
	 *            The file to backup.
	 * @param comment
	 *            A description of the backup.
	 * @return The backup reference. Use it to restore the backup.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public BackupVersion backupFile(MDB<?> mdb, String comment)
			throws IOException {
		return backupFile(mdb.getFile(), comment);
	}

	/**
	 * Backup a particular MDB file.
	 * 
	 * @param mdbFile
	 *            The file to backup.
	 * @param comment
	 *            A description of the backup.
	 * @return The backup reference. Use it to restore the backup.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public BackupVersion backupFile(File mdbFile, String comment)
			throws IOException {
		if (_memory)
			throw new UnsupportedOperationException(
					"Backup of memory databases are not implemented yet.");

		_writeLock.lock();
		try {
			if (!mdbFile.toPath().toAbsolutePath()
					.startsWith(_root.toPath().toAbsolutePath())) {
				throw new IOException("The file " + mdbFile
						+ " does not belong to this database: " + _root);
			}
			MDB<?> mdb = _cache.get(mdbFile);
			if (mdb != null) {
				mdb.flushAppender();
			}
			BackupVersion version = backupFile(_root, mdbFile, comment);
			return version;
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Backup an MDB file. Use this method if there is not an active session. In
	 * other case, use the session method {@link #backupFile(File, String)} or
	 * {@link #backupFile(MDB, String)}.
	 * 
	 * @param dbRoot
	 *            The root of the database.
	 * @param mdbFile
	 *            The file to backup.
	 * @param comment
	 *            The description of the backup.
	 * @return The backup reference.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public static BackupVersion backupFile(File dbRoot, File mdbFile,
			String comment) throws IOException {

		// mdRoot
		File arrayFile = new File(mdbFile.getPath() + ".array");
		Path relPath = dbRoot.toPath().relativize(mdbFile.toPath());

		Properties props = new Properties();
		props.put(KEY_VERSION_PARTIAL_BACKUP, relPath.toString());

		BackupVersion version = createVersion(dbRoot, comment, props, true);

		Path versionedFile = version.getPath().resolve(relPath);

		Files.copy(mdbFile.toPath(), versionedFile);

		if (arrayFile.exists()) {
			Files.copy(arrayFile.toPath(),
					versionedFile.resolveSibling(arrayFile.getName()));
		}

		Files.createFile(version.getPath().resolve("partial"));

		return version;
	}

	/**
	 * Backup the whole database. Use this method if there is not an active
	 * session. In other case, use the session method {@link #backup(String)}.
	 * 
	 * @param dbRoot
	 *            DB root
	 * @param comment
	 *            Comment of the backup
	 * @return The backup version
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public static BackupVersion backup(File dbRoot, String comment)
			throws IOException {
		BackupVersion version = createVersion(dbRoot, comment,
				new Properties(), false);

		Path versionPath = version.getPath();

		Files.walkFileTree(dbRoot.toPath(), new CopyVisitor(dbRoot.toPath(),
				versionPath));

		return version;
	}

	private static BackupVersion createVersion(File dbRoot, String comment,
			Properties props, boolean partial) throws IOException {
		final Path rootPath = dbRoot.toPath();
		Path backupDir = rootPath.resolveSibling("~" + dbRoot.getName());

		UUID id = UUID.randomUUID();
		Path versionPath = backupDir.resolve(id.toString());

		BackupVersion version = new BackupVersion(id, new Date(), comment,
				versionPath, rootPath, partial);

		props.put("Comment", comment);
		props.put("Date-Time", Long.toString(version.getDate().getTime()));

		Path propsPath = versionPath.resolve("version.properties");

		Files.createDirectories(propsPath.getParent());

		try (OutputStream stream = Files.newOutputStream(propsPath,
				StandardOpenOption.CREATE)) {

			props.store(stream, "");
		}
		return version;
	}

	/**
	 * <p>
	 * Restore the session to the given version. Be careful because the data of
	 * the database will be deleted and you cannot recover it. If you want, you
	 * can create a backup before restore a backup.
	 * </p>
	 * <p>
	 * Before to perform the copy, all the deferred cursors will be closed, but
	 * if there are a common cursor open, then an {@link IOException} exception
	 * is sent, so, be sure you closed all the cursors before. Take in
	 * consideration that when you restore a backup, maybe there are files that
	 * are not present in the current session, so the associated MDB will be
	 * invalid and removed from the session cache.
	 * </p>
	 * This method fails (it sends a {@link FileNotFoundException}) if the
	 * backup version was created only for one file (
	 * {@link #backupFile(File, String)} ) and the original file does not exist
	 * any more.
	 * 
	 * @param version
	 *            Backup version to restore.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 * @throws TimeoutException
	 *             This exception is sent if the session is busy. Check there is
	 *             not any open cursor before to call this method.
	 */
	public void restore(BackupVersion version) throws IOException,
			TimeoutException {
		_writeLock.lock();
		try {
			if (version.isPartial()) {
				restorePartial(_root, _cache, _deferredCursors, version);
			} else {
				for (MDB<?> mdb : _cache.values()) {
					mdb.disconnectFile();
				}

				closeCursors(null);

				for (MDB<?> mdb : _cache.values()) {
					if (mdb.getOpenCursorCount() > 0) {
						throw new IOException(
								"Cannot restore the backup, the file "
										+ mdb.getFile()
										+ " is locked by opened cursors.");
					}
				}

				restoreDatabase(_root, version);

				for (MDB<?> mdb : new ArrayList<>(_cache.values())) {
					if (!mdb.getFile().exists()) {
						err.println("The file "
								+ mdb.getFile()
								+ " is not present in the backup version "
								+ version.getDate()
								+ " - "
								+ version.getComment()
								+ ", from that reason it was removed from the session cache and if the user performs any operation on that file a FileNotFoundException will be sent.");
						_cache.remove(mdb.getFile());
					} else {
						mdb.reconnectFile();
					}
				}

				_properties.clear();
				try (InputStream in = Files.newInputStream(_propsFile.toPath())) {
					_properties.load(in);
				}
				readProperties();
			}
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Read the database properties of a particular backup.
	 * 
	 * @param version
	 *            The version to read.
	 * @return The properties of that backup.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public static Properties readBackupProperties(BackupVersion version)
			throws IOException {
		Properties props = new Properties();
		try (InputStream input = Files.newInputStream(version.getPath()
				.resolve("version.properties"))) {
			props.load(input);
		}
		return props;
	}

	private static void restorePartial(File dbRoot, Map<File, MDB<?>> mdbCache,
			List<ICursor<?>> deferredCursors, BackupVersion version)
			throws IOException {
		Properties props = readBackupProperties(version);

		String filename = props.getProperty(KEY_VERSION_PARTIAL_BACKUP);

		Path dbFile = dbRoot.toPath().resolve(filename);

		MDB<?> mdb = mdbCache == null ? null : mdbCache.get(dbFile.toFile());
		if (mdb != null) {
			for (ICursor<?> cursor : deferredCursors) {
				if (cursor != null) {
					if (cursor.getMDB() == mdb) {
						cursor.close();
					}
				}
			}

			if (mdb.getOpenCursorCount() > 0) {
				throw new IOException("Cannot restore the backup, the file "
						+ mdb.getFile() + " is locked by opened cursors.");
			}

			mdb.disconnectFile();
		}

		if (!Files.exists(dbFile)) {
			throw new FileNotFoundException(
					"File restore failed, the original file (" + dbFile
							+ ") does not exist.");
		}

		Path backupFile = version.getPath().resolve(filename);
		Files.copy(backupFile, dbFile, StandardCopyOption.REPLACE_EXISTING);

		String arrayFileName = backupFile.getFileName() + ".array";
		Path backupArrayfile = backupFile.resolveSibling(arrayFileName);
		if (Files.exists(backupArrayfile)) {
			Path dbArrayFile = dbFile.resolve(arrayFileName);

			// not always the array file exists, if the user does not add any
			// array element, the array file is not created.
			if (Files.exists(dbArrayFile)) {
				Files.copy(backupArrayfile, dbArrayFile,
						StandardCopyOption.REPLACE_EXISTING);
			}
		}

		if (mdb != null) {
			mdb.reconnectFile();
		}
	}

	/**
	 * Restore a backup. Use this method if there is not an active session. In
	 * other case, use the session method {@link #backup(String)}.
	 * 
	 * @param dbRoot
	 *            The database folder.
	 * @param version
	 *            The version to restore.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public static void restore(File dbRoot, BackupVersion version)
			throws IOException {
		if (version.isPartial()) {
			restorePartial(dbRoot, null, null, version);
		} else {
			restoreDatabase(dbRoot, version);
		}
	}

	private static void restoreDatabase(File dbRoot, BackupVersion version)
			throws IOException {
		// restore database
		delete(dbRoot);
		dbRoot.mkdirs();

		Path rootPath = dbRoot.toPath();
		Path versionPath = version.getPath();

		Files.walkFileTree(versionPath, new CopyVisitor(versionPath, rootPath));
		Files.delete(rootPath.resolve("version.properties"));
	}

	/**
	 * Get all the backups of a database.
	 * 
	 * @param dbPath
	 *            The path to the database folder.
	 * @return The list of backups.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files.
	 */
	public static List<BackupVersion> getBackups(Path dbPath)
			throws IOException {
		List<BackupVersion> list = new ArrayList<>();
		Path backupPath = dbPath.resolveSibling("~"
				+ dbPath.getName(dbPath.getNameCount() - 1));
		File[] files = backupPath.toFile().listFiles();
		if (files != null) {
			for (File f : files) {
				UUID id = UUID.fromString(f.getName());
				Properties props = new Properties();
				try (InputStream stream = Files.newInputStream(f.toPath()
						.resolve("version.properties"))) {
					props.load(stream);
				}
				String comment = props.getProperty("Comment", "");
				long millis = Long.parseLong(props.getProperty("Date-Time"));
				BackupVersion version = new BackupVersion(id, new Date(millis),
						comment, f.toPath(), dbPath,
						props.containsKey(KEY_VERSION_PARTIAL_BACKUP));
				list.add(version);
			}
		}
		return list;
	}

	/**
	 * Get all the backups.
	 * 
	 * @return The list of backups.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files. If
	 *             there is any problem writing to the session files.
	 */
	public List<BackupVersion> getBackups() throws IOException {
		return getBackups(_root.toPath());
	}

	private void close(final boolean releaseCache) throws IOException,
			TimeoutException {
		if (_debug) {
			out.println("Closing " + this + " - " + getRoot());
		}
		_writeLock.lock();
		try {
			if (_open) {
				_open = false;

				if (_filesTable != null) {
					_filesTable.close();
				}

				final Collection<MDB<?>> mdbList = _cache.values();

				for (final MDB<?> mdb : mdbList) {
					mdb.closeAppender();
				}

				closeCursors(null);

				if (releaseCache) {
					_cache = null;
				}
			}
		} catch (Exception e) {
			_open = true;
			throw e;
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Flush the session's appenders. It should be used after a recording
	 * process, to write pending records.
	 * 
	 * @throws IOException
	 *             If there is any problem writing/reading the session files. If
	 *             there is any problem writing to the session files.
	 */
	public void flush() throws IOException {
		_writeLock.lock();
		try {
			if (_open) {
				final Collection<MDB<?>> mdbList = _cache.values();

				for (final MDB<?> mdb : mdbList) {
					mdb.flushAppender();
				}
			}
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Get the session cache. This cache maps a file with an MDB instance.
	 * 
	 * @return The file and MDB instance map.
	 */
	public Map<File, MDB<?>> getCache() {
		return Collections.unmodifiableMap(_cache);
	}

	/**
	 * Close the session and delete all the files. If there are any cursor open,
	 * is possible this method will fail, specially in Windows operating
	 * systems.
	 * 
	 * @return The number of files cannot be deleted.
	 * @throws IOException
	 *             If there is any problem writing/reading the session files. If
	 *             there is any problem closing/deleting the session files.
	 * @throws TimeoutException
	 *             Exception sent if the session is busy and cannot be closed.
	 *             If you get this exception it means there are open cursors you
	 *             need to close before to call this method.
	 */
	public int closeAndDelete() throws IOException, TimeoutException {
		_writeLock.lock();
		try {
			if (_cache == null) {
				return 0;
			}

			close(false);

			int fail = 0;
			final Collection<MDB<?>> mdbList = _cache.values();

			for (final MDB<?> mdb : mdbList) {
				if (!mdb.deleteFiles()) {
					fail++;
				}
			}
			_cache = null;

			fail += delete(new File(_root, "session.properties"));

			if (_root != null) {
				fail += delete(_root);
			}

			if (_backupDir.exists()) {
				fail += delete(_backupDir);
			}

			return fail;
		} finally {
			_writeLock.unlock();
		}
	}

	/**
	 * Utility method to delete a tree (a directory or file).
	 * 
	 * @param tree
	 *            The tree to delete.
	 * @return The number of files cannot be deleted.
	 */
	public static int delete(final File tree) {
		int fail = 0;
		final File[] list = tree.listFiles();
		if (list != null) {
			for (final File f2 : list) {
				fail += delete(f2);
			}
		}
		if (tree.exists() && !tree.delete()) {
			java.lang.System.err.println("Cannot delete file " + tree);
			fail++;
		}
		return fail;
	}

	/**
	 * Delete the backup files.
	 * 
	 * @param version
	 *            Backup version to delete.
	 * @return If fails, it returns the number of non deleted files.
	 */
	public static int deleteBackup(BackupVersion version) {
		return delete(version.getPath().toFile());
	}
}
