<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://docbook.org/ns/docbook" xmlns:exsl="http://exslt.org/common" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0" exclude-result-prefixes="exsl d">

<!-- This stylesheet was created by template/titlepage.xsl-->

<xsl:template name="book.titlepage.recto">
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:bookinfo/d:mediaobject"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:info/d:mediaobject"/>
  <xsl:choose>
    <xsl:when test="d:bookinfo/d:title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:bookinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:bookinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="d:info/d:releaseinfo"/>
</xsl:template>

<xsl:template name="book.titlepage.verso">
  <xsl:choose>
    <xsl:when test="d:bookinfo/d:title">
      <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:corpauthor"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:corpauthor"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:authorgroup"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:authorgroup"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:author"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:author"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:othercredit"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:othercredit"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:releaseinfo"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:pubdate"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:pubdate"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:copyright"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:copyright"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:abstract"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:abstract"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:legalnotice"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:legalnotice"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:bookinfo/d:biblioid[@class = 'isbn']"/>
  <xsl:apply-templates mode="book.titlepage.verso.auto.mode" select="d:info/d:biblioid[@class = 'isbn']"/>
</xsl:template>

<xsl:template name="book.titlepage.separator"><fo:block break-after="page"/>
</xsl:template>

<xsl:template name="book.titlepage.before.recto">
</xsl:template>

<xsl:template name="book.titlepage.before.verso"><fo:block break-after="page"/>
</xsl:template>

<xsl:template name="book.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="book.titlepage.before.recto"/>
      <xsl:call-template name="book.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="book.titlepage.before.verso"/>
      <xsl:call-template name="book.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="book.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="book.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="book.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:mediaobject" mode="book.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.recto.style">
<xsl:apply-templates select="." mode="book.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:title" mode="book.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.recto.style" text-align="center" font-size="24.8832pt" space-before="4cm" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="division.title">
<xsl:with-param name="node" select="ancestor-or-self::d:book[1]"/>
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:releaseinfo" mode="book.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="book.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:title" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style" font-size="14.4pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="book.verso.title">
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:corpauthor" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:authorgroup" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:call-template name="verso.authorgroup">
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:author" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:othercredit" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:releaseinfo" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style" space-before="0.5em">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:pubdate" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style" space-before="1em">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:copyright" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:abstract" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:legalnotice" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style" font-size="8pt">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:biblioid" mode="book.titlepage.verso.auto.mode">
<fo:block xsl:use-attribute-sets="book.titlepage.verso.style">
<xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="dedication.titlepage.recto">
  <fo:block xsl:use-attribute-sets="dedication.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:dedication[1]"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:dedicationinfo/d:subtitle">
      <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:dedicationinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:dedicationinfo/d:itermset"/>
  <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="dedication.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="dedication.titlepage.verso">
</xsl:template>

<xsl:template name="dedication.titlepage.separator">
</xsl:template>

<xsl:template name="dedication.titlepage.before.recto">
</xsl:template>

<xsl:template name="dedication.titlepage.before.verso">
</xsl:template>

<xsl:template name="dedication.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="dedication.titlepage.before.recto"/>
      <xsl:call-template name="dedication.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="dedication.titlepage.before.verso"/>
      <xsl:call-template name="dedication.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="dedication.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="dedication.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="dedication.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="dedication.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="dedication.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="dedication.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="dedication.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="dedication.titlepage.recto.style">
<xsl:apply-templates select="." mode="dedication.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="acknowledgements.titlepage.recto">
  <fo:block xsl:use-attribute-sets="acknowledgements.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:acknowledgements[1]"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:acknowledgementsinfo/d:subtitle">
      <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:acknowledgementsinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:acknowledgementsinfo/d:itermset"/>
  <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="acknowledgements.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="acknowledgements.titlepage.verso">
</xsl:template>

<xsl:template name="acknowledgements.titlepage.separator">
</xsl:template>

<xsl:template name="acknowledgements.titlepage.before.recto">
</xsl:template>

<xsl:template name="acknowledgements.titlepage.before.verso">
</xsl:template>

