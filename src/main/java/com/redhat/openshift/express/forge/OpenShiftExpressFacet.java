package com.redhat.openshift.express.forge;

import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

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
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.util.NativeSystemCall;

import com.redhat.openshift.express.core.ICartridge;
import com.redhat.openshift.express.core.IOpenshiftService;
import com.redhat.openshift.express.core.OpenShiftServiceFactory;
import com.redhat.openshift.express.core.internal.Application;
import com.redhat.openshift.express.core.internal.InternalUser;

@Alias("forge.openshift.express")
public class OpenShiftExpressFacet extends BaseFacet {

    private static final String EXPRESS_CONF = System.getProperty("user.home") + "/.openshift/express.conf";

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
        Properties conf = new Properties();
        try {
            conf.load(new FileReader(EXPRESS_CONF));
            ShellMessages.info(out, "Loaded OpenShift configuration from " + EXPRESS_CONF);
        } catch (Exception e) {
            // Swallow
        }

        String name = getName(getProject().getFacet(MetadataFacet.class).getProjectName());
        String rhLogin = getRhLogin(conf.getProperty("default_rhlogin"));
        configuration.setName(null);
        configuration.setRhLogin(null);
        String password = prompt.promptSecret("Enter your Red Hat Login password");
        IOpenshiftService openshift = OpenShiftServiceFactory.create();

        Application application = openshift.createApplication(name, ICartridge.JBOSSAS_7, new InternalUser(rhLogin, password));

        if (!project.getProjectRoot().getChildDirectory(".git").exists()) {
            String[] params = { "init" };
            NativeSystemCall.execFromPath("git", params, out, project.getProjectRoot());
        }

        ShellMessages.info(out, "Waiting for OpenShift to propagate DNS");
        if (!waitForExpress(application.getApplicationUrl(), out)) {
            ShellMessages.error(out, "OpenShift did not propagate DNS properly");
            return false;
        }

        if (!Util.isOpenshiftRemotePresent(out, project)) {
            String[] remoteParams = { "remote", "add", "openshift", "-f", application.getGitUri() };
            NativeSystemCall.execFromPath("git", remoteParams, out, project.getProjectRoot());
        }
        addOpenShiftProfile();

        ShellMessages.success(out, "Application deployed to " + application.getApplicationUrl());

        return true;
    }

    private String getName(String _default) {
        if (configuration.getName() == null) {
            return prompt.prompt("Enter the application name [" + _default + "] ", String.class, _default);
        } else {
            return configuration.getName();
        }
    }

    private String getRhLogin(String _default) {
        if (configuration.getRhLogin() == null) {
            return prompt.prompt("Enter your Red Hat Login [" + _default + "] ", String.class, _default);
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
        for (int i = 0; i < 200; i++) {
            try {
                if (i % 5 == 0)
                    ShellMessages.info(out, "Trying to contact " + url + " (attempt " + (i + 1) + " of 200)");
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
