
package org.knoesis.lucene.indexer;

/**
 *
 * @author alan
 */
interface Constant {

    static final String PROP_DOC_BUFFER_SIZE = "luceneindexer.docbuffersize";
    static final String PROP_RAM_BUFFER_SIZE_MB = "luceneindexer.rambuffersizemb";
    static final String PROP_LUCENE_DIR = "luceneindexer.lucenedir";
    static final String PROP_CORPUS_READER_CLASS = "luceneindexer.corpusreaderclass";
    static final String PROP_INDEXER_CLASS = "luceneindexer.indexerclass";
    static final String PROP_INDEXER_THREADS = "luceneindexer.indexerthreads";
    static final String PROP_LUCENE_VERSION = "luceneindexer.version";
    static final String PROP_DOC_FIELD_CACHE_SIZE = "luceneindexer.docfieldcachesize";
    static final String PROP_FORCE_MERGE = "luceneindexer.forcemerge";
    static final String PROP_VERBOSE = "luceneindexer.verbose";
    
    static final String PROP_DEFAULT_ANALYZER = "luceneindexer.default.analyzer";
    static final String PROP_DEFAULT_STORE = "luceneindexer.default.store";
    static final String PROP_DEFAULT_INDEX = "luceneindexer.default.index";
    static final String PROP_DEFAULT_TERMVECTOR = "luceneindexer.default.termvector";
    static final String PROP_DEFAULT_FIELDTYPE = "luceneindexer.default.fieldtype";
    
    static final String PROP_PREFIX_FIELD = "luceneindexer.field.";
    static final String PROP_SUFFIX_FIELDKEY = ".fieldkey";
    static final String PROP_SUFFIX_ANALYZER = ".analyzer";
    static final String PROP_SUFFIX_STORE = ".store";
    static final String PROP_SUFFIX_INDEX = ".index";
    static final String PROP_SUFFIX_TERMVECTOR = ".termvector";
    static final String PROP_SUFFIX_FIELDTYPE = ".fieldtype";
    
    static final int DEFAULT_NUM_CONSUMERS = Runtime.getRuntime().availableProcessors() - 1;
    static final int DEFAULT_MAX_CACHE_SIZE = 128;
    
}
