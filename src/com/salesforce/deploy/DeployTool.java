package com.salesforce.deploy;

import java.io.*;

import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sforce.soap.metadata.AsyncRequestState;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CodeCoverageResult;
import com.sforce.soap.metadata.CodeLocation;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.RunTestSuccess;
import com.sforce.soap.metadata.RunTestsResult;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * This class is to create report 
 * @author Luis Arce
 *
 */
public class DeployTool {

	private MetadataConnection metadataconnection;

	private static String ZIP_FILE;

	// one second in milliseconds

	private static final long ONE_SECOND = 1000;
	// maximum number of attempts to deploy the zip file

	private static final int MAX_NUM_POLL_REQUESTS = 5000;

	private static String temporalFile;

	private static String xmlFileResume;
	private static String xmlFileCodeCoverage;
	private static String xmlFileUnitTest;
	private static String dirReports;

	/**
	 * 
	 * @param args
	 * @throws ServiceException
	 * @throws Exception
	 */
	public static void main(String[] args2) throws ServiceException, Exception {
		String[] args  = new String[1];
		
		MainGenerator generateXslFiles = new MainGenerator();
		generateXslFiles.run();
		
		DeployTool sample = new DeployTool();
		
		if(args.length >= 6) {
			dirReports = args[3];
			xmlFileResume = "resume.xml";
			xmlFileUnitTest = "unitTest.xml";
			xmlFileCodeCoverage = "codeCoverage.xml";
			temporalFile = args[4];
			ZIP_FILE = args[5];
			sample.run(args[0], args[1], args[2]);
		} else {
			// this you can add source code for default for running without parameters.
			dirReports = "logs";
			xmlFileResume = "resume.xml";
			xmlFileUnitTest = "unitTest.xml";
			xmlFileCodeCoverage = "codeCoverage.xml";
			temporalFile = "logs\\fail.temp";
			ZIP_FILE = "deploy.zip";
			//sample.run("luis.arce@jalasoft.com", "admin123UG35mxA4cnY7th4kOZUIWQxgW", "https://test.salesforce.com/services/Soap/c/26.0");
			sample.run("larce02@8demo.com", "admin987.03TZWjmHKYRK98igcjdfyT8TTL", "https://login.salesforce.com/services/Soap/c/26.0");
			System.out.println("The parameters are not complete");
			
		}
	}

	/**
	 * 
	 * @param login
	 * @param password
	 * @param url
	 * @throws ServiceException
	 * @throws Exception
	 */
	private void run(String login, String password, String url) throws ServiceException, Exception {
		if ((metadataconnection = login(login, password, url)) != null) {
			deployZip();
		}
	}

