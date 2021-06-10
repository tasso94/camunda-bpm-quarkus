package org.camunda.bpm.quarkus.engine.extension;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = "camunda.bpm")
public class CamundaBpmConfig {

  /**
   * Controls whether the process engine starts with an active job executor or not.
   */
  @ConfigItem(defaultValue = "true")
  public boolean jobExecutorActivate;

}