package com.jd.app.db.entity.common;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Common columns for entity having the track of create/update/delete with
 * timestamp and the user operating the action,
 * 
 * @author Joydeep Dey
 */
@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = false)
public class WhoCloumns extends CreateUpdateDeleteTSColumns {

	private static final long serialVersionUID = 1383519505195193879L;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "deleted_by")
	private String deletedBy;
}
