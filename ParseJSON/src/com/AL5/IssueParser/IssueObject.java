package com.AL5.IssueParser;

import java.util.ArrayList;

public class IssueObject {
	public String created_at;
	public String closed_at;
	public String comments_count;
	public String title;
	public String body;
	public String comments_URL;
	public ArrayList<String> comments_List;
	public String getComments_URL() {
		return comments_URL;
	}
	public ArrayList<String> getComments_List() {
		return comments_List;
	}
	public void setComments_List(ArrayList<String> comments_List) {
		this.comments_List = comments_List;
	}
	public void setComments_URL(String comments_URL) {
		this.comments_URL = comments_URL;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	public String getClosed_at() {
		return closed_at;
	}
	public void setClosed_at(String closed_at) {
		this.closed_at = closed_at;
	}
	public String getComments_count() {
		return comments_count;
	}
	public void setComments_count(String comments_count) {
		this.comments_count = comments_count;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
}
