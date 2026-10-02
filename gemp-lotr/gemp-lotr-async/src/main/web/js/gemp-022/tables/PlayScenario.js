/**
 * Play ▸ Scenarios: tutorials, puzzles and sandboxes, each a solo game against a scripted opponent (GET /scenarios).
 *
 * The list is fetched every time the form is shown.  On the left are the folders the scenarios are kept in; on the
 * right, the scenarios of the folder that is selected and of the folders inside it, with the player's progress on
 * each.  The top level is by kind (Tutorials, Puzzles, Sandboxes) and then the player's own scenarios; below that
 * the folders are the directories the files are in.
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
	foldersDiv = null;
	currentDiv = null;
	//The folder being looked at, as its names joined with "/"; null until the list has arrived
	selectedFolder = null;
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
		this.foldersDiv = $("#scenario-folders");
		this.currentDiv = $("#scenario-current");
		this.selectedFolder = TableFlow.cookie("scenario-folder");
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

	// "01-first-steps" -> "First steps": a folder's name as a person would write it
	static prettyFolder(name) {
		var text = String(name).replace(/^\d+[-_ .]+/, "").replace(/[-_]+/g, " ").trim();
		return text.length == 0 ? String(name) : text.charAt(0).toUpperCase() + text.slice(1);
	}

	// The folder a scenario is shown in, as a list of names from the top down.
	static folderOf(scenario) {
		var dirs = String(scenario.path || "").split("/").slice(0, -1);
		if (scenario.owner != null) {
			//A player's files give their place within that player's own folder
			dirs = dirs.map(PlayScenario.prettyFolder);
			return scenario.mine ? ["My scenarios"].concat(dirs) : ["Other players", scenario.owner].concat(dirs);
		}
		var heading = scenario.kind;
		for (const known of PlayScenario.KINDS)
			if (known.kind == scenario.kind)
				heading = known.heading;
		//Files already sorted into tutorials/, puzzles/ or sandboxes/ are not put in a second folder of that name
		if (dirs.length > 0 && [scenario.kind, scenario.kind + "s", scenario.kind + "es"].indexOf(dirs[0].toLowerCase()) > -1)
			dirs = dirs.slice(1);
		return [PlayScenario.prettyFolder(heading)].concat(dirs.map(PlayScenario.prettyFolder));
	}

	// {name, key, children: [...], scenarios: [...]} for every folder that has a scenario in or under it.
	buildTree() {
		var root = {name: null, key: "", children: [], scenarios: []};
		var sorted = this.scenarios.slice().sort(function (a, b) {
			return String(a.path || "").localeCompare(String(b.path || ""));
		});
		for (const scenario of sorted) {
			var node = root;
			for (const name of PlayScenario.folderOf(scenario)) {
				var child = null;
				for (const candidate of node.children)
					if (candidate.name == name)
						child = candidate;
				if (child == null) {
					child = {name: name, key: node.key == "" ? name : node.key + "/" + name, children: [], scenarios: []};
					node.children.push(child);
				}
				node = child;
			}
			node.scenarios.push(scenario);
		}
		//The kinds first, in their usual order; then the player's own; then everybody else's (admins see those)
		var order = PlayScenario.KINDS.map(function (known) { return known.heading; });
		var rank = function (node) {
			var index = order.indexOf(node.name);
			if (index > -1)
				return index;
			return node.name == "My scenarios" ? 100 : node.name == "Other players" ? 101 : 50;
		};
		root.children.sort(function (a, b) { return rank(a) - rank(b); });
		return root;
	}

	static countIn(node) {
		var count = node.scenarios.length;
		for (const child of node.children)
			count += PlayScenario.countIn(child);
		return count;
	}

	static findFolder(node, key) {
		if (node.key == key)
			return node;
		for (const child of node.children) {
			var found = PlayScenario.findFolder(child, key);
			if (found != null)
				return found;
		}
		return null;
	}

	render() {
		this.renderCurrent();
		this.foldersDiv.empty();
		this.listDiv.empty();
		if (this.scenarios.length == 0) {
			this.foldersDiv.hide();
			this.listDiv.append($("<div class='scenario-empty'></div>").text("There are no scenarios to play yet."));
			return;
		}

		var tree = this.buildTree();
		var selected = this.selectedFolder == null ? null : PlayScenario.findFolder(tree, this.selectedFolder);
		//Nothing chosen yet, or the folder chosen last time is gone: the first one
		if (selected == null || selected === tree)
			selected = tree.children[0];
		this.selectedFolder = selected.key;

		this.foldersDiv.show();
		this.renderFolders(tree, 0);
		this.renderFolder(selected, selected, true);
	}

	renderFolders(node, depth) {
		var that = this;
		for (const child of node.children) {
			var button = $("<button type='button' class='scenario-folder' role='treeitem'></button>")
				.css("padding-left", (0.6 + depth * 1.1) + "em")
				.attr("aria-selected", child.key == this.selectedFolder ? "true" : "false")
				.toggleClass("scenario-folder-selected", child.key == this.selectedFolder)
				.append($("<span class='scenario-folder-name'></span>").text(child.name))
				.append($("<span class='scenario-folder-count'></span>").text(PlayScenario.countIn(child)));
			button.click(function () {
				that.selectedFolder = child.key;
				$.cookie("scenario-folder", child.key, {expires: 365});
				that.render();
			});
			this.foldersDiv.append(button);
			this.renderFolders(child, depth + 1);
		}
	}

	// The scenarios in a folder, then those of each folder inside it under that folder's name.
	renderFolder(node, top, first) {
		if (node !== top && node.scenarios.length > 0)
			this.listDiv.append($("<h2 class='scenario-kind'></h2>").text(node.key.substring(top.key.length + 1).split("/").join(" › ")));
		else if (first)
			this.listDiv.append($("<h2 class='scenario-kind'></h2>").text(top.key.split("/").join(" › ")));
		for (const scenario of node.scenarios)
			this.listDiv.append(this.row(scenario));
		for (const child of node.children)
			this.renderFolder(child, top, false);
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
