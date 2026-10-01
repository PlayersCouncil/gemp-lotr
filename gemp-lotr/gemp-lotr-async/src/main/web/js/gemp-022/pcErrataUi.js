/**
 * Help › PC Errata (includes/help/pc-errata.html).
 *
 * The page is a filterable, sortable table of every errata'd card (GET /hall/errata/json, "entries"; built by
 * ErrataCatalog on the server) beside a "slot machine" viewer that shows one entry at a time.  Both fill the Help panel:
 * the left pane's title, filters and column headers stay put and only the rows scroll (in .pc-errata-table).
 *   - ↑/↓, j/k, the mouse wheel over the viewer and a swipe on it step through the filtered list one card at a time.
 *   - Scrolling the rows (wheel, swipe, scrollbar) skims: the selection rides along with the rows, so the viewer skips
 *     ahead several cards per wheel notch, and reaches the first / last card exactly at the top / bottom
 *     (PcErrataUI.followScroll).
 *
 * The viewer shows the title and one card image, centred above the readout of what changed: the stats (type included)
 * one per line, then the game text as Old (plain) and New (additions highlighted).  The image flips between the PC
 * version (the default) and the original (a click on it, or O); a right-click on it (a swipe up on touch) opens the
 * shared zoomable card preview (GempCardPreview, cardPreview.js; the hall's card dialog without it).  Enlarge shows
 * both images side by side over the hall.  The layout is fitted to the viewer (PcErrataUI.layout) so the viewer never
 * scrolls inside: the wheel over it only ever steps the list.
 *
 * Deep links (hash, so the hall keeps working as the page):
 *   #pc-errata                      Help › PC Errata
 *   #pc-errata?format=pc_movie      ... filtered: q (title), set, format, show=recent (see FILTER_KEYS)
 *   #errata-1_45 / #errata-51_45    ... showing that card (base or errata blueprint id); takes the same ?filters
 * The hall's link router calls GempErrataHelp.open(PcErrata.parseHash(hash)); GempErrataHelp.show({format: ...}) does
 * the same from script.  While browsing, the address bar follows the card on show (replaceState).
 *
 * PcErrata holds the pure helpers (parsing, filtering, sorting, the text diff), PcErrataUI the page and
 * GempErrataHelp the hash routing.
 */
var PcErrata = {
	FILTER_KEYS: ["q", "set", "format", "show"],
	// the View chips; "" is everything
	SHOWS: [
		{value: "", label: "All"},
		{value: "recent", label: "Latest"}
	],
	// formats left out of the Format dropdown (a link to them still works)
	HIDDEN_FORMATS: ["rev_tow_sta", "debug"],
	STAT_ORDER: ["Twilight", "Strength", "Vitality", "Resistance", "Site", "Unique"],
	SORTS: {
		num: "Collector's number",
		culture: "Culture",
		name: "Title",
		rev: "Number of PC revisions"
	},
	// Decipher scans: 357 x 497 (sites lie on their side)
	PORTRAIT: 357 / 497,
	LANDSCAPE: 497 / 357,
	preloaded: {},
	preloadLog: [],

	emptyFilters: function () {
		var filters = {};
		$.each(PcErrata.FILTER_KEYS, function (i, key) {
			filters[key] = "";
		});
		return filters;
	},

	// "#errata-51_45?format=pc_movie" -> {card: "51_45", params: {format: "pc_movie"}}; not an errata link -> null
	parseHash: function (hash) {
		var match = /^#(?:pc-errata|errata-([A-Za-z0-9_]+))(?:\?(.*))?$/.exec(hash || "");
		if (!match)
			return null;
		var params = {};
		if (match[2]) {
			$.each(match[2].split("&"), function (i, pair) {
				if (!pair)
					return;
				var eq = pair.indexOf("=");
				var key = eq < 0 ? pair : pair.substring(0, eq);
				var value = eq < 0 ? "" : pair.substring(eq + 1);
				try {
					key = decodeURIComponent(key.replace(/\+/g, " "));
					value = decodeURIComponent(value.replace(/\+/g, " "));
				} catch (malformed) {
					return;
				}
				params[key] = value;
			});
		}
		var card = match[1] || params.card || null;
		delete params.card;
		return {card: card, params: params};
	},

	buildHash: function (card, filters) {
		var parts = [];
		$.each(PcErrata.FILTER_KEYS, function (i, key) {
			if (filters && filters[key])
				parts.push(key + "=" + encodeURIComponent(filters[key]));
		});
		return "#" + (card ? "errata-" + card : "pc-errata") + (parts.length ? "?" + parts.join("&") : "");
	},

	fold: function (text) {
		text = String(text == null ? "" : text).toLowerCase();
		if (text.normalize)
			text = text.normalize("NFD").replace(/[̀-ͯ]/g, "");
		return text;
	},

	// The collector's info as printed on the card: "1U29"; the V-sets' "V3_19" (rarity still unset) as "V3 19".
	collectorLabel: function (entry) {
		var info = entry.collInfo ? String(entry.collInfo).trim() : "";
		if (!info)
			return entry.set + " " + entry.cardNum;
		return info.replace(/_+/g, " ");
	},

	// Adds the derived fields the filters and sorts use.
	prepare: function (entries) {
		$.each(entries || [], function (i, entry) {
			entry.setNum = parseInt(entry.set, 10) || 0;
			entry.cardNum = parseInt(entry.cardNum, 10) || 0;
			entry.formats = entry.formats || [];
			entry.revision = entry.revision || 0;
			// many errata files spell names without the accents ("Lorien Elf" for "Lórien Elf"): show the printed name
			if (entry.before && entry.before.name && PcErrata.fold(entry.before.name) === PcErrata.fold(entry.name))
				entry.name = entry.before.name;
			entry.coll = PcErrata.collectorLabel(entry);
			entry.searchName = PcErrata.fold(entry.name);
			entry.searchColl = PcErrata.fold(entry.coll).replace(/\s+/g, "");
		});
		return entries || [];
	},

	// the typed search as a card number: "1.45", "1_45", "1-45", "1 45", "51_45" -> "1_45"
	searchedNumber: function (q) {
		var match = /^(\d+)\s*[._\- ]\s*(\d+)$/.exec(q.trim());
		return match ? parseInt(match[1], 10) + "_" + parseInt(match[2], 10) : null;
	},

	matches: function (entry, filters) {
		if (filters.set && String(entry.set) !== String(filters.set))
			return false;
		if (filters.format && entry.formats.indexOf(filters.format) < 0)
			return false;
		if (filters.show === "recent" && !entry.recent)
			return false;
		var q = PcErrata.fold(filters.q).trim();
		if (q) {
			// the title; a collector's number ("1U29") or a set and card number ("1.29") also find the card
			if (entry.searchName.indexOf(q) >= 0)
				return true;
			if (q.replace(/\s+/g, "") === entry.searchColl)
				return true;
			var number = PcErrata.searchedNumber(q);
			return number != null && (entry.base === number || entry.id === number);
		}
		return true;
	},

	filter: function (entries, filters) {
		return $.grep(entries, function (entry) {
			return PcErrata.matches(entry, filters);
		});
	},

	byNumber: function (a, b) {
		return (a.setNum - b.setNum) || (a.cardNum - b.cardNum) || (a.id < b.id ? -1 : a.id > b.id ? 1 : 0);
	},

	// A sorted copy; ties fall back to the set/card number order.
	sort: function (entries, key, dir) {
		var text = function (field) {
			return function (a, b) {
				return String(a[field] || "￿").localeCompare(String(b[field] || "￿"));
			};
		};
		var compare = {
			num: PcErrata.byNumber,
			name: text("name"),
			culture: text("culture"),
			rev: function (a, b) {
				return a.revision - b.revision;
			}
		}[key] || PcErrata.byNumber;
		dir = dir < 0 ? -1 : 1;
		return entries.slice().sort(function (a, b) {
			return (dir * compare(a, b)) || PcErrata.byNumber(a, b);
		});
	},

	// ---- what changed ----

	tokens: function (text) {
		return String(text == null ? "" : text).replace(/<[^>]*>/g, "").match(/\s+|[A-Za-z0-9À-ɏ'’]+|[^\sA-Za-z0-9À-ɏ'’]/g) || [];
	},

	// Word diff of two game texts: [{op: "=" | "-" | "+", text}], runs merged.
	diff: function (before, after) {
		var a = PcErrata.tokens(before), b = PcErrata.tokens(after);
		var n = a.length, m = b.length, i, j;
		var lcs = [];
		for (i = 0; i <= n; i++) {
			lcs.push(new Array(m + 1).fill(0));
		}
		for (i = n - 1; i >= 0; i--) {
			for (j = m - 1; j >= 0; j--) {
				lcs[i][j] = a[i] === b[j] ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
			}
		}
		var ops = [];
		var push = function (op, text) {
			var last = ops[ops.length - 1];
			if (last && last.op === op)
				last.text += text;
			else
				ops.push({op: op, text: text});
		};
		i = 0;
		j = 0;
		while (i < n && j < m) {
			if (a[i] === b[j]) {
				push("=", a[i]);
				i++;
				j++;
			} else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
				push("-", a[i++]);
			} else {
				push("+", b[j++]);
			}
		}
		while (i < n)
			push("-", a[i++]);
		while (j < m)
			push("+", b[j++]);
		// Changes separated only by spaces read as one change: "~~a~~ b ~~c~~ d" becomes "~~a c~~ b d".  The
		// spaces go into both sides, so the removals still spell the old text and the insertions the new one.
		var merged = [];
		var k = 0;
		while (k < ops.length) {
			if (ops[k].op === "=") {
				merged.push(ops[k]);
				k++;
				continue;
			}
			var removed = "", inserted = "";
			while (k < ops.length) {
				var op = ops[k];
				if (op.op === "-") {
					removed += op.text;
				} else if (op.op === "+") {
					inserted += op.text;
				} else if (/^\s+$/.test(op.text) && k + 1 < ops.length && ops[k + 1].op !== "=") {
					removed += op.text;
					inserted += op.text;
				} else {
					break;
				}
				k++;
			}
			if (removed)
				merged.push({op: "-", text: removed});
			if (inserted)
				merged.push({op: "+", text: inserted});
		}
		return merged;
	},

	// The diff as two texts: old = unchanged + removed, new = unchanged + added (highlighted).  Old is shown plain.
	sides: function (ops) {
		var old = [], added = [];
		$.each(ops, function (i, op) {
			if (op.op !== "+")
				old.push(op);
			if (op.op !== "-")
				added.push(op);
		});
		return {old: old, "new": added};
	},

	// ops as one plain run of text
	plain: function (ops) {
		return $.map(ops, function (op) {
			return op.text;
		}).join("");
	},

	statText: function (value) {
		if (value === undefined || value === null)
			return "—";
		if (value === true)
			return "yes";
		if (value === false)
			return "no";
		return String(value);
	},

	// [{name, from, to}] for the stats that differ, after the name and the card type if the errata changed them
	// (Still Draws Breath: Event -> Condition)
	statChanges: function (before, after) {
		var changes = [];
		if (!before || !after)
			return changes;
		if (before.name && after.name && PcErrata.fold(before.name) !== PcErrata.fold(after.name))
			changes.push({name: "Name", from: before.name, to: after.name});
		if (before.type && after.type && before.type !== after.type)
			changes.push({name: "Type", from: before.type, to: after.type});
		var b = before.stats || {}, a = after.stats || {};
		var keys = PcErrata.STAT_ORDER.slice();
		$.each([b, a], function (i, stats) {
			$.each(stats, function (key) {
				if (keys.indexOf(key) < 0)
					keys.push(key);
			});
		});
		$.each(keys, function (i, key) {
			if (!(key in b) && !(key in a))
				return;
			if (PcErrata.statText(b[key]) !== PcErrata.statText(a[key]))
				changes.push({name: key, from: PcErrata.statText(b[key]), to: PcErrata.statText(a[key])});
		});
		return changes;
	},

	// {before, after} image urls; before is null when there is nothing to compare with
	imageUrls: function (entry) {
		if (entry == null || typeof Card == "undefined")
			return {before: null, after: null};
		var after = Card.getImageUrl(entry.id);
		var before = null;
		if (entry.kind === "revision") {
			// the V-sets revise a card in place: the original is the same image name with .0, and a revised image's
			// "E" (errata) becomes the set's "S" (LOTR-ENV2E005.1 -> LOTR-ENV2S005.0)
			var first = after.replace(/\.\d+_card\.jpg$/, ".0_card.jpg").replace(/(LOTR-ENV\d+)E(\d+\.0_card\.jpg)$/, "$1S$2");
			before = first !== after ? first : null;
		} else {
			before = Card.getImageUrl(entry.base, false, true);
		}
		return {before: before, after: after};
	},

	// What the card preview (GempCardPreview.open) shows for one side of an entry: the blueprint and the exact image
	// (a V-set card's original is the same blueprint's .0 image), or null when there is no image.
	previewArgs: function (entry, side) {
		if (!entry)
			return null;
		var urls = PcErrata.imageUrls(entry);
		var before = side === "before";
		var url = before ? urls.before : urls.after;
		if (!url)
			return null;
		return {
			blueprintId: before ? entry.base : entry.id,
			imageUrl: url,
			title: entry.name + (before ? " (original)" : " (PC errata)")
		};
	},

	// width / height of the card's image until it has loaded (then its own)
	aspectOf: function (entry) {
		return entry && entry.type === "Site" ? PcErrata.LANDSCAPE : PcErrata.PORTRAIT;
	},

	preload: function (url) {
		if (!url || PcErrata.preloaded[url])
			return;
		PcErrata.preloaded[url] = true;
		PcErrata.preloadLog.push(url);
		var image = new Image();
		image.src = url;
	},

	reducedMotion: function () {
		try {
			return !!(window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches);
		} catch (ignored) {
			return false;
		}
	}
};

