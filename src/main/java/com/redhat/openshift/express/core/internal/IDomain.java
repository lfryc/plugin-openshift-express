package com.redhat.openshift.express.core.internal;

import com.redhat.openshift.express.core.OpenshiftException;

public interface IDomain {

	public abstract String getNamespace() throws OpenshiftException;

	public abstract String getRhcDomain() throws OpenshiftException;

}