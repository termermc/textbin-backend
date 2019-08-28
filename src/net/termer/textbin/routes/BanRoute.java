package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;
import net.termer.twine.utils.Callback;

public class BanRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		String ip = r.request().params().get("ip");
		String commentId = r.request().params().get("comment_id");
		String postId = r.request().params().get("post_id");
		String reason = r.request().params().get("reason");
		boolean delete = r.request().params().get("delete") == null ? false : r.request().params().get("delete").equals("true");
		String banText = r.request().params().get("ban_text");
		
		// Validate input
		if(commentId != null) {
			// Prepare callback chain
			new Callback().then(cb -> {
				// Check if ban text is defined if not deleting post
				if(!delete && banText == null) {
					HttpUtils.apiError(r, "No ban text provided");
					cb.end();
				} else {
					try {
						SQL.createBanFromComment(Integer.parseInt(commentId), reason, (int) r.session().get("account"), res -> {
							if(res.succeeded()) {
								cb.next();
							} else {
								cb.fail(res.cause());
							}
						});
					} catch(NumberFormatException e) {
						e.printStackTrace();
						HttpUtils.apiError(r, "Invalid comment ID");
						cb.end();
					}
				}
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).then(cb -> {
				// Delete comment or set its ban text
				if(delete) {
					SQL.deleteComment(Integer.parseInt(commentId), res -> {
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
				} else {
					SQL.updateCommentBan(Integer.parseInt(commentId), banText, res -> {
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
				}
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).execute();
		} else if(postId != null) {
			// Prepare callback chain
			new Callback().then(cb -> {
				SQL.createBanFromPost(postId, reason, (int) r.session().get("account"), res -> {
					if(res.succeeded()) {
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
				// Delete post if specified
				if(delete) {
					SQL.deletePost(postId, res -> {
						if(res.succeeded()) {
							// Delete comments on post
							cb.next();
						} else {
							cb.fail(res.cause());
						}
					});
				} else {
					// Send success and end callbacks
					r.response().end(
						new JsonObject()
							.put("status", "success")
							.encode()
					);
					
					cb.end();
				}
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
				f.end();
			}).then(cb -> {
				SQL.deleteComments(postId, res -> {
					if(res.succeeded()) {
						// Success
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
		} else if(ip == null || reason == null) {
			HttpUtils.apiError(r, "No IP or reason");
		} else if(ip.equals(r.request().remoteAddress().host())) {
			HttpUtils.apiError(r, "Please don't ban yourself, you have so much to live for!");
		} else if(HttpUtils.validIp(ip)) {
			try {
				// Use hash if provided
				SQL.createBan(HttpUtils.hashIp(ip), reason, (int) r.session().get("account"), res -> {
					if(res.succeeded()) {
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.encode()
						);
					} else {
						System.out.println("F");
						res.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
					}
				});
			} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
				Twine.logger().error("Failed to hash IP address for ban");
				e.printStackTrace();
				HttpUtils.apiError(r, "Internal error");
			}
		} else {
			HttpUtils.apiError(r, "Invalid IP address");
		}
	}
}
