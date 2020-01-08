package lc2mdl.lc.problem.response;

import lc2mdl.Prefs;
import lc2mdl.lc.problem.Problem;
import lc2mdl.mdl.quiz.QuestionEssay;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class EssayResponse extends Response {

    private boolean file=false;
    private String fileExt ="";

    public EssayResponse(Problem problem, Node node) {
        super(problem, node);
        questionType = "essay";
        problem.addQuestionType(questionType);
    }

    @Override
    public void consumeNode() {

        log.finer("found essay response");
        Element e = (Element)node;

        // attributes
        consumeIdAndName(e);
        if (e.hasAttributes()){
            log.warning("-still unknown attributes in response.");
        }

        //RESPONSEPARAM
        consumeEssayResponseParameter(e);
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
        String text="";
        if (file) { text= Prefs.ESSAY_TEXT_FILE_ESSAY; }
        else { text = Prefs.ESSAY_TEXT_FIELD_ESSAY; }
        question.addToQuestionText(text);

    }

    public void addToMdlQuestion(QuestionEssay questionEssay){
        if (file) questionEssay.setParameterForFile();
    }

    private void consumeEssayResponseParameter(Element e){
		ArrayList<Node> nodesToRemove=new ArrayList<>();

		NodeList responseparams=e.getElementsByTagName("responseparam");
		if(responseparams.getLength()>0)log.finer("-found responseparam");
		for(int i=0;i<responseparams.getLength();i++){
			Element responseparam=(Element)responseparams.item(i);
			if(responseparam.getAttribute("type").equals("string_fileext")) {
			    file = true;
                fileExt = responseparam.getAttribute("default");
                nodesToRemove.add(responseparam);
            }

		}
		removeNodesFromDOM(nodesToRemove);
	}

}
