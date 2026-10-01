var chat;
var hall;

// The hall's top-level tabs (and every nested tab set loaded into them).
//  - A remote tab (Help, Events, Server Info, My Account, ...) is fetched on its first visit only.  jQuery UI would
//    otherwise re-fetch it on every visit, re-running its script: new UI objects, lost state, leaked dialogs.
//  - The active top-level tab is remembered in localStorage.  Which tab opens is decided by GempLinks
//    (js/gemp-022/hallLinks.js): a link's #fragment first, else the remembered tab, else the Game Hall.
var GempHallTabs = {
	STORAGE_KEY: "gemp.hall.activeTab",

	// jQuery UI tabs beforeLoad handler: cancel the request when the tab's content is already there.
	loadOnce: function (event, ui) {
		if (ui.tab.data("loaded")) {
			event.preventDefault();
			return;
		}
		ui.jqXHR.done(function () {
			ui.tab.data("loaded", true);
		});
	},

	readSaved: function () {
		try {
			return window.localStorage.getItem(this.STORAGE_KEY);
		} catch (e) {
			return null;
		}
	},

	save: function (href) {
		try {
			window.localStorage.setItem(this.STORAGE_KEY, href);
		} catch (e) {
			// storage unavailable: the hall simply opens on its first tab next time
		}
	},

	// ---- refresh on return ----
	// A tab's page is loaded once, so the data it shows would never change.  Each page instead registers the parts
	// that show live data:
	//
	//   GempHallTabs.onReturn($element, function () { ...re-fetch and re-render in place... });
	//
	// refresh() runs when the viewer comes back to $element (it becomes visible again because they switch to its tab
	// or sub-tab, or return to the browser tab / window while it is showing) after having been away from it for at
	// least REFRESH_AFTER_MS.  An element that is hidden when registered counts as left at that moment (its data was
	// just fetched), so its first showing refreshes only if that was long enough ago.  refresh() re-fetches DATA and
	// updates the page in place; it must keep what the viewer has open, typed or scrolled to.
	REFRESH_AFTER_MS: 10000,
	returnHandlers: [],
	pageHidden: false,     // the browser tab is hidden
	pageBlurred: false,    // the window lost focus (e.g. to a game window)
	watching: false,

	onReturn: function (element, refresh) {
		var node = element && element.jquery ? element[0] : element;
		if (node == null || typeof refresh != "function")
			return null;
		this.watch();
		var shown = this.isShown(node);
		var handler = {node: node, refresh: refresh, shown: shown, leftAt: shown ? null : this.now()};
		this.returnHandlers.push(handler);
		return handler;
	},

	now: function () {
		return Date.now();
	},

	// Visible as far as the tabs and the page are concerned: attached, no ancestor hidden by a tabs widget (or by
	// anything else with an inline display: none / the hidden attribute), and the page itself in front of the viewer.
	isShown: function (node) {
		if (this.pageHidden || this.pageBlurred)
			return false;
		if (!document.documentElement.contains(node))
			return false;
		for (var n = node; n != null && n.nodeType === 1; n = n.parentElement) {
			if ((n.style && n.style.display === "none") || n.hasAttribute("hidden")
					|| ($(n).hasClass("ui-tabs-panel") && n.getAttribute("aria-hidden") === "true"))
				return false;
		}
		return true;
	},

	// Re-evaluates every registered element; those that have just come back after long enough are refreshed.
	checkReturns: function () {
		var now = this.now();
		for (var i = 0; i < this.returnHandlers.length; i++) {
			var handler = this.returnHandlers[i];
			var shown = this.isShown(handler.node);
			if (handler.shown && !shown) {
				handler.leftAt = now;
			} else if (!handler.shown && shown && handler.leftAt != null && now - handler.leftAt >= this.REFRESH_AFTER_MS) {
				try {
					handler.refresh();
				} catch (e) {
					if (window.console)
						console.error("Refreshing a hall tab failed", e);
				}
			}
			handler.shown = shown;
		}
	},

	// Tab switches of any tabs widget (nested ones too) bubble up as "tabsactivate"; the check runs once the widget
	// has finished updating its panels' state.  Hiding / showing the browser tab and the window losing / regaining
	// focus count as leaving / returning as well.
	watch: function () {
		if (this.watching)
			return;
		this.watching = true;
		var that = this;
		this.pageHidden = document.visibilityState === "hidden";
		var later = function () {
			setTimeout(function () { that.checkReturns(); }, 0);
		};
		$(document).on("tabsactivate.gempReturn", later);
		$(document).on("visibilitychange.gempReturn", function () {
			that.pageHidden = document.visibilityState === "hidden";
			that.checkReturns();
		});
		$(window).on("blur.gempReturn", function () {
			that.pageBlurred = true;
			that.checkReturns();
		});
		$(window).on("focus.gempReturn", function () {
			that.pageBlurred = false;
			that.checkReturns();
		});
	}
};

