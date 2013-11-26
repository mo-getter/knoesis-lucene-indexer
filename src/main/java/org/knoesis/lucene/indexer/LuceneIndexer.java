package org.knoesis.lucene.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.knoesis.lucene.indexer.utils.PropUtils;
import org.knoesis.util.concurrent.producerconsumer.ProducerConsumer;

/**
 * Main class for creating Lucene indexes.<br/><br/>In addition to constructing 
 * a {@code LuceneIndexer} directly, this can be invoked in the following ways:
 *  <ul>
 *      <li>Set your project's main class to be this class.</li>
 *      <li>Call this method's main method via {@link #main(java.lang.String[])}</li>
 *  </ul>
 *
 */
public class LuceneIndexer {
    
    private static final String INDEX_PROPERTIES_FILENAME = "index.properties";
    
    private final Properties properties;
    private final ProducerConsumer<Document> pc;
    private final IndexWriter writer;
    private final FieldDocFactory fields;
    private final boolean forceMerge;

    public LuceneIndexer(Properties properties, boolean forceOverwrite) throws Exception {
        this.properties = properties;
        int bufferSize = PropUtils.getInt(properties, Constant.PROP_DOC_BUFFER_SIZE, Integer.MAX_VALUE);
        int numIndexerThreads = PropUtils.getInt(properties, Constant.PROP_INDEXER_THREADS, Constant.DEFAULT_NUM_CONSUMERS);
        String corpusReaderClassName = properties.getProperty(Constant.PROP_CORPUS_READER_CLASS);
        String indexerClassName = properties.getProperty(Constant.PROP_INDEXER_CLASS);
        forceMerge = PropUtils.getBoolean(properties, Constant.PROP_FORCE_MERGE);
        boolean verbose = PropUtils.getBoolean(properties, Constant.PROP_VERBOSE);
        
        fields = new FieldDocFactory(FieldParams.parseProperties(properties), PropUtils.getInt(properties, Constant.PROP_DOC_FIELD_CACHE_SIZE, Constant.DEFAULT_MAX_CACHE_SIZE));
        writer = IndexWriterFactory.createIndexWriter(properties, fields, forceOverwrite);
        if (verbose) {
            writer.setInfoStream(System.out);
        }
        
        CorpusReader corpusReader = (CorpusReader) Class.forName(corpusReaderClassName).newInstance();
        corpusReader.init(fields, properties);
        Class<Indexer> indexerClass = (Class<Indexer>) Class.forName(indexerClassName);
        
        ProducerConsumer.Builder<Document> pcBuilder = ProducerConsumer.<Document>newBuilder()
                .setBufferSize(bufferSize)
                .addProducer(corpusReader);
        
        for (int i=0; i<numIndexerThreads; i++) {
            Indexer indexer = indexerClass.newInstance();
            indexer.init(writer, fields, properties);
            pcBuilder.addConsumer(indexer);
        }
        pc = pcBuilder.build();
    }
    
    /**
     * Creates, commits, and optionally optimizes the Lucene index, blocking 
     * until all threads are finished.
     * @throws InterruptedException
     * @throws IOException 
     */
    public void createIndex() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        pc.begin();
        writer.prepareCommit();
        writer.commit();
        System.out.format("Indexed %s documents in %d minutes\n", NumberFormat.getInstance().format(writer.numDocs()), 
                TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start));
        if (forceMerge) {
            System.out.println("Merging segments...");
            writer.forceMerge(1, true);
            writer.prepareCommit();
            writer.commit();
        }
        writer.close();
    }
    
    /**
     * This method can be used as a main entry point, as it offers the 
     * convenience of parsing and validating the command-line arguments, as well
     * as printing usage information.
     * @param args
     * @throws Exception 
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption(OptionBuilder
                .withLongOpt("properties")
                .withDescription("LuceneIndexer properties file path")
                .hasArg()
                .withArgName("PATH")
                .create('p'));
        options.addOption(OptionBuilder
                .withLongOpt("force-overwrite")
                .withDescription("Overwrite (rather than append to) existing index if present")
                .create());
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage(null, options);
            throw ex;
        }
        
        if (!commandLine.hasOption('p') || commandLine.getOptionValue('p') == null) {
            usage("properties argument required", options);
            return;
        }
        
        Properties properties = new Properties();
        InputStream in = new FileInputStream(commandLine.getOptionValue('p'));
        try {
            properties.load(in);
        } finally {
            in.close();
        }
        
        LuceneIndexer indexer = new LuceneIndexer(properties, commandLine.hasOption("force-overwrite"));
        indexer.createIndex();
        indexer.copyPropertiesFileToIndexDir(commandLine.getOptionValue('p'));
        
    }
    
    private static void usage(String error, Options options) {
        if (error != null) {
            System.out.println("error: " + error);
        }
        new HelpFormatter().printHelp("java -jar LuceneIndexer.jar [OPTIONS]", options);
    }

    private void copyPropertiesFileToIndexDir(String propsPath) throws IOException {
        String luceneDir = properties.getProperty(Constant.PROP_LUCENE_DIR);
        FileChannel origProps = null;
        FileChannel newProps = null;
        try {
            origProps = new FileInputStream(propsPath).getChannel();
            newProps = new FileOutputStream(new File(luceneDir).getPath() + File.separator + INDEX_PROPERTIES_FILENAME).getChannel();
            newProps.transferFrom(origProps, 0, origProps.size());
        } finally {
            if (origProps != null) {
                origProps.close();
            }
            if (newProps != null) {
                newProps.close();
            }
        }
        
    }
    
}
