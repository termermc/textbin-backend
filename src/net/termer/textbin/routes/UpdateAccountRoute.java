package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.utils.Callback;

public class UpdateAccountRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		if(((int) r.session().get("account")) > -1) {
			// Welcome to definition-and-logic-in-one-line
			int account = (int) r.session().get("account");
			String curPass = r.request().params().get("current_password");
			String newPass = r.request().params().get("new_password");
			String confPass = r.request().params().get("confirm_password");
			
			// Check if changing logging setting
			if(r.request().params().get("record_logins") == null) {
				// Check if passwords match
				if(newPass.equals(confPass)) {
					// Setup callback chain
					new Callback().then(cb -> {
						SQL.getAccount(account, res -> {
							if(res.succeeded()) {
								// Pass data to next callback
								if(res.result().getNumRows() > 0) {
									cb.data().put("hash", res.result().getRows().get(0).getString("account_hash"));
									cb.next();
								} else {
									HttpUtils.apiError(r, "Account not found");
									// Reset account session var
									r.session().put("account", -1);
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
						// Check current password to confirm action
						if(HttpUtils.verifyPassword(curPass, cb.data().getString("hash"))) {
							// Apply password change
							SQL.updateAccountPassword(account, HttpUtils.hashPassword(newPass), res -> {
								if(res.succeeded()) {
									// Send success response
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
							HttpUtils.apiError(r, "Provided password does not match current password");
							cb.end();
						}
					}, f -> {
						f.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
						f.end();
					}).execute();
				} else {
					HttpUtils.apiError(r, "Passwords do not match");
				}
			} else {
				boolean recordLogins = r.request().params().get("record_logins").equals("true");
				
				// Update setting in database
				SQL.updateAccountSettings(account, recordLogins, res -> {
					if(res.succeeded()) {
						// Success
						r.response().end(
							new JsonObject()
								.put("status", "success")
								.encode()
						);
					} else {
						res.cause().printStackTrace();
						HttpUtils.apiError(r, "Database error");
					}
				});
			}
		} else {
			HttpUtils.apiError(r, "Not logged in");
		}
	}
}
