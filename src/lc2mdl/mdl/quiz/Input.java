package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Input extends QuizElement{

	//inherited from QuizElement private String name; //ans1
	
	private String type; //algebraic
	private String tans; //Loesung
	private int boxsize=15; //15
	private boolean strictsyntax=true; //1
	private boolean insertstars=false; //0
	private String syntaxhint=""; //empty
	private boolean syntaxattribute=false; //0
	private String forbidwords=""; //empty
	private String allowwords=""; //empty
	private boolean forbidfloat=true; //1
	private boolean requirelowestterms=false; //0
	private boolean checkanswertype; //0
	private boolean mustverify=true; //1
	private boolean showvalidation=true; //1
	private String options=""; //empty
	
	@Override
	public Element exportToDom(Document dom) {
		Element e=dom.createElement("input");

		addElementAndContent(dom, e, "name", name);
		addElementAndContent(dom, e, "type", type);
		addElementAndContent(dom, e, "tans", tans);
		addElementAndContent(dom, e, "boxsize", boxsize);
		addElementAndContent(dom, e, "strictsyntax", strictsyntax);
		addElementAndContent(dom, e, "insertstars", insertstars);
		addElementAndContent(dom, e, "syntaxhint", syntaxhint);
		addElementAndContent(dom, e, "syntaxattribute", syntaxattribute);
		addElementAndContent(dom, e, "forbidwords", forbidwords);
		addElementAndContent(dom, e, "allowwords", allowwords);
		addElementAndContent(dom, e, "forbidfloat", forbidfloat);
		addElementAndContent(dom, e, "requirelowestterms", requirelowestterms);
		addElementAndContent(dom, e, "checkanswertype", checkanswertype);
		addElementAndContent(dom, e, "mustverify", mustverify);
		addElementAndContent(dom, e, "showvalidation", showvalidation);
		addElementAndContent(dom, e, "options", options);
		
		return e;
	}
	
	//================================================================================
    // Getter and Setter
    //================================================================================
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTans() {
		return tans;
	}

	public void setTans(String tans) {
		this.tans = tans;
	}

	public int getBoxsize() {
		return boxsize;
	}

	public void setBoxsize(int boxsize) {
		this.boxsize = boxsize;
	}

	public boolean isStrictsyntax() {
		return strictsyntax;
	}

	public void setStrictsyntax(boolean strictsyntax) {
		this.strictsyntax = strictsyntax;
	}

	public boolean isInsertstars() {
		return insertstars;
	}

	public void setInsertstars(boolean insertstars) {
		this.insertstars = insertstars;
	}

	public String getSyntaxhint() {
		return syntaxhint;
	}

	public void setSyntaxhint(String syntaxhint) {
		this.syntaxhint = syntaxhint;
	}

	public boolean isSyntaxattribute() {
		return syntaxattribute;
	}

	public void setSyntaxattribute(boolean syntaxattribute) {
		this.syntaxattribute = syntaxattribute;
	}

	public String getForbidwords() {
		return forbidwords;
	}

	public void setForbidwords(String forbidwords) {
		this.forbidwords = forbidwords;
	}

	public String getAllowwords() {
		return allowwords;
	}

	public void setAllowwords(String allowwords) {
		this.allowwords = allowwords;
	}

	public boolean isForbidfloat() {
		return forbidfloat;
	}

	public void setForbidfloat(boolean forbidfloat) {
		this.forbidfloat = forbidfloat;
	}

	public boolean isRequirelowestterms() {
		return requirelowestterms;
	}

	public void setRequirelowestterms(boolean requirelowestterms) {
		this.requirelowestterms = requirelowestterms;
	}

	public boolean isCheckanswertype() {
		return checkanswertype;
	}

	public void setCheckanswertype(boolean checkanswertype) {
		this.checkanswertype = checkanswertype;
	}

	public boolean isMustverify() {
		return mustverify;
	}

	public void setMustverify(boolean mustverify) {
		this.mustverify = mustverify;
	}

	public boolean isShowvalidation() {
		return showvalidation;
	}

	public void setShowvalidation(boolean showvalidation) {
		this.showvalidation = showvalidation;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}
}
