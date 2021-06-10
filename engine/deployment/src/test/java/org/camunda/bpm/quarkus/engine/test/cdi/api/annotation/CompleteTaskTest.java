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
import org.camunda.bpm.engine.cdi.impl.annotation.CompleteTaskInterceptor;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.DeclarativeProcessController;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testcase for assuring that the {@link CompleteTaskInterceptor} works as
 * expected
 **/
public class CompleteTaskTest extends CdiProcessEngineTestCase {

  protected static final String RESOURCE = "org/camunda/bpm/quarkus/engine/test/cdi/api" +
      "/annotation/CompleteTaskTest.bpmn20.xml";

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(DeclarativeProcessController.class)
          .addAsResource(RESOURCE));

  @Test
  public void testCompleteTask() {
    processEngine.getRepositoryService().createDeployment().addClasspathResource(RESOURCE).deploy();

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    businessProcess.startProcessByKey("keyOfTheProcess");

    Task task = taskService.createTaskQuery().singleResult();
    
    // associate current unit of work with the task:
    businessProcess.startTask(task.getId());

    getBeanInstance(DeclarativeProcessController.class).completeTask();

    // assert that now the task is completed
    assertNull(taskService.createTaskQuery().singleResult());
  }

  
}