<xsl:template name="acknowledgements.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="acknowledgements.titlepage.before.recto"/>
      <xsl:call-template name="acknowledgements.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="acknowledgements.titlepage.before.verso"/>
      <xsl:call-template name="acknowledgements.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="acknowledgements.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="acknowledgements.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="acknowledgements.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="acknowledgements.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="acknowledgements.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="acknowledgements.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="acknowledgements.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="acknowledgements.titlepage.recto.style">
<xsl:apply-templates select="." mode="acknowledgements.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="preface.titlepage.recto">
  <fo:block xsl:use-attribute-sets="preface.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:preface[1]"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:prefaceinfo/d:subtitle">
      <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:corpauthor"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:corpauthor"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:corpauthor"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:authorgroup"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:authorgroup"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:authorgroup"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:author"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:author"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:author"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:othercredit"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:othercredit"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:othercredit"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:releaseinfo"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:copyright"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:copyright"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:copyright"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:legalnotice"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:legalnotice"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:legalnotice"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:pubdate"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:pubdate"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:pubdate"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:revision"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:revision"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:revision"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:revhistory"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:revhistory"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:revhistory"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:abstract"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:abstract"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:abstract"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:prefaceinfo/d:itermset"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="preface.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="preface.titlepage.verso">
</xsl:template>

<xsl:template name="preface.titlepage.separator">
</xsl:template>

<xsl:template name="preface.titlepage.before.recto">
</xsl:template>

<xsl:template name="preface.titlepage.before.verso">
</xsl:template>

<xsl:template name="preface.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="preface.titlepage.before.recto"/>
      <xsl:call-template name="preface.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="preface.titlepage.before.verso"/>
      <xsl:call-template name="preface.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="preface.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="preface.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="preface.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:corpauthor" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:authorgroup" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:author" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:othercredit" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:releaseinfo" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:copyright" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:legalnotice" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:pubdate" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revision" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revhistory" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:abstract" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="preface.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="preface.titlepage.recto.style">
<xsl:apply-templates select="." mode="preface.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="appendix.titlepage.recto">
  <xsl:choose>
    <xsl:when test="d:appendixinfo/d:title">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:title">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:choose>
    <xsl:when test="d:appendixinfo/d:subtitle">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:corpauthor"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:corpauthor"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:corpauthor"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:authorgroup"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:authorgroup"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:authorgroup"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:author"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:author"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:author"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:othercredit"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:othercredit"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:othercredit"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:releaseinfo"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:copyright"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:copyright"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:copyright"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:legalnotice"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:legalnotice"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:legalnotice"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:pubdate"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:pubdate"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:pubdate"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:revision"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:revision"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:revision"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:revhistory"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:revhistory"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:revhistory"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:abstract"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:abstract"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:abstract"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:appendixinfo/d:itermset"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="appendix.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="appendix.titlepage.verso">
</xsl:template>

<xsl:template name="appendix.titlepage.separator">
</xsl:template>

<xsl:template name="appendix.titlepage.before.recto">
</xsl:template>

<xsl:template name="appendix.titlepage.before.verso">
</xsl:template>

<xsl:template name="appendix.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="appendix.titlepage.before.recto"/>
      <xsl:call-template name="appendix.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="appendix.titlepage.before.verso"/>
      <xsl:call-template name="appendix.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="appendix.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="appendix.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="appendix.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:title" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:appendix[1]"/>
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:subtitle" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:corpauthor" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:authorgroup" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:author" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:othercredit" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:releaseinfo" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:copyright" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:legalnotice" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:pubdate" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revision" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revhistory" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:abstract" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="appendix.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="appendix.titlepage.recto.style">
<xsl:apply-templates select="." mode="appendix.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="glossary.titlepage.recto">
  <fo:block xsl:use-attribute-sets="glossary.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:glossary[1]"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:glossaryinfo/d:subtitle">
      <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:glossaryinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:glossaryinfo/d:itermset"/>
  <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="glossary.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="glossary.titlepage.verso">
</xsl:template>

<xsl:template name="glossary.titlepage.separator">
</xsl:template>

<xsl:template name="glossary.titlepage.before.recto">
</xsl:template>

<xsl:template name="glossary.titlepage.before.verso">
</xsl:template>

