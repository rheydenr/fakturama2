This is the main source folder for the Fakturama help files. The sources are written in DocBook v5.2 format, since we have to create several output formats. 
To create the manual in PDF format you have to install the DocBook XSL files locally. Currently, you can only use FOP 1.1 for PDF creation.

Here's the structure of this folder:

manual-main.xml .. The main file. It references the chapters in chapter subfolder. 
customization .... customization layer, contains some individual settings, template changes and the titlepage template
chapters ......... all the chapters from manual
images ........... images for DocBook adminitions (tip, warning etc)
