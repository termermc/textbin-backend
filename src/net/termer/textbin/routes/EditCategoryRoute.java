package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Str;
import net.termer.textbin.db.SQL;

public class EditCategoryRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			// Collect params
			int id = Integer.parseInt(r.request().params().get("id"));
			int rankRequired = Integer.parseInt(r.request().params().get("rank_required"));
			String name = r.request().params().get("name");
			String desc = r.request().params().get("description");
			String code = r.request().params().get("code");
			
			if(name == null || desc == null || code == null) {
				HttpUtils.apiError(r, "Category ID, rank required, name, description, and code must be present");
			} else {
				// Truncate code to four characters
				code = Str.truncate(code, 4);
				
				SQL.updateCategory(id, name, desc, code, rankRequired, res -> {
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
		} catch(NumberFormatException e) {
			HttpUtils.apiError(r, "Category ID and rank must be an integer");
		}
	}
}
