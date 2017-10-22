package com.transyslab.commons.io;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class CSVUtils {

	/**��ȡcsv�ļ�
     * @param filePath �ļ�·��
     * @param headers csv��ͷ
     * @return CSVRecord �б�
     * @throws IOException **/
    public static List<CSVRecord> readCSV(String filePath,String[] headers) throws IOException{
        
        //����CSVFormat
    	CSVFormat formator;
    	if(headers==null)
    		//���Ա�ͷ
    		formator = CSVFormat.DEFAULT.withIgnoreHeaderCase();
    	else 
    		formator = CSVFormat.DEFAULT.withHeader(headers);
        
        FileReader fileReader=new FileReader(filePath);
        
        //����CSVParser����
        CSVParser parser=new CSVParser(fileReader,formator);
        
        List<CSVRecord> records=parser.getRecords();
        
        parser.close();
       
        return records;    
    }
    //TODO ���Ӳ����ж��Ƿ�Ϊ��д
    public static void writeCSV(String filePath,String[] header, double[] data) throws IOException{
    	FileUtils.createFile(filePath);
        //����CSVFormat
    	CSVFormat formator;
    	if(header==null)
    		//���Ա�ͷ
    		formator = CSVFormat.DEFAULT.withIgnoreHeaderCase().withRecordSeparator("\n");
    	else 
    		formator = CSVFormat.DEFAULT.withHeader(header).withRecordSeparator("\n");
    	FileWriter fileWriter = new FileWriter(filePath);
        CSVPrinter printer = new CSVPrinter(fileWriter,formator);
        for(int i=0;i<data.length;i++){
        	printer.printRecord(data[i]);
        }
		printer.flush();
        printer.close();
    }
    public static void writeCSV(String filePath,String[] header, double[][] data) throws IOException{
    	FileUtils.createFile(filePath);
        //����CSVFormat
    	CSVFormat formator;
    	if(header==null)
    		//���Ա�ͷ, �س�����
    		formator = CSVFormat.DEFAULT.withIgnoreHeaderCase().withRecordSeparator("\n");
    	else 
    		formator = CSVFormat.DEFAULT.withHeader(header).withRecordSeparator("\n");
    	FileWriter fileWriter = new FileWriter(filePath);
        CSVPrinter printer = new CSVPrinter(fileWriter,formator);
        List<Double> dataRecord = new ArrayList<>(); 
        for(int i=0;i<data.length;i++){
        	
        	for(int j=0;j<data[i].length;j++){
        		dataRecord.add(data[i][j]);
        	}
        	printer.printRecord(dataRecord);
        	dataRecord.clear();
        }
		printer.flush();
        printer.close();
    }
	public static CSVParser getCSVParser(String filePath,String[] header) throws IOException{
		//����CSVFormat
		CSVFormat formator;
		if(header==null)
			//���Ա�ͷ
			formator = CSVFormat.DEFAULT.withIgnoreHeaderCase();
		else
			formator = CSVFormat.DEFAULT.withHeader(header);

		FileReader fileReader=new FileReader(filePath);

		//����CSVParser����
		return new CSVParser(fileReader,formator);
	}

	public static CSVParser getCSVParser(String filePath, boolean hasHeader)throws IOException{
		//����CSVFormat
		CSVFormat formator;
		if(hasHeader)
			// Ĭ�ϱ���һ��Ϊ��ͷ
			formator = CSVFormat.DEFAULT.withFirstRecordAsHeader();

		else
			//���Ա�ͷ
			formator = CSVFormat.DEFAULT.withIgnoreHeaderCase();
		FileReader fileReader=new FileReader(filePath);

		//����CSVParser����
		return new CSVParser(fileReader,formator);
	}
	public static CSVPrinter getCSVWriter(String filePath, String[] header, boolean append)throws IOException{
		FileUtils.createFile(filePath);
		//����CSVFormat
		CSVFormat formator;
		if(header==null)
			//���Ա�ͷ
			formator = CSVFormat.DEFAULT.withIgnoreHeaderCase().withRecordSeparator("\n");
		else
			formator = CSVFormat.DEFAULT.withHeader(header).withRecordSeparator("\n");
		FileWriter fileWriter = new FileWriter(filePath,append);
		return new CSVPrinter(fileWriter,formator);
	}
}
