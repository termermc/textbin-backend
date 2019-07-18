package net.termer.textbin;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Utility class for dealing with HTTP requests/responses
 * @author termer
 * @since 1.0
 */
public class HttpUtils {
	/**
	 * Prepare the route for an API response
	 * @param r the RoutingContext
	 * @since 1.0
	 */
	public static void api(RoutingContext r) {
		r.response().putHeader("Content-Type", "application/json");
		r.response().putHeader("Access-Control-Allow-Origin", r.request().getHeader("origin"));
		r.response().putHeader("Access-Control-Allow-Credentials", "true");
	}
	
	/**
	 * Returns the MD5 hash for the request's IP address
	 * @param r the RoutingContext
	 * @return the hashed IP address
	 * @throws NoSuchAlgorithmException if Java cannot find the MD5 algorithm
	 * @throws UnsupportedEncodingException if Java cannot encode the String
	 * @since 1.0
	 */
	public static String ip(RoutingContext r) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] ip = r.request().remoteAddress().host().getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hashed = md.digest(ip);
		
		return new String(hashed, "UTF-8");
	}
	
	/**
	 * Ends the provided RouterContext's response with a JSON error response
	 * @param r the RoutingContext
	 * @param err the error to return
	 * @since 1.0
	 */
	public static void apiError(RoutingContext r, String err) {
		r.response().end(
			new JsonObject()
				.put("status", "error")
				.put("error", err)
				.encode()
		);
	}
}
