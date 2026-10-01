/**
 * Month-grid calendar of leagues, scheduled tournaments and (for event admins) the leagues that schedules are
 * projected to create.  Data comes from GET /calendar as JSON; this only renders and routes clicks.
 *
 * Multi-day events are drawn as one horizontal bar per week row, spanning the days they cover in that week.
 * Bars within a week are packed into lanes so they never overlap; a bar that continues from the previous
 * week or into the next one is marked with the classes "continues-before" / "continues-after".
 *
 * Usage:
 *   var cal = new EventCalendarUI(comm, $("#calendar"), {
 *       onLeague: function (leagueCode, event) {...},          // open a real league
 *       onTournament: function (tournamentId, event) {...},   // open a scheduled tournament
 *       onProjected: function (scheduleId, event) {...},      // open a projected (not yet created) league
 *       details: $("#eventDetails"),                           // optional: clicking an event describes it here
 *       leagueUI: leagueResultsUI,                             // optional, with details: full preview (see below)
 *       tournamentUI: tournamentResultsUI                      // optional, with details: full preview (see below)
 *   });
 *
 * Without "details", clicking an event calls the matching callback straight away (the admin pages use this to
 * load the event into their forms).  With it, a click fills that container with the event's name, dates, description
 * and serie schedule plus a "Go to ..." button, and the callback runs when the button is pressed.  Whatever the
 * container holds before the first click (the Events tab's "Click on an event above..." placeholder) is replaced.
 *
 * With "details" AND leagueUI / tournamentUI (the player-facing Events tab), a click instead renders the same header
 * row the Current Leagues / Current Tournaments lists use, with a "Go to League" / "Go to Tournament" button in place
 * of See details, and its drawer already open underneath holding the full, working league / tournament detail (join,
 * draft, standings, deck links) from LeagueResultsUI / TournamentResultsUI.  Events those cannot show (a scheduled
 * tournament that has not started, a projected league, a failed fetch) get the facts described above inside the
 * same frame.  Without the matching UI, that kind falls back to the same frame with the facts.
 *
 * A scheduled tournament whose hall queue is open (event.queueOpen) also gets the hall's "Join Queue" button in its
 * preview (options.queueJoiner, default hall.tableJoiner; see joinQueueButton).
 *
 * Events the viewer has joined (event.joined) are drawn in bold.
 *   cal.show();               // current month
 *   cal.show(2026, 11);       // a specific month
 *   cal.refresh();            // re-fetch the month shown, keeping the selected event and its preview (see refresh)
 *
 * All date arithmetic is done in UTC, because the server reports dates in UTC.
 */
