package com.fastcode.ldapimport.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


@Repository
public class GroupDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	//groupsList String[] array
//	{ldapId, groupName, created, lastUpdated};

	public int[] addGroups(List<String[]> groupsList, String INSERT_SQL) {
		return this.jdbcTemplate.batchUpdate(INSERT_SQL,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, groupsList.get(i)[1]);
					}

					@Override
					public int getBatchSize() {
						return groupsList.size();
					}
				});
	}

	//groupsList String[] array
//{ldapId, groupName, created, lastUpdated};

	public int[] addGroupHistory(List<String[]> groupsList, String INSERT_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(INSERT_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, groupsList.get(i)[0]);
						ps.setString(2, groupsList.get(i)[1]);
						ps.setTimestamp(3, Timestamp.valueOf(groupsList.get(i)[3]));
					}

					@Override
					public int getBatchSize() {
						return groupsList.size();
					}
				});
	}

	//groupsList String[] array
//{ldapId, groupName, created, lastUpdated}; added groupName value from history table to this array as the last element

	public int[] updateGroups(List<String[]> updateGroupsList, String UPDATE_SQL) {
		return this.jdbcTemplate.batchUpdate(UPDATE_SQL,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, updateGroupsList.get(i)[1]);
						ps.setString(2, updateGroupsList.get(i)[4]);

					}

					@Override
					public int getBatchSize() {
						return updateGroupsList.size();
					}
				});
	}

	//groupsList String[] array
//{ldapId, groupName, created, lastUpdated};

	public int[] updateGroupHistory(List<String[]> updateGroupsList, String UPDATE_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(UPDATE_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						ps.setString(1, updateGroupsList.get(i)[1]);
						ps.setTimestamp(2, Timestamp.valueOf(updateGroupsList.get(i)[3]));
						ps.setString(3, updateGroupsList.get(i)[0]);
					}

					@Override
					public int getBatchSize() {
						return updateGroupsList.size();
					}
				});
	}

	// Map Object has the following information from ldap_group_history table
	// id, ldap_id, user_name, creation_time_stamp, modification_time_stamp

	public int[] deleteGroups(List<Map<String, Object>> deleteGroupsList, String DELETE_SQL) {
		return this.jdbcTemplate.batchUpdate(DELETE_SQL,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {

						final String[] name = {null};

						deleteGroupsList.get(i).forEach((key, value) -> {
							if(key.equals("name")) {
								name[0] = (String) value;
							}
						});

						ps.setString(1, name[0]);
					}

					@Override
					public int getBatchSize() {
						return deleteGroupsList.size();
					}
				});
	}

	// Map Object has the following information from ldap_user_history table
	// id, ldap_id, user_name, creation_time_stamp, modification_time_stamp

	public int[] deleteGroupHistory(List<Map<String, Object>> deleteGroupsList, String UPDATE_SQL_HISTORY) {
		return this.jdbcTemplate.batchUpdate(UPDATE_SQL_HISTORY,
				new BatchPreparedStatementSetter() {

					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {

						final String[] ldapId = {null};

						deleteGroupsList.get(i).forEach((key, value) -> {
							if(key.equals("ldap_id")) {
								ldapId[0] = (String) value;
							}
						});

						ps.setString(1, ldapId[0]);
					}

					@Override
					public int getBatchSize() {
						return deleteGroupsList.size();
					}
				});
	}


	public List<Map<String, Object>> getAllGroupHistoryData() {

		return jdbcTemplate.queryForList("select * from ldap_role_history");
	}


}
