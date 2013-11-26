
package org.knoesis.lucene.indexer.utils;

import java.util.Properties;

/**
 * Utilities for parsing values from the properties file.
 * @author Alan Smith
 */
public class PropUtils {
    
    public static int getInt(Properties properties, String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
    
    public static boolean getBoolean(Properties properties, String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    private PropUtils(){}
    
}
