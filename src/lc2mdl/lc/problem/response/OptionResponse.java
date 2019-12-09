package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.hints.ConditionalHint;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;

public class OptionResponse extends ChoiceResponse {

    private ArrayList<String> options = new ArrayList<>();
    private int mincheck=0;
    private int maxcheck=0;
    private String optionText = "Die vorhandenen Optionen sind: ";
    private String checkboxText="Bitte kreuzen Sie alle Aussagen an, auf die Option ";
    private String answerbox=responseprefix+"_box";

    public OptionResponse(Problem problem, Node node) {
        super(problem, node);
    }


    @Override
    public void consumeNode() {
        log.finer("optionresponse:");

         Element e=(Element)node;

         NodeList nodeList = e.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (element.getTagName().equals("foilgroup")) {

                if (element.hasAttribute("options")){
                    String optionString = element.getAttribute("options");
                    optionString = optionString.substring(1,optionString.length()-1);
                    // quick and dirty, may be be bit dangerous
                    optionString = optionString.replaceAll("'","");
                    String[] splitOptions = optionString.split(",");
                    if (splitOptions.length==2) { checkBox = true; }
                    options = new ArrayList<String>(Arrays.asList(splitOptions));
                    element.removeAttribute("options");
                    optionText += optionString;
                }else{
                    log.warning("option response without options! ");
                }



                if (element.hasAttribute("checkboxvalue")){
                    checkBoxValue = element.getAttribute("checkboxvalue");
                    if(!checkBoxValue.equals("")){
                        checkBox = true;
                    }
                    element.removeAttribute("checkboxvalue");
                }else{
                    if (checkBox){
                        checkBoxValue = options.get(0);
                    }
                }

                if (checkBox){
                     checkboxText = optionText+"<br/>"+checkboxText+checkBoxValue+" zutrifft!";
                }

                if  (element.hasAttribute("maxcheck")){
                    String check = element.getAttribute("maxcheck");
                    if(!check.equals("")){
                        maxcheck = Integer.parseInt(check);
                    }
                    element.removeAttribute("maxcheck");
                }
                if  (element.hasAttribute("mincheck")){
                    String check = element.getAttribute("mincheck");
                    if(!check.equals("")){
                        mincheck = Integer.parseInt(check);
                    }
                    element.removeAttribute("mincheck");
                }
                removeAttributeIfExist(e,"checkboxoptions");
            }
        }

        // Foils
        consumeFoils(e);

        if (checkBox){
            additionalCASVars += System.lineSeparator()+responseprefix+"_truechoice : mcq_correct("+answerdisplay+")";
            answer = responseprefix+"_truechoice";
        }else{
            additionalCASVars += System.lineSeparator()+responseprefix+"_options : [";
            boolean start=true;
            for (String o : options){
                if (!start){ additionalCASVars +=","; }
                additionalCASVars += "\""+o+"\"";
                start = false;
            }
            additionalCASVars += "] ";
            additionalCASVars += System.lineSeparator()+answerbox+" : []";
            additionalCASVars += System.lineSeparator()+"for i:1 thru "+numberOfFoils+" do ( ";
            additionalCASVars += "box : makelist([k,is("+answerdisplay+"[i][2]="+responseprefix+"_options[k]), "+responseprefix+"_options[k]],k,1,length("+responseprefix+"_options))";
            additionalCASVars += ", "+answerbox+" : endcons(box,+"+answerbox+") )";
            additionalCASVars += System.lineSeparator()+responseprefix+"_tans : []";
            additionalCASVars += System.lineSeparator()+"for i:1 thru "+numberOfFoils+" do ( ";
            additionalCASVars += "k : 1, while not is("+answerdisplay+"[i][2]="+responseprefix+"_options[k]) do (k : k+1),";
            additionalCASVars += responseprefix+"_tans : endcons (k,"+responseprefix+"_tans) )";

        }

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
    public void addToMdlQuestion(QuestionStack question) {
        //Add additional vars to questionvariables

        question.addToQuestionVariables(additionalCASVars);

        if (checkBox) {
            //Add input in questiontext
            question.addToQuestionText(checkboxText);
            question.addToQuestionText(inputString);

            //INPUT-TAG
            Input input = new Input();
            input.setName(inputName);
            input.setType("checkbox");
            input.setTans(answerdisplay);
            input.setBoxsize(textlineSize);
            input.setMustverify(false);
            input.setShowvalidation(false);
            question.addInput(input);
            //NODE-TAG
            NodeMdl nodeMdl = new NodeMdl();
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
        }else{
            for (int i=1; i<=numberOfFoils; i++){
                //Add input in questiontext
                String inputfoilname = inputName+"_"+i;
                inputString=" [[input:"+inputfoilname+"]] [[validation:"+inputfoilname+"]] ";
                question.addToQuestionText(inputString);
                question.addToQuestionText("{@ "+answerdisplay+"["+i+"][3] @}");
                question.addToQuestionText("<br/>");
                //INPUT-TAG
                Input input = new Input();
                input.setName(inputfoilname);
                input.setType("dropdown");
                // ab hier TODO
                input.setTans(answerbox+"["+i+"]");  // has to be a list
                input.setBoxsize(textlineSize);
                input.setMustverify(false);
                input.setShowvalidation(false);
                question.addInput(input);

                //NODE-TAG
                NodeMdl nodeMdl = new NodeMdl();
                nodeMdl.setAnswertest("AlgEquiv");
                nodeMdl.setSans(inputfoilname);
                nodeMdl.setTans(responseprefix+"_tans["+i+"]"); // has to be nr. with the true

                nodeMdl.setTruefeedback(correcthinttext);
                nodeMdl.setFalsefeedback(incorrecthinttext);

                question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

                //HINTNODES
                if (i == numberOfFoils) {
                    for (ConditionalHint hint : hints) {
                        hint.addHintNodeToMdlQuestion(question, nodeMdl);
                    }
                }
           }

        }

    }
}
