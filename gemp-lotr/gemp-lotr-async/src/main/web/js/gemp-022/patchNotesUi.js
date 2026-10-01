// ==== patch-notes: Server Info › Patch Notes (includes/info/patchNotes.html, css/gemp-001/patchNotes.css) ====
//
// The feed is the Markdown files in patchnotes/ (see patchnotes/README.md) plus the past server announcements, rendered
// and sanitized by the server (PatchNotesLibrary):
//   GET /patchnotes?start=&count=&from=<slug>&tag=  -> {total, start, count, tag, all, filters:[{tag, count}],
//                                                      notes:[{slug, kind, date, title, summary, tags, summaryHtml,
//                                                      html}]}
//   GET /patchnotes/months?tag=                     -> {tag, months:[{month: "2026-09", first: <slug>, count}]}
// The page shows PAGE_SIZE updates at a time, newest first.  At the top: the tag filter (All / Card Fixes / ...; server
// side, so paging and months follow it; not remembered past a reload) and the month navigation of the Events tab's
// completed events (MonthNav, eventHistoryUi.js): "<" / ">" step to the next older / newer month with updates, and a
// month picked from its dropdown shows that month's first update (its newest, the feed being newest first), from which
// the reader scrolls on down.  At the bottom: Newer / Older updates.  A page is a window that can start at any update,
// so every screen has a link:
//   #patch-notes          the newest updates
//   #patch-notes/<slug>   the updates from <slug> back (an update's own link puts it at the top); <slug> is a note's
//                         name or announcement-<id>
// Both work for visitors who are not logged in.  The page keeps the address bar on what it shows (replaceState).  A
// link to an update the active filter hides shows it under All instead.
//
// Entry point for the hall's link router (hallLinks.js): GempPatchNotes.show(slug|null) switches to Server Info ›
// Patch Notes (loading it if needed) and shows the newest updates, or the updates from <slug>.  When no router is
// loaded, this file follows #patch-notes links itself.
//
// Card links: the server renders a note's [[Cleaving Blow]] as <span class="cardHint patchnote-card" value="1_5">
// (patchnotes/README.md).  A click, a right-click, or Enter / Space opens the card in the shared zoomable card display
// (GempCardPreview, js/gemp-022/cardPreview.js); without it a click falls through to the hall's card dialog (.cardHint,
// src/Hall/GameHall.js).
// Share: each update's "Share" button copies its share link (/gemp-lotr/share/patch-notes/<slug>, shareLinks.js),
// which previews with the update's title, blurb and first screenshot where it is pasted.  "Link copied" shows to the
// button's left without moving anything (patchNotes.css).

var PatchNotes = {
	PAGE_SIZE: 5,
	HASH: "#patch-notes",
	// the filter bar, until the server's list arrives (PatchNote.TAGS)
	TAGS: ["Card Fixes", "PC Updates", "User Interface", "Announcement"],

	// "#patch-notes" -> {slug: null}; "#patch-notes/2023-12-09" -> {slug: "2023-12-09"}; anything else -> null
	parseHash: function (hash) {
		var match = /^#?patch-notes(?:\/([A-Za-z0-9-]*))?\/?$/.exec(String(hash == null ? "" : hash));
		if (!match)
			return null;
		return {slug: match[1] ? match[1].toLowerCase() : null};
	},

	buildHash: function (slug) {
		return slug ? this.HASH + "/" + slug : this.HASH;
	},

	// Dates show as YYYY-MM-DD everywhere (an international audience), exactly as the server sends them; anything
	// else is shown as it is.
	formatDate: function (iso) {
		return String(iso == null ? "" : iso);
	},

	// "2023-12-09" -> "2023-12"; null when it is not a date
	monthOf: function (iso) {
		var match = /^(\d{4}-\d{2})-\d{2}$/.exec(String(iso || ""));
		return match ? match[1] : null;
	},

	// replaceState: following the page adds no history entries
	setHash: function (hash) {
		if (window.location.hash === hash || !window.history || typeof window.history.replaceState != "function")
			return;
		try {
			window.history.replaceState(window.history.state, "", hash);
		} catch (ignored) {
			// the page still works without it
		}
	}
};

