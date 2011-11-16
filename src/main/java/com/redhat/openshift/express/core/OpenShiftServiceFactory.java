package com.redhat.openshift.express.core;

import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftService;

public class OpenShiftServiceFactory {

   private static final String ID = "com.redhat.openshift.express.forge";
   private static final String BASE_URL = "https://openshift.redhat.com";
	
   public static IOpenShiftService create() {
	  return new OpenShiftService(ID, BASE_URL);
   }
   
}
