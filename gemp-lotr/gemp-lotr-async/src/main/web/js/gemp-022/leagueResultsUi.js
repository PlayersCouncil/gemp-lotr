/**
 * Current leagues and one league's full detail.
 *
 *   new LeagueResultsUI(url)                       // legacy: list in $("#leagueResults"), shared detail area
 *   new LeagueResultsUI(url, joinCallback)         //         $("#leagueExtraInfo") (no page uses this mode now)
 *   new LeagueResultsUI(url, joinCallback, {list: $container})
 *       // list mode (the Events tab, and Play > League): every league is a header row ([+ Details] name ...... dates) with its own
 *       // drawer underneath (eventDrawer.js) holding the full detail, rendered where the viewer clicked.  Several
 *       // drawers may be open at once.  Options:
 *       //   list:     container for the rows (required for list mode)
 *       //   autoLoad: false = do not fetch the list from the constructor (default true)
 *       //   onJoinError: function (leagueCode, message, status) reporting a failed join (default: alert on 409)
 *
 * joinCallback(leagueCode), if given, runs after the viewer joins a league from any of this object's renders.  In list mode the
 * joined league's drawer (and any other open drawer showing that league, e.g. the calendar preview) is re-rendered in
 * place, still open.
 *
 * createLeagueRow(league, options) builds one row + drawer; the calendar preview (calendarUi.js) uses it too.
 *
 * refreshList() (list mode) re-fetches the list and updates it in place: rows keep their element and drawer, open
 * drawers are re-rendered where they are (keeping the results tab the viewer had chosen), shut ones re-fetch when next
 * opened, new leagues get rows and finished ones lose theirs.  The Events tab calls it when the viewer comes back.
 */
