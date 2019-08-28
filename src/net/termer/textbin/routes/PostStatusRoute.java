package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Module;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;
import net.termer.twine.utils.Callback;

public class PostStatusRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Retrieve hashed IP address
		String ip = null;
		try {
			ip = HttpUtils.ip(r);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Twine.logger().error("Failed to hash IP address");
			e.printStackTrace();
			HttpUtils.apiError(r, "Internal error");
		}
		if(ip != null) {
			// Prepare callback chain
			Callback c = new Callback();
			c.data().put("ip", ip);
			
			new Callback().then(cb -> {
				// Query for remaining posts for the hashed IP
				SQL.postLimit(cb.data().getString("ip"), res -> {
					if(res.succeeded()) {
						int cnt = res.result().getNumRows();
						int rem = Math.max(0, Module.config().max_posts-cnt);
						cb.data().put("remaining", rem);
						
						// Perform next query
						cb.next();
					} else {
						// Pass error to failure handler
						cb.fail(res.cause());
					}
				});
			}, f -> {
				Twine.logger().error("Error fetching post limit");
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).then(cb -> {
				SQL.getCategories(r.session().get("rank"), res -> {
					if(res.succeeded()) {
						JsonArray cats = new JsonArray();
						for(JsonObject cat : res.result().getRows()) {
							cats.add(cat);
						}
						
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.put("remaining", cb.data().getInteger("remaining"))
								.put("categories", cats)
								.encode()
						);
						cb.end();
					} else {
						cb.fail(res.cause());
					}
				});
			}, f -> {
				Twine.logger().error("Error while fetching categories");
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}).execute();
		}
	}
}