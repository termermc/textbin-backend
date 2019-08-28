package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Str;
import net.termer.textbin.db.SQL;

public class CreateCategoryRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			// Collect params
			String name = r.request().params().get("name");
			String desc = r.request().params().get("description");
			String code = r.request().params().get("code");
			int rank = Integer.parseInt(r.request().params().get("rank"));
			
			if(name == null || desc == null || code == null) {
				HttpUtils.apiError(r, "Must provide name, description, code, and rank");
			} else {
				// Truncate code to 4 characters or less
				code = Str.truncate(code, 4);
				
				SQL.createCategory(name, code, desc, rank, res -> {
					if(res.succeeded()) {
						// Success
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
		} catch(NumberFormatException e) {
			HttpUtils.apiError(r, "Poster rank required must be an integer");
		}
	}
}
