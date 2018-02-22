This is the main source folder for the Fakturama help files. The sources are written in DocBook v5.2 format, since we have to create several output formats. 
To create the manual in PDF format you have to install the DocBook XSL files locally. Currently, you can only use FOP 1.1 for PDF creation.

Here's the structure of this folder:

<table><tr><td width="150">manual-main.xml</td><td>The main file. It references the chapters in chapter subfolder.</td></tr>
<tr><td>customization</td><td>customization layer, contains some individual settings, template changes and the titlepage template</td></tr>
<tr><td>chapters</td><td>all the chapters from manual</td></tr>
<tr><td>images</td><td>images for DocBook adminitions (tip, warning etc)</td></tr></table>
