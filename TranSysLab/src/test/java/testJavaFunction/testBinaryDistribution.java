package testJavaFunction;
import org.apache.commons.math3.distribution.BinomialDistribution;

public class testBinaryDistribution {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BinomialDistribution bd = new BinomialDistribution(1,0.3);
		int i;
		for(int j =0;j<50;j++){
			i = bd.sample();
			System.out.println(i);
		}


	}

}
