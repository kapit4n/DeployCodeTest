<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy? -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <head>
        <title>Resume</title>
        <link rel="stylesheet" type="text/css" href="Tabs.css"
             title="Style"/>
      </head>
      <body>
        <div id="menu">
          <ul>
            <li>
              <a href="resume.xml" title="Resume" class="active">Resume</a>
            </li>
            <li>
              <a href="unitTest.xml" title="Unit Test">Unit Test</a>
            </li>
            <li>
              <a href="codeCoverage.xml" title="Code Coverage">Code Coverage</a>
            </li>
          </ul>
        </div>
        <div id="main">
          <br/>
          <h2>Totals</h2>
          <table width="100%">
            <tr bgcolor="#FFCC66">
              <th>Totals</th>
            </tr>
            <tr bgcolor="#99CCFF">
              <td>
                Total of lines of code
              </td>
              <td  bgcolor="#99CCFF">
                <xsl:value-of select="results/total/TotalCode/item" />
              </td>
            </tr>
            <tr bgcolor="#99CCFF">
              <td>
                Total of lines of code Coverage
              </td>
              <td  bgcolor="#99CCFF">
                <xsl:value-of select="results/total/TotalCodeCoverage/item" />
              </td>
            </tr>
            <tr  bgcolor="#99CCFF">
              <td>
                Total of lines of code not coverage
              </td>
              <td>
                <xsl:value-of select="results/total/TotalExceptionCode/item" />
              </td>
            </tr>
            <tr bgcolor="#99CCFF">
              <td>
                Percent Code Coverage
              </td>
              <td>
                <xsl:value-of select="results/total/AvgCodeCoverage/item" />
              </td>
            </tr>
            <tr  bgcolor="#99CCFF">
              <td>
                Failed Unit Test
              </td>
              <td>
                <xsl:value-of select="results/total/Failure/item" />
              </td>
            </tr>
            <tr  bgcolor="#99CCFF">
              <td>
                Success Unit Test
              </td>
              <td>
                <xsl:value-of select="results/total/Success/item" />
              </td>
            </tr>
          </table>
        </div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
