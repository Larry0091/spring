package com.xiaosa.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author yongcezhang
 * @date 2021/4/7 20:37
 */
@Component
public class testBeanfactoryPostProcessors implements BeanFactoryPostProcessor {

	/**
	 * 改变bean的类
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException
	 */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

//		GenericBeanDefinition gf = (GenericBeanDefinition) beanFactory.getBeanDefinition("cityService");
//		gf.setBeanClass(XXService.class);
	}
}
