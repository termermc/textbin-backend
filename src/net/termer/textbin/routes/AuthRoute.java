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

public class AuthRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Validate input
		String username = r.request().params().get("username");
		String password = r.request().params().get("password");
		
		// Setup callback chain
		if(username == null || password == null) {
			HttpUtils.apiError(r, "invalid_creds");
		} else {
			try {
				// Setup callback chain
				Callback callback = new Callback();
				callback.data().put("username", r.request().params().get("username"));
				callback.data().put("password", r.request().params().get("password"));
				callback.data().put("ip", HttpUtils.ip(r));
				
				callback.then(cb -> {
					SQL.getBan(cb.data().getString("ip"), res -> {
						if(res.succeeded()) {
							if(res.result().getNumRows() > 0) {
								HttpUtils.apiError(r, "banned");
								cb.end();
							} else {
								cb.next();
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
					SQL.getAccount(cb.data().getString("username"), res -> {
						if(res.succeeded()) {
							// Check if account exists
							if(res.result().getRows().size() > 0) {
								// Aggregate info
								int accId = res.result().getRows().get(0).getInteger("id");
								String accName = res.result().getRows().get(0).getString("account_username");
								String accHash = res.result().getRows().get(0).getString("account_hash");
								int accRank = res.result().getRows().get(0).getInteger("account_rank");
								boolean accRecordLogins = res.result().getRows().get(0).getInteger("account_record_logins") > 0;
								
								// Pass data to next callback
								cb.data()
									.put("accId", accId)
									.put("accName", accName)
									.put("accHash", accHash)
									.put("accRank", accRank)
									.put("accRecordLogins", accRecordLogins);
								cb.next();
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
					if(cb.data().getString("username").equals(cb.data().getString("accName"))) {
						if(HttpUtils.verifyPassword(cb.data().getString("password"), cb.data().getString("accHash"))) {
							// Update session
							r.session().put("username", cb.data().getString("username"));
							r.session().put("rank", cb.data().getInteger("accRank"));
							r.session().put("account", cb.data().getInteger("accId"));
							
							// Send response
							r.response().end(
								new JsonObject()
									.put("status", "success")
									.put("username", cb.data().getString("username"))
									.put("rank", cb.data().getInteger("accRank"))
									.encode()
							);
							cb.next();
						} else {
							HttpUtils.apiError(r, "invalid_creds");
						}
					} else {
						HttpUtils.apiError(r, "invalid_creds");
					}
				}, f -> {}).then(cb -> {
					if(cb.data().getBoolean("accRecordLogins")) {
						SQL.createLoginRecord(cb.data().getInteger("accId"), HttpUtils.ipClear(r), res -> {
							if(res.succeeded()) {
								// Inserted login record
							} else {
								cb.fail(res.cause());
							}
						});
					}
				}, f -> {
					Twine.logger().error("Failed to create login record for account ID "+f.data().getInteger("accId"));
					f.cause().printStackTrace();
				}).execute();
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				HttpUtils.apiError(r, "Internal error");
				e.printStackTrace();
			}
		}
	}
}
