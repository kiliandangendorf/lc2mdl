package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.PerlScript;
import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.hints.ConditionalHint;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CustomResponse extends Response {

	protected String answer="";// must fit perl params if there is a $-sign
	private String answerDisplay;
	protected String addToFeedbackVariables;
	protected String comment;


    public CustomResponse(Problem problem, Node node){
        super(problem, node);
        answer = "bool"+inputName;
    }

    @Override
    public void consumeNode() {
        log.finer("customresponse:");

        Element e=(Element)node;

        //ATTRIBUTES
        if(e.hasAttribute("answerdisplay")){
            answerDisplay=e.getAttribute("answerdisplay");
            if (!answerDisplay.equals("")) {
                log.finer("-found answerdisplay.");

                if (answerDisplay.charAt(0) == '$') {
                    //Variable
                    answerDisplay = answerDisplay.substring(1);
                    log.finer("--found answerdisplay-variable: " + answerDisplay);
                } else if (answerDisplay.charAt(0) == '@') {
                    //Array
                    answerDisplay = answerDisplay.substring(1);
                    log.finer("--found answerdisplay-array: " + answerDisplay);
                } else {
                    //String
                    //put in quotes, cause it's used as a string.
                    answerDisplay = "\"" + answerDisplay + "\"";
                    log.finer("-found answerdisplay: " + answerDisplay);
                }
            }else{
                answerDisplay=answer;
            }
			e.removeAttribute("answerdisplay");
		}else{
            answerDisplay=answer;
        }

        consumeIdAndName(e);
        if (e.hasAttributes()){
            log.warning("-still unknown attributes in response.");
        }

        // Answer
        consumeAnswer(e);

       //RESPONSEPARAM
		consumeResponseParameter(e);

		//TEXTLINE size
		consumeTextline(e);

		//HINTGROUP
		consumeHintgroups(e);

        //Additional Text
        consumeText(e);

    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
		//Add input in questiontext
		question.addToQuestionText(inputString);
		question.addToQuestionText(additionalText);

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
		nodeMdl.setSans(answer);
		nodeMdl.setTans("true");
		nodeMdl.setTruefeedback(correcthinttext);
		nodeMdl.setFalsefeedback(incorrecthinttext);

		question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

		//HINTNODES
		for(ConditionalHint hint:hints){
			hint.addHintNodeToMdlQuestion(question,nodeMdl);
		}

		//ADD MAXIMA TO FEEDBACK-VARS IN CURRENT PRT
		question.addToFeedbackVariablesOfCurrentPrt(addToFeedbackVariables);

		question.addComment(comment);
    }

    protected void consumeAnswer(Element e){
        NodeList nlist = e.getChildNodes();
        for (int i=0; i<nlist.getLength(); i++){
            if(!(nlist.item(i).getNodeType()==Node.ELEMENT_NODE))continue;
			Element element = (Element)nlist.item(i);
			if (element.getTagName().equals("answer")) {
			    String script =  element.getTextContent();
			    String startString = System.lineSeparator()+"submission : "+inputName+System.lineSeparator()+answer+" : false";
			    String patString = "return\\s*?'EXACT_ANS'\\s*;";
			    script = script.replaceAll(patString, answer+" : true");
			    patString = "return\\s*?'INCORRECT'\\s*;";
			    script = script.replaceAll(patString, answer+" : false");
			    script = startString+System.lineSeparator()+script;
                element.setTextContent(script);
                PerlScript ps= new PerlScript(problem,element);
                ps.consumeNode();
                addToFeedbackVariables = ps.getScript();
                comment = ps.getScriptComment();
                element.setTextContent(null);
			}
		}
    }
}
