package com.transyslab.commons.io;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;

/**
 * 从文件路径创建Configure类
 * Created by WangYimin on 2017/11/7.
 */
public class ConfigUtils {
	public static Configuration createConfig(String FileName) {
		File testFile = new File(FileName);
		Configurations configs = new Configurations();
		Configuration config = null;
		try {
			config = configs.properties(testFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return config;
	}
}
