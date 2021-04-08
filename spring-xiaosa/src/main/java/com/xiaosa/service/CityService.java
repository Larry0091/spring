package com.xiaosa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yongcezhang
 * @date 2021/4/8 9:02
 */
@Component
//@Scope("prototype")
public class CityService {

	@Autowired
	private TestService1 testService1;

	public CityService() {
		System.out.println("init cityService");
	}

}