<xsl:template name="glossary.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="glossary.titlepage.before.recto"/>
      <xsl:call-template name="glossary.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="glossary.titlepage.before.verso"/>
      <xsl:call-template name="glossary.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="glossary.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="glossary.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="glossary.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="glossary.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="glossary.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="glossary.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="glossary.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="glossary.titlepage.recto.style">
<xsl:apply-templates select="." mode="glossary.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="glossdiv.titlepage.recto">
  <xsl:choose>
    <xsl:when test="d:glossdivinfo/d:title">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:glossdivinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:title">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:docinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:choose>
    <xsl:when test="d:glossdivinfo/d:subtitle">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:glossdivinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:glossdivinfo/d:itermset"/>
  <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="glossdiv.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="glossdiv.titlepage.verso">
</xsl:template>

<xsl:template name="glossdiv.titlepage.separator">
</xsl:template>

<xsl:template name="glossdiv.titlepage.before.recto">
</xsl:template>

<xsl:template name="glossdiv.titlepage.before.verso">
</xsl:template>

<xsl:template name="glossdiv.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="glossdiv.titlepage.before.recto"/>
      <xsl:call-template name="glossdiv.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="glossdiv.titlepage.before.verso"/>
      <xsl:call-template name="glossdiv.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="glossdiv.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="glossdiv.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="glossdiv.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:title" mode="glossdiv.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="glossdiv.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="20.736pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:glossdiv[1]"/>
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:subtitle" mode="glossdiv.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="glossdiv.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="glossdiv.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="glossdiv.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="glossdiv.titlepage.recto.style">
<xsl:apply-templates select="." mode="glossdiv.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="index.titlepage.recto">
  <fo:block xsl:use-attribute-sets="index.titlepage.recto.style" margin-left="0pt" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:index[1]"/>
<xsl:with-param name="pagewide" select="1"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:indexinfo/d:subtitle">
      <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:indexinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:indexinfo/d:itermset"/>
  <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="index.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="index.titlepage.verso">
</xsl:template>

<xsl:template name="index.titlepage.separator">
</xsl:template>

<xsl:template name="index.titlepage.before.recto">
</xsl:template>

<xsl:template name="index.titlepage.before.verso">
</xsl:template>

<xsl:template name="index.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="index.titlepage.before.recto"/>
      <xsl:call-template name="index.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="index.titlepage.before.verso"/>
      <xsl:call-template name="index.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="index.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="index.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="index.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="index.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="index.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="index.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="index.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="index.titlepage.recto.style">
<xsl:apply-templates select="." mode="index.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="indexdiv.titlepage.recto">
  <fo:block xsl:use-attribute-sets="indexdiv.titlepage.recto.style">
<xsl:call-template name="indexdiv.title">
<xsl:with-param name="title" select="title"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:indexdivinfo/d:subtitle">
      <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:indexdivinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:indexdivinfo/d:itermset"/>
  <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="indexdiv.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="indexdiv.titlepage.verso">
</xsl:template>

<xsl:template name="indexdiv.titlepage.separator">
</xsl:template>

<xsl:template name="indexdiv.titlepage.before.recto">
</xsl:template>

<xsl:template name="indexdiv.titlepage.before.verso">
</xsl:template>

<xsl:template name="indexdiv.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="indexdiv.titlepage.before.recto"/>
      <xsl:call-template name="indexdiv.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="indexdiv.titlepage.before.verso"/>
      <xsl:call-template name="indexdiv.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="indexdiv.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="indexdiv.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="indexdiv.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="indexdiv.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="indexdiv.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="indexdiv.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="indexdiv.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="indexdiv.titlepage.recto.style">
<xsl:apply-templates select="." mode="indexdiv.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="setindex.titlepage.recto">
  <fo:block xsl:use-attribute-sets="setindex.titlepage.recto.style" margin-left="0pt" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:setindex[1]"/>
<xsl:with-param name="pagewide" select="1"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:setindexinfo/d:subtitle">
      <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:setindexinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:setindexinfo/d:itermset"/>
  <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="setindex.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="setindex.titlepage.verso">
</xsl:template>

<xsl:template name="setindex.titlepage.separator">
</xsl:template>

<xsl:template name="setindex.titlepage.before.recto">
</xsl:template>

