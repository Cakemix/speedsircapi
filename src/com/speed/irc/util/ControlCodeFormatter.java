package com.speed.irc.util;

/**
 * Formats messages with control codes, using a specified 'format' character.
 * <p/>
 * This file is part of Speed's IRC API.
 * <p/>
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * <p/>
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Shivam Mistry
 */
public class ControlCodeFormatter {
	public static final char UNICODE_COLOUR = '\u0003';
	public static final char UNICODE_BOLD = '\u0002';
	public static final char UNICODE_UNDERLINE = '\u001F';

	private static char format_character = '$';
	
	private char formatChar;

	public ControlCodeFormatter() {
		formatChar = '$';
	}

	public ControlCodeFormatter(final char format_char) {
		formatChar = format_char;
	}

	public enum Colour {
		WHITE(0), BLACK(1), NAVY_BLUE(2), GREEN(3), RED(4), CRIMSON_RED(5), MAGENTA(
				6), BROWN(7), YELLOW(8), LIME(9), TEAL(10), AQUA(11), ROYAL_BLUE(
				12), PINK(13), DARK_GREY(14), LIGHT_GREY(15);
		Colour(int code) {
			this.code = code;
		}

		int code;
	}

	/**
	 * Sets the default character to be replaced with colour control code in
	 * {@link #format(String, Colour...)}
	 * 
	 * @param c
	 *            the new format character
	 */
	public void setFormatChar(final char c) {
		formatChar = c;
	}

	/**
	 * Gets the default character to be replaced with colour control code in
	 * {@link #format(String, Colour...)}
	 * 
	 * @return the format character
	 */
	public char getFormatChar() {
		return formatChar;
	}

	/**
	 * Sets the default character to be replaced with colour control code in
	 * {@link #format(String, Colour...)}
	 * 
	 * @deprecated Use the instance instead {@link #setFormatChar(char)}
	 * @param c
	 *            the new format character
	 */
	public static void setFormatCharacter(final char c) {
		format_character = c;
	}

	/**
	 * Gets the default character to be replaced with colour control code in
	 * {@link #format(String, Colour...)}
	 * 
	 * @deprecated Use the instance instead {@link #getFormatChar()}
	 * @return the format character
	 */
	public static char getFormatCharacter() {
		return format_character;
	}

	/**
	 * Formats the string with the colours specified. Default format character
	 * is '$' and any format character is escaped using '\'.
	 * 
	 * 
	 * @param s
	 *            The string to be formatted.
	 * @param colours
	 *            The colours to format the string with.
	 * @return the formatted string
	 */
	public String formatString(final String s, final Colour... colours) {
		final StringBuilder builder = new StringBuilder();
		if (colours.length == 0)
			return s;
		int replaced = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (replaced >= colours.length) {
				if (c == '\\' && s.charAt(i + 1) == formatChar) {
					builder.append(formatChar);
					i++;
				} else {
					builder.append(c);
				}
				continue;
			} else if (c == formatChar) {
				if (i != 0) {
					if (s.charAt(i - 1) == '\\') {
						builder.deleteCharAt(builder.lastIndexOf("\\"));
						builder.append(c);
						continue;
					}
				}
				builder.append(UNICODE_COLOUR).append(colours[replaced++].code);

			} else {
				builder.append(c);
			}
		}
		return builder.toString() + ControlCodeFormatter.UNICODE_COLOUR;
	}

	/**
	 * Formats the string with the colours specified. Default format character
	 * is '$' and any format character is escaped using '\'.
	 * 
	 * Will be retained for fast formatting using the default char $
	 * 
	 * @param s
	 *            The string to be formatted.
	 * @param colours
	 *            The colours to format the string with.
	 * @return the formatted string
	 */
	public static String format(final String s, final Colour... colours) {
		final StringBuilder builder = new StringBuilder();
		if (colours.length == 0)
			return s;
		int replaced = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (replaced >= colours.length) {
				if (c == '\\' && s.charAt(i + 1) == format_character) {
					builder.append(format_character);
					i++;
				} else {
					builder.append(c);
				}
				continue;
			} else if (c == format_character) {
				if (i != 0) {
					if (s.charAt(i - 1) == '\\') {
						builder.deleteCharAt(builder.lastIndexOf("\\"));
						builder.append(c);
						continue;
					}
				}
				builder.append(UNICODE_COLOUR).append(colours[replaced++].code);

			} else {
				builder.append(c);
			}
		}
		return builder.toString() + ControlCodeFormatter.UNICODE_COLOUR;
	}
}