var PcErrataUI = Class.extend({
	root: null,
	comm: null,
	data: null,
	entries: null,
	formatNames: null,
	list: null,
	filters: null,
	sortKey: "num",
	sortDir: 1,
	cur: -1,
	showOriginal: false,
	enlarged: false,
	ready: false,
	touched: false,
	animating: null,
	wheel: null,
	edgeWheel: null,
	drag: null,
	searchTimer: null,
	// skimming the rows: the rows' geometry (cached), where the selection rides ({s: scrollTop, y: its y in the rows}),
	// a scroll this page started (ignored by followScroll), and the viewer catching up
	geom: null,
	follow: null,
	autoScroll: null,
	followTimer: null,
	lastFollowShow: 0,
	shownId: null,
	shownIndex: null,
	shareButton: null,
	lastSlide: 0,
	previewedAt: 0,

	DURATION: 240,
	// while the rows are skimmed, the viewer shows the selected card at most this often (ms)
	FOLLOW_INTERVAL: 150,
	// a scroll the page started (scrollToRow) still owns the rows' scroll events this long after it arrives (ms)
	AUTO_SCROLL_SETTLE: 200,

	init: function (root, comm) {
		this.root = $(root);
		this.comm = comm;
		this.filters = PcErrata.emptyFilters();
		this.list = [];
		this.wheel = {sum: 0, last: 0, lastStep: 0};
		this.edgeWheel = {sum: 0, last: 0};
		var last = PcErrataUI.lastState;
		if (last) {
			this.filters = $.extend(PcErrata.emptyFilters(), last.filters);
			this.sortKey = PcErrata.SORTS[last.sortKey] ? last.sortKey : "num";
			this.sortDir = last.sortDir;
		}
		this.build();
		this.bindKeys();
	},

	// ---- skeleton ----

	build: function () {
		var that = this;
		var root = this.root;
		this.summary = root.find(".pc-errata-summary");
		this.chips = root.find(".pc-errata-views");
		this.filterBar = root.find(".pc-errata-filters");
		this.tableBox = root.find(".pc-errata-table");
		this.status = $("<span class='pc-errata-status' aria-live='polite'></span>");

		var viewer = this.viewer = root.find(".pc-errata-viewer");
		this.reel = viewer.find(".pc-errata-reel");
		this.counter = viewer.find(".pc-errata-counter");
		this.prevButton = viewer.find(".pc-errata-prev").on("click", function () {
			that.touched = true;
			that.step(-1);
		});
		this.nextButton = viewer.find(".pc-errata-next").on("click", function () {
			that.touched = true;
			that.step(1);
		});
		// nothing to step through until the list is in
		this.prevButton.prop("disabled", true);
		this.nextButton.prop("disabled", true);
		// Original | PC errata: which version the card shows (in the viewer's bar, so the card gets the height)
		this.flipSwitch = viewer.find(".pc-errata-flip-switch");
		this.flipSwitch.find("button").on("click", function () {
			that.setShowOriginal($(this).attr("data-original") === "1");
		});
		this.enlargeButton = viewer.find(".pc-errata-enlarge").on("click", function () {
			that.setEnlarged(!that.enlarged);
		});
		// Share: the share link of the card on show (kept in step by show / showEmpty)
		viewer.find(".pc-errata-share").remove();
		if (window.GempShareLinks) {
			this.shareButton = GempShareLinks.button("errata", null, "Share")
				.addClass("pc-errata-share")
				.attr("aria-label", "Copy a share link to the errata on show")
				.prop("disabled", true);
			this.enlargeButton.before(this.shareButton);
		}
		// behind the enlarged viewer: a click closes it, the wheel still steps the list
		this.backdrop = root.find(".pc-errata-backdrop").on("click", function () {
			that.setEnlarged(false);
		});
		this.bindWheel(viewer.add(this.backdrop));
		this.bindSwipe();
		this.bindPreview();
		this.bindListScroll();
		if (typeof TableFlow != "undefined" && TableFlow.bindInfoToggles)
			TableFlow.bindInfoToggles(root);

		this.fit();
		$(window).off("resize.pcErrata").on("resize.pcErrata", function () {
			var ui = PcErrataUI.current;
			if (ui)
				ui.fit();
		});
		if (typeof ResizeObserver != "undefined") {
			var panel = this.panel();
			if (panel.length) {
				this.observer = new ResizeObserver(function () {
					that.fit();
				});
				this.observer.observe(panel[0]);
				this.observer.observe(root[0]);
				// the intro grows when its "i" text opens: the rows get less room
				var intro = root.find(".pc-errata-intro")[0];
				if (intro)
					this.observer.observe(intro);
			}
		}
	},

	// The element the page sits in (in the hall: #helpMain, around the Help sub-tab panels).
	panel: function () {
		for (var el = this.root[0] && this.root[0].parentElement; el && el.nodeType === 1; el = el.parentElement) {
			var overflow = window.getComputedStyle ? window.getComputedStyle(el).overflowY : "";
			if (overflow === "auto" || overflow === "scroll")
				return $(el);
		}
		return this.root.closest(".ui-tabs-panel");
	},

	// The page is as tall as the panel shows (the hall's chat takes the rest of the window): the viewer and the left
	// pane both run from where the layout starts to the panel's bottom, so the panel itself never scrolls; only the
	// rows do (under the left pane's title, filters and column headers).  The layout stacks when the panel is narrow.
	fit: function () {
		var panel = this.panel();
		var height = panel.length ? panel[0].clientHeight : 0;
		if (!height)
			height = $(window).height() || 600;
		var top = 8;
		var layout = this.root.find(".pc-errata-layout")[0];
		if (panel.length && layout && layout.getBoundingClientRect) {
			var start = layout.getBoundingClientRect().top - panel[0].getBoundingClientRect().top - (panel[0].clientTop || 0)
				+ panel[0].scrollTop;
			if (start > 0 && start < height / 2)
				top = start;
		}
		var style = panel.length && window.getComputedStyle ? window.getComputedStyle(panel[0]) : null;
		var rootStyle = window.getComputedStyle ? window.getComputedStyle(this.root[0]) : null;
		var below = (style ? parseFloat(style.paddingBottom) || 0 : 0) + (rootStyle ? parseFloat(rootStyle.paddingBottom) || 0 : 0);
		var view = Math.max(260, Math.min(1400, Math.floor(height - top - below - 1)));
		this.root[0].style.setProperty("--pc-errata-view-h", view + "px");
		var width = this.root[0].clientWidth || this.root.width() || 0;
		this.root.toggleClass("pc-errata-narrow", width > 0 && width < 700);
		this.root.toggleClass("pc-errata-compact", width > 0 && width < 1000);
		this.fitShare();
		this.geom = null;
		var that = this;
		this.reel.children(".pc-errata-frame").not(".pc-errata-leaving").each(function () {
			that.layout($(this));
		});
		this.rebaseFollow();
	},

	// ---- data ----

	load: function () {
		var that = this;
		if (PcErrataUI.cache) {
			this.setData(PcErrataUI.cache);
			return;
		}
		this.tableBox.empty().append($("<p class='pc-errata-loading'></p>").text("Loading the errata…"));
		var errors = typeof neutralErrorMap == "function" ? neutralErrorMap(this.tableBox, "the errata list") : {};
		this.comm.getErrata(function (json) {
			PcErrataUI.cache = json;
			// the page may have been left (and reloaded) while the list was on its way
			if (PcErrataUI.current === that)
				that.setData(json);
		}, errors);
	},

	setData: function (json) {
		var that = this;
		this.data = json || {};
		this.entries = PcErrata.prepare(this.data.entries || []);
		this.formatNames = {};
		$.each(this.data.formats || [], function (i, format) {
			that.formatNames[format.code] = format.name;
		});
		this.renderSummary();
		this.renderFilters();
		this.ready = true;

		var route = typeof GempErrataHelp != "undefined" ? GempErrataHelp.take() : null;
		if (route) {
			this.applyRoute(route);
		} else {
			var last = PcErrataUI.lastState;
			this.refresh(last ? last.id : null);
		}
		this.takeFocus();
	},

	// Opening the sub-tab leaves the focus on its tab, where the arrow keys switch tabs: hand it to the viewer, so ↑ / ↓
	// browse straight away (only from the tabs or nowhere: never out of a field).
	takeFocus: function () {
		var active = document.activeElement;
		var viewer = this.viewer[0];
		if (!viewer || !viewer.focus || !this.root.is(":visible"))
			return;
		if (active == null || active === document.body || $(active).is(".ui-tabs-anchor, [role=tab]")) {
			try {
				viewer.focus({preventScroll: true});
			} catch (ignored) {
				viewer.focus();
			}
		}
	},

	renderSummary: function () {
		var counts = this.data.counts || {};
		var total = this.entries.length;
		var errata = (counts.errata || 0) + (counts.playtest || 0);
		var revisions = counts.revision || 0;
		var text = total + " cards: " + errata + " PC errata of Decipher cards";
		if (revisions)
			text += " and " + revisions + " revised V-set cards";
		this.summary.text(text + ".");
	},

	countFor: function (show) {
		var filters = PcErrata.emptyFilters();
		filters.show = show;
		return PcErrata.filter(this.entries, filters).length;
	},

	// ---- filters ----

	renderFilters: function () {
		var that = this;
		var entries = this.entries;

		this.chips.empty();
		$.each(PcErrata.SHOWS, function (i, show) {
			var count = that.countFor(show.value);
			if (show.value && count === 0)
				return;
			var chip = $("<button type='button' class='pc-errata-chip' role='radio'></button>")
				.attr("data-show", show.value)
				.append($("<span class='pc-errata-chip-label'></span>").text(show.label))
				.append($("<span class='pc-errata-chip-count'></span>").text(count));
			chip.on("click", function () {
				that.touched = true;
				that.filters.show = show.value;
				that.syncControls();
				that.refresh();
			});
			that.chips.append(chip);
		});

		var bar = this.filterBar.empty();
		var field = function (label, control, cls) {
			var id = "pc-errata-f-" + cls;
			control.attr("id", id);
			return $("<div class='pc-errata-field'></div>").addClass("pc-errata-field-" + cls)
				.append($("<label></label>").attr("for", id).text(label))
				.append(control);
		};
		var select = function (key, allLabel, options) {
			var control = $("<select></select>").attr("data-key", key);
			control.append($("<option value=''></option>").text(allLabel));
			$.each(options, function (i, option) {
				control.append($("<option></option>").attr("value", option.value).text(option.label));
			});
			control.on("change", function () {
				that.touched = true;
				that.filters[key] = $(this).val() || "";
				that.refresh();
			});
			return control;
		};

		var search = $("<input type='search' data-key='q' autocomplete='off' spellcheck='false' placeholder='Title'>");
		search.on("input", function () {
			var value = $(this).val();
			clearTimeout(that.searchTimer);
			that.searchTimer = setTimeout(function () {
				that.touched = true;
				that.filters.q = value;
				that.refresh();
			}, 150);
		});

		var sets = {};
		$.each(entries, function (i, entry) {
			if (!sets[entry.set])
				sets[entry.set] = {value: String(entry.set), num: entry.setNum, name: entry.setName, count: 0};
			sets[entry.set].count++;
		});
		var setOptions = $.map(Object.keys(sets), function (key) {
			return sets[key];
		}).sort(function (a, b) {
			return a.num - b.num;
		});
		setOptions = $.map(setOptions, function (set) {
			return {value: set.value, label: set.value + (set.name ? " – " + set.name : "") + " (" + set.count + ")"};
		});

		var formatOptions = [];
		$.each(this.data.formats || [], function (i, format) {
			if (format.count > 0 && PcErrata.HIDDEN_FORMATS.indexOf(format.code) < 0)
				formatOptions.push({value: format.code, label: format.name + " (" + format.count + ")"});
		});

		bar.append(field("Title", search, "q"))
			.append(field("Set", select("set", "All sets", setOptions), "set"))
			.append(field("Format", select("format", "All formats", formatOptions), "format"))
			.append(this.status);
		this.syncControls();
	},

	// puts this.filters into the controls (after a deep link or a chip)
	syncControls: function () {
		var that = this;
		this.filterBar.find("[data-key]").each(function () {
			var control = $(this);
			var key = control.attr("data-key");
			var value = that.filters[key] || "";
			if (control.is("select")) {
				if (value && control.find("option").filter(function () {
					return this.value === value;
				}).length === 0) {
					// a link to a format (or set) that the list leaves out: keep it visible as the choice
					var label = key === "format" ? (that.formatNames[value] || value) : value;
					var count = PcErrata.filter(that.entries, $.extend(PcErrata.emptyFilters(), {format: key === "format" ? value : "", set: key === "set" ? value : ""})).length;
					control.append($("<option class='pc-errata-extra'></option>").attr("value", value).text(label + " (" + count + ")"));
				}
				control.val(value);
			} else if (control.val() !== value) {
				control.val(value);
			}
		});
		this.chips.find(".pc-errata-chip").each(function () {
			var chip = $(this);
			var on = chip.attr("data-show") === (that.filters.show || "");
			chip.toggleClass("pc-errata-chip-on", on).attr("aria-checked", on ? "true" : "false");
		});
	},

	// Re-filters and re-sorts; keeps the current card when it is still listed (or shows keepId's).
	refresh: function (keepId) {
		var current = keepId || (this.cur >= 0 && this.list[this.cur] ? this.list[this.cur].id : null);
		this.list = PcErrata.sort(PcErrata.filter(this.entries, this.filters), this.sortKey, this.sortDir);
		this.syncControls();
		var index = current ? this.indexOf(current) : -1;
		this.cur = -1;
		this.renderStatus();
		this.renderTable();
		if (this.list.length === 0) {
			this.showEmpty();
			this.tableBox.scrollTop(0);
			this.remember();
			this.updateHash();
			return;
		}
		this.select(index >= 0 ? index : 0, {direction: 0});
	},

	indexOf: function (id) {
		for (var i = 0; i < this.list.length; i++) {
			if (this.list[i].id === id || this.list[i].base === id)
				return i;
		}
		return -1;
	},

	renderStatus: function () {
		var total = this.entries.length;
		var shown = this.list.length;
		var text = shown === total ? total + " cards" : shown + " of " + total + " cards";
		if (shown === 0) {
			if (this.filters.format && this.onlyFilter("format"))
				text = "No PC errata are in effect in " + (this.formatNames[this.filters.format] || this.filters.format) + ".";
			else
				text = "No errata match these filters.";
		}
		this.status.text(text);
		this.status.toggleClass("pc-errata-status-empty", shown === 0);
	},

	onlyFilter: function (key) {
		var that = this;
		var others = $.grep(PcErrata.FILTER_KEYS, function (k) {
			return k !== key && that.filters[k];
		});
		return others.length === 0;
	},

	// ---- table: every row of the filtered list ----

	renderTable: function () {
		var that = this;
		var box = this.tableBox.empty();
		var table = $("<table class='gemp-table pc-errata-list'></table>").attr("aria-label", "PC errata");
		var head = $("<tr></tr>");
		var header = function (label, key, cls, title) {
			var th = $("<th scope='col'></th>").addClass(cls);
			var mark = that.sortKey === key ? (that.sortDir > 0 ? " ▲" : " ▼") : "";
			var button = $("<button type='button' class='gemp-table-sort'></button>").text(label)
				.attr("title", title || "Sort by " + PcErrata.SORTS[key].toLowerCase())
				.append($("<span class='gemp-table-sort-mark'></span>").text(mark));
			button.on("click", function () {
				that.touched = true;
				that.setSort(key);
			});
			th.append(button);
			if (that.sortKey === key)
				th.attr("aria-sort", that.sortDir > 0 ? "ascending" : "descending");
			head.append(th);
		};
		header("Card", "num", "pc-errata-col-num", "The collector's number; sort by set and card number");
		header("Culture", "culture", "pc-errata-col-culture");
		header("Title", "name", "pc-errata-col-name");
		header("#", "rev", "pc-errata-col-rev", "How many times the Player's Council has revised the card; sort by it");
		table.append($("<thead></thead>").append(head));

		var body = $("<tbody></tbody>");
		if (this.list.length === 0)
			body.append($("<tr></tr>").append($("<td class='gemp-table-empty' colspan='4'></td>").text(this.status.text())));
		var rows = [];
		for (var i = 0; i < this.list.length; i++)
			rows.push(this.row(this.list[i], i));
		body.append(rows);
		body.on("click", "tr[data-index]", function () {
			that.touched = true;
			that.select(parseInt($(this).attr("data-index"), 10), {scroll: false});
		});
		table.append(body);
		box.append(table);
		this.rows = body.children("tr[data-index]");
		this.highlighted = null;
		this.geom = null;
		this.follow = null;
		this.highlight();
	},

	row: function (entry, index) {
		var tr = $("<tr></tr>").attr("data-index", index).attr("data-id", entry.id);
		// the collector's number, then the New tag (in the same place on every row)
		var num = $("<div class='pc-errata-num'></div>")
			.append($("<span class='pc-errata-coll'></span>").text(entry.coll));
		if (entry.recent)
			num.append($("<span class='pc-errata-badge pc-errata-badge-new'></span>").text("New").attr("title", "In the latest batch of errata"));
		tr.append($("<td class='pc-errata-col-num'></td>").append(num)
			.attr("title", (entry.setName || ("Set " + entry.set)) + ", card " + entry.cardNum));
		// the culture's icon at its own shape (they are not square: Shire is 49 x 20), named in the tooltip
		var culture = $("<td class='pc-errata-col-culture'></td>");
		if (entry.culture)
			culture.attr("title", entry.culture);
		if (entry.cultureCode) {
			culture.append($("<img class='pc-errata-culture-icon' height='18'>")
				.attr({src: "images/cultures/" + entry.cultureCode + ".png", alt: entry.culture || entry.cultureCode})
				.on("error", function () {
					// no icon for it: the name instead
					$(this).replaceWith($("<span class='pc-errata-culture-name'></span>").text(entry.culture || ""));
				}));
		}
		tr.append(culture);
		var name = $("<td class='pc-errata-col-name'></td>").append($("<span class='pc-errata-name'></span>").text(entry.name));
		if (entry.kind === "playtest")
			name.append($("<span class='pc-errata-badge'></span>").text("playtest"));
		tr.append(name);
		tr.append($("<td class='pc-errata-col-rev'></td>").text(entry.revision || ""));
		return tr;
	},

	highlight: function () {
		if (this.highlighted)
			this.highlighted.removeClass("pc-errata-current").removeAttr("aria-current");
		this.highlighted = null;
		if (this.cur >= 0 && this.rows && this.rows[this.cur]) {
			this.highlighted = $(this.rows[this.cur]).addClass("pc-errata-current").attr("aria-current", "true");
		}
	},

	setSort: function (key) {
		if (this.sortKey === key) {
			this.sortDir = -this.sortDir;
		} else {
			this.sortKey = key;
			this.sortDir = 1;
		}
		this.refresh();
	},

	// ---- selection ----

	// options: direction (the viewer's slide; 0 = none), scroll (false: leave the rows where they are), follow (the
	// rows are being skimmed: the viewer catches up, see scheduleShow)
	select: function (index, options) {
		options = options || {};
		if (this.list.length === 0)
			return;
		index = Math.max(0, Math.min(this.list.length - 1, index));
		var previous = this.cur;
		var direction = options.direction != null ? options.direction : (index > previous ? 1 : index < previous ? -1 : 0);
		if (previous < 0)
			direction = 0;
		this.cur = index;
		this.highlight();
		this.updateCounter();
		if (options.follow) {
			this.scheduleShow();
		} else {
			clearTimeout(this.followTimer);
			this.followTimer = null;
			if (this.shownId !== this.list[index].id || options.direction === 0) {
				this.showOriginal = false;
				this.show(this.list[index], direction, index);
			}
			this.afterSelect();
		}
		if (options.scroll !== false)
			this.scrollToRow();
		if (!options.follow)
			this.rebaseFollow();
	},

	afterSelect: function () {
		this.preloadAround(this.cur);
		this.remember();
		this.updateHash();
	},

	// Skimming: the row highlight moves at once; the viewer shows the selected card at most every FOLLOW_INTERVAL ms
	// (and once the rows stop), sliding only when the last slide is over.
	scheduleShow: function () {
		var that = this;
		var run = function () {
			that.followTimer = null;
			var entry = that.list[that.cur];
			if (!entry)
				return;
			that.lastFollowShow = Date.now();
			if (that.shownId !== entry.id) {
				var direction = that.shownIndex == null ? 0 : (that.cur > that.shownIndex ? 1 : -1);
				if (Date.now() - that.lastSlide < that.DURATION)
					direction = 0;
				that.showOriginal = false;
				that.show(entry, direction, that.cur);
			}
			that.afterSelect();
		};
		if (this.followTimer)
			return;
		var wait = Math.max(0, this.lastFollowShow + this.FOLLOW_INTERVAL - Date.now());
		this.followTimer = setTimeout(run, Math.max(wait, 30));
	},

	step: function (delta) {
		if (this.cur < 0 || this.list.length === 0)
			return false;
		var next = this.cur + delta;
		if (next < 0 || next >= this.list.length) {
			this.bump(delta);
			return false;
		}
		this.select(next, {direction: delta > 0 ? 1 : -1});
		return true;
	},

	// at either end of the list the reel nudges and settles back
	bump: function (delta) {
		var viewer = this.viewer;
		if (PcErrata.reducedMotion())
			return;
		var cls = delta > 0 ? "pc-errata-bump-end" : "pc-errata-bump-start";
		viewer.removeClass("pc-errata-bump-end pc-errata-bump-start");
		if (viewer[0])
			void viewer[0].offsetWidth;
		viewer.addClass(cls);
		setTimeout(function () {
			viewer.removeClass(cls);
		}, 300);
	},

	updateCounter: function () {
		var n = this.list.length;
		this.counter.text(n ? (this.cur + 1) + " of " + n : "0 of 0");
		this.prevButton.prop("disabled", this.cur <= 0);
		this.nextButton.prop("disabled", this.cur < 0 || this.cur >= n - 1);
	},

	preloadAround: function (index) {
		var that = this;
		$.each([1, -1, 2, -2], function (i, delta) {
			var entry = that.list[index + delta];
			if (!entry)
				return;
			var urls = PcErrata.imageUrls(entry);
			PcErrata.preload(urls.after);
			PcErrata.preload(urls.before);
		});
	},

	// ---- the rows: where each one is, keeping the current one in view, and skimming ----

	// {tops, heights, headH}: each row's top and height in the scrolled rows (the column headers stick over the first
	// headH px); null while the rows are not laid out (hidden)
	geometry: function () {
		if (this.geom)
			return this.geom;
		var box = this.tableBox[0];
		if (!box || !this.rows || !this.rows.length || !box.getBoundingClientRect)
			return null;
		var base = box.getBoundingClientRect().top + (box.clientTop || 0) - box.scrollTop;
		var tops = [], heights = [], laidOut = false;
		for (var i = 0; i < this.rows.length; i++) {
			var r = this.rows[i].getBoundingClientRect();
			tops.push(r.top - base);
			heights.push(r.height);
			if (r.height > 0)
				laidOut = true;
		}
		if (!laidOut)
			return null;
		var head = this.tableBox.find("thead")[0];
		this.geom = {tops: tops, heights: heights, headH: head ? head.getBoundingClientRect().height : 0};
		return this.geom;
	},

	rowCenter: function (geom, index) {
		return geom.tops[index] + geom.heights[index] / 2;
	},

	// the row at y (in the scrolled rows)
	rowAt: function (geom, y) {
		var lo = 0, hi = geom.tops.length - 1;
		while (lo < hi) {
			var mid = (lo + hi + 1) >> 1;
			if (geom.tops[mid] <= y)
				lo = mid;
			else
				hi = mid - 1;
		}
		return lo;
	},

	rowHeight: function () {
		var geom = this.geometry();
		if (!geom || geom.tops.length < 2)
			return 25;
		return Math.max(8, (geom.tops[geom.tops.length - 1] - geom.tops[0]) / (geom.tops.length - 1));
	},

	// Brings the current row to the middle of the rows' window (as far as the rows scroll): the list moves past as the
	// viewer steps through it.
	scrollToRow: function () {
		var box = this.tableBox[0];
		var geom = this.geometry();
		if (!box || !geom || this.cur < 0 || !this.root.is(":visible"))
			return;
		var room = box.clientHeight - geom.headH;
		var target = this.rowCenter(geom, this.cur) - geom.headH - room / 2;
		target = Math.max(0, Math.min(box.scrollHeight - box.clientHeight, Math.round(target)));
		if (Math.abs(target - box.scrollTop) < 2)
			return;
		var smooth = !PcErrata.reducedMotion() && Math.abs(target - box.scrollTop) < 3 * box.clientHeight;
		this.autoScroll = {target: target, until: Date.now() + (smooth ? 900 : 200)};
		if (smooth && typeof box.scrollTo == "function") {
			try {
				box.scrollTo({top: target, behavior: "smooth"});
				return;
			} catch (ignored) {
				// old browsers: jump
			}
		}
		box.scrollTop = target;
	},

	// Where the selection rides in the rows, from now on: the current row's middle (kept inside the rows' window).
	rebaseFollow: function () {
		var box = this.tableBox[0];
		var geom = this.cur >= 0 ? this.geometry() : null;
		if (!box || !geom || this.cur >= geom.tops.length) {
			this.follow = null;
			return;
		}
		var s = box.scrollTop;
		var y = this.rowCenter(geom, this.cur);
		var low = s + geom.headH, high = s + box.clientHeight - 1;
		if (high > low)
			y = Math.max(low, Math.min(high, y));
		this.follow = {s: s, y: y};
	},

	bindListScroll: function () {
		var that = this;
		this.tableBox.on("scroll", function () {
			that.followScroll();
		});
		// a hand on the rows takes over from a scroll this page started
		this.tableBox.on("pointerdown touchstart keydown", function () {
			that.autoScroll = null;
		});
		this.tableBox.on("wheel", function (event) {
			that.onListWheel(event);
		});
	},

	// The rows scrolled (wheel, swipe, scrollbar, keys): the selection moves along with them.  Scrolling up it closes in
	// on the first row, scrolling down on the last, so that the top of the rows selects the first card and the bottom
	// the last; in between it moves about as far as the rows do (a few more rows per screen, to make up the distance).
	followScroll: function () {
		var box = this.tableBox[0];
		if (!box || !this.ready || this.cur < 0 || this.list.length === 0)
			return;
		var s = box.scrollTop;
		var auto = this.autoScroll;
		if (auto) {
			// ours (scrollToRow): the selection is already where it should be.  Arriving does not end it at once: a
			// smooth scroll can report a pixel short of its target and then the target itself, and at the very top /
			// bottom of the rows that last pixel would read as the viewer skimming to the first / last card.  So it
			// ends a moment after arriving (or when its time is up); a hand on the rows ends it at once (bindListScroll,
			// onListWheel).
			var now = Date.now();
			if (now > auto.until)
				this.autoScroll = null;
			else if (Math.abs(s - auto.target) <= 1)
				auto.until = Math.min(auto.until, now + this.AUTO_SCROLL_SETTLE);
			this.rebaseFollow();
			return;
		}
		var geom = this.geometry();
		if (!geom)
			return;
		var follow = this.follow;
		if (!follow) {
			this.rebaseFollow();
			return;
		}
		if (Math.abs(s - follow.s) < 0.5)
			return;   // no movement (a re-render, a resize)
		var max = Math.max(0, box.scrollHeight - box.clientHeight);
		var last = geom.tops.length - 1;
		var first = this.rowCenter(geom, 0), end = this.rowCenter(geom, last);
		var y;
		if (s <= 0)
			y = first;
		else if (s >= max - 1)
			y = end;
		else if (s < follow.s)
			y = first + (follow.y - first) * (s / follow.s);
		else
			y = end - (end - follow.y) * ((max - s) / (max - follow.s));
		this.follow = {s: s, y: y};
		var index = this.rowAt(geom, y);
		if (index !== this.cur) {
			this.touched = true;
			this.select(index, {scroll: false, follow: true});
		}
	},

	// The wheel over the rows scrolls them (and followScroll moves the selection).  When they can't scroll any further
	// that way (or all fit), the selection keeps going at the same pace, up to the first / last card.
	onListWheel: function (event) {
		var e = event.originalEvent || event;
		this.autoScroll = null;
		if (e.ctrlKey || !this.ready || this.cur < 0 || this.list.length === 0)
			return;
		var box = this.tableBox[0];
		var dy = e.deltaY * (e.deltaMode === 1 ? 16 : e.deltaMode === 2 ? box.clientHeight : 1);
		if (!dy)
			return;
		var max = box.scrollHeight - box.clientHeight;
		var atEdge = max <= 1 || (dy < 0 && box.scrollTop <= 0) || (dy > 0 && box.scrollTop >= max - 1);
		if (!atEdge)
			return;
		event.preventDefault();
		var edge = this.edgeWheel;
		var now = Date.now();
		if (now - edge.last > 250 || (edge.sum > 0) !== (dy > 0))
			edge.sum = 0;
		edge.last = now;
		edge.sum += dy;
		var height = this.rowHeight();
		var rows = edge.sum > 0 ? Math.floor(edge.sum / height) : Math.ceil(edge.sum / height);
		if (!rows)
			return;
		edge.sum -= rows * height;
		var next = Math.max(0, Math.min(this.list.length - 1, this.cur + rows));
		if (next === this.cur) {
			this.bump(rows);
			return;
		}
		this.touched = true;
		this.select(next, {scroll: false, follow: true});
		this.rebaseFollow();
	},

	remember: function () {
		PcErrataUI.lastState = {
			filters: $.extend({}, this.filters),
			sortKey: this.sortKey,
			sortDir: this.sortDir,
			id: this.cur >= 0 && this.list[this.cur] ? this.list[this.cur].id : null
		};
	},

	// The address bar follows what is shown (replaceState: no history entry per card), once the viewer has been used or
	// the page was opened from a link.
	updateHash: function () {
		if (!this.touched || !this.root.is(":visible"))
			return;
		var entry = this.list[this.cur];
		var hash = PcErrata.buildHash(entry ? entry.id : null, this.filters);
		if (window.location.hash === hash || !window.history || typeof window.history.replaceState != "function")
			return;
		try {
			window.history.replaceState(window.history.state, "", hash);
		} catch (ignored) {
			// a sandboxed page may refuse
		}
	},

	// ---- the viewer ----

	show: function (entry, direction, index) {
		var reel = this.reel;
		this.shownId = entry.id;
		this.shownIndex = index != null ? index : this.cur;
		if (this.shareButton)
			GempShareLinks.setId(this.shareButton, entry.id);
		var frame = this.frame(entry);
		this.syncFlip(frame);
		reel.children(".pc-errata-leaving").remove();
		var old = reel.children(".pc-errata-frame");
		var that = this;
		if (!direction || old.length === 0 || PcErrata.reducedMotion()) {
			old.remove();
			reel.append(frame);
			this.layout(frame);
			return;
		}
		this.lastSlide = Date.now();
		frame.addClass("pc-errata-entering").css("transform", "translateY(" + (direction * 100) + "%)");
		reel.append(frame);
		this.layout(frame);
		if (frame[0])
			void frame[0].offsetHeight;
		frame.addClass("pc-errata-moving").css("transform", "");
		old.addClass("pc-errata-leaving pc-errata-moving").css("transform", "translateY(" + (-direction * 100) + "%)");
		clearTimeout(this.animating);
		this.animating = setTimeout(function () {
			reel.children(".pc-errata-leaving").remove();
			reel.children(".pc-errata-frame").removeClass("pc-errata-entering pc-errata-moving");
			that.animating = null;
		}, this.DURATION + 60);
	},

	showEmpty: function () {
		this.shownId = null;
		this.shownIndex = null;
		if (this.shareButton)
			GempShareLinks.setId(this.shareButton, null);
		this.reel.empty().append($("<div class='pc-errata-frame pc-errata-frame-empty'></div>")
			.append($("<p></p>").text(this.status.text()))
			.append($("<p class='pc-errata-hint'></p>").text("Change the search, or pick another set, format or view.")));
		this.updateCounter();
	},

	head: function (entry) {
		var head = $("<div class='pc-errata-frame-head'></div>");
		head.append($("<span class='pc-errata-frame-name'></span>").text(entry.name));
		var meta = $("<span class='pc-errata-frame-meta'></span>");
		var kind = $.grep([entry.culture, entry.type], function (x) {
			return !!x;
		}).join(" ");
		meta.text(entry.coll + (kind ? " · " + kind : ""));
		meta.attr("title", (entry.setName ? entry.setName + " (set " + entry.set + ")" : "Set " + entry.set) + ", card " + entry.cardNum);
		head.append(meta);
		if (entry.recent)
			head.append($("<span class='pc-errata-badge pc-errata-badge-new'></span>").text("New").attr("title", "In the latest batch of errata"));
		if (entry.revision >= 1)
			head.append($("<span class='pc-errata-badge'></span>").text("PC revision " + entry.revision));
		return head;
	},

	// Compact: the title and the card centred in the stage, the readout of what changed below it.  Enlarge: the title,
	// both cards side by side, the readout below them.
	frame: function (entry) {
		var frame = $("<div class='pc-errata-frame'></div>").attr("data-id", entry.id)
			.toggleClass("pc-errata-frame-full", this.enlarged);
		frame.data("entry", entry);
		var urls = PcErrata.imageUrls(entry);
		var body = $("<div class='pc-errata-body'></div>");
		if (this.enlarged) {
			frame.append(this.head(entry));
			var pair = $("<div class='pc-errata-pair'></div>");
			pair.append(this.figure(urls.before, "Original", entry, "before"));
			pair.append($("<div class='pc-errata-arrow' aria-hidden='true'></div>"));
			pair.append(this.figure(urls.after, "PC errata", entry, "after"));
			body.append(pair);
		} else {
			body.append($("<div class='pc-errata-stage'></div>").append(this.head(entry), this.flipper(entry, urls)));
		}
		body.append(this.note(entry));
		frame.append(body);
		return frame;
	},

	// The card on show in the compact viewer: the PC version, or (a click on it, O, or the bar's switch) the original.
	flipper: function (entry, urls) {
		var flip = $("<div class='pc-errata-flip'></div>");
		var box = $("<div class='pc-errata-cardbox'></div>")
			.attr("title", "Click (or press O) to see the other version; right-click to zoom");
		box.append(this.card(urls.before, entry, "before").addClass("pc-errata-side-before"));
		box.append(this.card(urls.after, entry, "after").addClass("pc-errata-side-after"));
		// a click (a tap) on the card flips it: see bindSwipe, which tells a tap from a swipe
		flip.append(box);
		return flip;
	},

	syncFlip: function (scope) {
		var original = this.showOriginal;
		this.flipSwitch.find("button").each(function () {
			var on = ($(this).attr("data-original") === "1") === original;
			$(this).toggleClass("pc-errata-flip-on", on).attr("aria-pressed", on ? "true" : "false");
		});
		(scope || this.reel).find(".pc-errata-cardbox").toggleClass("pc-errata-showing-before", original);
	},

	setShowOriginal: function (original) {
		original = !!original;
		if (original === this.showOriginal || this.enlarged)
			return;
		this.showOriginal = original;
		var frame = this.reel.children(".pc-errata-frame").not(".pc-errata-leaving").last();
		var box = frame.find(".pc-errata-cardbox");
		var that = this;
		if (box.length && !PcErrata.reducedMotion()) {
			// a quick turn of the card: squeeze, swap, open
			box.addClass("pc-errata-turning");
			clearTimeout(this.turning);
			this.turning = setTimeout(function () {
				that.syncFlip(frame);
				box.removeClass("pc-errata-turning");
			}, 90);
		} else {
			this.syncFlip(frame);
		}
	},

	figure: function (url, caption, entry, side) {
		var figure = $("<figure class='pc-errata-figure'></figure>");
		figure.append($("<figcaption></figcaption>").text(caption));
		figure.append($("<div class='pc-errata-cardbox'></div>").attr("title", "Right-click to zoom").append(this.card(url, entry, side)));
		return figure;
	},

	// A card image drawn the way the game and the deck builder draw cards (Card.CreateSimpleCardDiv: rounded corners
	// and a black border overlay), which hides the white corners and the edge line of the scans.  Sized by layout().
	card: function (url, entry, side) {
		var that = this;
		var missing = side === "before" ? "No image of the original" : "No image";
		var placeholder = function () {
			return $("<div class='pc-errata-noimage'></div>").text(missing);
		};
		var holder = $("<div class='pc-errata-card'></div>").attr("data-side", side);
		if (!url) {
			holder.append(placeholder());
			return holder;
		}
		var card;
		if (typeof Card != "undefined" && Card.CreateSimpleCardDiv) {
			card = Card.CreateSimpleCardDiv("images/pixel.png", null, false, false, 0);
		} else {
			card = $("<div class='card'><img width='100%' height='100%'><div class='borderOverlay'></div></div>");
		}
		var image = card.children("img").first();
		image.attr({draggable: "false", alt: side === "before" ? "Original" : "PC errata"});
		image.on("load", function () {
			if (this.naturalWidth && this.naturalHeight) {
				holder.data("aspect", this.naturalWidth / this.naturalHeight);
				holder.data("naturalHeight", this.naturalHeight);
				var frame = holder.closest(".pc-errata-frame");
				if (frame.length && !frame.hasClass("pc-errata-leaving"))
					that.layout(frame);
			}
		});
		image.on("error", function () {
			card.replaceWith(placeholder());
			holder.addClass("pc-errata-card-missing");
		});
		image.attr("src", url);
		holder.append(card);
		return holder;
	},

	// What changed: the stats (and type) one per line, then the game text as Old (plain) and New (additions marked).
	note: function (entry) {
		var note = $("<div class='pc-errata-note'></div>");
		var changes = PcErrata.statChanges(entry.before, entry.after);
		if (changes.length) {
			var list = $("<ul class='pc-errata-stats'></ul>");
			$.each(changes, function (i, change) {
				list.append($("<li></li>")
					.append($("<span class='pc-errata-stat-name'></span>").text(change.name))
					.append($("<span class='pc-errata-stat-from'></span>").text(change.from))
					.append($("<span class='pc-errata-stat-arrow' aria-label='to'></span>").text("→"))
					.append($("<ins></ins>").text(change.to)));
			});
			note.append($("<div class='pc-errata-side pc-errata-statblock'></div>").append(list));
		}
		var after = entry.after ? entry.after.gametext : "";
		var texts = $("<div class='pc-errata-texts'></div>");
		var side = function (label, cls, ops) {
			var text = $("<p class='pc-errata-gametext'></p>");
			text.append($("<span class='pc-errata-side-label'></span>").text(label));
			$.each(ops, function (i, op) {
				if (op.op === "+")
					text.append($("<ins></ins>").text(op.text));
				else
					text.append(document.createTextNode(op.text));
			});
			return $("<div class='pc-errata-side'></div>").addClass(cls).append(text);
		};
		if (entry.before) {
			var ops = PcErrata.diff(entry.before.gametext, after);
			var changed = $.grep(ops, function (op) {
				return op.op !== "=";
			}).length > 0;
			if (changed) {
				var sides = PcErrata.sides(ops);
				texts.append(side("Old", "pc-errata-old", [{op: "=", text: PcErrata.plain(sides.old)}]),
					side("New", "pc-errata-new", sides["new"]));
			} else {
				texts.append(side("Game text (unchanged)", "pc-errata-same", ops));
				if (!changes.length)
					note.append($("<p class='pc-errata-hint'></p>").text(
						"The game text and stats read the same; the change is elsewhere on the card (compare the images)."));
			}
		} else {
			texts.append(side("Game text", "pc-errata-same", [{op: "=", text: PcErrata.tokens(after).join("")}]));
		}
		note.append(texts);
		return note;
	},

	// ---- fitting the frame to the viewer: no scrollbar inside it, ever ----

	aspect: function (frame, side) {
		var holder = frame.find(".pc-errata-card[data-side='" + side + "']");
		return holder.data("aspect") || PcErrata.aspectOf(frame.data("entry"));
	},

	// the scans' own height (the taller of the two images; 497, a Decipher portrait scan, until they load)
	naturalHeight: function (frame) {
		var tallest = 0;
		frame.find(".pc-errata-card").each(function () {
			tallest = Math.max(tallest, $(this).data("naturalHeight") || 0);
		});
		if (tallest)
			return tallest;
		return PcErrata.aspectOf(frame.data("entry")) > 1 ? 357 : 497;
	},

	sizeCard: function (frame, side, width, height) {
		width = Math.max(1, Math.round(width));
		height = Math.max(1, Math.round(height));
		var holder = frame.find(".pc-errata-card[data-side='" + side + "']");
		holder.css({width: width + "px", height: height + "px"});
		// the game's border: a thirtieth of the longer side (CardGroup's layoutCardElem)
		holder.find(".borderOverlay").css({"border-width": Math.floor(Math.max(width, height) / 30) + "px"});
	},

	layout: function (frame) {
		if (!frame || !frame.length || frame.hasClass("pc-errata-frame-empty"))
			return;
		var body = frame.find(".pc-errata-body");
		var width = body[0] ? body[0].clientWidth : 0;
		var height = body[0] ? body[0].clientHeight : 0;
		if (!width || !height)
			return;   // not laid out (hidden, or no layout engine)
		if (frame.hasClass("pc-errata-frame-full"))
			this.layoutFull(frame, body, width, height);
		else
			this.layoutCompact(frame, body, width, height);
	},

	// The readout across the bottom at its own height, the title and card centred in what is left above it, the card
	// as large as that allows (up to a quarter over the scan's own size).  The text shrinks a little (13px to 10px)
	// while the card would otherwise get less than about two thirds of the height.  A short viewer (a small window)
	// can't fit both that way: then the card goes on the left and the readout beside it, as before (PcErrataUI.READOUT).
	layoutCompact: function (frame, body, width, height) {
		var note = frame.find(".pc-errata-note");
		var stage = frame.find(".pc-errata-stage");
		var head = frame.find(".pc-errata-frame-head");
		var flip = frame.find(".pc-errata-flip");
		var gapOf = function (element) {
			var style = element[0] && window.getComputedStyle ? window.getComputedStyle(element[0]) : null;
			return style ? parseFloat(style.rowGap) || 0 : 0;
		};
		var aspectAfter = this.aspect(frame, "after"), aspectBefore = this.aspect(frame, "before");
		var aspect = Math.max(aspectAfter, aspectBefore);
		var natural = this.naturalHeight(frame) * PcErrataUI.MAX_UPSCALE;
		var mode = PcErrataUI.READOUT;

		var bottom = this.fitBottom(frame, body, stage, head, note, width, height, aspect, natural, gapOf);
		var chosen = bottom;
		if (mode === "side" || (mode === "auto" && (bottom.clipped || bottom.cardHeight < PcErrataUI.BOTTOM_MIN_CARD))) {
			var side = this.fitSide(frame, body, stage, head, note, width, height, aspect, natural, gapOf);
			// (only for a clearly bigger card: below it reads better, sites especially)
			if (mode === "side" || (side.cardHeight > bottom.cardHeight * 1.1 && (!side.clipped || bottom.clipped)))
				chosen = side;
		}
		this.placeReadout(frame, body, stage, head, note, chosen.side);
		note.css({"font-size": chosen.font + "px", width: chosen.side ? chosen.noteWidth + "px" : "",
			"max-height": chosen.noteMax + "px"});
		var cardHeight = Math.floor(chosen.cardHeight);
		flip.css({width: Math.round(cardHeight * aspect) + "px"});
		flip.find(".pc-errata-cardbox").css({height: cardHeight + "px"});
		this.sizeCard(frame, "after", cardHeight * aspectAfter, cardHeight);
		this.sizeCard(frame, "before", cardHeight * aspectBefore, cardHeight);
		frame.toggleClass("pc-errata-clipped", chosen.clipped);
	},

	// the title over the card (readout below), or over the readout (readout beside the card)
	placeReadout: function (frame, body, stage, head, note, side) {
		body.toggleClass("pc-errata-body-side", !!side);
		if (side && head.parent()[0] !== note[0])
			note.prepend(head);
		else if (!side && head.parent()[0] !== stage[0])
			stage.prepend(head);
	},

	fitBottom: function (frame, body, stage, head, note, width, height, aspect, natural, gapOf) {
		this.placeReadout(frame, body, stage, head, note, false);
		note.css({width: "", "max-height": "none"});
		var gap = gapOf(body), stageGap = gapOf(stage);
		var headHeight = head.outerHeight() || 0;
		// the card's height with the whole body to itself (no readout)
		var alone = Math.min(natural, width / aspect, height - headHeight - stageGap);
		var fonts = [13, 12.5, 12, 11.5, 11, 10.5, 10];
		var font = fonts[fonts.length - 1];
		for (var i = 0; i < fonts.length; i++) {
			note.css({"font-size": fonts[i] + "px"});
			if (Math.min(alone, height - note[0].scrollHeight - gap - headHeight - stageGap) >= alone * 0.66 - 0.5) {
				font = fonts[i];
				break;
			}
		}
		note.css({"font-size": font + "px"});
		var noteHeight = note[0].scrollHeight;
		// the readout gives way (clipped: "Enlarge to read it all") below a small card
		var cardHeight = Math.max(Math.min(alone, 110), Math.min(alone, height - noteHeight - gap - headHeight - stageGap));
		var noteMax = Math.max(0, Math.floor(height - gap - headHeight - stageGap - cardHeight));
		return {side: false, font: font, cardHeight: cardHeight, noteMax: noteMax, clipped: noteHeight > noteMax + 1};
	},

	// The card on the left, as tall as the viewer (at most half its width), the readout beside it; the text shrinks
	// a little before the card does.
	fitSide: function (frame, body, stage, head, note, width, height, aspect, natural, gapOf) {
		this.placeReadout(frame, body, stage, head, note, true);
		note.css({"max-height": "none"});
		var gap = 12;
		var maxWidth = Math.min(height * aspect, natural * aspect, width * (aspect > 1 ? 0.56 : 0.5), width - 170 - gap);
		maxWidth = Math.max(60, maxWidth);
		var tries = [[13, 1], [13, 0.88], [12, 1], [12, 0.88], [11.5, 0.8], [11, 0.72], [10.5, 0.64], [10, 0.56]];
		var chosen = null;
		for (var i = 0; i < tries.length; i++) {
			note.css({"font-size": tries[i][0] + "px", width: Math.floor(width - maxWidth * tries[i][1] - gap) + "px"});
			if (note[0].scrollHeight <= height + 1) {
				chosen = tries[i];
				break;
			}
		}
		var clipped = !chosen;
		chosen = chosen || tries[tries.length - 1];
		var cardWidth = maxWidth * chosen[1];
		return {side: true, font: chosen[0], cardHeight: cardWidth / aspect, noteWidth: Math.floor(width - cardWidth - gap),
			noteMax: Math.floor(height), clipped: clipped};
	},

	// Enlarge: both cards, then the text across the width below them (Old under Original, New under PC errata).
	// Sites (landscape) go side by side when that makes them bigger, else one above the other with the text beside.
	layoutFull: function (frame, body, width, height) {
		var note = frame.find(".pc-errata-note");
		var pair = frame.find(".pc-errata-pair");
		var arrow = pair.find(".pc-errata-arrow");
		// a figure is its caption, a gap, then the card
		var figure = pair.find(".pc-errata-figure").first();
		var styleGap = function (element) {
			var style = element[0] && window.getComputedStyle ? window.getComputedStyle(element[0]) : null;
			return style ? parseFloat(style.rowGap) || 0 : 0;
		};
		var captionHeight = (pair.find("figcaption").first().outerHeight(true) || 18) + styleGap(figure);
		var bodyGap = styleGap(body);
		var aspectBefore = this.aspect(frame, "before"), aspectAfter = this.aspect(frame, "after");
		var aspect = Math.max(aspectBefore, aspectAfter);
		var gap = 14, arrowSize = 34;
		var fonts = [15, 14, 13, 12];
		var that = this;

		var measureBelow = function (font) {
			note.css({"font-size": font + "px", width: width + "px"});
			return note[0].scrollHeight;
		};
		var measureBeside = function (font, textWidth) {
			note.css({"font-size": font + "px", width: textWidth + "px"});
			return note[0].scrollHeight;
		};
		// side by side: the cards' height, as big as the width and what the text leaves allow
		var sideBySide = function (font) {
			var textHeight = measureBelow(font);
			var byWidth = (width - arrowSize - 2 * gap) / 2 / aspect;
			var byHeight = height - textHeight - captionHeight - bodyGap;
			return {font: font, cardHeight: Math.min(byWidth, byHeight), byWidth: byWidth, fits: byHeight > 0};
		};
		var stacked = function (font) {
			// the cards in a column on the left, the text in the rest
			var cardHeight = (height - 2 * captionHeight - arrowSize) / 2;
			var cardWidth = Math.min(cardHeight * aspect, width * 0.55);
			var textHeight = measureBeside(font, Math.floor(width - cardWidth - bodyGap));
			return {font: font, cardHeight: cardWidth / aspect, cardWidth: cardWidth, fits: textHeight <= height + 1};
		};

		var best = null, i, option;
		for (i = 0; i < fonts.length; i++) {
			option = sideBySide(fonts[i]);
			// big enough: the cards take at least 60% of what the width alone would allow them, or of the height
			if (option.fits && option.cardHeight >= Math.min(option.byWidth, height * 0.62) * 0.98) {
				best = option;
				break;
			}
		}
		if (!best) {
			option = sideBySide(fonts[fonts.length - 1]);
			best = option.cardHeight > 40 ? option : {font: fonts[fonts.length - 1], cardHeight: 40};
		}
		best.layout = "row";
		var sites = PcErrataUI.ENLARGED_SITES;
		if (aspect > 1 && sites !== "row") {
			for (i = 0; i < fonts.length; i++) {
				option = stacked(fonts[i]);
				if (option.fits) {
					if (sites === "column" || option.cardHeight > best.cardHeight * 1.04) {
						option.layout = "column";
						best = option;
					}
					break;
				}
			}
		}

		var column = best.layout === "column";
		frame.toggleClass("pc-errata-full-column", column);
		// no bigger than a quarter over the scan's own size: past that it only gets blurry
		var natural = this.naturalHeight(frame) * PcErrataUI.MAX_UPSCALE;
		if (best.cardHeight > natural) {
			if (column)
				best.cardWidth = best.cardWidth * natural / best.cardHeight;
			best.cardHeight = natural;
		}
		var cardHeight = Math.max(20, Math.floor(best.cardHeight));
		if (column) {
			var cardWidth = Math.floor(best.cardWidth);
			note.css({"font-size": best.font + "px", width: Math.floor(width - cardWidth - bodyGap) + "px"});
			pair.css({width: cardWidth + "px"});
			arrow.text("↓").css({height: arrowSize + "px", width: "auto"});
		} else {
			note.css({"font-size": best.font + "px", width: width + "px"});
			pair.css({width: ""});
			arrow.text("→").css({height: cardHeight + "px", width: arrowSize + "px"});
		}
		that.sizeCard(frame, "before", cardHeight * aspectBefore, cardHeight);
		that.sizeCard(frame, "after", cardHeight * aspectAfter, cardHeight);
		frame.toggleClass("pc-errata-clipped", body[0].scrollHeight > body[0].clientHeight + 1);
	},

	setEnlarged: function (on) {
		on = !!on;
		this.enlarged = on;
		this.viewer.toggleClass("pc-errata-viewer-full", on);
		this.backdrop.prop("hidden", !on);
		this.enlargeButton.text(on ? "Close" : "Enlarge").attr("aria-pressed", on ? "true" : "false");
		$("body").toggleClass("pc-errata-enlarged", on);
		this.fitShare();
		if (this.cur >= 0 && this.list[this.cur]) {
			this.showOriginal = false;
			this.show(this.list[this.cur], 0);
		}
		if (on && this.viewer[0] && this.viewer[0].focus) {
			try {
				this.viewer[0].focus({preventScroll: true});
			} catch (ignored) {
				this.viewer[0].focus();
			}
		}
	},

	// ---- the card preview (right-click; a swipe up on touch) ----

	bindPreview: function () {
		var that = this;
		this.reel.on("contextmenu", ".pc-errata-card", function (event) {
			var side = $(this).attr("data-side");
			if (!that.preview(side, event))
				return;   // no image: the browser's own menu
			event.preventDefault();
		});
	},

	// Opens the shared zoomable card preview (GempCardPreview, from cardPreview.js) on one side of the current card;
	// without it, the hall's card dialog.  False when there is nothing to show.
	preview: function (side, event) {
		var entry = this.list[this.cur];
		var args = PcErrata.previewArgs(entry, side);
		if (!args)
			return false;
		var e = event ? (event.originalEvent || event) : null;
		this.previewedAt = Date.now();
		var open = function () {
			if (window.GempCardPreview && typeof window.GempCardPreview.open == "function") {
				window.GempCardPreview.open(args, e);
			} else {
				// the hall opens a clicked .cardHint in its card dialog (GameHall.js): hand it one
				var hint = $("<span class='cardHint' hidden></span>");
				hint[0].setAttribute("value", args.blueprintId);
				hint.appendTo("body");
				hint.trigger("click");
				hint.remove();
			}
		};
		// with the right button still down, a popup that closes on a click outside it would close on its release
		if (e && e.type === "contextmenu" && e.buttons) {
			$(document).one("mouseup.pcErrataPreview", function () {
				setTimeout(open, 0);
			});
		} else {
			open();
		}
		return true;
	},

	// ---- input ----

	bindKeys: function () {
		$(document).off("keydown.pcErrata").on("keydown.pcErrata", function (event) {
			var ui = PcErrataUI.current;
			if (ui)
				ui.onKey(event);
		});
	},

	onKey: function (event) {
		if (!this.ready || !this.root.is(":visible") || event.altKey || event.ctrlKey || event.metaKey)
			return;
		var target = $(event.target);
		if (target.is("input, select, textarea, [contenteditable]"))
			return;
		// the tabs use the arrow keys themselves; j / k / o still work from there
		var onTab = target.is("[role=tab], .ui-tabs-anchor");
		if (onTab && ["j", "J", "k", "K", "o", "O"].indexOf(event.key) < 0)
			return;
		// a dialog over the hall (the card preview, a popup) has the keys, Esc included (it may have closed itself
		// on this very key before it got here)
		if ($(".ui-dialog:visible").length || target.closest(".ui-dialog").length)
			return;
		var key = event.key;
		var handled = true;
		if (key === "Escape" && this.enlarged) {
			this.setEnlarged(false);
		} else if (key === "ArrowDown" || key === "j" || key === "J") {
			this.touched = true;
			this.step(1);
		} else if (key === "ArrowUp" || key === "k" || key === "K") {
			this.touched = true;
			this.step(-1);
		} else if (key === "o" || key === "O") {
			this.setShowOriginal(!this.showOriginal);
		} else if (key === "Home" && this.list.length) {
			this.touched = true;
			this.select(0);
		} else if (key === "End" && this.list.length) {
			this.touched = true;
			this.select(this.list.length - 1);
		} else {
			handled = false;
		}
		if (handled)
			event.preventDefault();
	},

	// The wheel over the viewer only ever steps through the list (the page does not scroll under it): a notch (or a
	// trackpad's worth of ~40px) is one card, at most one card per 140ms, so a flick does not fly past everything.
	bindWheel: function (targets) {
		var that = this;
		targets.on("wheel", function (event) {
			var e = event.originalEvent || event;
			if (e.ctrlKey)
				return;   // pinch / Ctrl+wheel zoom
			event.preventDefault();
			if (!that.ready)
				return;
			var dy = e.deltaY * (e.deltaMode === 1 ? 16 : e.deltaMode === 2 ? 400 : 1);
			var now = Date.now();
			var wheel = that.wheel;
			if (now - wheel.last > 250)
				wheel.sum = 0;
			wheel.last = now;
			wheel.sum += dy;
			if (Math.abs(wheel.sum) >= 40 && now - wheel.lastStep >= 140) {
				that.touched = true;
				that.step(wheel.sum > 0 ? 1 : -1);
				wheel.sum = 0;
				wheel.lastStep = now;
			}
		});
	},

	// A swipe (touch, pen or a mouse drag) on the reel: up or left is the next card, down or right the previous; a swipe
	// up that starts on the card, by touch or pen, opens the card preview instead.  The card follows the finger a little
	// while it moves.
	bindSwipe: function () {
		var that = this;
		var reel = this.reel;
		var current = function () {
			return reel.children(".pc-errata-frame").not(".pc-errata-leaving").last();
		};
		reel.on("pointerdown", function (event) {
			var e = event.originalEvent || event;
			if ((e.button != null && e.button > 0) || !that.ready || $(e.target).closest("a, button").length)
				return;
			var card = $(e.target).closest(".pc-errata-card");
			that.drag = {x: e.clientX, y: e.clientY, id: e.pointerId, moved: false, at: Date.now(),
				onCard: $(e.target).closest(".pc-errata-flip .pc-errata-cardbox").length > 0,
				side: card.length ? card.attr("data-side") : null,
				touch: e.pointerType === "touch" || e.pointerType === "pen"};
			try {
				if (e.pointerId != null && reel[0].setPointerCapture)
					reel[0].setPointerCapture(e.pointerId);
			} catch (ignored) {
				// the document still sees the moves over the viewer
			}
		});
		reel.on("pointermove", function (event) {
			var e = event.originalEvent || event;
			var drag = that.drag;
			if (!drag)
				return;
			var dy = e.clientY - drag.y, dx = e.clientX - drag.x;
			if (Math.abs(dy) > 4 || Math.abs(dx) > 4)
				drag.moved = true;
			if (drag.moved && !PcErrata.reducedMotion() && Math.abs(dy) >= Math.abs(dx) && !(drag.touch && drag.side && dy < 0))
				current().css("transform", "translateY(" + Math.round(dy * 0.35) + "px)");
		});
		var end = function (event, cancelled) {
			var e = event.originalEvent || event;
			var drag = that.drag;
			if (!drag)
				return;
			that.drag = null;
			current().css("transform", "");
			// a long press opened the preview (contextmenu): not a tap as well
			if (cancelled || that.previewedAt >= drag.at)
				return;
			// a tap on the card (not a swipe) flips it; with the pointer captured, the click itself goes to the reel
			if (!drag.moved && drag.onCard) {
				that.setShowOriginal(!that.showOriginal);
				return;
			}
			var dy = e.clientY - drag.y, dx = e.clientX - drag.x;
			if (drag.touch && drag.side && dy <= -40 && Math.abs(dy) >= Math.abs(dx)) {
				that.preview(drag.side, null);
				return;
			}
			var step = 0;
			if (Math.abs(dy) >= 40 && Math.abs(dy) >= Math.abs(dx))
				step = dy < 0 ? 1 : -1;
			else if (Math.abs(dx) >= 50 && Math.abs(dx) > Math.abs(dy))
				step = dx < 0 ? 1 : -1;
			if (step) {
				that.touched = true;
				that.step(step);
			}
		};
		reel.on("pointerup", function (event) {
			end(event, false);
		});
		reel.on("pointercancel", function (event) {
			end(event, true);
		});
	},

	// ---- deep links ----

	// {card, params} from a link: the filters it names (and nothing else), then its card.
	applyRoute: function (route) {
		var that = this;
		var filters = PcErrata.emptyFilters();
		$.each(route.params || {}, function (key, value) {
			if (PcErrata.FILTER_KEYS.indexOf(key) >= 0)
				filters[key] = value;
		});
		if (route.params && route.params.sort && PcErrata.SORTS[route.params.sort]) {
			this.sortKey = route.params.sort;
			this.sortDir = route.params.dir === "desc" ? -1 : 1;
		}
		this.filters = filters;
		var card = route.card;
		if (card) {
			var known = $.grep(this.entries, function (entry) {
				return entry.id === card || entry.base === card;
			});
			if (known.length === 0) {
				this.refresh();
				this.status.text("Card " + card + " has no PC errata. " + this.status.text());
			} else {
				if (!PcErrata.matches(known[0], this.filters)) {
					// the link's filters leave its card out: the card wins
					this.filters = PcErrata.emptyFilters();
				}
				this.refresh(known[0].id);
			}
		} else {
			this.refresh();
		}
		setTimeout(function () {
			that.fit();
			that.scrollToRow();
			that.takeFocus();
		}, 0);
	},

	// Share gives way when the viewer's bar is too narrow (Enlarge must stay in view): first to just its icon, then, in
	// a bar with no room even for that (a window narrower than about 1100px), it is left out (the address bar still
	// links to the card on show)
	fitShare: function () {
		var button = this.shareButton;
		if (!button)
			return;
		var bar = button.parent()[0];
		var overflows = function () {
			return bar && bar.clientWidth > 0 && bar.scrollWidth > bar.clientWidth + 1;
		};
		button.removeClass("share-link-icon-only pc-errata-share-hidden");
		if (overflows())
			button.addClass("share-link-icon-only");
		if (overflows())
			button.addClass("pc-errata-share-hidden");
	},

	destroy: function () {
		if (this.observer)
			this.observer.disconnect();
		clearTimeout(this.searchTimer);
		clearTimeout(this.animating);
		clearTimeout(this.turning);
		clearTimeout(this.followTimer);
		$(document).off("mouseup.pcErrataPreview");
		$("body").removeClass("pc-errata-enlarged");
	}
});

