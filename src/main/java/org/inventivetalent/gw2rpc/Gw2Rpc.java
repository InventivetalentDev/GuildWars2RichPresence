package org.inventivetalent.gw2rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

public class Gw2Rpc implements IPCListener {

	static final String PROCESS_NAME = "Gw2-64.exe";

	static final String[] PROFESSIONS = {
			"",
			"Guardian",
			"Warrior",
			"Engineer",
			"Ranger",
			"Thief",
			"Elementalist",
			"Mesmer",
			"Necromancer",
			"Revenant"
	};
	static final String[] RACES       = {
			"Asura",
			"Charr",
			"Human",
			"Norn",
			"Sylvari"
	};

	IPCClient       client;
	MumbleLink      mumbleLink;
	PresenceUpdater presenceUpdater;
	IdTranslator    idTranslator;
	boolean         processRunning = false;

	public Gw2Rpc() throws NoDiscordClientException, InterruptedException {
		this.client = new IPCClient(434425240794300418L);
		this.client.setListener(this);

		ProcessChecker processChecker = new ProcessChecker();
		while (true) {
			if (!processRunning) {
				System.out.println("Waiting for Guild Wars 2 to start...");
			}
			boolean check = processChecker.isProcessRunning(PROCESS_NAME);
			if (!processRunning && check) {
				System.out.println("Guild Wars 2 started!");
				processRunning = true;

				startup();
			}
			if (processRunning && !check) {
				System.out.println("Guild Wars 2 closed!");
				processRunning = false;

				shutdown();
			}

			Thread.sleep(2000);
		}
	}

	private void startup() throws NoDiscordClientException {
		this.presenceUpdater = new PresenceUpdater(this);
		this.idTranslator = new IdTranslator();

		System.out.println("Connecting Discord client...");
		this.client.connect();

		System.out.println("Starting MumbleLink thread...");
		this.mumbleLink = new MumbleLink();
		this.mumbleLink.start();
	}

	public void onReady(IPCClient client) {
		System.out.println("Starting Presence Updater...");
		this.presenceUpdater.start();
	}

	private void shutdown() {
		if (this.mumbleLink != null) { this.mumbleLink.kill(); }
		if (this.presenceUpdater != null) { this.presenceUpdater.kill(); }
		if (this.client != null) { this.client.close(); }
	}

}
