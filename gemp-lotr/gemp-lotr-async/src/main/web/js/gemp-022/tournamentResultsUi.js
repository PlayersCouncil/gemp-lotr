/**
 * Current (or recent) tournaments and one tournament's detail.
 *
 *   new TournamentResultsUI(url)                    // legacy: list in $("#tournamentResults"), details expand under
 *                                                   // each entry (Server Info's tournament page)
 *   new TournamentResultsUI(url, {list: $container})
 *       // list mode (the Events tab): every live tournament is a header row ([+ Details] name ...... start date)
 *       // with its own drawer underneath (eventDrawer.js).  Options:
 *       //   list:     container for the rows (required for list mode)
 *       //   autoLoad: false = do not fetch the list from the constructor (default true)
 *
 * createTournamentRow(tournament, options) builds one row + drawer; the calendar preview (calendarUi.js) uses it too.
 *
 * refreshList() (list mode) re-fetches the list and updates it in place, as LeagueResultsUI.refreshList does.
 */
var TournamentResultsUI = Class.extend({
    communication:null,
    formatDialog:null,
    options:null,
    list:null,           // list mode: the rows' container; null = legacy #tournamentResults
    rows:null,           // list mode: tournament id -> {element, header, button, drawer}
    listLoaded:false,
    listPending:null,

    init:function (url, options) {
        this.communication = new GempLotrCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });

        this.formatDialog = $("<div></div>")
            .dialog({
                autoOpen:false,
                closeOnEscape:true,
                resizable:false,
                modal:true,
                title:"Format description"
            });

        this.options = options || {};
        this.list = this.options.list || null;
        this.rows = {};

        if (this.options.autoLoad !== false)
            this.loadLiveTournaments();
    },

    loadLiveTournaments:function (expandTournamentId) {
        var that = this;
        if (this.list != null) {
            this.loadList(expandTournamentId === undefined ? null : function () {
                that.revealTournament(expandTournamentId);
            });
            return;
        }
        this.communication.getLiveTournaments(
            function (xml) {
                that.loadedTournaments(xml, expandTournamentId);
            });
    },

    loadHistoryTournaments:function () {
        var that = this;
        this.communication.getHistoryTournaments(
            function (xml) {
                that.loadedTournaments(xml);
            });
    },

    // options: {rounds: true} starts with a "Rounds: n" line (the list-mode rows no longer carry it)
    loadedTournament:function (xml, targetDiv, options) {
        var that = this;
        options = options || {};
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'tournament') {
            var tournament = root;

            var tournamentId = tournament.getAttribute("id");
            var tournamentName = tournament.getAttribute("name");
            var tournamentFormat = tournament.getAttribute("format");
            var tournamentCollection = tournament.getAttribute("collection");
            var tournamentRound = tournament.getAttribute("round");
            var tournamentStage = tournament.getAttribute("stage");

            if (options.rounds)
                targetDiv.append($("<div class='tournamentRound'></div>").append("<b>Rounds:</b> ")
                    .append(document.createTextNode(tournamentRound == null ? "" : tournamentRound)));

            targetDiv.append("<div class='tournamentFormat'><b>Format:</b> " + tournamentFormat + "</div>");
            targetDiv.append("<div class='tournamentCollection'><b>Collection:</b> " + tournamentCollection + "</div>");
            if (tournamentStage == "Playing games")
                targetDiv.append("<div class='tournamentRound'><b>Round:</b> " + tournamentRound + "</div>");

            var standings = tournament.getElementsByTagName("tournamentStanding");
            if (standings.length > 0)
                targetDiv.append(this.createStandingsTable(standings, tournamentId, tournamentStage));

            targetDiv.show();
        }
    },

    // expandTournamentId: optional; when given, that tournament's details are opened right away (calendar snap-to)
    loadedTournaments:function (xml, expandTournamentId) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
        if (this.list != null) {
            this.renderTournamentRows(root);
            if (expandTournamentId !== undefined)
                this.revealTournament(expandTournamentId);
            return;
        }
        if (root.tagName == 'tournaments') {
            $("#tournamentResults").html("");

            var tournaments = root.getElementsByTagName("tournament");
            for (var i = 0; i < tournaments.length; i++) {
                var tournament = tournaments[i];
                var tournamentId = tournament.getAttribute("id");
                var tournamentName = tournament.getAttribute("name");
                var tournamentFormat = tournament.getAttribute("format");
                var tournamentCollection = tournament.getAttribute("collection");
                var tournamentRound = tournament.getAttribute("round");
                var tournamentStage = tournament.getAttribute("stage");

                $("#tournamentResults").append("<div class='tournamentName'>" + tournamentName + "</div>");
                
                if(hall.userInfo.type.includes("l") || hall.userInfo.type.includes("a")) {
                    $("#tournamentResults").append("<span class='league-id'><a target='_blank' href='/gemp-lotr-server/tournament/" + tournamentId + "/report/html'>" + tournamentId + "</a></span>");
                }
            
                $("#tournamentResults").append("<div class='tournamentRound'><b>Rounds:</b> " + tournamentRound + "</div>");

                var detailsBut = $("<button>See details</button>").button();
                $("#tournamentResults").append(detailsBut);

                var extraInfoDiv = $("<div class='tournamentExtraInfo' style='display:none;'></div>");
                $("#tournamentResults").append(extraInfoDiv);

                detailsBut.click(
                    (function (id, extraInfoTarget) {
                        return function () {
                            var btn = $(this);
                            that.communication.getTournament(id,
                                function (xml) {
                                    that.loadedTournament(xml, extraInfoTarget);
                                    btn.hide();
                                });
                        };
                    })(tournamentId, extraInfoDiv));

                if (expandTournamentId !== undefined && expandTournamentId == tournamentId) {
                    detailsBut.click();
                    detailsBut[0].scrollIntoView();
                }
            }
            if (tournaments.length == 0)
                $("#tournamentResults").append("<i>There is no running tournaments at the moment</i>");
        }
    },

    // ---- list mode (the Events tab's Current Tournaments): one header row + drawer per tournament ----

    // Fetches and renders the list.  Concurrent callers share one request; each `then` runs after the render (or,
    // when the request fails, after the error is shown).
    loadList:function (then) {
        var that = this;
        if (this.listPending != null) {
            if (then)
                this.listPending.push(then);
            return;
        }
        this.listPending = then ? [then] : [];
        this.communication.getLiveTournaments(
            function (xml) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.renderTournamentRows(xml.documentElement);
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            },
            EventDrawer.errorMap(function (message) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.list.empty().append($("<div class='event-drawer-error'></div>")
                    .text("Could not load the current tournaments. " + message));
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            }));
    },

    // Re-fetches the list and updates it in place: open drawers are re-rendered where they are, shut ones re-fetch
    // when next opened.  A failed refresh keeps what is shown.
    refreshList:function () {
        var that = this;
        if (this.list == null)
            return;
        if (!this.listLoaded) {
            this.loadList();
            return;
        }
        if (this.listPending != null)
            return;
        this.listPending = [];
        this.communication.getLiveTournaments(
            function (xml) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.updateTournamentRows(xml.documentElement);
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            },
            EventDrawer.errorMap(function () {
                var waiting = that.listPending || [];
                that.listPending = null;
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            }));
    },

    updateTournamentRows:function (root) {
        var that = this;
        if (root == null || root.tagName != 'tournaments')
            return;
        var tournaments = root.getElementsByTagName("tournament");
        var data = [];
        for (var i = 0; i < tournaments.length; i++) {
            data.push({
                id: tournaments[i].getAttribute("id"),
                name: tournaments[i].getAttribute("name"),
                start: tournaments[i].getAttribute("start")
            });
        }
        this.rows = EventDrawer.updateRows(this.list, this.rows, data, {
            key: function (tournament) { return tournament.id; },
            create: function (tournament) { return that.createTournamentRow(tournament); },
            title: function (tournament) { return tournament.name; },
            date: function (tournament) { return tournament.start; },
            emptyText: "There are no running tournaments at the moment."
        });
        this.listLoaded = true;
    },

    renderTournamentRows:function (root) {
        this.list.empty();
        this.rows = {};
        if (root == null || root.tagName != 'tournaments')
            return;
        var tournaments = root.getElementsByTagName("tournament");
        for (var i = 0; i < tournaments.length; i++) {
            var tournament = tournaments[i];
            var data = {
                id: tournament.getAttribute("id"),
                name: tournament.getAttribute("name"),
                start: tournament.getAttribute("start")
            };
            var row = this.createTournamentRow(data);
            this.rows[data.id] = row;
            this.list.append(row.element);
        }
        if (tournaments.length == 0)
            this.list.append($("<i></i>").text("There are no running tournaments at the moment."));
        this.listLoaded = true;
    },

    // One tournament's header row and drawer.
    //   tournament: {id, name, start}      (start: the date shown on the right)
    //   options: {
    //       action:   undefined = the + Details / - Details toggle; {label, click} = a fixed button (the
    //                 calendar's "Go to Tournament")
    //       open:     true = born open
    //       fetch:    false = do not ask the server (a scheduled tournament that has not started); the fallback is
    //                 shown straight away
    //       fallback: function (content, message, status) rendering something instead of an error when the
    //                 tournament cannot be fetched
    //       extras:   more jQuery elements for the header
    //   }
    // Returns {element, header, button, drawer}.
    createTournamentRow:function (tournament, options) {
        var that = this;
        options = options || {};
        var id = tournament.id == null ? "" : String(tournament.id);
        var extras = [];
        // admins and league admins get the report link, the id as its text (as before)
        var type = EventDrawer.userType();
        if (id !== "" && (type.includes("l") || type.includes("a"))) {
            extras.push($("<span class='league-id event-row-code'></span>").append(
                $("<a target='_blank'></a>")
                    .attr("href", "/gemp-lotr-server/tournament/" + encodeURIComponent(id) + "/report/html")
                    .attr("title", "Tournament report")
                    .text(id)));
        }
        if (options.extras)
            extras = extras.concat(options.extras);
        return EventDrawer.row({
            kind: "tournament",
            title: tournament.name,
            date: tournament.start,
            dateTitle: "Server time (UTC)",
            extras: extras,
            action: options.action,
            open: options.open,
            load: function (drawer, content) {
                if (options.fetch === false && options.fallback) {
                    drawer.settle(content, function (c) {
                        c.empty();
                        options.fallback(c, null, null);
                    });
                    return;
                }
                that.loadTournamentDrawer(id, drawer, content, options.fallback);
            }
        });
    },

    loadTournamentDrawer:function (id, drawer, content, fallback) {
        var that = this;
        this.communication.getTournament(id,
            function (xml) {
                drawer.settle(content, function (c) {
                    c.empty();
                    try {
                        that.loadedTournament(xml, c, {rounds: true});
                    } catch (e) {
                        c.empty().append($("<div class='event-drawer-error'></div>").text("Could not display the tournament."));
                        return false;
                    }
                    if (c.children().length == 0)
                        c.append($("<i></i>").text("No details are available for this tournament."));
                    return true;
                });
            },
            EventDrawer.errorMap(function (message, status) {
                if (fallback) {
                    drawer.settle(content, function (c) {
                        c.empty();
                        fallback(c, message, status);
                    });
                } else {
                    drawer.fail(content, "Could not load the tournament (HTTP " + status + "). " + message);
                }
            }));
    },

    revealTournament:function (id) {
        var row = this.rows[id];
        if (row == null)
            return false;
        row.drawer.open();
        EventDrawer.scrollIntoView(row.element);
        return true;
    },

    // Opens that tournament's row (loading the list first if needed) and scrolls it into view.  A tournament that is
    // not in the live list (e.g. a scheduled one that has not started) just leaves the list showing.
    showTournament:function (id) {
        var that = this;
        if (this.list == null) {
            this.loadLiveTournaments(id);
            return;
        }
        if (this.listLoaded && this.listPending == null && this.revealTournament(id))
            return;
        this.loadList(function () { that.revealTournament(id); });
    },

    createStandingsTable:function (xmlstandings, tournamentId, tournamentStage) {
        var standingsTable = $("<table class='standings'></table>");

        standingsTable.append("<tr>"
                            + "<th>Standing</th>"
                            + "<th>Player</th>"
                            + "<th>Points</th>"
                            + "<th>Games played</th>"
                            + "<th>Mod. Median Score</th>"
                            + "<th>Cumulative Score</th>"
                            + "<th>Opponent Win %</th>" 
                            + "</tr>");

        var standings = [];
        for (var k = 0; k < xmlstandings.length; k++) {
            var standing = {};
            var xmlstanding = xmlstandings[k];
            
            standing.currentStanding = xmlstanding.getAttribute("standing");
            standing.player = xmlstanding.getAttribute("player");
            standing.points = parseInt(xmlstanding.getAttribute("points"));
            standing.gamesPlayed = parseInt(xmlstanding.getAttribute("gamesPlayed"));
            standing.opponentWinPerc = xmlstanding.getAttribute("opponentWin");
            standing.medianScore = xmlstanding.getAttribute("medianScore");
            standing.cumulativeScore = xmlstanding.getAttribute("cumulativeScore");

            if (tournamentStage == "Finished")
                standing.playerStr = "<a target='_blank' href='/gemp-lotr-server/tournament/" + encodeURIComponent(tournamentId) + "/deck/" + encodeURIComponent(standing.player) + "/html'>" + standing.player + "</a>";
            else
                standing.playerStr = standing.player;
            
            standings.push(standing);
        }
        
        standings.sort((a, b) => a.currentStanding - b.currentStanding);
        
        var secondColumnBaseIndex = Math.ceil(standings.length / 2);
        
        for (var k = 0; k < standings.length; k++) {
            var standing = standings[k];
            
            standingsTable.append("<tr>" + 
                                  "<td>" + standing.currentStanding + "</td>" 
                                  + "<td>" + standing.playerStr + "</td>"
                                  + "<td>" + standing.points + "</td>"
                                  + "<td>" + standing.gamesPlayed + "</td>" 
                                  + "<td>" + standing.medianScore + "</td>" 
                                  + "<td>" + standing.cumulativeScore + "</td>" 
                                  + "<td>" + standing.opponentWinPerc + "</td>"
                                  + "</tr>");
        }

        return standingsTable;
    }
});