<xsl:template name="setindex.titlepage.before.verso">
</xsl:template>

<xsl:template name="setindex.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="setindex.titlepage.before.recto"/>
      <xsl:call-template name="setindex.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="setindex.titlepage.before.verso"/>
      <xsl:call-template name="setindex.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="setindex.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="setindex.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="setindex.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="setindex.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="setindex.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="setindex.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="setindex.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="setindex.titlepage.recto.style">
<xsl:apply-templates select="." mode="setindex.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="colophon.titlepage.recto">
  <fo:block xsl:use-attribute-sets="colophon.titlepage.recto.style" margin-left="{$title.margin.left}" font-size="24.8832pt" font-family="{$title.fontset}" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:colophon[1]"/>
</xsl:call-template></fo:block>
  <xsl:choose>
    <xsl:when test="d:colophoninfo/d:subtitle">
      <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:colophoninfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:colophoninfo/d:itermset"/>
  <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="colophon.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="colophon.titlepage.verso">
</xsl:template>

<xsl:template name="colophon.titlepage.separator">
</xsl:template>

<xsl:template name="colophon.titlepage.before.recto">
</xsl:template>

<xsl:template name="colophon.titlepage.before.verso">
</xsl:template>

<xsl:template name="colophon.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="colophon.titlepage.before.recto"/>
      <xsl:call-template name="colophon.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="colophon.titlepage.before.verso"/>
      <xsl:call-template name="colophon.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="colophon.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="colophon.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="colophon.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:subtitle" mode="colophon.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="colophon.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="colophon.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="colophon.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="colophon.titlepage.recto.style">
<xsl:apply-templates select="." mode="colophon.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="sidebar.titlepage.recto">
  <xsl:choose>
    <xsl:when test="d:sidebarinfo/d:title">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:sidebarinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:title">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:docinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:choose>
    <xsl:when test="d:sidebarinfo/d:subtitle">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:sidebarinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:docinfo/d:subtitle">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:docinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:sidebarinfo/d:itermset"/>
  <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:docinfo/d:itermset"/>
  <xsl:apply-templates mode="sidebar.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="sidebar.titlepage.verso">
</xsl:template>

<xsl:template name="sidebar.titlepage.separator">
</xsl:template>

<xsl:template name="sidebar.titlepage.before.recto">
</xsl:template>

<xsl:template name="sidebar.titlepage.before.verso">
</xsl:template>

<xsl:template name="sidebar.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="sidebar.titlepage.before.recto"/>
      <xsl:call-template name="sidebar.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="sidebar.titlepage.before.verso"/>
      <xsl:call-template name="sidebar.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="sidebar.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="sidebar.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="sidebar.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:title" mode="sidebar.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="sidebar.titlepage.recto.style" font-family="{$title.fontset}" font-weight="bold">
<xsl:apply-templates select="." mode="sidebar.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:subtitle" mode="sidebar.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="sidebar.titlepage.recto.style" font-family="{$title.fontset}">
<xsl:apply-templates select="." mode="sidebar.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="sidebar.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="sidebar.titlepage.recto.style">
<xsl:apply-templates select="." mode="sidebar.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="qandaset.titlepage.recto">
  <xsl:choose>
    <xsl:when test="d:qandasetinfo/d:title">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:blockinfo/d:title">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:title"/>
    </xsl:when>
    <xsl:when test="d:info/d:title">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:title"/>
    </xsl:when>
    <xsl:when test="d:title">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:title"/>
    </xsl:when>
  </xsl:choose>

  <xsl:choose>
    <xsl:when test="d:qandasetinfo/d:subtitle">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:blockinfo/d:subtitle">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:info/d:subtitle">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:subtitle"/>
    </xsl:when>
    <xsl:when test="d:subtitle">
      <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:corpauthor"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:corpauthor"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:corpauthor"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:authorgroup"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:authorgroup"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:authorgroup"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:author"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:author"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:author"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:othercredit"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:othercredit"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:othercredit"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:releaseinfo"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:releaseinfo"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:copyright"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:copyright"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:copyright"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:legalnotice"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:legalnotice"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:legalnotice"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:pubdate"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:pubdate"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:pubdate"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:revision"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:revision"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:revision"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:revhistory"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:revhistory"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:revhistory"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:abstract"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:abstract"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:abstract"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:qandasetinfo/d:itermset"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:blockinfo/d:itermset"/>
  <xsl:apply-templates mode="qandaset.titlepage.recto.auto.mode" select="d:info/d:itermset"/>
