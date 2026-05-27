package com.aranaira.arcanearchives.util;

import me.towdium.pinin.PinIn;

import java.util.function.BiPredicate;

public class PinyinHelper {
	private static BiPredicate<String, String> matcher;

	public static BiPredicate<String, String> getMatcher () {
		if (matcher == null) {
			PinIn pinin = new PinIn();
			matcher = (text, filter) -> PinIn.Matcher.contains(text, filter, pinin);
		}
		return matcher;
	}
}
