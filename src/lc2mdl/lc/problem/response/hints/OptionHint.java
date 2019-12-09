package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.OptionResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class OptionHint extends OptionResponse implements ConditionalHint {

   	private boolean link;
   	private String addToFeedbackVariables="";
   	private String hintPrefix;
   	private String inputHint;

   	public OptionHint(Problem problem, Node node, boolean link, boolean checkbox){
		super(problem,node);
		this.link=link;
		int noHints = problem.getNumberOfHints()+1;
		problem.setNumberOfHints(noHints);
		inputName="ans"+(problem.getIndexFromResponse(this));
		responseprefix = "choice"+(problem.getIndexFromResponse(this))+"_";
		inputHint = inputName+"_optionhint"+noHints;
		hintPrefix = "optionhint"+noHints+"_";
		checkBox = checkbox;
	}


    @Override
    public void consumeNode() {
        log.finer("-optionhint:");
        //HINT

        Element e=(Element)node;
    }

    @Override
    public void addToMdlQuestion(QuestionStack question) { }

       @Override
	public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode){

		//Add additional vars to questionvariables
		question.addToQuestionVariables(additionalCASVars);

		//NODE-TAG
		NodeMdl hintnode=new NodeMdl();
		hintnode.setAnswertest("AlgEquiv");


		hintnode.setSans(inputHint);
		hintnode.setTans("true");
		hintnode.setTruefeedback(correcthinttext);

		question.addToFeedbackVariablesOfCurrentPrt(addToFeedbackVariables);
		question.addHintNodeToNode(parentNode,hintnode,link);
	}

}
