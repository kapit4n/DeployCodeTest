<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy? -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <head>
        <title>Code coverage</title>
        <link rel="stylesheet" type="text/css" href="Tabs.css"
             title="Style"/>
      </head>
      <body>
        <div id="menu">
          <ul>
            <li>
              <a href="resume.xml" title="Resume">Resume</a>
            </li>
            <li>
              <a href="unitTest.xml" title="Unit Test">Unit Test</a>
            </li>
            <li>
              <a href="codeCoverage.xml" title="Code Coverage"  class="active">Code Coverage</a>
            </li>
          </ul>
        </div>
        <div id="main">
          <br/>
          <table  width="100%">
            <tr>
              <h2>Code Coverage Class</h2>
            </tr>
            <tr>
              <table border="1"  width="100%">
                <tr bgcolor="#66CCFF">
                  <th>Number</th>
                  <th>Class</th>
                  <th>Code Coverage Class</th>
                </tr>
                <xsl:for-each select="results/codeCoverageClass/item">
                  <tr bgcolor="{color}">
                    <td>
                      <xsl:value-of select="number" />
                    </td>
                    <td>
                      <xsl:value-of select="class" />
                    </td>
                    <td>
                      <xsl:value-of select="percent" />
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </tr>
            <tr>
              <br/>
              <h3>Total Code coverage Classes: <xsl:value-of select="results/total/AvgCodeCoverageClass/item" /> </h3>
              <br/>
              <br/>
              <br/>
              <h2>Code Coverage Triggers</h2>
            </tr>
            <tr>
              <table border="1"  width="100%">
                <tr bgcolor="#66CCFF">
                  <th>Number</th>
                  <th>Trigger</th>
                  <th>Code Coverage Triggers</th>
                  <th>Code No coverage</th>
                </tr>
                <xsl:for-each select="results/codeCoverageTrigger/item">
                  <tr bgcolor="{color}">
                    <td>
                      <xsl:value-of select="number" />
                    </td>
                    <td>
                      <xsl:value-of select="trigger" />
                    </td>
                    <td>
                      <xsl:value-of select="percent" />
                    </td>
                    <td>
                      <xsl:for-each select="location">
                          <xsl:value-of select="."/>
                          <br/>                        
                      </xsl:for-each>
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
              <br/>
              <h3>
                Total Code coverage Triggers: <xsl:value-of select="results/total/AvgCodeCoverageTrigger/item" />
              </h3>
            </tr>       
          </table>
        </div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
