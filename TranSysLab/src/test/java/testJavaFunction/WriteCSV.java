package testJavaFunction;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.FileUtils;

public class WriteCSV {

	public static void main(String[] args) throws UnknownHostException, IOException {
		double[] data;
		NormalDistribution normDistr = new NormalDistribution(50, 10);
		data = normDistr.sample(300);
		FileUtils.createFile("R:\\test.csv");
		try {
			CSVUtils.writeCSV("R:\\test.csv", null, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("�ͻ�������...");  
		//����һ�����׽��ֲ��������ӵ�ָ�������ϵ�ָ���˿ں�  
        Socket client = new Socket("127.0.0.1", 1025);
        //��ȡ������������    
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //��������˷�������    
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
		// ��Ϣ��\n��β
        String firstLine = input.readLine();
        if (firstLine.equals("Connection built")){
        	System.out.println("Connection success");
            output.print("calcFitness");
            output.flush();
            while (true) {
            	try {
            		//��Ϣ��\n��β
                    String line = input.readLine();
                    //����python������
                    if(line.equals("done")){
                    	try {
                			List<CSVRecord> dataList = CSVUtils.readCSV("R:\\testPy.csv", null);

                		} catch (IOException e) {
                			e.printStackTrace();
                		}
                    	output.print("exit");
                    	output.flush();
                    }
                    if(line.equals("Connection closed")){
                    	break;
                    } 
                      
                } catch (Exception e) {  
                    System.out.println("�ͻ����쳣:" + e.getMessage());   
                } 
            }
            input.close();
            output.close();
            client.close();
        }
 
	}

}
