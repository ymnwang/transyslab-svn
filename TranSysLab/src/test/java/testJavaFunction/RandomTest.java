package testJavaFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomTest {

	public static void main(String[] args) {
		Random r = new Random();
		Random r3 = new Random();
		Random r2 = r;
		List<Integer> iList = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			iList.add(i);
		}
		for (int i = 0; i < 3; i++) {
			r.setSeed(1);
			r3.setSeed(2);
			Collections.sort(iList);	
			System.out.println("reset");
			for (int j = 0; j < 2; j++) {
				System.out.println(r.nextDouble() + " " + r2.nextDouble());
				Collections.shuffle(iList,r2);
				for (int k = 0; k < iList.size(); k++) {
					System.out.print(iList.get(k));
				}
				System.out.print("\r\n");
			}
		}

	}

}
