package org.springframework.xiaosa.testBean;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author haituchen
 * @date 2021/4/5 18:29
 */
public class test {

	public static void main(String[] args) {
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
