package org.mfg.mdb.compiler;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.mfg.mdb.compiler.internal.IJSON;

/**
 * The schema of the database. Contains the table definitions and other
 * information.
 * 
 * @author arian
 * @see Table
 * 
 */
public class Schema extends ArrayList<Table> implements IJSON {

	private static final long serialVersionUID = 1L;

	private String _name;
	private String _source;
	private String _pkgName;
	private int _bufferSize;

	private static int COUNT = 0;

	/**
	 * Schema constructor.
	 * 
	 * @param name
	 *            The schema name, use a valid Java identifier because this name
	 *            is used as part of classes or method names.
	 */
	public Schema(String name) {
		_name = name;
		_bufferSize = 100;
		_source = "";
		_pkgName = "mdb";
	}

	/**
	 * Schema constructor, it uses a default schema name.
	 */
	public Schema() {
		this("Schema" + ++COUNT);
	}

	/**
	 * Create an schema from a JSON object.
	 * 
	 * @param obj
	 *            JSON object with the schema definitions.
	 */
	public Schema(JSONObject obj) {
		updateFromJSON(obj);
	}

	@Override
	public boolean add(Table e) {
		e.setSchema(this);
		return super.add(e);
	}

	@Override
	public void add(int index, Table element) {
		element.setSchema(this);
		super.add(index, element);
	}

	/**
	 * The path to the target source, see {@link Compiler#compile(String)}.
	 * 
	 * @return The path.
	 */
	public String getSource() {
		return _source;
	}

	/**
	 * Set the source path of the target project.
	 * 
	 * @param source
	 *            Source path.
	 */
	public void setSource(String source) {
		_source = source;
	}

	/**
	 * Get the package of the generated classes. See
	 * {@link Compiler#packageName(String)}.
	 * 
	 * @return The package name.
	 */
	public String getPkgName() {
		return _pkgName;
	}

	/**
	 * Set the package of the generated classes. See
	 * {@link Compiler#packageName(String)}.
	 * 
	 * @param pkgName
	 *            The package name.
	 */
	public void setPkgName(String pkgName) {
		_pkgName = pkgName;
	}

	/**
	 * Get the default buffer size of the appenders and cursors of the MDB
	 * 
	 * @return The default buffer size.
	 */
	// TODO: This should be moved to the table definition.
	public int getBufferSize() {
		return _bufferSize;
	}

	/**
	 * Set the default buffer size of the appenders and cursors of the MDB.
	 * 
	 * @param bufferSize
	 *            The default buffer.
	 */
	public void setBufferSize(int bufferSize) {
		_bufferSize = bufferSize;
	}

	/**
	 * Get the schema's name.
	 * 
	 * @return A name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the schema's name. Use a valid Java identifier name because this is
	 * used as part of classes and methods names.
	 * 
	 * @param name
	 *            The name.
	 */
	public void setName(String name) {
		_name = name;
	}

	@Override
	public void updateFromJSON(JSONObject obj) {
		clear();
		try {
			_source = obj.getString("source");
			_pkgName = obj.getString("packageName");
			_bufferSize = obj.getInt("bufferSize");

			// migrate from old schemas
			JSONObject obj2 = obj;
			if (obj.has("schema")) {
				obj2 = obj2.getJSONObject("schema");
			}
			_name = obj2.optString("name", "Schema1");

			JSONArray array = obj2.getJSONArray("tables");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj3 = array.getJSONObject(i);
				add(new Table(obj3));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	@Override
	public String toJSONString() {
		try {
			JSONStringer s = new JSONStringer();
			s.object();

			s.key("name");
			s.value(_name);

			s.key("source");
			s.value(_source);

			s.key("packageName");
			s.value(_pkgName);

			s.key("bufferSize");
			s.value(_bufferSize);

			s.key("tables");
			s.array();
			for (Table t : this) {
				s.value(t);
			}
			s.endArray();
			s.endObject();
			return s.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}