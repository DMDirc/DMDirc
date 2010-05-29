<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header: /cvsroot/pmd/pmd/etc/xslt/pmd-report-per-class.xslt,v 1.1 2005/06/28 13:51:49 tomcopeland Exp $ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html" encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" 
	doctype-system="http://www.w3.org/TR/html4/loose.dtd" indent="yes"/>

<xsl:template name="message">
<xsl:value-of disable-output-escaping="yes" select="."/>
</xsl:template>

<xsl:template name="priorityDiv">
<xsl:if test="@priority = 1">p1</xsl:if>
<xsl:if test="@priority = 2">p2</xsl:if>
<xsl:if test="@priority = 3">p3</xsl:if>
<xsl:if test="@priority = 4">p4</xsl:if>
<xsl:if test="@priority = 5">p5</xsl:if>
</xsl:template>

<xsl:template match="pmd">
<html>
<head>
    <title>PMD Report</title>
	<script type="text/javascript" src="sorttable.js"></script>
    <style type="text/css">
        body { margin-left: 2%; margin-right: 2%; font:normal verdana,arial,helvetica; color:#000000; }
        table { border: 0; border-collapse: collapse; width: 100%; }
        table th { text-align: center; }
        table.sortable tr th { font-weight: normal; text-align:left; background:#a6caf0; }
        table.sortable tr td { background:#eeeee0; }
        table.classcount tr th { font-weight: normal; text-align:left; background:#a6caf0; }
        table.classcount tr td { background:#eeeee0; }
        table.summary tr th { font-weight: normal; text-align:center; background:#a6caf0; }
        table.summary tr td { background:#eeeee0;}
        .p1 { background:#FF9999; }
        .p2 { background:#FFCC66; }
        .p3 { background:#FFFF99; }
        .p4 { background:#99FF99; }
        .p5 { background:#9999FF; }
    </style>
</head>
<body>
    <table class="summary">
      <tr><th colspan="6">Summary</th></tr>
      <tr>
        <th>Total</th>
        <th>Priority 1</th>
        <th>Priority 2</th>
        <th>Priority 3</th>
        <th>Priority 4</th>
        <th>Priority 5</th>
      </tr>
      <tr>
        <td><xsl:value-of select="count(//violation)"/></td>
        <td><div class="p1"><xsl:value-of select="count(//violation[@priority = 1])"/></div></td>
        <td><div class="p2"><xsl:value-of select="count(//violation[@priority = 2])"/></div></td>
        <td><div class="p3"><xsl:value-of select="count(//violation[@priority = 3])"/></div></td>
        <td><div class="p4"><xsl:value-of select="count(//violation[@priority = 4])"/></div></td>
        <td><div class="p5"><xsl:value-of select="count(//violation[@priority = 5])"/></div></td>
      </tr>
    </table>
    <br />
    <table class="sortable">
      <xsl:attribute name="id">sortable_id_0</xsl:attribute>
      <tr>
        <th>File</th>
        <th>Total</th>
        <th>Priority 1</th>
        <th>Priority 2</th>
        <th>Priority 3</th>
        <th>Priority 4</th>
        <th>Priority 5</th>
      </tr>
    <xsl:for-each select="file">
      <xsl:sort data-type="number" order="descending" select="count(violation)"/>
      <xsl:variable name="filename" select="@name"/>
      <tr>
        <td><a><xsl:attribute name="href">#<xsl:value-of disable-output-escaping="yes" select="substring-before(translate(@name,'/','.'),'.java')"/>
        </xsl:attribute>
            <xsl:value-of disable-output-escaping="yes" select="substring-before(translate(@name,'/','.'),'.java')"/></a>
        </td>
        <td><xsl:value-of select="count(violation)"/></td>
        <td><div class="p1"><xsl:value-of select="count(violation[@priority = 1])"/></div></td>
        <td><div class="p2"><xsl:value-of select="count(violation[@priority = 2])"/></div></td>
        <td><div class="p3"><xsl:value-of select="count(violation[@priority = 3])"/></div></td>
        <td><div class="p4"><xsl:value-of select="count(violation[@priority = 4])"/></div></td>
        <td><div class="p5"><xsl:value-of select="count(violation[@priority = 5])"/></div></td>
      </tr>
    </xsl:for-each>
    </table>
    <hr/>
    <xsl:for-each select="file">
        <xsl:sort data-type="number" order="descending" select="count(violation)"/>
        <xsl:variable name="filename" select="@name"/>
        <H3><xsl:attribute name="id"><xsl:value-of disable-output-escaping="yes" select="substring-before(translate(@name,'/','.'),'.java')"/>
        </xsl:attribute>
            <xsl:value-of disable-output-escaping="yes" select="substring-before(translate(@name,'/','.'),'.java')"/></H3>
        <table class="sortable"><xsl:attribute name="id">sortable_id_<xsl:value-of select="position()"/></xsl:attribute>
            <tr>
				<th>Prio</th>
                <th>Line</th>
				<th>Method</th>
                <th align="left">Description</th>
            </tr>
	    
	    <xsl:for-each select="violation">
		    <tr>
			<td style="padding: 3px" align="right"><div><xsl:attribute name="class"><xsl:call-template name="priorityDiv"/></xsl:attribute><xsl:value-of disable-output-escaping="yes" select="@priority"/></div></td>
			<td style="padding: 3px" align="right"><xsl:value-of disable-output-escaping="yes" select="@line"/></td>
			<td style="padding: 3px" align="left"><xsl:value-of disable-output-escaping="yes" select="@method"/></td>
			<td style="padding: 3px" align="left" width="100%"><xsl:if test="@externalInfoUrl"><a><xsl:attribute name="href"><xsl:value-of select="@externalInfoUrl"/></xsl:attribute><xsl:call-template name="message"/></a></xsl:if><xsl:if test="not(@externalInfoUrl)"><xsl:call-template name="message"/></xsl:if></td>
		    </tr>
	    </xsl:for-each>
		</table>
		
		<table border="0" width="100%" class="classcount">
			<tr>
				<th>Total number of violations for this class: <xsl:value-of select="count(violation)"/></th>
			</tr>
        </table>
        <br/>
    </xsl:for-each>
<p>Report generated at: <xsl:value-of select="@timestamp"/></p>
</body>
</html>
</xsl:template>

</xsl:stylesheet>
