package net.termer.textbin.routes;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.github.cage.Cage;
import com.github.cage.GCage;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import net.termer.textbin.HttpUtils;
import net.termer.twine.ServerManager;
import net.termer.twine.utils.StringFilter;

public class CaptchaImageRoute implements Handler<RoutingContext> {
	public void handle(RoutingContext r) {
		HttpUtils.api(r);
		
		// Generate a new captcha String and set session
		String captcha = StringFilter.generateString(8);
		r.session().put("captcha", captcha);
		
		// Generate image
		Cage cage = new GCage();
		BufferedImage img = cage.drawImage(captcha);
		
		// Create image data
		ServerManager.vertx().executeBlocking(f -> {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpeg", baos);
				
				f.complete(
					new JsonObject()
						.put("status", "success")
						.put("base64", DatatypeConverter.printBase64Binary(baos.toByteArray()))
						.encode()
				);
			} catch (IOException e) {
				f.tryFail(e);
			}
		}, imgres -> {
			if(imgres.succeeded()) {
				r.response().end((String) imgres.result());
			} else {
				imgres.cause().printStackTrace();
				HttpUtils.apiError(r, "Error generating image");
			}
		});
	}
}
