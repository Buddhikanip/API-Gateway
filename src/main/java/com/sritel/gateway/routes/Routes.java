package com.sritel.gateway.routes;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.util.List;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Bean
    public RouterFunction<ServerResponse> accountServiceRoute() {
        String accountServiceUrl = getServiceUrl("account-service");

        return route("account-service")
                .route(RequestPredicates.path("/api/v1/account"), HandlerFunctions.http(accountServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("accountServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> accountServiceSwaggerRoute() {
        String accountServiceUrl = getServiceUrl("account-service");

        return route("account-service-swagger")
                .route(RequestPredicates.path("/aggregate/account-service/v3/api-docs"), HandlerFunctions.http(accountServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("accountServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> provisioningServiceRoute() {
        String provisioningServiceUrl = getServiceUrl("provisioning-service");

        return route("provisioning-service")
                .route(RequestPredicates.path("/api/v1/provisioning"), HandlerFunctions.http(provisioningServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("provisioningServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> provisioningServiceSwaggerRoute() {
        String provisioningServiceUrl = getServiceUrl("provisioning-service");

        return route("provisioning-service-swagger")
                .route(RequestPredicates.path("/aggregate/provisioning-service/v3/api-docs"), HandlerFunctions.http(provisioningServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("provisioningServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> billingServiceRoute() {
        String billingServiceUrl = getServiceUrl("billing-service");

        return route("billing-service")
                .route(RequestPredicates.path("/api/v1/billing"), HandlerFunctions.http(billingServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("billingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> billingServiceSwaggerRoute() {
        String billingServiceUrl = getServiceUrl("billing-service");

        return route("billing-service-swagger")
                .route(RequestPredicates.path("/aggregate/billing-service/v3/api-docs"), HandlerFunctions.http(billingServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("billingServiceSwaggerCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> discoveryServiceRoute() {
        return route("discovery-service")
                .route(RequestPredicates.path("/eureka/web"), HandlerFunctions.http("http://localhost:8761"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("discoveryServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .filter(setPath("/"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> discoveryServiceStaticRoute() {
        return route("discovery-service-static")
                .route(RequestPredicates.path("/eureka/**"), HandlerFunctions.http("http://localhost:8761"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("discoveryServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable"))
                .build();
    }

    private String getServiceUrl(String service) {
        List<ServiceInstance> instances = discoveryClient.getInstances(service);
        if (!instances.isEmpty()) {
            ServiceInstance serviceInstance = instances.get(0);
            return serviceInstance.getUri().toString();
        } else {
            return String.format("http://localhost:8080/%s", service);
        }
    }
}
