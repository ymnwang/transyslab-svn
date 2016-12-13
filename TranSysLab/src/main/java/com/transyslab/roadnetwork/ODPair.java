/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * @author yali
 *
 */
public class ODPair {
	protected Node oriNode_;
	protected Node desNode_;
	public ODPair() {
		oriNode_ = null;
		desNode_ = null;
	}
	public ODPair(Node o, Node d) {
		oriNode_ = o;
		desNode_ = d;
	}
	public ODPair(ODPair od) {
		oriNode_ = od.oriNode_;
		desNode_ = od.desNode_;
	}
	public Node getOriNode() {
		return oriNode_;
	}
	public Node getDesNode() {
		return desNode_;
	}
	public int ori() {
		return oriNode_.getCode();
	}
	public int des() {
		return desNode_.getCode();
	}

}
