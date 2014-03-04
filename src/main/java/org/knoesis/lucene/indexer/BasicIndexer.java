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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

/**
 * Provides a basic implementation of an {@link Indexer} which simply adds {@code Document}s to the {@code IndexWriter}.
 * @author Alan Smith
 */
public class BasicIndexer implements Indexer {
    
    private IndexWriter writer;
    private FieldDocFactory fields;

    public void init(IndexWriter writer, FieldDocFactory fields, Properties properties) {
        this.writer = writer;
        this.fields = fields;
    }

    /**
     * Consumes {@code Document}s by simply adding them to the {@code IndexWriter}.
     * @param documents a blocking {@link Iterable} of {@code Document}s produced by the {@link CorpusReader}.
     */
    public void consume(Iterable<Document> documents) {
        for (Document doc : documents) {
            try {
                writer.addDocument(doc);
            } catch (CorruptIndexException ex) {
                Logger.getLogger(BasicIndexer.class.getName()).log(Level.SEVERE, "Failed to add Document to index", ex);
            } catch (IOException ex) {
                Logger.getLogger(BasicIndexer.class.getName()).log(Level.SEVERE, "Failed to add Document to index", ex);
            }
            fields.recycle(doc);
        }
    }

}
