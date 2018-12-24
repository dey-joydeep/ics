package com.jd.app.db.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.jd.app.db.entity.common.CommonEntity;
import com.jd.app.db.entity.common.CreateUpdateDeleteTSColumns;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Repository
public class CommonDao implements Serializable {

	private static final long serialVersionUID = -749545283292765905L;

	@PersistenceContext
	private EntityManager em;

	private Class<?> clazz;

	protected final void setClass(final Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * @param id
	 * @return result
	 */
	public CommonEntity findById(final Object id) {
		Assert.notNull(id, "ID is mandatory");
		return (CommonEntity) em.find(clazz, id);
	}

	/**
	 * @param id
	 * @return result
	 */
	public CommonEntity findActiveById(final Object id) {
		Assert.notNull(id, "ID is mandatory");
		CommonEntity obj = findById(id);
		if (!(obj instanceof CreateUpdateDeleteTSColumns)) {
			return obj;
		}
		CreateUpdateDeleteTSColumns entity = (CreateUpdateDeleteTSColumns) obj;
		return entity.isActive() ? obj : null;
	}

	/**
	 * @return result
	 */
	@SuppressWarnings("unchecked")
	public List<? extends CommonEntity> findAll() {
		return em.createQuery("FROM " + clazz.getName()).getResultList();
	}

	/**
	 * @return result
	 */
	@SuppressWarnings("unchecked")
	public List<? extends CommonEntity> findAllActives() {
		return em.createQuery("FROM " + clazz.getName() + " WHERE active IS TRUE").getResultList();
	}

	/**
	 * @return result
	 */
	@SuppressWarnings("unchecked")
	public List<? extends CommonEntity> findAllInactives() {
		return em.createQuery("FROM " + clazz.getName() + " WHERE active IS FALSE").getResultList();
	}

	/**
	 * @param hql
	 * @param paramValueMap
	 * @return result
	 */
	public List<? extends CommonEntity> find(String hql, Map<String, Object> paramValueMap) {
		return find(hql, paramValueMap, 0);
	}

	/**
	 * @param hql
	 * @param paramValueMap
	 * @return result
	 */
	@SuppressWarnings("unchecked")
	public List<? extends CommonEntity> find(String hql, Map<String, Object> paramValueMap, int maxResult) {
		Query query = em.createQuery(hql);
		if (paramValueMap != null)
			setParamaeters(query, paramValueMap);
		if (maxResult > 0)
			query.setMaxResults(maxResult);
		log.info("Executing query: " + HibernateUtils.getHibernateQuery(query));
		return query.getResultList();
	}

	/**
	 * @param hql
	 * @param paramValueMap
	 * @return result
	 */
	public Object findUnique(String hql, Map<String, Object> paramValueMap) {
		Query query = em.createQuery(hql);
		query.setMaxResults(1);
		setParamaeters(query, paramValueMap);
		Object ob = null;
		try {
			ob = query.getSingleResult();
		} catch (NoResultException nre) {
			log.error(nre);
		}
		return ob;
	}

	/**
	 * @param entity
	 */
	public void create(final CommonEntity entity) {
		em.persist(entity);
	}

	/**
	 * @param entity
	 */
	public void create(final List<? extends CommonEntity> entities) {
		entities.forEach(e -> em.persist(e));
	}

	/**
	 * @param entity
	 * @return result
	 */
	public CommonEntity update(final CommonEntity entity) {
		return em.merge(entity);
	}

	/**
	 * Remove the persistence entity from the database.
	 * 
	 * @param entity
	 */
	public void delete(final CommonEntity entity) {
		em.remove(entity);
	}

	/**
	 * @param entityId
	 */
	public void deleteById(final Object entityId) {
		final CommonEntity entity = findById(entityId);
		delete(entity);
	}

	/**
	 * @param entityId
	 */
	public boolean deleteByQuery(final String hql, Map<String, Object> paramValueMap) {
		Query query = em.createQuery(hql);
		if (paramValueMap != null)
			query = setParamaeters(query, paramValueMap);
		int deletedCount = query.executeUpdate();
		return deletedCount > 0;
	}

	private static Query setParamaeters(Query query, Map<String, Object> paramValueMap) {

		for (String param : paramValueMap.keySet()) {
			Object value = paramValueMap.get(param);
//			if (value instanceof Enum<?>)
//				value = value.toString();
			query.setParameter(param, value);
		}

		return query;
	}
}
