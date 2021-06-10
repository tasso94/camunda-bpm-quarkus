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
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.MessageBean;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.ProcessScopedMessageBean;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.el.beans.DependentScopedBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ElTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(DependentScopedBean.class)
          .addClass(MessageBean.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/ElTest.testSetBeanProperty.bpmn20.xml")
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/ElTest.testDependentScoped.bpmn20.xml"));

  @Test
  public void testSetBeanProperty() throws Exception {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/ElTest.testSetBeanProperty.bpmn20.xml")
        .deploy();

    MessageBean messageBean = getBeanInstance(MessageBean.class);
    runtimeService.startProcessInstanceByKey("setBeanProperty");
    assertEquals("Greetings from Berlin", messageBean.getMessage());
  }

  @Test
  public void testDependentScoped() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/impl/el/ElTest.testDependentScoped.bpmn20.xml")
        .deploy();

    DependentScopedBean.reset();

    runtimeService.startProcessInstanceByKey("testProcess");

    // make sure the complete bean lifecycle (including invocation of @PreDestroy) was executed.
    // This ensures that the @Dependent scoped bean was properly destroyed.
    assertEquals(Arrays.asList("post-construct-invoked", "bean-invoked", "pre-destroy-invoked"), DependentScopedBean.lifecycle);
  }

}
