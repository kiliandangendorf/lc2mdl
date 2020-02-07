package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Meta extends ProblemElement {

    String basedOn = "";
    Boolean isBasedOn = false;

    public Meta(Problem problem, Node node) {
        super(problem, node);
    }

    @Override
    public void consumeNode() {
        log.finer("--found meta information");

        Element e = (Element)node;

        if (e.hasAttribute("name")) {
            String name = e.getAttribute("name");
            if (name.equals("isbasedonres")){
                if (e.hasAttribute("content")){
                    basedOn =  e.getAttribute("content");
                    basedOn = basedOn.replaceAll("\\\\%2f","/");
                    basedOn = basedOn.replaceAll("\\\\%2d"," ");
                    basedOn = basedOn.replaceAll("\\\\%2e",".");
                    isBasedOn = true;
                }
            else {
                log.finer("--found meta information: "+name+" and ignore it.");
                }
            }
        }else{
            log.warning("--found meta without name");
        }
        removeAttributeIfExist(e,"name");
        removeAttributeIfExist(e,"content");


    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {
        if (isBasedOn) {
            String quNote = question.getQuestionnote();
            quNote = quNote + ", " + System.lineSeparator() + "This question is based on the LON-CAPA problem " + basedOn;
            question.setQuestionnote(quNote);
        }
    }

    @Override
    public void addToMdlQuestion(Question question) {
        super.addToMdlQuestion(question);
    }
}
