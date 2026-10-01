/**
 * Shared base of every Play / Join flow (Bot, Casual, League and Tournament create forms, and the Join dialog).
 *
 * A flow owns a result box ("Result: Ready.") and reports every server answer through ONE presenter, respond(),
 * which always shows the outcome: in the result box when it is on screen, otherwise (the dialog was closed, or never
 * opened, as for sealed / draft queue joins) in a hall dialog.  Success and failure are read the same way for every
 * endpoint:
 *
 *   XML   <error message="...">      failure      <response message="...">  success      <result>OK</result>  success
 *   JSON  {"error": "..."}           failure      {"response": "OK"}        success
 *   HTTP  0 and 400-599              failure, with a readable message (a 400's JSON "error" or "message" header)
 *
 *   var r = flow.respond({
 *       title: "Join Queue",                    // dialog title when the result box is not visible
 *       onSuccess: function (message) {...},    // e.g. close the popup; return true if it showed the outcome
 *       onFailure: function (message) {...},    //   itself (its own confirmation dialog)
 *       quietSuccess: true                      // success needs no dialog even if the box is hidden (the effect is
 *   });                                         //   visible elsewhere, e.g. the new table in Waiting Tables)
 *   comm.joinQueue(id, deck, r.callback, r.errorMap);
 *
 * Also here: the (i) info toggles (the pattern from includes/admin/leagueAdmin.html), select-value helpers that survive
 * an asynchronous refill, and HTML escaping for server text.
 */
class TableFlow {
	mainHall = null;
	comm = null;
	mainDiv = null;
	formatManager = null;
	deckManager = null;
	resultDiv = null;

	constructor(mainHall, comm, div, formatManager, deckManager, resultDiv) {
		this.mainHall = mainHall;
		this.comm = comm;
		this.mainDiv = $(div);
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		this.resultDiv = resultDiv ? $(resultDiv) : $();
	}

	// ---- the result box ----

	resetResult() {
		this.showResult("Ready.", "ready");
	}

	// kind: "ready" | "pending" | "success" | "error".  Text only: server messages never become markup.
	showResult(text, kind) {
		this.resultDiv
			.text(text == null ? "" : String(text))
			.removeClass("result-ready result-pending result-success result-error")
			.addClass("result-" + (kind || "ready"));
		var label = this.resultDiv.closest(".result-label");
		if (label.length)
			label.show();
	}

	// Kept for the callers that report a form problem before anything is sent.
	updateResult(text) {
		this.showResult(text, "error");
	}

	isResultVisible() {
		var box = this.resultDiv;
		if (!box.length || !box[0].isConnected)
			return false;
		// hidden itself, or inside a hidden form or a closed dialog.  Walks the styles rather than asking for layout,
		// and asks a dialog whether it is open rather than looking at its (possibly still animating) wrapper.
		var dialog = box.closest(".ui-dialog-content");
		var stop = dialog.length ? dialog[0] : null;
		for (var node = box[0]; node != null && node.nodeType == 1; node = node.parentNode) {
			if (node.hidden || $(node).css("display") == "none")
				return false;
			if (node === stop)
				break;
		}
		if (dialog.length && dialog.dialog("instance") && !dialog.dialog("isOpen"))
			return false;
		return true;
	}

	// ---- the one presenter ----

	respond(options) {
		var that = this;
		options = options || {};
		var title = options.title || "Result";
		var finished = false;
		var done = function (ok, message) {
			if (finished)
				return;
			finished = true;
			that.showResult(message, ok ? "success" : "error");
			// onSuccess / onFailure return true when they have shown the outcome themselves (their own dialog)
			var shown = false;
			if (ok && options.onSuccess)
				shown = options.onSuccess(message) === true;
			if (!ok && options.onFailure)
				shown = options.onFailure(message) === true;
			if (shown || (ok && options.quietSuccess))
				return;
			// The result must be seen: if the box is not on screen (any more), say it in a dialog.
			if (!that.isResultVisible())
				that.showDialog(ok ? title : (title + " failed"), message);
		};
		this.showResult("Working…", "pending");
		return {
			callback: function (data) {
				var result = TableFlow.readResponse(data);
				done(result.ok, result.message);
			},
			errorMap: TableFlow.errorMap(function (message) {
				done(false, message);
			})
		};
	}

