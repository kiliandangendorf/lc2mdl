package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.hints.ConditionalHint;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class StringResponse extends Response {
    protected String type;
	protected String answer;// must fit perl params if there is a $-sign
	private String answerDisplay;
	protected String comment="";

    public StringResponse(Problem problem, Node node){

        super(problem,node);
        questionType = "stack";
        problem.addQuestionType(questionType);

    }

    @Override
    public void consumeNode() {
        log.finer("stringresponse:");

        Element e=(Element)node;

        //ATTRIBUTES
        if(e.hasAttribute("answer")){
            answer=e.getAttribute("answer");
            log.finer("-found answer: "+answer);
            if(answer.charAt(0)=='$'){
                answer=answer.substring(1);
            }else{
                //if not $ the first symbol, then create a var in questionvariables and reference here
                    //String
                    //put in quotes, cause it's used as a string.
               answer=addAdditionalCASVar("\""+answer+"\"");
            }
            e.removeAttribute("answer");
        }else{
            log.warning("-no answer found in response");
        }

        type = "cs";
        if (e.hasAttribute("type")){
            type = e.getAttribute("type");
            log.finer("-found type "+type);
            if (!(type.equals("cs")||type.equals("ci"))){
                log.warning("cannot handle type "+type+" Set type to \"cs\"");
                comment += "cannot handle type "+type+" Set type to \"cs\"";
                type="cs";
            }
            e.removeAttribute("type");
        }

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

        if (e.hasAttribute("preprocess")){
            log.warning("String response needs preprocessing, cannot to this.");
            comment += "String response needs preprocessing, cannot to this.";
            e.removeAttribute("preprocess");
        }

        consumeIdAndName(e);
        if (e.hasAttributes()){
            log.warning("-still unknown attributes in response.");
        }

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
    public void addToMdlQuestionStack(QuestionStack question) {
        //Add input in questiontext
		question.addToQuestionText(inputString);
        question.addToQuestionText(additionalText);

		//Add additional vars to questionvariables
		question.addToQuestionVariables(additionalCASVars);

		//INPUT-TAG
		Input input=new Input();
		input.setName(inputName);
		input.setType("string"); //because of stringresponse
		if(!isTextline)input.setType("textarea"); //if it was a textfield
		input.setTans(answerDisplay);
		input.setBoxsize(textlineSize);
		question.addInput(input);

		//NODE-TAG
		NodeMdl nodeMdl=new NodeMdl();
		if (type.equals("cs")) {         // case sensitive
            nodeMdl.setSans(inputName);
            nodeMdl.setTans(answer);
        }else{                          // case insensitive
		    nodeMdl.setSans("supcase("+inputName+")");
		    nodeMdl.setTans("supcase("+answer+")");
        }
		nodeMdl.setAnswertest("String");

        nodeMdl.setTruefeedback(correcthinttext);
        nodeMdl.setFalsefeedback(incorrecthinttext);

        question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

        //HINTNODES
        for(ConditionalHint hint:hints){
            hint.addHintNodeToMdlQuestion(question,nodeMdl);
        }



        question.addComment(comment);

    }
}
