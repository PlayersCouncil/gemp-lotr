/**
 * Browser for completed events of ONE kind ("league" or "tournament"), a month at a time.  The Events tab has one
 * panel under Current Leagues (kind "league") and one under Current Tournaments (kind "tournament").
 *
 *   new EventHistoryUI(hall.comm, $("#leagueHistory"), "league", leagueUI).show();
 *   new EventHistoryUI(hall.comm, $("#tournamentHistory"), "tournament", tourneyUI).show();
 *
 * The header mirrors the calendar's (.calendar-header): "<", the month title and ">".  show() builds the panel without
 * requesting anything; start() then loads it by itself: it fetches the list of months with completed events and shows
 * the newest of them (up to the current UTC month), or says there are none yet.  The Events tab calls start() once the
 * live list above the panel has rendered, so the completed events never hold up the current ones.  From then on every
 * arrow press / dropdown choice fetches straight away; one made before start() starts the panel on that month instead.
 *
 * Both the arrows and the dropdown work from the list of months the server reports as having completed events of this
 * kind (fetched once, on the first arrow press or dropdown opening): the arrows jump to the previous / next such
 * month, skipping empty ones, and are disabled when there is none in that direction.  If that list cannot be fetched
 * the arrows fall back to stepping one calendar month, so the control never becomes unusable.
 *
 * Data (see communication.js):
 *   comm.getEventHistoryMonths(kind)        -> {kind, months:["2026-09","2026-08",...]}   newest first
 *   comm.getEventHistory(month, kind)       -> {month, kind, isAdmin, events:[...]}
 *   comm.getLeague(id) / comm.getTournament(id) -> the XML the live displays render
 *
 * "+ Details" (first column, where the Current Leagues / Tournaments rows have theirs, and the same button) on a row
 * expands a row beneath it and renders the league / tournament with the SAME renderer the live
 * display uses (LeagueResultsUI.loadedLeague into a target, TournamentResultsUI.loadedTournament), so a finished
 * league shows its full serie breakdown and results tabs and a finished tournament its standings with deck links.
 * The row slides open and shut (and eases to its new height when the fetched details replace the loading line); the
 * drawer mechanism is EventDrawer (eventDrawer.js), shared with the Current Leagues / Tournaments lists.
 *
 * The header is a MonthNav (above), which Server Info › Patch Notes uses too.
 *
 * Admin extras are gated only on the response's isAdmin flag (never hall.userInfo, which is filled in
 * asynchronously and does not exist on every page).  Every server-provided string in the table goes into the DOM
 * through .text()/.attr().
 */
/**
 * MonthNav: the month navigation of the completed-events panels (EventHistoryUI below), shared with Server Info ›
 * Patch Notes (patchNotesUi.js).  "<", the month title and ">", in the calendar's header style (.calendar-header,
 * .calendar-title; hall.css); the title is also the button that opens a dropdown of months grouped by year.  It only
 * draws and reports: the host decides what the arrows and a chosen month do and which months there are.
 *
 *   var nav = new MonthNav({
 *       prevTitle: "...", nextTitle: "...",   // the arrows' tooltips
 *       onPrev: fn, onNext: fn,               // an arrow was pressed
 *       onOpen: fn,                           // the dropdown opened: fill it with renderList (loading it if need be)
 *       onClose: fn,                          // the dropdown closed
 *       onPick: fn(key),                      // a month ("yyyy-MM") was chosen in the dropdown (already closed)
 *       onRetry: fn                           // Retry in the dropdown's error state
 *   });
 *   container.append(nav.header);
 *   nav.setTitle("March 2026"); nav.setDisabled(prevOff, nextOff);
 *   nav.renderList("ready", months, currentKey, emptyText)   months: ["yyyy-MM", ...] as they should be listed
 *   nav.renderList("loading") / nav.renderList("error", null, null, message)
 *   nav.isOpen(), nav.open(), nav.close(), nav.toggle()
 */
