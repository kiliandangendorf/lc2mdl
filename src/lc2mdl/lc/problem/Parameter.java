package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Parameter extends ProblemElement {

    private String comment;

    public Parameter(Problem problem, Node node){
        super(problem,node);
    }

    @Override
    public void consumeNode() {
        log.finer("-found parameter");

        Element e= (Element)node;

        comment = "The parameter ";
        if (e.hasAttribute("description")){
            comment += e.getAttribute("description");
            e.removeAttribute("description");
        }
        comment += " is set to ";
        if (e.hasAttribute("default")){
            comment += e.getAttribute("default");
            e.removeAttribute("default");
        }

        removeAttributeIfExist(e,"type");
        removeAttributeIfExist(e,"display");
        removeAttributeIfExist(e,"id");
        removeAttributeIfExist(e,"name");

        if (e.hasAttributes()) {
             log.warning("-still unkwown attributes in parameter");
        }


    }

    @Override
    public void addToMdlQuestion(QuestionStack question) {
        question.addComment(comment);
    }
}
