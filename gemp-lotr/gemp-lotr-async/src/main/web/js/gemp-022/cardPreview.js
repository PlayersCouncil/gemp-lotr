/**
 * GempCardPreview: the hall's zoomable card window (one shared instance).
 *
 * Extracted from the Events tab's Race to Mount Doom meta-site display (leagueResultsUi.js), which opened the game's
 * CardInfoDialog: a "Card Information" dialog with a Card Size slider whose value is kept in the same cookie
 * (CardInfoDialog.SliderCookieName), so the zoom level persists across opens, reloads, and the game / deckbuilder.
 * Unlike CardInfoDialog it is sized to the card itself (portrait or landscape) so it never scrolls, it shrinks the
 * card to fit a small window, and it closes on a click outside it or on Esc.  The card is drawn with CardDisplay (the
 * game's and deckbuilder's card display) with the rounded black frame (css/gemp-001/cardPreview.css).  A click on the
 * card flips it (its back side, or upside down) as in the game's dialog.
 *
 *   GempCardPreview.open(args, event?)
 *     args: {blueprintId: "1_5"}                    a card by blueprint (image, orientation, wiki link from Card.js)
 *           {imageUrl: "...", title: "..."}         any image; orientation taken from the image once it loads
 *           {blueprintId, imageUrl, title}          a card's details, with this image instead of its own
 *           + overlayBlueprintId: "94_9"            (optional) a meta-site modifier drawn over the bottom of the card
 *           title defaults to "Card Information".
 *     event: (optional) the triggering event; its default action and propagation are stopped.
 *     Opening while the window is open swaps the card and keeps the window where it is.  While a new card's image
 *     loads, the card is blank (never the card shown before).
 *   GempCardPreview.bind(container, selector, {click, contextmenu, swipeUp, resolve})
 *     Delegated handlers on `container` for elements matching `selector`: click (default true), right-click
 *     (contextmenu, default true) and a swipe up on a touch screen (swipeUp, default false) open the preview with
 *     resolve(element) -> args (default: {blueprintId: the element's data-blueprint-id, or its value attribute}).
 *     A resolve returning null opens nothing and leaves the event alone.  Clicking another bound element while the window is open swaps the
 *     card instead of closing the window.  Binding the same container and selector again replaces the earlier
 *     binding.  Returns {unbind: function}.
 *   GempCardPreview.close(), GempCardPreview.isOpen()
 */
