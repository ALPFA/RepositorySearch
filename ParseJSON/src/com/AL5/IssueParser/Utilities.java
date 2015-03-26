package com.AL5.IssueParser;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Utilities {

	public static HashMap<String,String> getProjectMap(){
		HashMap<String,String> projectMap = new HashMap<String,String>();
		Properties properties = new Properties();
		JSONParser jsonParser = new JSONParser();
		InputStream input = null;
		Object obj = null;
		try{
			input = new FileInputStream("lib//ResourceHelper.properties");
			properties.load(input);
			obj = jsonParser.parse(new FileReader(properties.getProperty("PATH_FOR_NAME_IDS")));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray ids = (JSONArray) jsonObject.get("Ids");
			JSONArray names = (JSONArray) jsonObject.get("Names");
			for(int index = 0; index < ids.size();index++){
				String id = ids.get(index).toString();
				String name = names.get(index).toString();
				projectMap.put(name, id);
			}
		}
		catch(Exception e){
			System.out.println("Exception in creating map");
		}
		return projectMap;
	}
}
