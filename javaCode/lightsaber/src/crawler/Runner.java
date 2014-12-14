
package crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import crawler.document.Parser;

public class Runner {
	
	// REFERENCE: http://www.mkyong.com/java/how-to-create-xml-file-in-java-dom/
	
	public static final Logger logger = Logger.getLogger(Runner.class.getName());
	public static Properties dirProp = new Properties();
	public static InputStream dirPropInput = null;
	public static Properties configProp = new Properties();
	public static InputStream configPropInput = null;
	

	public static int xmlBlogId = 1;
	public static int xmlPediaId = 1;
	public static int xmlTechId = 1;
	
	public static String projDir = "";
	public static String blogDir = "";
	public static String xmlBlogDir = "";
	public static String pediaDir = "";
	public static String xmlPediaDir = "";
	public static String techDir = "";
	public static String xmlTechDir = "";
	
	public static int docCount = 0;
	
	public static int blogDocLimit = 0;
	public static int pediaDocLimit = 0;
	public static int techDocLimit = 0;
	
	public static int blogDocId = 0;
	public static int pediaDocId = 0;
	public static int techDocId = 0;
	
	public static String mapPath = "";
	public static HashMap<Integer, ArrayList<Integer>> relatedTechMap = null;
	public static HashMap<Integer, ArrayList<Integer>> relatedBlogMap = null;
	
	public static void main(String[] args) {
		
		logger.info("Running.....");
		loadProperties();
		
		try {
			File techFile = new File(mapPath+"mergedRelatedTechMap");
			File blogFile = new File(mapPath+"mergedRelatedTorMap");
			FileInputStream fit = new FileInputStream(techFile);
			ObjectInputStream oit = new ObjectInputStream(fit);
			FileInputStream fib = new FileInputStream(blogFile);
			ObjectInputStream oib = new ObjectInputStream(fib);
			relatedTechMap = (HashMap<Integer, ArrayList<Integer>>) oit.readObject();
			relatedBlogMap = (HashMap<Integer, ArrayList<Integer>>) oib.readObject();
			//System.out.println(relatedDocsMap.size());
			//System.out.println(relatedDocsMap.toString());
			oit.close();
			fit.close();
			oib.close();
			fib.close();
		} catch (FileNotFoundException f) {
			logger.log(Level.SEVERE, "FileNotFoundException caught in reading relatedDocs HashMap file: ", f);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException caught in reading relatedDocs HashMap file: ", e);
			//e.getMessage();
		} catch (ClassNotFoundException c) {
			logger.log(Level.SEVERE, "ClassNotFoundException caught in reading relatedDocs HashMap file: ", c);
			//System.out.println("Class not found");
			//c.getMessage();
			//System.out.println("CLASS NOT FOUND");
		}
		
		File blogDirectory = new File(blogDir);
		String[] blogFiles = blogDirectory.list();
		File pediaDirectory = new File(pediaDir);
		String[] pediaFiles = pediaDirectory.list();
		File techDirectory = new File(techDir);
		String[] techFiles = techDirectory.list();
		
		HashMap<String, Object> blogmap = getNewXmlFile();
		Document xmlBlogDoc = (Document)blogmap.get("xmlDoc");
		Element blogAdd = (Element)blogmap.get("add");
		
		HashMap<String, Object> pediamap = getNewXmlFile();
		Document xmlPediaDoc = (Document)pediamap.get("xmlDoc");
		Element pediaAdd = (Element)pediamap.get("add");
		
		HashMap<String, Object> techmap = getNewXmlFile();
		Document xmlTechDoc = (Document)techmap.get("xmlDoc");
		Element techAdd = (Element)techmap.get("add");
		
		logger.info("Converting blog files to XML.....");
		for (String bf : blogFiles) {
			String blogpath = blogDirectory.getAbsolutePath() + File.separator + bf;
			HashMap<String, Object> newMap = new HashMap<String, Object>();
			
			if (docCount >= blogDocLimit) {
				saveXmlFile(xmlBlogDoc, xmlBlogDir);
				blogmap.clear();
				blogmap = getNewXmlFile();
				xmlBlogDoc = (Document)blogmap.get("xmlDoc");
				blogAdd = (Element)blogmap.get("add");
			}
			
			newMap = Parser.blogToXml(blogpath, xmlBlogDoc, blogAdd, blogDocId);
			xmlBlogDoc = (Document)newMap.get("xmlDoc");
			blogAdd = (Element)newMap.get("add");
			
			blogDocId++;
			docCount++;
		}
		saveXmlFile(xmlBlogDoc, xmlBlogDir);
		
		logger.info("Converting encyclopedia files to XML.....");
		for (String pf : pediaFiles) {
			String pediapath = pediaDirectory.getAbsolutePath() + File.separator + pf;
			HashMap<String, Object> newMap = new HashMap<String, Object>();
			
			if (docCount >= pediaDocLimit) {
				saveXmlFile(xmlPediaDoc, xmlPediaDir);
				pediamap.clear();
				pediamap = getNewXmlFile();
				xmlPediaDoc = (Document)pediamap.get("xmlDoc");
				pediaAdd = (Element)pediamap.get("add");
			}
			
			newMap = Parser.pediaToXml(pediapath, xmlPediaDoc, pediaAdd, pediaDocId, relatedTechMap, relatedBlogMap);
			xmlPediaDoc = (Document)newMap.get("xmlDoc");
			pediaAdd = (Element)newMap.get("add");
			
			pediaDocId++;
			docCount++;
		}
		saveXmlFile(xmlPediaDoc, xmlPediaDir);
		
		logger.info("Converting technology files to XML.....");
		for (String tf : techFiles) {
			String techpath = techDirectory.getAbsolutePath() + File.separator + tf;
			HashMap<String, Object> newMap = new HashMap<String, Object>();
			
			if (docCount >= techDocLimit) {
				saveXmlFile(xmlTechDoc, xmlTechDir);
				techmap.clear();
				techmap = getNewXmlFile();
				xmlTechDoc = (Document)techmap.get("xmlDoc");
				techAdd = (Element)techmap.get("add");
			}
			
			newMap = Parser.techToXml(techpath, xmlTechDoc, techAdd, techDocId);
			xmlTechDoc = (Document)newMap.get("xmlDoc");
			techAdd = (Element)newMap.get("add");
			
			techDocId++;
			docCount++;
		}
		saveXmlFile(xmlTechDoc, xmlTechDir);
		
		logger.info("Done!");
	}
	
