package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

	
	 static Properties loadPropertiesFile(String file) {
		// src: https://mkyong.com/java/java-properties-file-examples/
		Properties p = new Properties();
		try (InputStream is = new FileInputStream(file)) {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
}
