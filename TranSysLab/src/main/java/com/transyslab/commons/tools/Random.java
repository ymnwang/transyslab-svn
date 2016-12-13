/**
 *
 */
package com.transyslab.commons.tools;

import java.util.HashMap;
import java.util.Vector;

import com.transyslab.roadnetwork.RoadNetworkPool;

/**
 * @author its312
 *
 */
public class Random {

	// friend class CmdArgsParser;

	protected static int flags_ = 0;
	protected int signature_;
	// c++ long 对应4字节,修改randomize，setSeed方法
	protected int seed_;
	protected static int counter = 0;

	public static Vector<Random> getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getRandom(threadid);
	}

	public static int Misc = 0;
	public static int Departure = 1;
	public static int Routing = 2;
	public int Behavior = 3;

	public static void create(int n, Vector<Random> theRandomizers) // create
																	// random
																	// numbers
	{
		for (int i = 0; i < n; i++) {
			theRandomizers.add(new Random());
			theRandomizers.get(i).randomize();
		}
	}

	public Random() {
		// 未处理 static int counter = 0;
		signature_ = counter;
		counter++;
		seed_ = 0;
	}

	public static int getFlags() {
		return flags_;
	}
	public static void setFlags(int s) {
		flags_ = s;
	}

	public int getSeed() {
		return seed_;
	}
	public void setSeed(int s) {
		seed_ = s;
	}

	// Set random seed
	public int randomize() {
		int s = 0xFF << (signature_ * 8);
		if (!((seed_ = (flags_ & s)) > 0)) {
			long ct = System.currentTimeMillis();
			// long E9 = MyMath.myPow(10, 9);
			// 取long型后九位
			// seed_ = (int) (ct%(ct/E9*E9));
			// seed_ = (int) System.currentTimeMillis();
			// 校对输出结果
			seed_ = 1468288583;
		}
		return seed_;
	}

	// uniform (0, 1]

	public double urandom() {
		// Constants for linear congruential random number generator.
		final int M = 2147483647; // M = modulus (2^31)
		final int A = 48271; // A = multiplier (was 16807)
		final int Q = M / A;
		final int R = M % A;

		seed_ = A * (seed_ % Q) - R * (seed_ / Q);
		seed_ = (seed_ > 0) ? (seed_) : (seed_ + M);

		return (double) seed_ / (double) M;
	}

	// uniform [0, n)

	public int urandom(int n) {
		return ((int) (urandom() * n));
	}

	// uniform (a, b]

	public double urandom(double a, double b) {
		return a + (b - a) * urandom();
	}

	// returns 1 with probability p

	public int brandom(double prob) {
		if (urandom() < prob)
			return (1);
		else
			return 0;
	}
	public int brandom(float prob) {
		if (urandom() < prob)
			return (1);
		else
			return 0;
	}

	// exponential with parameter r

	public double erandom(double lambda) {
		return -Math.log(urandom()) / lambda;
	}
	public double rrandom(double one_by_lambda) {
		return -Math.log(urandom()) * one_by_lambda;
	}

	// normal with mean m and stddev v

	public double nrandom() {
		double r1 = urandom(), r2 = urandom();
		double r = -2.0 * Math.log(r1);
		if (r > 0.0)
			return Math.sqrt(r) * Math.sin(2 * Math.PI * r2);
		else
			return 0.0;
	}
	public double nrandom_trunc(double r) {
		double x = nrandom();
		if (x >= -r && x < r)
			return x;
		else
			return urandom(-r, r);
	}
	public double nrandom(double mean, double stddev) {
		double r1 = urandom(), r2 = urandom();
		double r = -2.0 * Math.log(r1);
		if (r > 0.0)
			return (mean + stddev * Math.sqrt(r) * Math.sin(2 * Math.PI * r2));
		else
			return (mean);
	}
	public double nrandom_trunc(double mean, double stddev, double r) {
		double x = nrandom(mean, stddev);
		double dx = r * stddev;
		if (x >= mean - dx && x <= mean + dx) {
			return x;
		}
		else {
			return mean + urandom(-dx, dx);
		}
	}

	// discrete random number in [0, n) with given CDF

	public int drandom(int n, double cdf[]) {
		int i;
		double r = urandom();
		for (n = n - 1, i = 0; i < n && r > cdf[i]; i++);
		return i;
	}

	public int drandom(int n, float cdf[]) {
		int i;
		double r = urandom();
		for (n = n - 1, i = 0; i < n && r > cdf[i]; i++);
		return (i);
	}

	// randomly permute an array

	// Given as input the numbers: 0..N-1 it returns a random permutation
	// of those numbers This is achieved in a single pass
	public void permute(int n, int perm[]) {
		int i;
		int r, tmp;

		for (i = 0; i < n; i++)
			perm[i] = i;

		for (i = n - 1; i >= 0; i--) {
			r = urandom(i);
			tmp = perm[i];
			perm[i] = perm[r];
			perm[r] = tmp;
		}
	}

}
// extern vector<Random*> theRandomizers;