	public static void saveXmlFile(Document xmlDoc, String xmlDir) {
		String xmlFileName = "";
		int xmlId = 0;
		
		if (xmlDir == xmlBlogDir) {
			xmlId = xmlBlogId;
		} else if (xmlDir == xmlPediaDir) {
			xmlId = xmlPediaId;
		} else if (xmlDir == xmlTechDir) {
			xmlId = xmlTechId;
		}
		
		try {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(xmlDoc);
			xmlFileName = xmlDir + xmlId + ".xml";
			StreamResult result = new StreamResult(new File(xmlFileName));
			//StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);
			
			docCount = 0;
			if (xmlDir == xmlBlogDir) {
				xmlBlogId++;
			} else if (xmlDir == xmlPediaDir) {
				xmlPediaId++;
			} else if (xmlDir == xmlTechDir) {
				xmlTechId++;
			}
			
		} catch (TransformerException te) {
			logger.log(Level.SEVERE, "TransformerException caught in writing XML file - " + xmlFileName, te);
		}
		
	}
	
	public static HashMap<String, Object> getNewXmlFile() {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document xmlDoc = null;
		
		try {
			docBuilder = docFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
		} catch (ParserConfigurationException pce) {
			logger.log(Level.SEVERE, "ParserConfigurationException caught in creating XML file - ", pce);
		}
		
		// <add></add>
		Element add = xmlDoc.createElement("add");
		xmlDoc.appendChild(add);
		
		docCount = 0;
		map.put("xmlDoc", xmlDoc);
		map.put("add", add);
		
		return map;
	}
	
	public static void loadProperties() {
		try {
			dirPropInput = new FileInputStream("dir.properties");
			dirProp.load(dirPropInput);
			configPropInput = new FileInputStream("config.properties");
			configProp.load(configPropInput);
			projDir = dirProp.getProperty("projDir");
			
			blogDir = projDir + dirProp.getProperty("blogDir");
			xmlBlogDir = projDir + dirProp.getProperty("xmlBlogDir");
			pediaDir = projDir + dirProp.getProperty("pediaDir");
			xmlPediaDir = projDir + dirProp.getProperty("xmlPediaDir");
			techDir = projDir + dirProp.getProperty("techDir");
			xmlTechDir = projDir + dirProp.getProperty("xmlTechDir");
			
			mapPath = projDir + dirProp.getProperty("relatedDocsMapDir");
			
			blogDocLimit = Integer.parseInt(configProp.getProperty("blogDocLimit"));
			pediaDocLimit = Integer.parseInt(configProp.getProperty("pediaDocLimit"));
			techDocLimit = Integer.parseInt(configProp.getProperty("techDocLimit"));
			
		} catch (FileNotFoundException fnfe) {
			logger.log(Level.SEVERE, "FileNotFoundException caught in loading dir.properties", fnfe);
		} catch (IOException ioe) {
			logger.log(Level.SEVERE, "IOException caught in loading dir.properties", ioe);
		}
	}
	
}