package com.jd.app.db.entity.common;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Common columns for entity having track of create or update time
 * 
 * @author Joydeep Dey
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = false)
public class CreateUpdateTSColumns extends CommonEntity {

	private static final long serialVersionUID = -7887717628165506L;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
