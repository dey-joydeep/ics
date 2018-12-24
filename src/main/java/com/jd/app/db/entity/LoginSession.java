package com.jd.app.db.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.jd.app.db.entity.common.CommonEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "login_session")
@EqualsAndHashCode(callSuper = false)
public class LoginSession extends CommonEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuid2")
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@Column(name = "session_id", columnDefinition = "BINARY(16)")
	private String sessionId;

	@JoinColumn(name = "login_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Login login;

	@Column(name = "session_expiry_at")
	private ZonedDateTime sessionExpiryAt;

	@Column(name = "device_info")
	private String deviceInfo;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "last_accessed_at")
	private ZonedDateTime lastAccessedAt;
}