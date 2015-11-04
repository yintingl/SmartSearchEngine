import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.util.Version;

public class LuceneTester {
	
   //String indexDir = "./src/java/index";
   String indexDir = "./index";
   int k = indexDir.hashCode();
   //String dataDir = "./src/java/data";
   String dataDir = "./data";
   Indexer indexer;
   Searcher searcher;
   Analyzer analyzer;
   public ArrayList<String> results = new ArrayList<String>();

   public static void main(String[] args) {
      LuceneTester tester;
      try {
         tester = new LuceneTester();
         tester.createIndex();
         tester.search("search engine");
      } catch (IOException e) {
         e.printStackTrace();
      } catch (ParseException e) {
         e.printStackTrace();
      }
   }
   
   public LuceneTester() {
	   analyzer = new StandardAnalyzer(Version.LUCENE_43);
   }

   private void createIndex() throws IOException{
      indexer = new Indexer(indexDir);
      int numIndexed;
      long startTime = System.currentTimeMillis();	
      numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
      long endTime = System.currentTimeMillis();
      indexer.close();
      System.out.println(numIndexed+" File indexed, time taken: "
         +(endTime-startTime)+" ms");		
   }

   public void search(String searchQuery) throws IOException, ParseException{
	  QueryParser parser = new QueryParser(Version.LUCENE_43,LuceneConstants.CONTENTS,this.analyzer);
	  Query q = parser.parse(searchQuery);
	  SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<mark>","</mark>");
	  QueryScorer scorer = new QueryScorer(q,LuceneConstants.CONTENTS);
	  Highlighter highlighter = new Highlighter(formatter,scorer);
	  highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
      searcher = new Searcher(indexDir);
      long startTime = System.currentTimeMillis();
      TopDocs hits = searcher.search(searchQuery);
      long endTime = System.currentTimeMillis();
   
      System.out.println(hits.totalHits +
         " documents found. Time :" + (endTime - startTime));
      //TokenStream tokens = null;
      String result = null;
      
      for(ScoreDoc scoreDoc : hits.scoreDocs) {
         Document doc = searcher.getDocument(scoreDoc);
         String text = doc.get(LuceneConstants.CONTENTS);
         //tokens = analyzer.tokenStream("content",new StringReader(LuceneConstants.CONTENTS));
            try {
                    result = highlighter.getBestFragment(analyzer, LuceneConstants.CONTENTS, text);
            } catch (InvalidTokenOffsetsException e) {
                    e.printStackTrace();
            }
            System.out.println("File: "
            + doc.get(LuceneConstants.FILE_PATH));
            System.out.println(result);
            results.add(result);
            //System.out.println("1212121212121 "+resultString);
      }
      searcher.close();
   }
   
   public void testSearch(String searchQuery) throws IOException, ParseException {
       createIndex();
       search(searchQuery);
       System.out.println(results.get(0));
   }
   
   public String getResult() {
       StringBuilder sb = new StringBuilder();
       
       return results.get(results.size()-1);
   }
}