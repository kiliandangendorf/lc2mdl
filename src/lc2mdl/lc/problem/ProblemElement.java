package lc2mdl.lc.problem;

import lc2mdl.Prefs;
import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProblemElement {
	public static Logger log = Logger.getLogger(ProblemElement.class.getName());

	protected Problem problem;
	protected Node node;

	public ProblemElement(Problem problem, Node node){
		this.problem=problem;
		this.node=node;
	}

	/**
	 * Reads all expected data from node-member an remove them. 
	 */
	public abstract void consumeNode();
	/**
	 * Add converted values to given QuestionStack-object.
	 */
	public abstract void addToMdlQuestion(QuestionStack question);
	
	
	protected void removeNodesFromDOM(ArrayList<Node> nodesToRemove){
		for(Node n:nodesToRemove){
			removeNodeFromDOM(n);
		}
		nodesToRemove.clear();
	}
	protected void removeNodeFromDOM(Node n){
		n.getParentNode().removeChild(n);
	}

	protected void removeAttributeIfExist(Element e, String attr){
		if(e.hasAttribute(attr)){
			e.removeAttribute(attr);		
			log.finer("--removed attribut: "+attr);
		}

	}
	
	/**
	 * transforms LON-CAPA Outtext-elements (from any parent Element) into Moodle-STACK CAS-Text 
	 * @param text (LON-CAPA Outtext)
	 * @return converted CAS-Text
	 */
	protected String transformTextElement(String text){
		log.finer("-transform text");
		
		//VARS in {@ ... @}
		text=replaceVariables(text);
				
		//HTML-ELEMENTSs
		text=replaceHTMLTags(text);
		
		//LANGUAGEBLOCKS
		text=chooseOneLanguageBlock(text,Prefs.DEFAULT_LANG);

		//TRANSLATED
		text=chooseOneTranslated(text,Prefs.DEFAULT_LANG);

		//LATEX / MATH-EXPRESSION: <m>...</m> into \( ... \)  (<m eval="on">)
		text=replaceMathTags(text);

		//ESCAPE LEFT DOLLAR SIGNS
		if(text.contains("$")){
			log.warning("--still Dollar-signs in text. Replaced them by the String \"[DOLLAR-SIGN]\" (because Moolde don't like them)");
			text=text.replaceAll("\\$","[DOLLAR-SIGN]");
		}
			
		return text;
	}
	
	/**
	 * Searches string for first match with given regEx and return matching substring o string
	 * If there is no match this will return null 
	 */
	protected String getFirstMatchAsString(String string, String regEx){
		Matcher matcher=Pattern.compile(regEx).matcher(string);		
		while(matcher.find()){
			return matcher.group();
		}
		return null;
	}

	private String replaceVariables(String text){
		//VARS in {@ ... @}
		for(String var:problem.getPerlVars()){
			//only if it's exactly same name
			String varPat="\\$"+var+"(?![a-zA-Z0-9])";
			Matcher matcher=Pattern.compile(varPat).matcher(text);
			while(matcher.find()){
				String varString=matcher.group();
				log.finer("--replace "+varString+" with {@"+var+"@}");
				text=text.replaceAll(varPat,"{@"+var+"@}");
			}
		}
		return text;
	}
	
	private String replaceMathTags(String text){
		//LATEX / MATH-EXPRESSION: <m>...</m> into \( ... \)  (<m eval="on">)
		String leftMPat;
		String rightMPat;
		
		//TEX <m>$$ ... $$</m> into \[ ... \]
		leftMPat="<m {0,}(eval=\"on\"){0,1} {0,}> {0,}\\$\\$";
		rightMPat="\\$\\$ {0,}<\\/ {0,}m>";
//		Matcher matcherTex=Pattern.compile(leftMPat).matcher(text);
		text=replacePatternWithString(leftMPat,"\\\\[",text);
		text=replacePatternWithString(rightMPat,"\\\\]",text);
		
		//LaTeX <m>$ ... $</m> into \( ... \)		
		leftMPat="<m {0,}(eval=\"on\"){0,1} {0,}>\\${0,1}";
		rightMPat="\\${0,1}<\\/ {0,}m>";
		text=replacePatternWithString(leftMPat,"\\\\(",text);
		text=replacePatternWithString(rightMPat,"\\\\)",text);
		
		return text;
	}

	private String replaceHTMLTags(String text){
		
		HashMap<String,String> htmlReplacements=new HashMap<>();
		htmlReplacements.put("listing","pre");
		htmlReplacements.put("xmp","code");
		htmlReplacements.put("acronym","abbr");
		htmlReplacements.put("bgsound","audio");
		htmlReplacements.put("dir","ul");
		htmlReplacements.put("s","del");
		htmlReplacements.put("strike","del");
		htmlReplacements.put("tt","code");
		htmlReplacements.put("u","em");

		for(String tag:htmlReplacements.keySet()){
			String openTagPat="<"+tag+"[^>/]*>";
			text=replacePatternWithString(openTagPat,"<"+htmlReplacements.get(tag)+">",text);

			String closeTagPat="</[^>]*"+tag+">";
			text=replacePatternWithString(closeTagPat,"</"+htmlReplacements.get(tag)+">",text);

			String selfClosingTagPat="< {0,}"+tag+"[^>]*\\/ {0,}>";
			text=replacePatternWithString(selfClosingTagPat,"<"+htmlReplacements.get(tag)+"/>",text);
		}
		
		HashMap<String,String> htmlInfos=new HashMap<>();
		htmlInfos.put("applet","Use embed or object instead.");
		htmlInfos.put("frame","Either use iframe and CSS instead, or use server-side includes to generate complete pages with the various invariant parts merged in.");
		htmlInfos.put("frameset","Either use iframe and CSS instead, or use server-side includes to generate complete pages with the various invariant parts merged in.");
		htmlInfos.put("noframes","Either use iframe and CSS instead, or use server-side includes to generate complete pages with the various invariant parts merged in.");
		htmlInfos.put("isindex","Use an explicit form and text field combination instead.");
		htmlInfos.put("nextid","Use GUIDs instead.");
		htmlInfos.put("noembed","Use object instead of embed when fallback is necessary.");
		htmlInfos.put("plaintext","Use the \"text/plain\" MIME type instead.");
		htmlInfos.put("rb","Providing the ruby base directly inside the ruby element is sufficient; the rb element is unnecessary. Omit it altogether.");
		htmlInfos.put("basefont",null);
		htmlInfos.put("big",null);
		htmlInfos.put("blink",null);
		htmlInfos.put("center",null);
		htmlInfos.put("font",null);
		htmlInfos.put("marquee",null);
		htmlInfos.put("multicol",null);
		htmlInfos.put("nobr",null);
		htmlInfos.put("spacer",null);
		htmlInfos.put("wbr",null);
		for(String tag:htmlInfos.keySet()){
			String tagPat="<"+tag+"[^>]*>";
			Matcher matcher=Pattern.compile(tagPat).matcher(text);
			String info=htmlInfos.get(tag);
			while(matcher.find()){
				String s=matcher.group();
				log.warning("--found obsolete HTML-tag: "+s+((info==null)?"":"("+info+", acc. https://www.w3.org/TR/2010/WD-html5-20100304/obsolete.html)"));
			}
		}
		
		return text;
	}

	private String replacePatternWithString(String regExPattern,String replacement,String text){
		Matcher matcher=Pattern.compile(regExPattern).matcher(text);
		while(matcher.find()){
			String match=matcher.group();
			log.finer("--replace "+match+" with "+replacement);
			text=text.replaceFirst(regExPattern,replacement);
		}
		return text;
	}
	
	private String chooseOneLanguageBlock(String text,String defaultLang){
		String langBlockPat="< {0,}languageblock[\\s\\S]*?\\/ {0,}languageblock {0,}>";
		Matcher matcher=Pattern.compile(langBlockPat).matcher(text);
		while(matcher.find()){
			String langBlock=matcher.group();
			String textInDefaultLang="";
			try{
				Document dom=XMLParser.parseString2DOM(langBlock);
				Element langElement=(Element)dom.getElementsByTagName("languageblock").item(0);
				if(langElement.hasAttribute("include")){
					if(langElement.getAttribute("include").contains(defaultLang)){
						//text in defaultLang
						textInDefaultLang=langElement.getTextContent();
						log.finer("--found \""+defaultLang+"\"-languageblock");
					}else{
						//text in different language
						textInDefaultLang="<!-- lc2mdl: languageblock for different language found: "+langBlock+" -->";
						log.finer("--found languageblock different to \""+defaultLang+"\". Put in comments.");						
					}
				}else{//languageblock has attribute exclude
					if(!langElement.getAttribute("exclude").contains(defaultLang)){
						//text in defaultLang
						textInDefaultLang=langElement.getTextContent();
						log.finer("--found \""+defaultLang+"\"-languageblock");
					}else{
						//text in different language
						textInDefaultLang="<!-- lc2mdl: languageblock for different language found: "+langBlock+" -->";
						log.finer("--found languageblock different to \""+defaultLang+"\". Put in comments.");						
					}
				}
				text=text.replaceFirst(langBlockPat,textInDefaultLang);
			}catch(Exception e){
				log.warning("--unable to read languageblock.");
			}
		}
		return text;
	}

	private String chooseOneTranslated(String text,String defaultLang){
		String translatedBlockPat="< {0,}translated[\\s\\S]*?\\/ {0,}translated {0,}>";
		Matcher matcher=Pattern.compile(translatedBlockPat).matcher(text);
		while(matcher.find()){
			String translatedBlock=matcher.group();
			String textInDefaultLang=null;
			String defaultText=null;
			try{
				Document dom=XMLParser.parseString2DOM(translatedBlock);
				NodeList langs=dom.getElementsByTagName("lang");
				for(int i=0;i<langs.getLength();i++){
					Element lang=(Element)langs.item(i);
					if(lang.getAttribute("which").equals(defaultLang)){
						//text in defaultLang
						textInDefaultLang=lang.getTextContent();
					}
					if(lang.getAttribute("which").equals("default")){
						defaultText=lang.getTextContent();
					}
				}
				String outtext="";
				if(textInDefaultLang!=null){
					//text in defaultLang
					outtext=textInDefaultLang;
					log.finer("--found \""+defaultLang+"\" in translated-block");
				}else{
					//text not in defaultLang
					if(defaultText!=null){
						//default text 
						outtext=defaultText;
						log.finer("--found default text in translated-block");
					}else{
						//no text found
						log.warning("--found no text to language \""+defaultLang+"\"");						
					}
				}
				outtext="<!-- lc2mdl: chose best match in translated-block: "+translatedBlock+" -->"+outtext;
				text=text.replaceFirst(translatedBlockPat,outtext);
			}catch(Exception e){
				log.warning("--unable to read translated-block.");
				e.printStackTrace();
			}
		}
		return text;
	}
	
	//================================================================================
    // Getter and Setter
    //================================================================================			
	public Problem getProblem() {
		return problem;
	}

	public void setProblem(Problem problem) {
		this.problem = problem;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

}
