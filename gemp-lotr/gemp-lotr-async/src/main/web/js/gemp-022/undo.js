// Undo and revert: taking a game back to an earlier point (the server side is LotroGameMediator.requestUndo).
//
// Everything the game screen does about it is in this file, so that a new client has one place to read:
//
//   - A second poll, for notices, that runs all the time.  The game screen does not ask for game events while its
//     player is looking at a decision, so anything that has to reach a player then (a revert request, a concession)
//     comes as a notice.
//   - The Undo button, in the lower right of the alert box: there while the player's last choice is still their own
//     business (green: nobody else has answered or seen anything since), and takes it back at once.
//   - "Revert..." in the Options tab: the list of every place the player may go back to.  A green one is an undo;
//     a yellow one (the opponent replays choices) or a red one (hidden information was revealed) is asked of the
//     opponent.  It is in a tab on purpose: it is not meant to be used all the time.
//   - "Auto-decline revert requests" beside it: for this game only, off to begin with.
//   - The dialog a player is shown when the opponent asks, with Approve, Decline and Decline all.  Nothing in it is
//     the default: Enter does nothing, and the first Tab goes to Approve.  While a request waits the game stands
//     still, and the page lets no decision be answered.
//   - What happens when the game has been taken back: the table is fetched again from nothing.  This client does
//     that by loading the page again; the server has moved it to a new channel in any case.
//   - The banner that says a game was taken back, in a game and in a replay.
var GempLotrUndo = Class.extend({
    game: null,
    comm: null,
    lastNotice: -1,
    started: false,
    reloading: false,
    // Whether a request is waiting, so that nothing in the game may be answered
    frozen: false,
    policy: null,
    // The player's last choice, if it is theirs to undo: what the Undo button takes back
    latest: null,
    undoButton: null,
    listDialog: null,
    requestDialog: null,
    countdown: null,
    refreshing: false,
    refreshAgain: false,

    // How a tier is shown: its mark, what going back to it is called, and what it means (the "i" button)
    TIERS: {
        GREEN: {mark: "✓", word: "Undo", means: "Only your own choice. Nobody is asked."},
        YELLOW: {mark: "~", word: "Revert", means: "Your opponent replays choices."},
        RED: {mark: "✕", word: "Revert", means: "Hidden information was revealed."}
    },

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
            $("#undoAutoDecline").change(function () {
                that.comm.setUndoAutoDecline($(this).prop("checked"), function () {
                }, function () {
                });
            });
        }

        this.pollNotices();
        this.refresh();
    },

    /* ---- what can be taken back just now ---- */

    // Asks the server again what the player may go back to.  The game screen calls this whenever the game has moved
    // on (gameUi.processXml); asked while an answer is on its way, it asks once more when that has come.
    refresh: function () {
        if (!this.started || this.reloading)
            return;
        if (this.refreshing) {
            this.refreshAgain = true;
            return;
        }
        var that = this;
        this.refreshing = true;
        var done = function () {
            that.refreshing = false;
            if (that.refreshAgain) {
                that.refreshAgain = false;
                that.refresh();
            }
        };
        this.comm.getRestorePoints(function (json) {
            that.policy = json.policy || {};
            var allowed = that.policy.green || that.policy.yellow || that.policy.red;
            if (!that.game.spectatorMode) {
                // The row is there only in a game where something can be taken back (not, say, a scenario that is
                // being written as it is played); the switch only where there is somebody who could ask
                $(".undoOptions").toggle(!!allowed);
                $("#undoAutoDecline, label[for='undoAutoDecline']").toggle(!!that.policy.asks);
                $("#undoAutoDecline").prop("checked", !!json.autoDecline);
                that.showUndoButton(json.pending == null && json.points.length > 0 ? json.points[0] : null);
            }
            // A request may have been waiting since before this page was loaded
            if (json.pending != null && that.requestDialog == null)
                that.showPending(json.pending);
            done();
        }, done);
    },

    showUndoButton: function (latest) {
        var that = this;
        this.latest = latest != null && latest.tier == "GREEN" ? latest : null;
        // (the alert box is drawn once the game has said who is playing, which may be after this has started)
        if (this.undoButton == null && this.game.alertBox != null) {
            this.undoButton = $("<button id='undoLast' class='undoLast'></button>").text(this.TIERS.GREEN.mark + " Undo").button().hide();
            this.undoButton.click(function () {
                that.undoLatest();
            });
            this.game.alertBox.append(this.undoButton);
        }
        if (this.undoButton == null)
            return;
        if (this.latest == null) {
            this.undoButton.hide();
            this.game.alertButtons.removeClass("besideUndo");
            return;
        }
        var off = this.latest.unavailable != null;
        this.undoButton.button(off ? "disable" : "enable")
            .attr("title", off ? this.latest.unavailable : "Undo: " + this.latest.label).show();
        // (a Done button on the right of the alert box moves over for it)
        this.game.alertButtons.addClass("besideUndo");
    },

    undoLatest: function () {
        if (this.latest == null || this.frozen)
            return;
        var that = this;
        this.undoButton.button("disable");
        // ownOnly: if the opponent has answered in the meantime this is no longer an undo, and nobody is to be asked
        // for a revert by a button that says Undo
        this.comm.requestUndo(this.latest.entryCount, true, function (json) {
            if (json.status == "refused") {
                that.showBanner(json.message, null);
                that.refresh();
            }
        }, function () {
            that.showBanner("Undo could not be sent", null);
            that.refresh();
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
                turn: parseInt(notice.turn), phase: notice.phase,
                reasons: notice.reasons == null || notice.reasons == "" ? [] : notice.reasons.split("; "),
                secondsLeft: parseInt(notice.seconds)
            });
        } else if (notice.type == "undoResolved") {
            this.closeRequest();
            if (notice.outcome != "done" && notice.message != null)
                this.showBanner(notice.message, null);
            this.refresh();
        }
    },

    /* ---- how a place to go back to is drawn ---- */

    // The coloured mark of a tier: colour, and a sign that says the same without it.
    tierMark: function (tier) {
        var look = this.TIERS[tier];
        return $("<span class='undoMark'></span>").addClass("undoMark-" + (tier || "").toLowerCase())
            .attr("data-tier", tier || "").text(look == null ? "" : look.mark);
    },

    // The "i" button and what it opens: what the three colours mean.
    tierHelp: function () {
        var help = $("<div class='undoHelp'></div>").hide();
        var asks = this.policy != null && this.policy.asks;
        for (var tier in this.TIERS) {
            var look = this.TIERS[tier];
            var line = $("<div></div>").append(this.tierMark(tier)).append($("<b></b>").text(" " + look.word + ". "));
            line.append($("<span></span>").text(look.means + (tier == "GREEN" || !asks ? "" : " Needs their approval.")));
            help.append(line);
        }
        var button = $("<button class='undoInfo' title='What the colours mean'>i</button>").click(function () {
            help.toggle();
        });
        return {button: button, panel: help};
    },

    // "Turn 2 · Fellowship"; what is before the first turn is the game being set up
    when: function (turn, phase) {
        return (turn > 0 ? "Turn " + turn : "Setup") + (phase == null || phase == "" ? "" : " · " + phase);
    },

    reasonsOf: function (reasons) {
        var why = $("<div class='undoWhy'></div>");
        for (var i = 0; i < reasons.length; i++)
            why.append($("<div></div>").append($("<b></b>").text(reasons[i])));
        return why;
    },

    /* ---- the list ---- */

    openList: function () {
        var that = this;
        this.comm.getRestorePoints(function (json) {
            that.closeList();
            that.policy = json.policy || {};
            var help = that.tierHelp();
            var content = $("<div class='undoList'></div>");
            content.append($("<div class='undoHead'></div>").append($("<span></span>").text("Go back to before:")).append(help.button));
            content.append(help.panel);
            if (json.pending != null) {
                content.append($("<div class='undoNote'></div>").text("A revert request is pending."));
            } else if (json.points.length == 0) {
                content.append($("<div class='undoNote'></div>").text("Nothing to go back to."));
            }
            var rows = $("<div class='undoPoints'></div>");
            var under = null;
            for (var i = 0; i < json.points.length && json.pending == null; i++) {
                (function (point) {
                    var when = that.when(point.turn, point.phase);
                    if (when != under) {
                        under = when;
                        rows.append($("<div class='undoWhen'></div>").text(when));
                    }
                    var look = that.TIERS[point.tier];
                    var row = $("<div class='undoPoint'></div>").attr("data-tier", point.tier);
                    var button = $("<button class='undoGo'></button>").addClass("undoGo-" + point.tier.toLowerCase())
                        .text(look.mark + " " + look.word);
                    if (point.unavailable != null)
                        button.prop("disabled", true);
                    button.click(function () {
                        that.ask(point.entryCount);
                    });
                    row.append(button);
                    row.append($("<span class='undoLabel'></span>").text(point.label));
                    if (point.reasons.length > 0)
                        row.append(that.reasonsOf(point.reasons));
                    if (point.unavailable != null)
                        row.append($("<div class='undoOff'></div>").text(point.unavailable));
                    rows.append(row);
                })(json.points[i]);
            }
            content.append(rows);

            that.listDialog = content.dialog({
                title: "Revert",
                width: 460,
                modal: false,
                dialogClass: "undoDialog",
                close: function () {
                    $(this).dialog("destroy").remove();
                    that.listDialog = null;
                }
            });
        }, function () {
            that.showBanner("The list could not be fetched", null);
        });
    },

    closeList: function () {
        if (this.listDialog != null)
            this.listDialog.dialog("close");
    },

    ask: function (entryCount) {
        var that = this;
        this.comm.requestUndo(entryCount, false, function (json) {
            that.closeList();
            // "done" needs nothing: the notice that the game was taken back loads the page again.
            // "pending" needs nothing either: the notice that a request is waiting shows it.
            if (json.status == "refused" && json.message != null)
                that.showBanner(json.message, null);
        }, function () {
            that.showBanner("The request could not be sent", null);
        });
    },

    /* ---- a request that is waiting ---- */

    showPending: function (pending) {
        var that = this;
        this.closeRequest();
        this.closeList();
        var mine = pending.requester == this.game.bottomPlayerId && !this.game.spectatorMode;
        var asked = !mine && !this.game.spectatorMode;
        var left = pending.secondsLeft;
        var help = this.tierHelp();

        var content = $("<div class='undoRequest'></div>").attr("data-tier", pending.tier);
        content.append($("<div class='undoHead'></div>")
            .append($("<span></span>").text(mine ? "You asked to revert to before:" : pending.requester + " asks to revert to before:")));
        content.append(help.panel);
        content.append($("<div class='undoTarget'></div>").append(this.tierMark(pending.tier))
            .append($("<span class='undoLabel'></span>").text(pending.label))
            .append($("<span class='undoWhen'></span>").text(this.when(pending.turn, pending.phase))));
        if (pending.reasons != null && pending.reasons.length > 0)
            content.append(this.reasonsOf(pending.reasons));
        var clock = $("<div class='undoClock'></div>");
        content.append(clock);

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
            buttons["Withdraw"] = reply("cancel");
        if (asked) {
            buttons["Approve"] = reply("approve");
            buttons["Decline"] = reply("decline");
            buttons["Decline all"] = reply("declineAll");
        }

        this.requestDialog = content.dialog({
            title: mine ? "Revert requested" : "Revert request",
            width: 420,
            modal: false,
            closeOnEscape: false,
            dialogClass: "undoDialog undoRequestDialog",
            buttons: buttons,
            close: function () {
                $(this).dialog("destroy").remove();
                that.requestDialog = null;
            }
        });
        // Nothing here is the default: the dialog itself has the focus, so that Enter does nothing and the first Tab
        // goes to the first button (Approve, for the player who is asked).  The "i" button comes after the answers,
        // in the same row, so that it is not what Tab reaches first
        var frame = this.requestDialog.closest(".ui-dialog");
        frame.find(".ui-dialog-buttonpane").append(help.button);
        frame.attr("tabindex", -1);
        frame.find("button").attr("title", function () {
            return $(this).text() == "Decline all" ? "Decline this and every later revert request in this game" : null;
        });
        setTimeout(function () {
            frame.focus();
        }, 0);

        var tick = function () {
            clock.text(left > 0 ? "Game paused · auto-declines in " + left + "s" : "Out of time");
            left--;
        };
        tick();
        this.countdown = setInterval(tick, 1000);
        if (!this.game.spectatorMode)
            this.freeze(true);
    },

    closeRequest: function () {
        if (this.countdown != null) {
            clearInterval(this.countdown);
            this.countdown = null;
        }
        if (this.requestDialog != null)
            this.requestDialog.dialog("close");
        this.freeze(false);
    },

    /* ---- the game standing still ---- */

    // While a request waits no decision can be answered: the server would refuse the answer, and a player who had
    // clicked would not know why nothing came of it.  Cards can still be looked at and the chat used.
    freeze: function (on) {
        this.frozen = on;
        $("body").toggleClass("undoFrozen", on);
        var answering = $(".alertButtons").add(this.game.smallDialog == null ? null : this.game.smallDialog.closest(".ui-dialog"))
            .add(this.game.cardActionDialog == null ? null : this.game.cardActionDialog.closest(".ui-dialog"));
        if (on)
            answering.attr("inert", "");
        else
            answering.removeAttr("inert");
        if (this.undoButton != null && on)
            this.undoButton.button("disable");
        $("#undoGame").button(on ? "disable" : "enable");
    },

    // A decision drawn while a request waits (one that was on its way) is stood still like the rest.
    decisionShown: function () {
        if (this.frozen)
            this.freeze(true);
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
            banner.append(this.tierMark(tier));
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