var PatchNotesUI = Class.extend({
	root: null,
	comm: null,
	tag: null,            // the filter: null for All, else a tag; kept only while the hall is open
	state: null,          // {slug}: the first update shown, or null for the newest (what the address bar holds)
	data: null,           // the last page shown
	requestSeq: 0,
	nav: null,            // the month navigation (MonthNav)
	monthsSeq: 0,
	monthsState: "idle",  // idle | loading | ready | error: the months list of the filter `monthsTag`
	monthsTag: null,

	init: function (root, comm) {
		this.root = root;
		this.comm = comm;
		this.list = root.find(".patchnotes-list");
		this.pagers = root.find(".patchnotes-pager");
		this.status = root.find(".patchnotes-status");
		this.filters = root.find(".patchnotes-filters");
		this.tag = PatchNotesUI.lastTag;
		this.buildMonthNav(root.find(".patchnotes-months"));
		this.renderFilters(null);
		this.bindEvents();
	},

	bindEvents: function () {
		var that = this;
		this.root.on("click", ".patchnotes-older", function () {
			that.page(1);
		});
		this.root.on("click", ".patchnotes-newer", function () {
			that.page(-1);
		});
		this.root.on("click", ".patchnotes-retry", function () {
			that.go(that.state || {slug: null}, false);
		});
		// the filter: a chip shows the newest updates with its tag (the active one too: back to the top)
		this.filters.on("click", ".patchnotes-filter", function () {
			that.setTag($(this).attr("data-tag") || null);
		});
		// card links: the zoomable card display (click, right-click, Enter / Space)
		this.root.on("click contextmenu", ".patchnote-card", function (event) {
			return PatchNotesUI.openCard($(this), event);
		});
		this.root.on("keydown", ".patchnote-card", function (event) {
			if (event.key !== "Enter" && event.key !== " ")
				return true;
			return PatchNotesUI.openCard($(this), event);
		});
		// Card links count as the preview's own (no handlers of its own: the ones above open it), so the mouseup that
		// follows a right-click (Chrome on Linux and macOS fires contextmenu on mousedown) does not close the card it
		// has just opened, and a click on another card link swaps the card
		if (window.GempCardPreview && typeof GempCardPreview.bind == "function")
			GempCardPreview.bind(this.root, ".patchnote-card", {click: false, contextmenu: false});
		// screenshots: click to enlarge
		this.root.on("click", ".patchnote-body img:not(.patchnote-culture)", function (event) {
			var img = $(this);
			if (img.closest("a").length)
				return;           // an image that is a link follows the link
			event.preventDefault();
			PatchNotesLightbox.open(img.attr("src"), img.attr("alt") || "", this);
		});
	},

	// ---- the filter ----

	// The chips: All and each tag, with the counts of the last page (none before it arrives).
	renderFilters: function (data) {
		var that = this;
		var tags = [];
		if (data && $.isArray(data.filters)) {
			for (var i = 0; i < data.filters.length; i++)
				tags.push({tag: String(data.filters[i].tag), count: data.filters[i].count});
		} else {
			for (var t = 0; t < PatchNotes.TAGS.length; t++)
				tags.push({tag: PatchNotes.TAGS[t], count: null});
		}
		tags.unshift({tag: null, count: data ? data.all : null});

		this.filters.empty();
		$.each(tags, function (i, entry) {
			var on = (entry.tag || null) === (that.tag || null);
			var chip = $("<button type='button' class='patchnotes-filter' role='radio'></button>")
				.attr("data-tag", entry.tag || "")
				.attr("aria-checked", on ? "true" : "false")
				.toggleClass("patchnotes-filter-on", on)
				.append($("<span class='patchnotes-filter-label'></span>").text(entry.tag || "All"));
			if (entry.count != null)
				chip.append($("<span class='patchnotes-filter-count'></span>").text(entry.count));
			that.filters.append(chip);
		});
	},

	setTag: function (tag) {
		this.tag = tag || null;
		PatchNotesUI.lastTag = this.tag;
		this.filters.find(".patchnotes-filter").each(function () {
			var on = ($(this).attr("data-tag") || null) === (tag || null);
			$(this).toggleClass("patchnotes-filter-on", on).attr("aria-checked", on ? "true" : "false");
		});
		this.go({slug: null}, false);
	},

	// ---- the month navigation (the Events tab's, MonthNav in eventHistoryUi.js) ----

	buildMonthNav: function (container) {
		var that = this;
		if (!container.length || typeof MonthNav == "undefined")
			return;
		this.nav = new MonthNav({
			prevTitle: "Older month with updates",
			nextTitle: "Newer month with updates",
			onPrev: function () {
				that.jump(-1);
			},
			onNext: function () {
				that.jump(1);
			},
			onOpen: function () {
				that.renderMonthList();
				if (that.months() == null)
					that.loadMonths();
			},
			onPick: function (key) {
				that.showMonth(key);
			},
			onRetry: function () {
				that.loadMonths();
			}
		});
		container.empty().append(this.nav.header);
		this.nav.setTitle("");
		this.nav.setDisabled(true, true);
	},

	// the month the page shows: that of its first update
	currentMonth: function () {
		return this.data && this.data.notes.length ? PatchNotes.monthOf(this.data.notes[0].date) : null;
	},

	// the months list of the filter: [{month, first, count}] newest first, or null until it has been fetched
	months: function () {
		var entry = PatchNotesUI.monthsCache[this.tag || ""];
		return entry && Date.now() - entry.at < PatchNotesUI.cache.TTL_MS ? entry.months : null;
	},

	loadMonths: function (then) {
		var that = this;
		var tag = this.tag || "";
		var seq = ++this.monthsSeq;
		this.monthsState = "loading";
		this.monthsTag = tag;
		this.syncMonthNav();
		var done = function (months, error) {
			if (seq !== that.monthsSeq)
				return;           // the filter changed meanwhile: its own request is on its way
			that.monthsState = months ? "ready" : "error";
			that.monthsError = error || null;
			that.syncMonthNav();
			if (then)
				then(months);
		};
		var params = {};
		if (tag)
			params.tag = tag;
		this.comm.getPatchNoteMonths(params, function (json) {
			var months = json && $.isArray(json.months) ? json.months : [];
			PatchNotesUI.monthsCache[tag] = {at: Date.now(), months: months};
			done(months);
		}, PatchNotesUI.errorMap(function (xhr) {
			done(null, xhr && xhr.status ? "(the server answered " + xhr.status + ")" : "(no answer from the server)");
		}));
	},

	// The title, the arrows and (when open) the dropdown, from the page shown and the months list.
	syncMonthNav: function () {
		if (!this.nav)
			return;
		var current = this.currentMonth();
		this.nav.setTitle(current ? EventHistoryUI.monthLabel(current) : "");
		var months = this.months();
		if (months == null || current == null) {
			this.nav.setDisabled(this.monthsState === "loading" || current == null,
				this.monthsState === "loading" || current == null);
		} else {
			var keys = $.map(months, function (m) { return m.month; });
			this.nav.setDisabled(EventHistoryUI.neighbourMonth(keys, current, -1) == null,
				EventHistoryUI.neighbourMonth(keys, current, 1) == null);
		}
		if (this.nav.isOpen())
			this.renderMonthList();
	},

	renderMonthList: function () {
		if (!this.nav)
			return;
		var months = this.months();
		if (months == null) {
			if (this.monthsState === "error" && this.monthsTag === (this.tag || ""))
				this.nav.renderList("error", null, null, this.monthsError);
			else
				this.nav.renderList("loading");
			return;
		}
		this.nav.renderList("ready", $.map(months, function (m) { return m.month; }), this.currentMonth(),
			this.tag ? "No updates are tagged “" + this.tag + "” yet." : "There are no patch notes yet.");
	},

	// "<" (delta -1): the next older month with updates; ">" (+1): the next newer one
	jump: function (delta) {
		var that = this;
		var months = this.months();
		var current = this.currentMonth();
		if (current == null)
			return;
		if (months == null) {
			this.loadMonths(function (list) {
				if (list != null)
					that.jump(delta);
			});
			return;
		}
		var keys = $.map(months, function (m) { return m.month; });
		var target = EventHistoryUI.neighbourMonth(keys, current, delta);
		if (target != null)
			this.showMonth(target);
	},

	// Shows the updates from the month's first (newest) update on.
	showMonth: function (key) {
		var months = this.months() || [];
		for (var i = 0; i < months.length; i++) {
			if (months[i].month === key) {
				this.go({slug: months[i].first}, true);
				return;
			}
		}
	},

	// ---- paging ----

	// Moves a page older (+1) or newer (-1) from the window shown now.
	page: function (direction) {
		if (!this.data)
			return;
		var start = this.data.start + direction * PatchNotes.PAGE_SIZE;
		if (direction < 0 && start <= 0) {
			this.go({slug: null}, true);
			return;
		}
		start = Math.max(0, start);
		if (start >= this.data.total)
			return;
		this.go({start: start}, true);
	},

	// route: {slug} (null = newest) or {start}; highlight: the slug of an update to mark as the one linked to
	go: function (route, scroll, highlight) {
		var that = this;
		var seq = ++this.requestSeq;
		var params = {count: PatchNotes.PAGE_SIZE};
		if (route.slug)
			params.from = route.slug;
		else
			params.start = route.start || 0;
		if (this.tag)
			params.tag = this.tag;

		this.showStatus("loading");
		var cached = PatchNotesUI.cache.get(params);
		var done = function (data) {
			if (seq !== that.requestSeq)
				return;          // a later navigation won
			PatchNotesUI.cache.put(params, data);
			that.render(data, route, scroll, highlight);
		};
		if (cached) {
			done(cached);
			return;
		}
		// the same page asked for again while it is on its way (a deep link on first load: the page's mount and the
		// link router both ask for it) waits for that answer instead of fetching it twice
		var key = PatchNotesUI.cache.key(params);
		var request = PatchNotesUI.inflight[key];
		var first = request == null || Date.now() - request.at > PatchNotesUI.INFLIGHT_MS;
		if (first)
			request = PatchNotesUI.inflight[key] = {at: Date.now(), done: [], fail: []};
		request.done.push(done);
		request.fail.push(this.failure(route, seq, scroll, highlight));
		if (!first)
			return;
		var settle = function (callbacks, value) {
			if (PatchNotesUI.inflight[key] === request)
				delete PatchNotesUI.inflight[key];
			for (var i = 0; i < callbacks.length; i++)
				callbacks[i](value);
		};
		this.comm.getPatchNotes(params, function (data) {
			settle(request.done, data);
		}, PatchNotesUI.errorMap(function (xhr) {
			settle(request.fail, xhr);
		}));
	},

	// What a failed request does for the navigation `seq` (ignored once a later one started).
	failure: function (route, seq, scroll, highlight) {
		var that = this;
		return function (xhr) {
			if (seq !== that.requestSeq)
				return;
			if (xhr && xhr.status === 404 && route.slug) {
				if (that.tag) {
					// a link to an update the filter hides: show it under All
					that.notice = "Showing all updates: that one is not tagged “" + that.tag + "”.";
					that.setTagQuietly(null);
					that.go(route, scroll, highlight);
					return;
				}
				// an old or mistyped link: say so, and show the newest updates instead
				that.missingSlug = route.slug;
				that.go({slug: null}, false);
				return;
			}
			that.showStatus("error", xhr ? xhr.status : 0);
		};
	},

	// the filter changes without a request of its own (the caller makes one)
	setTagQuietly: function (tag) {
		this.tag = tag || null;
		PatchNotesUI.lastTag = this.tag;
		this.renderFilters(this.data);
	},

	showStatus: function (kind, code) {
		var that = this;
		clearTimeout(this.loadingTimer);
		this.status.empty().removeClass("load-error").attr("hidden", null);
		if (kind === "loading") {
			// the first page: "Loading patch notes…", but only once the answer is slow in coming (a warm server answers
			// within a round trip, and a message that flashes for a moment reads as a glitch)
			this.status.attr("hidden", "hidden");
			if (this.data == null) {
				this.loadingTimer = setTimeout(function () {
					if (that.root.attr("aria-busy") === "true")
						that.status.text("Loading patch notes…").attr("hidden", null);
				}, PatchNotesUI.LOADING_DELAY_MS);
			}
			this.root.attr("aria-busy", "true");
			return;
		}
		this.root.attr("aria-busy", "false");
		if (kind === "error") {
			this.status.addClass("load-error")
				.append($("<span></span>").text("The patch notes could not be loaded"
					+ (code ? " (the server answered " + code + ")" : " (no answer from the server)") + ". "))
				.append($("<button type='button' class='patchnotes-retry'></button>").text("Try again"));
			return;
		}
		this.status.attr("hidden", "hidden");
	},

	render: function (data, route, scroll, highlight) {
		this.data = data;
		this.showStatus("done");
		var slug = data.start > 0 && data.notes.length ? data.notes[0].slug : null;
		this.state = slug ? {slug: slug} : {slug: null};
		PatchNotesUI.lastState = this.state;
		this.renderFilters(data);

		this.list.empty();
		if (this.missingSlug) {
			this.list.append($("<p class='patchnotes-notice'></p>")
				.text("There is no update named “" + this.missingSlug + "”. Here are the newest ones."));
			this.missingSlug = null;
		}
		if (this.notice) {
			this.list.append($("<p class='patchnotes-notice'></p>").text(this.notice));
			this.notice = null;
		}
		if (!data.notes.length) {
			this.list.append($("<p class='patchnotes-empty'></p>").text(data.total ? "There are no older updates."
				: this.tag ? "No updates are tagged “" + this.tag + "” yet." : "There are no patch notes yet."));
		}
		for (var i = 0; i < data.notes.length; i++)
			this.list.append(this.renderNote(data.notes[i], data.notes[i].slug === highlight));
		this.renderPagers(data);

		this.syncMonthNav();
		if (this.months() == null && !(this.monthsState === "loading" && this.monthsTag === (this.tag || "")))
			this.loadMonths();

		if (this.isShowing())
			PatchNotes.setHash(PatchNotes.buildHash(slug));
		// the page's top: the filter and month navigation stay in view above the update a link points to, which is first
		if (scroll && typeof this.root[0].scrollIntoView == "function")
			this.root[0].scrollIntoView({block: "start"});
	},

	renderNote: function (note, highlight) {
		var article = $("<article class='patchnote'></article>")
			.attr("data-slug", note.slug)
			.attr("id", "patchnote-" + note.slug);
		if (note.kind === "announcement")
			article.addClass("patchnote-announcement");
		if (highlight)
			article.addClass("patchnote-target");

		var head = $("<header class='patchnote-head'></header>");
		var dateText = PatchNotes.formatDate(note.date);
		var title = $("<h2 class='patchnote-title'></h2>").text(note.title || dateText);
		head.append(title);
		if (note.title)
			head.append($("<time class='patchnote-date'></time>").attr("datetime", note.date).text(dateText));
		else
			title.wrapInner($("<time></time>").attr("datetime", note.date));
		if ($.isArray(note.tags) && note.tags.length) {
			var tags = $("<span class='patchnote-tags'></span>");
			for (var i = 0; i < note.tags.length; i++)
				tags.append($("<span class='patchnote-tag'></span>").text(note.tags[i]));
			head.append(tags);
		}
		if (window.GempShareLinks) {
			// "Link copied" goes to the button's left, over the header, so nothing moves (patchNotes.css)
			head.append($("<span class='patchnote-share-wrap'></span>").append(
				GempShareLinks.button("patch-notes", note.slug, "Share")
					.addClass("patchnote-share")
					.attr("aria-label", "Copy a share link to the "
						+ (note.kind === "announcement" ? "announcement" : "update") + " of " + dateText)));
		}
		article.append(head);

		// summaryHtml: the summary with its culture icons, escaped by the server (PatchNote.toJson); else plain text
		if (note.summaryHtml)
			article.append($("<p class='patchnote-summary'></p>").html(note.summaryHtml));
		else if (note.summary)
			article.append($("<p class='patchnote-summary'></p>").text(note.summary));
		// html is rendered and sanitized by the server (PatchNoteRenderer); it is authored content from the repo, or a
		// server announcement written by an admin
		article.append($("<div class='patchnote-body'></div>").html(note.html || ""));
		return article;
	},

	// Newer / Older at the bottom, for someone reading on down
	renderPagers: function (data) {
		var first = data.start + 1;
		var last = data.start + data.notes.length;
		var hasNewer = data.start > 0;
		var hasOlder = last < data.total;
		this.pagers.each(function () {
			var pager = $(this).empty();
			if (!data.total)
				return;
			if (hasNewer)
				pager.append($("<button type='button' class='patchnotes-newer'></button>").text("‹ Newer updates"));
			pager.append($("<span class='patchnotes-range'></span>").text(data.notes.length
				? "Updates " + first + "–" + last + " of " + data.total
				: data.total + " updates"));
			if (hasOlder)
				pager.append($("<button type='button' class='patchnotes-older'></button>").text("Older updates ›"));
		});
	},

	// On screen as far as the tabs are concerned: attached, and no ancestor hidden by a tabs widget or otherwise.
	isShowing: function () {
		var node = this.root[0];
		if (!node || !document.documentElement.contains(node))
			return false;
		for (var n = node; n != null && n.nodeType === 1; n = n.parentElement) {
			if ((n.style && n.style.display === "none") || n.hasAttribute("hidden")
					|| ($(n).hasClass("ui-tabs-panel") && n.getAttribute("aria-hidden") === "true"))
				return false;
		}
		return true;
	},

	// A route from a link, or the state kept from the last visit, or the newest updates.
	load: function (route) {
		if (route)
			this.go(route, true, route.slug);
		else
			this.go(PatchNotesUI.lastState || {slug: null}, false);
	}
});

