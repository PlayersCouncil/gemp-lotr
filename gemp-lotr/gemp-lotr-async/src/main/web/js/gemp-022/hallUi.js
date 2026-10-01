// The connection readout at the left end of the main bar ("• Connected"): a coloured dot plus a word, as a button.
// Hovering or focusing it, or clicking/tapping it, shows a small popup with the status, the server time of the last
// successful update and, when the hall can no longer update by itself, what to do about it.  The popup never opens by
// itself; a visually hidden live region announces each change of state (with its reason when disconnected) instead.
// States: connecting, connected, shutdown, reconnecting, disconnected, and loggedout (a visitor who is not logged in:
// neutral grey, "Not logged in", with a "Log in" link beside it).  shutdown (yellow) is "connected, but the server is in
// shutdown mode ahead of a restart" (the hall update's shutdown="true"); a connection problem (amber reconnecting, red
// disconnected) shows instead of it, and the next good update puts it back.
var HallConnectionIndicator = Class.extend({
	root: null,
	button: null,
	label: null,
	live: null,
	popup: null,

	state: null,
	// the server's clock ("yyyy-MM-dd HH:mm:ss", GMT) at the last successful update, as the main bar shows it
	lastUpdate: null,
	detail: null,
	pinned: false,

	LABELS: {
		connecting: "Connecting",
		connected: "Connected",
		shutdown: "Shutdown",
		reconnecting: "Reconnecting",
		disconnected: "Disconnected",
		loggedout: "Not logged in"
	},

	STATUS: {
		connecting: "Connecting to the Game Hall…",
		connected: "Connected: the Game Hall is updating live.",
		shutdown: "Shutdown: the server will restart for an update when the games in progress end. You can finish or "
			+ "watch games, but can't start or join tables, bot games or tournament queues until then; waiting tables "
			+ "and queues were closed.",
		reconnecting: "Reconnecting: the Game Hall lost contact with the server and is retrying.",
		disconnected: "Disconnected: the Game Hall is not updating.",
		loggedout: "Not logged in: log in to see the Game Hall's tables and chat and to play."
	},

	init: function (root) {
		var that = this;
		this.root = root;
		this.button = root.find(".hall-connection-readout");
		this.label = root.find(".hall-connection-label");
		this.live = root.find(".hall-connection-live");
		this.popup = root.find(".hall-connection-popup");
		this.signin = root.find(".hall-connection-signin");
		// the login page is given the current link, so logging in comes back to what was showing
		this.signin.on("click", function () {
			this.href = HallConnectionIndicator.loginUrl();
		});

		root.on("mouseenter", function () {
			that.open(false);
		});
		root.on("mouseleave", function () {
			if (!that.pinned && !that.keyboardFocusInside())
				that.close();
		});
		// keyboard users get the same popup as a hover while the readout has focus (a mouse click is handled above)
		root.on("focusin", function () {
			if (that.keyboardFocusInside())
				that.open(false);
		});
		root.on("focusout", function (event) {
			if (!that.pinned && !(event.relatedTarget && $.contains(root[0], event.relatedTarget)))
				that.close();
		});
		this.button.on("click", function () {
			if (that.pinned)
				that.close();
			else
				that.open(true);
		});
		root.on("keydown", function (event) {
			if (event.key === "Escape" && that.isOpen()) {
				that.close();
				that.button.trigger("focus");
			}
		});
		$(document).on("click", function (event) {
			if (that.pinned && root.length && !$.contains(root[0], event.target) && root[0] !== event.target)
				that.close();
		});

		this.set("connecting");
	},

	// Focus inside the readout that came from the keyboard (browsers also focus a button when it is clicked).
	keyboardFocusInside: function () {
		var active = document.activeElement;
		if (active == null || !this.root.length || !$.contains(this.root[0], active))
			return false;
		try {
			return active.matches(":focus-visible");
		} catch (e) {
			return true;
		}
	},

	isOpen: function () {
		return this.popup.length > 0 && !this.popup.prop("hidden");
	},

	open: function (pin) {
		this.pinned = this.pinned || pin;
		this.render();
		this.popup.prop("hidden", false);
		this.button.attr("aria-expanded", "true");
	},

	close: function () {
		this.pinned = false;
		this.popup.prop("hidden", true);
		this.button.attr("aria-expanded", "false");
	},

	// detail: {message: text, action: "reload" | "login" | null}
	set: function (state, detail) {
		var changed = state !== this.state;
		this.state = state;
		this.detail = detail || null;
		this.root.attr("data-state", state);
		this.label.text(this.LABELS[state]);
		if (this.signin != null)
			this.signin.prop("hidden", state !== "loggedout");
		if (changed) {
			// Screen readers hear the change (and, once the hall has stopped, why) without the popup being shown.
			var spoken = state === "loggedout" ? "Not logged in."
				: state === "shutdown" ? "Server in shutdown mode: new games can't be started until it restarts."
				: "Game Hall " + this.LABELS[state].toLowerCase() + ".";
			if (state === "disconnected" && this.detail != null && this.detail.message)
				spoken += " " + this.detail.message;
			this.live.text(spoken);
		}
		this.render();
	},

	// serverTime: the server clock at this update, formatted as the main bar shows it; shutdown: the update said the
	// server is in shutdown mode
	updated: function (detail, serverTime, shutdown) {
		this.lastUpdate = serverTime || null;
		// a message given while connected (e.g. "reconnected after a pause") stays until the connection changes
		if ((this.state === "connected" || this.state === "shutdown") && detail == null)
			detail = this.detail;
		this.set(shutdown ? "shutdown" : "connected", detail);
	},

	render: function () {
		this.popup.find(".hall-connection-popup-status").text(this.STATUS[this.state]);
		this.popup.find(".hall-connection-popup-updated").text("Last update: "
			+ (this.lastUpdate == null ? "none yet" : this.lastUpdate + " (server time)"))
			.prop("hidden", this.state === "loggedout" && this.lastUpdate == null);

		var message = this.popup.find(".hall-connection-popup-message").empty();
		if (this.detail != null && this.detail.message)
			message.append($("<span></span>").text(this.detail.message));
		if (this.detail != null && this.detail.action === "reload") {
			message.append(" ", $("<a class='hall-connection-reload'></a>")
				.attr("href", window.location.href)
				.text("Reload the page")
				.on("click", function (event) {
					event.preventDefault();
					window.location.reload();
				}));
		} else if (this.detail != null && this.detail.action === "login") {
			message.append(" ", $("<a class='hall-connection-login'></a>")
				.attr("href", HallConnectionIndicator.loginUrl())
				.text(this.state === "loggedout" ? "Log in or register" : "Go to the main page to log in")
				.on("click", function () {
					this.href = HallConnectionIndicator.loginUrl();
				}));
		}
		message.prop("hidden", message.is(":empty"));
	}
});

// The login page (coming back to the current hall link once logged in, when GempLinks is on the page).
HallConnectionIndicator.loginUrl = function () {
	return window.GempLinks ? GempLinks.loginUrl() : "/gemp-lotr/";
};

// Epoch ms -> "yyyy-MM-dd HH:mm:ss" in GMT, the way the server writes its clock (DateUtils.getStringDateWithHour).
HallConnectionIndicator.formatServerTime = function (ms) {
	var date = new Date(ms);
	var two = function (n) {
		return (n < 10 ? "0" : "") + n;
	};
	return date.getUTCFullYear() + "-" + two(date.getUTCMonth() + 1) + "-" + two(date.getUTCDate()) + " "
		+ two(date.getUTCHours()) + ":" + two(date.getUTCMinutes()) + ":" + two(date.getUTCSeconds());
};

