package com.redhat.openshift.express.forge;

import java.io.IOException;

import javax.enterprise.inject.Alternative;

import org.jboss.forge.project.Project;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.util.NativeSystemCall;

public class Util {
   
   
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
  

}
