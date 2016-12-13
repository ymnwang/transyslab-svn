/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * Label
 *
 * @author YYL 2016-6-2
 */
public class Label extends CodedObject {
	protected static int sorted_;
	protected int length_;

	public Label() {
		length_ = 0;
	}

	public int getLength() {
		return length_;
	}
	public static int sorted() {
		return sorted_;
	}
	public int init(int c, String n) {
		if (c == 0) {
			// cerr << "Error:: Label code <0> is not allowed. ";
			return -1;
		}
		setName(n);
		setCode(c);
		length_ = getName() != null ? getName().length() : 0;

		RoadNetwork.getInstance().addLabel(this);

		/*
		 * if (ToolKit::debug()) { cout << indent << "<" << c << endc << n <<
		 * ">" << endl; }
		 */

		return 0;
	}
	@Override
	public void print() {

	}
}