</xsl:template>

<xsl:template name="qandaset.titlepage.verso">
</xsl:template>

<xsl:template name="qandaset.titlepage.separator">
</xsl:template>

<xsl:template name="qandaset.titlepage.before.recto">
</xsl:template>

<xsl:template name="qandaset.titlepage.before.verso">
</xsl:template>

<xsl:template name="qandaset.titlepage">
  <fo:block font-family="{$title.fontset}">
    <xsl:variable name="recto.content">
      <xsl:call-template name="qandaset.titlepage.before.recto"/>
      <xsl:call-template name="qandaset.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block start-indent="0pt" text-align="center"><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="qandaset.titlepage.before.verso"/>
      <xsl:call-template name="qandaset.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="qandaset.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="qandaset.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="qandaset.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="d:title" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" keep-with-next.within-column="always" font-size="24.8832pt" font-weight="bold">
<xsl:call-template name="component.title">
<xsl:with-param name="node" select="ancestor-or-self::d:qandaset[1]"/>
</xsl:call-template>
</fo:block>
</xsl:template>

<xsl:template match="d:subtitle" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:corpauthor" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em" font-size="14.4pt">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:authorgroup" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em" font-size="14.4pt">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:author" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em" font-size="14.4pt">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:othercredit" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:releaseinfo" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:copyright" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:legalnotice" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" text-align="start" margin-left="0.5in" margin-right="0.5in" font-family="{$body.fontset}">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:pubdate" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revision" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:revhistory" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:abstract" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style" space-before="0.5em" text-align="start" margin-left="0.5in" margin-right="0.5in" font-family="{$body.fontset}">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template match="d:itermset" mode="qandaset.titlepage.recto.auto.mode">
<fo:block xsl:use-attribute-sets="qandaset.titlepage.recto.style">
<xsl:apply-templates select="." mode="qandaset.titlepage.recto.mode"/>
</fo:block>
</xsl:template>

<xsl:template name="table.of.contents.titlepage.recto">
  <fo:block xsl:use-attribute-sets="table.of.contents.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'TableofContents'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="table.of.contents.titlepage.verso">
</xsl:template>

<xsl:template name="table.of.contents.titlepage.separator">
</xsl:template>

<xsl:template name="table.of.contents.titlepage.before.recto">
</xsl:template>

<xsl:template name="table.of.contents.titlepage.before.verso">
</xsl:template>

<xsl:template name="table.of.contents.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="table.of.contents.titlepage.before.recto"/>
      <xsl:call-template name="table.of.contents.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="table.of.contents.titlepage.before.verso"/>
      <xsl:call-template name="table.of.contents.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="table.of.contents.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="table.of.contents.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="table.of.contents.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.tables.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.tables.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofTables'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.tables.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.tables.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.tables.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.tables.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.tables.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.tables.titlepage.before.recto"/>
      <xsl:call-template name="list.of.tables.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.tables.titlepage.before.verso"/>
      <xsl:call-template name="list.of.tables.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.tables.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.tables.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.tables.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.figures.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.figures.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofFigures'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.figures.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.figures.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.figures.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.figures.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.figures.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.figures.titlepage.before.recto"/>
      <xsl:call-template name="list.of.figures.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.figures.titlepage.before.verso"/>
      <xsl:call-template name="list.of.figures.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.figures.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.figures.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.figures.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.examples.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.examples.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofExamples'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.examples.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.examples.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.examples.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.examples.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.examples.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.examples.titlepage.before.recto"/>
      <xsl:call-template name="list.of.examples.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.examples.titlepage.before.verso"/>
      <xsl:call-template name="list.of.examples.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.examples.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.examples.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.examples.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.equations.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.equations.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofEquations'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.equations.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.equations.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.equations.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.equations.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.equations.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.equations.titlepage.before.recto"/>
      <xsl:call-template name="list.of.equations.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.equations.titlepage.before.verso"/>
      <xsl:call-template name="list.of.equations.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.equations.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.equations.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.equations.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.procedures.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.procedures.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofProcedures'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.procedures.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.procedures.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.procedures.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.procedures.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.procedures.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.procedures.titlepage.before.recto"/>
      <xsl:call-template name="list.of.procedures.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.procedures.titlepage.before.verso"/>
      <xsl:call-template name="list.of.procedures.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.procedures.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.procedures.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.procedures.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage.recto">
  <fo:block xsl:use-attribute-sets="list.of.unknowns.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1.5em" space-before.maximum="2em" space-after="0.5em" start-indent="0pt" font-size="17.28pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofUnknown'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage.verso">
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage.separator">
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage.before.recto">
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage.before.verso">
</xsl:template>

