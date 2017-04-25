package testJavaFunction;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


import org.apache.commons.math3.distribution.NormalDistribution;


import jhplot.stat.Statistics;


public class testCEM {
	private double rho;//分位数，不宜过小
	private double alpha;//平滑权重,0.5-0.9
	private int n;  //样本量
	private int dim;//解维数；参数个数
	private NormalDistribution normDistr;//AbstractRealDistribution
	private double mu, sigma;//正态分布参数
	public static void main(String[] args) {
		testCEM test = new testCEM();
		test.initAlgParam(0.1, 100, 1, 0.7,-6, 10);
		while(Math.pow(test.sigma,2)> 2.2*Math.pow(10, -16)){
			double[] sortedSample = test.selectSample();
			Arrays.sort(sortedSample);
			System.out.println(test.mu+","+test.sigma+","+sortedSample[0]+","+sortedSample[9]);
			test.update(sortedSample);
		}
		System.out.println();
	}
	// -2<=x<=2
	public double testFunction(double x){
		double a = -Math.pow(x-2, 2);
		double b = -Math.pow(x+2, 2);
		double y = Math.exp(a)+0.8*Math.exp(b);
		return y;
	}
	public void initAlgParam(double rho, int n, int dim, double alpha,double mu, double sigma){
		this.rho = rho;
		this.n = n;
		this.dim = dim;
		this.alpha = alpha;
		this.mu = mu;
		this.sigma = sigma;
		normDistr = new NormalDistribution(this.mu, this.sigma);
	}
	public double[] selectSample(){
		double[] sample = new double[n];
		for(int i=0;i<n;i++){
			double tmpSam = normDistr.sample();
			/*while( tmpSam>2 || tmpSam<-2){
				tmpSam =normDistr.sample();
			}*/
			sample[i] = tmpSam;
		}

		double[] funcValue = new double[sample.length];
		Map<Double, Double> yx = new TreeMap<>(
			new Comparator<Double>() {
				public int compare(Double obj1, Double obj2){
					//降序排序
					return obj2.compareTo(obj1);
				}
			}	
		);
		for(int i= 0; i< sample.length;i++){
			funcValue[i] = testFunction(sample[i]);
			yx.put(funcValue[i], sample[i]);
		}
		int counter = 0;
		int k = (int) Math.round(rho*n);
		Set<Double> mapKey = yx.keySet();
		Iterator<Double> iterator = mapKey.iterator();
		double[] sortedSample = new double[k]; 
		while(iterator.hasNext()&& counter <k){
			double tmp = iterator.next();
//			System.out.println(tmp);
			sortedSample[counter] = yx.get(tmp);
			counter ++ ;
		}
		return sortedSample;
	}
	public void update(double[] sortedSample){
		double tmpMu = Statistics.mean(sortedSample);
		double tmpSigma = Math.sqrt(Statistics.variance(sortedSample));
		this.mu = this.alpha*tmpMu + (1-alpha)*this.mu;
		this.sigma = this.alpha*tmpSigma + (1-alpha)*this.sigma;
		normDistr = new NormalDistribution(this.mu, this.sigma);
	}

}
