package lc2mdl.xml;

import lc2mdl.Prefs;
import lc2mdl.mdl.quiz.Quiz;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

public class XMLWriter {
	private static final Logger log = Logger.getLogger(XMLWriter.class.getName());

	/**
	 * Writes the Documnet of the given Quiz-object into XML File outputfile
	 */
	public void writeQuiz2XML(Quiz quiz, File outputfile) throws Exception {
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting exporting Question.");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;

		try{
			db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
	
			//get DOM from question-Object
			dom.appendChild(quiz.exportToDom(dom));
//
//			try{
//				validateQuizDom(dom);
//			}catch(Exception e){
//				log.warning("error while validating. Message: \""+e.getMessage()+"\"");
//			}
		
			writeDom2XML(dom,outputfile);
			
			
		}catch(ParserConfigurationException e){
			log.severe("unable to create DOM.");
			throw e;
		}

		log.fine("Done exporting Question.");
	}
	
	private void validateQuizDom(Document dom) throws Exception{
		URL schemaUrl=getClass().getClassLoader().getResource(Prefs.MDL_XML_SCHEMA);
		validateDOMvsSchema(dom, schemaUrl);
	}
	
	/**
	 * validates Document against URL (XML Schema) 
	 */
	public void validateDOMvsSchema(Document dom, URL schemaUrl) throws Exception{
		String constant = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		SchemaFactory xsdFactory = SchemaFactory.newInstance(constant);

		Schema schema;
		try{
			schema=xsdFactory.newSchema(schemaUrl);
			Validator validator=schema.newValidator();

			log.finer("validate xml against "+Prefs.MDL_XML_SCHEMA);
			validator.validate(new DOMSource(dom));
			log.finer("-done");
			
		}catch(SAXException e){
			log.warning("unable to validate xml vs. schema.");
			throw e;
		}catch(IOException e){
			log.severe("unable to open xml schema-file.");
			throw e;
		}
	}
	
	/**
	 * Writes given Documnet into XML File outputfile
	 */
	public static void writeDom2XML(Document dom, File outputfile) throws Exception {
		DOMSource domSource = new DOMSource(dom);
		StreamResult streamResult = new StreamResult(outputfile);
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			Transformer tf = tff.newTransformer();
			//set for new lines
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			//set for no selfclosing tags (as moodle does)
			tf.setOutputProperty(OutputKeys.METHOD, "html");
			//set indent of child-tags in spaces
			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			log.finer("writing xml file: "+outputfile.getName());
			tf.transform(domSource, streamResult);
			log.finer("-done");
			
						
		} catch (Exception e) {
			log.severe("unable to write file.");
			throw e;
		}
	}
}
