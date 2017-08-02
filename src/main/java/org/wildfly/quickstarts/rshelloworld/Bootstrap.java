package org.wildfly.quickstarts.rshelloworld;

import java.util.Collections;
import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.weld.environment.servlet.Listener;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

/**
 * @author Stuart Douglas
 */
public class Bootstrap {

    public static void main(String... args) {

        try {

            ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
            resteasyDeployment.setInjectorFactoryClass("org.jboss.resteasy.cdi.CdiInjectorFactory");
            resteasyDeployment.setActualResourceClasses(Collections.singletonList(HelloWorld.class));
            DeploymentInfo info = Servlets.deployment()
                    .setDeploymentName("helloworld-rs")
                    .setContextPath("/helloworld-rs")
                    .addWelcomePage("index.html")
                    .setClassLoader(Bootstrap.class.getClassLoader())
                    .setResourceManager(new ClassPathResourceManager(Bootstrap.class.getClassLoader(), "web"))
                    .addListener(Servlets.listener(Listener.class))
                    .addServlet(Servlets.servlet(HttpServlet30Dispatcher.class)
                            .addMappings("/rest/*")
                            .setAsyncSupported(true)
                            .setLoadOnStartup(1)
                            .addInitParam("resteasy.servlet.mapping.prefix", "/rest"))
                    .addServletContextAttribute(ResteasyDeployment.class.getName(), resteasyDeployment);

            DeploymentManager deploymentManager = Servlets.defaultContainer()
                    .addDeployment(info);

            deploymentManager.deploy();
            HttpHandler handler = deploymentManager.start();

            PathHandler pathHandler = Handlers.path()
                    .addPrefixPath("/helloworld-rs", handler);


            Undertow undertow = Undertow.builder()
                    .addHttpListener(8081, "localhost")
                    .setHandler(pathHandler)
                    .build();
            undertow.start();

        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
