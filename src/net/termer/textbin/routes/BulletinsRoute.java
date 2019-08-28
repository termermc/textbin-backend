package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class BulletinsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		SQL.getBulletins(res -> {
			if(res.succeeded()) {
				JsonArray bulletins = new JsonArray();
				
				for(JsonObject bulletin : res.result().getRows())
					bulletins.add(bulletin);
				
				// Send response
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("bulletins", bulletins)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
