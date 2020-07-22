package org.mfg.mdbplugin.jobs;

import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("boxing")
public class DiffComputer {
	private static class Node {
		int _leftIndex;
		int _rightIndex;
		List<Node> _children;

		public Node(int leftIndex, int rightIndex) {
			this._leftIndex = leftIndex;
			this._rightIndex = rightIndex;
			_children = new ArrayList<>();
		}

		public boolean add(int leftIndex, int rightIndex) {
			boolean added = false;
			if (rightIndex > this._rightIndex && leftIndex > this._leftIndex) {
				_children.add(new Node(leftIndex, rightIndex));
				added = true;
			}
			for (Node n : _children) {
				boolean added2 = n.add(leftIndex, rightIndex);
				added = added2 || added;
			}
			return added;
		}

		public List<Node> findLongest() {
			List<Node> list = new ArrayList<>();
			for (Node n : _children) {
				List<Node> list2 = n.findLongest();
				if (list2.size() > list.size()) {
					list = list2;
				}
			}
			list.add(0, this);
			return list;
		}

		@Override
		public String toString() {
			return _leftIndex + "-" + _rightIndex;
		}
	}

	public class DiffRecord {
		public static final int EQUAL = 0;
		public static final int CHANGED = 1;
		public static final int DELETED = 2;
		public static final int ADDED = 3;

		public int _type;
		public Object _left;
		public Object _right;

		public DiffRecord(Object left, Object right) {
			super();
			boolean same = left != null && right != null
					&& DiffComputer.this.equals(left, right);
			this._type = same ? EQUAL : (left == null ? ADDED
					: (right == null ? DELETED : CHANGED));
			this._left = left;
			this._right = right;
		}

		@Override
		public String toString() {
			String right = this._right == null ? "" : toStringValue(this._right);
			String left = this._left == null ? "" : toStringValue(this._left);
			switch (_type) {
			case ADDED:
				return "\t+" + right;
			case DELETED:
				return "-" + left;
			case CHANGED:
				return "*" + left + "\t*" + right;
			case EQUAL:
				return "=" + left + "\t=" + right;
			default:
				break;
			}
			return super.toString();
		}
	}

	public String toStringValue(Object v) {
		if (v instanceof Object[]) {
			Object[] arr = (Object[]) v;
			StringBuilder b = new StringBuilder();
			b.append("[");
			for (Object obj : arr) {
				if (b.length() > 1) {
					b.append(", ");
				}
				b.append(toStringValue(obj));
			}
			b.append("]");
			return b.toString();
		}
		return v == null ? "null" : v.toString();
	}

	public List<DiffRecord> diff(Object[] left, Object[] right) {
		List<Node> seq = new ArrayList<>();
		for (int t = 0; t < left.length; t++) {
			List<Node> nodes = new ArrayList<>();
			for (int i = t; i < left.length; i++) {
				Object vleft = left[i];
				for (int j = 0; j < right.length; j++) {
					Object vright = right[j];
					if (equals(vleft, vright)) {
						for (Node n : nodes) {
							n.add(i, j);
						}
						nodes.add(new Node(i, j));
					}
				}
			}

			for (Node n : nodes) {
				List<Node> longest = n.findLongest();
				if (longest.size() > seq.size()) {
					seq = longest;
				}
			}
		}

		// for (Node n : seq) {
		// out.println(left[n.leftIndex] + "-" + right[n.rightIndex] + "    "
		// + n.leftIndex + "-" + n.rightIndex);
		// }

		List<DiffRecord> diffReslut = new ArrayList<>();

		int i = 0, j = 0;
		for (Node n : seq) {
			while (i < left.length || j < right.length) {
				if (i < n._leftIndex && j < n._rightIndex) {
					diffReslut.add(new DiffRecord(left[i], right[j]));
					i++;
					j++;
					continue;
				}

				if (i == n._leftIndex && j == n._rightIndex) {
					diffReslut.add(new DiffRecord(left[i], right[j]));
					i++;
					j++;
					break;
				}

				if (n._leftIndex < n._rightIndex) {
					int size = diffReslut.size();
					while (j < n._rightIndex) {
						diffReslut.add(new DiffRecord(null, right[j]));
						j++;
					}
					if (size != diffReslut.size())
						continue;
				}

				if (n._leftIndex > n._rightIndex) {
					int size = diffReslut.size();
					while (i < n._leftIndex) {
						diffReslut.add(new DiffRecord(left[i], null));
						i++;
					}
					if (size != diffReslut.size())
						continue;
				}

				if (i < n._leftIndex) {
					diffReslut.add(new DiffRecord(left[i], null));
					i++;
				}
				if (j < n._rightIndex) {
					diffReslut.add(new DiffRecord(null, right[j]));
					j++;
				}
			}
		}

		while (i < left.length || j < right.length) {
			Object vleft = null;
			Object vright = null;
			if (i < left.length) {
				vleft = left[i];
				i++;
			}
			if (j < right.length) {
				vright = right[j];
				j++;
			}
			DiffRecord rec = new DiffRecord(vleft, vright);
			diffReslut.add(rec);
		}

		// out.println("---");
		//
		// for (DiffRecord rec : table) {
		// out.println(rec);
		// }

		return diffReslut;
	}

	public boolean equals(Object vleft, Object vright) {
		if (vleft == vright || vleft.equals(vright)) {
			return true;
		}
		if (vleft instanceof Object[] && vright instanceof Object[]) {
			Object[] arr1 = (Object[]) vleft;
			Object[] arr2 = (Object[]) vright;
			if (arr1.length == arr2.length) {
				for (int i = 0; i < arr1.length; i++) {
					if (!equals(arr1[i], arr2[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		DiffComputer diff = new DiffComputer();
		// Object[] left = { 1, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		// Object[] right = { 3, 2, 1, 2, 6, 2, 3, 7, 7, 7, 8, 9 };
		// Object[] sec = computeLongestCommonSubsequence(left, right);
		// out.println(Arrays.toString(sec));

		// Object[] left = { 'X', 'A', 'G', 'C', 'A', 'T' };
		// Object[] right = { 'G', 'A', 'C', 'T' };
		// Object[] sec = diff(left, right);

		// Object[] left = { 'A', 'B', 'B', 'B', };
		// Object[] right = { 'B', 'C', 'A', 'C', 'C', 'C', 'C', 'C', 'Z' };
		// diff(left, right);

		// Object[] left = { 'A', 'B', 'T' };
		// Object[] right = { 'B', 'C', 'T' };
		// diff(left, right);

		// Object[] left = { 'B', 'C', 'T' };
		// Object[] right = { 'A', 'B', 'T' };
		// diff(left, right);
		//
		Object[] left = { 'B', 'A', 'T', 'X', 'A', 'Y', 'A', 'A', 'A' };
		Object[] right = { 'A', 'B', 'A', 'T', 'G', 'A', 'A', 'A', 'A', 'A' };
		List<DiffRecord> res = diff.diff(left, right);
		for (DiffRecord r : res) {
			out.println(r);
		}

		// Object[] left = { 'A', 'B', 'A', 'T', 'A', 'A', 'A', 'A', 'A', 'A' };
		// Object[] right = { 'A', 'A', 'B', 'A', 'T', 'A', 'A', 'A', 'A', 'A',
		// };
		// diff(left, right);

		// Object[] left = { 'B' };
		// Object[] right = { 'A' };
		// List<DiffRecord> res = diff.diff(left, right);
		// for (DiffRecord r : res) {
		// out.println(r);
		// }
	}
}
