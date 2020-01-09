package lc2mdl.lc.problem.response.hints;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.OptionResponse;
import lc2mdl.lc.problem.response.Response;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OptionHint extends OptionResponse implements ConditionalHint {

   	private boolean link;
   	private String addToFeedbackVariables="";
   	private String hintPrefix;
   	private String inputHint;
   	private Boolean isConcept=false;
   	private int indexFoil=0;

   	public OptionHint(Problem problem, Node node, boolean link){
		super(problem,node);
		this.link=link;
		int noHints = problem.getNumberOfHints()+1;
		problem.setNumberOfHints(noHints);
		inputName="ans"+(problem.getIndexFromResponse(this));
		responseprefix = "choice"+(problem.getIndexFromResponse(this));
		inputHint = inputName+"_optionhint"+noHints;
		hintPrefix = "optionhint"+noHints+"_";
		Response currentResponse = problem.getCurrentResponse(this);
		if (currentResponse != null) {
            OptionResponse parentResponse = (OptionResponse)currentResponse;
            isCheckBox = parentResponse.isCheckBox();
            checkBoxValue = parentResponse.getCheckBoxValue();
            if (!isCheckBox){
                nodeMdls = parentResponse.getNodeMdls();
                foilsList = parentResponse.getFoilsList();
                options = parentResponse.getOptions();
            }
        }else{
		    log.warning("--did not find current response");
        }
	}


    @Override
    public void consumeNode() {
        log.finer("-optionhint:");
        //HINT

        Element e=(Element)node;

         //ATTRIBUTES
       if (!(e.hasAttribute("answer") || e.hasAttribute("concept"))){
            log.warning("neither answer nor concept in optionhint");
        }

        if(e.hasAttribute("answer")){
            answer=e.getAttribute("answer");
            if (!answer.equals("")) {
                log.finer("-found answer: " + answer);
                if (answer.charAt(0) == '$') {
                    answer = answer.substring(1);
                } else {
                    answer = answer.substring(1, answer.length() - 1); // remove parentheses
                }
            }
            e.removeAttribute("answer");
        }

        if(e.hasAttribute("concept")){
            answer=e.getAttribute("concept");
            log.finer("-found concept: "+answer);
            if (!answer.equals("")) {
                if (answer.charAt(0) == '$') {
                    answer = answer.substring(1);
                } else {
                    answer = answer.substring(1, answer.length() - 1); // remove parentheses
                }
                isConcept = true;
            }
            e.removeAttribute("concept");
        }

        consumeIdAndName(e);

        addToFeedbackVariables += System.lineSeparator()+System.lineSeparator()+"/* hint name: "+name+"*/";
        String[] answerSplit = answer.split(",");
        addToFeedbackVariables += System.lineSeparator()+inputHint+" : false";
        if (isCheckBox){
            if (isConcept){
                for (int i=0; i<answerSplit.length; i++){
                    String[] split = answerSplit[i].split("'\\s*?=>\\s*?'");
                    if (split.length>1) {
                        String conceptname = "\"" + split[0].replaceAll("'","")+"\"";
                        String value = split[1].replaceAll("'","");
                        Boolean check = value.equalsIgnoreCase("incorrect");
                        String checkString = "";
                        if (check) { checkString = "in"; }
                        addToFeedbackVariables += System.lineSeparator()+"for k : 1 thru length("+responseprefix+"_concepts) do (";
                        addToFeedbackVariables += "if is("+responseprefix+"_concepts[k]="+conceptname+") then (";
                        addToFeedbackVariables += " for j : 1 thru length("+responseprefix+"_conceptfoils[k]) do (";
                        addToFeedbackVariables += inputHint+" : "+inputHint+" or (member("+responseprefix+"_conceptfoils[k],"+inputName+")";
                        addToFeedbackVariables += "and member("+responseprefix+"_conceptfoils[k], mcq_"+checkString+"correct("+responseprefix+" ))), ";
                        if (check) {checkString="";} else { checkString = "in"; }
                        addToFeedbackVariables += inputHint+" : "+inputHint+" or (not member("+responseprefix+"_conceptfoils[k],"+inputName+")";
                        addToFeedbackVariables += "and member("+responseprefix+"_conceptfoils[k], mcq_"+checkString+"correct("+responseprefix+" ))), ";
                        addToFeedbackVariables += "))"; // for, if is
                    }
                }
            }else{
                for (int i=0; i<answerSplit.length; i++){
                    String[] split = answerSplit[i].split("'\\s*?=>\\s*?'");
                    if (split.length>1) {
                        String foilname = "\"" + split[0].replaceAll("'","")+"\"";
                        String value = split[1].replaceAll("'","");
                        Boolean check = (value.equals(checkBoxValue));
                        String checkString = "";
                        if (!check) { checkString = " not "; }
                        addToFeedbackVariables += System.lineSeparator()+inputHint+" : "+inputHint+" or (";
                        addToFeedbackVariables += checkString+"member("+foilname+", mcq("+responseprefix+")) )";
                    }
                }
            }
        }else{
            if (isConcept){
               for (int i=0; i<answerSplit.length; i++) {
                   String[] split = answerSplit[i].split("'\\s*?=>\\s*?'");
                   if (split.length > 1) {
                       String conceptname = "\"" + split[0].replaceAll("'", "") + "\"";
                       String value = split[1].replaceAll("'", "");
                       Boolean check = value.equalsIgnoreCase("incorrect");
                       String checkString="";
                       if (check) { checkString = "not"; }
                       int index=0;
                       for (InputFoil ifoil : foilsList) {
                           if (ifoil.getConceptName().equals(conceptname)) {
                               indexFoil = index;
                               addToFeedbackVariables += System.lineSeparator()+inputHint+" : "+inputHint+" or (";
                               addToFeedbackVariables += checkString+"is("+ifoil.getInName()+" = "+responseprefix+"_tans["+(index+1)+"]))";
                               break;
                           }
                       }
                   }
               }
            }else{
               for (int i=0; i<answerSplit.length; i++) {
                   String[] split = answerSplit[i].split("'\\s*?=>\\s*?'");
                   if (split.length > 1) {
                       String foilname = "\"" + split[0].replaceAll("'", "") + "\"";
                       String value = split[1].replaceAll("'", "");
                       int indexOption=1;
                       for (String o : options){
                           if (o.equals(value)){ break; }
                           indexOption++;
                       }

                       int index=0;
                       for (InputFoil ifoil : foilsList){
                           if (ifoil.getFoilName().equals(foilname)){
                               indexFoil = index;
                               addToFeedbackVariables  += System.lineSeparator()+inputHint+" : "+inputHint+" or (";
                               addToFeedbackVariables  += "is("+ifoil.getInName()+" = "+indexOption+"))";
                               break;
                           }
                           index++;
                       }
                   }
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
    public void addToMdlQuestionStack(QuestionStack question) { }

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

        if (isCheckBox) {
            question.addHintNodeToNode(parentNode, hintnode, link);
        }else{
            question.addHintNodeToNode(nodeMdls.get(indexFoil),hintnode, link);
        }
	}



}
