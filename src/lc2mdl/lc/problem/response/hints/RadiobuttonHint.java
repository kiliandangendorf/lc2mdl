package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.RadiobuttonResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RadiobuttonHint extends RadiobuttonResponse implements ConditionalHint {

   	private boolean link;
   	private String hintPrefix;
   	private String inputHint;

	public RadiobuttonHint(Problem problem, Node node, boolean link){
		super(problem,node);
		this.link=link;
		int noHints = problem.getNumberOfHints()+1;
		problem.setNumberOfHints(noHints);
		inputName="ans"+(problem.getIndexFromResponse(this));
		responseprefix = "choice"+(problem.getIndexFromResponse(this));
		inputHint = inputName+"_radiohint"+noHints;
		hintPrefix = "radiohint"+noHints+"_";
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){}

    @Override
    public void consumeNode(){
        log.finer("-radiobuttonhint:");
        //HINT

        Element e=(Element)node;

        //ATTRIBUTES
        if(e.hasAttribute("answer")){
            answer=e.getAttribute("answer");
            log.finer("-found answer: "+answer);
            if(answer.charAt(0)=='$'){
                answer=answer.substring(1);
            }else{
                 answer = answer.substring(1,answer.length()-1); // remove parentheses
            }
            e.removeAttribute("answer");
        }else{
            log.warning("--no answer found in hint");
        }

         consumeIdAndName(e);

        String[] split = answer.split(",");
        addToFeedbackVariables += System.lineSeparator()+System.lineSeparator()+"/* hint name: "+name+"*/";
        addToFeedbackVariables += System.lineSeparator()+hintPrefix;
        if (split.length>0){

            // hint on single foils
            if (split[0].equals("'foil'")){
                addToFeedbackVariables += "foils : [";
                for (int i=1; i<split.length; i++){
                    split[i] = "\""+split[i].substring(1,split[i].length()-1)+"\"";
                    if (i>1) { addToFeedbackVariables += ","; }
                    addToFeedbackVariables += split[i];
                }
                addToFeedbackVariables += "]";
                addToFeedbackVariables += System.lineSeparator()+inputHint+" :  member("+inputName+","+hintPrefix+"foils)";
            }else{

                // hint on concept group
                if (split[0].equals("'concept'")) {
                     addToFeedbackVariables += "concepts :[";
                    for (int i = 1; i < split.length; i++) {
                        split[i] = "\"" + split[i].substring(1, split[i].length() - 1) + "\"";
                        if (i>1) { addToFeedbackVariables += ","; }
                        addToFeedbackVariables += split[i];
                    }
                    addToFeedbackVariables += "]";
                    addToFeedbackVariables += System.lineSeparator()+inputHint+" : false";
                    addToFeedbackVariables += System.lineSeparator()+"for k : 1 thru length("+responseprefix+"_concepts) do (";
                    addToFeedbackVariables += "if member("+responseprefix+"_concepts[k],"+hintPrefix+"concepts) then (";
                    addToFeedbackVariables += inputHint+" : "+inputHint+" or member("+inputName+",";
                    addToFeedbackVariables += responseprefix+"_conceptfoils[k])) )";
                }
            }
        }



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
                    hint = transformTextElement(hint);
                    addHinttext(hint,true);
                    hinttext.setTextContent(null);
                }

                removeAttributeIfExist(hintpart,"on");
                if(hintpart.hasAttributes())log.warning("--still unknown attributes in hintpart.");
            }
        }
    }

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
