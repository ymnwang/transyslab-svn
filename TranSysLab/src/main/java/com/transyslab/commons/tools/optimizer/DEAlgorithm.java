package com.transyslab.commons.tools.optimizer;


import java.util.Random;

/**
 * Created by yali on 2017/9/14.
 */
public class DEAlgorithm {
	private double F; //学习率
	private double Cr;
	private Individual[] newidvds;
	private Individual[] idvds;
	private int dims;
	private int population;
	private int maxItrGeneration;
	private int bestIdvdIndex;
	private double[] plower;
	private double[] pupper;
	private double gbestFitness;
	private double[] gbest;
	private boolean isMaximum;
	private Random random;

	//默认处理目标最小化
	public DEAlgorithm(){
		this.random = new Random(System.currentTimeMillis());
		this.isMaximum = false;
		this.gbestFitness = Double.MAX_VALUE;
	}
	public DEAlgorithm(boolean isMaximum){
		this.random = new Random(System.currentTimeMillis());
		this.isMaximum = isMaximum;
		if(this.isMaximum)
			this.gbestFitness = Double.MIN_VALUE;
		else
			this.gbestFitness = Double.MAX_VALUE;
	}
	public DEAlgorithm(boolean isMaximum, long seed){
		this.random  = new Random(seed);
		this.isMaximum = isMaximum;
		if(this.isMaximum)
			this.gbestFitness = Double.MIN_VALUE;
		else
			this.gbestFitness = Double.MAX_VALUE;
	}
	public void setMaxItrGeneration(int maxItrGeneration) {
		this.maxItrGeneration = maxItrGeneration;
	}
	public int getMaxItrGeneration() {
		return maxItrGeneration;
	}
	public int getBestIdvdIndex(){
		return this.bestIdvdIndex;
	}
	public int getDim() {
		return dims;
	}
	public int getPopulation() {
		return population;
	}
	public double[] getPosition(int i) {
		return idvds[i].pos;
	}
	public double[] getNewPosition(int i) {
		return newidvds[i].pos;
	}

	public void init(int p, int dim, int maxItrGeneration,double f, double cr, double[] pl, double[] pu) {
		this.maxItrGeneration = maxItrGeneration;
		this.dims = dim;
		this.population = p;
		this.F = f;
		this.Cr = cr;
		this.gbest = new double[dims];
		this.plower = pl;
		this.pupper = pu;
		this.idvds = new Individual[population];
		this.newidvds = new Individual[population];
		for (int i = 0; i < p; i++) {
			idvds[i] = new Individual(dim, this.isMaximum);
			newidvds[i] = new Individual(dim, this.isMaximum);
			//生成初始解
			idvds[i].init(pl, pu);
			for (int j = 0; j < dims; j++) {
				newidvds[i].pos[j] = idvds[i].pos[j];
			}
		}
	}
	public void selection(int pi) {
		if (newidvds[pi].fitness < idvds[pi].fitness) {
			for (int j = 0; j < dims; j++) {
				idvds[pi].pos[j] = newidvds[pi].pos[j];
			}
			idvds[pi].fitness = newidvds[pi].fitness;
		}
	}
	public void changePos(int pi) {
		int pi1, pi2, pi3;
		pi1 = random.nextInt(population);
		do {
			pi2 = random.nextInt(population);
		} while (pi1 == pi2);
		do {
			pi3 = random.nextInt(population);
		} while (pi1 == pi3 || pi2 == pi3);
		// int counter=0;
		for (int j = 0; j < dims; j++) {
			newidvds[pi].pos[j] = idvds[pi1].pos[j] + F * (idvds[pi2].pos[j] - idvds[pi3].pos[j]);
			if (newidvds[pi].pos[j] > pupper[j] || newidvds[pi].pos[j] < plower[j])
				newidvds[pi].pos[j] = idvds[pi].pos[j];
			if (random.nextDouble() > Cr)
				newidvds[pi].pos[j] = idvds[pi].pos[j];
		}
	}
	public void evoluteGeneration(double[] fitness){
		for (int i = 0; i < this.population; i++) {
			newidvds[i].fitness = fitness[i];
			selection(i);
			if (newidvds[i].fitness< gbestFitness) {
				setGbest(newidvds[i].pos);
				setGbestFitness(newidvds[i].fitness);
				bestIdvdIndex = i;
			}
			changePos(i);
		}
	}
	public void evoluteIndividual(int pi, double fitness){
		newidvds[pi].fitness = fitness;
		selection(pi);
		if(fitness==Double.MAX_VALUE)
			System.out.print("");
		if (newidvds[pi].fitness< gbestFitness) {
			setGbest(newidvds[pi].pos);
			setGbestFitness(newidvds[pi].fitness);
			bestIdvdIndex = pi;
		}
		changePos(pi);
	}
	public double[] getGbest() {
		return gbest;
	}
	public double getGbestFitness() {
		return gbestFitness;
	}
	public void setGbestFitness(double gbf) {
		gbestFitness = gbf;
	}
	public void setGbest(double[] gbest) {
		for (int i = 0; i < gbest.length; i++) {
			this.gbest[i] = gbest[i];
		}
	}
	public double getFitness(int pi) {
		return idvds[pi].fitness;
	}

	public String showGBestPos() {
		String s = "";
		for (int i = 0; i < gbest.length; i++) {
			s += String.valueOf(gbest[i]);
			if(i!=gbest.length-1)
				s += ",";
		}
		return s;
	}

	public class Individual{
		protected double[] pos;
		protected double fitness;
		protected boolean flag;
		protected double[] results;
		public Individual(int dims, boolean isMaximum) {
			pos = new double[dims];
			if(isMaximum)
				fitness = Double.MIN_VALUE;
			else
				fitness = Double.MAX_VALUE;
			flag = true;
			results = new double[2];
		}
		public double[] getPos() {
			return pos;
		}
		public void init(double[] pl, double[] pu) {
			for (int i = 0; i < dims; i++) {
				pos[i] = pl[i] + (pu[i] - pl[i]) * random.nextDouble();
			}
		}
	}


}