var MonthNav = Class.extend({
    options: null,
    uid: 0,
    openState: false,

    header: null,
    prevBut: null,
    nextBut: null,
    title: null,
    titleText: null,
    dropdown: null,

    init: function (options) {
        var that = this;
        this.options = options || {};
        this.uid = ++MonthNav.instances;

        this.header = $("<div class='calendar-header event-history-header'></div>");
        this.prevBut = $("<button type='button'>&lt;</button>").attr("title", this.options.prevTitle || "Previous month")
            .button().click(function () { that.call("onPrev"); });
        this.nextBut = $("<button type='button'>&gt;</button>").attr("title", this.options.nextTitle || "Next month")
            .button().click(function () { that.call("onNext"); });

        var titleWrap = $("<span class='event-history-title-wrap'></span>");
        this.title = $("<span class='calendar-title event-history-month-title' role='button' tabindex='0'"
            + " aria-haspopup='true' aria-expanded='false' title='Choose a month'></span>");
        this.titleText = $("<span class='event-history-month-text'></span>");
        this.title.append(this.titleText).append("<span class='event-history-caret' aria-hidden='true'>&#9662;</span>");
        this.title.click(function () { that.toggle(); });
        this.title.keydown(function (e) {
            if (e.key == "Enter" || e.key == " ") {
                e.preventDefault();
                that.toggle();
            }
        });
        this.dropdown = $("<div class='event-history-dropdown'></div>").hide();
        titleWrap.append(this.title).append(this.dropdown);

        this.header.append(this.prevBut).append(titleWrap).append(this.nextBut);
    },

    call: function (name, arg) {
        if (typeof this.options[name] == "function")
            this.options[name].call(this, arg);
    },

    setTitle: function (text) {
        this.titleText.text(text);
    },

    setDisabled: function (prevOff, nextOff) {
        this.prevBut.button("option", "disabled", !!prevOff);
        this.nextBut.button("option", "disabled", !!nextOff);
    },

    isOpen: function () {
        return this.openState;
    },

    toggle: function () {
        if (this.openState)
            this.close();
        else
            this.open();
    },

    open: function () {
        var that = this;
        this.openState = true;
        this.title.attr("aria-expanded", "true").addClass("open");
        this.dropdown.show();
        var ns = ".monthNav" + this.uid;
        $(document).on("mousedown" + ns, function (e) {
            if ($(e.target).closest(that.title.parent()).length == 0)
                that.close();
        });
        $(document).on("keydown" + ns, function (e) {
            if (e.key == "Escape" || e.key == "Esc") {
                that.close();
                that.title.focus();
            }
        });
        this.call("onOpen");
    },

    close: function () {
        this.openState = false;
        this.title.attr("aria-expanded", "false").removeClass("open");
        this.dropdown.hide();
        $(document).off(".monthNav" + this.uid);
        this.call("onClose");
    },

    // state: "loading" | "error" (text: the reason) | "ready" (months as listed; current: the one to mark; text: what
    // an empty list says)
    renderList: function (state, months, current, text) {
        var that = this;
        var dd = this.dropdown;
        dd.empty();

        if (state == "loading") {
            dd.append($("<div class='event-history-dd-status event-history-loading'></div>").text("Loading months…"));
            return;
        }
        if (state == "error") {
            dd.append($("<div class='event-history-dd-status event-history-error'></div>")
                .text("Could not load the list of months. " + (text || "")));
            dd.append($("<button type='button'>Retry</button>").button().click(function () {
                that.call("onRetry");
            }));
            return;
        }

        months = months || [];
        if (months.length == 0) {
            dd.append($("<div class='event-history-dd-status event-history-empty'></div>").text(text || ""));
            return;
        }

        var group = null, groupYear = null;
        for (var i = 0; i < months.length; i++) {
            var key = String(months[i]);
            var year = key.split("-")[0];
            if (year !== groupYear) {
                groupYear = year;
                var yearDiv = $("<div class='event-history-dd-year'></div>");
                yearDiv.append($("<div class='event-history-dd-year-label'></div>").text(year));
                group = $("<div class='event-history-dd-months'></div>");
                yearDiv.append(group);
                dd.append(yearDiv);
            }
            var item = $("<button type='button' class='event-history-dd-month'></button>")
                .attr("data-month", key)
                .attr("title", EventHistoryUI.monthLabel(key))
                .text(EventHistoryUI.monthName(key));
            if (key == current)
                item.addClass("selected");
            item.click((function (value) {
                return function () {
                    that.close();
                    that.title.focus();
                    that.call("onPick", value);
                };
            })(key));
            group.append(item);
        }
    }
});

