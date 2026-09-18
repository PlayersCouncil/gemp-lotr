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
 *       details: $("#eventDetails")                            // optional: clicking an event describes it here
 *   });
 *
 * Without "details", clicking an event calls the matching callback straight away (the admin page uses this to
 * load the event into its form).  With it, a click fills that container with the event's name, dates, description
 * and serie schedule plus a "Go to ..." button, and the callback runs when the button is pressed.
 *
 * Events the viewer has joined (event.joined) are drawn in bold.
 *   cal.show();               // current month
 *   cal.show(2026, 11);       // a specific month
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

    load: function () {
        var that = this;
        var range = this.gridRange();
        this.comm.getCalendar(range.from, range.to,
            function (json) {
                that.events = json.events || [];
                that.render();
            },
            {
                "400": function () { that.container.html("<div>Could not load the calendar.</div>"); },
                "401": function () { that.container.html("<div>You must be logged in to see the calendar.</div>"); }
            });
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

    renderDetails: function (event) {
        var that = this;
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
        panel.append(head);

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
    }
});

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
