/**
 * Hall deep links: the one place that decides which tab (and sub-tab) the hall shows.
 *
 * A hall URL's fragment names what is on screen, so the address bar is always a link to it:
 *
 *   #game-hall, #help, #events, #info, #account, #admin        a top-level tab
 *   #help/formats, #events/leagues, #info/stats, ...            a sub-tab (slugs in GempLinks.TABS)
 *   #help/manual                                               How Do I Play?, scrolled to the Manual
 *   #format-<code>                                             Help › Format Definitions, that format (GempFormatHelp)
 *   #pc-errata[?k=v&...], #errata-<card id>                    Help › PC Errata (GempErrataHelp / PcErrata.parseHash)
 *   #patch-notes[/<slug>]                                      Server Info › Patch Notes (GempPatchNotes.show)
 *   #league-<code>, #tournament-<id>                           Events, that league's / tournament's drawer open
 *
 * On load the fragment wins; without one the tab the viewer last had open (GempHallTabs, localStorage) is restored,
 * else the Game Hall.  A fragment typed or followed later (hashchange) is handled the same way.  Switching tabs by
 * hand writes the new tab's link into the address bar (history.replaceState: no reload, no history entry); a page
 * that shows something more specific (a format, an errata card, a patch note) writes its own link the same way, and
 * that link is left alone for as long as its tab is showing.
 *
 * Routing: GempLinks.go(hash) opens the route's top-level tab, waits for that tab's page (and its sub-tabs) to load,
 * opens the sub-tab, waits for it to load, and then calls the route's handler once.
 *
 * Help and Server Info are public: a visitor who is not logged in can read them, and nothing asks them to log in
 * until they open a tab that needs it (GempHallSession below).
 */
