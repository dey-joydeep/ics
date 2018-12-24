package com.jd.app.db.entity.common;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Common column for entities having track of logic delete operation
 * 
 * @author Joydeep Dey
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = false)
public class CreateUpdateDeleteTSColumns extends CreateUpdateTSColumns {

	private static final long serialVersionUID = -7887717628165506L;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "active")
	private boolean active;
}
