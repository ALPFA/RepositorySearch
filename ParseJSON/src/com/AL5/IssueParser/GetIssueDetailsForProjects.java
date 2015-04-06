package com.AL5.IssueParser;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
/**
 * This program gets closed issue objects for each projects and stores them as JSON
 * @author theja
 *
 */
public class GetIssueDetailsForProjects {
	public static long apiRequests = getCurrentAPIRequests();
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		JSONParser jsonParser = new JSONParser();
		Properties properties_dir = new Properties();
		Properties properties_auth = new Properties();
		InputStream dir_props = null;
		InputStream auth_props = null;
		Object obj = null;
		HashMap<String,String> projectMap = Utilities.getProjectMap();
		try{
			//Read all the properties from property file
			dir_props = new FileInputStream("lib//linux_directories.properties");
			auth_props = new FileInputStream("lib//authentication.properties");
			properties_dir.load(dir_props);
			properties_auth.load(auth_props);
			obj = jsonParser.parse(new FileReader(properties_dir.getProperty("PATH_FOR_ISSUES_URL")));
			JSONObject jsonObject = (JSONObject) obj;
			// The required content is stored in a property called IssueURL
			//Ex: "IssueURL":["https://api.github.com/repos/ReactiveX/RxJava/issues"]
			JSONArray issueURLs = (JSONArray) jsonObject.get("IssueURL");
			//for each of the project create one JSON Object for each issue
			for(int project = 1; project < issueURLs.size(); project++){
				String url = issueURLs.get(project-1).toString();
				String projectName = getProjectName(url,"/issues?");
				String projectId = projectMap.get(projectName);
				HashMap<String,IssueObject> issuesOfProjectMap = new HashMap<String, IssueObject>();
				//Get only closed issues
				issuesOfProjectMap = getIssues(url+"&state=closed");
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
						String commentsURL = issue.getComments_URL()+"?client_id="+properties_auth.getProperty("CLIENT_ID")+"&client_secret="+properties_auth.getProperty("CLIENT_SECRET");
						String commentsJson = null;
						boolean hasNextpage = true;
						try{
							//Some times the URL may be damaged or service is not available
							commentsJson = IOUtils.toString(new URL(commentsURL));
							apiRequests--;
							checkAPIRateLimit();
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
									commentsJsonArr = checkIfnewPageHasData(++count,commentsURL);
									if(commentsJsonArr.size() > 0){
										hasNextpage = true;
									}
								}
							}
						}
						catch(Exception e){
							//TODO: Implement a log mechanism to write to a log file
							System.out.println("URL Issue " + e);
							System.exit(1);
						}
						issueObj.put("Comments_content", commentsList);
					}
					//write the content to JSON File
					GetProjectMetadata.writeToFile(properties_dir.getProperty("PATH_FOR_ISSUES_METADATA")+projectName,key,issueObj);
				}
				
			}//for
		}
		catch(Exception e){
			System.out.println("Exception "+e);
			System.exit(1);
		}
		System.out.println("Executed!");
	}
	/**
	 * Get the current available rate limit and return the value
	 * @return
	 */
	private static long getCurrentAPIRequests() {
		//Hit API rate limit end point to determine remaining capacity
		long currentlimit = 0;
		Properties properties_auth = new Properties();
		InputStream auth_props = null;
		String rateLimitJson = null;
		try{
			auth_props = new FileInputStream("lib//authentication.properties");
			properties_auth.load(auth_props);
			String url = "https://api.github.com/rate_limit?client_id="+properties_auth.getProperty("CLIENT_ID")+"&client_secret="+properties_auth.getProperty("CLIENT_SECRET");
			rateLimitJson = IOUtils.toString(new URL(url));
			JSONObject rateLimit = (JSONObject) JSONValue.parseWithException(rateLimitJson);
			JSONObject rate = (JSONObject)rateLimit.get("rate");
			currentlimit = (long) rate.get("remaining");
		}
		catch (Exception e){
			System.out.println("Exception in getCurrentAPIRequests "+e);
			System.exit(1);
		}
		return currentlimit;
	}
	/**
	 * Pause the execution for an hour and then continue execution
	 */
	private static void checkAPIRateLimit() {
		if(apiRequests <= 200){
			try {
				apiRequests = getCurrentAPIRequests();
				if(apiRequests <= 200){
					Thread.sleep(3600001);
					apiRequests = getCurrentAPIRequests();
				}
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			    return;
			}
		}
		
	}

	private static HashMap<String,IssueObject> getIssues(String url) {
		String issuesJson = null;
		boolean hasNextpage = true;
		HashMap<String,IssueObject> issues = new HashMap<String, IssueObject>();
		try{
			//Some times the URL may be damaged or service is not available
			issuesJson = IOUtils.toString(new URL(url));
			apiRequests--;
			checkAPIRateLimit();
			JSONArray issuesJsonArr = (JSONArray) JSONValue.parseWithException(issuesJson);
			int count = 1;
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
				issuesJsonArr = checkIfnewPageHasData(++count,url);
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
	/**
	 * Return project name from URL
	 * @param url
	 * @param param
	 * @return
	 */
	public static String getProjectName(String url,String param) {
		String name = null;
		String[] parse1 = url.split(param);
		String[] parse2 = parse1[0].split("/repos/");
		String[] parse3 = parse2[1].split("/");
		name = parse3[1];
		return name;
	}
	/**
	 * function to fetch next page data
	 * @param count
	 * @param url
	 * @return
	 */
	public static JSONArray checkIfnewPageHasData(int count, String url) {
		JSONArray issuesJsonArr = null;
		try {
			String issuesJson = IOUtils.toString(new URL(url+"&page="+count));
			apiRequests--;
			checkAPIRateLimit();
			issuesJsonArr = (JSONArray) JSONValue.parseWithException(issuesJson);
		}
		catch(Exception e){
			System.out.println("Exception in checkIfnewPageHasData" + e);
		}
		return issuesJsonArr;
	}
}
