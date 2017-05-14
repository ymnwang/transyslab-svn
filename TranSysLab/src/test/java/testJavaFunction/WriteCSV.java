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

		System.out.println("客户端启动...");  
		//创建一个流套接字并将其连接到指定主机上的指定端口号  
        Socket client = new Socket("127.0.0.1", 1025);
        //读取服务器端数据    
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //向服务器端发送数据    
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);
		// 消息以\n结尾
        String firstLine = input.readLine();
        if (firstLine.equals("Connection built")){
        	System.out.println("Connection success");
            output.print("calcFitness");
            output.flush();
            while (true) {
            	try {
            		//消息以\n结尾
                    String line = input.readLine();
                    //接收python计算结果
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
                    System.out.println("客户端异常:" + e.getMessage());   
                } 
            }
            input.close();
            output.close();
            client.close();
        }
 
	}

}
