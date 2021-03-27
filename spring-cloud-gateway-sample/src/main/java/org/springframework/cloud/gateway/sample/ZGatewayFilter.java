package org.springframework.cloud.gateway.sample;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ZGatewayFilter implements GatewayFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		System.out.println("Z ==> "+chain);
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			System.out.println("Z ==> ZZZZZZZZ");
		}));
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}
}
