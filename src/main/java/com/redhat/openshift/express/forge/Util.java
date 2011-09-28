package com.redhat.openshift.express.forge;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.enterprise.inject.Alternative;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.util.NativeSystemCall;

public class Util {
   
   private static final String EXPRESS_CONF = System.getProperty("user.home") + "/.openshift/express.conf";
   
   public static boolean isOpenshiftRemotePresent(ShellPrintWriter out, Project project) throws IOException{
      String [] params = {"remote", "show", "openshift"};
      return NativeSystemCall.execFromPath("git", params, new DummyOut() , project.getProjectRoot()) == 0;
   }
   
   public static boolean isGitInit(Project project) throws IOException{
      return project.getProjectRoot().getChildDirectory(".git").exists();
   }
   
   @Alternative
   private static class DummyOut implements ShellPrintWriter {
      
      public DummyOut() {
         // TODO Auto-generated constructor stub
      }

      @Override
      public void write(byte b) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void print(String output) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void println(String output) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void println() {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void print(ShellColor color, String output) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public void println(ShellColor color, String output) {
         // TODO Auto-generated method stub
         
      }

      @Override
      public String renderColor(ShellColor color, String output) {
         // TODO Auto-generated method stub
         return null;
      }
      
   }
   
   public static String getDefaultRhLogin(ShellPrintWriter out) {
      Properties conf = new Properties();
      try {
          conf.load(new FileReader(EXPRESS_CONF));
          ShellMessages.info(out, "Loaded OpenShift configuration from " + EXPRESS_CONF);
      } catch (Exception e) {
          // Swallow
      }

      return conf.getProperty("default_rhlogin");
   }
   
   public static String getName(Project project, ShellPrompt prompt) {
      String _default = project.getFacet(MetadataFacet.class).getProjectName();
      return prompt.prompt("Enter the application name [" + _default + "] ", String.class, _default);
   }

   public static String getRhLogin(ShellPrintWriter out, ShellPrompt prompt) {
      String _default = getDefaultRhLogin(out);
      if (_default == null) {
         ShellMessages.info(out,"If you do not have a Red Hat login, visit http://openshift.com");
      }
      return prompt.prompt("Enter your Red Hat Login [" + _default + "] ", String.class, _default);
   }
   
   public static String getPassword(ShellPrompt prompt) {
      return prompt.promptSecret("Enter your Red Hat Login password");
   }
   
   
  

}
