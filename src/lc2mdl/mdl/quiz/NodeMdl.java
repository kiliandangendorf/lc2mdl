package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NodeMdl extends QuizElement{
	//internal constant for linking on next node
	public static final int NEXT_NODE=-2;
	//internal constant to define last node in prt
	public static final int STOP_PRT=-1;
	
	//private String name; // 0	//inherited from QuizElement
	private String answertest="AlgEquiv"; // AlgEquiv
	private String sans; // ans1
	private String tans; // Loesung
	private String testoptions=""; // empty
	private boolean quiet=false; // 0
	private String truescoremode="="; // =
	private double truescore=1.0; // 1.0
	private String truepenalty=""; // empty
	private int truenextnode=STOP_PRT; // -1
	private String trueanswernote="prt1-1-T"; // prt1-1-T
	// text empty html
	private String truefeedback="";
	private String falsescoremode="="; // =
	private double falsescore=0.0; // 0.0
	private String falsepenalty=""; // empty
	private int falsenextnode=STOP_PRT; // -1
	private String falseanswernote="prt1-1-F"; // prt1-1-F
	// text empty html
	private String falsefeedback="";

	@Override
	public Element exportToDom(Document dom){
		Element e=dom.createElement("node");
		
		addElementAndContent(dom, e, "name", name);		
		addElementAndContent(dom, e, "answertest", answertest);		
		addElementAndContent(dom, e, "sans", sans);		
		addElementAndContent(dom, e, "tans", tans);		
		addElementAndContent(dom, e, "testoptions", testoptions);		
		addElementAndContent(dom, e, "quiet", quiet);		
		addElementAndContent(dom, e, "truescoremode", truescoremode);
		addElementAndContent(dom, e, "truescore", truescore);
		addElementAndContent(dom, e, "truepenalty", truepenalty);
		addElementAndContent(dom, e, "truenextnode", truenextnode);
		addElementAndContent(dom, e, "trueanswernote", trueanswernote);
		
		addElementAndTextContent(dom, e, "truefeedback", truefeedback).setAttribute("format", "html");
		
		addElementAndContent(dom, e, "falsescoremode", falsescoremode);
		addElementAndContent(dom, e, "falsescore", falsescore);
		addElementAndContent(dom, e, "falsepenalty", falsepenalty);
		addElementAndContent(dom, e, "falsenextnode", falsenextnode);
		addElementAndContent(dom, e, "falseanswernote", falseanswernote);
		addElementAndTextContent(dom, e, "falsefeedback", falsefeedback).setAttribute("format", "html");
		
		return e;
	}
	
	
	//================================================================================
    // Getter and Setter
    //================================================================================
	public String getAnswertest() {
		return answertest;
	}

	public void setAnswertest(String answertest) {
		this.answertest = answertest;
	}

	public String getSans() {
		return sans;
	}

	public void setSans(String sans) {
		this.sans = sans;
	}

	public String getTans() {
		return tans;
	}

	public void setTans(String tans) {
		this.tans = tans;
	}

	public String getTestoptions() {
		return testoptions;
	}

	public void setTestoptions(String testoptions) {
		this.testoptions = testoptions;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	public String getTruescoremode() {
		return truescoremode;
	}

	public void setTruescoremode(String truescoremode) {
		this.truescoremode = truescoremode;
	}

	public double getTruescore() {
		return truescore;
	}

	public void setTruescore(double truescore) {
		this.truescore = truescore;
	}

	public String getTruepenalty() {
		return truepenalty;
	}

	public void setTruepenalty(String truepenalty) {
		this.truepenalty = truepenalty;
	}

	public int getTruenextnode() {
		return truenextnode;
	}

	public void setTruenextnode(int truenextnode) {
		this.truenextnode = truenextnode;
	}

	public String getTrueanswernote() {
		return trueanswernote;
	}

	public void setTrueanswernote(String trueanswernote) {
		this.trueanswernote = trueanswernote;
	}

	public String getTruefeedback() {
		return truefeedback;
	}

	public void setTruefeedback(String truefeedback) {
		this.truefeedback = truefeedback;
	}

	public String getFalsescoremode() {
		return falsescoremode;
	}

	public void setFalsescoremode(String falsescoremode) {
		this.falsescoremode = falsescoremode;
	}

	public double getFalsescore() {
		return falsescore;
	}

	public void setFalsescore(double falsescore) {
		this.falsescore = falsescore;
	}

	public String getFalsepenalty() {
		return falsepenalty;
	}

	public void setFalsepenalty(String falsepenalty) {
		this.falsepenalty = falsepenalty;
	}

	public int getFalsenextnode() {
		return falsenextnode;
	}

	public void setFalsenextnode(int falsenextnode) {
		this.falsenextnode = falsenextnode;
	}

	public String getFalseanswernote() {
		return falseanswernote;
	}

	public void setFalseanswernote(String falseanswernote) {
		this.falseanswernote = falseanswernote;
	}

	public String getFalsefeedback() {
		return falsefeedback;
	}

	public void setFalsefeedback(String falsefeedback) {
		this.falsefeedback = falsefeedback;
	}	
}