PatchNotesUI.current = null;
PatchNotesUI.lastState = null;
PatchNotesUI.lastTag = null;   // the filter while the hall stays open (a reload starts on All again)
PatchNotesUI.inflight = {};    // cache key -> {at, done: [], fail: []} of a request on its way
PatchNotesUI.INFLIGHT_MS = 15000;   // an answer that has not come by then is not waited for again
PatchNotesUI.LOADING_DELAY_MS = 300;   // "Loading patch notes…" shows once the first page takes longer than this
PatchNotesUI.monthsCache = {};   // tag ("" for All) -> {at, months}

// Every status (0 and 400-599) goes to `fail(xhr)`, so no failure here reaches communication.js's global popup.
PatchNotesUI.errorMap = function (fail) {
	var map = {};
	for (var status = 0; status < 600; status += (status < 400 ? 400 : 1))
		map[String(status)] = fail;
	return map;
};

// A card link was clicked, right-clicked or pressed: the shared zoomable card display when it is loaded.  Without it,
// a click goes on to the hall's .cardHint dialog, and a right-click or a key is turned into that click.
PatchNotesUI.openCard = function (link, event) {
	var id = String(link.attr("value") || "");
	if (!/^\d{1,3}_\d{1,4}$/.test(id))
		return true;
	if (window.GempCardPreview && typeof GempCardPreview.open == "function") {
		event.preventDefault();
		event.stopPropagation();
		GempCardPreview.open({blueprintId: id, title: link.attr("title") || link.text()}, event);
		return false;
	}
	if (event.type === "click")
		return true;          // the body's .cardHint handler opens the card dialog
	event.preventDefault();
	event.stopPropagation();
	link.trigger("click");
	return false;
};

