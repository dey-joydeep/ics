package com.jd.app.db.entity;

import java.time.ZonedDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jd.app.db.entity.common.CreateUpdateDeleteTSColumns;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "login")
@EqualsAndHashCode(callSuper = false)
public class Login extends CreateUpdateDeleteTSColumns {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "email")
	private String email;

	@Column(name = "password")
	private String password;

	@Column(name = "last_login_at", columnDefinition = "DATETIME")
	private ZonedDateTime lastLoginAt;

	@Column(name = "last_online_at", columnDefinition = "DATETIME")
	private ZonedDateTime lastOnlineAt;

	@Column(name = "has_ip_restriction")
	private boolean ipRestricted;

	@Column(name = "allowed_ip")
	private String allowedIp;

	@OneToOne(mappedBy = "login", cascade = CascadeType.ALL)
	private User user;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "login", orphanRemoval = true)
	private Set<LoginSession> loginSessions;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "login", orphanRemoval = true)
	private Set<AccessLog> accessLog;
}
