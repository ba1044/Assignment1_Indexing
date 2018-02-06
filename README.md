# Assignment1_Indexing
This repository contains the details of TREC- Car Paragraph corpus searching and indexing.


Installation
Clone the repository into your desired directory in your system.

Open a terminal, and move into the directory.

You should find a pom.xml file. Enter the following command:

mvn package

Execution

Once the above command is executed, you should find a new directory called target. Change your current directory to target.

cd target

Building the Lucene Index

In order to create the Lucene Index. Run the following command where the first argument is the paragraph corpus file and the second argument is the output directory into which the index will be created.

java -jar Builder-jar-with-dependencies.jar <paragraphCBOR> <LuceneIndex>

Querying the Lucene Index

The second step is to query the created Index. Run the following command where the first argument is the cbor outline file and the second argument is the location of the Lucene Index.

java -jar Searcher-jar-with-dependencies.jar <OutlinesCBOR> <LuceneIndex>