// Pages already fetched, so going back and forth (and coming back to the sub-tab, which jQuery UI reloads on every
// visit) does not ask again.  Kept for a few minutes: a deploy with a new update shows up without a reload.
PatchNotesUI.cache = {
	TTL_MS: 5 * 60 * 1000,
	entries: {},
	key: function (params) {
		return (params.tag ? "tag:" + params.tag + "/" : "")
			+ (params.from ? "from:" + params.from : "start:" + (params.start || 0)) + "/" + params.count;
	},
	get: function (params) {
		var entry = this.entries[this.key(params)];
		if (entry && Date.now() - entry.at < this.TTL_MS)
			return entry.data;
		return null;
	},
	put: function (params, data) {
		this.entries[this.key(params)] = {at: Date.now(), data: data};
	},
	clear: function () {
		this.entries = {};
		PatchNotesUI.monthsCache = {};
	}
};

// Called by includes/info/patchNotes.html each time the sub-tab loads.
PatchNotesUI.mount = function (root, comm) {
	var ui = new PatchNotesUI(root, comm || PatchNotesUI.defaultComm());
	PatchNotesUI.current = ui;
	ui.load(GempPatchNotes.take());
	return ui;
};

// The hall's communication object; a visitor who is not logged in still gets one (the page needs no login).
PatchNotesUI.defaultComm = function () {
	if (window.hall && hall.comm)
		return hall.comm;
	return new GempLotrCommunication("/gemp-lotr-server", function () {
	});
};

