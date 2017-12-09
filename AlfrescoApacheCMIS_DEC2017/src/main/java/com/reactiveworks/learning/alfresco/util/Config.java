package com.reactiveworks.learning.alfresco.util;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import static com.reactiveworks.learning.alfresco.util.AlfrescoConstant.*;

/**
 * This is the config class
 */
public class Config {
	
	static Logger logger=Logger.getLogger(Config.class);
/**
 * This method will return String value after getting from the properties file	
 * @return String
 * @throws FileNotFoundException 
 */
  public  String getPropValues(String key) throws FileNotFoundException{
	  Properties properties=new Properties();
	  String propFileName=PROPERTIES_FILE_NAME;
	  InputStream inputStream=getClass().getClassLoader().getResourceAsStream(propFileName);
	  String keyVal=null;
	  try {
	  if(inputStream!=null){
		 
		properties.load(inputStream);
	  }
	  else{
		  throw new FileNotFoundException("No File Exist with name:: "+propFileName);
	  }
	  } catch (IOException e) {
		  throw new FileNotFoundException("There is IO Exception");
	  }
	  keyVal=properties.getProperty(key);
	  logger.debug("keyVal:: "+keyVal);
	  return keyVal;
  }
}