package net.termer.textbin.routes;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.textbin.db.SQL;
import net.termer.twine.Twine;

public class SessionInfoRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		try {
			SQL.getBan(HttpUtils.ip(r), res -> {
				if(res.succeeded()) {
					r.response().end(
						new JsonObject()
							.put("status", "success")
							.put("rank", (int) r.session().get("rank"))
							.put("banned", res.result().getNumRows() > 0)
							.put("username", (String) r.session().get("username"))
							.encode()
					);
				} else {
					HttpUtils.apiError(r, "Database error");
				}
			});
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			Twine.logger().error("Failed to hash IP address");
			e.printStackTrace();
			HttpUtils.apiError(r, "Internal error");
		}
	}
}