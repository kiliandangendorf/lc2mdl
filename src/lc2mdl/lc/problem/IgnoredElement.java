package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class IgnoredElement extends ProblemElement {

 	public IgnoredElement(Problem problem, Node node){
		super(problem,node);
	}


    @Override
    public void consumeNode() {
        Element e =(Element)node;
		String name = e.getTagName();
		log.finer("consume ignored element: "+name);

		if(!e.hasChildNodes()) {
            removeNodeFromDOM(e);
        }
    }

    @Override
    public void addToMdlQuestionStack(QuestionStack question) {

    }
}
