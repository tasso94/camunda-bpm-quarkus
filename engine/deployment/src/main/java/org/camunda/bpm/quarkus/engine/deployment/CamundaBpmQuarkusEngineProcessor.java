package org.camunda.bpm.quarkus.engine.deployment;

import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.cdi.ProcessVariables;
import org.camunda.bpm.engine.cdi.compat.CamundaTaskForm;
import org.camunda.bpm.engine.cdi.compat.FoxTaskForm;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableLocalMap;
import org.camunda.bpm.engine.cdi.impl.ProcessVariableMap;
import org.camunda.bpm.engine.cdi.impl.context.DefaultContextAssociationManager;
import org.camunda.bpm.engine.cdi.jsf.TaskForm;
import org.camunda.bpm.quarkus.engine.extension.CamundaBpmConfig;
import org.camunda.bpm.quarkus.engine.extension.CamundaBpmRecorder;
import org.jboss.jandex.DotName;

import java.util.List;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

class CamundaBpmQuarkusEngineProcessor {

    protected static final String FEATURE = "camunda-bpm-quarkus-engine";

    @BuildStep
    protected FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    protected UnremovableBeanBuildItem unremovableBeans() {
        return UnremovableBeanBuildItem.beanTypes(
            DotName.createSimple(DefaultContextAssociationManager.class.getName() + "$RequestScopedAssociation"));
    }

    @BuildStep
    protected AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
            .setDefaultScope(DotNames.APPLICATION_SCOPED)
            .addBeanClasses(
                DefaultContextAssociationManager.class,
                BusinessProcess.class,
                ProcessVariableLocalMap.class,
                ProcessVariables.class,
                ProcessVariableMap.class,
                CamundaTaskForm.class,
                FoxTaskForm.class,
                TaskForm.class
            ).build();
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    protected ProcessEngineBuildItem processEngine(CamundaBpmRecorder recorder,
                                                   List<JdbcDataSourceBuildItem> jdbcDataSourcesBuildItems,
                                                   BeanContainerBuildItem beanContainerBuildItem,
                                                   CamundaBpmConfig config) {
        JdbcDataSourceBuildItem jdbcDataSourceBuildItem = jdbcDataSourcesBuildItems.stream()
            .findFirst()
            .filter(JdbcDataSourceBuildItem::isDefault)
            .orElseThrow(() -> new ProcessEngineException("Please configure agroal datasource!"));

        String databaseName = jdbcDataSourceBuildItem.getName();
        String databaseType = jdbcDataSourceBuildItem.getDbKind();
        return new ProcessEngineBuildItem(recorder.createProcessEngine(databaseName, databaseType, beanContainerBuildItem.getValue(), config));
    }

    @Consume(ProcessEngineBuildItem.class)
    @BuildStep
    @Record(RUNTIME_INIT)
    void startupEvent(CamundaBpmRecorder recorder) {
        recorder.emitStartupEvent();
    }

    @Consume(ProcessEngineBuildItem.class)
    @BuildStep
    @Record(RUNTIME_INIT)
    ProcessApplicationDeployedBuildItem processApplication(CamundaBpmRecorder recorder) {
        return new ProcessApplicationDeployedBuildItem(recorder.deployProcessApplication());
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    void shutdown(CamundaBpmRecorder recorder,
                  ProcessEngineBuildItem processEngineBuildItem,
                  ProcessApplicationDeployedBuildItem processApplicationDeployedBuildItem,
                  ShutdownContextBuildItem shutdownContext) {
        recorder.registerShutdownTask(shutdownContext,
            processEngineBuildItem.getProcessEngine(),
            processApplicationDeployedBuildItem.getProcessApplication());
    }

}
