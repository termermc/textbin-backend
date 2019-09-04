package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Module;
import net.termer.textbin.Str;
import net.termer.textbin.crypt.TripGen;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;
import net.termer.twine.utils.Callback;

public class PostReplyRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		Session sess = r.session();
		
		String captcha = r.request().params().get("captcha");
		captcha = captcha == null ? "invalid" : captcha;
		
		// Check captcha
		if(captcha.equalsIgnoreCase(sess.get("captcha"))) {
			String postId = r.request().params().get("post_id");
			// Check if post ID is valid
			if(postId != null && postId.length() > 9) {
				// Setup callback chain
				new Callback().then(cb -> {
					// Check if user is banned before posting
					try {
						SQL.getBan(HttpUtils.ip(r), res -> {
							if(res.succeeded()) {
								if(res.result().getRows().size() > 0) {
									HttpUtils.apiError(r, "banned");
									cb.end();
								} else {
									cb.next();
								}
							} else {
								cb.fail(res.cause());
							}
						});
					} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
						e.printStackTrace();
						HttpUtils.apiError(r, "Internal error");
						cb.end();
					}
				}, f -> {
					f.cause().printStackTrace();
					HttpUtils.apiError(r, "Database error");
					f.end();
				}).then(cb -> {
					// Query to check if post ID exists
					SQL.postExists(postId, res -> {
						if(res.succeeded()) {
							if(res.result().getRows().get(0).getInteger("count") > 0) {
								// Posts exists, proceed to next callback
								cb.next();
							} else {
								HttpUtils.apiError(r, "Invalid post ID");
								cb.end();
							}
						} else {
							cb.fail(res.cause());
						}
					});
				}, f -> {
					f.cause().printStackTrace();
					HttpUtils.apiError(r, "Database error");
					f.end();
				}).then(cb -> {
					// Collect params
					String comment = r.request().params().get("comment");
					String name = r.request().params().get("name");
					name = name != null && name.trim().length() > 0 ? Str.truncate(name.trim(), 40) : "Anonymous";
					String email = r.request().params().get("email");
					email = email != null && name.trim().length() > 0 ? Str.truncate(email.trim(), 30) : null;
					
					if(comment != null && comment.length() > 0) {
						if(comment.trim().equalsIgnoreCase("bump")) {
							HttpUtils.apiError(r, "bump");
							cb.end();
						} else {
							short kb = 1024;
							int mb = 1024*kb;
							
							// Check if comment is less than half a MB long
							try {
								if(comment.getBytes("UTF-8").length > 0.5*mb) {
									HttpUtils.apiError(r, "Comment must be less than half a megabyte long");
									cb.end();
								} else {
									// Get date and time
									String date = Module.date();
									String time = Module.time();
									
									try {
										// Hash IP
										String ip = HttpUtils.ip(r);
										
										// Resolve trip
										String trip = null;
										if(name.contains("#")) {
											if(Module.config().trip_strategy.equals("secure")) {
												trip = TripGen.secure(name);
											} else if(Module.config().trip_strategy.equals("classic-secure")) {
												trip = TripGen.classic(name, true);
											} else {
												trip = TripGen.classic(name, false);
											}
											name = name.split("#")[0];
										}
										
										// Put data for next callback
										cb.data()
											.put("name", name)
											.put("comment", comment)
											.put("date", date)
											.put("time", time)
											.put("ip", ip)
											.put("email", email)
											.put("trip", trip);
										
										// Proceed to query
										cb.next();
									} catch (NoSuchAlgorithmException | UnsupportedEncodingException | CharacterCodingException e) {
										cb.fail(e);
									}
								}
							} catch (UnsupportedEncodingException ex) {
								// I never expect this to trigger
								ex.printStackTrace();
							}
						}
					} else {
						HttpUtils.apiError(r, "Comments may not be blank");
						cb.end();
					}
				}, f -> {
					f.cause().printStackTrace();
					HttpUtils.apiError(r, "Internal error");
					f.end();
				}).then(cb -> {
					// Alias to cb.data()
					JsonObject cd = cb.data();
					SQL.postComment(
							cd.getString("name"),
							cd.getString("comment"),
							cd.getString("date"),
							cd.getString("time"),
							postId,
							cd.getString("ip"),
							cd.getString("email"),
							cd.getString("trip"),
							r.session().get("rank"),
							res -> {
						if(res.succeeded()) {
							// Delete captcha session variable to stop re-use
							sess.put("captcha", null);
							
							r.response().end(
								new JsonObject()
									.put("status", "success")
									.encode()
							);
							
							// Bump thread if not sage
							if(r.request().params().contains("email") && !r.request().params().get("email").equalsIgnoreCase("sage")) {
								cb.next();
							} else {
								cb.end();
							}
						} else {
							cb.fail(res.cause());
						}
					});
				}, f -> {
					f.cause().printStackTrace();
					HttpUtils.apiError(r, "Database error");
					f.end();
				}).then(cb -> {
					// Bump post
					SQL.bumpPost(postId, Module.date(), Module.time(), res -> {
						if(!res.succeeded()) {
							cb.fail(res.cause());
						}
						cb.end();
					});
				}, f -> {
					Twine.logger().error("Error occurred while bumping thread");
					f.cause().printStackTrace();
				}).execute();
			}
		} else {
			HttpUtils.apiError(r, "invalid_captcha");
		}
	}
}
