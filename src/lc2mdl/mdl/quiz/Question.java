package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

public abstract class Question extends QuizElement{
    protected ArrayList<String> tags = new ArrayList<>();
    protected String path;
	protected ArrayList<String> comm = new ArrayList<>();

    protected Element setTagsAndCommentsInDom(Document dom, Element e){
        tags.add("lc2mdl");
        Element t = dom.createElement("tags");
		e.appendChild(t);
		for (String s: tags) {
            addElementAndTextContent(dom, t, "tag", s);
        }
		for (String s: comm){
			e.appendChild(dom.createComment(s));
		}

		return e;
    }

 	public void addComment(String s){
		comm.add(s);
	}

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

}
