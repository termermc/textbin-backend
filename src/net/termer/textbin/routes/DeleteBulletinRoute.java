package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class DeleteBulletinRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		if(((int) r.session().get("account")) > -1) {
			String id = r.request().params().get("id");
			
			if(id == null) {
				HttpUtils.apiError(r, "No ID provided");
			} else {
				try {
					SQL.deleteBulletin(Integer.parseInt(id), res -> {
						if(res.succeeded()) {
							// Succeeded
							r.response().end(
								new JsonObject()
									.put("status", "success")
									.encode()
							);
						} else {
							res.cause().printStackTrace();
							HttpUtils.apiError(r, "Database error");
						}
					});
				} catch(NumberFormatException e) {
					HttpUtils.apiError(r, "ID must be an integer");
				}
			}
		} else {
			HttpUtils.apiError(r, "Not logged in");
		}
	}
}