var EventCalendarUI = Class.extend({
    comm: null,
    container: null,
    options: null,
    year: 0,
    month: 0,   // 1-12
    events: [],
    selected: null,   // the event shown in the details panel, if any
    preview: null,    // preview mode: the {element, header, button, drawer} row in the details panel
    scrollKeeper: null,   // preview mode: keeps the page from jumping while the preview is swapped / animates

    init: function (comm, container, options) {
        this.comm = comm;
        this.container = container;
        this.options = options || {};
        var now = new Date();
        this.year = now.getUTCFullYear();
        this.month = now.getUTCMonth() + 1;
    },

    show: function (year, month) {
        if (year !== undefined && month !== undefined) {
            this.year = year;
            this.month = month;
        }
        this.load();
    },

    previousMonth: function () {
        this.month--;
        if (this.month < 1) {
            this.month = 12;
            this.year--;
        }
        this.load();
    },

    nextMonth: function () {
        this.month++;
        if (this.month > 12) {
            this.month = 1;
            this.year++;
        }
        this.load();
    },

    // ---- data ----

    // then: runs after a successful load has been rendered
    load: function (then) {
        var that = this;
        var range = this.gridRange();
        this.comm.getCalendar(range.from, range.to,
            function (json) {
                that.events = json.events || [];
                that.render();
                if (then)
                    then();
            },
            {
                "400": function () { that.container.html("<div>Could not load the calendar.</div>"); },
                "401": function () { that.container.html("<div>You must be logged in to see the calendar.</div>"); }
            });
    },

    // A data refresh (the Events tab calls it when the viewer comes back): the month shown is fetched and redrawn, the
    // selected event stays selected, and its details / preview are brought up to date in place.  A preview whose event
    // changed in the calendar feed (joined, started, queue opened...) is rebuilt; otherwise only its drawer re-fetches.
    refresh: function () {
        var that = this;
        this.load(function () {
            that.refreshSelected();
        });
    },

    refreshSelected: function () {
        var old = this.selected;
        if (old == null || !this.options.details)
            return;
        var fresh = null;
        for (var i = 0; i < this.events.length; i++) {
            var event = this.events[i];
            if (event.kind == old.kind && event.id == old.id && event.scheduleId == old.scheduleId) {
                fresh = event;
                break;
            }
        }
        if (fresh != null && JSON.stringify(fresh) !== JSON.stringify(old)) {
            this.selected = fresh;
            this.renderDetails(fresh);
            return;
        }
        if (fresh != null)
            this.selected = fresh;
        if (this.previewMode() && this.preview != null && this.preview.drawer.isOpen())
            this.preview.drawer.refresh(true);
    },

    // The grid always shows whole weeks: the first row starts on the Monday on or before the 1st of the month and
    // the last row ends on the Sunday on or after the last day of the month.  The fetched range covers those
    // leading/trailing days too.
    gridRange: function () {
        var first = new Date(Date.UTC(this.year, this.month - 1, 1));
        var lead = (first.getUTCDay() + 6) % 7;                         // days back from the 1st to reach a Monday
        var start = EventCalendarUI.addDays(first, -lead);
        var last = new Date(Date.UTC(this.year, this.month, 0));        // day 0 of next month = last day of this one
        var trail = (7 - last.getUTCDay()) % 7;                         // days forward from the last day to reach a Sunday
        var end = EventCalendarUI.addDays(last, trail);
        return {from: EventCalendarUI.isoDate(start), to: EventCalendarUI.isoDate(end), start: start, end: end};
    },

    // ---- rendering ----

    render: function () {
        var that = this;
        var range = this.gridRange();
        this.container.empty();

        var header = $("<div class='calendar-header'></div>");
        var prev = $("<button>&lt;</button>").button().click(function () { that.previousMonth(); });
        var next = $("<button>&gt;</button>").button().click(function () { that.nextMonth(); });
        var today = $("<button>Today</button>").button().click(function () {
            var now = new Date();
            that.show(now.getUTCFullYear(), now.getUTCMonth() + 1);
        });
        header.append(prev);
        header.append("<span class='calendar-title'>" + EventCalendarUI.monthNames[this.month - 1] + " " + this.year + "</span>");
        header.append(next);
        header.append(today);
        header.append("<span class='calendar-legend'>"
            + "<span class='calendar-chip kind-league'>League</span> "
            + "<span class='calendar-chip kind-tournament'>Tournament</span> "
            + "<span class='calendar-chip kind-projected'>Scheduled (not yet created)</span>"
            + "</span>");
        this.container.append(header);

        var grid = $("<div class='calendar-grid'></div>");
        var weekdays = $("<div class='calendar-weekdays'></div>");
        var dayNames = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
        for (var d = 0; d < 7; d++)
            weekdays.append($("<div class='calendar-weekday'></div>").text(dayNames[d]));
        grid.append(weekdays);

        var todayIso = EventCalendarUI.isoDate(new Date());
        var weekStart = range.start;
        while (weekStart <= range.end) {
            grid.append(this.renderWeek(weekStart, todayIso));
            weekStart = EventCalendarUI.addDays(weekStart, 7);
        }
        this.container.append(grid);
    },

    // One week row: a 7-column background layer with the day cells, the day numbers in grid row 1 and the
    // event bars in grid rows 2..n (one row per lane).
    renderWeek: function (weekStart, todayIso) {
        var week = $("<div class='calendar-week'></div>");
        var background = $("<div class='calendar-week-bg'></div>");
        var weekDays = [];
        for (var i = 0; i < 7; i++) {
            var day = EventCalendarUI.addDays(weekStart, i);
            var iso = EventCalendarUI.isoDate(day);
            weekDays.push(iso);
            var inMonth = day.getUTCMonth() + 1 == this.month;
            var stateClasses = (inMonth ? "" : " other-month") + (iso == todayIso ? " today" : "");
            background.append("<div class='calendar-day" + stateClasses + "'></div>");
            var number = $("<div class='calendar-day-number" + stateClasses + "'></div>").text(day.getUTCDate());
            number.css({"grid-column": String(i + 1), "grid-row": "1"});
            week.append(number);
        }
        week.prepend(background);

        var segments = this.weekSegments(weekDays[0], weekDays[6]);
        var lanes = this.assignLanes(segments);
        week.css("grid-template-rows", "auto" + (lanes > 0 ? " repeat(" + lanes + ", 1.25em)" : ""));
        for (var s = 0; s < segments.length; s++)
            week.append(this.renderBar(segments[s], weekDays));
        return week;
    },

    // Clips every event to [weekFrom, weekTo] and returns one segment per event that touches the week.
    // Columns are 1-based (1 = Monday), as CSS grid lines are.
    weekSegments: function (weekFrom, weekTo) {
        var segments = [];
        var weekStart = EventCalendarUI.parseIsoDate(weekFrom);
        for (var i = 0; i < this.events.length; i++) {
            var event = this.events[i];
            if (!event.start || !event.end || event.end < weekFrom || event.start > weekTo)
                continue;
            var from = event.start < weekFrom ? weekFrom : event.start;
            var to = event.end > weekTo ? weekTo : event.end;
            var col = EventCalendarUI.dayOffset(weekStart, from) + 1;
            var span = EventCalendarUI.dayOffset(weekStart, to) + 2 - col;
            segments.push({
                event: event,
                from: from,
                to: to,
                col: col,
                span: span,
                continuesBefore: event.start < weekFrom,
                continuesAfter: event.end > weekTo,
                lane: 0
            });
        }
        return segments;
    },

    // Greedy lane packing: longest-first among segments with the same start, each into the first lane with all
    // of its columns free.  Sets segment.lane (0-based) and returns the number of lanes used.
    assignLanes: function (segments) {
        segments.sort(function (a, b) {
            if (a.col != b.col)
                return a.col - b.col;
            if (a.span != b.span)
                return b.span - a.span;
            return EventCalendarUI.kindOrder(a.event) - EventCalendarUI.kindOrder(b.event);
        });
        var lanes = [];   // lanes[l][c] == true when column c (0-based) of lane l is taken
        for (var i = 0; i < segments.length; i++) {
            var seg = segments[i];
            var lane = 0;
            while (lane < lanes.length && !EventCalendarUI.laneFree(lanes[lane], seg.col - 1, seg.span))
                lane++;
            if (lane == lanes.length)
                lanes.push([false, false, false, false, false, false, false]);
            for (var c = seg.col - 1; c < seg.col - 1 + seg.span; c++)
                lanes[lane][c] = true;
            seg.lane = lane;
        }
        return lanes.length;
    },

    renderBar: function (segment, weekDays) {
        var that = this;
        var event = segment.event;
        var bar = $("<div class='calendar-bar kind-" + event.kind + "'></div>");
        if (segment.continuesBefore)
            bar.addClass("continues-before");
        if (segment.continuesAfter)
            bar.addClass("continues-after");
        if (event.kind == "projected" && event.error)
            bar.addClass("has-error");
        if (event.joined)
            bar.addClass("joined");
        if (this.selected != null && this.selected.kind == event.kind && this.selected.id == event.id)
            bar.addClass("selected");
        bar.css({"grid-column": segment.col + " / span " + segment.span, "grid-row": String(segment.lane + 2)});

        // The bar is split into one part per serie it covers this week; every part after the first starts with a
        // divider and says which serie begins there.
        var parts = this.barParts(segment);
        for (var p = 0; p < parts.length; p++) {
            var part = $("<span class='calendar-bar-part'></span>").text(parts[p].text);
            part.css("flex-basis", (100 * parts[p].days / segment.span) + "%");
            if (p > 0)
                part.addClass("serie-start").attr("title", parts[p].title);
            bar.append(part);
        }

        bar.attr("title", this.tooltip(event));
        bar.data("calendarEvent", event);
        bar.click(function () { that.eventClicked(event); });
        return bar;
    },

    // [{days, text, title}] left to right across the segment.  For a league with several series the divisions fall
    // where a serie starts; other events are a single part.
    barParts: function (segment) {
        var event = segment.event;
        var label = event.name || "";
        if (event.kind == "tournament" && event.startTime)
            label = event.startTime.substring(11, 16) + " " + label;
        if (segment.continuesBefore)
            label = "\u25C2 " + label;

        var series = (event.kind == "league" && event.series) ? event.series : [];
        if (series.length < 2)
            return [{days: segment.span, text: label, title: label}];

        var segmentStart = EventCalendarUI.parseIsoDate(segment.from);
        var cuts = [];   // {day, index} for every serie that starts strictly inside this segment
        var currentSerie = 0;
        for (var s = 1; s < series.length; s++) {
            var serieStart = series[s].start;
            if (!serieStart)
                continue;
            if (serieStart <= segment.from)
                currentSerie = s;
            else if (serieStart <= segment.to)
                cuts.push({day: EventCalendarUI.dayOffset(segmentStart, serieStart), index: s});
        }
        // A continuation row names the serie in progress, since the divider that introduced it was on an earlier row
        if (segment.continuesBefore && currentSerie > 0)
            label += " \u00B7 " + EventCalendarUI.serieLabel(series, currentSerie);

        var parts = [];
        var from = 0;
        for (var c = 0; c <= cuts.length; c++) {
            var to = c < cuts.length ? cuts[c].day : segment.span;
            if (to > from) {
                var text = c == 0 ? label : EventCalendarUI.serieLabel(series, cuts[c - 1].index);
                var title = c == 0 ? label : EventCalendarUI.serieLabel(series, cuts[c - 1].index) + " starts " + series[cuts[c - 1].index].start;
                parts.push({days: to - from, text: text, title: title});
            }
            from = to;
        }
        return parts;
    },

    tooltip: function (event) {
        var lines = [];
        if (event.kind == "tournament" && event.startTime)
            lines.push(event.name + " (" + event.start + " " + event.startTime.substring(11, 16) + " UTC)");
        else if (event.start == event.end)
            lines.push(event.name + " (" + event.start + ")");
        else
            lines.push(event.name + " (" + event.start + " to " + event.end + ")");

        if (event.kind == "projected" && event.error)
            lines.push("Cannot be created: " + event.error);
        else if (event.kind == "projected")
            lines.push("Will be created on " + event.createdOn + " by schedule '" + event.scheduleName + "'");
        if (event.joined)
            lines.push("You are signed up");

        if (event.kind == "league" && event.series && event.series.length > 1) {
            for (var s = 1; s < event.series.length; s++) {
                var serie = event.series[s];
                lines.push((serie.name || ("Serie " + (s + 1))) + " starts " + serie.start
                    + (serie.format ? " (" + serie.format + ")" : ""));
            }
        }
        return lines.join("\n");
    },

    eventClicked: function (event) {
        if (this.options.details) {
            this.selected = event;
            this.container.find(".calendar-bar.selected").removeClass("selected");
            var that = this;
            this.container.find(".calendar-bar").each(function () {
                // re-mark from the event objects rather than re-rendering the whole grid
                var bar = $(this);
                if (bar.data("calendarEvent") === event)
                    bar.addClass("selected");
            });
            this.renderDetails(event);
            return;
        }
        this.openEvent(event);
    },

    openEvent: function (event) {
        if (event.kind == "league" && this.options.onLeague)
            this.options.onLeague(event.id, event);
        else if (event.kind == "tournament" && this.options.onTournament)
            this.options.onTournament(event.id, event);
        else if (event.kind == "projected" && this.options.onProjected)
            this.options.onProjected(event.scheduleId, event);
    },

    // ---- details panel (player-facing calendar) ----

    previewMode: function () {
        return !!(this.options.leagueUI || this.options.tournamentUI);
    },

    renderDetails: function (event) {
        var that = this;
        if (this.previewMode()) {
            this.renderPreview(event);
            return;
        }
        var panel = this.options.details;
        panel.empty().addClass("calendar-details kind-" + event.kind);

        var head = $("<div class='calendar-details-head'></div>");
        head.append($("<span class='calendar-details-name'></span>").text(event.name || ""));
        // the "Go to" button sits right by the name so it is in the same place for every event
        if (event.kind != "projected") {
            head.append($("<button type='button' class='calendar-details-go'></button>")
                .text(event.kind == "tournament" ? "Go to tournament" : "Go to league")
                .button()
                .click(function () { that.openEvent(event); }));
        }
        head.append($("<span class='calendar-chip kind-" + event.kind + "'></span>").text(EventCalendarUI.kindLabel(event)));
        if (event.joined)
            head.append("<span class='calendar-details-joined'>You are signed up</span>");
        var joinButton = this.joinQueueButton(event);
        if (joinButton)
            head.append(joinButton);
        panel.append(head);
        this.renderFacts(event, panel);
    },

    // The event's facts, description and serie schedule, appended to target.
    renderFacts: function (event, target) {
        var panel = target;
        var facts = $("<div class='calendar-details-facts'></div>");
        if (event.kind == "tournament") {
            facts.append(EventCalendarUI.fact("Starts", event.start + " " + (event.startTime ? event.startTime.substring(11, 16) + " UTC" : "")));
            if (event.format)
                facts.append(EventCalendarUI.fact("Format", event.format));
        } else {
            facts.append(EventCalendarUI.fact("Runs", event.start == event.end ? event.start : event.start + " to " + event.end));
        }
        if (event.cost != null && typeof formatPrice == "function")
            facts.append(EventCalendarUI.fact("Cost", formatPrice(event.cost), true));
        if (event.inviteOnly)
            facts.append(EventCalendarUI.fact("Entry", "Invite only"));
        if (event.kind == "projected")
            facts.append(EventCalendarUI.fact("Status", event.error ? "Cannot be created: " + event.error
                : "Not created yet; will appear on " + event.createdOn + " (schedule '" + event.scheduleName + "')"));
        panel.append(facts);

        // description arrives from the server already rendered from markdown
        if (event.description)
            panel.append($("<div class='calendar-details-desc'></div>").html(event.description));

        if (event.series && event.series.length > 0) {
            var table = $("<table class='calendar-details-series'><thead><tr><th></th><th>Serie</th><th>Format</th><th>Dates</th></tr></thead></table>");
            var body = $("<tbody></tbody>");
            for (var s = 0; s < event.series.length; s++) {
                var serie = event.series[s];
                var row = $("<tr></tr>");
                row.append($("<td></td>").text(s + 1));
                row.append($("<td></td>").text(serie.name && serie.name != "Serie " + (s + 1) ? serie.name : ""));
                row.append($("<td></td>").text(serie.format || ""));
                row.append($("<td></td>").text(serie.start + " to " + serie.end));
                body.append(row);
            }
            table.append(body);
            panel.append(table);
        }
    },

    // ==== tabs-account: "Join Queue" for a scheduled tournament whose hall queue is taking sign-ups ====
    // The server registers a scheduled tournament's hall queue under the tournament's own id, and the calendar feed
    // says whether it is open (queueOpen) with the queue's type and start text.  The button is the hall's own
    // (JoinTable.generateJoinQueueButton, fed an element shaped like the hall poll's <queue>), so joining works
    // exactly as in the Waiting Tables list.  No button where there is no hall (admin pages) or no open queue.
    joinQueueButton: function (event) {
        if (event.kind != "tournament" || event.queueOpen !== true || event.joined)
            return null;
        var joiner = this.options.queueJoiner || (window.hall ? window.hall.tableJoiner : null);
        if (!joiner || typeof joiner.generateJoinQueueButton != "function")
            return null;
        var queue = $.parseXML("<queue/>").documentElement;
        queue.setAttribute("id", String(event.id));
        queue.setAttribute("queue", event.name == null ? "" : String(event.name));
        queue.setAttribute("type", event.queueType == null ? "" : String(event.queueType));
        queue.setAttribute("start", event.queueStart == null ? "" : String(event.queueStart));
        queue.setAttribute("joinable", "true");
        queue.setAttribute("signedUp", "false");
        var button = joiner.generateJoinQueueButton(queue);
        if (button)
            $(button).addClass("calendar-join-queue");
        return button;
    },
    // ==== end tabs-account ====

    // Header row (as in the Current Leagues / Tournaments lists) with a "Go to ..." button and the drawer open below.
    renderPreview: function (event) {
        var that = this;
        var panel = this.options.details;
        if (this.scrollKeeper == null)
            this.scrollKeeper = new EventCalendarUI.ScrollKeeper(panel);
        // Pin the viewer's scroll position across the swap: emptying the panel and the new drawer's loading line and
        // eased growth would otherwise shrink the page for a moment and make the browser clamp the scroll upward.
        this.scrollKeeper.pin();
        panel.empty().removeClass("calendar-details kind-league kind-tournament kind-projected").addClass("calendar-preview");

        var action = false;
        if (event.kind == "league" && this.options.onLeague)
            action = {label: "Go to League", click: function () { that.openEvent(event); }};
        else if (event.kind == "tournament" && this.options.onTournament)
            action = {label: "Go to Tournament", click: function () { that.openEvent(event); }};

        var extras = [];
        if (event.kind != "league" && event.kind != "tournament")
            extras.push($("<span class='calendar-chip kind-" + event.kind + "'></span>").text(EventCalendarUI.kindLabel(event)));
        if (event.joined)
            extras.push($("<span class='calendar-details-joined'></span>").text("You are signed up"));
        var joinButton = this.joinQueueButton(event);
        if (joinButton)
            extras.push(joinButton);

        var fallback = function (content) {
            that.renderFacts(event, content);
        };
        var row;
        if (event.kind == "league" && this.options.leagueUI) {
            row = this.options.leagueUI.createLeagueRow(
                {code: event.id, name: event.name, start: event.start, end: event.end},
                {action: action, open: true, fallback: fallback, extras: extras});
        } else if (event.kind == "tournament" && this.options.tournamentUI) {
            // a scheduled tournament that has not started yet has no tournament to fetch
            row = this.options.tournamentUI.createTournamentRow(
                {id: event.id, name: event.name, start: EventCalendarUI.previewDate(event)},
                {action: action, open: true, fallback: fallback, extras: extras, fetch: event.started === true});
        } else {
            row = EventDrawer.row({
                kind: event.kind,
                title: event.name,
                date: EventCalendarUI.previewDate(event),
                dateTitle: "Server time (UTC)",
                extras: extras,
                action: action,
                open: true,
                load: function (drawer, content) {
                    drawer.settle(content, function (c) {
                        c.empty();
                        fallback(c);
                    });
                }
            });
        }
        panel.append(row.element);
        this.preview = row;
        this.scrollKeeper.fit();
    }
});

