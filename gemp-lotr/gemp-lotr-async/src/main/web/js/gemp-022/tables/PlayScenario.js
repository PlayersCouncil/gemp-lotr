/**
 * Play ▸ Play Scenario or Tutorial: tutorials, puzzles and sandboxes, each a solo game against a scripted opponent
 * (GET /scenarios).
 *
 * The list is fetched every time the form is shown.  On the left are the folders the scenarios are kept in; on the
 * right, the scenarios of the folder that is selected and of the folders inside it, with the player's progress on
 * each.  The top level is by kind (Tutorials, Puzzles, Sandboxes) and then the player's own scenarios, which are
 * divided by kind in the same way; below that the folders are the directories the files are in.
 * Starting one (POST /scenarios/start) answers with a game id and the game opens in its own window, as every other
 * game does.  The window is opened on the click itself and pointed at the game once the server has answered: a
 * window opened from the answer has no user gesture behind it and pop-up blockers stop it.  If it is blocked anyway,
 * the hall offers the game behind a link (hallUi.showReadyModal).
 *
 * A scenario game the player is still in the middle of is offered first, to carry on with or to start over.
 * Starting any scenario gives up the one in progress (the server does that), so the buttons say so.
 *
 * Whoever may write scenarios (the list says: mayAuthor) gets, under the list, a link to My Account > My Scenarios,
 * where scenarios are written, copied and submitted (scenario/scenarioShelf.js).  A file with mistakes in it that the
 * authoring tool can open all the same is offered for editing in the list of those (unreadable).  An author has one
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
	//The scenarios that are next in their campaigns, {id: true}; null until asked for (see nextIds)
	next = null;
	//Scenario files with mistakes in them, which are in no list: [{path, owner, mine, id, title, errors: [...],
	// openable}].  The server sends an admin the public ones and everybody their own.  'openable' ones can be opened
	// in the authoring tool all the same, with the mistaken parts left out.
	unreadable = [];
	unreadableDiv = null;
	//What the Result box was last made to say about them (null if it says something else)
	unreadableSaid = null;

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
		this.unreadableDiv = $("#scenario-unreadable");
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
			that.unreadable = (data && data.unreadable) || [];
			that.render();
			that.sayUnreadable();
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

	// The folder a scenario is shown in, as a list of names from the top down.  Its kind is the first division,
	// among the public scenarios and within a player's own alike; the folders its file is in come below that.
	static folderOf(scenario) {
		var dirs = String(scenario.path || "").split("/").slice(0, -1);
		var heading = scenario.kind;
		for (const known of PlayScenario.KINDS)
			if (known.kind == scenario.kind)
				heading = known.heading;
		//Files are kept under tutorials/, puzzles/ or sandboxes/ (the authoring tool sees to it): that folder is the
		// division itself, not a folder inside it
		if (dirs.length > 0 && [scenario.kind, scenario.kind + "s", scenario.kind + "es"].indexOf(dirs[0].toLowerCase()) > -1)
			dirs = dirs.slice(1);
		var within = [PlayScenario.prettyFolder(heading)].concat(dirs.map(PlayScenario.prettyFolder));
		if (scenario.owner == null)
			return within;
		return (scenario.mine ? ["My scenarios"] : ["Other players", scenario.owner]).concat(within);
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
		var sort = function (node) {
			//(stable: folders that are not kinds stay as their paths ordered them)
			node.children.sort(function (a, b) { return rank(a) - rank(b); });
			node.children.forEach(sort);
		};
		sort(root);
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

	// The files with mistakes in them, each with what is wrong with it.  A file like that is in no list, so without
	// this it would simply be gone.  Most can still be opened in the authoring tool, which leaves the mistaken parts
	// out and says what they were; one that cannot (it is not HJSON, it has no title) has to be put right by hand.
	renderUnreadable() {
		var that = this;
		this.unreadableDiv.empty();
		if (this.unreadable.length == 0) {
			this.unreadableDiv.prop("hidden", true);
			return;
		}
		this.unreadableDiv.prop("hidden", false);
		var count = this.unreadable.length;
		this.unreadableDiv.append($("<div class='scenario-unreadable-heading'></div>").text(
			(count == 1 ? "1 scenario file has" : count + " scenario files have") + " mistakes in "
			+ (count == 1 ? "it" : "them") + ", and cannot be played:"));
		for (const file of this.unreadable) {
			var row = $("<div class='scenario-unreadable-file'></div>");
			var text = $("<div class='scenario-unreadable-text'></div>").appendTo(row);
			text.append($("<code></code>").text(file.path));
			var errors = file.errors || [];
			if (errors.length == 0)
				errors = ["It could not be read."];
			var list = $("<ul></ul>");
			for (const error of errors)
				list.append($("<li></li>").text(error));
			text.append(list);
			if (file.openable && file.id != null) {
				let id = file.id;
				row.append($("<button class='scenario-edit'></button>")
					.attr("title", "Open it in the authoring tool, with the mistaken parts left out. The tool shows what they were.")
					.text("Edit").button().click(function () {
						that.author({id: id});
					}));
			} else if (this.mayAuthor) {
				text.append($("<div class='scenario-unreadable-note'></div>").text("The authoring tool cannot open this one: the file itself has to be put right."));
			}
			this.unreadableDiv.append(row);
		}
	}

	// The same in the Result box, in a line.  It only takes the box when the box has nothing to say, and gives it
	// back when the files have been put right.
	sayUnreadable() {
		var quiet = this.resultDiv.hasClass("result-ready") || this.unreadableSaid != null && this.resultDiv.text() == this.unreadableSaid;
		if (!quiet) {
			this.unreadableSaid = null;
			return;
		}
		if (this.unreadable.length == 0) {
			if (this.unreadableSaid != null)
				this.resetResult();
			this.unreadableSaid = null;
			return;
		}
		var first = this.unreadable[0];
		var errors = first.errors || [];
		var text = this.unreadable.length == 1
			? first.path + " cannot be played: " + (errors[0] || "see above.") + (errors.length > 1 ? "  (and " + (errors.length - 1) + " more, above)" : "")
			: this.unreadable.length + " scenario files have mistakes in them and cannot be played (they are listed above).";
		this.showResult(text, "error");
		this.unreadableSaid = this.resultDiv.text();
	}

	render() {
		this.next = null;
		this.renderCurrent();
		this.renderAuthor();
		this.renderUnreadable();
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
		//The tag comes first, and is as wide whatever it says: the titles start in a line
		var status = this.statusOf(scenario);
		var title = $("<div class='scenario-title'></div>")
			.append($("<span class='scenario-status'></span>").addClass("scenario-status-" + status.kind).attr("title", status.hint).text(status.text))
			.append($("<span class='scenario-title-text'></span>").text(scenario.title));
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

	// For those who may write scenarios: where to do it.  Writing, copying and submitting scenarios is under My
	// Account > My Scenarios (scenario/scenarioShelf.js); the draft they have open is offered there, and above
	// (scenario-current) when its game is the one they are in the middle of.
	renderAuthor() {
		var that = this;
		this.authorDiv.empty();
		if (!this.mayAuthor) {
			this.authorDiv.prop("hidden", true);
			return;
		}
		var link = $("<a href='#account/scenarios'></a>").text("My Account > My Scenarios").click(function (event) {
			event.preventDefault();
			that.mainHall.tableCreator.closeAfterSuccess();
			if (window.GempLinks)
				GempLinks.go("#account/scenarios");
		});
		this.authorDiv.append($("<span class='scenario-author-note'></span>")
			.append(document.createTextNode("Create your own Puzzle or Tutorial Scenarios on "))
			.append(link).append(document.createTextNode(".")));
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

	// {kind, text, hint} for the tag in front of a title: New, In Progress, Complete (or Updated, when the scenario
	// has been revised since it was finished), and Next for the one a campaign has the player on.
	statusOf(scenario) {
		var status = PlayScenario.progressOf(scenario, this.current);
		if (status.kind == "new" && this.nextIds()[scenario.id])
			return {kind: "next", text: "Next", hint: "The next one in " + ((scenario.campaign && scenario.campaign.title) || "its campaign")};
		return status;
	}

	static progressOf(scenario, current) {
		var progress = scenario.progress;
		if (progress != null && progress.status == "complete") {
			if (progress.version != null && scenario.version != null && progress.version < scenario.version)
				return {kind: "updated", text: "Updated", hint: "You have completed it, and it has been revised since"};
			return {kind: "complete", text: "Complete", hint: "You have played it through"};
		}
		if (progress != null || (current != null && current.id == scenario.id))
			return {kind: "progress", text: "In Progress", hint: "You have started it, and not finished it"};
		return {kind: "new", text: "New", hint: "You have not played it"};
	}

	// In each campaign: the first scenario the player has not played that comes after one they have finished.
	// {id: true}; worked out once for each list.
	nextIds() {
		if (this.next != null)
			return this.next;
		var campaigns = {};
		for (const scenario of this.scenarios) {
			if (scenario.campaign == null || scenario.campaign.id == null)
				continue;
			(campaigns[scenario.campaign.id] = campaigns[scenario.campaign.id] || []).push(scenario);
		}
		this.next = {};
		for (const id in campaigns) {
			var inOrder = campaigns[id].slice().sort(function (a, b) { return a.campaign.position - b.campaign.position; });
			var finished = false;
			for (const scenario of inOrder) {
				var kind = PlayScenario.progressOf(scenario, this.current).kind;
				if (kind == "complete" || kind == "updated") {
					finished = true;
				} else if (kind == "new" && finished) {
					this.next[scenario.id] = true;
					break;
				}
			}
		}
		return this.next;
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
