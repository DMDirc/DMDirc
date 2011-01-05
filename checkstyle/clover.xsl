<xsl:stylesheet	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"/>
<xsl:decimal-format decimal-separator="." grouping-separator="," />
<xsl:template match="coverage">
	<xsl:variable name="total" select="project/metrics/@elements"/>
	<xsl:variable name="covered" select="project/metrics/@coveredelements"/>
	<xsl:variable name="decimal" select="$covered div $total"/>
	<build>
		<statisticValue key="cloverTotalElements">
			<xsl:attribute name="value">
				<xsl:value-of select="$total"/>
			</xsl:attribute>
		</statisticValue>
		<statisticValue key="cloverCoveredElements" value="">
                	<xsl:attribute name="value">
	                        <xsl:value-of select="$covered"/>
        	        </xsl:attribute>
	        </statisticValue>
		<statisticValue key="cloverPercentElements" value="">
	                <xsl:attribute name="value">
        	                <xsl:value-of select="$decimal"/>
                	</xsl:attribute>
	        </statisticValue>

		<statusInfo>
			<text action="append"> coverage: <xsl:value-of select="round($decimal * 10000) div 100"/>%</text>
		</statusInfo>
	</build>
</xsl:template>

</xsl:stylesheet>
