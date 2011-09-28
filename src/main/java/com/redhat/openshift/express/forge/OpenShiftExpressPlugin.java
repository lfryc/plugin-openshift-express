package com.redhat.openshift.express.forge;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;
import org.jboss.forge.shell.util.NativeSystemCall;

import com.redhat.openshift.express.core.OpenshiftException;

public @Alias("rhc-express") @RequiresProject @RequiresFacet(OpenShiftExpressFacet.class)
class OpenShiftExpressPlugin implements org.jboss.forge.shell.plugins.Plugin {
   
   @Inject
   private Event<InstallFacets> request;
   
   @Inject Project project;
   
   @Inject OpenShiftExpressConfiguration configuration;
   
   
   @SetupCommand
   public void setup(PipeOut out, @Option(name = "app") final String app, @Option(name = "rhlogin") final String rhLogin) throws OpenshiftException, IOException {
      if (!project.hasFacet(OpenShiftExpressFacet.class))
      {
         configuration.setName(app);
         configuration.setRhLogin(rhLogin);
         request.fire(new InstallFacets(OpenShiftExpressFacet.class));
      }

      if (project.hasFacet(OpenShiftExpressFacet.class))
      {
         ShellMessages.success(out, "OpenShift Express (rhc-express) is installed.");
      }
      
   }
   
   @Command
   public void deploy(PipeOut out) throws IOException {
      String[] commitParams = {"commit", "-a", "-m", "\"deploy\""};
      NativeSystemCall.execFromPath("git", commitParams, out, project.getProjectRoot());
      
      String[] pushParams = {"push", "openshift", "HEAD", "-f"};
      NativeSystemCall.execFromPath("git", pushParams, out, project.getProjectRoot());
   }

   
}