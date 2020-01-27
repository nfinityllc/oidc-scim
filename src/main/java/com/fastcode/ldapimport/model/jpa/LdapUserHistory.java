package com.fastcode.ldapimport.model.jpa;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

@Entity
@Table(name = "ldap_user_history")
public class LdapUserHistory implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String ldapId;
	private String userName;
	private Timestamp creationTimeStamp;
    private Timestamp modificationTimeStamp;
    
    public LdapUserHistory() {}

	public LdapUserHistory(String ldapId, String userName, Timestamp creationDate, Timestamp modificationDate) {
		this.ldapId = ldapId;
		this.userName = userName;
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
	@Column(name = "user_name", nullable = false, length = 32)
	@NotNull
	@Length(max = 32, message = "The field must be less than 32 characters")
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Basic
    @Column(name = "ldap_id", nullable = false, length = 32)
    @NotNull
    @Length(max = 32, message = "The field must be less than 32 characters")
	public String getldapId() {
		return ldapId;
	}
	public void setldapId(String oidcId) {
		this.ldapId = ldapId;
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
