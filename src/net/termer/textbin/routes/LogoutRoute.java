package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;

public class LogoutRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		r.session().put("rank", 0);
		r.session().put("account", -1);
		
		r.response().end(
			new JsonObject()
				.put("status", "success")
				.encode()
		);
	}
}
