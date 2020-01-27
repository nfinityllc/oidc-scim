package com.fastcode.ldapimport.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
public class UserDAO {

	@Autowired
    private JdbcTemplate jdbcTemplate;

	//By default SpringApplication will convert any command line option arguments (starting with “--”, e.g. --server.port=9000)
	//to a property and add it to the Spring Environment.

	//userList String[] array
	//{ldapId, firstName, lastName, email, userName, created, lastUpdated}

    public int[] addUsers(List<String[]> usersList, String INSERT_SQL) {
    	return this.jdbcTemplate.batchUpdate(INSERT_SQL,
			new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setString(1, usersList.get(i)[3]);
					ps.setString(2, usersList.get(i)[4]);
					ps.setString(3, usersList.get(i)[1]);
					ps.setString(4, usersList.get(i)[2]);
				}
				
				@Override
				public int getBatchSize() {
					return usersList.size();
				}
		});
    }

	//userList String[] array
	//{ldapId, firstName, lastName, email, userName, createDate, lastUpdated}

	public int[] addUserHistory(List<String[]> usersList, String INSERT_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(INSERT_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, usersList.get(i)[0]);
						ps.setString(2, usersList.get(i)[4]);
						ps.setTimestamp(3, Timestamp.valueOf(usersList.get(i)[6]));
					}

					@Override
					public int getBatchSize() {
						return usersList.size();
					}
				});
	}

	//userList String[] array
	//{ldapId, firstName, lastName, email, userName, createDate, lastUpdated}; Added the userName value from history table at the end of array

    public int[] updateUsers(List<String[]> updateUsersList, String UPDATE_SQL) {
    	return this.jdbcTemplate.batchUpdate(UPDATE_SQL,
			new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setString(1, updateUsersList.get(i)[4]);
					ps.setString(2, updateUsersList.get(i)[3]);
					ps.setString(3, updateUsersList.get(i)[1]);
					ps.setString(4, updateUsersList.get(i)[2]);
					ps.setString(5, updateUsersList.get(i)[7]);

				}
				
				@Override
				public int getBatchSize() {
					return updateUsersList.size();
				}
		});
    }

	//userList String[] array
	//{ldapId, firstName, lastName, email, userName, createDate, lastUpdated}

	public int[] updateUserHistory(List<String[]> updateUsersList, String UPDATE_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(UPDATE_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, updateUsersList.get(i)[4]);
						ps.setTimestamp(2, Timestamp.valueOf(updateUsersList.get(i)[6]));
						ps.setString(3, updateUsersList.get(i)[0]);
					}

					@Override
					public int getBatchSize() {
						return updateUsersList.size();
					}
				});
	}

	// Map Object has the following information from ldap_user_history table
	// id, ldap_id, user_name, creation_time_stamp, modification_time_stamp

	public int[] deleteUsers(List<Map<String, Object>> deleteUsersList, String DELETE_SQL) {
		return this.jdbcTemplate.batchUpdate(DELETE_SQL,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {

						final String[] userName = {null};

						deleteUsersList.get(i).forEach((key, value) -> {
							if(key.equals("user_name")) {
								userName[0] = (String) value;
							}
						});

						ps.setString(1, userName[0]);
					}

					@Override
					public int getBatchSize() {
						return deleteUsersList.size();
					}
				});
	}

	// Map Object has the following information from ldap_user_history table
	// id, ldap_id, user_name, creation_time_stamp, modification_time_stamp

	public int[] deleteUserHistory(List<Map<String, Object>> deleteUsersList, String UPDATE_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(UPDATE_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {

						final String[] ldapId = {null};

						deleteUsersList.get(i).forEach((key, value) -> {
							if(key.equals("ldap_id")) {
								ldapId[0] = (String) value;
							}
						});


						ps.setString(1, ldapId[0]);
					}

					@Override
					public int getBatchSize() {
						return deleteUsersList.size();
					}
				});
	}


	public Boolean doesTableExistInDb (String tableName) {

		Connection con = null;

		try {
			con = jdbcTemplate.getDataSource().getConnection();

			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getTables(null, null, tableName, null);
			if (rs.next()) {
				//Table exists
				return true;
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	public List<Map<String, Object>> getAllUserHitoryData() {

		return jdbcTemplate.queryForList("select * from ldap_user_history");
	}

	public ArrayList<String> getColumnNames (String tableName) {

    	List<String> columnNames = new ArrayList<>();

		Connection con = null;
		try {
			con = jdbcTemplate.getDataSource().getConnection();


		Statement statement = con.createStatement();
		ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();

		// The column count starts from 1
		for (int i = 1; i <= columnCount; i++ ) {
			String name = rsmd.getColumnName(i);
			columnNames.add(name);
		}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		finally {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return (ArrayList<String>) columnNames;
	}

}