/**
 * Keeps a scroll container's position stable while the content of `panel` (which sits inside it) is replaced and
 * animates.  It inserts an invisible spacer right after the panel whose height is always just enough that the
 * container can still scroll to the position being held, so the page never gets shorter than the viewer's current
 * scroll offset and the browser never clamps it; as real content grows back, the spacer shrinks to nothing.
 *
 * pin() holds the current offset exactly until the viewer next scrolls, wheels, touches or presses a key (so the
 * pre-click and post-click offsets are identical, whatever the old and new previews' heights).  Afterwards the spacer
 * only protects whatever offset the viewer has scrolled to.  The panel also gets overflow-anchor: none (in CSS) so
 * the browser's own scroll anchoring cannot pick a node inside the panel being replaced.
 */
EventCalendarUI.ScrollKeeper = function (panel) {
    var that = this;
    this.panel = panel;
    this.scroller = EventCalendarUI.scrollParent(panel[0]);
    this.isDocument = (this.scroller === document.scrollingElement || this.scroller === document.documentElement
        || this.scroller === document.body);
    this.spacer = $("<div class='calendar-scroll-spacer' aria-hidden='true'></div>").css({height: "0px"});
    panel.after(this.spacer);
    this.pinnedTop = null;
    this.fitting = false;

    var release = function () { that.pinnedTop = null; };
    var target = this.isDocument ? $(window) : $(this.scroller);
    target.on("wheel touchstart mousedown keydown", release);
    if (this.isDocument)
        $(document).on("keydown", release);
    // After the viewer scrolls, trim the spacer back to what their new position needs.
    var scheduled = false;
    target.on("scroll", function () {
        if (that.fitting || scheduled)
            return;
        scheduled = true;
        var run = function () { scheduled = false; that.fit(); };
        if (window.requestAnimationFrame) window.requestAnimationFrame(run); else setTimeout(run, 16);
    });
    // The drawer eases its height over several frames; follow every change.
    if (typeof ResizeObserver != "undefined") {
        this.observer = new ResizeObserver(function () { that.fit(); });
        this.observer.observe(panel[0]);
    }
};

