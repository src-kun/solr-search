package com.sgk.util;

public class FilterUtil {

	static String[] filterList = { "12345", "123456", "1234567", "12345678", "123456789", "1234567890", "987654321",
			"87654321", "7654321", "654321", "54321", "123123", "112233", "11223344", "111222333", "111222333444",
			"abcde", "abcdef", "abcdefg", "abcdefgh", "abcdefghi", "abcdefghij", "aaa123", "abc123", "bbb123", "aabbcc",
			"abcabc", "abcdabcd", "aaabbbccc", "aabbccdd", "ccc123", "qwert", "qwerty", "asdfg", "asdfgh", "qazwsx",
			"user", "guest", "admin", "administrator", "manager" };

	/**
	 * ¼ì²âËÑË÷¹Ø¼ü×ÖÊÇ·ñ¿ÉÓÃ
	 * 
	 * @param str
	 *            ¹Ø¼ü×Ö
	 * @return boolean
	 * 
	 */
	public static boolean futility(String str) {
		boolean notlegal = false;

		for (String s : filterList) {
			if (str.equals(s))
					return true;
		}
//		char first = str.charAt(0);
//		for (int i = 1; i < str.length(); i++) {
//			if (str.charAt(i) == first)
//				notlegal = true;
//			else
//				notlegal = false;
//		}
		return notlegal;
	}

	/**
	 * ¼ì²éÃô¸Ð´Ê
	 * 
	 * @param str
	 * @return
	 */
	public static boolean sensitive(String str) {
		String[] keyfilter = {"*"};
		for (int i = 0; i < keyfilter.length; i++) {
			if (str.contains(keyfilter[i])) 
			{
				return true;
			}
		}
		return false;
	}
}
