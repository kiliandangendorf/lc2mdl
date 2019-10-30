package lc2mdl.lc;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.*;
import lc2mdl.lc.problem.response.FormulaResponse;
import lc2mdl.lc.problem.response.MathResponse;
import lc2mdl.lc.problem.response.NumericalResponse;
import org.w3c.dom.*;

import java.util.logging.Logger;

public class ProblemReader{
	public static Logger log = Logger.getLogger(ProblemReader.class.getName());
	
	/**
	 * Reading all LON-CAPA elements from given Document and adding them as ProblemElement-objects to new Problem-object. 
	 * @param dom Document of LON-CAPA-problem XML file 
	 * @param problemName will be set to new Problem-Object.
	 * @return New Problem-Object containing all LON-CAPA-elements as ProblemElement-objects.
	 * @throws Exception if no problem-element was found in given Document.
	 */
	public Problem readingDom(Document dom, String problemName) throws Exception{
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting reading DOM.");
		
		Problem problem=null;
		
		if(dom.getElementsByTagName("problem").getLength()==0){
			log.severe("no problem-tag was found. Stop.");
			throw new Exception("no problem-tag was found.");
		}

		if(dom.getElementsByTagName("problem").getLength()>1)log.warning("more than one problem. Only first will be worked on.");
		Node problemNode=dom.getElementsByTagName("problem").item(0);

		problem=new Problem(problemName);
		readingRecursively(problem,problemNode);
		
		log.fine("Done reading DOM.");
		return problem;
	}
	
	private void readingRecursively(Problem problem, Node problemNode){
		NodeList elements=problemNode.getChildNodes();
		for(int i=0;i<elements.getLength();i++){

			//make sure, if I can cast to Element
			if(!(elements.item(i).getNodeType()==Node.ELEMENT_NODE))continue;
			
			Element element=(Element)elements.item(i);
			switch(element.getTagName()){
				case "outtext":
					log.finer("found outtext");
					problem.addElement(new Outtext(problem,element));
					break;
				case "script":
					if(nodeHasAttributePair(element,"type","loncapa/perl")){
						log.finer("found perl-script");
						problem.addElement(new PerlScript(problem,element));
					}else{
						log.warning("unknown script found");
					}
					break;
				case "numericalresponse":
					log.finer("found numericalresponse");
					problem.addElement(new NumericalResponse(problem,element));
					break;
				case "formularesponse":
					log.finer("found formularesponse");
					problem.addElement(new FormulaResponse(problem,element));
					break;
				case "mathresponse":
					log.finer("found mathresponse");
					problem.addElement(new MathResponse(problem,element));
					break;
				case "part":
					log.finer("found part");
					problem.addElement(new Part(problem,element));
					
					//run again, for all elements within part-element
					readingRecursively(problem,element);
					break;
				case "table":case "tr": case "td": case "ol": case "ul": case "li":
					log.finer("found HTML element: "+element.getTagName());
					//put open-tag before and close-tag behind all child elements
					problem.addElement(new HtmlElement(problem,element,HtmlElement.OPEN));					
					readingRecursively(problem,element);
					problem.addElement(new HtmlElement(problem,element,HtmlElement.CLOSE));
					break;
				case "allow":
					log.finer("found allow tag - ignoring it.");
					break;
				default:
					log.warning("unknown element: "+element.getTagName());
					problem.addElement(new UnknownElement(problem,element));
					if(element.hasChildNodes())readingRecursively(problem,element);
					break;
			}
		}
	}
	
	private boolean nodeHasAttributePair(Node n, String key, String value) {
		if (n.hasAttributes()) {
			// get attributes names and values
			NamedNodeMap nodeAttr = n.getAttributes();
			for (int i = 0; i < nodeAttr.getLength(); i++) {
				Node attr = nodeAttr.item(i);
				String attr_name=attr.getNodeName();
				String attr_value=attr.getNodeValue();
//				log.finer("found attribute in "+nname+": "+attr_name+"="+attr_value);
				if(key.equals(attr_name) && value.equals(attr_value))return true;
			}
		}		
		return false;
	}
}