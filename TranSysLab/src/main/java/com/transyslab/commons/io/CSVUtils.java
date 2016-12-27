package com.transyslab.commons.io;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

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
        fileReader.close();
       
        return records;    
    }
    public static void writeCSV(String filePath,String[] headers, List data) throws IOException{
        
        //����CSVFormat
    	CSVFormat formator;
    	if(headers==null)
    		//���Ա�ͷ
    		formator = CSVFormat.DEFAULT.withHeader(headers).withSkipHeaderRecord();
    	else 
    		formator = CSVFormat.DEFAULT.withHeader(headers);
    	FileWriter fileWriter = new FileWriter(filePath);
        CSVPrinter printer = new CSVPrinter(fileWriter,formator);
 /*       
        for (int i=0;i<data.size();i++ ) {
            List<String> records = new ArrayList<>();
            records.add(data.get(i));
            printer.printRecord(records);
        }*/
/*        
        //����CSVParser����
        CSVParser parser=new CSVParser(fileReader,formator);
        
        List<CSVRecord> records=parser.getRecords();
        
        parser.close();
        fileReader.close();
        
        return records;  */  
    }
}
