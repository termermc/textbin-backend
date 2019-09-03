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
			"SELECT COUNT(*) FROM posts WHERE post_ip = ?",
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
	 * @param dateStr the date, in MM/dd/yyyy format
	 * @param timeStr the time, in HH:mm format
	 * @param expireDateStr the expiration date, in MM/dd/yyyy format
	 * @param expireTimeStr the expiration time, in HH:mm format
	 * @param ipHash the hashed IP of the poster
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void createPost(String name, String text, String type, int category, String id, String dateStr, String timeStr, String expireDateStr, String expireTimeStr, String ipHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO posts (\n" + 
			"	post_id,\n" + 
			"	post_name,\n" + 
			"	post_date,\n" + 
			"	post_time,\n" + 
			"	post_expire_date,\n" + 
			"	post_expire_time,\n" + 
			"	post_type,\n" + 
			"	post_ip,\n" + 
			"	post_category,\n" + 
			"	post_text,\n" + 
			"	post_bump\n" + 
			") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_TIMESTAMP(?, 'MM/DD/YYYY HH24:MI'))",
			new JsonArray()
				.add(id)
				.add(name)
				.add(dateStr)
				.add(timeStr)
				.add(expireDateStr)
				.add(expireTimeStr)
				.add(type)
				.add(ipHash)
				.add(category)
				.add(text)
				.add(dateStr+' '+timeStr),
			hdlr
		);
	}
	
	/**
	 * Queries for all public posts
	 * @param categoryCode the post category to search (null for all)
	 * @param limit the amount of posts to return. Specify 0 to return all posts
	 * @param offset the offset of posts retrieved
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void publicPosts(String categoryCode, int limit, int offset, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT\n" + 
			"	post_name AS name,\n" + 
			"	post_date AS date,\n" + 
			"	post_time AS time,\n" + 
			"	post_category AS category,\n" + 
			"   post_sticky AS sticky,\n" + 
			"	post_id,\n" + 
			"	category_code,\n" + 
			"	category_name,\n" + 
			"	(\n" + 
			"		SELECT COUNT(*)\n" + 
			"		FROM comments\n" + 
			"		WHERE post_id = posts.post_id\n" + 
			"	) AS comment_count\n" + 
			"FROM posts\n" + 
			"JOIN categories ON posts.post_category = categories.id\n" + 
			"WHERE post_category > -1 AND category_code "+(categoryCode==null?"!= ? AND post_sticky < 1":"= ?")+"\n" +
			"ORDER BY "+(categoryCode==null?"":"post_sticky DESC, ")+"post_bump DESC" + 
			(limit > 0 ? "\nLIMIT "+limit : "") + 
			(offset > 0 ? "\nOFFSET "+offset : ""),
			new JsonArray().add(categoryCode == null ? "" : categoryCode),
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
			"SELECT\n" + 
			"	post_name AS name,\n" + 
			"	post_type AS type,\n" + 
			"	post_text AS text,\n" + 
			"	post_date AS date,\n" + 
			"	post_time AS time,\n" + 
			"   post_sticky AS sticky,\n" + 
			"	post_category AS category,\n" + 
			"	category_name,\n" + 
			"   category_code\n" +
			"FROM posts\n" + 
			"JOIN categories\n" + 
			"ON categories.id = posts.post_category \n" + 
			"WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Queries for all comments on the specified post ID
	 * @param postId the post ID
	 * @param limit the amount of comments to return
	 * @param offset the offset of comments to return
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void getComments(String postId, int limit, int offset, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT comment_name AS name,\n" + 
			"       comments.id,\n" + 
			"		comment_ip AS ip,\n" + 
			"		comment_text AS text,\n" + 
			"		comment_date AS date,\n" + 
			"		comment_time AS time,\n" + 
			"		comment_poster_rank AS poster_rank,\n" + 
			"		comment_poster_ban AS ban_text,\n" + 
			"       comment_email AS email,\n" + 
			"       comment_trip AS trip,\n" + 
			"		rank_name,\n" + 
			"		rank_flare\n" + 
			"FROM comments\n" + 
			"JOIN ranks ON comments.comment_poster_rank = ranks.id\n" + 
			"WHERE post_id = ?" + 
			"ORDER BY comments.id ASC" + 
			(limit > 0 ? "\nLIMIT "+limit : "") + 
			(offset > 0 ? "\nOFFSET "+offset : ""),
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
	 * @param email the commenter's email (may be null)
	 * @param trip the tripcode for the comment, or null for none
	 * @param rank the poster's rank
	 * @since 1.0
	 */
	public static void postComment(String name, String text, String date, String time, String postId, String ipHash, String email, String trip, int rank, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO comments\n" + 
			"(\n" + 
			"	comment_name,\n" + 
			"	comment_date,\n" + 
			"	comment_time,\n" + 
			"	comment_text,\n" + 
			"	post_id,\n" + 
			"	comment_ip,\n" + 
			"	comment_email,\n" + 
			"	comment_trip,\n" + 
			"	comment_poster_rank\n" + 
			") values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
			new JsonArray()
				.add(name)
				.add(date)
				.add(time)
				.add(text)
				.add(postId)
				.add(ipHash)
				.add(email)
				.add(trip)
				.add(rank),
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
			"SELECT COUNT(id) FROM posts WHERE post_id = ?",
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
			"SELECT post_text AS text, post_name AS name FROM posts WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Returns the expiration dates and times of all posts
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void postExpireDatesTimes(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query("SELECT post_expire_date, post_expire_time, post_id, post_sticky FROM posts", hdlr);
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
	
	/**
	 * Deletes all comments for the specified post ID
	 * @param postId the post ID
	 * @param hdlr the callback for this query
	 * @since 1.0
	 */
	public static void deleteComments(String postId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM comments WHERE post_id = ?",
			new JsonArray().add(postId),
			hdlr
		);
	}
	
	/**
	 * Queries for ban info on the specified IP address, if any
	 * @param ipHash the hashed IP address
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getBan(String ipHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT\n" + 
			"	bans.id,\n" + 
			"	ban_account AS banner_id,\n" + 
			"	account_username AS banner_username,\n" + 
			"	ban_ip AS ip,\n" + 
			"	ban_reason AS reason\n" + 
			"FROM bans\n" + 
			"JOIN accounts ON ban_account = accounts.id\n" + 
			"WHERE ban_ip = ?",
			new JsonArray().add(ipHash),
			hdlr
		);
	}
	
	/**
	 * Queries for all categories that require the specified rank or lower
	 * @param rankRequired the rank required to post
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getCategories(int rankRequired, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT id, category_name AS name, category_description AS description, category_code AS code FROM categories WHERE category_rank_required <= ?",
			new JsonArray().add(rankRequired),
			hdlr
		);
	}
	
	/**
	 * Queries for all non-private categories, along with the name of the required posting rank
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getCategories(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	categories.id,\n" + 
			"	category_name AS name,\n" + 
			"	category_description AS description,\n" + 
			"	category_code AS code,\n" + 
			"	category_rank_required AS rank_required,\n" + 
			"	rank_name\n" + 
			"FROM categories\n" + 
			"JOIN ranks ON ranks.id = category_rank_required\n" + 
			"WHERE categories.id > -1",
			hdlr
		);
	}
	
	/**
	 * Queries for the category with the specified ID
	 * @param id the category ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getCategory(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT * FROM categories WHERE id = ?",
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Creates a new account
	 * @param username the username of the account
	 * @param pwdHash the password hash for the account
	 * @param rank the account's rank
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createAccount(String username, String pwdHash, int rank, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO accounts (account_username, account_hash, account_rank) VALUES (?, ?, ?)",
			new JsonArray()
				.add(username)
				.add(pwdHash)
				.add(rank),
			hdlr
		);
	}
	
	/**
	 * Queries for the account with the specified ID
	 * @param id the account ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getAccount(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT * FROM accounts WHERE id = ?",
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Queries for the account with the specified username
	 * @param username the account's username
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getAccount(String username, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT * FROM accounts WHERE account_username = ?",
			new JsonArray().add(username),
			hdlr
		);
	}
	
	/**
	 * Sets the last bump date on a post
	 * @param postId the post ID
	 * @param timeStr the bump time
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void bumpPost(String postId, String dateStr, String timeStr, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE posts\n" + 
			"SET post_bump = TO_TIMESTAMP(?, 'MM/DD/YYYY HH24:MI')\n" + 
			"WHERE post_id = ?",
			new JsonArray()
				.add(dateStr+' '+timeStr)
				.add(postId),
			hdlr);
	}
	
	/**
	 * Queries for site-wide statistics
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void siteStats(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	(SELECT COUNT(id) FROM posts WHERE post_category > -1) AS public_post_count,\n" + 
			"	(SELECT COUNT(id) FROM posts WHERE post_category < 0) AS private_post_count,\n" + 
			"	(SELECT COUNT(id) FROM posts) AS total_post_count,\n" + 
			"	(SELECT COUNT(id) FROM comments) AS comment_count,\n" + 
			"	(SELECT COUNT(ip) FROM (\n" + 
			"		SELECT DISTINCT post_ip AS ip\n" + 
			"			FROM posts\n" + 
			"			UNION\n" + 
			"			SELECT DISTINCT comment_ip AS ip\n" + 
			"			FROM comments\n" + 
			"	) AS unique_ips) AS unique_posters",
			hdlr
		);
	}
	
	/**
	 * Returns the latest comments (on public posts)
	 * @param limit the amount of comments to return. Specify 0 to return all comments
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void latestComments(int limit, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	comments.id,\n" + 
			"	comments.post_id,\n" + 
			"	comment_ip AS ip,\n" + 
			"	comment_name AS name,\n" + 
			"	comment_text AS text,\n" + 
			"	comment_date AS date,\n" + 
			"	comment_time AS time,\n" + 
			"	comment_poster_rank AS poster_rank,\n" + 
			"	comment_poster_ban AS ban_text,\n" + 
			"	post_category AS category,\n" + 
			"	category_code,\n" + 
			"	rank_name,\n" + 
			"	rank_flare\n" + 
			"FROM comments\n" + 
			"JOIN ranks ON comments.comment_poster_rank = ranks.id\n" + 
			"JOIN posts ON comments.post_id = posts.post_id\n" + 
			"JOIN categories ON post_category = categories.id\n" + 
			"WHERE post_category > -1 AND comment_poster_ban IS NULL\n" + 
			"ORDER BY id DESC" + 
			(limit > 0 ? "\nLIMIT "+limit : ""),
			hdlr
		);
	}
	
	/**
	 * Fetches the latest posts
	 * @param limit the amount of posts to return. Specify 0 to return all posts
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void latestPosts(int limit, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	post_name AS name, \n" + 
			"	post_date AS date,\n" + 
			"	post_time AS time,\n" + 
			"	post_category AS category,\n" + 
			"	post_id,\n" + 
			"	category_code,\n" + 
			"	category_name,\n" + 
			"	(\n" + 
			"		SELECT COUNT(*)\n" + 
			"		FROM comments\n" + 
			"		WHERE post_id = posts.post_id\n" + 
			"	) AS comment_count\n" + 
			"	FROM posts\n" + 
			"	JOIN categories ON posts.post_category = categories.id\n" + 
			"	WHERE post_category > -1 AND post_sticky < 1\n" + 
			"	ORDER BY posts.id DESC" + 
			(limit > 0 ? "\nLIMIT "+limit : ""),
			hdlr
		);
	}
	
	/**
	 * Creates a new account login record
	 * @param accountId the ID of the account which was accessed
	 * @param ip the IP address that logged into the account
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createLoginRecord(int accountId, String ip, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO account_logins\n" + 
			"(\n" + 
			"	account_id,\n" + 
			"	ip,\n" + 
			"   timestamp\n" + 
			") VALUES (?, ?, NOW()::timestamp)",
			new JsonArray()
				.add(accountId)
				.add(ip),
			hdlr
		);
	}
	
	/**
	 * Queries for all ban records
	 * @param limit the amount of ban records to return. Specify 0 to return all ban records
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getBans(int limit, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	bans.id,\n" + 
			"	ban_account AS banner_id,\n" + 
			"	account_username AS banner_username,\n" + 
			"	ban_ip AS ip,\n" + 
			"	ban_reason AS reason,\n" + 
			"   ban_timestamp AS timestamp\n" + 
			"FROM bans\n" + 
			"JOIN accounts ON ban_account = accounts.id\n" + 
			"ORDER BY bans.id DESC" + 
			(limit > 0 ? "\nLIMIT "+limit : ""),
			hdlr
		);
	}
	
	/**
	 * Creates a new ban record
	 * @param ipHash the hash of the banned IP address
	 * @param reason the reason for the ban
	 * @param banner the ID of the account that banned the IP
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createBan(String ipHash, String reason, int banner, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO bans\n" + 
			"(\n" + 
			"	ban_ip,\n" + 
			"	ban_reason,\n" + 
			"	ban_account,\n" + 
			"   ban_timestamp\n" + 
			") VALUES (?, ?, ?, NOW())",
			new JsonArray()
				.add(ipHash)
				.add(reason)
				.add(banner),
			hdlr
		);
	}
	
	/**
	 * Creates a new ban record, pulling the IP hash from a comment
	 * @param commentId the ID of the comment
	 * @param reason the reason for the ban
	 * @param banner the ID of the account that banned the IP
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createBanFromComment(int commentId, String reason, int banner, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO bans (ban_ip, ban_reason, ban_account, ban_timestamp) VALUES (\n" + 
			"	(SELECT comment_ip FROM comments WHERE comments.id = ?),\n" + 
			"	?,\n" + 
			"	?,\n" + 
			"   NOW()\n" + 
			")",
			new JsonArray()
				.add(commentId)
				.add(reason)
				.add(banner),
			hdlr
		);
	}
	
	/**
	 * Creates a new ban record, pulling the IP hash from a post
	 * @param postId the ten character post ID
	 * @param reason the reason for the ban
	 * @param banner the ID of the account that banned the IP
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createBanFromPost(String postId, String reason, int banner, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO bans (ban_ip, ban_reason, ban_account, ban_timestamp) VALUES (\n" + 
			"	(SELECT post_ip FROM posts WHERE post_id = ?),\n" + 
			"	?,\n" + 
			"	?,\n" + 
			"   NOW()\n" + 
			")",
			new JsonArray()
				.add(postId)
				.add(reason)
				.add(banner),
			hdlr
		);
	}
	
	/**
	 * Deletes a single comment
	 * @param id the comment ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteComment(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM comments WHERE id = ?", 
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Updates the ban text on a comment
	 * @param id the comment ID
	 * @param banText the ban text
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void updateCommentBan(int id, String banText, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE comments SET comment_poster_ban = ? WHERE id = ?", 
			new JsonArray()
				.add(banText)
				.add(id),
			hdlr
		);
	}
	
	/**
	 * Deletes a ban record
	 * @param id the ban ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteBan(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM bans WHERE id = ?",
			new JsonArray().add(id), hdlr
		);
	}
	
	/**
	 * Returns info about an account
	 * @param id the account ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getAccountInfo(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT\n" + 
			"	accounts.id,\n" + 
			"	account_username AS username,\n" + 
			"	account_rank AS rank,\n" + 
			"	account_record_logins AS record_logins,\n" + 
			"	rank_name,\n" + 
			"	rank_flare\n" + 
			"FROM accounts\n" + 
			"JOIN ranks ON ranks.id = account_rank\n" + 
			"WHERE accounts.id = ?",
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Returns all login records for a specified account
	 * @param accountId the account ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getAccountLogins(int accountId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT ip, timestamp FROM account_logins WHERE account_id = ?",
			new JsonArray().add(accountId),
			hdlr
		);
	}
	
	/**
	 * Updates an account's password
	 * @param accountId the account ID to update
	 * @param passHash the hashed password
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void updateAccountPassword(int accountId, String passHash, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE accounts\n" + 
			"SET account_hash = ?\n" + 
			"WHERE id = ?",
			new JsonArray()
				.add(passHash)
				.add(accountId),
			hdlr
		);
	}
	
	/**
	 * Updates account settings
	 * @param accountId the account ID to update
	 * @param recordLogins whether to record login IP addresses
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void updateAccountSettings(int accountId, boolean recordLogins, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE accounts\n" + 
			"SET account_record_logins = ?\n" + 
			"WHERE id = ?",
			new JsonArray()
				.add(recordLogins ? 1 : 0)
				.add(accountId),
			hdlr
		);
	}
	
	/**
	 * Queries for all bulletins
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getBulletins(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"   bulletins.id,\n" + 
			"	bulletin_content AS content,\n" + 
			"	bulletin_date AS date,\n" + 
			"	bulletin_time AS time,\n" + 
			"	account_username AS poster_username,\n" + 
			"	rank_flare AS poster_flare\n" + 
			"FROM bulletins\n" + 
			"JOIN accounts ON accounts.id = bulletin_poster\n" + 
			"JOIN ranks ON account_rank = ranks.id\n" + 
			"ORDER BY bulletins.id DESC",
			hdlr
		);
	}
	
	/**
	 * Creates a new bulletin
	 * @param poster the account that posted the bulletin
	 * @param content the bulletin's content
	 * @param date the post date
	 * @param time the post time
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createBulletin(int poster, String content, String date, String time, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO bulletins\n" + 
			"(\n" + 
			"	bulletin_poster,\n" + 
			"	bulletin_content,\n" + 
			"	bulletin_date,\n" + 
			"	bulletin_time\n" + 
			") VALUES (?, ?, ?, ?)",
			new JsonArray()
				.add(poster)
				.add(content)
				.add(date)
				.add(time),
			hdlr
		);
	}
	
	/**
	 * Deletes a bulletin
	 * @param id the bulletin ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteBulletin(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM bulletins WHERE id = ?",
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Sets whether a post is sticky
	 * @param postId the post ID
	 * @param sticky whether it should be sticky
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void setPostSticky(String postId, boolean sticky, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE posts\n" + 
			"SET post_sticky = ?\n" + 
			"WHERE post_id = ?",
			new JsonArray()
				.add(sticky ? 1 : 0)
				.add(postId),
			hdlr
		);
	}
	
	/**
	 * Queries for all staff account info
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getAccounts(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"   accounts.id," + 
			"	account_username AS username,\n" + 
			"	account_rank AS rank,\n" + 
			"	rank_name\n" + 
			"FROM accounts\n" + 
			"JOIN ranks ON ranks.id = account_rank",
			hdlr
		);
	}
	
	/**
	 * Deletes an account
	 * @param id the account ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteAccount(int id, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM accounts WHERE id = ?",
			new JsonArray().add(id),
			hdlr
		);
	}
	
	/**
	 * Updates an account's rank
	 * @param id the account ID
	 * @param rank the new rank ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void updateAccountRank(int id, int rank, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE accounts\n" + 
			"SET account_rank = ?\n" + 
			"WHERE id = ?",
			new JsonArray()
				.add(rank)
				.add(id),
			hdlr
		);
	}
	
	/**
	 * Queries for all ranks
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void getRanks(Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().query(
			"SELECT\n" + 
			"	id,\n" + 
			"	rank_name AS name,\n" + 
			"	rank_flare AS flare\n" + 
			"FROM ranks", 
			hdlr
		);
	}
	
	/**
	 * Deletes all login records for the specified account ID
	 * @param accountId the account ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteLoginRecords(int accountId, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM account_logins WHERE account_id = ?",
			new JsonArray().add(accountId),
			hdlr
		);
	}
	
	/**
	 * Deletes all comments on posts in the specified category
	 * @param category the category ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteCommentsByCategory(int category, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"SELECT deletecomments(?)",
			new JsonArray().add(category),
			hdlr
		);
	}
	
	/**
	 * Deletes all posts in the specified category
	 * @param category the category ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deletePostsByCategory(int category, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM posts WHERE post_category = ?",
			new JsonArray().add(category),
			hdlr
		);
	}
	
	/**
	 * Deletes a category
	 * @param category the category ID
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void deleteCategory(int category, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"DELETE FROM categories WHERE id = ?",
			new JsonArray().add(category),
			hdlr
		);
	}
	
	/**
	 * Creates a new category
	 * @param name the category name
	 * @param code the category code (would appear in routes like /<the code>/)
	 * @param description the category description
	 * @param rankRequired the rank minimum rank required to post. 0 for any rank.
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void createCategory(String name, String code, String description, int rankRequired, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"INSERT INTO categories\n" + 
			"(\n" + 
			"	category_name,\n" + 
			"	category_code,\n" + 
			"	category_description,\n" + 
			"	category_rank_required\n" + 
			") VALUES (\n" + 
			"	?, ?, ?, ?\n" + 
			")", 
			new JsonArray()
				.add(name)
				.add(code)
				.add(description)
				.add(rankRequired),
			hdlr
		);
	}
	
	/**
	 * Updates a category's info
	 * @param id the category ID
	 * @param name the new name
	 * @param description the new description
	 * @param code the new code
	 * @param rankRequired the new minimum poster rank required
	 * @param hdlr the callback for this query
	 * @since 2.0
	 */
	public static void updateCategory(int id, String name, String description, String code, int rankRequired, Handler<AsyncResult<ResultSet>> hdlr) {
		Database.client().queryWithParams(
			"UPDATE categories\n" + 
			"SET\n" + 
			"	category_name = ?,\n" + 
			"	category_description = ?,\n" + 
			"	category_code = ?,\n" + 
			"	category_rank_required = ?\n" + 
			"WHERE id = ?",
			new JsonArray()
				.add(name)
				.add(description)
				.add(code)
				.add(rankRequired)
				.add(id),
			hdlr
		);
	}
}
