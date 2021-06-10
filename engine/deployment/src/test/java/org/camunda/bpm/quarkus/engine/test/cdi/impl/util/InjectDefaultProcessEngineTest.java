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
package org.camunda.bpm.quarkus.engine.test.cdi.impl.util;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.InjectedProcessEngineBean;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InjectDefaultProcessEngineTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(InjectedProcessEngineBean.class));

  @Test
  public void testProcessEngineInject() {
    //given only default engine exist

    //when TestClass is created
    InjectedProcessEngineBean testClass = ProgrammaticBeanLookup.lookup(InjectedProcessEngineBean.class);
    Assertions.assertNotNull(testClass);

    //then default engine is injected
    Assertions.assertEquals("default", testClass.processEngine.getName());
  }
}
