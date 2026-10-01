/**
 * PlayerPicker: choose ONE player, from a dropdown of the players in the hall or by typing to search every
 * registered player.  The type-to-search behaviour follows the admin prize form's player box (addItemsForm.js: jQuery
 * UI autocomplete, a server search from 2 characters, at most 10 names), limited to a single player.
 *
 *   var picker = new PlayerPicker($("#unranked-invitee"), {
 *       search: function (prefix, limit, done, failed) {...},  // registered players by prefix (optional);
 *                                                              //   done(names) or failed()
 *       onChange: function (name) {...},                       // a name was picked or typed (optional)
 *       players: function () {...},                            // (optional) the players in the hall, asked on
 *                                                              //   every search, instead of setPlayers()
 *       self: function () {...}                                // (optional) your name, asked on every search,
 *                                                              //   instead of setSelf()
 *   });
 *   picker.setPlayers(["alice", "bob"]);   // the players in the hall (bare names), e.g. from the chat user list
 *   picker.setSelf("carol");               // never offered
 *   picker.val("bob"); picker.val();       // the chosen (or typed) name
 *   picker.enable(false);
 *
 * The input stays the form field (its value is the chosen name); the picker adds a ▾ button after it that opens
 * the whole list, as a <select> would.  Typing filters the players in the hall at once and, from 2 characters, adds
 * matching registered players under "Other players".  A name that is neither can still be typed; whoever uses the
 * value checks it (the Casual form's server does).  Names are always put into the DOM as text.
 */
class PlayerPicker {
	static MIN_SEARCH = 2;
	static LIMIT = 10;

	input = null;
	toggle = null;
	wrapper = null;
	options = null;
	online = [];         // bare names of the players in the hall
	pinned = [];         // names set through val() that are not in the hall (still offered, under "Other players")
	self = "";
	cache = {};          // prefix (lower case) -> registered names
	wasOpen = false;

	constructor(input, options) {
		var that = this;
		this.input = $(input);
		this.options = options || {};
		this.cache = {};
		this.online = [];
		this.pinned = [];

		this.input.attr({autocomplete: "off", role: "combobox", "aria-autocomplete": "list"});
		this.input.addClass("player-picker-input");
		this.wrapper = $("<span class='player-picker'></span>");
		this.input.after(this.wrapper);
		this.wrapper.append(this.input);
		this.toggle = $("<button type='button' class='player-picker-toggle' tabindex='-1'></button>")
			.attr({"aria-label": "Show the players in the hall", title: "Show the players in the hall"})
			.text("▾");
		this.wrapper.append(this.toggle);

		this.input.autocomplete({
			minLength: 0,
			delay: this.options.delay != null ? this.options.delay : 250,
			autoFocus: false,
			classes: {"ui-autocomplete": "player-picker-menu"},
			// open upward when there is no room below (short windows), as a <select> would
			position: {my: "left top", at: "left bottom", collision: "flipfit"},
			source: function (request, response) {
				that.suggest(request.term, response);
			},
			search: function () {
				// The menu belongs with the input's dialog, else it opens behind it.  The dialog may be created after
				// the picker (the Play popup is), so this is looked up on every search.
				var front = that.input.closest(".ui-front, dialog");
				var host = front.length ? front[0] : document.body;
				if (that.input.autocomplete("option", "appendTo") !== host)
					that.input.autocomplete("option", "appendTo", host);
			},
			focus: function () {
				return false;
			},
			select: function (event, ui) {
				if (ui.item && ui.item.value != null) {
					that.input.val(ui.item.value);
					that.changed();
				}
				return false;
			}
		});
		var widget = this.input.autocomplete("instance");
		// category headings and notes are not menu items (the jQuery UI "categories" pattern)
		this.input.autocomplete("widget").menu("option", "items", "> :not(.player-picker-heading)");
		widget._renderMenu = function (ul, items) {
			var heading = null;
			$.each(items, function (index, item) {
				if (item.heading != null && item.heading !== heading) {
					heading = item.heading;
					$("<li class='player-picker-heading' role='presentation'></li>").text(heading).appendTo(ul);
				}
				if (item.note) {
					$("<li class='player-picker-heading player-picker-note' role='presentation'></li>").text(item.note).appendTo(ul);
					return;
				}
				widget._renderItemData(ul, item);
			});
		};
		widget._renderItem = function (ul, item) {
			return $("<li></li>").append($("<div></div>").text(item.label)).appendTo(ul);
		};

		this.toggle.on("mousedown", function (event) {
			// remember whether the list was open before the input loses focus and closes it
			that.wasOpen = that.input.autocomplete("widget").is(":visible");
		}).on("click", function (event) {
			event.preventDefault();
			if (that.input.prop("disabled"))
				return;
			if (that.wasOpen) {
				that.wasOpen = false;
				that.input.autocomplete("close");
				return;
			}
			that.openList();
		});

		// an empty field opens the list like a dropdown
		this.input.on("click", function () {
			if (!that.input.prop("disabled") && $.trim(that.input.val()) === "" && !that.input.autocomplete("widget").is(":visible"))
				that.openList();
		});
		this.input.on("keydown", function (event) {
			// Alt+Down / Down on an empty field opens the list, as on a <select>
			if (event.which == 40 && !that.input.autocomplete("widget").is(":visible") && (event.altKey || $.trim(that.input.val()) === "")) {
				event.preventDefault();
				that.openList();
			}
		});
		this.input.on("change", function () {
			that.changed();
		});
	}