	showDialog(title, message) {
		if (this.mainHall && typeof this.mainHall.showDialog == "function")
			this.mainHall.showDialog(title, TableFlow.escapeHtml(message).replace(/\n/g, "<br/>"), 240);
	}

	// ---- static helpers ----

	// {ok, message} from whatever an endpoint answered on HTTP 200.
	static readResponse(data) {
		if (data == null || data === "" || data === "OK")
			return {ok: true, message: "OK"};
		if (typeof data == "string") {
			try {
				data = JSON.parse(data);
			} catch (notJson) {
				return {ok: true, message: data};
			}
		}
		if (data.documentElement !== undefined) {
			var root = data.documentElement;
			if (root == null)
				return {ok: true, message: "OK"};
			if (root.tagName == "error")
				return {ok: false, message: root.getAttribute("message") || "The server refused the request."};
			if (root.tagName == "response")
				return {ok: true, message: root.getAttribute("message") || "OK"};
			return {ok: true, message: "OK"};
		}
		if (typeof data == "object") {
			if (data.error != null)
				return {ok: false, message: String(data.error)};
			if (data.response != null)
				return {ok: true, message: String(data.response)};
		}
		return {ok: true, message: "OK"};
	}

	// An errorMap for communication.js covering status 0 and 400-599, so no failure falls through to the hall's
	// global handler; report(message, status) gets a readable sentence.
	static errorMap(report) {
		var messages = {
			"0": "Could not reach the server. Check your internet connection and try again.",
			"401": "You are not logged in. Log in again from the main page.",
			"403": "You do not have permission to do that.",
			"404": "The server could not find that; it may have been removed. Refresh the hall and try again.",
			"409": "That conflicts with the current state on the server (for example, not enough currency).",
			"410": "You were logged out after being inactive. Refresh the page to reconnect.",
			"500": "The server ran into an error. Please try again, and report a bug if it keeps happening."
		};
		var handler = function (xhr) {
			var status = (xhr && xhr.status != null) ? xhr.status : 0;
			var message = messages[String(status)];
			if (status == 400) {
				var detail = TableFlow.errorDetail(xhr);
				message = detail != null ? detail : "The server rejected the request as malformed.";
			} else if (message == null) {
				message = "The server answered with an error (HTTP " + status + ").";
			}
			report(message, status);
		};
		var map = {"0": handler};
		for (var code = 400; code < 600; code++)
			map[String(code)] = handler;
		return map;
	}

	// The message a 400 carries: a JSON body {"error": "..."}, else the "message" header.
	static errorDetail(xhr) {
		if (xhr == null)
			return null;
		var body = xhr.responseJSON;
		if (body == null && xhr.responseText) {
			try {
				body = JSON.parse(xhr.responseText);
			} catch (ignored) {
				body = null;
			}
		}
		if (body != null && body.error != null)
			return String(body.error);
		var header = xhr.getResponseHeader ? xhr.getResponseHeader("message") : null;
		return header ? String(header) : null;
	}

