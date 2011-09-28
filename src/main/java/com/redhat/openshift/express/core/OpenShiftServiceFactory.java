package com.redhat.openshift.express.core;

import com.redhat.openshift.express.core.internal.OpenshiftService;

public class OpenShiftServiceFactory {

   public static IOpenshiftService create() {
      return new OpenshiftService();
   }
   
}