var LeagueResultsUI = Class.extend({
    communication:null,
    questionDialog:null,
    formatDialog:null,
    joinCallback:null,
    cardInfoDialog:null,
    options:null,
    list:null,           // list mode: the rows' container; null = legacy #leagueResults / #leagueExtraInfo
    rows:null,           // list mode: league code -> {element, header, button, drawer}
    listLoaded:false,
    listPending:null,    // list mode: callbacks waiting on the in-flight list request
    drawers:null,        // league code -> [EventDrawer] rendered by this object (list rows and calendar previews)

    init:function (url, joinCallback, options) {
        this.communication = new GempLotrCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });

        this.questionDialog = $("<div></div>")
            .dialog({
                autoOpen:false,
                closeOnEscape:true,
                resizable:false,
                modal:true,
                title:"League operation",
                closeText: ''
            });

        this.formatDialog = $("<div></div>")
            .dialog({
                autoOpen:false,
                closeOnEscape:true,
                resizable:false,
                modal:true,
                title:"Format description",
                closeText: ''
            });
            
        this.joinCallback = joinCallback;
        this.options = options || {};
        this.list = this.options.list || null;
        this.rows = {};
        this.drawers = {};

        if (this.options.autoLoad !== false)
            this.loadResults();
    },

    // then: list mode only; runs once the list has been (re)rendered
    loadResults:function (then) {
        var that = this;
        if (this.list != null) {
            this.loadList(then);
            return;
        }
        this.communication.getLeagues(
            function (xml) {
                that.loadedLeagueResults(xml);
            });
    },

    // Legacy: reloads the list and shows the league in #leagueExtraInfo.  List mode: opens that league's row.
    loadResultsWithLeague:function (type) {
        var that = this;
        if (this.list != null) {
            this.showLeague(type);
            return;
        }
        this.communication.getLeagues(
            function (xml) {
                that.loadedLeagueResults(xml);
                that.communication.getLeague(type,
                    function (xml) {
                        that.loadedLeague(xml);
                    });
            });
    },

    // Renders one league's full detail (cost, membership, description, RTMD path, per-serie format/collection and
    // the results tabs) into "target".
    //   target:  a jQuery container; defaults to $("#leagueExtraInfo"), the Current Leagues panel, which is what every
    //            pre-existing caller relies on.
    //   options: {
    //       idPrefix:   prefix for the tab panel ids (<prefix>overall, <prefix>matches, <prefix>serie<j>).  Defaults
    //                   to "league", i.e. the historical ids #leagueoverall / #leaguematches / #leagueserie<j>.  Any
    //                   render into a non-default target must pass its own prefix so two leagues on one page do not
    //                   produce duplicate ids.
    //       membership: false hides the membership / join / draft / invite-only block (used for finished leagues,
    //                   whatever the server says about "joinable").  Defaults to true.
    //       showName:   false leaves out the big league name (a drawer's header row already shows it).  Defaults to true.
    //       onJoined:   function (leagueCode) run after a successful join instead of the default, which reloads the
    //                   list and re-renders the league into #leagueExtraInfo (loadResultsWithLeague).  joinCallback
    //                   runs either way.
    //   }
    // Scrolling the league form back to the top only happens for the default target.
    loadedLeague:function (xml, target, options) {
        var that = this;
        var isDefaultTarget = (target == null);
        target = isDefaultTarget ? $("#leagueExtraInfo") : target;
        options = options || {};
        var idPrefix = (options.idPrefix != null) ? String(options.idPrefix) : "league";
        var showMembership = options.membership !== false;
        var onJoined = (typeof options.onJoined == "function") ? options.onJoined : null;
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'league') {
            target.html("");

            var league = root;

            var leagueName = league.getAttribute("name");
            var leagueType = league.getAttribute("code");
            var cost = parseInt(league.getAttribute("cost"));
            var start = league.getAttribute("start");
            var end = league.getAttribute("end");
            var member = league.getAttribute("member");
            var joinable = league.getAttribute("joinable");
            var draftable = league.getAttribute("draftable");

            var id = league.getAttribute("id");
            var desc = league.getAttribute("desc");
            var inviteOnly = league.getAttribute("inviteOnly");

            var isRTMD = league.getAttribute("type") === "RTMD"; // RTMD

            if (options.showName !== false)
                target.append("<div class='leagueName'>" + leagueName + "</div>");

            var costStr = formatPrice(cost);
            target.append("<div class='leagueCost'><b>Cost:</b> " + costStr + "</div><br>");


            if (!showMembership) {
                // finished league (event history): no join / buy / draft controls
            } else if (member == "true") {
                var memberDiv = $("<div class='leagueMembership'>You are already a member of this league. </div>");
                if (draftable == "true") {
                    var draftBut = $("<button>Go to draft</button>").button();
                    var draftFunc = (function (leagueCode) {
                        return function() {
                            var win = window.open("/gemp-lotr/soloDraft.html?eventId=" + leagueCode, '_blank');
                            if (win) {
                                win.focus();
                            }
                        };
                    })(leagueType);
                    draftBut.click(draftFunc);
                    memberDiv.append(draftBut);
                }
                target.append(memberDiv);
            } else if (joinable == "true") {
                var joinBut = $("<button>Join league</button>").button();

                var joinFunc = (function (leagueCode, costString) {
                    return function () {
                        that.displayBuyAction("Do you want to join the league by paying " + costString + "?",
                            function () {
                                that.communication.joinLeague(leagueCode, function () {
                                    if (onJoined != null)
                                        onJoined(leagueCode);
                                    else
                                        that.loadResultsWithLeague(leagueCode);
                                    if(that.joinCallback != null) {
                                        that.joinCallback(leagueCode);
                                    }
                                }, that.joinErrorMap(leagueCode));
                            });
                    };
                })(leagueType, costStr);

                joinBut.click(joinFunc);
                var joinDiv = $("<div class='leagueMembership'>You're not a member of this league. </div>");
                joinDiv.append(joinBut);
                target.append(joinDiv);
            } else if (inviteOnly == "true") {
                var joinDiv = $("<div class='leagueMembership'><b>Invitation-only. See below for how to join.</b></div>");
                target.append(joinDiv);
            }

            if(desc) {
                let descDiv = $("<div></div>");
                descDiv.html(desc);
                target.append(descDiv);
                target.append("<br><hr><br>");
            }

            // RTMD: Race path display
            if (isRTMD) {
                var pathLength = parseInt(league.getAttribute("pathLength"));
                var playerPosition = league.getAttribute("playerPosition");
                var cumulative = league.getAttribute("cumulative") === "true";
                var advancementMode = league.getAttribute("advancementMode");
                var advanceFactor = parseInt(league.getAttribute("advanceFactor"));

                var pathDiv = $("<div class='rtmd-path-display'></div>");
                pathDiv.append("<h3>Race to Mount Doom</h3>");
                pathDiv.append(`<div style='color:#888; margin-bottom:8px;'><p>Race to Mount Doom is a meta-progression constructed league.  Every player starts at the first meta-site, which will be played automatically to your support area whenever you play matches in this league.  Such meta-sites tend to have game text which is beneficial at low levels, detrimental at middle levels, and catastrophic at high levels.</p>
                    <p>As you play matches, good performance will push you to harder and harder meta-sites.  Each time you get promoted, you will leave the old meta-site behind permanently and start using the next meta-site.  Some meta-sites impose deckbuilding restrictions, so watch out!</p>
                    <p>There are dozens of different meta-site modifiers, so no two Races will ever be the same!</p>
                    <p>NOTE: Any meta-site that says "you" or "yours" <i>only</i> affects that player.  If it does not have one of those words, it affects both players.</p></div>`);

                if (playerPosition) {
                    pathDiv.append("<div class='rtmd-player-position'>Your level: <b>" + playerPosition + "</b> of " + pathLength + "</div>");
                }

                var advanceDesc = advancementMode === "SCORE" ? "points" : "win(s)";
                if (advanceFactor > 1) {
                    pathDiv.append("<div style='color:#888; margin-bottom:8px;'>Advance every " + advanceFactor + " " + advanceDesc + "</div>");
                }

                var cardRow = $("<div style='display:flex;flex-wrap:wrap;gap:8px;justify-content:center;margin:12px auto;max-width:700px;'></div>");
                var metaSites = league.getElementsByTagName("metaSite");
                for (var i = 0; i < metaSites.length; i++) {
                    var site = metaSites[i];
                    var pos = parseInt(site.getAttribute("position"));
                    var modBpId = site.getAttribute("blueprintId");
                    var visBpId = site.getAttribute("visualBlueprintId");

                    // Determine highlight state
                    var borderStyle = "2px solid transparent";
                    if (playerPosition) {
                        var playerPosInt = parseInt(playerPosition);
                        if (pos === playerPosInt) {
                            borderStyle = "3px solid gold";
                        } else if (cumulative && pos < playerPosInt) {
                            borderStyle = "2px solid rgba(255,215,0,0.4)";
                        }
                    }

                    // Build composite card thumbnail
                    var visualUrl = visBpId ? Card.getImageUrl(visBpId) : Card.getImageUrl(modBpId);
                    var modifierUrl = Card.getImageUrl(modBpId);
                    var overlayHeight = Card.MetaSiteOverlayHeight;

                    var thumbWrapper = $("<div class='rtmd-meta-site' style='position:relative;width:120px;height:167px;border:" + borderStyle
                        + ";border-radius:8px;overflow:hidden;cursor:pointer;flex-shrink:0;'></div>")
                        .attr({"data-modifier": modBpId || "", "data-visual": visBpId || ""});
                    thumbWrapper.append("<img src='" + visualUrl + "' style='width:100%;height:100%;object-fit:cover;'>");
                    if (visBpId) {
                        thumbWrapper.append("<div style='position:absolute;bottom:0;width:100%;height:" + overlayHeight
                            + "%;overflow:hidden;'><img src='" + modifierUrl
                            + "' style='width:100%;height:100%;object-fit:cover;object-position:bottom;'></div>");
                    }
                    thumbWrapper.append("<div style='position:absolute;top:30%;left:10%;font-size:11px;font-weight:bold;"
                        + "color:white;text-shadow:0 0 3px black,0 0 3px black;'>" + pos + "</div>");

                    // Click handler (without the hall's shared card preview): open card in a CardInfoDialog
                    if (!window.GempCardPreview) (function(vBpId, mBpId) {
                        thumbWrapper.on("click contextmenu", function(event) {
                            event.preventDefault();
                            if (!that.cardInfoDialog) {
                                that.cardInfoDialog = new CardInfoDialog(window.innerWidth, window.innerHeight);
                                // Close dialog on click outside (same pattern as deckbuilder/game)
                                $(document).on("mouseup", function(e) {
                                    if (that.cardInfoDialog && that.cardInfoDialog.isOpen()) {
                                        // Don't close if clicking inside the dialog
                                        if ($(e.target).closest(".ui-dialog").length === 0) {
                                            that.cardInfoDialog.mouseUp();
                                        }
                                    }
                                });
                            }
                            // Create a Card from the visual blueprint (or modifier if no visual)
                            var card = new Card(vBpId || mBpId, "", "", "SPECIAL", null, "");
                            if (vBpId) {
                                Card.metaSiteOverlays[mBpId] = card.imageUrl;
                                card.overlayImageUrl = Card.getImageUrl(mBpId);
                            }
                            that.cardInfoDialog.showCard(card);
                            event.stopPropagation();
                        });
                    })(visBpId, modBpId);

                    cardRow.append(thumbWrapper);
                }
                pathDiv.append(cardRow);
                // Click / right-click: the hall's zoomable card preview (js/gemp-022/cardPreview.js), which remembers
                // its zoom level: the position's visual card with the modifier over its bottom
                if (window.GempCardPreview) {
                    GempCardPreview.bind(cardRow, ".rtmd-meta-site", {
                        click: true,
                        contextmenu: true,
                        resolve: function (element) {
                            var mod = $(element).attr("data-modifier");
                            var vis = $(element).attr("data-visual");
                            if (!mod && !vis)
                                return null;
                            return vis ? {blueprintId: vis, overlayBlueprintId: mod || null} : {blueprintId: mod};
                        }
                    });
                }

                if (cumulative) {
                    pathDiv.append("<div style='color:#888; font-size:0.9em; margin-top:4px;'>Cumulative mode: all prior meta-sites remain active</div>");
                }

                pathDiv.append("<br><hr><br>");
                target.append(pathDiv);
            }

            var tabDiv = $("<div width='100%'></div>");
            var tabNavigation = $("<ul></ul>");
            tabDiv.append(tabNavigation);

            // Overall tab
            var tabContent = $("<div></div>").attr("id", idPrefix + "overall");

            var standings = league.getElementsByTagName("leagueStanding");
            if (standings.length > 0)
                tabContent.append(this.createStandingsTable(standings, isRTMD)); // RTMD: pass flag
            tabDiv.append(tabContent);

            tabNavigation.append($("<li></li>").append($("<a>Overall results</a>").attr("href", "#" + idPrefix + "overall")));
            tabNavigation.append($("<li></li>").append($("<a>Your league matches</a>").attr("href", "#" + idPrefix + "matches")));

            var matchResults = $("<div></div>").attr("id", idPrefix + "matches");
            tabDiv.append(matchResults);

            var series = league.getElementsByTagName("serie");
            for (var j = 0; j < series.length; j++) {
                var serie = series[j];
                matchResults.append("<div>Serie " + (j + 1) + "</div>");
                var matchGroup = $("<table class='standings'><tr><th>Winner</th><th>Loser</th></tr></table>");
                var matches = serie.getElementsByTagName("match");
                for (var k = 0; k<matches.length; k++) {
                    var match = matches[k];
                    matchGroup.append("<tr><td>"+match.getAttribute("winner")+"</td><td>"+match.getAttribute("loser")+"</td></tr>");
                }

                matchResults.append(matchGroup);

                var tabContent = $("<div></div>").attr("id", idPrefix + "serie" + j);

                var serieName = serie.getAttribute("type");
                var serieStart = serie.getAttribute("start");
                var serieEnd = serie.getAttribute("end");
                var maxMatches = serie.getAttribute("maxMatches");
                var formatType = serie.getAttribute("formatType");
                var format = serie.getAttribute("format");
                var collection = serie.getAttribute("collection");
                var limited = serie.getAttribute("limited");

                var serieText = serieName + " - " + serieStart + " to " + serieEnd;
                target.append("<div class='serieName'>" + serieText + "</div>");

                // formatAvailable is absent from older servers; treat that as available.
                var formatAvailable = serie.getAttribute("formatAvailable") !== "false" && formatType;
                var formatName = $("<span" + (formatAvailable ? " class='clickableFormat'" : "") + ">"
                    + ((limited == "true") ? "" : "Constructed ") + format + (formatAvailable ? "" : " (retired)") + "</span>");
                var formatDiv = $("<div><b>Format:</b> </div>");
                formatDiv.append(formatName);
                if (formatAvailable) formatName.click(
                    (function (ft) {
                        return function () {
                            that.formatDialog.html("");
                            that.formatDialog.dialog("open");
                            that.communication.getFormat(ft,
                                function (html) {
                                    that.formatDialog.html(html);
                                });
                        };
                    })(formatType));
                target.append(formatDiv);
                target.append("<div><b>Collection:</b> " + collection + "</div>");

                tabContent.append("<div>Maximum ranked matches in serie: " + maxMatches + "</div>");

                var standings = serie.getElementsByTagName("standing");
                if (standings.length > 0)
                    tabContent.append(this.createStandingsTable(standings, false)); // Serie standings don't need position
                tabDiv.append(tabContent);

                tabNavigation.append($("<li></li>").append($("<a></a>").attr("href", "#" + idPrefix + "serie" + j).text("Serie " + (j + 1))));
            }

            tabDiv.tabs();

            target.append(tabDiv);
            if (isDefaultTarget)
                $(".top-of-league-form").parent().scrollTop(0);
        }
    },

    loadedLeagueResults:function (xml) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
        if (this.list != null) {
            this.renderLeagueRows(root);
            return;
        }
        if (root.tagName == 'leagues') {
            $("#leagueResults").html("");

            var leagues = root.getElementsByTagName("league");
            for (var i = 0; i < leagues.length; i++) {
                var league = leagues[i];
                var leagueName = league.getAttribute("name");
                var leagueType = league.getAttribute("code");
                var start = league.getAttribute("start");
                var end = league.getAttribute("end");
                var desc = league.getAttribute("desc");
                var inviteOnly = league.getAttribute("inviteOnly") === "true";

                $("#leagueResults").append("<div class='leagueName'>" + leagueName + "</div>");

                if(hall.userInfo.type.includes("l")) {
                    $("#leagueResults").append("<span class='league-id'>" + leagueType + "</span>");
                }

                var duration = start + " to " + end;
                $("#leagueResults").append("<div class='leagueDuration'><b>Duration (server time, UTC):</b> " + duration + "</div>");

                var detailsBut = $("<button>See details</button>").button();
                detailsBut.click(
                    (function (type) {
                        return function () {
                            that.communication.getLeague(type,
                                function (xml) {
                                    that.loadedLeague(xml);
                                });
                        };
                    })(leagueType));
                $("#leagueResults").append(detailsBut);
            }
        }
    },

    // ---- list mode (the Events tab's Current Leagues): one header row + drawer per league ----

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
        this.communication.getLeagues(
            function (xml) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.loadedLeagueResults(xml);
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            },
            EventDrawer.errorMap(function (message) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.list.empty().append($("<div class='event-drawer-error'></div>")
                    .text("Could not load the current leagues. " + message));
                for (var i = 0; i < waiting.length; i++)
                    waiting[i]();
            }));
    },

    // Re-fetches the list and updates it in place (see the header comment).  A failed refresh keeps what is shown.
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
        this.communication.getLeagues(
            function (xml) {
                var waiting = that.listPending || [];
                that.listPending = null;
                that.updateLeagueRows(xml.documentElement);
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

    updateLeagueRows:function (root) {
        var that = this;
        if (root == null || root.tagName != 'leagues')
            return;
        var leagues = root.getElementsByTagName("league");
        var data = [];
        for (var i = 0; i < leagues.length; i++) {
            data.push({
                code: leagues[i].getAttribute("code"),
                name: leagues[i].getAttribute("name"),
                start: leagues[i].getAttribute("start"),
                end: leagues[i].getAttribute("end")
            });
        }
        this.rows = EventDrawer.updateRows(this.list, this.rows, data, {
            key: function (league) { return league.code; },
            create: function (league) { return that.createLeagueRow(league); },
            title: function (league) { return league.name; },
            date: function (league) { return LeagueResultsUI.dateRange(league.start, league.end); },
            emptyText: "There are no current leagues at the moment."
        });
        this.listLoaded = true;
    },

    renderLeagueRows:function (root) {
        this.list.empty();
        this.rows = {};
        if (root == null || root.tagName != 'leagues')
            return;
        var leagues = root.getElementsByTagName("league");
        for (var i = 0; i < leagues.length; i++) {
            var league = leagues[i];
            var data = {
                code: league.getAttribute("code"),
                name: league.getAttribute("name"),
                start: league.getAttribute("start"),
                end: league.getAttribute("end")
            };
            var row = this.createLeagueRow(data);
            this.rows[data.code] = row;
            this.list.append(row.element);
        }
        if (leagues.length == 0)
            this.list.append($("<i></i>").text("There are no current leagues at the moment."));
        this.listLoaded = true;
    },

    // One league's header row and drawer.
    //   league:  {code, name, start, end}
    //   options: {
    //       action:   undefined = the + Details / - Details toggle; {label, click} = a fixed button (the
    //                 calendar's "Go to League"), in which case the drawer is meant to stay open
    //       open:     true = born open
    //       fallback: function (content, message, status) rendering something instead of an error when the league
    //                 cannot be fetched (the calendar's own facts)
    //       extras:   more jQuery elements for the header
    //   }
    // Returns {element, header, button, drawer}.
    createLeagueRow:function (league, options) {
        var that = this;
        options = options || {};
        var code = league.code == null ? "" : String(league.code);
        var extras = [];
        // league admins see the league code (as before), as text a click selects whole
        if (code !== "" && EventDrawer.userType().includes("l")) {
            extras.push($("<span class='league-id event-row-code event-row-select'></span>")
                .attr("title", "League code - click to select")
                .text(code)
                .click(function () { EventDrawer.selectText(this); }));
        }
        if (options.extras)
            extras = extras.concat(options.extras);
        return EventDrawer.row({
            kind: "league",
            title: league.name,
            date: LeagueResultsUI.dateRange(league.start, league.end),
            dateTitle: "Server time (UTC)",
            extras: extras,
            action: options.action,
            open: options.open,
            load: function (drawer, content) {
                that.loadLeagueDrawer(code, drawer, content, options.fallback);
            }
        });
    },

    loadLeagueDrawer:function (code, drawer, content, fallback) {
        var that = this;
        this.rememberDrawer(code, drawer);
        this.communication.getLeague(code,
            function (xml) {
                drawer.settle(content, function (c) {
                    return that.renderLeagueDrawer(xml, code, drawer, c);
                });
            },
            EventDrawer.errorMap(function (message, status) {
                if (fallback) {
                    drawer.settle(content, function (c) {
                        c.empty();
                        fallback(c, message, status);
                    });
                } else {
                    drawer.fail(content, "Could not load the league (HTTP " + status + "). " + message);
                }
            }));
    },

    // Returns false when the league could not be displayed.
    renderLeagueDrawer:function (xml, code, drawer, content) {
        var that = this;
        // a re-render (refresh, join) keeps the results tab the viewer had chosen
        var tabs = content.find(".ui-tabs").first();
        var activeTab = tabs.length > 0 && tabs.tabs("instance") ? tabs.tabs("option", "active") : null;
        content.empty();
        try {
            this.loadedLeague(xml, content, {
                idPrefix: "leagueDrawer" + (++LeagueResultsUI.drawerRenders) + "-",
                showName: false,
                onJoined: function () { that.leagueJoined(code); }
            });
        } catch (e) {
            content.empty().append($("<div class='event-drawer-error'></div>").text("Could not display the league."));
            return false;
        }
        if (content.children().length == 0)
            content.append($("<i></i>").text("No details are available for this league."));
        if (typeof activeTab == "number" && activeTab > 0) {
            var newTabs = content.find(".ui-tabs").first();
            if (newTabs.length > 0 && activeTab < newTabs.find("> ul > li").length)
                newTabs.tabs("option", "active", activeTab);
        }
        return true;
    },

    rememberDrawer:function (code, drawer) {
        var list = this.drawers[code] || (this.drawers[code] = []);
        if ($.inArray(drawer, list) < 0)
            list.push(drawer);
    },

    // After a join: every drawer this object rendered for that league that is still on the page is re-rendered in
    // place (membership, draft button, standings), keeping its open / closed state.
    leagueJoined:function (code) {
        var list = this.drawers[code] || [];
        var live = [];
        for (var i = 0; i < list.length; i++) {
            var drawer = list[i];
            if (drawer.content != null && $.contains(document.documentElement, drawer.content[0])) {
                live.push(drawer);
                drawer.refresh();
            }
        }
        this.drawers[code] = live;
    },

    // Opens that league's row (loading the list first if needed) and scrolls it into view.
    showLeague:function (code) {
        var that = this;
        if (this.list == null) {
            this.loadResultsWithLeague(code);
            return;
        }
        var reveal = function () {
            var row = that.rows[code];
            if (row == null)
                return false;
            row.drawer.open();
            EventDrawer.scrollIntoView(row.element);
            return true;
        };
        if (this.listLoaded && this.listPending == null && reveal())
            return;
        this.loadList(reveal);
    },

    // options.onJoinError(leagueCode, message, status), if given, reports every failed join (the Play > League
    // panel shows it inline); otherwise only "not enough funds" is reported, with an alert, as before.
    joinErrorMap:function (leagueCode) {
        var report = this.options.onJoinError;
        if (typeof report != "function") {
            return {
                "409":function () {
                    alert("You don't have enough funds to join this league.");
                }
            };
        }
        return EventDrawer.errorMap(function (message, status) {
            report(leagueCode, status == 409 ? "You don't have enough funds to join this league." : message, status);
        });
    },

    displayBuyAction:function (text, yesFunc) {
        var that = this;
        this.questionDialog.html("");
        this.questionDialog.html("<div style='scroll: auto'></div>");
        var questionDiv = $("<div>" + text + "</div>");
        questionDiv.append("<br/>");
        questionDiv.append($("<button>Yes</button>").button().click(
            function () {
                that.questionDialog.dialog("close");
                yesFunc();
            }));
        questionDiv.append($("<button>No</button>").button().click(
            function () {
                that.questionDialog.dialog("close");
            }));
        this.questionDialog.append(questionDiv);

        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var horSpace = 250;
        var vertSpace = 120;

        this.questionDialog.dialog({width:Math.min(horSpace, windowWidth), height:Math.min(vertSpace, windowHeight)});
        this.questionDialog.dialog("open");
    },

    createStandingsTable:function (xmlstandings, showPosition) {
        var standingsTable = $("<table class='standings'></table>");

        var headerRow = "<tr><th>Standing</th>";
        if (showPosition) {
            headerRow += "<th>Level</th>";
        }
        headerRow += "<th>Player</th><th>Points</th><th>Games played</th><th>Opp. Win %</th>";
        headerRow += "<th></th><th>Standing</th>";
        if (showPosition) {
            headerRow += "<th>Level</th>";
        }
        headerRow += "<th>Player</th><th>Points</th><th>Games played</th><th>Opp. Win %</th></tr>";
        standingsTable.append(headerRow);

        var standings = [];
        for (var k = 0; k < xmlstandings.length; k++) {
            var standing = {};
            var xmlstanding = xmlstandings[k];

            standing.currentStanding = xmlstanding.getAttribute("standing");
            standing.player = xmlstanding.getAttribute("player");
            standing.points = parseInt(xmlstanding.getAttribute("points"));
            standing.gamesPlayed = parseInt(xmlstanding.getAttribute("gamesPlayed"));
            standing.opponentWinPerc = xmlstanding.getAttribute("opponentWin");
            standing.position = xmlstanding.getAttribute("position"); // RTMD

            standings.push(standing);
        }

        standings.sort((a, b) => a.currentStanding - b.currentStanding);

        var secondColumnBaseIndex = Math.ceil(standings.length / 2);

        for (var k = 0; k < secondColumnBaseIndex; k++) {
            var standing = standings[k];

            var row = "<tr><td>" + standing.currentStanding + "</td>";
            if (showPosition) row += "<td>" + (standing.position || "-") + "</td>";
            row += "<td>" + standing.player + "</td><td>"
                + standing.points + "</td><td>" + standing.gamesPlayed
                + "</td><td>" + standing.opponentWinPerc + "</td></tr>";
            standingsTable.append(row);
        }

        for (var k = secondColumnBaseIndex; k < standings.length; k++) {
            var standing = standings[k];

            var cells = "<td></td><td>" + standing.currentStanding + "</td>";
            if (showPosition) cells += "<td>" + (standing.position || "-") + "</td>";
            cells += "<td>" + standing.player + "</td><td>" + standing.points
                + "</td><td>" + standing.gamesPlayed + "</td><td>"
                + standing.opponentWinPerc + "</td>";

            $("tr:eq(" + (k - secondColumnBaseIndex + 1) + ")", standingsTable)
                .append(cells);
        }

        return standingsTable;
    }
});
LeagueResultsUI.drawerRenders = 0;   // makes each drawer's tab ids unique on the page

// "2026-09-01 to 2026-09-30"; a missing end shows as the start alone.
LeagueResultsUI.dateRange = function (start, end) {
    var from = start == null ? "" : String(start);
    var to = end == null ? "" : String(end);
    if (to === "" || to === from)
        return from;
    return from + " to " + to;
};
