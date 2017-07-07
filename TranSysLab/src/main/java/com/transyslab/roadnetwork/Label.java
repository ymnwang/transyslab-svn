/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * Label
 *
 * @author YYL 2016-6-2
 */
public class Label {
	protected int id;
	protected String name;
	protected static int sorted;
	protected int length;

	public Label() {
		length = 0;
	}
	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public int getLength() {
		return length;
	}
	public static int sorted() {
		return sorted;
	}
	public int init(int id, String name) {
		if (id == 0) {
			// cerr << "Error:: Label code <0> is not allowed. ";
			return -1;
		}
		this.id = id;
		this.name = name;
		length = getName() != null ? getName().length() : 0;


		return 0;
	}

}
