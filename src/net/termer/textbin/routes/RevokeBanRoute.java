package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class RevokeBanRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			int id = Integer.parseInt(r.request().params().get("id"));
			SQL.deleteBan(id, res -> {
				if(res.succeeded()) {
					// Succeeded
					r.response().end(
						new JsonObject()
							.put("status", "success")
							.encode()
					);
				} else {
					HttpUtils.apiError(r, "Database error");
				}
			});
		} catch(NumberFormatException e) { // Handle non-number ID
			e.printStackTrace();
			HttpUtils.apiError(r, "Invalid ban ID");
		}
	}
}
