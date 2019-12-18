package lc2mdl.mdl.quiz;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class Quiz{

	private ArrayList<QuizElement> questions = new ArrayList<>();

	public void addQuizelement(QuizElement question){
		questions.add(question);

	}

	public void addAllQuizelements(ArrayList<QuizElement> list){
		questions.addAll(list);
	}
	
	public Element exportToDom(Document dom) {

		Element e=dom.createElement("quiz");
		
		for(QuizElement q: questions){
			e.appendChild(q.exportToDom(dom));
		}
		return e;
	}
}
