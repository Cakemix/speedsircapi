package com.speed.irc.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.speed.irc.event.EventManager;
import com.speed.irc.event.NoticeEvent;
import com.speed.irc.event.PrivateMessageEvent;
import com.speed.irc.event.RawMessageEvent;
import com.speed.irc.types.Channel;
import com.speed.irc.types.NOTICE;
import com.speed.irc.types.PRIVMSG;
import com.speed.irc.types.RawMessage;

/**
 * A class representing a socket connection to an IRC server with the
 * functionality of sending raw commands and messages.
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
public class Connection implements ConnectionHandler, Runnable {
	public final BufferedWriter write;
	public final BufferedReader read;
	private final Socket socket;
	public EventManager eventManager = new EventManager();
	private final Thread thread;
	private boolean autoJoin;
	private String nick;
	private Thread eventThread;
	private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<String>();
	public Map<String, Channel> channels = new HashMap<String, Channel>();

	/**
	 * Used to set whether the client should rejoin a channel after being kicked
	 * from it.
	 * 
	 * @param on
	 *            true if the client should rejoin after being kicked.
	 */
	public void setAutoRejoin(final boolean on) {
		autoJoin = on;
	}

	public Connection(Socket sock) throws IOException {
		socket = sock;
		write = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		read = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		eventThread = new Thread(eventManager);
		eventThread.start();
		new Thread(this).start();
		thread = new Thread(new Runnable() {
			public void run() {
				String s;
				try {
					while ((s = read.readLine()) != null) {
						s = s.substring(1);
						if (s.contains("VERSION")) {
							String sender = s.split("!")[0];
							sendRaw("NOTICE " + sender + " :\u0001VERSION Speed's IRC API\u0001\n");
						}
						if (s.contains("PING") && !s.contains("PRIVMSG")) {
							sendRaw("PONG " + socket.getInetAddress().getHostAddress() + "\n");
						} else if (s.contains("KICK") && (!s.contains("PRIVMSG") || !s.contains("NOTICE"))
								&& s.contains(nick) && autoJoin) {
							Thread.sleep(700);
							joinChannel(s.substring(s.indexOf("KICK")).split(" ")[1]);
						} else if (s.contains("NOTICE")) {

							String message = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
							String sender = s.split("!")[0];
							String channel = null;
							if (s.contains("NOTICE #"))
								channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
							eventManager.fireEvent(new NoticeEvent(new NOTICE(message, sender, channel), this));
						} else if (s.contains(":") && s.substring(0, s.indexOf(":")).contains("PRIVMSG")) {
							String message = s.substring(s.indexOf(" :")).trim().replaceFirst(":", "");
							String sender = s.split("!")[0];
							String channel = null;
							if (s.contains("PRIVMSG #"))
								channel = s.substring(s.indexOf("#")).split(" ")[0].trim();
							eventManager.fireEvent(new PrivateMessageEvent(new PRIVMSG(message, sender, channels
									.get(channel)), this));
						} else {
							eventManager.fireEvent(new RawMessageEvent(new RawMessage(s), this));
						}
					}

					socket.close();
					eventManager.setRunning(false);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		thread.start();
	}

	public void setNick(final String nick) {
		this.nick = nick;
	}

	/**
	 * Sends a message to a channel/nick
	 * 
	 * @param CHANNEL
	 *            Either the channel or nick you wish to send the message to.
	 * @param message
	 *            The message you wish to send
	 * 
	 */
	public void sendMessage(final PRIVMSG message) {
		sendRaw("PRIVMSG " + message.getChannel().getName() + " :" + message.getMessage() + "\n");
	}

	/**
	 * Sends a raw command to the server, remember to use "\n" for a new line
	 * everytime you send a raw command.
	 * 
	 * @param raw
	 *            The raw command you wish to send to the server.
	 */
	public void sendRaw(String raw) {

		messageQueue.add(raw);

	}

	/**
	 * Sends a notice to the specified nick.
	 * 
	 * @param to
	 *            The nick/channel you wish to send the message to.
	 * @param message
	 *            The message you wish to send.
	 */
	public void sendNotice(final NOTICE notice) {
		sendRaw("NOTICE " + notice.getChannel() + " :" + notice.getMessage() + "\n");
	}

	/**
	 * Joins a channel.
	 * 
	 * @param channel
	 *            The channel you wish to join.
	 */
	public void joinChannel(String channel) {
		sendRaw("JOIN " + channel + "\n");
	}

	/**
	 * Sends an action to a channel/nick.
	 * 
	 * @param channel
	 *            The specified nick you would like to send the action to.
	 * @param action
	 *            The action you would like to send.
	 */
	public void sendAction(String channel, String action) {
		sendRaw("PRIVMSG " + channel + ": \u0001ACTION " + action + "\u0001\n");
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void run() {
		while (true) {

			String s = null;
			synchronized (messageQueue) {
				s = messageQueue.poll();
			}
			if (s != null) {
				if (!s.endsWith("\n")) {
					s = s + "\n";
				}
				try {
					if (write != null) {
						write.write(s);
						write.flush();
					} else {
						messageQueue.add(s);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
