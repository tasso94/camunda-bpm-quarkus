package org.camunda.bpm.quarkus.engine.test;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.StartupEvent;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.quarkus.engine.extension.EngineStartupEvent;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomConfigurationBeanTest {


    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
        .withConfigurationResource("application.properties")
        .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
            .addClass(MyConfig.class)
            .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/TaskListenerInvocationTest.test.bpmn20.xml"));

    @ApplicationScoped
    static class MyConfig {

        @Produces
        @Unremovable
        public CdiStandaloneProcessEngineConfiguration getConfig() {
            CdiStandaloneProcessEngineConfiguration cdiJtaProcessEngineConfiguration = new CdiStandaloneProcessEngineConfiguration();
            cdiJtaProcessEngineConfiguration.setProcessEngineName("quarkus-rocks");
            cdiJtaProcessEngineConfiguration.setDatabaseSchemaUpdate("create-drop");
            return cdiJtaProcessEngineConfiguration;
        }

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
    protected ProcessEngine processEngine;

    @Test
    public void shouldUseCustomConfigurationBean() {
        assertEquals(processEngine.getName(), "quarkus-rocks");
    }

}
