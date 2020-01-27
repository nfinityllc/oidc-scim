package com.fastcode.ldapimport.model.jpa;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "ldap_role_history")
public class LdapRoleHistory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String ldapId;
	private String name;
	private Timestamp creationTimeStamp;
	private Timestamp modificationTimeStamp;

	public LdapRoleHistory() {}

	public LdapRoleHistory(String ldapId, String name, Timestamp creationDate, Timestamp modificationDate) {
		this.ldapId = ldapId;
		this.name = name;
		this.creationTimeStamp = creationDate;
		this.modificationTimeStamp = modificationDate;
	}

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "ldap_id", nullable = false)
	@NotNull
	public String getldapId() {
		return ldapId;
	}
	public void setldapId(String ldapId) {
		this.ldapId = ldapId;
	}

	@Basic
	@Column(name = "name", nullable = false)
	@NotNull
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Basic
	@Column(name = "creation_time_stamp", nullable = true)
	public Timestamp getCreationTimeStamp() {
		return creationTimeStamp;
	}
	public void setCreationTimeStamp(Timestamp creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	@Basic
	@Column(name = "modification_time_stamp", nullable = true)
	public Timestamp getModificationTimeStamp() {
		return modificationTimeStamp;
	}
	public void setModificationTimeStamp(Timestamp modificationTimeStamp) {
		this.modificationTimeStamp = modificationTimeStamp;
	}

}
