package org.mfg.mdb.compiler;

/**
 * The order of a column. The MDB runtime assumes all the
 * values of this columns will accomplish the given order. The order is used to
 * generate "indexOf" methods that perform binary searches to find the index of
 * a value. You can see this like the equivalent of the indexes in SQL systems.
 * 
 * @author arian
 * 
 */
public enum Order {
	/**
	 * Ascending order.
	 */
	ASCENDING,

	/**
	 * Descending order.
	 */
	DESCENDING,

	/**
	 * No order, no "indexOf" methods are generated for this column.
	 */
	NONE;

	/**
	 * A shorter method to know if this is {@link #NONE}.
	 * 
	 * @return If this is {@link #NONE}.
	 */
	public boolean isNone() {
		return this == NONE;
	}
}