var GempLinks = {
	// The top-level tabs, matched by their link's href.  login: the tab needs a logged-in player.  panel: the tab set
	// the tab's page creates for its sub-tabs; each sub-tab is matched by its link's href (exactly for "#local"
	// panels, else by a part of the file name).
	TABS: [
		{slug: "game-hall", href: "#gameHall", login: true, aliases: ["hall", "gameHall"]},
		{slug: "help", href: "includes/help.html", login: false, panel: "#helpMain", subs: [
			{slug: "howtoplay", match: ["#howtoplay"]},
			{slug: "coc", match: ["coc.html"]},
			{slug: "formats", match: ["formatRules"]},
			{slug: "leagues", match: ["leagueRules"]},
			{slug: "pc-errata", match: ["pc-errata"]}
		]},
		{slug: "events", href: "includes/events.html", login: true, panel: "#eventsMain", subs: [
			{slug: "calendar", match: ["#calendar"]},
			{slug: "leagues", match: ["#leagues"]},
			{slug: "tournaments", match: ["#tournaments"]}
		]},
		{slug: "info", href: "includes/info.html", login: false, panel: "#infoMain", aliases: ["server-info"], subs: [
			{slug: "stats", match: ["#server-stats"]},
			{slug: "patch-notes", match: ["patchNotes", "patch-notes", "changeLog"]},
			{slug: "contribute", match: ["contribute"]},
			{slug: "about", match: ["#about"]}
		]},
		{slug: "users", href: "includes/user.html", login: true},
		{slug: "account", href: "includes/account.html", login: true, aliases: ["my-account"], panel: "#accountMain", subs: [
			{slug: "overview", match: ["#account"]},
			{slug: "history", match: ["gameHistory"]},
			{slug: "stats", match: ["yourStats"]}
		]},
		{slug: "admin", href: "includes/admin.html", login: true, panel: "#adminMain", subs: [
			{slug: "general", match: ["generalAdmin"]},
			{slug: "league", match: ["leagueAdmin"]},
			{slug: "tournament", match: ["tournamentAdmin"]},
			{slug: "prizes", match: ["prizeAdmin"]},
			{slug: "users", match: ["userAdmin"]}
		]}
	],

	// Links to something inside a page.  run(match, hash) is called once the sub-tab's page has loaded.
	ROUTES: [
		{pattern: /^#(format-[A-Za-z0-9_-]+)$/, tab: "help", sub: "formats", run: function (m) {
			if (window.GempFormatHelp)
				GempFormatHelp.open(m[1]);
		}},
		{pattern: /^#(?:pc-errata|errata-[A-Za-z0-9_]+)(?:\?.*)?$/, tab: "help", sub: "pc-errata", run: function (m, hash) {
			if (window.GempErrataHelp && window.PcErrata)
				GempErrataHelp.open(PcErrata.parseHash(hash));
		}},
		{pattern: /^#patch-notes(?:\/([A-Za-z0-9_.-]+))?$/, tab: "info", sub: "patch-notes", run: function (m) {
			if (window.GempPatchNotes && typeof GempPatchNotes.show == "function")
				GempPatchNotes.show(m[1] || null);
		}},
		{pattern: /^#help\/manual$/, tab: "help", sub: "howtoplay", run: function () {
			var heading = document.getElementById("help-manual");
			if (heading != null && typeof heading.scrollIntoView == "function")
				heading.scrollIntoView({block: "start"});
		}},
		{pattern: /^#league-([A-Za-z0-9_-]+)$/, tab: "events", sub: "leagues", run: function (m) {
			if (window.GempEventsPage)
				GempEventsPage.showLeague(m[1]);
		}},
		{pattern: /^#tournament-([A-Za-z0-9_.-]+)$/, tab: "events", sub: "tournaments", run: function (m) {
			if (window.GempEventsPage)
				GempEventsPage.showTournament(m[1]);
		}}
	],

	POLL_MS: 50,
	MAX_TRIES: 200,        // 10 s: a tab that is still hidden (Admin before the player info) or still loading by then is given up on

	started: false,
	initialized: false,
	initial: null,         // the route chosen on load (the fragment's, else the remembered tab's)
	pending: null,         // the route being opened
	token: 0,              // bumped by every navigation; a stale one stops
	timer: null,
	step: null,
	switching: false,      // the router itself is switching a tab
	syncTimer: null,

	// ---- the tab table ----

	tab: function (slug) {
		for (var i = 0; i < this.TABS.length; i++) {
			var t = this.TABS[i];
			if (t.slug === slug || (t.aliases && t.aliases.indexOf(slug) >= 0))
				return t;
		}
		return null;
	},

	tabByHref: function (href) {
		for (var i = 0; i < this.TABS.length; i++) {
			if (this.TABS[i].href === href)
				return this.TABS[i];
		}
		return null;
	},

	sub: function (tab, slug) {
		if (tab == null || !tab.subs)
			return null;
		for (var i = 0; i < tab.subs.length; i++) {
			if (tab.subs[i].slug === slug)
				return tab.subs[i];
		}
		return null;
	},

	subMatches: function (sub, href) {
		href = String(href == null ? "" : href);
		for (var i = 0; i < sub.match.length; i++) {
			var m = sub.match[i];
			if (m.charAt(0) === "#" ? href === m : href.toLowerCase().indexOf(m.toLowerCase()) >= 0)
				return true;
		}
		return false;
	},

	subByHref: function (tab, href) {
		if (tab == null || !tab.subs)
			return null;
		for (var i = 0; i < tab.subs.length; i++) {
			if (this.subMatches(tab.subs[i], href))
				return tab.subs[i];
		}
		return null;
	},

	// The <li> of a top-level tab.
	topItem: function (tab) {
		return $("#tabs > ul > li").filter(function () {
			return $(this).children("a").attr("href") === tab.href;
		}).first();
	},

	// The <li> of a sub-tab inside its page's tab set.
	subItem: function (tab, sub) {
		var that = this;
		return $(tab.panel).find(".ui-tabs-nav").first().children("li").filter(function () {
			return that.subMatches(sub, $(this).children("a").attr("href"));
		}).first();
	},

	visible: function (li) {
		return li.length > 0 && li.css("display") !== "none";
	},

	// ---- parsing ----

	// {tab, sub, run, match, hash} for a hash this router knows, else null.
	parse: function (hash) {
		hash = String(hash == null ? "" : hash);
		if (hash.charAt(0) !== "#")
			hash = "#" + hash;
		for (var i = 0; i < this.ROUTES.length; i++) {
			var m = this.ROUTES[i].pattern.exec(hash);
			if (m)
				return {tab: this.ROUTES[i].tab, sub: this.ROUTES[i].sub, run: this.ROUTES[i].run, match: m, hash: hash};
		}
		var parts = /^#([A-Za-z-]+)(?:\/([A-Za-z0-9-]+))?\/?$/.exec(hash);
		if (!parts)
			return null;
		var tab = this.tab(parts[1]);
		if (tab == null)
			return null;
		var sub = parts[2] ? this.sub(tab, parts[2]) : null;
		return {tab: tab.slug, sub: sub ? sub.slug : null, run: null, match: null, hash: hash};
	},

	// "#help/formats" for a tab and an optional sub-tab
	link: function (tab, sub) {
		return "#" + tab + (sub ? "/" + sub : "");
	},

	// ---- start-up ----

	// The index the hall's tab set starts on: the fragment's tab, else the remembered one, else the Game Hall.  A tab
	// that is hidden for now (Admin, until the player info shows an admin) is opened by start() once it appears.
	initialTabIndex: function () {
		var route = this.parse(window.location.hash);
		if (route == null && window.GempHallTabs) {
			var saved = this.tabByHref(GempHallTabs.readSaved());
			if (saved != null)
				route = {tab: saved.slug, sub: null, run: null, match: null, hash: this.link(saved.slug)};
		}
		this.initial = route;
		this.initialized = true;
		var tab = route != null ? this.tab(route.tab) : null;
		var li = tab != null ? this.topItem(tab) : $();
		return this.visible(li) ? li.index() : 0;
	},

	// Called once the hall's tab set exists.
	start: function () {
		var that = this;
		if (this.started)
			return;
		this.started = true;
		if (!this.initialized)
			this.initialTabIndex();

		$(window).on("hashchange.gempLinks", function () {
			that.go(window.location.hash);
		});
		// Tab switches of the hall's tab set or of a page's sub-tab set (they bubble up as tabsactivate / tabscreate /
		// tabsload).  A switch the viewer makes (a click or a key: the event carries it) replaces a navigation still in
		// progress; a page choosing its own first sub-tab while it loads (Admin) does not.
		$(document).on("tabsactivate.gempLinks tabscreate.gempLinks tabsload.gempLinks", function (event) {
			if (!that.isHallTabSet(event.target))
				return;
			if (event.type === "tabsactivate" && !that.switching && that.pending != null && event.originalEvent)
				that.cancel();
			that.scheduleSync();
		});

		if (this.initial != null)
			this.go(this.initial.hash, true);
		else
			this.scheduleSync();
	},

	isHallTabSet: function (element) {
		if (element == null)
			return false;
		if (element.id === "main")
			return true;
		for (var i = 0; i < this.TABS.length; i++) {
			if (this.TABS[i].panel && element.id === this.TABS[i].panel.substring(1))
				return true;
		}
		return false;
	},

	// ---- navigating ----

	// Opens what the hash names.  False for a hash this router does not know (nothing happens).
	go: function (hash, keepHash) {
		var route = this.parse(hash);
		if (route == null)
			return false;
		var that = this;
		var token = ++this.token;
		clearTimeout(this.timer);
		this.pending = route;
		if (!keepHash && window.location.hash !== route.hash)
			this.replaceHash(route.hash);
		var tries = 0;
		var step = this.step = function () {
			clearTimeout(that.timer);
			if (token !== that.token)
				return;
			if (that.advance(route, token)) {
				that.pending = null;
				that.scheduleSync();
				return;
			}
			if (++tries < that.MAX_TRIES) {
				that.timer = setTimeout(step, that.POLL_MS);
			} else {
				that.pending = null;
				that.scheduleSync();
			}
		};
		step();
		return true;
	},

	// Opens what a link names, from script (e.g. a "Help › Format Definitions" link in a popup).
	open: function (hash) {
		return this.go(hash, false);
	},

	// Something a navigation may be waiting for has just happened (the Admin tab was revealed): try again now.
	poke: function () {
		if (this.pending != null && typeof this.step == "function")
			this.step();
	},

	cancel: function () {
		this.token++;
		clearTimeout(this.timer);
		this.pending = null;
	},

	// One step of opening a route: true once it is done (its handler scheduled), false to be called again.
	advance: function (route, token) {
		var main = $("#main");
		if (!main.length || !main.tabs("instance"))
			return false;
		var tab = this.tab(route.tab);
		var li = this.topItem(tab);
		if (!this.visible(li))
			return false;          // hidden for now (Admin before the player info)
		this.activate(main, li.index());

		var sub = this.sub(tab, route.sub);
		if (sub == null) {
			this.schedule(route, token);
			return true;
		}
		var nested = $(tab.panel);
		if (!nested.length || !nested.tabs("instance"))
			return false;          // the tab's page is still loading
		var subLi = this.subItem(tab, sub);
		if (!subLi.length)
			return true;           // no such sub-tab on this page
		if (!this.visible(subLi))
			return true;           // a sub-tab this viewer cannot open (Admin)
		this.activate(nested, subLi.index());
		if (!this.loaded(subLi))
			return false;
		this.schedule(route, token);
		return true;
	},

	activate: function (tabs, index) {
		if (tabs.tabs("option", "active") === index)
			return;
		this.switching = true;
		try {
			tabs.tabs("option", "active", index);
		} finally {
			this.switching = false;
		}
	},

	// A sub-tab's page is there: a local panel always is; a remote one once jQuery UI has put its page in.
	loaded: function (li) {
		if (li.hasClass("ui-tabs-loading"))
			return false;
		var href = String(li.children("a").attr("href") || "");
		if (href.charAt(0) === "#")
			return true;
		var panel = $("#" + li.attr("aria-controls"));
		return panel.length > 0 && panel.children().length > 0;
	},

	// The handler runs after the scripts of the page just put in (their ready callbacks are queued the same way).
	schedule: function (route, token) {
		var that = this;
		if (typeof route.run != "function")
			return;
		setTimeout(function () {
			if (token !== that.token)
				return;
			try {
				route.run(route.match, route.hash);
			} catch (e) {
				if (window.console)
					console.error("Opening " + route.hash + " failed", e);
			}
		}, 0);
	},

	// ---- the address bar ----

	replaceHash: function (hash) {
		if (window.location.hash === hash || !window.history || typeof window.history.replaceState != "function")
			return;
		try {
			window.history.replaceState(window.history.state, "", hash);
		} catch (ignored) {
			// a sandboxed page may refuse; the link is only a convenience
		}
	},

	// For a page showing something with a link of its own (e.g. a league's drawer): puts that link in the address bar.
	setLink: function (hash) {
		if (this.parse(hash) != null)
			this.replaceHash(hash);
	},

	// {tab, sub} slugs of what is showing (sub null while the tab's page is loading or has no sub-tabs).
	current: function () {
		var main = $("#main");
		if (!main.length || !main.tabs("instance"))
			return null;
		var active = main.tabs("option", "active");
		var a = $("#tabs > ul > li").eq(active).children("a");
		var tab = this.tabByHref(a.attr("href"));
		if (tab == null)
			return null;
		var sub = null;
		var nested = tab.panel ? $(tab.panel) : $();
		if (nested.length && nested.tabs("instance")) {
			var subA = nested.find(".ui-tabs-nav").first().children("li").eq(nested.tabs("option", "active")).children("a");
			sub = this.subByHref(tab, subA.attr("href"));
		}
		return {tab: tab, sub: sub};
	},

	currentTab: function () {
		var current = this.current();
		return current ? current.tab : null;
	},

	// True while a tab that needs a logged-in player is showing (the Game Hall until the tabs exist).
	currentTabNeedsLogin: function () {
		var tab = this.currentTab();
		return tab == null ? true : !!tab.login;
	},

	scheduleSync: function () {
		var that = this;
		clearTimeout(this.syncTimer);
		this.syncTimer = setTimeout(function () {
			that.sync();
		}, 0);
	},

	// Writes the showing tab's link into the address bar, unless the current link already points into that tab
	// (a format, an errata card, a patch note: the page keeps it up to date itself).
	sync: function () {
		if (!this.started || this.pending != null)
			return;
		var current = this.current();
		if (current == null)
			return;
		var subSlug = current.sub ? current.sub.slug : null;
		var hash = window.location.hash;
		var route = this.parse(hash);
		// remember the most specific link each page last showed (a format, an errata card, a patch note...), so
		// coming back to that page puts it back in the address bar instead of the page's generic link
		if (route != null && hash && hash !== this.link(route.tab, route.sub || null)) {
			this.lastLinks = this.lastLinks || {};
			this.lastLinks[route.tab + "/" + (route.sub || "")] = hash;
		}
		if (route != null && route.tab === current.tab.slug && (route.sub || null) === subSlug)
			return;
		var remembered = this.lastLinks ? this.lastLinks[current.tab.slug + "/" + (subSlug || "")] : null;
		this.replaceHash(remembered || this.link(current.tab.slug, subSlug));
	},

	// ---- logging in ----

	// The login page, coming back to what is showing now once logged in (src/Login/login.js).
	loginUrl: function () {
		var hash = window.location.hash;
		return "/gemp-lotr/" + (/^#[A-Za-z0-9_\-\/?=&%.+]{1,200}$/.test(hash) ? hash : "");
	}
};


