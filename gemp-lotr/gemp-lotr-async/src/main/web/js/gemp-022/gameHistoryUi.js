/**
 * My Account > Game History: the logged-in player's games, newest first, a page at a time, with server-side filters
 * (format, opponent, event, date range).  Data comes from GET /gameHistory (XML; see GameHistoryRequestHandler) and
 * the filter suggestions from GET /gameHistory/filters.
 *
 *   new GameHistoryUI(url, {container: $("#gameHistory"), comm: optionalCommunication, pageSize: 20})
 *   ui.refresh();   // re-fetch the page shown, with the filters last applied (the Account tab calls it when the
 *                   // viewer comes back); what is typed in the filter form but not applied is left alone
 */
var GameHistoryUI = Class.extend({
    communication:null,
    container:null,
    itemStart:0,
    pageSize:20,
    matching:0,
    total:0,
    filters:null,
    requestNumber:0,

    formSelect:null,
    opponentInput:null,
    eventInput:null,
    eventList:null,
    fromInput:null,
    toInput:null,
    statusDiv:null,
    tableDiv:null,
    pagers:null,

    init:function (url, options) {
        options = options || {};
        this.communication = options.comm || new GempLotrCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });
        this.container = options.container || $("#gameHistory");
        if (options.pageSize)
            this.pageSize = options.pageSize;
        this.filters = {};
        this.buildLayout();
        this.loadFilterOptions();
        this.loadHistory();
    },

    buildLayout:function () {
        var that = this;
        this.container.empty().addClass("game-history");

        var form = $("<form class='game-history-filters' autocomplete='off'></form>");
        var field = function (label, input) {
            var id = "game-history-" + label.toLowerCase().replace(/[^a-z]+/g, "-");
            input.attr("id", id);
            return $("<span class='game-history-field'></span>")
                .append($("<label></label>").attr("for", id).text(label))
                .append(input);
        };

        this.formSelect = $("<select name='format'></select>").append($("<option value=''></option>").text("Any format"));
        this.opponentInput = $("<input type='text' name='opponent' maxlength='30' placeholder='Name or start of name'>");
        this.eventList = $("<datalist id='game-history-event-options'></datalist>");
        this.eventInput = $("<input type='text' name='event' maxlength='255' list='game-history-event-options' placeholder='League, tournament or \"casual\"'>");
        this.fromInput = $("<input type='date' name='from'>");
        this.toInput = $("<input type='date' name='to'>");

        form.append(field("Format", this.formSelect))
            .append(field("Opponent", this.opponentInput))
            .append(field("Event", this.eventInput))
            .append(this.eventList)
            .append(field("From", this.fromInput))
            .append(field("To", this.toInput));

        var apply = $("<button type='submit' class='game-history-apply'>Apply</button>").button();
        var clear = $("<button type='button' class='game-history-clear'>Clear</button>").button();
        form.append($("<span class='game-history-buttons'></span>").append(apply).append(clear));

        form.on("submit", function (e) {
            e.preventDefault();
            that.applyFilters();
        });
        clear.on("click", function () {
            that.clearFilters();
        });

        this.statusDiv = $("<div class='game-history-status' role='status' aria-live='polite'></div>");
        this.tableDiv = $("<div class='game-history-table'></div>");
        this.pagers = [this.createPager(), this.createPager()];

        this.container.append(form)
            .append(this.pagers[0].element)
            .append(this.statusDiv)
            .append(this.tableDiv)
            .append(this.pagers[1].element);
    },

    createPager:function () {
        var that = this;
        var element = $("<div class='game-history-pager'></div>");
        var mk = function (label, cls, ariaLabel) {
            return $("<button type='button'></button>").addClass(cls).text(label).attr("aria-label", ariaLabel).button();
        };
        var first = mk("« First", "pager-first", "First page");
        var prev = mk("‹ Previous", "pager-prev", "Previous page");
        var info = $("<span class='pager-info'></span>");
        var next = mk("Next ›", "pager-next", "Next page");
        var last = mk("Last »", "pager-last", "Last page");
        first.on("click", function () { that.goToStart(0); });
        prev.on("click", function () { that.goToStart(that.itemStart - that.pageSize); });
        next.on("click", function () { that.goToStart(that.itemStart + that.pageSize); });
        last.on("click", function () { that.goToStart((that.pageCount() - 1) * that.pageSize); });
        element.append(first).append(prev).append(info).append(next).append(last);
        return {element: element, first: first, prev: prev, info: info, next: next, last: last};
    },

    pageCount:function () {
        return Math.max(1, Math.ceil(this.matching / this.pageSize));
    },

    goToStart:function (start) {
        var maxStart = (this.pageCount() - 1) * this.pageSize;
        start = Math.max(0, Math.min(maxStart, start));
        if (start === this.itemStart)
            return;
        this.itemStart = start;
        this.loadHistory();
    },

    readFilters:function () {
        return {
            format: this.formSelect.val() || "",
            opponent: $.trim(this.opponentInput.val() || ""),
            event: $.trim(this.eventInput.val() || ""),
            from: this.fromInput.val() || "",
            to: this.toInput.val() || ""
        };
    },

    applyFilters:function () {
        var filters = this.readFilters();
        if (filters.from && filters.to && filters.from > filters.to) {
            this.statusDiv.text("The 'From' date is after the 'To' date.");
            return;
        }
        this.filters = filters;
        this.itemStart = 0;
        this.loadHistory();
    },

    clearFilters:function () {
        this.formSelect.val("");
        this.opponentInput.val("");
        this.eventInput.val("");
        this.fromInput.val("");
        this.toInput.val("");
        this.filters = {};
        this.itemStart = 0;
        this.loadHistory();
    },

    hasFilters:function () {
        var f = this.filters || {};
        return !!(f.format || f.opponent || f.event || f.from || f.to);
    },

    loadFilterOptions:function () {
        var that = this;
        if (!this.communication.getGameHistoryFilters)
            return;
        this.communication.getGameHistoryFilters(function (json) {
            var formats = (json && json.formats) || [];
            var events = (json && json.events) || [];
            var current = that.formSelect.val();
            that.formSelect.find("option:not(:first)").remove();
            $.each(formats, function (i, format) {
                that.formSelect.append($("<option></option>").attr("value", format).text(format));
            });
            that.formSelect.val(current);
            that.eventList.empty();
            that.eventList.append($("<option value='casual'></option>").text("Casual games"));
            $.each(events, function (i, event) {
                that.eventList.append($("<option></option>").attr("value", event));
            });
        }, {
            // the filters still work as free text without suggestions
            "0": function () {}, "400": function () {}, "401": function () {}, "500": function () {}
        });
    },

    // A data refresh: the same page and applied filters, fetched again without the "Loading…" line; a failure
    // keeps the page shown.  Suggestions for the filters are fetched again too (the typed values stay).
    refresh:function () {
        this.loadFilterOptions();
        this.loadHistory(true);
    },

    // quiet: a data refresh (see refresh)
    loadHistory:function (quiet) {
        var that = this;
        var requestNumber = ++this.requestNumber;
        if (quiet) {
            this.communication.getGameHistory(this.itemStart, this.pageSize,
                function (xml) {
                    if (requestNumber === that.requestNumber)
                        that.loadedGameHistory(xml);
                }, {
                    "0": function () {}, "400": function () {}, "401": function () {}, "500": function () {}
                }, this.filters);
            return;
        }
        this.statusDiv.text("Loading…");
        this.setPagerEnabled(false);
        this.communication.getGameHistory(this.itemStart, this.pageSize,
            function (xml) {
                if (requestNumber !== that.requestNumber)
                    return;     // an older request answering after a newer one
                that.loadedGameHistory(xml);
            }, {
                "0": function () { that.showError("Could not reach the server. Check your connection and try again."); },
                "400": function (xhr) {
                    var message = xhr && xhr.getResponseHeader ? xhr.getResponseHeader("message") : null;
                    that.showError(message || "The server could not use those filters.");
                },
                "401": function () { that.showError("You are not logged in."); },
                "500": function () { that.showError("Could not load your game history. Please try again later."); }
            }, this.filters);
    },

    showError:function (text) {
        this.statusDiv.text(text);
        this.setPagerEnabled(true);
        this.updatePager();
    },

    setPagerEnabled:function (enabled) {
        $.each(this.pagers, function (i, pager) {
            pager.element.find("button").button("option", "disabled", !enabled);
        });
    },

    updatePager:function () {
        var that = this;
        var pages = this.pageCount();
        var page = Math.floor(this.itemStart / this.pageSize) + 1;
        var atStart = this.itemStart <= 0;
        var atEnd = page >= pages;
        $.each(this.pagers, function (i, pager) {
            pager.first.button("option", "disabled", atStart);
            pager.prev.button("option", "disabled", atStart);
            pager.next.button("option", "disabled", atEnd);
            pager.last.button("option", "disabled", atEnd);
            pager.info.text("Page " + page + " of " + pages);
            pager.element.toggle(that.matching > that.pageSize);
        });
    },

    // One historyEntry element -> plain row object for the table
    entryToRow:function (entry, playerId) {
        var attr = function (name) { return entry.getAttribute(name); };
        var winner = attr("winner");
        var result = attr("result") || (winner === playerId ? "W" : "L");
        var opponent = attr("opponent") || (result === "W" ? attr("loser") : winner);
        var startMs = attr("startMs") != null ? parseInt(attr("startMs"), 10) : null;
        var endMs = attr("endMs") != null ? parseInt(attr("endMs"), 10) : null;
        return {
            result: result,
            winReason: attr("winReason"),
            loseReason: attr("loseReason"),
            opponent: opponent,
            deck: attr("deckName"),
            format: attr("formatName"),
            event: attr("tournament"),
            startMs: startMs,
            endMs: endMs,
            endTime: attr("endTime"),
            duration: startMs != null && endMs != null ? endMs - startMs : null,
            recordingId: attr("gameRecordingId")
        };
    },

    loadedGameHistory:function (xml) {
        var root = xml && xml.documentElement;
        if (!root || root.tagName != 'gameHistory')
            return;

        var playerId = root.getAttribute("playerId");
        this.matching = parseInt(root.getAttribute("count"), 10) || 0;
        this.total = root.getAttribute("total") != null ? (parseInt(root.getAttribute("total"), 10) || 0) : this.matching;
        var start = root.getAttribute("start");
        if (start != null)
            this.itemStart = parseInt(start, 10) || 0;

        var rows = [];
        var entries = root.getElementsByTagName("historyEntry");
        for (var i = 0; i < entries.length; i++)
            rows.push(this.entryToRow(entries[i], playerId));

        this.tableDiv.empty().append(this.renderRows(rows, playerId));

        var filtered = this.hasFilters();
        if (this.matching === 0) {
            this.statusDiv.text(filtered ? "No games match these filters." : "You have not finished any games yet.");
        } else {
            var first = this.itemStart + 1;
            var last = this.itemStart + rows.length;
            var text = "Showing " + first + "–" + last + " of " + this.matching + (this.matching == 1 ? " game" : " games");
            if (filtered)
                text += " matching the filters (" + this.total + " in all)";
            this.statusDiv.text(text + ".");
        }
        this.setPagerEnabled(true);
        this.updatePager();
    },

    renderRows:function (rows, playerId) {
        // Header text is centred; cell text is centred except in the name-like columns (gh-left: Format, Event,
        // My Deck), and My Deck wraps early so the other columns keep their room.
        var columns = [
            {key: "endMs", title: "Date", className: "gh-when",
                headerTitle: "The day the game ended, in server time (UTC)",
                render: function (row) {
                    var day = GameHistoryUI.serverDate(row);
                    var cell = $("<span></span>").text(day);
                    if (row.endMs != null)
                        cell.attr("title", "Ended " + (row.endTime ? row.endTime + " server time (UTC); " : "")
                            + formatTime(row.endMs) + " your time");
                    return cell;
                }},
            {key: "duration", title: "Length", className: "gh-duration",
                render: function (row) { return formatDuration(row.duration); }},
            {key: "result", title: "Result", className: "gh-result",
                render: function (row) {
                    var won = row.result === "W";
                    return $("<span></span>").addClass(won ? "gh-win" : "gh-loss").text(won ? "W" : "L")
                        .attr("title", won ? "Won" : "Lost");
                }},
            // the loser's reason for both wins and losses: it names the timeout, concession, corruption and so on
            {key: "loseReason", title: "Reason", className: "gh-reason",
                headerTitle: "Why the losing player lost: a timeout, a concession, a corrupted Ring-bearer and so on",
                render: function (row) {
                    var cell = $("<span></span>").text(row.loseReason || "—");
                    if (row.winReason)
                        cell.attr("title", "Winner: " + row.winReason);
                    return cell;
                }},
            {key: "opponent", title: "Opponent", className: "gh-opponent"},
            {key: "format", title: "Format", className: "gh-format gh-left"},
            {key: "event", title: "Event", className: "gh-event gh-left",
                render: function (row) { return row.event || "Casual"; }},
            {key: "deck", title: "My Deck", className: "gh-deck gh-left",
                render: function (row) { return $("<div class='gh-deck-name'></div>").text(row.deck || "—"); }},
            {key: "recordingId", title: "Replay", className: "gh-replay",
                render: function (row) {
                    if (!row.recordingId)
                        return $("<i></i>").text("not stored");
                    return $("<a target='_blank' rel='noopener'></a>").text("Replay")
                        .attr("title", "Opens the replay in a new tab")
                        .attr("href", "game.html?replayId=" + playerId + "$" + row.recordingId);  // same link as before
                }}
        ];
        // Pages come from the server newest first; sorting inside one page would only mislead, so no sort headers.
        return renderTable(columns, rows, {
            sortable: false,
            className: "gameHistory",
            emptyText: this.hasFilters() ? "No games match these filters." : "No games yet."
        });
    }
});

/** The day a game ended, "yyyy-MM-dd" in server time (UTC), from its epoch ms or else its endTime text. */
GameHistoryUI.serverDate = function (row) {
    if (row.endMs != null && !isNaN(row.endMs)) {
        var date = new Date(row.endMs);
        if (!isNaN(date.getTime()))
            return date.toISOString().substring(0, 10);
    }
    return row.endTime ? String(row.endTime).substring(0, 10) : "";
};
