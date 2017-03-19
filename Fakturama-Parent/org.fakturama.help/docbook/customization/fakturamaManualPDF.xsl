<?xml version='1.0'?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">
  <!-- import of the origin XSL and a custom title page -->
  <xsl:import href="/Projekte/herold-doclet/docbook-xsl-1.79.1/fo/docbook.xsl"/> 
  <xsl:import href="fakturamaManualTitlepage.xsl"/>
  
  <xsl:param name="draft.mode" select="'no'"/>

  <!-- setting some params -->
  <xsl:param name="html.stylesheet" select="'corpstyle.css'"/>
  <xsl:param name="admon.graphics">1</xsl:param>
  <xsl:param name="admon.textlabel">1</xsl:param>
  <xsl:param name="paper.type">A4</xsl:param>
  <xsl:param name="double.sided">1</xsl:param>
  <!-- turning on the extension is for using bookmarks in pdf -->
  <xsl:param name="fop.extensions">0</xsl:param>
  <!-- if using FOP 1.x we have to turn on the appropriate extension -->
  <xsl:param name="fop1.extensions">1</xsl:param>
  <xsl:param name="sidebar.float.type">start</xsl:param>
  
  <!-- use DocBook extensions -->
  <xsl:param name="use.extensions">0</xsl:param>
  <!-- this is for processing olinks -->
  <xsl:param name="current.docid" select="/*/@id"/>
  
  <!-- font styles
  <xsl:param name="body.font.family">sans-serif</xsl:param>
  <xsl:param name="title.font.family">serif</xsl:param> -->  
  
  <!-- set the numbering for sections -->
  <xsl:param name="section.autolabel">1</xsl:param>
  <xsl:param name="section.label.includes.component.label">1</xsl:param>
  <xsl:param name="section.autolabel.max.depth">1</xsl:param>
  <xsl:param name="toc.section.depth">1</xsl:param>
  <xsl:param name="xref.with.number.and.title">0</xsl:param>

  <!-- suppress URL inside external links -->
  <xsl:param name="ulink.show">0</xsl:param>
  
  <!-- adapted from origin DocBook XSL fo/pagesetup.xsl -->
<xsl:template name="footer.content">
  <xsl:param name="pageclass" select="''"/>
  <xsl:param name="sequence" select="''"/>
  <xsl:param name="position" select="''"/>
  <xsl:param name="gentext-key" select="''"/>

<!--
  <fo:block>[DEBUG]
    <xsl:value-of select="$pageclass"/>
    <xsl:text>, </xsl:text>
    <xsl:value-of select="$sequence"/>
    <xsl:text>, </xsl:text>
    <xsl:value-of select="$position"/>
    <xsl:text>, </xsl:text>
    <xsl:value-of select="$gentext-key"/>
  </fo:block>
 -->

  <fo:block>
    <!-- pageclass can be front, body, back -->
    <!-- sequence can be odd, even, first, blank -->
    <!-- position can be left, center, right -->
    <xsl:choose>
      <xsl:when test="$pageclass = 'titlepage'">
        <!-- nop; no footer on title pages -->
      </xsl:when>

      <xsl:when test="$double.sided != 0 and $sequence = 'even'
                      and $position='left'">
        <fo:page-number/>
      </xsl:when>

      <xsl:when test="$double.sided != 0 and ($sequence = 'odd' or $sequence = 'first')
                      and $position='right'">
        <fo:page-number/>
      </xsl:when>

      <xsl:when test="$double.sided = 0 and $position='center'">
        <fo:page-number/>
      </xsl:when>

      <xsl:when test="$sequence='blank'">
        <xsl:choose>
          <xsl:when test="$double.sided != 0 and $position = 'left'">
            <fo:page-number/>
          </xsl:when>
          <xsl:when test="$double.sided = 0 and $position = 'center'">
            <fo:page-number/>
          </xsl:when>
          <xsl:when test="$double.sided != 0 and $position = 'right'">
            <xsl:value-of select="/book/bookinfo/footerurl"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- nop -->
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:otherwise>
        <!-- nop -->
        <xsl:if test="$double.sided != 0 and ($position = 'right' or $position = 'left')">
        <fo:block><xsl:value-of select="/book/bookinfo/footerurl"/></fo:block></xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </fo:block>
</xsl:template>

<!-- Customizing cross reference typography -->
<xsl:attribute-set name="xref.properties">
  <xsl:attribute name="color">
    <xsl:choose>
      <xsl:when test="self::ulink">#000080</xsl:when>
      <xsl:when test="self::xref">#000080</xsl:when>
      <xsl:when test="self::link">#000080</xsl:when>
      <xsl:otherwise>inherit</xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level1.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.7"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-width">
    <xsl:text>0.25pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-style">
    <xsl:text>solid</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-color">
    <xsl:text>gray</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="margin-top">
    <xsl:text>25pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level2.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.6"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="color">
    <xsl:text>#445588</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-width">
    <xsl:text>0.25pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-style">
    <xsl:text>solid</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-bottom-color">
    <xsl:text>gray</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="margin-top">
    <xsl:text>30pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level3.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.1"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="margin-top">
    <xsl:text>10pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:template match="*" mode="simple.xlink.properties">
  <!-- Placeholder template to apply properties to links made from
       elements other than xref, link, and olink.
       This template should generate attributes only, as it is
       applied right after the opening <fo:basic-link> tag.
       -->
  <!-- for example  -->
  <xsl:attribute name="color">#000080</xsl:attribute>
  <xsl:attribute name="text-decoration">underline</xsl:attribute>  
  <!-- Since this is a mode, you can create different
       templates with different properties for different linking elements -->
</xsl:template>

<!-- wrap long listings -->
<xsl:attribute-set name="monospace.verbatim.properties">
    <xsl:attribute name="wrap-option">wrap</xsl:attribute>
    <xsl:attribute name="hyphenation-character">\</xsl:attribute>
</xsl:attribute-set>

<!-- decrease font size -->
<xsl:attribute-set name="monospace.verbatim.properties">
  <xsl:attribute name="font-size">
    <xsl:choose>
      <xsl:when test="processing-instruction('db-font-size')"><xsl:value-of
           select="processing-instruction('db-font-size')"/></xsl:when>
      <xsl:otherwise>inherit</xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:template  match="sect1|sect2|sect3|sect4|sect5|section"  
               mode="insert.title.markup">
  <xsl:param name="purpose"/>
  <xsl:param name="xrefstyle"/>
  <xsl:param name="title"/>

  <xsl:choose>
    <xsl:when test="$purpose = 'xref'">
      <fo:inline font-style="italic">
        <xsl:copy-of select="$title"/>
      </fo:inline>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="$title"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
 
</xsl:stylesheet>  