/**
 * Play ▸ Scenarios: tutorials, puzzles and sandboxes, each a solo game against a scripted opponent (GET /scenarios).
 *
 * The list is fetched every time the form is shown, grouped by kind, with the player's progress on each scenario.
 * Starting one (POST /scenarios/start) answers with a game id and the game opens in its own window, as every other
 * game does.  The window is opened on the click itself and pointed at the game once the server has answered: a
 * window opened from the answer has no user gesture behind it and pop-up blockers stop it.  If it is blocked anyway,
 * the hall offers the game behind a link (hallUi.showReadyModal).
 *
 * A scenario game the player is still in the middle of is offered first, to carry on with or to start over.
 * Starting any scenario gives up the one in progress (the server does that), so the buttons say so.
 */
class PlayScenario extends TableFlow {
	listDiv = null;
	currentDiv = null;
	//What GET /scenarios last answered
	scenarios = [];
	current = null;
	//True from a click on a start button until the server has answered
	starting = false;

	// as GempScenarioOverlay.MIN_WIDTH / MIN_HEIGHT
	static MIN_WIDTH = 1000;
	static MIN_HEIGHT = 600;

	static KINDS = [
		{kind: "tutorial", heading: "Tutorials"},
		{kind: "puzzle", heading: "Puzzles"},
		{kind: "sandbox", heading: "Sandboxes"}
	];

	constructor(mainHall, comm, div) {
		super(mainHall, comm, div, null, null, $("#scenario-result"));
		this.listDiv = $("#scenario-list");
		this.currentDiv = $("#scenario-current");
	}

	hide() {
		this.mainDiv.hide();
	}

	show() {
		this.mainDiv.show();
		//The game opens in a window like this one, and a tutorial's bubbles need room to point at things (the game
		// screen makes the same check and says so again)
		$("#scenario-small-window").prop("hidden",
			!(window.innerWidth < PlayScenario.MIN_WIDTH || window.innerHeight < PlayScenario.MIN_HEIGHT));
		this.refresh();
	}

	refresh() {
		var that = this;
		if (this.scenarios.length == 0)
			this.listDiv.empty().append($("<div class='scenario-empty'></div>").text("Loading…"));
		this.comm.getScenarios(function (data) {
			that.scenarios = (data && data.scenarios) || [];
			that.current = (data && data.current) || null;
			that.render();
		}, TableFlow.errorMap(function (message) {
			that.listDiv.empty().append($("<div class='scenario-empty'></div>").text("The list of scenarios could not be loaded."));
			that.showResult(message, "error");
		}));
	}

	byId(id) {
		for (const scenario of this.scenarios)
			if (scenario.id == id)
				return scenario;
		return null;
	}

	render() {
		var that = this;
		this.renderCurrent();
		this.listDiv.empty();
		if (this.scenarios.length == 0) {
			this.listDiv.append($("<div class='scenario-empty'></div>").text("There are no scenarios to play yet."));
			return;
		}

		var known = {};
		var groups = PlayScenario.KINDS.slice();
		for (const group of groups)
			known[group.kind] = true;
		//A kind this page has not heard of still gets listed
		for (const scenario of this.scenarios) {
			if (!known[scenario.kind]) {
				known[scenario.kind] = true;
				groups.push({kind: scenario.kind, heading: scenario.kind});
			}
		}

		for (const group of groups) {
			var members = this.scenarios.filter(function (scenario) { return scenario.kind == group.kind; });
			if (members.length == 0)
				continue;
			this.listDiv.append($("<h2 class='scenario-kind'></h2>").text(group.heading));
			for (const scenario of members)
				this.listDiv.append(that.row(scenario));
		}
	}

	// The game still in progress, if there is one: carry on with it, or start it again.
	renderCurrent() {
		var that = this;
		this.currentDiv.empty();
		if (this.current == null) {
			this.currentDiv.prop("hidden", true);
			return;
		}
		var scenario = this.byId(this.current.id);
		var title = scenario != null ? scenario.title : this.current.id;
		var gameId = this.current.gameId;
		this.currentDiv.append($("<div class='scenario-current-text'></div>")
			.append(document.createTextNode("You are in the middle of "))
			.append($("<b></b>").text(title))
			.append(document.createTextNode(".")));
		this.currentDiv.append($("<button class='scenario-continue'></button>").text("Continue").button().click(function () {
			that.open(that.reserveWindow(), gameId);
			that.mainHall.tableCreator.closeAfterSuccess();
		}));
		this.currentDiv.prop("hidden", false);
	}

