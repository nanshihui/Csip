package com.csip.util;


import java.util.List;

import com.csip.bean.ContactBean;
import com.csip.serve.T9Service;

import android.content.Intent;

public class MyApplication extends android.app.Application {

	
	private List<ContactBean> contactBeanList;
	
	public List<ContactBean> getContactBeanList() {
		return contactBeanList;
	}
	public void setContactBeanList(List<ContactBean> contactBeanList) {
		this.contactBeanList = contactBeanList;
	}

	public void onCreate() {
		
		
		Intent startService = new Intent(MyApplication.this, T9Service.class);
		this.startService(startService);

	}
}
