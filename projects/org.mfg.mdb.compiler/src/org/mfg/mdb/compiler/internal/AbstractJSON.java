package org.mfg.mdb.compiler.internal;

import org.json.JSONException;
import org.json.JSONStringer;

/**
 * Extend this class to get a JSON serialization pattern.
 * 
 * @author arian
 * 
 */
public abstract class AbstractJSON implements IJSON {
	@Override
	public String toJSONString() {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			toJSON(s);
			s.endObject();
			return s.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Serialize to JSON.
	 * 
	 * @param stringer
	 *            The JSON stringer.
	 * @throws JSONException
	 *             If there is any JSON error.
	 */
	public abstract void toJSON(JSONStringer stringer) throws JSONException;
}
