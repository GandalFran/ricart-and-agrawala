package util;

import java.util.Properties;

public interface IConstants {
	
	final static String PROPERTIES_FILE = "constants.properties";
	final static Properties PROPERTIES = Utils.loadPropertiesFile(PROPERTIES_FILE);

	
	public final static int MIN_CALCULUS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("minCalculusTime"));
	public final static int MAX_CALCULUS_TIME_MS = Integer.parseInt((String) PROPERTIES.get("maxCalculusTime"));
	
	
	
	
}
