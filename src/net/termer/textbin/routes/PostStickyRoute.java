package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class PostStickyRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		if(r.request().params().get("sticky") == null) {
			HttpUtils.apiError(r, "Must specify post sticky value");
		} else if(r.request().params().get("post_id") == null) {
			HttpUtils.apiError(r, "No post ID provided");
		} else {
			String postId = r.request().params().get("post_id");
			boolean sticky = r.request().params().get("sticky").equals("true");
			SQL.setPostSticky(postId, sticky, res -> {
				if(res.succeeded()) {
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
	}
}
