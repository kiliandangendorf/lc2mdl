package lc2mdl.mdl.quiz;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lc2mdl.util.ConvertAndFormatMethods;

public abstract class QuizElement {
	protected String name="lc2mdl";
	
	/**
	 * Adding new nodes for every member to given Document and fill them with values. 
	 */
	public abstract Element exportToDom(Document dom);
	
	/**
	 * Creates Element-node in Document as child of parent.
	 * Sets name and content.
	 * For texcontent this Method also puts text-nodes around the element.
	 */
	protected Element addElementAndTextContent(Document dom, Element parent, String name, String content){
		Element child=dom.createElement(name);
		Element text=dom.createElement("text");
		child.appendChild(text);
		
		if(content!=null){
		CDATASection cdata=dom.createCDATASection(content);
		text.appendChild(cdata);
		}
		
		parent.appendChild(child);
		return child;
	}

	/**
	 * Creates Element-node in Document as child of parent.
	 * Sets name and content.
	 */
	protected Element addElementAndContent(Document dom, Element parent, String name, String content){
		Element child=dom.createElement(name);
		child.setTextContent(content);
		parent.appendChild(child);
		return child;
	}
	/**
	 * Creates Element-node in Document as child of parent.
	 * Sets name and int-content.
	 */
	protected Element addElementAndContent(Document dom, Element parent, String name, int content){
		return addElementAndContent(dom, parent, name, Integer.toString(content));
	}
	/**
	 * Creates Element-node in Document as child of parent.
	 * Sets name and double-content. Formats double for Moodle-STACK.
	 */
	protected Element addElementAndContent(Document dom, Element parent, String name, double content){
		return addElementAndContent(dom, parent, name, ConvertAndFormatMethods.double2StackString(content));
	}
	/**
	 * Creates Element-node in Document as child of parent.
	 * Sets name and boolean-content.
	 */
	protected Element addElementAndContent(Document dom, Element parent, String name, boolean content){
		return addElementAndContent(dom, parent, name, (content?"1":"0"));
	}

	
	//================================================================================
    // Getter and Setter
    //================================================================================
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
