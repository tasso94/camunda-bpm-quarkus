/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.quarkus.engine.extension;

import io.quarkus.agroal.runtime.DataSources;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.CdiJtaProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventSupportBpmnParseListener;
import org.camunda.bpm.engine.cdi.impl.util.BeanManagerLookup;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.NotifyAcquisitionRejectedJobsHandler;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.List;

import static com.arjuna.ats.jta.TransactionManager.transactionManager;

@Recorder
public class CamundaBpmRecorder {

  public RuntimeValue<ProcessEngine> createProcessEngine(String dataSource,
                                                         String databaseType,
                                                         BeanContainer beanContainer,
                                                         CamundaBpmConfig config) {
    ProcessEngineConfigurationImpl engineConfig = null;

    boolean isConfigBeanPresent = true;
    try {
      engineConfig = beanContainer.instance(ProcessEngineConfigurationImpl.class);
    } catch (RuntimeException e) {
      isConfigBeanPresent = false;
    }

    // agroal does not support transaction management for H2
    if (engineConfig instanceof CdiJtaProcessEngineConfiguration
        || (engineConfig == null && !DatabaseKind.H2.equals(databaseType))) {
      CdiJtaProcessEngineConfiguration cdiJtaProcessEngineConfiguration = null;
      if (engineConfig == null) {
        cdiJtaProcessEngineConfiguration = new CdiJtaProcessEngineConfiguration();

      } else {
        cdiJtaProcessEngineConfiguration = (CdiJtaProcessEngineConfiguration) engineConfig;

      }

      if (cdiJtaProcessEngineConfiguration.getTransactionManager() == null
          && !DatabaseKind.H2.equals(databaseType)) {
        TransactionManager transactionManager = transactionManager();
        cdiJtaProcessEngineConfiguration.setTransactionManager(transactionManager);
      }
    }

    if (engineConfig == null) {
      engineConfig = new CdiStandaloneProcessEngineConfiguration();
    }

    if (engineConfig.getProcessEngineName() == null) {
      engineConfig.setProcessEngineName("default");
    }

    if (engineConfig.getDatabaseType() == null) {
      engineConfig.setDatabaseType(databaseType);
    }

    if (engineConfig.getDataSource() == null) {
      engineConfig.setDataSource(DataSources.fromName(dataSource));
    }

    if (!isConfigBeanPresent) {
      engineConfig.setDatabaseSchemaUpdate(ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_TRUE);
    }

    if (engineConfig.getJobExecutor() == null) {
      ManagedExecutor managedExecutor = beanContainer.instance(ManagedExecutor.class);
      QuarkusJobExecutor quarkusJobExecutor = new QuarkusJobExecutor(managedExecutor);
      engineConfig.setJobExecutor(quarkusJobExecutor);
    }

    if (!engineConfig.isJobExecutorActivate()) {
      engineConfig.setJobExecutorActivate(config.jobExecutorActivate);
    }

    if (engineConfig.getCustomPostBPMNParseListeners() == null) {
      ArrayList<BpmnParseListener> parseListeners = new ArrayList<>();
      parseListeners.add(new CdiEventSupportBpmnParseListener());
      engineConfig.setCustomPostBPMNParseListeners(parseListeners);
    }

    if (BeanManagerLookup.localInstance == null) {
      BeanManagerLookup.localInstance = beanContainer.instance(BeanManager.class);
    }
    ProcessEngine processEngine = engineConfig.buildProcessEngine();

    RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
    runtimeContainerDelegate.registerProcessEngine(processEngine);

    return new RuntimeValue<>(processEngine);
  }

  public RuntimeValue<ProcessApplication> deployProcessApplication() {
    ProcessApplication processApplication = new ProcessApplication();
    processApplication.deploy();
    return new RuntimeValue<>(processApplication);
  }

  public void registerShutdownTask(ShutdownContext shutdownContext,
                                   RuntimeValue<ProcessEngine> processEngine,
                                   RuntimeValue<ProcessApplication> processApplication) {
    shutdownContext.addShutdownTask(() -> {
      RuntimeContainerDelegate runtimeContainerDelegate = RuntimeContainerDelegate.INSTANCE.get();
      runtimeContainerDelegate.unregisterProcessEngine(processEngine.getValue());

      JobExecutor jobExecutor =
          ((ProcessEngineConfigurationImpl) processEngine.getValue().getProcessEngineConfiguration()).getJobExecutor();

      jobExecutor.shutdown();

      runtimeContainerDelegate.undeployProcessApplication(processApplication.getValue());
    });
  }

  public void emitStartupEvent() {
    Arc.container().beanManager().fireEvent(new EngineStartupEvent());
  }

}
