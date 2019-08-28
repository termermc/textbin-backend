package net.termer.textbin;

/**
 * Utility class for processing Strings
 * @author termer
 * @since 2.0
 */
public class Str {
	/**
	 * Truncates the provided String
	 * @param str the String to truncate
	 * @param len the desired length
	 * @since 2.0
	 */
	public static String truncate(String str, int len) {
		return str.substring(0, Math.min(len, str.length()));
	}
	
	/**
	 * Ensures a String is at least a minimum length by padding it with extra chars if it does not meet the length requirement
	 * @param str the String to process
	 * @param len the desired minimum length
	 * @param padChar the char to pad the String with if it does not meet the length requirement
	 * @return the processed String
	 * @since 2.0
	 */
	public static String minLen(String str, int len, char padChar) {
		StringBuilder out = new StringBuilder(str);
		if(str.length() < len) {
			out.ensureCapacity(len);
			if(str.length() < len) {
				out.append(padChar);
			}
		}
		return out.toString();
	}
	
	/**
	 * Takes in a String and either truncates it, pads it with a character to meet a certain length, or does nothing until it is a certain length
	 * @param str the String
	 * @param len the desired length
	 * @param padChar the char to pad the String with if it does not meet the length requirement
	 * @return the processed String
	 * @since 2.0
	 */
	public static String toLength(String str, int len, char padChar) {
		StringBuilder out = new StringBuilder(str);
		if(str.length() > len) {
			out = new StringBuilder(truncate(str, len));
		} else if(str.length() < len) {
			out.ensureCapacity(len);
			while(out.length() < len) {
				out.append(padChar);
			}
		}
		return out.toString();
	}
}
