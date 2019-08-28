package net.termer.textbin;

/**
 * Class to use for JSON mapping
 * @author termer
 * @since 1.0
 */
public class TextBinConfig {
	public String db_address = "localhost";
	public int db_port = 3333;
	public String db_name = "textbin";
	public String db_user = "me";
	public String db_pass = "drowssap";
	public int db_max_pool_size = 10;
	public String domain = "textbin";
	public int max_posts = 20;
	public String frontend_host = "http://localhost:8080";
	public String super_admin_username = "super";
	public String super_admin_password = "drowssap";
	public String trip_strategy = "classic";
	public String trip_salt = "PleaseChangeMe";
	public String ip_hash_algorithm = "SHA-256";
	public String ip_hash_salt = "PleaseChangeMe";
	public int site_stat_rank_required = 1;
}