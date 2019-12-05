package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.RadiobuttonResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class RadiobuttonHint extends RadiobuttonResponse implements ConditionalHint {

   	private boolean link;
   	private ArrayList<String> answerList = new ArrayList<>();
   	private String addToFeedbackVariables="";
   	private String hintPrefix;
   	private String inputHint;

	public RadiobuttonHint(Problem problem, Node node, boolean link){
		super(problem,node);
		this.link=link;
		inputName="ans"+(problem.getIndexFromResponse(this));
		responseprefix = "choice"+(problem.getIndexFromResponse(this))+"_";
		inputHint = inputName+"radiohint"+(problem.getIndexFromSameClassOnly(this));
		hintPrefix = "radiohint"+(problem.getIndexFromSameClassOnly(this))+"_";
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
                //if not $ the first symbol, then create a var in questionvariables and reference here
                answer=addAdditionalCASVar(answer);
            }
            e.removeAttribute("answer");
        }else{
            log.warning("--no answer found in hint");
        }
        consumeIdAndName(e);

        String[] split = answer.split(",");
        addToFeedbackVariables += System.lineSeparator()+"/* hint name: "+name+"*/";
        addToFeedbackVariables += System.lineSeparator()+hintPrefix;
        if (split.length>0){
            if (split[0].equals("'foil'")){
                addToFeedbackVariables += "foils : [";
                for (int i=1; i<split.length; i++){
                    split[i] = "\""+split[i].substring(1,split[i].length()-1)+"\"";
                    if (i>1) { addToFeedbackVariables += ","; }
                    addToFeedbackVariables += split[i];
                }
                addToFeedbackVariables += "]";
            }else{
                if (split[0].equals("'concept'")) {
                     addToFeedbackVariables += "concepts :[";
                    for (int i = 1; i < split.length; i++) {
                        split[i] = "\"" + split[i].substring(1, split[i].length() - 1) + "\"";
                        if (i>1) { addToFeedbackVariables += ","; }
                        addToFeedbackVariables += split[i];
                    }
                    addToFeedbackVariables += "]";
                    addToFeedbackVariables += System.lineSeparator()+hintPrefix+ "foils : []";
                    addToFeedbackVariables += System.lineSeparator()+hintPrefix+"_k : 1";
                    addToFeedbackVariables += System.lineSeparator()+"while z in "+responseprefix+"concepts do (";
                    addToFeedbackVariables += "if member("+hintPrefix+"concepts) then ( append("+hintPrefix+"foils,";
                    addToFeedbackVariables += responseprefix+"conceptfoils["+hintPrefix+"_k])), ";
                    addToFeedbackVariables += hintPrefix+"_k : "+hintPrefix+"_k + 1 )";
                }
            }
            addToFeedbackVariables += System.lineSeparator()+inputHint+" : member( "+inputName+","+hintPrefix+"foils)";
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
