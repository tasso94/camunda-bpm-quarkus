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
package org.camunda.bpm.quarkus.engine.deployment;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.runtime.RuntimeValue;
import org.camunda.bpm.engine.ProcessEngine;

public final class ProcessEngineBuildItem extends SimpleBuildItem {

  protected RuntimeValue<ProcessEngine> processEngine;

  public ProcessEngineBuildItem(RuntimeValue<ProcessEngine> processEngine) {
    this.processEngine = processEngine;
  }

  public RuntimeValue<ProcessEngine> getProcessEngine() {
    return processEngine;
  }

}
