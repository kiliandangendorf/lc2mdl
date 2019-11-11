package lc2mdl.lc.problem.response.hints;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.response.NumericalResponse;
import lc2mdl.mdl.quiz.NodeMdl;
import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.util.ConvertAndFormatMethods;

public class NumericalHint extends NumericalResponse implements ConditionalHint{
	
	private boolean link;
		
	public NumericalHint(Problem problem,Node node, boolean link){
		super(problem,node);
		this.link=link;
	}

	@Override
	public void addToMdlQuestion(QuestionStack question){}
	
	
	@Override
	public void consumeNode(){
		log.finer("-numericalhint:");
		//HINT

		Element e=(Element)node;

		//ATTRIBUTES
		if(e.hasAttribute("answer")){
			answer=e.getAttribute("answer");
			log.finer("-found answer: "+answer);
			if(answer.charAt(0)=='$'){
				answer=answer.substring(1);
			}else{
				//if not $ the first symbol, then create a var in questionvariables and reference here
				answer=addAdditionalCASVar(answer);
			}
			e.removeAttribute("answer");
		}else{
			log.warning("--no answer found in hint");
		}
		
		if(e.hasAttribute("unit")){
			unit=e.getAttribute("unit");
			e.removeAttribute("unit");
			log.finer("--found unit: "+unit);
		}		
		
		consumeIdAndName(e);

		if(e.hasAttributes())log.warning("--still unknown attributes in hint.");

		//RESPONSEPARAM
		consumeResponseParameter(e);
		
		
		
		//HINTPARTS
		Node hintgroup=node.getParentNode();
		if(hintgroup==null || hintgroup.getNodeType()!=Node.ELEMENT_NODE){
			log.warning("--no parent elementnode was found.");
			return;
		}
		Element p=(Element)hintgroup;
		NodeList hintparts=p.getElementsByTagName("hintpart");
		for(int i=0;i<hintparts.getLength();i++){
			Element hintpart=(Element)hintparts.item(i);
			if(hintpart.getAttribute("on").equals(name)){
				NodeList hinttexts=hintpart.getElementsByTagName("outtext");
				for(int j=0;j<hinttexts.getLength();j++){
					Element hinttext=(Element)hinttexts.item(j);
					String hint=hinttext.getTextContent();
					addHinttext(hint,true);
					hinttext.setTextContent(null);
				}

				removeAttributeIfExist(hintpart,"on");
				if(hintpart.hasAttributes())log.warning("--still unknown attributes in hintpart.");
			}
		}
	}

	@Override
	public void addHintNodeToMdlQuestion(QuestionStack question,NodeMdl parentNode){
		
		//Add additional vars to questionvariables
		question.addToQuestionVariables(additionalCASVars);
		
		//NODE-TAG
		NodeMdl hintnode=new NodeMdl();
		if(relativeTol)hintnode.setAnswertest("NumRelative");
		else hintnode.setAnswertest("NumAbsolute");
		hintnode.setTestoptions(ConvertAndFormatMethods.double2StackString(tolerance));
		
		hintnode.setSans(parentNode.getSans());		
		hintnode.setTans(answer);
		hintnode.setTruefeedback(correcthinttext);		
		
		question.addHintNodeToNode(parentNode,hintnode,link);	
	}
}
