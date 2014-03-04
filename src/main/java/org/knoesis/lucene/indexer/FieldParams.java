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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.knoesis.lucene.indexer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 *
 * @author Alan Smith
 */
class FieldParams {
    
    private static Store DEFAULT_STORE = Store.YES;
    private static Index DEFAULT_INDEX = Index.NOT_ANALYZED_NO_NORMS;
    private static TermVector DEFAULT_TERM_VECTOR = TermVector.NO;
    private static FieldType DEFAULT_FIELD_TYPE = FieldType.TEXT;
    private static Class<? extends Analyzer> DEFAULT_ANALYZER_CLASS = KeywordAnalyzer.class;
    
    public static FieldParams defaults(String fieldName) {
        return new FieldParams(fieldName, DEFAULT_ANALYZER_CLASS, DEFAULT_STORE, DEFAULT_INDEX, DEFAULT_TERM_VECTOR, DEFAULT_FIELD_TYPE);
    }
    
    public static void setDefaults(Class<? extends Analyzer> analyzerClass, Store store, Index index, TermVector tv, FieldType type) {
        DEFAULT_ANALYZER_CLASS = analyzerClass;
        DEFAULT_STORE = store;
        DEFAULT_INDEX = index;
        DEFAULT_TERM_VECTOR = tv;
        DEFAULT_FIELD_TYPE = type;
    }
    
    public static void setDefaultAnalyzerClass(Class<? extends Analyzer> analyzerClass) {
        DEFAULT_ANALYZER_CLASS = analyzerClass;
    }
    
    public static void setDefaultStore(Store store) {
        DEFAULT_STORE = store;
    }
    
    public static void setDefaultIndex(Index index) {
        DEFAULT_INDEX = index;
    }
    
    public static void setDefaultTermVector(TermVector tv) {
        DEFAULT_TERM_VECTOR = tv;
    }
    
    public static void setDefaultFieldType(FieldType type) {
        DEFAULT_FIELD_TYPE = type;
    }
    
    public static Class<Analyzer> getDefaultAnalyzerClass() {
        return (Class<Analyzer>) DEFAULT_ANALYZER_CLASS;
    }
    
    public static Store getDefaultStore() {
        return DEFAULT_STORE;
    }
    
    public static Index getDefaultIndex() {
        return DEFAULT_INDEX;
    }
    
    public static TermVector getDefaultTermVector() {
        return DEFAULT_TERM_VECTOR;
    }
    
    public static FieldType getDefaultFieldType() {
        return DEFAULT_FIELD_TYPE;
    }
    
    public static Map<String, FieldParams> parseProperties(Properties properties) {
        try { setDefaultAnalyzerClass((Class<Analyzer>) Class.forName(properties.getProperty(Constant.PROP_DEFAULT_ANALYZER, DEFAULT_ANALYZER_CLASS.getName()))); } catch(Exception ex) {}
        try { setDefaultStore(Store.valueOf(properties.getProperty(Constant.PROP_DEFAULT_STORE, DEFAULT_STORE.toString()))); } catch(Exception ex) {}
        try { setDefaultIndex(Index.valueOf(properties.getProperty(Constant.PROP_DEFAULT_INDEX, DEFAULT_INDEX.toString()))); } catch(Exception ex) {}
        try { setDefaultTermVector(TermVector.valueOf(properties.getProperty(Constant.PROP_DEFAULT_TERMVECTOR, DEFAULT_TERM_VECTOR.toString()))); } catch(Exception ex) {}
        try { setDefaultFieldType(FieldType.valueOf(properties.getProperty(Constant.PROP_DEFAULT_FIELDTYPE, DEFAULT_FIELD_TYPE.toString()))); } catch(Exception ex) {}
        
        Map<String, FieldParams> params = new HashMap<String, FieldParams>();
        for (String property : properties.stringPropertyNames()) {
            if (property.startsWith(Constant.PROP_PREFIX_FIELD)) {
                String fieldName = property.substring(Constant.PROP_PREFIX_FIELD.length(), property.indexOf(".", Constant.PROP_PREFIX_FIELD.length()));
                if (params.containsKey(fieldName)) {
                    continue;
                }
                FieldParams.Builder builder = new FieldParams.Builder(fieldName);
                try { builder.setAnalyzerClass((Class<Analyzer>) Class.forName(properties.getProperty(Constant.PROP_PREFIX_FIELD + fieldName + Constant.PROP_SUFFIX_ANALYZER))); } catch (Exception ex) {}
                try { builder.setStore(Store.valueOf(properties.getProperty(Constant.PROP_PREFIX_FIELD + fieldName + Constant.PROP_SUFFIX_STORE))); } catch (Exception ex) {}
                try { builder.setIndex(Index.valueOf(properties.getProperty(Constant.PROP_PREFIX_FIELD + fieldName + Constant.PROP_SUFFIX_INDEX))); } catch (Exception ex) {}
                try { builder.setTermVector(TermVector.valueOf(properties.getProperty(Constant.PROP_PREFIX_FIELD + fieldName + Constant.PROP_SUFFIX_TERMVECTOR))); } catch (Exception ex) {}
                try { builder.setType(FieldType.valueOf(properties.getProperty(Constant.PROP_PREFIX_FIELD + fieldName + Constant.PROP_SUFFIX_FIELDTYPE))); } catch (Exception ex) {}
                params.put(fieldName, builder.build());
            }
        }
        return params;
    }
    
    private final String fieldName;
    private final Class<Analyzer> analyzerClass;
    private final Store store;
    private final Index index;
    private final TermVector tv;
    private final FieldType type;
    
    public FieldParams(String fieldName, Class<? extends Analyzer> analyzerClass, Store store, Index index, TermVector tv, FieldType type) {
        this.fieldName = fieldName;
        this.analyzerClass = (Class<Analyzer>) analyzerClass;
        this.store = store;
        this.index = index;
        this.tv = tv;
        this.type = type;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public Class<Analyzer> getAnalyzerClass() {
        return analyzerClass;
    }

    public Store getStore() {
        return store;
    }

    public Index getIndex() {
        return index;
    }

    public TermVector getTermVector() {
        return tv;
    }

    public FieldType getType() {
        return type;
    }
    
    public static final class Builder {
        
        private final String fieldname;
        private Class<? extends Analyzer> analyzerClass = null;
        private Store store = null;
        private Index index = null;
        private TermVector tv = null;
        private FieldType type = null;

        public Builder(String fieldName) {
            this.fieldname = fieldName;
        }

        public Builder setAnalyzerClass(Class<? extends Analyzer> analyzerClass) {
            this.analyzerClass = analyzerClass;
            return this;
        }

        public Builder setStore(Store store) {
            this.store = store;
            return this;
        }

        public Builder setIndex(Index index) {
            this.index = index;
            return this;
        }

        public Builder setTermVector(TermVector tv) {
            this.tv = tv;
            return this;
        }

        public Builder setType(FieldType type) {
            this.type = type;
            return this;
        }
        
        public FieldParams build() {
            return new FieldParams(fieldname,
                    analyzerClass != null ? analyzerClass : DEFAULT_ANALYZER_CLASS,
                    store != null ? store : DEFAULT_STORE,
                    index != null ? index : DEFAULT_INDEX,
                    tv != null ? tv : DEFAULT_TERM_VECTOR,
                    type != null ? type : DEFAULT_FIELD_TYPE);
        }
        
    }
    
}
