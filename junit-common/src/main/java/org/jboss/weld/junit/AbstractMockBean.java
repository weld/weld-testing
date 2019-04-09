package org.jboss.weld.junit;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Predecessor of {@link MockBean} and {@link MockBeanWithPriority} that holds share codebase for both.
 *
 * @author Matej Novotny
 */
public abstract class AbstractMockBean<T> implements Bean<T>, PassivationCapable {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private final Set<Class<? extends Annotation>> stereotypes;
    private final boolean alternative;
    private final String name;
    private final Set<Annotation> qualifiers;
    private final Set<Type> types;
    private final Class<? extends Annotation> scope;
    private final MockBean.CreateFunction<T> createCallback;
    private final MockBean.DestroyFunction<T> destroyCallback;
    private final String id;
    private final Class<?> beanClass;

    protected AbstractMockBean(Class<?> beanClass, Set<Class<? extends Annotation>> stereotypes, boolean alternative, boolean selectForSyntheticBeanArchive, String name,
                               Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope, MockBean.CreateFunction<T> createCallback,
                               MockBean.DestroyFunction<T> destroyCallback) {
        this.beanClass = beanClass;
        this.stereotypes = stereotypes;
        this.alternative = alternative;
        this.name = name;
        this.qualifiers = qualifiers;
        this.types = types;
        this.scope = scope;
        this.createCallback = createCallback;
        this.destroyCallback = destroyCallback;
        this.id = new StringBuilder().append(MockBean.class.getName()).append("_").append(SEQUENCE.incrementAndGet()).toString();
    }

