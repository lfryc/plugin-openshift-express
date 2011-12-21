package com.redhat.openshift.express.forge;

import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.inject.Alternative;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;

import com.openshift.express.client.InvalidCredentialsOpenShiftException;
import com.openshift.express.internal.client.ApplicationInfo;
import com.openshift.express.internal.client.EmbeddableCartridgeInfo;

public class Util {
   
   private static final String EXPRESS_CONF = System.getProperty("user.home") + "/.openshift/express.conf";

   private static final String GIT_URI_PATTERN = "ssh://{0}@{1}-{2}.{3}/~/git/{1}.git/";
   private static final String APPLICATION_URL_PATTERN = "https://{0}-{1}.{2}/";
   
   public static boolean isOpenshiftRemotePresent(ShellPrintWriter out, Project project) throws IOException{
      return project.getProjectRoot().getChildDirectory(".git").getChildDirectory("refs").getChildDirectory("remotes").getChildDirectory("openshift").exists();
   }
   
   public static boolean isGitInit(Project project) throws IOException{
      return project.getProjectRoot().getChildDirectory(".git").exists();
   }
   
   @Alternative
   @SuppressWarnings("unused")
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

      @Override
      public void write(int b) {
         // TODO Auto-generated method stub
      }

      @Override
      public void write(byte[] b) {
         // TODO Auto-generated method stub
      }

      @Override
      public void write(byte[] b, int offset, int length) {
         // TODO Auto-generated method stub
      }

      @Override
      public void flush() {
         // TODO Auto-generated method stub}
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
   
   public static String getDefaultBaseUrl(ShellPrintWriter out) {
      Properties conf = new Properties();
      try {
          conf.load(new FileReader(EXPRESS_CONF));
          ShellMessages.info(out, "Loaded OpenShift configuration from " + EXPRESS_CONF);
      } catch (Exception e) {
          // Swallow
      }

      String hostname = conf.getProperty("libra_server");

      if (hostname == null || hostname.trim().length() == 0) {
         return null;
      } else {
         return "https://".concat(hostname);
      }
   }

   public static String getName(Project project, ShellPrompt prompt) {
      String _default = project.getFacet(MetadataFacet.class).getProjectName();
      _default = _default.replaceAll("[\\W_]", "");
      _default = _default.substring(0, (_default.length() > 15 ? 15 : _default.length()));
      return prompt.prompt("Enter the application name [" + _default + "] ", String.class, _default);
   }

   public static String getRhLogin(ShellPrintWriter out, ShellPrompt prompt) {
      String _default = getDefaultRhLogin(out);
      if (_default == null) {
         ShellMessages.info(out, "If you do not have a Red Hat login, visit http://openshift.com");
      }
      return prompt.prompt("Enter your Red Hat Login [" + _default + "] ", String.class, _default);
   }
   
   public static String getPassword(ShellPrompt prompt) {
      return prompt.promptSecret("Enter your Red Hat Login password");
   }

   public static void displayCredentialsError(ShellPrintWriter out, InvalidCredentialsOpenShiftException e) {
      out.println("\nInvalid user credentials.  Please check your Red Hat login and password and try again.\n");
   }

   public static String formatApplicationInfo(ApplicationInfo app, String namespace, String domain) {
      Map<String, String> attrs = new LinkedHashMap<String, String>();
      attrs.put("Framework", app.getCartridge().getName());
      attrs.put("Creation", app.getCreationTime().toString());
      attrs.put("UUID", app.getUuid());

      //TODO: client library should provide these URIs
      attrs.put("Git URL", MessageFormat.format(GIT_URI_PATTERN, app.getUuid(), app.getName(), namespace, domain));
      attrs.put("Public URL", MessageFormat.format(APPLICATION_URL_PATTERN, app.getName(), namespace, domain));

      attrs.put("Embedded", formatEmbeddedCartridges(app.getEmbeddedCartridges()));

      int longest = 0;
      for (String key : attrs.keySet()) {
         longest = Math.max(longest, key.length());
      }

      final StringBuilder str = new StringBuilder();
      str.append(String.format(app.getName()));
      for (String key : attrs.keySet()) {
         str.append(String.format("\n  %s %s", pad(key+":", longest), attrs.get(key)));
      }
      str.append("\n");
      return str.toString();
   }

   private static String formatEmbeddedCartridges(Collection<EmbeddableCartridgeInfo> cartridges) {
      if (cartridges.size() == 0) {
         return "None";
      }

      StringBuilder carts = new StringBuilder();

      for (EmbeddableCartridgeInfo info : cartridges) {
         if (carts.length() > 0) {
            carts.append(", ");
         }
         carts.append(info.getName());
      }

      return carts.toString();
   }
   
   private static String pad(String str, int len) {
      StringBuilder result = new StringBuilder(str);
      for (int i = 0; i < len - str.length() + 1; i++) {
         result.append(" ");
      }
      return result.toString();
   }

}
