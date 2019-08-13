package lc2mdl.mdl.quiz;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Quiz{

	private ArrayList<Question> questions = new ArrayList<>();

	public void addQuestion(Question question){
		questions.add(question);
	}
	
	public Element exportToDom(Document dom) {

		Element e=dom.createElement("quiz");
		
		for(Question q: questions){
			e.appendChild(q.exportToDom(dom));
		}
		return e;
	}
}
