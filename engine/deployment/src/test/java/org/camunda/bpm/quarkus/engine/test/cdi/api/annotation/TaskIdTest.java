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
package org.camunda.bpm.quarkus.engine.test.cdi.api.annotation;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.DeclarativeProcessController;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TaskIdTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/TaskIdTest.testTaskIdInjectable.bpmn20.xml"));

  @Test
  public void testTaskIdInjectable() {
    processEngine.getRepositoryService().createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/TaskIdTest.testTaskIdInjectable.bpmn20.xml")
        .deploy();

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    businessProcess.startProcessByKey("keyOfTheProcess");  
    
    businessProcess.startTask(taskService.createTaskQuery().singleResult().getId());   
    
    // assert that now the 'taskId'-bean can be looked up
    assertNotNull(getBeanInstance("taskId"));
    
    businessProcess.completeTask();
  }
  
}
