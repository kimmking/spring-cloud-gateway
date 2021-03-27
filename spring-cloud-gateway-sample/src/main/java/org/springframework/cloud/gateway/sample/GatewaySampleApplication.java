/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.sample;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledPredicate;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * @author Spencer Gibb
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(AdditionalRoutes.class)
public class GatewaySampleApplication {

	public static final String HELLO_FROM_FAKE_ACTUATOR_METRICS_GATEWAY_REQUESTS = "hello from fake /actuator/metrics/gateway.requests";

	@Value("${test.uri:http://httpbin.org:80}")
	String uri;

	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}

	@Bean
	public RouteLocator getRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.path("/t/**")
				.filters(f -> f.stripPrefix(1).filter(new ZGatewayFilter()).filter(new TimeGatewayFilter())).uri(uri).order(9997).id("timefilter_java_route")).build();
	}

	@Bean
	public RouteLocator getTokenRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.predicate(new TokenRoutePredicateFactory().apply(new TokenRoutePredicateFactory.Config().setToken("KK"))).and().path("/token/**")
				.filters(f -> f.stripPrefix(1).filter(new ZGatewayFilter()).filter(new TimeGatewayFilter()).addResponseHeader("KK15","token_java_route")).uri(uri).order(9998).id("token_java_route")).build();
	}

	@Bean
	public RouteLocator getKKRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.path("/kk1/**")
				.filters(f -> f.stripPrefix(1).prefixPath("/status").filter(new ZGatewayFilter()).addResponseHeader("KK08","kk_route"))
				.uri(uri).order(9990).id("kk_route")).build();
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		//@formatter:off
		// String uri = "http://httpbin.org:80";
		// String uri = "http://localhost:9080";
		return builder.routes()
				.route(r -> r.host("**.abc.org").and().path("/anything/png")
					.filters(f ->
							f.prefixPath("/httpbin")
									.addResponseHeader("X-TestHeader", "foobar"))
					.uri(uri)
				)
				.route("read_body_pred", r -> r.host("*.readbody.org")
						.and().readBody(String.class,
										s -> s.trim().equalsIgnoreCase("hi"))
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "read_body_pred")
					).uri(uri)
				)
				.route("rewrite_request_obj", r -> r.host("*.rewriterequestobj.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_request")
							.modifyRequestBody(String.class, Hello.class, MediaType.APPLICATION_JSON_VALUE,
									(exchange, s) -> {
										return Mono.just(new Hello(s.toUpperCase()));
									})
					).uri(uri)
				)
				.route("rewrite_request_upper", r -> r.host("*.rewriterequestupper.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_request_upper")
							.modifyRequestBody(String.class, String.class,
									(exchange, s) -> {
										return Mono.just(s.toUpperCase() + s.toUpperCase());
									})
					).uri(uri)
				)
				.route("rewrite_response_upper", r -> r.host("*.rewriteresponseupper.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_response_upper")
							.modifyResponseBody(String.class, String.class,
									(exchange, s) -> {
										return Mono.just(s.toUpperCase());
									})
					).uri(uri)
				)
				.route("rewrite_empty_response", r -> r.host("*.rewriteemptyresponse.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_empty_response")
							.modifyResponseBody(String.class, String.class,
									(exchange, s) -> {
										if (s == null) {
											return Mono.just("emptybody");
										}
										return Mono.just(s.toUpperCase());
									})

					).uri(uri)
				)
				.route("rewrite_response_fail_supplier", r -> r.host("*.rewriteresponsewithfailsupplier.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_response_fail_supplier")
							.modifyResponseBody(String.class, String.class,
									(exchange, s) -> {
										if (s == null) {
											return Mono.error(new IllegalArgumentException("this should not happen"));
										}
										return Mono.just(s.toUpperCase());
									})
					).uri(uri)
				)
				.route("rewrite_response_obj", r -> r.host("*.rewriteresponseobj.org")
					.filters(f -> f.prefixPath("/httpbin")
							.addResponseHeader("X-TestHeader", "rewrite_response_obj")
							.modifyResponseBody(Map.class, String.class, MediaType.TEXT_PLAIN_VALUE,
									(exchange, map) -> {
										Object data = map.get("data");
										return Mono.just(data.toString());
									})
							.setResponseHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
					).uri(uri)
				)
				.route(r -> r.path("/image/webp")
					.filters(f ->
							f.prefixPath("/httpbin")
									.addResponseHeader("X-AnotherHeader", "baz"))
					.uri(uri)
				)
				.route(r -> r.order(-1)
					.host("**.throttle.org").and().path("/get")
					.filters(f -> f.prefixPath("/httpbin")
									.filter(new ZGatewayFilter())
									.filter(new ThrottleGatewayFilter()
									.setCapacity(1)
									.setRefillTokens(1)
									.setRefillPeriod(10)
									.setRefillUnit(TimeUnit.SECONDS)))
					.uri(uri)
				)
				.build();
		//@formatter:on
	}

	@Bean
	public RouterFunction<ServerResponse> testFunRouterFunction() {
		RouterFunction<ServerResponse> route = RouterFunctions.route(
				RequestPredicates.path("/testfun"),
				request -> ServerResponse.ok().body(BodyInserters.fromValue("hello")));
		return route;
	}

	@Bean
	public RouterFunction<ServerResponse> timeRouterFunction() {
		RouterFunction<ServerResponse> route = RouterFunctions.route(
				RequestPredicates.path("/time"),
				request -> {
					System.out.println("2=》"+request.uri());
					return ServerResponse.ok().body(BodyInserters.fromValue("time"));
				});
		return route.filter((request, next) -> {
				System.out.println("1=》"+request.uri());
				Mono<ServerResponse> response = next == null ? null : next.handle(request);
				System.out.println("3=》"+request.uri());
				return response;
			});
	}

	@Bean
	public RouterFunction<ServerResponse> testWhenMetricPathIsNotMeet() {
		RouterFunction<ServerResponse> route = RouterFunctions.route(
				RequestPredicates.path("/actuator/metrics/gateway.requests"),
				request -> ServerResponse.ok().body(BodyInserters
						.fromValue(HELLO_FROM_FAKE_ACTUATOR_METRICS_GATEWAY_REQUESTS)));
		return route;
	}

	static class Hello {

		String message;

		Hello() {
		}

		Hello(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

	@Bean
	@ConditionalOnEnabledFilter
	public TimeGatewayFilterFactory addTimeGatewayFilterFactory() {
		return new TimeGatewayFilterFactory();
	}

	@Bean
	@ConditionalOnEnabledPredicate
	public TokenRoutePredicateFactory addTokenRoutePredicateFactory() {
		return new TokenRoutePredicateFactory();
	}

}
