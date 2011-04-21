package com.speed.irc.types;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.speed.irc.connection.Connection;
import com.speed.irc.event.IRCEventListener;

/**
 * The abstract class for making robots. To create a robot, you can extend this
 * class.
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
 * 
 * @author Speed
 * 
 */
public abstract class Bot {

	public Connection connection;
	private final String server;
	private final int port;

	public int getPort() {
		return port;
	}

	public String getServer() {
		return server;
	}

	public abstract void onStart();

	public Bot(final String server, final int port) {
		this.server = server;
		this.port = port;

		try {
			connection = new Connection(new Socket(server, port));
			connection.sendRaw("NICK " + getNick() + "\n");
			connection.sendRaw("USER " + getNick() + " team-deathmatch.com TB: Speed Bot\n");
			if (this instanceof IRCEventListener) {
				connection.eventManager.addListener((IRCEventListener) this);
			}
			connection.setNick(getNick());
			for (Channel s : getChannels()) {
				s.join();
			}
			onStart();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public abstract Channel[] getChannels();

	public abstract String getNick();

	public String getUser() {
		return "Speed";
	}

	/**
	 * Used to identify to NickServ.
	 * 
	 * @param password
	 *            The password assigned to your nick
	 * @throws IOException
	 */
	public void identify(String password) throws IOException {
		connection.write.write("PRIVMSG NickServ :identify " + password + "\n");
	}
}
