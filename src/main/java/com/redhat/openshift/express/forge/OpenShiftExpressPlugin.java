package com.redhat.openshift.express.forge;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;
import org.jboss.forge.shell.util.NativeSystemCall;

import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.InvalidCredentialsOpenShiftException;
import com.openshift.express.client.OpenShiftException;
import com.openshift.express.internal.client.ApplicationInfo;
import com.openshift.express.internal.client.InternalUser;
import com.openshift.express.internal.client.UserInfo;
import com.redhat.openshift.express.core.OpenShiftServiceFactory;

public @Alias("rhc-express")
@RequiresProject
@RequiresFacet(OpenShiftExpressFacet.class)
class OpenShiftExpressPlugin implements org.jboss.forge.shell.plugins.Plugin {

    @Inject
    private Event<InstallFacets> request;

    @Inject
    Project project;

    @Inject
    OpenShiftExpressConfiguration configuration;
    
    @Inject ShellPrompt prompt;

    @SetupCommand(help = "Install and set up the OpenShift Express plugin")
    public void setup(PipeOut out, @Option(name = "app", help = "Application name (alphanumeric - max 32 chars)") final String app, @Option(name = "rhlogin", help = "Red Hat login (RHN or OpenShift login with OpenShift Express access)") final String rhLogin)
            throws OpenShiftException, IOException {
        if (!project.hasFacet(OpenShiftExpressFacet.class)) {
            configuration.setName(app);
            configuration.setRhLogin(rhLogin);
            request.fire(new InstallFacets(OpenShiftExpressFacet.class));
        }

        if (project.hasFacet(OpenShiftExpressFacet.class)) {
            ShellMessages.success(out, "OpenShift Express (rhc-express) is installed.");
        }

    }

    @Command(help = "Deploys the current application to OpenShift Express")
    public void deploy(PipeOut out) throws Exception {
        String[] commitParams = { "commit", "-a", "-m", "\"deploy\"" };
        NativeSystemCall.execFromPath("git", commitParams, out, project.getProjectRoot());

        String[] remoteParams = { "merge", "openshift/master", "-s", "recursive", "-X", "ours" };
        if (NativeSystemCall.execFromPath("git", remoteParams, out, project.getProjectRoot()) != 0) {
           ShellMessages.error(out, "Failed to rebase onto openshift express");
        }
        
        /*
         * --progress is needed to see git status output from stderr
         */
        String[] pushParams = { "push", "openshift", "HEAD", "-f", "--progress" };
        NativeSystemCall.execFromPath("git", pushParams, out, project.getProjectRoot());
    }
    
    @Command(help = "Checks the status of a deployed application")
    public void status(PipeOut out) throws Exception {
        String rhLogin = Util.getRhLogin(out, prompt);
        String name = Util.getName(project, prompt);
        String password = Util.getPassword(prompt);
        String baseUrl = Util.getDefaultBaseUrl(out);

        IOpenShiftService openshiftService = OpenShiftServiceFactory.create(baseUrl);
        try {
           String status = openshiftService.getStatus(name, ICartridge.JBOSSAS_7, new InternalUser(rhLogin, password, openshiftService));
           out.print(status);
        } catch (InvalidCredentialsOpenShiftException e) {
           Util.displayCredentialsError(out, e);
        }
    }
    
    @Command(help = "Removes the current application from OpenShift Express")
    public void destroy(PipeOut out) throws Exception {
        String rhLogin = Util.getRhLogin(out, prompt);
        String name = Util.getName(project, prompt);
        String password = Util.getPassword(prompt);
        String baseUrl = Util.getDefaultBaseUrl(out);

        boolean confirm = prompt.promptBoolean("About to destroy application " + name + " on OpenShift Express. Are you sure?", true);
        
        if (confirm) {
           IOpenShiftService openshiftService = OpenShiftServiceFactory.create(baseUrl);
           try {
              openshiftService.destroyApplication(name, ICartridge.JBOSSAS_7, new InternalUser(rhLogin, password, openshiftService));
              ShellMessages.success(out, "Destroyed application " + name + " on OpenShift Express");
           } catch (InvalidCredentialsOpenShiftException e) {
              Util.displayCredentialsError(out, e);
           }
        }
    }
    
    @Command(help = "Displays information about your OpenShift Express applications")
    public void list(PipeOut out) throws Exception {
        String rhLogin = Util.getRhLogin(out, prompt);
        String password = Util.getPassword(prompt);
        String baseUrl = Util.getDefaultBaseUrl(out);

        IOpenShiftService openshiftService = OpenShiftServiceFactory.create(baseUrl);
        try {
           UserInfo info = openshiftService.getUserInfo(new InternalUser(rhLogin, password, openshiftService));
           out.println("\nApplications on OpenShift Express:\n");
           for (ApplicationInfo app : info.getApplicationInfos()) {
              out.println(Util.formatApplicationInfo(app, info.getNamespace(), info.getRhcDomain()));
           }
        } catch (InvalidCredentialsOpenShiftException e) {
           Util.displayCredentialsError(out, e);
        }
    }

}