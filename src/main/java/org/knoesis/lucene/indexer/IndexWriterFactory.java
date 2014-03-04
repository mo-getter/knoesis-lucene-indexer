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

package org.knoesis.lucene.indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.knoesis.lucene.indexer.utils.PropUtils;

/**
 *
 * @author Alan Smith
 */
class IndexWriterFactory {
    
    public static IndexWriter createIndexWriter(Properties properties, FieldDocFactory fields, boolean forceOverwrite) throws Exception {
        String luceneDir = properties.getProperty(Constant.PROP_LUCENE_DIR);
        File file = new File(luceneDir);
        if(!file.exists()) {
            if (!file.mkdirs()) {
                throw new IOException("Failed to create new Lucene index directory");
            }
        }
        Directory directory = FSDirectory.open(file);
        Version version = Version.valueOf(properties.getProperty(Constant.PROP_LUCENE_VERSION));
        Class<Analyzer> analyzerClass = (Class<Analyzer>) Class.forName(properties.getProperty(Constant.PROP_DEFAULT_ANALYZER));
        Analyzer defaultAnalyzer = createAnalyzer(analyzerClass, version);
        Map<String, Analyzer> perField = new HashMap<String, Analyzer>();
        for (FieldParams field : fields.getFieldParams()) {
            perField.put(field.getFieldName(), createAnalyzer(field.getAnalyzerClass(), version));
        }
        Analyzer analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer, perField);
        IndexWriterConfig config = new IndexWriterConfig(version, analyzer)
                .setMaxThreadStates(PropUtils.getInt(properties, Constant.PROP_INDEXER_THREADS, Constant.DEFAULT_NUM_CONSUMERS))
                .setOpenMode(forceOverwrite ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
                .setRAMBufferSizeMB(PropUtils.getInt(properties, Constant.PROP_RAM_BUFFER_SIZE_MB, (int) IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB));
        return new IndexWriter(directory, config);
    }
    
    private static Analyzer createAnalyzer(Class<? extends Analyzer> analyzerClass, Version version) throws InstantiationException, IllegalAccessException {
        try {
            return analyzerClass.getConstructor(Version.class).newInstance(version);
        } catch (Exception ex) {
            return analyzerClass.newInstance();
        }
    }

}
