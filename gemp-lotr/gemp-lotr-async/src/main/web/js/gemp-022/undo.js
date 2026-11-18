// Taking a game back to an earlier point (the server side is LotroGameMediator.requestUndo).
//
// Everything the game screen does about it is in this file, so that a new client has one place to read:
//
//   - A second poll, for notices, that runs all the time.  The game screen does not ask for game events while its
//     player is looking at a decision, so anything that has to reach a player then (a request to take the game
//     back, a concession) comes as a notice.
//   - The "Take back..." button: the list of places the player may go back to, each with its tier (green: only your
//     own choices; yellow: the other player's too; red: you have seen something since) and why it may not be asked
//     for just now.
//   - The dialog a player is shown when the other player asks, with Approve, Decline and Decline all.
//   - What happens when the game has been taken back: the table is fetched again from nothing.  This client does
//     that by loading the page again; the server has moved it to a new channel in any case.
//   - The banner that says a game was taken back, in a game and in a replay.
var GempLotrUndo = Class.extend({
    game: null,
    comm: null,
    lastNotice: -1,
    started: false,
    reloading: false,
    listDialog: null,
    requestDialog: null,
    countdown: null,

    init: function (game) {
        this.game = game;
        this.comm = game.communication;
    },

    // To be called once the game session has started (never for a replay).
    start: function () {
        if (this.started)
            return;
        this.started = true;
        var that = this;

        // A banner left by the page that was showing when the game was taken back
        try {
            var left = window.sessionStorage.getItem("gempUndoBanner");
            if (left != null) {
                window.sessionStorage.removeItem("gempUndoBanner");
                var banner = JSON.parse(left);
                if (banner.gameId == getUrlParam("gameId"))
                    this.showBanner(banner.message, banner.tier);
            }
        } catch (e) {
        }

        // (somebody only watching has nothing to take back, but is told of everything like anybody else)
        if (!this.game.spectatorMode) {
            $("#undoGame").button().click(function () {
                that.openList();
            });
        }

        this.pollNotices();
        this.comm.getRestorePoints(function (json) {
            // The button is there only in a game where something can be taken back (not, say, a scenario that is
            // being written as it is played)
            var policy = json.policy || {};
            if (!that.game.spectatorMode && (policy.green || policy.yellow || policy.red))
                $("#undoGame").closest(".setting-row").show();
            // A request may have been waiting since before this page was loaded
            if (json.pending != null)
                that.showPending(json.pending);
        }, function () {
        });
    },

    /* ---- notices ---- */

    pollNotices: function () {
        var that = this;
        this.comm.getGameNotices(this.lastNotice,
            function (json) {
                if (json.latest != null && that.lastNotice < 0)
                    that.lastNotice = json.latest;
                var notices = json.notices || [];
                for (var i = 0; i < notices.length; i++) {
                    that.lastNotice = Math.max(that.lastNotice, notices[i].number);
                    that.notice(notices[i]);
                }
                if (!that.reloading)
                    that.pollNotices();
            },
            function () {
                // The server may be restarting, or the game may be gone: ask again after a while, not at once
                if (!that.reloading)
                    setTimeout(function () {
                        that.pollNotices();
                    }, 5000);
            });
    },

    notice: function (notice) {
        if (notice.type == "resync") {
            if (notice.reason == "revert") {
                // The game is no longer the game this page has drawn.  Start over.
                this.reloading = true;
                try {
                    window.sessionStorage.setItem("gempUndoBanner", JSON.stringify({
                        gameId: getUrlParam("gameId"), message: notice.message, tier: notice.tier
                    }));
                } catch (e) {
                }
                location.reload();
            } else {
                // Something happened that the player has to hear of even while looking at a decision (the game
                // ended): ask for the game's events once.  With no decision waiting they are on their way already.
                this.game.pollOnce();
            }
        } else if (notice.type == "undoRequested") {
            this.showPending({
                requestId: notice.requestId, requester: notice.requester, label: notice.label, tier: notice.tier,
                reasons: notice.reasons == null || notice.reasons == "" ? [] : notice.reasons.split("; "),
                secondsLeft: parseInt(notice.seconds)
            });
        } else if (notice.type == "undoResolved") {
            this.closeRequest();
            if (notice.outcome != "done" && notice.message != null)
                this.showBanner(notice.message, null);
        }
    },

    /* ---- asking ---- */

    tierWords: function (tier) {
        if (tier == "GREEN")
            return "Green: only your own choices are undone";
        if (tier == "YELLOW")
            return "Yellow: the other player will have to redo some of their choices";
        if (tier == "RED")
            return "Red: cards have been seen since then that would not have been";
        return "";
    },

    tierChip: function (tier) {
        return $("<span class='undoTier'></span>").addClass("undoTier-" + (tier || "").toLowerCase())
            .text(tier == null ? "" : tier.charAt(0) + tier.substring(1).toLowerCase()).attr("title", this.tierWords(tier));
    },

    openList: function () {
        var that = this;
        this.comm.getRestorePoints(function (json) {
            that.closeList();
            var content = $("<div class='undoList'></div>");
            if (json.pending != null) {
                content.append($("<div class='undoNote'></div>").text("A request to take the game back is waiting for an answer."));
            } else if (json.points.length == 0) {
                content.append($("<div class='undoNote'></div>").text("There is nothing you can take back just now."));
            }
            for (var i = 0; i < json.points.length && json.pending == null; i++) {
                (function (point) {
                    var row = $("<div class='undoPoint'></div>");
                    var button = $("<button></button>").text(point.tier == "GREEN" ? "Take back" : "Ask").button();
                    if (point.unavailable != null)
                        button.button("disable");
                    button.click(function () {
                        that.ask(point.entryCount);
                    });
                    row.append(button);
                    row.append(that.tierChip(point.tier));
                    row.append($("<span class='undoLabel'></span>").text(point.label));
                    if (point.unavailable != null)
                        row.append($("<div class='undoWhy'></div>").text(point.unavailable));
                    else if (point.reasons.length > 0)
                        row.append($("<div class='undoWhy'></div>").text(point.reasons.join("; ")));
                    content.append(row);
                })(json.points[i]);
            }
            var declineAll = $("<input type='checkbox' id='undoDeclineAll'>").prop("checked", json.autoDecline);
            declineAll.change(function () {
                that.comm.setUndoAutoDecline($(this).prop("checked"), function () {
                }, function () {
                });
            });
            content.append($("<div class='undoDecline'></div>").append(declineAll)
                .append($("<label for='undoDeclineAll'></label>").text(" Decline every request to take this game back")));

            that.listDialog = content.dialog({
                title: "Take the game back",
                width: 520,
                modal: false,
                close: function () {
                    $(this).dialog("destroy").remove();
                    that.listDialog = null;
                }
            });
        }, function () {
            that.showBanner("The list of places to go back to could not be fetched.", null);
        });
    },

    closeList: function () {
        if (this.listDialog != null)
            this.listDialog.dialog("close");
    },

    ask: function (entryCount) {
        var that = this;
        this.comm.requestUndo(entryCount, function (json) {
            that.closeList();
            // "done" needs nothing: the notice that the game was taken back loads the page again.
            // "pending" needs nothing either: the notice that a request is waiting shows it.
            if (json.status == "refused" && json.message != null)
                that.showBanner(json.message, null);
        }, function () {
            that.showBanner("The request could not be sent.", null);
        });
    },

    /* ---- a request that is waiting ---- */

    showPending: function (pending) {
        var that = this;
        this.closeRequest();
        var mine = pending.requester == this.game.bottomPlayerId && !this.game.spectatorMode;
        var asked = !mine && !this.game.spectatorMode;
        var left = pending.secondsLeft;

        var content = $("<div class='undoRequest'></div>");
        content.append($("<div></div>").text(mine ? "You have asked for the game to be taken back to:"
            : pending.requester + " asks for the game to be taken back to:"));
        content.append($("<div class='undoLabel'></div>").text(pending.label));
        content.append($("<div></div>").append(this.tierChip(pending.tier)).append($("<span></span>").text(" " + this.tierWords(pending.tier))));
        if (pending.reasons != null && pending.reasons.length > 0)
            content.append($("<div class='undoWhy'></div>").text(pending.reasons.join("; ")));
        var clock = $("<div class='undoClock'></div>");
        content.append(clock);
        content.append($("<div class='undoNote'></div>").text("The game is standing still until this is settled."));

        var reply = function (answer) {
            return function () {
                that.comm.replyUndo(pending.requestId, answer, function (json) {
                    if (json.status == "refused" && json.message != null && answer == "approve")
                        that.showBanner(json.message, null);
                }, function () {
                });
            };
        };
        var buttons = {};
        if (mine)
            buttons["Withdraw the request"] = reply("cancel");
        if (asked) {
            buttons["Approve"] = reply("approve");
            buttons["Decline"] = reply("decline");
            buttons["Decline all for this game"] = reply("declineAll");
        }

        this.requestDialog = content.dialog({
            title: "Take the game back?",
            width: 480,
            modal: false,
            closeOnEscape: false,
            buttons: buttons,
            close: function () {
                $(this).dialog("destroy").remove();
                that.requestDialog = null;
            }
        });
        var tick = function () {
            clock.text(left > 0 ? "Refused in " + left + " seconds if nobody answers" : "Out of time");
            left--;
        };
        tick();
        this.countdown = setInterval(tick, 1000);
    },

    closeRequest: function () {
        if (this.countdown != null) {
            clearInterval(this.countdown);
            this.countdown = null;
        }
        if (this.requestDialog != null)
            this.requestDialog.dialog("close");
    },

    /* ---- saying so ---- */

    // The game event that marks a game as taken back, in a game and in a replay.
    gameReverted: function (gameEvent) {
        this.showBanner(gameEvent.getAttribute("message"), gameEvent.getAttribute("tier"));
    },

    showBanner: function (message, tier) {
        if (message == null || message == "")
            return;
        $(".undoBanner").remove();
        var banner = $("<div class='undoBanner'></div>");
        if (tier != null && tier != "")
            banner.append(this.tierChip(tier));
        banner.append($("<span></span>").text(" " + message));
        banner.click(function () {
            $(this).remove();
        });
        $("body").append(banner);
        setTimeout(function () {
            banner.fadeOut(1000, function () {
                $(this).remove();
            });
        }, 10000);
    }
});
