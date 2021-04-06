package org.springframework.xiaosa.testBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author haituchen
 * @date 2021/4/5 18:34
 */
@Component
public class X {

	@Autowired
	Y y;

	public X() {
		System.out.println("X create");
	}
}