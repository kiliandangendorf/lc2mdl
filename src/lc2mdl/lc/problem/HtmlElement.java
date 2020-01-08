package lc2mdl.lc.problem;

import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HtmlElement extends ProblemElement{

	public static final boolean OPEN=true;
	public static final boolean CLOSE=false;
	private boolean open;
	private String htmlString;
	
	public HtmlElement(Problem problem,Node node, boolean open){
		super(problem,node);
		this.open=open;
	}

	@Override
	public void consumeNode(){
		log.finer("HTML element:");
		
		//just recreate HTML-tag 
		if(open){
			htmlString="<"+node.getNodeName();
			NamedNodeMap attributes=node.getAttributes();
			for(int i=0;i<attributes.getLength();i++){
				Node attr=attributes.item(i);
				
				String attr_name=attr.getNodeName();
				String attr_value=attr.getNodeValue();
				htmlString+=" "+attr_name+"=\""+attr_value+"\"";
			}
			htmlString+=">";

			//remove all attributes
			while(node.getAttributes().getLength()>0){
				Node attr=node.getAttributes().item(0);
				node.getAttributes().removeNamedItem(attr.getNodeName());
			}
			
		}else{
			htmlString="</"+node.getNodeName()+">";
		}
		log.finer("-create HTML-tag: "+htmlString);
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){
		//Add HTML-tag to questiontext
		question.addToQuestionText(htmlString);
	}

	public void addToMdlQuestion(Question question){
		//Add HTML-tag to questiontext
		question.addToQuestionText(htmlString);
	}


}
