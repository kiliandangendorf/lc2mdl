package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

public abstract class Question extends QuizElement{
    protected ArrayList<String> tags = new ArrayList<>();
	protected ArrayList<String> comm = new ArrayList<>();
    // text & CDATA
    protected String questiontext="";
    // text
    protected String generalfeedback="";
    protected double defaultgrade=1.0;
    protected double penalty=0.0;//0.1;
    protected boolean hidden=false;

    protected String idnumber="";


    @Override
    public Element exportToDom(Document dom) {
        Element e=dom.createElement("question");
        addElementAndTextContent(dom, e, "name", name); //only this classes name is in text-tags

        addElementAndTextContent(dom, e, "questiontext", questiontext).setAttribute("format", "html");
        addElementAndTextContent(dom, e, "generalfeedback", generalfeedback).setAttribute("format", "html");;
        addElementAndContent(dom, e, "defaultgrade", defaultgrade);
        addElementAndContent(dom, e, "penalty", penalty);
        addElementAndContent(dom, e, "hidden", hidden);
        addElementAndContent(dom, e, "idnumber", idnumber);

        return e;
    }

    protected Element setCommentsInDom(Document dom, Element e){
		for (String s: comm){
			e.appendChild(dom.createComment(s));
		}

		return e;
    }

 	public void addComment(String s){
		comm.add(s);
	}

    public void addToQuestionText(String s){
        if(s==null)return;
        this.questiontext = this.questiontext+System.lineSeparator()+s.trim();
    }


    //================================================================================
    // Getter and Setter
    //================================================================================
   public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getQuestiontext() {
        return questiontext;
    }

    public void setQuestiontext(String questiontext) {
        this.questiontext = questiontext;
    }

    public String getGeneralfeedback() {
        return generalfeedback;
    }

    public void setGeneralfeedback(String generalfeedback) {
        this.generalfeedback = generalfeedback;
    }

    public double getDefaultgrade() {
        return defaultgrade;
    }

    public void setDefaultgrade(double defaultgrade) {
        this.defaultgrade = defaultgrade;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }


}
