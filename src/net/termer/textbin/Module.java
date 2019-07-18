package net.termer.textbin;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.github.cage.Cage;
import com.github.cage.GCage;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Session;
import net.termer.textbin.db.Database;
import net.termer.textbin.db.SQL;
import net.termer.twine.ServerManager;
import net.termer.twine.Twine;
import net.termer.twine.modules.TwineModule;
import net.termer.twine.utils.Reader;
import net.termer.twine.utils.StringFilter;
import net.termer.twine.utils.Writer;

public class Module implements TwineModule {
	private static TextBinConfig _cfg = new TextBinConfig();
	
	public void initialize() {
		// Create config
		Twine.logger().info("Setting up config...");
		File conf = new File("configs/textbin.json");
		try {
			if(conf.exists()) {
				_cfg = Json.decodeValue(Reader.read(conf), TextBinConfig.class);
			} else {
				Writer.write("configs/textbin.json", Json.encodePrettily(_cfg));
			}
		} catch(IOException e) {
			Twine.logger().error("Failed to read/write TextBin config");
		}
		
		// Initialize database
		Twine.logger().info("Setting up database...");
		Database.init();
		
		Twine.logger().info("Registering routes...");
		
		// Setup API Routes //
		// Returns the post limit of the IP address
		String dom = Twine.domains().byName(_cfg.domain).domain();
		ServerManager.get("/api/v1/post_limit", dom, r -> {
			HttpUtils.api(r);
			
			String ip = null;
			try {
				ip = HttpUtils.ip(r);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				Twine.logger().error("Failed to hash IP address");
				e.printStackTrace();
				HttpUtils.apiError(r, "Internal error");
			}
			if(ip != null) {
				SQL.postLimit(ip, res -> {
					if(res.succeeded()) {
						int cnt = res.result().getRows().get(0).getInteger("count");
						int rem = Math.max(0, _cfg.max_posts-cnt);
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.put("remaining", rem)
								.encode()
						);
					} else {
						res.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
					}
				});
			}
		});
		// Makes a post
		ServerManager.post("/api/v1/post", dom, r -> {
			HttpUtils.api(r);
			// Check post limit
			try {
				String ip = HttpUtils.ip(r);
				SQL.postLimit(ip, res -> {
					if(res.succeeded()) {
						int rem = Math.max(0, _cfg.max_posts-res.result().getRows().get(0).getInteger("count"));
						
						// Only allow if remaining posts > 0
						if(rem > 0) {
							// Read params
							String name = r.request().params().get("name");
							name = name != null && name.length() > 0 ? truncate(name, 80) : "Untitled Post";
							String text = r.request().params().get("text");
							String type = r.request().params().get("type");
							type = type != null ? truncate(type, 4) : "text";
							String visibility = r.request().params().get("visibility");
							visibility = visibility != null && visibility.equals("private") ? visibility : "public";
							
							// Check if post is empty or unset
							if(text != null && text.length() > 0) {
								// Generate ID
								String id = StringFilter.generateString(10);
								
								//Generate date and time Strings
								String date = date();
								String time = time();
								
								// Create post
								SQL.createPost(name, text, type, visibility.equals("public"), id, date, time, ip, postRes -> {
									if(postRes.succeeded()) {
										r.response().end(
											new JsonObject()
												.put("status", "success")
												.put("post_id", id)
												.encode()
										);
									} else {
										postRes.cause().printStackTrace();
										HttpUtils.apiError(r, "Database error");
									}
								});
							} else {
								HttpUtils.apiError(r, "Posts may not be blank");
							}
						} else {
							HttpUtils.apiError(r, "Wait for a post to expire before posting again");
						}
					} else {
						res.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
					}
				});
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				e.printStackTrace();
				HttpUtils.apiError(r, "Internal error");
			}
		});
		// Returns all public posts
		ServerManager.get("/api/v1/public_posts", dom, r -> {
			HttpUtils.api(r);
			
			// Query for public posts
			SQL.publicPosts(res -> {
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
		});
		// Returns a post's public data
		ServerManager.get("/api/v1/get_post", dom, r -> {
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
					HttpUtils.apiError(r, "Database error");
				}
			});
		});
		// Returns all comments for a post
		ServerManager.get("/api/v1/comments", dom, r -> {
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
								post.putNull("ip");
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
		});
		// Returns a captcha image in base64 format
		ServerManager.get("/api/v1/captcha_image", dom, r -> {
			HttpUtils.api(r);
			
			// Generate a new captcha String and set session
			String captcha = StringFilter.generateString(8);
			r.session().put("captcha", captcha);
			
			// Generate image
			Cage cage = new GCage();
			BufferedImage img = cage.drawImage(captcha);
			
			// Create image data
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ImageIO.write(img, "jpeg", baos);
				
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("base64", DatatypeConverter.printBase64Binary(baos.toByteArray()))
						.encode()
				);
			} catch (IOException e) {
				e.printStackTrace();
				HttpUtils.apiError(r, "Error generating image");
			}
		});
		// Posts a comment
		ServerManager.post("/api/v1/post_reply", dom, r -> {
			HttpUtils.api(r);
			Session sess = r.session();
			
			String captcha = r.request().params().get("captcha");
			captcha = captcha == null ? "invalid" : captcha;
			
			// Check captcha
			if(captcha.equalsIgnoreCase(sess.get("captcha"))) {
				String postId = r.request().params().get("post_id");
				// Check if post ID is valid
				if(postId != null && postId.length() > 9) {
					// Query to check if post ID exists
					SQL.postExists(postId, res -> {
						if(res.succeeded()) {
							if(res.result().getRows().get(0).getInteger("count") > 0) {
								// Collect params
								String comment = r.request().params().get("comment");
								String name = r.request().params().get("name");
								name = name != null && name.trim().length() > 0 ? truncate(name.trim(), 25) : "Anonymous";
								
								if(comment != null && comment.length() > 0) {
									comment = truncate(comment, 1000);
									// Get date and time
									String date = date();
									String time = time();
									
									try {
										// Hash IP
										String ip = HttpUtils.ip(r);
										
										SQL.postComment(name, comment, date, time, postId, ip, postRes -> {
											if(postRes.succeeded()) {
												// Delete captcha session variable to stop re-use
												sess.put("captcha", null);
												
												r.response().end(
													new JsonObject()
														.put("status", "success")
														.encode()
												);
											} else {
												postRes.cause().printStackTrace();
												HttpUtils.apiError(r, "Database error");
											}
										});
									} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
										e.printStackTrace();
										HttpUtils.apiError(r, "Internal error");
									}
								} else {
									HttpUtils.apiError(r, "Comments may not be blank");
								}
							} else {
								HttpUtils.apiError(r, "Invalid post ID");
							}
						} else {
							res.cause().printStackTrace();
							HttpUtils.apiError(r, "Database error");
						}
					});
				} else {
					HttpUtils.apiError(r, "Invalid post ID");
				}
			} else {
				HttpUtils.apiError(r, "invalid_captcha");
			}
		});
		// Returns the raw content of a post
		ServerManager.get("/api/v1/:type/:id", dom, r -> {
			String type = r.pathParam("type");
			String postId = r.pathParam("id");
			
			SQL.postContent(postId, res -> {
				if(res.succeeded()) {
					if(res.result().getNumRows() > 0) {
						r.response().putHeader("Content-Type", type.equals("html") ? "text/html" : "text/plain;charset=UTF-8");
						r.response().end(res.result().getRows().get(0).getString("text"));
					} else {
						r.response().end("Invalid post ID");
					}
				} else {
					res.cause().printStackTrace();
					r.response().end("Database error");
				}
			});
		});
		
		// Create deletion daemon
		Twine.logger().info("Starting post deletion daemon...");
		new Thread() {
			public void run() {
				while(true) {
					// Delete posts that are 24 hours old
					SQL.postDatesTimes(r -> {
						if(r.succeeded()) {
							Calendar cal = Calendar.getInstance();
							int hour = cal.get(Calendar.HOUR_OF_DAY);
							int month = cal.get(Calendar.MONTH);
							int day = cal.get(Calendar.DAY_OF_MONTH);
							
							for(JsonObject post : r.result().getRows()) {
								int pHour = Integer.parseInt(post.getString("time").split(":")[0]);
								int pMonth = Integer.parseInt(post.getString("date").split("/")[0]);
								int pDay = Integer.parseInt(post.getString("date").split("/")[1]);
								
								// Whether to delete
								boolean delete = false;
								
								if(month > pMonth) {
									day+=30;
								}
								if(day > pDay) {
									hour+=24;
								}
								if(hour-pHour >= 24) {
									delete = true;
								}
								if(delete) {
									// Delete post
									SQL.deletePost(post.getString("post_id"), res -> {
										if(res.succeeded()) {
											Twine.logger().info("Deleted post ID "+post.getString("post_id")+" since it's over 24 hours old");
										} else {
											Twine.logger().error("Error deleting post:");
											res.cause().printStackTrace();
										}
									});
									// Delete comments
									SQL.deleteComments(post.getString("post_id"), res -> {
										if(res.succeeded()) {
											Twine.logger().info("Deleted all comments for post ID "+post.getString("post_id")+" since it's over 24 hours old");
										} else {
											Twine.logger().error("Error deleting comments:");
											res.cause().printStackTrace();
										}
									});
								}
							}
						} else {
							Twine.logger().error("Error fetching all posts in post deletion daemon:");
							r.cause().printStackTrace();
						}
					});
					
					// Sleep for an hour
					try {
						Thread.sleep(1000*60*60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		Twine.logger().info("TextBin initialized successfully!");
	}
	
	
	/**
	 * Returns the JSON config
	 * @return the config
	 * @since 1.0
	 */
	public static TextBinConfig config() {
		return _cfg;
	}
	// Truncates the provided String
	private String truncate(String str, int len) {
		return str.substring(0, Math.min(len, str.length()));
	}
	// Returns a MM/DD/YYYY format date
	private String date() {
		String dateStr = "MM/dd/yyyy";
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateStr);
		return dateFormat.format(new Date());
	}
	// Returns a HH:MM format time
	private String time() {
		String timeStr = "HH:mm";
		SimpleDateFormat timeFormat = new SimpleDateFormat(timeStr);
		return timeFormat.format(new Date());
	}
	
	public String name() {
		return "TextBin";
	}
	
	public Priority priority() {
		return Priority.LOW;
	}
	
	public void shutdown() {
		Database.close();
	}
	
	public String twineVersion() {
		return "1.0-alpha+";
	}
}
