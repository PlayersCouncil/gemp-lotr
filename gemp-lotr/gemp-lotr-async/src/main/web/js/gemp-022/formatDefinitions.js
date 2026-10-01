/**
 * Help › Format Definitions (includes/help/formatRules.html).
 *
 * The server sends every hall format as data (GET /hall/formats/json, FormatDefinitions on the server): the facts the
 * Play popup's format (i) shows (setSummary, sites, Ring-bearer skirmish cancel), other rules, whether the format plays
 * with PC errata, and its card lists (X-list, R-list, limits, extra legal cards) with each card's name and set.
 * This page draws one <section class="format-entry" id="format-<code>"> per format (the id GempFormatHelp in
 * tables/TableFlow.js scrolls to for a #format-<code> deep link, e.g. from the Play popup's (i)), with a sticky
 * format list beside them and a filter that finds formats by name or by a card on their lists.
 *
 * Card lists are drawn as name chips grouped by set; "Card images" swaps them for lazily loaded thumbnails.  A click or
 * right-click on a chip or thumbnail opens the hall's zoomable card preview (GempCardPreview, cardPreview.js); they
 * are .cardHint buttons too, so without it a click opens the hall's card dialog (src/Hall/GameHall.js).  Their
 * tooltips give the card's collector's info ("1R29").  The errata themselves are not listed here: a format that uses
 * them links to Help › PC Errata.
 */