// The bar along the top edge of the chat: drag it (or focus it and use the Up / Down arrow keys) to make the chat
// taller or shorter.  The height is kept in the chatResize cookie.
// The Discord chat is an iframe, and an iframe takes the pointer events of whatever moves over it: while dragging, the
// handle holds pointer capture and every iframe on the page ignores the pointer (body.hall-chat-resizing), so the drag
// follows the pointer both ways.  The height is applied at most once per animation frame.
var HallChatResizer = Class.extend({
	chat: null,
	handle: null,
	drag: null,
	frame: null,

	COOKIE: "chatResize",
	DEFAULT_HEIGHT: 300,
	MIN_HEIGHT: 100,
	// room the rest of the page keeps when the chat is made as tall as it goes
	PAGE_RESERVE: 160,
	KEY_STEP: 20,

	init: function (chat) {
		var that = this;
		this.chat = chat;
		if (!chat.length)
			return;
		this.handle = $("<div class='ui-resizable-handle ui-resizable-n hall-chat-resizer' role='separator' tabindex='0'></div>")
			.attr("aria-orientation", "horizontal")
			.attr("aria-label", "Resize the chat")
			.attr("title", "Drag to resize the chat");
		chat.prepend(this.handle);

		var stored = parseInt($.cookie(this.COOKIE), 10);
		this.apply(isNaN(stored) ? this.DEFAULT_HEIGHT : stored);

		this.handle.on("pointerdown", function (event) {
			that.start(event.originalEvent || event);
		});
		this.handle.on("lostpointercapture", function () {
			that.end();
		});
		this.handle.on("keydown", function (event) {
			var step = event.key === "ArrowUp" ? that.KEY_STEP : event.key === "ArrowDown" ? -that.KEY_STEP : 0;
			if (step === 0)
				return;
			event.preventDefault();
			that.apply(that.chat.height() + step);
			that.save();
		});
	},

	maxHeight: function () {
		return Math.max(this.MIN_HEIGHT, $(window).height() - this.PAGE_RESERVE);
	},

	apply: function (height) {
		var clamped = Math.round(Math.min(this.maxHeight(), Math.max(this.MIN_HEIGHT, height)));
		this.chat.height(clamped);
		this.handle.attr({"aria-valuemin": this.MIN_HEIGHT, "aria-valuemax": this.maxHeight(), "aria-valuenow": clamped});
		return clamped;
	},

	save: function () {
		$.cookie(this.COOKIE, this.chat.height(), {expires: 365});
	},

	start: function (event) {
		if (this.drag != null || (event.button != null && event.button !== 0))
			return;
		var that = this;
		// no text selection, no native drag
		if (event.preventDefault)
			event.preventDefault();
		this.drag = {startY: event.clientY, y: event.clientY, startHeight: this.chat.height(), pointerId: event.pointerId};
		try {
			if (event.pointerId != null && this.handle[0].setPointerCapture)
				this.handle[0].setPointerCapture(event.pointerId);
		} catch (e) {
			// capture is a nicety: the document listeners below and the iframe shield do the work without it
		}
		$("body").addClass("hall-chat-resizing");
		$(document)
			.on("pointermove.hallChatResize", function (e) {
				that.move(e.originalEvent || e);
			})
			.on("pointerup.hallChatResize pointercancel.hallChatResize", function (e) {
				that.move(e.originalEvent || e);
				that.end();
			});
	},

	move: function (event) {
		if (this.drag == null || event.clientY == null)
			return;
		var that = this;
		this.drag.y = event.clientY;
		if (this.frame == null) {
			this.frame = window.requestAnimationFrame(function () {
				that.frame = null;
				that.applyDrag();
			});
		}
	},

	applyDrag: function () {
		if (this.drag != null)
			this.apply(this.drag.startHeight + (this.drag.startY - this.drag.y));
	},

	end: function () {
		if (this.drag == null)
			return;
		if (this.frame != null) {
			window.cancelAnimationFrame(this.frame);
			this.frame = null;
		}
		this.applyDrag();
		var pointerId = this.drag.pointerId;
		this.drag = null;
		$(document).off(".hallChatResize");
		$("body").removeClass("hall-chat-resizing");
		try {
			if (pointerId != null && this.handle[0].hasPointerCapture && this.handle[0].hasPointerCapture(pointerId))
				this.handle[0].releasePointerCapture(pointerId);
		} catch (e) {
			// already released
		}
		this.save();
	}
});

