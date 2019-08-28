package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class CategoryRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			// Fetch ID
			int id = Integer.parseInt(r.request().params().get("id"));
			
			SQL.getCategory(id, res -> {
				if(res.succeeded()) {
					if(res.result().getNumRows() > 0) {
						JsonObject resp = res.result().getRows().get(0);
						
						// Send category info
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.put("name", resp.getString("category_name"))
								.put("description", resp.getString("category_description"))
								.put("code", resp.getString("category_code"))
								.put("rank_required", resp.getInteger("category_rank_required"))
								.encode()
						);
					} else {
						HttpUtils.apiError(r, "Invalid category ID");
					}
				} else {
					res.cause().printStackTrace();
					HttpUtils.apiError(r, "Database error");
				}
			});
		} catch(NumberFormatException e) {
			HttpUtils.apiError(r, "Category ID must be an integer");
		}
	}
}
