package lc2mdl.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import lc2mdl.Prefs;

public class XMLParser {
	private static final Logger log = Logger.getLogger(XMLParser.class.getName());

	/**
	 * Parses XML and validates it against XML Schema
	 * @param xmlfile Path to XML File
	 * @return parsed DOM
	 * @throws Exception 
	 */
	public Document parseXML2DOM(final File xmlfile) throws Exception {
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting XML Parsing.");

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		Document doc = null;

		URL schemaUrl = getClass().getClassLoader().getResource(Prefs.LC_XML_SCHEMA);

		// create schema
		String constant = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		SchemaFactory xsdFactory = SchemaFactory.newInstance(constant);
		try {
			Schema schema = xsdFactory.newSchema(schemaUrl);

			// set schema
			dbfac.setSchema(schema);
			
			DocumentBuilder db = dbfac.newDocumentBuilder();
			
			db.setErrorHandler(new ErrorHandler(){
				@Override
				public void warning(SAXParseException exception) throws SAXException{
					logErr(exception,"Warning");
				}
				@Override
				public void fatalError(SAXParseException exception) throws SAXException{
					logErr(exception,"FatalError");
				}
				@Override
				public void error(SAXParseException exception) throws SAXException{
					logErr(exception,"Error");
				}
				private void logErr(SAXParseException e, String type){
					//if it's my own "outtext"-tag everything is fine, else warning;)
					if(e.getMessage().contains("'outtext'"))return;
					
					log.warning("XML-Parsing-"+type+" in line "+e.getLineNumber()+": "+e.getMessage().trim());
				}
			});
			doc = db.parse(xmlfile);				
		} catch (Exception e) {
			log.severe("error while reading xml.");
			throw e;
		}
		
		if(doc==null){
			log.severe("DOM is null.");
			throw new Exception("DOM is null.");
		}
		
		log.fine("Done XML Parsing.");
		return doc;
	}
	
	public static Document parseString2DOM(String string) throws SAXException, IOException, ParserConfigurationException{
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbfac.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(string)));		
		return doc;
	} 
}
