
package net_utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Class that allows a device to identify itself on the INTRANET.
 * 
 * @author Decoded4620 2016
 *         http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
 */

public class HostAdress {
	
	private static final boolean LOG = false;

	private String loopbackHost = "";
	private String host = "";

	private String loopbackIp = "";

	private String ip = "";
	private String hostAddress = "";

	public static final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";

	
	private Pattern pattern = Pattern.compile(IPV4_REGEX);

	private boolean validIP(String ip) {
		if (ip == null || ip.isEmpty())
			return false;
		ip = ip.trim();
		if ((ip.length() < 6) & (ip.length() > 15))
			return false;
		try {
			Matcher matcher = pattern.matcher(ip);
			return matcher.matches();
		} catch (PatternSyntaxException ex) {
			return false;
		}
	}

	public HostAdress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();
				if (i != null) {
					Enumeration<InetAddress> addresses = i.getInetAddresses();
					if (LOG) {
						log(i.getDisplayName());
						log("\t- name: " + i.getName());
						log("\t- idx: " + i.getIndex());
						log("\t- max trans unit (MTU): " + i.getMTU());
						log("\t- is loopback: " + (i.isLoopback() ? "true" : "false"));
						log("\t- is PPP: " + (i.isPointToPoint() ? "true" : "false"));
						log("\t- isUp: " + (i.isUp() ? "true" : "false"));
						log("\t- isVirtual: " + (i.isVirtual() ? "true" : "false"));
						log("\t- supportsMulticast: " + (i.supportsMulticast() ? "true" : "false"));
					}

					while (addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						String hostAddr = address.getHostAddress();

						// local loopback
						if (hostAddr.indexOf("127.") == 0) {
							this.loopbackIp = address.getHostAddress();
							this.loopbackHost = address.getHostName();
						}

						// internal ip addresses (behind this router)
						if (hostAddr.indexOf("192.168") == 0 || hostAddr.indexOf("10.") == 0
								|| hostAddr.indexOf("172.16") == 0) {
							this.host = address.getHostName();
							this.ip = address.getHostAddress();
						}
						if (LOG)
							log("\t\t-" + address.getHostName() + ":" + address.getHostAddress() + " - "
									+ address.getAddress());
						if (i.isUp() && !i.isVirtual()) {
							String hAddress = new String(address.getHostAddress());
							if (validIP(hAddress)) {
								if (!i.getDisplayName().contains("VirtualBox")) {
									this.hostAddress = hAddress;
									if (LOG)
										log("*********  " + this.hostAddress);
								}
							}
						}
					}
				}
			}
		} catch (SocketException e) {

		}
		try {
			InetAddress loopbackIpAddress = InetAddress.getLocalHost();
			this.loopbackIp = loopbackIpAddress.getHostName();

		} catch (UnknownHostException e) {
			System.err.println("ERR: " + e.toString());
		}
	}

	public String getLoopbackHost() {
		return loopbackHost;
	}

	public String getHost() {
		return host;
	}

	public String getIp() {
		return ip;
	}

	public String getLoopbackIp() {
		return loopbackIp;
	}

	public static String getHostAddress() {
		return new HostAdress().hostAddress;
	}

	public static void main(String[] args) {
		try {

			HostAdress ni = new HostAdress();
			if (LOG) {
				log(ni.getHost());
				log(ni.getIp());
				log(ni.getLoopbackHost());
				log(ni.getLoopbackIp());
				log(ni.hostAddress);
			}

		} catch (Exception e) {

		}
	}

	private static void log(String m) {
		if (LOG)
			System.out.println(m);
	}



}