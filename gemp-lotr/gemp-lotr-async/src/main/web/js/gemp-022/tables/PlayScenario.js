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
 *
 * Whoever may write scenarios (the list says: mayAuthor) also gets an Edit button on each, and a way to start a new
 * one.  Either opens a game with the authoring panel beside it (see scenario/scenarioAuthor.js); an author has one
 * draft at a time, and its game is the one "in the middle of" above.
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
	//Whether this player may write scenarios, and the draft they have open: {gameId, id, title, over, test} (null if
	// they have none)
	mayAuthor = false;
	authoring = null;
	authorDiv = null;

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
		this.authorDiv = $("#scenario-author");
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
			that.mayAuthor = !!(data && data.mayAuthor);
			that.authoring = (data && data.authoring) || null;
			that.ownFolder = !!(data && data.ownFolder);
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
		this.renderAuthor();
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
		//(a draft being written has its own line, with its own way back: see renderAuthor)
		if (this.authoring != null && String(this.authoring.gameId) == String(gameId)) {
			this.currentDiv.prop("hidden", true);
			return;
		}
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

		if (this.mayAuthor)
			row.append($("<button class='scenario-edit'></button>").attr("title", "Open it in the authoring tool").text("Edit").button().click(function () {
				that.author({id: scenario.id});
			}));

		var inProgress = this.current != null && this.current.id == scenario.id;
		var label = inProgress ? "Start Over" : (scenario.progress != null ? "Play Again" : "Play");
		row.append($("<button class='scenario-play'></button>").text(label).button().click(function () {
			that.start(scenario);
		}));
		return row;
	}

	// For those who may write scenarios: a new one, by id and title.
	renderAuthor() {
		var that = this;
		this.authorDiv.empty();
		if (!this.mayAuthor) {
			this.authorDiv.prop("hidden", true);
			return;
		}
		if (this.authoring != null) {
			//The draft is on the server whatever has become of the game it was being written in
			var draft = this.authoring;
			this.authorDiv.append($("<span class='scenario-author-draft'></span>")
				.append(document.createTextNode("You have a draft open: "))
				.append($("<b></b>").text(draft.title || draft.id))
				.append(document.createTextNode((draft.test ? " (being played as a player would)" : "")
					+ (draft.dirty ? ", with changes that have not been saved." : "."))));
			this.authorDiv.append($("<button class='scenario-continue'></button>").text("Continue writing").button().click(function () {
				that.resumeDraft();
			}));
			this.authorDiv.append($("<button class='scenario-close-draft'></button>").text("Close it").button().click(function () {
				if (draft.dirty !== false && !confirm("Close the draft? It has changes that have not been saved, and they are lost."))
					return;
				that.comm.scenarioAuthor("POST", "close", {}, function () {
					that.refresh();
				}, function () {
					that.refresh();
				});
			}));
			this.authorDiv.append($("<span class='scenario-author-break'></span>"));
		}
		//The id is what the scenario is known by, and what its file is called: it is asked once, here.  Left empty,
		// it is made from the title.
		var id = $("<input type='text' maxlength='60'/>").attr("placeholder", "id: basics-02-the-ring").attr("aria-label", "The new scenario's id")
			.attr("title", "What the scenario is known by, and its file's name: small letters, digits and dashes. Empty: made from the title.");
		var title = $("<input type='text' maxlength='80'/>").attr("placeholder", "Title").attr("aria-label", "The new scenario's title");
		var slug = function (text) {
			return String(text).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").substring(0, 60);
		};
		title.bind("input", function () {
			id.attr("placeholder", slug(title.val()) != "" ? "id: " + slug(title.val()) : "id: basics-02-the-ring");
		});
		this.authorDiv.append($("<span></span>").text("Write a scenario:")).append(id).append(title);
		//Where its file goes, for a server that has somewhere besides its own scenarios to keep one.  What was chosen
		// last time is what is offered next time.
		var where = null;
		if (this.ownFolder) {
			var last = null;
			try {
				last = localStorage.getItem("gemp-scenario-new-where");
			} catch (e) {
				last = null;
			}
			where = $("<select></select>").attr("aria-label", "Where the new scenario's file is kept")
				.attr("title", "Where its file is kept. One of the game's own goes in the scenarios folder, under tutorials/; your own is offered only to you and the admins.")
				.append($("<option value='own'></option>").text("in my own folder"))
				.append($("<option value='canonical'></option>").text("with the game's own"))
				.val(last == "canonical" ? "canonical" : "own");
			this.authorDiv.append(where);
		}
		this.authorDiv.append($("<button class='scenario-new'></button>").text("New").button().click(function () {
			var canonical = where == null || where.val() == "canonical";
			try {
				localStorage.setItem("gemp-scenario-new-where", canonical ? "canonical" : "own");
			} catch (e) {
				// only a convenience
			}
			var name = String(id.val()).trim() != "" ? String(id.val()).trim() : slug(title.val());
			that.author({"new": true, id: name, title: String(title.val()).trim(), canonical: canonical});
		}));
		this.authorDiv.prop("hidden", false);
	}

	// Back to the draft: the game it is being written in if that is still going, and a fresh one (played by the script
	// to where the last one had got) if it is not.
	resumeDraft() {
		var that = this;
		if (this.starting)
			return;
		this.starting = true;
		this.showResult("Setting up the table…", "pending");
		var win = this.reserveWindow();
		var failed = function (xhr) {
			that.starting = false;
			if (win != null)
				win.close();
			var message = null;
			try {
				message = JSON.parse(xhr.responseText).error;
			} catch (e) {
				message = null;
			}
			that.showResult(message || "The draft could not be gone back to.", "error");
			that.refresh();
		};
		this.comm.scenarioAuthor("POST", "resume", {}, function (result) {
			that.starting = false;
			that.showResult("Opened.", "success");
			that.open(win, result.gameId);
			that.mainHall.tableCreator.closeAfterSuccess();
		}, failed);
	}

	// Opens a scenario (or a new one) in the authoring tool: a game with the authoring panel beside it.
	author(what) {
		var that = this;
		if (this.starting)
			return;
		//(a draft with nothing unsaved in it is given up without a word)
		if (this.authoring != null && this.authoring.dirty !== false
			&& !confirm("You have a draft open with changes that have not been saved. Opening another drops it, and they are lost."))
			return;
		this.starting = true;
		this.showResult("Setting up the table…", "pending");
		var win = this.reserveWindow();
		this.comm.scenarioAuthor("POST", "open", what, function (result) {
			that.starting = false;
			that.showResult("Opened.", "success");
			that.open(win, result.gameId);
			that.mainHall.tableCreator.closeAfterSuccess();
		}, function (xhr) {
			that.starting = false;
			if (win != null)
				win.close();
			var message = null;
			try {
				message = JSON.parse(xhr.responseText).error;
			} catch (e) {
				message = null;
			}
			that.showResult(message || "The scenario could not be opened for writing (HTTP " + ((xhr && xhr.status) || 0) + ").", "error");
		});
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