<xsl:template name="list.of.unknowns.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="list.of.unknowns.titlepage.before.recto"/>
      <xsl:call-template name="list.of.unknowns.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="list.of.unknowns.titlepage.before.verso"/>
      <xsl:call-template name="list.of.unknowns.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="list.of.unknowns.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="list.of.unknowns.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="list.of.unknowns.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.tables.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofTables'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.tables.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.tables.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.tables.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.tables.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.tables.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.tables.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.tables.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.tables.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.figures.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofFigures'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.figures.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.figures.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.figures.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.figures.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.figures.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.figures.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.figures.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.figures.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.examples.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofExamples'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.examples.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.examples.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.examples.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.examples.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.examples.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.examples.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.examples.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.examples.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.equations.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofEquations'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.equations.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.equations.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.equations.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.equations.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.equations.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.equations.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.equations.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.equations.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.procedures.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofProcedures'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.procedures.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.procedures.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.procedures.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.procedures.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.procedures.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.procedures.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.procedures.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.procedures.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage.recto">
  <fo:block xsl:use-attribute-sets="component.list.of.unknowns.titlepage.recto.style" space-before.minimum="1em" space-before.optimum="1em" space-before.maximum="1em" space-after="0.5em" margin-left="{$title.margin.left}" font-size="12pt" font-weight="bold" font-family="{$title.fontset}">
<xsl:call-template name="gentext">
<xsl:with-param name="key" select="'ListofUnknown'"/>
</xsl:call-template></fo:block>
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage.verso">
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage.separator">
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage.before.recto">
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage.before.verso">
</xsl:template>

<xsl:template name="component.list.of.unknowns.titlepage">
  <fo:block>
    <xsl:variable name="recto.content">
      <xsl:call-template name="component.list.of.unknowns.titlepage.before.recto"/>
      <xsl:call-template name="component.list.of.unknowns.titlepage.recto"/>
    </xsl:variable>
    <xsl:variable name="recto.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($recto.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($recto.content) != '') or ($recto.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$recto.content"/></fo:block>
    </xsl:if>
    <xsl:variable name="verso.content">
      <xsl:call-template name="component.list.of.unknowns.titlepage.before.verso"/>
      <xsl:call-template name="component.list.of.unknowns.titlepage.verso"/>
    </xsl:variable>
    <xsl:variable name="verso.elements.count">
      <xsl:choose>
        <xsl:when test="function-available('exsl:node-set')"><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor'), 'Apache Software Foundation')">
          <!--Xalan quirk--><xsl:value-of select="count(exsl:node-set($verso.content)/*)"/></xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="(normalize-space($verso.content) != '') or ($verso.elements.count &gt; 0)">
      <fo:block><xsl:copy-of select="$verso.content"/></fo:block>
    </xsl:if>
    <xsl:call-template name="component.list.of.unknowns.titlepage.separator"/>
  </fo:block>
</xsl:template>

<xsl:template match="*" mode="component.list.of.unknowns.titlepage.recto.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

<xsl:template match="*" mode="component.list.of.unknowns.titlepage.verso.mode">
  <!-- if an element isn't found in this mode, -->
  <!-- try the generic titlepage.mode -->
  <xsl:apply-templates select="." mode="titlepage.mode"/>
</xsl:template>

</xsl:stylesheet>
