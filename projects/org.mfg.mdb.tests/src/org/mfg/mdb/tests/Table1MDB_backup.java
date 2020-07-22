package org.mfg.mdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mfg.mdb.runtime.BackupVersion;
import org.mfg.mdb.runtime.MDBList;
import org.mfg.mdb.runtime.MDBSession;
import org.mfg.mdb.tests.mdb.JUnitTestsMDBSession;
import org.mfg.mdb.tests.mdb.Table1MDB;
import org.mfg.mdb.tests.mdb.Table1MDB.Appender;
import org.mfg.mdb.tests.mdb.Table1MDB.Cursor;
import org.mfg.mdb.tests.mdb.Table1MDB.Record;


// the meta-data is not ready yet
public class Table1MDB_backup {
	private static final int NUM_ROWS = 231;
	private static final int NUM_VERSIONS = 10;
	private JUnitTestsMDBSession _session;
	private Table1MDB _mdb;
	private Table1MDB.Appender _app;

	@Before
	public void before() throws IOException {
		assertFalse(AllTests.TEST_MEMORY);

		File root = new File("db-test-" + getClass().getSimpleName());

		MDBSession.delete(root);
		assertFalse(root.exists());

		_session = new JUnitTestsMDBSession("test-first", root);

		_mdb = _session.connectTo_Table1MDB("test.mdb");
		_app = _mdb.appender();
	}

	@After
	public void after() throws IOException, TimeoutException {
		_session.closeAndDelete();
		assertFalse(_session.getRoot().exists());
	}

	@Test
	public void backup_delete() throws IOException {
		BackupVersion v1 = _session.backup("v1");
		BackupVersion v2 = _session.backupFile(_mdb, "v2");

		List<BackupVersion> backups = _session.getBackups();
		assertEquals("Two backups", 2, backups.size());

		MDBSession.deleteBackup(v1);

		backups = _session.getBackups();
		assertEquals("One backups", 1, backups.size());

		MDBSession.deleteBackup(v2);

		backups = _session.getBackups();
		assertEquals("No backups", 0, backups.size());
	}

	@Test
	public void backup_file() throws IOException, TimeoutException {
		BackupVersion version = _session.backupFile(_mdb,
				"first single file backup");

		assertEquals("Partial version", Boolean.TRUE,
				Boolean.valueOf(version.isPartial()));

		_app.int_0 = 1;
		_app.append();

		_app.int_0 = 2;
		_app.append();

		// _app.flush(); in this way we get the bug about to the _bufSize that
		// is not set 0 at disconnect the file.
		Record[] all = _mdb.selectAll(_mdb.thread_cursor());
		assertEquals("Len before restore", 2, all.length);

		_session.restore(version);

		all = _mdb.selectAll(_mdb.thread_cursor());
		assertEquals("Len after restore", 0, all.length);
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_test_asList() throws IOException, TimeoutException {
		List<Record> memList = new ArrayList<>();
		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.array_int_1 = new int[] { i, i + 2 };
			r.double_0 = i;
			r.int_0 = 1;
			memList.add(r);
		}

		boolean onlyBackupFile = false;
		JUnitTestsMDBSession s = new JUnitTestsMDBSession("backup_test_asList",
				new File("backup_test_asList"));
		s.setDebug(true);
		Table1MDB m = s.connectTo_Table1MDB("test.mdb");

		// if you use just asList() it uses
		// a non
		// deferred cursor (the same of
		// asList(false)), so you have to close
		// the list before do a restore.
		MDBList<Record> dbList = m.list(m.thread_randomCursor());

		for (Record r : memList) {
			dbList.add(r);
		}

		BackupVersion v1 = onlyBackupFile ? s.backupFile(m, "v1") : s
				.backup("v1");

		for (int i = 0; i < dbList.size(); i++) {
			assertEquals("record[" + i + "]", memList.get(i).toString(), dbList
					.get(i).toString());
		}

		for (Record r : memList) {
			dbList.add(r);
		}

		assertEquals("Double of size", memList.size() * 2, dbList.size());

		s.restore(v1);

		assertEquals("Same size", memList.size(), dbList.size());
		try {
			for (int i = 0; i < dbList.size(); i++) {
				assertEquals("record[" + i + "]", memList.get(i).toString(),
						dbList.get(i).toString());
			}
			fail("The cursor under the list was closed, but not IOException was sent");
		} catch (Exception e) {
			// expected
		}

		// request the list again, it must work.
		dbList = m.list(m.thread_randomCursor());
		for (int i = 0; i < dbList.size(); i++) {
			assertEquals("record[" + i + "]", memList.get(i).toString(), dbList
					.get(i).toString());
		}

		s.closeAndDelete();
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_file_fail() throws IOException, TimeoutException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession("backup_file_fail",
				new File("backup_file_fail"));

		BackupVersion empty = s.backup("empty");

		Table1MDB m = s.connectTo_Table1MDB("test.mdb");
		m.appender().append();

		BackupVersion onefile = s.backupFile(m, "backup one file");

		s.restore(empty); // delete the file

		try {
			s.restore(onefile); // wrong! because the file does not exist
								// anymore.
			fail("The file does not exist, so it cannot be recovered");
		} catch (FileNotFoundException e) {
			// expected
		}

		s.closeAndDelete();
	}

