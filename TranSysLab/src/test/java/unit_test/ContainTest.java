package unit_test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainTest {

	public static void main(String[] args) {
//		for (int i = 0; i < 5; i++) {
//			System.out.println("i = " + i);
//			for (int j = 0; j < 4; j++) {
//				System.out.println("j = " + j);
//				if (j>=2) {
//					break;
//				}
//			}
//		}
		
		List<Integer> L1 = new ArrayList<>();
		List<Integer> L2 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			L1.add(i+1);
			L2.add((i+1)*2);
		}
		System.out.println(L1.contains(L2));
		List<Integer> L3 = new ArrayList<>(L1);
		Collections.copy(L3, L1);
		L3.retainAll(L2);
		System.out.println(L1);
		System.out.println(L3);
	}

}
