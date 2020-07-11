package com.redhat.emergency.response.quarkus.rest.metrics;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_TIME;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "rest-metrics", phase = BUILD_TIME)
public class RestMetricsConfig {

    /**
     * Whether or not JAX-RS metrics should be enabled if the Metrics capability is present and Vert.x is being used.
     */
    @ConfigItem(name = "metrics.enabled", defaultValue = "false")
    public boolean metricsEnabled;

}
