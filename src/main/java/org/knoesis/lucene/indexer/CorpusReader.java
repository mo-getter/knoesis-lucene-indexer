
package org.knoesis.lucene.indexer;

import java.util.Properties;
import org.apache.lucene.document.Document;
import org.knoesis.util.concurrent.producerconsumer.Producer;

/**
 * An interface for defining the behavior a corpus reader (a producer of 
 * documents to be indexed). Since only one instance is constructed, the 
 * {@link #produce(org.knoesis.utils.concurrent.producerconsumer.Production)} 
 * method should do as little work as possible, delegating work to be done to the 
 * {@link Indexer}s, which may run concurrently.
 * @author Alan Smith
 */
public interface CorpusReader extends Producer<Document> {
    
    /**
     * Initializes the {@code CorpusReader} with the given {@link FieldDocFactory} and the {@link Properties} that were specified upon program 
     * invocation. This provides a means to provide implementation-dependent 
     * configuration parameters specific to a particular class of this type.
     * @param fields the {@code FieldDocFactory} to be used by this instance.
     * @param properties 
     */
    void init(FieldDocFactory fields, Properties properties);

}
