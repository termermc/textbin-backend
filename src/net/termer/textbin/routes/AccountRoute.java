package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.utils.Callback;

public class AccountRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		if(((int) r.session().get("account")) > -1) {
			// Setup callback chain
			new Callback().then(cb -> {
				SQL.getAccountInfo((int) r.session().get("account"), res -> {
					if(res.succeeded()) {
						if(res.result().getNumRows() < 1) {
							HttpUtils.apiError(r, "Account not found");
							// Reset account session var
							r.session().put("account", -1);
							cb.end();
						} else {
							// Pass data to next callback
							JsonObject resp = res.result().getRows().get(0);
							cb.data()
								.put("id", resp.getInteger("id"))
								.put("username", resp.getString("username"))
								.put("rank", resp.getInteger("rank"))
								.put("record_logins", resp.getInteger("record_logins") > 0)
								.put("rank_name", resp.getString("rank_name"))
								.put("rank_flare", resp.getString("rank_flare"));
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
				SQL.getAccountLogins((int) r.session().get("account"), res -> {
					if(res.succeeded()) {
						// Grab login records
						JsonArray logins = new JsonArray();
						for(JsonObject login : res.result().getRows())
							logins.add(login);
						
						// Compose response
						r.response().end(
							cb.data()
								.put("logins", logins)
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
		} else {
			HttpUtils.apiError(r, "Not logged in");
		}
	}
}
