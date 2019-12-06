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
    private String checkboxText="Bitte kreuzen Sie alle Aussagen an, die ";
    private String answerbox=responseprefix+"box";

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
                    options = new ArrayList<>(Arrays.asList(splitOptions));
                    element.removeAttribute("options");
                }else{
                    log.warning("option response without options! ");
                }

                if (element.hasAttribute("checkboxvalue")){
                    checkBoxValue = element.getAttribute("checkboxvalue");
                    if(!checkBoxValue.equals("")){
                        checkboxText += checkBoxValue+" sind!";
                        checkBox = true;
                    }
                    element.removeAttribute("checkboxvalue");
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
            additionalCASVars += System.lineSeparator()+responseprefix+"truechoice : mcq_correct("+answerdisplay+")";
            answer = responseprefix+"truechoice";
        }else{
            additionalCASVars += System.lineSeparator()+responseprefix+"options : [";
            boolean start=true;
            for (String o : options){
                if (!start){ additionalCASVars +=","; }
                additionalCASVars += "\""+o+"\"";
                start = false;
            }
            additionalCASVars += "], ";
            additionalCASVars += System.lineSeparator()+"for i:1 thru "+numberOfFoils+"do(";
            additionalCASVars += answerbox+"[i] : makelist([i,is("+answerdisplay+"[i][2]=options[k]), options[k]],k,1,length("+responseprefix+"options))";
            additionalCASVars += ")";
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
                input.setTans(answerdisplay); //Liste1
                input.setBoxsize(textlineSize);
                input.setMustverify(false);
                input.setShowvalidation(false);
                question.addInput(input);
                //inputString=" [[input:"+inputName+"]] [[validation:"+inputName+"]] ";
                //NODE-TAG
                NodeMdl nodeMdl = new NodeMdl();
                nodeMdl.setAnswertest("AlgEquiv");
                nodeMdl.setSans(inputfoilname);
                nodeMdl.setTans(answer); //nr.

                nodeMdl.setTruefeedback(correcthinttext);
                nodeMdl.setFalsefeedback(incorrecthinttext);

                question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

                //HINTNODES
                for(ConditionalHint hint:hints){
                    hint.addHintNodeToMdlQuestion(question,nodeMdl);
                }
           }

        }

    }
}
