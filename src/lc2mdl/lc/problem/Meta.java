package lc2mdl.lc.problem;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;

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
                    
                    //remove backslash in front of reference
                    basedOn=basedOn.replace("\\%","%");
                    //use java to decode URL references like "\%2d" to "-" etc.
                    try{
						basedOn=URLDecoder.decode(basedOn, "UTF-8");
					}catch(UnsupportedEncodingException ex){
						// worst case, it will stay with references 
					}
                    					
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
