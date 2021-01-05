package org.springframework.cloud.gateway.sample;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.core.style.ToStringCreator;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TokenRoutePredicateFactory extends AbstractRoutePredicateFactory<TokenRoutePredicateFactory.Config> {

	public TokenRoutePredicateFactory() {
		super(TokenRoutePredicateFactory.Config.class);
	}

	@Override
	public ShortcutType shortcutType() {
		return ShortcutType.GATHER_LIST;
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Collections.singletonList("token");
	}

	@Override
	public Predicate<ServerWebExchange> apply(TokenRoutePredicateFactory.Config config) {
		return new GatewayPredicate() {
			@Override
			public boolean test(ServerWebExchange exchange) {
				String token = exchange.getRequest().getHeaders().getFirst("Token");
				return null == token ? false : validate(token);
			}

			private boolean validate(String token) {
				// 可以替换成shiro、jwt、oauth或者cas/sso之类的，，其实这个场景更适合用filter
				// 这里只是演示一下predicate，传递的参数如果是以这个token开头就运行，走指定路由
				return token.toLowerCase().startsWith(config.getToken().toLowerCase());
			}

			@Override
			public String toString() {
				return String.format("Token: %s", config.getToken());
			}
		};
	}

	public static class Config {

		private String token = null;

		public String getToken() {
			return token;
		}

		public TokenRoutePredicateFactory.Config setToken(String token) {
			this.token = token;
			return this;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("token", token).toString();
		}
	}
}
