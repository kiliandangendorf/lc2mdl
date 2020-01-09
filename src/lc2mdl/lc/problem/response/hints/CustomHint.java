package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.CustomResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CustomHint extends CustomResponse implements ConditionalHint {

	private boolean link;

    public CustomHint(Problem problem, Node node, boolean link) {
        super(problem, node);
		int noHints = problem.getNumberOfHints()+1;
		problem.setNumberOfHints(noHints);
		inputName="ans"+(problem.getIndexFromResponse(this));
		this.link=link;
		inputName = inputName+"_customhint"+noHints;
		answer = "bool"+inputName;
    }

    @Override
    public void consumeNode() {
		log.finer("-mathhint:");
		//HINT

		Element e=(Element)node;

        consumeIdAndName(e);

		if(e.hasAttributes())log.warning("--still unknown attributes in hint.");

		consumeAnswer(e);

		//RESPONSEPARAM
		consumeResponseParameter(e);

        //HINTPARTS
        Node hintgroup=node.getParentNode();
        if(hintgroup==null || hintgroup.getNodeType()!=Node.ELEMENT_NODE){
            log.warning("--no parent elementnode was found.");
            return;
        }
        Element p=(Element)hintgroup;
        NodeList hintparts=p.getElementsByTagName("hintpart");
        for(int i=0;i<hintparts.getLength();i++){
            Element hintpart=(Element)hintparts.item(i);
            if(hintpart.getAttribute("on").equals(name)){
                NodeList hinttexts=hintpart.getElementsByTagName("outtext");
                for(int j=0;j<hinttexts.getLength();j++){
                    Element hinttext=(Element)hinttexts.item(j);
                    String hint=hinttext.getTextContent();
                    addHinttext(hint,true);
                    hinttext.setTextContent(null);
                }

                removeAttributeIfExist(hintpart,"on");
                if(hintpart.hasAttributes())log.warning("--still unknown attributes in hintpart.");
            }
        }


    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {
    }

    @Override
    public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode) {
        //Add additional vars to questionvariables
        question.addToQuestionVariables(additionalCASVars);

		//NODE-TAG
		NodeMdl hintnode=new NodeMdl();
        hintnode.setAnswertest("AlgEquiv");

		hintnode.setSans(answer);
		hintnode.setTans("true");
		hintnode.setTruefeedback(correcthinttext);

		//ADD MAXIMA TO FEEDBACK-VARS IN CURRENT PRT
		question.addToFeedbackVariablesOfCurrentPrt(addToFeedbackVariables);
		question.addComment(comment);
		question.addHintNodeToNode(parentNode,hintnode,link);

    }
}
