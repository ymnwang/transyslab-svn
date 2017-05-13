package unit_test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainTest {

	public static void main(String[] args) {
		double [][] A = new double[5][3];
		for (int j = 0; j < 5; j++) {
			for (int k = 0; k < 3; k++) {
				A[j][k] = Math.pow(j+1, k+1);
			}
		}
		System.out.println(A.length);
		
		List<Integer> L1 = new ArrayList<>();
		List<Integer> L2 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			L1.add(i+1);
			L2.add((i+1)*2);
		}
		System.out.println(L2);
		L2.forEach(item -> item = 0);
		System.out.println(L2);
		List<Integer> L3 = new ArrayList<>(L1);
		Collections.copy(L3, L1);
		L3.retainAll(L2);
		System.out.println(L1);
		System.out.println(L3);
	}

}
