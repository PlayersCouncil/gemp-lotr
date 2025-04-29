var TournamentResultsUI = Class.extend({
    communication:null,
    formatDialog:null,

    init:function (url) {
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

        this.loadLiveTournaments();
    },

    loadLiveTournaments:function () {
        var that = this;
        this.communication.getLiveTournaments(
            function (xml) {
                that.loadedTournaments(xml);
            });
    },

    loadHistoryTournaments:function () {
        var that = this;
        this.communication.getHistoryTournaments(
            function (xml) {
                that.loadedTournaments(xml);
            });
    },

    loadedTournament:function (xml, targetDiv) {
        var that = this;
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

    loadedTournaments:function (xml) {
        var that = this;
        log(xml);
        var root = xml.documentElement;
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
            }
            if (tournaments.length == 0)
                $("#tournamentResults").append("<i>There is no running tournaments at the moment</i>");
        }
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
                standing.playerStr = "<a target='_blank' href='/gemp-lotr-server/tournament/" + tournamentId + "/deck/" + standing.player + "/html'>" + standing.player + "</a>";
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
