package org.camunda.bpm.quarkus.engine.test;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.quarkus.engine.extension.EngineStartupEvent;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatchEngineStartupEventTest {

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
        .withConfigurationResource("application.properties")
        .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
            .addClass(EventCatcher.class)
            .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/TaskListenerInvocationTest.test.bpmn20.xml"));

    @ApplicationScoped
    static class EventCatcher {

        @Inject
        RepositoryService repositoryService;

        @Inject
        RuntimeService runtimeService;

        public void deploy(@Observes EngineStartupEvent ev) {
            repositoryService.createDeployment()
                .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/TaskListenerInvocationTest.test.bpmn20.xml")
                .deploy();

            runtimeService.startProcessInstanceByKey("process", "quarkus-rocks");
        }

    }

    @Inject
    protected RuntimeService runtimeService;

    @Test
    public void shouldCheckProcessInstanceStarted() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

        assertEquals(processInstance.getBusinessKey(), "quarkus-rocks");
    }

}
