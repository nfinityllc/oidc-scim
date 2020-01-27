package com.fastcode.ldapimport.info;

public class LDAPInput {

	private String url;
	private String userId;
	private String password;
	private String userSearchBase;
	private String userSearchFilter;
	private String groupSearchBase;
	private String groupSearchFilter;


	public String getUrl() { return url; }

	public void setUrl(String url) { this.url = url; }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserSearchBase() {
		return userSearchBase;
	}

	public void setUserSearchBase(String userSearchBase) {
		this.userSearchBase = userSearchBase;
	}

	public String getUserSearchFilter() {
		return userSearchFilter;
	}

	public void setUserSearchFilter(String userSearchFilter) {
		this.userSearchFilter = userSearchFilter;
	}

	public String getGroupSearchBase() {
		return groupSearchBase;
	}

	public void setGroupSearchBase(String groupSearchBase) {
		this.groupSearchBase = groupSearchBase;
	}

	public String getGroupSearchFilter() {
		return groupSearchFilter;
	}

	public void setGroupSearchFilter(String groupSearchFilter) {
		this.groupSearchFilter = groupSearchFilter;
	}

	public String getAuthenticationTable() {
		return authenticationTable;
	}

	public void setAuthenticationTable(String authenticationTable) {
		this.authenticationTable = authenticationTable;
	}

	private String authenticationTable;

}
