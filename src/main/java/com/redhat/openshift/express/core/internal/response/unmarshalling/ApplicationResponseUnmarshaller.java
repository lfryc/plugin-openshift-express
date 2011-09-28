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
package com.redhat.openshift.express.core.internal.response.unmarshalling;

import org.jboss.dmr.ModelNode;

import com.redhat.openshift.express.core.ICartridge;
import com.redhat.openshift.express.core.internal.Application;
import com.redhat.openshift.express.core.internal.InternalUser;
import com.redhat.openshift.express.core.internal.OpenshiftService;

/**
 * @author André Dietisheim
 */
public class ApplicationResponseUnmarshaller extends AbstractOpenshiftJsonResponseUnmarshaller<Application> {

	private InternalUser user;
	private String applicationName;
	private ICartridge cartridge;
	private OpenshiftService service;

	public ApplicationResponseUnmarshaller(String applicationName, ICartridge cartridge, InternalUser user, OpenshiftService service) {
		this.applicationName = applicationName;
		this.cartridge = cartridge;
		this.user = user;
		this.service = service;
	}

	@Override
	protected Application createOpenshiftObject(ModelNode node) {
		return new Application(applicationName, cartridge, user, service);
	}
}
