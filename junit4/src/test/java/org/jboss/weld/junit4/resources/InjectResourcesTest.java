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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.Cache;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.ConnectionConsumer;
import jakarta.persistence.ConnectionFunction;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityHandler;
import jakarta.persistence.EntityListenerRegistration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FindOption;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockOption;
import jakarta.persistence.PersistenceUnitTransactionType;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.RefreshOption;
import jakarta.persistence.SchemaManager;
import jakarta.persistence.Statement;
import jakarta.persistence.StatementOrTypedQuery;
import jakarta.persistence.StatementReference;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.TypedQueryReference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaSelect;
import jakarta.persistence.criteria.CriteriaStatement;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.sql.ResultSetMapping;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.Nullable;

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
            .setPersistenceContextFactory(getPCFactory())
            .setPersistenceAgentFactory(getPAFactory()).build();

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
        assertNotNull(foo.entityAgent);
        assertEquals("MockEntityAgent", foo.entityAgent.toString());
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

            @Deprecated(since = "4.0", forRemoval = true)
            @Override
            public Statement createQuery(CriteriaStatement<?> statement) {
                return null;
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
            public Statement createStatement(String qlString) {
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
            public <T> EntityGraph<T> getEntityGraph(Class<T> rootType, String graphName) {
                return null;
            }

            @Override
            public Object getDelegate() {
                return null;
            }

            @Override
            public void addOption(Option option) {

            }

            @Override
            public Set<Option> getOptions() {
                return Set.of();
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
            public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
                return null;
            }

            @Override
            public <T> T get(Class<T> entityClass, Object id) {
                return null;
            }

            @Override
            public <T> T get(Class<T> entityClass, Object id, @Nullable FindOption... options) {
                return null;
            }

            @Override
            public <T> T get(EntityGraph<T> graph, Object id, @Nullable FindOption... options) {
                return null;
            }

            @Override
            public <T> List<T> getMultiple(Class<T> entityClass, List<?> ids, @Nullable FindOption... options) {
                return List.of();
            }

            @Override
            public <T> List<T> getMultiple(EntityGraph<T> graph, List<?> ids, @Nullable FindOption... options) {
                return List.of();
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
            public <T> TypedQuery<T> createQuery(String qlString, EntityGraph<T> resultGraph) {
                return null;
            }

            @Override
            public Statement createNamedStatement(String name) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createQuery(String qlString) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNativeQuery(String sqlString, String resultSetMapping) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNativeQuery(String sqlString, ResultSetMapping<T> resultSetMapping) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNativeQuery(String sqlString, Class<T> resultClass) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNativeQuery(String sqlString) {
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
            public Statement createStatement(StatementReference reference) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNamedQuery(String name) {
                return null;
            }

            @Deprecated(since = "4.0", forRemoval = true)
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

            @Override
            public <T> T find(Class<T> entityClass, Object primaryKey, FindOption... options) {
                return null;
            }

            @Override
            public <T> T find(EntityGraph<T> entityGraph, Object primaryKey, FindOption... options) {
                return null;
            }

            @Override
            public <T> List<T> findMultiple(Class<T> entityClass, List<?> ids, @Nullable FindOption... options) {
                return List.of();
            }

            @Override
            public <T> List<T> findMultiple(EntityGraph<T> graph, List<?> ids, @Nullable FindOption... options) {
                return List.of();
            }

            @Override
            public <T> T getReference(T entity) {
                return null;
            }

            @Override
            public void lock(Object entity, LockModeType lockMode, LockOption... options) {
            }

            @Override
            public void refresh(Object entity, RefreshOption... options) {
            }

            @Override
            public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {
            }

            @Override
            public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {
            }

            @Override
            public CacheRetrieveMode getCacheRetrieveMode() {
                return null;
            }

            @Override
            public CacheStoreMode getCacheStoreMode() {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
                return null;
            }

            @Override
            public Statement createStatement(CriteriaStatement<?> statement) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
                return null;
            }

            @Override
            public Statement createNativeStatement(String sqlString) {
                return null;
            }

            @Override
            public <C> void runWithConnection(ConnectionConsumer<C> action) {
            }

            @Override
            public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
                return null;
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
            public EntityManager createEntityManager(@Nullable EntityManager.CreationOption... options) {
                return null;
            }

            @Override
            public EntityAgent createEntityAgent(@Nullable EntityAgent.CreationOption... options) {
                return null;
            }

            @Override
            public EntityAgent createEntityAgent(@Nullable Map<?, ?> properties) {
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
            public EntityManager createEntityManager(SynchronizationType synchronizationType, Map<?, ?> map) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(Map<?, ?> map) {
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public void addNamedQuery(String name, Query query) {
            }

            @Override
            public <R> TypedQueryReference<R> addNamedQuery(String name, TypedQuery<R> query) {
                return null;
            }

            @Override
            public StatementReference addNamedStatement(String name, Statement statement) {
                return null;
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return null;
            }

            @Override
            public SchemaManager getSchemaManager() {
                return null;
            }

            @Override
            public <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> resultType) {
                return null;
            }

            @Override
            public Map<String, StatementReference> getNamedStatements() {
                return Map.of();
            }

            @Override
            public <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> entityType) {
                return null;
            }

            @Override
            public <R> Map<String, ResultSetMapping<R>> getResultSetMappings(Class<R> resultType) {
                return Map.of();
            }

            @Override
            public <E> EntityListenerRegistration addListener(Class<E> entityType, Class<? extends Annotation> callbackType,
                    Consumer<? super E> listener) {
                return null;
            }

            @Override
            public void runInTransaction(Consumer<EntityManager> work) {
            }

            @Override
            public <R> R callInTransaction(Function<EntityManager, R> work) {
                return null;
            }

            @Override
            public <H extends EntityHandler> void runInTransaction(Class<H> handlerClass, Consumer<H> work) {

            }

            @Override
            public <R, H extends EntityHandler> R callInTransaction(Class<H> handlerClass, Function<H, R> work) {
                return null;
            }
        };
    }

    static Function<InjectionPoint, Object> getPAFactory() {
        return ip -> new EntityAgent() {
            @Override
            public void insert(Object entity) {

            }

            @Override
            public void insertMultiple(List<?> entities) {

            }

            @Override
            public void update(Object entity) {

            }

            @Override
            public void updateMultiple(List<?> entities) {

            }

            @Override
            public void delete(Object entity) {

            }

            @Override
            public void deleteMultiple(List<?> entities) {

            }

            @Override
            public void upsert(Object entity) {

            }

            @Override
            public void upsertMultiple(List<?> entities) {

            }

            @Override
            public void refresh(Object entity) {

            }

            @Override
            public void refreshMultiple(List<?> entities) {

            }

            @Override
            public void refresh(Object entity, LockModeType lockMode) {

            }

            @Override
            public <T> T fetch(T association) {
                return null;
            }

            @Override
            public void addOption(Option option) {

            }

            @Override
            public Set<Option> getOptions() {
                return Set.of();
            }

            @Override
            public <T> T get(Class<T> entityClass, Object id) {
                return null;
            }

            @Override
            public <T> T get(Class<T> entityClass, Object id, FindOption... options) {
                return null;
            }

            @Override
            public <T> T get(EntityGraph<T> graph, Object id, FindOption... options) {
                return null;
            }

            @Override
            public <T> List<T> getMultiple(Class<T> entityClass, List<?> ids,
                    FindOption... options) {
                return List.of();
            }

            @Override
            public <T> List<T> getMultiple(EntityGraph<T> graph, List<?> ids,
                    FindOption... options) {
                return List.of();
            }

            @Override
            public <T> T find(Class<T> entityClass, Object id) {
                return null;
            }

            @Override
            public <T> T find(Class<T> entityClass, Object id, FindOption... options) {
                return null;
            }

            @Override
            public <T> T find(EntityGraph<T> graph, Object id, FindOption... options) {
                return null;
            }

            @Override
            public <T> List<T> findMultiple(Class<T> entityClass, List<?> ids,
                    FindOption... options) {
                return List.of();
            }

            @Override
            public <T> List<T> findMultiple(EntityGraph<T> graph, List<?> ids,
                    FindOption... options) {
                return List.of();
            }

            @Override
            public void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode) {

            }

            @Override
            public void setCacheStoreMode(CacheStoreMode cacheStoreMode) {

            }

            @Override
            public CacheRetrieveMode getCacheRetrieveMode() {
                return null;
            }

            @Override
            public CacheStoreMode getCacheStoreMode() {
                return null;
            }

            @Override
            public void setProperty(String propertyName, Object value) {

            }

            @Override
            public Map<String, Object> getProperties() {
                return Map.of();
            }

            @Override
            public Statement createStatement(String qlString) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createQuery(String qlString) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery) {
                return null;
            }

            @Override
            public Statement createStatement(CriteriaStatement<?> statement) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(String qlString, EntityGraph<T> resultGraph) {
                return null;
            }

            @Override
            public Statement createNamedStatement(String name) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNamedQuery(String name) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
                return null;
            }

            @Override
            public Statement createStatement(StatementReference reference) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference) {
                return null;
            }

            @Override
            public Statement createNativeStatement(String sqlString) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNativeQuery(String sqlString) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNativeQuery(String sqlString, Class<T> resultClass) {
                return null;
            }

            @Override
            public StatementOrTypedQuery createNativeQuery(String sqlString, String resultSetMapping) {
                return null;
            }

            @Override
            public <T> TypedQuery<T> createNativeQuery(String sqlString,
                    ResultSetMapping<T> resultSetMapping) {
                return null;
            }

            @Override
            public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
                return null;
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
                return null;
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName,
                    Class<?>... resultClasses) {
                return null;
            }

            @Override
            public StoredProcedureQuery createStoredProcedureQuery(String procedureName,
                    String... resultSetMappings) {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> type) {
                return null;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public EntityTransaction getTransaction() {
                return null;
            }

            @Override
            public EntityManagerFactory getEntityManagerFactory() {
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
                return null;
            }

            @Override
            public EntityGraph<?> getEntityGraph(String graphName) {
                return null;
            }

            @Override
            public <T> EntityGraph<T> getEntityGraph(Class<T> rootType, String graphName) {
                return null;
            }

            @Override
            public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
                return List.of();
            }

            @Override
            public <C> void runWithConnection(ConnectionConsumer<C> action) {

            }

            @Override
            public <C, T> T callWithConnection(ConnectionFunction<C, T> function) {
                return null;
            }

            @Override
            public String toString() {
                return "MockEntityAgent";
            }
        };
    }
}