	static escapeHtml(text) {
		return String(text == null ? "" : text)
			.replace(/&/g, "&amp;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;")
			.replace(/"/g, "&quot;")
			.replace(/'/g, "&#39;");
	}

	// ---- (i) info toggles ----
	// <span class="info-toggle" data-for="help-x">i</span> shows / hides <div id="help-x" class="info-text" hidden>,
	// as on includes/admin/leagueAdmin.html; here the toggles are also keyboard buttons.
	static bindInfoToggles(root) {
		root = $(root);
		root.find(".info-toggle").each(function () {
			var toggle = $(this);
			if (!toggle.attr("role"))
				toggle.attr({role: "button", tabindex: "0", "aria-expanded": "false", title: "What is this?"});
			var target = $("#" + toggle.data("for"));
			if (target.length)
				toggle.attr("aria-controls", target.attr("id"));
		});
		root.off(".infoToggle")
			.on("click.infoToggle", ".info-toggle", function (event) {
				event.preventDefault();
				TableFlow.toggleInfo($(this));
			})
			.on("keydown.infoToggle", ".info-toggle", function (event) {
				if (event.key == "Enter" || event.key == " ") {
					event.preventDefault();
					TableFlow.toggleInfo($(this));
				}
			});
	}

	static toggleInfo(toggle) {
		var target = $("#" + toggle.data("for"));
		var show = target.prop("hidden");
		target.prop("hidden", !show);
		toggle.attr("aria-expanded", show ? "true" : "false");
	}

	// ---- select values that survive a refill ----
	// A <select> filled asynchronously (formats, leagues, decks) loses a value set before its options arrive.  The
	// wanted value is kept on the element and re-applied by the refill (see keepWanted).

	static want(select, value) {
		select = $(select);
		select.data("wanted", (value == null || value === "") ? null : String(value));
		TableFlow.applyWanted(select);
	}

	// After a refill: the wanted value if it is an option, else the previous value, else the first enabled option.
	static applyWanted(select, previous) {
		select = $(select);
		var wanted = select.data("wanted");
		if (wanted != null && TableFlow.hasOption(select, wanted)) {
			select.val(wanted);
			return;
		}
		if (previous != null && previous !== "" && TableFlow.hasOption(select, previous)) {
			select.val(previous);
			return;
		}
		if (select[0] && select[0].selectedIndex < 0) {
			var first = select.find("option:not([disabled])").first();
			if (first.length)
				select.val(first.val());
		}
	}

	static hasOption(select, value) {
		var found = false;
		$(select).find("option").each(function () {
			if (this.value === String(value))
				found = true;
		});
		return found;
	}

	// Remembers the viewer's own choice, so a later refill keeps it.
	static keepWanted(select) {
		$(select).on("change.wanted", function () {
			var value = $(this).val();
			if (value != null && value !== "")
				$(this).data("wanted", value);
		});
	}

	// A cookie value, where a stored "" counts as unset (loadFromCookie(name, "") stores "" on first use).
	static cookie(name) {
		var value = $.cookie(name);
		return (value === undefined || value === null || value === "" || value === "null" || value === "undefined") ? null : value;
	}
	static FORMAT_INTRO = "A 'format' is a shorthand for what cards are legal to use and what ruleset is being followed.";

	// What the (i) next to a format shows: what a format is, the format's own description if its definition has one,
	// then its legal sets ("Cards from sets 1-10, V1-V3", the server's setSummary), site block and Ring-bearer
	// skirmish rule, and a link to its entry on Help › Format Definitions, where it can be compared with the others.
	// Used by the Casual, Bot and Tournament forms.
	static renderFormatInfo(target, formatManager, code) {
		target = $(target);
		target.empty();
		target.append($("<div class='format-intro'></div>").text(TableFlow.FORMAT_INTRO));
		var format = formatManager.getFormat(code);
		if (format == null) {
			target.append($("<div></div>").text("Choose a format to see what it allows."));
			return;
		}
		if (format.description)
			target.append($("<div class='format-description'></div>").text(format.description));
		var facts = $("<ul class='format-facts'></ul>");
		if (format.setSummary)
			facts.append($("<li></li>").text(format.setSummary));
		if (format.sites)
			facts.append($("<li></li>").text("Sites: " + format.sites));
		facts.append($("<li></li>").text("Ring-bearer skirmish cancel: " + (format.cancelRingBearerSkirmish ? "Yes" : "No")));
		target.append(facts);
		var link = $("<a class='format-help-link'></a>")
			.attr("href", "#" + GempFormatHelp.anchorId(code))
			.text("Help › Format Definitions")
			.on("click", function (event) {
				event.preventDefault();
				GempFormatHelp.show(code);
			});
		target.append($("<div class='format-links'></div>")
			.append(document.createTextNode("Compare " + format.name + " with the other formats: "))
			.append(link));
	}

	// Kept for older callers: Help › Format Definitions without a particular format.
	static openFormatHelp(code) {
		return GempFormatHelp.show(code || null);
	}
}

/**
 * Help › Format Definitions deep links.  Every format on that page is a <section class="format-entry"
 * id="format-<code>"> (HallRequestHandler.appendFormat / FormatSummary.anchorId), so "#format-<code>" names it:
 *   - the Play popup's format (i) link calls show(code);
 *   - a hall URL ending in #format-<code> (on load, or when the hash changes) does the same.
 * Either way the hall switches to the Help tab and its Format Definitions sub-tab (loading them if needed), then
 * scrolls to the format and highlights it.  includes/help/formatRules.html calls rendered() once the definitions
 * have arrived.
 */
var GempFormatHelp = {
	pending: null,        // the anchor id waiting for the definitions to arrive
	waitTimer: null,

	anchorId: function (code) {
		return "format-" + String(code == null ? "" : code).replace(/[^A-Za-z0-9_-]/g, "-");
	},

	// "format-pc_movie" from "#format-pc_movie", else null
	anchorFromHash: function (hash) {
		var match = /^#(format-[A-Za-z0-9_-]+)$/.exec(hash || "");
		return match ? match[1] : null;
	},

	// From the Play popup: close it, put the format in the URL (shareable) and go there.
	show: function (code) {
		$("#create-table-popup, #join-table-popup").each(function () {
			if ($(this).dialog("instance") && $(this).dialog("isOpen"))
				$(this).dialog("close");
		});
		var anchor = code != null ? this.anchorId(code) : null;
		// the hall's router (hallLinks.js) opens the tab, puts the link in the address bar and calls open(anchor)
		if (window.GempLinks)
			return GempLinks.open(anchor != null ? "#" + anchor : "#help/formats");
		if (anchor != null && window.history && typeof window.history.replaceState == "function") {
			try {
				window.history.replaceState(window.history.state, "", "#" + anchor);
			} catch (ignored) {
				// a sandboxed page may refuse; the link still works
			}
		}
		return this.open(anchor);
	},

	// Switch to Help › Format Definitions and reveal the anchor (null: just the sub-tab).
	open: function (anchor) {
		var that = this;
		this.pending = anchor;
		clearTimeout(this.waitTimer);
		var tries = 0;
		var step = function () {
			if (that.activate()) {
				that.reveal();
				return true;
			}
			if (++tries < 100)
				that.waitTimer = setTimeout(step, 50);
			return false;
		};
		return step();
	},

	// Makes Help › Format Definitions the active tab.  False until both tab sets exist (the hall starts its tabs on
	// document ready; help.html starts its own after it loads); the caller tries again.
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
		var formatsIndex = this.tabIndex(help, "formatRules");
		if (formatsIndex >= 0 && help.tabs("option", "active") !== formatsIndex)
			help.tabs("option", "active", formatsIndex);
		return true;
	},

	tabIndex: function (tabs, hrefPart) {
		var index = -1;
		tabs.find(".ui-tabs-nav").first().find("> li > a").each(function (i) {
			if (String($(this).attr("href")).indexOf(hrefPart) >= 0)
				index = i;
		});
		return index;
	},

	// Called by formatRules.html once the definitions are on the page.
	rendered: function () {
		this.reveal();
	},

	// Set by the Format Definitions page (formatDefinitions.js): called with the anchor before it is looked up, so the
	// page can clear a filter that hides it.
	beforeReveal: null,

	reveal: function () {
		var anchor = this.pending;
		if (anchor == null)
			return false;
		if (typeof this.beforeReveal == "function")
			this.beforeReveal(anchor);
		var entry = document.getElementById(anchor);
		if (entry == null)
			return false;       // not loaded yet: rendered() comes back here
		this.pending = null;
		var section = $(entry);
		$(".format-entry.format-highlight").removeClass("format-highlight");
		// after the tab switch has laid the panel out
		setTimeout(function () {
			if (typeof entry.scrollIntoView == "function")
				entry.scrollIntoView({block: "start", behavior: "smooth"});
			section.addClass("format-highlight");
			setTimeout(function () {
				section.removeClass("format-highlight");
			}, 4000);
		}, 0);
		return true;
	}
};

// A hall URL ending in #format-<code> (on load, or when the hash changes) is routed by GempLinks (hallLinks.js), which
// calls GempFormatHelp.open(anchor) once Help › Format Definitions is showing.