/**
 * Click-to-enlarge for screenshots: the image at full size over a dimmed page (the hall stays visible behind it).  A
 * click anywhere but on the image, the close button or Esc dismisses it, and focus goes back to the screenshot.
 */
var PatchNotesLightbox = {
	overlay: null,
	returnFocus: null,

	open: function (src, alt, from) {
		this.close();
		var that = this;
		this.returnFocus = from || null;
		var overlay = $("<div class='patchnotes-lightbox' role='dialog' aria-modal='true'></div>")
			.attr("aria-label", alt ? "Screenshot: " + alt : "Screenshot");
		var figure = $("<figure class='patchnotes-lightbox-figure'></figure>");
		figure.append($("<img>").attr("src", src).attr("alt", alt));
		if (alt)
			figure.append($("<figcaption></figcaption>").text(alt));
		var close = $("<button type='button' class='patchnotes-lightbox-close' aria-label='Close'>×</button>");
		overlay.append(figure).append(close);
		overlay.on("click", function (event) {
			if ($(event.target).is("img"))
				return;          // a click on the picture itself keeps it open
			that.close();
		});
		$(document).on("keydown.patchNotesLightbox", function (event) {
			if (event.key === "Escape" || event.keyCode === 27) {
				event.preventDefault();
				event.stopPropagation();
				that.close();
			}
		});
		$("body").append(overlay).addClass("patchnotes-lightbox-open");
		this.overlay = overlay;
		close[0].focus();
	},

	close: function () {
		if (!this.overlay)
			return;
		this.overlay.remove();
		this.overlay = null;
		$(document).off("keydown.patchNotesLightbox");
		$("body").removeClass("patchnotes-lightbox-open");
		if (this.returnFocus && typeof this.returnFocus.focus == "function" && document.documentElement.contains(this.returnFocus))
			this.returnFocus.focus();
		this.returnFocus = null;
	},

	isOpen: function () {
		return this.overlay != null;
	}
};

