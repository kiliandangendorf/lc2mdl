package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.Input;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


//Gerrit
public class RadiobuttonResponse extends ChoiceResponse {

    public RadiobuttonResponse(Problem problem, Node node) {
        super(problem, node);
    }


    @Override
    public void consumeNode() {
        log.finer("radiobuttonresponse:");

         Element e=(Element)node;
         checkBox=true;


        // Foils
        consumeFoils(e);

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

        //Add input in questiontext
        question.addToQuestionText(inputString);

        //Add additional vars to questionvariables
        question.addToQuestionVariables(additionalCASVars);

        //INPUT-TAG
        Input input=new Input();
        input.setName(inputName);
        input.setType("radio");
        input.setTans(answerdisplay);
        input.setBoxsize(textlineSize);
        input.setMustverify(false);
        input.setShowvalidation(false);
        question.addInput(input);

        //NODE-TAG - only a single value expected here
        NodeMdl nodeMdl=new NodeMdl();
        nodeMdl.setAnswertest("AlgEquiv");
        nodeMdl.setSans(inputName);
        nodeMdl.setTans(answer);

        addHintsToMdlQuestion(question,nodeMdl);
        question.addNodeToCurrentPrtAndSetNodeLink(nodeMdl);

    }

}
