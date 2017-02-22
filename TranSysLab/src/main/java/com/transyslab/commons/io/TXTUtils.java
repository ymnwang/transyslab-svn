package com.transyslab.commons.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TXTUtils {
	protected BufferedWriter writer;
	
	public TXTUtils(String filepath) {
		//establish writer
		File file = new File(filepath);
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
	
	public void writeNFlush(String str) {
		try {
			writer.write(str);
			writer.flush();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
	public void write(String str) {
		try {
			writer.write(str);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
	public void flushBuffer() {
		try {
			writer.flush();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
	public void closeWriter() {
		try {
			writer.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
}
