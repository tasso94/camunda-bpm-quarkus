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
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.quarkus.engine.test.cdi.impl.beans.SpecializedTestBean;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgrammaticBeanLookupTest {


  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(TestBean.class)
          .addClass(AlternativeTestBean.class)
          //.addClass(SpecializedTestBean.class) not supported by quarkus
          .addClass(BeanWithProducerMethods.class));

  @Test
  public void testLookupBean() {
    Object lookup = ProgrammaticBeanLookup.lookup("testBean");
    assertTrue(lookup instanceof TestBean);
  }

  @Test
  public void testLookupShouldFindAlternative() {
    Object lookup = ProgrammaticBeanLookup.lookup("alternativeTestBean");
    assertTrue(lookup instanceof AlternativeTestBean);
  }

  @Test
  @Disabled("specialization not supported")
  public void testLookupShouldFindSpecialization() {
    Object lookup = ProgrammaticBeanLookup.lookup("specializedTestBean");
    assertTrue(lookup instanceof SpecializedTestBean);
  }

  @Test
  public void testLookupShouldSupportProducerMethods() {
    assertEquals("exampleString", ProgrammaticBeanLookup.lookup("producedString"));
  }

  @Named
  @Singleton // newly added
  public static class TestBean {
  }

  @Named("alternativeTestBean")
  @ApplicationScoped // newly added
  public static class OtherTestBean extends TestBean {
  }

  @Alternative
  @Priority(1) // newly added
  @Named("alternativeTestBean")
  @ApplicationScoped // newly added
  public static class AlternativeTestBean extends OtherTestBean {
  }
}