var GempLotrHallUI = Class.extend({
	comm:null,
	chat:null,

	tablesDiv:null,
	adminTab:null,
	userInfo:null,
	players: [],

	tableJoiner:null,
	tableCreator:null,

	inTournament:false,

	// My Account shows this (includes/account.html reads hall.pocketValue)
	pocketValue:null,
	hallChannelId: null,

	// ---- the hall long-poll ----
	connection: null,
	pollFailures: 0,
	pollTimer: null,
	pollStopped: false,
	// the next poll must be a full GET /hall (first load, or the server dropped our channel)
	pollFull: true,
	resubscribed: false,
	RETRY_BASE_MS: 1000,
	RETRY_CAP_MS: 30000,
	FAILURES_BEFORE_DISCONNECTED: 5,

	// server clock minus ours, from the poll's serverTimeMs; waiting-table timers are measured on the server clock
	serverClockOffset: 0,
	// one shared interval ticks every waiting table's timer
	AGE_REFRESH_MS: 1000,

	// queue id -> {shown, dialog, deadline, timer}
	readyChecks: null,
	// "game:<gameId>" / "draft:<tournamentId>" -> the "your game is ready" dialog shown when the pop-up was blocked
	readyModals: null,
	// table id -> game id, so a table's removal can close its game's ready modal
	tableGames: null,
	// game ids whose window was already opened or offered: a re-signup after a 410 (or any repeated newGame) resends
	// newGame for games still running, and those must not be offered again
	offeredGames: null,

	// deferStart: build everything but leave the player info and the hall poll to start() (GameHall.js hands that to
	// GempHallSession, so a visitor reading a public tab is not asked to log in).
	init:function (url, chat, deferStart) {
		var that = this;

		this.readyChecks = {};
		this.readyModals = {};
		this.tableGames = {};
		this.offeredGames = {};
		this.connection = new HallConnectionIndicator($("#hall-connection"));

		// Requests other than the long-poll that have no handler of their own for an error end up here.
		this.comm = new GempLotrCommunication(url, function (xhr, ajaxOptions, thrownError) {
			that.requestFailed(xhr, thrownError);
		});

		this.chat = chat;
		this.chat.updatePlayerListener((players) => {
			that.players = players;
			that.tableCreator.createUnrankedTable.updatePlayers(players, that.userInfo ? that.userInfo.name : "");
		});
		this.chat.tournamentCallback = function(from, message) {
			const thisName = that.userInfo ? that.userInfo.name : null;
			// the server builds these with a little markup (<b>, <br>) around names it does not escape
			if (from == "TournamentSystem" && that.inTournament) {
				that.showDialog("Tournament Update", GempLotrHallUI.serverMarkup(message), 320);
			} else if (from.startsWith("TournamentSystemTo:")) {
				// Extract and split the user list
				const users = from.split(":")[1].split(";");

				// Check if thisName is in the list
				if (thisName != null && users.includes(thisName)) {
					that.showDialog("Tournament Update", GempLotrHallUI.serverMarkup(message), 320);
				}
			}
		};

		this.chatResizer = new HallChatResizer($("#chat"));

		this.tablesDiv = $("#tablesDiv");

		var deckManager = new DeckManager(this.comm);
		var formatManager = new FormatManager(this.comm);
		this.tableCreator = new CreateTable(this, this.comm, $("#create-table-popup"), formatManager, deckManager);
		this.tableJoiner = new JoinTable(this, this.comm, $("#join-table-popup"), formatManager, deckManager);
		$("#open-table-button").button().click(
			function() {
				// a visitor who is not logged in is told so instead of getting a Play popup full of errors
				if (typeof GempHallSession !== "undefined" && GempHallSession.loggedOut) {
					that.showLoginRequired(true);
					return;
				}
				that.tableCreator.showPopup();
			});

		this.adminTab = $("#tabs > ul :nth-child(7)");
		this.adminTab.hide();

		// Section open/closed state, saved in the hallSettings cookie as "|"-separated flags.  Slots 5 and 7 belonged
		// to sections that no longer exist (solo, queue spawner); they are kept so older cookies still line up.
		var hallSettings = this.readHallSettings();
		this.initTable(hallSettings[0] == "1", "waitingTablesHeader", "waitingTablesContent");
		this.initTable(hallSettings[1] == "1", "playingTablesHeader", "playingTablesContent");
		this.initTable(hallSettings[2] == "1", "finishedTablesHeader", "finishedTablesContent");
		this.initTable(hallSettings[6] == "1", "recurringQueuesHeader", "recurringQueuesContent");

		$("#deckbuilder-button").button();
		$("#bug-button").button();
		$("#report-button").button();
		$("#discord-button").button();
		$("#wiki-button").button();
		$("#merchant-button").button();

		// Waiting tables show how long they have been open ("Waiting 01:30"); one interval ticks them all, with no
		// request to the server.
		setInterval(function () {
			that.refreshTableAges();
		}, this.AGE_REFRESH_MS);

		if (!deferStart)
			this.start();
	},

	started: false,
	startQueue: null,

	// Runs fn once the hall is live (at once if it is): requests only a logged-in player can make.
	whenStarted: function (fn) {
		if (this.started) {
			fn();
			return;
		}
		(this.startQueue = this.startQueue || []).push(fn);
	},

	// The hall goes live: the player info (Admin tab), the hall poll and whatever waited for it (whenStarted).
	start: function () {
		var that = this;
		if (this.started)
			return;
		this.started = true;
		this.connection.set("connecting");
		var queued = this.startQueue || [];
		this.startQueue = null;
		for (var i = 0; i < queued.length; i++) {
			try {
				queued[i]();
			} catch (e) {
				if (window.console)
					console.error("Starting the hall failed", e);
			}
		}

		// One /player request for the whole page (chat.js asks for the same one).
		this.comm.getPlayerInfoShared(function(json) {
			that.userInfo = json;
			if (that.userInfo.type.includes("a") || that.userInfo.type.includes("l")) {
				// a link to the Admin tab (or a remembered Admin tab) is opened by GempLinks once it shows
				that.adminTab.show();
				if (typeof GempLinks !== "undefined")
					GempLinks.poke();
			} else {
				that.adminTab.hide();
			}
		});

		this.getHall();
	},

	setPendingTournament: function(value) {
		this.inTournament = value;
	},

	HALL_SETTINGS_DEFAULT: "1|1|0|0|0|0|0|0",
	// cookie slot -> the section content it records (null = retired slot)
	HALL_SETTINGS_SLOTS: ["waitingTablesContent", "playingTablesContent", "finishedTablesContent", "wcQueuesContent",
		"scheduledQueuesContent", null, "recurringQueuesContent", null],

	readHallSettings: function () {
		return ($.cookie("hallSettings") || this.HALL_SETTINGS_DEFAULT).split("|");
	},

	// A collapsible section: a <button class="eventHeader"> header and its content div.
	createSectionHeader: function (id, sectionClass, title, contentId) {
		return $("<button type='button' class='eventHeader'></button>")
			.attr("id", id)
			.attr("aria-controls", contentId)
			.attr("aria-expanded", "false")
			.addClass(sectionClass)
			.text(title)
			.append("<span class='count'>(0)</span>");
	},

	addWcQueuesSection: function() {
		if ($("#wcQueuesHeader").length) return;

		const $header = this.createSectionHeader("wcQueuesHeader", "wc-queues", "World Championship Queues", "wcQueuesContent");

		const $content = $("<div>")
			.attr("id", "wcQueuesContent")
			.addClass("visibilityToggle")
			.append(
				$("<table>").addClass("tables wc-queues").append(`
					<tr>
						<th width='10%'>Format</th>
						<th width='8%'>Collection</th>
						<th width='30%'>Event Name</th>
						<th width='16%'>Starts</th>
						<th width='16%'>System</th>
						<th width='8%'>Cost</th>
						<th width='12%'>Prizes</th>
					</tr>
				`)
			);

		this.insertEventSection($header, $content, "wcQueues");
	},

	addScheduledQueuesSection: function() {
		if ($("#scheduledQueuesHeader").length) return;

		const $header = this.createSectionHeader("scheduledQueuesHeader", "scheduledQueues", "Scheduled Events", "scheduledQueuesContent");

		const $content = $("<div>")
			.attr("id", "scheduledQueuesContent")
			.addClass("visibilityToggle")
			.append(
				$("<table>").addClass("tables scheduledQueues").append(`
					<tr>
						<th width='10%'>Format</th>
						<th width='8%'>Collection</th>
						<th width='30%'>Event Name</th>
						<th width='16%'>Starts</th>
						<th width='16%'>System</th>
						<th width='8%'>Cost</th>
						<th width='12%'>Prizes</th>
					</tr>
				`)
			);

		this.insertEventSection($header, $content, "scheduledQueues");
	},

	insertEventSection: function($header, $content, sectionKey) {
		const sectionOrder = [
			"wcQueues",
			"scheduledQueues"
		];

		const sectionIds = {
			wcQueues: "wcQueuesContent",
			scheduledQueues: "scheduledQueuesContent"
		};

		const index = sectionOrder.indexOf(sectionKey);
		if (index === -1) {
			console.error("Unknown section key:", sectionKey);
			return;
		}

		// Look ahead for any section that should come after this one
		let inserted = false;

		for (let i = index + 1; i < sectionOrder.length; i++) {
			const $nextContent = $("#" + sectionIds[sectionOrder[i]]);
			if ($nextContent.length) {
			const $nextHeader = $("#" + sectionOrder[i] + "Header");
				$header.insertBefore($nextHeader);
				$content.insertBefore($nextHeader);
				inserted = true;
				break;
			}
		}

		// Fallback insert
		if (!inserted) {
			const $anchor = $("#recurringQueuesHeader");
			$header.insertBefore($anchor);
			$content.insertBefore($anchor);
		}

		const hallSettings = this.readHallSettings();
		const settingIndex = this.HALL_SETTINGS_SLOTS.indexOf($content.attr("id"));
		if (settingIndex !== -1) {
			this.initTable(hallSettings[settingIndex] === "1", $header.attr("id"), $content.attr("id"));
		}
	},

	removeWcQueuesSection: function() {
		$("#wcQueuesHeader").remove();
		$("#wcQueuesContent").remove();
	},

	removeScheduledQueuesSection: function() {
		$("#scheduledQueuesHeader").remove();
		$("#scheduledQueuesContent").remove();
	},

	initTable: function(displayed, headerID, tableID) {
		const header = $("#" + headerID);
		const content = $("#" + tableID);
		const that = this;

		// Inject triangle span if not already present
		if (header.find(".disclosureTriangle").length === 0) {
			header.prepend("<span class='disclosureTriangle' aria-hidden='true'></span>");
		}
		header.attr("aria-controls", tableID);

		// The header is a <button>, so Enter and Space activate it like a click.
		const toggle = function () {
			content.toggleClass("hidden").toggle("blind", {}, 200);
			header.toggleClass("expanded");
			header.attr("aria-expanded", header.hasClass("expanded") ? "true" : "false");
			that.updateHallSettings();
		};

		header.off("click").on("click", toggle);

		if (displayed) {
			content.removeClass("hidden").show();
			header.addClass("expanded");
		} else {
			content.addClass("hidden").hide();
			header.removeClass("expanded");
		}
		header.attr("aria-expanded", displayed ? "true" : "false");
	},


	updateHallSettings: function() {
		const ids = this.HALL_SETTINGS_SLOTS;

		// Load current cookie (or use default if missing)
		let currentSettings = this.readHallSettings();

		// Update only if the element exists
		for (let i = 0; i < ids.length; i++) {
			if (ids[i] == null)
				continue;
			const $el = $("#" + ids[i]);
			if ($el.length) {
				currentSettings[i] = $el.hasClass("hidden") ? "0" : "1";
			}
		}

		$.cookie("hallSettings", currentSettings.join("|"), { expires: 365 });
	},

	// ---- the hall long-poll ------------------------------------------------------------------------------------------
	// GET /hall signs up for a channel and returns everything; POST /hall/update then long-polls that channel for
	// changes.  Every outcome schedules the next request: success after processHall (in a finally, so an exception
	// while drawing cannot stop the loop), transient failures (no answer, time-out, 5xx) with exponential backoff capped
	// at RETRY_CAP_MS.  After FAILURES_BEFORE_DISCONNECTED consecutive failures the readout turns red, but retrying
	// goes on.  Only "not logged in", "the hall was opened elsewhere" and other 4xx answers stop it.

	getHall: function() {
		var that = this;
		this.pollTimer = null;
		this.comm.getHall(
			function(xml) {
				that.pollSucceeded(xml);
				that.clearHallRows();
				that.processHall(xml, true);
			},
			function (xhr, textStatus, errorThrown) {
				that.pollFailed(xhr, textStatus, errorThrown);
			});
	},

	updateHall:function () {
		var that = this;
		this.pollTimer = null;
		this.comm.updateHall(
			function (xml) {
				that.pollSucceeded(xml);
				that.processHall(xml);
			}, this.hallChannelId,
			function (xhr, textStatus, errorThrown) {
				that.pollFailed(xhr, textStatus, errorThrown);
			});
	},

	schedulePoll: function (delay) {
		var that = this;
		if (this.pollStopped)
			return;
		if (this.pollTimer != null)
			clearTimeout(this.pollTimer);
		this.pollTimer = setTimeout(function () {
			if (that.pollFull || that.hallChannelId == null)
				that.getHall();
			else
				that.updateHall();
		}, delay);
	},

	pollSucceeded: function (xml) {
		var detail = null;
		if (this.pollFull && this.resubscribed)
			detail = {message: "Reconnected after the server had dropped this window from the hall (it had been away too long). "
				+ "Any table or queue you were waiting in may have been closed."};
		this.pollFull = false;
		this.resubscribed = false;
		this.pollFailures = 0;
		var root = xml != null && xml.documentElement != null ? xml.documentElement : null;
		this.connection.updated(detail, this.serverTimeOf(xml), root != null && root.getAttribute("shutdown") === "true");
	},

	// The server clock of a poll answer, as the main bar shows it: its serverTime, else serverTimeMs formatted the same
	// way, else our clock corrected by the last known offset.
	serverTimeOf: function (xml) {
		var root = xml != null && xml.documentElement != null ? xml.documentElement : null;
		var text = root != null ? root.getAttribute("serverTime") : null;
		if (text)
			return text;
		var ms = root != null ? parseInt(root.getAttribute("serverTimeMs"), 10) : NaN;
		return HallConnectionIndicator.formatServerTime(isNaN(ms) ? Date.now() + this.serverClockOffset : ms);
	},

	pollFailed: function (xhr, textStatus, errorThrown) {
		var status = xhr != null ? xhr.status : 0;
		if (status == 401) {
			if (typeof GempHallSession !== "undefined")
				GempHallSession.markLoggedOut();
			this.stopPolling("You are not logged in (or your session has ended).", "login", "loggedout");
		} else if (status == 409) {
			this.stopPolling("The Game Hall was opened in another window or tab, so this one stopped updating. "
				+ "Close this one, or reload it to use the hall here.", "reload");
		} else if (status == 410) {
			// The server dropped this channel after a long silence (a sleeping laptop, say): sign up again and redraw
			// the hall from a full snapshot.
			this.pollFull = true;
			this.resubscribed = true;
			this.hallChannelId = null;
			this.retryPoll("the server had dropped this window from the hall");
		} else if (status == 0 || status >= 500) {
			var reason;
			if (status == 0)
				reason = textStatus === "timeout" ? "the server did not answer in time" : "could not reach the server";
			else
				reason = "the server answered with error " + status;
			this.retryPoll(reason);
		} else {
			this.stopPolling("The Game Hall stopped updating: the server answered with error " + status + ".", "reload");
		}
	},

	retryPoll: function (reason) {
		this.pollFailures++;
		var delay = Math.min(this.RETRY_CAP_MS, this.RETRY_BASE_MS * Math.pow(2, this.pollFailures - 1));
		var seconds = Math.round(delay / 1000);
		if (this.pollFailures >= this.FAILURES_BEFORE_DISCONNECTED) {
			this.connection.set("disconnected", {
				message: "Lost contact with the server (" + reason + "). Still retrying every " + seconds
					+ " s; if it does not come back, reload the page.",
				action: "reload"
			});
		} else {
			this.connection.set("reconnecting", {
				message: "Lost contact with the server (" + reason + "). Retrying in " + seconds + " s."
			});
		}
		this.schedulePoll(delay);
	},

	stopPolling: function (message, action, state) {
		this.pollStopped = true;
		if (this.pollTimer != null)
			clearTimeout(this.pollTimer);
		this.pollTimer = null;
		// The readout turns red (grey "Not logged in" for a viewer who is not logged in) and its live region announces
		// why; the popup itself only opens on hover, focus or a click (a dialog about the same problem may already be
		// on screen).
		this.connection.set(state || "disconnected", {message: message, action: action});
	},

	// Errors of hall requests other than the long-poll (a request whose caller gave no handler for that status).
	requestFailed: function (xhr, thrownError) {
		if (thrownError == "abort")
			return;
		var status = xhr != null ? xhr.status : 0;
		if (status == 401) {
			this.showLoginRequired();
		} else if (status == 0 || status == 504) {
			// the connection readout reports connectivity; one lost side request is not worth a dialog
			console.log("HTTP error communicating with server: " + status);
		} else {
			this.showErrorDialog("Server error", "The server could not complete that request (error " + status
				+ "). Try again; if it keeps happening, reload the page.", true, false);
		}
	},

	// Kept for pages loaded into the hall's tabs that pass it to their own requests (Help, Playtesting).
	hallErrorMap:function() {
		var that = this;
		return {
			"0": function() {
				that.showErrorDialog("Server connection error", "Unable to connect to server. Either server is down or there is a problem with your internet connection.", true, false);
			},
			"401":function() {
				that.showLoginRequired();
			},
			"409":function() {
				that.showErrorDialog("Concurrent access error", "You are accessing Game Hall from another browser or window. Close this window or if you wish to access Game Hall from here, click \"Refresh page\".", true, false);
			},
			"410":function() {
				that.showErrorDialog("Inactivity error", "You were inactive for too long and have been removed from the Game Hall. If you wish to re-enter, click \"Refresh page\".", true, false);
			},
			"400":function() {
				that.showErrorDialog("Request error", "The server could not process that request. Try again; if it keeps happening, reload the page.", false, false);
			}
		};
	},

	showErrorDialog:function(title, text, reloadButton, mainPageButton) {
		var buttons = {};
		if (reloadButton) {
			buttons["Refresh page"] =
				function () {
					location.reload(true);
				};
		}
		if (mainPageButton) {
			buttons["Go to main page"] =
				function() {
					location.href = HallConnectionIndicator.loginUrl();
				};
		}

		var dialog = $("<div></div>").dialog({
			title: title,
			resizable: false,
			height: 160,
			modal: true,
			buttons: buttons,
			closeText: ''
		}).text(text);
	},

	// "Not logged in": a request needed a logged-in player.  Shown only while a tab that needs one is showing (Game
	// Hall, Events, My Account, Admin), and one at a time; on Help or Server Info it waits until the viewer opens such a
	// tab (GempHallSession.loginTabOpened).  force: shown on any tab (the viewer pressed Play).
	loginDialog: null,

	showLoginRequired: function (force) {
		if (typeof GempHallSession !== "undefined")
			GempHallSession.markLoggedOut();
		if (!force && typeof GempLinks !== "undefined" && !GempLinks.currentTabNeedsLogin())
			return;
		if (this.loginDialog != null && this.loginDialog.dialog("instance") && this.loginDialog.dialog("isOpen"))
			return;
		var that = this;
		this.loginDialog = $("<div class='hall-login-required'></div>")
			.append($("<p></p>").text("You are not logged in. The Game Hall, Events and My Account need you to log in; "
				+ "Help and Server Info can be read without."))
			.dialog({
				title: "Not logged in",
				resizable: false,
				modal: true,
				width: 380,
				closeText: "",
				buttons: [
					{
						text: "Register or log in",
						click: function () {
							location.href = HallConnectionIndicator.loginUrl();
						}
					},
					{
						text: "Close",
						click: function () {
							$(this).dialog("close");
						}
					}
				],
				close: function () {
					$(this).dialog("destroy").remove();
					that.loginDialog = null;
				}
			});
	},

	// `text` is markup: callers pass their own markup, or server text through GempLotrHallUI.serverMarkup / escapeHtml.
	showDialog:function(title, text, height) {
		if(height == null)
			height = 200
		var dialog = $("<div></div>").dialog({
			title: title,
			resizable: true,
			height: height,
			modal: true,
			closeOnEscape: true,
			buttons: [
				{
					text: "OK",
					click: function() {
						$( this ).dialog( "close" );
					}
				}
			],
			closeText: ''
		}).html(text);
	},

	// <error message> / <response message> answers: the message is server text (it can echo a player's input), so it
	// is shown escaped, keeping only simple formatting tags.  Returns false for an error.
	processResponse:function (xml) {
		if (xml != null && xml != "OK" && xml.documentElement != null) {
			var root = xml.documentElement;
			if (root.tagName == "error" || root.tagName == "response") {
				var message = GempLotrHallUI.serverMarkup(root.getAttribute("message"));
				this.chat.appendMessage(message, "warningMessage");
				this.showDialog(root.tagName == "error" ? "Error" : "Response", message, 320);
				return root.tagName != "error";
			}
		}
		return true;
	},

	animateRowUpdate: function(row) {
		$(row)
			.css({borderTopColor:"#000000", borderLeftColor:"#000000", borderBottomColor:"#000000", borderRightColor:"#000000"})
			.animate({borderTopColor:"#ffffff", borderLeftColor:"#ffffff", borderBottomColor:"#ffffff", borderRightColor:"#ffffff"}, "fast");
	},

	PlaySound: function(soundObj) {
		var myAudio = document.getElementById(soundObj);
		if (myAudio == null)
			return;
		try {
			var playing = myAudio.play();
			// browsers refuse autoplay before the first interaction with the page; the sound is only a hint
			if (playing && typeof playing.catch === "function")
				playing.catch(function () {});
		} catch (e) {
			// same
		}
	},

	AddTesterFlag: function() {
		var that = this;

		that.comm.addTesterFlag(function () {
			window.location.reload(true);
		});
	},

	RemoveTesterFlag: function() {
		var that = this;

		that.comm.removeTesterFlag(function () {
			window.location.reload(true);
		});
	},

	MigrateTrophies: function() {
		var that = this;

		that.comm.migrateTrophies(function () {
			window.location.reload(true);
		});
	},

	// ---- rows ----------------------------------------------------------------------------------------------------------
	// Every row the hall draws is a <tr class="hall-row"> whose data-hall-key is "<kind>:<id>".  Kinds: "table" (a game
	// table), "queue" (a queue's row in the Recurring / Scheduled / World Championship sections), "queue-waiting" (the
	// same queue's row in Waiting Tables) and "tournament" (a running tournament in Playing Tables).  Queues, tournaments
	// and tables have separate id spaces, so the kind keeps an update to one from replacing another.

	rowsFor: function (kind, id, scope) {
		var key = kind + ":" + id;
		return $("tr.hall-row", scope || this.tablesDiv).filter(function () {
			return this.getAttribute("data-hall-key") === key;
		});
	},

	newRow: function (kind, id) {
		return $("<tr class='hall-row'></tr>").attr("data-hall-key", kind + ":" + id);
	},

	// Removes every drawn row, before drawing a full snapshot (a fresh GET /hall).  "Your game is ready" prompts stay
	// until the snapshot is drawn: those whose game or draft is still running survive it (see pruneReadyModals).
	clearHallRows: function () {
		$("tr.hall-row", this.tablesDiv).remove();
		this.removeWcQueuesSection();
		this.removeScheduledQueuesSection();
		this.tableGames = {};
		for (var id in this.readyChecks)
			this.endReadyCheck(id);
	},

	// full: xml is a complete snapshot (GET /hall), drawn on a cleared hall
	processHall:function (xml, full) {
		try {
			this.drawHall(xml);
			if (full)
				this.pruneReadyModals();
		} finally {
			this.schedulePoll(100);
		}
	},

	// After a full snapshot: closes the prompts of games whose table and drafts whose tournament are gone.
	pruneReadyModals: function () {
		var liveGames = {};
		for (var tableId in this.tableGames)
			liveGames["game:" + this.tableGames[tableId]] = true;
		for (var key in this.readyModals) {
			if (key.indexOf("game:") === 0 ? !liveGames[key] : this.rowsFor("tournament", key.substring(6)).length === 0)
				this.closeReadyModal(key);
		}
	},

	// Runs fn(element) for each element; an exception on one is logged and does not stop the others.
	forEachSafely: function (elements, fn) {
		for (let i = 0; i < elements.length; i++) {
			try {
				fn.call(this, elements[i]);
			} catch (e) {
				console.error("Game hall: could not show " + elements[i].tagName + " " + elements[i].getAttribute("id"), e);
			}
		}
	},

	drawHall: function (xml) {
		const root = xml.documentElement;
		if (root.tagName != "hall")
			return;

		this.hallChannelId = root.getAttribute("channelNumber");

		const currency = parseInt(root.getAttribute("currency"));
		if (!isNaN(currency))
			this.pocketValue = currency;

		const serverTimeMs = parseInt(root.getAttribute("serverTimeMs"));
		if (!isNaN(serverTimeMs))
			this.serverClockOffset = serverTimeMs - Date.now();

		const motd = root.getAttribute("motd");
		if (motd != null)
			$("#motd").html("<b>MOTD:</b> " + motd);

		const serverTime = root.getAttribute("serverTime");
		if (serverTime != null)
			$(".server-time").html(serverTime.replace(" ", "<br>"));

		this.forEachSafely(root.getElementsByTagName("queue"), this.drawQueue);

		if ($('.wc-queues tr').length <= 1) {
			this.removeWcQueuesSection();
		}

		if ($('.scheduledQueues tr').length <= 1) {
			this.removeScheduledQueuesSection();
		}

		this.forEachSafely(root.getElementsByTagName("tournament"), this.drawTournament);
		this.forEachSafely(root.getElementsByTagName("table"), this.drawTable);

		$(".count", $(".eventHeader.recurringQueues")).text("(" + ($("tr", $("table.recurringQueues")).length - 1) + ")");
		$(".count", $(".eventHeader.scheduledQueues")).text("(" + ($("tr", $("table.scheduledQueues")).length - 1) + ")");
		$(".count", $(".eventHeader.waitingTables")).text("(" + ($("tr", $("table.waitingTables")).length - 1) + ")");
		$(".count", $(".eventHeader.playingTables")).text("(" + ($("tr", $("table.playingTables")).length - 1) + ")");
		$(".count", $(".eventHeader.finishedTables")).text("(" + ($("tr", $("table.finishedTables")).length - 1) + ")");
		$(".count", $(".eventHeader.wc-queues")).text("(" + ($("tr", $("table.wc-queues")).length - 1) + ")");

		const games = root.getElementsByTagName("newGame");
		let started = false;
		for (let i = 0; i < games.length; i++) {
			const gameId = games[i].getAttribute("id");
			// a channel signed up again (after a 410) re-announces games that are still running: offer each game once
			if (gameId == null || this.offeredGames[gameId])
				continue;
			this.offeredGames[gameId] = true;
			started = true;
			this.openOrOffer("game:" + gameId, "/gemp-lotr/game.html?gameId=" + encodeURIComponent(gameId) + this.participantIdParam(),
				"Your game is ready", "Your game has started, but your browser blocked the new window.", "Open game");
		}
		if (started) {
			this.PlaySound("gamestart");
		}
	},

	participantIdParam: function () {
		const participantId = getUrlParam("participantId");
		return participantId != null ? "&participantId=" + encodeURIComponent(participantId) : "";
	},

	// ---- queues ----

	drawQueue: function (queue) {
		const that = this;
		const id = queue.getAttribute("id");
		const action = queue.getAttribute("action");

		if (action == "remove") {
			// Remove from both the Waiting Tables Section and Queue Sections
			this.rowsFor("queue", id).remove();
			this.rowsFor("queue-waiting", id).remove();
			this.endReadyCheck(id);
			return;
		}
		if (action != "add" && action != "update")
			return;

		const isWC = queue.getAttribute("wc") == "true";
		const isRecurring = queue.getAttribute("recurring") == "true";
		const isScheduled = queue.getAttribute("scheduled") == "true";
		const displayInWaitingTables = queue.getAttribute("displayInWaitingTables") == "true";
		const joined = queue.getAttribute("signedUp") == "true";
		const actionsField = $("<td></td>");

		if (!joined && queue.getAttribute("joinable") == "true") {
			actionsField.append(this.tableJoiner.generateJoinQueueButton(queue));
		} else if (joined) {
			this.inTournament = true;
			actionsField.append($("<button type='button'>Leave Queue</button>").button().click(function () {
				that.comm.leaveQueue(id, function (xml) {
					if (that.processResponse(xml))
						that.inTournament = false;
				});
			}));

			if (queue.getAttribute("startable") == "true") {
				actionsField.append($("<button type='button'>Start Now</button>").button().click(function () {
					that.comm.startQueue(id, function (xml) {
						that.processResponse(xml);
					});
				}));
			}

			const secs = +queue.getAttribute("readyCheckSecsRemaining");
			if (secs > -1) {
				const checkBut = $("<button type='button'></button>").text("READY CHECK - " + secs + " s");
				checkBut.button().click(function () {
					that.confirmReadyCheck(id);
				});
				if (queue.getAttribute("confirmedReadyCheck") == "true")
					checkBut.button("option", "label", "Waiting for others - " + secs + " s").button("disable");
				actionsField.append(checkBut);
			}
		}
		this.updateReadyCheck(queue);

		// The Recurring / Scheduled rows carry a clickable prize breakdown, but this Waiting Tables row is the only one
		// many queues have (player-made ones have no section row), so it keeps its own button.
		const prizes = GempLotrHallUI.parsePrizes(queue.getAttribute("prizes"));
		if (prizes.label || prizes.items.length) {
			actionsField.append($("<button type='button'>Show Prizes</button>").button().click(function () {
				that.showPrizes(prizes);
			}));
		}

		const type = GempLotrHallUI.displayType(queue.getAttribute("type"));
		const isLimited = type.includes("Sealed") || type.includes("Draft");
		const draftCode = (type.includes("Table") && queue.hasAttribute("draftCode")) ? queue.getAttribute("draftCode") : null;
		const queueName = queue.getAttribute("queue");

		// Row in the Recurring / Scheduled / World Championship sections
		const row = this.newRow("queue", id);
		row.append(GempLotrHallUI.cell(queue.getAttribute("format")));
		row.append(GempLotrHallUI.cell(isLimited ? type : queue.getAttribute("collection")));
		row.append($("<td></td>").append(GempLotrHallUI.eventName(queueName, draftCode)));
		row.append(GempLotrHallUI.cell(queue.getAttribute("start")));
		row.append(GempLotrHallUI.cell(queue.getAttribute("system")));
		row.append($("<td align='right'></td>").html(formatPrice(parseInt(queue.getAttribute("cost"), 10) || 0)));
		row.append($("<td></td>").append(GempLotrHallUI.prizeCell(prizes)));

		// Row for tournament queue waiting table
		const tablesRow = this.newRow("queue-waiting", id);
		tablesRow.append(GempLotrHallUI.cell(queue.getAttribute("format")));
		const start = queue.getAttribute("start");
		let prefix = "";
		if (isWC) {
			prefix = "World Championship - ";
		} else if (start === "When 2 players join" || start === "When 1 players join") {
			prefix = "";
		} else {
			// For system, ignore all after ',' (min players)
			prefix = (queue.getAttribute("system") || "").split(',')[0] + " Tournament - ";
		}
		tablesRow.append($("<td></td>").text(prefix + type + " - ").append(GempLotrHallUI.eventName(queueName, draftCode)));
		tablesRow.append(GempLotrHallUI.cell(start));
		tablesRow.append(GempLotrHallUI.cell(queue.getAttribute("playerList")));
		tablesRow.append(actionsField);
		if (joined) {
			tablesRow.addClass("played");
		}
		if (isWC) {
			tablesRow.addClass("bold");
		}

		if (action == "add") {
			if (isWC) {
				this.addWcQueuesSection();
				$("table.wc-queues", this.tablesDiv).append(row);
			} else if (isRecurring) {
				$("table.recurringQueues", this.tablesDiv).append(row);
			} else if (isScheduled) {
				this.addScheduledQueuesSection();
				$("table.scheduledQueues", this.tablesDiv).append(row);
			}
			// Display queues in waiting tables section
			if (displayInWaitingTables) {
				$("table.waitingTables", this.tablesDiv).append(tablesRow);
			}
		} else {
			this.rowsFor("queue", id).replaceWith(row);
			// Display queues with waiting players also as waiting tables
			if (displayInWaitingTables) {
				const existingRow = this.rowsFor("queue-waiting", id);
				if (existingRow.length > 0) {
					existingRow.replaceWith(tablesRow);
				} else {
					$("table.waitingTables", this.tablesDiv).append(tablesRow);
				}
			} else {
				this.rowsFor("queue-waiting", id).remove();
			}
		}

		this.animateRowUpdate(row);
	},

	showPrizes: function (prizes) {
		const content = $("<div></div>");
		if (prizes.items.length) {
			const list = $("<ul></ul>");
			prizes.items.forEach(function (item) {
				list.append($("<li></li>").text(item));
			});
			content.append(list);
		} else if (prizes.label) {
			content.append($("<p></p>").text(prizes.label));
		} else {
			content.append($("<p></p>").text("No prize information available."));
		}
		content.dialog({
			title: "Prizes details",
			closeOnEscape: true,
			resizable: false,
			width: 300,
			closeText: "",
			close: function () {
				content.dialog("destroy").remove();
			}
		});
	},

	// ---- the ready check ----
	// When a queue's ready check starts, a dialog carries the Ready action itself; OK just closes it (the row keeps its
	// READY CHECK button).  It closes by itself once confirmed or when the check ends.

	updateReadyCheck: function (queue) {
		const id = queue.getAttribute("id");
		const secs = +queue.getAttribute("readyCheckSecsRemaining");
		if (queue.getAttribute("signedUp") != "true" || !(secs > -1)) {
			this.endReadyCheck(id);
			return;
		}

		let check = this.readyChecks[id];
		if (check == null)
			check = this.readyChecks[id] = {shown: false, dialog: null, deadline: 0, timer: null};
		check.deadline = Date.now() + secs * 1000;

		if (queue.getAttribute("confirmedReadyCheck") == "true") {
			check.shown = true;
			this.closeReadyCheckDialog(check);
			return;
		}
		if (!check.shown) {
			check.shown = true;
			this.showReadyCheckDialog(id, queue.getAttribute("queue"), check);
			this.PlaySound("gamestart");
		}
		this.tickReadyCheck(check);
	},

	showReadyCheckDialog: function (queueId, queueName, check) {
		const that = this;
		const content = $("<div class='hall-ready-check'></div>");
		content.append($("<p></p>").text("Ready Check started for the ").append($("<b></b>").text(queueName)).append(" tournament."));
		content.append($("<p></p>").text("Confirm you are present within ")
			.append($("<span class='hall-ready-check-secs'></span>"))
			.append(" seconds."));
		check.dialog = content;
		content.dialog({
			title: "Ready Check",
			modal: true,
			resizable: false,
			closeOnEscape: true,
			width: 360,
			closeText: "",
			buttons: [
				{
					text: "Ready",
					"class": "hall-ready-check-confirm",
					click: function () {
						that.confirmReadyCheck(queueId);
					}
				},
				{
					text: "OK",
					click: function () {
						$(this).dialog("close");
					}
				}
			],
			close: function () {
				if (check.timer != null)
					clearInterval(check.timer);
				check.timer = null;
				check.dialog = null;
				content.dialog("destroy").remove();
			}
		});
		check.timer = setInterval(function () {
			that.tickReadyCheck(check);
		}, 1000);
	},

	tickReadyCheck: function (check) {
		if (check.dialog != null)
			check.dialog.find(".hall-ready-check-secs").text(Math.max(0, Math.round((check.deadline - Date.now()) / 1000)));
	},

	confirmReadyCheck: function (queueId) {
		const that = this;
		const check = this.readyChecks[queueId];
		if (check != null)
			this.closeReadyCheckDialog(check);
		this.comm.confirmReadyCheckQueue(queueId, function (xml) {
			that.processResponse(xml);
		});
	},

	closeReadyCheckDialog: function (check) {
		if (check.dialog != null)
			check.dialog.dialog("close");
	},

	endReadyCheck: function (queueId) {
		const check = this.readyChecks[queueId];
		if (check != null) {
			this.closeReadyCheckDialog(check);
			delete this.readyChecks[queueId];
		}
	},

	// ---- "your game is ready" ----
	// Games and drafts open in a new window from the poll's callback, which has no user gesture, so pop-up blockers
	// often stop it.  When they do, a modal offers the same page behind a real link (a click is a gesture).  It stays
	// until used, or until its game or draft is over.

	openOrOffer: function (key, url, title, text, label) {
		let win = null;
		try {
			win = window.open(url, "_blank");
		} catch (e) {
			win = null;
		}
		if (win != null && !win.closed) {
			try {
				win.focus();
			} catch (e) {
				// focusing is best effort
			}
			return true;
		}
		this.showReadyModal(key, url, title, text, label);
		return false;
	},

	showReadyModal: function (key, url, title, text, label) {
		if (this.readyModals[key] != null)
			return;
		const that = this;
		const content = $("<div class='hall-ready-modal'></div>");
		content.append($("<p></p>").text(text));
		const link = $("<a class='hall-ready-open' target='_blank'></a>").attr("href", url).text(label);
		content.append($("<p class='hall-ready-actions'></p>").append(link));
		this.readyModals[key] = content;
		content.dialog({
			title: title,
			modal: true,
			resizable: false,
			closeOnEscape: false,
			width: 360,
			closeText: "",
			dialogClass: "hall-ready-dialog",
			open: function () {
				$(this).closest(".ui-dialog").find(".ui-dialog-titlebar-close").hide();
			},
			close: function () {
				if (that.readyModals[key] === content)
					delete that.readyModals[key];
				content.dialog("destroy").remove();
			}
		});
		link.button().on("click", function () {
			// after the browser has followed the link
			setTimeout(function () {
				that.closeReadyModal(key);
			}, 0);
		});
		link.trigger("focus");
	},

	closeReadyModal: function (key) {
		const modal = this.readyModals[key];
		if (modal != null) {
			delete this.readyModals[key];
			modal.dialog("close");
		}
	},

	// ---- tournaments ----

	drawTournament: function (tournament) {
		const that = this;
		const id = tournament.getAttribute("id");
		const action = tournament.getAttribute("action");

		if (action == "remove") {
			this.rowsFor("tournament", id).remove();
			this.closeReadyModal("draft:" + id);
			return;
		}
		if (action != "add" && action != "update")
			return;

		const isWC = tournament.getAttribute("wc") == "true";
		let type = tournament.getAttribute("type");
		if (type !== null)
			type = type.toLowerCase();
		let stage = tournament.getAttribute("stage");
		if (stage !== null)
			stage = stage.toLowerCase();
		const joinable = tournament.getAttribute("joinable") === "true";
		const abandoned = tournament.getAttribute("abandoned") === "true";
		const joined = tournament.getAttribute("signedUp") == "true";
		const tourneyName = tournament.getAttribute("name");
		const actionsField = $("<td></td>");

		const preDraftStages = ["deck-building", "registering decks", "awaiting kickoff"];
		let draftPage = null;
		if ((type === "solodraft") && preDraftStages.includes(stage))
			draftPage = "soloDraft.html";
		else if ((type === "table_solodraft" && preDraftStages.includes(stage)) || (type === "table_draft" && stage === "drafting"))
			draftPage = "tableDraft.html";
		const draftUrl = draftPage != null ? "/gemp-lotr/" + draftPage + "?eventId=" + encodeURIComponent(id) : null;

		if (joined) {
			this.inTournament = true;
			if (draftUrl != null) {
				actionsField.append($("<button type='button'>Go to Draft</button>").button().click(function () {
					const win = window.open(draftUrl, '_blank');
					if (win) {
						//Browser has allowed it to be opened
						win.focus();
					}
				}));
			}
			if((type === "sealed" || type === "solodraft" || type === "table_solodraft" || type === "table_draft")
				&& (preDraftStages.includes(stage) || stage === "paused between rounds")) {
				//Register Deck
				actionsField.append(this.tableJoiner.generateRegisterDeckButton(tournament));
			}

			actionsField.append($("<button type='button'>Abandon Tournament</button>").button().click(function () {
				const isExecuted = confirm("Are you sure you want to resign from the " + tourneyName + " tournament? This cannot be undone.");
				if (isExecuted) {
					that.comm.dropFromTournament(id, function (xml) {
						// a refusal (still playing a match, already dropped...) comes back as an <error>
						if (that.processResponse(xml))
							that.inTournament = false;
					});
				}
			}).css("color", "#d44"));
		} else if (!abandoned && joinable) {
			actionsField.append(this.tableJoiner.generateLateJoinButton(tournament));
		}

		// Row for tournament playing table
		const displayType = GempLotrHallUI.displayType(type);
		const playerList = tournament.getAttribute("playerList") || "";
		const playerCount = +tournament.getAttribute("playerCount");
		const tablesRow = this.newRow("tournament", id);
		tablesRow.append(GempLotrHallUI.cell(tournament.getAttribute("format")));
		let label;
		if (isWC) {
			label = "World Championship - ";
		} else if (playerCount == 2) {
			label = "Tournament - ";
		} else if (!playerList.includes(',')) {
			label = "Practice - ";
		} else {
			label = tournament.getAttribute("system") + " Tournament - ";
		}
		tablesRow.append(GempLotrHallUI.cell(label + displayType + " - " + tourneyName));
		const rawStage = tournament.getAttribute("stage");
		if (tournament.hasAttribute("timeRemaining")) {
			tablesRow.append(GempLotrHallUI.cell(rawStage + " - " + tournament.getAttribute("timeRemaining")));
		} else if (rawStage === "Playing Games") {
			tablesRow.append(GempLotrHallUI.cell(rawStage + " - Round " + tournament.getAttribute("round")));
		} else {
			tablesRow.append(GempLotrHallUI.cell(rawStage));
		}
		if (playerCount <= 8) {
			tablesRow.append(GempLotrHallUI.cell(playerList));
		} else {
			tablesRow.append($("<td></td>").append(GempLotrHallUI.hintButton("prizeHint", String(playerCount), "Competing Players",
				GempLotrHallUI.escapeHtml(playerList) + "<br><br>* = abandoned")));
		}
		tablesRow.append(actionsField);
		if (joined) {
			tablesRow.addClass("played");
		}
		if (isWC) {
			tablesRow.addClass("bold");
		}

		if (action == "add") {
			// Display running tournaments as playing tables
			$("table.playingTables", this.tablesDiv).append(tablesRow);

			// Open draft window
			if (joined && draftUrl != null) {
				this.openOrOffer("draft:" + id, draftUrl, "Your draft is ready",
					"Your draft has started, but your browser blocked the new window.", "Open draft");
				this.PlaySound("gamestart");
			}
		} else {
			// Update row in playing tables section
			const existingRow = this.rowsFor("tournament", id);
			if (existingRow.length > 0) {
				existingRow.replaceWith(tablesRow);
			} else {
				$("table.playingTables", this.tablesDiv).append(tablesRow);
			}
		}
		if (draftUrl == null)
			this.closeReadyModal("draft:" + id);

		this.animateRowUpdate(tablesRow);
	},

	// ---- game tables ----

	drawTable: function (table) {
		const that = this;
		const id = table.getAttribute("id");
		const action = table.getAttribute("action");

		if (action == "remove") {
			this.rowsFor("table", id).remove();
			if (this.tableGames[id] != null)
				this.closeReadyModal("game:" + this.tableGames[id]);
			delete this.tableGames[id];
			return;
		}
		if (action != "add" && action != "update")
			return;

		const status = table.getAttribute("status");
		const gameId = table.getAttribute("gameId");
		const statusDescription = table.getAttribute("statusDescription");
		const watchable = table.getAttribute("watchable");
		const playersAttr = table.getAttribute("players") || "";
		const formatName = table.getAttribute("format");
		const tournamentName = table.getAttribute("tournament");
		const userDesc = table.getAttribute("userDescription");
		const isPrivate = (table.getAttribute("isPrivate") === "true");
		const isInviteOnly = (table.getAttribute("isInviteOnly") === "true");
		// computed by the server for this viewer: the invitee is named in the description
		const inviteForYou = (table.getAttribute("invitedYou") === "true");
		const players = playersAttr.length > 0 ? playersAttr.split(",") : [];
		const playing = table.getAttribute("playing");
		const winner = table.getAttribute("winner");

		if (gameId != null)
			this.tableGames[id] = gameId;

		const row = this.newRow("table", id);

		row.append(GempLotrHallUI.cell(formatName));
		const info = $("<td></td>").text(tournamentName);
		let note = null;
		if (userDesc) {
			if (isInviteOnly)
				note = (isPrivate ? "Private match for user '" : "Match for user '") + userDesc + "'.";
			else
				note = isPrivate ? "Private match: [" + userDesc + "]" : "[" + userDesc + "]";
		} else if (isPrivate) {
			note = "Private.";
		}
		if (note != null)
			info.append(" - ", $("<i></i>").text(note));
		row.append(info);

		// "Waiting 01:30": a waiting table's timer, ticking every second on the server clock
		const statusCell = GempLotrHallUI.cell(statusDescription);
		const createdAt = parseInt(table.getAttribute("createdAt"), 10);
		if (status == "WAITING" && !isNaN(createdAt)) {
			statusCell.append(" ", $("<span class='table-age'></span>").attr("data-created-at", createdAt));
			this.renderTableAge(statusCell.find(".table-age"));
		}
		row.append(statusCell);

		row.append(GempLotrHallUI.cell(players.join(", ")));

		const lastField = $("<td></td>");
		if (status == "WAITING") {
			if (playing == "true") {
				lastField.append($("<button type='button'>Leave Table</button>").button().click(function () {
					that.comm.leaveTable(id);
				}));
			} else if (!isInviteOnly || inviteForYou) {
				//Join Table
				lastField.append(this.tableJoiner.generateJoinButton(id));
			}
		} else if (status == "PLAYING") {
			if (playing == "true" || watchable == "true") {
				lastField.append($("<a class='hall-row-link' target='_blank'></a>")
					.attr("href", "game.html?gameId=" + encodeURIComponent(gameId) + this.participantIdParam())
					.text(playing == "true" ? "Play Match" : "Spectate")
					.button());
			}
		} else if (status == "FINISHED") {
			if (winner != null) {
				lastField.text(winner);
			}
			this.closeReadyModal("game:" + gameId);
		}

		row.append(lastField);

		const sections = {WAITING: "table.waitingTables", PLAYING: "table.playingTables", FINISHED: "table.finishedTables"};
		const section = sections[status];
		if (action == "add") {
			if (section != null)
				$(section, this.tablesDiv).append(row);
		} else if (section != null) {
			const existing = this.rowsFor("table", id);
			if (this.rowsFor("table", id, $(section, this.tablesDiv)).length > 0) {
				existing.replaceWith(row);
			} else {
				existing.remove();
				$(section, this.tablesDiv).append(row);
			}

			this.animateRowUpdate(row);
		}

		if (playing == "true")
			row.addClass("played");

		if(inviteForYou)
			row.addClass("privateForPlayer");
	},

	// "01:30", from createdAt against the server clock (our clock plus the offset from the last poll), so a wrong
	// clock on this computer does not show.
	renderTableAge: function (span) {
		const createdAt = +span.attr("data-created-at");
		const ageMs = Date.now() + this.serverClockOffset - createdAt;
		span.text(GempLotrHallUI.formatAge(ageMs));
		if (span.attr("data-title-for") !== String(createdAt)) {
			span.attr("data-title-for", createdAt);
			span.attr("title", "Open since " + HallConnectionIndicator.formatServerTime(createdAt) + " (server time)");
		}
	},

	refreshTableAges: function () {
		const that = this;
		$(".table-age", this.tablesDiv).each(function () {
			that.renderTableAge($(this));
		});
	}
});

