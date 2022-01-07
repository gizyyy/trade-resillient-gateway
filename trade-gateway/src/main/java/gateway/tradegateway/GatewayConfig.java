package gateway.tradegateway;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.handler.predicate.QueryRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import reactor.core.publisher.Mono;

/**
 * Gateway forwards customer either to overview application or trader server
 * application. Config comes from this file and also application.yml.
 * 
 * @author gizem.yilmaz
 *
 */
@Configuration
public class GatewayConfig {

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder,
			QueryRoutePredicateFactory queryRoutePredicateFactory) {
		Function<PredicateSpec, Buildable<Route>> funtionForRest = r -> r.path("/tradegateway/entry/**").filters(f -> f
				.rewritePath("/tradegateway/entry/(?<path>.*)", "/trade/${path}").addRequestHeader("source", "gateway"))
				.uri("http://localhost:8080");

		return builder.routes().route("rest", funtionForRest).build();
	}

	@Bean
	public RouterFunction<ServerResponse> routerFunction() {
		return RouterFunctions.route(RequestPredicates.GET("overview-fallback"), this::handleGetFallback);
	}
	public Mono<ServerResponse> handleGetFallback(ServerRequest serverRequest) {
		return ServerResponse.ok().body(Mono.empty(), String.class);
	}
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()).build());
	}

}
