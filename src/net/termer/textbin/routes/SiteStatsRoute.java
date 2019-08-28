package net.termer.textbin.routes;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;

public class SiteStatsRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		SQL.siteStats(res -> {
			if(res.succeeded()) {
				// Write response
				r.response().end(
					res.result().getRows().get(0)
						.put("status", "success")
						.encode()
				);
			} else {
				HttpUtils.apiError(r, "Database error");
			}
		});
	}
}
