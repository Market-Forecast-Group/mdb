package org.mfg.mdb.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.mfg.mdb.compiler.internal.IJSON;

/**
 * Definition of a table. Can be used by external tools like schema editors.
 * 
 * @author arian
 * 
 */
public class Table extends LinkedList<Column> implements IJSON {

	private static final long serialVersionUID = 1L;

	private String _name;
	private UUID _uuid;

	private Schema _schema;

	/**
	 * The constructor.
	 * 
	 * @param name
	 *            The name. Use a valid identifier. This can be used as part of
	 *            class or method name.
	 * @param id
	 *            A unique identifier.
	 */
	public Table(final String name, UUID id) {
		if (name.length() == 0) {
			throw new IllegalArgumentException();
		}
		_name = name;
		_uuid = id;
	}

	/**
	 * Create a table definition with the data of the given JSON object.
	 * 
	 * @param obj
	 *            JSON object.
	 * @throws JSONException
	 *             If there is any JSON error.
	 */
	public Table(final JSONObject obj) throws JSONException {
		updateFromJSON(obj);
	}

	/**
	 * The whole database schema.
	 * 
	 * @return The schema definitions.
	 */
	public Schema getSchema() {
		return _schema;
	}

	/**
	 * Set the schema.
	 * 
	 * @param schema
	 *            The schema.
	 */
	public void setSchema(Schema schema) {
		_schema = schema;
	}

	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the name.
	 * 
	 * @param name
	 *            The name.
	 */
	public void setName(final String name) {
		_name = name;
	}

	/**
	 * The unique identifier.
	 * 
	 * @return Unique identifier.
	 */
	public UUID getUUID() {
		return _uuid;
	}

	/**
	 * This method return the size (in bytes) of a record.
	 * 
	 * @return The row size in bytes
	 */
	public int getRowSize() {
		int size = 0;
		for (final Column col : this) {
			if (!col.isVirtual()) {
				size += col.getType().getSize();
			}
		}
		return size;
	}

	/**
	 * If the table contains at least one virtual column that is based on the
	 * position variable.
	 * 
	 * @see Column#isFormulaBasedInPosition()
	 * @return True if it is based in the position variable.
	 */
	public boolean containsVirtualColumnsBasedOnPosition() {
		return stream().anyMatch(c -> c.isFormulaBasedInPosition());
	}

	/**
	 * Get the list of columns are virtual.
	 * 
	 * @return A list with the virtual columns.
	 */
	public List<Column> virtualColumns() {
		List<Column> list = new ArrayList<>();
		for (Column c : this) {
			if (c.isVirtual()) {
				list.add(c);
			}
		}
		return list;
	}

	/**
	 * Get the list of columns are not virtual.
	 * 
	 * @return A list with the non-virtual columns.
	 */
	public List<Column> realColumns() {
		List<Column> list = new ArrayList<>();
		for (Column c : this) {
			if (!c.isVirtual()) {
				list.add(c);
			}
		}
		return list;
	}

	/**
	 * Get the list of the non-virtual columns with primitive type.
	 * 
	 * @return A list with the non-virtual columns.
	 */
	public List<Column> realPrimColumns() {
		List<Column> list = new ArrayList<>();
		for (Column c : this) {
			if (!c.isVirtual() && !c.getType().isArray()) {
				list.add(c);
			}
		}
		return list;
	}

	@Override
	public void updateFromJSON(final JSONObject obj) throws JSONException {
		clear();

		_name = obj.getString("name");
		_uuid = UUID.fromString(obj.optString("uuid", UUID.randomUUID()
				.toString()));
		JSONArray array = obj.getJSONArray("columns");
		for (int i = 0; i < array.length(); i++) {
			Column c = new Column(array.getJSONObject(i));
			c.setIndex(i);
			add(c);
		}

	}

	@Override
	public String toJSONString() {
		final JSONStringer s = new JSONStringer();
		try {
			s.object();

			s.key("uuid");
			s.value(_uuid.toString());

			s.key("name");
			s.value(_name);

			s.key("columns");
			s.array();
			for (final Column col : this) {
				s.value(col);
			}
			s.endArray();

			s.endObject();

			return s.toString();
		} catch (final JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	/**
	 * If this table contains at least one column with an array type.
	 * 
	 * @return If has an array column.
	 */
	public boolean hasArray() {
		for (final Column c : this) {
			if (c.getType().isArray() && !c.isVirtual()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this table contains at least one column with an string type.
	 * 
	 * @return If has an string column.
	 */
	public boolean hasString() {
		for (final Column c : this) {
			if (c.getType() == Type.STRING && !c.isVirtual()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the given table contains at least one virtual column.
	 * 
	 * @param table
	 *            The table.
	 * 
	 * @return If has an array column.
	 */
	public static boolean hasVirtualColumns(final List<Column> table) {
		for (final Column c : table) {
			if (c.isVirtual()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this table contains at least one column with an array type.
	 * 
	 * @return If has an array column.
	 */
	public boolean hasVirtualColumns() {
		return hasVirtualColumns(this);
	}

	/**
	 * Compute the signature of this table. A signature is formed by the name of
	 * the columns and his types. The signature is used to check if an old MDB
	 * file is yet compatible with the current version of the schema.
	 * 
	 * @return The table signature.
	 */
	public String computeSignature() {
		StringBuilder sb = new StringBuilder();
		for (Column c : this) {
			if (!c.isVirtual()) {
				sb.append(c.getUUID() + " " + c.getType().toString() + "; ");
			}
		}
		return sb.toString();
	}

}