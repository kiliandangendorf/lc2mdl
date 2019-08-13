package lc2mdl.mdl.quiz;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Prt extends QuizElement{

	//inherited from QuizElement private String name; //prt1
	private double value=1.0; //1.0
	private boolean autosimplify=true; //1
	// text empty
	private String feedbackvariables="";
	private ArrayList<NodeMdl> nodes=new ArrayList<>();
		
	@Override
	public Element exportToDom(Document dom) {
		Element e=dom.createElement("prt");
		
		addElementAndContent(dom, e, "name", name);		
		addElementAndContent(dom, e, "value", value);
		addElementAndContent(dom, e, "autosimplify", autosimplify);

		addElementAndTextContent(dom, e, "feedbackvariables", feedbackvariables); //text but no html
		
		for(NodeMdl n: nodes){
			e.appendChild(n.exportToDom(dom));
		}
		
		return e;
	}

	/**
	 * adds Node and sets unique Node-name
	 */
	public void addNode(NodeMdl n){
		nodes.add(n);
		n.setName(String.valueOf(nodes.indexOf(n)));
	}

	public void addToFeedbackVariables(String s){
		if(s==null)return;
		this.feedbackvariables = this.feedbackvariables+System.lineSeparator()+s.trim();
	}

	
	//================================================================================
    // Getter and Setter
    //================================================================================
	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public boolean isAutosimplify() {
		return autosimplify;
	}


	public void setAutosimplify(boolean autosimplify) {
		this.autosimplify = autosimplify;
	}


	public String getFeedbackvariables() {
		return feedbackvariables;
	}


	public void setFeedbackvariables(String feedbackvariables) {
		this.feedbackvariables = feedbackvariables;
	}


	public ArrayList<NodeMdl> getNode() {
		return nodes;
	}


	public void setNode(ArrayList<NodeMdl> node) {
		this.nodes = node;
	}
}
