/**
 * The "Add items to collections" form on the Prizes admin tab: hands cards, packs, selections and awards straight to
 * players' collections through POST /admin/addItems (detailed=true).
 *
 * The markup lives in includes/admin/prizeAdmin.html; this class binds to it by id inside the given root:
 *   #additems-player-box / #additems-player-input   player chips + fuzzy name search (paste a list to add many)
 *   #additems-event / #additems-event-load-button    load every player with a match in a recent league/tournament
 *   #additems-item-input / #additems-item-list       item search (cards, packs, selections, awards) + one row per item
 *   #additems-collection                             My cards, Trophy, or an active sealed/draft league's collection
 *   #additems-reason                                 optional note recorded on the transfer
 *   #additems-review-button / #additems-summary      confirmation step before anything is sent
 *   #additems-result / #additems-player-results      outcome, one line per player
 *
 * Usage:
 *   var form = new AddItemsForm($("#additems-section"), {comm: hall.comm});
 *
 * Every string that came from the server or the admin is put into the DOM with .text()/.attr().
 */
var AddItemsForm = Class.extend({
    root: null,
    comm: null,
    searchDelay: 250,
    players: null,        // [{input, name, status: "checking" | "ok" | "unknown", suggestions:[...], chip}]
    collections: null,    // value -> collection description from /admin/addItemsCollections
    events: null,         // "kind:id" -> event description from /admin/addItemsEvents
    resolveSerial: 0,

    init: function (root, options) {
        var that = this;
        options = options || {};
        this.root = root;
        this.comm = options.comm;
        if (options.searchDelay != null)
            this.searchDelay = options.searchDelay;
        this.players = [];
        this.collections = {};
        this.events = {};

        this.playerBox = root.find("#additems-player-box");
        this.playerInput = root.find("#additems-player-input");
        this.eventSelect = root.find("#additems-event");
        this.eventLoadButton = root.find("#additems-event-load-button");
        this.itemInput = root.find("#additems-item-input");
        this.itemList = root.find("#additems-item-list");
        this.collectionSelect = root.find("#additems-collection");
        this.reasonInput = root.find("#additems-reason");
        this.summary = root.find("#additems-summary");
        this.result = root.find("#additems-result");
        this.playerResults = root.find("#additems-player-results");

        this._bindPlayers();
        this._bindItems();

        this.eventLoadButton.button().click(function () { that.loadFromEvent(); });
        // the select is width-clamped (long names ellipsize): the full name of the chosen event shows on hover
        this.eventSelect.on("change", function () { that._updateEventTitle(); });

        root.find("#additems-review-button").button().click(function () { that.review(); });
        root.find("#additems-clear-button").button().click(function () { that.clear(); });
        root.find("#additems-send-button").button().click(function () { that.send(); });
        root.find("#additems-back-button").button().click(function () { that._hideSummary(); });

        // any edit after the summary was shown makes the summary stale
        root.on("input change", "#additems-form input, #additems-form select", function () { that._hideSummary(); });

        this._refreshItemsEmpty();
        this.loadCollections();
        this.loadEvents();
    },

    // ---- players ----

    _bindPlayers: function () {
        var that = this;
        var input = this.playerInput;

        input.autocomplete({
            minLength: 2,
            delay: this.searchDelay,
            autoFocus: true,
            source: function (request, response) {
                that.comm.searchPlayers(request.term, 10, function (json) {
                    var names = (json && json.players) || [];
                    response($.map(names, function (name) { return {label: name, value: name}; }));
                }, that._silentErrors(function () { response([]); }));
            },
            focus: function () { return false; },
            select: function (event, ui) {
                that._addResolvedPlayer(ui.item.value);
                input.val("");
                return false;
            }
        });
        input.autocomplete("instance")._renderItem = function (ul, item) {
            return $("<li></li>").append($("<div></div>").text(item.label)).appendTo(ul);
        };

        input.on("keydown", function (event) {
            if (event.which == 13) {
                var ac = input.autocomplete("instance");
                if (ac && ac.menu && ac.menu.active)
                    return; // the autocomplete picks the highlighted name
                event.preventDefault();
                var text = $.trim(input.val());
                if (text) {
                    input.autocomplete("close");
                    input.val("");
                    that.addPlayerList(text);
                }
            } else if (event.which == 8 && input.val() === "" && that.players.length > 0) {
                that._removePlayer(that.players[that.players.length - 1]);
            }
        });

        // a pasted list keeps its line breaks only if we read it before the text input flattens it
        input.on("paste", function (event) {
            var clipboard = event.originalEvent && event.originalEvent.clipboardData;
            var text = clipboard ? clipboard.getData("text") : null;
            if (text != null && /[\s,;]/.test($.trim(text))) {
                event.preventDefault();
                input.autocomplete("close");
                input.val("");
                that.addPlayerList(text);
            }
        });

        // typing a separator finishes the name(s) before it
        input.on("input", function () {
            var value = input.val();
            if (/[,;\n]/.test(value) || /\S\s+\S/.test(value) || /\S\s$/.test(value)) {
                var parts = value.split(/[\s,;]+/);
                var rest = /[\s,;]$/.test(value) ? "" : parts.pop();
                var done = parts.join("\n");
                input.val(rest);
                if ($.trim(done))
                    that.addPlayerList(done);
            }
        });

        this.playerBox.on("click", function (event) {
            if (event.target === that.playerBox[0])
                input.focus();
        });
    },

    /**
     * Adds every name in {@code text} (newline / comma / semicolon / space separated) as a chip and asks the server
     * which of them are real players; unknown ones are flagged with suggestions.
     */
    addPlayerList: function (text) {
        var that = this;
        var names = $.grep(String(text || "").split(/[\s,;]+/), function (n) { return n !== ""; });
        var fresh = [];
        var seen = {};
        for (var i = 0; i < names.length; i++) {
            var key = names[i].toLowerCase();
            if (seen[key] || this._findPlayer(names[i]) != null)
                continue;
            seen[key] = true;
            var entry = {input: names[i], name: null, status: "checking", suggestions: []};
            this.players.push(entry);
            this._renderChip(entry);
            fresh.push(entry);
        }
        if (fresh.length == 0)
            return;
        this._hideSummary();
        var serial = ++this.resolveSerial;
        this.comm.resolvePlayers($.map(fresh, function (e) { return e.input; }).join("\n"), function (json) {
            var byInput = {};
            var list = (json && json.players) || [];
            for (var j = 0; j < list.length; j++)
                byInput[String(list[j].input).toLowerCase()] = list[j];
            for (var k = 0; k < fresh.length; k++) {
                var entry = fresh[k];
                if (entry.removed)
                    continue;
                var answer = byInput[entry.input.toLowerCase()];
                if (answer && answer.name) {
                    var duplicate = that._findPlayer(answer.name, entry);
                    if (duplicate != null) {
                        that._removePlayer(entry);
                        continue;
                    }
                    entry.name = answer.name;
                    entry.status = "ok";
                    entry.suggestions = [];
                } else {
                    entry.status = "unknown";
                    entry.suggestions = (answer && answer.suggestions) || [];
                }
                that._renderChip(entry);
            }
        }, this._silentErrors(function () {
            for (var k = 0; k < fresh.length; k++) {
                if (fresh[k].status == "checking") {
                    fresh[k].status = "unknown";
                    that._renderChip(fresh[k]);
                }
            }
            that.result.text("Could not check the player names; try again.");
        }));
    },

    /** meta (optional): where an event's player stands, {standing, gamesPlayed, dropped}; shown as the chip's tooltip. */
    _addResolvedPlayer: function (name, meta) {
        if (this._findPlayer(name) != null)
            return;
        var entry = {input: name, name: name, status: "ok", suggestions: [], meta: meta || null};
        this.players.push(entry);
        this._renderChip(entry);
        this._hideSummary();
    },

    _findPlayer: function (name, except) {
        var lower = String(name).toLowerCase();
        for (var i = 0; i < this.players.length; i++) {
            var p = this.players[i];
            if (p === except)
                continue;
            if ((p.name != null && p.name.toLowerCase() == lower) || p.input.toLowerCase() == lower)
                return p;
        }
        return null;
    },

    _removePlayer: function (entry) {
        entry.removed = true;
        this.players = $.grep(this.players, function (p) { return p !== entry; });
        if (entry.chip)
            entry.chip.remove();
        this._hideSummary();
    },

    _renderChip: function (entry) {
        var that = this;
        var chip = $("<span class='additems-chip'></span>").addClass("additems-chip-" + entry.status);
        chip.append($("<span class='additems-chip-name'></span>").text(entry.status == "ok" ? entry.name : entry.input));
        if (entry.status == "checking")
            chip.attr("title", "Checking...");
        if (entry.status == "ok" && entry.meta) {
            // loaded from an event: 0-game players (always the last ones loaded) are dimmed, so they are easy to spot
            chip.attr("title", this._standingText(entry.meta));
            if (!(entry.meta.gamesPlayed > 0))
                chip.addClass("additems-chip-nogames");
            if (entry.meta.dropped)
                chip.addClass("additems-chip-dropped");
        }
        if (entry.status == "unknown") {
            chip.attr("title", "No player with this name");
            var sugg = $("<span class='additems-chip-suggestions'></span>");
            if (entry.suggestions.length > 0) {
                sugg.append("<span class='additems-chip-hint'>did you mean</span>");
                $.each(entry.suggestions, function (i, suggestion) {
                    sugg.append($("<a href='#' class='additems-suggestion'></a>").text(suggestion).click(function (event) {
                        event.preventDefault();
                        if (that._findPlayer(suggestion, entry) != null) {
                            that._removePlayer(entry);
                            return;
                        }
                        entry.name = suggestion;
                        entry.input = suggestion;
                        entry.status = "ok";
                        entry.suggestions = [];
                        that._renderChip(entry);
                        that._hideSummary();
                    }));
                });
            } else {
                sugg.append("<span class='additems-chip-hint'>unknown</span>");
            }
            chip.append(sugg);
        }
        chip.append($("<a href='#' class='additems-chip-remove' title='Remove'>&times;</a>").click(function (event) {
            event.preventDefault();
            that._removePlayer(entry);
        }));
        if (entry.chip)
            entry.chip.replaceWith(chip);
        else
            chip.insertBefore(this.playerInput);
        entry.chip = chip;
    },

    // ---- events ("load from event") ----

    loadEvents: function () {
        var that = this;
        var select = this.eventSelect;
        select.empty().append($("<option value=''></option>").text("Loading..."));
        this.comm.getAddItemsEvents(function (json) {
            that.setEvents((json && json.events) || []);
        }, this._silentErrors(function () {
            select.empty().append($("<option value=''></option>").text("Could not load events"));
        }));
    },

    setEvents: function (list) {
        var select = this.eventSelect;
        this.events = {};
        select.empty().append($("<option value=''></option>").text(list.length > 0 ? "Choose an event" : "No recent events"));
        var leagues = $("<optgroup label='Leagues'></optgroup>");
        var tournaments = $("<optgroup label='Tournaments'></optgroup>");
        for (var i = 0; i < list.length; i++) {
            var e = list[i];
            var key = e.kind + ":" + e.id;
            this.events[key] = e;
            var label = this._eventLabel(e);
            var option = $("<option></option>").attr("value", key).attr("title", label).text(label);
            (e.kind == "tournament" ? tournaments : leagues).append(option);
        }
        if (leagues.children().length > 0)
            select.append(leagues);
        if (tournaments.children().length > 0)
            select.append(tournaments);
        select.val("");
        this._updateEventTitle();
    },

    _updateEventTitle: function () {
        var option = this.eventSelect.find("option:selected");
        if (this.eventSelect.val() && option.length > 0)
            this.eventSelect.attr("title", option.text());
        else
            this.eventSelect.removeAttr("title");
    },

    _eventLabel: function (e) {
        var text = e.name || e.id;
        var dates = this._dateText(e.start);
        if (e.end)
            dates += (dates ? " – " : "") + this._dateText(e.end);
        else if (e.running)
            dates += (dates ? " – " : "") + "running";
        if (this._notStarted(e))
            dates += (dates ? ", " : "") + "not started";
        if (dates)
            text += " (" + dates + ")";
        if (e.playerCount != null)
            text += " · " + e.playerCount + " signed up";
        return text;
    },

    /** A league ("upcoming") or tournament ("scheduled") that has not started; listed only once someone signed up. */
    _notStarted: function (e) {
        return e.status == "upcoming" || e.status == "scheduled";
    },

    /** Tooltip of a chip loaded from an event: "3rd place · 4 games", "No games played · dropped", ... */
    _standingText: function (meta) {
        var parts = [];
        if (meta.standing != null && meta.gamesPlayed > 0)
            parts.push(this._ordinal(meta.standing) + " place");
        parts.push(meta.gamesPlayed > 0 ? meta.gamesPlayed + " game" + (meta.gamesPlayed == 1 ? "" : "s")
            : "No games played");
        if (meta.dropped)
            parts.push("dropped");
        return parts.join(" · ");
    },

    _ordinal: function (n) {
        var mod100 = n % 100, mod10 = n % 10;
        var suffix = (mod100 >= 11 && mod100 <= 13) ? "th" : mod10 == 1 ? "st" : mod10 == 2 ? "nd" : mod10 == 3 ? "rd" : "th";
        return n + suffix;
    },

    _dateText: function (iso) {
        return iso ? String(iso).substr(0, 10) : "";
    },

    /**
     * Adds every participant of the chosen event, whether they have played or not, to the players list in the order
     * the server sends them: the event's standings, so players with no games come last (order "standings"), or
     * alphabetical when nobody has played yet (order "name").  Names already there (input or resolved name, ignoring
     * case) are left alone and not counted again.
     */
    loadFromEvent: function () {
        var that = this;
        var key = this.eventSelect.val();
        var event = key ? this.events[key] : null;
        if (!event) {
            this.result.text("Choose an event to load players from first.");
            return;
        }
        var button = this.eventLoadButton;
        button.button("option", "disabled", true);
        this.result.text("Loading players from " + event.name + "...");
        this.comm.getAddItemsEventParticipants(event.kind, event.id, function (json) {
            button.button("option", "disabled", false);
            var players = (json && json.players) || [];
            var added = 0, addedNoGames = 0;
            for (var i = 0; i < players.length; i++) {
                var p = players[i];
                if (!p || !p.name || that._findPlayer(p.name) != null)
                    continue;
                that._addResolvedPlayer(p.name, {standing: p.standing, gamesPlayed: p.gamesPlayed || 0, dropped: !!p.dropped});
                added++;
                if (!(p.gamesPlayed > 0))
                    addedNoGames++;
            }
            var eventName = (json && json.name) || event.name;
            if (players.length == 0) {
                that.result.text("Nobody has signed up for " + eventName + ".");
                return;
            }
            var skipped = players.length - added;
            var text = "Added " + added + " player" + (added == 1 ? "" : "s") + " from " + eventName;
            if (added > 1)
                text += (json && json.order == "standings") ? ", in standings order" : ", alphabetically (no games played yet)";
            if (json && json.order == "standings" && addedNoGames > 0)
                text += "; the last " + (addedNoGames == 1 ? "one has" : addedNoGames + " have") + " played no games";
            if (skipped > 0)
                text += " (" + skipped + " already in the list)";
            that.result.text(text + ".");
        }, this._errorMap(this.result, function () { button.button("option", "disabled", false); }));
    },

    // ---- items ----

    _bindItems: function () {
        var that = this;
        var input = this.itemInput;
        input.autocomplete({
            minLength: 2,
            delay: this.searchDelay,
            autoFocus: true,
            source: function (request, response) {
                that.comm.searchItems(request.term, 20, function (json) {
                    var items = (json && json.items) || [];
                    response($.map(items, function (item) { return {label: item.title, value: item.value, item: item}; }));
                }, that._silentErrors(function () { response([]); }));
            },
            focus: function () { return false; },
            select: function (event, ui) {
                that.addItem(ui.item.item);
                input.val("");
                return false;
            }
        });
        input.autocomplete("instance")._renderItem = function (ul, entry) {
            var item = entry.item;
            var div = $("<div class='additems-option'></div>");
            div.append($("<span class='additems-kind'></span>").addClass("additems-kind-" + item.kind).text(item.kind));
            div.append($("<span class='additems-option-title'></span>").text(item.title));
            if (item.subtitle)
                div.append($("<span class='additems-option-subtitle'></span>").text(item.subtitle));
            var meta = item.kind == "card" ? item.value + (item.detail ? " · " + item.detail : "") : (item.detail || "");
            if (meta)
                div.append($("<span class='additems-option-meta'></span>").text(meta));
            return $("<li></li>").append(div).appendTo(ul);
        };
    },

    /**
     * Adds a search result ({value, kind, title, subtitle, detail}) to the item list; picking the same thing again
     * adds one to its quantity instead.
     */
    addItem: function (item, count) {
        var that = this;
        count = count || 1;
        var existing = this.itemList.find(".additems-item").filter(function () {
            return $(this).data("item").value === item.value;
        });
        if (existing.length > 0) {
            var qty = existing.find(".additems-item-count");
            qty.val(this._toInt(qty.val(), 0) + count);
            this._hideSummary();
            return existing;
        }

        var row = $("<div class='additems-item flex-horiz'></div>").data("item", item);
        row.append($("<input type='number' min='1' class='additems-item-count'>").val(count));
        row.append("<span class='additems-item-x'>x</span>");
        row.append($("<span class='additems-kind'></span>").addClass("additems-kind-" + item.kind).text(item.kind));

        var name = $("<span class='additems-item-name'></span>");
        if (item.kind == "card") {
            name.append($("<div class='cardHint'></div>").attr("value", item.value).text(item.title));
            if (item.subtitle)
                name.append($("<span class='additems-item-subtitle'></span>").text(item.subtitle));
        } else {
            name.text(item.title);
        }
        row.append(name);

        var meta = item.kind == "card" ? item.value + (item.detail ? " · " + item.detail : "") : (item.detail || "");
        row.append($("<span class='additems-item-meta'></span>").text(meta));

        if (item.kind == "card") {
            var foil = $("<label class='additems-item-foil'></label>");
            foil.append($("<input type='checkbox' class='additems-item-foil-box'>")).append(" foil");
            row.append(foil);
        }
        row.append($("<button type='button' class='additems-item-remove'>Remove</button>").button().click(function () {
            row.remove();
            that._refreshItemsEmpty();
            that._hideSummary();
        }));
        this.itemList.append(row);
        this._refreshItemsEmpty();
        this._hideSummary();
        return row;
    },

    _refreshItemsEmpty: function () {
        this.itemList.find(".additems-items-empty").remove();
        if (this.itemList.find(".additems-item").length == 0)
            this.itemList.append("<div class='additems-items-empty'>No items yet: search above and pick from the list.</div>");
    },

    /**
     * @return [{line: "3x1_1*", count, value, item}] as the addItems endpoint reads them
     */
    getItems: function () {
        var that = this;
        var result = [];
        this.itemList.find(".additems-item").each(function () {
            var row = $(this);
            var item = row.data("item");
            var value = item.value + (row.find(".additems-item-foil-box").prop("checked") ? "*" : "");
            var count = that._toInt(row.find(".additems-item-count").val(), 0);
            result.push({line: count + "x" + value, count: count, value: value, item: item, row: row});
        });
        return result;
    },

    // ---- collections ----

    loadCollections: function () {
        var that = this;
        var select = this.collectionSelect;
        select.empty().append($("<option value=''></option>").text("Loading..."));
        this.comm.getAddItemsCollections(function (json) {
            that.setCollections((json && json.collections) || []);
        }, this._silentErrors(function () {
            select.empty().append($("<option value=''></option>").text("Could not load the collections"));
        }));
    },

    setCollections: function (list) {
        var select = this.collectionSelect;
        this.collections = {};
        select.empty().append($("<option value=''></option>").text("Choose a collection"));
        var running = $("<optgroup label='Running leagues'></optgroup>");
        var upcoming = $("<optgroup label='Upcoming leagues'></optgroup>");
        for (var i = 0; i < list.length; i++) {
            var c = list[i];
            var option = $("<option></option>").attr("value", c.value).text(this._collectionLabel(c));
            this.collections[c.value] = c;
            if (c.kind == "league")
                (c.running ? running : upcoming).append(option);
            else
                select.append(option);
        }
        if (running.children().length > 0)
            select.append(running);
        if (upcoming.children().length > 0)
            select.append(upcoming);
        select.val("");
    },

    _collectionLabel: function (c) {
        if (c.kind != "league")
            return c.label;
        var type = c.leagueType == "SOLODRAFT" ? "draft" : (c.leagueType == "SEALED" ? "sealed" : (c.leagueType || "").toLowerCase());
        var text = c.label + (type ? " (" + type : "");
        if (!c.running && c.start)
            text += (type ? ", " : " (") + "starts " + c.start;
        else if (c.end)
            text += (type ? ", " : " (") + "until " + c.end;
        if (type || c.start || c.end)
            text += ")";
        return text;
    },

    // ---- review / send ----

    /**
     * @return null when the form is complete, else what is missing
     */
    validate: function () {
        this.itemList.find(".additems-invalid").removeClass("additems-invalid");
        if (this.players.length == 0)
            return "Add at least one player.";
        var checking = $.grep(this.players, function (p) { return p.status == "checking"; });
        if (checking.length > 0)
            return "Still checking " + checking.length + " player name" + (checking.length == 1 ? "" : "s") + "; try again in a moment.";
        var unknown = $.grep(this.players, function (p) { return p.status != "ok"; });
        if (unknown.length > 0)
            return "Unknown player" + (unknown.length == 1 ? "" : "s") + ": " + $.map(unknown, function (p) { return p.input; }).join(", ")
                + ". Pick a suggestion or remove the names marked in red.";
        var items = this.getItems();
        if (items.length == 0)
            return "Add at least one item.";
        for (var i = 0; i < items.length; i++) {
            if (!(items[i].count >= 1)) {
                items[i].row.addClass("additems-invalid");
                return "Each item needs a quantity of at least 1 (" + items[i].item.title + ").";
            }
        }
        if (!this.collectionSelect.val())
            return "Choose the collection to add the items to.";
        if ($.trim(this.reasonInput.val()).length > 255)
            return "The reason must be 255 characters or less.";
        return null;
    },

    /**
     * Shows who gets what, into which collection, with the Send button.  Nothing is sent from here.
     */
    review: function () {
        var problem = this.validate();
        if (problem != null) {
            this._hideSummary();
            this.result.text(problem);
            return false;
        }
        var players = this.getPlayerNames();
        var items = this.getItems();
        var collection = this.collections[this.collectionSelect.val()] || {label: this.collectionSelect.val()};
        var body = this.summary.find("#additems-summary-body").empty();

        var head = $("<div class='additems-summary-head'></div>");
        head.append(document.createTextNode("Each of these " + players.length + " player" + (players.length == 1 ? "" : "s") + " gets:"));
        body.append(head);
        var ul = $("<ul class='additems-summary-items'></ul>");
        for (var i = 0; i < items.length; i++) {
            var it = items[i];
            var li = $("<li></li>");
            li.append(document.createTextNode(it.count + "x "));
            li.append($("<span class='additems-kind'></span>").addClass("additems-kind-" + it.item.kind).text(it.item.kind));
            li.append(document.createTextNode(" " + it.item.title + (it.item.subtitle ? ", " + it.item.subtitle : "")
                + (it.value != it.item.title ? " (" + it.value + ")" : "")));
            ul.append(li);
        }
        body.append(ul);
        body.append($("<div class='additems-summary-line'></div>").text("Into: " + this._collectionLabel(collection)));
        body.append($("<div class='additems-summary-line'></div>").text("Players: " + players.join(", ")));
        var reason = $.trim(this.reasonInput.val());
        body.append($("<div class='additems-summary-line'></div>").text("Recorded as: " + (reason || "Administrator action")));
        if (collection.kind == "league")
            body.append($("<div class='additems-summary-note'></div>").text("Players who have not joined this league have no such collection and are skipped."));
        body.append($("<div class='additems-summary-note'></div>").text("This cannot be undone."));

        this.summary.prop("hidden", false);
        this.result.text("Check the summary, then send.");
        return true;
    },

    getPlayerNames: function () {
        return $.map($.grep(this.players, function (p) { return p.status == "ok"; }), function (p) { return p.name; });
    },

    send: function () {
        var that = this;
        var problem = this.validate();
        if (problem != null) {
            this._hideSummary();
            this.result.text(problem);
            return;
        }
        var players = this.getPlayerNames();
        var lines = $.map(this.getItems(), function (it) { return it.line; });
        var collection = this.collectionSelect.val();
        var reason = $.trim(this.reasonInput.val());
        var sendButton = this.root.find("#additems-send-button");
        sendButton.button("option", "disabled", true);
        this.result.text("Processing...");
        this.playerResults.empty();

        var errorMap = this._errorMap(this.result, function () { sendButton.button("option", "disabled", false); });
        this.comm.addItemsDetailed(collection, lines.join("\n"), players.join("\n"), reason, function (json) {
            sendButton.button("option", "disabled", false);
            that._hideSummary();
            that.renderResults(json);
            // the items were delivered: clear them so a second click cannot hand them out twice
            that.itemList.find(".additems-item").remove();
            that._refreshItemsEmpty();
        }, errorMap);
    },

    renderResults: function (json) {
        var results = (json && json.results) || [];
        var ok = $.grep(results, function (r) { return r.status == "ok"; }).length;
        var text = "Delivered to " + ok + " of " + results.length + " player" + (results.length == 1 ? "" : "s")
            + " (" + (json.collectionName || json.collectionType) + ").";
        this.result.text(text);
        var list = this.playerResults.empty();
        for (var i = 0; i < results.length; i++) {
            var r = results[i];
            var li = $("<li></li>").addClass("additems-result-" + r.status);
            li.append($("<span class='additems-result-player'></span>").text(r.player));
            li.append($("<span class='additems-result-status'></span>").text(r.status == "ok" ? "delivered" : (r.status + (r.message ? ": " + r.message : ""))));
            list.append(li);
        }
    },

    clear: function () {
        for (var i = this.players.length - 1; i >= 0; i--)
            this._removePlayer(this.players[i]);
        this.playerInput.val("");
        this.itemInput.val("");
        this.itemList.find(".additems-item").remove();
        this._refreshItemsEmpty();
        this.collectionSelect.val("");
        this.reasonInput.val("");
        this.playerResults.empty();
        this.result.text("Ready.");
        this._hideSummary();
    },

    _hideSummary: function () {
        if (this.summary && !this.summary.prop("hidden"))
            this.summary.prop("hidden", true);
    },

    // ---- helpers ----

    _toInt: function (value, fallback) {
        var n = parseInt(value, 10);
        return isNaN(n) ? fallback : n;
    },

    _silentErrors: function (callback) {
        var map = {};
        $.each(["0", "400", "401", "403", "404", "410", "500"], function (i, code) { map[code] = callback; });
        return map;
    },

    // same messages as the rest of the Prizes tab (prizeErrorMap); the 400 message is the server's own
    _errorMap: function (output, after) {
        var done = function (text) {
            output.text(text);
            if (after)
                after();
        };
        return {
            "0": function () { done("0: Server has been shut down or there was a problem with your internet connection."); },
            "400": function (xhr) {
                var message = xhr && xhr.getResponseHeader ? xhr.getResponseHeader("message") : null;
                done(message != null ? "400; malformed input: " + message
                    : "400: One of the provided parameters was malformed.  Double-check your input and try again.");
            },
            "401": function () { done("401: You are not logged in."); },
            "403": function () { done("403: You do not have permission to perform such actions."); },
            "404": function () { done("404: Info not found.  Check that your input is correct with removed whitespace and try again."); },
            "410": function () { done("410: You have been inactive for too long and were logged out. Refresh the page if you wish to re-establish connection."); },
            "500": function () { done("500: Server error. One of the provided parameters was probably malformed.  Double-check your input and try again."); }
        };
    }
});
