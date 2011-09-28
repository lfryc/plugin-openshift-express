/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.redhat.openshift.express.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.redhat.openshift.express.core.IApplication;
import com.redhat.openshift.express.core.ICartridge;
import com.redhat.openshift.express.core.IOpenshiftService;
import com.redhat.openshift.express.core.ISSHPublicKey;
import com.redhat.openshift.express.core.IUser;
import com.redhat.openshift.express.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class InternalUser implements IUser {

	private String rhlogin;
	private String password;
	private ISSHPublicKey sshKey;
	private Domain domain;
	private UserInfo userInfo;
	private List<ICartridge> cartridges;
	private List<IApplication> applications = new ArrayList<IApplication>();

	private IOpenshiftService service;

	public InternalUser(String rhlogin, String password) {
		this(rhlogin, password, (ISSHPublicKey) null, new OpenshiftService());
	}

	public InternalUser(String rhlogin, String password, IOpenshiftService service) {
		this(rhlogin, password, (ISSHPublicKey) null, service);
	}

	public InternalUser(String rhlogin, String password, ISSHPublicKey sshKey, IOpenshiftService service) {
		this.rhlogin = rhlogin;
		this.password = password;
		this.sshKey = sshKey;
		this.service = service;
	}

	public IDomain getDomain() throws OpenshiftException {
		if (domain == null) {
			this.domain = new Domain(
					getUserInfo().getNamespace()
					, getUserInfo().getRhcDomain(), this);
		}
		return domain;
	}

	public ISSHPublicKey getSshKey() throws OpenshiftException {
		if (sshKey == null) {
			sshKey = getUserInfo().getSshPublicKey();
		}
		return sshKey;
	}

	public String getRhlogin() {
		return rhlogin;
	}

	public String getPassword() {
		return password;
	}

	public String getUUID() throws OpenshiftException {
		return getUserInfo().getUuid();
	}

	public List<ICartridge> getCartridges() throws OpenshiftException {
		if (cartridges == null) {
			this.cartridges = service.getCartridges(this);
		}
		return Collections.unmodifiableList(cartridges);
	}

	public IApplication createApplication(String name, ICartridge cartridge) throws OpenshiftException {
		Application application = service.createApplication(name, cartridge, this);
		add(application);
		return application;
	}

	public Collection<IApplication> getApplications() throws OpenshiftException {
		if (getUserInfo().getApplicationInfos().size() > applications.size()) {
			update(getUserInfo().getApplicationInfos());
		}
		return Collections.unmodifiableList(applications);
	}

	public IApplication getApplicationByName(String name) throws OpenshiftException {
		return getApplicationByName(name, getApplications());
	}

	private IApplication getApplicationByName(String name, Collection<IApplication> applications) {
		IApplication matchingApplication = null;
		for (IApplication application : applications) {
			if (name.equals(application.getName())) {
				matchingApplication = application;
			}
		}
		return matchingApplication;
	}

	public void add(Application application) {
		applications.add(application);
	}

	public void remove(IApplication application) {
		applications.remove(application);
	}

	public void setSshPublicKey(ISSHPublicKey key) {
		this.sshKey = key;
	}

	protected UserInfo getUserInfo() throws OpenshiftException {
		if (userInfo == null) {
			this.userInfo = service.getUserInfo(this);
		}
		return userInfo;
	}

	public void refresh() throws OpenshiftException {
		this.domain = null;
		this.sshKey = null;
		getUserInfo();
	}
	
	private void update(List<ApplicationInfo> applicationInfos) {
		for (ApplicationInfo applicationInfo : applicationInfos) {
			IApplication application = getApplicationByName(applicationInfo.getName(), applications);
			if (application == null) {
				applications.add(createApplication(applicationInfo));
			}
		}
	}

	private Application createApplication(ApplicationInfo applicationInfo) {
		return new Application(applicationInfo.getName()
				, applicationInfo.getCartridge()
				, applicationInfo
				, this, service);
	}

	protected IOpenshiftService getService() {
		return service;
	}

}