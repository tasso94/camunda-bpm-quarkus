package org.camunda.bpm.quarkus.rest;

import com.fasterxml.jackson.core.util.JacksonFeature;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;

import javax.enterprise.inject.Default;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/engine-rest")
public class QuarkusApplication extends Application {

    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(CamundaRestResources.getResourceClasses());
        classes.addAll(CamundaRestResources.getConfigurationClasses());
        classes.add(JacksonFeature.class);
        classes.add(MyResource.class);
        return classes;
    }

}