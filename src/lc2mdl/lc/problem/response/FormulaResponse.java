package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.hints.ConditionalHint;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FormulaResponse extends Response{

	protected String answer;// must fit perl params if there is a $-sign

	public FormulaResponse(Problem problem,Node node){
		super(problem,node);
	}

	@Override
	public void consumeNode(){
		log.finer("formularesponse:");

		Element e=(Element)node;

		//ATTRIBUTES
		if(e.hasAttribute("answer")){
			answer=e.getAttribute("answer");
			if (!answer.equals("")) {
				log.finer("-found answer: " + answer);
				if (answer.charAt(0) == '$') {
					answer = answer.substring(1);
				} else {
					//if not $ the first symbol, then create a var in questionvariables and reference here
					answer = addAdditionalCASVar(answer);
				}
			}
			e.removeAttribute("answer");
		}else{
			log.warning("-no answer found in response");
		}
		
		removeAttributeIfExist(e,"samples"); //not needed in AlgEquiv

		consumeIdAndName(e);

		if(e.hasAttributes())log.warning("-still unknown attributes in response.");

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
	public void addToMdlQuestion(QuestionStack question){
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
		input.setTans(answer);
		input.setBoxsize(textlineSize);
		question.addInput(input);
		
		//NODE-TAG
		NodeMdl nodeMdl=new NodeMdl();
		nodeMdl.setAnswertest("AlgEquiv");
		nodeMdl.setSans(inputName);
		nodeMdl.setTans(answer);

		nodeMdl.setTruefeedback(correcthinttext);
		nodeMdl.setFalsefeedback(incorrecthinttext);

		question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

		//HINTNODES
		for(ConditionalHint hint:hints){
			hint.addHintNodeToMdlQuestion(question,nodeMdl);
		}

	}
}