MonthNav.instances = 0;

var EventHistoryUI = Class.extend({
    comm: null,
    container: null,
    kind: null,            // "league" | "tournament"
    resultsUI: null,       // LeagueResultsUI / TournamentResultsUI used to render a row's details
    options: null,
    uid: 0,

    year: 0,               // the month in the title
    month: 0,              // 1-12
    live: false,           // false until start(); afterwards navigation fetches immediately
    started: false,
    touched: false,        // the viewer navigated before start(): start() shows their month, not the newest one
    requested: null,       // "yyyy-MM" most recently requested; responses for any other month are discarded
    monthsState: "idle",   // idle | loading | ready | error   (the months-with-events list)
    monthsError: null,
    monthsWaiting: null,   // callbacks queued on the in-flight months request; each gets the list, or null on failure
    dropdownOpen: false,

    nav: null,             // the MonthNav: "<", the month title with its dropdown, ">"
    root: null,
    prevBut: null,
    nextBut: null,
    title: null,
    titleText: null,
    dropdown: null,
    status: null,
    body: null,

    init: function (comm, container, kind, resultsUI, options) {
        this.comm = comm;
        this.container = container;
        this.kind = (kind == "tournament") ? "tournament" : "league";
        this.resultsUI = resultsUI;
        this.options = options || {};
        this.uid = ++EventHistoryUI.instances;
        var now = EventHistoryUI.currentMonth();
        this.year = now.year;
        this.month = now.month;
    },

    // Builds the panel.  No request is made here.
    show: function () {
        var that = this;
        this.container.empty();

        this.root = $("<div class='event-history'></div>").addClass("event-history-" + this.kind);
        this.root.append($("<div class='event-history-heading'></div>")
            .text(this.kind == "league" ? "Completed leagues" : "Completed tournaments"));

        this.nav = new MonthNav({
            prevTitle: "Previous month with events",
            nextTitle: "Next month with events",
            onPrev: function () { that.jump(-1); },
            onNext: function () { that.jump(1); },
            onOpen: function () {
                that.dropdownOpen = true;
                if (EventHistoryUI.monthsCache[that.kind] != null)
                    that.monthsState = "ready";
                else
                    that.loadMonths();
                that.renderDropdown();
            },
            onClose: function () { that.dropdownOpen = false; },
            onPick: function (key) { that.setMonth(key); },
            onRetry: function () {
                that.monthsState = "idle";
                that.loadMonths();
            }
        });
        this.prevBut = this.nav.prevBut;
        this.nextBut = this.nav.nextBut;
        this.title = this.nav.title;
        this.titleText = this.nav.titleText;
        this.dropdown = this.nav.dropdown;
        this.root.append(this.nav.header);

        this.status = $("<div class='event-history-status'></div>");
        this.root.append(this.status);
        this.body = $("<div class='event-history-body'></div>");
        this.root.append(this.body);

        this.container.append(this.root);

        this.sync();
        this.setStatus("Loading completed " + this.kindPlural() + "…", "loading");
        return this;
    },

    // Loads the panel: the newest month with completed events (the months list is fetched first), or the month the
    // viewer has already navigated to.  Runs once; later calls do nothing.
    start: function () {
        var that = this;
        if (this.started)
            return this;
        this.started = true;
        this.live = true;
        if (this.touched) {
            this.load();
            return this;
        }
        this.loadMonths(function (list) {
            if (that.touched)
                return;   // the viewer navigated while the list was loading; that navigation has loaded their month
            if (list == null) {
                that.load();   // no list: show the month in the title (the current one)
                return;
            }
            var newest = EventHistoryUI.newestMonth(list, EventHistoryUI.monthKey(that.year, that.month));
            if (newest == null) {
                that.setStatus("No completed " + that.kindPlural() + " have been recorded yet.", "empty");
                return;
            }
            that.setMonth(newest, true);
        });
        return this;
    },

    // ---- navigation ----

    monthKey: function () {
        return EventHistoryUI.monthKey(this.year, this.month);
    },

    isAtCurrentMonth: function () {
        var now = EventHistoryUI.currentMonth();
        return this.year * 12 + this.month >= now.year * 12 + now.month;
    },

    // Moves to the nearest month with events before (delta < 0) or after (delta > 0) the one in the title.  The
    // months list is fetched on the first press; while it is in flight the arrows are disabled.
    jump: function (delta) {
        var that = this;
        var months = EventHistoryUI.monthsCache[this.kind];
        if (months != null) {
            var target = EventHistoryUI.neighbourMonth(months, this.monthKey(), delta);
            if (target != null)
                this.setMonth(target);
            return;
        }
        this.loadMonths(function (list) {
            if (list == null)
                that.step(delta);   // the list is unavailable: fall back to plain calendar stepping
            else
                that.jump(delta);
        });
    },

    // One calendar month, used only when the months list could not be fetched.
    step: function (delta) {
        if (delta > 0 && this.isAtCurrentMonth())
            return;
        var index = this.year * 12 + (this.month - 1) + delta;
        this.year = Math.floor(index / 12);
        this.month = index % 12 + 1;
        this.navigated();
    },

    // auto: set by start() itself; any other call is the viewer navigating
    setMonth: function (key, auto) {
        var parts = String(key).split("-");
        var year = parseInt(parts[0], 10);
        var month = parseInt(parts[1], 10);
        if (isNaN(year) || isNaN(month) || month < 1 || month > 12)
            return;
        this.year = year;
        this.month = month;
        this.navigated(auto);
    },

    navigated: function (auto) {
        if (!auto)
            this.touched = true;
        this.sync();
        if (this.live)
            this.load();
        else
            this.start();
    },

    sync: function () {
        this.nav.setTitle(EventHistoryUI.monthLabel(this.monthKey()));
        var months = EventHistoryUI.monthsCache[this.kind];
        var prevOff, nextOff;
        if (this.monthsState == "loading") {
            prevOff = nextOff = true;
        } else if (months != null) {
            prevOff = EventHistoryUI.neighbourMonth(months, this.monthKey(), -1) == null;
            nextOff = EventHistoryUI.neighbourMonth(months, this.monthKey(), 1) == null;
        } else {
            // not fetched yet (or the fetch failed): stay usable; only the future is ruled out
            prevOff = false;
            nextOff = this.isAtCurrentMonth();
        }
        this.nav.setDisabled(prevOff, nextOff);
        if (this.dropdownOpen)
            this.renderDropdown();
    },

    // ---- month dropdown ----

    toggleDropdown: function () {
        this.nav.toggle();
    },

    openDropdown: function () {
        this.nav.open();
    },

    closeDropdown: function () {
        this.nav.close();
    },

    // Fetches the months-with-events list (once; concurrent callers share the request).  `then`, if given, is called
    // with the list, or with null if it could not be fetched.
    loadMonths: function (then) {
        var that = this;
        var kind = this.kind;
        var cached = EventHistoryUI.monthsCache[kind];
        if (cached != null) {
            this.monthsState = "ready";
            if (then)
                then(cached);
            return;
        }
        if (this.monthsState == "loading") {
            if (then)
                this.monthsWaiting.push(then);
            return;
        }
        this.monthsState = "loading";
        this.monthsError = null;
        this.monthsWaiting = then ? [then] : [];
        this.sync();

        var finish = function (list) {
            var waiting = that.monthsWaiting || [];
            that.monthsWaiting = null;
            that.sync();
            for (var i = 0; i < waiting.length; i++)
                waiting[i](list);
        };
        this.comm.getEventHistoryMonths(kind,
            function (json) {
                var list = (json && json.months) ? json.months : [];
                EventHistoryUI.monthsCache[kind] = list;
                that.monthsState = "ready";
                finish(list);
            },
            this.errorMap(function (message) {
                that.monthsState = "error";
                that.monthsError = message;
                finish(null);
            }));
    },

    renderDropdown: function () {
        if (this.monthsState == "loading" || this.monthsState == "error") {
            this.nav.renderList(this.monthsState, null, null, this.monthsError);
            return;
        }
        this.nav.renderList("ready", EventHistoryUI.monthsCache[this.kind] || [], this.monthKey(),
            "No completed " + this.kindPlural() + " have been recorded yet.");
    },

    // ---- one month ----

    load: function () {
        var that = this;
        var month = this.monthKey();
        var kind = this.kind;
        var cacheKey = kind + "|" + month;
        this.requested = month;

        var cached = EventHistoryUI.cache[cacheKey];
        if (cached != null) {
            this.renderMonth(cached, month);
            return;
        }
        this.body.empty();
        this.setStatus("Loading " + EventHistoryUI.monthLabel(month) + "…", "loading");
        this.comm.getEventHistory(month, kind,
            function (json) {
                EventHistoryUI.cache[cacheKey] = json;
                // a slow response for a month the viewer has already navigated away from must not overwrite the view
                if (that.requested == month)
                    that.renderMonth(json, month);
            },
            this.errorMap(function (message) {
                if (that.requested != month)
                    return;
                that.body.empty();
                that.setStatus("Could not load " + EventHistoryUI.monthLabel(month) + ". " + message, "error");
                that.status.append(" ").append($("<button type='button'>Retry</button>").button().click(function () {
                    if (that.requested == month)
                        that.load();
                }));
            }));
    },

    renderMonth: function (json, month) {
        var events = (json && json.events) ? json.events : [];
        var isAdmin = !!(json && json.isAdmin);
        var label = EventHistoryUI.monthLabel(month);

        this.body.empty();

        if (events.length == 0) {
            this.setStatus("No completed " + this.kindPlural() + " in " + label + ".", "empty");
            return;
        }
        this.setStatus(label + ": " + events.length + " completed "
            + (events.length == 1 ? this.kind : this.kindPlural()) + ".", "loaded");

        // the details toggle comes first, as it does on the Current Leagues / Tournaments rows
        var columns = this.kind == "league"
            ? ["", "Name", "Dates", "Format", "Players"]
            : ["", "Name", "Date", "Format", "Players", "Rounds"];
        if (isAdmin)
            columns.push("Admin");

        var table = $("<table class='event-history-table'></table>");
        var headRow = $("<tr></tr>");
        for (var c = 0; c < columns.length; c++)
            headRow.append($("<th></th>").text(columns[c]));
        table.append($("<thead></thead>").append(headRow));

        var tbody = $("<tbody></tbody>");
        for (var i = 0; i < events.length; i++)
            tbody.append(this.renderRow(events[i] || {}, isAdmin, columns.length));
        table.append(tbody);
        this.body.append(table);
    },

    renderRow: function (event, isAdmin, columnCount) {
        var row = $("<tr class='event-history-row'></tr>");
        var detailCell = $("<td class='event-history-detail-cell'></td>");
        if (event.id != null)
            detailCell.append(this.renderDetailsButton(row, String(event.id), columnCount));
        row.append(detailCell);
        row.append($("<td class='event-history-name'></td>").text(EventHistoryUI.orDash(event.name)));
        if (this.kind == "league") {
            var start = EventHistoryUI.orDash(event.startDate);
            var end = EventHistoryUI.orDash(event.endDate);
            row.append($("<td class='event-history-dates'></td>").text(start + " – " + end));
        } else {
            row.append($("<td class='event-history-dates'></td>").text(EventHistoryUI.orDash(event.startDate)));
        }
        row.append($("<td></td>").text(EventHistoryUI.orDash(event.format)));
        row.append($("<td class='event-history-number'></td>").text(EventHistoryUI.orDash(event.playerCount)));
        if (this.kind == "tournament")
            row.append($("<td class='event-history-number'></td>").text(EventHistoryUI.orDash(event.rounds)));

        if (isAdmin)
            row.append(this.renderAdminCell(event));
        return row;
    },

    // Details expand into a row directly beneath the event's row, as an EventDrawer (eventDrawer.js).  The first
    // expansion fetches and renders; later toggles only slide the row open / shut.  A failed fetch is retried on the
    // next expansion.
    renderDetailsButton: function (row, id, columnCount) {
        var that = this;
        var button = $("<button type='button' class='event-history-details' aria-expanded='false'></button>")
            .addClass(EventDrawer.BUTTON_CLASS).text(EventDrawer.CLOSED_LABEL).button();
        var drawer = new EventDrawer({
            header: row,
            button: button,
            slideClass: "event-history-detail-slide",
            paneClass: "event-history-detail",
            loadingClass: "event-history-loading",
            errorClass: "event-history-error",
            mount: function (slide) {
                var detailRow = $("<tr class='event-history-detail-row'></tr>")
                    .append($("<td></td>").attr("colspan", columnCount).append(slide));
                row.after(detailRow);
                return detailRow;
            },
            load: function (drawer, content) {
                that.loadDetails(id, drawer, content);
            }
        });
        button.click(function () { drawer.toggle(); });
        return button;
    },

    loadDetails: function (id, drawer, content) {
        var that = this;
        var success = function (xml) {
            drawer.settle(content, function () {
                return that.renderDetails(xml, content);
            });
        };
        var failure = this.errorMap(function (message, httpStatus) {
            drawer.fail(content, "Could not load the details (HTTP " + httpStatus + "). " + message);
        });

        if (this.kind == "league")
            this.comm.getLeague(id, success, failure);
        else
            this.comm.getTournament(id, success, failure);
    },

    // Returns false when the details could not be displayed.
    renderDetails: function (xml, content) {
        var that = this;
        content.empty();
        try {
            if (that.kind == "league")
                that.resultsUI.loadedLeague(xml, content,
                    {idPrefix: "eventHistory" + (++EventHistoryUI.detailRenders) + "-", membership: false});
            else
                that.resultsUI.loadedTournament(xml, content);
        } catch (e) {
            content.empty();
            content.append($("<div class='event-history-error'></div>").text("Could not display the details."));
            return false;
        }
        if (content.children().length == 0)
            content.append($("<i></i>").text("No details are available for this " + that.kind + "."));
        return true;
    },

    // A link with a url is a real anchor (the tournament report, whose text is the tournament id); one without is
    // plain text (the league code) that a click selects whole.
    renderAdminCell: function (event) {
        var cell = $("<td class='event-history-admin'></td>");
        var links = event.adminLinks;
        var any = false;
        for (var i = 0; links != null && i < links.length; i++) {
            var link = links[i];
            if (link == null)
                continue;
            var label = link.label == null ? "" : String(link.label);
            var text = link.text != null ? String(link.text) : label;
            var url = EventHistoryUI.safeUrl(link.url);
            var item = $("<span class='event-history-admin-item'></span>");
            if (url != null) {
                item.append($("<a class='event-history-admin-link' target='_blank'></a>")
                    .attr("href", url)
                    .attr("title", label ? label + ": " + url : url)
                    .text(text || url));
            } else if (text) {
                item.append($("<span class='event-history-admin-text'></span>")
                    .attr("title", (label ? label + " - " : "") + "click to select")
                    .text(text)
                    .click(function () { EventDrawer.selectText(this); }));
            } else {
                continue;
            }
            cell.append(item);
            any = true;
        }
        if (!any)
            cell.append($("<span class='event-history-admin-none'></span>").text("—"));
        return cell;
    },

    // ---- helpers ----

    kindPlural: function () {
        return this.kind == "league" ? "leagues" : "tournaments";
    },

    setStatus: function (text, state) {
        this.status.attr("class", "event-history-status event-history-" + state).text(text);
    },

    // Every status (0 and 400-599) is mapped, so no failure here reaches communication.js's global failure popup;
    // report(message, status) writes it inline instead.
    errorMap: function (report) {
        return EventDrawer.errorMap(report);
    }
});

