/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.junit4.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
@SuppressWarnings("rawtypes")
public class InjectResourcesTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.fromTestPackage()
                                                .bindResource("bar", "hello1")
                                                .bindResource("java:comp/env/baz", "hello2")
                                                .setEjbFactory(ip -> new DummySessionBean("ping"))
                                                .setPersistenceUnitFactory(getPUFactory())
                                                .setPersistenceContextFactory(getPCFactory()).build();

    @Test
    public void testResourceInjection() {
        FooResources foo = weld.select(FooResources.class).get();
        assertEquals("hello1", foo.bar);
        assertEquals("hello2", foo.baz);
    }

    @Test
    public void testEjbInjection() {
        FooEjbs foo = weld.select(FooEjbs.class).get();
        assertEquals("ping", foo.dummySessionBean.id);
    }

    @Test
    public void testJpaInjection() {
        FooJpa foo = weld.select(FooJpa.class).get();
        assertNotNull(foo.entityManagerFactory);
        assertFalse(foo.entityManagerFactory.isOpen());
        assertNotNull(foo.entityManager);
        assertFalse(foo.entityManager.isOpen());
    }

    // Mock objects

    static Function<InjectionPoint, Object> getPCFactory() {
        return ip -> new EntityManager() {

            @Override
            public <T> T unwrap(Class<T> cls) {
                return null;
            }

            @Override
            public void setProperty(String propertyName, Object value) {
            }

            @Override
            public void setFlushMode(FlushModeType flushMode) {
            }

            @Override
            public void remove(Object entity) {
            }

            @Override
            public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            }

            @Override
            public void refresh(Object entity, LockModeType lockMode) {
            }

            @Override
            public void refresh(Object entity, Map<String, Object> properties) {
            }

            @Override
            public void refresh(Object entity) {
            }

            @Override
            public void persist(Object entity) {
            }

            @Override
            public <T> T merge(T entity) {
                return null;
            }

            @Override
            public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
            }

            @Override
            public void lock(Object entity, LockModeType lockMode) {
            }

            @Override
            public void joinTransaction() {
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isJoinedToTransaction() {
                return false;
            }

            @Override
            public EntityTransaction getTransaction() {
                return null;
            }

            @Override
            public <T> T getReference(Class<T> entityClass, Object primaryKey) {
                return null;
            }

            @Override
            public Map<String, Object> getProperties() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public LockModeType getLockMode(Object entity) {
                return null;
            }

            @Override
            public FlushModeType getFlushMode() {
                return null;
            }

            @Override
            public EntityManagerFactory getEntityManagerFactory() {
                return null;
            }

            @Override
            public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
                return null;
            }

            @Override
            public EntityGraph<?> getEntityGraph(String graphName) {
                return null;
            }

            @Override
            public Object getDelegate() {
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public void flush() {
            }

            @Override
            public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
                return null;
            }

            @Override
            public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
                return null;
            }

            @Override
            public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
                return null;
            }

            @Override
            public <T> T find(Class<T> entityClass, Object primaryKey) {
                return null;
            }

            @Override
            public void detach(Object entity) {
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
                return null;
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
                return null;
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
                return null;
            }

            @Override
            public Query createQuery(CriteriaDelete deleteQuery) {
                return null;
            }

            @Override
            public Query createQuery(CriteriaUpdate updateQuery) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
                return null;
            }

            @Override
            public Query createQuery(String qlString) {
                return null;
            }

            @Override
            public Query createNativeQuery(String sqlString, String resultSetMapping) {
                return null;
            }

            @Override
            public Query createNativeQuery(String sqlString, Class resultClass) {
                return null;
            }

            @Override
            public Query createNativeQuery(String sqlString) {
                return null;
            }

            @Override
            public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
                return null;
            }

            @Override
            public Query createNamedQuery(String name) {
                return null;
            }

            @Override
            public EntityGraph<?> createEntityGraph(String graphName) {
                return null;
            }

            @Override
            public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
                return null;
            }

            @Override
            public boolean contains(Object entity) {
                return false;
            }

            @Override
            public void close() {
            }

            @Override
            public void clear() {
            }
        };
    }

    static Function<InjectionPoint, Object> getPUFactory() {
        return ip -> new EntityManagerFactory() {

            @Override
            public <T> T unwrap(Class<T> cls) {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public Map<String, Object> getProperties() {
                return null;
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public Cache getCache() {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(Map map) {
                return null;
            }

            @Override
            public EntityManager createEntityManager() {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public void addNamedQuery(String name, Query query) {
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
            }
        };
    }

}
