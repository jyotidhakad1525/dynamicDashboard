package com.automate.df.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

public final class PasswordEncryptor {
	private PasswordEncryptor() {
	}

	public static final int NUMBER_12 = 12;

	/**
	 * Hash a {@link java.lang.String}
	 * 
	 * @param value the value to hash
	 * @return the hashed value
	 */
	public static String encrypt(final String value) {
		return BCrypt.hashpw(value, BCrypt.gensalt(NUMBER_12));
	}
}
