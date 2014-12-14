
package crawler.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Parser {
	
	public static final Logger logger = Logger.getLogger(Parser.class.getName());
	
	// Blog Parser
	public static HashMap<String, Object> blogToXml (String blogpath, Document xmlDoc, Element add, int docId) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if (!isValidName(blogpath)) {
			// Check if the blogpath is valid or not
			logger.warning("Invalid blogname!");
		} else {
			// Valid blogpath
			SFDocument blogDoc = new SFDocument();
			BufferedReader br = null;
			String currLine = "";
			int lineCount = 0;
			int authorStrLimit = 40;
			
			String title = "";
			String date = "";
			String author = "";
			String body = "";
			String[] tags = null;
			
			// Title
			if (blogpath.contains(File.separator)) {
				String titleStr = blogpath.substring(blogpath.lastIndexOf(File.separator)+1);
				if (titleStr.endsWith(".txt")) {
					title = titleStr.substring(0, titleStr.length()-4);
					if (title.startsWith("_")) {
						title = title.substring(1);
					}
					title = title.replace("-", " ");
					title = title.replace("_", " ");
				}
			}
			
			// Read blog file
			try {
				br = new BufferedReader(new FileReader(blogpath));
			} catch (FileNotFoundException fnfe) {
				logger.log(Level.SEVERE, "Blog file not found - " + blogpath, fnfe);
			}
			
			try {
				while((currLine = br.readLine()) != null) {
					if (lineCount == 0) {
						// Date
						date = currLine;
						date = date.trim();
					} else if (lineCount == 1) {
						if (currLine.length() > authorStrLimit) {
							// No Author, Body
							body += currLine;
						} else {
							// Author
							author += currLine;
							author = author.trim();
						}
					} else if (lineCount > 1) {
						// Body
						body += currLine;
					}
					lineCount++;
				}
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, "IOException in reading blog file - " + blogpath, ioe);
			}
			
			body = body.trim();
			if (author == "" || author.length() == 0) {
				author = "Tor.com";
			}
			
			blogDoc.setField(FieldNames.TITLE, title);
			blogDoc.setField(FieldNames.TAGS, tags);
			blogDoc.setField(FieldNames.DATE, date);
			blogDoc.setField(FieldNames.AUTHOR, author);
			blogDoc.setField(FieldNames.BODY, body);
			
			map = addToXmlFile(blogpath, blogDoc, xmlDoc, add, docId, null, null);
		}
		
		return map;
	}
	
	// Encyclopedia Parser
	public static HashMap<String, Object> pediaToXml(String pediapath, Document xmlDoc, Element add, int docId, HashMap<Integer, ArrayList<Integer>> relatedTechMap, HashMap<Integer, ArrayList<Integer>> relatedBlogMap) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if (!isValidName(pediapath)) {
			// Check if the pediapath is valid or not
			logger.warning("Invalid pediapath!");
		} else {
			// Valid pediapath
			SFDocument pediaDoc = new SFDocument();
			BufferedReader br = null;
			String currLine = "";
			int lineCount = 0;
			
			String title = "";
			String tagStr = "";
			String[] tags = null;
			String date = "";
			String author = "";
			String body = "";
			
			// Title
			if (pediapath.contains(File.separator)) {
				String titleStr = pediapath.substring(pediapath.lastIndexOf(File.separator)+1);
				if (titleStr.endsWith(".txt")) {
					title = titleStr.substring(0, titleStr.length()-4);
					if (title.startsWith("_")) {
						title = title.substring(1);
					}
					title = title.replace("-", " ");
					title = title.replace("_", " ");
				}
			}
			
			// Read pedia file
			try {
				br = new BufferedReader(new FileReader(pediapath));
			} catch (FileNotFoundException fnfe) {
				logger.log(Level.SEVERE, "Pedia file not found - " + pediapath, fnfe);
			}
			
			try {
				while((currLine = br.readLine()) != null) {
					if (lineCount == 0) {
						// Tags
						tagStr = currLine;
						tagStr = tagStr.trim();
					} else if (lineCount > 0) {
						// Body
						body += currLine;
					}
					lineCount++;
				}
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, "IOException in reading pedia file - " + pediapath, ioe);
			}
			
			body = body.trim();
			
			if (tagStr.startsWith("Tagged:")) {
				tagStr = tagStr.substring(7);
				tagStr = tagStr.trim();
				tags = tagStr.split("\\|");
			}
			
			pediaDoc.setField(FieldNames.TITLE, title);
			pediaDoc.setField(FieldNames.TAGS, tags);
			pediaDoc.setField(FieldNames.DATE, date);
			pediaDoc.setField(FieldNames.AUTHOR, author);
			pediaDoc.setField(FieldNames.BODY, body);
			
			map = addToXmlFile(pediapath, pediaDoc, xmlDoc, add, docId, relatedTechMap, relatedBlogMap);
		}
		
		return map;
	}
	
	// Technology Parser
	public static HashMap<String, Object> techToXml(String techpath, Document xmlDoc, Element add, int docId) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		if (!isValidName(techpath)) {
			// Check if the techpath is valid or not
			logger.warning("Invalid techpath!");
		} else {
			// Valid techpath
			SFDocument techDoc = new SFDocument();
			BufferedReader br = null;
			String currLine = "";
			int lineCount = 0;
			int titleLimit = 100;
			
			String title = "";
			String[] tags = null;
			String date = "";
			String author = "";
			String body = "";
			
			// Read tech file
			try {
				br = new BufferedReader(new FileReader(techpath));
			} catch (FileNotFoundException fnfe) {
				logger.log(Level.SEVERE, "Tech file not found - " + techpath, fnfe);
			}
			
			try {
				while((currLine = br.readLine()) != null) {
					if (lineCount == 0) {
						if (currLine.length() < titleLimit) {
							// Title
							title = currLine;
							title = title.trim();
						} else {
							// Body
							body += currLine;
						}
					} else if (lineCount > 0) {
						// Body
						body += currLine;
					}
					lineCount++;
				}
			} catch (IOException ioe) {
				logger.log(Level.SEVERE, "IOException in reading tech file - " + techpath, ioe);
			}
			
			body = body.trim();
			
			techDoc.setField(FieldNames.TITLE, title);
			techDoc.setField(FieldNames.TAGS, tags);
			techDoc.setField(FieldNames.DATE, date);
			techDoc.setField(FieldNames.AUTHOR, author);
			techDoc.setField(FieldNames.BODY, body);
			
			map = addToXmlFile(techpath, techDoc, xmlDoc, add, docId, null, null);
		}
		
		return map;
	}
	
	// Generic add data to XML file
	public static HashMap<String, Object> addToXmlFile(String path, SFDocument sfDoc, Document xmlDoc, Element add, int docId, HashMap<Integer, ArrayList<Integer>> relatedTechMap, HashMap<Integer, ArrayList<Integer>> relatedBlogMap) {
 
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		// <doc></doc>
		Element doc = xmlDoc.createElement("doc");
		add.appendChild(doc);
 
		// <field name="id"></field>
		Element id = xmlDoc.createElement("field");
		doc.appendChild(id);
		id.setAttribute("name", "id");
		id.appendChild(xmlDoc.createTextNode(String.valueOf(docId)));
		
		// <field name="title"></field>
		String titleStr = (String)sfDoc.getField(FieldNames.TITLE);
		if (titleStr != null && titleStr != "") {
			Element title = xmlDoc.createElement("field");
			doc.appendChild(title);
			title.setAttribute("name", "title");
			title.appendChild(xmlDoc.createTextNode(titleStr));
		}
		
		// <field name="date"></field>
		String dateStr = (String)sfDoc.getField(FieldNames.DATE);
		if (dateStr != "" && dateStr != null) {
			Element date = xmlDoc.createElement("field");
			doc.appendChild(date);
			date.setAttribute("name", "date");
			date.appendChild(xmlDoc.createTextNode(dateStr));
		}
		
		// <field name="author"></field>
		String authorStr = (String)sfDoc.getField(FieldNames.AUTHOR);
		if (authorStr != "" && authorStr != null) {
			Element author = xmlDoc.createElement("field");
			doc.appendChild(author);
			author.setAttribute("name", "author");
			author.appendChild(xmlDoc.createTextNode(authorStr));
		}
		
		// <field name="body"></field>
		String bodyStr = (String)sfDoc.getField(FieldNames.BODY);
		if (bodyStr != "" && bodyStr != null) {
			Element body = xmlDoc.createElement("field");
			doc.appendChild(body);
			body.setAttribute("name", "body");
			body.appendChild(xmlDoc.createTextNode(bodyStr));
		}
		
		// <field name="tag"></field>
		String[] tags = (String[])sfDoc.getField(FieldNames.TAGS);
		if (tags != null && tags.length > 0) {
			for (String t : tags) {
				t = t.trim();
				Element tag = xmlDoc.createElement("field");
				doc.appendChild(tag);
				tag.setAttribute("name", "tag");
				tag.appendChild(xmlDoc.createTextNode(t));
			}
		}
		
		// <field name="relatedTech"></field>
		if (relatedTechMap != null && relatedTechMap.size() > 0) {
			if (relatedTechMap.containsKey(docId)) {
				ArrayList<Integer> relatedTechIds = (ArrayList<Integer>)relatedTechMap.get(docId);
				if (relatedTechIds != null && relatedTechIds.size() > 0) {
					for (int relatedTechId : relatedTechIds) {
						String relatedTechIdStr = String.valueOf(relatedTechId);
						Element relatedTech = xmlDoc.createElement("field");
						doc.appendChild(relatedTech);
						relatedTech.setAttribute("name", "relatedTech");
						relatedTech.appendChild(xmlDoc.createTextNode(relatedTechIdStr));
					}
				}
			}
		}
		
		// <field name="relatedBlog"></field>
		if (relatedBlogMap != null && relatedBlogMap.size() > 0) {
			if (relatedBlogMap.containsKey(docId)) {
				ArrayList<Integer> relatedBlogIds = (ArrayList<Integer>)relatedBlogMap.get(docId);
				if (relatedBlogIds != null && relatedBlogIds.size() > 0) {
					for (int relatedBlogId : relatedBlogIds) {
						String relatedBlogIdStr = String.valueOf(relatedBlogId);
						Element relatedBlog = xmlDoc.createElement("field");
						doc.appendChild(relatedBlog);
						relatedBlog.setAttribute("name", "relatedBlog");
						relatedBlog.appendChild(xmlDoc.createTextNode(relatedBlogIdStr));
					}
				}
			}
		}
		
		map.put("xmlDoc", xmlDoc);
		map.put("add", add);
		
		return map;
	}
	
	// Check if filename is valid
	public static boolean isValidName(String name) {
		boolean isValid = true;
		
		if (name == null || name == "" || !name.endsWith(".txt")) {
			isValid = false;
		}
		
		return isValid;
	}
	
}