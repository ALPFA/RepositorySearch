package com.AL5.IssueParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class GetIssueDetailsForProjects {

	public static void main(String[] args) {
		Properties properties = new Properties();
		JSONParser jsonParser = new JSONParser();
		InputStream input = null;
		Object obj = null;
		HashMap<String,String> projectMap = Utilities.getProjectMap();
		try{
			//Read all the properties from property file
			input = new FileInputStream("lib//ResourceHelper.properties");
			properties.load(input);
			obj = jsonParser.parse(new FileReader(properties.getProperty("PATH_FOR_ISSUES_URL")));
			JSONObject jsonObject = (JSONObject) obj;
			// The required content is stored in a property called IssueURL
			//Ex: "IssueURL":["https://api.github.com/repos/ReactiveX/RxJava/issues"]
			JSONArray issueURLs = (JSONArray) jsonObject.get("IssueURL");
			//for each of the project create one JSON Object for each issue
			for(int project = 2; project < 3; project++){
				String url = issueURLs.get(project-1).toString();
				String projectName = getProjectName(url);
				String projectId = projectMap.get(projectName);
				HashMap<String,IssueObject> issuesOfProjectMap = new HashMap<String, IssueObject>();
				issuesOfProjectMap = getIssuesTitle(url);
				//write content of each bug as a seperate JSON
				for (String key : issuesOfProjectMap.keySet()) {
					JSONObject issueObj = new JSONObject();
					IssueObject issue = issuesOfProjectMap.get(key);
					issueObj.put("Project ID", projectId);
					issueObj.put("Project Name", projectName);
					issueObj.put("Title", issue.getTitle());
					issueObj.put("Issue Number", key);
					issueObj.put("Body", issue.getBody());
					issueObj.put("Comments", issue.getComments_count());
					issueObj.put("Created At", issue.getCreated_at());
					issueObj.put("Closed At", issue.getClosed_at());
					//Get all comments for this bug
					if(Integer.parseInt(issue.getComments_count()) > 0){
						JSONArray commentsList = new JSONArray();
						String commentsURL = issue.getComments_URL()+"?client_id="+properties.getProperty("CLIENT_ID")+"&client_secret="+properties.getProperty("CLIENT_SECRET");
						String commentsJson = null;
						boolean hasNextpage = true;
						try{
							//Some times the URL may be damaged or service is not available
							commentsJson = IOUtils.toString(new URL(commentsURL));
							JSONArray commentsJsonArr = (JSONArray) JSONValue.parseWithException(commentsJson);
							int count = 0;
							//get the body of issue - description of issue
							while(hasNextpage){
								if(commentsJsonArr.size()!=30){
									hasNextpage = false;
								}
								for(int comment = 1; comment <= commentsJsonArr.size(); comment++){
									JSONObject commentObj = (JSONObject)commentsJsonArr.get(comment-1);
									commentsList.add(commentObj.get("body"));
								}
								if(hasNextpage){
									commentsJsonArr = GetCommentsForIssues.checkIfnewPageHasData(++count,commentsURL);
									if(commentsJsonArr.size() > 0){
										hasNextpage = true;
									}
								}
							}
						}
						catch(Exception e){
							//TODO: Implement a log mechanism to write to a log file
							System.out.println("URL Issue " + e);
						}
						issueObj.put("Comments_content", commentsList);
					}
					//write the content to JSON File
					writeToFile(properties,projectName,key,issueObj);
				}
				
			}//for
		}
		catch(Exception e){
			System.out.println("Exception "+e);
			System.exit(1);
		}
		System.out.println("Executed!");
	}

	private static void writeToFile(Properties properties, String projectName, String key, JSONObject issueObj) {
		try{
			String directory = properties.getProperty("PATH_FOR_SUBJECTS")+"//"+projectName;
			String absPath = properties.getProperty("PATH_FOR_SUBJECTS")+"//"+projectName+"//"+key+".json";
			File file = new File(directory);
			if (!file.exists()) {
				file.mkdir();
			}
			File jsonFile = new File(absPath);
			FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(issueObj.toJSONString());
			bw.close();
		}
		catch(Exception e){
			System.out.println("Exception in writeToFile "+ e);
			System.exit(0);
		}
		
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String,IssueObject> getIssuesTitle(String url) {
		String issuesJson = null;
		boolean hasNextpage = true;
		HashMap<String,IssueObject> issues = new HashMap<String, IssueObject>();
		try{
			//Some times the URL may be damaged or service is not available
			issuesJson = IOUtils.toString(new URL(url));
			JSONArray issuesJsonArr = (JSONArray) JSONValue.parseWithException(issuesJson);
			int count = 0;
			 //get the body of issue - description of issue
			while(hasNextpage){
				if(issuesJsonArr.size()!=30){
					hasNextpage = false;
				}
				for(int issueNum = 1; issueNum <= issuesJsonArr.size(); issueNum++){
					JSONObject issue = (JSONObject)issuesJsonArr.get(issueNum-1);
					IssueObject issueObj = new IssueObject();
					StringBuffer issueTitle = new StringBuffer("");
					if(issue.get("title")!=null){
						issueTitle.append(issue.get("title").toString());
						issueObj.setTitle(issueTitle.toString());
					}
					if(issue.get("closed_at")!=null){
						issueObj.setClosed_at(issue.get("closed_at").toString());
					}
					if(issue.get("created_at")!=null){
						issueObj.setCreated_at(issue.get("created_at").toString());
					}
					if(issue.get("body")!=null){
						issueObj.setBody(issue.get("body").toString());
					}
					if(issue.get("comments")!=null){
						issueObj.setComments_count(issue.get("comments").toString());
					}
					if(issue.get("comments_url")!=null){
						issueObj.setComments_URL(issue.get("comments_url").toString());
					}
					issues.put(issue.get("number").toString(),issueObj);
				}
				issuesJsonArr = GetCommentsForIssues.checkIfnewPageHasData(++count,url);
				if(issuesJsonArr.size() > 0){
					hasNextpage = true;
				}
			}
		}
		catch(Exception e){
			//TODO:Implement a log mechanism to write to a log file
			System.out.println("URL Issue " + e);
		}
		return issues;
	}

	public static String getProjectName(String url) {
		String name = null;
		String[] parse1 = url.split("/issues?");
		String[] parse2 = parse1[0].split("/repos/");
		String[] parse3 = parse2[1].split("/");
		name = parse3[1];
		return name;
	}

}
