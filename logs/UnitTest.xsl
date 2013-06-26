<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy? -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <head>
        <title>Unit Test</title>
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
              <a href="unitTest.xml" title="Unit Test" class="active">Unit Test</a>
            </li>
            <li>
              <a href="codeCoverage.xml" title="Code Coverage">Code Coverage</a>
            </li>
          </ul>
        </div>
        <div id="main">
          <br/>
          <table>
          <tr>
            <h2>Failed unit test</h2>
          </tr>
          <tr>
            <table border="1"  width="100%">
              <tr bgcolor="#3399FF">
                <th>Nro</th>
                <th>ID</th>
                <th>Class</th>
                <th>Method</th>
                <th>stackTrace</th>
              </tr>
              <xsl:for-each select="results/failures/item">
                <tr  bgcolor="#FF7575">
                  <td>
                    <xsl:value-of select="number" />
                  </td>
                  <td>
                    <xsl:value-of select="id" />
                  </td>
                  <td>
                    <xsl:value-of select="name" />
                  </td>
                  <td>
                    <xsl:value-of select="method" />
                  </td>
                  <td>
                    <xsl:value-of select="stackTrace" />
                  </td>
                </tr>
              </xsl:for-each>
            </table>
          </tr>
          <tr>
            <br/>
          </tr>
          <tr>
            <h2>Success unit test</h2>
          </tr>
          <tr>
            <table border="1"  width="100%">
              <tr bgcolor="#3399FF">
                <th>Nro</th>
                <th>ID</th>
                <th>Class</th>
                <th>Method</th>
              </tr>
              <xsl:for-each select="results/success/item">
                <tr  bgcolor="#99FF99">
                  <td>
                    <xsl:value-of select="number" />
                  </td>
                  <td>
                    <xsl:value-of select="id" />
                  </td>
                  <td>
                    <xsl:value-of select="name" />
                  </td>
                  <td>
                    <xsl:value-of select="method" />
                  </td>
                </tr>
              </xsl:for-each>
            </table>
          </tr>
          </table>
        </div>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
