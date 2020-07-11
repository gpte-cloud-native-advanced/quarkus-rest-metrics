package com.redhat.emergency.response.quarkus.rest.metrics;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.metrics.MetricRegistries;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.Tag;

public class QuarkusJaxRsMetricsFilter implements ContainerRequestFilter {

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        Long start = System.nanoTime();
        final Class<?> resourceClass = resourceInfo.getResourceClass();
        final Method resourceMethod = resourceInfo.getResourceMethod();
        /*
         * The reason for using a Vert.x handler instead of ContainerResponseFilter is that
         * RESTEasy does not call the response filter for requests that ended up with an unmapped exception.
         * This way we can capture these responses as well and update the metrics accordingly.
         */
        RoutingContext routingContext = CDI.current().select(CurrentVertxRequest.class).get().getCurrent();
        routingContext.addBodyEndHandler(
                event -> finishRequest(start, resourceClass, resourceMethod));
    }

    private void finishRequest(Long start, Class<?> resourceClass, Method resourceMethod) {
        long value = System.nanoTime() - start;
        MetricID metricID = getMetricID(resourceClass, resourceMethod);

        MetricRegistry registry = MetricRegistries.get(MetricRegistry.Type.APPLICATION);
        if (!registry.getMetadata().containsKey(metricID.getName())) {
            // if no metric with this name exists yet, register it
            Metadata metadata = Metadata.builder()
                    .withName(metricID.getName())
                    .withDescription(
                            "The distribution of the invocation time of this RESTful resource method since the start of the server.")
                    .withUnit(MetricUnits.NANOSECONDS)
                    .build();
            registry.timer(metadata, metricID.getTagsAsArray());
        }
        registry.timer(metricID.getName(), metricID.getTagsAsArray())
                .update(value, TimeUnit.NANOSECONDS);
    }

    private MetricID getMetricID(Class<?> resourceClass, Method resourceMethod) {
        Tag classTag = new Tag("class", resourceClass.getName());
        String methodName = resourceMethod.getName();
        String encodedParameterNames = Arrays.stream(resourceMethod.getParameterTypes())
                .map(clazz -> {
                    if (clazz.isArray()) {
                        return clazz.getComponentType().getName() + "[]";
                    } else {
                        return clazz.getName();
                    }
                })
                .collect(Collectors.joining("_"));
        String methodTagValue = encodedParameterNames.isEmpty() ? methodName : methodName + "_" + encodedParameterNames;
        Tag methodTag = new Tag("method", methodTagValue);
        return new MetricID("REST.request", classTag, methodTag);
    }
}
