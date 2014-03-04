/**
 * Copyright (C) 2014 Kno.e.sis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
