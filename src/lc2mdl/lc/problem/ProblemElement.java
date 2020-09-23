package lc2mdl.lc.problem;

import lc2mdl.Prefs;
import lc2mdl.mdl.quiz.Question;
import lc2mdl.mdl.quiz.QuestionStack;
import lc2mdl.util.ConvertAndFormatMethods;
import lc2mdl.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ProblemElement {
	public static Logger log = Logger.getLogger(ProblemElement.class.getName());

	protected Problem problem;
	protected Node node;
	protected String questionType="all";


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
	public abstract void addToMdlQuestionStack(QuestionStack question);
	public  void addToMdlQuestion(Question question){}

	
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
		return transformText(text,false);
	}

	/**
	 * transforms LON-CAPA Outtext-elements (from any parent Element) into Moodle-STACK CAS-Textvariables.
	 * Makes also sure that text is in quotes
	 * @param text (LON-CAPA Outtext)
	 * @return converted CAS-Text
	 */
	protected String transformTextVariable(String text){
		log.finer("-transfom text variable");
		return transformText(text,true);
	}
	
	protected String transformText(String text, boolean isVariable){
		//gnuplot
		text=replaceGnuPlot(text);

		// img tags
		text = replaceImages(text);

		//LATEX / MATH-EXPRESSION: <m>...</m> into \( ... \)  (<m eval="on">)
		text=replaceMathTags(text,isVariable);

		//HTML-ELEMENTSs
		text=replaceHTMLTags(text);

		//LANGUAGEBLOCKS
		text=chooseOneLanguageBlock(text,Prefs.DEFAULT_LANG);

		//TRANSLATED
		text=chooseOneTranslated(text,Prefs.DEFAULT_LANG);

		//VARS 
		if(isVariable){
			//TEXT-VARIABLES
			//remove {@ and @} from transformed variables
			text = replacePatternWithString("\\{@","",text);
			text = replacePatternWithString("@\\}","",text);
	
			//VARS use sconcat(
			//puts quotes around if there are none
			text=replacesVariablesInTextVariables(text);
			//remove CR/LF in maxima strings 
			if(!Prefs.ALLOW_MULTILINE_MAXIMA_STRINGS)text=ConvertAndFormatMethods.removeCR(text);
		}else{
			//VARS in {@ ... @}
			text=replaceVariables(text);			
		}

		//ESCAPE LEFT DOLLAR SIGNS
		if(text.contains("$")){
			log.warning("--still Dollar-signs in text. Replaced them by the String \"[DOLLAR-SIGN]\" (because Moodle doesn't like them)");
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

	/**
	 * makes also sure that text is in quotes
	 */
	private String replacesVariablesInTextVariables(String text){

		String buf = text;
		//make sure this string starts and ends with "
		if (!buf.startsWith("\"")){ 
			buf = "\""+buf; 
		}
		if (!buf.endsWith("\"")||(buf.length()==1)) {
			buf += "\"";
		}
		text = buf;
		
		for(String var:problem.getPerlVars()){
			//arrays
			String arrayBeginPat="\\$"+var+"\\[";
			Matcher matcher=Pattern.compile(arrayBeginPat).matcher(buf);
			while(matcher.find()){
				int openBrace=matcher.end()-1;
				int closingBrace=ConvertAndFormatMethods.findMatchingParentheses(buf,openBrace,'[',']')-1;
				if(closingBrace<0){
					log.warning("--found array access without closing parenthesis");
					continue;
				}
				String varString=buf.substring(matcher.start(),closingBrace+1);
				//remove first $-sign
				String replacement=varString.substring(1);
				replacement="\", "+replacement+" ,\"";
				log.finer("--replace "+varString+" with "+replacement);				
				buf=buf.replaceFirst(Pattern.quote(varString),Matcher.quoteReplacement(replacement));
				matcher=Pattern.compile(arrayBeginPat).matcher(buf);
			}
			
			//vars
			String varPat="\\$"+var+"(?=\\b)";
			matcher=Pattern.compile(varPat).matcher(buf);
			StringBuffer sb=new StringBuffer();			
			while(matcher.find()){
				String varString=matcher.group();
				String replacement = varString.substring(1);
				replacement="\", "+replacement+" ,\"";
				log.finer("--replace "+varString+" with "+replacement);
				matcher.appendReplacement(sb,replacement);				
			}			
			matcher.appendTail(sb);
			buf=sb.toString();

		}

		//add sconcat(...) if there are multiple vars after another (if "," was added above)
		if (!buf.equals(text)) {
			//remove beginning ",
			if (buf.startsWith("\", ")) {
				buf = buf.substring(3);
			}
			//remove trailing ,"
			if (buf.endsWith(" ,\"")) {
				buf = buf.substring(0, buf.length() - 3);
			}

			//remove empty Strings in buf ,"",
			buf=buf.replaceAll(",\"\",",",");		
			//remove beginning "",
			if (buf.startsWith("\"\", ")) {
				buf = buf.substring(4);
			}
			//remove trailing ,""
			if (buf.endsWith(" ,\"\"")) {
				buf = buf.substring(0, buf.length() - 4);
			}
			
			buf = "sconcat(" +buf+ ")";
		}
		
		return buf;
	}

	private String replaceVariables(String text){
		//VARS in {@ ... @}
		
		//ARRAYS
		for(String var:problem.getPerlVars()){
			//make sure it's exactly same name
			
			//arrays $a[...] -->{@ a[...] @}
			String arrayBeginPat="\\$"+var+"\\[";
			Matcher matcher=Pattern.compile(arrayBeginPat).matcher(text);
			while(matcher.find()){
				int openBrace=matcher.end()-1;
				int closingBrace=ConvertAndFormatMethods.findMatchingParentheses(text,openBrace,'[',']')-1;
				if(closingBrace<0){
					log.warning("--found array access without closing parenthesis");
					continue;
				}
				
				String varString=text.substring(matcher.start(),closingBrace+1);
				//remove first $-sign
				String replacement=varString.substring(1);
								
				//replace vars within array-index
				//{@ a[$i] @} -->{@ a[i] @}
				if(replacement.contains("$")){
					for(String innerVar:problem.getPerlVars()){
						String innerVarPat="\\$"+innerVar+"(?=\\b)";
						Matcher innerMatcher=Pattern.compile(innerVarPat).matcher(replacement);
						StringBuffer sb=new StringBuffer();
						while(innerMatcher.find()){
							String innerString=innerMatcher.group();
							//remove $
							String innerReplacement = innerString.substring(1);
//							log.finer("--replace "+innerString+" with "+innerReplacement);
							innerMatcher.appendReplacement(sb,innerReplacement);
						}
						innerMatcher.appendTail(sb);
						replacement=sb.toString();
					}
				}
				
				replacement="{@ "+replacement+" @}";
				log.finer("--replace "+varString+" with "+replacement);
				text=text.replaceFirst(Pattern.quote(varString),Matcher.quoteReplacement(replacement));
				matcher=Pattern.compile(arrayBeginPat).matcher(text);
			}
		}

		//vars $a --> {@ a @} (but no arrays)
		for(String var:problem.getPerlVars()){
			String varPat="\\$"+var+"(?=\\b)";
			Matcher matcher=Pattern.compile(varPat).matcher(text);
			StringBuffer sb=new StringBuffer();			
			while(matcher.find()){
				String varString=matcher.group();
				String replacement = varString.substring(1);
				replacement="{@ "+replacement+" @}";
				log.finer("--replace "+varString+" with "+replacement);
				matcher.appendReplacement(sb,replacement);				
			}
			matcher.appendTail(sb);
			text=sb.toString();
		}
		return text;
	}

	private String replaceGnuPlot(String text) {
		String gnuplotPat = "< {0,}gnuplot[\\s\\S]*?\\/ {0,}gnuplot {0,}>";
		Matcher matcher = Pattern.compile(gnuplotPat).matcher(text);
		StringBuffer sb=new StringBuffer();
		while (matcher.find()) {
			String gnuString = matcher.group();
			Gnuplot gnuplotEl = new Gnuplot(problem, node, gnuString);
			gnuplotEl.consumeNode();
			String repalcement=gnuplotEl.getPlotString();
			matcher.appendReplacement(sb,Matcher.quoteReplacement(repalcement));
//			text=text.replace(gnuString, gnuplotEl.getPlotString());
		}
		matcher.appendTail(sb);
		text=sb.toString();
		return text;
	}

	private String replaceImages(String text){
		String imgPat = "< {0,}img[^>]*\\/ {0,}>";
		Matcher matcher = Pattern.compile(imgPat).matcher(text);
		while (matcher.find()) {
			String imgString = matcher.group();
			Image imageEl = new Image(problem, node, imgString);
			imageEl.consumeNode();
			text.replace(imgString, imageEl.getImgString());
		}
		return text;

	}

	private String replaceMathTags(String text, boolean isVariable){
		//LATEX / MATH-EXPRESSION: <m>...</m> into \( ... \)  (<m eval="on">)
		List<String> leftPat = new ArrayList<String>();
		List<String> rightPat = new ArrayList<String>();

		String addbackslashes ="";
//		if (isVariable) { addbackslashes ="\\\\"; } //doubling below

		//Note: one backslash (literal) in text means 4 in java-regex (1. and 3. for escaping the following backslash. It remains one literal backslash) 
		
		//"on" and "off" should be already replaced by lc2mdltext#
		String mathBeginTag="<m(\\s+eval=(\"on\"|\"off\"|lc2mdltext\\d+))?\\s*>";
		
		// TEX all with begin / end e.g. eqnarray*,equation*
		leftPat.add(mathBeginTag+"\\s{0,}\\\\begin");
		rightPat.add("\\*\\}\\s{0,}<\\/\\s{0,}m>");

		for (String s: leftPat){
//			text=replacePatternWithString(s,addbackslashes+"\\\\begin",text);
			text=replacePatternWithString(s,addbackslashes+"\\begin",text);
		}
		for (String s: rightPat) {
			text = replacePatternWithString(s, addbackslashes+"\\*"+addbackslashes+"\\}", text);
		}

		leftPat.clear();
		rightPat.clear();

		//TEX all displaymath e.g.<m>$$ ... $$</m> into \[ ... \]
		leftPat.add(mathBeginTag+"\\s{0,}\\$\\$");
		rightPat.add("\\$\\$\\s{0,}<\\/\\s{0,}m>");

		//Yes, 6 backslashes. 4 for the one literal and two for the [
		leftPat.add(mathBeginTag+"\\s{0,}\\\\\\[");
		rightPat.add("\\\\\\]\\s{0,}<\\/\\s{0,}m>");

		for (String s: leftPat){
//			text=replacePatternWithString(s,addbackslashes+"\\\\[",text);
			text=replacePatternWithString(s,addbackslashes+"\\[",text);
		}
		for (String s: rightPat) {
//			text = replacePatternWithString(s, addbackslashes+"\\\\]", text);
			text = replacePatternWithString(s, addbackslashes+"\\]", text);
		}

		leftPat.clear();
		rightPat.clear();

		//LaTeX all other math into \( ... \)
		leftPat.add(mathBeginTag+"\\${0,1}");
		rightPat.add("\\${0,1}\\s{0,}<\\/\\s{0,}m>");

		leftPat.add("<algebra\\s{0,}>\\s{0,}\\${0,1}");
		rightPat.add("\\${0,1}\\s{0,}<\\/ {0,}algebra>");

		leftPat.add("<tex\\s{0,}{0,}>\\s{0,}\\${0,1}");
		rightPat.add("\\${0,1}\\s{0,}<\\/\\s{0,}tex>");

		for (String s: leftPat){
//			text=replacePatternWithString(s,addbackslashes+"\\\\(",text);
			text=replacePatternWithString(s,addbackslashes+"\\(",text);
		}
		for (String s: rightPat){
//			text=replacePatternWithString(s,addbackslashes+"\\\\)",text);
			text=replacePatternWithString(s,addbackslashes+"\\)",text);
		}

		//double backslashes in maxima string
		if (isVariable)text=text.replace("\\","\\\\");
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

	/**
	 * Note replacement String may not contain regex-group identifiers. This method uses Matcher.quoteReplacement
	 */
	private String replacePatternWithString(String regExPattern,String replacement,String text){
		Matcher matcher=Pattern.compile(regExPattern).matcher(text);
		while(matcher.find()){
			String match=matcher.group();
			log.finer("--replace "+match+" with "+replacement);
			text=text.replaceFirst(regExPattern,Matcher.quoteReplacement(replacement));
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
						log.finer(textInDefaultLang);
					}else{
						//text in different language
						textInDefaultLang="<!-- lc2mdl: languageblock for different language found: "+langBlock+" -->";
						log.finer("--found languageblock different to \""+defaultLang+"\". Put in comments.");						
					}
				}
				text=text.replace(langBlock,textInDefaultLang);
			}catch(Exception e){
				log.warning("--unable to read languageblock.");
				log.warning(e.getLocalizedMessage());
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
				for(int i=0;i<langs.getLength();i++) {
					Element lang = (Element) langs.item(i);
					if (lang.getAttribute("which").equals(defaultLang)) {
						//text in defaultLang
						textInDefaultLang = lang.getTextContent();
					}
					if (lang.getAttribute("which").equals("default")) {
						defaultText = lang.getTextContent();
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
				text=text.replace(translatedBlock,outtext);
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

	public String getQuestionType() { return questionType; 	}
}
