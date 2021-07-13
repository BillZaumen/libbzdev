<?xml version="1.0"  encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
  <html>
    <body>
	<xsl:for-each select="toc/node">
	  <h2> <xsl:value-of select="@title"/></h2>
	  <xsl:for-each select="node">
	    <ul>
	      <li><xsl:value-of select="@title"/>
	      <xsl:for-each select="node">
		<ul>
		  <li><xsl:value-of select="@title"/></li>
		</ul>
	      </xsl:for-each>   
	      </li>
	    </ul>
	  </xsl:for-each>
	</xsl:for-each>
    </body>
  </html>
</xsl:template>
</xsl:stylesheet>
