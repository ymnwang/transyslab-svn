package com.transyslab.commons.tools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.xmlgraphics.util.io.Finalizable;

import com.transyslab.commons.io.CSVUtils;

public class FitnessFunction {
	public static double evaRNSE(double[] sim, double[] obs){
		if(sim.length ==0 || sim.length != obs.length){
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}
		double numerator = 0;
		double denominator = 0;
		for(int i=0;i<sim.length;i++){
			numerator += Math.pow(sim[i]-obs[i],2);
			denominator += obs[i];
		}
		return Math.sqrt(numerator*sim.length)/denominator;		
	}
	public static double evaMAPE(double[] sim, double[] obs){
		if (sim.length ==0 || sim.length != obs.length) {
			System.out.print("Error:The length of two input arrays are not equal!");
			return Double.NaN;
		}		
		double sum = 0.0;
		int count = 0;
		for (int i = 0; i < sim.length; i++) {
			double del = Math.abs(sim[i] - obs[i]);
			if (Math.abs(obs[i])>0.0001) {
				sum += del / obs[i];
				count += 1;
			}
		}		
		if (count > 0) 
			return (sum / count);
		else 
			return 0.0;
	}
	public static double evaKSDistance(double[] sim, double[] obs){
		Arrays.sort(sim);
		Arrays.sort(obs);
		int lenSim = sim.length;
		int lenObs = obs.length;
		double[] dataAll = ArrayUtils.addAll(obs, sim);
		double[] simECDF = new double[dataAll.length];
		double[] obsECDF = new double[dataAll.length];
		double maxDistance = 0.0;
		// ���ֲ���dataAll��sim��λ��
		for(int i=0;i<dataAll.length;i++){
			simECDF[i] = binarySearchIndex(sim, dataAll[i])*1.0/lenSim;
			obsECDF[i] = binarySearchIndex(obs, dataAll[i])*1.0/lenObs;
			maxDistance = Math.max(Math.abs(simECDF[i]-obsECDF[i]),maxDistance);
		}
		return maxDistance;
	}
	public static int binarySearchIndex(final double[] array,final double data){
		int mid = 0,from = 0,to = array.length-1;
		int tarid = 0;
		if(array[to]<=data)
			return to+1;
		if(array[from]>=data){
			return from+1;
		}
		while(from <= to && tarid ==0) {
            mid = from + (to - from) / 2;
            if (array[mid] < data) {
            	if(mid!=array.length-1){
            		if(array[mid+1]>data)
                		tarid = mid+1;
                	else if(array[mid+1]<data)
                		from = mid + 1;
                	else 
                		tarid = mid + 2;
            	}
            	else tarid = mid + 1;
            	
            }else if(array[mid] > data) {
                to = mid - 1;
            }else {
            	tarid = mid + 1;
            }
        }
		if(tarid == 0)
			System.out.println("Error:Fail to find the approprate index to insert");
		return tarid;
	}
	public static void main(String[] args) {
		double[] empData;
		try {
			// ���б��
			List<CSVRecord> results = CSVUtils.readCSV("R:\\DetSpeed2.csv", null);
			double[] tmpEmpData = new double[results.size()]; 
			for(int i=0;i<tmpEmpData.length;i++){
				tmpEmpData[i] = Double.parseDouble(results.get(i).get(0));
			}
			empData = tmpEmpData;
			double[] simData = new double[]{58.8063757,61.41356076,59.46558025,60.55506715,60.23640314,61.70664375,60.22416935
					,62.23609696,57.74289612,57.1121967,58.86835477,56.34740228,60.26346681,60.28685208,61.09150313,59.73280032
					,60.16183973,58.14598878,61.02911202,59.97470777};
			double fitness = evaRNSE(simData, empData);
			System.out.println(fitness);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
