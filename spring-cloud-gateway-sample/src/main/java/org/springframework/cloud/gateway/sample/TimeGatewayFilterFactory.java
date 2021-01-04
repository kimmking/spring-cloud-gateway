package org.springframework.cloud.gateway.sample;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

public class TimeGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
	@Override
	public GatewayFilter apply(Object config) {
		return new TimeGatewayFilter();
	}
}