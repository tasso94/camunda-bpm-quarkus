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
package org.camunda.bpm.quarkus.engine.test.cdi.api;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.quarkus.engine.test.cdi.CdiProcessEngineTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class ProcessVariableMapTest extends CdiProcessEngineTestCase {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .withConfigurationResource("application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/cdi/api/BusinessProcessBeanTest.test.bpmn20.xml"));

  private static final String VARNAME_1 = "aVariable";
  private static final String VARNAME_2 = "anotherVariable";

  @Test
  public void testProcessVariableMap() {
    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);

    VariableMap variables = (VariableMap) getBeanInstance("processVariableMap");
    assertNotNull(variables);

    ///////////////////////////////////////////////////////////////////
    // Put a variable via BusinessProcess and get it via VariableMap //
    ///////////////////////////////////////////////////////////////////
    String aValue = "aValue";
    businessProcess.setVariable(VARNAME_1, Variables.stringValue(aValue));

    // Legacy API
    assertEquals(aValue, variables.get(VARNAME_1));

    // Typed variable API
    TypedValue aTypedValue = variables.getValueTyped(VARNAME_1);
    assertEquals(ValueType.STRING, aTypedValue.getType());
    assertEquals(aValue, aTypedValue.getValue());
    assertEquals(aValue, variables.getValue(VARNAME_1, String.class));

    // Type API with wrong type
    try {
      variables.getValue(VARNAME_1, Integer.class);
      fail("ClassCastException expected!");
    } catch(ClassCastException ex) {
      assertEquals("Cannot cast variable named 'aVariable' with value 'aValue' to type 'class java.lang.Integer'.", ex.getMessage());
    }

    ///////////////////////////////////////////////////////////////////
    // Put a variable via VariableMap and get it via BusinessProcess //
    ///////////////////////////////////////////////////////////////////
    String anotherValue = "anotherValue";
    variables.put(VARNAME_2, Variables.stringValue(anotherValue));

    // Legacy API
    assertEquals(anotherValue, businessProcess.getVariable(VARNAME_2));

    // Typed variable API
    TypedValue anotherTypedValue = businessProcess.getVariableTyped(VARNAME_2);
    assertEquals(ValueType.STRING, anotherTypedValue.getType());
    assertEquals(anotherValue, anotherTypedValue.getValue());
  }

  @Test
  public void testProcessVariableMapLocal() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/cdi/api/BusinessProcessBeanTest.test.bpmn20.xml")
        .deploy();

    BusinessProcess businessProcess = getBeanInstance(BusinessProcess.class);
    businessProcess.startProcessByKey("businessProcessBeanTest");

    VariableMap variables = (VariableMap) getBeanInstance("processVariableMapLocal");
    assertNotNull(variables);

    ///////////////////////////////////////////////////////////////////
    // Put a variable via BusinessProcess and get it via VariableMap //
    ///////////////////////////////////////////////////////////////////
    String aValue = "aValue";
    businessProcess.setVariableLocal(VARNAME_1, Variables.stringValue(aValue));

    // Legacy API
    assertEquals(aValue, variables.get(VARNAME_1));

    // Typed variable API
    TypedValue aTypedValue = variables.getValueTyped(VARNAME_1);
    assertEquals(ValueType.STRING, aTypedValue.getType());
    assertEquals(aValue, aTypedValue.getValue());
    assertEquals(aValue, variables.getValue(VARNAME_1, String.class));

    // Type API with wrong type
    try {
      variables.getValue(VARNAME_1, Integer.class);
      fail("ClassCastException expected!");
    } catch(ClassCastException ex) {
      assertEquals("Cannot cast variable named 'aVariable' with value 'aValue' to type 'class java.lang.Integer'.", ex.getMessage());
    }

    ///////////////////////////////////////////////////////////////////
    // Put a variable via VariableMap and get it via BusinessProcess //
    ///////////////////////////////////////////////////////////////////
    String anotherValue = "anotherValue";
    variables.put(VARNAME_2, Variables.stringValue(anotherValue));

    // Legacy API
    assertEquals(anotherValue, businessProcess.getVariableLocal(VARNAME_2));

    // Typed variable API
    TypedValue anotherTypedValue = businessProcess.getVariableLocalTyped(VARNAME_2);
    assertEquals(ValueType.STRING, anotherTypedValue.getType());
    assertEquals(anotherValue, anotherTypedValue.getValue());
  }
}
