package net.termer.textbin.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

/**
 * Utility class to compose SQL queries
 * @author termer
 * @since 1.0
 */
public class SQL {
	/**
	 * Queries for the post limit of the provided IP hash
	 * @param ipHash the hashed IP address to lookup
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void postLimit(String ipHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT COUNT(*) FROM posts WHERE ip = ?",
			new JsonArray().add(ipHash),
			hdlr
		);
	}
	
	/**
	 * Inserts a new post
	 * @param name the name of the post
	 * @param text the post content
	 * @param type the post type ("text" or "html")
	 * @param visible whether the post is public
	 * @param id the post_id
	 * @param dateStr the date, in MM/DD/YYYY format
	 * @param timeStr the time, in HH:MM format
	 * @param ipHash the hashed IP of the poster
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void createPost(String name, String text, String type, boolean visible, String id, String dateStr, String timeStr, String ipHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO posts (post_id, name, date, time, type, ip, public, text) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
			new JsonArray()
				.add(id)
				.add(name)
				.add(dateStr)
				.add(timeStr)
				.add(type)
				.add(ipHash)
				.add(visible ? 1 : 0)
				.add(text),
			hdlr
		);
	}
	
	/**
	 * Queries for all public posts
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void publicPosts(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT name, date, time, post_id FROM posts WHERE public = 1 ORDER BY id DESC",
			hdlr
		);
	}
	
	/**
	 * Queries for the specified post ID's public data
	 * @param postId the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void viewPost(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT name, type, text, date, time FROM posts WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Queries for all comments on the specified post ID
	 * @param postId the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void getComments(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT name, id, ip, text, date, time FROM comments WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Inserts a new comment
	 * @param name the commenter's name
	 * @param text the comment text
	 * @param date the date
	 * @param time the time
	 * @param postId the ID of the post this comment is on
	 * @param ipHash the commenter's hashed IP
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void postComment(String name, String text, String date, String time, String postId, String ipHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO comments (name, date, time, text, post_id, ip) values (?, ?, ?, ?, ?, ?)",
			new JsonArray()
				.add(name)
				.add(date)
				.add(time)
				.add(text)
				.add(postId)
				.add(ipHash),
			hdlr
		);
	}
	
	/**
	 * Returns a table counting all the posts with the provided ID (should be 1 if exists, 0 if not)
	 * @param postId the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void postExists(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT COUNT(*) FROM posts WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Returns the content of a post
	 * @param postId the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void postContent(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT text FROM posts WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Returns the dates and times of all posts
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void postDatesTimes(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query("SELECT date, time, post_id FROM posts", hdlr);
	}
	
	/**
	 * Deletes a post
	 * @param postID the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void deletePost(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM posts WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	public static void deleteComments(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM comments WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
}
