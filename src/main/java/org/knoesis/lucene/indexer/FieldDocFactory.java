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

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

/**
 * This class provides convenient construction, caching, and recycling of 
 * {@code Fieldable}s and {@code Document}s. For efficiency reasons (decreasing 
 * memory footprint), {@link CorpusReader}s and {@link Indexer}s should obtain 
 * all {@code Fieldable}s and {@code Document}s via the methods of this class, 
 * rather than constructing new ones on their own.
 * @author Alan Smith
 */
public class FieldDocFactory {
    
    private final ConcurrentMap<String, FieldParams> fieldParams;
    private final ConcurrentMap<String, BlockingQueue<SoftReference<Fieldable>>> fieldCache = new ConcurrentHashMap<String, BlockingQueue<SoftReference<Fieldable>>>();
    private final BlockingQueue<SoftReference<Document>> docCache;
    private final int maxCacheSize;
    
    FieldDocFactory(Map<String, FieldParams> fieldParams) {
        this(fieldParams, Constant.DEFAULT_MAX_CACHE_SIZE);
    }
    
    FieldDocFactory(Map<String, FieldParams> fieldParams, int maxCacheSize) {
        this.fieldParams = new ConcurrentHashMap<String, FieldParams>(fieldParams);
        this.maxCacheSize = maxCacheSize;
        this.docCache = new LinkedBlockingQueue<SoftReference<Document>>(maxCacheSize);
    }
    
    /**
     * Returns a cached or newly created field with the specified name and 
     * value, and {@link FieldType} specified in the properties file. If the 
     * {@link FieldType} is numeric and a {@code NumberFormatException} is 
     * thrown while attempting to parse {@code fieldValue}, the field will be 
     * converted to type {@code Field} (text) for indexing.
     * @param fieldName the name of the field to create (or return from cache)
     * @param fieldValue the (possibly numeric) value this field should contain
     * @return a new or recycled {@code Fieldable} with the specified name and 
     * value
     */
    public Fieldable createField(String fieldName, String fieldValue) {
        BlockingQueue<SoftReference<Fieldable>> fields = fieldCache.get(fieldName);
        if (fields == null) {
            BlockingQueue<SoftReference<Fieldable>> newFields = new LinkedBlockingQueue<SoftReference<Fieldable>>(maxCacheSize);
            fields = fieldCache.putIfAbsent(fieldName, newFields);
            if (fields == null) {
                fields = newFields;
            }
        }
        Fieldable field;
        SoftReference<Fieldable> fieldRef = fields.poll();
        if (fieldRef == null) {
            field = create(fieldName);
        } else {
            field = fieldRef.get();
            if (field == null) {
                field = create(fieldName);
            }
        }
        FieldParams params = fieldParams.get(fieldName);
        try {
            switch (params.getType()) {
                case INT:
                    ((NumericField)field).setIntValue(Integer.parseInt(fieldValue));
                    break;
                case LONG:
                    ((NumericField)field).setLongValue(Long.parseLong(fieldValue));
                    break;
                case FLOAT:
                    ((NumericField)field).setFloatValue(Float.parseFloat(fieldValue));
                    break;
                case DOUBLE:
                    ((NumericField)field).setDoubleValue(Double.parseDouble(fieldValue));
                    break;
                default:
                    ((Field)field).setValue(fieldValue);
            }
        } catch (NumberFormatException ex) {
            Logger.getLogger(FieldDocFactory.class.getName()).log(Level.WARNING, String.format("Unable to parse \"%s\" as %s for field %s. Adding as text.", fieldValue, params.getType(), fieldName), ex);
            ((Field)field).setValue(fieldValue);
        }
       return field;
    }
    
    /**
     * Creates a new {@code Document} or returns one from cache. If the 
     * {@code Document} comes from cache, it is "certified like-new", containing 
     * no fields and having a boost of 1.0 (the default).
     * @return a new {@code Document}, or an empty one from cache
     */
    public Document createDocument() {
        Document doc;
        SoftReference<Document> docRef = docCache.poll();
        if (docRef == null) {
            return new Document();
        } else {
            doc = docRef.get();
            if (doc == null) {
                doc = new Document();
            }
            return doc;
        }
    }
    
    /**
     * Recycles a document, removing all its fields and adding them to cache, 
     * resetting the document's boost value to the default (1.0), and adding the
     * document instance to cache.
     * @param document the document to recycle
     */
    public void recycle(Document document) {
        Set<String> fieldNames = new HashSet<String>();
        for (Fieldable field : document.getFields()) {
            fieldNames.add(field.name());
        }
        for (String fieldName : fieldNames) {
            BlockingQueue<SoftReference<Fieldable>> cache = fieldCache.get(fieldName);
            for (Fieldable field : document.getFieldables(fieldName)) {
                cache.offer(new SoftReference<Fieldable>(field));
            }
            document.removeFields(fieldName);
        }
        document.setBoost(1.0f);
        docCache.offer(new SoftReference<Document>(document));
    }

    Iterable<FieldParams> getFieldParams() {
        return Collections.unmodifiableCollection(fieldParams.values());
    }
    
    private Fieldable create(String fieldName) {
        FieldParams params = fieldParams.get(fieldName);
        if (params == null) {
            FieldParams newParams = FieldParams.defaults(fieldName);
            params = fieldParams.putIfAbsent(fieldName, newParams);
            if (params == null) {
                params = newParams;
            }
        }
        if (params.getType() == FieldType.TEXT) {
            return new Field(fieldName, "", params.getStore(), params.getIndex(), params.getTermVector());
        }
        return new NumericField(fieldName, params.getStore(), params.getIndex() != Field.Index.NO);
    }
    
}

