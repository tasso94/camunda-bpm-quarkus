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
import org.camunda.bpm.engine.cdi.annotation.ExecutionIdLiteral;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.DeclarativeProcessController;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutionIdTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/ExecutionIdTest.testExecutionIdInjectableByName.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/ExecutionIdTest.testExecutionIdInjectableByQualifier.bpmn20.xml"));

  @Test
  public void testExecutionIdInjectableByName() {
    processEngine.getRepositoryService().createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/ExecutionIdTest.testExecutionIdInjectableByName.bpmn20.xml")
        .deploy();

    getBeanInstance(BusinessProcess.class).startProcessByKey("keyOfTheProcess");
    String processInstanceId = (String) getBeanInstance("processInstanceId");
    Assertions.assertNotNull(processInstanceId);
    String executionId = (String) getBeanInstance("executionId");
    Assertions.assertNotNull(executionId);
    
    assertEquals(processInstanceId, executionId);
  }
  
  @Test
  public void testExecutionIdInjectableByQualifier() {
    processEngine.getRepositoryService().createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/api/annotation/ExecutionIdTest.testExecutionIdInjectableByQualifier.bpmn20.xml")
        .deploy();

    getBeanInstance(BusinessProcess.class).startProcessByKey("keyOfTheProcess");
    
    Set<Bean<?>> beans = beanManager.getBeans(String.class, new ExecutionIdLiteral());    
    Bean<String> bean = (Bean<String>) beanManager.resolve(beans);
    
    CreationalContext<String> ctx = beanManager.createCreationalContext(bean);
    String executionId = (String) beanManager.getReference(bean, String.class, ctx);   
    Assertions.assertNotNull(executionId);
    
    String processInstanceId = (String) getBeanInstance("processInstanceId");
    Assertions.assertNotNull(processInstanceId);
    
    assertEquals(processInstanceId, executionId);
  }
  
}
