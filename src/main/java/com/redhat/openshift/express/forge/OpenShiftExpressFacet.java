package com.redhat.openshift.express.forge;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.maven.plugins.ConfigurationBuilder;
import org.jboss.forge.maven.plugins.ConfigurationElementBuilder;
import org.jboss.forge.maven.plugins.MavenPlugin;
import org.jboss.forge.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.maven.plugins.MavenPluginBuilder;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.util.NativeSystemCall;

import com.openshift.express.client.IApplication;
import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.OpenShiftEndpointException;
import com.openshift.express.internal.client.InternalUser;
import com.redhat.openshift.express.core.OpenShiftServiceFactory;

@Alias("forge.openshift.express")
public class OpenShiftExpressFacet extends BaseFacet {
  
    private static final int MAX_WAIT = 500;

    @Inject
    private ShellPrompt prompt;

    @Inject
    private ShellPrintWriter out;

    @Inject
    OpenShiftExpressConfiguration configuration;

    @Override
    public boolean isInstalled() {
        try {
            return Util.isGitInit(project) && Util.isOpenshiftRemotePresent(out, project);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean install() {
        try {
            return internalInstall();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean internalInstall() throws Exception {       
        
        String name = getName();
        String rhLogin = getRhLogin();
        String baseUrl = Util.getDefaultBaseUrl(out);

        // Wipe the singleton
        configuration.setName(null);
        configuration.setRhLogin(null);
        String password = Util.getPassword(prompt);
        IOpenShiftService openshift = OpenShiftServiceFactory.create(baseUrl);
        IApplication application = null;
        try {
            application = openshift.createApplication(name, ICartridge.JBOSSAS_7, new InternalUser(rhLogin, password, openshift));
        } catch (OpenShiftEndpointException e) {
           ShellMessages.error(out, "OpenShift failed to create the application");
           ShellMessages.error(out, e.getMessage());
           if (e.getCause().getClass() != null)
              ShellMessages.error(out, e.getCause().getMessage());
           return false;
        }
        

        if (!project.getProjectRoot().getChildDirectory(".git").exists()) {
            String[] params = { "init" };
            if (NativeSystemCall.execFromPath("git", params, out, project.getProjectRoot()) != 0)
               return false;
        }

        ShellMessages.info(out, "Waiting for OpenShift to propagate DNS");
        if (!waitForExpress(application.getApplicationUrl(), out)) {
            ShellMessages.error(out, "OpenShift did not propagate DNS properly");
            return false;
        }

        if (!Util.isOpenshiftRemotePresent(out, project)) {
            String[] remoteParams = { "remote", "add", "openshift", "-f", application.getGitUri() };
            if (NativeSystemCall.execFromPath("git", remoteParams, out, project.getProjectRoot()) != 0) {
               ShellMessages.error(out, "Failed to connect to OpenShift Express GIT repository, project is in an inconsistent state. Remove the .git directory manually, and delete the application using rhc-ctl-app -c destroy -a " + application.getName() + " -b");
               return false;
            }
        } else
           ShellMessages.info(out, "'openshift' remote alias already present in Git, using it");
        
        addOpenShiftProfile();

        ShellMessages.success(out, "Application deployed to " + application.getApplicationUrl());

        return true;
    }

    private String getName() {
        if (configuration.getName() == null) {
            return Util.getName(project, prompt);
        } else {
            return configuration.getName();
        }
    }

    private String getRhLogin() {
        if (configuration.getRhLogin() == null) {
            return Util.getRhLogin(out, prompt);
        } else {
            return configuration.getRhLogin();
        }
    }

    private void addOpenShiftProfile() {
        MavenCoreFacet facet = getProject().getFacet(MavenCoreFacet.class);
        for (Profile p : facet.getPOM().getProfiles()) {
            if (p.getId().equals("openshift"))
                return;
        }
        MavenPlugin plugin = MavenPluginBuilder
                .create()
                .setDependency(
                        DependencyBuilder.create().setGroupId("org.apache.maven.plugins").setArtifactId("maven-war-plugin")
                                .setVersion("2.1.1"))
                .setConfiguration(
                        ConfigurationBuilder
                                .create()
                                .addConfigurationElement(
                                        ConfigurationElementBuilder.create().setName("outputDirectory").setText("deployments"))
                                .addConfigurationElement(
                                        ConfigurationElementBuilder.create().setName("warName").setText("ROOT")));
        Profile profile = new Profile();
        profile.setId("openshift");
        profile.setBuild(new BuildBase());
        profile.getBuild().addPlugin(new MavenPluginAdapter(plugin));

        Model pom = facet.getPOM();
        pom.addProfile(profile);
        facet.setPOM(pom);
    }

    private boolean waitForExpress(String url, ShellPrintWriter out) {
        for (int i = 0; i < MAX_WAIT; i++) {
            try {
                if (i % 5 == 0)
                    ShellMessages.info(out, "Trying to contact " + url + " (attempt " + (i + 1) + " of " + MAX_WAIT + ")");
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    return true;
            } catch (Exception e) {

            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

}
