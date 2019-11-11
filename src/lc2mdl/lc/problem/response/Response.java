package lc2mdl.lc.problem.response;

import lc2mdl.lc.problem.Problem;
import lc2mdl.lc.problem.ProblemElement;
import lc2mdl.lc.problem.response.hints.ConditionalHint;
import lc2mdl.lc.problem.response.hints.NumericalHint;
import lc2mdl.lc.problem.response.hints.StringHint;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public abstract class Response extends ProblemElement{

	protected String inputName;
	protected String inputString;
	
	
	protected boolean isTextline=true;
	protected Integer textlineSize=15; // mdl BoxSize

	protected String id;
	protected String name;

	protected boolean relativeTol=true;
	protected Double tolerance=0.05;//if there is a tolerance according: https://stack2.maths.ed.ac.uk/demo2018/question/type/stack/doc/doc.php/Authoring/Answer_tests_numerical.md
	
	protected String correcthinttext="";
	protected String incorrecthinttext="";
	protected ArrayList<ConditionalHint> hints=new ArrayList<>();
	
	protected String additionalCASVars="";
	
	public Response(Problem problem,Node node){
		super(problem,node);
		inputName="ans"+(problem.getIndexFromResponse(this)+1);
		inputString=" [[input:"+inputName+"]] [[validation:"+inputName+"]] ";
	}

	/**
	 * Generates unique variable name and lets problem-object know.
	 * Also builds Maxima expression which assigns given String as value to the new variable.
	 * Puts Maxima expression to additionalCASVars-member.
	 * @param value of new variable
	 * @return name of new variable
	 */
	protected String addAdditionalCASVar(String value){
		String var0="lc2mdlvar"+problem.getIndexFromResponse(this);
		String uniqueVar=var0;
		
		//check, if var-name already exists in problem
		int i=0;
		while(!problem.addVar(uniqueVar)){
			uniqueVar=var0+(i++);
		}
		log.finer("--defined unique var: "+uniqueVar+" with: "+value);
		String definition=uniqueVar+": "+value;
		additionalCASVars+=System.lineSeparator()+definition+System.lineSeparator();
		return uniqueVar;
	}
	
	/**
	 * Reads all (conditional) hints from given element.
	 */
	protected void consumeHintgroups(Element e){
		ArrayList<Node> nodesToRemove=new ArrayList<>();

		NodeList hintgroups=e.getElementsByTagName("hintgroup");
		for(int i=0;i<hintgroups.getLength();i++){
			if(!(hintgroups.item(i).getNodeType()==Node.ELEMENT_NODE))continue;
			Element hintgroup=(Element)hintgroups.item(i);
			
			//Acceptance: if not specified, hints will be given on wrong inputs 
			boolean correct=false;
			if(hintgroup.hasAttribute("showoncorrect")){
				if(hintgroup.getAttribute("showoncorrect").equals("no")){
					correct=false;
				}else{
					correct=true;
				}
				removeAttributeIfExist(hintgroup,"showoncorrect");
			}
			//if there's an empty hintgroup -->remove
			if(!hintgroup.hasChildNodes()){
				nodesToRemove.add(hintgroup);
				continue;
			}
			
			NodeList hintparts=hintgroup.getChildNodes();
			for(int j=0;j<hintparts.getLength();j++){
				if(!(hintparts.item(i).getNodeType()==Node.ELEMENT_NODE))continue;
				Element hint=(Element)hintparts.item(j);
				switch(hint.getTagName()){
					case "numericalhint":
						NumericalHint nh = new NumericalHint(problem, hint, correct);
						nh.consumeNode();
						this.hints.add(nh);
						break;
					case "stringhint":
						StringHint sh = new StringHint(problem, hint,correct);
						sh.consumeNode();
						this.hints.add(sh);
						break;
					case "hintpart":
						if(hint.getAttribute("on").equals("default")){
							//remove attributes, so simplifier will remove it
							removeAttributeIfExist(hint,"on");
							removeAttributeIfExist(hint,"id");
							
							NodeList hinttexts=hint.getElementsByTagName("outtext");
							for(int k=0;k<hinttexts.getLength();k++){
								String hinttext=hinttexts.item(k).getTextContent();
								addHinttext(hinttext,correct);
								nodesToRemove.add(hinttexts.item(k));
							}
						}
						break;
					case "outtext":
						String hinttext=hint.getTextContent();
						addHinttext(hinttext,correct);
						nodesToRemove.add(hint);
						break;
					default:
						//TODO: add more conditional hint types here
						log.warning("-unknown hint ("+hint.getTagName()+")");
						break;
				}
			}
		}
		removeNodesFromDOM(nodesToRemove);
	}
	
	protected void addHinttext(String hinttext, boolean correct){
		if(hinttext==null)return;
		log.finer("-found "+correct+" hinttext");
		hinttext=transformTextElement(hinttext);
		if(correct)correcthinttext+=System.lineSeparator()+hinttext.trim();
		else incorrecthinttext+=System.lineSeparator()+hinttext.trim();
	}
	
	protected void consumeIdAndName(Element e){
		this.id=e.getAttribute("id");
		removeAttributeIfExist(e,"id");
		this.name=e.getAttribute("name");
		removeAttributeIfExist(e,"name");
	}
	
	/**
	 * Reads textline or textfield elements.
	 */
	protected void consumeTextline(Element e){
		//textline
		NodeList textlines=e.getElementsByTagName("textline");
		if(textlines.getLength()>1)log.warning("-more than one textline. Will work on only first one.");
		if(textlines.getLength()>0){
			log.finer("-found textline");
			Element textline=(Element)textlines.item(0);
			if(textline.hasAttribute("size")){
				textlineSize=Integer.parseInt(textline.getAttribute("size"));
				log.finer("--found size: "+textlineSize);
			}	
			textline.removeAttribute("size");
			textline.removeAttribute("readonly");
		}else{
			//textfield
			isTextline=false;
			NodeList textfields=e.getElementsByTagName("textfield");
			if(textfields.getLength()>1)log.warning("-more than one textfield. Will work on only first one.");
			if(textfields.getLength()>0){
				log.finer("-found textfield");
				Element textfield=(Element)textfields.item(0);
				if(textfield.hasAttribute("cols")){
					textlineSize=Integer.parseInt(textfield.getAttribute("cols"));
					log.finer("--found size: "+textlineSize);
				}
				textfield.removeAttribute("rows");
				textfield.removeAttribute("cols");
				textfield.removeAttribute("readonly");		
			}
		}
	}
	
	/**
	 * Reads response parameter from given element.
	 */
	protected void consumeResponseParameter(Element e){
		ArrayList<Node> nodesToRemove=new ArrayList<>();

		NodeList responseparams=e.getElementsByTagName("responseparam");
		if(responseparams.getLength()>0)log.finer("-found responseparam");
		for(int i=0;i<responseparams.getLength();i++){
			Element responseparam=(Element)responseparams.item(i);
			switch(responseparam.getAttribute("type")){
				//TOLERANCE
				case "tolerance":
					String tol=responseparam.getAttribute("default");
					//check if relative or absolute tolerance
					if(tol.charAt(tol.length()-1)=='%'){
						//remove %-symbol
						tol=tol.substring(0,tol.length()-1);
						relativeTol=true;
					}else{
						relativeTol=false;
					}
					if(tol==null)tol="0";
					try{
						tolerance=Double.parseDouble(tol);
					}catch(Exception ex){
						log.warning("--could not read tolerance. Set to 0. "+tol);
						tolerance=0.0;
					}
					tolerance=0.01*tolerance;
					nodesToRemove.add(responseparam);
					log.finer("--found tolerance: "+tolerance+" ("+(relativeTol?"rel.":"abs.")+")");
					break;
				case "int_range,0-16":
					String sig=responseparam.getAttribute("default");
					log.finer("--found significant figures: \""+sig+"\"");						
					if(sig.equals("0,15")){
						nodesToRemove.add(responseparam);
						log.finer("---removed, because won't affect anyway.");						
					}else{
						log.warning("---different significant firgues. Check if it is required. If not remove it.");
					}
					break;
				default:
					log.warning("--unknown responseparam type: "+responseparam.getAttribute("type"));
					break;
			}
		}
		removeNodesFromDOM(nodesToRemove);
	}
}