	@Test
	public void backup_file_and_test_cursors() throws IOException,
			TimeoutException {
		utility___backup_and_test_cursors(true);
	}

	@Test
	public void backup_and_test_cursors() throws IOException, TimeoutException {
		utility___backup_and_test_cursors(false);
	}

	public void utility___backup_and_test_cursors(boolean singleFileBackup)
			throws IOException, TimeoutException {
		List<Record> list = new ArrayList<>();

		for (int i = 0; i < NUM_ROWS; i++) {
			Record r = new Record();
			r.int_0 = i;
			r.array_int_1 = new int[] { i };
			list.add(r);
		}

		for (Record r : list) {
			_app.int_0 = r.int_0;
			_app.array_int_1 = r.array_int_1;
			_app.append();
		}

		Cursor c = _mdb.cursor();
		c.next();
		c.next();

		Cursor c2 = _mdb.cursor();
		_session.defer(c2);
		c2.next();
		c2.next();

		_session.getProperties().put("key", "v1");
		BackupVersion v1 = singleFileBackup ? _session.backupFile(_mdb,
				"backup 1") : _session.backup("backup 1");

		c.close();

		for (Record r : list) {
			_app.int_0 = r.int_0;
			_app.array_int_1 = r.array_int_1;
			_app.append();
		}
		_app.flush();

		Record[] all = _mdb.selectAll(_mdb.thread_cursor());

		assertEquals("Double number of rows of initial data", list.size() * 2,
				all.length);

		_session.getProperties().put("key", "v2");
		_session.saveProperties();
		_session.restore(v1);

		_mdb = _session.connectTo_Table1MDB("test.mdb");
		all = _mdb.selectAll(_mdb.thread_cursor());

		assertEquals("Same number of rows of v1 and initial data", list.size(),
				all.length);
		if (singleFileBackup) {
			assertEquals(_session.getProperties().get("key"), "v2");
		} else {
			assertEquals(_session.getProperties().get("key"), "v1");
		}

		int fail = _session.closeAndDelete();
		assertEquals("All session file was deleted successfully", 0, fail);
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_file_N_then_restore_N() throws IOException,
			TimeoutException {
		utility__backup_N_then_restore_N(true, "backup_file_N_then_restore_N");
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_N_then_restore_N() throws IOException, TimeoutException {
		utility__backup_N_then_restore_N(false, "backup_N_then_restore_N");
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_file_restore_from_initial_state() throws IOException,
			TimeoutException {
		utility__backup_restore_from_initial_state(false,
				"backup_file_restore_from_initial_state");
	}

	private static void utility__backup_N_then_restore_N(
			boolean singleFileBackup, String dbname) throws IOException,
			TimeoutException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession(dbname, new File(
				dbname));
		List<Record[]> membackups = new ArrayList<>();
		{
			List<Record> memdb = new ArrayList<>();
			for (int i = 0; i < NUM_VERSIONS; i++) {
				for (int j = 0; j < NUM_ROWS; j++) {
					Record r = new Record();
					r.array_int_1 = new int[] { i, j };
					r.double_0 = i;
					r.int_0 = 1;
					memdb.add(r);
				}
				membackups.add(memdb.toArray(new Record[memdb.size()]));
			}
		}

		Table1MDB m = s.connectTo_Table1MDB("test.mdb");
		Appender a = m.appender();
		BackupVersion[] backups = new BackupVersion[NUM_ROWS];
		for (int i = 0; i < NUM_VERSIONS; i++) {
			for (int j = 0; j < NUM_ROWS; j++) {
				Record r = new Record();
				r.array_int_1 = new int[] { i, j };
				r.double_0 = i;
				r.int_0 = 1;
				a.append(r);
			}
			backups[i] = singleFileBackup ? s.backupFile(m, "v" + i) : s
					.backup("v" + i);
		}

		for (int i = 0; i < NUM_VERSIONS; i++) {
			BackupVersion v = backups[i];
			Record[] membackup = membackups.get(i);
			s.restore(v);
			Record[] all = m.selectAll(m.thread_cursor());
			assertEquals("v" + i + ".length", membackup.length, all.length);
			for (int j = 0; j < all.length; j++) {
				assertEquals("v" + i + "[" + j + "]", membackup[j].toString(),
						all[j].toString());
			}
		}

		s.closeAndDelete();

	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_restore_from_initial_state() throws IOException,
			TimeoutException {
		utility__backup_restore_from_initial_state(false,
				"backup_restore_from_initial_state");
	}

	public static void utility__backup_restore_from_initial_state(
			boolean onlyBackupFile, String dbname) throws IOException,
			TimeoutException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession(dbname, new File(
				dbname));
		List<Record[]> membackups = new ArrayList<>();
		{
			for (int i = 0; i < NUM_VERSIONS; i++) {
				List<Record> memdb = new ArrayList<>();
				for (int j = 0; j < NUM_ROWS; j++) {
					Record r = new Record();
					r.array_int_1 = new int[] { i, j };
					r.double_0 = i;
					r.int_0 = 1;
					memdb.add(r);
				}
				membackups.add(memdb.toArray(new Record[memdb.size()]));
			}
		}

		Table1MDB m = s.connectTo_Table1MDB("test.mdb");
		BackupVersion initial = onlyBackupFile ? s.backupFile(m, "initial") : s
				.backup("initial");

		Appender a = m.appender();
		BackupVersion[] backups = new BackupVersion[NUM_ROWS];
		for (int i = 0; i < NUM_VERSIONS; i++) {
			s.restore(initial);
			for (int j = 0; j < NUM_ROWS; j++) {
				Record r = new Record();
				r.array_int_1 = new int[] { i, j };
				r.double_0 = i;
				r.int_0 = 1;
				a.append(r);
			}
			backups[i] = onlyBackupFile ? s.backupFile(m, "v" + i) : s
					.backup("v" + i);
		}

		for (int i = 0; i < NUM_VERSIONS; i++) {
			BackupVersion v = backups[i];
			Record[] membackup = membackups.get(i);
			s.restore(v);
			Record[] all = m.selectAll(m.thread_cursor());
			assertEquals("v" + i + ".length", membackup.length, all.length);
			for (int j = 0; j < all.length; j++) {
				assertEquals("v" + i + "[" + j + "]", membackup[j].toString(),
						all[j].toString());
			}
		}

		s.closeAndDelete();
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_restore_diff_files() throws IOException,
			TimeoutException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession(
				"backup_restore_diff_files", new File(
						"backup_restore_diff_files"));

		// backup 3 files
		Table1MDB[] cache = new Table1MDB[6];
		for (int i = 0; i < 3; i++) {
			Table1MDB m = s.connectTo_Table1MDB("test" + i + ".mdb");
			cache[i] = m;
			Appender a = m.appender();
			for (int j = 0; j < NUM_ROWS; j++) {
				Record r = new Record();
				r.array_int_1 = new int[] { i, j };
				r.double_0 = i;
				r.int_0 = 1;
				a.append(r);
			}
		}
		BackupVersion v_3_files = s.backup("files from 0 - 2");

		// create other 3 files
		for (int i = 3; i < 6; i++) {
			Table1MDB m = s.connectTo_Table1MDB("test" + i + ".mdb");
			cache[i] = m;
			Appender a = m.appender();
			for (int j = 0; j < NUM_ROWS; j++) {
				Record r = new Record();
				r.array_int_1 = new int[] { i, j };
				r.double_0 = i;
				r.int_0 = 1;
				a.append(r);
			}
		}

		s.restore(v_3_files);

		// m4 is an MDB that is not present in the
		// restored backup, so it was disconnected
		// from the files, and any access to it
		// should throws a FileNotFoundException.
		Table1MDB m4 = cache[4];
		try (Table1MDB.RandomCursor c4 = m4.randomCursor()) {
			m4.indexOfDouble_0(c4, 0);
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		try {
			m4.cursor();
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}

		try {
			m4.thread_cursor();
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		try {
			m4.appender().append();
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		s.closeAndDelete();
	}

	@SuppressWarnings("static-method")
	@Test
	public void backup_restore_empty() throws IOException, TimeoutException {
		JUnitTestsMDBSession s = new JUnitTestsMDBSession(
				"backup_restore_empty", new File("backup_restore_empty"));

		BackupVersion empty = s.backup("Empty DB backup");

		Table1MDB m = s.connectTo_Table1MDB("test.mdb");
		m.appender().append(); // create a file and appends one record

		s.restore(empty);

		try (Table1MDB.RandomCursor c = m.randomCursor()) {
			m.indexOfDouble_0(c, 0);
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		try {
			m.cursor();
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		try {
			m.appender().append();
			fail("FileNotFoundException expected");
		} catch (FileNotFoundException e) {
			// expected
		}
		s.closeAndDelete();
	}
}
