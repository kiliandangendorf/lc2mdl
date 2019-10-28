package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathResponse extends Response{

	private String cas;
	private String answerDisplay;
	private String args;
	private String answerMaxima;

	private String boolPrefix="bool";
	
	public MathResponse(Problem problem,Node node){
		super(problem,node);
	}

	@Override
	public void consumeNode(){
		log.finer("mathresponse:");

//		ArrayList<Node> nodesToRemove=new ArrayList<>();
		Element e=(Element)node;

		//ATTRIBUTES
		cas=e.getAttribute("cas");
		if(cas.equals("maxima")){
			log.finer("-found maxima code.");
			e.removeAttribute("cas");
		}else{
			log.warning("-found cas not equal maxima. Won't work on this response further.");
			return;
		}
		//answerdisplay
		if(e.hasAttribute("answerdisplay")){
			answerDisplay=e.getAttribute("answerdisplay");
			log.finer("-found answerdisplay.");

			if(answerDisplay.charAt(0)=='$'){
				//Variable
				answerDisplay=answerDisplay.substring(1);
				log.finer("--found answerdisplay-variable: "+answerDisplay);
			}else if(answerDisplay.charAt(0)=='@'){
				//Array
				answerDisplay=answerDisplay.substring(1);
				log.finer("--found answerdisplay-array: "+answerDisplay);
			}else{
				//String
				//put in quotes, cause it's used as a string.
				answerDisplay="\""+answerDisplay+"\"";
				log.finer("-found answerdisplay: "+answerDisplay);
			}
			e.removeAttribute("answerdisplay");
		}
		
		if(e.hasAttribute("args")){
			args=e.getAttribute("args");
			log.finer("-found args.");
			if (!args.equals("")) {
				if (args.charAt(0) == '$') {
					//Variable
					args = args.substring(1);
					log.finer("--found args-variable: " + args);
				} else if (args.charAt(0) == '@') {
					//Array
					args = args.substring(1);
					log.finer("--found args-array: " + args);
				} else {
					//Maxima or Perl exression
					//create var (array) from maxima String in questionvariables and reference here
					args = addAdditionalCASVar(args);
					log.warning("-found args as expression (neither var or array). Check if var/array \"" + args + "\" is set perperly in questionvariables.");
				}
			}
			e.removeAttribute("args");			
		}
						
		consumeIdAndName(e);

		if(e.hasAttributes())log.warning("-still unknown attributes in response.");

		//ANSWER
		NodeList answers=e.getElementsByTagName("answer");
		if(answers.getLength()>0){
			log.finer("-found answer");
			if(answers.getLength()>1)log.warning("-more than one answer. Will work on only first one.");
			Element answer=(Element)answers.item(0);
			this.answerMaxima=answer.getTextContent();
			removeNodeFromDOM(answer);
			
			transformAnswerFromLC2Maxima();
		}
		
		//RESPONSEPARAM
		consumeRepsonseParameter(e);
		
		//TEXTLINE size
		consumeTextline(e);
		
		//HINTGROUP
		consumeHintgroups(e);

	}
	
	/**
	 * Transforms LON-CAPA Maxima String into valid Maxima String by replacing RESPONSE- and LONCAPALIST-arrays.
	 * Also assigns last expression to a new boolean-variable. 
	 */
	private void transformAnswerFromLC2Maxima(){
		//read size of RESPONSE
		int maxIndexRESPONSE=0;
		String regExIndexInRESPONSE="(?<=RESPONSE\\[)\\d(?=\\])";
		Pattern pattern=Pattern.compile(regExIndexInRESPONSE);
		Matcher matcher=pattern.matcher(answerMaxima);
		while(matcher.find()){
			String indexString=matcher.group();
			int index=0;
			try{
				index=Integer.parseInt(indexString);
			}catch (Exception e) {
				log.warning("--unable to read RESPONSE-index.");
			}
			if(index>maxIndexRESPONSE)maxIndexRESPONSE=index;
		}
		if(maxIndexRESPONSE>0){
			log.finer("--found RESPONSE-Array with size of "+maxIndexRESPONSE);
		}else{
			log.warning("--no RESPONSE-Array was found.");			
		}

		//read size of LONCAPALIST
		int maxIndexLONCAPALIST=0;
		String regExIndexInLONCAPALIST="(?<=LONCAPALIST\\[)\\d(?=\\])";
		pattern=Pattern.compile(regExIndexInLONCAPALIST);
		matcher=pattern.matcher(answerMaxima);
		while(matcher.find()){
			String indexString=matcher.group();
			int index=0;
			try{
				index=Integer.parseInt(indexString);
			}catch (Exception e) {
				log.warning("--unable to read LONCAPALIST-index.");
			}
			if(index>maxIndexLONCAPALIST)maxIndexLONCAPALIST=index;
		}		
		if(maxIndexLONCAPALIST>0){
			log.finer("--found LONCAPALIST-Array with size of "+maxIndexLONCAPALIST);
		}else{
			log.warning("--no LONCAPALIST-Array was found.");			
		}

		//replace RESPONSE		
		if(maxIndexRESPONSE>1){		
			answerMaxima=answerMaxima.replaceAll("RESPONSE\\[",inputName+"\\[");
		}else{
			answerMaxima=answerMaxima.replaceAll("RESPONSE\\[1\\]",inputName);			
		}
			
		//replace LONCAPALIST
		if(maxIndexLONCAPALIST>1){		
			answerMaxima=answerMaxima.replaceAll("LONCAPALIST\\[",args+"\\[");
		}else{
			answerMaxima=answerMaxima.replaceAll("LONCAPALIST\\[1\\]",args);			
		}
		
		//find last expression and assign to var
		String var=boolPrefix+inputName;
		int lastSemicolon=answerMaxima.lastIndexOf(";");
		int beginOfLastStatement=answerMaxima.substring(0,lastSemicolon).lastIndexOf(';')+1;
		while(answerMaxima.charAt(beginOfLastStatement)==' ' || answerMaxima.charAt(beginOfLastStatement)=='\n')beginOfLastStatement++;
		String value=answerMaxima.substring(beginOfLastStatement,answerMaxima.length());
		answerMaxima=answerMaxima.substring(0,beginOfLastStatement)+var+": "+value;
		log.finer("--defined var: "+var+" with: "+value);
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){
		//Add input in questiontext
		question.addToQuestionText(inputString);
		
		//Add additional vars to questionvariables
		question.addToQuestionVariables(additionalCASVars);

		//INPUT-TAG
		Input input=new Input();
		input.setName(inputName);
		input.setType("algebraic");
		if(!isTextline)input.setType("textarea"); //if it was a textfield
		input.setTans(answerDisplay);
		input.setBoxsize(textlineSize);
		question.addInput(input);
		
		//NODE-TAG
		NodeMdl nodeMdl=new NodeMdl();
		nodeMdl.setAnswertest("AlgEquiv");
		nodeMdl.setSans(boolPrefix+inputName);
		nodeMdl.setTans("true");
		nodeMdl.setTruefeedback(correcthinttext);
		nodeMdl.setFalsefeedback(incorrecthinttext);
		question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

		//ADD MAXIMA TO FEEDBACK-VARS IN CURRENT PRT
		question.addToFeedbackVariablesOfCurrentPrt(answerMaxima);
	}
}
