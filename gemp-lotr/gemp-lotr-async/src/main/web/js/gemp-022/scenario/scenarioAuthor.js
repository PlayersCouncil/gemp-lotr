/*
 * The scenario authoring panel: a scenario written by playing it (see ScenarioAuthoring on the server).
 *
 * It is docked down the right-hand side of an otherwise ordinary game of the scenario.  The game is played as usual;
 * the panel shows the script as a timeline, lets every part of a step be edited (most of all its messages, which are
 * pointed at things by clicking on them), answers for the opponent where the script does not, changes the board, and
 * gets about in the timeline by having the script play itself to a step.
 *
 * The server holds the draft.  The panel keeps a copy of it as the scenario's file would have it, sends what it
 * changes (never the whole draft: a request is too small for that), and is given the draft back with where its game
 * stands.  A change the game in hand can take is on the screen at once; one it cannot take comes back as a fresh game,
 * and the page moves to it.
 *
 * The overlay makes one of these when the game says its player is its author, and calls in at:
 *
 *     gameMoved()              the game has sent something (a decision, a step): where it stands may have changed
 *     stepShown(step)          a step has just been put on the screen
 *     restart(fromCheckpoint)  the player asked to start again
 */
var GempScenarioAuthor = Class.extend({
    WIDTH: 430,
    FOLDED: 30,
    PHASES: ["fellowship", "shadow", "maneuver", "archery", "assignment", "skirmish", "regroup"],
    /** How long the path of a scenario's file may be (the server's ScenarioLibrary.MAX_PATH). */
    MAX_PATH: 120,
    TYPES: ["action", "actionChoice", "cards", "arbitrary", "choice", "number", "assign"],
    SEATS: ["freeps", "shadow", "human", "opponent"],
    ZONES: ["characters", "support", "minions", "hand", "deck.top", "deck.bottom", "discard", "deadPile", "removed"],
    /** Where a card can be, in the order the Board tab lists them, with what each is called there. */
    PLACED: [["characters", "Characters"], ["minions", "Minions"], ["support", "Support area"], ["attached", "On another card"],
        ["stacked", "Stacked on another card"], ["hand", "Hand"], ["deck", "Deck, top first"], ["discard", "Discard pile, top first"],
        ["deadPile", "Dead pile"], ["removed", "Removed from the game"]],
    /** The places whose cards come in an order that can be changed: order(seat.zone, card, card). */
    ORDERED: ["hand", "deck", "discard", "deadPile"],
    PARTS: ["strength", "vitality", "resistance", "siteNumber", "wounds", "cost", "title", "culture", "type", "text", "art", "stats", "signet", "shadowNumber"],
    PLACES: ["zone:self.hand", "zone:self.characters", "zone:self.support", "zone:opp.characters", "zone:opp.support",
        "zone:minions", "pile:self.deck", "pile:self.discard", "pile:self.deadPile", "pile:self.adventureDeck",
        "pile:self.removed", "pile:self.hand", "pile:opp.deck", "pile:opp.discard", "pile:opp.deadPile",
        "pile:opp.adventureDeck", "pile:opp.removed", "pile:opp.hand", "ui:twilightPool", "ui:phase", "ui:initiative",
        "ui:ruleof4", "ui:clock", "ui:advPath", "ui:alertBox", "ui:skirmish", "ui:skirmish:fpStrength",
        "ui:skirmish:shadowStrength", "ui:stats", "ui:stats:archery", "ui:stats:move", "ui:chat", "ui:settings",
        "ui:dialog", "ui:cardInfo", "ui:button:pass", "ui:button:done", "ui:button:reset"],
    TABS: [["timeline", "Timeline"], ["step", "Step"], ["opponent", "Opponent"], ["board", "Board"], ["scenario", "Scenario"]],

    game: null,
    overlay: null,
    /** False in a player's game of the draft (a test): the panel is then only the way back. */
    authoring: true,
    state: null,
    draft: null,
    /**
     * What the panel remembers from one game of the draft to the next: {tab, selected, selectedUid, follow, where,
     * folded}, and for the step selected (see settle): atUid, the step the game was last seen at; hold, the step
     * selected was asked for across a change and stays; jump, the step the game is at is to be selected whatever.
     */
    ui: null,
    /** The step being edited: {index, step (a working copy), sent, base}. */
    form: null,
    /** The message the author is working on, which is the one to show when its step is put up again: {step, index}. */
    focusMessage: null,
    requests: 0,

    init: function (game, overlay, authoring) {
        this.game = game;
        this.overlay = overlay;
        this.authoring = authoring;
        this.ui = this.recall();
        this.build();
        this.refresh();
    },

    /* ---------------------------------------------------------------- small things */

    el: function (tag, cls, text) {
        var elem = $("<" + tag + "></" + tag + ">");
        if (cls)
            elem.addClass(cls);
        if (text != null)
            elem.text(text);
        return elem;
    },

    button: function (label, title, action, cls) {
        var that = this;
        return this.el("button", "sa-button" + (cls ? " " + cls : ""), label).attr("type", "button").attr("title", title || "").click(function (event) {
            action.call(that, event);
            return false;
        });
    },

    select: function (options, value, changed) {
        var select = this.el("select", "sa-input");
        for (var i = 0; i < options.length; i++) {
            var option = $.isArray(options[i]) ? options[i] : [options[i], options[i]];
            select.append($("<option></option>").attr("value", option[0]).text(option[1]));
        }
        select.val(value == null ? "" : value);
        if (changed)
            select.change(function () {
                changed($(this).val());
            });
        return select;
    },

    field: function (label, control, hint) {
        var row = this.el("label", "sa-field");
        row.append(this.el("span", "sa-label", label).attr("title", hint || ""));
        row.append(control);
        return row;
    },

    /** A line of terms ("freeps, action, phase(fellowship)") as its terms; commas inside parentheses belong to their term. */
    splitTerms: function (text) {
        var parts = [], depth = 0, part = "";
        text = text == null ? "" : "" + text;
        for (var i = 0; i < text.length; i++) {
            var ch = text.charAt(i);
            if (ch == "(")
                depth++;
            if (ch == ")")
                depth--;
            if (ch == "," && depth <= 0) {
                parts.push($.trim(part));
                part = "";
            } else {
                part += ch;
            }
        }
        if ($.trim(part) != "")
            parts.push($.trim(part));
        return $.grep(parts, function (term) {
            return term != "";
        });
    },

    clone: function (value) {
        return value == null ? value : JSON.parse(JSON.stringify(value));
    },

    /** Whether somebody is in the middle of typing something into part of the panel. */
    typingIn: function (box) {
        var active = document.activeElement;
        if (active == null || !$.contains(box[0], active))
            return false;
        return active.tagName == "TEXTAREA" || (active.tagName == "INPUT" && (active.type == "text" || active.type == "number"));
    },

    recall: function () {
        var ui = null;
        try {
            ui = JSON.parse(sessionStorage.getItem("gemp-author-ui"));
        } catch (e) {
            ui = null;
        }
        ui = ui || {};
        if (ui.tab == null)
            ui.tab = "timeline";
        if (ui.follow == null)
            ui.follow = true;
        if (ui.where == null)
            ui.where = "now";
        return ui;
    },

    remember: function () {
        try {
            sessionStorage.setItem("gemp-author-ui", JSON.stringify(this.ui));
        } catch (e) {
            // only a convenience
        }
    },

    gameId: function () {
        return "" + getUrlParam("gameId");
    },

    /* ---------------------------------------------------------------- the panel */

    build: function () {
        var that = this;
        this.panel = this.el("div", "sa").attr("id", "scenarioAuthor").appendTo("body");
        var head = this.el("div", "sa-head").appendTo(this.panel);
        this.foldButton = this.button("\u00bb", "Fold the panel away", function () {
            that.ui.folded = !that.ui.folded;
            that.remember();
            that.dock();
        }, "sa-fold").appendTo(head);
        this.titleElem = this.el("span", "sa-title").appendTo(head);
        this.busyElem = this.el("span", "sa-busy", "\u2026").appendTo(head).hide();
        this.saveButton = this.button("Save", "Write the draft to its file", function () {
            that.save();
        }, "sa-save").appendTo(head);
        this.whereElem = this.el("div", "sa-where").appendTo(this.panel);
        this.notices = this.el("div", "sa-notices").appendTo(this.panel);

        this.tabBar = this.el("div", "sa-tabs").appendTo(this.panel);
        this.body = this.el("div", "sa-body").appendTo(this.panel);
        this.tabs = {};
        $.each(this.TABS, function (index, tab) {
            that.button(tab[1], "", function () {
                that.showTab(tab[0]);
            }, "sa-tabbutton sa-tabbutton-" + tab[0]).appendTo(that.tabBar);
            that.tabs[tab[0]] = that.el("div", "sa-tab sa-tab-" + tab[0]).appendTo(that.body).hide();
        });
        this.pickBox = this.el("div", "sa-pickbox").appendTo("body").hide();
        this.pickLabel = this.el("div", "sa-picklabel").appendTo("body").hide();

        //Typing in the panel is not for the game to hear
        this.panel.bind("keydown keyup keypress", function (event) {
            event.stopPropagation();
        });
        //The game moved on while a step was being typed in: its step is turned to once the typing has stopped
        this.tabs.step.bind("focusout", function () {
            if (that.followDue)
                setTimeout(function () {
                    that.catchUp();
                }, 300);
        });
        //The server is told when this page goes away, so that the Game Hall does not go on offering a draft that has
        // been saved and left.  (Going on to a fresh game of the draft is not going away; and a page that is only
        // being reloaded is heard from again as soon as it is back.)
        window.addEventListener("pagehide", function () {
            if (that.gone || that.state == null || !that.state.session || !navigator.sendBeacon)
                return;
            try {
                navigator.sendBeacon(that.game.communication.url + "/scenarios/author/left", new URLSearchParams({left: "true"}));
            } catch (e) {
                // only a courtesy
            }
        });
        //The game's own windows open in the middle of the browser's, which is partly under the panel
        $(document).bind("dialogopen dialogresizestop", function (event) {
            that.keepClear($(event.target).closest(".ui-dialog"));
        });
        $("body").addClass("scenario-authoring");
        this.dock();
    },

    keepClear: function (dialogs) {
        var limit = $(window).width() - (this.game.reservedRight || 0) - 6;
        dialogs.each(function () {
            var rect = this.getBoundingClientRect();
            if (rect.width > 0 && rect.right > limit)
                $(this).css("left", Math.max(0, $(this).offset().left - (rect.right - limit)));
        });
    },

    /** Makes the table give the panel its room. */
    dock: function () {
        //In a player's game of the draft the table has the screen to itself, as the player's will
        var floating = this.state != null && this.state.session && this.state.mode == "test";
        var width = floating ? 0 : this.ui.folded ? this.FOLDED : this.WIDTH;
        this.panel.toggleClass("sa-floating", floating);
        if (floating) {
            this.panel.removeClass("sa-folded").css("width", "");
            document.body.style.setProperty("--sa-width", "0px");
            this.game.reservedRight = 0;
            this.game.layoutUI(true);
            this.overlay.bubbleSituation = null;
            return;
        }
        this.panel.toggleClass("sa-folded", !!this.ui.folded).css("width", width);
        this.foldButton.text(this.ui.folded ? "\u00ab" : "\u00bb").attr("title", this.ui.folded ? "Open the authoring panel" : "Fold the panel away");
        document.body.style.setProperty("--sa-width", width + "px");
        this.game.reservedRight = width;
        this.game.layoutUI(true);
        this.overlay.bubbleSituation = null;
        this.keepClear($(".ui-dialog:visible"));
    },

    showTab: function (name) {
        this.ui.tab = name;
        this.remember();
        this.render();
    },

    busy: function (more) {
        this.requests += more ? 1 : -1;
        this.busyElem.toggle(this.requests > 0);
    },

    /* ---------------------------------------------------------------- talking to the server */

    call: function (method, what, data, done, failed) {
        var that = this;
        this.busy(true);
        this.game.communication.scenarioAuthor(method, what, data, function (result) {
            that.busy(false);
            //(what went wrong stays said until something the author does goes right: not until the next look at the state)
            if (method == "POST")
                that.problem = null;
            if (method == "POST" && result != null && result.gameId != null)
                that.handle(result);
            if (done)
                done.call(that, result);
            else if (method == "POST")
                that.renderNotices();
        }, function (xhr) {
            that.busy(false);
            var message = null;
            try {
                message = JSON.parse(xhr.responseText).error;
            } catch (e) {
                message = null;
            }
            that.problem = message || (xhr.status == 403 ? "Writing scenarios is not open to this account."
                : xhr.status == 0 ? "The server could not be reached." : "The server answered " + xhr.status + ".");
            that.renderNotices();
            if (failed)
                failed.call(that);
        });
    },

    /** What every change comes back with: a fresh game to go to, or the state of this one. */
    handle: function (result) {
        if (result.rebuilt && ("" + result.gameId) != this.gameId()) {
            this.go(result.gameId);
            return;
        }
        if (result.state != null)
            this.setState(result.state);
        if (result.events)
            this.game.pollOnce();
    },

    go: function (gameId) {
        this.remember();
        this.gone = true;
        //The game being left is given up by the server, and its ending is not news
        this.overlay.leaving = true;
        location.href = "/gemp-lotr/game.html?gameId=" + encodeURIComponent(gameId);
    },

    refresh: function () {
        var that = this;
        if (this.gone)
            return;
        this.call("GET", "state", null, function (state) {
            that.setState(state);
        });
    },

    gameMoved: function () {
        var that = this;
        clearTimeout(this.movedTimer);
        this.movedTimer = setTimeout(function () {
            //(a change of the panel's own is on its way back with the state in it)
            if (that.requests == 0)
                that.refresh();
        }, 250);
    },

    edit: function (patch, goTo, done, failed) {
        var data = {patch: JSON.stringify(patch)};
        if (goTo != null)
            data["goto"] = goTo;
        this.call("POST", "edit", data, done, failed);
    },

    goTo: function (step) {
        var that = this;
        this.flush(function () {
            that.call("POST", "goto", {step: step});
        });
    },

    /** Sends whatever of the step being edited has not been sent, and then does something (if that went through). */
    flush: function (then) {
        if (this.form != null && this.dirty().any)
            this.sendStep(then);
        else
            then.call(this);
    },

    restart: function (fromCheckpoint) {
        if (this.state == null || !this.state.session)
            return false;
        if (!this.authoring) {
            this.startTest(this.state.testFrom || 1);
            return true;
        }
        this.goTo(fromCheckpoint ? this.lastCheckpoint(true) : 1);
        return true;
    },

    startTest: function (step) {
        var that = this;
        this.flush(function () {
            that.call("POST", "test", {step: step});
        });
    },

    /** The last checkpoint step before where the game is (or, at one, that one); 1 if there is none. */
    lastCheckpoint: function (inclusive) {
        var steps = (this.draft && this.draft.steps) || [];
        var at = this.state.position.step;
        for (var i = Math.min(steps.length, inclusive ? at : at - 1) - 1; i >= 0; i--)
            if (steps[i].checkpoint)
                return i + 1;
        return 1;
    },

    /* ---------------------------------------------------------------- state */

    setState: function (state) {
        //The same as last time: nothing is drawn again, and nothing anybody was in the middle of is disturbed
        var text = JSON.stringify(state);
        var same = text == this.stateText;
        this.stateText = text;
        this.state = state;
        if (!state.session) {
            this.draft = null;
            this.render();
            return;
        }
        this.draft = state.draft;
        this.draft.steps = this.draft.steps || [];
        if (same)
            return;
        var steps = this.draft.steps;
        var uids = state.uids || [];
        if ((state.mode == "test") != this.panel.hasClass("sa-floating"))
            this.dock();
        this.settle();
        this.panel.toggleClass("sa-wanted", state.waiting != null && state.waiting.opponent != null && !!state.waiting.opponent.held);
        //Somebody has to have asked for the Opponent tab to be given it; but it is where to look when it is waiting
        var held = state.waiting != null && state.waiting.opponent != null && state.waiting.opponent.held;
        if (held && !this.wasHeld && this.authoring)
            this.flash("opponent");
        this.wasHeld = held;
        this.render();
    },

    /**
     * Which step is selected.  That is the author's to say, until the game comes to another step: then the step the
     * game is at is selected, so that the Step tab is about what is on the table unless it has been turned to
     * something else since the table last changed.
     */
    settle: function () {
        var state = this.state, ui = this.ui;
        var steps = this.draft.steps, uids = state.uids || [];
        //The step selected is known by its name: where it comes changes when the game writes a step in before it
        if (ui.selectedUid != null && $.inArray(ui.selectedUid, uids) > -1)
            ui.selected = $.inArray(ui.selectedUid, uids) + 1;
        var at = state.position.step;
        //(the step in hand; or one that repeats and is between its decisions; or, failing those, the last one played)
        var current = steps.length == 0 ? null : Math.max(1, Math.min(state.position.inHand || state.position.underWay ? at : at - 1, steps.length));
        var currentUid = current == null ? null : uids[current - 1];
        var lost = ui.selected == null || ui.selected > steps.length;
        var due = false;
        if (ui.hold && !lost) {
            //(asked for by the change that moved the game: moving a step, taking one out)
            ui.hold = false;
        } else if (lost || ui.jump || (ui.follow && currentUid != ui.atUid)) {
            //Nobody's place is taken from under them while they are typing in it, or choosing something on the
            // table for it: the game's step waits until they have done
            if (!lost && !ui.jump && this.engaged())
                due = true;
            else
                ui.selected = current;
        }
        this.followDue = due;
        if (!due) {
            ui.atUid = currentUid;
            ui.jump = false;
        }
        ui.selectedUid = ui.selected == null ? null : uids[ui.selected - 1];
        this.remember();
    },

    /** Whether the author is in the middle of something in the Step tab that turning to another step would lose. */
    engaged: function () {
        return this.ui.tab == "step" && (this.picking != null || this.typingIn(this.tabs.step));
    },

    /** Turns to the step the game came to while the author was busy with another, now that they are not. */
    catchUp: function () {
        if (!this.followDue || this.state == null || !this.state.session || this.draft == null || this.engaged())
            return;
        this.settle();
        this.render();
    },

    flash: function (tab) {
        var button = this.tabBar.find(".sa-tabbutton-" + tab);
        button.addClass("sa-attention");
    },

    aliasOfCard: function (cardId) {
        var cards = (this.state && this.state.board && this.state.board.cards) || [];
        for (var i = 0; i < cards.length; i++)
            if (("" + cards[i].cardId) == ("" + cardId))
                return cards[i].alias;
        return null;
    },

    cardMap: function () {
        var map = {};
        var cards = (this.state && this.state.board && this.state.board.cards) || [];
        for (var i = 0; i < cards.length; i++)
            map[cards[i].alias] = cards[i].cardId;
        return map;
    },

    aliases: function () {
        return $.map((this.state && this.state.board && this.state.board.cards) || [], function (card) {
            return card.alias;
        });
    },

    isHumanSeat: function (seat) {
        return seat == "human" || seat == (this.draft.human || "freeps");
    },

    /* ---------------------------------------------------------------- drawing */

    render: function () {
        var state = this.state;
        this.panel.find(".sa-tabbutton").removeClass("sa-active");
        if (state == null)
            return;
        if (!state.session || ("" + state.gameId) != this.gameId()) {
            this.renderElsewhere();
            return;
        }
        if (state.mode == "test") {
            this.renderTest();
            return;
        }
        this.tabBar.show();
        this.saveButton.show();
        //Changes that are in no file are said where nobody can miss them
        this.titleElem.text((state.dirty ? "* " : "") + (this.draft.title || this.draft.id))
            .attr("title", state.dirty ? "Changed since it was saved" : "");
        this.panel.toggleClass("sa-unsaved", !!state.dirty);
        this.saveButton.toggleClass("sa-dirty", !!state.dirty);
        var position = state.position;
        var count = this.draft.steps.length;
        var where = position.over ? "The game is over" : position.frontier ? "Past the end of the script (" + count + " steps)"
            : "Step " + position.step + " of " + count + (position.inHand ? ""
                : position.underWay ? " (which repeats, and is under way)" : " (waiting to come to it)");
        this.whereElem.text(where + (position.over ? "" : " \u00b7 turn " + position.turn + ", " + position.phase));
        this.renderNotices();

        this.tabBar.find(".sa-tabbutton-" + this.ui.tab).addClass("sa-active").removeClass("sa-attention");
        for (var name in this.tabs)
            if (this.tabs.hasOwnProperty(name))
                this.tabs[name].toggle(name == this.ui.tab);
        if (this.ui.tab == "timeline")
            this.renderTimeline();
        else if (this.ui.tab == "step")
            this.renderStep();
        else if (this.ui.tab == "opponent")
            this.renderOpponent();
        else if (this.ui.tab == "board")
            this.renderBoard();
        else if (this.ui.tab == "scenario")
            this.renderScenario();
    },

    renderNotices: function () {
        var that = this;
        var box = this.notices.empty();
        if (this.problem != null) {
            var line = this.el("div", "sa-notice sa-error").appendTo(box);
            $.each(this.problem.split("\n"), function (index, text) {
                line.append(that.el("div", null, text));
            });
            line.append(this.button("\u00d7", "Dismiss", function () {
                that.problem = null;
                that.renderNotices();
            }, "sa-dismiss"));
        }
        var state = this.state;
        if (state == null || !state.session)
            return;
        //What the file had wrong with it when it was opened, and was opened without
        if ((state.leftOut || []).length > 0)
            box.append(this.leftOutNotice(state.leftOut));
        //A decision nobody has written a step for is said first, with the way to write one
        var open = this.uncovered();
        if (open != null)
            box.append(this.uncoveredNotice(open));
        //(where the script stopped is what that says, when that is why it stopped)
        if (state.notice && state.notice != this.noticeRead && !(open != null && state.stopped == "uncovered")) {
            var notice = this.el("div", "sa-notice sa-warning", state.notice).appendTo(box);
            var at = state.position;
            //Stopped at a step it could not play: the step can be given its answer by doing it
            if (state.stopped == "step" && at.inHand && !at.over && !(this.draft.steps[at.step - 1] || {}).pause)
                notice.append(this.button("Take its answer from what I do next", "Step " + at.step + " forgets what it allows and how it is answered, and takes both from what you do next in the game. Its messages stay.", function () {
                    that.retake();
                }, "sa-inline"));
            notice.append(this.button("\u00d7", "Dismiss", function () {
                that.noticeRead = state.notice;
                that.renderNotices();
            }, "sa-dismiss"));
        }
        //What has gone wrong with the script in this game.  Once read it is not said again; what goes wrong next is.
        var problems = (state.problems || []).slice(this.problemsRead || 0);
        if (problems.length > 0)
            box.append(this.el("div", "sa-notice sa-warning", problems[problems.length - 1]
                + (problems.length > 1 ? " (and " + (problems.length - 1) + " more before it)" : "")).append(this.button("\u00d7", "Dismiss", function () {
                that.problemsRead = (state.problems || []).length;
                that.renderNotices();
            }, "sa-dismiss")));
        //How the last save went; no longer news once the draft has been changed again
        if (this.saved != null && state.dirty && !this.saved.stays)
            this.saved = null;
        if (this.saved != null) {
            var saved = this.el("div", "sa-notice " + (this.saved.good ? "sa-good" : "sa-warning"), this.saved.text).appendTo(box);
            if (this.saved.why && this.ui.tab != "scenario")
                saved.append(this.button("See why", "The whole report, under Scenario", function () {
                    that.saved = null;
                    that.showTab("scenario");
                }, "sa-inline"));
            saved.append(this.button("\u00d7", "Dismiss", function () {
                that.saved = null;
                that.renderNotices();
            }, "sa-dismiss"));
        }
        if (this.told)
            box.append(this.el("div", "sa-notice sa-told", this.told));
    },

    /**
     * The file had mistakes in it, and was opened with the mistaken parts left out: each of them, what was wrong with
     * it and what it said, with the way to the step it was in.  It stays until it is put away: what the parts said is
     * in the file only until the draft is saved over it.
     */
    leftOutNotice: function (leftOut) {
        var that = this;
        var uids = this.state.uids || [];
        var box = this.el("div", "sa-notice sa-error sa-leftout");
        box.append(this.el("div", "sa-leftout-head", (leftOut.length == 1 ? "One part of the file was" : leftOut.length + " parts of the file were")
            + " mistaken, and " + (leftOut.length == 1 ? "has" : "have") + " been left out so that it could be opened."
            + (this.state.dirty ? " Saving writes the file without " + (leftOut.length == 1 ? "it" : "them") + "." : "")));
        $.each(leftOut, function (index, one) {
            var row = that.el("div", "sa-leftout-row").appendTo(box);
            var at = one.uid == null ? -1 : $.inArray(one.uid, uids);
            var where = one.where;
            //(a step is said by where it is now; the file's number for it only if that is another)
            if (at > -1)
                where = "step " + (at + 1) + where.replace(/^step \d+( \([^)]*\))?/, "") + (one.step != at + 1 ? " (step " + one.step + " of the file)" : "");
            else if (one.step > 0)
                where += " of the file" + (/^step \d+( \([^)]*\))?$/.test(one.where) ? ", left out whole" : "");
            row.append(that.el("span", "sa-leftout-where", where));
            row.append(document.createTextNode(": " + one.message));
            if (at > -1)
                row.append(that.button("Show", "Go to step " + (at + 1), function () {
                    that.ui.tab = "step";
                    that.selectStep(at + 1);
                }, "sa-inline"));
            row.append(that.el("pre", "sa-leftout-was", one.was));
        });
        box.append(this.button("\u00d7", "Put this away for good. What the parts said is not kept anywhere else once the draft is saved.", function () {
            that.call("POST", "settings", {seenLeftOut: true});
        }, "sa-dismiss"));
        return box;
    },

    /** What was left out of a step when the file was opened: [{where, message, was}]. */
    leftOutOf: function (uid) {
        return $.grep((this.state && this.state.leftOut) || [], function (one) {
            return uid != null && one.uid == uid;
        });
    },

    /** Whether the draft has anything in it that is in no file (and this is the game it is being written in). */
    unsaved: function () {
        var state = this.state;
        return this.authoring && !this.gone && state != null && !!state.session && state.mode != "test" && !!state.dirty
            && ("" + state.gameId) == this.gameId();
    },

    /** Asked on the way back to the Game Hall with unsaved changes: save them, drop them, or stay. */
    askBeforeLeaving: function () {
        var that = this;
        $(".sa-leave, .sa-leave-shade").remove();
        var shade = this.el("div", "sa-leave-shade").appendTo("body");
        var box = this.el("div", "sa-leave").appendTo("body");
        box.append(this.el("h2", null, "You have unsaved changes."));
        box.append(this.el("p", null, (this.draft.title || this.draft.id) + " has changes that are not in its file"
            + (this.state.path ? " (" + this.state.path + ")." : ".")));
        var said = this.el("p", "sa-leave-problem").appendTo(box).hide();
        var buttons = this.el("div", "sa-leave-buttons").appendTo(box);
        var put = function () {
            shade.remove();
            box.remove();
        };
        var failed = function () {
            buttons.find("button").prop("disabled", false);
            said.text(that.problem || "That could not be done.").show();
        };
        var away = function () {
            that.call("POST", "close", {}, function () {
                put();
                that.gone = true;
                that.overlay.goToHall();
            }, failed);
        };
        buttons.append(this.button("Save and exit", "Write the draft to its file, and go back to the Game Hall", function () {
            if (!that.mayOverwrite())
                return;
            buttons.find("button").prop("disabled", true);
            that.flush(function () {
                that.call("POST", "save", {}, away, failed);
            });
        }, "sa-primary"));
        buttons.append(this.button("Discard changes", "Go back to the Game Hall. The file stays as it was last saved; what has been changed since is lost.", function () {
            buttons.find("button").prop("disabled", true);
            away();
        }, "sa-danger"));
        buttons.append(this.button("Stay here", "Carry on writing", put));
    },

    /** The decision that is waiting with no step to cover it, where that is something to do something about. */
    uncovered: function () {
        var state = this.state;
        var open = state == null || !state.session ? null : state.uncovered;
        if (open == null || state.mode == "test" || state.position.over || state.position.frontier)
            return null;
        if ((open.seat == "human" ? state.waiting.human : state.waiting.opponent) == null)
            return null;
        //A scenario that leaves its player free between steps has such decisions as a matter of course: there it is
        // only said where the script, playing itself, could get no further
        if (this.draft.coverage == "guided" && open.seat == "human" && state.stopped != "uncovered")
            return null;
        return open;
    },

    /** What is said about it: what is being asked, the button that writes a step for it, and what that will do. */
    uncoveredNotice: function (open) {
        var that = this;
        var state = this.state;
        var asked = open.seat == "human" ? state.waiting.human : state.waiting.opponent;
        var box = this.el("div", "sa-notice sa-uncovered");
        box.append(this.el("div", "sa-uncovered-head", state.stopped == "uncovered"
            ? "The script stopped here: no step covers this decision." : "No step covers this decision."));
        var offers = $.map(asked.options || [], function (option) {
            return option.term || option.alias || option.label;
        });
        box.append(this.el("div", null, (open.seat == "human" ? "You are" : "The opponent is") + " being asked \"" + asked.text + "\" ("
            + asked.where.replace(/_/g, " ") + ")" + (offers.length == 0 ? (asked.pass ? ". It offers nothing but Pass." : ".")
                : ". It offers: " + offers.slice(0, 4).join(", ") + (offers.length > 4 ? ", and " + (offers.length - 4) + " more" : "") + (asked.pass ? ", or Pass." : "."))));
        if (open.problem != null) {
            box.append(this.el("div", "sa-uncovered-how", "A step cannot be added for it as things stand: " + open.problem));
            return box;
        }
        var how = open.reuses != null ? "Step " + open.reuses + ", which says nothing yet, becomes the step for it."
            : open.repeating != null ? "It goes in after step " + open.repeating + ". That step repeats until \"" + open.until + "\"; that becomes \""
                + open.becomes + "\", so that it ends where the new step begins."
            : open.after == 0 ? "It goes in at the start of the script." : "It goes in after step " + open.after + ".";
        var row = this.el("div", "sa-uncovered-how").appendTo(box);
        row.append(this.button("Add a step for this decision", "Writes a step in the right place, waiting for exactly this decision. What is then done in the game becomes its answer; messages can be put on it first.", function () {
            that.addStepHere();
        }, "sa-primary"));
        row.append(document.createTextNode(how));
        box.append(this.el("div", "sa-uncovered-or", open.seat == "opponent"
            ? "Or answer for it under Opponent: with \"write it into the script\" ticked, that writes the same step."
            : state.recording ? "Or just answer it in the game: that writes the same step, with what you did as what it allows. Add the step first if it is to say something before it is answered."
                : "\"Write down what I do\" is off, so answering it in the game writes nothing."));
        return box;
    },

    /** A step for the decision that is waiting, written where the script stands; the Step tab turns to it. */
    addStepHere: function () {
        var that = this;
        this.flush(function () {
            that.ui.jump = true;
            that.ui.tab = "step";
            that.form = null;
            that.call("POST", "here", {});
        });
    },

    /** The step in hand takes what it allows, and its answer, from what its author does next. */
    retake: function () {
        var that = this;
        this.flush(function () {
            that.form = null;
            that.call("POST", "retake", {});
        });
    },

    isAwaiting: function (uid) {
        return uid != null && $.inArray(uid, (this.state && this.state.awaiting) || []) > -1;
    },

    renderElsewhere: function () {
        var that = this;
        this.tabBar.hide();
        this.saveButton.hide();
        this.panel.removeClass("sa-unsaved");
        this.titleElem.text("Scenario authoring").attr("title", "");
        this.whereElem.text("");
        this.renderNotices();
        for (var name in this.tabs)
            if (this.tabs.hasOwnProperty(name))
                this.tabs[name].hide();
        var box = this.tabs.timeline.empty().show();
        if (!this.state.session) {
            box.append(this.el("p", "sa-note", "No draft is open. This game was a draft's, and the draft has been closed (or the server has been restarted). Open a scenario for editing from the Game Hall."));
            return;
        }
        box.append(this.el("p", "sa-note", "The draft has moved on to a fresh game."));
        box.append(this.button("Go to it", "", function () {
            that.go(that.state.gameId);
        }));
    },

    renderTest: function () {
        var that = this;
        this.tabBar.hide();
        this.saveButton.hide();
        this.titleElem.text(this.draft.title || this.draft.id);
        this.whereElem.text("A player's game of the draft, from step " + (this.state.testFrom || 1));
        this.renderNotices();
        for (var name in this.tabs)
            if (this.tabs.hasOwnProperty(name))
                this.tabs[name].hide();
        var box = this.tabs.timeline.empty().show();
        box.append(this.el("p", "sa-note", "This is what a player sees: nothing is written down, the opponent does what the script and its fallback say, and leaving the script ends the game the way the scenario says it should."));
        box.append(this.button("Back to writing", "Start a game for writing again, at the step this test began", function () {
            that.call("POST", "resume", {});
        }, "sa-primary"));
        box.append(this.button("Test again", "The same test from the same step", function () {
            that.startTest(that.state.testFrom || 1);
        }));
    },

    /* ---------------------------------------------------------------- the timeline */

    summary: function (step) {
        var when = typeof step.when == "string" ? step.when : (step.when != null ? "{...}" : "");
        var seat = null, rest = [];
        var terms = this.splitTerms(when);
        for (var i = 0; i < terms.length; i++) {
            var term = terms[i], match;
            if ($.inArray(term.toLowerCase(), this.SEATS) > -1)
                seat = term.toLowerCase();
            else if ((match = /^phase\((.*)\)$/i.exec(term)) != null)
                rest.push(match[1]);
            else if ((match = /^turn\((.*)\)$/i.exec(term)) != null)
                rest.push("t" + match[1]);
            else
                rest.push(term);
        }
        var who = step.pause || this.isOpening(step) ? "Read" : seat == null ? "Any" : this.isHumanSeat(seat) ? "You" : "Opp";
        var act = "";
        if (this.isOpening(step))
            act = step["do"] || "";
        else if (step.answer)
            act = step.answer;
        else if (step.allow)
            act = typeof step.allow == "string" ? step.allow : (step.allow.answers || "pass when it may");
        return {who: who, when: rest.join(" "), act: act};
    },

    /** Whether a step is the one that is read before the first turn, on the board as it is laid out. */
    isOpening: function (step) {
        return step != null && typeof step.when == "string" && $.trim(step.when).toLowerCase() == "opening";
    },

    messagesOf: function (step) {
        if (step.messages == null)
            return [];
        return $.isArray(step.messages) ? step.messages : [step.messages];
    },

    renderTimeline: function () {
        var that = this;
        var state = this.state, steps = this.draft.steps, position = state.position;
        var box = this.tabs.timeline;
        var scroll = box.find(".sa-steps").scrollTop();
        box.empty();

        var bar = this.el("div", "sa-bar").appendTo(box);
        bar.append(this.button("\u23ee Start", "A fresh game from step 1", function () {
            that.goTo(1);
        }));
        bar.append(this.button("\u2691 Checkpoint", "A fresh game, played by the script up to the last checkpoint before here", function () {
            that.goTo(that.lastCheckpoint(false));
        }));
        bar.append(this.button("\u25c0 Back one", "A fresh game, played by the script up to the step before this one", function () {
            that.goTo(Math.max(1, position.step - 1));
        }));
        bar.append(this.button("\u21bb This step", "A fresh game, played by the script up to the step the game is at now", function () {
            that.goTo(position.step);
        }));
        bar.append(this.button("\u23ed End", "A fresh game, with the whole script played", function () {
            that.goTo(steps.length + 1);
        }));

        var options = this.el("div", "sa-options").appendTo(box);
        var check = function (label, title, value, changed) {
            var input = $("<input type='checkbox'/>").prop("checked", !!value).change(function () {
                changed($(this).prop("checked"));
            });
            return that.el("label", "sa-check").attr("title", title).append(input).append(document.createTextNode(" " + label));
        };
        options.append(check("Write down what I do", "Where the script has nothing to say, what you do (and what you have the opponent do) becomes steps", state.recording, function (on) {
            that.call("POST", "settings", {recording: on});
        }));
        options.append(check("Ask me for the opponent", "Where the script does not answer for the opponent, it waits for you (see the Opponent tab). Otherwise it does what its orders and its fallback say.", state.askOpponent, function (on) {
            that.call("POST", "settings", {askOpponent: on});
        }));
        options.append(check("Follow the game", "When the game comes to another step, that step is selected (and the Step tab turns to it)", this.ui.follow, function (on) {
            that.ui.follow = on;
            that.ui.jump = on;
            that.settle();
            that.render();
        }));
        if ((state.flags || []).length > 0) {
            var way = this.el("div", "sa-way").appendTo(box);
            way.append(this.el("span", "sa-label", "At the script's forks, choose").attr("title", "Which of a pause's choices the script takes when it plays itself to a step. Without one, it takes the first."));
            $.each(state.flags, function (index, flag) {
                way.append(check(flag, "", $.inArray(flag, state.way) > -1, function (on) {
                    var chosen = $.grep(state.way, function (name) {
                        return name != flag;
                    });
                    if (on)
                        chosen.push(flag);
                    that.call("POST", "settings", {way: chosen.join(",")});
                }));
            });
        }

        var list = this.el("div", "sa-steps").appendTo(box);
        $.each(steps, function (index, step) {
            var number = index + 1;
            var summary = that.summary(step);
            var row = that.el("div", "sa-step sa-status-" + (state.statuses[index] || "unreached")).appendTo(list);
            row.toggleClass("sa-current", number == position.step && !position.over && !position.frontier);
            row.toggleClass("sa-selected", number == that.ui.selected);
            row.append(that.el("span", "sa-num", number));
            row.append(that.el("span", "sa-who sa-who-" + summary.who.toLowerCase(), summary.who));
            var what = that.el("span", "sa-what").appendTo(row);
            what.append(that.el("span", "sa-when", summary.when));
            if (summary.act != "")
                what.append(that.el("span", "sa-act", summary.act));
            if (step.id)
                what.append(that.el("span", "sa-id", step.id));
            var count = that.messagesOf(step).length;
            var tags = that.el("span", "sa-tags").appendTo(row);
            if (step["if"])
                tags.append(that.el("span", "sa-tag", "if " + step["if"]));
            if (step.unless)
                tags.append(that.el("span", "sa-tag", "unless " + step.unless));
            if (step["do"])
                tags.append(that.el("span", "sa-tag", "do").attr("title", step["do"]));
            if (count > 0)
                tags.append(that.el("span", "sa-tag sa-bubbles", "\u25ac" + count).attr("title", count + " message" + (count == 1 ? "" : "s")));
            if (step.checkpoint)
                tags.append(that.el("span", "sa-tag sa-flag", "\u2691").attr("title", "A checkpoint"));
            if (step.end)
                tags.append(that.el("span", "sa-tag", "end"));
            if (that.isAwaiting(state.uids[index]))
                tags.append(that.el("span", "sa-tag sa-noanswer", "no answer yet").attr("title", "It says nothing yet about what is done. When the game comes to it, what you do there is written in."));
            var mistaken = that.leftOutOf(state.uids[index]);
            if (mistaken.length > 0)
                tags.append(that.el("span", "sa-tag sa-mistake", "left out").attr("title", $.map(mistaken, function (one) {
                    return one.where.replace(/^step \d+( \([^)]*\))?\.?/, "") + ": " + one.message + "\nIt said: " + one.was;
                }).join("\n\n")));
            row.append(that.button("\u25b6", "A fresh game, played by the script up to this step", function () {
                that.goTo(number);
            }, "sa-go"));
            row.click(function () {
                that.ui.selected = number;
                that.ui.selectedUid = state.uids[index];
                that.ui.tab = "step";
                that.remember();
                that.render();
            });
        });

        var end = this.el("div", "sa-end").appendTo(list);
        if (position.over) {
            end.text("The game is over" + (position.result ? " (" + position.result.replace("_", " ") + ")" : "") + ". Go back to a step to carry on.");
        } else if (position.frontier) {
            end.addClass("sa-current");
            end.append(this.el("div", null, "The script ends here. What you do from here on " + (state.recording ? "is written down." : "is not written down.")));
        }
        var waiting = state.waiting || {};
        var unscripted = !position.over && !position.inHand && (waiting.human != null || (waiting.opponent != null && waiting.opponent.held));
        if (unscripted) {
            var asked = waiting.human != null ? waiting.human : waiting.opponent;
            end.append(this.el("div", "sa-asked", (waiting.human != null ? "You are" : "The opponent is") + " being asked: " + asked.text + " (" + asked.where + ")"));
            end.append(this.button("Add a step for this decision", "A step to hang messages on before the decision is answered; its answer is filled in when it is given", function () {
                that.addStepHere();
            }, "sa-primary"));
        }

        var foot = this.el("div", "sa-bar").appendTo(box);
        if (!this.isOpening(steps[0]))
            foot.append(this.button("+ Opening", "A first step that is read before the first turn begins, on the board as the scenario lays it out: nothing drawn, nothing asked. What it does (drawing a hand, say) it does once it has been read.", function () {
                that.insertStep(0, {when: "opening", messages: [{text: "..."}]});
            }));
        foot.append(this.button("+ Pause", "A step that is only read, put in before the selected step (or at the end)", function () {
            var index = that.ui.selected == null ? steps.length : that.ui.selected - 1;
            that.insertStep(index, {pause: true, messages: [{text: "..."}]});
        }));
        foot.append(this.button("+ Step after", "A step after the selected one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows. (After the step the game is at, with a decision waiting that no step covers, it is the step for that decision.)", function () {
            var index = that.ui.selected == null ? steps.length : that.ui.selected;
            //Put in where the script stands, with a decision waiting that nothing covers: that decision is what it is for
            var open = state.uncovered;
            if (open != null && open.problem == null && open.reuses == null && index == open.after && !position.over) {
                that.addStepHere();
                return;
            }
            that.insertStep(index, {when: (that.draft.human || "freeps") + ", action"}, true);
        }));
        foot.append(this.button("Rewrite from here", "Take the step the game is at, and every step after it, out of the script, and play on from here writing new ones", function () {
            var from = position.step;
            if (from > steps.length || !confirm("Take steps " + from + " to " + steps.length + " out of the script, messages and all, and write them again by playing?"))
                return;
            var ops = [];
            for (var i = steps.length - 1; i >= from - 1; i--)
                ops.push({op: "delete", uid: state.uids[i]});
            that.ui.jump = true;
            that.changeSteps(ops, from, null);
        }, "sa-danger").prop("disabled", position.step > steps.length || position.over));
        foot.append(this.button("Test from here", "A player's game of the draft from the selected step: no panel, no author", function () {
            that.startTest(that.ui.selected || 1);
        }));
        foot.append(this.button("Test from the start", "A player's game of the draft", function () {
            that.startTest(1);
        }));

        //The list is brought to where the game is when the game has moved; otherwise it stays where it was put
        var seenAt = position.step + "/" + position.inHand + "/" + this.gameId();
        var current = list.find(".sa-current").first();
        if (this.ui.follow && seenAt != this.timelineAt && current.length > 0)
            list.scrollTop(Math.max(0, current[0].offsetTop - list[0].offsetTop - 60));
        else
            list.scrollTop(scroll);
        this.timelineAt = seenAt;
    },

    /** A change to which steps there are.  One at or before where the game is brings a fresh game, at the step changed. */
    changeSteps: function (ops, firstChanged, select) {
        var that = this;
        var goTo = firstChanged <= this.state.position.step ? Math.max(1, firstChanged) : null;
        if (select != null) {
            this.ui.selected = select;
            //(whichever step ends up there: its name is learnt from what comes back)
            this.ui.selectedUid = null;
            this.ui.hold = true;
        }
        this.form = null;
        this.remember();
        this.edit({steps: ops}, goTo);
    },

    /** @param awaiting whether the step is to take its answer from what is done when the game comes to it */
    insertStep: function (index, step, awaiting) {
        var that = this;
        this.flush(function () {
            that.ui.tab = "step";
            var op = {op: "insert", before: that.state.uids[index] || null, step: step};
            if (awaiting)
                op.awaiting = true;
            that.changeSteps([op], index + 1, index + 1);
        });
    },

    /* ---------------------------------------------------------------- one step */

    /** A step as the form works on it: every part there, messages as objects, 'allow' taken apart. */
    working: function (step) {
        var that = this;
        var work = this.clone(step);
        work.messages = $.map(this.messagesOf(step), function (message) {
            return [typeof message == "string" ? {text: message} : that.clone(message)];
        });
        //(lists of things on the table are edited as the line of text a file usually has them as)
        $.each(work.messages, function (index, message) {
            $.each(["anchor", "highlight", "avoid"], function (i, name) {
                if ($.isArray(message[name]))
                    message[name] = message[name].join(", ");
            });
            if (typeof message.gap == "number")
                message.gap = "" + message.gap;
        });
        if (work.allow != null && typeof work.allow == "string")
            work.allow = {answers: work.allow};
        work.allow = work.allow || {};
        if (work.end != null && typeof work.end == "string")
            work.end = {result: work.end};
        if (work.end != null)
            work.end.messages = $.map(this.messagesOf(work.end), function (message) {
                return [typeof message == "string" ? {text: message} : that.clone(message)];
            });
        return work;
    },

    /** The working copy as the step its file would have. */
    written: function (work) {
        var blank = function (value) {
            return value == null || value === "" || value === false;
        };
        var step = {};
        var tidy = function (message) {
            var out = {};
            for (var name in message)
                if (message.hasOwnProperty(name) && !blank(message[name])) {
                    if (name == "choices") {
                        var choices = {}, any = false;
                        for (var choice in message.choices)
                            if (message.choices.hasOwnProperty(choice) && choice != "") {
                                choices[choice] = message.choices[choice];
                                any = true;
                            }
                        if (any)
                            out.choices = choices;
                    } else {
                        out[name] = message[name];
                    }
                }
            if (out.text == null)
                out.text = "";
            return out;
        };
        $.each(["id", "note", "checkpoint", "pause", "if", "unless", "when", "until", "skipWhen", "do"], function (index, name) {
            if (!blank(work[name]))
                step[name] = work[name];
        });
        if (work.messages.length > 0)
            step.messages = $.map(work.messages, function (message) {
                return [tidy(message)];
            });
        var allow = work.allow || {};
        var hints = {}, anyHint = false;
        for (var alias in (allow.hints || {}))
            if (allow.hints.hasOwnProperty(alias) && alias != "" && !blank(allow.hints[alias])) {
                hints[alias] = allow.hints[alias];
                anyHint = true;
            }
        if (!blank(allow.passWhen) || !blank(allow.otherwise) || anyHint) {
            step.allow = {};
            if (!blank(allow.answers))
                step.allow.answers = allow.answers;
            if (!blank(allow.passWhen))
                step.allow.passWhen = allow.passWhen;
            if (!blank(allow.otherwise))
                step.allow.otherwise = allow.otherwise;
            if (anyHint)
                step.allow.hints = hints;
        } else if (!blank(allow.answers)) {
            step.allow = allow.answers;
        }
        if (!blank(work.answer))
            step.answer = work.answer;
        if (work.end != null && !blank(work.end.result)) {
            if (work.end.messages != null && work.end.messages.length > 0)
                step.end = {result: work.end.result, messages: $.map(work.end.messages, function (message) {
                    return [tidy(message)];
                })};
            else
                step.end = work.end.result;
        }
        return step;
    },

    renderStep: function () {
        var box = this.tabs.step;
        var steps = this.draft.steps;
        var number = this.ui.selected;
        if (number == null || number > steps.length) {
            this.form = null;
            box.empty().append(this.el("p", "sa-note", steps.length == 0
                ? "The script has no steps yet. Play: what you do becomes steps. Or write a step for the decision in front of you (Timeline)."
                : "Select a step in the timeline."));
            return;
        }
        var index = number - 1;
        var uid = this.state.uids[index];
        var base = JSON.stringify(steps[index]);
        if (this.form != null && this.form.uid == uid && box.children().length > 0) {
            //The same step.  The form is left as it is unless the step has been changed from somewhere else (the
            // game giving it its answer, say).  Then whatever of the form has not been sent goes first, and the
            // form is made again from the step as it now is.
            this.form.index = index;
            if (this.form.base == base || this.form.sending) {
                this.renderStepHead();
                return;
            }
            if (this.dirty().any) {
                this.renderStepHead();
                this.sendStep();
                return;
            }
        } else if (this.form != null && this.dirty().any) {
            //Another step is being turned to: what was not sent of this one still goes
            this.sendStep();
        }
        this.makeForm(index);
    },

    /** Makes the form for a step afresh, leaving whoever was typing in one of its messages where they were. */
    makeForm: function (index) {
        var box = this.tabs.step;
        var caret = null;
        var active = document.activeElement;
        if (active != null && $.contains(box[0], active) && $(active).hasClass("sa-text"))
            caret = {message: box.find("textarea.sa-text").index(active), at: active.selectionStart};
        var step = this.draft.steps[index];
        var work = this.working(step);
        this.form = {uid: this.state.uids[index], index: index, step: work, origin: this.written(work), base: JSON.stringify(step)};
        this.buildStepForm();
        if (caret != null) {
            var area = box.find("textarea.sa-text").eq(caret.message);
            if (area.length > 0) {
                area.focus();
                try {
                    area[0].setSelectionRange(caret.at, caret.at);
                } catch (e) {
                    // the end will do
                }
            }
        }
    },

    /** The parts of the step in the form that are not as they were last sent (or first found): {set, unset, any}. */
    dirty: function () {
        var form = this.form;
        var result = {set: {}, unset: [], any: false};
        if (form == null)
            return result;
        var now = this.written(form.step);
        var name;
        for (name in now)
            if (now.hasOwnProperty(name) && JSON.stringify(now[name]) != JSON.stringify(form.origin[name])) {
                result.set[name] = now[name];
                result.any = true;
            }
        for (name in form.origin)
            if (form.origin.hasOwnProperty(name) && !now.hasOwnProperty(name)) {
                result.unset.push(name);
                result.any = true;
            }
        return result;
    },

    /** The form has been changed: what changed is sent, a moment after the last change. */
    stepChanged: function (now) {
        var that = this;
        clearTimeout(this.stepTimer);
        if (this.form == null)
            return;
        this.stepTimer = setTimeout(function () {
            that.sendStep();
        }, now ? 0 : 700);
    },

    /**
     * Sends the parts of the step that have been changed in the form, and only those: the rest is the server's,
     * which may know more about the step than this page does (the answer the game has just written into it).
     * @param then what to do once they have gone through (and at once if there is nothing to send)
     */
    sendStep: function (then) {
        var that = this;
        var form = this.form;
        clearTimeout(this.stepTimer);
        if (form == null) {
            if (then)
                then.call(this);
            return;
        }
        if (form.sending) {
            //One at a time: what has changed since goes when this one is back
            form.again = true;
            if (then)
                (form.after = form.after || []).push(then);
            return;
        }
        var changes = this.dirty();
        var text = JSON.stringify(changes);
        if (!changes.any || text == form.refused) {
            if (then && !changes.any)
                then.call(this);
            return;
        }
        var sent = this.written(form.step);
        form.sending = true;
        var finished = function (went) {
            form.sending = false;
            var after = form.after || [];
            form.after = null;
            if (form.again) {
                form.again = false;
                if (that.form === form) {
                    that.sendStep(function () {
                        $.each(after, function (i, each) {
                            each.call(that);
                        });
                    });
                    return;
                }
            }
            if (went)
                $.each(after, function (i, each) {
                    each.call(that);
                });
        };
        this.edit({steps: [{op: "merge", uid: form.uid, set: changes.set, unset: changes.unset}]}, null, function () {
            var name;
            for (name in changes.set)
                if (changes.set.hasOwnProperty(name))
                    form.origin[name] = sent[name];
            $.each(changes.unset, function (i, gone) {
                delete form.origin[gone];
            });
            form.refused = null;
            if (that.form === form && that.draft != null) {
                var at = $.inArray(form.uid, that.state.uids || []);
                if (at > -1) {
                    form.index = at;
                    form.base = JSON.stringify(that.draft.steps[at]);
                    //Anything else about the step that is not as the form has it was changed somewhere else
                    var theirs = that.written(that.working(that.draft.steps[at]));
                    var mine = that.written(form.step);
                    var stale = false;
                    for (name in theirs)
                        if (theirs.hasOwnProperty(name) && !changes.set.hasOwnProperty(name) && JSON.stringify(theirs[name]) != JSON.stringify(mine[name]))
                            stale = true;
                    for (name in mine)
                        if (mine.hasOwnProperty(name) && !theirs.hasOwnProperty(name) && !changes.set.hasOwnProperty(name))
                            stale = true;
                    if (stale && !form.again && !that.dirty().any && that.ui.tab == "step")
                        that.makeForm(at);
                }
                that.renderNotices();
            }
            finished(true);
            if (then)
                then.call(that);
        }, function () {
            //Refused: it stays unsent (and is not sent again as it is).  If several parts went together, each is
            // tried by itself, so that one that is wrong does not hold the others back.
            var parts = [];
            var name;
            for (name in changes.set)
                if (changes.set.hasOwnProperty(name))
                    parts.push({set: name});
            $.each(changes.unset, function (i, gone) {
                parts.push({unset: gone});
            });
            if (parts.length < 2 || that.form !== form) {
                form.refused = text;
                finished(false);
                return;
            }
            var said = that.problem;
            var next = function (i) {
                if (i >= parts.length) {
                    form.refused = JSON.stringify(that.dirty());
                    if (that.problem == null)
                        that.problem = said;
                    that.renderNotices();
                    finished(false);
                    return;
                }
                var op = {op: "merge", uid: form.uid, set: {}, unset: []};
                if (parts[i].set != null)
                    op.set[parts[i].set] = changes.set[parts[i].set];
                else
                    op.unset.push(parts[i].unset);
                that.edit({steps: [op]}, null, function () {
                    if (parts[i].set != null)
                        form.origin[parts[i].set] = sent[parts[i].set];
                    else
                        delete form.origin[parts[i].unset];
                    next(i + 1);
                }, function () {
                    said = that.problem;
                    next(i + 1);
                });
            };
            next(0);
        });
    },

    /** An input that reads and writes one part of an object, and says so when it is changed. */
    bound: function (object, name, kind, changed) {
        var that = this;
        var input;
        if (kind == "area")
            input = this.el("textarea", "sa-input").attr("rows", 3);
        else if (kind == "check")
            input = $("<input type='checkbox'/>");
        else
            input = $("<input type='text'/>").addClass("sa-input");
        if (kind == "check")
            input.prop("checked", !!object[name]);
        else
            input.val(object[name] == null ? "" : (typeof object[name] == "string" ? object[name] : JSON.stringify(object[name])));
        var read = function (now) {
            var value = kind == "check" ? input.prop("checked") : input.val();
            if (kind == "json" && $.trim(value) != "") {
                //(something written as { ... } is kept as what it is, not as its text)
                try {
                    value = /^\s*[\[{]/.test(value) ? JSON.parse(value) : value;
                    input.removeClass("sa-bad");
                } catch (e) {
                    input.addClass("sa-bad");
                    return;
                }
            }
            object[name] = value;
            if (changed)
                changed(value);
            that.stepChanged(now);
        };
        if (kind == "check") {
            input.change(function () {
                read(true);
            });
        } else {
            input.bind("input", function () {
                read(false);
            }).change(function () {
                read(true);
            });
        }
        return input;
    },

    renderStepHead: function () {
        var form = this.form;
        if (form == null || this.stepHead == null)
            return;
        var number = form.index + 1;
        var position = this.state.position;
        var status = this.state.statuses[form.index] || "unreached";
        var where = number == position.step && position.inHand ? "in hand now" : status == "matched" ? "played"
            : status == "bypassed" ? "not on this way through" : status == "skipped" ? "skipped" : "not reached";
        if (this.isAwaiting(form.uid))
            where += ", no answer yet";
        this.stepHead.find(".sa-stepname").text("Step " + number + " of " + this.draft.steps.length);
        this.stepHead.find(".sa-stepwhere").text(where).attr("class", "sa-stepwhere sa-status-" + status);
    },

    buildStepForm: function () {
        var that = this;
        var form = this.form, work = form.step, index = form.index, number = index + 1;
        var steps = this.draft.steps;
        var box = this.tabs.step.empty();

        var head = this.stepHead = this.el("div", "sa-stephead").appendTo(box);
        head.append(this.button("\u25c0", "The step before", function () {
            that.selectStep(number - 1);
        }).prop("disabled", number <= 1));
        head.append(this.el("span", "sa-stepname"));
        head.append(this.button("\u25b6", "The step after", function () {
            that.selectStep(number + 1);
        }).prop("disabled", number >= steps.length));
        head.append(this.el("span", "sa-stepwhere"));
        this.renderStepHead();

        var acts = this.el("div", "sa-bar").appendTo(box);
        acts.append(this.button("\u21bb Play to here", "A fresh game, played by the script up to this step", function () {
            that.goTo(number);
        }));
        var uids = this.state.uids;
        acts.append(this.button("\u2191", "Move this step earlier", function () {
            that.flush(function () {
                that.changeSteps([{op: "move", uid: form.uid, before: uids[index - 1]}], number - 1, number - 1);
            });
        }).prop("disabled", index == 0));
        acts.append(this.button("\u2193", "Move this step later", function () {
            that.flush(function () {
                that.changeSteps([{op: "move", uid: form.uid, before: uids[index + 2] || null}], number, number + 1);
            });
        }).prop("disabled", index >= steps.length - 1));
        acts.append(this.button("Delete", "Take this step out of the script", function () {
            if (confirm("Take step " + number + " out of the script?"))
                that.changeSteps([{op: "delete", uid: form.uid}], number, Math.max(1, number - 1));
        }, "sa-danger"));
        var inHandNow = number == this.state.position.step && this.state.position.inHand;
        if (inHandNow && !work.pause && !this.isOpening(work))
            acts.append(this.button("Answer it again", "This step forgets what it allows and how it is answered, and takes both from what you do next. Its messages stay.", function () {
                that.retake();
            }));
        $.each(this.leftOutOf(form.uid), function (at, one) {
            var part = one.where.replace(/^step \d+( \([^)]*\))?\.?/, "");
            var note = that.el("div", "sa-note sa-mistaken").appendTo(box);
            note.append(that.el("div", null, "Left out when the file was opened" + (part == "" ? "" : ", from '" + part + "'") + ": " + one.message));
            note.append(that.el("pre", "sa-leftout-was", one.was));
        });
        if (this.isAwaiting(form.uid))
            box.append(this.el("div", "sa-note sa-awaiting", inHandNow
                ? "This step says nothing yet about what is done. Do it in the game now, and what you do is written in as what it allows. Messages put on it here first are shown before it is answered."
                : "This step says nothing yet about what is done. When the game comes to it, what you do there is written in as what it allows."));

        /* ---- what it waits for ---- */
        var opening = this.isOpening(work);
        var when = this.el("div", "sa-section").appendTo(box);
        when.append(this.el("div", "sa-heading", opening ? "The opening" : work.pause ? "Read over the player's decision when it is" : "Applies to the decision that is"));
        if (opening) {
            when.append(this.el("div", "sa-note", "Read before the first turn begins, on the board as the scenario lays it out: nobody has been asked anything, and nothing has been drawn. "
                + "Its messages are read one after another; then what it does is done; then the first turn begins. Only a scenario's first step can be this."));
            when.append(this.field("name", this.bound(work, "id", "text").addClass("sa-short"), "Only to make reports easier to read"));
        } else if (work.when != null && typeof work.when != "string") {
            when.append(this.field("When", this.bound(work, "when", "json"), "Written out in full because it has requirements"));
        } else {
            var raw = this.bound(work, "when", "text", function () {
                syncWhen();
            });
            var terms = function () {
                return that.splitTerms(work.when);
            };
            var has = function (test) {
                var found = null;
                $.each(terms(), function (i, term) {
                    var match = test(term);
                    if (match != null && found == null)
                        found = match;
                });
                return found;
            };
            var put = function (test, term) {
                var kept = $.grep(terms(), function (existing) {
                    return test(existing) == null;
                });
                if (term != null && term != "")
                    kept.push(term);
                //(seat, kind, phase, turn: the order a person would say them in)
                var rank = function (t) {
                    var low = t.toLowerCase();
                    return $.inArray(low, that.SEATS) > -1 ? 0 : $.inArray(t, that.TYPES) > -1 ? 1 : /^phase\(/i.test(t) ? 2
                        : /^turn\(/i.test(t) ? 3 : /^skirmish\(/i.test(t) ? 4 : 5;
                };
                kept.sort(function (a, b) {
                    return rank(a) - rank(b);
                });
                work.when = kept.join(", ");
                raw.val(work.when);
                that.stepChanged(true);
            };
            var plain = function (names) {
                return function (term) {
                    return $.inArray(term.toLowerCase(), $.map(names, function (n) {
                        return n.toLowerCase();
                    })) > -1 ? term : null;
                };
            };
            var call = function (name) {
                return function (term) {
                    var match = new RegExp("^" + name + "\\((.*)\\)$", "i").exec(term);
                    return match == null ? null : match[1];
                };
            };
            var seat = this.select([["", "either seat"]].concat(this.SEATS), "", function (value) {
                put(plain(that.SEATS), value);
            });
            var type = this.select([["", "any kind"]].concat(this.TYPES), "", function (value) {
                put(plain(that.TYPES), value);
            });
            var phase = this.select([["", "any phase"]].concat(this.PHASES).concat([["between_turns", "between turns"]]), "", function (value) {
                put(call("phase"), value == "" ? null : "phase(" + value + ")");
            });
            var turn = $("<input type='number' min='1'/>").addClass("sa-input sa-short").attr("placeholder", "turn").change(function () {
                put(call("turn"), $(this).val() == "" ? null : "turn(" + $(this).val() + ")");
            });
            var syncWhen = function () {
                var found = has(plain(that.SEATS));
                seat.val(found == null ? "" : found.toLowerCase());
                found = has(plain(that.TYPES));
                type.val(found == null ? "" : $.grep(that.TYPES, function (t) {
                    return t.toLowerCase() == found.toLowerCase();
                })[0]);
                phase.val(has(call("phase")) || "");
                turn.val(has(call("turn")) || "");
            };
            syncWhen();
            when.append(this.el("div", "sa-row").append(seat).append(type).append(phase).append(turn));
            when.append(this.field("When", raw, "seat, kind of decision, phase(...), turn(...), skirmish(n), offers(card), text(part of what the decision says)"));
        }
        var flagsRow = this.el("div", "sa-row").appendTo(when).toggle(!opening);
        flagsRow.append(this.el("label", "sa-check").attr("title", "A step that is only read: it has messages, and no answer. The decision goes to the step after it.")
            .append(this.bound(work, "pause", "check", function () {
                that.sendStep();
            })).append(document.createTextNode(" pause")));
        flagsRow.append(this.el("label", "sa-check").attr("title", "A player who fails later can come back to here")
            .append(this.bound(work, "checkpoint", "check")).append(document.createTextNode(" checkpoint")));
        flagsRow.append(this.field("name", this.bound(work, "id", "text").addClass("sa-short"), "Only to make reports easier to read"));
        if ((this.state.flags || []).length > 0 && !opening) {
            when.append(this.field("If", this.bound(work, "if", "text"), "Only part of the game if these were chosen at a pause's choice: " + this.state.flags.join(", ")));
            when.append(this.field("Unless", this.bound(work, "unless", "text"), "Not part of the game if any of these was chosen"));
        }

        /* ---- what happens ---- */
        if (opening) {
            var after = this.el("div", "sa-section").appendTo(box);
            after.append(this.el("div", "sa-heading", "What is done once it has been read"));
            after.append(this.field("Do", this.bound(work, "do", "text").attr("placeholder", "draw(5)"),
                "Changes to the board, made after the opening's messages and before the first turn begins, so that the first decision is worked out with them: "
                + "draw(n) or draw(seat, n) from the top of the deck, move(card, seat.zone), attach(card, bearer), stack(card, host), wound(card, n), burdens(n). "
                + "(Twilight set here is gone again when the turn starts.)"));
        } else if (!work.pause) {
            var does = this.el("div", "sa-section").appendTo(box);
            does.append(this.el("div", "sa-heading", "What is done"));
            does.append(this.field("Allow", this.bound(work.allow, "answers", "text"),
                "What the player may do: play(card), use(card), choose(card, card), option(text), number(n), assign(character: minion + minion), pass. Several, separated by commas; a ? after one that may not be on offer. Empty: anything."));
            does.append(this.field("Answer", this.bound(work, "answer", "text"),
                "What the script plays: the opponent's answer, or the player's when the script plays itself"));
            does.append(this.field("Do", this.bound(work, "do", "text"),
                "Changes to the board when the step's decision arrives: move(card, seat.zone), draw(n), wound(card, n), heal(card, n), twilight(n), burdens(n), attach(card, bearer), stack(card, host). "
                + "The decision itself was worked out before them: what they put in a hand can be played from the next decision on."));
            var more = $("<details class='sa-more'></details>").appendTo(does);
            more.append($("<summary></summary>").text("Wrong clicks, repeating, giving up"));
            more.append(this.field("Otherwise", this.bound(work.allow, "otherwise", "area").attr("rows", 2), "What to say when the player clicks something legal that is not allowed. Empty: the stock line."));
            more.append(this.field("Pass when", this.bound(work.allow, "passWhen", "json"), "Requirements (as on a card) that have to hold before Pass is allowed"));
            work.allow.hints = work.allow.hints || {};
            var hintBox = this.el("div", "sa-hints").appendTo(more);
            var drawHints = function () {
                hintBox.empty().append(that.el("div", "sa-label", "What to say about a click on one particular card"));
                $.each(work.allow.hints, function (alias) {
                    var row = that.el("div", "sa-row").appendTo(hintBox);
                    row.append(that.el("span", "sa-alias", alias));
                    row.append(that.bound(work.allow.hints, alias, "text"));
                    row.append(that.button("\u00d7", "Take this hint out", function () {
                        delete work.allow.hints[alias];
                        drawHints();
                        that.stepChanged(true);
                    }));
                });
                var add = that.select([["", "+ a hint for\u2026"]].concat(that.aliases()), "", function (alias) {
                    if (alias != "" && work.allow.hints[alias] == null) {
                        work.allow.hints[alias] = "";
                        drawHints();
                    }
                });
                hintBox.append(add);
            };
            drawHints();
            more.append(this.field("Until", this.bound(work, "until", "json"), "Keeps applying to decisions until one arrives that fits this"));
            more.append(this.field("Skip when", this.bound(work, "skipWhen", "json"), "Given up on if a decision that fits this arrives first"));
            if (work.allow.passWhen != null || work.allow.otherwise || work.until != null || work.skipWhen != null)
                more.attr("open", "open");
        }

        /* ---- what it says ---- */
        var says = this.el("div", "sa-section").appendTo(box);
        says.append(this.el("div", "sa-heading", "Messages"));
        this.messageBox = this.el("div", "sa-messages").appendTo(says);
        this.drawMessages(work.messages, this.messageBox, false);

        /* ---- the rest ---- */
        var rest = this.el("div", "sa-section").appendTo(box);
        work.end = work.end || {result: "", messages: []};
        var endBox = this.el("div", "sa-messages");
        rest.append(this.field("Ends the scenario", this.select([["", "no"], ["complete", "yes: complete"], ["fail", "yes: failed"]], work.end.result || "", function (value) {
            work.end.result = value;
            endBox.toggle(value != "");
            that.stepChanged(true);
        }), "Once this step has been answered (or, for a pause, read)"));
        rest.append(endBox);
        this.drawMessages(work.end.messages, endBox, true);
        endBox.toggle((work.end.result || "") != "");
        rest.append(this.field("Note", this.bound(work, "note", "area").attr("rows", 2), "For whoever works on the scenario next. Never shown to a player."));
    },

    selectStep: function (number) {
        if (number < 1 || number > this.draft.steps.length)
            return;
        this.ui.selected = number;
        this.ui.selectedUid = this.state.uids[number - 1];
        this.remember();
        //(what has not been sent of the step being left goes as the form for the other is made)
        this.render();
    },

    /**
     * The cards a step's messages are edited on.
     * @param closing whether these are the messages that close the scenario, which are shown all together and
     *                point at nothing
     */
    drawMessages: function (messages, box, closing) {
        var that = this;
        var form = this.form, work = form.step, index = form.index, number = index + 1;
        var steps = this.draft.steps;
        box.empty();
        $.each(messages, function (position, message) {
            var card = that.el("div", "sa-message").appendTo(box);
            var focus = function () {
                if (!closing)
                    that.focusMessage = {step: number, index: position};
            };
            card.bind("focusin", focus);

            var top = that.el("div", "sa-messagehead").appendTo(card);
            top.append(that.el("span", "sa-messagenum", (position + 1) + "."));
            if (!closing) {
                top.append(that.button("Show", "Put this message on the screen as it stands (when its step is the one in hand)", function () {
                    focus();
                    that.showOnScreen();
                }));
                top.append(that.button("\u25c0 Earlier", "Move this message to the step before", function () {
                    that.moveMessage(position, index - 1);
                }).prop("disabled", index == 0));
                top.append(that.button("Later \u25b6", "Move this message to the step after", function () {
                    that.moveMessage(position, index + 1);
                }).prop("disabled", index >= steps.length - 1));
                top.append(that.button("Before\u2026", "Take this message (and the ones before it in this step) out into a pause of their own, read before this step's decision is answered", function () {
                    that.splitToPause(position);
                }));
            }
            top.append(that.button("\u2191", "Earlier in this step", function () {
                messages.splice(position - 1, 0, messages.splice(position, 1)[0]);
                that.drawMessages(messages, box, closing);
                that.stepChanged(true);
            }).prop("disabled", position == 0));
            top.append(that.button("\u2193", "Later in this step", function () {
                messages.splice(position + 1, 0, messages.splice(position, 1)[0]);
                that.drawMessages(messages, box, closing);
                that.stepChanged(true);
            }).prop("disabled", position >= messages.length - 1));
            top.append(that.button("\u00d7", "Delete this message", function () {
                messages.splice(position, 1);
                that.drawMessages(messages, box, closing);
                that.stepChanged(true);
            }, "sa-danger"));

            card.append(that.bound(message, "text", "area").attr("rows", 4).addClass("sa-text")
                .attr("placeholder", "What it says. **bold**, [[Card Name]] or [[alias]], [gondor], (2) for twilight, {strength:alias} and the like for numbers from the game."));
            if (closing) {
                card.append(that.field("Heading", that.bound(message, "header", "text")));
                return;
            }

            var anchor = that.bound(message, "anchor", "text");
            var anchorRow = that.el("div", "sa-row sa-anchor").appendTo(card);
            anchorRow.append(that.el("span", "sa-label", "Points at").attr("title", "What the bubble sits beside and points at. Several things, separated by commas, and it points at each of them. Empty: the middle of the screen."));
            anchorRow.append(anchor);
            anchorRow.append(that.button("Pick", "Click the thing on the table", function () {
                focus();
                that.pick(function (picked) {
                    anchor.val(picked).change();
                    //Usually what is pointed at is what is outlined
                    if ($.trim(highlight.val()) == "")
                        highlight.val(picked).change();
                });
            }));
            anchorRow.append(that.button("+", "Click another thing on the table for it to point at as well", function () {
                focus();
                that.pick(function (picked) {
                    var have = that.splitTerms(anchor.val());
                    if ($.inArray(picked, have) < 0)
                        have.push(picked);
                    anchor.val(have.join(", ")).change();
                });
            }));
            anchorRow.append(that.partSelect(anchor));

            //Which side of it, and how far off: tried on the screen as they are changed, without waiting to be sent
            var stockGap = (that.draft.options || {}).bubbleGap || that.overlay.STOCK_GAP;
            var placeRow = that.el("div", "sa-row sa-anchor").appendTo(card);
            placeRow.append(that.el("span", "sa-label", "Sits").attr("title", "Where the bubble goes, of what it points at"));
            placeRow.append(that.select([["", "wherever fits best"], ["right", "to its right"], ["left", "to its left"], ["above", "above it"], ["below", "below it"]], message.side || "", function (value) {
                message.side = value;
                that.tryOnScreen(position, "side", value);
                that.stepChanged(true);
            }).attr("title", "Which side of what it points at the bubble goes on. A side named here is the side it goes on, whatever else that covers; left to itself it goes where it covers least."));
            placeRow.append(that.el("span", "sa-label sa-inline", "gap").attr("title", "How far from what it points at"));
            var gap = that.bound(message, "gap", "text", function (value) {
                if (/^\s*-?\d+(\.\d+)?\s*(px)?\s*$/i.test(value) || $.trim(value) == "")
                    that.tryOnScreen(position, "gap", $.trim(value));
            }).addClass("sa-short").attr("placeholder", stockGap)
                .attr("title", "How far from what it points at the bubble sits. A plain number is hundredths of the window's shorter side, so it keeps its proportion to the cards on any screen; 20px is pixels whatever the screen. "
                    + "Empty: the scenario's own (" + stockGap + "). Up and down arrows change it a little at a time.");
            gap.keydown(function (event) {
                if (event.keyCode != 38 && event.keyCode != 40)
                    return;
                var written = $.trim(gap.val()) == "" ? "" + stockGap : $.trim(gap.val()).toLowerCase();
                var pixels = written.indexOf("px") > -1;
                var amount = parseFloat(written);
                if (isNaN(amount))
                    return;
                amount += (event.keyCode == 38 ? 1 : -1) * (pixels ? 2 : 0.25);
                gap.val((Math.round(amount * 100) / 100) + (pixels ? "px" : "")).trigger("input");
                event.preventDefault();
            });
            placeRow.append(gap);

            var highlight = that.bound(message, "highlight", "text");
            var highlightRow = that.el("div", "sa-row sa-anchor").appendTo(card);
            highlightRow.append(that.el("span", "sa-label", "Outlines").attr("title", "Things to draw an outline around, separated by commas"));
            highlightRow.append(highlight);
            highlightRow.append(that.button("+ Pick", "Click a thing on the table to add it", function () {
                focus();
                that.pick(function (picked) {
                    var have = that.splitTerms(highlight.val());
                    if ($.inArray(picked, have) < 0)
                        have.push(picked);
                    highlight.val(have.join(", ")).change();
                });
            }));
            highlightRow.append(that.select([["", "gold"], "red", "green", "blue"], message.highlightStyle || "", function (value) {
                message.highlightStyle = value;
                that.stepChanged(true);
            }));

            var more = $("<details class='sa-more'></details>").appendTo(card);
            more.append($("<summary></summary>").text("Heading, what takes it away, a card or a picture shown large"));
            more.append(that.field("Heading", that.bound(message, "header", "text"), "Most messages have none"));
            var avoid = that.bound(message, "avoid", "text");
            var avoidRow = that.el("div", "sa-row sa-anchor").appendTo(more);
            avoidRow.append(that.el("span", "sa-label", "Keeps clear of").attr("title", "Things the bubble must not cover"));
            avoidRow.append(avoid);
            avoidRow.append(that.button("+ Pick", "Click a thing on the table to add it", function () {
                that.pick(function (picked) {
                    var have = that.splitTerms(avoid.val());
                    if ($.inArray(picked, have) < 0)
                        have.push(picked);
                    avoid.val(have.join(", ")).change();
                });
            }));
            more.append(that.field("Goes away on", that.bound(message, "advance", "text").attr("placeholder", "next, decision, inspect, inspect(card), close"),
                "Empty: Next, except the step's last message, which stays up while the decision is answered. inspect: the player opens a card's information window; close: they close it."));
            if (!work.pause && !that.isOpening(work))
                more.append(that.field("Shown when", that.bound(message, "on", "text").attr("placeholder", "picked(card), assigned(character), ready"),
                    "A moment inside the answering: the player has clicked that card; that character has all its minions; the answer is complete and only Done remains. Empty: when the decision arrives."));
            //What is shown large, the parts of it the message is about, and how much larger than fits it is drawn.
            // The last two do nothing without the first, and say so.
            var large = that.bound(message, "zoom", "text", function () {
                syncLarge();
            }).attr("placeholder", "card:alias, site:3, blueprint:1_83, image:file.png");
            var largeRow = that.el("div", "sa-row sa-anchor").appendTo(more);
            largeRow.append(that.el("span", "sa-label", "Shows large").attr("title", "A card, or a picture, shown as large as the window allows while the message is up, with the bubble beside it. "
                + "card:alias is one of the scenario's cards, site:3 a site on the path, blueprint:1_83 any card at all, image:file.png a picture from the server's images/scenarios folder."));
            largeRow.append(large);
            var pictures = that.el("div", "sa-pictures").hide();
            largeRow.append(that.button("Picture" + "\u2026", "Choose from the pictures in the server's images/scenarios folder", function () {
                that.choosePicture(pictures, function (name) {
                    large.val("image:" + name).change();
                });
            }));
            more.append(pictures);
            var parts = that.bound(message, "focus", "text");
            more.append(that.field("Its parts", parts,
                "The parts of what is shown large that the message is about: each is outlined, and the rest is dimmed. On a card: " + that.PARTS.join(", ")
                + ". On anything, a picture included: rect(left, top, width, height), in percent of it. Several, separated by commas."));
            var magnified = that.bound(message, "magnify", "text").addClass("sa-short");
            more.append(that.field("Magnified", magnified,
                "How many times larger than fits the window it is drawn: 2 is twice as large, up to 6; empty (or 1) is the whole of it. "
                + "It is seen through a frame the size the whole would have been, with Its parts in the middle of the frame (its own middle, if there are none)."));
            var syncLarge = function () {
                var none = $.trim(large.val()) == "";
                parts.attr("placeholder", none ? "(nothing is shown large)" : "strength, text, rect(l, t, w, h)").prop("disabled", none && $.trim(parts.val()) == "");
                magnified.attr("placeholder", none ? "(nothing)" : "1").prop("disabled", none && $.trim(magnified.val()) == "");
            };
            syncLarge();
            if (work.pause && position == messages.length - 1) {
                message.choices = message.choices || {};
                var choiceBox = that.el("div", "sa-hints").appendTo(more);
                var drawChoices = function () {
                    choiceBox.empty().append(that.el("div", "sa-label", "Ends in a choice (buttons in place of Next; later steps can say 'if' and 'unless')"));
                    $.each(message.choices, function (name) {
                        var row = that.el("div", "sa-row").appendTo(choiceBox);
                        row.append(that.el("span", "sa-alias", name));
                        row.append(that.bound(message.choices, name, "text"));
                        row.append(that.button("\u00d7", "Take this button out", function () {
                            delete message.choices[name];
                            drawChoices();
                            that.stepChanged(true);
                        }));
                    });
                    var name = $("<input type='text'/>").addClass("sa-input sa-short").attr("placeholder", "name");
                    var row = that.el("div", "sa-row").appendTo(choiceBox).append(name);
                    row.append(that.button("+ button", "A choice needs at least two", function () {
                        var value = $.trim(name.val());
                        if (/^[A-Za-z][A-Za-z0-9_-]*$/.test(value) && message.choices[value] == null) {
                            message.choices[value] = value;
                            drawChoices();
                        }
                    }));
                };
                drawChoices();
            }
            more.append(that.field("Note", that.bound(message, "note", "text"), "Never shown to a player"));
            if (message.header || message.side || message.avoid || message.advance || message.on || message.zoom || message.note
                || (message.choices != null && !$.isEmptyObject(message.choices)))
                more.attr("open", "open");
        });
        box.append(this.button("+ Message", "Another message on this step", function () {
            messages.push({text: ""});
            that.drawMessages(messages, box, closing);
            box.find(".sa-message").last().find("textarea").first().focus();
        }, "sa-primary"));
    },

    /** Beside an anchor's input: says which part of the card is meant, when the anchor is a card. */
    partSelect: function (input) {
        var that = this;
        var current = function () {
            //(one card: of several things, each is written out in full)
            var parts = $.trim(input.val()).split(":");
            return parts[0] == "card" && parts.length >= 2 && input.val().indexOf(",") < 0 ? parts : null;
        };
        var select = this.select([["", "the whole card"]].concat(this.PARTS), "", function (part) {
            var parts = current();
            if (parts == null)
                return;
            input.val("card:" + parts[1] + (part == "" ? "" : ":" + part)).change();
        }).attr("title", "Which part of the card");
        var sync = function () {
            var parts = current();
            select.toggle(parts != null);
            select.val(parts != null && parts.length > 2 ? parts[2] : "");
        };
        input.bind("input change", sync);
        sync();
        return select;
    },

    moveMessage: function (position, toIndex) {
        var form = this.form;
        var steps = this.draft.steps;
        if (form == null || toIndex < 0 || toIndex >= steps.length)
            return;
        var from = this.written(form.step);
        var moved = from.messages.splice(position, 1)[0];
        if (from.messages.length == 0)
            delete from.messages;
        var target = this.written(this.working(steps[toIndex]));
        target.messages = target.messages || [];
        //Going earlier it is the last thing that step says; going later, the first
        if (toIndex < form.index)
            target.messages.push(moved);
        else
            target.messages.unshift(moved);
        var uids = this.state.uids;
        var ops = [{op: "merge", uid: form.uid, set: from.messages != null ? {messages: from.messages} : {}, unset: from.messages != null ? [] : ["messages"]},
            {op: "merge", uid: uids[toIndex], set: {messages: target.messages}, unset: []}];
        //(whatever else of this step has not been sent goes with it)
        var rest = this.dirty();
        for (var name in rest.set)
            if (rest.set.hasOwnProperty(name) && name != "messages")
                ops[0].set[name] = rest.set[name];
        $.each(rest.unset, function (i, gone) {
            if (gone != "messages")
                ops[0].unset.push(gone);
        });
        this.form = null;
        this.ui.selected = toIndex + 1;
        this.ui.selectedUid = uids[toIndex];
        this.ui.hold = true;
        this.focusMessage = {step: toIndex + 1, index: toIndex < form.index ? target.messages.length - 1 : 0};
        this.remember();
        this.edit({steps: ops}, null);
    },

    /** The messages up to and including one become a pause of their own, in front of the step they were on. */
    splitToPause: function (position) {
        var form = this.form;
        if (form == null)
            return;
        var from = this.written(form.step);
        var taken = from.messages.splice(0, position + 1);
        if (from.messages.length == 0)
            delete from.messages;
        var pause = {pause: true, messages: taken};
        //A message that waited on the answering cannot do that in a step that is only read
        $.each(pause.messages, function (i, message) {
            delete message.on;
            if (message.advance == "decision")
                delete message.advance;
        });
        if (from["if"])
            pause["if"] = from["if"];
        if (from.unless)
            pause.unless = from.unless;
        if (typeof from.when == "string") {
            var kept = $.grep(this.splitTerms(from.when), function (term) {
                return !/^(offers|text)\(/i.test(term);
            });
            pause.when = kept.join(", ");
        }
        var rest = this.dirty();
        var merge = {op: "merge", uid: form.uid, set: rest.set, unset: rest.unset};
        if (from.messages != null) {
            merge.set.messages = from.messages;
        } else {
            delete merge.set.messages;
            if ($.inArray("messages", merge.unset) < 0)
                merge.unset.push("messages");
        }
        this.changeSteps([merge, {op: "insert", before: form.uid, step: pause}], form.index + 1, form.index + 1);
    },

    /* ---------------------------------------------------------------- what is on the screen */

    /**
     * Shows what a change to where a message's bubble sits looks like, at once, if that message is the one on the
     * screen.  The change itself goes to the server in its own time, and comes back as the step put up again.
     */
    tryOnScreen: function (position, name, value) {
        var overlay = this.overlay, form = this.form;
        if (form == null || overlay.step == null || overlay.step.step != form.index + 1)
            return;
        var message = (overlay.step.messages || [])[position];
        if (message == null || message !== overlay.currentMessage)
            return;
        message[name] = value === "" ? null : value;
        overlay.bubbleSituation = null;
        overlay.refresh();
    },

    stepShown: function (step) {
        var focus = this.focusMessage;
        if (focus == null || step == null || focus.step != step.step)
            return;
        this.showOnScreen();
    },

    /** Puts the message being worked on up, if its step is the one on the screen. */
    showOnScreen: function () {
        var overlay = this.overlay, focus = this.focusMessage;
        if (focus == null || overlay.step == null || overlay.step.step != focus.step)
            return false;
        var message = (overlay.step.messages || [])[focus.index];
        if (message == null)
            return false;
        var at = $.inArray(message, overlay.startMessages || []);
        if (at > -1)
            overlay.messageIndex = at;
        //The author can always get on with the game: a message being looked at is never in the way of the answer
        if (overlay.step.seat == "human" && !overlay.step.pause)
            overlay.phase = "answering";
        overlay.showMessage(message);
        overlay.markCards();
        return true;
    },

    /**
     * The pictures a message can show that are not cards, to choose one from.
     * @param box where to list them (pressed again with the list open, it is put away)
     */
    choosePicture: function (box, done) {
        var that = this;
        if (box.is(":visible")) {
            box.hide();
            return;
        }
        box.empty().append(this.el("div", "sa-note", "Looking in images/scenarios" + "\u2026")).show();
        this.call("GET", "images", null, function (result) {
            box.empty();
            var images = result.images || [];
            if (images.length == 0)
                box.append(that.el("div", "sa-note", "There are no pictures. They are files (.png, .jpg, .gif, .webp) in the web folder's images/scenarios, put there by whoever runs the server."));
            $.each(images, function (index, image) {
                var item = that.el("div", "sa-picture").attr("title", "image:" + image.name).appendTo(box);
                item.append($("<img alt=''/>").attr("src", image.url));
                item.append(that.el("span", null, image.name));
                item.click(function () {
                    box.hide();
                    done(image.name);
                });
            });
        });
    },

    /* ---------------------------------------------------------------- picking things on the table */

    pick: function (done) {
        var that = this;
        this.stopPicking();
        $("body").addClass("sa-picking");
        this.told = "Click what the message is about: a card, a row, a pile, a button. Esc to cancel.";
        this.renderNotices();
        var cards = this.cardMap();

        var hit = function (event) {
            if ($(event.target).closest("#scenarioAuthor").length > 0)
                return null;
            return that.thingAt(event.clientX, event.clientY, cards);
        };
        var listeners = {
            mousemove: function (event) {
                var found = hit(event);
                if (found == null) {
                    that.pickBox.hide();
                    that.pickLabel.hide();
                    return;
                }
                that.pickBox.css({display: "block", left: found.rect.left - 3, top: found.rect.top - 3, width: found.rect.width, height: found.rect.height});
                that.pickLabel.text(found.anchor).css({display: "block", left: found.rect.left, top: Math.max(0, found.rect.top - 22)});
            },
            click: function (event) {
                var inPanel = $(event.target).closest("#scenarioAuthor").length > 0;
                var found = hit(event);
                event.preventDefault();
                event.stopPropagation();
                that.stopPicking();
                if (found != null && !inPanel)
                    done(found.anchor);
            },
            mousedown: function (event) {
                event.preventDefault();
                event.stopPropagation();
            },
            mouseup: function (event) {
                event.preventDefault();
                event.stopPropagation();
            },
            keydown: function (event) {
                if (event.keyCode == 27) {
                    event.preventDefault();
                    event.stopPropagation();
                    that.stopPicking();
                }
            }
        };
        for (var name in listeners)
            if (listeners.hasOwnProperty(name))
                document.addEventListener(name, listeners[name], true);
        this.picking = listeners;
    },

    stopPicking: function () {
        if (this.picking != null)
            for (var name in this.picking)
                if (this.picking.hasOwnProperty(name))
                    document.removeEventListener(name, this.picking[name], true);
        var was = this.picking != null;
        this.picking = null;
        this.told = null;
        $("body").removeClass("sa-picking");
        this.pickBox.hide();
        this.pickLabel.hide();
        this.renderNotices();
        if (was && this.followDue) {
            //(once what was picked has been written into the step it was picked for)
            var that = this;
            setTimeout(function () {
                that.catchUp();
            }, 300);
        }
    },

    /** The smallest thing a scenario can point at that is under a point of the screen: {anchor, rect}. */
    thingAt: function (x, y, cards) {
        var that = this;
        var best = null;
        var consider = function (anchor, rect) {
            if (rect == null || x < rect.left || x > rect.left + rect.width || y < rect.top || y > rect.top + rect.height)
                return;
            var area = rect.width * rect.height;
            if (best == null || area < best.area)
                best = {anchor: anchor, rect: rect, area: area};
        };
        $(".card:visible").each(function () {
            var card = $(this).data("card");
            if (card == null)
                return;
            var rect = ScenarioAnchors.rectOf($(this));
            var alias = that.aliasOfCard(card.cardId);
            if (alias != null)
                consider("card:" + alias, rect);
            else if (card.zone == "ADVENTURE_PATH" && card.siteNumber != null)
                consider("ui:site:" + card.siteNumber, rect);
        });
        $.each(this.PLACES, function (index, anchor) {
            consider(anchor, ScenarioAnchors.resolve(that.game, anchor, cards));
        });
        for (var alias in cards)
            if (cards.hasOwnProperty(alias) && this.game.freePeopleAssignGroups != null && this.game.freePeopleAssignGroups[cards[alias]] != null)
                consider("ui:assignment:" + alias, ScenarioAnchors.resolve(this.game, "ui:assignment:" + alias, cards));
        return best;
    },

    /* ---------------------------------------------------------------- the opponent */

    renderOpponent: function () {
        var that = this;
        var box = this.tabs.opponent;
        if (this.typingIn(box))
            return;
        box.empty();
        var waiting = this.state.waiting || {};
        var asked = waiting.opponent;
        var answer = function (term, record) {
            that.call("POST", "answer", {term: term, record: record});
        };

        var section = this.el("div", "sa-section").appendTo(box);
        if (asked == null) {
            section.append(this.el("p", "sa-note", "The opponent is not being asked anything."));
        } else {
            section.append(this.el("div", "sa-heading", "The opponent is asked"));
            section.append(this.el("div", "sa-asked", asked.text + " (" + asked.where + ")"));
            if (!asked.held) {
                section.append(this.el("p", "sa-note", asked.step != null ? "Step " + asked.step + " answers it, once what it says has been read."
                    : "It answers by itself in a moment (its standing orders, or its fallback)."));
            } else {
                var write = $("<input type='checkbox'/>").prop("checked", true);
                var record = function () {
                    return write.prop("checked");
                };
                var options = this.el("div", "sa-answers").appendTo(section);
                if (asked.type == "action" || asked.type == "actionChoice" || asked.type == "choice") {
                    $.each(asked.options, function (index, option) {
                        options.append(that.button(option.label, option.term, function () {
                            answer(option.term, record());
                        }, "sa-answer"));
                    });
                } else if (asked.type == "cards" || asked.type == "arbitrary") {
                    options.append(this.el("div", "sa-note", "Choose " + asked.min + (asked.max != asked.min ? " to " + asked.max : "") + ":"));
                    $.each(asked.options, function (index, option) {
                        options.append(that.el("label", "sa-check sa-block").append($("<input type='checkbox'/>").data("option", option))
                            .append(document.createTextNode(" " + option.label + (option.alias ? " (" + option.alias + ")" : ""))));
                    });
                    options.append(this.button("Choose these", "", function () {
                        var chosen = [];
                        options.find("input:checked").each(function () {
                            chosen.push($(this).data("option"));
                        });
                        var named = $.grep(chosen, function (option) {
                            return option.alias != null;
                        }).length == chosen.length;
                        answer(chosen.length == 0 ? "pass" : named ? "choose(" + $.map(chosen, function (option) {
                            return option.alias;
                        }).join(", ") + ")" : "raw(" + $.map(chosen, function (option) {
                            return option.id;
                        }).join(",") + ")", record());
                    }, "sa-primary"));
                } else if (asked.type == "assign") {
                    options.append(this.el("div", "sa-note", "Who each minion fights:"));
                    var characters = [["", "nobody"]].concat($.map(asked.characters, function (character) {
                        return [[character.alias, character.label + " (" + character.alias + ")"]];
                    }));
                    $.each(asked.options, function (index, minion) {
                        var row = that.el("div", "sa-row").appendTo(options);
                        row.append(that.el("span", "sa-alias", minion.alias || minion.label));
                        row.append(that.select(characters, "").data("minion", minion.alias));
                    });
                    options.append(this.button("Assign", "", function () {
                        var groups = {};
                        options.find("select").each(function () {
                            var character = $(this).val();
                            if (character != "")
                                (groups[character] = groups[character] || []).push($(this).data("minion"));
                        });
                        var pairings = [];
                        for (var character in groups)
                            if (groups.hasOwnProperty(character))
                                pairings.push(character + ": " + groups[character].join(" + "));
                        answer(pairings.length == 0 ? "pass" : "assign(" + pairings.join(", ") + ")", record());
                    }, "sa-primary"));
                } else if (asked.type == "number") {
                    var number = $("<input type='number'/>").addClass("sa-input sa-short").val(asked.min != null ? asked.min : 0);
                    if (asked.min != null)
                        number.attr("min", asked.min);
                    if (asked.max != null)
                        number.attr("max", asked.max);
                    options.append(number);
                    options.append(this.button("Answer", "", function () {
                        answer("number(" + number.val() + ")", record());
                    }, "sa-primary"));
                }
                if (asked.pass && asked.type != "assign")
                    options.append(this.button("Pass", "pass", function () {
                        answer("pass", record());
                    }, "sa-answer"));
                section.append(this.el("label", "sa-check sa-block").attr("title", "Unticked: the opponent does it this once, and the script is left as it is").append(write)
                    .append(document.createTextNode(" write it into the script")));
                var rawRow = this.el("div", "sa-row").appendTo(section);
                var raw = $("<input type='text'/>").addClass("sa-input").attr("placeholder", "or in the script's words: play(runner1)");
                rawRow.append(raw).append(this.button("Send", "", function () {
                    if ($.trim(raw.val()) != "")
                        answer($.trim(raw.val()), record());
                }));
                section.append(this.button("Let it do the least it can, unwritten", "What its fallback would do. Nothing is written into the script.", function () {
                    that.call("POST", "answer", {least: true, record: false});
                }));
            }
        }

        var yours = this.el("div", "sa-section").appendTo(box);
        yours.append(this.el("div", "sa-heading", "You are asked"));
        if (waiting.human == null) {
            yours.append(this.el("p", "sa-note", "Nothing, for now."));
        } else {
            yours.append(this.el("div", "sa-asked", waiting.human.text + " (" + waiting.human.where + ")"));
            yours.append(this.el("p", "sa-note", waiting.human.step != null ? "Step " + waiting.human.step + " applies to it."
                : "No step applies to it." + (this.state.recording ? " What you answer becomes a step." : "")));
        }

        var how = this.el("div", "sa-section").appendTo(box);
        how.append(this.el("div", "sa-heading", "Where the script does not answer for the opponent"));
        how.append(this.el("label", "sa-check sa-block").append($("<input type='radio' name='sa-ask'/>").prop("checked", !!this.state.askOpponent).change(function () {
            that.call("POST", "settings", {askOpponent: true});
        })).append(document.createTextNode(" it waits for me, here")));
        how.append(this.el("label", "sa-check sa-block").append($("<input type='radio' name='sa-ask'/>").prop("checked", !this.state.askOpponent).change(function () {
            that.call("POST", "settings", {askOpponent: false});
        })).append(document.createTextNode(" it does what a player's game would have it do (orders, then fallback: " + ((this.draft.opponent && this.draft.opponent.fallback) || "fail") + ")")));
    },

    /* ---------------------------------------------------------------- the board */

    renderBoard: function () {
        var that = this;
        var box = this.tabs.board;
        if (this.typingIn(box))
            return;
        var scroll = box.scrollTop();
        box.empty();
        var board = this.state.board;
        if (board == null) {
            box.append(this.el("p", "sa-note", "The board has not been laid out."));
            return;
        }
        var inSetup = this.ui.where == "setup";
        var position = this.state.position;
        var act = function (op) {
            that.call("POST", "do", {op: op, setup: inSetup});
        };

        var where = this.el("div", "sa-section").appendTo(box);
        where.append(this.el("div", "sa-heading", "Changes are made"));
        var choose = function (value, label, note) {
            var row = that.el("label", "sa-check sa-block").appendTo(where);
            row.append($("<input type='radio' name='sa-where'/>").prop("checked", that.ui.where == value).change(function () {
                that.ui.where = value;
                that.remember();
                that.renderBoard();
            }));
            row.append(document.createTextNode(" " + label));
            row.append(that.el("div", "sa-note", note));
        };
        choose("now", "to the board as it stands", position.inHand
            ? "Done at once, and by step " + position.step + " whenever the script is played. The decision on the screen was worked out before the change: it shows in what the game offers from the next decision on."
            : "Done at once, and by the next step you write. The decision on the screen was worked out before the change.");
        choose("setup", "to the board the scenario starts with", "The setup is changed, and a fresh game is played by the script back to here.");
        if ((this.state.pending || []).length > 0)
            where.append(this.el("div", "sa-note sa-pending", "Waiting for a step to carry them: " + this.state.pending.join(", ")));

        var counters = this.el("div", "sa-section").appendTo(box);
        var counter = function (label, name, value, disabled) {
            var input = $("<input type='number' min='0'/>").addClass("sa-input sa-short").val(value);
            var row = that.el("div", "sa-row").appendTo(counters);
            row.append(that.el("span", "sa-label", label));
            row.append(input);
            row.append(that.button("Set", "", function () {
                act(name + "(" + (parseInt(input.val()) || 0) + ")");
            }).prop("disabled", !!disabled));
        };
        counter("Twilight", "twilight", board.twilight, inSetup);
        counter("Burdens", "burdens", board.burdens);
        counter("Threats", "threats", board.threats);

        //(what another card can be put on: anything on the table, a card that is itself on another included)
        var bearers = $.grep(board.cards, function (card) {
            return card.zone == "characters" || card.zone == "minions" || card.zone == "support" || card.zone == "attached";
        });
        $.each(["freeps", "shadow"], function (index, seat) {
            var section = that.el("div", "sa-section").appendTo(box);
            section.append(that.el("div", "sa-heading", seat == "freeps" ? "Free Peoples seat" : "Shadow seat"));
            var zones = $.map(that.ZONES, function (zone) {
                return [[seat + "." + zone, zone]];
            });
            //The seat's cards by where they are: what is on the table, then the hand and the piles, each of those in
            // the order its cards come (which the arrows change)
            var places = that.PLACED.slice();
            var held = {};
            $.each(board.cards, function (i, card) {
                if (card.owner != seat)
                    return;
                if (held[card.zone] == null) {
                    held[card.zone] = [];
                    if ($.grep(places, function (place) { return place[0] == card.zone; }).length == 0)
                        places.push([card.zone, card.zone]);
                }
                held[card.zone].push(card);
            });
            var listed = [];
            $.each(places, function (p, place) {
                var there = held[place[0]];
                if (there == null)
                    return;
                var ordered = $.inArray(place[0], that.ORDERED) > -1;
                if (ordered)
                    there.sort(function (a, b) {
                        return (a.at == null ? 9999 : a.at) - (b.at == null ? 9999 : b.at);
                    });
                $.each(there, function (at, card) {
                    listed.push({card: card, first: at == 0, label: place[1] + " (" + there.length + ")", ordered: ordered && there.length > 1, at: at, among: there});
                });
            });
            $.each(listed, function (i, entry) {
                var card = entry.card;
                if (entry.first)
                    section.append(that.el("div", "sa-zonehead", entry.label));
                var row = that.el("div", "sa-card").appendTo(section);
                var name = that.el("div", "sa-cardname").appendTo(row);
                //(a card hint, as the game's chat has them: the game screen shows the card while the pointer is on
                // it, and opens its window on a click)
                name.append(that.el("span", "sa-alias cardHint", card.alias).attr("value", card.blueprintId).attr("title", ""));
                name.append(document.createTextNode(" " + card.name));
                var controls = that.el("div", "sa-row").appendTo(row);
                if (entry.ordered) {
                    //Where it comes in its hand or pile: one place earlier or later, or first
                    var reorder = function (to) {
                        var aliases = $.map(entry.among, function (other) { return other.alias; });
                        aliases.splice(entry.at, 1);
                        aliases.splice(to, 0, card.alias);
                        act("order(" + seat + "." + card.zone + ", " + aliases.join(", ") + ")");
                    };
                    var pile = card.zone != "hand";
                    controls.append(that.button("\u2191", pile ? "One nearer the top" : "One earlier in the hand", function () {
                        reorder(entry.at - 1);
                    }, "sa-order").prop("disabled", entry.at == 0));
                    controls.append(that.button("\u2193", pile ? "One nearer the bottom" : "One later in the hand", function () {
                        reorder(entry.at + 1);
                    }, "sa-order").prop("disabled", entry.at == entry.among.length - 1));
                    controls.append(that.button("\u2912", pile ? "To the top" : "To the front of the hand", function () {
                        reorder(0);
                    }, "sa-order").prop("disabled", entry.at == 0));
                }
                controls.append(that.select([["", card.zone + (card.on ? " on " + card.on : "")]].concat(zones), "", function (zone) {
                    if (zone != "")
                        act("move(" + card.alias + ", " + zone + ")");
                }).attr("title", "Move it"));
                var inPlay = card.zone == "characters" || card.zone == "minions";
                if (inPlay || card.wounds > 0) {
                    controls.append(that.button("\u2212", "Heal a wound", function () {
                        act("heal(" + card.alias + ", 1)");
                    }).prop("disabled", card.wounds == 0));
                    controls.append(that.el("span", "sa-count", card.wounds + "w").attr("title", "wounds"));
                    controls.append(that.button("+", "Add a wound", function () {
                        act("wound(" + card.alias + ", 1)");
                    }));
                }
                if (card.zone == "support" || card.tokens > 0)
                    controls.append(that.button("+token" + (card.tokens > 0 ? " (" + card.tokens + ")" : ""), "Add a culture token", function () {
                        act("tokens(" + card.alias + ", 1)");
                    }));
                //Any card can be put on any other that is on the table: attached to it (as a possession is borne),
                // or stacked on it (as cards are kept under another)
                var hosts = $.grep(bearers, function (bearer) {
                    return bearer.alias != card.alias;
                });
                if (hosts.length > 0)
                    controls.append(that.select([["", "put on" + "\u2026"]].concat($.map(hosts, function (host) {
                        return [["attach(" + card.alias + ", " + host.alias + ")", "attach to " + host.alias]];
                    })).concat($.map(hosts, function (host) {
                        return [["stack(" + card.alias + ", " + host.alias + ")", "stack on " + host.alias]];
                    })), "", function (op) {
                        if (op != "")
                            act(op);
                    }).attr("title", "Attach it to another card, or stack it on one"));
                controls.append(that.button("\u00d7", "Take this card out of the scenario", function () {
                    if (confirm("Take " + card.alias + " out of the scenario? (A fresh game is played back to here.)"))
                        that.removeCard(card);
                }, "sa-danger"));
            });
        });

        var add = this.el("div", "sa-section").appendTo(box);
        add.append(this.el("div", "sa-heading", "Add a card to the scenario"));
        var query = $("<input type='text'/>").addClass("sa-input").attr("placeholder", "name of a card");
        var results = this.el("div", "sa-results");
        var chosen = this.el("div", "sa-chosen");
        add.append(query).append(results).append(chosen);
        var search = null;
        query.bind("input", function () {
            clearTimeout(search);
            search = setTimeout(function () {
                that.call("GET", "cards", {q: query.val()}, function (found) {
                    results.empty();
                    $.each(found.cards || [], function (i, card) {
                        results.append(that.el("div", "sa-result", card.name + "  \u00b7  " + card.id + "  \u00b7  " + (card.culture || "") + " " + (card.type || "")
                            + (card.cost != null ? " (" + card.cost + ")" : "")).click(function () {
                            offer(card);
                        }));
                    });
                    if ((found.cards || []).length == 0)
                        results.append(that.el("div", "sa-note", "No card by that name."));
                });
            }, 300);
        });
        var offer = function (card) {
            results.empty();
            chosen.empty();
            var taken = that.aliases();
            //The first word of its name that says something: "The One Ring" is "one", not "the"
            var words = $.grep(card.name.toLowerCase().replace(/[^a-z0-9 ]/g, "").split(" "), function (word) {
                return word.length > 0 && $.inArray(word, ["the", "a", "an", "of"]) < 0;
            });
            var stem = words[0] || "card";
            if (/^[0-9]/.test(stem))
                stem = "c" + stem;
            var alias = stem;
            for (var n = 2; $.inArray(alias, taken) > -1; n++)
                alias = stem + n;
            var name = $("<input type='text'/>").addClass("sa-input sa-short").val(alias);
            var seat = that.select(["freeps", "shadow"], card.side == "shadow" ? "shadow" : "freeps");
            var host = that.select($.map(bearers, function (bearer) {
                return bearer.alias;
            }), bearers.length > 0 ? bearers[0].alias : "").attr("title", "The card it is put on").hide();
            var places = [["", "deck (bottom)"], "hand", "characters", "support", "minions", "discard", "deadPile"];
            if (bearers.length > 0)
                places = places.concat([["attach", "attached to" + "\u2026"], ["stack", "stacked on" + "\u2026"]]);
            var zone = that.select(places, card.side == "shadow" ? "" : "hand", function (value) {
                host.toggle(value == "attach" || value == "stack");
            });
            chosen.append(that.el("div", "sa-cardname", card.name + " (" + card.id + ")"));
            var row = that.el("div", "sa-row sa-wrap").appendTo(chosen);
            row.append(that.el("span", "sa-label", "called")).append(name).append(seat).append(zone).append(host);
            row.append(that.button("Add", "A fresh game is played by the script back to here", function () {
                that.addCard(card.id, $.trim(name.val()), seat.val(), zone.val(), host.val());
            }, "sa-primary"));
        };
        box.scrollTop(scroll);
    },

    /** @param zone a zone of the seat's; "" for the deck; or "attach" / "stack", with the card it goes on as host */
    addCard: function (blueprintId, alias, seat, zone, host) {
        if (!/^[A-Za-z][A-Za-z0-9_]*$/.test(alias)) {
            this.problem = "A card's name in the scenario is made of letters, digits and underscores, starting with a letter.";
            this.renderNotices();
            return;
        }
        var cards = this.clone(this.draft.cards);
        cards[seat] = cards[seat] || {};
        cards[seat][alias] = blueprintId;
        var top = {cards: cards};
        if (zone == "attach" || zone == "stack") {
            var onto = this.clone(this.draft.setup) || {};
            onto[zone] = onto[zone] || {};
            onto[zone][alias] = host;
            top.setup = onto;
        } else if (zone != "") {
            var setup = this.clone(this.draft.setup) || {};
            setup[seat] = setup[seat] || {};
            setup[seat][zone] = setup[seat][zone] ? setup[seat][zone] + ", " + alias : alias;
            top.setup = setup;
        }
        this.edit({top: top}, null);
    },

    removeCard: function (card) {
        var that = this;
        var cards = this.clone(this.draft.cards);
        delete cards[card.owner][card.alias];
        var setup = this.clone(this.draft.setup);
        if (setup != null) {
            $.each(["freeps", "shadow"], function (index, seat) {
                for (var zone in (setup[seat] || {}))
                    if (setup[seat].hasOwnProperty(zone)) {
                        var kept = $.grep(that.splitTerms(setup[seat][zone]), function (alias) {
                            return alias != card.alias;
                        });
                        if (kept.length == 0)
                            delete setup[seat][zone];
                        else
                            setup[seat][zone] = kept.join(", ");
                    }
                if (setup[seat] != null && $.isEmptyObject(setup[seat]))
                    delete setup[seat];
            });
            $.each(["attach", "stack", "wounds", "tokens"], function (index, name) {
                if (setup[name] != null) {
                    delete setup[name][card.alias];
                    if ($.isEmptyObject(setup[name]))
                        delete setup[name];
                }
            });
        }
        this.edit({top: {cards: cards, setup: setup == null || $.isEmptyObject(setup) ? null : setup}}, null);
    },

    /* ---------------------------------------------------------------- the scenario as a whole */

    renderScenario: function () {
        var that = this;
        var box = this.tabs.scenario;
        if (this.typingIn(box))
            return;
        var scroll = box.scrollTop();
        box.empty();
        var draft = this.draft, state = this.state;
        var top = function (name, value) {
            var patch = {};
            patch[name] = value === "" ? null : value;
            that.edit({top: patch}, null);
        };
        var text = function (label, name, hint, area) {
            var input = area ? that.el("textarea", "sa-input").attr("rows", 3) : $("<input type='text'/>").addClass("sa-input");
            input.val(draft[name] == null ? "" : draft[name]).change(function () {
                top(name, name == "version" ? (parseInt($(this).val()) || 1) : $(this).val());
            });
            return that.field(label, input, hint);
        };

        var file = this.el("div", "sa-section").appendTo(box);
        file.append(this.el("div", "sa-heading", "The file"));
        file.append(this.el("div", "sa-note sa-path", state.path + (state.canonical ? " (public)" : "")
            + (!state.onDisk ? " \u2014 not saved yet" : state.dirty ? " \u2014 changed since it was saved" : " \u2014 saved")));
        if ((state.commentsLost || 0) > 0)
            file.append(this.el("div", "sa-note sa-pending", "The file has " + state.commentsLost + " line(s) of comment that saving will drop. Notes are kept."));
        //A scenario is kept under the folder of its kind: one that is not is moved there as it is saved
        if (state.onDisk && state.fileOnSave != null && state.fileOnSave != state.file)
            file.append(this.el("div", "sa-note sa-pending", "Saving moves the file to " + state.fileOnSave + ": a " + (draft.kind || "tutorial")
                + " is kept under " + state.kindFolder + "/."));
        var buttons = this.el("div", "sa-bar").appendTo(file);
        buttons.append(this.button("Save", "Write the draft to its file", function () {
            that.save();
        }, "sa-primary"));
        var filing = function (how) {
            return function () {
                that.filing = that.filing == how ? null : how;
                that.renderScenario();
            };
        };
        buttons.append(this.button("Save as" + "\u2026", "Save the draft as another scenario, with a file and an id of its own", filing("copy")).toggleClass("sa-on", this.filing == "copy"));
        buttons.append(this.button("Rename" + "\u2026", "Give the file another name, or move it to another folder. It stays the same scenario.", filing("rename")).toggleClass("sa-on", this.filing == "rename"));
        if (this.filing != null)
            file.append(this.filingForm(this.filing));
        buttons.append(this.button("Check", "Play the draft through with nobody at the table, the way the server does before it offers a scenario", function () {
            that.call("POST", "check", {}, function (result) {
                that.report = (result.passed ? "Scenario validated.\n\n" : "It does not play through.\n\n") + result.report
                    + ((result.warnings || []).length > 0 ? "\n\nWarnings:\n" + result.warnings.join("\n") : "");
                that.renderScenario();
            });
        }));
        buttons.append(this.button("See the file", "The draft as the file it would be saved as", function () {
            that.call("GET", "source", null, function (result) {
                that.report = result.source;
                that.renderScenario();
            });
        }));
        buttons.append(this.button("Close the draft", "Give the draft up. Anything not saved is lost.", function () {
            if (!state.dirty || confirm("The draft has changes that have not been saved. Close it anyway?"))
                that.call("POST", "close", {}, function () {
                    that.gone = true;
                    that.overlay.goToHall();
                });
        }, "sa-danger"));
        if (this.report != null) {
            file.append(this.el("pre", "sa-report", this.report));
            file.append(this.button("Put that away", "", function () {
                that.report = null;
                that.renderScenario();
            }));
        }

        var about = this.el("div", "sa-section").appendTo(box);
        about.append(this.el("div", "sa-heading", "About it"));
        about.append(text("Title", "title"));
        about.append(text("Summary", "summary", "What the Game Hall says about it", true));
        about.append(text("Id", "id", "What the scenario is known as. Changing it makes it a different scenario as far as players' progress goes."));
        about.append(text("Author", "author"));
        about.append(text("Tags", "tags", "Separated by commas"));
        about.append(text("Version", "version", "Raise it when the scenario changes in a way players should hear about"));
        about.append(this.field("Kind", this.select(["tutorial", "puzzle", "sandbox"], draft.kind, function (value) {
            top("kind", value);
        })));
        about.append(text("Note", "note", "For whoever works on the scenario next. Never shown to a player.", true));

        var play = this.el("div", "sa-section").appendTo(box);
        play.append(this.el("div", "sa-heading", "How it plays"));
        var opponent = draft.opponent || {};
        var options = draft.options || {};
        var opponentName = $("<input type='text'/>").addClass("sa-input").val(opponent.name || "").change(function () {
            var changed = that.clone(opponent);
            if ($(this).val() == "")
                delete changed.name;
            else
                changed.name = $(this).val();
            top("opponent", $.isEmptyObject(changed) ? null : changed);
        });
        play.append(this.field("Opponent is called", opponentName, "What the opponent goes by on screen, and in the log beside what the messages say"));
        play.append(this.field("Opponent, unscripted", this.select([["fail", "fail: that is leaving the script"], ["pass", "pass: does as little as it can"]], opponent.fallback || "fail", function (value) {
            var changed = that.clone(opponent);
            if (value == "fail")
                delete changed.fallback;
            else
                changed.fallback = value;
            top("opponent", $.isEmptyObject(changed) ? null : changed);
        }), "What the opponent does with a decision no step (and no standing order) covers"));
        play.append(this.field("Player, unscripted", this.select([["strict", "strict: every decision needs a step"], ["guided", "guided: free between steps"]], draft.coverage, function (value) {
            top("coverage", value);
        }), "Whether the player's every decision has to be covered by a step"));
        play.append(this.field("Player sits", this.select(["freeps", "shadow"], draft.human, function (value) {
            top("human", value);
        })));
        var passRow = this.el("div", "sa-row sa-wrap").appendTo(play);
        passRow.append(this.el("span", "sa-label", "Passed through").attr("title", "Phases in which the player is not asked to pass when they have nothing to play (unless the next step is about that very pass)"));
        var passed = this.splitTerms(options.autoPass || "");
        $.each(this.PHASES, function (index, phase) {
            passRow.append(that.el("label", "sa-check").append($("<input type='checkbox'/>").prop("checked", $.inArray(phase, passed) > -1).change(function () {
                var now = $.grep(passed, function (name) {
                    return name != phase;
                });
                if ($(this).prop("checked"))
                    now.push(phase);
                var changed = that.clone(options);
                if (now.length == 0)
                    delete changed.autoPass;
                else
                    changed.autoPass = now.join(", ");
                top("options", $.isEmptyObject(changed) ? null : changed);
            })).append(document.createTextNode(" " + phase)));
        });

        var bubbleGap = $("<input type='text'/>").addClass("sa-input sa-short").val(options.bubbleGap || "").attr("placeholder", this.overlay.STOCK_GAP).change(function () {
            var changed = that.clone(options);
            if ($.trim($(this).val()) == "")
                delete changed.bubbleGap;
            else
                changed.bubbleGap = $.trim($(this).val());
            top("options", $.isEmptyObject(changed) ? null : changed);
        });
        play.append(this.field("Bubble gap", bubbleGap, "How far from what it points at a bubble sits, for every message that does not say. A plain number is hundredths of the window's shorter side; 20px is pixels. Empty: " + this.overlay.STOCK_GAP + "."));

        var parts = this.el("div", "sa-section").appendTo(box);
        parts.append(this.el("div", "sa-heading", "The rest of the file"));
        parts.append(this.el("p", "sa-note", "Each part as the file has it, in HJSON: what is copied out of a scenario's file can be pasted here (with or without its name in front), and the other way about. "
            + "Changing one starts a fresh game, played by the script back to here."));
        $.each(["cards", "ringBearer", "ring", "sites", "setup", "rules", "rng", "opponent", "options", "outcome"], function (index, name) {
            var details = $("<details class='sa-more'></details>").appendTo(parts);
            details.append($("<summary></summary>").text(name + (draft[name] == null ? " (none)" : "")));
            var written = (state.parts || {})[name] || "";
            var area = that.el("textarea", "sa-input sa-code").attr("rows", Math.max(4, Math.min(22, written.split("\n").length + 1)))
                .attr("spellcheck", "false").val(written);
            //(a tab is part of what is being written, not a way out of the box)
            area.keydown(function (event) {
                if (event.keyCode != 9 || event.shiftKey || event.ctrlKey || event.altKey)
                    return;
                event.preventDefault();
                var at = this.selectionStart, value = this.value;
                this.value = value.substring(0, at) + "\t" + value.substring(this.selectionEnd);
                this.selectionStart = this.selectionEnd = at + 1;
            });
            details.append(area);
            details.append(that.button("Apply", "", function () {
                //The server reads it, as it reads the file: what is wrong with it is said at the top of the panel
                var given = {};
                given[name] = area.val();
                that.edit({topText: given}, null);
            }));
        });
        box.scrollTop(scroll);
    },

    save: function () {
        var that = this;
        var data = {};
        if (!this.mayOverwrite())
            return;
        this.flush(function () {
            that.sendSave(data);
        });
    },

    /** Whether the file may be written over, where that loses the comments somebody wrote in it by hand. */
    mayOverwrite: function () {
        var lost = this.state.commentsLost || 0;
        return lost == 0 || confirm("The file has " + lost + " line" + (lost == 1 ? "" : "s") + " of comment that will not be in it after saving. "
            + "(Notes are part of the scenario, and are kept.)\n\nSave over it?");
    },

    /** The folders scenarios are kept under by kind: no path is given with one of these in front (the server puts it there). */
    KIND_FOLDERS: ["tutorials", "puzzles", "sandboxes"],

    /** A file's place as its author says it: without the folder of its kind in front, and without .hjson on the end. */
    bareName: function (path) {
        var bare = ("" + (path == null ? "" : path)).replace(/\.hjson$/i, "");
        var slash = bare.indexOf("/");
        if (slash > 0 && $.inArray(bare.substring(0, slash).toLowerCase(), this.KIND_FOLDERS) > -1)
            bare = bare.substring(slash + 1);
        return bare;
    },

    /**
     * What an author types for where a file goes, as a path the server will take: slashes one way, nothing but what
     * a file's name may be made of, and .hjson on the end (whether or not it was typed).
     */
    tidyPath: function (typed) {
        var path = $.trim(("" + typed).replace(/\\/g, "/")).replace(/\.hjson$/i, "").replace(/[^A-Za-z0-9_. \/-]+/g, "-");
        //(each folder's name, and the file's: nothing but a letter or a digit at either end)
        path = $.grep($.map(path.split("/"), function (name) {
            return name.replace(/^[^A-Za-z0-9]+|[^A-Za-z0-9]+$/g, "");
        }), function (name) {
            return name != "";
        }).join("/");
        return path == "" ? "" : path + ".hjson";
    },

    /** The form under Save as / Rename: where the file is to go. */
    filingForm: function (how) {
        var that = this;
        var state = this.state;
        var copy = how == "copy";
        var form = this.el("div", "sa-filing");
        form.append(this.el("div", "sa-note", copy
            ? "Saves the draft as another scenario. The copy is known by its file's name (that becomes its id), and is what you are writing from then on. The scenario it came from stays as it was last saved."
            : "Gives the file another name, or moves it to another folder. It stays the same scenario: its id, and players' progress in it, do not change." + (state.dirty ? " What has not been saved stays unsaved." : "")));
        //The name is given without its ending (.hjson is always put on), and without the folder of its kind (it is
        // always kept under that): both are shown either side of it
        var folder = (state.kindFolder || "tutorials") + "/";
        var suggested = this.bareName(state.file || this.draft.id);
        if (copy)
            suggested += "-copy";
        var path = $("<input type='text'/>").addClass("sa-input").attr("maxlength", this.MAX_PATH - folder.length - 6).val(suggested)
            .attr("title", "A name, with folders before it if it goes in any: basics/basics-02-the-ring. Letters, digits, spaces, dots, dashes and underscores. "
                + "It is kept under " + folder + " because of its kind, and .hjson is put on the end.");
        var shown = this.el("div", "sa-note");
        //Only somebody who may publish, on a server that keeps authors' own scenarios too, has anything to choose
        var chooses = !!state.mayPublish && !!state.canSaveAnywhere;
        var canonical = $("<input type='checkbox'/>").prop("checked", !state.canSaveAnywhere || (chooses && !!state.canonical));
        var show = function () {
            var tidy = that.tidyPath(path.val());
            shown.text(tidy == "" ? "" : (canonical.prop("checked") ? "" : "your own folder: ") + folder + tidy
                + (copy ? "  (id: " + tidy.replace(/^.*\//, "").replace(/\.hjson$/i, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "") + ")" : ""));
        };
        path.bind("input", show);
        canonical.change(show);
        var named = this.el("span", "sa-named");
        named.append(this.el("span", "sa-affix", folder)).append(path).append(this.el("span", "sa-affix", ".hjson"));
        form.append(this.field(copy ? "Save as" : "Rename to", named));
        if (chooses)
            form.append(this.el("label", "sa-check sa-block").attr("title", "Kept with the game's own scenarios, and offered to everybody once it plays through. Otherwise it goes in your own folder, where only you and the admins are offered it.")
                .append(canonical).append(document.createTextNode(" Public Scenario")));
        form.append(shown);
        var go = function () {
            var tidy = that.tidyPath(path.val());
            if (tidy == "") {
                path.addClass("sa-bad");
                return;
            }
            that.flush(function () {
                that.call("POST", "save", {as: how, path: tidy, canonical: canonical.prop("checked")}, function (result) {
                    that.filing = null;
                    if (copy) {
                        that.afterSave(result);
                        return;
                    }
                    that.saved = {good: true, stays: true, text: "The file is now " + result.renamed + "."};
                    that.render();
                });
            });
        };
        path.keydown(function (event) {
            if (event.keyCode == 13)
                go();
        });
        var row = this.el("div", "sa-bar").appendTo(form);
        row.append(this.button(copy ? "Save the copy" : "Rename", "", go, "sa-primary"));
        row.append(this.button("Cancel", "", function () {
            that.filing = null;
            that.renderScenario();
        }));
        show();
        return form;
    },

    sendSave: function (data) {
        var that = this;
        this.call("POST", "save", data, function (result) {
            that.afterSave(result);
        });
    },

    /** What is said about a save that went through: at the top of the panel, and in full under Scenario. */
    afterSave: function (result) {
        var that = this;
        (function () {
            var saved = result.saved || {};
            var broken = (saved.errors || []).length > 0;
            var warnings = (saved.warnings || []).length;
            //Saving leaves the author where they were.  What there is to read about it is kept under Scenario.
            that.report = "Saved to " + saved.path + (saved.movedFrom ? " (moved from " + saved.movedFrom + ": that is where its kind is kept)" : "") + ".\n"
                + (broken ? "It cannot be played:\n" + saved.errors.join("\n")
                    : saved.verified ? "Scenario validated." : "It does not play through yet:\n" + (saved.report || ""))
                + (warnings > 0 ? "\n\nWarnings:\n" + saved.warnings.join("\n") : "");
            that.saved = {
                good: !broken && !!saved.verified,
                why: broken || !saved.verified || warnings > 0,
                text: "Saved to " + saved.path + (saved.movedFrom ? ", moved from " + saved.movedFrom : "") + ". " + (broken ? "It cannot be played as it stands."
                    : saved.verified ? "Scenario validated." : "It does not play through yet.")
                    + (warnings > 0 ? " " + warnings + " warning" + (warnings == 1 ? "" : "s") + "." : "")
            };
            if (that.saved.good && warnings == 0 && that.ui.tab != "scenario")
                that.report = null;
            that.render();
        })();
    }
});
