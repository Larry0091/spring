/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Convenient adapter for programmatic registration of bean classes.
 * <p>This is an alternative to {@link ClassPathBeanDefinitionScanner}, applying
 * the same resolution of annotations but for explicitly registered classes only.
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @author Phillip Webb
 * @see AnnotationConfigApplicationContext#register
 * @since 3.0
 */
public class AnnotatedBeanDefinitionReader {

	private final BeanDefinitionRegistry registry;

	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private ConditionEvaluator conditionEvaluator;

	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry.
	 * <p>If the registry is {@link EnvironmentCapable}, e.g. is an {@code ApplicationContext},
	 * the {@link Environment} will be inherited, otherwise a new
	 * {@link StandardEnvironment} will be created and used.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @see #AnnotatedBeanDefinitionReader(BeanDefinitionRegistry, Environment)
	 * @see #setEnvironment(Environment)
	 */
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code AnnotatedBeanDefinitionReader} for the given registry,
	 * using the given {@link Environment}.
	 * @param registry the {@code BeanFactory} to load bean definitions into,
	 * in the form of a {@code BeanDefinitionRegistry}
	 * @param environment the {@code Environment} to use when evaluating bean definition
	 * profiles.
	 * @since 3.1
	 */
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		//condition评估会用到
		conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}

	/**
	 * Get the BeanDefinitionRegistry that this reader operates on.
	 */
	public final BeanDefinitionRegistry getRegistry() {
		return registry;
	}

	/**
	 * Set the {@code Environment} to use when evaluating whether
	 * {@link Conditional @Conditional}-annotated component classes should be registered.
	 * <p>The default is a {@link StandardEnvironment}.
	 * @see #registerBean(Class, String, Class...)
	 */
	public void setEnvironment(Environment environment) {
		conditionEvaluator = new ConditionEvaluator(registry, environment, null);
	}

	/**
	 * Set the {@code BeanNameGenerator} to use for detected bean classes.
	 * <p>The default is a {@link AnnotationBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
	}

	/**
	 * Set the {@code ScopeMetadataResolver} to use for registered component classes.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = (scopeMetadataResolver != null ?
				scopeMetadataResolver :
				new AnnotationScopeMetadataResolver());
	}

	/**
	 * Register one or more component classes to be processed.
	 * <p>Calls to {@code register} are idempotent; adding the same
	 * component class more than once has no additional effect.
	 * @param componentClasses one or more component classes,
	 * e.g. {@link Configuration @Configuration} classes
	 */
	public void register(Class<?>... componentClasses) {
		for (Class<?> componentClass : componentClasses) {
			registerBean(componentClass);
		}
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 */
	public void registerBean(Class<?> beanClass) {
		doRegisterBean(beanClass, null, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param instanceSupplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> beanClass, @Nullable Supplier<T> instanceSupplier) {
		doRegisterBean(beanClass, instanceSupplier, null, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations, using the given supplier for obtaining a new
	 * instance (possibly declared as a lambda expression or method reference).
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * @param instanceSupplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @since 5.0
	 */
	public <T> void registerBean(Class<T> beanClass, String name, @Nullable Supplier<T> instanceSupplier) {
		doRegisterBean(beanClass, instanceSupplier, name, null);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	public void registerBean(Class<?> beanClass, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(beanClass, null, null, qualifiers);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param name an explicit name for the bean
	 * @param qualifiers specific qualifier annotations to consider,
	 * in addition to qualifiers at the bean class level
	 */
	public void registerBean(Class<?> beanClass, String name, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(beanClass, null, name, qualifiers);
	}

	/**
	 * Register a bean from the given bean class, deriving its metadata from
	 * class-declared annotations.
	 * @param beanClass the class of the bean
	 * @param instanceSupplier a callback for creating an instance of the bean
	 * (may be {@code null})
	 * @param name an explicit name for the bean
	 * @param qualifiers specific qualifier annotations to consider, if any,
	 * in addition to qualifiers at the bean class level
	 * @param definitionCustomizers one or more callbacks for customizing the
	 * factory's {@link BeanDefinition}, e.g. setting a lazy-init or primary flag
	 * @since 5.0
	 */
	<T> void doRegisterBean(Class<T> beanClass, @Nullable Supplier<T> instanceSupplier, @Nullable String name,
			@Nullable Class<? extends Annotation>[] qualifiers, BeanDefinitionCustomizer... definitionCustomizers) {

		/**
		 * 根据指定的bean创建一个AnnotatedGenericBeanDefinition
		 * 这个AnnotatedGenericBeanDefinition可以理解为一个数据结构
		 * AnnotatedGenericBeanDefinition包含了类的其他信息，比如一些元信息 scope，lazy等
		 */
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
		/**
		 * 判断这个类是否需要跳过解析
		 * 通过代码可以知道spring判断是否跳过解析，主要判断类有没有加注解
		 * condition的判断，如果不满足condition，那么这个bean不会被注册
		 */

		if (conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		abd.setInstanceSupplier(instanceSupplier);
		// 得到类的作用域
		ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(abd);
		// 把类的作用域添加到数据结构中  /设置单例，多例
		abd.setScope(scopeMetadata.getScopeName());
		// 生成类的名字通过beanNameGenerator
		String beanName = (name != null ? name : beanNameGenerator.generateBeanName(abd, registry));

		/**
		 * 处理类当中的通用注解
		 * 分析源码可以知道他主要处理 Lazy DependsOn Primary Role 等等注解
		 * 处理完成之后processCommonDefinitionAnnotations中依然是把它添加到数据结构中
		 */
		//处理公共的注解Lazy，Primary，DependsOn，Role
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		/**
		 * 如果再向容器注册注解bean定义时，使用了额外的限定符注解则解析
		 * Qualifier和Primary主要涉及到spring的自动装配
		 * byName和qualifiers这个变量是Annotation类型的数组，里面存不仅仅是Qualifier注解
		 * 理论上里面存的是一切注解，所以可以看到下面的代码spring去循环了这个数组
		 * 然后依次判断了注解当中是否包含了Primary,是否包含了Lazyd
		 */
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				// 如果配置了@Primary注解，如果加了则作为首选
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				}
				// 懒加载，前面加过
				else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				}
				else {
					// 如果使用了除@Primary和@Lazy以为的其他注解，则为该Bean添加一个根据名字自动装配的限定
					// 这里难以理解，后面会详细介绍
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
		for (BeanDefinitionCustomizer customizer : definitionCustomizers) {
			customizer.customize(abd);
		}

		// BeanDefinitionHolder 也是一个数据结构
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		/**
		 * ScopedProxyMode 这个知识点比较复杂，需要结合web去理解
		 * 暂时可以放一下，等说到springmvc的时候再说
		 */
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, registry);
		/**
		 * 把上述的这个数据结构注册给registry
		 * registry就是AnnotatonConfigApplicationContext，在初始化的时候通过调用父类的构造方法
		 * 实例化一个DefaultListableBeanFactory
		 * registerBeanDefinition里面就是把definitionHolder这个数据结构包含的信息注册到DefaultListableBeanFactory这个工厂
		 */
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}

	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
