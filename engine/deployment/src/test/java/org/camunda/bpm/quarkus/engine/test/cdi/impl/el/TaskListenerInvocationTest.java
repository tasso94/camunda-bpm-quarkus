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
package org.camunda.bpm.quarkus.engine.test.cdi.impl.el;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.MessageBean;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.CdiTaskListenerBean;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.DependentScopedBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.CdiTaskListenerBean.INITIAL_VALUE;
import static org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.CdiTaskListenerBean.UPDATED_VALUE;
import static org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.CdiTaskListenerBean.VARIABLE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskListenerInvocationTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(CdiTaskListenerBean.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/TaskListenerInvocationTest.test.bpmn20.xml"));

  @Test
  public void test() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/TaskListenerInvocationTest.test.bpmn20.xml")
        .deploy();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(VARIABLE_NAME, INITIAL_VALUE);

    runtimeService.startProcessInstanceByKey("process", variables);

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), "demo");

    assertEquals(UPDATED_VALUE, taskService.getVariable(task.getId(), VARIABLE_NAME));
  }

}
