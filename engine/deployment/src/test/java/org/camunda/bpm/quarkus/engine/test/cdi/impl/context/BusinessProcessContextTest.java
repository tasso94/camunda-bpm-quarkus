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
package org.camunda.bpm.quarkus.engine.test.cdi.impl.context;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.CreditCard;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.ProcessScopedMessageBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BusinessProcessContextTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(CreditCard.class)
          .addClass(ProcessScopedMessageBean.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testResolution.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testConversationalBeanStoreFlush.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testChangeProcessScopedBeanProperty.bpmn20.xml"));


  @Test
  public void testResolution() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testResolution.bpmn20.xml")
        .deploy();

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    businessProcess.startProcessByKey("testResolution").getId();

    assertNotNull(getBeanInstance(CreditCard.class));
  }

  @Test
  public void testResolutionBeforeProcessStart() {
    // assert that @BusinessProcessScoped beans can be resolved in the absence of an underlying process instance:
    assertNotNull(getBeanInstance(CreditCard.class));
  }

  @Test
  @Deployment
  public void testConversationalBeanStoreFlush() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testConversationalBeanStoreFlush.bpmn20.xml")
        .deploy();

    getBeanInstance(BusinessProcess.class).setVariable("testVariable", "testValue");
    String pid =  getBeanInstance(BusinessProcess.class).startProcessByKey("testConversationalBeanStoreFlush").getId();

    getBeanInstance(BusinessProcess.class).associateExecutionById(pid);

    // assert that the variable assigned on the businessProcess bean is flushed 
    assertEquals("testValue", runtimeService.getVariable(pid, "testVariable"));

    // assert that the value set to the message bean in the first service task is flushed
    assertEquals("Hello from Activiti", getBeanInstance(ProcessScopedMessageBean.class).getMessage());
    
    // complete the task to allow the process instance to terminate
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
  }

  @Test
  public void testChangeProcessScopedBeanProperty() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/context/BusinessProcessContextTest.testChangeProcessScopedBeanProperty.bpmn20.xml")
        .deploy();
    
    // resolve the creditcard bean (@BusinessProcessScoped) and set a value:
    getBeanInstance(CreditCard.class).setCreditcardNumber("123");
    String pid = getBeanInstance(BusinessProcess.class).startProcessByKey("testConversationalBeanStoreFlush").getId();
    
    getBeanInstance(BusinessProcess.class).startTask(taskService.createTaskQuery().singleResult().getId());
        
    // assert that the value of creditCardNumber is '123'
    assertEquals("123", getBeanInstance(CreditCard.class).getCreditcardNumber());
    // set a different value:
    getBeanInstance(CreditCard.class).setCreditcardNumber("321");
    // complete the task
    getBeanInstance(BusinessProcess.class).completeTask();
    
    getBeanInstance(BusinessProcess.class).associateExecutionById(pid);

    // now assert that the value of creditcard is "321":
    assertEquals("321", getBeanInstance(CreditCard.class).getCreditcardNumber());
    
    // complete the task to allow the process instance to terminate
    taskService.complete(taskService.createTaskQuery().singleResult().getId());
    
  }
    
}
