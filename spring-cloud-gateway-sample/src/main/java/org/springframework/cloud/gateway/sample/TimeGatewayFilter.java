package org.springframework.cloud.gateway.sample;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TimeGatewayFilter implements GatewayFilter, Ordered {

	private static final String REQUEST_TIME_BEGIN = "request-time-begin";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//记录请求开始时间
		exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
				if (startTime != null) {
					//打印 时间
					System.out.println(exchange.getRequest().getURI() + " 耗时" + (System.currentTimeMillis() - startTime) + "ms");
				}
		}));
	}

	@Override
	public int getOrder() {
		//filter执行的优先级,值越小则优先级越大
		return Integer.MIN_VALUE;
	}
}
