package org.camunda.bpm.quarkus.rest.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;

class CamundaBpmQuarkusRestProcessor {

    protected static final String FEATURE = "camunda-bpm-quarkus-rest";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void addDependency(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(
            new IndexDependencyBuildItem("org.camunda.bpm.quarkus", "camunda-bpm-quarkus-rest"));
    }

}
