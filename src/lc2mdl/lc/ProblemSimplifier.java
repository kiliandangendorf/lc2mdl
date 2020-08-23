package lc2mdl.lc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lc2mdl.Prefs;

public class ProblemSimplifier {
	public static Logger log = Logger.getLogger(ProblemSimplifier.class.getName());

	/**
	 * Simplifies given DOM recursively by removing all empty nodes. 
	 */
	public void simplify(Node n){
		log.fine(Prefs.CLI_LINE_SEP);
		log.fine("Starting simplifying DOM.");
		simplifiyRecursive(n);
		log.fine("Done simplifying DOM.");
	}
	
	private void simplifiyRecursive(Node node){
		if(node.hasChildNodes()){
			NodeList childNodes=node.getChildNodes();
			
			ArrayList<Node> tmpNodes=new ArrayList<>();
			for(int i=0;i<childNodes.getLength();i++){
				tmpNodes.add(childNodes.item(i));
			}
			for(Node n2:tmpNodes){				
				simplifiyRecursive(n2);
			}
		}
		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				if(!node.hasChildNodes()){
					if(!node.hasAttributes()){
						if(!isHTML(node)){
							removeNode(node);
						}
					}
				}
				break;
			case Node.TEXT_NODE:
				if(node.getNodeValue().trim().isEmpty())removeNode(node);
				break;
			case Node.CDATA_SECTION_NODE:
				if(node.getTextContent().trim().isEmpty())removeNode(node);
				break;
			case Node.DOCUMENT_NODE:break;
		}
	}
	private void removeNode(Node n){
		log.finer("remove empty "+n.getNodeName()+" in "+n.getParentNode().getNodeName());
		n.getParentNode().removeChild(n);
	}
	private boolean isHTML(Node n){
		ArrayList<String> htmlTagsToBePreventedBySimplifying=new ArrayList<>(
				Arrays.asList("table","tr","td","ol","ul","li","center","br","hr","div","p","b"));
		if(htmlTagsToBePreventedBySimplifying.contains(n.getNodeName()))return true;
		return false;
	}
}
