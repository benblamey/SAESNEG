package com.benblamey.core;

import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

import java.net.UnknownHostException;

public class MongoClientInstance {

	private static MongoClient _clientLocal;
	private static MongoClient _clientTunnel;

	/**
	 * Get a MongoClient for the Mongo DB on the GeekISP server, tunneling if necessary.
	 * Assumes the tunnel has already been set up.
	 * @return
	 */
	public static synchronized MongoClient getBenBlameyCom() {

		/*
		 *
		 * The Java MongoDB driver is thread safe. If you are using in a web serving environment,
		 * for example, you should create a single MongoClient instance, and you can use it in
		 * every request. The
		 * MongoClient object maintains an internal pool of connections to the database (default
		 * pool size of 10). For every request to the DB (find, insert, etc) the Java thread will
		 * obtain a connection from the pool, execute the operation, and release the connection.
		 * This means the connection (socket) used may be different each time.
		 *
		 * http://docs.mongodb.org/ecosystem/drivers/java-concurrency/#java-driver-concurrency
		 */

		boolean useLocal;

		switch (SystemInfo.detectServer()) {
                    case BENBLAMEY_OVH_STAGING:
                    case BENBLAMEY_OVH:
                            useLocal = true;
                            break;
                    case LOCAL_MACHINE:
                            useLocal = false;
                            break;
                    default:
                            throw new RuntimeException("server not supported.");
		}

		if (useLocal) {
			return getClientLocal();
		} else {
			// System.out.println("Using SSH tunnel on port 1234 for mongodb...");
			return getClientRemote();
		}

	}

	public static synchronized MongoClient getClientLocal() {
		if (_clientLocal == null) {
			try {
				_clientLocal = new MongoClient("localhost");
			} catch (UnknownHostException e) {
				// Can't find localhost is a runtime error - pointless to catch.
				throw new RuntimeException(e);
			}
			_clientLocal.setWriteConcern(WriteConcern.SAFE);
		}
		return _clientLocal;
	}

	public static synchronized MongoClient getClientRemote() {

		if (_clientTunnel == null) {
			try {
				_clientTunnel = new MongoClient("localhost", 27018);
			} catch (UnknownHostException e) {
				// Can't find localhost is a runtime error - pointless to catch.
				throw new RuntimeException(e);
			}
			_clientTunnel.setWriteConcern(WriteConcern.SAFE);
		}
		return _clientTunnel;
	}

}
