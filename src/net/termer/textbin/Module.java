package net.termer.textbin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.termer.textbin.db.Database;
import net.termer.textbin.db.SQL;
import net.termer.textbin.routes.*;
import net.termer.twine.Events;
import net.termer.twine.Events.Type;
import net.termer.twine.ServerManager;
import net.termer.twine.Twine;
import net.termer.twine.modules.TwineModule;
import net.termer.twine.utils.Reader;
import net.termer.twine.utils.Writer;

public class Module implements TwineModule {
	private static TextBinConfig _cfg = new TextBinConfig();
	private static boolean _superInserted = false;
	private static HashMap<String, Integer> _guardedRoutes = null;
	
	public void initialize() {
		// Setup config
		Twine.logger().info("Setting up config...");
		try {
			configure();
		} catch(IOException e) {
			e.printStackTrace();
			Twine.logger().error("Failed to read/write TextBin config");
		}
		
		// Reload config on server config reload
		Events.on(Type.CONFIG_RELOAD, e -> {
			try {
				configure();
			} catch(IOException ex) {
				ex.printStackTrace();
				Twine.logger().error("Failed to read/write TextBin config");
			}
		});
		
		// Initialize database
		Twine.logger().info("Setting up database...");
		Database.init();
		
		Twine.logger().info("Inserting super admin entry...");
		Database.client().queryWithParams(
			"INSERT INTO accounts (id, account_username, account_hash, account_rank) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING",
			new JsonArray()
				.add(0)
				.add(_cfg.super_admin_username)
				.add(HttpUtils.hashPassword(_cfg.super_admin_password))
				.add(3),
			r -> {
				if(r.succeeded()) {
					_superInserted = true;
				} else {
					r.cause().printStackTrace();
					Twine.logger().error("Failed to insert super admin entry into database. Exiting...");
					Twine.shutdown();
				}
		});
		// Block thread until query is finished
		while(!_superInserted) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Twine.logger().info("Registering routes...");
		
		String dom = Twine.domains().byName(_cfg.domain).domain();
		
		// Session setup and API guard route
		ServerManager.handler(dom, r -> {
			if(!r.session().data().containsKey("rank")) {
				r.session().put("rank", 0);
			}
			if(!r.session().data().containsKey("account")) {
				r.session().put("account", -1);
			}
			
			if(_guardedRoutes.containsKey(r.request().path())) {
				int rankReq = _guardedRoutes.get(r.request().path());
				
				if(((int) r.session().get("rank")) >= rankReq) {
					// Sufficient rank, dispatch route handler
					r.next();
				} else {
					// Access denied
					HttpUtils.api(r);
					HttpUtils.apiError(r, "Access denied");
				}
			} else {
				// Dispatch route handler if no guard is present
				r.next();
			}
		});
		
		// Setup API Routes //
		
		// Returns the info of the user's session
		ServerManager.get("/api/v2/session_info", dom, new SessionInfoRoute());
		// Returns the posting status of the user
		ServerManager.get("/api/v2/post_status", dom, new PostStatusRoute());
		// Makes a post
		ServerManager.post("/api/v2/post", dom, new PostRoute());
		// Returns all public posts
		ServerManager.get("/api/v2/public_posts", dom, new PublicPostsRoute());
		// Returns a post's public data
		ServerManager.get("/api/v2/get_post", dom, new GetPostRoute());
		// Returns all comments for a post
		ServerManager.get("/api/v2/comments", dom, new CommentsRoute());
		// Returns a captcha image in base64 format
		ServerManager.get("/api/v2/captcha_image", dom, new CaptchaImageRoute());
		// Posts a comment
		ServerManager.post("/api/v2/post_reply", dom, new PostReplyRoute());
		// Returns the raw content of a post
		ServerManager.get("/api/v[1-2]/:type/:id", dom, new ViewRawRoute());
		// Returns all categories (or any for a specific rank)
		ServerManager.get("/api/v2/categories", dom, new CategoriesRoute());
		// Returns site statistics
		ServerManager.get("/api/v2/site_stats", dom, new SiteStatsRoute());
		// Returns the latest comments on public posts
		ServerManager.get("/api/v2/latest_comments", dom, new LatestCommentsRoute());
		// Returns the latest public posts
		ServerManager.get("/api/v2/latest_posts", dom, new LatestPostsRoute());
		// Authenticates a user
		ServerManager.post("/api/v2/auth", dom, new AuthRoute());
		// Returns all ban records
		ServerManager.get("/api/v2/bans", dom, new BansRoute());
		// Logs out the user's session
		ServerManager.post("/api/v2/logout", dom, new LogoutRoute());
		// Bans an IP address
		ServerManager.post("/api/v2/ban", dom, new BanRoute());
		// Revokes a ban
		ServerManager.post("/api/v2/revoke_ban", dom, new RevokeBanRoute());
		// Returns account info
		ServerManager.get("/api/v2/account", dom, new AccountRoute());
		// Updates account info (password and settings)
		ServerManager.post("/api/v2/update_account", dom, new UpdateAccountRoute());
		// Returns all bulletins
		ServerManager.get("/api/v2/bulletins", dom, new BulletinsRoute());
		// Creates a new bulletin
		ServerManager.post("/api/v2/create_bulletin", dom, new CreateBulletinRoute());
		// Deletes a bulletin
		ServerManager.post("/api/v2/delete_bulletin", dom, new DeleteBulletinRoute());
		// Sets whether a post is sticky
		ServerManager.post("/api/v2/post_sticky", dom, new PostStickyRoute());
		// Returns all staff accounts
		ServerManager.get("/api/v2/accounts", dom, new AccountsRoute());
		// Returns all ranks
		ServerManager.get("/api/v2/ranks", dom, new RanksRoute());
		// Deletes a user account
		ServerManager.post("/api/v2/delete_account", dom, new DeleteAccountRoute());
		// Creates a new account
		ServerManager.post("/api/v2/create_account", dom, new CreateAccountRoute());
		// Edits an account
		ServerManager.post("/api/v2/edit_account", dom, new EditAccountRoute());
		// Deletes a category
		ServerManager.post("/api/v2/delete_category", dom, new DeleteCategoryRoute());
		// Creates a new category
		ServerManager.post("/api/v2/create_category", dom, new CreateCategoryRoute());
		// Returns a category's info
		ServerManager.get("/api/v2/category", dom, new CategoryRoute());
		// Edits a category
		ServerManager.post("/api/v2/edit_category", dom, new EditCategoryRoute());
		
		// Create deletion daemon
		Twine.logger().info("Starting post deletion daemon...");
		new Thread(() -> {
			while(true) {
				// Poll posts for their expiration times
				SQL.postExpireDatesTimes(r -> {
					if(r.succeeded()) {
						Calendar curDate = Calendar.getInstance();
						
						for(JsonObject post : r.result().getRows()) {
							// Check if post is sticky
							if(post.getInteger("post_sticky") < 1) {
								try {
									// Parse integers out of date and time Strings
									int pMinute = Integer.parseInt(post.getString("post_expire_time").split(":")[1]);
									int pHour = Integer.parseInt(post.getString("post_expire_time").split(":")[0]);
									int pYear = Integer.parseInt(post.getString("post_expire_date").split("/")[2]);
									int pMonth = Integer.parseInt(post.getString("post_expire_date").split("/")[0]);
									int pDay = Integer.parseInt(post.getString("post_expire_date").split("/")[1]);
									
									Calendar pCal = Calendar.getInstance();
									pCal.clear();
									pCal.set(pYear, pMonth-1, pDay, pHour, pMinute, 0);
									
									// Check if post is after its deletion time; delete if so
									if(curDate.after(pCal)) {
										// Delete post
										SQL.deletePost(post.getString("post_id"), res -> {
											if(res.succeeded()) {
												Twine.logger().info("Deleted post ID "+post.getString("post_id")+" since it's past its expiration date");
											} else {
												Twine.logger().error("Error deleting post:");
												res.cause().printStackTrace();
											}
										});
										// Delete comments
										SQL.deleteComments(post.getString("post_id"), res -> {
											if(res.succeeded()) {
												Twine.logger().info("Deleted all comments for post ID "+post.getString("post_id")+" since it's past its expiration date");
											} else {
												Twine.logger().error("Error deleting comments:");
												res.cause().printStackTrace();
											}
										});
									}
								} catch(Exception e) {
									Twine.logger().error("Error evaluating post "+post.getString("post_id")+" for deletion");
									e.printStackTrace();
								}
							}
						}
					} else {
						Twine.logger().error("Error fetching all posts in post deletion daemon:");
						r.cause().printStackTrace();
					}
				});
				
				// Sleep for 30 minutes
				try {
					Thread.sleep(1000*60*30);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		Twine.logger().info("TextBin initialized successfully!");
	}
	
	private void configure() throws DecodeException, IOException {
		_guardedRoutes = new HashMap<String, Integer>();
		
		File conf = new File("configs/textbin.json");
		if(conf.exists()) {
			_cfg = Json.decodeValue(Reader.read(conf), TextBinConfig.class);
		} else {
			Writer.write("configs/textbin.json", Json.encodePrettily(_cfg));
		}
		
		// Define route guards
		_guardedRoutes.put("/api/v2/site_stats", _cfg.site_stat_rank_required);
		_guardedRoutes.put("/api/v2/bans", 1);
		_guardedRoutes.put("/api/v2/ban", 1);
		_guardedRoutes.put("/api/v2/revoke_ban", 1);
		_guardedRoutes.put("/api/v2/create_bulletin", 2);
		_guardedRoutes.put("/api/v2/delete_bulletin", 2);
		_guardedRoutes.put("/api/v2/post_sticky", 1);
		_guardedRoutes.put("/api/v2/accounts", 2);
		_guardedRoutes.put("/api/v2/delete_account", 2);
		_guardedRoutes.put("/api/v2/create_account", 2);
		_guardedRoutes.put("/api/v2/edit_account", 2);
		_guardedRoutes.put("/api/v2/delete_category", 2);
		_guardedRoutes.put("/api/v2/edit_category", 2);
		_guardedRoutes.put("/api/v2/create_category", 2);
	}
	
	/**
	 * Returns the JSON config
	 * @return the config
	 * @since 1.0
	 */
	public static TextBinConfig config() {
		return _cfg;
	}
	
	/**
	 * Returns a MM/DD/YYYY format date
	 * @return a MM/DD/YYYY format date
	 * @since 2.0
	 */
	public static String date() {
		String dateStr = "MM/dd/yyyy";
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateStr);
		return dateFormat.format(new Date());
	}
	
	/**
	 * Returns a HH:MM format time
	 * @return a HH:MM format time
	 * @since 2.0
	 */
	public static String time() {
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
		// Terminate database connection
		Database.close();
	}
	
	public String twineVersion() {
		return "1.0-alpha+";
	}
}
