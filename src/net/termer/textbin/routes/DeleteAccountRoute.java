package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.utils.Callback;

public class DeleteAccountRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			int id = Integer.parseInt(r.request().params().get("id"));
			
			// Check if session account matches param
			if(((int) r.session().get("account")) == id) {
				HttpUtils.apiError(r, "You cannot delete your own account");
			} else {
				// Setup callback chain
				new Callback().then(cb -> {
					SQL.getAccount(id, res -> {
						if(res.succeeded()) {
							if(res.result().getNumRows() > 0) {
								// Check account rank
								int rank = res.result().getRows().get(0).getInteger("account_rank");
								
								if(rank < (int) r.session().get("rank")) {
									// Continue
									cb.next();
								} else {
									// End
									HttpUtils.apiError(r, "You may not delete higher ranked accounts");
									cb.end();
								}
							} else {
								HttpUtils.apiError(r, "Invalid account");
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
					SQL.deleteAccount(id, res -> {
						if(res.succeeded()) {
							// Proceed to next callback
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
					SQL.deleteLoginRecords(id, res -> {
						if(res.succeeded()) {
							// Send success status
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
			HttpUtils.apiError(r, "No account ID provided or invalid ID");
		}
	}
}
