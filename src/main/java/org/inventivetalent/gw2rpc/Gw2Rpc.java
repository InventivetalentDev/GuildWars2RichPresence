package org.inventivetalent.gw2rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import java.io.File;

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

	File            dataDir;
	IPCClient       client;
	MumbleLink      mumbleLink;
	PresenceUpdater presenceUpdater;
	APIHelper       apiHelper;
	RegionManager   regionManager;
	StatusDialog    dialog;
	boolean         processRunning = false;
	boolean         shouldShutdown = false;

	public Gw2Rpc() throws NoDiscordClientException, InterruptedException {
		this.dataDir = new File("data");
		if (!this.dataDir.exists()) {
			this.dataDir.mkdir();
		}

		this.client = new IPCClient(434425240794300418L);
		this.client.setListener(this);

		this.dialog = new StatusDialog(this);
		this.dialog.createDialog();

		new Thread(() -> {
			ProcessChecker processChecker = new ProcessChecker();
			while (true) {
				if (!processRunning) {
					System.out.println("Waiting for Guild Wars 2 to start...");
					dialog.labelContent.setText("Waiting for Guild Wars 2 to start...");
				}
				boolean check = processChecker.isProcessRunning(PROCESS_NAME);
				if (!processRunning && check) {
					System.out.println("Guild Wars 2 started!");
					dialog.labelContent.setText("Guild Wars 2 started! Waiting for info...");
					processRunning = true;

					try {
						startup();
					} catch (NoDiscordClientException e) {
						e.printStackTrace();
					}
				}
				if (processRunning && !check || shouldShutdown) {
					if (shouldShutdown) {
						dialog.labelContent.setText("Shutting down...");
					} else {
						dialog.labelContent.setText("Guild Wars 2 closed!");
					}
					processRunning = false;

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					shutdown();
					break;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

		this.dialog.pack();
		this.dialog.setVisible(true);
	}

	private void startup() throws NoDiscordClientException {
		this.presenceUpdater = new PresenceUpdater(this);
		this.apiHelper = new APIHelper();
		this.regionManager = new RegionManager(this);

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
		System.out.println("Shutting down...");

		if (this.mumbleLink != null) { this.mumbleLink.kill(); }
		if (this.presenceUpdater != null) { this.presenceUpdater.kill(); }
		if (this.client != null) {
			try {
				this.client.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}

}