/**
 * #patch-notes links: switch to Server Info › Patch Notes (loading it if needed) and hand the link to the page.
 */
var GempPatchNotes = {
	pending: null,
	waitTimer: null,

	take: function () {
		var route = this.pending;
		this.pending = null;
		return route;
	},

	// slug: an update's name (its file name without .md, or announcement-<id>), or null for the newest updates
	show: function (slug) {
		var route = {slug: slug ? String(slug).toLowerCase() : null};
		PatchNotes.setHash(PatchNotes.buildHash(route.slug));
		return this.open(route);
	},

	open: function (route) {
		var that = this;
		this.pending = route;
		clearTimeout(this.waitTimer);
		var tries = 0;
		var step = function () {
			if (that.activate()) {
				var ui = PatchNotesUI.current;
				if (ui && ui.root.closest("body").length && that.pending) {
					var r = that.take();
					ui.go(r, true, r.slug);
				}
				// otherwise the page is still loading: PatchNotesUI.mount takes the route
				return true;
			}
			if (++tries < 100)
				that.waitTimer = setTimeout(step, 50);
			else
				that.pending = null;   // no Server Info tab to show it in: do not hand it to a later visit
			return false;
		};
		return step();
	},

	// Makes Server Info › Patch Notes the active tab; false until both tab sets exist (the caller tries again).
	activate: function () {
		var main = $("#main");
		if (!main.length || !main.tabs("instance"))
			return false;
		var infoIndex = this.tabIndex(main, "includes/info.html");
		if (infoIndex < 0)
			return false;
		if (main.tabs("option", "active") !== infoIndex)
			main.tabs("option", "active", infoIndex);
		var info = $("#infoMain");
		if (!info.length || !info.tabs("instance"))
			return false;
		var notesIndex = this.tabIndex(info, "patchNotes");
		if (notesIndex < 0)
			return false;
		if (info.tabs("option", "active") !== notesIndex)
			info.tabs("option", "active", notesIndex);
		return true;
	},

	tabIndex: function (tabs, hrefPart) {
		var index = -1;
		tabs.find(".ui-tabs-nav").first().find("> li > a").each(function (i) {
			if (String($(this).attr("href")).indexOf(hrefPart) >= 0)
				index = i;
		});
		return index;
	}
};

// Without the hall's link router (js/gemp-022/hallLinks.js, GempLinks), follow #patch-notes links here.
$(function () {
	if (window.GempLinks)
		return;
	var follow = function () {
		var route = PatchNotes.parseHash(window.location.hash);
		if (route != null && !(PatchNotesUI.current && PatchNotesUI.current.state
				&& PatchNotesUI.current.state.slug === route.slug && PatchNotesUI.current.isShowing()))
			GempPatchNotes.open(route);
	};
	$(window).on("hashchange.patchNotes", follow);
	follow();
});
// ==== end patch-notes ====
