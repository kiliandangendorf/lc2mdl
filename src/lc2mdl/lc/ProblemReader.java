package lc2mdl.lc;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.*;
import lc2mdl.lc.problem.display.PostAnswerDate;
import lc2mdl.lc.problem.display.Solved;
import lc2mdl.lc.problem.response.*;
import org.w3c.dom.*;

import java.util.HashMap;
import java.util.logging.Logger;

public class ProblemReader{
	public static Logger log = Logger.getLogger(ProblemReader.class.getName());
	private HashMap<String,Document> libs;
	
	/**
	 * Reading all LON-CAPA elements from given Document and adding them as ProblemElement-objects to new Problem-object. 
	 * @param dom Document of LON-CAPA-problem XML file 
	 * @param problemName will be set to new Problem-Object.
	 * @return New Problem-Object containing all LON-CAPA-elements as ProblemElement-objects.
	 * @throws Exception if no problem-element was found in given Document.
	 */
	public Problem readingDom(Document dom, String problemName, HashMap<String,Document> libs, HashMap<String,String> images, String path) throws Exception{
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting reading DOM.");
		
		Problem problem=null;
		
		if(dom.getElementsByTagName("problem").getLength()==0){
			log.severe("no problem-tag was found. Stop.");
			throw new Exception("no problem-tag was found.");
		}

		if(dom.getElementsByTagName("problem").getLength()>1)log.warning("more than one problem. Only first will be worked on.");
		Node problemNode=dom.getElementsByTagName("problem").item(0);

		this.libs = libs;

		problem=new Problem(problemName,images,path);
		readingRecursively(problem,problemNode);
		
		log.fine("Done reading DOM.");
		return problem;
	}

	private Node readingLibDom(String path)  {
	    Node libNode = null;
	    String[] pathSplit = path.split("/");
	    String libname = pathSplit[pathSplit.length-1];
	    if (!libs.containsKey(libname)){
	        log.warning("library "+ libname +" not found");
	      //  throw new Exception("library "+ libname +" not found");
        }else{
	        if (libs.get(libname).getElementsByTagName("library").getLength()>1) {
                log.warning("more than one library. Only first will be worked on.");
            }
	            libNode = libs.get(libname).getElementsByTagName("library").item(0);

        }

	    return libNode;

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
				case "gnuplot":
					log.finer("found gnuplot");
					problem.addElement(new Gnuplot(problem,element));
					break;
				case "img":
					log.finer("found image");
					problem.addElement(new Image(problem,element));
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
				case "stringresponse":
					log.finer("found stringresponse");
					problem.addElement(new StringResponse(problem,element));
					break;
				case "radiobuttonresponse":
					log.finer("found radiobuttonresponse");
					problem.addElement(new RadiobuttonResponse(problem,element));
					break;
				case "optionresponse":
					log.finer("found optionresponse");
					problem.addElement(new OptionResponse(problem,element));
					break;
				case "customresponse":
					log.finer("found customresponse");
					problem.addElement(new CustomResponse(problem,element));
					break;
					// TODO essay response as other moodle question
				case "essayresponse":
					log.finer("found essayresponse");
					problem.addElement(new EssayResponse(problem,element));
					break;


				case "part":
					log.finer("found part");
					problem.addElement(new Part(problem,element));
					
					//run again, for all elements within part-element
					readingRecursively(problem,element);
					break;
				case "block":
					log.finer("found conditional block");
					problem.addElement(new Block(problem,element));

					//run again, for all elements within part-element
					readingRecursively(problem,element);
					break;

				case "table":case "tr": case "td": case "ol": case "ul": case "li" :case "center": case "br": case "hr" : case "div" : case "p": case "b":
					log.finer("found HTML element: "+element.getTagName());
					//put open-tag before and close-tag behind all child elements
					problem.addElement(new HtmlElement(problem,element,HtmlElement.OPEN));					
					readingRecursively(problem,element);
					problem.addElement(new HtmlElement(problem,element,HtmlElement.CLOSE));
					break;
				case "allow": case "meta":
					log.finer("found "+element.getTagName()+" tag - ignoring it.");
					problem.addElement(new IgnoredElement(problem,element));
					break;
				case "parameter":
					log.finer("found parameter");
					problem.addElement(new Parameter(problem,element));
					break;
				case "notsolved": case "preduedate":
					log.finer("found element"+element.getTagName());
					readingRecursively(problem,element);
					problem.addElement(new IgnoredElement(problem,element));
					break;
				case "solved":
					log.finer("found solved");
					if (element.getElementsByTagName("part").getLength()==0) {
						problem.addElement(new Solved(problem, element));
					}else{
						log.warning("--ignore that the part before should be solved before showing the next part.");
						readingRecursively(problem,element);
						problem.addElement(new IgnoredElement(problem,element));
					}
					break;
				case "postanswerdate":
					log.finer("found postanswerdate");
					problem.addElement(new PostAnswerDate(problem,element));
					break;
                case "import":
                	log.finer("found import");
                    String path= element.getTextContent();
                    Node libNode = readingLibDom(path);
                    if (libNode !=  null){
                       readingRecursively(problem,libNode);
                       element.setTextContent(null);
                    }
                    problem.addElement(new IgnoredElement(problem,element));
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