package lc2mdl.multilanguage;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lc2mdl.ConvertOptions;
import lc2mdl.lc.problem.Problem;

public class Messages{
	private static final String BUNDLE_NAME="lc2mdl.multilanguage.messages";
	private static ResourceBundle RESOURCE_BUNDLE=null;
	
	public static void setLanguage(Locale locale){
		RESOURCE_BUNDLE=ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}

	public static String getString(String key, Problem p){
		boolean multilang=ConvertOptions.isMultilang();

		if(multilang){
			LinkedHashSet<String> supportedLanguages=p.getSupportedLangugaes();
			if(supportedLanguages.size()>0){
				//iterate
				String multilangString="";
				//and return multilangugae string
				for(String lang:supportedLanguages){
					String stringInLang=getStringInLanguage(key, lang);
					multilangString+="<span lang=\""+lang+"\" class=\"multilang\">"+System.lineSeparator();
					multilangString+=stringInLang+System.lineSeparator();
					multilangString+="</span>"+System.lineSeparator();
				}
				//reset default language
				RESOURCE_BUNDLE=getDefaultResourceBundle();
				return multilangString;
			}
		}
		//else
		return getMonoLanguageString(key);		
	}

	private static String getStringInLanguage(String key, String lang){
		setLanguage(new Locale(lang));
		return getMonoLanguageString(key);
	}

	public static String getMonoLanguageString(String key){
		if(RESOURCE_BUNDLE==null){
			RESOURCE_BUNDLE=getDefaultResourceBundle();
		}
		
		try{
			return RESOURCE_BUNDLE.getString(key);
		}catch(MissingResourceException e){
			return getStringFromDefaultBundle(key);
		}
	}
	
	private static ResourceBundle getDefaultResourceBundle(){
		Locale defaultLocale=new Locale(ConvertOptions.getDefaultLang());
		return ResourceBundle.getBundle(BUNDLE_NAME, defaultLocale);
	}
	
	private static String getStringFromDefaultBundle(String key){
		ResourceBundle defaultBundle=getDefaultResourceBundle();
		try{
			return defaultBundle.getString(key);
		}catch(MissingResourceException e){
			return '!'+key+'!';
		}		
	}

}
