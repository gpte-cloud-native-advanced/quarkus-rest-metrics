package com.redhat.emergency.response.quarkus.rest.metrics;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import org.jboss.logging.Logger;

public class RestMetricsProcessor {

    private static final Logger log = Logger.getLogger("io.quarkus.rest-metrics");

    private static final String FEATURE = "rest-metrics";

    @BuildStep
    void enableMetrics(RestMetricsConfig buildConfig, BuildProducer<ResteasyJaxrsProviderBuildItem> jaxRsProviders, Capabilities capabilities) {
        if (buildConfig.metricsEnabled && capabilities.isCapabilityPresent(Capabilities.METRICS)) {
            if (capabilities.isCapabilityPresent(Capabilities.SERVLET)) {
                // if running with servlet, do nothing
                log.warn("Running with servlet, rest metrics are not enabled");
            } else {
                jaxRsProviders.produce(
                        new ResteasyJaxrsProviderBuildItem("com.redhat.emergency.response.quarkus.rest.metrics.QuarkusJaxRsMetricsFilter"));
            }
        }
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

}
