package com.AL5.IssueParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 * This program just generates a JSON object that stores project names and their IDs
 * @author theja
 *
 */
public class GenerateProjectBasicDetails {

	public static void prepareProjectNameIdMapping() {
		JSONParser parser = new JSONParser();
		InputStream dir_props = null;
		Properties properties_dir = new Properties();
		try{
			JSONObject projObj = new JSONObject();
			dir_props = new FileInputStream("lib"+File.separator+"linux_directories.properties");
			properties_dir.load(dir_props);
			projObj.put("File", "projects");
			projObj.put("Description", "Contains list of java project ids and their names on github");
			JSONArray nameList = new JSONArray();
			JSONArray idList = new JSONArray();
			final File dir = new File(properties_dir.getProperty("PATH_FOR_SEARCH_RESULTS"));
			for (final File fileEntry : dir.listFiles()) {
				String path = fileEntry.getPath();
		    	Object obj = parser.parse(new FileReader(path));
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray projects = (JSONArray) jsonObject.get("items");
				for(int i = 0; i < projects.size(); i++){
					JSONObject project = (JSONObject)projects.get(i);
					String projectName = project.get("name").toString();
					String projectId = project.get("id").toString();
					nameList.add(projectName);
					idList.add(projectId);
				}
			}
			projObj.put("Names", nameList);
			projObj.put("Ids", idList);
			//Write this JSON to a file
			GetProjectMetadata.writeToFile(properties_dir.getProperty("PATH_FOR_PROJECT_NAME_ID_OBJECT"),projObj);
			System.out.println("Executed");
		}
		catch(Exception e){
			System.out.println("Exception from main "+ e);
		}
	}

}