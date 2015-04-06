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
/**
 * This program collects release details and issue URLs of valid projects
 * @author theja
 *
 */
public class GetProjectMetadata {
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		Properties properties_dir = new Properties();
		Properties properties_auth = new Properties();
		InputStream dir_props = null;
		InputStream auth_props = null;
		try{
			dir_props = new FileInputStream("lib"+File.separator+"linux_directories.properties");
			auth_props = new FileInputStream("lib"+File.separator+"authentication.properties");
			properties_dir.load(dir_props);
			properties_auth.load(auth_props);
			final File dir = new File(properties_dir.getProperty("PATH_FOR_SEARCH_RESULTS"));
			collectIssueAndReleaseInfo(dir,properties_auth,properties_dir,parser);
			GenerateProjectBasicDetails.prepareProjectNameIdMapping();
			System.out.println("Finished GetProjectMetadata");
		}
		
		catch(Exception e){
			System.out.println("Exception occured "+e);
			System.exit(0);
		}

	
	}
	
	/**
	 * Heavy lifting done by this function
	 * @param dir
	 * @param properties_auth
	 * @param properties_dir
	 * @param parser
	 */
	public static void collectIssueAndReleaseInfo(final File dir,Properties properties_auth, Properties properties_dir, JSONParser parser) {
		//properties for saving issueURLs
		JSONArray urlList = new JSONArray();
		JSONObject projObj = new JSONObject();
		projObj.put("File", "issuesURL");
		projObj.put("Description", "Contains URL's of all Issues for all the projects under consideration");
		try{
			for (final File fileEntry : dir.listFiles()) {
		        //Read required content from each file
		    	String path = fileEntry.getPath();
		    	Object obj = parser.parse(new FileReader(path));
				JSONObject jsonObject = (JSONObject) obj;
				JSONArray projects = (JSONArray) jsonObject.get("items");
				//Get Version details - while doing so check if their no.of releases count is more than some threshold. i.e., consider projects only which have more than 4 releases
				for(int i = 0; i < projects.size(); i++){
					//get the release URL for each project and check its version count
					JSONObject project = (JSONObject)projects.get(i);
					StringBuffer releasesURL = new StringBuffer();
					releasesURL.append(project.get("releases_url").toString());
					releasesURL.delete(releasesURL.length()-5, releasesURL.length());
					releasesURL.append("?client_id="+properties_auth.getProperty("CLIENT_ID")+"&client_secret="+properties_auth.getProperty("CLIENT_SECRET"));
					String projectName = GetIssueDetailsForProjects.getProjectName(releasesURL.toString(),"/releases?");
					//Now the releaseURL is ready. Using this URL find valid projects
					boolean isValidProject = collectReleaseDetails(releasesURL,properties_dir,projectName);
					//If a project is valid store its Issue URL in to a JSON Object so that we can just access this JSON to collect issue details
					if(isValidProject){
						StringBuffer issuesURL = new StringBuffer();
						issuesURL.append(project.get("issues_url").toString());
						issuesURL.delete(issuesURL.length()-9, issuesURL.length());
						issuesURL.append("?client_id="+properties_auth.getProperty("CLIENT_ID")+"&client_secret="+properties_auth.getProperty("CLIENT_SECRET"));
						urlList.add(issuesURL.toString());
					}
				}
		    }
			projObj.put("IssueURL", urlList);
			writeToFile(properties_dir.getProperty("PATH_FOR_ISSUES_URL"),projObj);
		}
		catch(Exception e){
			System.out.println("Exception in listFilesInDirectory "+e);
			System.exit(1);
		}
	}
	/**
	 * Write content to a file - nothing with sub directories
	 * @param path
	 * @param projObj
	 */
	public static void writeToFile(String path, JSONObject projObj) {
		//Write this JSON to a file
		try{
			FileWriter file = new FileWriter(path);
			file.write(projObj.toJSONString());
			file.flush();
			file.close();
		}
		catch(Exception e){
			System.out.println("Exception in writeToFile"+ e);
			System.exit(1);
		}
	}
	/**
	 * Write content to a file - with sub directory hierarchy
	 * @param properties
	 * @param projectName
	 * @param tagName
	 * @param issueObj
	 */
	public static void writeToFile(String parentDirectory,String fileName, JSONObject issueObj) {
		
		try{
			String absPath = parentDirectory+File.separator+fileName+".json";
			File file = new File(parentDirectory);
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
	
	/**
	 * This method collects the release details of project and stores it as JSON object
	 * @param releasesURL
	 * @param properties_dir
	 * @param projectName
	 * @return
	 */
	private static boolean collectReleaseDetails(StringBuffer releasesURL,Properties properties_dir, String projectName) {
		boolean isValidProject = false;
		String releaseJson = null;
		boolean hasNextpage = true;
		try{
			releaseJson = IOUtils.toString(new URL(releasesURL.toString()));
			JSONArray releasesJsonArr = (JSONArray) JSONValue.parseWithException(releaseJson);
			//Considering projects with releases greater than 2
			if(releasesJsonArr.size() > 2){
				int count = 0;
				while(hasNextpage){
					if(releasesJsonArr.size()!=30){
						hasNextpage = false;
					}
					for(int release = 1; release <= releasesJsonArr.size(); release++){
						JSONObject releaseObj = (JSONObject)releasesJsonArr.get(release-1);
						String tagName = releaseObj.get("tag_name").toString();
						String tagValidName[] = tagName.split("/");
						tagName = tagValidName.length>1?tagValidName[1]:tagName;
						//write the release content to JSON file
						writeToFile(properties_dir.getProperty("PATH_FOR_RELEASES")+projectName, tagName, releaseObj);
					}
					if(hasNextpage){
						releasesJsonArr = GetIssueDetailsForProjects.checkIfnewPageHasData(++count,releasesURL.toString());
						if(releasesJsonArr.size() > 0){
							hasNextpage = true;
						}
					}
				}
				isValidProject = true;
			}
		}
		catch(Exception e){
			System.out.println("Exception in collectReleaseDetails"+ e);
			System.exit(1);
		}
		return isValidProject;
	}
}
