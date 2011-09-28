package com.redhat.openshift.express.forge;

import javax.inject.Singleton;

@Singleton
public class OpenShiftExpressConfiguration {
   
   private String name;
   private String rhLogin;
   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getRhLogin() {
      return rhLogin;
   }
   public void setRhLogin(String rhLogin) {
      this.rhLogin = rhLogin;
   }

}
