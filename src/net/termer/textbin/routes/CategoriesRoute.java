package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class CategoriesRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		SQL.getCategories(res -> {
			if(res.succeeded()) {
				JsonArray cats = new JsonArray();
				
				for(JsonObject cat : res.result().getRows()) {
					cats.add(cat);
				}
				
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("categories", cats)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
