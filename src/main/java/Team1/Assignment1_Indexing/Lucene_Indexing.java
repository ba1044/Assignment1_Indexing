/* This Program contains Indexing for our dataset then creating lucene search engine.
After that we are getting all queries 
and  retrieving search result */ 
		

package Team1.Assignment1_Indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import co.nstant.in.cbor.CborException;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class Lucene_Indexing {

	static final private String INDEX_DIRECTORY = "index";
	static final private String OUTPUT_DIR = "output";

	static ArrayList<String> pageQueryList;
	static ArrayList<String> sectionQueryList;
	//newly added
	static ArrayList<String> allHeadingList;
	static ArrayList<String> allSectionList;
	//end
	static final private int Max_Results = 100;
	static String pagesRunFileName = "bm25Pages.run";
	static String sectionsRunFileName = "bm25Sections.run";

	

	public static void main(String[] args) {
		
        String queryPath="I:/CS980Assignment1/benchmarkY1-train.v2.0.tar (1)/benchmarkY1-train.v2.0/benchmarkY1/benchmarkY1-train/train.pages.cbor";
		String dataPath="I:/CS980Assignment1/paragraphCorpus.v2.0.tar (1)/paragraphCorpus.v2.0/paragraphCorpus/dedup.articles-paragraphs.cbor";
	    //System.setProperty("file.encoding", "UTF-8");
		

		try {

			 //*************************** indexing all data******************************************
			System.out.println("Start indexing");
			
			// This function is being used for creating indexes.Index_Directory will contain the indexes.
			indexAllData(INDEX_DIRECTORY, dataPath);   
            checkQuery_Data(queryPath);
			ArrayList<String> pageList = getpageQueries();
			ArrayList<String> sectionList = getSectionQueries();
			// Creation of Pages  run file ...
			System.out.println("Total Page list size=" + pageList.size());

			ArrayList<String> pageResults = fetchSearchResult(pageList, Max_Results);
			System.out.println("Total Page List Size received is " + pageResults.size() + " and results are being written to " + OUTPUT_DIR + "/"
					+ pagesRunFileName);
			writeToFile(pagesRunFileName, pageResults);
			System.out.println("Pages run File Completed.");

			// Creation of section run file ...
			System.out.println("Total Section sizes are = " + sectionList.size() + " sections...");
			ArrayList<String> sectionResults = fetchSearchResult(sectionList, Max_Results);
			
			System.out.println("Got " + sectionResults.size() + " results for sections. Write results to " + OUTPUT_DIR
					+ "/" + sectionsRunFileName);
			writeToFile(sectionsRunFileName, sectionResults);
			System.out.println("Sections Run File  Completed.");

		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}
	
	public static void extractAllQueryData(String queryFilePath)
	{
		if (allHeadingList==null || allSectionList ==null)
		{
			try
			{
				allHeadingList=new ArrayList<String>();
                allSectionList=new ArrayList<String>();
                 storeAllHeading(queryFilePath);				
				
			}catch(FileNotFoundException e)
			{
				System.out.println("Unable to find file");
				e.printStackTrace();
			}
		
		}
		
	}
	
	
	public static void  storeAllHeading(String QueryFilePath) throws FileNotFoundException
	{
		
		
	}
	public static void checkQuery_Data(String queryFilePath) {
		
		if (pageQueryList == null || sectionQueryList == null) {
			try {
				sectionQueryList = new ArrayList<String>();
				pageQueryList = new ArrayList<String>();
				
				StoreAllQueryData(queryFilePath);
			} catch (FileNotFoundException e) {
				System.out.println("Unable to find query data.");
				e.printStackTrace();
			}
		}
	}
	
	// From previous code
	private static void StoreAllQueryData(String file_path) throws FileNotFoundException {
		
		System.out.println("Retrieve queries from " + file_path);
		FileInputStream fis = new FileInputStream((new File(file_path)));

		for (Data.Page page : DeserializeData.iterableAnnotations(fis)) {
			pageQueryList.add(page.getPageName());

			for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
				String queryStr = page.getPageName();
				for (Data.Section section : sectionPath) {
					queryStr += " ";
					queryStr += section.getHeading();
				}
				sectionQueryList.add(queryStr);
			}

		}
	}
	
	public static ArrayList<String> getpageQueries() {
		return pageQueryList;
	}

	public static ArrayList<String> getSectionQueries() {
		return sectionQueryList;
	}
	
	
	// index all data.
	public static void indexAllData(String INDEX_DIRECTORY, String file_path) throws CborException, IOException {
		
		//this directory will contain the indexes
		Directory indexDirectory = FSDirectory.open((new File(INDEX_DIRECTORY)).toPath());
		
		//create the indexer
		//Holds all the configuration that is used to create an IndexWriter
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexDirectory, conf);
        System.out.println("Indexing started...");
         //indexing paragraph
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(file_path)))) {
			Document doc = getDocument(p);
			System.out.println(doc);
			iw.addDocument(doc);

		}
	
		iw.commit();
		iw.close();
		
		System.out.println("Indexing Completed.");
	}

	
	 // from previous
	private static Document getDocument(Data.Paragraph para) throws IOException {
		Document doc = new Document();
         //indexing Praid and its contents
		doc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
		doc.add(new TextField("content", para.getTextOnly(), Field.Store.NO));

		return doc;
	}

	
	// From previous code
	private static ArrayList<String> fetchSearchResult(ArrayList<String> queriesStr, int max_result)
			throws IOException, ParseException {
		ArrayList<String> runFileStr = new ArrayList<String>();

		IndexSearcher searcher = new IndexSearcher(
				DirectoryReader.open(FSDirectory.open((new File(INDEX_DIRECTORY).toPath()))));
		searcher.setSimilarity(new BM25Similarity());

		QueryParser parser = new QueryParser("content", new StandardAnalyzer());

		for (String queryStr : queriesStr) {
			Query q = parser.parse(QueryParser.escape(queryStr));
			

			TopDocs tops = searcher.search(q, max_result);
			ScoreDoc[] scoreDoc = tops.scoreDocs;
			for (int i = 0; i < scoreDoc.length; i++) {
				ScoreDoc score = scoreDoc[i];
				Document doc = searcher.doc(score.doc);
				String paraId = doc.getField("paraid").stringValue();
				float rankScore = score.score;
				int rank = i + 1;

				String runStr = queryStr + " Q0 " + paraId + " " + rank + " " + rankScore + " BM-25";
				runFileStr.add(runStr);
			}
		}

		return runFileStr;
	}

	// From previous code
	
	private static void writeToFile(String filename, ArrayList<String> runfileStrings) {
		String fullpath = OUTPUT_DIR + "/" + filename;
		try (FileWriter runfile = new FileWriter(new File(fullpath))) {
			for (String line : runfileStrings) {
				runfile.write(line + "\n");
			}

			runfile.close();
		} catch (IOException e) {
			System.out.println("Could not open " + fullpath);
		}
	}

}