	/**
	 * This method is to deploy a zip file.
	 * @throws RemoteException
	 * @throws Exception
	 */
	private void deployZip() throws RemoteException, Exception {
		byte zipBytes[] = readZipFile();
		DeployOptions deployOptions = new DeployOptions();
		deployOptions.setPerformRetrieve(false);
		deployOptions.setRollbackOnError(false);
		deployOptions.setRunAllTests(true);

		AsyncResult asyncResult = metadataconnection.deploy(zipBytes, deployOptions);

		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		while (!asyncResult.isDone()) {
			Thread.sleep(waitTimeMilliSecs);
		
			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new Exception(
						"Request timed out. If this is a large set "
							+ "of metadata components, check that the time allowed by "
							+ "MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			asyncResult = metadataconnection.checkStatus(new String[] { asyncResult.getId() })[0];
			System.out.println("Status is: " + asyncResult.getState());
		}

		if (asyncResult.getState() != AsyncRequestState.Completed) {
			throw new Exception(asyncResult.getStatusCode() + " msg: "
					+ asyncResult.getMessage());
		}

		System.out.println("Asyn Status Completed: " + AsyncRequestState.Completed);
		System.out.println("Asyn Status Error: " + AsyncRequestState.Error);
		System.out.println("Asyn Status InProcess: " + AsyncRequestState.InProgress);

		DeployResult result = metadataconnection.checkDeployStatus(asyncResult.getId());

		if (!result.isSuccess()) {
			RunTestsResult testResults = result.getRunTestResult();
			toXML(testResults);			
		} else {
			RunTestsResult testResults = result.getRunTestResult();
			toXML(testResults);		
		}

		System.out.println("The file " + ZIP_FILE + " was successfully deployed");
	}

	/**
	 * This method is to read a zip file for later deploying.
	 * @return a array of bytes of the zip file read.
	 * @throws Exception
	 */
	private byte[] readZipFile() throws Exception {

		File deployZip = new File(ZIP_FILE);
		if (!deployZip.exists() || !deployZip.isFile())
			throw new Exception("Cannot find the zip file to deploy. Looking for " + deployZip.getAbsolutePath());

		FileInputStream fos = new FileInputStream(deployZip);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int readbyte = -1;
		while ((readbyte = fos.read()) != -1) {
			bos.write(readbyte);
		}
		fos.close();
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 
	 * @param login
	 * @param password
	 * @param url
	 * @return
	 * @throws ConnectionException
	 */
	public MetadataConnection login(String login, String password, String url) throws ConnectionException {
		String USERNAME = login;
		String PASSWORD = password;
		String URL = url;
		LoginResult loginResult = loginToSalesforce(USERNAME, PASSWORD, URL);
		MetadataConnection res = createMetadataConnection(loginResult);
		return res;
	}

	/**
	 * 
	 * @param loginResult
	 * @return
	 * @throws ConnectionException
	 */
	private MetadataConnection createMetadataConnection(LoginResult loginResult) throws ConnectionException {
		ConnectorConfig config = new ConnectorConfig();
		config.setServiceEndpoint(loginResult.getMetadataServerUrl());
		config.setSessionId(loginResult.getSessionId());
		MetadataConnection res = new MetadataConnection(config);
		return res;
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param loginUrl
	 * @return
	 * @throws ConnectionException
	 */
	private LoginResult loginToSalesforce(final String username, String password, final String loginUrl)
			throws ConnectionException {
		
		ConnectorConfig config = new ConnectorConfig();
		config.setAuthEndpoint(loginUrl);
		config.setServiceEndpoint(loginUrl);
		config.setManualLogin(true);
		LoginResult res = (new EnterpriseConnection(config)).login(username, password);
		return res;
	}

	/**
	 * This method is used to generate the xml document with the result of the deploy.
	 * @param results
	 * @throws IOException
	 */
	public static void toXML(RunTestsResult results) throws IOException {

		NumberFormat df = DecimalFormat.getInstance();
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);

		boolean failures = false;
		int counterClassCovegare = 0;
		int counterTriggerCovegare = 0;

		double counterTotalCode = 0;
		double counterTotalExceptionCode = 0; 
		
		double counterTotalCodeClass = 0;
		double counterTotalCodeClassExceptions = 0;

		double counterTotalCodeTrigger = 0;
		double counterTotalCodeTriggerExceptions = 0;

		double avgCovegare = 100;
		boolean ceroPercetTrigger = false;

		try {

			Document xmldocResume = null;
			Document xmldocCodeCoverage = null;
			Document xmldocUnitTest = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();

			xmldocResume = impl.createDocument(null, "results", null);
			xmldocCodeCoverage = impl.createDocument(null, "results", null);
			xmldocUnitTest = impl.createDocument(null, "results", null);
			String hrefResume = "type=\"text/xsl\" href=\"Resume.xsl\"";
			String hrefCodeCoverage = "type=\"text/xsl\" href=\"CodeCoverage.xsl\"";
			String hrefUnitTest = "type=\"text/xsl\" href=\"UnitTest.xsl\"";

			Node piResume = xmldocResume.createProcessingInstruction("xml-stylesheet",hrefResume);
			Node piCodeCoverage = xmldocCodeCoverage.createProcessingInstruction("xml-stylesheet",hrefCodeCoverage);
			Node piUnitTest = xmldocUnitTest.createProcessingInstruction("xml-stylesheet",hrefUnitTest);

			Element rootResume = xmldocResume.getDocumentElement();
			Element rootCodeCoverage = xmldocCodeCoverage.getDocumentElement();
			Element rootUnitTest = xmldocUnitTest.getDocumentElement();

			xmldocResume.insertBefore(piResume, rootResume);
			xmldocCodeCoverage.insertBefore(piCodeCoverage, rootCodeCoverage);
			xmldocUnitTest.insertBefore(piUnitTest, rootUnitTest);

			Element codeCoverageClass = xmldocCodeCoverage.createElementNS(null, "codeCoverageClass");
			Element codeCoverageTrigger = xmldocCodeCoverage.createElementNS(null, "codeCoverageTrigger");

			CodeCoverageResult[] codeCoverageArray = results.getCodeCoverage();

			CodeCoverageComparable[] codeCoverageSorted = new CodeCoverageComparable[codeCoverageArray.length];
			for (int i = 0; i < codeCoverageArray.length; i++) {
				codeCoverageSorted[i] = new CodeCoverageComparable(codeCoverageArray[i]);
			}

			Arrays.sort(codeCoverageSorted);

			for (int i = 0; i < codeCoverageSorted.length; i++) {
				Element item = xmldocCodeCoverage.createElementNS(null, "item");
				counterTotalCode += codeCoverageSorted[i].getData().getNumLocations();
				counterTotalExceptionCode += codeCoverageSorted[i].getData().getLocationsNotCovered().length;

				addChildToElement(xmldocCodeCoverage, item, "percent", df.format(codeCoverageSorted[i].getPercent()) + "%");

				if(codeCoverageSorted[i].getData().getType().equalsIgnoreCase("trigger")) {
					addChildToElement(xmldocCodeCoverage, item, "trigger", codeCoverageSorted[i].getData().getName());

					counterTotalCodeTrigger += codeCoverageSorted[i].getData().getNumLocations();
					counterTotalCodeTriggerExceptions += codeCoverageSorted[i].getData().getNumLocationsNotCovered();

					if(codeCoverageSorted[i].getPercent() == 0) {
						ceroPercetTrigger = true;
					}

					CodeLocation[] codeLocatioAux = codeCoverageSorted[i].getData().getLocationsNotCovered();
					for (int j = 0; j < codeLocatioAux.length; j++) {
						Element itemCodeLocations = xmldocCodeCoverage.createElementNS(null, "location");						
						addChildToElement(xmldocCodeCoverage, itemCodeLocations, "Column: " + codeLocatioAux[j].getColumn() + ", Line: " + codeLocatioAux[j].getLine());
						item.appendChild(itemCodeLocations);
					}
					
					counterTriggerCovegare++;
					
					addChildToElement(xmldocCodeCoverage, item, "number", counterTriggerCovegare + "");
					codeCoverageTrigger.appendChild(item);
				} else {
					addChildToElement(xmldocCodeCoverage, item, "class", codeCoverageSorted[i].getData().getName());
					
					counterTotalCodeClass += codeCoverageSorted[i].getData().getNumLocations();
					counterTotalCodeClassExceptions += codeCoverageSorted[i].getData().getNumLocationsNotCovered();
					
					counterClassCovegare++;
					addChildToElement(xmldocCodeCoverage, item, "number", counterClassCovegare + "");
					codeCoverageClass.appendChild(item);
				}

				if(codeCoverageSorted[i].getPercent() < 100.0) {	
					if(codeCoverageSorted[i].getPercent() == 0.0)
					{
						addChildToElement(xmldocCodeCoverage, item, "color", "#FF5C5C");
					} else {
						addChildToElement(xmldocCodeCoverage, item, "color", "#FFFF99");
					}
				} else {
					addChildToElement(xmldocCodeCoverage, item, "color", "#99FF99");
				}
			}

			rootCodeCoverage.appendChild(codeCoverageClass);
			rootCodeCoverage.appendChild(codeCoverageTrigger);

			Element failuresItems = xmldocUnitTest.createElementNS(null, "failures");
			RunTestFailure[] failuresArray = results.getFailures();

			if(failuresArray.length > 0) {
				failures = true;
			}

			for (int i = 0; i < failuresArray.length; i++) {
				Element item = xmldocUnitTest.createElementNS(null, "item");
				addChildToElement(xmldocUnitTest, item, "number", "" + (i + 1));
				addChildToElement(xmldocUnitTest, item, "id", failuresArray[i].getId());
				addChildToElement(xmldocUnitTest, item, "name", failuresArray[i].getName());
				addChildToElement(xmldocUnitTest, item, "method",failuresArray[i].getMethodName());
				addChildToElement(xmldocUnitTest, item, "stackTrace",failuresArray[i].getStackTrace());
				failuresItems.appendChild(item);
			}
			
			Element successItems = xmldocUnitTest.createElementNS(null, "success");
			RunTestSuccess[] successArray = results.getSuccesses();

			for (int i = 0; i < successArray.length; i++) {
				Element item = xmldocUnitTest.createElementNS(null, "item");
				addChildToElement(xmldocUnitTest, item, "number", "" + (i + 1));
				addChildToElement(xmldocUnitTest, item, "id", successArray[i].getId());
				addChildToElement(xmldocUnitTest, item, "name", successArray[i].getName());
				addChildToElement(xmldocUnitTest, item, "method", successArray[i].getMethodName());
				successItems.appendChild(item);
			}
			
			rootUnitTest.appendChild(failuresItems);
			rootUnitTest.appendChild(successItems);

			Element resume = xmldocResume.createElementNS(null, "total");
			Element resume2 = xmldocCodeCoverage.createElementNS(null, "total");
 			
			if(counterTotalCode > 0 ){
				avgCovegare = ((1.0 - counterTotalExceptionCode / counterTotalCode) *100 );
			}

			addChildToElement(xmldocResume, resume, "item", successArray.length + "", "Success");
			addChildToElement(xmldocResume, resume, "item", failuresArray.length + "", "Failure");

			addChildToElement(xmldocResume, resume, "item", (int)counterClassCovegare + "", "CodeCoverageClass");
			addChildToElement(xmldocResume, resume, "item", (int)counterTriggerCovegare + "", "CodeCoverageTrigger");

			addChildToElement(xmldocResume, resume, "item", (int)counterTotalCodeClass + "", "CounterTotalCodeClass");
			addChildToElement(xmldocResume, resume, "item", (int)counterTotalCodeTrigger + "", "counterTotalCodeTrigger");

			addChildToElement(xmldocResume, resume, "item", (int)counterTotalCodeClassExceptions + "", "CounterTotalCodeClassExceptions");
			addChildToElement(xmldocResume, resume, "item", (int)counterTotalCodeTriggerExceptions + "", "counterTotalCodeTriggerExceptions");

			double avgCodeCoverageClass = 100;
			double avgCodeCoverageTrigger = 100;

			if(avgCodeCoverageClass > 0.0)
			{
				avgCodeCoverageClass = ((counterTotalCodeClass - counterTotalCodeClassExceptions) / counterTotalCodeClass) * 100;
			}

			if(counterTotalCodeTrigger > 0.0)
			{
				avgCodeCoverageTrigger = ((counterTotalCodeTrigger - counterTotalCodeTriggerExceptions)/counterTotalCodeTrigger ) * 100;
			}

			addChildToElement(xmldocResume, resume, "item", df.format(avgCodeCoverageClass) + "%", "AvgCodeCoverageClass");
			addChildToElement(xmldocResume, resume, "item", df.format(avgCodeCoverageTrigger) + "%", "AvgCodeCoverageTrigger");

			addChildToElement(xmldocCodeCoverage,resume2 , "item", df.format(avgCodeCoverageClass) + "%", "AvgCodeCoverageClass");
			addChildToElement(xmldocCodeCoverage, resume2, "item", df.format(avgCodeCoverageTrigger) + "%", "AvgCodeCoverageTrigger");
			
			addChildToElement(xmldocResume, resume, "item", (int)counterTotalCode + "", "TotalCode");
			addChildToElement(xmldocResume, resume, "item", (int)(counterTotalCode - counterTotalExceptionCode) + "", "TotalCodeCoverage");
			addChildToElement(xmldocResume, resume, "item", (int)counterTotalExceptionCode + "", "TotalExceptionCode");
			addChildToElement(xmldocResume, resume, "item",  df.format(avgCovegare) + "%", "AvgCodeCoverage");

			rootResume.appendChild(resume);
			rootCodeCoverage.appendChild(resume2);
			createXmlFile(xmldocResume, xmlFileResume);
			createXmlFile(xmldocCodeCoverage, xmlFileCodeCoverage);
			createXmlFile(xmldocUnitTest, xmlFileUnitTest);
		} catch (Exception e) {
			System.out.println(e);
		}

		File file = new File(temporalFile);
		
		if (failures || avgCovegare < 75.0 || ceroPercetTrigger) {
			if (!file.exists()) {
				file.createNewFile();
			}
		} else {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * This method is used to add a child with a tagName in a element.
	 * @param xmldoc
	 * @param parent
	 * @param name
	 * @param value
	 * @param tagName
	 */
	public static void addChildToElement(Document xmldoc, Element parent, String name, String value, String tagName) {
		Element child = xmldoc.createElementNS(null, tagName);
		Element auxElement = xmldoc.createElement(name);
		Node auxText = xmldoc.createTextNode(value);
		auxElement.appendChild(auxText);
		child.appendChild(auxElement);
		parent.appendChild(child);
	}

	/**
	 * This method is used to add a child in a element.
	 * @param xmldoc
	 * @param parent
	 * @param name
	 * @param value
	 */
	public static void addChildToElement(Document xmldoc, Element parent, String name, String value) {
		Element child = xmldoc.createElement(name);
		Node aux = xmldoc.createTextNode(value);
		child.appendChild(aux);
		parent.appendChild(child);
	}

	/**
	 * This method is used to add a child in a element.
	 * @param xmldoc
	 * @param parent
	 * @param name
	 * @param value
	 */
	public static void addChildToElement(Document xmldoc, Element parent, String value) {
		Node aux = xmldoc.createTextNode(value);
		parent.appendChild(aux);
	}

	/**
	 * This method is used to create a file xml. 
	 * @param xmldoc, This is the content for the xml document.
	 * @param outputFilePath, this is the path to create the file.
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 */
	public static void createXmlFile(Document xmldoc, String outputFilePath) throws FileNotFoundException, TransformerException
	{	
		try {
			MkdirMethod(dirReports);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File(dirReports + File.separator +  outputFilePath);
		PrintWriter out = new PrintWriter(file);
		
		DOMSource domSource = new DOMSource(xmldoc);			
		StreamResult streamResult = new StreamResult(out);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer serializer = tf.newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.transform(domSource, streamResult);
	}
	
	public static void MkdirMethod(String pathDir) throws IOException
	{
	 	String md5Replace = pathDir.replace(File.separator, "21232f297a57a5a743894a0e4a801fc3");
	 	String[] mkdirs = md5Replace.split("21232f297a57a5a743894a0e4a801fc3");

	 	String newPath = mkdirs[0];
	 	System.out.println(newPath);
	 	if(!newPath.contains(":")) {
	 		File fx = new File(newPath);
	 		fx.mkdir();
	 	}

	 	for (int i = 1; i < mkdirs.length; i++) {
	 		newPath += File.separator + mkdirs[i];
	 		File fx = new File(newPath);
	 		fx.mkdir();
		}
	}
	
}
