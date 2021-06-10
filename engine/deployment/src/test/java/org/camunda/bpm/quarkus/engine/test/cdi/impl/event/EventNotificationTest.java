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
package org.camunda.bpm.quarkus.engine.test.cdi.impl.event;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EventNotificationTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(TestEventListener.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process2.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.testMultiInstanceEvents.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.testMultiInstanceEventsAfterExternalTrigger.bpmn20.xml"));
  
  protected List<String> deploymentIds = new ArrayList<>();

  @AfterEach
  public void clearDatabase() {
    deploymentIds.forEach(deploymentId -> repositoryService.deleteDeployment(deploymentId, true));
    deploymentIds.clear();
  }

  @Test
  public void testReceiveAll() {
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml")
        .deploy()
        .getId());

    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    // complete user task
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertEquals(16, listenerBean.getEventsReceived().size());
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process1.bpmn20.xml",
      "org/camunda/bpm/engine/cdi/test/impl/event/EventNotificationTest.process2.bpmn20.xml" })
  public void testSelectEventsPerProcessDefinition() {
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml")
        .deploy()
.getId());
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process2.bpmn20.xml")
        .deploy()
.getId());
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getEventsReceivedByKey().size());
    //start the 2 processes
    runtimeService.startProcessInstanceByKey("process1");
    runtimeService.startProcessInstanceByKey("process2");

    // assert that now the bean has received 11 events
    assertEquals(11, listenerBean.getEventsReceivedByKey().size());
  }

  @Test
  public void testSelectEventsPerActivity() {
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml")
        .deploy()
.getId());
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getEndActivityService1());
    assertEquals(0, listenerBean.getStartActivityService1());
    assertEquals(0, listenerBean.getTakeTransition1());

    // start the process
    runtimeService.startProcessInstanceByKey("process1");

    // assert
    assertEquals(1, listenerBean.getEndActivityService1());
    assertEquals(1, listenerBean.getStartActivityService1());
    assertEquals(1, listenerBean.getTakeTransition1());
  }

  @Test
  public void testSelectEventsPerTask() {
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.process1.bpmn20.xml")
        .deploy()
.getId());
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(0, listenerBean.getCreateTaskUser1());
    assertEquals(0, listenerBean.getAssignTaskUser1());
    assertEquals(0, listenerBean.getCompleteTaskUser1());
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    runtimeService.startProcessInstanceByKey("process1");

    Task task = taskService.createTaskQuery().singleResult();
    taskService.setAssignee(task.getId(), "demo");

    taskService.complete(task.getId());

    assertEquals(1, listenerBean.getCreateTaskUser1());
    assertEquals(1, listenerBean.getAssignTaskUser1());
    assertEquals(1, listenerBean.getCompleteTaskUser1());
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    listenerBean.reset();
    assertEquals(0, listenerBean.getDeleteTaskUser1());

    // assert that the bean has received 0 events
    assertEquals(0, listenerBean.getEventsReceived().size());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process1");

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    assertEquals(1, listenerBean.getDeleteTaskUser1());
  }

  @Test
  @Deployment
  public void testMultiInstanceEvents(){
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.testMultiInstanceEvents.bpmn20.xml")
        .deploy()
.getId());
    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    assertEquals(listenerBean.getEventsReceived().size(), 0);
    runtimeService.startProcessInstanceByKey("process1");
    waitForJobExecutorToProcessAllJobs(TimeUnit.SECONDS.toMillis(5L), 500L);

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals(task.getName(), "User Task");

    // 2: start event (start + end)
    // 1: transition to first mi activity
    // 2: first mi body (start + end)
    // 4: two instances of the inner activity (start + end)
    // 1: transition to second mi activity
    // 2: second mi body (start + end)
    // 4: two instances of the inner activity (start + end)
    // 1: transition to the user task
    // 2: user task (start + task create event)
    // = 19
    assertEquals(listenerBean.getEventsReceived().size(), 19);
  }

  @Test
  public void testMultiInstanceEventsAfterExternalTrigger() {
    deploymentIds.add(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/event/EventNotificationTest.testMultiInstanceEventsAfterExternalTrigger.bpmn20.xml")
        .deploy()
.getId());

    runtimeService.startProcessInstanceByKey("process");

    TestEventListener listenerBean = getBeanInstance(TestEventListener.class);
    listenerBean.reset();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    // 6: three user task instances (complete + end)
    // 1: one mi body instance (end)
    // 1: one sequence flow instance (take)
    // 2: one end event instance (start + end)
    // = 5
    Set<BusinessProcessEvent> eventsReceived = listenerBean.getEventsReceived();
    assertEquals(eventsReceived.size(), 10);
  }

}
