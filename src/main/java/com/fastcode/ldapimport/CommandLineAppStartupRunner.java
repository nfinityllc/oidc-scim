package com.fastcode.ldapimport;

import com.fastcode.ldapimport.info.LDAPInput;
import com.fastcode.ldapimport.repository.GroupDAO;
import com.fastcode.ldapimport.repository.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.Timestamp;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {

	@Autowired
	UserDAO userDAO;

	@Autowired
	GroupDAO groupDAO;

	@Autowired
	LDAPUtilityRunner lUtility;

	@Override
    public void run(String...args) throws Exception {

		String userTable = null;
		Map<String, String> mappingResults = null;
		List<String> fcMandatoryFields = new ArrayList<String>();
		// Ask for customer's User Table Mapping to the required fields of fastCode User Table
		fcMandatoryFields.add("user_name");
		fcMandatoryFields.add("email_address");
		fcMandatoryFields.add("first_name");
		fcMandatoryFields.add("last_name");

		Boolean ownAuthTable = true;

		LDAPInput ldapInput = new LDAPInput();
		Scanner scanner = new Scanner(System.in);

		ldapInput.setUrl(getLdapInput(scanner, "Full LDAP Url With Base Path"));
		ldapInput.setUserId(getLdapInput(scanner, "User ID"));
		ldapInput.setPassword(getLdapInput(scanner, "Password"));
		ldapInput.setUserSearchBase(getLdapInput(scanner, "User Search Base"));
		ldapInput.setUserSearchFilter(getLdapInput(scanner, "User Search Filter"));
		ldapInput.setGroupSearchBase(getLdapInput(scanner, "Group Search Base"));
		ldapInput.setGroupSearchFilter(getLdapInput(scanner, "Group Search Filter"));

		System.out.print("\nWhat information do you store in LDAP?");
		System.out.print("\n1. Users only: ");
		System.out.print("\n2. Users and Groups: ");
		System.out.print("\nEnter 1 or 2: ");

		int value = scanner.nextInt();

		while (value < 1 || value > 2) {
			System.out.println("\nInvalid Input. \nPlease enter the value again :");
			value = scanner.nextInt();
		}

		scanner.nextLine();

		System.out.print("\nWhich field in your LDAP Directory are you using for User Logon Name?");
		System.out.print("\nTypically, this could be userPrincipalName or sAMAccountName in Active Directory" +
				"and uid for most LDAP implementations based on OpenLDAP.");
		System.out.print("\nPlease enter your User Logon Name:");

		String logonName = scanner.nextLine();

		while(logonName == null || logonName.isEmpty())
		{
			System.out.print("\nThe Logon Name you entered is invalid.  Please enter the Login Name again:");
			logonName = scanner.nextLine();
		}

		System.out.print("\nDo you have your own user table? (y/n)");

		String tmpString = scanner.nextLine();

		while(tmpString == null || tmpString.isEmpty() || !((tmpString.matches("(?i:y) | (?i:yes)")))) // case-insensitive
		{
			System.out.print("\nYour answer is invalid. Please only enter (y or n):");
			tmpString = scanner.nextLine();
		}

		if (tmpString.equalsIgnoreCase("y") || tmpString.equalsIgnoreCase("yes")) {
			System.out.print("\nEnter table name :");
			userTable = scanner.nextLine();

			while(userTable == null || userTable.isEmpty())
			{
				System.out.print("\nThe table name entered is invalid.  Please enter the table name again :");
				userTable = scanner.nextLine();
			}
			// Now validate that the customer's user table exists in the database
			while(!userDAO.doesTableExistInDb(userTable)) {

				System.out.print("\nThe table name entered does not exist in your database.  Please enter the table name again :");
				userTable = scanner.nextLine();
			}

			ldapInput.setAuthenticationTable(userTable);

			// Key - fcMandatoryFields, Value - Customer's User table column names
			mappingResults = displayAuthFieldsAndGetMapping(fcMandatoryFields, userTable);
		}

		// Customers does not have their own User table
		else {
			ownAuthTable = false;
			mappingResults = new HashMap<String, String>();

			//key and value will be the same after the for statement below
			for (String mandField : fcMandatoryFields) {
				mappingResults.put(mandField, mandField);
			}
			// fastCode by default uses the name "user" for it's user table
            ldapInput.setAuthenticationTable("user");
		}

		if((value == 1) || ((value == 2) && (ownAuthTable))) {

			//// Storing only user information in LDAP (OR) user & group information in LDAP with own
			// user table in the application database schema
			// We will need to stoe user information in the application database under these two scenarios

			// First, get all the users in the User History table in the database
			// We have history tables for User and Group that will track help us understand which
			// user and group records have changed in LDAP since the last LDAP Import tun

			List<Map<String, Object>> rows = userDAO.getAllUserHitoryData();

			// One element of the list for each row of data
			// The Map (inside the list) contains key-value pairs - each key-value is the column name and column data

			// Get the list of all LDAP users
			List<String[]> userList = lUtility.loadLDAPUsersOrGroups("users", ldapInput, logonName);

// 			Format of each user String[] array - {ldapId, firstName, lastName, email, userName, created, lastUpdated};

			// Get the list of users from ldap who don't exist in the user history table
			// If the user doesn't exist, we need to add the user to the User Table and User History Table

			List<String[]> exclusiveldapUsers = null;

			if(userList!=null && rows !=null) {
				exclusiveldapUsers = getExclusiveldapUsersOrGroups(userList, rows);
			}
			if((exclusiveldapUsers !=null) && (exclusiveldapUsers.size()!=0)) {

			// Insert into user table in database

			String INSERT_SQL_USER  = "INSERT INTO blog." + "\""+ ldapInput.getAuthenticationTable() + "\"" +  " (" + mappingResults.get("email_address") + "," + mappingResults.get("user_name") + "," + mappingResults.get("first_name") + "," + mappingResults.get("last_name") + ") VALUES (?, ?, ?, ?)";
			System.out.print(INSERT_SQL_USER);
			userDAO.addUsers(exclusiveldapUsers, INSERT_SQL_USER);

			String INSERT_SQL_USER_HISTORY  = "INSERT INTO ldap_user_history (ldap_id, user_name, modification_time_stamp) VALUES (?,?,?)";
			userDAO.addUserHistory(exclusiveldapUsers, INSERT_SQL_USER_HISTORY);

			}

			// If he exists, we need to check the last update timestamp.  If the timestamps are different, we need to update the
			// user in the User table and the User History table. If timestamps are the same, don't do anything

			List<String[]> updatableUsers = getUpdatableldapUsersOrGroups(userList, rows, true);

			if(updatableUsers.size()!=0) {

			String UPDATE_SQL_USER = "UPDATE " + "blog." + "\""+ ldapInput.getAuthenticationTable() + "\"" + " set " + mappingResults.get("user_name") + "= ?," +    mappingResults.get("email_address") + "= ?," + mappingResults.get("first_name") + "= ?," + mappingResults.get("last_name") + "= ?" + " where user_name = ?";
			String UPDATE_SQL_USER_HISTORY = "UPDATE ldap_user_history set " + "user_name=?" + "," + "modification_time_stamp= ? where ldap_id = ?";

			userDAO.updateUsers(updatableUsers, UPDATE_SQL_USER);
			userDAO.updateUserHistory(updatableUsers, UPDATE_SQL_USER_HISTORY);

			}

			// Once we are done with the above, we need to do it in the reverse directtion to check whether every user in the User History
			// table exists in our resultset from ldap Provider. If a user in the History table does not exist in the result set, we need to delete
			// the user from the User table and the User History table

			List<Map<String, Object>> exclusiveDBUsers = getExclusiveDBUsersOrGroups(rows, userList);

			if(exclusiveDBUsers.size()!=0) {

			String DELETE_SQL_USER = "DELETE from " + "blog." + "\"" + ldapInput.getAuthenticationTable() + "\"" + " where user_name = ?";
			String DELETE_SQL_USER_HISTORY = "DELETE from ldap_user_history where ldap_id = ?";

			userDAO.deleteUsers(exclusiveDBUsers, DELETE_SQL_USER);
			userDAO.deleteUserHistory(exclusiveDBUsers, DELETE_SQL_USER_HISTORY);

			}

		}


		if((value ==2))

		{
			// Import Groups

			// First get all the users in the User History table in the database
			List<Map<String, Object>> rows = groupDAO.getAllGroupHistoryData();

			// One element of the list for each row of data
			// The Map (inside the list) contains key-value pairs - each key-value is the column name and column data

			List<String[]> groupList = lUtility.loadLDAPUsersOrGroups("groups", ldapInput, logonName);

			// Get the list of users from ldap Provider who don't exist in the user history table
			// If he doesn't exist, we need to add the user to the User Table and User History Table

			List<String[]> exclusiveldapGroups = getExclusiveldapUsersOrGroups(groupList, rows);

			if(exclusiveldapGroups.size()!=0) {

			// Insert into Role table (name) where User.user_name = exclusiveldapUsers[4]

			String INSERT_SQL_GROUP  = "INSERT INTO role (name) VALUES (?)";
			groupDAO.addGroups(exclusiveldapGroups, INSERT_SQL_GROUP);

			String INSERT_SQL_GROUP_HISTORY  = "INSERT INTO ldap_role_history (ldap_id, name, modification_time_stamp) VALUES (?,?,?)";
			groupDAO.addGroupHistory(exclusiveldapGroups, INSERT_SQL_GROUP_HISTORY);

			}

			// If he exists, we need to check the last update timestamp.  If the timestamps are different, we need to update the
			// user in the User table and the User History table. If timestamps are the same, don't do anything

			List<String[]> updatableGroups = getUpdatableldapUsersOrGroups(groupList, rows, false);

			if(updatableGroups.size() !=0) {

			String UPDATE_SQL_GROUP = "UPDATE role set name = ? where name = ?";
			String UPDATE_SQL_GROUP_HISTORY = "UPDATE ldap_role_history set name=?, modification_time_stamp= ? where ldap_id = ?";

			groupDAO.updateGroups(updatableGroups, UPDATE_SQL_GROUP);
			groupDAO.updateGroupHistory(updatableGroups, UPDATE_SQL_GROUP_HISTORY);

			}

			// Once we are done with the above, we need to do it in the reverse direction to check whether every user in the User History
			// table exists in our result set from ldap Provider. If a user in the History table does not exist in the result set, we need to delete
			// the user from the User table and the User History table

			List<Map<String, Object>> exclusiveDBGroups = getExclusiveDBUsersOrGroups(rows, groupList);

			if(exclusiveDBGroups.size()!=0) {

			String DELETE_SQL_GROUP = "DELETE from role where name = ?";
			String DELETE_SQL_GROUP_HISTORY = "DELETE from ldap_role_history where ldap_id = ?";

			groupDAO.deleteGroups(exclusiveDBGroups, DELETE_SQL_GROUP);
			groupDAO.deleteGroupHistory(exclusiveDBGroups, DELETE_SQL_GROUP_HISTORY);

			}

		}

    }

    
    public static String getLdapInput(Scanner scanner, String inputStr) {
		System.out.print("Please Enter Value for " + inputStr + ": ");
		return scanner.nextLine();
	}



	public List<String[]> getExclusiveldapUsersOrGroups(List<String[]> userOrGroupList, List<Map<String, Object>> rows)
	{
		List<String[]> resultList = new ArrayList<String[]>();

		Boolean exclusiveElement = true;

		for (String[] ldapUserOrGroup : userOrGroupList) {

			outer:
			for (Map<String, Object> map : rows) {

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					if (entry.getKey().equals("ldap_id")) {
						if (ldapUserOrGroup[0].equals(entry.getValue())) {
							exclusiveElement = false;
							break outer;
						}
					}
				}
			}

			// The "break outer" statement gets us here

			if(exclusiveElement) {
				resultList.add(ldapUserOrGroup);
			}
			else {
				exclusiveElement = true;
			}
		}
		return resultList;
	}

    public List<String[]> getUpdatableldapUsersOrGroups(List<String[]> userOrGroupList, List<Map<String, Object>> rows, Boolean isUsers)
    {
        List<String[]> resultList = new ArrayList<String[]>();
        int userNameOrGroupNameIndex = isUsers ? 6 : 3;
        String name = isUsers ? "user_name" : "name";

        for (String[] ldapUserOrGroup : userOrGroupList) {

            for (Map<String, Object> row : rows) {

                if ((row.get("ldap_id")).equals(ldapUserOrGroup[0])) {
                    // Check the modification_time_stamp now
                    if ((Timestamp.valueOf(ldapUserOrGroup[userNameOrGroupNameIndex]).after((Timestamp) row.get("modification_time_stamp")))) {
                        List<String> as = new ArrayList<String>(Arrays.asList(ldapUserOrGroup));
                        as.add((String) row.get(name));
                        resultList.add(as.toArray(new String[as.size()]));
                        break;

                    }
                }
            }
        }

        return resultList;
    }

	public List<Map<String, Object>> getExclusiveDBUsersOrGroups(List<Map<String, Object>> rows, List<String[]> userOrGroupList)
	{
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

		Boolean exclusiveElement = true;

		for (Map<String, Object> map : rows) {

			outer:
			for (Map.Entry<String, Object> entry : map.entrySet()) {

						if (entry.getKey().equals("ldap_id")) {
							for (String[] ldapUserOrGroup : userOrGroupList) {
								if (ldapUserOrGroup[0].equals(entry.getValue())) {
								exclusiveElement = false;
								break outer;
								}
							}
						}
			}

			// The "break outer" statement gets us here

			if(exclusiveElement) {
				resultList.add(map);
			}
			else {
				exclusiveElement = true;
			}
		}
		return resultList;
	}


	public Map<String, String> displayAuthFieldsAndGetMapping(List<String> fcMandatoryFields, String authenticationTable)
	{

		Map<String, String> mapping = new HashMap<>();

		UserDAO userDAO = new UserDAO();
		ArrayList<String> columnNames = userDAO.getColumnNames(authenticationTable);

		Scanner scanner = new Scanner(System.in);

		for(String fcFieldsEntry:fcMandatoryFields)
		{
			int index = 1;
			StringBuilder builder = new StringBuilder();
			for (String f : columnNames) {
				builder.append(MessageFormat.format("{0}.{1} ", index, f));
				index++;
			}

			System.out.println("\n Select field you want to map on "+ fcFieldsEntry +" by typing its corresponding number : ");
			System.out.println(builder.toString());

			int input = scanner.nextInt();
			String selected = columnNames.get(input - 1);
			mapping.put(fcFieldsEntry, selected);
			columnNames.remove(input-1);
			fcMandatoryFields.remove(selected);
		}
		return mapping;
	}


	private Timestamp convertISO8601ToTimestamp(String ISO8601String) {
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date date = null;
		try {
			date = df1.parse(ISO8601String);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Timestamp(date.getTime());
	}

}
