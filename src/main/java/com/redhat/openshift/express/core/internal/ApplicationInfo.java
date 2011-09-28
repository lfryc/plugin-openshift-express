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

import java.text.MessageFormat;
import java.util.Date;

import com.redhat.openshift.express.core.ICartridge;
import com.redhat.openshift.express.core.OpenshiftException;

/**
 * @author André Dietisheim
 */
public class ApplicationInfo {

	private String name;
	private String uuid;
	private String embedded;
	private ICartridge cartridge;
	private Date creationTime;
	private String namespace;
	private String rhcDomain;
	

	public ApplicationInfo(String name, String uuid, String embedded, ICartridge cartridge, Date creationTime, String namespace, String rhcDomain) {
		this.name = name;
		this.uuid = uuid;
		this.embedded = embedded;
		this.cartridge = cartridge;
		this.creationTime = creationTime;
		this.namespace = namespace;
		this.rhcDomain = rhcDomain;
	}

	public String getName() {
		return name;
	}

	public String getEmbedded() {
		return embedded;
	}

	public String getUuid() {
		return uuid;
	}

	public ICartridge getCartridge() {
		return cartridge;
	}

	public Date getCreationTime() {
		return creationTime;
	}
	
   public String getApplicationUrl()  {
      return MessageFormat.format(Application.APPLICATION_URL_PATTERN, name, namespace, rhcDomain);
   }
	
   public String getGitUri() {
      return MessageFormat
            .format(Application.GIT_URI_PATTERN, uuid, getName(), namespace, rhcDomain);
   }

	
	@Override
	public String toString() {
	   return name + ": framework [" + getCartridge().getName() + "], Creation [" + getCreationTime().toString() + "], UUID [" + getUuid() + "], Public URL [" + getApplicationUrl() + "], Git URL [" + getGitUri() + "]";
	}

}
