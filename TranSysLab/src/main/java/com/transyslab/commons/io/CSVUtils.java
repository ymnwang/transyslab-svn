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
	/**读取csv文件
     * @param filePath 文件路径
     * @param headers csv列头
     * @return CSVRecord 列表
     * @throws IOException **/
    public static List<CSVRecord> readCSV(String filePath,String[] headers) throws IOException{
        
        //创建CSVFormat
    	CSVFormat formator;
    	if(headers==null)
    		//忽略表头
    		formator = CSVFormat.DEFAULT.withIgnoreHeaderCase();
    	else 
    		formator = CSVFormat.DEFAULT.withHeader(headers);
        
        FileReader fileReader=new FileReader(filePath);
        
        //创建CSVParser对象
        CSVParser parser=new CSVParser(fileReader,formator);
        
        List<CSVRecord> records=parser.getRecords();
        
        parser.close();
        fileReader.close();
       
        return records;    
    }
    public static void writeCSV(String filePath,String[] headers, List data) throws IOException{
        
        //创建CSVFormat
    	CSVFormat formator;
    	if(headers==null)
    		//忽略表头
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
        //创建CSVParser对象
        CSVParser parser=new CSVParser(fileReader,formator);
        
        List<CSVRecord> records=parser.getRecords();
        
        parser.close();
        fileReader.close();
        
        return records;  */  
    }
}
