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
package org.camunda.bpm.quarkus.engine.extension;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.eclipse.microprofile.context.ManagedExecutor;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

public class QuarkusJobExecutor extends JobExecutor {

  protected ManagedExecutor taskExecutor;

  public QuarkusJobExecutor(ManagedExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  @Override
  protected void startExecutingJobs() {
    startJobAcquisitionThread();
  }

  @Override
  protected void stopExecutingJobs() {
    stopJobAcquisitionThread();
  }

  @Override
  public void executeJobs(List<String> jobIds, ProcessEngineImpl processEngine) {
    try {
      taskExecutor.execute(getExecuteJobsRunnable(jobIds, processEngine));
    } catch (RejectedExecutionException e) {

      logRejectedExecution(processEngine, jobIds.size());
      rejectedJobsHandler.jobsRejected(jobIds, processEngine, this);
    }
  }
}