EventCalendarUI.ScrollKeeper.prototype.pin = function () {
    this.pinnedTop = this.scroller.scrollTop;
};

// Sizes the spacer for the held offset (the pinned one, else the current one) and restores that offset.  The content
// height without the spacer is measured with the spacer temporarily set taller than the viewport (a zero-height spacer
// would not do: scrollHeight never reports less than the viewport, which hides how short the content really is).
// Everything happens in one task, so the probe height is never painted.
EventCalendarUI.ScrollKeeper.prototype.fit = function () {
    var scroller = this.scroller;
    var top = (this.pinnedTop != null) ? this.pinnedTop : scroller.scrollTop;
    var client = scroller.clientHeight;
    this.fitting = true;
    var probe = Math.ceil(top + client) + 1;
    this.spacer[0].style.height = probe + "px";
    var content = scroller.scrollHeight - probe;   // everything scrollable except the spacer
    var need = Math.max(0, Math.ceil(top + client - content));
    this.spacer[0].style.height = need + "px";
    if (scroller.scrollTop !== top)
        scroller.scrollTop = top;
    this.fitting = false;
};

// The nearest ancestor that scrolls vertically, or the document's scrolling element.
EventCalendarUI.scrollParent = function (element) {
    var node = element ? element.parentElement : null;
    while (node && node !== document.body && node !== document.documentElement) {
        var overflowY = window.getComputedStyle(node).overflowY;
        if (overflowY == "auto" || overflowY == "scroll" || overflowY == "overlay")
            return node;
        node = node.parentElement;
    }
    return document.scrollingElement || document.documentElement;
};

