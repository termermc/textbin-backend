package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;
import net.termer.twine.utils.Callback;

public class DeleteCategoryRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			// Fetch ID
			int id = Integer.parseInt(r.request().params().get("id"));
			
			Twine.logger().info("Preparing to delete category ID "+id+"...");
			
			// Setup callback chain
			new Callback().then(cb -> {
				Twine.logger().info("Deleting comments...");
				// Delete comments
				SQL.deleteCommentsByCategory(id, res -> {
					if(res.succeeded()) {
						// Next callback
						cb.next();
					} else {
						cb.fail(res.cause());
					}
				});
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).then(cb -> {
				Twine.logger().info("Deleting posts...");
				// Delete posts
				SQL.deletePostsByCategory(id, res -> {
					if(res.succeeded()) {
						// Next callback
						cb.next();
					} else {
						cb.fail(res.cause());
					}
				});
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).then(cb -> {
				Twine.logger().info("Deleting category...");
				SQL.deleteCategory(id, res -> {
					// Success
					Twine.logger().info("Successfully deleted category ID "+id);
					if(res.succeeded()) {
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.encode()
						);
					} else {
						cb.fail(res.cause());
					}
				});
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).execute();
		} catch(NumberFormatException e) {
			HttpUtils.apiError(r, "Category ID must be an integer");
		}
	}
}
