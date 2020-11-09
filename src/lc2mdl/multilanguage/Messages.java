package lc2mdl.multilanguage;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lc2mdl.ConvertOptions;

public class Messages{
	private static final String BUNDLE_NAME="lc2mdl.multilanguage.messages";
	private static ResourceBundle RESOURCE_BUNDLE=null;
	
	public static void setLanguage(Locale locale){
		RESOURCE_BUNDLE=ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}

	public static String getString(String key){
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
