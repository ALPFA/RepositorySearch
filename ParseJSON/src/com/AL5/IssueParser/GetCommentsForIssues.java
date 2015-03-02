package com.AL5.IssueParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class GetCommentsForIssues {

	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(new FileReader("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\IssueURLs.json"));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray issueURLs = (JSONArray) jsonObject.get("IssueURL");
			for(int project = 1; project <= issueURLs.size(); project++){
				JSONObject projObj = new JSONObject();
				projObj.put("File", "issueBody");
				projObj.put("Description", "This file contains body of all the issues for project");
				JSONArray issuesList = new JSONArray();
				boolean hasNextpage = true;
				//Append client secret and id to get 5000 rate limit
				String url = issueURLs.get(project-1).toString();
				String issuesJson = IOUtils.toString(new URL(url));
				JSONArray issuesJsonArr = (JSONArray) JSONValue.parseWithException(issuesJson);
				int count = 0;
	            //get the body of issue - description of issue
				while(hasNextpage){
					if(issuesJsonArr.size()!=30){
						hasNextpage = false;
					}
					for(int issueNum = 1; issueNum <= issuesJsonArr.size(); issueNum++){
						JSONObject issue = (JSONObject)issuesJsonArr.get(issueNum-1);
						StringBuffer issueBody = new StringBuffer("");
						if(issue.get("body")!=null){
							issueBody.append(issue.get("body").toString());
						}
						/*if(((count*30)+issueNum) == 171){
							System.out.println("why am I failing here");
						}*/
						issuesList.add("Issue "+((count*30)+issueNum)+" :"+(issueBody.toString()));
						System.out.println("For Project "+ project +" issue "+ ((count*30)+issueNum));
					}
					issuesJsonArr = checkIfnewPageHasData(++count,url);
					if(issuesJsonArr.size() > 0){
						hasNextpage = true;
					}
				}
				projObj.put("All_Closed_Issues_Body", issuesList);
				//Write this JSON to a file
				FileWriter file = new FileWriter("C:\\Users\\ThejaSwarup\\Box Sync\\Spring 2015\\JavaProjects\\IssuesForProjects\\IssuesContentForProject"+project+".json");
				file.write(projObj.toJSONString());
				file.flush();
				file.close();
				System.out.println("Executed for project "+project);
			}
			System.out.println("Executed for all projects");
		}
		catch(Exception e){
			System.out.println("Exception occured "+e);
			System.exit(0);
		}

	}

	private static JSONArray checkIfnewPageHasData(int count, String url) {
		JSONArray issuesJsonArr = null;
		try {
			String issuesJson = IOUtils.toString(new URL(url+"&page="+count));
			issuesJsonArr = (JSONArray) JSONValue.parseWithException(issuesJson);
		}
		catch(Exception e){
			System.out.println("EXception in checkIfnewPageHasData" + e);
		}
		return issuesJsonArr;
	}

}
