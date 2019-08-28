package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Module;
import net.termer.textbin.db.SQL;

public class CreateBulletinRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		if(((int) r.session().get("account")) > -1) {
			String content = r.request().params().get("content");
			String date = Module.date();
			String time = Module.time();
			
			if(content == null) {
				HttpUtils.apiError(r, "No content provided");
			} else {
				SQL.createBulletin((int) r.session().get("account"), content, date, time, res -> {
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
			}
		} else {
			HttpUtils.apiError(r, "Not logged in");
		}
	}
}
