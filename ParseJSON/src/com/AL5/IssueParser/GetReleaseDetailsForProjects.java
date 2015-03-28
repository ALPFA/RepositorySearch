package com.AL5.IssueParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

public class GetReleaseDetailsForProjects {
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		Properties properties = new Properties();
		InputStream input = null;
		try{
			input = new FileInputStream("lib//ResourceHelper.properties");
			properties.load(input);
			Object obj = parser.parse(new FileReader(properties.getProperty("PATH_FOR_ISSUES_URL")));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray issueURLs = (JSONArray) jsonObject.get("IssueURL");
			for(int project = 1; project <= issueURLs.size(); project++){
				String url = issueURLs.get(project-1).toString();
				String projectName = GetIssueDetailsForProjects.getProjectName(url);
				String releaseURL = url.replace("issues", "releases");
				//Get all these release objects in to JSON files and then feed them to database
				String releaseJson = null;
				boolean hasNextpage = true;
				try{
					releaseJson = IOUtils.toString(new URL(releaseURL));
					JSONArray issueJsonArr = (JSONArray) JSONValue.parseWithException(releaseJson);
					//Consider projects with at least 3 versions
					if(issueJsonArr.size() > 2){
						int count = 0;
						while(hasNextpage){
							if(issueJsonArr.size()!=30){
								hasNextpage = false;
							}
							for(int release = 1; release <= issueJsonArr.size(); release++){
								JSONObject releaseObj = (JSONObject)issueJsonArr.get(release-1);
								String tagName = releaseObj.get("tag_name").toString();
								String tagValidName[] = tagName.split("/");
								tagName = tagValidName.length>1?tagValidName[1]:tagName;
								writeToFile(properties, projectName, tagName, releaseObj);
							}
							if(hasNextpage){
								issueJsonArr = GetCommentsForIssues.checkIfnewPageHasData(++count,releaseURL);
								if(issueJsonArr.size() > 0){
									hasNextpage = true;
								}
							}
						}
					}
					else{
						//Note this project and exclude from our list
						System.out.println(projectName);
					}
				}
				catch(Exception e){
					System.out.println("Exception "+ e);
					System.exit(1);
				}
				
			}
			System.out.println("Executed for all projects");
		}
		
		catch(Exception e){
			System.out.println("Exception occured "+e);
			System.exit(0);
		}

	
	}
	
	private static void writeToFile(Properties properties, String projectName,String tagName, JSONObject issueObj) {
		try{
			String directory = properties.getProperty("PATH_FOR_RELEASES")+"//"+projectName;
			String absPath = properties.getProperty("PATH_FOR_RELEASES")+"//"+projectName+"//"+tagName+".json";
			File file = new File(directory);
			if (!file.exists()) {
				file.mkdirs();
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
}
