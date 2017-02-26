create a single xml docbook file

* create one html file if not exists
* after this do:

java -jar d:\Projekte\herold-doclet\herold\jars\herold.jar --in *.html --out meins.xml -p d:\Projekte\herold-doclet\herold\profiles\default.her

This creates a single xml docbook file.

replace all "./../../com.sebulli.fakturama.help" by ".."

Create PDF:
d:\Projekte\herold-doclet\fop-2.0\fop.bat -xml meins.xml -xsl d:\Projekte\herold-doclet\docbook-xsl-ns\fo\docbook.xsl -pdf meins.pdf -param paper.type A4

Example for splitting a big docbook file (book) into single chunks:
java -jar d:\ZusatzSW\XSLT\xalan-j_2_7_2\xalan.jar -IN firstTest.xml -XSL .\dbchunk\dbsplit.xsl -out meins