PcErrataUI.MAX_UPSCALE = 1.25;
// Where the compact viewer puts the readout of what changed: "auto" (below the card, unless that would cut the text
// off or leave the card less than BOTTOM_MIN_CARD px tall, as in a short viewer: then beside it, if that shows the card
// clearly bigger), "bottom" or "side" (always)
PcErrataUI.READOUT = "auto";
PcErrataUI.BOTTOM_MIN_CARD = 160;
// Enlarge with sites (landscape): "auto" puts them one above the other when that shows them bigger than side by side
// (a tall, narrow window), "row" / "column" always side by side / one above the other
PcErrataUI.ENLARGED_SITES = "auto";
PcErrataUI.current = null;
PcErrataUI.cache = null;
PcErrataUI.lastState = null;

// Called by includes/help/pc-errata.html each time the sub-tab loads (jQuery UI reloads it on every visit).
PcErrataUI.mount = function (root, comm) {
	if (PcErrataUI.current)
		PcErrataUI.current.destroy();
	var ui = new PcErrataUI(root, comm);
	PcErrataUI.current = ui;
	ui.load();
	return ui;
};

/**
 * #pc-errata / #errata-<id> links: switch to Help › PC Errata (loading it if needed) and hand the link to the page.
 */
var GempErrataHelp = {
	pending: null,
	waitTimer: null,

	take: function () {
		var route = this.pending;
		this.pending = null;
		return route;
	},

	// From script, e.g. Format Definitions: GempErrataHelp.show({format: "pc_movie"}) or show({card: "1_45"}).
	show: function (params) {
		params = params || {};
		var hash = PcErrata.buildHash(params.card || null, params);
		if (window.history && typeof window.history.replaceState == "function") {
			try {
				window.history.replaceState(window.history.state, "", hash);
			} catch (ignored) {
				// the link still works
			}
		}
		return this.open(PcErrata.parseHash(hash));
	},

	// The hall's link router calls this with PcErrata.parseHash(location.hash).
	open: function (route) {
		var that = this;
		this.pending = route;
		clearTimeout(this.waitTimer);
		var tries = 0;
		var step = function () {
			if (that.activate()) {
				var ui = PcErrataUI.current;
				if (ui && ui.ready && ui.root.closest("body").length && that.pending) {
					ui.touched = true;
					ui.applyRoute(that.take());
				}
				return true;
			}
			if (++tries < 100)
				that.waitTimer = setTimeout(step, 50);
			else
				that.pending = null;   // no Help tab to show it in: do not hand it to a later visit
			return false;
		};
		return step();
	},

	// Makes Help › PC Errata the active tab; false until both tab sets exist (the caller tries again).
	activate: function () {
		var main = $("#main");
		if (!main.length || !main.tabs("instance"))
			return false;
		var helpIndex = this.tabIndex(main, "includes/help.html");
		if (helpIndex < 0)
			return false;
		if (main.tabs("option", "active") !== helpIndex)
			main.tabs("option", "active", helpIndex);
		var help = $("#helpMain");
		if (!help.length || !help.tabs("instance"))
			return false;
		var errataIndex = this.tabIndex(help, "pc-errata");
		if (errataIndex < 0)
			return false;
		if (help.tabs("option", "active") !== errataIndex)
			help.tabs("option", "active", errataIndex);
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

// #pc-errata / #errata-<id> hall URLs (on load, or when the hash changes) are routed by GempLinks (hallLinks.js), which
// calls GempErrataHelp.open(PcErrata.parseHash(hash)) once Help › PC Errata is showing.
