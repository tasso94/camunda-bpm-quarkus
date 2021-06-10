package org.acme;

import org.camunda.bpm.engine.cdi.BusinessProcess;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello-resteasy")
public class GreetingResource {

    @Inject
    protected BusinessProcess businessProcess;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        businessProcess.startProcessByKey("Process_1pq0lae");
        return "Hello RESTEasy";
    }
}