package lc2mdl.mdl.quiz;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QuestionStack extends Question{
//	private String attr_type="stack"; //stack
//	only stack-only questions will be exported 
	
	//inherited from QuizElement private String name;
	
	// text & CDATA
	private String questiontext="";
	// text
	private String generalfeedback="";
	private double defaultgrade=1.0;
	private double penalty=0.0;//0.1;
	private boolean hidden=false;
	
	private String idnumber="";

	// text
	// to make sure, its "newer" Stack (not old Syntax)
	private String stackversion="2018080600";

	// text & ggf. CDATA
	private String questionvariables="";
	// text & CDATA
	private String specificfeedback="";

	//MUST NOT BE EMPTY IF THERE IS RAND IN QUESTION (MOODLE)
	private String questionnote="lc2mdl-converter_"+new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()); 
	
	private boolean questionsimplify=true;
	private boolean assumepositive=false;
	private boolean assumereal=false;
	
	// text & ggf. CDATA
	private String prtcorrect="<span style=\"font-size: 1.5em; color:green;\"><i class=\"fa fa-check\"></i></span> Richtige Antwort, gut gemacht!";//Correct answer, well done.
	// text & ggf. CDATA
	private String prtpartiallycorrect="<span style=\"font-size: 1.5em; color:orange;\"><i class=\"fa fa-adjust\"></i></span> Ihre Antwort ist teilweise korrekt.";//Your answer is partially correct.
	// text & ggf. CDATA
	private String prtincorrect="<span style=\"font-size: 1.5em; color:red;\"><i class=\"fa fa-times\"></i></span> Falsche Antwort.";//Incorrect answer.
	
	private String multiplicationsign="dot"; //default dot
	private String sqrtsign="1"; //default 1
	private String complexno="i"; //default i
	private String inversetrig="cos-1"; //default cos-1
	private String matrixparens="["; //default [
	
	private String variantsselectionseed=""; //default empty
	
	private ArrayList<Input> input = new ArrayList<>();
	private ArrayList<Prt> prt = new ArrayList<>();//Arrays.asList(new Prt()));
	
	@Override
	public Element exportToDom(Document dom) {

		Element e=dom.createElement("question");
		e.setAttribute("type", "stack");

		addElementAndTextContent(dom, e, "name", name); //only this classes name is in text-tags
		
		addElementAndTextContent(dom, e, "questiontext", questiontext).setAttribute("format", "html");
		addElementAndTextContent(dom, e, "generalfeedback", generalfeedback).setAttribute("format", "html");;
		addElementAndContent(dom, e, "defaultgrade", defaultgrade);
		addElementAndContent(dom, e, "penalty", penalty);
		addElementAndContent(dom, e, "hidden", hidden);
		addElementAndContent(dom, e, "idnumber", idnumber);
		addElementAndTextContent(dom, e, "stackversion", stackversion);
		addElementAndTextContent(dom, e, "questionvariables", questionvariables);
		addElementAndTextContent(dom, e, "specificfeedback", specificfeedback).setAttribute("format", "html");;
		addElementAndTextContent(dom, e, "questionnote", questionnote);
		addElementAndContent(dom, e, "questionsimplify", questionsimplify);
		addElementAndContent(dom, e, "assumepositive", assumepositive);
		addElementAndContent(dom, e, "assumereal", assumereal);

		addElementAndTextContent(dom, e, "prtcorrect", prtcorrect).setAttribute("format", "html");;
		addElementAndTextContent(dom, e, "prtpartiallycorrect", prtpartiallycorrect).setAttribute("format", "html");;
		addElementAndTextContent(dom, e, "prtincorrect", prtincorrect).setAttribute("format", "html");;
		
		addElementAndContent(dom, e, "multiplicationsign", multiplicationsign);
		addElementAndContent(dom, e, "sqrtsign", sqrtsign);
		addElementAndContent(dom, e, "complexno", complexno);
		addElementAndContent(dom, e, "inversetrig", inversetrig);
		addElementAndContent(dom, e, "matrixparens", matrixparens);
		addElementAndContent(dom, e, "variantsselectionseed", variantsselectionseed);
				
		for(Input i: input){
			e.appendChild(i.exportToDom(dom));
		}
		for(Prt p: prt){
			e.appendChild(p.exportToDom(dom));
		}
				
		return e;
	}
	
	public void correctPrtValuesAndLinks(){
		correctNodeAndPrtValues();
		correctPrtStopLinks();
	}
	private void correctNodeAndPrtValues(){
		double valuePerPrt=(double)defaultgrade/(double)prt.size();
		for(Prt p:prt){
			p.setValue(valuePerPrt);
			ArrayList<NodeMdl> nodes=p.getNode();
			//find all NOT hint-nodes
			int nodesWithValue=0;
			for(NodeMdl n:nodes){
				if(!(n.getTruescore()==0.0 && n.getFalsescore()==0.0))nodesWithValue++;
			}
			double valuePerNode=valuePerPrt/nodesWithValue;
			for(NodeMdl n:nodes){
				if(!(n.getTruescore()==0.0 && n.getFalsescore()==0.0))n.setTruescore(valuePerNode);
			}
		}
	}
	private void correctPrtStopLinks(){
		for(Prt p:prt){
			for(NodeMdl n:p.getNode()){
				if(n.getTruenextnode()==NodeMdl.NEXT_NODE)n.setTruenextnode(NodeMdl.STOP_PRT);
				if(n.getFalsenextnode()==NodeMdl.NEXT_NODE)n.setFalsenextnode(NodeMdl.STOP_PRT);
			}
		}
	}

	
	public void addToQuestionText(String s){
		if(s==null)return;
		this.questiontext = this.questiontext+System.lineSeparator()+s.trim();
	}
	public void addToQuestionVariables(String s){
		if(s==null)return;
		this.questionvariables= this.questionvariables+System.lineSeparator()+s.trim();
	}
	
	public void addInput(Input i){
		input.add(i);
	}
	
	private Prt getCurrentPrt(){
		if(prt.size()==0)addPrt(new Prt());
		Prt curPrt=getPrt().get(getPrt().size()-1);
		return curPrt;
	}
	private Prt getPrtOfNode(NodeMdl n){
		if(prt.size()==0)return null;
		for(Prt p:getPrt()){
			if(p.getNode().contains(n))return p;
		}
		return null;		
	}

	public void addToFeedbackVariablesOfCurrentPrt(String s){
		getCurrentPrt().addToFeedbackVariables(s);
	}

	public void addNodeToCurrentPrtAndSetNodeLink(NodeMdl n){
		Prt curPrt=getCurrentPrt();
		curPrt.addNode(n);
		
		int nodeNumber=curPrt.getNode().indexOf(n)+1;
		String prtName=curPrt.getName();
		n.setTrueanswernote(prtName+"-"+nodeNumber+"-T");
		n.setFalseanswernote(prtName+"-"+nodeNumber+"-F");

		//TODO comment
		for(NodeMdl nParent:curPrt.getNode()){
			if(nParent.getTruenextnode()==NodeMdl.NEXT_NODE)nParent.setTruenextnode(nodeNumber-1);
			if(nParent.getFalsenextnode()==NodeMdl.NEXT_NODE)nParent.setFalsenextnode(nodeNumber-1);
		}
		//link this node to next node
		n.setTruenextnode(NodeMdl.NEXT_NODE);
		n.setFalsenextnode(NodeMdl.NEXT_NODE);
		//link parent-node to this node 
//		if(nodeNumber>1){
//			curPrt.getNode().get(nodeNumber-2).setTruenextnode(nodeNumber-1);
//			curPrt.getNode().get(nodeNumber-2).setFalsenextnode(nodeNumber-1);
//		}
	}
	public void addHintNodeToNode(NodeMdl parentNode, NodeMdl hintnode, boolean link){
		//TODO: maybe cleanup here?
		//hint nodes doesn't affect score
		hintnode.setTruescore(0.0);
		hintnode.setFalsescore(0.0);
		
		//get belonging prt and add hintnode (node-name will be set)
		Prt curPrt=getPrtOfNode(parentNode);
		if(curPrt==null)return;
		curPrt.addNode(hintnode);
		
		//set hintnode note
		int nodeNumber=curPrt.getNode().indexOf(hintnode)+1;
		String prtName=curPrt.getName();
		hintnode.setTrueanswernote(prtName+"-"+nodeNumber+"-T");
		hintnode.setFalseanswernote(prtName+"-"+nodeNumber+"-F");

		int nextNode=NodeMdl.NEXT_NODE;
		//set link from parent
		if(link){
			nextNode=parentNode.getTruenextnode();
			parentNode.setTruenextnode(Integer.parseInt(hintnode.getName()));
		}
		if(!link){
			nextNode=parentNode.getFalsenextnode();
			parentNode.setFalsenextnode(Integer.parseInt(hintnode.getName()));
		}
		
		//link hintnode to next node
//		hintnode.setTruenextnode(NodeMdl.NEXT_NODE);
//		hintnode.setFalsenextnode(NodeMdl.NEXT_NODE);
		hintnode.setTruenextnode(nextNode);
		hintnode.setFalsenextnode(nextNode);
	}
	
	public void addPrt(Prt p){
		prt.add(p);
		
		//only here name of prt is set 
		int prtNumber=prt.indexOf(p)+1;
		p.setName("prt"+prtNumber);
		
		//and link name in specificfeedback
		specificfeedback=specificfeedback+"[[feedback:"+p.getName()+"]]";
	}
	
	//================================================================================
    // Getter and Setter
    //================================================================================
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

	public String getStackversion() {
		return stackversion;
	}

	public void setStackversion(String stackversion) {
		this.stackversion = stackversion;
	}

	public String getQuestionvariables() {
		return questionvariables;
	}

	public void setQuestionvariables(String questionvariables) {
		this.questionvariables = questionvariables;
	}

	public String getSpecificfeedback() {
		return specificfeedback;
	}

	public void setSpecificfeedback(String specificfeedback) {
		this.specificfeedback = specificfeedback;
	}

	public String getQuestionnote() {
		return questionnote;
	}

	public void setQuestionnote(String questionnote) {
		this.questionnote = questionnote;
	}

	public boolean isQuestionsimplify() {
		return questionsimplify;
	}

	public void setQuestionsimplify(boolean questionsimplify) {
		this.questionsimplify = questionsimplify;
	}

	public boolean isAssumepositive() {
		return assumepositive;
	}

	public void setAssumepositive(boolean assumepositive) {
		this.assumepositive = assumepositive;
	}

	public boolean isAssumereal() {
		return assumereal;
	}

	public void setAssumereal(boolean assumereal) {
		this.assumereal = assumereal;
	}

	public String getPrtcorrect() {
		return prtcorrect;
	}

	public void setPrtcorrect(String prtcorrect) {
		this.prtcorrect = prtcorrect;
	}

	public String getPrtpartiallycorrect() {
		return prtpartiallycorrect;
	}

	public void setPrtpartiallycorrect(String prtpartiallycorrect) {
		this.prtpartiallycorrect = prtpartiallycorrect;
	}

	public String getPrtincorrect() {
		return prtincorrect;
	}

	public void setPrtincorrect(String prtincorrect) {
		this.prtincorrect = prtincorrect;
	}

	public String getMultiplicationsign() {
		return multiplicationsign;
	}

	public void setMultiplicationsign(String multiplicationsign) {
		this.multiplicationsign = multiplicationsign;
	}

	public String getSqrtsign() {
		return sqrtsign;
	}

	public void setSqrtsign(String sqrtsign) {
		this.sqrtsign = sqrtsign;
	}

	public String getComplexno() {
		return complexno;
	}

	public void setComplexno(String complexno) {
		this.complexno = complexno;
	}

	public String getInversetrig() {
		return inversetrig;
	}

	public void setInversetrig(String inversetrig) {
		this.inversetrig = inversetrig;
	}

	public String getMatrixparens() {
		return matrixparens;
	}

	public void setMatrixparens(String matrixparens) {
		this.matrixparens = matrixparens;
	}

	public String getVariantsselectionseed() {
		return variantsselectionseed;
	}

	public void setVariantsselectionseed(String variantsselectionseed) {
		this.variantsselectionseed = variantsselectionseed;
	}

	public ArrayList<Input> getInput() {
		return input;
	}

	public void setInput(ArrayList<Input> input) {
		this.input = input;
	}

	public ArrayList<Prt> getPrt() {
		return prt;
	}

	public void setPrt(ArrayList<Prt> prt) {
		this.prt = prt;
	}

	public String getIdnumber(){
		return idnumber;
	}

	public void setIdnumber(String idnumber){
		this.idnumber=idnumber;
	}
}
