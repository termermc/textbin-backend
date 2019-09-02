package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class PublicPostsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Get limit and offset
		int limit = 0;
		int offset = 0;
		try {
			if(r.request().params().get("limit") != null)
				limit = Integer.parseInt(r.request().params().get("limit"));
			if(r.request().params().get("offset") != null)
				offset = Integer.parseInt(r.request().params().get("offset"));
		} catch(NumberFormatException e) {
			// Simply exclude values
		}
		
		// Query for public posts
		SQL.publicPosts(r.request().params().get("category"), limit, offset, res -> {
			if(res.succeeded()) {
				JsonArray posts = new JsonArray();
				for(JsonObject post : res.result().getRows()) {
					posts.add(post);
				}
				
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("posts", posts)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
