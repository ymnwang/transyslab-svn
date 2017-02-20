package unit_test;

import oracle.net.aso.b;

public class testOverride {
	public A testa;
	public B testb;
	public C testc;
	public D testd;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		testb = new B();
	}
	public class A{
		public C getC(){
			return null;
		}
	}
	public class B extends A{
		public D getC(){
			return testd;
		}
	}
	public class C{
		
	}
	public class D extends C{
		
	}
}
