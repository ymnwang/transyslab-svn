package unit_test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class testReadCSV {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String[] FILE_HEADER = {"StuId","Name","Gender","Major"};
		final String FILE_NAME = "E:\\MesoInput_Cases\\12.25goodluck\\input1.csv";
		
		testReadCSV test = new testReadCSV();
		
        // 配置CSV文件的Header，然后设置跳过Header（要不然读的时候会把头也当成一条记录）
//        CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER).withSkipHeaderRecord();
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreHeaderCase();

        try(Reader in = new FileReader(FILE_NAME)) {
            Iterable<CSVRecord> records = format.parse(in);
            String strID;
            String strName;
            for (CSVRecord record : records) {
                strID = record.get(0);
                strName = record.get(1);
                System.out.println(strID + " " + strName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	class Student {
	    public Student(String id, String name, String gender, String major) {
	        super();
	        this.id = id;
	        this.name = name;
	        this.gender = gender;
	        this.major = major;
	    }

	    private String id;
	    private String name;
	    private String gender;
	    private String major;

	    public String getID() {
	        return id;
	    }
	    public void setID(String id) {
	        this.id = id;
	    }
	    public String getName() {
	        return name;
	    }
	    public void setName(String name) {
	        this.name = name;
	    }
	    public String getGender() {
	        return gender;
	    }
	    public void setGender(String gender) {
	        this.gender = gender;
	    }
	    public String getMajor() {
	        return major;
	    }
	    public void setMajor(String major) {
	        this.major = major;
	    }

	    @Override
	    public String toString() {
	        return id + ',' + name + ',' + gender + ',' + major;
	    }
	}
}
