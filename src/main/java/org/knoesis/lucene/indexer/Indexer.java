
package org.knoesis.lucene.indexer;

import java.util.Properties;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.knoesis.util.concurrent.producerconsumer.Consumer;

/**
 * Defines the behavior of documents are indexed. There may be many instances 
 * of this class running in parallel, configured by the main properties file.
 * @author Alan Smith
 */
public interface Indexer extends Consumer<Document> {
    
    /**
     * Initializes the {@code Indexer} with the given {@code IndexWriter}, 
     * {@link FieldDocFactory}, and the {@link Properties} that were specified 
     * upon program invocation. This provides a means for 
     * implementation-dependent configuration parameters specific to a 
     * particular class of this type.
     * @param writer the {@code IndexWriter} to use for indexing
     * @param fields the {@code FieldDocFactory} to be used by this instance.
     * @param properties 
     */
    void init(IndexWriter writer, FieldDocFactory fields, Properties properties);
    
    /**
     * Adds {@code Document}s to the index. <b>Important:</b> this method is 
     * also responsible for recycling each document after it is added to the 
     * {@code IndexWriter}, via {@link FieldDocFactory#recycle(org.apache.lucene.document.Document)}.
     * @param documents {@code Document}s to be indexed by the {@code IndexWriter}.
     */
    void consume(Iterable<Document> documents);

}
