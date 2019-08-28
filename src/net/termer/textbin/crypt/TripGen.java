/* Copyright (c) 2012, Jeffrey Dileo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * Modifications were made to use class as a utility rather than an executable, along with addition documentation.
 * Style modifications were made to adhere to the style of the rest of the project.
 * All remaining original code is inside of the classic() method.
 */

package net.termer.textbin.crypt;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.termer.textbin.Module;
import net.termer.textbin.Str;

/**
 * Utility class for generating tripcodes
 * @author Jeffrey Dileo
 * @since 2.0
 */
public class TripGen {
	/**
	 * Generates a tripcode for the supplied trip (either the trip or the name and trip separated by a # character) using the classic method
	 * @param trip the trip or full name
	 * @param secure whether a preconfigured salt should be appended before hashing
	 * @return The tripcode corresponding to the provided trip
	 * @throws CharacterCodingException if encoding the trip fails
	 * @throws UnsupportedEncodingException if encoding the trip fails
	 * @since 2.0
	 */
	public static String classic(String trip, boolean secure) throws CharacterCodingException, UnsupportedEncodingException {
		if(trip.contains("#")) {
			trip = trip.substring(trip.lastIndexOf("#")+1);
		}
		// Ensure the trip's length
		trip = Str.toLength(trip, 8, 'f');
		byte[] trip2 = {};
		Charset charset = Charset.forName("Shift_JIS");
		CharsetEncoder encoder = charset.newEncoder();
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(trip));
		@SuppressWarnings("unused")
		CharBuffer cbuf = decoder.decode(bbuf);
		trip2 = bbuf.array();
		
		String salt = (trip + "H.").substring(1,3);
		salt = salt.replaceAll("[^\\.-z]", ".");
		String from = ":;<=>?@[\\]^_`";
		String to = "ABCDEFGabcdef";
		for(int i = 0; i < from.length(); i++ ) {
			salt = salt.replace(from.charAt(i), to.charAt(i));
		}
		String hash = Crypt.crypt((salt+(secure?Module.config().trip_salt:"")).getBytes("UTF-8"), trip2).substring(3,13);
		return hash;
	}
	
	/**
	 * Generates a tripcode for the supplied trip (either the trip or the name and trip separated by a # character) using the secure method
	 * @param trip the trip or full name
	 * @return The tripcode corresponding to the provided trip
	 * @throws NoSuchAlgorithmException if encoding the trip fails
	 * @throws UnsupportedEncodingException if encoding the trip fails
	 * @since 2.0
	 */
	public static String secure(String trip) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		if(trip.contains("#")) {
			trip = trip.substring(trip.lastIndexOf("#")+1);
		}
		// Ensure the trip's length
		trip = Str.toLength(trip, 8, 'f');
		
		byte[] str = (trip+Module.config().trip_salt).getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(str);
		
		// Convert to hex
		StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; i+=2) {
        	byte b = hash[i];
            sb.append(String.format("%02x", b));
        }
        
		return sb.toString().substring(3, 13);
	}
}
