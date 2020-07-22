package org.mfg.mdb.runtime;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

/**
 * Contains the information of a backup.
 * 
 * @author arian
 * 
 */
public class BackupVersion implements Comparable<BackupVersion> {
	private UUID _id;
	private Date _date;
	private String _comment;
	private Path _path;
	private Path _dbRoot;
	private boolean _partial;

	BackupVersion(UUID id, Date date, String comment, Path path, Path dbRoot,
			boolean partial) {
		super();
		_id = id;
		_date = date;
		_comment = comment;
		_path = path;
		_dbRoot = dbRoot;
		_partial = partial;
	}

	/**
	 * The unique identifier of the backup.
	 * 
	 * @return The unique identifier.
	 */
	public UUID getId() {
		return _id;
	}

	/**
	 * The date of the backup.
	 * 
	 * @return The date.
	 */
	public Date getDate() {
		return _date;
	}

	/**
	 * The comment of the backup.
	 * 
	 * @return The comment.
	 */
	public String getComment() {
		return _comment;
	}

	/**
	 * The backup path.
	 * 
	 * @return The path.
	 */
	public Path getPath() {
		return _path;
	}

	/**
	 * Path to the database.
	 * 
	 * @return The path.
	 */
	public Path getDbRoot() {
		return _dbRoot;
	}

	/**
	 * If it is partial backup. A partial backup only contains the content of
	 * one MDB file and not the whole database.
	 * 
	 * @return <code>true</code> if this is partial.
	 */
	public boolean isPartial() {
		return _partial;
	}

	@Override
	public int compareTo(BackupVersion o) {
		return _date.compareTo(o._date);
	}
}
