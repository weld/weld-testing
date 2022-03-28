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
package org.jboss.weld.junit;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.junit.MockInterceptor.MockInterceptorInstance;
import org.jboss.weld.util.bean.SerializableForwardingBean;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * This custom {@link Interceptor} implementation is useful for mocking.
 * <p>
 * A new instance is usually created through a {@link Builder} (see also {@link #withBindings(Annotation...)} method) and then passed to the
 * {@code WeldInitiator.Builder#addBeans(Bean...)} method.
 * </p>
 * <p>
 * Note that by default all mock interceptors are automatically enabled for the synthetic bean archive. If needed a custom bean class can be set through the
 * {@link Builder#beanClass(Class)} method - the bean class can be used to enable the interceptor for a bean archive. It's not possible to enable a mock
 * interceptor globally (per application).
 * </p>
 *
 * @author Martin Kouba
 * See also {code WeldInitiator.Builder#addBean(Bean)} method.
 * @since 1.2.1
 */
public class MockInterceptor implements Interceptor<MockInterceptorInstance> {

    /**
     *
     * @param interceptorBindings
     * @return a new builder instance with the specified interceptor bindings
     */
    public static Builder withBindings(Annotation... interceptorBindings) {
        return new Builder().bindings(interceptorBindings);
    }

    private final Class<?> beanClass;

    private final InterceptionType type;

    private final InterceptionCallback callback;

    private final Set<Annotation> interceptorBindings;

    /**
     *
     * @param beanClass
     * @param type
     * @param callback
     * @param interceptorBindings
     */
    private MockInterceptor(Class<?> beanClass, InterceptionType type, InterceptionCallback callback, Set<Annotation> interceptorBindings) {
        this.beanClass = beanClass;
        this.type = type;
        this.callback = callback;
        this.interceptorBindings = interceptorBindings;
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return ImmutableSet.copyOf(interceptorBindings);
    }

    @Override
    public boolean intercepts(InterceptionType type) {
        return this.type.equals(type);
    }

    @Override
    public Object intercept(InterceptionType type, MockInterceptorInstance instance, InvocationContext ctx) throws Exception {
        return callback.invoke(ctx, instance.getInterceptedBean());
    }

    @Override
    public MockInterceptorInstance create(CreationalContext<MockInterceptorInstance> creationalContext) {
        return new MockInterceptorInstance(getInterceptedBean(creationalContext), BeanManagerProxy.unwrap(CDI.current().getBeanManager()).getContextId());
    }

    @Override
    public void destroy(MockInterceptorInstance instance, CreationalContext<MockInterceptorInstance> creationalContext) {
        // No-op
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(Object.class);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    boolean hasDefaultBeanClass() {
        return MockInterceptor.class.equals(beanClass);
    }

    private Bean<?> getInterceptedBean(CreationalContext<MockInterceptorInstance> ctx) {
        if (!ctx.getClass().getName().startsWith("org.jboss.weld")) {
            return null;
        }
        Bean<?> interceptedBean = null;
        try {
            // Note that we need to support both 2.x and 3.x
            Class<?> ctxImplClazz;
            if (ctx.getClass().getName().startsWith("org.jboss.weld.contexts")) {
                // 3.x
                ctxImplClazz = MockInterceptor.class.getClassLoader().loadClass("org.jboss.weld.contexts.CreationalContextImpl");
            } else {
                // 2.x
                ctxImplClazz = MockInterceptor.class.getClassLoader().loadClass("org.jboss.weld.context.CreationalContextImpl");
            }
            Object parentContext = ctxImplClazz.getMethod("getParentCreationalContext").invoke(ctx);
            if (parentContext != null) {
                Contextual<?> interceptedContextual = (Contextual<?>) ctxImplClazz.getMethod("getContextual").invoke(parentContext);
                if (interceptedContextual instanceof Bean<?>) {
                    interceptedBean = (Bean<?>) interceptedContextual;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return interceptedBean;
    }

    public static class MockInterceptorInstance implements Serializable {

        private static final long serialVersionUID = -1623826535751475203L;

        private final SerializableForwardingBean<?> interceptedBean;

        MockInterceptorInstance(Bean<?> interceptedBean, String contextId) {
            this.interceptedBean = interceptedBean != null ? SerializableForwardingBean.of(contextId, interceptedBean) : null;
        }

        public Bean<?> getInterceptedBean() {
            return interceptedBean;
        }

    }

    public static class Builder {

        private Set<Annotation> bindings = new HashSet<>();

        private InterceptionType type;

        private InterceptionCallback callback;

        private Class<?> beanClass;

        /**
         *
         * @param bindings
         * @return self
         */
        Builder bindings(Annotation... bindings) {
            this.bindings.clear();
            Collections.addAll(this.bindings, bindings);
            return this;
        }

        /**
         *
         * @param type
         * @return self
         */
        public Builder type(InterceptionType type) {
            this.type = type;
            return this;
        }

        /**
         * Allows to specify a bean class of an interceptor. This is only required if you later on need to enforce interceptor ordering via
         * <code>Weld.enableInterceptors()</code>. Note that such ordering corresponds to enabling interceptors via beans.xml (e.g. per bean archive).
         *
         * @param beanClass
         * @return self
         */
        public Builder beanClass(Class<?> beanClass) {
            this.beanClass = beanClass;
            return this;
        }

        /**
         *
         * @param callback The interception callback, intercepted bean might be <code>null</code>
         * @return self
         */
        public Builder callback(InterceptionCallback callback) {
            this.callback = callback;
            return this;
        }

        public MockInterceptor aroundInvoke(InterceptionCallback callback) {
            return type(InterceptionType.AROUND_INVOKE).callback(callback).build();
        }

        public MockInterceptor aroundConstruct(InterceptionCallback callback) {
            return type(InterceptionType.AROUND_CONSTRUCT).callback(callback).build();
        }

        public MockInterceptor postConstruct(InterceptionCallback callback) {
            return type(InterceptionType.POST_CONSTRUCT).callback(callback).build();
        }

        public MockInterceptor preDestroy(InterceptionCallback callback) {
            return type(InterceptionType.PRE_DESTROY).callback(callback).build();
        }

        public MockInterceptor build() {
            if (type == null) {
                throw new IllegalStateException("Interception type not set");
            }
            if (callback == null) {
                throw new IllegalStateException("Interception callback not set");
            }
            if (bindings.isEmpty()) {
                throw new IllegalStateException("No interceptor bindings specified");
            }
            return new MockInterceptor(beanClass != null ? beanClass : MockInterceptor.class, type, callback, bindings);
        }

    }

    @FunctionalInterface
    public interface InterceptionCallback {

        /**
         *
         * @param invocationContext
         * @param interceptedBean May be <code>null</code>
         * @return the result
         */
        Object invoke(InvocationContext invocationContext, Bean<?> interceptedBean) throws Exception;

    }

}