// ---- helpers (static) ------------------------------------------------------------------------------------------------

GempLotrHallUI.escapeHtml = function (value) {
	return String(value == null ? "" : value)
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#39;");
};

// A <td> holding a server-provided string as text.
GempLotrHallUI.cell = function (text) {
	return $("<td></td>").text(text == null ? "" : text);
};

// A hint (.prizeHint / .cardHint / .draftFormatInfo) as a real button; GameHall.js opens it.  `value` must already be
// safe markup (it is shown with .html()).
GempLotrHallUI.hintButton = function (hintClass, text, title, value) {
	const button = $("<button type='button'></button>").addClass(hintClass).text(text);
	if (title != null)
		button.attr("title", title);
	if (value != null)
		button.attr("value", value);
	return button;
};

// An event's name; a table draft's name is a button that opens its draft format.
GempLotrHallUI.eventName = function (name, draftCode) {
	if (draftCode == null)
		return document.createTextNode(name == null ? "" : name);
	return GempLotrHallUI.hintButton("draftFormatInfo", name, "Show the draft format").attr("draftCode", draftCode);
};

GempLotrHallUI.displayType = function (type) {
	if (type == null)
		return "";
	const names = {
		sealed: "Sealed",
		solodraft: "Solo Draft",
		table_solodraft: "Solo Table Draft",
		table_draft: "Table Draft",
		constructed: "Constructed"
	};
	const lower = type.toLowerCase();
	return names[lower] != null ? names[lower] : lower;
};

