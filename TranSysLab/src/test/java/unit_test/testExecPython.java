package unit_test;
import java.io.BufferedReader;  
import java.io.InputStreamReader;  
  

public class testExecPython {

	public static void main(String[] args) {
       try{  
            System.out.println("start");  
                     Process pr = Runtime.getRuntime().exec("python E:\\�������\\pycode\\17��ʵ��\\pdfDistance.py");  
                     BufferedReader in = new BufferedReader(new InputStreamReader(
         					pr.getErrorStream()));//or pr.getInputStream()���ؽ��������������Ϣ
         			String line;
         			while ((line = in.readLine()) != null) {
         				System.out.println(line);
         			}
         			in.close();
                     pr.waitFor();  
                     System.out.println("end");  
             } catch (Exception e){  
                     e.printStackTrace();  
             }  
       }  

}
