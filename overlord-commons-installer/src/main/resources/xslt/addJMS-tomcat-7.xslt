<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="xalan"
  version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <!-- Copy everything. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

  <!-- Add ActiveMQ to the root context.xml -->
  <xsl:template match="Context">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <Resource
        name="ConnectionFactory"
        auth="Container"
        type="org.apache.activemq.ActiveMQConnectionFactory"
        description="JMS Connection Factory"
        factory="org.apache.activemq.jndi.JNDIReferenceFactory"
        brokerURL="vm://localhost"
        brokerName="ActiveMQBroker"
        useEmbeddedBroker="true" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>