package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class GetPostRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		String id = r.request().params().get("id");
		id = id == null ? "invalidid" : id;
		// Query for post
		SQL.viewPost(id, res -> {
			if(res.succeeded()) {
				if(res.result().getNumRows() > 0) {
					JsonObject post = res.result().getRows().get(0);
					post.put("status", "success");
					r.response().end(post.encode());
				} else {
					HttpUtils.apiError(r, "not_found");
				}
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
