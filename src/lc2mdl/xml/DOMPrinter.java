package lc2mdl.xml;

import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lc2mdl.Prefs;

public class DOMPrinter {
	public static Logger log = Logger.getLogger(DOMPrinter.class.getName());

	public int depth = 0;
	
	public void printDoc(Document dom){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting printing DOM.");
	
		if (dom.hasChildNodes()) {
			 printNode(dom.getChildNodes());
		}
		log.fine("Done printing DOM.");
	}
	
	private void syso(String s) {
		for (int k = 0; k < depth; k++){
			System.out.print("  ");
		}
		System.out.println(s);
	}
	
	private void printNode(NodeList nodeList) {
		depth++;
	
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node curNode = nodeList.item(i);
			
			syso("START: "+curNode.getNodeName());
			depth++;
			
			if (curNode.hasAttributes()) {
				// get attributes names and values
				NamedNodeMap nodeMap = curNode.getAttributes();
				depth++;
				syso("ATTRIBUTES:");
				for (int j = 0; j < nodeMap.getLength(); j++) {
					Node node = nodeMap.item(j);
					syso(node.getNodeName()+ " = "+node.getNodeValue());
				}
				depth--;
			}
	
			switch (curNode.getNodeType()) {
			case Node.TEXT_NODE:
				syso("Node Value =" + curNode.getTextContent());
				break;
			case Node.CDATA_SECTION_NODE:
				syso("Node Value =" + curNode.getTextContent());			
				break;
			case Node.ELEMENT_NODE:
				syso("Node Value =" + curNode.getNodeValue());
				break;
			default:
				break;
			}
			
			if (curNode.hasChildNodes()) {
				printNode(curNode.getChildNodes());
			}
			depth--;
			syso("END:   " + curNode.getNodeName());
		}
		depth--;
	}
}
