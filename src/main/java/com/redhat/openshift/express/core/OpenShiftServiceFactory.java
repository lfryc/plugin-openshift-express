package com.redhat.openshift.express.core;

import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftService;

public class OpenShiftServiceFactory {

   private static final String ID = "com.redhat.openshift.express.forge";
   private static final String DEFAULT_BASE_URL = "https://openshift.redhat.com";

   public static IOpenShiftService create(String baseUrl) {
	   if (baseUrl == null) {
		   baseUrl = DEFAULT_BASE_URL;
	   }

	   final OpenShiftService service = new OpenShiftService(ID, baseUrl);

	   // only verify SSL certificates when the default host is used
	   service.setEnableSSLCertChecks(DEFAULT_BASE_URL.equals(baseUrl));

       return service;
   }

   public static IOpenShiftService create() {
	   return create(DEFAULT_BASE_URL);
   }
   
}
