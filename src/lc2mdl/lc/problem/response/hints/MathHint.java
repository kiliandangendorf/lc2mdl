package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.MathResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MathHint extends MathResponse implements ConditionalHint {


	private boolean link;

	public MathHint(Problem problem, Node node, boolean link){
		super(problem,node);
		inputName="ans"+(problem.getIndexFromResponse(this));
		this.link=link;
	}

    @Override
    public void addToMdlQuestionStack(QuestionStack question){ }

    @Override
    public void consumeNode() {
		log.finer("-mathhint:");
		//HINT

		Element e=(Element)node;

        //ATTRIBUTES
        cas=e.getAttribute("cas");
        if(cas.equals("maxima")){
            log.finer("-found maxima code.");
            e.removeAttribute("cas");
        }else{
            log.warning("-found cas not equal maxima. Won't work on this hint further.");
            return;
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


        //ANSWER
        NodeList answers=e.getElementsByTagName("answer");
        if(answers.getLength()>0){
            log.finer("-found answer");
            if(answers.getLength()>1)log.warning("-more than one answer. Will work on only first one.");
            Element answer=(Element)answers.item(0);
            this.answerMaxima=answer.getTextContent();
            removeNodeFromDOM(answer);
            String postfix= "_hint"+(problem.getIndexFromSameClassOnly(this));

            transformAnswerFromLC2Maxima(postfix);
        }


        consumeIdAndName(e);

		if(e.hasAttributes())log.warning("--still unknown attributes in hint.");

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
    public void addHintNodeToMdlQuestion(QuestionStack question, NodeMdl parentNode) {
        //Add additional vars to questionvariables
        question.addToQuestionVariables(additionalCASVars);

		//NODE-TAG
		NodeMdl hintnode=new NodeMdl();
        hintnode.setAnswertest("AlgEquiv");

		hintnode.setSans(boolans);
		hintnode.setTans("true");
		hintnode.setTruefeedback(correcthinttext);

		//ADD MAXIMA TO FEEDBACK-VARS IN CURRENT PRT
		question.addToFeedbackVariablesOfCurrentPrt(answerMaxima);
		question.addHintNodeToNode(parentNode,hintnode,link);

    }
}
