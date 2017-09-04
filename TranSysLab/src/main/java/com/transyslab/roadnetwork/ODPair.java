/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yali
 *
 */
public class ODPair implements NetworkObject{
	protected int id;
	protected String name;
	protected Node oriNode;
	protected Node desNode;
	protected List<Path> paths;
	protected double[] splits; // probabilities to choose each getPath
	protected static int nSplits; // num of splits parsed so far for current odpair
	protected boolean isSelected;

	public ODPair() {
		paths = new ArrayList<>();
	}
	public ODPair(Node o, Node d) {
		oriNode = o;
		desNode = d;
		paths = new ArrayList<>();
	}
	public int setSplit(float split) {
		int n = nPaths();
		if (nSplits >= n) {
			return -1; // too many splits
		}
		if (splits == null) {
			splits = new double[n];
		}
		else {
			split += splits[nSplits - 1];
		}

		if (split < 0.0)
			return -2;
		if (split > 1.0)
			return -3;

		splits[nSplits] = split;
		nSplits++;

		return 0;
	}
	public int nSplits() {
		return nSplits;
	}
	public double split(int i) {
		return splits[i];
	}
	public double[] splits() {
		return splits;
	}
	public int nPaths(){
		return paths.size();
	}
	public int getId(){
		return this.id;
	}
	public String getObjInfo(){
		return this.name;
	}
	public boolean isSelected(){
		return this.isSelected;
	}
	public void setSelected(boolean flag){
		this.isSelected = flag;
	}
	public Node getOriNode() {
		return oriNode;
	}
	public Node getDesNode() {
		return desNode;
	}
	public Path getPath(int i){
		return paths.get(i);
	}
	public List<Path> getPaths() {
		return paths;
	}
	public void addPath(Path p){
		paths.add(p);
	}
	public Path findPathAccd2Id(int pathId) {
		for(Path tmpPath:paths){
			if (tmpPath.id == pathId) {
				return tmpPath;
			}
		}
		return null;
	}
	public Path findPathAccd2Index(int index) {
		if(index>=paths.size())
			return null;
		else
			return paths.get(index);
	}
	public Path chooseRoute(Vehicle pv, RoadNetwork network) {
		double r = network.sysRand.nextDouble();
		int n = nPaths();
		int i;
		for (n = n - 1, i = 0; i < n && r > splits[i]; i++);
		return paths.get(i);
	}
	public List<Path> verifyPath(Vehicle veh) {
		//todo 检查path可行性
		return paths;
	}
	public Path assignRoute(Vehicle veh) {
		//TODO 路径选择行为：可以考虑放在Vehicle类中
		return verifyPath(veh).get(0);
	}
}
