package com.transyslab.commons.io;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;

/**
 * 从文件路径创建Configure类
 * Created by WangYimin on 2017/11/7.
 */
public class ConfigUtils {
	public static Configuration createConfig(String fileName) {
		try {
			return new Configurations().properties(fileName);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static Configuration createConfig(File inputFile) {
		try {
			return new Configurations().properties(inputFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
