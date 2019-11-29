package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QuestionCategory extends Question{

	// text
	private String category="$course$/top/Default for mdl_test";
	private String prefix="$course/top";
	// text
	private String info="The default category for questions shared in context 'mdl_test'.";

	
	@Override
	public Element exportToDom(Document dom) {

		
		
		//QUESTION COMMENT WITH ID
		
		
		Element e=dom.createElement("question");
		e.setAttribute("type", "category");

		//only here no name field
		
		addElementAndTextContent(dom, e, "category", category);
		addElementAndTextContent(dom, e, "info", info);

		return e;
	}
	
	//================================================================================
    // Getter and Setter
    //================================================================================
	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}

	public void setCategoryWithPrefix(String category){ this.category = prefix+category; }


	public String getInfo() {
		return info;
	}


	public void setInfo(String info) {
		this.info = info;
	}
}