// A queue's prize description is plain text ("No prizes") or a fixed
// <div class='prizeHint' value='<ul><li>…</li></ul>'>Prize Breakdown</div> built by the server.  It is parsed inertly
// (DOMParser runs no scripts and loads nothing) and only its text is kept: {label, items}.
GempLotrHallUI.parsePrizes = function (markup) {
	const result = {label: "", items: []};
	if (markup == null)
		return result;
	const trimmed = String(markup).trim();
	if (trimmed.charAt(0) !== "<") {
		result.label = trimmed;
		return result;
	}
	const parser = new DOMParser();
	const body = parser.parseFromString(trimmed, "text/html").body;
	const hint = body.firstElementChild;
	result.label = (hint != null ? hint.textContent : body.textContent).trim();
	const value = hint != null ? hint.getAttribute("value") : null;
	if (value) {
		const inner = parser.parseFromString(value, "text/html").body;
		$(inner).find("li").each(function () {
			const text = $(this).text().replace(/\s+/g, " ").trim();
			if (text)
				result.items.push(text);
		});
		if (result.items.length === 0 && inner.textContent.trim())
			result.items.push(inner.textContent.replace(/\s+/g, " ").trim());
	}
	return result;
};

// The Prizes cell of a queue row: the label, as a hint button when there is a breakdown behind it.
GempLotrHallUI.prizeCell = function (prizes) {
	if (prizes.items.length === 0)
		return document.createTextNode(prizes.label);
	const list = "<ul>" + prizes.items.map(function (item) {
		return "<li>" + GempLotrHallUI.escapeHtml(item) + "</li>";
	}).join("") + "</ul>";
	return GempLotrHallUI.hintButton("prizeHint", prizes.label || "Prize Breakdown", "Prizes details", list);
};

// How long a table has waited, as a timer: "00:07", "01:30", "59:59", then "1:00:00", "26:05:09" (hours keep counting).
GempLotrHallUI.formatAge = function (ageMs) {
	const total = Math.floor(Math.max(0, ageMs) / 1000);
	const two = function (n) {
		return (n < 10 ? "0" : "") + n;
	};
	const hours = Math.floor(total / 3600);
	const minutes = Math.floor(total / 60) % 60;
	const seconds = total % 60;
	return (hours > 0 ? hours + ":" + two(minutes) : two(minutes)) + ":" + two(seconds);
};

// Server text (an <error>/<response> message, a tournament announcement) as safe markup: it is escaped, so anything
// in it shows as the text it is, and then only the bare formatting tags the server itself writes (<b>, <strong>, <i>,
// <em>, <u> and <br>, with no attributes) are turned back into tags.
GempLotrHallUI.serverMarkup = function (text) {
	if (text == null)
		return "";
	return GempLotrHallUI.escapeHtml(text)
		.replace(/&lt;(\/?)(b|strong|i|em|u)&gt;/gi, function (match, slash, tag) {
			return "<" + slash + tag.toLowerCase() + ">";
		})
		.replace(/&lt;br\s*\/?&gt;/gi, "<br>");
};