// The date shown on the right of a preview header: "start to end" for a league, date and time for a tournament.
EventCalendarUI.previewDate = function (event) {
    if (event.kind == "tournament")
        return (event.start || "") + (event.startTime ? " " + String(event.startTime).substring(11, 16) : "");
    if (!event.end || event.end == event.start)
        return event.start || "";
    return event.start + " to " + event.end;
};

EventCalendarUI.kindLabel = function (event) {
    if (event.kind == "tournament")
        return "Tournament";
    if (event.kind == "projected")
        return "Scheduled";
    return "League";
};

EventCalendarUI.fact = function (label, value, isHtml) {
    var fact = $("<div class='calendar-details-fact'></div>");
    fact.append($("<span class='calendar-details-label'></span>").text(label));
    var v = $("<span></span>");
    if (isHtml) v.html(value); else v.text(value);
    return fact.append(v);
};

// The serie's own name ("Serie 2" unless an admin named it); the format is in the tooltip and the details panel.
EventCalendarUI.serieLabel = function (series, index) {
    var serie = series[index];
    return serie.name || ("Serie " + (index + 1));
};

EventCalendarUI.monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September",
    "October", "November", "December"];

EventCalendarUI.isoDate = function (date) {
    return date.getUTCFullYear() + "-" + String(date.getUTCMonth() + 1).padStart(2, "0") + "-" + String(date.getUTCDate()).padStart(2, "0");
};

// "yyyy-MM-dd" -> Date at 00:00 UTC of that day
EventCalendarUI.parseIsoDate = function (iso) {
    var parts = iso.split("-");
    return new Date(Date.UTC(parseInt(parts[0], 10), parseInt(parts[1], 10) - 1, parseInt(parts[2], 10)));
};

// Adds whole days in UTC (Date.UTC normalises overflowing day numbers, so no millisecond arithmetic is needed)
EventCalendarUI.addDays = function (date, days) {
    return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + days));
};

// Number of days from a UTC midnight Date to a "yyyy-MM-dd" day (both are UTC midnights, so the division is exact)
EventCalendarUI.dayOffset = function (fromDate, toIso) {
    return Math.round((EventCalendarUI.parseIsoDate(toIso).getTime() - fromDate.getTime()) / 86400000);
};

EventCalendarUI.laneFree = function (lane, firstColumn, span) {
    for (var c = firstColumn; c < firstColumn + span; c++)
        if (lane[c])
            return false;
    return true;
};

// Tie-breaker for lane packing so bars of the same shape get a stable order: leagues, then projected, then tournaments
EventCalendarUI.kindOrder = function (event) {
    return event.kind == "league" ? 0 : (event.kind == "projected" ? 1 : 2);
};
