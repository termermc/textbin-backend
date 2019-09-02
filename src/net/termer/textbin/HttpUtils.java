package net.termer.textbin;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Utility class for dealing with HTTP requests/responses
 * @author termer
 * @since 1.0
 */
public class HttpUtils {
	// IP address regular expressions
	private static Pattern _ipv4 = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"); 
	private static Pattern _ipv6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))");
	
	/**
	 * Prepare the route for an API response
	 * @param r the RoutingContext
	 * @since 1.0
	 */
	public static void api(RoutingContext r) {
		r.response().putHeader("Content-Type", "application/json");
		r.response().putHeader("Access-Control-Allow-Origin",
				Module.config().frontend_host.equals("*") ? r.request().getHeader("Origin") : Module.config().frontend_host);
		r.response().putHeader("Access-Control-Allow-Credentials", "true");
	}
	/**
	 * Returns the IP address associated with the provided request, unhashed
	 * @param r the RoutingContext
	 * @return the IP address
	 * @since 2.0
	 */
	public static String ipClear(RoutingContext r) {
		return r.request().getHeader("X-Forwarded-For") == null ? r.request().remoteAddress().host() : r.request().getHeader("X-Forwarded-For");
	}
	
	/**
	 * Hashes the provided IP address (or any input, but should only be used for IP addresses)
	 * @param ip the IP address
	 * @return the hash IP address
	 * @throws UnsupportedEncodingException if Java cannot encode the String
	 * @throws NoSuchAlgorithmException if Java cannot find the hashing algorithm
	 * @since 2.0
	 */
	public static String hashIp(String ip) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		return hash(ip+Module.config().ip_hash_salt, Module.config().ip_hash_algorithm);
	}
	
	/**
	 * Returns the hash for the request's IP address
	 * @param r the RoutingContext
	 * @return the hashed IP address
	 * @throws NoSuchAlgorithmException if Java cannot find the hashing algorithm
	 * @throws UnsupportedEncodingException if Java cannot encode the String
	 * @since 1.0
	 */
	public static String ip(RoutingContext r) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		return hashIp(ipClear(r));
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
	
	/**
	 * Hashes the provided String with the specified algorithm
	 * @param content The String to hash
	 * @param algoirthm The algorithm to hash it with
	 * @return The hashed String
	 * @throws UnsupportedEncodingException If Java fails to encode the String
	 * @throws NoSuchAlgorithmException  If the specified hashing algorithm cannot be found
	 * @since 2.0
	 */
	public static String hash(String content, String algorithm) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] str = content.getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance(algorithm);
		byte[] hashed = md.digest(str);
		
		return new String(hashed, "UTF-8");
	}
	
	/**
	 * Hashes and salts the provided password
	 * @param password the password to hash
	 * @return the hashed and salted password
	 * @since 2.0
	 */
	public static String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}
	
	/**
	 * Verifies whether the specified password matches the provided hash
	 * @param password the password
	 * @param hash the hash to check the password against
	 * @return whether the password matches the hash
	 * @since 2.0
	 */
	public static boolean verifyPassword(String password, String hash) {
		return BCrypt.checkpw(password, hash);
	}
	
	/**
	 * Returns whether the provided IP address is a valid IPv4 or IPv6 address
	 * @param ip the IP address to validate
	 * @return whether the provided IP address is valid
	 * @since 2.0
	 */
	public static boolean validIp(String ip) {
		return _ipv4.matcher(ip).matches() || _ipv6.matcher(ip).matches();
	}
}
