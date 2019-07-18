package net.termer.textbin.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import net.termer.textbin.Module;
import net.termer.twine.ServerManager;

/**
 * Utility class for accessing the database
 * @author termer
 * @since 1.0
 */
public class Database {
	private static SQLClient _client = null;
	
	/**
	 * Initializes the database connection
	 * @since 1.0
	 */
	public static void init() {
		HikariConfig cfg = new HikariConfig();
		cfg.setJdbcUrl(
			"jdbc:postgresql://"+
			Module.config().db_address+
			":"+
			Module.config().db_port+
			"/"+
			Module.config().db_name
		);
		cfg.setUsername(
			Module.config().db_user
		);
		cfg.setPassword(
			Module.config().db_pass
		);
		cfg.setMaximumPoolSize(
			Module.config().db_max_pool_size
		);
		_client = JDBCClient.create(ServerManager.vertx(), new HikariDataSource(cfg));
	}
	
	/**
	 * Closes the database connection and does any necessary clean up
	 * @since 1.0
	 */
	public static void close() {
		_client.close();
	}
	
	/**
	 * Returns a database client
	 * @return the client
	 * @since 1.0
	 */
	public static SQLClient client() {
		return _client;
	}
}