var GempCardPreview = {
	DIALOG_CLASS: "card-preview-dialog",
	DEFAULT_TITLE: "Card Information",
	MARGIN: 10,          // kept clear around the window inside the browser window
	OVERHANG: 2,         // CardDisplay's frame reaches 2px past the image on the right and bottom
	MIN_LONG_SIDE: 150,
	MIN_ZOOM: 0.4,       // the Card Size slider: a fraction of CardDisplay.MaxTarget, as in CardInfoDialog
	MAX_ZOOM: 1,
	DEFAULT_ZOOM: 0.5,
	SWIPE_MIN: 40,       // px upwards for a swipe up
	SWIPE_MAX_MS: 800,

	dialog: null,
	slider: null,
	cardBox: null,
	footer: null,
	display: null,
	current: null,       // {card, horizontal, noBorder, args}
	sliding: false,
	DATA_KEY: "gempCardPreviewBindings",   // on a bound container: {selector: event namespace}
	PENDING_CLASS: "card-preview-pending",  // on the card's image (and overlay) while a new one loads
	bindCount: 0,

	// ---- public ----

	open: function (args, event) {
		if (!args || (!args.blueprintId && !args.imageUrl))
			return false;
		if (event) {
			if (event.preventDefault)
				event.preventDefault();
			if (event.stopPropagation)
				event.stopPropagation();
		}
		this.ensure();
		var wasOpen = this.isOpen();
		this.syncZoom();
		this.showCard(args);
		this.dialog.dialog("option", "title", args.title ? String(args.title) : this.DEFAULT_TITLE);
		if (!wasOpen)
			this.dialog.dialog("open");
		this.fit(!wasOpen);
		return true;
	},

	close: function () {
		if (this.isOpen())
			this.dialog.dialog("close");
	},

	isOpen: function () {
		return this.dialog != null && !!this.dialog.dialog("instance") && this.dialog.dialog("isOpen");
	},

	bind: function (container, selector, options) {
		var that = this;
		container = $(container);
		options = $.extend({click: true, contextmenu: true, swipeUp: false, resolve: null}, options || {});
		var resolve = typeof options.resolve == "function" ? options.resolve : this.defaultResolve;

		// binding the same container and selector again replaces the earlier binding.  The bindings are kept on the
		// container (jQuery data), so they go when it does.
		var bindings = container.data(this.DATA_KEY);
		if (!bindings) {
			bindings = {};
			container.data(this.DATA_KEY, bindings);
		}
		if (bindings[selector])
			container.off(bindings[selector]);
		var namespace = ".gempCardPreview" + (++this.bindCount);
		bindings[selector] = namespace;

		// true when it opened (the event is then stopped); an element resolve gives nothing for is left alone
		var openFor = function (element, event) {
			var args = resolve.call(element, element);
			return args ? that.open(args, event) : false;
		};
		if (options.click)
			container.on("click" + namespace, selector, function (event) {
				if (openFor(this, event))
					return false;
			});
		if (options.contextmenu)
			container.on("contextmenu" + namespace, selector, function (event) {
				if (openFor(this, event))
					return false;
			});
		if (options.swipeUp) {
			container.on("touchstart" + namespace, selector, function (event) {
				var touches = event.originalEvent && event.originalEvent.touches;
				if (!touches || touches.length !== 1) {
					$(this).removeData("gempCardPreviewTouch");
					return;
				}
				$(this).data("gempCardPreviewTouch", {x: touches[0].clientX, y: touches[0].clientY, t: Date.now()});
			});
			container.on("touchend" + namespace, selector, function (event) {
				var start = $(this).data("gempCardPreviewTouch");
				$(this).removeData("gempCardPreviewTouch");
				var touches = event.originalEvent && event.originalEvent.changedTouches;
				if (!start || !touches || touches.length === 0)
					return;
				if (that.isSwipeUp(start, {x: touches[0].clientX, y: touches[0].clientY, t: Date.now()}))
					openFor(this, event);
			});
		}
		return {
			unbind: function () {
				container.off(namespace);
				var current = container.data(that.DATA_KEY);
				if (current && current[selector] === namespace)
					delete current[selector];
			}
		};
	},

	// ---- helpers ----

	defaultResolve: function (element) {
		var id = $(element).attr("data-blueprint-id") || $(element).attr("value");
		return id ? {blueprintId: String(id)} : null;
	},

	isSwipeUp: function (start, end) {
		var up = start.y - end.y;
		return up >= this.SWIPE_MIN && Math.abs(end.x - start.x) < up && (end.t - start.t) <= this.SWIPE_MAX_MS;
	},

	// The slider's value from the shared cookie (another page may have changed it).
	storedZoom: function () {
		var value = NaN;
		try {
			value = parseFloat(typeof loadFromCookie == "function"
				? loadFromCookie(this.cookieName(), String(this.DEFAULT_ZOOM)) : this.DEFAULT_ZOOM);
		} catch (ignored) {
			// no cookies: the default
		}
		if (isNaN(value))
			value = this.DEFAULT_ZOOM;
		return Math.min(this.MAX_ZOOM, Math.max(this.MIN_ZOOM, value));
	},

	saveZoom: function (value) {
		try {
			if (typeof saveToCookie == "function")
				saveToCookie(this.cookieName(), "" + value);
		} catch (ignored) {
			// no cookies: the zoom lasts until the page reloads
		}
	},

	cookieName: function () {
		return (typeof CardInfoDialog != "undefined" && CardInfoDialog.SliderCookieName)
			? CardInfoDialog.SliderCookieName : "card-info-dialog-slider-last-value";
	},

	zoom: function () {
		return this.slider ? this.slider.slider("value") : this.storedZoom();
	},

	syncZoom: function () {
		if (this.slider && !this.sliding)
			this.slider.slider("value", this.storedZoom());
	},

	maxTarget: function () {
		return (typeof CardDisplay != "undefined" && CardDisplay.MaxTarget) ? CardDisplay.MaxTarget : 1039;
	},

	ratio: function () {
		return (typeof CardDisplay != "undefined" && CardDisplay.TargetVertRatio) ? CardDisplay.TargetVertRatio : 745 / 1039;
	},

	// The long side (px) to draw a card at: the zoom's size, or less when the window has no room for it.
	longSide: function (zoomLong, horizontal, availWidth, availHeight) {
		var ratio = this.ratio();
		var fit = horizontal ? Math.min(availWidth, availHeight / ratio) : Math.min(availHeight, availWidth / ratio);
		return Math.max(this.MIN_LONG_SIDE, Math.floor(Math.min(zoomLong, fit)));
	},

	// ---- the window ----

	ensure: function () {
		if (this.dialog != null)
			return;
		var that = this;
		this.dialog = $("<div class='card-preview'></div>").dialog({
			autoOpen: false,
			closeOnEscape: true,
			closeText: "",
			resizable: false,
			title: this.DEFAULT_TITLE,
			width: 300,
			height: "auto",
			close: function () {
				if (that.display)
					that.display.clear();
				that.current = null;
			}
		});
		this.dialog.dialog("widget").addClass(this.DIALOG_CLASS);

		this.slider = $("<div class='card-preview-zoom' title='Card Size'></div>").slider({
			value: this.storedZoom(),
			min: this.MIN_ZOOM,
			max: this.MAX_ZOOM,
			step: 0.1,
			range: "min",
			animate: true,
			slide: function (event, ui) {
				that.saveZoom(ui.value);
				that.fit(false, ui.value);
			},
			change: function (event, ui) {
				if (event.originalEvent) {      // the keyboard, or a click on the track; not syncZoom
					that.saveZoom(ui.value);
					that.fit(false, ui.value);
				}
			},
			start: function () {
				that.sliding = true;
			},
			stop: function () {
				setTimeout(function () {
					that.sliding = false;
				}, 1);
			}
		}).appendTo(this.dialog);
		if (typeof onTouchDevice == "function" && onTouchDevice())
			this.slider.addClass("card-preview-zoom-touch");      // hidden: jQuery UI's slider does not take touches

		this.cardBox = $("<div class='card-preview-card'></div>").appendTo(this.dialog);
		this.display = new CardDisplay();
		this.display.appendTo(this.cardBox);
		this.display.cardImage.addEventListener("load", function () {
			that.imageSettled(this);
			that.imageLoaded(this);
		});
		this.display.cardImage.addEventListener("error", function () {
			that.imageSettled(this);
		});
		if (this.display.overlayImage) {
			$(this.display.overlayImage).on("load error", function () {
				that.imageSettled(this);
			});
		}
		this.footer = $("<div class='card-preview-footer'></div>").appendTo(this.dialog);

		// a click (or tap) outside the window closes it; one on an element bound to open it swaps the card instead
		$(document).off("mouseup.gempCardPreview").on("mouseup.gempCardPreview", function (event) {
			if (!that.isOpen() || that.sliding)
				return;
			var target = $(event.target);
			if (target.closest(that.dialog.dialog("widget")).length > 0)
				return;
			if (that.isBoundTarget(event.target))
				return;
			that.close();
		});
		// Esc closes it wherever the focus is (jQuery UI's closeOnEscape only sees keys inside the window)
		$(document).off("keydown.gempCardPreview").on("keydown.gempCardPreview", function (event) {
			if ((event.key === "Escape" || event.keyCode === 27) && that.isOpen()) {
				that.close();
				event.preventDefault();
			}
		});
		$(window).off("resize.gempCardPreview").on("resize.gempCardPreview", function () {
			if (that.isOpen())
				that.fit(false);
		});
	},

	// Whether `element` is (in) an element some bind() opens the preview for.
	isBoundTarget: function (element) {
		var key = this.DATA_KEY;
		var found = false;
		$(element).parents().each(function () {
			var bindings = $.data(this, key);
			if (!bindings)
				return true;
			var container = this;
			$.each(bindings, function (selector) {
				var match = $(element).closest(selector, container);
				if (match.length > 0 && match[0] !== container) {
					found = true;
					return false;
				}
			});
			return !found;
		});
		return found;
	},

	showCard: function (args) {
		var display = this.display;
		var before = this.imageSources();
		var card = null;
		if (args.blueprintId) {
			try {
				card = new Card(String(args.blueprintId), null, null, "SPECIAL", null, "");
			} catch (ignored) {
				card = null;
			}
		}
		var noBorder = false;
		var horizontal = false;
		if (card != null) {
			if (args.imageUrl)
				card.imageUrl = String(args.imageUrl);
			if (args.overlayBlueprintId) {
				// the Race to Mount Doom meta-site: the position's visual card with the modifier's text over its bottom
				Card.metaSiteOverlays[String(args.overlayBlueprintId)] = card.imageUrl;
				card.overlayImageUrl = Card.getImageUrl(String(args.overlayBlueprintId));
			}
			noBorder = typeof card.isPack == "function" && card.isPack();
			horizontal = !!(card.horizontal || (typeof card.effectivelyHorizontal == "function" && card.effectivelyHorizontal()));
			display.reloadFromCard(card, 300, 300);
			display.setInvert(false);
			display.baseDiv.off("click");
			if (!args.overlayBlueprintId && !Card.isMetaSiteModifier(card.bareBlueprint))
				display.addInvertClick();
		} else {
			var url = args.imageUrl || Card.getImageUrl(String(args.blueprintId));
			display.reversible = false;
			display.backside = null;
			display.baseDiv.off("click");
			display.reload(300, 300, url, false, false, false, null, null);
		}
		display.baseDiv.addClass("card-preview-display").toggleClass("card-preview-pack", noBorder);
		this.current = {card: card, horizontal: horizontal, noBorder: noBorder, args: args};

		this.footer.empty();
		if (card != null && typeof card.hasWikiInfo == "function" && card.hasWikiInfo()) {
			this.footer.append($("<a target='_blank' rel='noopener'></a>").attr("href", card.getWikiLink()).text("Go to Wiki Page"));
			this.footer.prop("hidden", false);
		} else {
			this.footer.prop("hidden", true);
		}

		// a browser keeps drawing an <img>'s old picture until its new one has loaded: hide the card (and the meta-site
		// overlay) until then, so a swap never shows the previous card for a moment
		this.hideUntilLoaded(display.cardImage, before.card);
		if (display.overlayImage)
			this.hideUntilLoaded(display.overlayImage, before.overlay);

		var image = display.cardImage;
		if (image && image.complete && image.naturalWidth > 0)
			this.imageLoaded(image, true);
	},

	imageSources: function () {
		var display = this.display;
		return {
			card: display.cardImage ? display.cardImage.getAttribute("src") : null,
			overlay: display.overlayImage ? display.overlayImage.getAttribute("src") : null
		};
	},

	// Hidden (CSS: .card-preview-pending) while `image` loads a picture other than `before`; shown as soon as it has
	// one (at once when the browser already has it).
	hideUntilLoaded: function (image, before) {
		if (!image)
			return;
		var src = image.getAttribute("src");
		if (!src) {
			$(image).removeClass(this.PENDING_CLASS);
			return;
		}
		var ready = image.complete && image.naturalWidth > 0;
		$(image).toggleClass(this.PENDING_CLASS, !ready && (src !== before || !image.complete));
	},

	// The image has loaded, or failed to (a failure shows as it always did): show it.
	imageSettled: function (image) {
		$(image).removeClass(this.PENDING_CLASS);
	},

	// The image's own orientation wins over the guess from its blueprint (or the portrait default for a bare image).
	imageLoaded: function (image, noFit) {
		if (!this.current || image !== this.display.cardImage || !image.naturalWidth || !image.naturalHeight)
			return;
		if (this.current.noBorder)
			return;
		var horizontal = image.naturalWidth > image.naturalHeight;
		if (horizontal !== this.current.horizontal) {
			this.current.horizontal = horizontal;
			if (!noFit && this.isOpen())
				this.fit(false);
		}
	},

	// Draws the card at the zoom's size (smaller if the browser window has no room), sizes the window to it and keeps
	// the window inside the browser window: centred when `center`, else where it is.
	fit: function (center, zoomValue) {
		if (!this.current || !this.dialog)
			return;
		var dialog = this.dialog;
		var widget = dialog.dialog("widget");
		var style = window.getComputedStyle(dialog[0]);
		var padX = (parseFloat(style.paddingLeft) || 0) + (parseFloat(style.paddingRight) || 0);
		var padY = (parseFloat(style.paddingTop) || 0) + (parseFloat(style.paddingBottom) || 0);
		var frameX = Math.max(0, widget.outerWidth() - dialog.outerWidth());
		var frameY = Math.max(0, widget.outerHeight() - dialog.outerHeight());
		var extraY = (this.slider.is(":visible") ? this.slider.outerHeight(true) : 0)
			+ (this.footer.is(":visible") ? this.footer.outerHeight(true) : 0);
		var viewWidth = $(window).width();
		var viewHeight = $(window).height();
		var availWidth = viewWidth - 2 * this.MARGIN - frameX - padX - this.OVERHANG;
		var availHeight = viewHeight - 2 * this.MARGIN - frameY - padY - extraY - this.OVERHANG;

		// where the window is now (the reader may have dragged it): a width change makes jQuery UI re-centre it
		var offset = center ? null : widget.offset();

		var zoomLong = (zoomValue != null ? zoomValue : this.zoom()) * this.maxTarget();
		var horizontal = this.current.horizontal;
		var long = this.longSide(zoomLong, horizontal, availWidth, availHeight);
		this.display.resize(horizontal, long, long, this.current.noBorder);
		var width = Math.ceil(this.display.width() + this.OVERHANG + padX);
		var minWidth = 175;       // CardInfoDialog.MinWidth: any narrower and the title bar crowds
		dialog.dialog("option", {width: Math.max(width, minWidth), height: "auto"});

		// a title bar that wrapped, a font that differs...: shrink the card by what still overflows
		var overflow = widget.outerHeight() - (viewHeight - 2 * this.MARGIN);
		if (overflow > 0) {
			long = Math.max(this.MIN_LONG_SIDE, long - Math.ceil(horizontal ? overflow / this.ratio() : overflow));
			this.display.resize(horizontal, long, long, this.current.noBorder);
			width = Math.ceil(this.display.width() + this.OVERHANG + padX);
			dialog.dialog("option", {width: Math.max(width, minWidth), height: "auto"});
		}
		this.current.longSide = long;

		if (center) {
			dialog.dialog("option", "position", {my: "center", at: "center", of: window, collision: "fit"});
		} else {
			var scrollLeft = $(window).scrollLeft();
			var scrollTop = $(window).scrollTop();
			var left = Math.min(offset.left - scrollLeft, viewWidth - widget.outerWidth() - this.MARGIN);
			var top = Math.min(offset.top - scrollTop, viewHeight - widget.outerHeight() - this.MARGIN);
			widget.css({left: Math.max(this.MARGIN, left) + scrollLeft, top: Math.max(this.MARGIN, top) + scrollTop});
		}
	}
};
