package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.utils.Callback;

public class CreateAccountRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		String username = r.request().params().get("username");
		String password = r.request().params().get("password");
		String rankStr = r.request().params().get("rank");
		
		if(username == null || password == null || rankStr == null) {
			// Null value(s)
			HttpUtils.apiError(r, "Must provide username, password, and rank ID");
		} else {
			// Convert rankStr to an integer
			try {
				int rank = Integer.parseInt(rankStr);
				
				// Check if rank is below creator rank + check if valid rank ID
				if(rank >= (int) r.session().get("rank")) {
					HttpUtils.apiError(r, "Rank equal or above your rank");
				} else if(rank < 1) {
					HttpUtils.apiError(r, "Rank must be at least 1");
				} else {
					// Setup callback chain
					new Callback().then(cb -> {
						// Check if account with same username exists
						SQL.getAccount(username, res -> {
							if(res.succeeded()) {
								if(res.result().getNumRows() > 0) {
									// Account already exists
									HttpUtils.apiError(r, "Account with same username already exists");
									cb.end();
								} else {
									// Continue to next callback
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
						// Create new account
						SQL.createAccount(username, HttpUtils.hashPassword(password), rank, res -> {
							if(res.succeeded()) {
								// Succeeded
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
				}
			} catch(NumberFormatException e) {
				HttpUtils.apiError(r, "Rank must be an integer");
			}
		}
	}
}
