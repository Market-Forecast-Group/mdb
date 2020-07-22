package org.mfg.mdb.compiler;

import java.nio.ByteBuffer;

import org.mfg.mya.MyA;

/**
 * The type of a column. Contains some helper
 * methods used by the {@link MyA} templates of the compiler.
 * 
 * @author arian
 * 
 */
@SuppressWarnings("boxing")
public enum Type {
	/**
	 * The equivalent of {@link Byte}.
	 */
	BYTE {

		@Override
		public int getSize() {
			return 1;
		}

		@Override
		public String getGetName() {
			// ByteBuffer.get(), without no suffix
			return "";
		}

		@Override
		public Class<?> getJavaType() {
			return byte.class;
		}

		@Override
		public Byte readValue(ByteBuffer buf) {
			return buf.get();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.put((Byte) value);
		}

		@Override
		public Object parseString(String str) {
			return Byte.parseByte(str);
		}

		@Override
		public Object createDefault() {
			return (byte) 0;
		}

		@Override
		public String boxing(String expr) {
			return "Byte.valueOf(" + expr + ")";
		}
	},
	/**
	 * The equivalent of {@link Short}.
	 */
	SHORT {
		@Override
		public int getSize() {
			return Short.SIZE / Byte.SIZE;
		}

		@Override
		public String getGetName() {
			return "Short";
		}

		@Override
		public Class<?> getJavaType() {
			return short.class;
		}

		@Override
		public Object readValue(ByteBuffer buf) {
			return buf.getShort();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.putShort((Short) value);
		}

		@Override
		public Object parseString(String str) {
			return Short.parseShort(str);
		}

		@Override
		public Object createDefault() {
			return (short) 0;
		}

		@Override
		public String boxing(String expr) {
			return "Short.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of {@link Integer}.
	 */
	INTEGER {
		@Override
		public int getSize() {
			return Integer.SIZE / Byte.SIZE;
		}

		@Override
		public String getGetName() {
			return "Int";
		}

		@Override
		public Class<?> getJavaType() {
			return int.class;
		}

		@Override
		public Integer readValue(ByteBuffer buf) {
			return buf.getInt();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.putInt((Integer) value);
		}

		@Override
		public Object parseString(String str) {
			return Integer.parseInt(str);
		}

		@Override
		public Object createDefault() {
			return 0;
		}

		@Override
		public String boxing(String expr) {
			return "Integer.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of {@link Long}.
	 */
	LONG {
		@Override
		public int getSize() {
			return Long.SIZE / Byte.SIZE;
		}

		@Override
		public String getGetName() {
			return "Long";
		}

		@Override
		public Class<?> getJavaType() {
			return long.class;
		}

		@Override
		public Long readValue(ByteBuffer buf) {
			return buf.getLong();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.putLong((Long) value);
		}

		@Override
		public Object parseString(String str) {
			return Long.parseLong(str);
		}

		@Override
		public Object createDefault() {
			return (long) 0;
		}

		@Override
		public String boxing(String expr) {
			return "Long.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of {@link Float}.
	 */
	FLOAT {
		@Override
		public int getSize() {
			return Float.SIZE / Byte.SIZE;
		}

		@Override
		public String getGetName() {
			return "Float";
		}

		@Override
		public Class<?> getJavaType() {
			return float.class;
		}

		@Override
		public Float readValue(ByteBuffer buf) {
			return buf.getFloat();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.putFloat((Float) value);
		}

		@Override
		public Object parseString(String str) {
			return Float.parseFloat(str);
		}

		@Override
		public Object createDefault() {
			return (float) 0;
		}

		@Override
		public String boxing(String expr) {
			return "Float.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of {@link Double}.
	 */
	DOUBLE {
		@Override
		public int getSize() {
			return Double.SIZE / Byte.SIZE;
		}

		@Override
		public String getGetName() {
			return "Double";
		}

		@Override
		public Class<?> getJavaType() {
			return double.class;
		}

		@Override
		public Double readValue(ByteBuffer buf) {
			return buf.getDouble();
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			buf.putDouble((Double) value);
		}

		@Override
		public Object parseString(String str) {
			return Double.parseDouble(str);
		}

		@Override
		public Object createDefault() {
			return (double) 0;
		}

		@Override
		public String boxing(String expr) {
			return "Double.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of {@link Boolean}.
	 */
	BOOLEAN {
		@Override
		public int getSize() {
			return 1;
		}

		@Override
		public String getGetName() {
			// it would be ByteBuffer.get(), without no suffix
			// but lets avoid mistakes
			return "Boolean";
		}

		@Override
		public Class<?> getJavaType() {
			return boolean.class;
		}

		@Override
		public Boolean readValue(ByteBuffer buf) {
			return buf.get() == 1;
		}

		@Override
		public void writeValue(ByteBuffer buf, Object value) {
			byte b;
			if (value instanceof Boolean) {
				b = (byte) ((Boolean) value ? 1 : 0);
			} else {
				b = (byte) (((Byte) value).byteValue() == 1 ? 1 : 0);
			}
			buf.put(b);
		}

		@Override
		public Object parseString(String str) {
			return Boolean.parseBoolean(str);
		}

		@Override
		public Object createDefault() {
			return (byte) 1;
		}

		@Override
		public String boxing(String expr) {
			return "Boolean.valueOf(" + expr + ")";
		}
	},

	/**
	 * The equivalent of an array of {@link Double}.
	 */
	ARRAY_DOUBLE {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return double[].class;
		}

		@Override
		public Type getElementType() {
			return DOUBLE;
		}

		@Override
		public Object createDefault() {
			return new double[0];
		}
	},

	/**
	 * The equivalent of an array of {@link Float}.
	 */
	ARRAY_FLOAT {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return float[].class;
		}

		@Override
		public Type getElementType() {
			return FLOAT;
		}

		@Override
		public Object createDefault() {
			return new float[0];
		}
	},

	/**
	 * The equivalent of an array of {@link Long}.
	 */
	ARRAY_LONG {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return long[].class;
		}

		@Override
		public Type getElementType() {
			return LONG;
		}

		@Override
		public Object createDefault() {
			return new long[0];
		}
	},
	/**
	 * The equivalent of an array of {@link Integer}.
	 */
	ARRAY_INTEGER {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return int[].class;
		}

		@Override
		public Type getElementType() {
			return INTEGER;
		}

		@Override
		public Object createDefault() {
			return new int[0];
		}
	},

	/**
	 * The equivalent of an array of {@link Short}.
	 */
	ARRAY_SHORT {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return short[].class;
		}

		@Override
		public Type getElementType() {
			return SHORT;
		}

		@Override
		public Object createDefault() {
			return new short[0];
		}
	},

	/**
	 * The equivalent of an array of {@link Byte}.
	 */
	ARRAY_BYTE {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return byte[].class;
		}

		@Override
		public Type getElementType() {
			return BYTE;
		}

		@Override
		public Object createDefault() {
			return new byte[0];
		}
	},

	/**
	 * The equivalent of an array of {@link Boolean}.
	 */
	ARRAY_BOOLEAN {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "Array";
		}

		@Override
		public Class<?> getJavaType() {
			return boolean[].class;
		}

		@Override
		public Type getElementType() {
			return BOOLEAN;
		}

		@Override
		public Object createDefault() {
			return new byte[0];
		}
	},

	/**
	 * The equivalent of an array of {@link String}.
	 */
	STRING {
		@Override
		public int getSize() {
			return LONG.getSize() + INTEGER.getSize();
		}

		@Override
		public String getGetName() {
			return "String";
		}

		@Override
		public Class<?> getJavaType() {
			return String.class;
		}

		@Override
		public Type getElementType() {
			return BYTE;
		}

		@Override
		public Object createDefault() {
			return "";
		}
	};

	/**
	 * Get the {@link Type} equivalent of the given Java class.
	 * 
	 * @param cls
	 *            Java class.
	 * @return The equivalent {@link Type}.
	 */
	public static Type fromJavaType(final Class<?> cls) {
		if (cls == double.class)
			return Type.DOUBLE;
		if (cls == float.class)
			return Type.FLOAT;
		if (cls == long.class)
			return Type.LONG;
		if (cls == int.class)
			return Type.INTEGER;
		if (cls == short.class)
			return Type.SHORT;
		if (cls == byte.class)
			return Type.BYTE;
		if (cls == boolean.class)
			return Type.BOOLEAN;
		if (cls == double[].class)
			return Type.ARRAY_DOUBLE;
		if (cls == float[].class)
			return Type.ARRAY_FLOAT;
		if (cls == long[].class)
			return Type.ARRAY_LONG;
		if (cls == int[].class)
			return Type.ARRAY_INTEGER;
		if (cls == short[].class)
			return Type.ARRAY_SHORT;
		if (cls == byte[].class)
			return Type.ARRAY_BYTE;
		if (cls == boolean[].class)
			return Type.ARRAY_BOOLEAN;
		return null;
	}

	/**
	 * If the type is a number.
	 * 
	 * @return If is a number.
	 */
	public boolean isNumber() {
		return this != BOOLEAN && !isArray();
	}

	/**
	 * Get the size (in bytes) of the type values. For example, the type
	 * {@link #BYTE} returns <code>1</code>.
	 * 
	 * @return The size in bytes.
	 */
	public abstract int getSize();

	/**
	 * Get the name used by the {@link ByteBuffer} to read a value of this type.
	 * For example, for the {@link ByteBuffer#getDouble()} method, the name is
	 * <code>"Double"</code>.
	 * 
	 * @return The name used in the {@link ByteBuffer} method.
	 */
	public abstract String getGetName();

	/**
	 * Get the equivalent Java class.
	 * 
	 * @return The Java class.
	 */
	public abstract Class<?> getJavaType();

	/**
	 * Get the {@link Type} of the array elements. Returns <code>null</code> if
	 * this type is not an array.
	 * 
	 * @return The array element {@link Type}.
	 */
	@SuppressWarnings("static-method")
	public Type getElementType() {
		return null;
	}

	/**
	 * If this is an array type.
	 * 
	 * @return <code>true</code> if this is an array type.
	 */
	public boolean isArray() {
		return getElementType() != null;
	}

	/**
	 * If this is an array type but not the {@link #STRING} type.
	 * 
	 * @return <code>true</code> if this is an array but not a string.
	 */
	public boolean isArrayButNotString() {
		return isArray() && this != STRING;
	}

	/**
	 * <p>
	 * Read from the buffer a value of this {@link Type}. For example, a
	 * {@link #DOUBLE} does <code>buf.getDouble()</code>.
	 * </p>
	 * <p>
	 * This method can be used by external tools to read an arbitrary MDB file.
	 * </p>
	 * 
	 * @param buf
	 *            the buffer to read the value.
	 * @return The value read from the buffer.
	 */
	public Object readValue(ByteBuffer buf) {
		assert isArray();
		throw new UnsupportedOperationException(
				"Cannot read an array from the main buffer.");
	}

	/**
	 * <p>
	 * Write to the buffer a value of this {@link Type} . For example, a
	 * {@link #DOUBLE} does <code>buf.putDouble(value)</code>.
	 * </p>
	 * <p>
	 * This method can be used by external tools to write into an arbitrary MDB
	 * file.
	 * </p>
	 * 
	 * @param buf
	 *            The buffer to write the value.
	 * @param value
	 *            The value to write.
	 */
	public void writeValue(ByteBuffer buf, Object value) {
		assert isArray();
		throw new UnsupportedOperationException(
				"Cannot write an array from the main buffer.");
	}

	/**
	 * Parse the string and get the equivalent value. This method can be used by
	 * external tools in tasks like import a CSV file into an MDB file.
	 * 
	 * @param str
	 *            The string to parse.
	 * @return The value represented by the string.
	 */
	public Object parseString(String str) {
		throw new UnsupportedOperationException("Cannot parse type " + this
				+ ".");
	}

	/**
	 * Create the default value. This can be used by external tools like a
	 * database editor.
	 * 
	 * @return The default value of this {@link Type}.
	 */
	public abstract Object createDefault();

	/**
	 * Return an expression to "box" a primitive value in an Object.
	 * 
	 * @param expr
	 *            The expression of a primitive type.
	 * @return The boxing expression.
	 */
	@SuppressWarnings("static-method")
	public String boxing(String expr) {
		return expr;
	}
}
