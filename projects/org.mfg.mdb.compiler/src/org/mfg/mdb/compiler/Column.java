package org.mfg.mdb.compiler;

import java.io.Serializable;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.mfg.mdb.compiler.internal.AbstractJSON;

/**
 * Definition of a column. Can be used by external tools like schema editors.
 * 
 * @author arian
 * @see Table
 * @see Schema
 */
public class Column extends AbstractJSON implements Serializable {
	private static final long serialVersionUID = 1L;

	private String _name;
	private UUID _uuid;
	private Type _type;
	private Order _order;
	private int _offset;
	private String _formula;
	private boolean _virtual;
	private int _index;

	/**
	 * Creates a column.
	 * 
	 * @param name
	 *            The name. Use a valid Java identifier name, because this one
	 *            is used as part of fields, parameters and methods names in the
	 *            generated code.
	 * @param type
	 *            The type.
	 * @param order
	 *            The order (ASC or DESC).
	 * @param virtual
	 *            If the column is virtual.
	 * @param formula
	 *            The formula. It is should be a valid Java expression. TODO:
	 *            Missing reference.
	 * @param offset
	 *            The byte-offset. TODO: Missing reference.
	 * @param uuid
	 *            A unique identifier.
	 */
	public Column(final String name, final Type type, final Order order,
			final boolean virtual, final String formula, final int offset,
			UUID uuid) {
		if (order != Order.NONE && !type.isNumber()) {
			throw new IllegalArgumentException(
					"The type "
							+ type
							+ " can not be ordered. You have to use other type or the order NONE");
		}
		_name = name;
		_uuid = uuid;
		_type = type;
		_order = order;
		_offset = offset;
		_formula = formula == null ? "" : formula;
		_virtual = virtual;
	}

	/**
	 * Create a column, it gets the information from a JSON object.
	 * 
	 * @param obj
	 *            The JSON object with the column information.
	 * @throws JSONException
	 *             If there is any JSON error.
	 * @see #updateFromJSON(JSONObject)
	 */
	public Column(final JSONObject obj) throws JSONException {
		updateFromJSON(obj);
	}

	/**
	 * The index of the column in the table definition.
	 * 
	 * @return The index.
	 */
	public int getIndex() {
		return _index;
	}

	/**
	 * Set the index of the column in the table definition.
	 * 
	 * @param index
	 *            The index.
	 */
	public void setIndex(int index) {
		this._index = index;
	}

	/**
	 * Get the column's name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the coumn's name.
	 * 
	 * @param name
	 *            Column name.
	 */
	public void setName(final String name) {
		this._name = name;
	}

	/**
	 * Get the column unique identifier.
	 * 
	 * @return The unique identifier.
	 */
	public UUID getUUID() {
		return _uuid;
	}

	/**
	 * Get the column type.
	 * 
	 * @return The column's type.
	 */
	public Type getType() {
		return _type;
	}

	/**
	 * Set the column type
	 * 
	 * @param type
	 *            The type
	 */
	public void setType(final Type type) {
		_type = type;
	}

	/**
	 * Get the column's order.
	 * 
	 * @return The order.
	 * @see Order
	 */
	public Order getOrder() {
		return _order;
	}

	/**
	 * Get the column's byte-offset. TODO: Missing reference.
	 * 
	 * @return The byte-offset.
	 */
	public int getOffset() {
		return _offset;
	}

	/**
	 * Set the column order.
	 * 
	 * @param order
	 *            Order
	 */
	public void setOrder(final Order order) {
		_order = order;
	}

	/**
	 * The formula of the virtual column. See {@link #isVirtual()}. A formula
	 * string should be a well formed java expression (r-value), used as value
	 * of the column. There are some predefined "variables" that can be used in
	 * a formula:
	 * <ul>
	 * <li><code>$pos$</code>: It is the record position. It can be used to get
	 * an auto-increment field.
	 * <li><code>$$</code>: It is a reference to the record. For example, if we
	 * want to store fake and real prices we use a <code>rawPrice</code> of type
	 * <code>long</code>, if the price is real, we use a positive number, else
	 * we use a negative number. Then, we define 3 columns:
	 * <code>rawPrice</code>, <code>price</code> and <code>real</code>, where
	 * <code>price</code> and <code>real</code> are virtual:
	 * 
	 * <pre>
	 * 
	 * Formula of price:
	 * 		"java.lang.Math.abs($$.rawPrice)"
	 * 
	 * Formula of real: 
	 *  		$$.rawPrice &lt; 0
	 * </pre>
	 * 
	 * </li>
	 * </ul>
	 * 
	 * TODO: This explanation should be moved to the main documentation and here
	 * we just put a reference.
	 * 
	 * 
	 * @see #isVirtual()
	 * @return the formula
	 */
	public String getFormula() {
		return _formula;
	}

	/**
	 * If the {@link #getFormula()} uses the <code>$pos$</code> variable.
	 * 
	 * @return If it uses the position variable.
	 */
	public boolean isFormulaBasedInPosition() {
		return _virtual && _formula.contains("$pos$");
	}

	/**
	 * Set the formula.
	 * 
	 * @param formula
	 *            the formula to set
	 */
	public void setFormula(final String formula) {
		_formula = formula;
	}

	/**
	 * If this is a virtual column. //TODO: Missing reference.
	 * 
	 * @see #getFormula()
	 * @return If is virtual.
	 */
	public boolean isVirtual() {
		return _virtual;
	}

	/**
	 * Set if this is virtual.
	 * 
	 * @param virtual
	 *            the virtual to set
	 */
	public void setVirtual(final boolean virtual) {
		_virtual = virtual;
	}

	/**
	 * Shortcut for <code>getType().getJavaType().getSimpleName()</code>.
	 * 
	 * @return The Java type name, like in a var declaration.
	 */
	public String getJavaTypeName() {
		return getType().getJavaType().getSimpleName();
	}

	@Override
	public void toJSON(final JSONStringer s) throws JSONException {
		s.key("name");
		s.value(_name);

		s.key("uuid");
		s.value(_uuid.toString());

		s.key("type");
		s.value(_type.name());

		s.key("order");
		s.value(_order.name());

		s.key("virtual");
		s.value(_virtual);

		s.key("formula");
		s.value(_formula);
	}

	@Override
	public void updateFromJSON(final JSONObject obj) throws JSONException {
		_name = obj.getString("name");
		_uuid = UUID.fromString(obj.optString("uuid", UUID.randomUUID()
				.toString()));
		_type = Type.valueOf(obj.getString("type"));
		_order = Order.valueOf(obj.getString("order"));
		_formula = obj.optString("formula", "");
		if (_formula == null) {
			_formula = "";
		}
		_virtual = obj.optBoolean("virtual", false);
	}
}