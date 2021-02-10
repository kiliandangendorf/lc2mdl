package lc2mdl.lc.problem;

import lc2mdl.ConvertOptions;
import lc2mdl.util.ConvertAndFormatMethods;
import lc2mdl.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultilanguageTextTransformer{
	public static Logger log=Logger.getLogger(MultilanguageTextTransformer.class.getName());

	
	private Problem problem;

	public MultilanguageTextTransformer(Problem problem){
		this.problem=problem;
	}

	public String transformMultilanguage(String text){
		String defaultLang=ConvertOptions.getDefaultLang();
		boolean multilang=ConvertOptions.isMultilang();
		
		//TRANSLATED
		if(text.contains("translated")){
			log.finer("--transform translated-blocks.");
			text=findLanguagesInTranslated(text, defaultLang, multilang);
		}		
		
		//LANGUAGEBLOCKS
		//exclude="" show always without multilang, so remove surrounding languageblock 
		//will be replaced by its inner content as it would rendered anyways in LC
		text=text.replaceAll("< {0,}languageblock {0,}exclude {0,}= {0,}\\\" {0,}\\\" {0,}>([\\s\\S]*?)< {0,}\\/ {0,}languageblock {0,}>","$1");

		if(text.contains("languageblock")){			
			String dummyGroup="lc2mdl_dummy_languageblock_group";
			log.finer("--group languageblocks into dummy-groups temporarily.");
			text=groupLanguageBlocks(text, dummyGroup);

			//find languages in group similar to translated-blocks
			log.finer("--sort and replace languageblocks-groups.");
			text=findLanguagesInLanguageBlockGroups(text,defaultLang, dummyGroup, multilang);
		}
		return text;
	}

	//================================================================================
    // MULTILANG
    //================================================================================

	private String generateMultilangOutput(HashMap<String,String> translations, String defaultLang){
		String outtext="";
		
		//sort list and give found langugaes to problem
		LinkedHashMap<String,String> sortedTranslations=problem.sortTranslationsMapAndSaveLanguages(translations, defaultLang);
	
		//generate multilang output
		for(String lang:sortedTranslations.keySet()){
			outtext+="<span lang=\""+lang+"\" class=\"multilang\">"+sortedTranslations.get(lang)+"</span>"+System.lineSeparator();
		}
		
		return outtext;
	}

	private String generateOutputForBlock(HashMap<String,String> translations, String defaultLang, String blocktype, String wholeBlock, boolean multilang){
		String outtext="";
		
		//IF MULTILANG
		if(multilang){
			log.finer("---generating multilang output for "+blocktype+".");
			outtext=generateMultilangOutput(translations,defaultLang);					
		}
		
		//IF NOT MULTILANG: CHOOSE ONE
		if(!multilang){
			if(translations.containsKey(defaultLang)){
				//text in defaultLang
				outtext=translations.get(defaultLang);
				log.finer("---found \""+defaultLang+"\" in "+blocktype+".");
			}else{
				//text not in defaultLang
				if(translations.containsKey("default")){
					//default text 
					outtext=translations.get("default");
					log.finer("---found default text in "+blocktype+".");
				}else{
					//no text found
					outtext="<!-- lc2mdl: found no match in "+blocktype+": "+wholeBlock+" -->";
					log.warning("---found no text to preferred language \""+defaultLang+"\"");
				}
			}
		}
		return outtext;
	}
	
	//================================================================================
    // TRANSLATED
    //================================================================================
	
	private String findLanguagesInTranslated(String text,String defaultLang, boolean multilang){
		String translatedBlockPat="< {0,}translated[\\s\\S]*?\\/ {0,}translated {0,}>";
		Matcher matcher=Pattern.compile(translatedBlockPat).matcher(text);
		
		StringBuffer sb=new StringBuffer();
		while(matcher.find()){
			String translatedBlock=matcher.group();
			
			try{
				Document dom=XMLParser.parseString2DOM(translatedBlock);
				NodeList langs=dom.getElementsByTagName("lang");
				
				HashMap<String,String> translations=new HashMap<>();
	
				//find translations
				for(int i=0;i<langs.getLength();i++) {
					Element lang = (Element) langs.item(i);
					if(getNodeContent(lang).trim().equals("")){
						//skip if no content
						continue;
					}
					putNodeContentIntoTranslationsMap(translations,lang,lang.getAttribute("which"));
					log.finer("---found \""+lang.getAttribute("which")+"\" in translated-block.");
				}
					
				String outtext=generateOutputForBlock(translations,defaultLang,"translated-block",translatedBlock,multilang);
				
				matcher.appendReplacement(sb,Matcher.quoteReplacement(outtext));
				
			}catch(Exception e){
				log.warning("---unable to read translated-block.");
				log.warning(e.getLocalizedMessage());
			}
		}			
		matcher.appendTail(sb);
		text=sb.toString();
		return text;
	}

	//================================================================================
    // LANGUAGEBLOCK
    //================================================================================
	
	private String groupLanguageBlocks(String text, String dummyGroup){
		String dummyTagName="lc2mdl_dummy_outtext_tag";

		//put text into dummy-tags for parsing
		text=surroundByDummyTags(text,dummyTagName);
		try{
			Document dom=XMLParser.parseString2DOM(text);
			NodeList wholeLangList=dom.getElementsByTagName("languageblock");
			
			if(wholeLangList.getLength()==0){
				//continue doing nothing (should never reach this code ;) )
			}else{
				Node thisBlock, next, nextNext;
								
				ArrayList<Node> languageblockGroup=new ArrayList<>();
				
				for(int i=0;i<wholeLangList.getLength();i++){
					thisBlock=wholeLangList.item(i);
					next=thisBlock.getNextSibling();
					nextNext=null;
					
					languageblockGroup.add(thisBlock);
					
					boolean thereIsAFollowingLanguageBlock=false;

					if(next!=null){
						if(next.getNodeType()==Node.TEXT_NODE){
							if(next.getTextContent().trim().equals("")){
								//clear content as it is empty anyway ;)
								next.setTextContent("");
								//found empty text-node, check if languageblock follows
								nextNext=next.getNextSibling();
								if(nextNext!=null){
									if(nextNext.getNodeType()==Node.ELEMENT_NODE){
										if(nextNext.getNodeName().equals("languageblock")){
											//found following languageblock
											thereIsAFollowingLanguageBlock=true;
										}
									}
								}
							}
						}
					}
					if(!thereIsAFollowingLanguageBlock){
						Node parent=thisBlock.getParentNode();
						Node group=dom.createElement(dummyGroup);
						parent.insertBefore(group,thisBlock);
						
						//move languageblock-nodes to group
						for(Node node:languageblockGroup){
							//will move automatically
							group.appendChild(node);
						}

						//then clear list
						languageblockGroup=new ArrayList<>();
					}
				}
				//get XML content
				String buf=ConvertAndFormatMethods.getNodeString(dom.getElementsByTagName(dummyTagName).item(0));
				text=(buf==null)?"":buf;
			}
		}catch(Exception e){
			log.warning("---unable to read languageblock.");
			log.warning(e.getLocalizedMessage());
			e.printStackTrace();
		}		
		//text with languageblock-GROUPS
		text=removeDummyTags(text,dummyTagName);
		return text;
	}
	
	private String findLanguagesInLanguageBlockGroups(String text,String defaultLang,String dummyGroup, boolean multilang){
		String languageBlockGroupPat="< {0,}"+dummyGroup+"[\\s\\S]*?\\/ {0,}"+dummyGroup+">[\\r\\n]?";
		Matcher matcher=Pattern.compile(languageBlockGroupPat).matcher(text);
		
		StringBuffer sb=new StringBuffer();
		while(matcher.find()){
			String languageBlockGroup=matcher.group();
			
			try{
				Document dom=XMLParser.parseString2DOM(languageBlockGroup);
				NodeList languageblocks=dom.getElementsByTagName("languageblock");
				
				HashMap<String,String> translations=new HashMap<>();

				//find translations
				for(int i=0;i<languageblocks.getLength();i++) {
					Element languageblock = (Element)languageblocks.item(i);
									
					//Check if content is empty :/
					if(getNodeContent(languageblock).trim().equals("")){
						//continue; content will be replaced by nothing
						continue;
					}

					//exclude and include come in arbitrary order...
					
					if(languageblock.hasAttribute("include")){
						String languageList=languageblock.getAttribute("include").toLowerCase();

						//split on comma or space 
						String[] languages=languageList.split("[,\\s]+");
						if(languages.length==0){
							//include="" —>remove (won’t be shown anyway)
							continue;
						}else{
							for(String lang:languages){
								putNodeContentIntoTranslationsMap(translations,languageblock,lang);
								log.finer("---found languageblock for \""+lang+"\".");
							}
						}
					}

					if(languageblock.hasAttribute("exclude")){
						String languageList=languageblock.getAttribute("exclude").toLowerCase();

						//exclude="x y, z" will become "default" (whatever languages are in)
						putNodeContentIntoTranslationsMap(translations,languageblock,"default");
						log.finer("---found languageblock excluding \""+languageList+"\". Changed it to \"default\".");						
					}
				}

				String outtext=generateOutputForBlock(translations,defaultLang,"languageblock",languageBlockGroup,multilang);

				matcher.appendReplacement(sb,Matcher.quoteReplacement(outtext));
				
			}catch(Exception e){
				log.warning("---unable to read prepared languageblock-group ("+dummyGroup+").");
				log.warning(e.getLocalizedMessage());
			}
		}			
		matcher.appendTail(sb);
		text=sb.toString();
		return text;
	}
	
	
	//================================================================================
    // HELPER
    //================================================================================
	
	private void putNodeContentIntoTranslationsMap(HashMap<String,String> translations, Node node, String language){
		String translation=getNodeContent(node);
	
		//if key in this block already exists, just append it ;)
		String previousValue=translations.get(language);
		if(previousValue!=null){
			translation=previousValue+translation;
		}
		
		translations.put(language,translation);
	}

	private String getNodeContent(Node node){
		String name=node.getNodeName();
		String text=ConvertAndFormatMethods.getNodeString(node);
		
		//TODO if(text==null)
		
		//remove opening tag
		text=text.replaceAll("< {0,}"+name+"[\\s\\S]*?>","");
		//remove closing tag
		text=text.replaceAll("< {0,}\\/ {0,}"+name+" {0,}>[\\r\\n]?","");
		
		return text;
	}

	private String surroundByDummyTags(String text, String dummyTagName){
		String prefix="<"+dummyTagName+">";
		String postfix="</"+dummyTagName+">";
		return prefix+text+postfix;
	}

	private String removeDummyTags(String text, String dummyTagName){
		String prefix="<"+dummyTagName+">";
		String postfix="</"+dummyTagName+">";
		text=text.replace(prefix,"");
		text=text.replace(postfix,"");
		return text;
	}
	
}
