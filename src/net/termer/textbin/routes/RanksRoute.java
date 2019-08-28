package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class RanksRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		SQL.getRanks(res -> {
			if(res.succeeded()) {
				JsonArray ranks = new JsonArray();
				
				for(JsonObject rank : res.result().getRows())
					ranks.add(rank);
				
				// Send ranks
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("ranks", ranks)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
