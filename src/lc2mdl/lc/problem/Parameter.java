package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Parameter extends ProblemElement {

    private String comment;
    private String variable = "lcparam";
    private String type = "string";

    public Parameter(Problem problem, Node node){
        super(problem,node);
    }

    @Override
    public void consumeNode() {
        log.finer("-found parameter");

        Element e= (Element)node;

        if (e.hasAttribute("name")){
            variable = System.lineSeparator()+variable+"_"+e.getAttribute("name") + ": ";
            e.removeAttribute("name");
        }
        if(e.hasAttribute("type")){
            type = e.getAttribute("type");
            e.removeAttribute("type");
        }

        comment = "The parameter ";
        if (e.hasAttribute("description")){
            comment += e.getAttribute("description");
            e.removeAttribute("description");
        }
        comment += " is set to ";
        if (e.hasAttribute("default")){
            String text = e.getAttribute("default");
            comment += text;
            if (type.equals("string")) {
                text = "\""+text+"\"";}
            variable += text;
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
    public void addToMdlQuestionStack(QuestionStack question) {
        question.addToQuestionVariables(variable);
        question.addComment(comment);
    }
}
