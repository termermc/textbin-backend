package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class AccountsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		SQL.getAccounts(res -> {
			if(res.succeeded()) {
				JsonArray accounts = new JsonArray();
				
				for(JsonObject account : res.result().getRows()) {
					accounts.add(account);
				}
				
				// Send result
				r.response().end(
					new JsonObject()
						.put("status", "success")
						.put("accounts", accounts)
						.encode()
				);
			} else {
				res.cause().printStackTrace();
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
