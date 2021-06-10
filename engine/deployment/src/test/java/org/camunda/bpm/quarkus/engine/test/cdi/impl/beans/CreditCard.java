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
package org.camunda.bpm.quarkus.engine.test.cdi.impl.beans;

import org.camunda.bpm.engine.cdi.annotation.BusinessProcessScoped;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@BusinessProcessScoped
@ApplicationScoped // newly added
public class CreditCard implements Serializable {

  private String creditcardNumber;

  public void setCreditcardNumber(String creditcardNumber) {
    this.creditcardNumber = creditcardNumber;
  }

  public String getCreditcardNumber() {
    return creditcardNumber;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CreditCard other = (CreditCard) obj;
    if (creditcardNumber == null) {
      if (other.creditcardNumber != null)
        return false;
    } else if (!creditcardNumber.equals(other.creditcardNumber))
      return false;
    return true;
  }

}
