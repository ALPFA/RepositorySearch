package com.AL5.IssueParser;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GenerateProjectBasicDetails {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try{
			JSONObject projObj = new JSONObject();
			projObj.put("File", "projects");
			projObj.put("Description", "Contains list of java project ids and their names on github");
			JSONArray nameList = new JSONArray();
			JSONArray idList = new JSONArray();
			for(int pgnum = 1; pgnum <= 20; pgnum++){
				Object obj = parser.parse(new FileReader("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\Project Names - JSONS\\Page"+pgnum+".json"));
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
			FileWriter file = new FileWriter("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\NameAndIds.json");
			file.write(projObj.toJSONString());
			file.flush();
			file.close();
			System.out.println("Executed");
		}
		catch(Exception e){
			System.out.println("Exception from main "+ e);
		}
	}

}