	row(scenario) {
		var that = this;
		var row = $("<div class='scenario-row'></div>").attr("data-scenario", scenario.id);
		var text = $("<div class='scenario-text'></div>");
		var title = $("<div class='scenario-title'></div>").append($("<span></span>").text(scenario.title));
		var status = PlayScenario.statusOf(scenario);
		if (status != null)
			title.append($("<span class='scenario-status'></span>").addClass("scenario-status-" + status.kind).text(status.text));
		text.append(title);
		if (scenario.summary)
			text.append($("<div class='scenario-summary'></div>").text(scenario.summary));

		var notes = [];
		if (!scenario.canonical)
			notes.push(scenario.author ? "By " + scenario.author : "Not an official scenario");
		if (!scenario.verified)
			notes.push("Not verified: this scenario does not play through from start to finish yet");
		if (scenario.progress != null && scenario.progress.attempts > 1)
			notes.push("Played " + scenario.progress.attempts + " times");
		if (notes.length > 0)
			text.append($("<div class='scenario-notes'></div>").text(notes.join(" · ")));
		row.append(text);

		var inProgress = this.current != null && this.current.id == scenario.id;
		var label = inProgress ? "Start Over" : (scenario.progress != null ? "Play Again" : "Play");
		row.append($("<button class='scenario-play'></button>").text(label).button().click(function () {
			that.start(scenario);
		}));
		return row;
	}

	// {kind, text} for the badge next to a title; null when the scenario has not been played.
	static statusOf(scenario) {
		var progress = scenario.progress;
		if (progress == null)
			return null;
		if (progress.status == "complete") {
			//Finished, but the scenario has been revised since
			if (progress.version != null && scenario.version != null && progress.version < scenario.version)
				return {kind: "updated", text: "Completed · updated since"};
			return {kind: "complete", text: "Completed"};
		}
		return {kind: "started", text: "Started"};
	}

	// A window opened while the click is still a user gesture; null if the browser will not give one.
	reserveWindow() {
		var win = null;
		try {
			win = window.open("", "_blank");
		} catch (e) {
			win = null;
		}
		if (win == null || win.closed)
			return null;
		try {
			win.document.title = "Gemp-LotR";
			win.document.body.style.background = "#111";
		} catch (e) {
			// only cosmetic
		}
		return win;
	}

	open(win, gameId) {
		var url = "/gemp-lotr/game.html?gameId=" + encodeURIComponent(gameId) + this.mainHall.participantIdParam();
		if (win != null && !win.closed) {
			win.location.href = url;
			try {
				win.focus();
			} catch (e) {
				// focusing is best effort
			}
			return;
		}
		this.mainHall.showReadyModal("game:" + gameId, url, "Your game is ready",
			"Your game has started, but your browser blocked the new window.", "Open game");
	}

	start(scenario) {
		var that = this;
		if (this.starting)
			return;
		this.starting = true;
		//Continue as well: the game it would open is the one this start gives up
		this.mainDiv.find(".scenario-play, .scenario-continue").button("disable");
		this.showResult("Setting up the table…", "pending");
		var win = this.reserveWindow();

		var finished = function () {
			that.starting = false;
			that.mainDiv.find(".scenario-play, .scenario-continue").button("enable");
		};
		this.comm.startScenario(scenario.id, false, function (result) {
			finished();
			if (result == null || result.gameId == null) {
				if (win != null)
					win.close();
				that.showResult((result && result.error) || "The scenario could not be started.", "error");
				return;
			}
			that.showResult("Started.", "success");
			that.open(win, result.gameId);
			that.mainHall.tableCreator.closeAfterSuccess();
		}, function (xhr) {
			finished();
			if (win != null)
				win.close();
			var message = TableFlow.errorDetail(xhr);
			if (message == null) {
				var status = (xhr && xhr.status != null) ? xhr.status : 0;
				message = status == 0 ? "Could not reach the server. Check your internet connection and try again."
					: "The scenario could not be started (HTTP " + status + ").";
			}
			that.showResult(message, "error");
			//The list may be what was out of date
			that.refresh();
		});
	}
}
