# Assignment1_Indexing
This repository contains the details of TREC- Car Paragraph corpus searching and indexing.
Installation
1. Clone the repository into your desired directory in your system.
2. Open a terminal and move into the directory.
3. You should find a pom.xml file. Enter the following command:
               mvn package
Execution
1.Once the above command is executed, you should find a new directory called target. 2.Change your current directory to target.
                cd target
Building the Lucene Index
1.In order to create the Lucene Index. Run the following command where the first argument is the paragraph corpus file and the second argument is the output directory into which the index will be created.
java -jar Lucene_indexing-jar-with-dependencies.jar <paragraphCBOR corpus file> <LuceneIndex directory>
