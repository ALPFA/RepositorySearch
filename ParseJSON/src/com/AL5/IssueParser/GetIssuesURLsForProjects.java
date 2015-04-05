package com.AL5.IssueParser;

import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
/**
 * Not required. TODO: Delete this file
 * @author theja
 *
 */
public class GetIssuesURLsForProjects {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try{
			JSONObject projObj = new JSONObject();
			projObj.put("File", "issuesURL");
			projObj.put("Description", "Contains URL's of all Issues for all the projects under consideration");
			JSONArray urlList = new JSONArray();
			for(int pgnum = 1; pgnum <= 20; pgnum++){
				Object obj = parser.parse(new FileReader("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\Project Names - JSONS\\Page"+pgnum+".json"));
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray projects = (JSONArray) jsonObject.get("items");
				for(int i = 0; i < projects.size(); i++){
					JSONObject project = (JSONObject)projects.get(i);
					StringBuffer issuesURL = new StringBuffer();
					issuesURL.append(project.get("issues_url").toString());
					issuesURL.delete(issuesURL.length()-9, issuesURL.length());
					issuesURL.append("?client_id=80a63d9e364460892790&client_secret=42d543af8e3781933af0528bec8c66725c1a6b68&state=closed");
					urlList.add(issuesURL.toString());
				}
			}
			projObj.put("IssueURL", urlList);
			//Write this JSON to a file
			FileWriter file = new FileWriter("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\IssueURLs.json");
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