/**
 * Logged-in or not.  The hall's live parts (the table poll, the chat, the player info) start at once when the page
 * opens on a tab that needs a logged-in player.  When it opens on a public tab (Help, Server Info), the player info
 * is asked for first, quietly: a logged-in player's hall then starts as usual, while a visitor reads on without any
 * "not logged in" popup; the readout says "Not logged in" with a link to the login page, and the chat says so too.
 * The hall starts (and the popup appears) once the visitor opens a tab that needs logging in.
 */
var GempHallSession = {
	hall: null,
	chat: null,
	started: false,
	loggedOut: false,     // a request found the viewer not logged in

	begin: function (hall, chat) {
		var that = this;
		this.hall = hall;
		this.chat = chat;
		$("#main").on("tabsactivate.gempSession", function (event) {
			if (event.target === this && GempLinks.currentTabNeedsLogin())
				that.loginTabOpened();
		});
		if (GempLinks.currentTabNeedsLogin()) {
			this.start();
			return;
		}
		// Every answer but "not logged in" starts the hall as usual (its own error handling takes over from there).
		var errors = {};
		var startAnyway = function () {
			that.start();
		};
		errors["0"] = startAnyway;
		for (var status = 400; status < 600; status++)
			errors[String(status)] = startAnyway;
		errors["401"] = function () {
			that.visitor();
		};
		hall.comm.getPlayerInfoShared(function () {
			that.start();
		}, errors);
	},

	start: function () {
		if (this.started)
			return;
		this.started = true;
		if (this.chat)
			this.chat.start();
		if (this.hall)
			this.hall.start();
	},

	// Not logged in, on a public tab: nothing starts, nothing pops up.
	visitor: function () {
		this.loggedOut = true;
		if (this.started)
			return;
		if (this.hall)
			this.hall.connection.set("loggedout", {action: "login"});
		if (this.chat)
			this.chat.showLoggedOut();
	},

	// A request answered "not logged in".
	markLoggedOut: function () {
		this.loggedOut = true;
	},

	// The viewer opened the Game Hall, Events, My Account or Admin.
	loginTabOpened: function () {
		if (!this.started)
			this.start();
		else if (this.loggedOut && this.hall)
			this.hall.showLoginRequired();
	}
};