// Every tabs widget on the page (the hall's and those in the pages loaded into it) loads its remote tabs once, unless
// it passes a beforeLoad of its own.
$.ui.tabs.prototype.options.beforeLoad = GempHallTabs.loadOnce;

// ==== format-definitions: the hall's card dialog ====
// The "Card information" dialog a .cardHint opens (Help › Format Definitions' card chips, event prizes, admin pages...).
// Each card is drawn with CardDisplay, the game's and deckbuilder's card display: a black frame with rounded corners
// over the scan's own edge, so Decipher scans' white corners and edge lines don't show.  The dialog is sized to its
// cards: at the images' own size (497px on the long side) when the window has room, smaller when it hasn't, so it
// never needs scrollbars.  (It used to be a fixed 370x550 (portrait) / 515x430 (landscape) box around the cards'
// 357x497 / 497x357 images: its content padding left less room than the card needed, so the card scrolled.)
var GempCardHintDialog = {
	DIALOG_CLASS: "card-hint-dialog",
	LONG_SIDE: 497,      // the card images' own long side: never drawn larger than the scan
	MIN_LONG_SIDE: 150,
	GAP: 6,              // between the cards of a hint that shows several (hall.css .card-hint-cards)
	OVERHANG: 2,         // CardDisplay's frame reaches 2px past the image on the right and bottom
	MARGIN: 10,          // kept clear around the dialog inside the window

	// The long side (px) to draw every card at so they fit side by side in maxWidth x maxHeight.  `horizontals` holds
	// one boolean per card: true for a landscape card (a site).
	longSide: function (horizontals, maxWidth, maxHeight) {
		var ratio = CardDisplay.TargetVertRatio;    // short side / long side
		var widthPerLong = 0;
		var heightPerLong = 0;
		for (var i = 0; i < horizontals.length; i++) {
			widthPerLong += horizontals[i] ? 1 : ratio;
			heightPerLong = Math.max(heightPerLong, horizontals[i] ? ratio : 1);
		}
		if (widthPerLong === 0)
			return this.LONG_SIDE;
		var gaps = this.GAP * (horizontals.length - 1);
		var fit = Math.min(this.LONG_SIDE, (maxWidth - gaps) / widthPerLong, maxHeight / heightPerLong);
		return Math.max(this.MIN_LONG_SIDE, Math.floor(fit));
	},

	// Shows the cards (blueprint ids) in `dialog` (a jQuery UI dialog) and sizes it to them.
	show: function (dialog, ids) {
		dialog.dialog("widget").addClass(this.DIALOG_CLASS);
		dialog.empty();
		var row = $("<div class='card-hint-cards'></div>").appendTo(dialog);
		var shown = [];
		for (var i = 0; i < ids.length; i++) {
			var id = $.trim(ids[i]);
			if (id === "")
				continue;
			var card = new Card(id, null, null, "SPECIAL", null, "");
			var display = new CardDisplay();
			display.appendTo(row);
			display.reloadFromCard(card, this.LONG_SIDE, this.LONG_SIDE);
			var pack = typeof card.isPack == "function" && card.isPack();
			display.baseDiv.addClass("card-hint-card").toggleClass("card-hint-pack", pack);
			shown.push({display: display, horizontal: !!card.horizontal, noBorder: pack});
		}
		dialog.dialog("option", {title: "Card information", height: "auto", width: 300});
		if (!dialog.dialog("isOpen"))
			dialog.dialog("open");
		this.fit(dialog, shown);
		return shown;
	},

	// Draws the cards as large as the window allows (up to their own size), then sizes and centres the dialog on them.
	fit: function (dialog, shown) {
		if (shown.length === 0)
			return;
		var widget = dialog.dialog("widget");
		var row = dialog.children(".card-hint-cards");
		var style = window.getComputedStyle(dialog[0]);
		var padX = (parseFloat(style.paddingLeft) || 0) + (parseFloat(style.paddingRight) || 0);
		var padY = (parseFloat(style.paddingTop) || 0) + (parseFloat(style.paddingBottom) || 0);
		// the dialog around its content: the title bar, the dialog's padding and border
		var frameX = Math.max(0, widget.outerWidth() - dialog.outerWidth());
		var frameY = Math.max(0, widget.outerHeight() - dialog.outerHeight());
		var maxWidth = $(window).width() - 2 * this.MARGIN - frameX - padX - this.OVERHANG;
		var maxHeight = $(window).height() - 2 * this.MARGIN - frameY - padY - this.OVERHANG;

		var horizontals = $.map(shown, function (s) { return s.horizontal; });
		var longSide = this.longSide(horizontals, maxWidth, maxHeight);
		var width = 0;
		for (var i = 0; i < shown.length; i++) {
			shown[i].display.resize(shown[i].horizontal, longSide, longSide, shown[i].noBorder);
			width += shown[i].display.width();
		}
		width += this.GAP * (shown.length - 1);
		row.css({width: width + "px"});      // plus its OVERHANG padding (hall.css)
		dialog.dialog("option", {width: Math.ceil(width + this.OVERHANG + padX), height: "auto"});
		dialog.dialog("option", "position", {my: "center", at: "center", of: window, collision: "fit"});
	},

	// Closes `dialog` on a click anywhere outside it.
	closeOnClickOutside: function (dialog) {
		$(document).off("mouseup.cardHintDialog").on("mouseup.cardHintDialog", function (event) {
			if (!dialog.dialog("instance") || !dialog.dialog("isOpen"))
				return;
			if ($(event.target).closest(dialog.dialog("widget")).length > 0)
				return;
			dialog.dialog("close");
		});
	}
};
// ==== end format-definitions ====

