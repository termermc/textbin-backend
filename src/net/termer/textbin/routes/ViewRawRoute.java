package net.termer.textbin.routes;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.db.SQL;
import net.termer.twine.ServerManager;

public class ViewRawRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		String type = r.pathParam("type");
		String postId = r.pathParam("id");
		
		SQL.postContent(postId, res -> {
			if(res.succeeded()) {
				if(res.result().getNumRows() > 0) {
					if(type.equals("markdown")) {
						r.response().putHeader("Content-Type", "text/html");
						// Parse markdown in worker to avoid blocking the event loop
						ServerManager.vertx().executeBlocking(f -> {
							Parser parser = Parser.builder().build();
							Node doc = parser.parse(res.result().getRows().get(0).getString("text"));
							HtmlRenderer htmlren = HtmlRenderer.builder().build();
							f.complete(htmlren.render(doc));
						}, mdres -> {
							r.response().end(
								"<!DOCTYPE html>"
							  + "<html>"
							  + "<head>"
							  + "<title>"+res.result().getRows().get(0).getString("name")+"</title>"
							  + "</head>"
							  + "<body>"
							  + mdres.result()
							  + "</body>"
							  + "</html>"
							);
						});
						
					} else {
						r.response().putHeader("Content-Type", type.equals("html") ? "text/html" : "text/plain;charset=UTF-8");
						r.response().end(res.result().getRows().get(0).getString("text"));
					}
				} else {
					r.response().end("Invalid post ID");
				}
			} else {
				res.cause().printStackTrace();
				r.response().end("Database error");
			}
		});
	}
}
