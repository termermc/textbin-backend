package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class CommentsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			String ip = HttpUtils.ip(r);
			String postId = r.request().params().get("post_id");
			if(postId == null) {
				HttpUtils.apiError(r, "Invalid post ID");
			} else {
				SQL.getComments(postId, res -> {
					if(res.succeeded()) {
						JsonArray comments = new JsonArray();
						
						for(JsonObject post : res.result().getRows()) {
							String pIp = post.getString("ip");
							
							// Preserve IP hash if user is a moderator
							if(((int) r.session().get("rank")) < 1) 
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
			}
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			HttpUtils.apiError(r, "Internal error");
		}
	}
}