var GempFormatDefinitions = {
	IMAGES_KEY: "gemp.formatDefinitions.images",

	// The card lists, in the order they are shown.
	LISTS: [
		{key: "banned", title: "X-list", note: "can't be used"},
		{key: "restricted", title: "R-list", note: "no more than 1 copy per deck"},
		{key: "limit2", title: "Limit 2", note: "up to two copies per deck"},
		{key: "limit3", title: "Limit 3", note: "up to three copies per deck"},
		{key: "valid", title: "Also legal", note: "cards from outside the format's sets"}
	],

	CARD_SELECTOR: "button.fd-chip[value], button.fd-thumb[value]",

	root: null,
	formats: [],
	images: false,
	filterTimer: null,
	spyFrame: null,
	pin: null,           // {anchor, until}: the format just revealed; the scroll-spy keeps it while it is in view
	PIN_MS: 3000,        // a reveal's smooth scroll may take this long to bring the format into view

	// Fetches the definitions and draws them into `root`, then calls `drawn` (if given).
	load: function (root, drawn) {
		var that = this;
		root = $(root);
		this.root = root;
		root.empty().append($("<p class='fd-loading'></p>").text("Loading the format definitions…"));
		hall.comm.getFormatDefinitions(function (json) {
			that.render(root, json);
			if (typeof drawn == "function")
				drawn();
		}, neutralErrorMap(root, "the format definitions"));
	},

	render: function (root, data) {
		var started = (window.performance && performance.now) ? performance.now() : Date.now();
		var that = this;
		root = $(root);
		this.root = root;
		this.formats = (data && $.isArray(data.formats)) ? data.formats : [];
		this.images = this.loadImagesPreference();
		root.empty();

		var side = $("<aside class='fd-side'></aside>");
		var filter = $("<input type='search' class='fd-filter' autocomplete='off' spellcheck='false'>")
			.attr({placeholder: "Find a format or card", "aria-label": "Find a format, or the formats that list a card"});
		var imagesToggle = $("<label class='fd-images-toggle'></label>")
			.append($("<input type='checkbox' class='fd-images'>").prop("checked", this.images))
			.append(document.createTextNode(" Card images"));
		var status = $("<div class='fd-filter-status' aria-live='polite'></div>");
		var nav = $("<ul class='fd-nav'></ul>");
		side.append($("<div class='fd-side-tools'></div>").append(filter, imagesToggle, status), nav);

		var main = $("<div class='fd-main'></div>");
		if (this.formats.length == 0)
			main.append($("<p class='fd-empty'></p>").text("No formats are available right now."));

		for (var i = 0; i < this.formats.length; i++) {
			var format = this.formats[i];
			main.append(this.renderFormat(format));
			nav.append(this.renderNavItem(format));
		}
		root.append(side, main);

		filter.on("input", function () {
			clearTimeout(that.filterTimer);
			var value = $(this).val();
			that.filterTimer = setTimeout(function () {
				that.applyFilter(value);
			}, 120);
		});
		root.off(".fd");
		root.on("change.fd", ".fd-images", function () {
			that.setImages($(this).prop("checked"));
		});
		root.on("click.fd", ".fd-nav a, .fd-permalink", function (event) {
			event.preventDefault();
			that.go($(this).attr("data-anchor"));
		});
		root.on("click.fd", ".fd-errata-link", function (event) {
			event.preventDefault();
			that.openErrata($(this).attr("data-format"));
		});
		// a click or right-click on a card opens the zoomable preview (before the hall's .cardHint click handler)
		if (window.GempCardPreview)
			GempCardPreview.bind(root, this.CARD_SELECTOR, {
				click: true,
				contextmenu: true,
				resolve: function (element) {
					var id = $(element).attr("value");
					return id ? {blueprintId: id, title: $(element).attr("data-title") || null} : null;
				}
			});
		if (typeof TableFlow != "undefined" && TableFlow.bindInfoToggles)
			TableFlow.bindInfoToggles(root.closest(".fd-page").length ? root.closest(".fd-page") : root);

		this.watchScroller();
		if (typeof GempFormatHelp != "undefined")
			GempFormatHelp.beforeReveal = function (anchor) {
				that.prepareReveal(anchor);
			};

		var ended = (window.performance && performance.now) ? performance.now() : Date.now();
		root.attr("data-render-ms", Math.round(ended - started));
	},

	// ---- one format ----

	renderFormat: function (format) {
		var section = $("<section class='format-entry fd-format'></section>")
			.attr({id: format.anchor, "data-format": format.code});

		var head = $("<header class='fd-head'></header>");
		head.append($("<h2 class='format-name'></h2>").text(format.name));
		if (format.errata && format.errata.pc)
			head.append($("<span class='fd-badge fd-badge-pc' title='Uses the Player&#39;s Council errata'></span>").text("PC errata"));
		if (format.playtest)
			head.append($("<span class='fd-badge fd-badge-playtest'></span>").text("Playtest"));
		head.append($("<a class='fd-permalink' title='Link to this format'></a>")
			.attr({href: "#" + format.anchor, "data-anchor": format.anchor}).text("#"));
		if (window.GempShareLinks)
			head.append(GempShareLinks.button("format", format.code, "Share")
				.addClass("fd-share")
				.attr("aria-label", "Copy a share link to " + format.name));
		section.append(head);

		if (format.description)
			section.append($("<p class='format-description'></p>").text(format.description));

		section.append(this.renderFacts(format));

		var lists = $("<div class='fd-lists'></div>");
		var any = false;
		for (var i = 0; i < this.LISTS.length; i++) {
			var def = this.LISTS[i];
			var cards = (format.lists && format.lists[def.key]) || [];
			if (cards.length == 0)
				continue;
			any = true;
			lists.append(this.renderList(format, def, cards));
		}
		if (format.restrictedNames && format.restrictedNames.length > 0) {
			any = true;
			lists.append(this.renderNameList(format.restrictedNames));
		}
		if (!any)
			lists.append($("<p class='fd-no-lists'></p>").text("No X-, R- or limited cards: every card from its sets is legal."));
		section.append(lists);
		return section;
	},

	// The facts: the Play popup's three (sets, sites, Ring-bearer skirmish cancel) first, then errata and whatever
	// else the format changes.  (No deck-size / copy-limit line: it read the same for every hall format.)
	renderFacts: function (format) {
		var facts = $("<dl class='fd-facts'></dl>");
		var add = function (label, value) {
			facts.append($("<dt></dt>").text(label), $("<dd></dd>").append(value));
		};
		if (format.setSummary)
			add("Sets", document.createTextNode(this.setsText(format.setSummary)));
		if (format.sites)
			add("Sites", document.createTextNode(format.sites));
		add("Ring-bearer skirmish cancel", document.createTextNode(format.cancelRingBearerSkirmish ? "Yes" : "No"));
		var errataFlags = format.errata || {};
		if (errataFlags.pc || errataFlags.pcCardsLegal || errataFlags.playtest) {
			var errata = $("<span></span>");
			if (errataFlags.pc || errataFlags.pcCardsLegal)
				// a format that applies PC errata links to its own filtered list; one where the PC versions are only
				// legal alongside the originals (Anything Goes) links to the whole errata list
				errata.append($("<a class='fd-errata-link' href='#'></a>").attr("data-format", errataFlags.pc ? format.code : "").text("PC Errata"));
			if (!errataFlags.pc && errataFlags.pcCardsLegal)
				errata.append(document.createTextNode(" versions are legal alongside the originals"));
			if (errataFlags.playtest)
				errata.append(document.createTextNode((errataFlags.pc || errataFlags.pcCardsLegal ? ", plus " : "") + "playtest errata"));
			add("Errata", errata);
		}
		var rules = this.otherRules(format);
		if (rules.length > 0) {
			var list = $("<ul class='fd-rules'></ul>");
			for (var i = 0; i < rules.length; i++)
				list.append($("<li></li>").text(rules[i]));
			add("Other rules", list);
		}
		return facts;
	},

	// "Cards from sets 1-10, V1-V3" (the server's setSummary, as the Play popup shows it) reads "1-10, V1-V3" under
	// a "Sets" label.
	setsText: function (setSummary) {
		var match = /^Cards from sets? (.*)$/.exec(setSummary);
		return match ? match[1] : setSummary;
	},

	otherRules: function (format) {
		var rules = [];
		if (format.winAtEndOfRegroup)
			rules.push("The game ends after Regroup actions are made (instead of at the start of Regroup)");
		if (format.discardPileIsPublic)
			rules.push("Discard piles are public information for both sides");
		if (format.usesMaps)
			rules.push("Each deck includes a Map");
		if (format.winOnControlling5Sites)
			rules.push("A player who controls 5 sites wins");
		if (format.ruleOfFour === false)
			rules.push("No rule of 4");
		if (format.mulliganRule === false)
			rules.push("No mulligan");
		return rules;
	},

	// ---- card lists ----

	renderList: function (format, def, cards) {
		var details = $("<details class='fd-list' open></details>").addClass("fd-list-" + def.key).attr("data-list", def.key);
		var summary = $("<summary></summary>")
			.append($("<span class='fd-list-title'></span>").text(def.title))
			.append($("<span class='fd-count'></span>").text(cards.length))
			.append($("<span class='fd-list-note'></span>").text(def.note));
		details.append(summary);
		var body = $("<div class='fd-list-body'></div>");
		details.data("cards", cards);
		this.fillList(body, cards);
		details.append(body);
		return details;
	},

	renderNameList: function (names) {
		var details = $("<details class='fd-list fd-list-names' open></details>").attr("data-list", "restrictedNames");
		details.append($("<summary></summary>")
			.append($("<span class='fd-list-title'></span>").text("R-list by title"))
			.append($("<span class='fd-count'></span>").text(names.length))
			.append($("<span class='fd-list-note'></span>").text("one copy of any card with the title")));
		var body = $("<div class='fd-list-body fd-chips'></div>");
		for (var i = 0; i < names.length; i++)
			body.append($("<span class='fd-chip fd-chip-name'></span>").text(names[i]));
		details.append(body);
		return details;
	},

	// Names grouped by set (one row per set), or thumbnails when "Card images" is on.
	fillList: function (body, cards) {
		body.empty().toggleClass("fd-thumbs", this.images);
		if (this.images) {
			for (var t = 0; t < cards.length; t++)
				body.append(this.thumb(cards[t]));
			return;
		}
		var rows = [];
		var bySet = {};
		for (var i = 0; i < cards.length; i++) {
			var card = cards[i];
			var key = card.setLabel != null ? card.setLabel : "?";
			if (!bySet[key]) {
				bySet[key] = {label: key, name: card.setName, cards: []};
				rows.push(bySet[key]);
			}
			bySet[key].cards.push(card);
		}
		for (var r = 0; r < rows.length; r++) {
			var row = $("<div class='fd-set-row'></div>");
			var tag = $("<span class='fd-set'></span>").text(rows[r].label);
			if (rows[r].name)
				tag.attr("title", rows[r].name);
			var chips = $("<div class='fd-chips'></div>");
			for (var c = 0; c < rows[r].cards.length; c++)
				chips.append(this.chip(rows[r].cards[c]));
			body.append(row.append(tag, chips));
		}
	},

	chip: function (card) {
		return $("<button type='button' class='cardHint fd-chip'></button>")
			.attr({value: card.id, title: this.tooltip(card), "data-title": card.name, "data-name": this.fold(card.name)})
			.text(card.name);
	},

	// "<name> (1R29)": the card's name and collector's info, the label Help › PC Errata shows.
	tooltip: function (card) {
		var info = this.collectorLabel(card);
		return info ? card.name + " (" + info + ")" : card.name;
	},

	// The collector's info as printed on the card ("1R29"); the V-sets' "V1_5" (rarity still unset) as "V1 5".  A
	// card without one: its set and card number ("V1 5").  (The same label as pcErrataUi.js' collectorLabel.)
	collectorLabel: function (card) {
		var info = card.collInfo ? $.trim(String(card.collInfo)) : "";
		if (info)
			return info.replace(/_+/g, " ");
		var match = /^\d+_(\d+)/.exec(String(card.id || ""));
		if (match && card.setLabel != null)
			return card.setLabel + " " + parseInt(match[1], 10);
		return "";
	},

	thumb: function (card) {
		var url = null;
		var horizontal = false;
		try {
			var info = new Card(card.id, "SPECIAL", "hint", "");
			url = info.imageUrl;
			horizontal = !!info.horizontal;
		} catch (ignored) {
			try {
				url = Card.getImageUrl(card.id);
			} catch (ignoredToo) {
				url = null;
			}
		}
		var button = $("<button type='button' class='cardHint fd-thumb'></button>")
			.toggleClass("fd-thumb-horizontal", horizontal)
			.attr({value: card.id, title: this.tooltip(card), "data-title": card.name, "data-name": this.fold(card.name)});
		if (url)
			button.append($("<img loading='lazy' decoding='async'>").attr({src: url, alt: card.name}));
		button.append($("<span class='fd-thumb-name'></span>").text(card.name));
		return button;
	},

	setImages: function (on) {
		this.images = !!on;
		try {
			window.localStorage.setItem(this.IMAGES_KEY, this.images ? "1" : "0");
		} catch (ignored) {
			// no storage: the choice lasts until the page reloads
		}
		var that = this;
		this.root.find(".fd-images").prop("checked", this.images);
		this.root.find(".fd-list").each(function () {
			var cards = $(this).data("cards");
			if (cards)
				that.fillList($(this).children(".fd-list-body"), cards);
		});
		this.applyFilter(this.root.find(".fd-filter").val());
	},

	loadImagesPreference: function () {
		try {
			return window.localStorage.getItem(this.IMAGES_KEY) == "1";
		} catch (ignored) {
			return false;
		}
	},

	// ---- the format list and filter ----

	renderNavItem: function (format) {
		var link = $("<a></a>").attr({href: "#" + format.anchor, "data-anchor": format.anchor});
		link.append($("<span class='fd-nav-name'></span>").text(format.name));
		var meta = [];
		var banned = (format.lists && format.lists.banned) ? format.lists.banned.length : 0;
		var restricted = (format.lists && format.lists.restricted) ? format.lists.restricted.length : 0;
		if (banned)
			meta.push("X" + banned);
		if (restricted)
			meta.push("R" + restricted);
		if (meta.length)
			link.append($("<span class='fd-nav-meta'></span>").text(meta.join(" "))
				.attr("title", (banned ? banned + " X-listed" : "") + (banned && restricted ? ", " : "") + (restricted ? restricted + " R-listed" : "")));
		return $("<li></li>").attr("data-anchor", format.anchor).append(link);
	},

	// Lower case, without accents, so "eowyn" finds "Éowyn".
	fold: function (text) {
		text = String(text == null ? "" : text).toLowerCase();
		if (text.normalize)
			text = text.normalize("NFD").replace(/[̀-ͯ]/g, "");
		return text;
	},

	// Shows the formats whose name matches, and those listing a matching card (its chips marked).
	applyFilter: function (value) {
		var root = this.root;
		if (!root)
			return;
		var query = this.fold($.trim(value || ""));
		var sections = root.find(".fd-format");
		root.find(".fd-hit").removeClass("fd-hit");
		if (query === "") {
			sections.prop("hidden", false);
			root.find(".fd-nav li").prop("hidden", false);
			root.find(".fd-filter-status").text("");
			return;
		}
		var shown = 0;
		var byCard = 0;
		var that = this;
		sections.each(function () {
			var section = $(this);
			var name = that.fold(section.find(".format-name").first().text() + " " + section.attr("data-format"));
			var nameMatch = name.indexOf(query) >= 0;
			var hits = query.length >= 2
				? section.find("[data-name]").filter(function () {
					return this.getAttribute("data-name").indexOf(query) >= 0;
				})
				: $();
			hits.addClass("fd-hit");
			hits.closest("details").prop("open", true);
			var visible = nameMatch || hits.length > 0;
			if (!nameMatch && hits.length > 0)
				byCard++;
			section.prop("hidden", !visible);
			root.find(".fd-nav li[data-anchor='" + section.attr("id") + "']").prop("hidden", !visible);
			if (visible)
				shown++;
		});
		var status = shown + " of " + sections.length + " formats";
		if (byCard > 0)
			status += " (" + byCard + " by a listed card)";
		root.find(".fd-filter-status").text(status);
	},

	// Before GempFormatHelp reveals a format (a #format-<code> link, the Play popup's (i), the list here): a format
	// hidden by the filter clears the filter first, and the scroll-spy holds on to it while the panel scrolls there
	// (and afterwards while it is in view, e.g. the last formats, which can't scroll to the top of the panel).
	prepareReveal: function (anchor) {
		if (!this.root || anchor == null)
			return;
		this.pin = {anchor: anchor, until: this.now() + this.PIN_MS};
		var entry = document.getElementById(anchor);
		if (entry != null && entry.hidden) {
			this.root.find(".fd-filter").val("");
			this.applyFilter("");
		}
		this.markActive(anchor);
	},

	go: function (anchor) {
		if (anchor == null)
			return;
		if (typeof GempFormatHelp != "undefined") {
			GempFormatHelp.pending = anchor;
			GempFormatHelp.reveal();
		} else {
			this.prepareReveal(anchor);
			var entry = document.getElementById(anchor);
			if (entry != null && entry.scrollIntoView)
				entry.scrollIntoView({block: "start"});
		}
		this.setHash(anchor);
	},

	// Puts #<anchor> in the address bar without a reload or a history entry (and without a hashchange), so it is
	// always a link to the format on screen.
	setHash: function (anchor) {
		if (window.location.hash === "#" + anchor)
			return;
		if (window.history && typeof window.history.replaceState == "function") {
			try {
				window.history.replaceState(window.history.state, "", "#" + anchor);
			} catch (ignored) {
				// a sandboxed page may refuse; the link still works
			}
		}
	},

	now: function () {
		return Date.now();
	},

	markActive: function (anchor) {
		if (!this.root)
			return;
		var items = this.root.find(".fd-nav li");
		items.removeClass("fd-active");
		var item = items.filter("[data-anchor='" + anchor + "']").addClass("fd-active");
		var nav = this.root.find(".fd-side")[0];
		if (item.length && nav && nav.scrollHeight > nav.clientHeight) {
			var top = item[0].offsetTop;
			if (top < nav.scrollTop || top > nav.scrollTop + nav.clientHeight - 30)
				nav.scrollTop = Math.max(0, top - nav.clientHeight / 3);
		}
	},

	// ---- the scrolling panel: the list stays in view and follows the reading position ----

	scroller: function () {
		var main = $("#helpMain");
		return main.length ? main : null;
	},

	watchScroller: function () {
		var that = this;
		var scroller = this.scroller();
		// the list's height: the panel's inner height (a sticky element keeps clear of the panel's padding)
		var fit = function () {
			var height = $(window).height();
			if (scroller) {
				var style = window.getComputedStyle(scroller[0]);
				height = scroller[0].clientHeight - (parseFloat(style.paddingTop) || 0) - (parseFloat(style.paddingBottom) || 0);
			}
			if (height > 0 && that.root)
				that.root[0].style.setProperty("--fd-view-height", height + "px");
		};
		fit();
		$(window).off("resize.formatDefinitions").on("resize.formatDefinitions", fit);
		if (scroller && typeof ResizeObserver != "undefined") {
			if (this.resizeObserver)
				this.resizeObserver.disconnect();
			this.resizeObserver = new ResizeObserver(fit);
			this.resizeObserver.observe(scroller[0]);
		}
		if (scroller) {
			// the reader scrolling by hand lets go of a revealed format
			scroller.off("wheel.formatDefinitions touchstart.formatDefinitions keydown.formatDefinitions mousedown.formatDefinitions")
				.on("wheel.formatDefinitions touchstart.formatDefinitions keydown.formatDefinitions mousedown.formatDefinitions", function () {
					that.pin = null;
				});
			scroller.off("scroll.formatDefinitions").on("scroll.formatDefinitions", function () {
				if (that.spyFrame != null)
					return;
				var schedule = window.requestAnimationFrame || function (f) { return setTimeout(f, 50); };
				that.spyFrame = schedule(function () {
					that.spyFrame = null;
					that.spy();
				});
			});
		}
	},

	// Marks the format at the top of the panel in the list, and puts it in the address bar (#format-<code>) - or the
	// format just revealed, while the panel scrolls to it and while it stays in view.
	spy: function () {
		var scroller = this.scroller();
		if (!scroller || !this.root || !this.root.is(":visible"))
			return;
		var box = scroller[0].getBoundingClientRect();
		var top = box.top + 40;
		var current = null;
		this.root.find(".fd-format:not([hidden])").each(function () {
			var rect = this.getBoundingClientRect();
			if (rect.bottom > top) {
				current = this.id;
				return false;
			}
		});
		if (this.pin) {
			var pinned = document.getElementById(this.pin.anchor);
			var rect = pinned && !pinned.hidden ? pinned.getBoundingClientRect() : null;
			var inView = rect != null && rect.height > 0 && rect.top < box.bottom && rect.bottom > box.top;
			if (pinned && !pinned.hidden && (this.now() < this.pin.until || inView))
				current = this.pin.anchor;
			else
				this.pin = null;
		}
		if (current == null)
			return;
		this.markActive(current);
		this.setHash(current);
	},

	// ---- errata ----

	// Help › PC Errata.  A future hook: if that page can show one format's errata, pass `code` to it here.
	openErrata: function (code) {
		// Help › PC Errata filtered to this format's errata, when that page's router is loaded (pcErrataUi.js)
		if (typeof GempErrataHelp != "undefined" && GempErrataHelp.show)
			return GempErrataHelp.show(code ? {format: code} : {});
		var help = $("#helpMain");
		if (!help.length || !help.tabs("instance"))
			return false;
		var index = -1;
		help.find(".ui-tabs-nav").first().find("> li > a").each(function (i) {
			if (String($(this).attr("href")).indexOf("pc-errata") >= 0)
				index = i;
		});
		if (index < 0)
			return false;
		help.tabs("option", "active", index);
		help.scrollTop(0);
		return true;
	}
};
