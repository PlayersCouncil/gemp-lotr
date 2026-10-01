// ==== share-links: links that preview nicely where they are pasted (Discord, forums...) ====
//
// The server answers /gemp-lotr/share/<kind>/<id> (ShareRequestHandler) with a page of Open Graph / Twitter tags that
// sends a person straight on to the hall link:
//   patch-notes/<slug> -> hall.html#patch-notes/<slug>     errata/<card id> -> hall.html#errata-<card id>
//   format/<code>      -> hall.html#format-<code>          card/<blueprint> -> hall.html#card-<blueprint>
//
//   GempShareLinks.url("patch-notes", "2026-09-27-hall-overhaul")   the absolute share link
//   GempShareLinks.button("errata", "1_5")                          a small "Copy share link" button (jQuery)
//   GempShareLinks.setId(button, "1_8")                             the same button, now for another item
//   GempShareLinks.copy(url, element)                                copies it, and says so beside `element`
//
// #card-<blueprint id> (where a card share link lands) opens that card in the card display over Help.

var GempShareLinks = {
	KINDS: ["patch-notes", "errata", "format", "card"],
	CARD_HASH: /^#card-(\d{1,3}_\d{1,4})$/,

	url: function (kind, id) {
		var origin = window.location.origin || (window.location.protocol + "//" + window.location.host);
		return origin + "/gemp-lotr/share/" + encodeURIComponent(kind) + (id ? "/" + encodeURIComponent(id) : "");
	},

	// A button that copies the share link of kind/id.  label: its text (default "Copy share link").
	button: function (kind, id, label) {
		var that = this;
		return $("<button type='button' class='share-link-button'></button>")
			.attr("title", "Copy a link to share (it shows a preview where you paste it)")
			.attr("data-share-kind", kind)
			.attr("data-share-id", id)
			.append($("<img alt='' aria-hidden='true' src='images/icons/share-from-square-solid.svg'>"))
			.append($("<span class='share-link-label'></span>").text(label || "Copy share link"))
			.on("click", function (event) {
				event.preventDefault();
				event.stopPropagation();
				// the id as it is now: a page that shows one item at a time moves it along (setId)
				that.copy(that.url(kind, $(this).attr("data-share-id")), this);
			});
	},

	// Points a button made by button() at another item (the page moved on to it); a falsy id disables it.
	setId: function (button, id) {
		$(button).attr("data-share-id", id || "").prop("disabled", !id);
	},

	// Copies text to the clipboard; the note beside `element` says whether it worked (or shows the link to copy).
	copy: function (text, element) {
		var that = this;
		var done = function (ok) {
			that.say(element, ok ? "Link copied" : null, text);
		};
		try {
			if (navigator.clipboard && typeof navigator.clipboard.writeText == "function" && window.isSecureContext !== false) {
				navigator.clipboard.writeText(text).then(function () {
					done(true);
				}, function () {
					done(that.copyByCommand(text));
				});
				return;
			}
		} catch (ignored) {
			// fall through to the old way
		}
		done(this.copyByCommand(text));
	},

	copyByCommand: function (text) {
		var area = $("<textarea readonly class='share-link-copy-area'></textarea>").val(text).appendTo("body");
		var ok = false;
		try {
			area[0].select();
			ok = document.execCommand && document.execCommand("copy");
		} catch (ignored) {
			ok = false;
		}
		area.remove();
		return !!ok;
	},

	// "Link copied" beside the button for a moment; when copying failed, the link itself, selected, to copy by hand.
	say: function (element, message, text) {
		var anchor = $(element);
		anchor.nextAll(".share-link-note").remove();
		var note = $("<span class='share-link-note' role='status'></span>");
		if (message) {
			note.text(message);
		} else {
			note.addClass("share-link-note-manual")
				.append($("<input type='text' readonly>").val(text).attr("aria-label", "Share link"));
		}
		anchor.after(note);
		if (!message) {
			var input = note.find("input")[0];
			input.focus();
			input.select();
		}
		clearTimeout(anchor.data("shareNoteTimer"));
		anchor.data("shareNoteTimer", setTimeout(function () {
			note.remove();
		}, message ? 2000 : 15000));
	},

	// Shows a card in the card display: the shared zoomable preview when it is there, else the hall's card dialog.
	showCard: function (blueprintId, title, event) {
		if (window.GempCardPreview && typeof GempCardPreview.open == "function") {
			GempCardPreview.open({blueprintId: blueprintId, title: title || undefined}, event);
			return;
		}
		// the hall's .cardHint handler (src/Hall/GameHall.js) opens its dialog on a click that bubbles to the body
		var hint = $("<span class='cardHint' hidden></span>").attr("value", blueprintId).appendTo("body");
		hint.trigger("click");
		hint.remove();
	}
};

// #card-<blueprint id>: the card display over Help (public, like the other share targets).
$(function () {
	if (!window.GempLinks || !$.isArray(GempLinks.ROUTES))
		return;
	GempLinks.ROUTES.push({
		pattern: GempShareLinks.CARD_HASH, tab: "help", sub: null, run: function (m) {
			GempShareLinks.showCard(m[1]);
		}
	});
});
// ==== end share-links ====
