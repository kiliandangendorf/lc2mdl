package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//Gerrit
public class RadiobuttonResponse extends ChoiceResponse {

    protected String answer;// must fit perl params if there is a $-sign
    private String solutionVar;

    public RadiobuttonResponse(Problem problem, Node node) {
        super(problem, node);
    }

    @Override
    public void consumeNode() {
        log.finer("radiobuttonresponse:");

        String answerTemp = "[";
        boolean randPermutation = false;
        Element e=(Element)node;
        if (e.hasAttribute("ra"))

        if(e.getElementsByTagName("foilgroup").getLength() > 0) {
            log.finer("-" + e.getElementsByTagName("foilgroup").getLength() + " foilgroup(s) found (use only first):");

            Element foilgroup = (Element) e.getElementsByTagName("foilgroup").item(0);
            if(foilgroup.getElementsByTagName("foil").getLength() > 0) {
                log.finer("--" + foilgroup.getElementsByTagName("foil").getLength() + " foil(s) found:");


                NodeList foils = foilgroup.getElementsByTagName("foil");
                for(int i = 0; i < foils.getLength(); i++) {
                    Node n = foils.item(i);

                    String foilText = n.getTextContent();
                    String foilValue = n.getAttributes().getNamedItem("value").getTextContent();

                    log.finer("---Added Foil (Text: " + foilText + " | Value: "+ foilValue + " )");

                    if(i > 0) answerTemp += ",";

                    if(foilValue.equalsIgnoreCase("true") || foilValue.equalsIgnoreCase("1")) {
                        solutionVar = addAdditionalCASVar("\"" + foilText + "\"");
                        answerTemp += "[" + solutionVar + "," + foilValue + "]";
                    }else {
                        answerTemp += "[" + addAdditionalCASVar("\"" + foilText + "\"") + "," + foilValue + "]";
                    }

                    if(i == foils.getLength() -1) answerTemp += "]";

                    if(n.getAttributes().getNamedItem("location") != null && n.getAttributes().getNamedItem("location").getTextContent().equals("random")) {
                        randPermutation = true;
                    }
                }

                answer = addAdditionalCASVar(answerTemp);
                if(randPermutation) {
                    answer = addAdditionalCASVar("random_permutation("+answer+")");
                }

            }else {
                log.warning("No foil");
            }
        }else {
            log.warning("No foilgroup");
        }

    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {

        //Add input in questiontext
        question.addToQuestionText(inputString);

        //Add additional vars to questionvariables
        question.addToQuestionVariables(additionalCASVars);

        //INPUT-TAG
        Input input=new Input();
        input.setName(inputName);
        input.setType("radio");
        input.setTans(answer);
        input.setBoxsize(textlineSize);
        input.setMustverify(false);
        input.setShowvalidation(false);
        question.addInput(input);

        //NODE-TAG
        NodeMdl nodeMdl=new NodeMdl();
        nodeMdl.setAnswertest("AlgEquiv");
        nodeMdl.setSans(inputName);
        nodeMdl.setTans(solutionVar);

        addHintsToMdlQuestion(question,nodeMdl);
        question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

    }

}
