package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class BansRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Determine limit
		int limit = 0;
		try {
			limit = Integer.parseInt(r.request().params().get("limit"));
		} catch(Exception e) {}
		
		// Get bans
		SQL.getBans(limit, res -> {
			if(res.succeeded()) {
				// Add bans to aray
				JsonArray bans = new JsonArray();
				for(JsonObject ban : res.result().getRows()) {
					bans.add(ban);
				}
				
				// Send JSON reply
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("bans", bans)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
