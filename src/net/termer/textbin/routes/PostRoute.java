package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.Module;
import net.termer.textbin.Str;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;
import net.termer.twine.utils.Callback;
import net.termer.twine.utils.StringFilter;

public class PostRoute implements Handler<RoutingContext> {
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
			Callback callback = new Callback();
			callback.data().put("ip", ip);
			callback.then(cb -> {
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
				// Query for remaining posts for the hashed IP
				SQL.postLimit(cb.data().getString("ip"), res -> {
					if(res.succeeded()) {
						int cnt = res.result().getNumRows();
						int rem = Math.max(0, Module.config().max_posts-cnt);
						
						// Check post limit
						if(rem > 0) {
							// Perform next query
							cb.next();
						} else {
							HttpUtils.apiError(r, "Wait for a post to expire before posting again");
							cb.end();
						}
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
				// Read params
				String text = r.request().params().get("text");
				
				// Check if post is empty or unset
				if(text != null && text.length() > 0) {
					int category = -1; // Private
					try {
						category = Integer.parseInt(r.request().params().get("category"));
					} catch(NumberFormatException e) { /* Category will be the default -1 */ }
					
					// Check if poster is allowed to post in the specified category
					SQL.getCategory(category, res -> {
						if(res.succeeded()) {
							if(
							   res.result().getNumRows() > 0 &&
							   res.result().getRows().get(0).getInteger("category_rank_required") <= (int) r.session().get("rank") 
							) {
								int cat = -1; // Private
								try {
									cat = Integer.parseInt(r.request().params().get("category"));
								} catch(NumberFormatException e) { /* Category will be the default -1 */ }
								
								// Generate ID
								String id = StringFilter.generateString(10);
								
								// Generate date and time Strings
								String date = Module.date();
								String time = Module.time();
								
								// Generate expire date and time Strings
								Calendar cal = Calendar.getInstance();
								// Add 7 or 1 day to the current date, depending on whether the post is private or not
								cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)+(cat > -1 ? 7 : 1));
								SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
								String fullTime = sdf.format(cal.getTime());
								String expireDate = fullTime.split(" ")[0];
								String expireTime = fullTime.split(" ")[1];
								
								// Create post
								String name = r.request().params().get("name");
								name = name != null && name.length() > 0 ? Str.truncate(name, 80) : "Untitled Post";
								String type = r.request().params().get("type");
								type = type != null ? Str.truncate(type, 8) : "text";
								
								// Provide data for next query
								cb.data()
									.put("name", name)
									.put("text", text)
									.put("type", type)
									.put("category", cat)
									.put("id", id)
									.put("date", date)
									.put("time", time)
									.put("expireDate", expireDate)
									.put("expireTime", expireTime);
								cb.next();
							} else {
								HttpUtils.apiError(r, "Insufficient rank");
							}
						} else {
							cb.fail(res.cause());
						}
					});
				} else {
					HttpUtils.apiError(r, "Posts may not be blank");
				}
			}, f -> {
				f.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}).then(cb -> {
				// Alias to callback data
				JsonObject cd = cb.data();
				SQL.createPost(
					cd.getString("name"),
					cd.getString("text"),
					cd.getString("type"),
					cd.getInteger("category"),
					cd.getString("id"),
					cd.getString("date"),
					cd.getString("time"),
					cd.getString("expireDate"),
					cd.getString("expireTime"),
					cd.getString("ip"), res -> {
					if(res.succeeded()) {
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.put("post_id", cd.getString("id"))
								.encode()
						);
						cb.end();
					} else {
						res.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
					}
				});
			}, f -> {
				
			}).execute();
		}
	}
}
