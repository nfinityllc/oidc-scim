package com.fastcode.ldapimport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.springframework.stereotype.Component;
import com.fastcode.ldapimport.info.LDAPInput;

@Component
public class LDAPUtilityRunner {

	private static final String LDAP_DATE_FORMAT = "yyyyMMddHHmmss";

	public List<String[]> loadLDAPUsersOrGroups(String type, LDAPInput ldapInput, String logonName) {
		String[] returnInputAttributes = new String[] {"sn", "givenName", "mail", logonName, "createTimeStamp", "modifyTimeStamp"};

		Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL, ldapInput.getUrl());
		properties.put(Context.SECURITY_AUTHENTICATION, "simple");
		properties.put(Context.SECURITY_PRINCIPAL, ldapInput.getUserId()); 
		properties.put(Context.SECURITY_CREDENTIALS, ldapInput.getPassword());

		if ("users".equalsIgnoreCase(type)) {
			return getAllLDAPUsers(properties, ldapInput.getUserSearchBase(),  ldapInput.getUserSearchFilter(), returnInputAttributes, logonName);
		}

		else if ("groups".equalsIgnoreCase(type))
        {
            return getAllLDAPGroups(properties, ldapInput.getGroupSearchBase(), "(&(" + ldapInput.getGroupSearchFilter() +")(cn=*))", new String[] {"cn", "createTimeStamp", "modifyTimeStamp"});
		}
		return null;
	}

	private List<String[]> getAllLDAPUsers(Properties properties, String searchBase, String searchFilter, String[] returnAttributes, String logonName) {
        List<String[]> usersInfoList = new ArrayList<>();
        Timestamp creationDate = null, modificationDate = null;
        try {
            DirContext context = new InitialDirContext(properties);
            SearchControls searchCtrls = new SearchControls();
            searchCtrls.setReturningAttributes(returnAttributes);
            searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<SearchResult> result = context.search(searchBase, searchFilter, searchCtrls);
            if (result != null) {
                while (result.hasMore()) {
                    SearchResult rslt = result.next();
                    Attributes attrs = rslt.getAttributes();
                    if (attrs != null && attrs.get(logonName) != null) {
                        // Check that all the attributes are present and that they have data
                        // Add a record to the userInfoList only if the attributes exist and are not null

                        if((attrs.get("givenName")!= null) && (attrs.get("sn") != null) && (attrs.get("mail") != null) && (attrs.get("createTimeStamp")!=null) && (attrs.get("modifyTimeStamp")!=null))
                        {
                            Attribute at = attrs.get("givenName");
                            String firstName = attrs.get("givenName").toString().split(":")[1].trim();
                            String lastName = attrs.get("sn").toString().split(":")[1].trim();
                            String email = attrs.get("mail").toString().split(":")[1].trim();
                            String logonNameValue = attrs.get(logonName).toString().split(":")[1].trim();
                            creationDate = Utils.getDateFromLDAP(attrs.get("createTimeStamp").toString().split(":")[1].split("\\.")[0].trim(), LDAP_DATE_FORMAT);
                            if (attrs.get("modifyTimeStamp") != null) {
                                modificationDate = Utils.getDateFromLDAP(attrs.get("modifyTimeStamp").toString().split(":")[1].split("\\.")[0].trim(), LDAP_DATE_FORMAT);
                            }

                            //ldapId, firstName, lastName, email, userName, created, lastUpdated}

                            String[] values = {logonNameValue, firstName, lastName, email, logonNameValue, creationDate.toString(), modificationDate.toString()};
                            usersInfoList.add(values);
                        }
                    }
                } 
                context.close(); 
            }
        } catch (NamingException ne) {
            System.out.println("Problem searching directory: " + ne);
        }
        return usersInfoList;
    } 
     
    private List<String[]> getAllLDAPGroups(Properties properties, String searchBase, String searchFilter, String[] returnAttributes) {

	    List<String[]> rolesInfoList =  new ArrayList<>();
        Timestamp creationDate = null, modificationDate = null;
        try { 
            DirContext context = new InitialDirContext(properties); 
            SearchControls searchCtrls = new SearchControls(); 
            searchCtrls.setReturningAttributes(returnAttributes); 
            searchCtrls.setSearchScope(SearchControls.SUBTREE_SCOPE); 
             
            NamingEnumeration<SearchResult> result = context.search(searchBase, searchFilter, searchCtrls); 
            if (result != null) { 

                while (result.hasMore()) {
                    SearchResult rslt = (SearchResult) result.next();
                    Attributes attrs = rslt.getAttributes();

                    if (attrs != null && attrs.get("cn") != null) {
                        // Check that all the attributes are present and that they have data
                        // Add a record to the userInfoList only if the attributes exist and are not null

                        if((attrs.get("createTimeStamp")!=null) && (attrs.get("modifyTimeStamp")!=null))
                        {
                            Attribute cnAttribute = attrs.get("cn");
                            String roleName = cnAttribute.toString().split(":")[1].trim();
                            creationDate = Utils.getDateFromLDAP(attrs.get("createTimeStamp").toString().split(":")[1].split("\\.")[0], LDAP_DATE_FORMAT);
                            if (attrs.get("modifyTimeStamp") != null) {
                                modificationDate = Utils.getDateFromLDAP(attrs.get("modifyTimeStamp").toString().split(":")[1].split("\\.")[0], LDAP_DATE_FORMAT);
                            }
                            // {ldapId, groupName, created, lastUpdated};
                            //use roleName as the unique ID - ldapId
                            String[] results = {roleName, roleName, creationDate.toString(), modificationDate.toString()};
                            rolesInfoList.add(results);
                        }
                    }
                }
                context.close(); 
            }
        } catch (NamingException ne) { 
            System.out.println("Problem searching directory: " + ne);    
        }
        return rolesInfoList; 
    }

}