    protected static <T, K extends AbstractBuilder<T, K>> K readInternal(Class<T> beanClass, K builder) {
        // Note that we cannot use BeanManager here as the container may not be started yet
        K localBuilder = builder.beanClass(beanClass);

        // Find all stereotypes
        Set<Annotation> stereotypes = getStereotypes(beanClass);
        // Name
        Named named = beanClass.getAnnotation(Named.class);
        if (named != null) {
            if ("".equals(named.value())) {
                builder.name(getDefaultName(beanClass));
            } else {
                builder.name(named.value());
            }
        } else {
            for (Annotation stereotype : stereotypes) {
                if (stereotype.annotationType().isAnnotationPresent(Named.class)) {
                    builder.name(getDefaultName(beanClass));
                    break;
                }
            }
        }
        // Scope
        Set<Annotation> scopes = getScopes(beanClass);
        if (scopes.isEmpty()) {
            for (Annotation stereotype : stereotypes) {
                scopes.addAll(getScopes(stereotype.annotationType()));
            }
        }
        if (!scopes.isEmpty()) {
            if (scopes.size() > 1) {
                throw new IllegalStateException("At most one scope may be specifie [beanClass: " + beanClass + ", scopes: " + scopes + "]");
            }
            builder.scope(scopes.iterator().next().annotationType());
        }
        // Types
        builder.types(new HierarchyDiscovery(beanClass).getTypeClosure());
        // Qualifiers
        for (Annotation annotation : beanClass.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                builder.addQualifier(annotation);
            }
        }
        // Alternative
        if (beanClass.isAnnotationPresent(Alternative.class)) {
            builder.setAlternativeInternal(true);
        } else {
            for (Annotation stereotype : stereotypes) {
                if (stereotype.annotationType().isAnnotationPresent(Alternative.class)) {
                    builder.setAlternativeInternal(true);
                    break;
                }
            }
        }
        return localBuilder;
    }

    private static Set<Annotation> getStereotypes(AnnotatedElement element) {
        Set<Annotation> stereotypes = new HashSet<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Stereotype.class)) {
                stereotypes.add(annotation);
                stereotypes.addAll(getStereotypes(annotation.annotationType()));
            }
        }
        return stereotypes;
    }

    private static Set<Annotation> getScopes(AnnotatedElement element) {
        Set<Annotation> scopes = new HashSet<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class) || annotation.annotationType().isAnnotationPresent(NormalScope.class)) {
                scopes.add(annotation);
            }
        }
        return scopes;
    }

    private static String getDefaultName(Class<?> beanClass) {
        StringBuilder defaultName = new StringBuilder(beanClass.getSimpleName());
        defaultName.setCharAt(0, Character.toLowerCase(beanClass.getSimpleName().charAt(0)));
        return defaultName.toString();
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return createCallback.create(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        if (destroyCallback != null) {
            destroyCallback.destroy(instance, creationalContext);
        }
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
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return stereotypes;
    }

    @Override
    public boolean isAlternative() {
        return alternative;
    }

    @Override
    public String getId() {
        return id;
    }

    public interface CreateFunction<T> {

        /**
         * @param creationalContext
         * @return a new bean instance
         */
        T create(CreationalContext<T> creationalContext);

    }

    public interface DestroyFunction<T> {

        /**
         * @param instance
         * @param creationalContext
         * @return a new bean instance
         */
        void destroy(T instance, CreationalContext<T> creationalContext);

    }

    /**
     * A builder instance should not be reused nor shared.
     *
     * @author Matej Novotny
     *
     * @param <T>
     */
        public static abstract class AbstractBuilder<T, K extends AbstractBuilder<T, K>> {

        protected Class<?> beanClass;
        protected Set<Class<? extends Annotation>> stereotypes;
        protected boolean alternative;
        protected boolean selectForSyntheticBeanArchive;
        protected String name;
        protected Set<Annotation> qualifiers;
        protected Set<Type> types;
        protected Class<? extends Annotation> scope;
        protected CreateFunction<T> createCallback;
        protected DestroyFunction<T> destroyCallback;

        protected AbstractBuilder() {
            this.stereotypes = new HashSet<>();
            this.alternative = false;
            this.qualifiers = new HashSet<>();
            this.qualifiers.add(Any.Literal.INSTANCE);
            this.scope = Dependent.class;
            this.types = new HashSet<>();
            this.types.add(Object.class);
            this.beanClass = WeldCDIExtension.class;
        }

        protected abstract K self();

        // intentionally hidden from user so it's not visible from MockBeanWithPriority
        K setAlternativeInternal(boolean value) {
            this.alternative = value;
            return self();
        }

        /**
         *
         * @param beanClass
         * @return self
         * @see Bean#getBeanClass()
         */
        public K beanClass(Class<?> beanClass) {
            this.beanClass = beanClass;
            return self();
        }

        /**
         *
         * @param scope
         * @return self
         * @see Bean#getScope()
         */
        public K scope(Class<? extends Annotation> scope) {
            this.scope = scope;
            return self();
        }

        /**
         *
         * @param name
         * @return self
         * @see Bean#getName()
         */
        public K name(String name) {
            this.name = name;
            return self();
        }

        /**
         *
         * @param types
         * @return self
         * @see Bean#getTypes()
         */
        public K types(Type... types) {
            this.types.clear();
            Collections.addAll(this.types, types);
            return self();
        }

        /**
         *
         * @param types
         * @return self
         */
        public K types(Set<Type> types) {
            this.types.clear();
            this.types.addAll(types);
            return self();
        }

        /**
         *
         * @param type
         * @return self
         */
        public K addType(Type type) {
            this.types.add(type);
            return self();
        }

        /**
         *
         * @param qualifiers
         * @return self
         * @see Bean#getQualifiers()
         */
        public K qualifiers(Annotation... qualifiers) {
            this.qualifiers.clear();
            Collections.addAll(this.qualifiers, qualifiers);
            return self();
        }

        /**
         *
         * @param qualifier
         * @return self
         */
        public K addQualifier(Annotation qualifier) {
            this.qualifiers.add(qualifier);
            return self();
        }

        /**
         *
         * @param stereotypes
         * @return self
         * @see Bean#getStereotypes()
         */
        @SuppressWarnings("unchecked")
        public K stereotypes(Class<? extends Annotation>... stereotypes) {
            this.stereotypes.clear();
            Collections.addAll(this.stereotypes, stereotypes);
            return self();
        }

        /**
         *
         * @param stereotype
         * @return self
         */
        public K addStereotype(Class<? extends Annotation> stereotype) {
            this.stereotypes.add(stereotype);
            return self();
        }

        /**
         * Each invocation of {@link Bean#create(CreationalContext)} will return the same instance.
         *
         * @param instance
         * @return self
         */
        public K creating(final T instance) {
            this.createCallback = ctx -> instance;
            return self();
        }

        /**
         * Use {@link Unmanaged} to create/destroy the bean instance.
         *
         * <p>
         * NOTE: {@link CreationalContext#toString()} is used as a key in a map and therefore must be unique for the lifetime of a bean instance. Weld
         * implementation fulfills this requirement.
         * </p>
         *
         * @return self
         * @see Unmanaged.UnmanagedInstance
         */
        @SuppressWarnings("unchecked")
        public K useUnmanaged(Class<T> beanClass) {
            Map<String, Unmanaged.UnmanagedInstance<?>> ctxToUnmanaged = new ConcurrentHashMap<>();
            create(ctx -> {
                Unmanaged<?> unmanaged = new Unmanaged<>(WeldContainer.current().getBeanManager(), beanClass);
                Unmanaged.UnmanagedInstance<?> unmanagedInstance = unmanaged.newInstance();
                ctxToUnmanaged.put(ctx.toString(), unmanagedInstance);
                return (T) unmanagedInstance.produce().inject().postConstruct().get();
            });
            destroy((o, ctx) -> {
                Unmanaged.UnmanagedInstance<?> unmanagedInstance = ctxToUnmanaged.remove(ctx.toString());
                if (unmanagedInstance != null) {
                    if (!unmanagedInstance.get().equals(o)) {
                        throw new IllegalStateException(
                                "Unmanaged instance [" + unmanagedInstance.get() + "] is not equal to the bean instance to be destroyed: " + o);
                    }
                    unmanagedInstance.preDestroy().dispose();
                }
            });
            return self();
        }

        /**
         *
         * @param callback
         * @return self
         * @see Bean#create(CreationalContext)
         */
        public K create(CreateFunction<T> callback) {
            this.createCallback = callback;
            return self();
        }

        /**
         *
         * @param callback
         * @return self
         * @see Bean#destroy(Object, CreationalContext)
         */
        public K destroy(DestroyFunction<T> callback) {
            this.destroyCallback = callback;
            return self();
        }

        /**
         *
         * @return a new {@link AbstractMockBean} instance
         * @throws IllegalStateException If a create callback is not set
         */
        public abstract AbstractMockBean<T> build();
    }

}
