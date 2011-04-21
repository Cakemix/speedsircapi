package com.speed.irc.types;

/**
 * Represents a raw message.
 * 
 * This file is part of Speed's IRC API.
 * 
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Speed
 * 
 */
public class RawMessage {

	private String raw;
	private String code;
	private String sender;

	public RawMessage(String raw) {
		this.raw = raw;
		String[] strings = raw.split(" ");
		code = strings[1];
		sender = strings[0];
	}

	public String getRaw() {
		return raw;
	}

	public String getCode() {
		return code;
	}

	public String getSender() {
		return sender;
	}
}
