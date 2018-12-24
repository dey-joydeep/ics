package com.jd.app.db.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jd.app.db.entity.common.CreateUpdateTSColumns;
import com.jd.app.shared.constant.enums.Gender;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends CreateUpdateTSColumns {

	private static final long serialVersionUID = 1L;

	public User(String username) {
		this.username = username;
	}

	@Id
	@Column(name = "username")
	private String username;

	@JoinColumn(name = "login_id")
	@OneToOne(cascade = CascadeType.ALL)
	private Login login;

	@Column(name = "firstname")
	private String firstname;

	@Column(name = "lastname")
	private String lastname;

	@Enumerated
	@Column(name = "gender", columnDefinition = "tinyint")
	private Gender gender;

	@Column(name = "avatar_path")
	private String avatarPath;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "sender", orphanRemoval = true)
	private Set<Message> messages;
}
