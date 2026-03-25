var LeagueResultsUI = Class.extend({
    communication:null,
    questionDialog:null,
    formatDialog:null,
    joinCallback:null,
    cardInfoDialog:null,

    init:function (url, joinCallback) {
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

        this.loadResults();
    },

    loadResults:function () {
        var that = this;
        this.communication.getLeagues(
            function (xml) {
                that.loadedLeagueResults(xml);
            });
    },

    loadResultsWithLeague:function (type) {
        var that = this;
        this.communication.getLeagues(
            function (xml) {
                that.loadedLeagueResults(xml);
                that.communication.getLeague(type,
                    function (xml) {
                        that.loadedLeague(xml);
                    });
            });
    },

    loadedLeague:function (xml) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
        if (root.tagName == 'league') {
            $("#leagueExtraInfo").html("");

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

            $("#leagueExtraInfo").append("<div class='leagueName'>" + leagueName + "</div>");

            var costStr = formatPrice(cost);
            $("#leagueExtraInfo").append("<div class='leagueCost'><b>Cost:</b> " + costStr + "</div><br>");


            if (member == "true") {
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
                $("#leagueExtraInfo").append(memberDiv);
            } else if (joinable == "true") {
                var joinBut = $("<button>Join league</button>").button();

                var joinFunc = (function (leagueCode, costString) {
                    return function () {
                        that.displayBuyAction("Do you want to join the league by paying " + costString + "?",
                            function () {
                                that.communication.joinLeague(leagueCode, function () {
                                    that.loadResultsWithLeague(leagueCode);
                                    if(that.joinCallback != null) {
                                        that.joinCallback();
                                    }
                                }, {
                                    "409":function () {
                                        alert("You don't have enough funds to join this league.");
                                    }
                                });
                            });
                    };
                })(leagueType, costStr);

                joinBut.click(joinFunc);
                var joinDiv = $("<div class='leagueMembership'>You're not a member of this league. </div>");
                joinDiv.append(joinBut);
                $("#leagueExtraInfo").append(joinDiv);
            } else if (inviteOnly == "true") {
                var joinDiv = $("<div class='leagueMembership'><b>Invitation-only. See below for how to join.</b></div>");
                $("#leagueExtraInfo").append(joinDiv);
            }

            if(desc) {
                let descDiv = $("<div></div>");
                descDiv.html(desc);
                $("#leagueExtraInfo").append(descDiv);
                $("#leagueExtraInfo").append("<br><hr><br>");
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

                    var thumbWrapper = $("<div style='position:relative;width:120px;height:167px;border:" + borderStyle
                        + ";border-radius:8px;overflow:hidden;cursor:pointer;flex-shrink:0;'></div>");
                    thumbWrapper.append("<img src='" + visualUrl + "' style='width:100%;height:100%;object-fit:cover;'>");
                    if (visBpId) {
                        thumbWrapper.append("<div style='position:absolute;bottom:0;width:100%;height:" + overlayHeight
                            + "%;overflow:hidden;'><img src='" + modifierUrl
                            + "' style='width:100%;height:100%;object-fit:cover;object-position:bottom;'></div>");
                    }
                    thumbWrapper.append("<div style='position:absolute;top:30%;left:10%;font-size:11px;font-weight:bold;"
                        + "color:white;text-shadow:0 0 3px black,0 0 3px black;'>" + pos + "</div>");

                    // Click handler: open card in the shared CardInfoDialog
                    (function(vBpId, mBpId) {
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

                if (cumulative) {
                    pathDiv.append("<div style='color:#888; font-size:0.9em; margin-top:4px;'>Cumulative mode: all prior meta-sites remain active</div>");
                }

                pathDiv.append("<br><hr><br>");
                $("#leagueExtraInfo").append(pathDiv);
            }

            var tabDiv = $("<div width='100%'></div>");
            var tabNavigation = $("<ul></ul>");
            tabDiv.append(tabNavigation);

            // Overall tab
            var tabContent = $("<div id='leagueoverall'></div>");

            var standings = league.getElementsByTagName("leagueStanding");
            if (standings.length > 0)
                tabContent.append(this.createStandingsTable(standings, isRTMD)); // RTMD: pass flag
            tabDiv.append(tabContent);

            tabNavigation.append("<li><a href='#leagueoverall'>Overall results</a></li>");
            tabNavigation.append("<li><a href='#leaguematches'>Your league matches</a></li>");

            var matchResults = $("<div id='leaguematches'></div>");
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

                var tabContent = $("<div id='leagueserie" + j + "'></div>");

                var serieName = serie.getAttribute("type");
                var serieStart = serie.getAttribute("start");
                var serieEnd = serie.getAttribute("end");
                var maxMatches = serie.getAttribute("maxMatches");
                var formatType = serie.getAttribute("formatType");
                var format = serie.getAttribute("format");
                var collection = serie.getAttribute("collection");
                var limited = serie.getAttribute("limited");

                var serieText = serieName + " - " + serieStart + " to " + serieEnd;
                $("#leagueExtraInfo").append("<div class='serieName'>" + serieText + "</div>");

                var formatName = $("<span class='clickableFormat'>" + ((limited == "true") ? "" : "Constructed ") + format + "</span>");
                var formatDiv = $("<div><b>Format:</b> </div>");
                formatDiv.append(formatName);
                formatName.click(
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
                $("#leagueExtraInfo").append(formatDiv);
                $("#leagueExtraInfo").append("<div><b>Collection:</b> " + collection + "</div>");

                tabContent.append("<div>Maximum ranked matches in serie: " + maxMatches + "</div>");

                var standings = serie.getElementsByTagName("standing");
                if (standings.length > 0)
                    tabContent.append(this.createStandingsTable(standings, false)); // Serie standings don't need position
                tabDiv.append(tabContent);

                tabNavigation.append("<li><a href='#leagueserie" + j + "'>Serie " + (j + 1) + "</a></li>");
            }

            tabDiv.tabs();

            $("#leagueExtraInfo").append(tabDiv);
            $(".top-of-league-form").parent().scrollTop(0);
        }
    },

    loadedLeagueResults:function (xml) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
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
                $("#leagueResults").append("<div class='leagueDuration'><b>Duration (GMT+0):</b> " + duration + "</div>");

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