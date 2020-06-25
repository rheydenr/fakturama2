@REM Pfad setzen
@REM set PATH="d:\Programme\Java\jdk1.8\bin";d:\Javalibs\fop-2.1;"%PATH%";

@REM aktuelle Fakturama-Version
set VERSION=2.1.1

set PATH=d:\Javalibs\fop-1.1\;%PATH%;d:\Programme\Java\jdk1.8\bin;
set CLASSPATH=.;d:\Javalibs\xalan-j_2_7_2\serializer.jar;d:\Javalibs\xalan-j_2_7_2\xalan.jar;d:\Javalibs\xalan-j_2_7_2\xercesImpl.jar;d:\Javalibs\xalan-j_2_7_2\xml-apis.jar;%CLASSPATH%

@REM if you want to use FOP 2.1 you have to adapt fakturamaManualPDF.xsl
@REM set PATH=d:\Javalibs\fop-2.1\;%PATH%;d:\Programme\Java\jdk1.8\bin;

@REM Erstellen des Titlepage-XSL
java -jar d:\Javalibs\xalan-j_2_7_2\xalan.jar -IN customization\titlepage.spec.xml -XSL d:\Projekte\herold-doclet\docbook-xsl-1.79.1\template\titlepage.xsl -OUT customization/fakturamaManualTitlepage.xsl

@REM Transformation mit FOP und customization layer
fop -xsl customization\fakturamaManualPDF.xsl -xml manual-main.xml -pdf Handbuch-Fakturama_%VERSION%.pdf

@REM Transformation mit xalan
@REM java -jar d:\Javalibs\xalan-j_2_7_2\xalan.jar -IN manual-main.xml -XSL customization\fakturamaManualPDF.xsl -OUT hb.fo

@REM create a DocBook targetdb file
@REM java -jar d:\Javalibs\xalan-j_2_7_2\xalan.jar -IN referencedb/olinkdb.xml -XSL /Projekte/herold-doclet/docbook-xsl-1.79.1/fo/docbook.xsl -PARAM collect.xref.targets "only"

@REM create a HTML file
java -jar d:\Javalibs\xalan-j_2_7_2\xalan.jar -IN manual-main.xml -OUT mybook.html -XSL d:\Projekte\herold-doclet\docbook-xsl-1.79.1\xhtml5\docbook.xsl
@rem xhtml5/chunk.xsl

java -Djava.endorsed.dirs="d:\Javalibs\xerces-2_11_0;d:\Javalibs\xalan-j_2_7_2" -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration org.apache.xalan.xslt.Process -out bookfile.html -in manual-main.xml -xsl d:\Projekte\herold-doclet\docbook-xsl-1.79.1\xhtml5\docbook.xsl
java -Djava.endorsed.dirs="d:\Javalibs\xerces-2_11_0;d:\Javalibs\xalan-j_2_7_2" -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration org.apache.xalan.xslt.Process -out bookfile.html -in manual-main.xml -xsl d:\Projekte\herold-doclet\docbook-xsl-1.79.1\webhelp\xsl\webhelp.xsl
java -Djava.endorsed.dirs="d:\Javalibs\xerces-2_11_0;d:\Javalibs\xalan-j_2_7_2" -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration org.apache.xalan.xslt.Process -out bookfile.epub -in manual-main.xml -xsl d:\Projekte\herold-doclet\docbook-xsl-1.79.1\epub3\chunk.xsl
java -Djava.endorsed.dirs="d:\Javalibs\xerces-2_11_0;d:\Javalibs\xalan-j_2_7_2" -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration org.apache.xalan.xslt.Process -out bookfile.epub -in manual-main.xml -xsl d:\Projekte\herold-doclet\docbook-xsl-1.79.1\epub\docbook.xsl