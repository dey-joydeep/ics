package com.jd.app.db.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jd.app.db.entity.common.CommonEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Joydeep Dey
 */
@Data
@Entity
@Table(name = "access_log")
@EqualsAndHashCode(callSuper = false)
public class AccessLog extends CommonEntity {

	private static final long serialVersionUID = 1373435521385333761L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "log_id")
	private long logId;

	@JoinColumn(name = "login_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Login login;

	@Column(name = "user_agent")
	private String userAgent;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "is_auto_login_on")
	private boolean autoLoginOn;

	@Column(name = "accessed_at")
	private Instant accessedAt;

	@Column(name = "expired_at")
	private Instant expiredAt;
}
