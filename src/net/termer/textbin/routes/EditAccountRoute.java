package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.utils.Callback;

public class EditAccountRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			// Collect params
			int id = Integer.parseInt(r.request().params().get("id"));
			int rank = Integer.parseInt(r.request().params().get("rank"));
			
			// Check if rank is below editor rank + if rank ID is valid
			if(rank >= (int) r.session().get("rank")) {
				HttpUtils.apiError(r, "Rank equal or above your rank");
			} else if(rank < 1) {
				HttpUtils.apiError(r, "Rank must be at least 1");
			} else {
				// Setup callback chain
				new Callback().then(cb -> {
					// Fetch account
					SQL.getAccount(id, res -> {
						if(res.succeeded()) {
							if(res.result().getNumRows() > 0) {
								// How's that for a run-on sentence?
								if(res.result().getRows().get(0).getInteger("account_rank") < (int) r.session().get("rank")) {
									cb.next();
								} else {
									HttpUtils.apiError(r, "Cannot edit account of a higher rank");
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
					SQL.updateAccountRank(id, rank, res -> {
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
			HttpUtils.apiError(r, "Account ID and rank must be integers");
		}
	}
}
