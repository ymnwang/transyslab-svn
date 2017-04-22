/**
 *
 */
package com.transyslab.roadnetwork;
import java.util.*;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
/**
 * extern RN_PathTable * thePathTable; PathTable
 *
 * @author YYL 2016-6-4
 */
public class PathTable {
	//
	protected Vector<Path> paths_;
	public static PathTable getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getPathTable(threadid);
	}

	public static String name_; // file name
	public static String getName() {
		return name_;
	}
	// public static char ** nameptr() { return &name_; }

	public PathTable() {

	}

	// This may be overloaded by derived class

	public Path newPath() {
		return new Path();
	}
	public Vector<Path> getPaths() {
		return paths_;
	}

	public int addPath(int code, int ori, int des) {
		if (paths_ == null)
			paths_ = new Vector<Path>();
		Path p = newPath();
		/*
		 * c++有毒，用指针用得这么肆无忌惮，先add进数组，然后再初始化，java不传地址，而是新开辟空间并复制对象 paths_.add(p);
		 * return p.init(code, ori, des);
		 */
		p.init(code, ori, des);
		paths_.add(p);
		return 1;
	}
	public Path lastPath() {
		return paths_.lastElement();
	}

	public int nPaths() {
		return paths_.size();
	}
	public Path path(int i) {
		return paths_.get(i);
	}
	public Path findPath(int c) {
		ListIterator<Path> i = paths_.listIterator();
		while (i.hasNext()) {
			Path templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}

	public void print() {

	}

}