EventHistoryUI.instances = 0;
EventHistoryUI.detailRenders = 0;               // makes each rendered league's tab ids unique on the page
EventHistoryUI.cache = {};                      // "kind|yyyy-MM" -> month response
EventHistoryUI.monthsCache = {league: null, tournament: null};   // kind -> months list, fetched at most once

EventHistoryUI.monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
    "October", "November", "December"];

EventHistoryUI.currentMonth = function () {
    var now = new Date();
    return {year: now.getUTCFullYear(), month: now.getUTCMonth() + 1};
};

EventHistoryUI.monthKey = function (year, month) {
    return year + "-" + (month < 10 ? "0" : "") + month;
};

EventHistoryUI.monthIndex = function (key) {
    var parts = String(key).split("-");
    if (parts.length < 2)
        return -1;
    var index = parseInt(parts[1], 10) - 1;
    return (isNaN(index) || index < 0 || index > 11) ? -1 : index;
};

// "2026-03" -> "March"; anything unexpected is echoed back.
EventHistoryUI.monthName = function (key) {
    var index = EventHistoryUI.monthIndex(key);
    return index < 0 ? String(key) : EventHistoryUI.monthNames[index];
};

// "2026-03" -> "March 2026"; anything unexpected is echoed back rather than turned into "undefined NaN".
EventHistoryUI.monthLabel = function (key) {
    if (key == null)
        return "";
    var index = EventHistoryUI.monthIndex(key);
    return index < 0 ? String(key) : EventHistoryUI.monthNames[index] + " " + String(key).split("-")[0];
};

