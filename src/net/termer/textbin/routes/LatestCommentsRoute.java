package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class LatestCommentsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Get limit
		int limit = 0;
		if(r.request().params().get("limit") != null)
			limit = Integer.parseInt(r.request().params().get("limit"));
		
		try {
			String ip = HttpUtils.ip(r);
			SQL.latestComments(limit, res -> {
				if(res.succeeded()) {
					JsonArray comments = new JsonArray();
					
					for(JsonObject post : res.result().getRows()) {
						String pIp = post.getString("ip");
						post.remove("ip");
						if(pIp.equals(ip)) {
							post.put("you", true);
						} else {
							post.put("you", false);
						}
						
						comments.add(post);
					}
					
					r.response().end(
						new JsonObject()
							.put("status", "success")
							.put("comments", comments)
							.encode()
					);
				} else {
					res.cause().printStackTrace();
					HttpUtils.apiError(r, "Database error");
				}
			});
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			HttpUtils.apiError(r, "Internal error");
		}
	}
}
