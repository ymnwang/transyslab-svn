package testJavaFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MLPNetworkPool;

public class Testing {

	public static void main(String[] args) {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		for (int i = 0; i < 3; i++) {
			Random r1 = new Random((long) 1);
			for (int j = 0; j < 3; j++) {
				List<Integer> iList = new ArrayList<>();
				for (int k = 0; k < 10; k++) {
					iList.add(k+1);
				}
				Collections.shuffle(iList, r1);
				System.out.println(iList.toString());
			}
		}
		for (int i = 0; i < 3; i++) {
			Random r = new Random((long) 1);
			for (int j = 0; j < 3; j++) {
				System.out.println(r.nextDouble());
			}
		}
	}

}
