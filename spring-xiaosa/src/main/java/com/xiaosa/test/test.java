package com.xiaosa.test;

import com.xiaosa.config.Appconfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author yongcezhang
 * @date 2021/4/5 18:29
 */
public class test {

	/**
	 * beanDefaultMap 是一个map里边存的是bean的名字和描述
	 * @param args
	 */

	public static void main(String[] args) {
		//初始化spring 容器 --bean存储位置--map--单例池
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(Appconfig.class);

		//手动关闭循环依赖
//		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
//		ac.register(Appconfig.class);
//		AbstractAutowireCapableBeanFactory beanFactory = (AbstractAutowireCapableBeanFactory) ac.getBeanFactory();
//		beanFactory.setAllowCircularReferences(false);
//		ac.refresh();
//		System.out.println(ac.getBean(X.class));
	}

}
