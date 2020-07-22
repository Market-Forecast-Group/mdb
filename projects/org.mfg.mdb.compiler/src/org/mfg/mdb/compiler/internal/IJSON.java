package org.mfg.mdb.compiler.internal;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * Interface for classes with JSON serialization.
 * 
 * @author arian
 * 
 */
public interface IJSON extends JSONString {
	/**
	 * Update the state of this instance with the given JSON object.
	 * 
	 * @param obj
	 *            JSON Object.
	 * @throws JSONException
	 *             If there is any JSON error.
	 */
	public void updateFromJSON(JSONObject obj) throws JSONException;
}