	// ---- the public API ----

	setPlayers(names) {
		var seen = {};
		var list = [];
		for (const raw of (names || [])) {
			var name = String(raw == null ? "" : raw).trim();
			if (name === "" || seen[name.toLowerCase()])
				continue;
			seen[name.toLowerCase()] = true;
			list.push(name);
		}
		list.sort(PlayerPicker.compareNames);
		this.online = list;
	}

	setSelf(name) {
		this.self = String(name == null ? "" : name).trim();
	}

	val(name) {
		if (arguments.length == 0)
			return $.trim(this.input.val());
		name = String(name == null ? "" : name).trim();
		this.input.val(name);
		if (name !== "" && !PlayerPicker.contains(this.online, name) && !PlayerPicker.contains(this.pinned, name))
			this.pinned.push(name);
		return this;
	}

	enable(enabled) {
		this.input.prop("disabled", !enabled);
		this.toggle.prop("disabled", !enabled);
		if (!enabled)
			this.input.autocomplete("close");
		return this;
	}

	openList() {
		this.input.trigger("focus");
		this.input.autocomplete("search", "");
	}

	// ---- suggestions ----

	isSelf(name) {
		return this.self !== "" && String(name).toLowerCase() === this.self.toLowerCase();
	}

	// Players in the hall first (all of them for an empty term), then registered players from the server search.
	suggest(term, response) {
		var that = this;
		term = $.trim(term || "");
		// a picker whose page is not fed the hall's list (My Account) reads it when it is needed
		if (typeof this.options.players == "function")
			this.setPlayers(this.options.players());
		if (typeof this.options.self == "function")
			this.setSelf(this.options.self());
		var items = [];
		var inHall = PlayerPicker.rank(this.online.filter((name) => !that.isSelf(name)), term);
		for (const name of inHall)
			items.push({label: name, value: name, heading: "In the hall"});

		var others = function (names) {
			var list = [];
			for (const name of names) {
				if (name == null || that.isSelf(name) || PlayerPicker.contains(inHall, name) || PlayerPicker.contains(list, name))
					continue;
				list.push(String(name));
			}
			return list;
		};

		if (term === "") {
			for (const name of others(this.pinned))
				items.push({label: name, value: name, heading: "Other players"});
			if (items.length == 0)
				items.push({note: "Nobody else is in the hall. Type a name to search all players."});
			response(items);
			return;
		}

		var search = this.options.search;
		if (term.length < PlayerPicker.MIN_SEARCH || typeof search != "function") {
			for (const name of others(PlayerPicker.rank(this.pinned, term)))
				items.push({label: name, value: name, heading: "Other players"});
			response(items);
			return;
		}

		var key = term.toLowerCase();
		var finish = function (names) {
			for (const name of others(names || []))
				items.push({label: name, value: name, heading: "Other players"});
			if (items.length == 0)
				items.push({note: "No player's name starts with \"" + term + "\"."});
			response(items);
		};
		if (Object.hasOwn(this.cache, key)) {
			finish(this.cache[key]);
			return;
		}
		// (the autocomplete ignores an answer to an older search)
		search(term, PlayerPicker.LIMIT, function (names) {
			that.cache[key] = Array.isArray(names) ? names.slice(0, PlayerPicker.LIMIT) : [];
			finish(that.cache[key]);
		}, function () {
			// the players in the hall are still offered when the search fails
			response(items);
		});
	}

	changed() {
		if (typeof this.options.onChange == "function")
			this.options.onChange(this.val());
	}

	// ---- helpers ----

	static compareNames(a, b) {
		return a.toLowerCase().localeCompare(b.toLowerCase());
	}

	static contains(list, name) {
		var lower = String(name).toLowerCase();
		return list.some((entry) => String(entry).toLowerCase() === lower);
	}

	// Names matching term (ignoring case): exact first, then those starting with it, then those containing it;
	// alphabetical within each.  An empty term keeps every name.
	static rank(names, term) {
		var q = String(term || "").toLowerCase();
		if (q === "")
			return names.slice().sort(PlayerPicker.compareNames);
		var hits = [];
		for (const name of names) {
			var folded = name.toLowerCase();
			var rank = folded === q ? 0 : folded.startsWith(q) ? 1 : folded.indexOf(q) >= 0 ? 2 : -1;
			if (rank >= 0)
				hits.push({name: name, rank: rank});
		}
		hits.sort((a, b) => (a.rank - b.rank) || PlayerPicker.compareNames(a.name, b.name));
		return hits.map((hit) => hit.name);
	}
}