EventHistoryUI.orDash = function (value) {
    return (value == null || value === "") ? "—" : String(value);
};

// Only same-origin absolute paths and http(s) URLs become hrefs; anything else (javascript:, data:, a
// protocol-relative //host) is refused so a bad server value cannot turn into script.
EventHistoryUI.safeUrl = function (url) {
    if (url == null)
        return null;
    var value = String(url);
    if (/^\/(?!\/)/.test(value) || /^https?:\/\//i.test(value))
        return value;
    return null;
};

EventHistoryUI.selectText = function (node) {
    EventDrawer.selectText(node);
};

// The nearest entry of `months` ("yyyy-MM", any order) strictly before (delta < 0) or after (delta > 0) `current`,
// or null if there is none in that direction.  "yyyy-MM" strings compare correctly as plain strings.
EventHistoryUI.neighbourMonth = function (months, current, delta) {
    var best = null;
    for (var i = 0; i < months.length; i++) {
        var key = String(months[i]);
        if (delta < 0 ? key < current : key > current) {
            if (best == null || (delta < 0 ? key > best : key < best))
                best = key;
        }
    }
    return best;
};

// The newest entry of `months` ("yyyy-MM", any order) that is not after `upTo`, or null if there is none.
EventHistoryUI.newestMonth = function (months, upTo) {
    var best = null;
    for (var i = 0; i < months.length; i++) {
        var key = String(months[i]);
        if (key <= upTo && (best == null || key > best))
            best = key;
    }
    return best;
};

// The slide / resize animation now lives in eventDrawer.js (shared with the Current Leagues / Tournaments lists and
// the calendar preview); these names are kept for any caller of the old helpers.
EventHistoryUI.duration = function () {
    return EventDrawer.duration();
};

EventHistoryUI.slide = function (wrapper, open, done) {
    EventDrawer.slide(wrapper, open, done);
};

EventHistoryUI.resize = function (wrapper, open, mutate) {
    EventDrawer.resize(wrapper, open, mutate);
};
