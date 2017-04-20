package testJavaFunction;


public class testOverride {
	public A testa;
//	public B testb; 
	public C testc = new C();
	public D testd = new D();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testOverride test = new testOverride();
		A testb = test.new B();
		testb.getC();
//		((A) testb).getC();
	}
	public class A{
		public C getC(){
			System.out.println("I'm C");
			return testc;
		}
	}
	public class B extends A{
		public D getC(){
//			super.getC();
			System.out.println("I'm D");
			return testd;
		}
	}
	public class C{
		
	}
	public class D extends C{
		
	}
}