$(document).ready(function () {

	//Hiding the Users tab until that feature is ready
	$("#tabs > ul :nth-child(5)").hide();
	// The Admin tab stays hidden until the hall's player info shows an admin (hallUi.js); a link to it, or a remembered
	// Admin tab, is opened by GempLinks once it appears.
	$("#tabs > ul :nth-child(7)").hide();

	$("#main").tabs({
		active: GempLinks.initialTabIndex(),
		activate: function (event, ui) {
			GempHallTabs.save(ui.newTab.children("a").attr("href"));
		}
	});

	// The chat and the hall are built now but go live through GempHallSession: at once on a tab that needs a
	// logged-in player, else once the player info shows one (a visitor reading Help / Server Info is left alone).
	chat = new ChatBoxUI("Game Hall", $("#chat"), "/gemp-lotr-server", true, null, false, null, true, true);
	chat.showTimestamps = true;

	hall = new GempLotrHallUI("/gemp-lotr-server", chat, true);
	GempHallSession.begin(hall, chat);
	GempLinks.start();


	var infoDialog = $("<div></div>")
			.dialog({
				autoOpen:false,
				closeOnEscape:true,
				resizable:false,
				title:"Card information",
				closeText: ""
			});

	// Hints: .cardHint (card images), .prizeHint (a breakdown in its value attribute) and .draftFormatInfo (a draft
	// format page).  The hall draws them as <button>s; hints drawn elsewhere as <div>/<span> are made focusable below,
	// and Enter / Space on them works like a click.
	var HINTS = ".cardHint, .prizeHint, .draftFormatInfo";

	var openHint = function (hint) {
		if (hint.hasClass("cardHint")) {
			GempCardHintDialog.show(infoDialog, String(hint.attr("value") || "").split(","));
		} else if (hint.hasClass("prizeHint")) {
			var prizeDescription = hint.attr("value");
			var title = hint.attr("title");
			if(!title) {
				title = "Prizes details";
			}

			infoDialog.dialog("widget").removeClass(GempCardHintDialog.DIALOG_CLASS);
			infoDialog.html(prizeDescription);

			infoDialog.dialog({title: title, width: 300, height: 150});
			infoDialog.dialog("open");
		} else if (hint.hasClass("draftFormatInfo")) {
			var draftCode = hint.attr("draftCode");
			window.open('/gemp-lotr-server/deck/draftHtml?draftCode=' + encodeURIComponent(draftCode), "_blank");
		}
	};

	// A click anywhere outside the dialog closes it (as the Events tab's race path cards do, leagueResultsUi.js).  A
	// click on another hint closes it here and opens that hint's in the click below.
	GempCardHintDialog.closeOnClickOutside(infoDialog);

	$("body").click(
		function (event) {
			var hint = $(event.target).closest(HINTS);
			if (hint.length === 0)
				return true;

			openHint(hint);
			event.stopPropagation();
			return false;
		});

	$("body").on("keydown", HINTS, function (event) {
		// a real <button> already turns Enter / Space into a click
		if (this.tagName === "BUTTON" || (event.key !== "Enter" && event.key !== " "))
			return;
		event.preventDefault();
		event.stopPropagation();
		openHint($(this));
	});

	var makeHintsFocusable = function (root) {
		$(root).find(HINTS).addBack(HINTS).each(function () {
			if (this.tagName !== "BUTTON" && this.tagName !== "A" && !this.hasAttribute("tabindex")) {
				this.setAttribute("tabindex", "0");
				this.setAttribute("role", "button");
			}
		});
	};
	makeHintsFocusable(document.body);
	if (typeof MutationObserver !== "undefined") {
		new MutationObserver(function (mutations) {
			for (var i = 0; i < mutations.length; i++) {
				for (var j = 0; j < mutations[i].addedNodes.length; j++) {
					var node = mutations[i].addedNodes[j];
					if (node.nodeType === 1)
						makeHintsFocusable(node);
				}
			}
		}).observe(document.body, {childList: true, subtree: true});
	}
});
