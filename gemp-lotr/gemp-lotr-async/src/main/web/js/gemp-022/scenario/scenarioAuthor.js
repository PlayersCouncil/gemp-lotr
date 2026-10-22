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
        "ui:ruleof4", "ui:clock", "ui:advPath", "ui:alertBox", "ui:gameLog", "ui:skirmish", "ui:skirmish:selfStrength", "ui:skirmish:selfDamage", "ui:skirmish:freeps", "ui:skirmish:shadow",
        "ui:skirmish:oppStrength", "ui:skirmish:oppDamage", "ui:stats", "ui:stats:archery", "ui:stats:move", "ui:chat", "ui:settings",
        "ui:dialog", "ui:cardInfo", "ui:button:pass", "ui:button:done", "ui:button:reset"],
    TABS: [["timeline", "Timeline"], ["step", "Step"], ["opponent", "Opponent"], ["board", "Board"], ["scenario", "Scenario"]],
    /** The parts of a step that a file can have written out in full, as { ... }: typed here as the file has them. */
    TYPED: ["when", "until", "skipWhen"],

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
    /**
     * The step being edited: {uid, index, sub, kind, step (a working copy), origin, base}.  'index' is where it comes
     * in the script (from 0) and, for one of the steps in a block, 'sub' is where it comes in the block (-1 for a
     * step of the script itself).  'kind' is "step", "block" (what a block says of itself) or "child" (a step in one).
     */
    form: null,
    /** The message the author is working on, which is the one to show when its step is put up again: {step, sub, index}. */
    focusMessage: null,
    requests: 0,

    init: function (game, overlay, authoring) {
        this.game = game;
        this.overlay = overlay;
        this.authoring = authoring;
        this.ui = this.recall();
        //What went wrong in the game the page has just left, in something that was sent across a change of game
        if (this.ui.carried != null) {
            this.problem = this.ui.carried;
            delete this.ui.carried;
        }
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
            //(what a block's page shows besides the form, which changed while the form was being typed in)
            if (that.redrawDue)
                setTimeout(function () {
                    if (that.redrawDue && that.ui.tab == "step" && !that.typingIn(that.tabs.step))
                        that.render();
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
            //(one wider than the room beside the panel, as the game makes a window of cards, is made to fit it: its
            // buttons are at its right-hand end)
            var content = $(this).children(".ui-dialog-content");
            if (rect.width > limit - 6 && content.length > 0) {
                try {
                    content.dialog("option", "width", Math.max(300, limit - 12));
                    content.trigger("dialogresize");
                } catch (e) {
                    // (not one of jQuery UI's)
                }
                rect = this.getBoundingClientRect();
            }
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
            //(a change that is part of several is not gone from until the rest have been made: see submitBoard)
            if (this.holding)
                this.arrived = result.gameId;
            else
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

    /**
     * The author has answered a decision on the game's screen.  What it led to can be the opponent being asked, or the
     * script being written, with no game event to say so (an opponent's decision left for the author shows nothing on
     * the author's screen): the state is looked at once the answer has had time to arrive, and again a little later.
     */
    answeredHere: function () {
        var that = this;
        clearTimeout(this.answeredTimer);
        this.answeredTimer = setTimeout(function () {
            that.gameMoved();
            that.answeredTimer = setTimeout(function () {
                that.gameMoved();
            }, 1200);
        }, 500);
    },

    edit: function (patch, goTo, done, failed) {
        var data = {patch: JSON.stringify(patch)};
        if (goTo != null)
            data["goto"] = goTo;
        this.call("POST", "edit", data, done, failed);
    },

    /** @param sub in a block, the one of its steps to play up to (from 1); nothing for the block's first decision */
    goTo: function (step, sub) {
        var that = this;
        this.flush(function () {
            that.call("POST", "goto", sub ? {step: step, sub: sub} : {step: step});
        });
    },

    /**
     * Sends whatever of the step being edited has not been sent, and then does something (if that went through).
     * @param regardless whether to do it also when the server refused what was in the form (a save is of the draft as
     *     it stands: a step in the form that is wrong is not a reason for it not to happen); 'then' is given whether
     *     the form went through
     */
    flush: function (then, regardless) {
        if (this.form != null && this.dirty().any)
            this.sendStep(then, regardless);
        else
            then.call(this, true);
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
        //The opponent's decision is about to be answered (by the script, or by itself), or about to be left for the
        // author; either way something happens with no game event to say so: it is looked at again shortly
        var settling = state.waiting != null && state.waiting.opponent != null && !state.waiting.opponent.held;
        clearTimeout(this.settlingTimer);
        this.settlingCount = settling ? (same ? (this.settlingCount || 0) + 1 : 0) : 0;
        if (settling && this.settlingCount < 10) {
            var that = this;
            this.settlingTimer = setTimeout(function () {
                if (that.requests == 0)
                    that.refresh();
            }, 700);
        }
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
        //The step selected is known by its name: where it comes changes when the game writes a step in before it.
        // In a block, the one of its steps that is selected ('sub') is known by its name too.
        var found = ui.sub == null ? null : this.locate(ui.sub);
        if (found != null) {
            ui.selected = found.index + 1;
            ui.selectedUid = uids[found.index];
            //(taken out of its block, it is a step of the script)
            if (found.sub < 0)
                ui.sub = null;
        } else {
            ui.sub = null;
            if (ui.selectedUid != null && $.inArray(ui.selectedUid, uids) > -1)
                ui.selected = $.inArray(ui.selectedUid, uids) + 1;
        }
        var at = state.position.step;
        //(the step in hand; or one that repeats and is between its decisions; or, failing those, the last one played)
        var current = steps.length == 0 ? null : Math.max(1, Math.min(state.position.inHand || state.position.underWay ? at : at - 1, steps.length));
        var currentUid = current == null ? null : uids[current - 1];
        //(in a block, the one of its steps that is in hand)
        var currentSub = current != null && current == at && state.position.inHand && state.position.sub > 0
            ? (this.kidsOf(current - 1)[state.position.sub - 1] || null) : null;
        var here = currentUid == null ? null : currentUid + (currentSub == null ? "" : "/" + currentSub);
        var lost = ui.selected == null || ui.selected > steps.length;
        var due = false;
        if (ui.hold && !lost) {
            //(asked for by the change that moved the game: moving a step, taking one out)
            ui.hold = false;
            //(...and in a block, by where in the block the step asked for would come)
            if (ui.subAt != null)
                ui.sub = this.kidsOf(ui.selected - 1)[ui.subAt] || null;
        } else if (lost || ui.jump || (ui.follow && here != ui.atUid)) {
            //Nobody's place is taken from under them while they are typing in it, or choosing something on the
            // table for it: the game's step waits until they have done
            if (!lost && !ui.jump && this.engaged()) {
                due = true;
            } else {
                ui.selected = current;
                ui.sub = currentSub;
            }
        }
        ui.subAt = null;
        this.followDue = due;
        if (!due) {
            ui.atUid = here;
            ui.jump = false;
        }
        ui.selectedUid = ui.selected == null ? null : uids[ui.selected - 1];
        if (ui.sub != null && $.inArray(ui.sub, this.kidsOf(ui.selected - 1)) < 0)
            ui.sub = null;
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

    /** The names of the steps in a block, by where the block comes among the steps of the script (from 0). */
    kidsOf: function (index) {
        return ((this.state && this.state.kids) || [])[index] || [];
    },

    /** A step without the steps in it: what a block says of itself. */
    own: function (step) {
        if (step == null || step.anyOrder == null)
            return step;
        var own = {};
        for (var name in step)
            if (step.hasOwnProperty(name) && name != "anyOrder")
                own[name] = step[name];
        return own;
    },

    /**
     * Where the step of a name is: {index, sub, step}, 'sub' being where it comes in its block (-1 for a step of
     * the script itself); null if there is no such step.
     */
    locate: function (uid) {
        var steps = (this.draft && this.draft.steps) || [], uids = (this.state && this.state.uids) || [];
        if (uid == null)
            return null;
        for (var i = 0; i < steps.length; i++) {
            if (uids[i] == uid)
                return {index: i, sub: -1, step: steps[i]};
            var at = $.inArray(uid, this.kidsOf(i));
            if (at > -1 && steps[i].anyOrder != null && steps[i].anyOrder[at] != null)
                return {index: i, sub: at, step: steps[i].anyOrder[at]};
        }
        return null;
    },

    /** What a step is called: "34", or "34.2" for the second of the steps in a block. */
    numberOf: function (index, sub) {
        return (index + 1) + (sub > -1 ? "." + (sub + 1) : "");
    },

    aliasOfCard: function (cardId) {
        var cards = (this.state && this.state.board && this.state.board.cards) || [];
        for (var i = 0; i < cards.length; i++)
            if (("" + cards[i].cardId) == ("" + cardId))
                return cards[i].alias;
        return null;
    },

    /** For "card:alias": "shadow" or "freeps" if that card is in the skirmish being fought, on that side; null otherwise. */
    skirmishSideOf: function (anchor) {
        var match = /^card:([A-Za-z0-9_]+)$/.exec(anchor || "");
        var id = match == null ? null : this.cardMap()[match[1]];
        if (id == null)
            return null;
        var game = this.game;
        var holds = function (group) {
            if (group == null || typeof group.getCardElems != "function")
                return false;
            var found = false;
            $.each(group.getCardElems(), function (index, elem) {
                var card = elem.data("card");
                if (card != null && String(card.cardId) == String(id))
                    found = true;
            });
            return found;
        };
        return holds(game.skirmishShadowGroup) ? "shadow" : holds(game.skirmishFellowshipGroup) ? "freeps" : null;
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
        }).concat(this.siteAliases());
    },

    /** The names of the sites, both seats' (site1, site1opp, or the names they have been given). */
    siteAliases: function () {
        var paths = (this.state && this.state.board && this.state.board.sites) || {};
        var all = [];
        $.each(["freeps", "shadow"], function (i, seat) {
            $.each(paths[seat] || [], function (j, site) {
                if (site.alias)
                    all.push(site.alias);
            });
        });
        return all;
    },

    /** The name a site has when it has not been given one: site3, and site3opp for the opponent's. */
    defaultSiteAlias: function (seat, number) {
        return "site" + number + (this.isHumanSeat(seat) ? "" : "opp");
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
        var inBlock = (this.draft.steps[position.step - 1] || {}).anyOrder != null;
        var where = position.over ? "The game is over" : position.frontier ? "Past the end of the script (" + count + " steps)"
            : "Step " + position.step + (position.sub > 0 ? "." + position.sub : "") + " of " + count + (position.inHand ? ""
                : position.underWay ? (inBlock ? " (a block, between its decisions)" : " (which repeats, and is under way)") : " (waiting to come to it)");
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
            that.flushForSave(function (unsent) {
                that.call("POST", "save", {}, function (result) {
                    if (unsent == null) {
                        away();
                        return;
                    }
                    //(the step that was not taken would be lost by leaving: the rest is saved, and the author stays)
                    buttons.find("button").prop("disabled", false);
                    said.text("The rest is saved. " + unsent + " Fix it and save again, or discard it by leaving with Discard changes.").show();
                    that.afterSave(result, unsent);
                }, failed);
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
        var how = open.block != null ? "It goes into the block the game is in, as step " + open.block + "." + open.sub + "."
            : open.reuses != null ? "Step " + open.reuses + ", which says nothing yet, becomes the step for it."
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
    /** Whether a decision is waiting that no step covers, so that a step can be added for it (addStepHere). */
    unscripted: function () {
        var state = this.state;
        if (state == null || !state.session)
            return false;
        var position = state.position, waiting = state.waiting || {};
        return !position.over && !position.inHand && (waiting.human != null || (waiting.opponent != null && waiting.opponent.held));
    },

    addStepHere: function () {
        var that = this;
        this.flush(function () {
            that.ui.jump = true;
            that.ui.tab = "step";
            that.form = null;
            that.call("POST", "here", {});
        });
    },

    /**
     * "Repeat until": a step that goes on applying to every decision that fits its When, until one arrives that fits
     * its Until (which goes to the step after it).  Ticking it fills Until in with the phase after the one the step
     * is in (or the next turn), to be changed if that is not it.  While a step repeats, what its author does at it is
     * written into its Answer, one answer for each time round, and into what it allows (the server does that).
     */
    repeatRow: function (work) {
        var that = this;
        var blank = function (value) {
            return value == null || ("" + value).trim() == "";
        };
        var row = this.el("div", "sa-repeat");
        var line = this.el("div", "sa-row").appendTo(row);
        var untilInput = this.bound(work, "until", "hjson").attr("rows", 1)
            .attr("title", "The first decision that fits this ends the repeating, and goes to the step after this one. Written like When: "
                + "phase(shadow), turn(4), freeps, action, phase(regroup) ... or in full as the file has it, in HJSON.");
        var shown = this.el("div", "sa-repeat-more");
        var box = $("<input type='checkbox'/>").prop("checked", !blank(work.until)).change(function () {
            if (this.checked) {
                if (blank(work.until))
                    work.until = that.untilFromNext(that.form == null ? -1 : that.form.index) || that.nextUntil(work.when);
                untilInput.val(work.until || "").trigger("sa-fit");
                shown.show();
                if (blank(work.until))
                    untilInput.focus();
                else
                    that.stepChanged(true);
            } else {
                work.until = "";
                untilInput.val("").trigger("sa-fit");
                shown.hide();
                that.stepChanged(true);
            }
        });
        line.append(this.el("label", "sa-check").attr("title", "Go on applying to every decision that fits When, not only the first, until a decision arrives that fits what is given here")
            .append(box).append(document.createTextNode(" repeat until")));
        line.append(untilInput.addClass("sa-until"));
        shown.append(this.el("div", "sa-note", "It applies to every decision that fits When, until one arrives that fits this, which goes to the next step. "
            + "Play it through as the player should: each thing you do is added to Answer, in order, and to Allow."));
        //Repeating to do several things and then pass: Pass only once they have been done
        var plays = [];
        ("" + (work.allow.answers || "") + ", " + (work.answer || "")).replace(/play\(\s*([A-Za-z0-9_]+)\s*\)/g, function (all, alias) {
            if ($.inArray(alias, plays) < 0)
                plays.push(alias);
            return all;
        });
        if (blank(work.allow.passWhen)) {
            var gate = this.button("Pass only once " + (plays.length == 0 ? "they are" : plays.join(" and ") + (plays.length == 1 ? " is" : " are")) + " in play",
                plays.length == 0 ? "Allow play(card) first: this makes Pass wait until each card played here is on the table"
                    : "Sets Pass when: Pass is not allowed until each of these is on the table. (Say what to tell a player who passes too early in Otherwise.)", function () {
                    work.allow.passWhen = "[\n" + $.map(plays, function (alias) {
                        return "\t{\n\t\ttype: canSpot\n\t\tfilter: alias(" + alias + ")\n\t}";
                    }).join("\n") + "\n]";
                    var typed = that.form != null && that.form.typed != null ? that.form.typed.passWhen : null;
                    if (typed != null)
                        typed.input.val(work.allow.passWhen).trigger("sa-fit");
                    if (that.form != null && that.form.more != null)
                        that.form.more.attr("open", "open");
                    gate.remove();
                    that.stepChanged(true);
                });
            if (plays.length == 0)
                gate.prop("disabled", true);
            shown.append(this.el("div", "sa-bar").append(gate));
        }
        //...and to go on until they have been: Until, written out with what has to be on the table
        var inPlay = function (aliases) {
            return "{\n\tseat: " + (that.draft.human || "freeps") + "\n\tdecision: action\n\trequire: [\n" + $.map(aliases, function (alias) {
                return "\t\t{\n\t\t\ttype: canSpot\n\t\t\tfilter: alias(" + alias + ")\n\t\t}";
            }).join("\n") + "\n\t]\n}";
        };
        var until = this.button("Until " + (plays.length == 0 ? "they are" : plays.join(" and ") + (plays.length == 1 ? " is" : " are")) + " in play",
            plays.length == 0 ? "Allow play(card) first: this makes the step go on until each card played here is on the table"
                : "Sets Until: the step goes on until each of these is on the table, and the player's next decision after that goes to the next step", function () {
                work.until = inPlay(plays);
                untilInput.val(work.until).trigger("sa-fit");
                that.stepChanged(true);
            });
        if (plays.length == 0 || ("" + (work.until || "")) == inPlay(plays))
            until.prop("disabled", true);
        shown.append(this.el("div", "sa-bar").append(until));
        row.append(shown.toggle(!blank(work.until)));
        return row;
    },

    /** Whether a step can be joined into one before it that waits for the same (see joinAlike). */
    joinable: function (step) {
        return step != null && step.anyOrder == null && !step.pause && !step.checkpoint && step.end == null && (step.messages || []).length == 0
            && (step.until == null || ("" + step.until).trim() == "") && !step["if"] && !step.unless && !step["do"] && !step.skipWhen;
    },

    /** How many steps straight after this one wait for exactly what it waits for, and could be joined into it. */
    alikeAfter: function (index) {
        var steps = this.draft.steps, top = steps[index];
        if (top == null || top.anyOrder != null || top.pause || typeof top.when != "string" || this.isOpening(top))
            return 0;
        var count = 0;
        for (var i = index + 1; i < steps.length && typeof steps[i].when == "string" && steps[i].when == top.when && this.joinable(steps[i]); i++)
            count++;
        return count;
    },

    /** The When of the first step after this one that waits for something else: what this one would repeat until. */
    untilFromNext: function (index) {
        var steps = this.draft.steps;
        if (index < 0 || steps[index] == null)
            return null;
        for (var i = index + 1; i < steps.length; i++)
            if (JSON.stringify(steps[i].when) != JSON.stringify(steps[index].when) && steps[i].when != null)
                return typeof steps[i].when == "string" ? steps[i].when : JSON.stringify(steps[i].when, null, "\t");
        return null;
    },

    /** Joins the count steps after this one into it, as one step that repeats (see alikeAfter). */
    joinAlike: function (index, count) {
        var that = this;
        var steps = this.draft.steps, uids = this.state.uids;
        var top = steps[index];
        var allowOf = function (step) {
            return step.allow == null ? "" : typeof step.allow == "string" ? step.allow : (step.allow.answers || "");
        };
        var allowed = [], answers = [];
        for (var i = index; i <= index + count; i++) {
            $.each(this.splitTerms(allowOf(steps[i])), function (n, term) {
                term = term.replace(/\?$/, "");
                if ($.inArray(term, allowed) < 0)
                    allowed.push(term);
            });
            //(what the script plays: the step's answers, or what it allows if that is one thing)
            var own = this.splitTerms(steps[i].answer || "");
            if (own.length == 0 && this.splitTerms(allowOf(steps[i])).length == 1)
                own = this.splitTerms(allowOf(steps[i]));
            answers = answers.concat(own);
        }
        var set = {until: this.untilFromNext(index + count) || this.nextUntil(top.when)};
        if (top.allow != null && typeof top.allow == "object") {
            var allow = this.clone(top.allow);
            allow.answers = allowed.join(", ");
            set.allow = allow;
        } else if (allowed.length > 0) {
            set.allow = allowed.join(", ");
        }
        //(answers for some of the decisions and not others would not line up: then the script plays what is allowed)
        var answered = $.grep(steps.slice(index, index + count + 1), function (step) {
            return that.splitTerms(step.answer || "").length > 0 || that.splitTerms(allowOf(step)).length == 1;
        }).length == count + 1;
        var ops = [{op: "merge", uid: uids[index], set: set, unset: answered && answers.length > 0 ? [] : ["answer"]}];
        if (answered && answers.length > 0)
            set.answer = answers.join(", ");
        for (var k = 1; k <= count; k++)
            ops.push({op: "delete", uid: uids[index + k]});
        this.flush(function () {
            that.changeSteps(ops, index + 1, index + 1);
        });
    },

    /**
     * Whether what a step waits for, or ends at, is in the assignment or the skirmishes: phase(assignment),
     * phase(skirmish), an assign decision, fighting(...), inSkirmish(...), after(...), text(... skirmish ...), skirmish(n).
     */
    skirmishy: function (step) {
        if (step == null)
            return false;
        var that = this;
        var found = false;
        $.each(["when", "until"], function (i, name) {
            var value = step[name];
            if (value == null)
                return;
            var text = typeof value == "string" ? value : JSON.stringify(value);
            if (/phase\(\s*(assignment|skirmish)\s*\)|fighting\(|inSkirmish\(|\bafter\(|text\([^)]*skirmish|skirmish\(\s*\d+\s*\)/i.test(text))
                found = true;
            if ($.grep(that.splitTerms(typeof value == "string" ? value : ""), function (term) {
                return term.toLowerCase() == "assign";
            }).length > 0 || (typeof value == "object" && /^assign$/i.test(value.type || value.kind || "")))
                found = true;
        });
        return found;
    },

    /** Whether the step at this place in the script, or either of its neighbours, is in the assignment or the skirmishes. */
    nearSkirmishes: function (index) {
        var steps = this.draft.steps;
        return this.skirmishy(steps[index - 1]) || this.skirmishy(steps[index]) || this.skirmishy(steps[index + 1]);
    },

    /**
     * Turns a step of the script into a block with the step as the first of its steps.  The block waits as the step did
     * as far as the turn (and, for a block that is not the skirmishes of a turn, the phase) goes, and the step keeps the
     * rest of what it waits for; it ends where the step after says (or where the step said it repeats until), and
     * the step, which was one applying to one decision, now applies once in the block, as steps put into one do.
     * @param skirmishes whether the block is a turn's skirmishes (see skirmishPage), as "+ Skirmishes" makes one
     */
    convertStep: function (index, skirmishes) {
        var that = this;
        this.flush(function () {
            var steps = that.draft.steps;
            var uid = that.state.uids[index];
            var step = that.clone(steps[index]);
            if (step == null || step.anyOrder != null)
                return;
            var turn = null, phase = null, kept = [];
            var text = typeof step.when == "string" ? step.when : null;
            $.each(text == null ? [] : that.splitTerms(text), function (i, term) {
                if (turn == null && /^turn\(/i.test(term))
                    turn = term;
                else if (!skirmishes && phase == null && /^phase\(/i.test(term))
                    phase = term;
                else
                    kept.push(term);
            });
            var block = {};
            var when = $.grep([phase, turn], function (term) {
                return term != null;
            });
            if (skirmishes && turn == null && that.state.position.turn > 0)
                when = ["turn(" + that.state.position.turn + ")"];
            if (when.length > 0)
                block.when = when.join(", ");
            //Where it ends: where the step says it repeats until; or the step after's When; or the end of the turn's phase as a
            // step that repeats would end there; and as "+ Block" and "+ Skirmishes" end it, at the regroup, failing all that
            var until = step.until != null && ("" + step.until).trim() != "" ? step.until
                : skirmishes ? "phase(regroup)" : (that.untilFromNext(index) || that.nextUntil(step.when) || "phase(regroup)");
            block.until = until;
            //(what is said of a step as a whole of the script, and not of one decision, is said of the block)
            $.each(["skipWhen", "checkpoint", "if", "unless"], function (i, name) {
                if (step[name] != null && step[name] !== "" && step[name] !== false)
                    block[name] = step[name];
                delete step[name];
            });
            if (skirmishes)
                block.skirmishes = true;
            var repeating = step.until != null && ("" + step.until).trim() != "";
            delete step.until;
            if (text != null) {
                if (kept.length > 0)
                    step.when = kept.join(", ");
                else
                    delete step.when;
            }
            if (!step.pause && !repeating)
                step.once = true;
            block.anyOrder = [step];
            that.changeSteps([{op: "set", uid: uid, step: block}], index + 1, index + 1);
        });
    },

    /** What a step that repeats is likely to repeat until: the phase after the one its When names, or the next turn. */
    nextUntil: function (when) {
        var text = "" + (when || "");
        var phase = /phase\(\s*([a-z]+)\s*\)/i.exec(text);
        var turn = /turn\(\s*(\d+)\s*\)/i.exec(text);
        if (phase != null) {
            var at = $.inArray(phase[1].toLowerCase(), this.PHASES);
            if (at > -1 && at < this.PHASES.length - 1)
                return "phase(" + this.PHASES[at + 1] + ")";
        }
        return turn != null ? "turn(" + (parseInt(turn[1]) + 1) + ")" : "";
    },

    SILENT: "A pass the game makes by itself, when there is nothing to play: this step is used up by it with nobody asked. "
        + "Give it a message and the pass is asked for, and the message said over it.",

    /** A step that is only a pass and says nothing (see the server's ScenarioDirector.isSilentPass). */
    silentPass: function (step) {
        if (step == null || step.pause || step.anyOrder != null || (step.messages || []).length > 0 || step.checkpoint
            || (step.end != null && (typeof step.end == "string" ? step.end != "" : !!step.end.result)))
            return false;
        var that = this;
        var onlyPass = function (text) {
            var terms = that.splitTerms(text || "");
            return terms.length > 0 && $.grep(terms, function (term) {
                return term.replace(/\?$/, "") != "pass";
            }).length == 0;
        };
        var allow = step.allow == null ? null : typeof step.allow == "string" ? {answers: step.allow} : step.allow;
        var answers = step.answer != null && $.trim(step.answer) != "";
        var allows = allow != null && allow.passWhen == null && onlyPass(allow.answers);
        return (answers && onlyPass(step.answer) && (allow == null || allows)) || (allows && !answers);
    },

    /**
     * A silent pass in a place where the game passes by itself: the opponent's (with nothing to play it is never
     * asked), or the player's in a phase they are passed through.  (The player's anywhere else is asked all the same.)
     */
    passesByItself: function (step) {
        if (!this.silentPass(step) || typeof step.when != "string")
            return false;
        var terms = this.splitTerms(step.when);
        var seat = (terms[0] || "").toLowerCase();
        if ($.inArray(seat, this.SEATS) > -1 && !this.isHumanSeat(seat))
            return true;
        var phase = /phase\(\s*([a-z_]+)\s*\)/i.exec(step.when);
        var passed = (this.draft.options || {}).autoPass;
        if (phase == null || passed == null)
            return false;
        return passed === true || ("" + passed).toLowerCase() == "true" || $.inArray(phase[1].toLowerCase(), this.splitTerms("" + passed)) > -1;
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
        if (step.anyOrder != null)
            return {who: "Block", when: rest.join(" "), act: (step.skirmishes ? "skirmishes" : "any order") + ", until " + (typeof step.until == "string" ? step.until : "{...}")};
        if (this.isOpening(step))
            act = step["do"] || "";
        else if (step.answer)
            act = step.answer;
        else if (step.allow)
            act = typeof step.allow == "string" ? step.allow : (step.allow.answers || "pass when it may");
        return {who: who, when: rest.join(" "), act: act};
    },

    PHASE_NAMES: {
        opening: "Before the first turn", fellowship: "Fellowship", shadow: "Shadow", maneuver: "Maneuver", archery: "Archery",
        assignment: "Assignment", skirmish: "Skirmish", regroup: "Regroup", between_turns: "Between turns", block: "Several phases, in any order",
        skirmishes: "Assignment and skirmishes, in any order",
        "after-block": "After the block"
    },

    /**
     * The turn and phase a step comes in: {turn, phase}, each null where it cannot be told.  What its When names, or
     * else where the game was when the step had its first decision (seen: {turn, phase} from the server, for a step
     * that has been played to), or else what its When says about the decision (an assignment, a skirmish's).
     */
    placeOf: function (step, seen) {
        if (this.isOpening(step))
            return {turn: 0, phase: "opening"};
        var when = typeof step.when == "string" ? step.when : "";
        var turn = /turn\(\s*(\d+)\s*\)/i.exec(when), phase = /phase\(\s*([a-z_]+)\s*\)/i.exec(when);
        var said = phase != null ? phase[1].toLowerCase() : null;
        if (step.anyOrder != null)
            said = step.skirmishes ? "skirmishes" : said || "block";
        else if (said == null && seen != null)
            said = seen.phase;
        else if (said == null && /(^|,)\s*assign\s*(,|$)/i.test(when))
            said = "assignment";
        else if (said == null && /\b(inSkirmish|fighting)\(/i.test(when))
            said = "skirmish";
        return {
            turn: turn != null ? parseInt(turn[1]) : seen != null ? seen.turn : null,
            phase: said
        };
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
        //A row for each step; and under a block, a row for each of the steps in it
        var drawRow = function (step, index, sub) {
            var number = index + 1;
            var child = sub > -1;
            var uid = child ? that.kidsOf(index)[sub] : state.uids[index];
            var status = child ? ((state.substatuses || [])[index] || [])[sub] : state.statuses[index];
            var summary = that.summary(step);
            var row = that.el("div", "sa-step sa-status-" + (status || "unreached")).appendTo(list);
            row.toggleClass("sa-substep", child).toggleClass("sa-blockstep", step.anyOrder != null);
            row.toggleClass("sa-current", number == position.step && !position.over && !position.frontier && (!child || position.sub == sub + 1));
            row.toggleClass("sa-selected", number == that.ui.selected && (child ? that.ui.sub == uid : that.ui.sub == null));
            row.append(that.el("span", "sa-num", that.numberOf(index, sub)));
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
            if (step.once)
                tags.append(that.el("span", "sa-tag", "once").attr("title", "It applies to one decision. Without this, a step in a block applies to every decision that fits it, for as long as the block lasts."));
            //(faded, and said on hover: a pass the game makes by itself)
            if (that.passesByItself(step))
                row.addClass("sa-silent").attr("title", that.SILENT);
            if (step.checkpoint)
                tags.append(that.el("span", "sa-tag sa-flag", "\u2691").attr("title", "A checkpoint"));
            if (step.end)
                tags.append(that.el("span", "sa-tag", "end"));
            if (that.isAwaiting(uid))
                tags.append(that.el("span", "sa-tag sa-noanswer", "no answer yet").attr("title", "It says nothing yet about what is done. When the game comes to it, what you do there is written in."));
            var mistaken = child ? [] : that.leftOutOf(uid);
            if (mistaken.length > 0)
                tags.append(that.el("span", "sa-tag sa-mistake", "left out").attr("title", $.map(mistaken, function (one) {
                    return one.where.replace(/^step \d+( \([^)]*\))?\.?/, "") + ": " + one.message + "\nIt said: " + one.was;
                }).join("\n\n")));
            row.append(that.button("\u25b6", child ? "A fresh game, played by the script up to the first decision this step takes" : "A fresh game, played by the script up to this step", function () {
                that.goTo(number, child ? sub + 1 : null);
            }, "sa-go"));
            row.click(function () {
                that.ui.selected = number;
                that.ui.selectedUid = state.uids[index];
                that.ui.sub = child ? uid : null;
                that.ui.tab = "step";
                that.remember();
                that.render();
            });
        };
        //The steps are grouped by the turn and phase they are in, with a heading where that changes: a step that
        // says neither is in the same place as the one before it.  Each phase has a colour down the rows' edge.
        var group = {turn: null, phase: null};
        $.each(steps, function (index, step) {
            var where = that.placeOf(step, (state.places || [])[index]);
            var turn = where.turn != null ? where.turn : group.turn;
            var phase = where.phase != null ? where.phase : group.phase;
            if (turn !== group.turn || phase !== group.phase) {
                group = {turn: turn, phase: phase};
                var words = [];
                if (turn != null)
                    words.push("Turn " + turn);
                if (phase != null)
                    words.push(that.PHASE_NAMES[phase] || phase);
                if (words.length > 0)
                    list.append(that.el("div", "sa-tl-group sa-ph-" + (phase || "none") + (turn != null && turn % 2 == 0 ? " sa-turn-even" : ""), words.join(" \u00b7 ")));
            }
            var mark = function () {
                list.children(".sa-step").last().addClass("sa-ph-" + (group.phase || "none")).toggleClass("sa-turn-even", group.turn != null && group.turn % 2 == 0);
            };
            drawRow(step, index, -1);
            mark();
            $.each(step.anyOrder || [], function (sub, child) {
                drawRow(child, index, sub);
                mark();
            });
            //(a block may run through several phases: what follows it says where it is, or is in the block's last)
            if (step.anyOrder != null && typeof step.until == "string") {
                var ends = /phase\(\s*([a-z_]+)\s*\)/i.exec(step.until);
                if (ends != null)
                    group.phase = "after-block";
            }
        });

        var end = this.el("div", "sa-end").appendTo(list);
        if (position.over) {
            end.text("The game is over" + (position.result ? " (" + position.result.replace("_", " ") + ")" : "") + ". Go back to a step to carry on.");
        } else if (position.frontier) {
            end.addClass("sa-current");
            end.append(this.el("div", null, "The script ends here. What you do from here on " + (state.recording ? "is written down." : "is not written down.")));
        }
        var waiting = state.waiting || {};
        if (this.unscripted()) {
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
            if (open != null && open.problem == null && open.reuses == null && open.block == null && index == open.after && !position.over) {
                that.addStepHere();
                return;
            }
            that.insertStep(index, {when: (that.draft.human || "freeps") + ", action"}, true);
        }));
        foot.append(this.button("+ Block", "A block, after the selected step: steps for a stretch of the game whose decisions come in no order the script can know (which skirmish is fought first, how often each side acts in it). "
            + "Each decision goes to the first of the block's steps that fits it, until one arrives that fits the block's Until. It starts as this turn, until the regroup phase; with the game in it, what you do is written into it.", function () {
            var index = that.ui.selected == null ? steps.length : that.ui.selected;
            var block = {until: "phase(regroup)", anyOrder: []};
            if (position.turn > 0)
                block.when = "turn(" + position.turn + ")";
            that.insertStep(index, block);
        }));
        foot.append(this.button("+ Skirmishes", "A turn's assignment and skirmishes, after the selected step: a block whose steps are shown skirmish by skirmish (whichever is fought first), "
            + "and what comes after each. It starts as this turn, until the regroup phase; with the game in it, what you do is written into the skirmish you do it in.", function () {
            var index = that.ui.selected == null ? steps.length : that.ui.selected;
            var block = {skirmishes: true, until: "phase(regroup)", anyOrder: []};
            if (position.turn > 0)
                block.when = "turn(" + position.turn + ")";
            that.insertStep(index, block);
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
        var was = this.selection();
        if (select != null) {
            this.ui.selected = select;
            //(whichever step ends up there: its name is learnt from what comes back)
            this.ui.selectedUid = null;
            this.ui.sub = null;
            this.ui.hold = true;
        }
        this.form = null;
        this.remember();
        this.edit({steps: ops}, goTo, null, function () {
            that.refused(was);
        });
    },

    /** Which step the Step tab is on: {selected, selectedUid, sub}. */
    selection: function () {
        return {selected: this.ui.selected, selectedUid: this.ui.selectedUid, sub: this.ui.sub};
    },

    /**
     * A change to which steps there are was not taken by the server (what went wrong is said at the top of the
     * panel).  The Step tab goes back to the step it was on, with a form made afresh: the one it had was given up
     * when the change was sent.
     */
    refused: function (was) {
        this.ui.selected = was.selected;
        this.ui.selectedUid = was.selectedUid;
        this.ui.sub = was.sub;
        this.ui.subAt = null;
        this.ui.hold = false;
        this.form = null;
        this.remember();
        this.render();
    },

    /**
     * A change to which steps a block has.  In a block the game has been through it brings a fresh game, at the
     * block; in the block the game is in, the game carries on if it can.
     * @param subAt where in the block the step to turn to will come (from 0); nothing for the block itself
     */
    changeBlock: function (ops, index, subAt) {
        var that = this;
        var number = index + 1;
        var was = this.selection();
        this.ui.selected = number;
        this.ui.selectedUid = null;
        this.ui.sub = null;
        this.ui.subAt = subAt;
        this.ui.hold = true;
        this.form = null;
        this.remember();
        this.edit({steps: ops}, number < this.state.position.step ? number : null, null, function () {
            that.refused(was);
        });
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
    working: function (step, uid) {
        var that = this;
        var work = this.clone(this.own(step));
        //What is written out in full ({ ... }) is edited as the file has it: the server gives it that way, and
        // reads it that way
        var texts = uid == null ? {} : (((this.state && this.state.texts) || {})[uid] || {});
        var filed = function (value, text) {
            return value == null || typeof value == "string" ? value : text != null ? text : JSON.stringify(value, null, "\t");
        };
        $.each(this.TYPED, function (index, name) {
            work[name] = filed(work[name], texts[name]);
        });
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
        work.allow.passWhen = filed(work.allow.passWhen, texts.passWhen);
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
        $.each(["id", "note", "checkpoint", "pause", "once", "if", "unless", "when", "until", "skipWhen", "do", "skirmishes"], function (index, name) {
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
        var step = steps[index];
        //A block's page has a section for the block and one for each of the steps in it.  One of them is open,
        // and that is the form: the block's own, unless one of its steps is selected.
        var kids = this.kidsOf(index);
        var sub = step.anyOrder == null || this.ui.sub == null ? -1 : $.inArray(this.ui.sub, kids);
        var uid = sub > -1 ? kids[sub] : this.state.uids[index];
        var base = JSON.stringify(this.own(sub > -1 ? step.anyOrder[sub] : step));
        if (this.form != null && this.form.uid == uid && box.children().length > 0) {
            //The same step.  The form is left as it is unless the step has been changed from somewhere else (the
            // game giving it its answer, say).  Then whatever of the form has not been sent goes first, and the
            // form is made again from the step as it now is.
            this.form.index = index;
            this.form.sub = sub;
            var same = this.form.base == base || this.form.sending;
            if (same && this.around == this.aroundOf(index, sub)) {
                this.renderStepHead();
                return;
            }
            if (this.dirty().any) {
                this.renderStepHead();
                this.sendStep();
                return;
            }
            //What has changed is the rest of a block's page (another of its steps, written in by the game): it is
            // drawn again once nobody is typing in the form
            if (same && this.typingIn(box)) {
                this.redrawDue = true;
                this.renderStepHead();
                return;
            }
        } else if (this.form != null && this.dirty().any) {
            //Another step is being turned to: what was not sent of this one still goes
            this.sendStep();
        }
        this.makeForm(index, sub);
    },

    /**
     * What the Step tab's page shows besides the form: where the step comes in the script and, for a block, the
     * block's other steps and (when the form is one of its steps) what the block says of itself.
     */
    aroundOf: function (index, sub) {
        var step = this.draft.steps[index];
        //(where the step comes is part of it: the page's buttons know the step by that)
        if (step == null || step.anyOrder == null)
            return "" + index;
        var others = [];
        for (var i = 0; i < step.anyOrder.length; i++)
            others.push(i == sub ? null : step.anyOrder[i]);
        return JSON.stringify([index, this.kidsOf(index), sub > -1 ? this.own(step) : null, others, this.state.awaiting]);
    },

    /**
     * Makes the form for a step afresh, leaving whoever was typing in one of its messages where they were.
     * @param sub for one of the steps in a block, where it comes in the block (from 0); otherwise -1 or nothing
     */
    makeForm: function (index, sub) {
        var box = this.tabs.step;
        var caret = null;
        var active = document.activeElement;
        if (active != null && $.contains(box[0], active) && $(active).hasClass("sa-text"))
            caret = {message: box.find("textarea.sa-text").index(active), at: active.selectionStart};
        var top = this.draft.steps[index];
        sub = sub == null || top.anyOrder == null ? -1 : sub;
        var step = sub > -1 ? top.anyOrder[sub] : top;
        var uid = sub > -1 ? this.kidsOf(index)[sub] : this.state.uids[index];
        //(turned to another step, the tab starts at its top: where the one before was scrolled to means nothing here)
        var turned = this.form == null || this.form.index != index || (sub < 0 && this.form.uid != uid);
        var work = this.working(step, uid);
        this.form = {uid: uid, index: index, sub: sub, kind: sub > -1 ? "child" : top.anyOrder != null ? "block" : "step",
            step: work, origin: this.written(work), base: JSON.stringify(this.own(step))};
        this.around = this.aroundOf(index, sub);
        this.redrawDue = false;
        if (turned)
            this.panelShown = null;
        this.buildStepForm();
        if (turned && sub < 0)
            box.scrollTop(0);
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
     * @param then what to do once they have gone through (and at once if there is nothing to send); it is given
     *     whether they did
     * @param regardless whether 'then' is also to be done if the server refused them (the form then stays as it is)
     */
    sendStep: function (then, regardless) {
        var that = this;
        var form = this.form;
        clearTimeout(this.stepTimer);
        if (form == null) {
            if (then)
                then.call(this, true);
            return;
        }
        if (form.sending) {
            //One at a time: what has changed since goes when this one is back
            form.again = true;
            if (then)
                (form.after = form.after || []).push({then: then, regardless: regardless});
            return;
        }
        var changes = this.dirty();
        var text = JSON.stringify(changes);
        if (!changes.any || text == form.refused) {
            //(what was refused is refused again as it is: it is not sent again, and stays unsent)
            if (then && (!changes.any || regardless))
                then.call(this, !changes.any);
            return;
        }
        var sent = this.written(form.step);
        form.sending = true;
        var finished = function (went) {
            form.sending = false;
            var after = form.after || [];
            form.after = null;
            var run = function (list, did) {
                $.each(list, function (i, each) {
                    if (did || each.regardless)
                        each.then.call(that, did);
                });
            };
            if (form.again) {
                form.again = false;
                if (that.form === form) {
                    that.sendStep(function (did) {
                        run(after, did);
                    }, true);
                    return;
                }
            }
            run(after, went);
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
                var found = that.locate(form.uid);
                if (found != null) {
                    form.index = found.index;
                    form.sub = found.sub;
                    form.base = JSON.stringify(that.own(found.step));
                    //Anything else about the step that is not as the form has it was changed somewhere else
                    var theirs = that.written(that.working(found.step, form.uid));
                    that.asFiled(form, sent, theirs, changes);
                    var mine = that.written(form.step);
                    var stale = false;
                    for (name in theirs)
                        if (theirs.hasOwnProperty(name) && !changes.set.hasOwnProperty(name) && JSON.stringify(theirs[name]) != JSON.stringify(mine[name]))
                            stale = true;
                    for (name in mine)
                        if (mine.hasOwnProperty(name) && !theirs.hasOwnProperty(name) && !changes.set.hasOwnProperty(name))
                            stale = true;
                    if (stale && !form.again && !that.dirty().any && that.ui.tab == "step")
                        that.makeForm(found.index, found.sub);
                }
                that.renderNotices();
            }
            finished(true);
            if (then)
                then.call(that, true);
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
                if (then && regardless)
                    then.call(that, false);
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
                    if (then && regardless)
                        then.call(that, false);
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

    /**
     * What was typed in full ({ ... }) has been read by the server, and comes back written the way a file has it.
     * Where the form still has what was sent, it takes the file's way of writing it: left as it was typed, it would
     * look from then on as if somebody else had changed the step.
     */
    asFiled: function (form, sent, theirs, changes) {
        var now = this.written(form.step);
        var take = function (name, mine, filed, was) {
            var typed = (form.typed || {})[name];
            if (typed == null || filed == null || typeof filed != "string" || mine == filed || mine != was || document.activeElement === typed.input[0])
                return false;
            typed.object[name] = filed;
            typed.input.val(filed).trigger("sa-fit");
            return true;
        };
        $.each(this.TYPED, function (index, name) {
            if (changes.set.hasOwnProperty(name) && take(name, now[name], theirs[name], sent[name]))
                form.origin[name] = theirs[name];
        });
        var passWhen = function (allow) {
            return allow != null && typeof allow == "object" ? allow.passWhen : null;
        };
        if (changes.set.hasOwnProperty("allow") && take("passWhen", passWhen(now.allow), passWhen(theirs.allow), passWhen(sent.allow))) {
            var kept = this.clone(sent.allow);
            kept.passWhen = passWhen(theirs.allow);
            form.origin.allow = kept;
        }
    },

    /** An input that reads and writes one part of an object, and says so when it is changed. */
    bound: function (object, name, kind, changed) {
        var that = this;
        var input;
        if (kind == "area" || kind == "hjson")
            input = this.el("textarea", "sa-input").attr("rows", 3);
        else if (kind == "check")
            input = $("<input type='checkbox'/>");
        else
            input = $("<input type='text'/>").addClass("sa-input");
        if (kind == "check")
            input.prop("checked", !!object[name]);
        else
            input.val(object[name] == null ? "" : (typeof object[name] == "string" ? object[name] : JSON.stringify(object[name])));
        if (kind == "hjson") {
            //Something a file can have written out in full: it is typed as the file has it, and the server reads
            // it.  It is as tall as what is in it, and is sent when it is left, not while it is half written.
            input.addClass("sa-code").attr("spellcheck", "false").bind("sa-fit", function () {
                //(a line wider than the box wraps, and takes the rows it needs: of the box's own height, once it is on the page)
                var lines = ("" + input.val()).split("\n").length;
                input.attr("rows", Math.max(1, Math.min(16, lines)));
                var area = input[0];
                if (area.clientWidth > 0) {
                    input.css("height", "auto");
                    var extra = area.offsetHeight - area.clientHeight;
                    var room = parseFloat(input.css("line-height")) || 15;
                    if (area.scrollHeight > area.clientHeight)
                        input.css("height", Math.min(area.scrollHeight, room * 16) + extra);
                }
            }).trigger("sa-fit");
            //(it has no width until it is on the page, and can have none again while its tab is away)
            setTimeout(function () {
                input.trigger("sa-fit");
            }, 0);
            input.keydown(function (event) {
                //(a tab is part of what is being written, not a way out of the box)
                if (event.keyCode != 9 || event.shiftKey || event.ctrlKey || event.altKey || this.value.indexOf("\n") < 0)
                    return;
                event.preventDefault();
                var at = this.selectionStart, value = this.value;
                this.value = value.substring(0, at) + "\t" + value.substring(this.selectionEnd);
                this.selectionStart = this.selectionEnd = at + 1;
            });
            if (this.form != null)
                (this.form.typed = this.form.typed || {})[name] = {object: object, input: input};
        }
        var read = function (now) {
            var value = kind == "check" ? input.prop("checked") : input.val();
            object[name] = value;
            if (changed)
                changed(value);
            if (kind == "hjson")
                input.trigger("sa-fit");
            //What is typed goes to the server when the field is left (or Enter is pressed in a line), not each time
            // the typing pauses: each send can bring a fresh game, which is no thing to have happen mid-sentence
            if (now)
                that.stepChanged(true);
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

    /**
     * What has become of a step in the game in hand, in words: {words, status}.
     * @param sub for one of the steps in a block, where it comes in the block (from 0); otherwise -1
     */
    whereOf: function (index, sub) {
        var state = this.state, position = state.position;
        var child = sub > -1;
        var uid = child ? this.kidsOf(index)[sub] : state.uids[index];
        var status = (child ? ((state.substatuses || [])[index] || [])[sub] : state.statuses[index]) || "unreached";
        var here = index + 1 == position.step && !position.over;
        //(a block is where the game is for as long as it lasts; one of its steps, while its decision is in hand)
        var current = here && (child ? position.inHand && position.sub == sub + 1
            : position.inHand || (position.underWay && this.draft.steps[index].anyOrder != null));
        var words = current ? "Current Step" : status == "matched" ? "Completed Step" : status == "bypassed" ? "not on this way through"
            : status == "skipped" ? "skipped" : "Future Step";
        if (this.isAwaiting(uid))
            words += ", no answer yet";
        return {words: words, status: status};
    },

    renderStepHead: function () {
        var that = this;
        var form = this.form;
        if (form == null || this.stepHead == null)
            return;
        var number = form.index + 1;
        var where = this.whereOf(form.index, -1);
        this.stepHead.find(".sa-stepname").text("Step " + number + " of " + this.draft.steps.length);
        this.stepHead.find(".sa-addhere").prop("disabled", !this.unscripted());
        this.stepHead.find(".sa-stepwhere").text(where.words).attr("class", "sa-stepwhere sa-status-" + where.status);
        //(on a block's page, each of the steps in it says what has become of it)
        this.tabs.step.find(".sa-panelwhere").each(function () {
            var each = that.whereOf(form.index, parseInt($(this).attr("data-sub")));
            $(this).text(each.words).attr("class", "sa-panelwhere sa-status-" + each.status);
        });
    },

    /**
     * The Step tab: which step it is and what can be done with it as a whole, and then either its form or, for a
     * block, a section for the block and one for each of the steps in it (the open one of which has the form).
     */
    buildStepForm: function () {
        var that = this;
        var form = this.form, index = form.index, number = index + 1;
        var steps = this.draft.steps, uids = this.state.uids;
        var top = steps[index], topUid = uids[index];
        var isBlock = top.anyOrder != null;
        var position = this.state.position;
        var box = this.tabs.step.empty();

        var head = this.stepHead = this.el("div", "sa-stephead").appendTo(box);
        head.append(this.button("\u25c0", "The step before", function () {
            that.selectStep(number - 1);
        }).prop("disabled", number <= 1));
        head.append(this.el("span", "sa-stepname"));
        head.append(this.button("\u25b6", "The step after", function () {
            that.selectStep(number + 1);
        }).prop("disabled", number >= steps.length));
        //To the step the game is at; from there (or past it), to the last step
        head.append(this.button("\u25b6\u25b6", "To the step the game is at; from there or past it, to the last step", function () {
            var at = that.state.position.step;
            that.selectStep(number >= at || at > steps.length ? steps.length : at);
        }, "sa-tocurrent"));
        //The Timeline's "Add a step for this decision", from here: it goes where that decision comes, whatever step is shown
        head.append(this.button("+", "Add a step for the decision that is waiting, which no step covers (as on the Timeline). "
            + "It goes where that decision comes in the script, whatever step is shown here.", function () {
            that.addStepHere();
        }, "sa-addhere"));
        head.append(this.el("span", "sa-stepwhere"));

        var acts = this.el("div", "sa-bar").appendTo(box);
        acts.append(this.button("\u21bb Play to here", "A fresh game, played by the script up to this step", function () {
            that.goTo(number);
        }));
        acts.append(this.button("\u2191", "Move this step earlier", function () {
            that.flush(function () {
                that.changeSteps([{op: "move", uid: topUid, before: uids[index - 1]}], number - 1, number - 1);
            });
        }).prop("disabled", index == 0));
        acts.append(this.button("\u2193", "Move this step later", function () {
            that.flush(function () {
                that.changeSteps([{op: "move", uid: topUid, before: uids[index + 2] || null}], number, number + 1);
            });
        }).prop("disabled", index >= steps.length - 1));
        //A new step before this one, or after it.  It says nothing yet: what is done when the game comes to it is
        // written in.  (Put in where the game stands, with a decision waiting that no step covers, it is the step
        // for that decision.)
        var blank = function (at) {
            var open = that.state.uncovered;
            if (open != null && open.problem == null && open.reuses == null && open.block == null && at == open.after && !position.over) {
                that.addStepHere();
                return;
            }
            that.insertStep(at, {when: (that.draft.human || "freeps") + ", action"}, true);
        };
        acts.append(this.button("Before", "A new step before this one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows.", function () {
            blank(index);
        }));
        acts.append(this.button("After", "A new step after this one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows.", function () {
            blank(index + 1);
        }));
        acts.append(this.button("Delete", isBlock ? "Take this block, and the steps in it, out of the script" : "Take this step out of the script", function () {
            if (confirm(isBlock ? "Take step " + number + " out of the script, with the " + top.anyOrder.length + " step" + (top.anyOrder.length == 1 ? "" : "s") + " in it?"
                : "Take step " + number + " out of the script?"))
                that.changeSteps([{op: "delete", uid: topUid}], number, Math.max(1, number - 1));
        }, "sa-danger"));
        //Several steps in a row that wait for the same thing (a sanctuary's heals, one at a time) can be one step that
        // repeats, for as many of those decisions as come
        var alike = this.alikeAfter(index);
        if (alike > 0)
            acts.append(this.button("Join the next " + (alike == 1 ? "step" : alike + " steps") + " into this one",
                "The step" + (alike == 1 ? "" : "s") + " after this one wait" + (alike == 1 ? "s" : "") + " for the same as it does. Joined, this step repeats until the step after them comes: "
                + "however many of these decisions there are, one or several, they are all this step's. What each allowed it allows, and their answers are its answers, in order.", function () {
                    that.joinAlike(index, alike);
                }));
        //A step of the script can be made a block of its own (and in the assignment or the skirmishes, the skirmishes of the
        // turn); a block there can be made the turn's skirmishes.  (Not one of a block's steps, nor the skirmishes.)
        if (form.sub < 0 && !this.isOpening(top)) {
            var skirmishing = this.nearSkirmishes(index);
            if (!isBlock) {
                if (!skirmishing)
                    acts.append(this.button("Convert", "Turn this step into a block, with this step as its first step: for a stretch of the game whose decisions come in no order the script can know. "
                        + "It waits for the turn (and phase) this step does, and ends where the step after it begins.", function () {
                            that.convertStep(index, false);
                        }));
                else
                    acts.append(this.select([["", "Convert\u2026"], ["block", "Block"], ["skirmishes", "Skirmishes"]], "", function (kind) {
                        if (kind != "")
                            that.convertStep(index, kind == "skirmishes");
                    }).attr("title", "Turn this step into a block with this step as its first step: an ordinary one (Block), or a turn's skirmishes, shown skirmish by skirmish (Skirmishes)"));
            } else if (!top.skirmishes && skirmishing) {
                acts.append(this.button("Convert to Skirmishes", "Show this block's steps skirmish by skirmish, as a turn's assignment and skirmishes are written: what it waits for and ends at stays as it is.", function () {
                    that.flush(function () {
                        that.edit({steps: [{op: "merge", uid: topUid, set: {skirmishes: true}, unset: []}]}, null, function () {
                            that.form = null;
                            that.render();
                        });
                    });
                }));
            }
        }
        //A step next to a block can be made one of the block's
        if (!isBlock && !this.isOpening(top)) {
            var into = function (toIndex, first) {
                var kids = that.kidsOf(toIndex);
                var op = {op: "move", uid: topUid};
                if (first && kids.length > 0)
                    op.before = kids[0];
                else
                    op.into = uids[toIndex];
                that.flush(function () {
                    //(the block is where it was, or one earlier now that this step is not in front of it)
                    var blockAt = toIndex > index ? toIndex - 1 : toIndex;
                    var was = that.selection();
                    that.ui.selected = blockAt + 1;
                    that.ui.selectedUid = null;
                    that.ui.sub = null;
                    that.ui.subAt = first ? 0 : kids.length;
                    that.ui.hold = true;
                    that.form = null;
                    that.remember();
                    //(a step that says what only a step of the script can, an 'until' say, is not taken into a block)
                    that.edit({steps: [op]}, Math.min(number, blockAt + 1) <= position.step ? Math.min(number, blockAt + 1) : null, null, function () {
                        that.refused(was);
                    });
                });
            };
            if (index > 0 && steps[index - 1].anyOrder != null)
                acts.append(this.button("Into block " + index, "Make this step the last of the steps in the block before it", function () {
                    into(index - 1, false);
                }));
            if (index < steps.length - 1 && steps[index + 1].anyOrder != null)
                acts.append(this.button("Into block " + (number + 1), "Make this step the first of the steps in the block after it", function () {
                    into(index + 1, true);
                }));
        }
        $.each(this.leftOutOf(topUid), function (at, one) {
            var part = one.where.replace(/^step \d+( \([^)]*\))?\.?/, "");
            var note = that.el("div", "sa-note sa-mistaken").appendTo(box);
            note.append(that.el("div", null, "Left out when the file was opened" + (part == "" ? "" : ", from '" + part + "'") + ": " + one.message));
            note.append(that.el("pre", "sa-leftout-was", one.was));
        });

        if (!isBlock) {
            this.buildFields(box);
            this.renderStepHead();
            return;
        }

        if (top.skirmishes) {
            this.skirmishPage(box, index, top);
            return;
        }
        box.append(this.el("div", "sa-note", "A block: steps for a stretch of the game whose decisions come in no order the script can know. "
            + "Each decision goes to the first of its steps that fits it, wherever in the list that step comes, until a decision arrives that fits the block's Until."));
        var own = this.blockPanel(box, -1);
        if (own != null)
            this.buildFields(own);
        box.append(this.el("div", "sa-heading sa-substeps", top.anyOrder.length == 0 ? "It has no steps yet"
            : "Its " + top.anyOrder.length + " step" + (top.anyOrder.length == 1 ? "" : "s") + ", in whatever order the game asks for them"));
        $.each(top.anyOrder, function (sub) {
            var body = that.blockPanel(box, sub);
            if (body != null)
                that.buildFields(body);
        });
        var foot = this.el("div", "sa-bar").appendTo(box);
        foot.append(this.button("+ Step", "Another step in this block, after the others. It says nothing yet: what you do at its decision is written in. "
            + "(With the game in this block, and a decision waiting that none of its steps takes, it is the step for that decision.)", function () {
            var open = that.state.uncovered;
            if (open != null && open.problem == null && open.block == number && !position.over) {
                that.addStepHere();
                return;
            }
            that.insertInBlock(index, top.anyOrder.length, {once: true, when: (that.draft.human || "freeps") + ", action"}, true);
        }, "sa-primary"));
        foot.append(this.button("+ Pause", "A step of this block that is only read: once, over the first of the player's decisions that fits it", function () {
            that.insertInBlock(index, top.anyOrder.length, {pause: true, when: (that.draft.human || "freeps") + ", action", messages: [{text: "..."}]});
        }));
        this.renderStepHead();
        //(the open section is what was asked for: it is brought into sight)
        var opened = box.find(".sa-panel.sa-open");
        if (form.sub > -1 && opened.length > 0 && this.panelShown != form.uid)
            box.scrollTop(Math.max(0, box.scrollTop() + opened.position().top - 70));
        this.panelShown = form.uid;
    },

    /**
     * Where a step of a skirmishes block belongs: {part: "before" (the assignment, say), "first" (the first choice of
     * a skirmish), "fight" (a decision in that character's skirmish), "after" (between that skirmish and the next),
     * who}.  Read off its When: inSkirmish(x) (once fighting(x)), after(x), text(next skirmish).
     */
    skirmishPart: function (step) {
        var when = step.when;
        var fighting = null, after = null, next = false, skirmish = false;
        if (typeof when == "string") {
            $.each(this.splitTerms(when), function (i, term) {
                if (/^phase\(\s*skirmish\s*\)$/i.test(term))
                    skirmish = true;
                var m = /^(fighting|inSkirmish|after|text)\((.*)\)$/i.exec(term);
                if (m == null)
                    return;
                var what = m[1].toLowerCase(), arg = $.trim(m[2]);
                if (what == "fighting" || what == "inskirmish")
                    fighting = arg;
                else if (what == "after")
                    after = arg;
                else if (/next skirmish/i.test(arg))
                    next = true;
            });
        } else if (when != null && typeof when == "object") {
            fighting = when.inSkirmish || when.fighting || null;
            after = when.after || null;
            next = /next skirmish/i.test(when.text || "");
            skirmish = /^skirmish$/i.test(when.phase || "");
        }
        if (fighting != null)
            return {part: "fight", who: fighting};
        if (after != null)
            return {part: "after", who: after};
        return {part: next ? "first" : skirmish ? "every" : "before", who: null};
    },

    /**
     * A block that is a turn's skirmishes, shown the way they are fought: what comes before them (the assignment);
     * the first choice of which skirmish is fought; and for each character's skirmish, the steps of it in the order
     * they come, and then what comes after it (the choice of the next).  Underneath it is a block like any other: a
     * step of a skirmish says fighting(that character), a step after it after(that character), and each takes the
     * first decision that fits it.
     */
    skirmishPage: function (box, index, top) {
        var that = this;
        var form = this.form, number = index + 1;
        var human = this.draft.human || "freeps";
        var kids = this.kidsOf(index);
        box.append(this.el("div", "sa-note", "The skirmishes of a turn, whichever order they are fought in: what comes first, then each character's skirmish, "
            + "a step at a time in the order its decisions come, and what comes after it. What you do in the game is written into the skirmish it is done in."));
        var own = this.blockPanel(box, -1);
        if (own != null)
            this.buildFields(own);
        //Its steps by part, in their order in the block
        var parts = {before: [], first: [], every: []}, fights = [], afters = {};
        var cards = (this.state.board && this.state.board.cards) || [];
        var cardOf = function (alias) {
            return $.grep(cards, function (card) {
                return card.alias == alias;
            })[0];
        };
        //(a skirmish is the Free Peoples character's: a step for one a minion is in is for whichever skirmish that is)
        var minion = function (alias) {
            var card = cardOf(alias);
            return card != null && card.owner != human;
        };
        var shortName = function (alias) {
            var card = cardOf(alias);
            return card == null ? alias : card.name.split(",")[0];
        };
        $.each(top.anyOrder, function (sub, step) {
            var place = that.skirmishPart(step);
            if (place.part == "fight" || place.part == "after") {
                if ($.inArray(place.who, fights) < 0)
                    fights.push(place.who);
                var key = place.part + ":" + place.who;
                (parts[key] = parts[key] || []).push(sub);
                if (place.part == "after")
                    afters[place.who] = true;
            } else {
                parts[place.part].push(sub);
            }
        });
        $.each(this.ui.newFights || [], function (i, who) {
            if ($.inArray(who, fights) < 0 && $.inArray(who, that.aliases()) > -1)
                fights.push(who);
        });
        var firstChoice = parts.first.length > 0 ? parts.first[0] : top.anyOrder.length;
        var group = function (title, note, subs, add) {
            var section = that.el("div", "sa-skirmishpart").appendTo(box);
            section.append(that.el("div", "sa-heading sa-substeps", title));
            if (note)
                section.append(that.el("div", "sa-note", note));
            $.each(subs, function (i, sub) {
                var body = that.blockPanel(section, sub, {prev: i > 0 ? subs[i - 1] : null, next: i < subs.length - 1 ? subs[i + 1] : null});
                if (body != null)
                    that.buildFields(body);
            });
            if (add != null)
                section.append(that.el("div", "sa-bar").append(add));
            return section;
        };
        var adding = function (label, title, at, step) {
            return that.button(label, title, function () {
                var open = that.state.uncovered;
                if (open != null && open.problem == null && open.block == number && !that.state.position.over && that.fitsHere(step)) {
                    that.addStepHere();
                    return;
                }
                that.insertInBlock(index, at, step, true);
            });
        };
        var after = function (subs) {
            return subs.length == 0 ? top.anyOrder.length : subs[subs.length - 1] + 1;
        };
        group("Before the skirmishes, or at any moment", "The assignment, and anything else not said to be in one skirmish or after it.", parts.before,
            adding("+ Step", "A step for a decision before the skirmishes", after(parts.before), {once: true, when: human + ", assign"}));
        group("Choosing the first skirmish", "Said when the player chooses which skirmish is fought first.", parts.first,
            parts.first.length > 0 ? null : adding("+ Step", "A step for the first choice of skirmish", top.anyOrder.length,
                {once: true, when: human + ", cards, text(next skirmish)"}));
        if (parts.every.length > 0)
            group("In every skirmish", "Steps for any skirmish, whoever is fighting it: after the steps of the skirmish being fought, which come first if they fit.", parts.every, null);
        //(the steps for one skirmish come before the ones for every skirmish, which would otherwise take its decisions)
        var firstEvery = parts.every.length > 0 ? parts.every[0] : null;
        $.each($.grep(fights, minion), function (i, who) {
            group("Any skirmish " + shortName(who) + " (" + who + ") is in", "Steps for the skirmish this minion is in, whichever character it fights.",
                parts["fight:" + who] || [], null);
        });
        $.each($.grep(fights, function (who) {
            return !minion(who);
        }), function (i, who) {
            var name = shortName(who);
            var fight = parts["fight:" + who] || [];
            var end = fight.length > 0 ? after(fight) : firstEvery != null ? firstEvery : top.anyOrder.length;
            if (fight.length > 0 && firstEvery != null && end > firstEvery)
                end = firstEvery;
            group(name + "'s skirmish", null, fight,
                adding("+ Step", "A step for the next decision in this skirmish", end, {once: true, when: human + ", action, inSkirmish(" + who + ")"}));
            var afterSubs = parts["after:" + who] || [];
            //(before the first choice's step, which would otherwise take the choices after a skirmish too)
            group("After " + name + "'s skirmish", "Said at the first decision after it, whatever that is: the choice of the next skirmish, the next skirmish's own, "
                + "a fierce assignment, or the first of the Regroup phase (the block's end does not cut it off).", afterSubs,
                afterSubs.length > 0 ? null : adding("+ Step", "A pause read at the first decision after this skirmish", Math.min(firstChoice, top.anyOrder.length),
                    {once: true, pause: true, when: human + ", after(" + who + ")", messages: [{text: "..."}]}));
        });
        //Another character's skirmish, to write steps for
        var mine = $.grep(cards, function (card) {
            return card.owner == human && (card.type == "COMPANION" || card.type == "ALLY") && $.inArray(card.alias, fights) < 0;
        });
        var foot = this.el("div", "sa-bar").appendTo(box);
        if (mine.length > 0)
            foot.append(this.select([["", "+ a skirmish for\u2026"]].concat($.map(mine, function (card) {
                return [[card.alias, card.alias + " (" + card.name + ")"]];
            })), "", function (who) {
                if (who == "")
                    return;
                that.ui.newFights = (that.ui.newFights || []).concat([who]);
                that.remember();
                that.flush(function () {
                    that.form = null;
                    that.render();
                });
            }).attr("title", "Steps for that character's skirmish, whenever it is fought"));
        foot.append(this.button("+ Pause", "A step of this block that is only read: once, over the first of the player's decisions that fits it", function () {
            that.insertInBlock(index, top.anyOrder.length, {pause: true, when: human + ", action", messages: [{text: "..."}]});
        }));
        this.renderStepHead();
        var opened = box.find(".sa-panel.sa-open");
        if (form.sub > -1 && opened.length > 0 && this.panelShown != form.uid)
            box.scrollTop(Math.max(0, box.scrollTop() + opened.position().top - 70));
        this.panelShown = form.uid;
    },

    /** Whether the decision waiting (no step of the block covering it) is one the step about to be added is for. */
    fitsHere: function (step) {
        var open = this.state.uncovered;
        if (open == null || open.when == null)
            return false;
        var want = this.skirmishPart(step), have = this.skirmishPart({when: open.when});
        return want.part == have.part && want.who == have.who;
    },

    /**
     * One section of a block's page: the block itself (sub -1), or one of the steps in it.  Its head says what it
     * is and is the way to open it; the one that is open is the form.
     * @return where the open section's form goes; null for a section that is not open
     */
    blockPanel: function (box, sub, order) {
        var that = this;
        var form = this.form, index = form.index, number = index + 1;
        var top = this.draft.steps[index], kids = this.kidsOf(index);
        var child = sub > -1;
        var step = child ? top.anyOrder[sub] : top;
        var uid = child ? kids[sub] : this.state.uids[index];
        var open = form.sub == sub;
        var panel = this.el("div", "sa-panel " + (child ? "sa-panel-step" : "sa-panel-block") + (open ? " sa-open" : "")).appendTo(box);
        var head = this.el("div", "sa-panelhead").appendTo(panel);
        var title = this.el("div", "sa-paneltitle").attr("title", open ? "" : "Open this to change it").appendTo(head);
        title.append(this.el("span", "sa-twist", open ? "\u25be" : "\u25b8"));
        var summary = this.summary(step);
        if (child)
            title.append(this.el("span", "sa-num", this.numberOf(index, sub)));
        title.append(this.el("span", "sa-who sa-who-" + summary.who.toLowerCase(), child ? summary.who : "Block"));
        var what = this.el("span", "sa-what").appendTo(title);
        what.append(this.el("span", "sa-when", summary.when != "" ? summary.when : child ? "any decision" : "every decision"));
        if (summary.act != "")
            what.append(this.el("span", "sa-act", summary.act));
        if (step.id)
            what.append(this.el("span", "sa-id", step.id));
        var tags = this.el("span", "sa-tags").appendTo(title);
        var count = this.messagesOf(step).length;
        if (step.once)
            tags.append(this.el("span", "sa-tag", "once"));
        if (step["do"])
            tags.append(this.el("span", "sa-tag", "do").attr("title", step["do"]));
        if (count > 0)
            tags.append(this.el("span", "sa-tag sa-bubbles", "\u25ac" + count).attr("title", count + " message" + (count == 1 ? "" : "s")));
        if (step.checkpoint)
            tags.append(this.el("span", "sa-tag sa-flag", "\u2691").attr("title", "A checkpoint"));
        if (step.end)
            tags.append(this.el("span", "sa-tag", "end"));
        title.click(function () {
            if (!open)
                that.openPanel(sub);
        });
        var line = this.el("div", "sa-panelacts").appendTo(head);
        if (child) {
            line.append(this.el("span", "sa-panelwhere").attr("data-sub", sub));
            line.append(this.button("\u25b6", "A fresh game, played by the script up to the first decision this step takes", function () {
                that.goTo(number, sub + 1);
            }));
            var position = this.state.position;
            var holding = position.step == number && position.inHand && !position.over && position.sub == sub + 1;
            line.append(this.button("Insert", holding
                ? "A new step in the block, before this one, for the decision waiting now and no other (in a skirmish, that skirmish's): it takes the decision from this step. Give it a message, then do what the player should, and that is written in as what it allows."
                : "A new step in the block, before this one. It says nothing yet: what you do at its decision is written in.", function () {
                if (holding) {
                    that.flush(function () {
                        that.ui.jump = true;
                        that.ui.tab = "step";
                        that.form = null;
                        that.call("POST", "before", {});
                    });
                    return;
                }
                that.insertInBlock(index, sub, {once: true, when: (that.draft.human || "freeps") + ", action"}, true);
            }));
            //(on a skirmishes page, earlier and later among the steps of the same part of it)
            var prev = order != null ? order.prev : sub - 1, next = order != null ? order.next : sub + 1;
            line.append(this.button("\u2191", "Earlier in the block. (Where a step comes matters only when a decision fits more than one of them: it goes to the first.)", function () {
                that.flush(function () {
                    that.changeBlock([{op: "move", uid: uid, before: kids[prev]}], index, prev);
                });
            }).prop("disabled", prev == null || prev < 0));
            line.append(this.button("\u2193", "Later in the block", function () {
                that.flush(function () {
                    var op = {op: "move", uid: uid};
                    if (kids[next + 1] != null)
                        op.before = kids[next + 1];
                    else
                        op.into = that.state.uids[index];
                    that.changeBlock([op], index, next);
                });
            }).prop("disabled", next == null || next > kids.length - 1));
            line.append(this.button("Out", "Take this step out of the block, and make it the step of the script after the block", function () {
                that.flush(function () {
                    that.changeSteps([{op: "move", uid: uid, before: that.state.uids[index + 1] || null}], number, number + 1);
                });
            }));
            line.append(this.button("\u00d7", "Take this step out of the script", function () {
                if (confirm("Take step " + that.numberOf(index, sub) + " out of the script?"))
                    that.flush(function () {
                        that.changeBlock([{op: "delete", uid: uid}], index, null);
                    });
            }, "sa-danger"));
        }
        if (!open) {
            //(what it says, so that the block can be read through without opening each of its steps)
            $.each(this.messagesOf(step), function (i, message) {
                panel.append(that.el("div", "sa-panelsays", typeof message == "string" ? message : message.text).click(function () {
                    that.openPanel(sub);
                }));
            });
            return null;
        }
        return this.el("div", "sa-panelbody").appendTo(panel);
    },

    /** Opens one of the sections of a block's page: the block's own (-1), or one of its steps. */
    openPanel: function (sub) {
        var kids = this.kidsOf(this.ui.selected - 1);
        this.ui.sub = sub > -1 ? kids[sub] : null;
        this.panelShown = null;
        this.remember();
        //(what has not been sent of the section being left goes as the form for the other is made)
        this.render();
    },

    /**
     * Puts a step into a block, and turns to it.
     * @param at where in the block it goes (from 0; the number of steps the block has puts it last)
     * @param awaiting whether the step is to take its answer from what is done when the game comes to it
     */
    insertInBlock: function (index, at, step, awaiting) {
        var that = this;
        this.flush(function () {
            var kids = that.kidsOf(index);
            var op = {op: "insert", step: step};
            if (kids[at] != null)
                op.before = kids[at];
            else
                op.into = that.state.uids[index];
            if (awaiting)
                op.awaiting = true;
            that.ui.tab = "step";
            that.changeBlock([op], index, at);
        });
    },

    /**
     * The form for one step: a step of the script, what a block says of itself, or one of the steps in a block
     * (form.kind says which).
     */
    buildFields: function (box) {
        var that = this;
        var form = this.form, work = form.step, index = form.index, number = index + 1;
        var child = form.kind == "child", block = form.kind == "block";
        var position = this.state.position;
        var inHandNow = number == position.step && position.inHand && (position.sub || 0) == (child ? form.sub + 1 : 0);
        if (inHandNow && !block && !work.pause && !this.isOpening(work))
            box.append(this.el("div", "sa-bar").append(this.button("Answer it again", "This step forgets what it allows and how it is answered, and takes both from what you do next. Its messages stay.", function () {
                that.retake();
            })));
        if (this.isAwaiting(form.uid))
            box.append(this.el("div", "sa-note sa-awaiting", inHandNow
                ? "This step says nothing yet about what is done. Do it in the game now, and what you do is written in as what it allows. Messages put on it here first are shown before it is answered."
                : "This step says nothing yet about what is done. When the game comes to it, what you do there is written in as what it allows."));

        if (!block && this.passesByItself(this.written(work)))
            box.append(this.el("div", "sa-note sa-awaiting", this.SILENT));

        /* ---- what it waits for ---- */
        var opening = this.isOpening(work);
        var when = this.el("div", "sa-section").appendTo(box);
        when.append(this.el("div", "sa-heading", opening ? "The opening" : block ? "Covers the decisions that are"
            : work.pause ? (child ? "Read once, over the player's first decision that is" : "Read over the player's decision when it is")
            : child ? (work.once ? "Applies to one decision that is" : "Applies to each decision that is") : "Applies to the decision that is"));
        if (block)
            when.append(this.el("div", "sa-note", "Every decision that fits this is put to the steps below, from where the script comes to the block until it ends. Empty: every decision there is."));
        if (opening) {
            when.append(this.el("div", "sa-note", "Read before the first turn begins, on the board as the scenario lays it out: nobody has been asked anything, and nothing has been drawn. "
                + "Its messages are read one after another; then what it does is done; then the first turn begins. Only a scenario's first step can be this."));
            when.append(this.field("name", this.bound(work, "id", "text").addClass("sa-short"), "Only to make reports easier to read"));
        } else {
            var raw = this.bound(work, "when", "hjson", function () {
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
                raw.val(work.when).trigger("sa-fit");
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
            var pickers = this.el("div", "sa-row").append(seat).append(type).append(phase).append(turn);
            var syncWhen = function () {
                //(written out in full, as { ... }, it is not a line of terms to be taken apart)
                var full = /^\s*([{\[]|when\s*:)/.test(work.when || "");
                pickers.toggle(!full);
                if (full)
                    return;
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
            when.append(pickers);
            when.append(this.field("When", raw, "seat, kind of decision, phase(...), turn(...), skirmish(n), inSkirmish(card), after(card), offers(card), text(part of what the decision says). "
                + "Or written out in full, as the file has it, in HJSON: { seat: freeps, require: { ... } }. What is copied out of a file can be pasted here, with or without 'when:' in front."));
        }
        var flagsRow = this.el("div", "sa-row").appendTo(when).toggle(!opening);
        if (!block)
            flagsRow.append(this.el("label", "sa-check").attr("title", child ? "A step that is only read: it has messages, and no answer. It is read once, and the decision then goes to whichever of the block's other steps fits it."
                : "A step that is only read: it has messages, and no answer. The decision goes to the step after it.")
                .append(this.bound(work, "pause", "check", function () {
                    that.sendStep();
                })).append(document.createTextNode(" pause")));
        if (child && !work.pause)
            flagsRow.append(this.el("label", "sa-check").attr("title", "It applies to one decision, and is then done with. Without this it applies to every decision that fits it, for as long as the block lasts (and what it says is said the first time).")
                .append(this.bound(work, "once", "check", function () {
                    that.sendStep();
                })).append(document.createTextNode(" once")));
        if (!child)
            flagsRow.append(this.el("label", "sa-check").attr("title", "A player who fails later can come back to here")
                .append(this.bound(work, "checkpoint", "check")).append(document.createTextNode(" checkpoint")));
        flagsRow.append(this.field("name", this.bound(work, "id", "text").addClass("sa-short"), "Only to make reports easier to read"));
        if (!child && !block && !opening && !work.pause)
            when.append(this.repeatRow(work));
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
        } else if (block) {
            var ends = this.el("div", "sa-section").appendTo(box);
            ends.append(this.el("div", "sa-heading", "Where it ends"));
            ends.append(this.field("Until", this.bound(work, "until", "hjson"),
                "The block is over at the first decision that fits this, which goes to the step after the block: phase(regroup), say, or turn(3). A block has to have one. "
                + "Written like When: a line of terms, or in full as the file has it, in HJSON."));
            ends.append(this.field("Skip when", this.bound(work, "skipWhen", "hjson"), "Given up on, before any of its steps has applied, if a decision that fits this arrives"));
            ends.append(this.el("label", "sa-check sa-block").attr("title", "Its steps shown by the skirmish they are in, and after: for a turn's assignment and skirmishes")
                .append(this.bound(work, "skirmishes", "check", function () {
                    //(the page is laid out afresh, skirmish by skirmish or not, once the change has gone through)
                    setTimeout(function () {
                        that.sendStep(function () {
                            that.form = null;
                            that.render();
                        });
                    }, 0);
                })).append(document.createTextNode(" the skirmishes of a turn (shown skirmish by skirmish)")));
        } else if (!work.pause) {
            var does = this.el("div", "sa-section").appendTo(box);
            does.append(this.el("div", "sa-heading", "What is done"));
            var allowInput = this.bound(work.allow, "answers", "text");
            var answerInput = this.bound(work, "answer", "text");
            var seatTerm = (this.splitTerms(typeof work.when == "string" ? work.when : "")[0] || "").toLowerCase();
            var players = !work.when || typeof work.when != "string" || $.inArray(seatTerm, this.SEATS) < 0 || this.isHumanSeat(seatTerm);
            if (players) {
                //A step where the player may do whatever they like: what is written is only what the script plays
                var free = ("" + (work.allow.answers || "")).trim() == "" && ("" + (work.answer || "")).trim() != "";
                does.append(this.el("label", "sa-check sa-block").attr("title", "Ticked, the step allows anything, and its Answer is only what the script plays when it plays itself (as Check does)")
                    .append($("<input type='checkbox'/>").prop("checked", free).change(function () {
                        if (this.checked) {
                            if (("" + (work.answer || "")).trim() == "")
                                work.answer = work.allow.answers;
                            work.allow.answers = "";
                        } else {
                            work.allow.answers = work.answer || "";
                        }
                        allowInput.val(work.allow.answers).prop("disabled", this.checked);
                        answerInput.val(work.answer || "");
                        that.stepChanged(true);
                    })).append(document.createTextNode(" the player may do anything here")));
                allowInput.prop("disabled", free);
            }
            does.append(this.field("Allow", allowInput,
                "What the player may do: play(card), use(card), choose(card, card), option(text), number(n), assign(character: minion + minion), pass. Several, separated by commas: each is allowed when it is on offer (the other copy of a card, a way the player did not go), and something in it has to be. Empty: anything. "
                + "choose(...) can pick a card by its skirmish and narrow it with card filters and a number: choose(skirmishing), choose(skirmishing(aragorn), title(Orthanc Warrior), 2), choose(aragorn, not(exhausted)). Sites are site1, site2... (site1opp... for the opponent's)."));
            //With its decision on the table: what else the decision offers, to be allowed as well with a click
            var asked = this.state.waiting && this.state.waiting.human;
            if (players && inHandNow && asked != null && !work.pause) {
                var offered = $.map(asked.options || [], function (option) {
                    return option.term || null;
                });
                if (asked.pass)
                    offered.push("pass");
                var chips = this.el("div", "sa-row sa-wrap sa-offered");
                var have = function () {
                    return that.splitTerms(work.allow.answers || "");
                };
                $.each(offered, function (i, term) {
                    if ($.inArray(term, have()) > -1)
                        return;
                    chips.append(that.button("+ " + term, "Allow this as well", function () {
                        var now = have();
                        now.push(term);
                        work.allow.answers = now.join(", ");
                        allowInput.val(work.allow.answers);
                        $(this).remove();
                        that.stepChanged(true);
                    }, "sa-inline"));
                });
                if (chips.children().length > 0)
                    does.append(this.el("div", "sa-note", "Also on offer here:")).append(chips);
            }
            does.append(this.field("Answer", answerInput,
                "What the script plays: the opponent's answer, or the player's when the script plays itself. Empty: the first thing Allow lets the player do that is on offer"));
            does.append(this.field("Do", this.bound(work, "do", "text"),
                "Changes to the board when the step's decision arrives: move(card, seat.zone), draw(n), wound(card, n), heal(card, n), twilight(n), burdens(n), attach(card, bearer), stack(card, host). "
                + "The decision itself was worked out before them: what they put in a hand can be played from the next decision on."));
            var more = $("<details class='sa-more'></details>").appendTo(does);
            more.append($("<summary></summary>").text(child ? "Wrong clicks" : "Wrong clicks, giving up"));
            more.append(this.field("Otherwise", this.bound(work.allow, "otherwise", "area").attr("rows", 2), "What to say when the player clicks something legal that is not allowed. Empty: the stock line."));
            more.append(this.field("Pass when", this.bound(work.allow, "passWhen", "hjson"), "Requirements (as on a card) that have to hold before Pass is allowed, as the file has them, in HJSON: { type: canSpot, filter: alias(aragorn) }"));
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
            //(Until is with "repeat until", under When)
            if (!child)
                more.append(this.field("Skip when", this.bound(work, "skipWhen", "hjson"), "Given up on if a decision that fits this arrives first"));
            if (work.allow.passWhen != null || work.allow.otherwise || work.skipWhen != null)
                more.attr("open", "open");
            this.form.more = more;
        }

        /* ---- what it says ---- */
        if (!block) {
            var says = this.el("div", "sa-section").appendTo(box);
            says.append(this.el("div", "sa-heading", "Messages"));
            this.messageBox = this.el("div", "sa-messages").appendTo(says);
            this.drawMessages(work.messages, this.messageBox, false);
        }

        /* ---- the rest ---- */
        var rest = this.el("div", "sa-section").appendTo(box);
        work.end = work.end || {result: "", messages: []};
        if (!block) {
            var endBox = this.el("div", "sa-messages");
            rest.append(this.field("Ends the scenario", this.select([["", "no"], ["complete", "yes: complete"], ["fail", "yes: failed"]], work.end.result || "", function (value) {
                work.end.result = value;
                endBox.toggle(value != "");
                that.stepChanged(true);
            }), "Once this step has been answered (or, for a pause, read)"));
            rest.append(endBox);
            this.drawMessages(work.end.messages, endBox, true);
            endBox.toggle((work.end.result || "") != "");
        }
        rest.append(this.field("Note", this.bound(work, "note", "area").attr("rows", 2), "For whoever works on the scenario next. Never shown to a player."));
    },

    selectStep: function (number) {
        if (number < 1 || number > this.draft.steps.length)
            return;
        this.ui.selected = number;
        this.ui.selectedUid = this.state.uids[number - 1];
        this.ui.sub = null;
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
        var child = form.sub > -1;
        box.empty();
        $.each(messages, function (position, message) {
            var card = that.el("div", "sa-message").appendTo(box);
            var focus = function () {
                if (!closing)
                    that.focusMessage = {step: number, sub: child ? form.sub + 1 : 0, index: position};
            };
            card.bind("focusin", focus);

            var top = that.el("div", "sa-messagehead").appendTo(card);
            top.append(that.el("span", "sa-messagenum", (position + 1) + "."));
            top.append(that.button("Insert", "A new message before this one", function () {
                messages.splice(position, 0, {text: ""});
                that.drawMessages(messages, box, closing);
                box.find(".sa-message").eq(position).find("textarea").first().focus();
            }));
            if (!closing) {
                //(from one step of the script to the next: the steps in a block come in no order to move it along)
                if (!child) {
                    top.append(that.button("\u25c0 Earlier", "Move this message to the step before", function () {
                        that.moveMessage(position, index - 1);
                    }).prop("disabled", index == 0 || steps[index - 1].anyOrder != null));
                    top.append(that.button("Later \u25b6", "Move this message to the step after", function () {
                        that.moveMessage(position, index + 1);
                    }).prop("disabled", index >= steps.length - 1 || steps[index + 1].anyOrder != null));
                }
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
            //A card in the skirmish being fought may not be the one fighting there in another game (the player
            // assigned the other minion, say): it can be pointed at as "whichever is fighting on that side" instead
            var either = that.el("div", "sa-note sa-either").hide();
            anchorRow.append(that.button("Pick", "Click the thing on the table", function () {
                focus();
                that.pick(function (picked) {
                    var was = $.trim(highlight.val());
                    anchor.val(picked).change();
                    //Usually what is pointed at is what is outlined
                    if (was == "")
                        highlight.val(picked).change();
                    var side = that.skirmishSideOf(picked);
                    either.empty().hide();
                    if (side != null) {
                        var name = picked.replace(/^card:/, "");
                        either.append(document.createTextNode(name + " is " + (side == "shadow" ? "a minion" : "the character") + " in the skirmish being fought. "
                            + "If another game can have a different card there, point at whichever is: "));
                        either.append(that.button(side == "shadow" ? "Whichever minion is fighting" : "Whichever character is fighting",
                            "Points at (and outlines) the cards on that side of the skirmish being fought, whichever they are: ui:skirmish:" + side, function () {
                                anchor.val("ui:skirmish:" + side).change();
                                if ($.trim(highlight.val()) == picked)
                                    highlight.val("ui:skirmish:" + side).change();
                                either.hide();
                            }, "sa-inline"));
                        either.show();
                    }
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
            card.append(either);

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
        var target = this.written(this.working(steps[toIndex], this.state.uids[toIndex]));
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
        this.ui.sub = null;
        this.ui.hold = true;
        this.focusMessage = {step: toIndex + 1, sub: 0, index: toIndex < form.index ? target.messages.length - 1 : 0};
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
        if (typeof from.when == "string" && !/^\s*([{\[]|when\s*:)/.test(from.when)) {
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
        if (form.sub > -1) {
            //In a block the pause is one of its steps, before this one: it is read first, and once
            this.changeBlock([merge, {op: "insert", before: form.uid, step: pause}], form.index, form.sub);
            return;
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
        if (form == null || overlay.step == null || overlay.step.step != form.index + 1 || (overlay.step.sub || 0) != (form.sub > -1 ? form.sub + 1 : 0))
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
        if (focus == null || step == null || focus.step != step.step || (focus.sub || 0) != (step.sub || 0))
            return;
        this.showOnScreen();
    },

    /** Puts the message being worked on up, if its step is the one on the screen. */
    showOnScreen: function () {
        var overlay = this.overlay, focus = this.focusMessage;
        if (focus == null || overlay.step == null || overlay.step.step != focus.step || (overlay.step.sub || 0) != (focus.sub || 0))
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
        //Cards lie over one another (what is attached to a character, under it): the one under the pointer is the
        // one on top there, whatever the others' boxes are
        var top = null;
        var stack = document.elementsFromPoint ? document.elementsFromPoint(x, y) : [];
        for (var s = 0; s < stack.length && top == null; s++) {
            var under = $(stack[s]);
            if (under.closest("#scenarioAuthor, #scenarioLayer, .sa-pickbox, .sa-picklabel").length > 0)
                continue;
            var cardElem = under.closest(".card");
            if (cardElem.length > 0)
                top = cardElem[0];
        }
        var consider = function (anchor, rect) {
            if (rect == null || x < rect.left || x > rect.left + rect.width || y < rect.top || y > rect.top + rect.height)
                return;
            var area = rect.width * rect.height;
            if (best == null || area < best.area)
                best = {anchor: anchor, rect: rect, area: area};
        };
        $(".card:visible").each(function () {
            var card = $(this).data("card");
            if (card == null || (top != null && this !== top))
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
                section.append(this.el("p", "sa-note", asked.step != null ? "Step " + asked.step + (asked.sub > 0 ? "." + asked.sub : "") + " answers it, once what it says has been read."
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
            yours.append(this.el("p", "sa-note", waiting.human.step != null ? "Step " + waiting.human.step + (waiting.human.sub > 0 ? "." + waiting.human.sub : "") + " applies to it."
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

    /**
     * What the Board tab has gathered and not yet sent: {draft, ops, added, removed}.  'ops' are changes to the board
     * ({name, args}: "wound", ["aragorn", 1]) in the order they were made; 'added' are cards to put in the scenario
     * ({alias, blueprintId, name, type, seat, zone, host}, the zone as addCard has it); 'removed' are the names of cards
     * to take out.  It is kept with what the panel remembers (a page can be left for a fresh game, and come back to it),
     * for the draft it was gathered in.
     */
    gathered: function () {
        var ui = this.ui, id = this.draft == null ? null : this.draft.id;
        if (ui.board == null || ui.board.draft != id)
            ui.board = {draft: id, ops: [], added: [], removed: []};
        return ui.board;
    },

    gatheredCount: function () {
        var gathered = this.gathered();
        return gathered.ops.length + gathered.added.length + gathered.removed.length;
    },

    /** A change to the board as the server reads it. */
    opText: function (op) {
        return op.name + "(" + op.args.join(", ") + ")";
    },

    /**
     * Gathers one change to the board.  A change that undoes or goes over the one before it (the same pile put in
     * another order, a card moved again, a wound and then a healing, a total set again) takes its place.
     */
    gather: function (name, args) {
        var gathered = this.gathered();
        var ops = gathered.ops;
        var last = ops.length == 0 ? null : ops[ops.length - 1];
        var same = last != null && last.name == name && ("" + last.args[0]).toLowerCase() == ("" + args[0]).toLowerCase();
        var signed = function (op) {
            return (op.name == "heal" ? -1 : 1) * (parseInt(op.args[1]) || 0);
        };
        if (name == "order" || name == "move") {
            if (same)
                ops.pop();
        } else if (name == "wound" || name == "heal") {
            if (last != null && (last.name == "wound" || last.name == "heal") && last.args[0] == args[0]) {
                var net = signed(last) + signed({name: name, args: args});
                ops.pop();
                if (net == 0) {
                    this.remember();
                    return;
                }
                name = net > 0 ? "wound" : "heal";
                args = [args[0], Math.abs(net)];
            }
        } else if (name == "tokens") {
            if (same) {
                args = [args[0], (parseInt(last.args[1]) || 0) + (parseInt(args[1]) || 0)];
                ops.pop();
            }
        } else if (name == "twilight" || name == "burdens" || name == "threats") {
            //(a total is set to what it is to be, whatever it was set to before: and not at all if that is what it is)
            gathered.ops = ops = $.grep(ops, function (op) {
                return op.name != name;
            });
            if (this.state.board != null && this.state.board[name] == parseInt(args[0])) {
                this.remember();
                return;
            }
        }
        ops.push({name: name, args: args});
        this.remember();
    },

    /** Drops what has been gathered about a card (it is being taken out, or put in again as another). */
    forgetCard: function (alias) {
        var gathered = this.gathered();
        var kept = [];
        $.each(gathered.ops, function (i, op) {
            if ($.inArray(op.name, ["wound", "heal", "tokens", "move"]) > -1 && op.args[0] == alias)
                return;
            if ($.inArray(op.name, ["attach", "stack"]) > -1 && (op.args[0] == alias || op.args[1] == alias))
                return;
            if (op.name == "order" && $.inArray(alias, op.args) > 0) {
                var rest = $.grep(op.args, function (arg, at) {
                    return at == 0 || arg != alias;
                });
                if (rest.length < 2)
                    return;
                op = {name: "order", args: rest};
            }
            kept.push(op);
        });
        gathered.ops = kept;
    },

    /** A card has been given another name: what has been gathered says the new one. */
    renameGathered: function (from, to) {
        var gathered = this.gathered();
        var swap = function (alias) {
            return alias == from ? to : alias;
        };
        $.each(gathered.ops, function (i, op) {
            if (op.name == "order")
                op.args = $.map(op.args, function (arg, at) {
                    return at == 0 ? arg : swap(arg);
                });
            else if (op.name == "attach" || op.name == "stack")
                op.args = [swap(op.args[0]), swap(op.args[1])];
            else if ($.inArray(op.name, ["wound", "heal", "tokens", "move"]) > -1)
                op.args[0] = swap(op.args[0]);
        });
        gathered.removed = $.map(gathered.removed, swap);
        this.remember();
    },

    /** What was gathered about cards the scenario does not have (any more: the page may have been left for another game) is let go. */
    pruneGathered: function () {
        var gathered = this.gathered();
        var known = {};
        $.each(this.state.board.cards, function (i, card) {
            known[card.alias] = true;
        });
        $.each(gathered.added, function (i, add) {
            known[add.alias] = true;
        });
        gathered.removed = $.grep(gathered.removed, function (alias) {
            return known[alias] === true && $.inArray(alias, $.map(gathered.added, function (add) {
                return add.alias;
            })) < 0;
        });
        gathered.ops = $.grep(gathered.ops, function (op) {
            var named = op.name == "order" ? op.args.slice(1) : $.inArray(op.name, ["attach", "stack"]) > -1 ? op.args
                : $.inArray(op.name, ["wound", "heal", "tokens", "move"]) > -1 ? [op.args[0]] : [];
            for (var i = 0; i < named.length; i++)
                if (known[named[i]] !== true)
                    return false;
            return true;
        });
    },

    /**
     * The board as it will be once what has been gathered is sent: the server's board with the changes made to it
     * here.  Each card says whether anything was gathered about it ('touched'), whether it is to be added ('added') or
     * taken out ('removing').
     */
    boardView: function () {
        var that = this;
        var board = this.state.board;
        var gathered = this.gathered();
        var view = {twilight: board.twilight, burdens: board.burdens, threats: board.threats, cards: []};
        var added = {};
        $.each(gathered.added, function (i, add) {
            added[add.alias] = true;
        });
        $.each(board.cards, function (i, card) {
            //(a card put in under the name of one the scenario has takes its place)
            if (!added[card.alias])
                view.cards.push(that.clone(card));
        });
        var serial = 0;
        var find = function (alias) {
            for (var i = 0; i < view.cards.length; i++)
                if (view.cards[i].alias == alias)
                    return view.cards[i];
            return null;
        };
        var cardsIn = function (owner, zone) {
            return $.grep(view.cards, function (card) {
                return card.owner == owner && card.zone == zone;
            }).sort(function (a, b) {
                return (a.at == null ? 9999 : a.at) - (b.at == null ? 9999 : b.at);
            });
        };
        $.each(gathered.added, function (i, add) {
            var placed = add.zone == "" ? "deck" : add.zone == "attach" ? "attached" : add.zone == "stack" ? "stacked" : add.zone;
            view.cards.push({alias: add.alias, blueprintId: add.blueprintId, name: add.name, type: add.type, owner: add.seat, zone: placed,
                at: 1000 + (++serial), wounds: 0, tokens: 0, on: add.zone == "attach" || add.zone == "stack" ? add.host : null, added: true, touched: true});
        });
        $.each(gathered.removed, function (i, alias) {
            var card = find(alias);
            if (card != null) {
                card.removing = true;
                card.touched = true;
            }
        });
        $.each(gathered.ops, function (i, op) {
            var card = op.name == "order" ? null : find(op.args[0]);
            if (op.name == "twilight" || op.name == "burdens" || op.name == "threats") {
                view[op.name] = parseInt(op.args[0]) || 0;
            } else if (op.name == "order") {
                var parts = ("" + op.args[0]).split(".");
                var there = cardsIn(parts[0], parts[1]);
                var first = [];
                $.each(op.args.slice(1), function (n, alias) {
                    var each = find(alias);
                    if (each != null && each.owner == parts[0] && each.zone == parts[1]) {
                        first.push(each);
                        each.touched = true;
                    }
                });
                var order = first.concat($.grep(there, function (each) {
                    return $.inArray(each, first) < 0;
                }));
                $.each(order, function (n, each) {
                    each.at = n;
                });
            } else if (card == null) {
                return;
            } else if (op.name == "move") {
                var to = ("" + op.args[1]).split(".");
                card.zone = to[1];
                card.on = null;
                //(on the top of a pile is before every card, and before those put on the top earlier)
                card.at = to[2] == "top" ? -1000 - (++serial) : 1000 + (++serial);
                card.touched = true;
            } else if (op.name == "attach" || op.name == "stack") {
                card.zone = op.name == "attach" ? "attached" : "stacked";
                card.on = op.args[1];
                card.touched = true;
            } else if (op.name == "wound") {
                card.wounds += parseInt(op.args[1]) || 0;
                card.touched = true;
            } else if (op.name == "heal") {
                card.wounds = Math.max(0, card.wounds - (parseInt(op.args[1]) || 0));
                card.touched = true;
            } else if (op.name == "tokens") {
                card.tokens += parseInt(op.args[1]) || 0;
                card.touched = true;
            }
        });
        return view;
    },

    /** The names of the cards the scenario has, with the ones gathered to be added and without the ones gathered to be taken out. */
    boardAliases: function () {
        return $.map($.grep(this.boardView().cards, function (card) {
            return !card.removing;
        }), function (card) {
            return card.alias;
        }).concat(this.siteAliases());
    },

    /** The changes in the cards (adds and takings-out) as the scenario's top-level parts: {cards, setup}. */
    cardChange: function () {
        var that = this;
        var gathered = this.gathered();
        if (gathered.added.length == 0 && gathered.removed.length == 0)
            return null;
        var cards = this.clone(this.draft.cards) || {};
        var setup = this.clone(this.draft.setup) || {};
        //A card out of the scenario: out of its seat's cards and out of every place the setup puts it
        var take = function (alias) {
            $.each(["freeps", "shadow"], function (index, seat) {
                if (cards[seat] != null)
                    delete cards[seat][alias];
                for (var zone in (setup[seat] || {}))
                    if (setup[seat].hasOwnProperty(zone)) {
                        var kept = $.grep(that.splitTerms(setup[seat][zone]), function (each) {
                            return each != alias;
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
                    delete setup[name][alias];
                    if ($.isEmptyObject(setup[name]))
                        delete setup[name];
                }
            });
        };
        $.each(gathered.removed, function (i, alias) {
            take(alias);
        });
        $.each(gathered.added, function (i, add) {
            //(one that is put in under the name of a card the scenario has is that card, replaced)
            take(add.alias);
            cards[add.seat] = cards[add.seat] || {};
            cards[add.seat][add.alias] = add.blueprintId;
            if (add.zone == "attach" || add.zone == "stack") {
                setup[add.zone] = setup[add.zone] || {};
                setup[add.zone][add.alias] = add.host;
            } else if (add.zone != "") {
                setup[add.seat] = setup[add.seat] || {};
                setup[add.seat][add.zone] = setup[add.seat][add.zone] ? setup[add.seat][add.zone] + ", " + add.alias : add.alias;
            }
        });
        return {cards: cards, setup: $.isEmptyObject(setup) ? null : setup};
    },

    /**
     * Sends what the Board tab has gathered, all at once: the cards put in and taken out first, as one change to the
     * scenario (which brings a fresh game), and then the changes to the board as one request, which the server makes
     * in turn (in the scenario's setup, with one fresh game played for them all).
     */
    submitBoard: function () {
        var that = this;
        var gathered = this.gathered();
        if (this.gatheredCount() == 0)
            return;
        var inSetup = this.ui.where == "setup";
        var change = this.cardChange();
        var ops = $.map(gathered.ops, function (op) {
            return that.opText(op);
        }).join(", ");
        this.flush(function () {
            //(a fresh game that the cards bring is gone to once the changes to the board have been made to it)
            that.holding = true;
            that.arrived = null;
            var finish = function (failure) {
                that.holding = false;
                var arrived = that.arrived;
                that.arrived = null;
                if (failure != null && arrived != null)
                    that.ui.carried = failure;
                that.remember();
                if (arrived != null)
                    that.go(arrived);
                else
                    that.render();
            };
            var sendOps = function () {
                if (ops == "") {
                    finish(null);
                    return;
                }
                that.call("POST", "do", {op: ops, setup: inSetup}, function () {
                    that.gathered().ops = [];
                    finish(null);
                }, function () {
                    //(in the game in hand the changes before the one that went wrong have been made, and the rest not: what
                    // is left is not sent again.  In the setup nothing was made, and they stay to be put right.)
                    var said = that.problem;
                    if (!inSetup) {
                        that.gathered().ops = [];
                        that.problem = "Some changes to the board were made, and the rest were not: " + said;
                    }
                    finish(that.problem);
                });
            };
            if (change == null) {
                sendOps();
                return;
            }
            that.edit({top: change}, null, function () {
                var now = that.gathered();
                now.added = [];
                now.removed = [];
                sendOps();
            }, function () {
                that.holding = false;
                that.arrived = null;
                that.render();
            });
        });
    },

    /** Lets go of what has been gathered, so that the board is as the scenario has it. */
    resetBoard: function () {
        this.ui.board = null;
        this.remember();
        this.renderBoard();
    },

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
        this.pruneGathered();
        var gathered = this.gathered();
        var count = this.gatheredCount();
        //The board as it will be once what has been gathered is sent; each change is gathered, not sent
        var view = this.boardView();
        var queue = function (name, args) {
            that.gather(name, args);
            that.renderBoard();
        };

        //Submit and Reset, and what is waiting to be sent
        var bar = this.el("div", "sa-section sa-boardbar").appendTo(box);
        var buttons = this.el("div", "sa-row").appendTo(bar);
        buttons.append(this.button("Submit changes (" + count + ")", inSetup
            ? "Makes all of them to the board the scenario starts with, and plays a fresh game by the script back to here"
            : "Makes all of them at once to the board as it stands", function () {
            that.submitBoard();
        }, "sa-primary").prop("disabled", count == 0));
        buttons.append(this.button("Reset", "Lets go of the changes gathered here: the board is as the scenario has it", function () {
            that.resetBoard();
        }).prop("disabled", count == 0));
        if (count == 0) {
            buttons.append(this.el("span", "sa-note", "Changes are gathered here until they are submitted."));
        } else {
            var list = this.el("div", "sa-gathered").appendTo(bar);
            var line = function (text, undo) {
                var row = that.el("div", "sa-gathered-row").appendTo(list);
                row.append(that.el("span", "sa-gathered-text", text));
                row.append(that.button("×", "Do not make this change", undo, "sa-undo"));
            };
            $.each(gathered.removed, function (i, alias) {
                line("take out " + alias, function () {
                    gathered.removed.splice(i, 1);
                    that.remember();
                    that.renderBoard();
                });
            });
            $.each(gathered.added, function (i, add) {
                line("add " + add.name + " as " + add.alias + " (" + add.seat + ", " + (add.zone == "" ? "deck" : add.zone == "attach" ? "on " + add.host
                    : add.zone == "stack" ? "stacked on " + add.host : add.zone) + ")", function () {
                    that.forgetCard(add.alias);
                    gathered.added.splice(i, 1);
                    that.remember();
                    that.renderBoard();
                });
            });
            $.each(gathered.ops, function (i, op) {
                line(that.opText(op), function () {
                    gathered.ops.splice(i, 1);
                    that.remember();
                    that.renderBoard();
                });
            });
        }

        var where = this.el("div", "sa-section").appendTo(box);
        where.append(this.el("div", "sa-heading", "Changes are made"));
        var choose = function (value, label, note) {
            var row = that.el("label", "sa-check sa-block").appendTo(where);
            row.append($("<input type='radio' name='sa-where'/>").prop("checked", that.ui.where == value).change(function () {
                //What has been gathered is for one of the two boards: it is sent, or let go of, before the other is chosen
                if (that.gatheredCount() > 0) {
                    that.problem = "Changes are waiting under Board: submit them or reset them before changing where changes are made.";
                    that.renderNotices();
                    that.renderBoard();
                    return;
                }
                that.ui.where = value;
                that.remember();
                that.renderBoard();
            }));
            row.append(document.createTextNode(" " + label));
            row.append(that.el("div", "sa-note", note));
        };
        choose("now", "to the board as it stands", position.inHand
            ? "Done once submitted, and by step " + position.step + " whenever the script is played. The decision on the screen was worked out before the change: it shows in what the game offers from the next decision on."
            : "Done once submitted, and by the next step you write. The decision on the screen was worked out before the change.");
        choose("setup", "to the board the scenario starts with", "The setup is changed, and a fresh game is played by the script back to here.");
        if ((this.state.pending || []).length > 0)
            where.append(this.el("div", "sa-note sa-pending", "Waiting for a step to carry them: " + this.state.pending.join(", ")));

        var counters = this.el("div", "sa-section").appendTo(box);
        var counter = function (label, name, disabled) {
            var input = $("<input type='number' min='0'/>").addClass("sa-input sa-short").val(view[name]);
            var row = that.el("div", "sa-row").appendTo(counters);
            row.toggleClass("sa-changed", view[name] != board[name]);
            row.append(that.el("span", "sa-label", label));
            row.append(input);
            row.append(that.button("Set", "Gathered with the other changes, and made when they are submitted", function () {
                queue(name, [parseInt(input.val()) || 0]);
            }).prop("disabled", !!disabled));
        };
        counter("Twilight", "twilight", inSetup);
        counter("Burdens", "burdens");
        counter("Threats", "threats");

        this.renderSites(box, board);

        //(what another card can be put on: anything on the table, a card that is itself on another included)
        var bearers = $.grep(view.cards, function (card) {
            return !card.removing && (card.zone == "characters" || card.zone == "minions" || card.zone == "support" || card.zone == "attached");
        });
        $.each(["freeps", "shadow"], function (index, seat) {
            var section = that.el("div", "sa-section").appendTo(box);
            section.append(that.el("div", "sa-heading", seat == "freeps" ? "Free Peoples seat" : "Shadow seat"));
            section.append(that.ringRow(seat, board));
            var zones = $.map(that.ZONES, function (zone) {
                return [[seat + "." + zone, zone]];
            });
            //The seat's cards by where they are: what is on the table, then the hand and the piles, each of those in
            // the order its cards come (which the arrows change)
            var places = that.PLACED.slice();
            var held = {};
            $.each(view.cards, function (i, card) {
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
                row.toggleClass("sa-changed", !!card.touched).toggleClass("sa-added", !!card.added).toggleClass("sa-removing", !!card.removing);
                var name = that.el("div", "sa-cardname").appendTo(row);
                //(a card hint, as the game's chat has them: the game screen shows the card while the pointer is on
                // it, and opens its window on a click)
                name.append(that.el("span", "sa-alias cardHint", card.alias).attr("value", card.blueprintId).attr("title", ""));
                name.append(document.createTextNode(" " + card.name));
                if (card.added)
                    name.append(that.el("span", "sa-tag", "new"));
                else if (card.removing)
                    name.append(that.el("span", "sa-tag", "to be taken out"));
                else
                    name.append(that.button("✎", "Give this card another name in the scenario. Every part of the scenario that says the old name is changed to say the new one.", function () {
                        that.renameCard(card, name);
                    }, "sa-rename"));
                var controls = that.el("div", "sa-row").appendTo(row);
                if (card.removing) {
                    controls.append(that.button("Keep it", "Do not take this card out", function () {
                        gathered.removed = $.grep(gathered.removed, function (alias) {
                            return alias != card.alias;
                        });
                        that.remember();
                        that.renderBoard();
                    }));
                    return;
                }
                if (entry.ordered) {
                    //Where it comes in its hand or pile: one place earlier or later, or first
                    var reorder = function (to) {
                        var aliases = $.map(entry.among, function (other) { return other.alias; });
                        aliases.splice(entry.at, 1);
                        aliases.splice(to, 0, card.alias);
                        queue("order", [seat + "." + card.zone].concat(aliases));
                    };
                    var pile = card.zone != "hand";
                    controls.append(that.button("↑", pile ? "One nearer the top" : "One earlier in the hand", function () {
                        reorder(entry.at - 1);
                    }, "sa-order").prop("disabled", entry.at == 0));
                    controls.append(that.button("↓", pile ? "One nearer the bottom" : "One later in the hand", function () {
                        reorder(entry.at + 1);
                    }, "sa-order").prop("disabled", entry.at == entry.among.length - 1));
                    controls.append(that.button("⤒", pile ? "To the top" : "To the front of the hand", function () {
                        reorder(0);
                    }, "sa-order").prop("disabled", entry.at == 0));
                }
                controls.append(that.select([["", card.zone + (card.on ? " on " + card.on : "")]].concat(zones), "", function (zone) {
                    if (zone != "")
                        queue("move", [card.alias, zone]);
                }).attr("title", "Move it"));
                var inPlay = card.zone == "characters" || card.zone == "minions";
                if (inPlay || card.wounds > 0) {
                    controls.append(that.button("−", "Heal a wound", function () {
                        queue("heal", [card.alias, 1]);
                    }).prop("disabled", card.wounds == 0));
                    controls.append(that.el("span", "sa-count", card.wounds + "w").attr("title", "wounds"));
                    controls.append(that.button("+", "Add a wound", function () {
                        queue("wound", [card.alias, 1]);
                    }));
                }
                if (card.zone == "support" || card.tokens > 0)
                    controls.append(that.button("+token" + (card.tokens > 0 ? " (" + card.tokens + ")" : ""), "Add a culture token", function () {
                        queue("tokens", [card.alias, 1]);
                    }));
                //Any card can be put on any other that is on the table: attached to it (as a possession is borne),
                // or stacked on it (as cards are kept under another)
                var hosts = $.grep(bearers, function (bearer) {
                    return bearer.alias != card.alias;
                });
                if (hosts.length > 0)
                    controls.append(that.select([["", "put on" + "…"]].concat($.map(hosts, function (host) {
                        return [["attach|" + host.alias, "attach to " + host.alias]];
                    })).concat($.map(hosts, function (host) {
                        return [["stack|" + host.alias, "stack on " + host.alias]];
                    })), "", function (what) {
                        if (what != "")
                            queue(what.split("|")[0], [card.alias, what.split("|")[1]]);
                    }).attr("title", "Attach it to another card, or stack it on one"));
                controls.append(that.button("×", card.added ? "Do not add this card" : "Take this card out of the scenario (when the changes are submitted)", function () {
                    that.dropCard(card);
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
                        results.append(that.el("div", "sa-result", card.name + "  ·  " + card.id + "  ·  " + (card.culture || "") + " " + (card.type || "")
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
            var taken = that.boardAliases();
            //The first word of its name that says something: "The One Ring" is "one", not "the"
            //(accents taken off first: Palantír is "palantir")
            var plain = card.name.normalize ? card.name.normalize("NFD").replace(/[\u0300-\u036f]/g, "") : card.name;
            var words = $.grep(plain.toLowerCase().replace(/[^a-z0-9 ]/g, "").split(" "), function (word) {
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
                places = places.concat([["attach", "attached to" + "…"], ["stack", "stacked on" + "…"]]);
            var zone = that.select(places, card.side == "shadow" ? "" : "hand", function (value) {
                host.toggle(value == "attach" || value == "stack");
            });
            chosen.append(that.el("div", "sa-cardname", card.name + " (" + card.id + ")"));
            var row = that.el("div", "sa-row sa-wrap").appendTo(chosen);
            row.append(that.el("span", "sa-label", "called")).append(name).append(seat).append(zone).append(host);
            row.append(that.button("Add", "Gathered with the other changes, and made when they are submitted", function () {
                that.addCard(card, $.trim(name.val()), seat.val(), zone.val(), host.val());
            }, "sa-primary"));
        };
        box.scrollTop(scroll);
    },

    /**
     * Each seat's adventure path, a site to a row: a site is changed (or one added after the last) by finding it by
     * name, as a card is.  Both seats usually have the same path, and are changed together while they do.
     */
    renderSites: function (box, board) {
        var that = this;
        var paths = board.sites || {};
        var ids = function (seat) {
            return $.map(paths[seat] || [], function (site) {
                return site.id;
            });
        };
        if (this.ui.sitesApart == null)
            this.ui.sitesApart = ids("freeps").join(",") != ids("shadow").join(",");
        var apart = this.ui.sitesApart;
        var section = this.el("div", "sa-section sa-sites").appendTo(box);
        section.append(this.el("div", "sa-heading", "The adventure path"));
        section.append(this.el("label", "sa-check sa-block").attr("title", "Unticked, each seat's path is changed on its own")
            .append($("<input type='checkbox'/>").prop("checked", !apart).change(function () {
                that.ui.sitesApart = !this.checked;
                that.remember();
                if (this.checked && ids("freeps").join(",") != ids("shadow").join(","))
                    that.putSites(ids("freeps"), ids("freeps"));
                else
                    that.renderBoard();
            })).append(document.createTextNode(" the same sites for both seats")));
        $.each(apart ? ["freeps", "shadow"] : ["freeps"], function (index, seat) {
            if (apart)
                section.append(that.el("div", "sa-zonehead", seat == "freeps" ? "Free Peoples" : "Shadow"));
            var list = ids(seat);
            var change = function (at, id) {
                var now = list.slice();
                if (id == null)
                    now.splice(at, 1);
                else
                    now[at] = id;
                if (!apart)
                    that.putSites(now, now);
                else
                    that.putSites(seat == "freeps" ? now : ids("freeps"), seat == "shadow" ? now : ids("shadow"));
            };
            $.each(paths[seat] || [], function (at, site) {
                var row = that.el("div", "sa-card").appendTo(section);
                var name = that.el("div", "sa-cardname").appendTo(row);
                name.append(that.el("span", "sa-alias", "Site " + site.number));
                name.append(that.el("span", "cardHint", " " + (site.name || "(no such card)")).attr("value", site.id).attr("title", ""));
                name.append(that.el("span", "sa-note", " " + site.id));
                //Its name in the scenario (the opponent's too, while the paths are the same), which steps pick it by
                $.each(apart ? [seat] : ["freeps", "shadow"], function (k, whose) {
                    var named = ((paths[whose] || [])[at] || {}).alias;
                    if (!named)
                        return;
                    var holder = that.el("span", "sa-sitename").appendTo(name);
                    holder.append(that.el("span", "sa-alias", " " + named).attr("title", "Its name in the scenario: choose(" + named + "), card:" + named));
                    holder.append(that.button("\u270e", "Give this site another name in the scenario. Every part of the scenario that says the old name is changed to say the new one.", function () {
                        that.renameCard({alias: named}, holder);
                    }, "sa-rename"));
                });
                var controls = that.el("div", "sa-row").appendTo(row);
                controls.append(that.siteFinder(site.number, function (id) {
                    change(at, id);
                }, "Change"));
                if (at == list.length - 1 && list.length > 1)
                    controls.append(that.button("\u00d7", "Take the last site off the path", function () {
                        change(at, null);
                    }, "sa-danger"));
            });
            section.append(that.el("div", "sa-row").append(that.siteFinder(list.length + 1, function (id) {
                change(list.length, id);
            }, "+ Site " + (list.length + 1))));
        });
    },

    /** A button that opens a search for a site by name; done(id) with the one chosen. */
    siteFinder: function (number, done, label) {
        var that = this;
        var holder = this.el("span", "sa-sitefinder");
        holder.append(this.button(label, "Find a site by its name", function () {
            holder.empty();
            var query = $("<input type='text'/>").addClass("sa-input sa-short").attr("placeholder", "name of a site");
            var results = that.el("div", "sa-results");
            holder.append(query).append(that.button("Cancel", "", function () {
                that.renderBoard();
            })).append(results);
            var search = null;
            query.bind("input", function () {
                clearTimeout(search);
                search = setTimeout(function () {
                    that.call("GET", "cards", {q: query.val(), sites: true}, function (found) {
                        results.empty();
                        //(the sites for this place on the path first)
                        var cards = (found.cards || []).slice().sort(function (a, b) {
                            return (a.site == number ? 0 : 1) - (b.site == number ? 0 : 1);
                        });
                        $.each(cards, function (i, card) {
                            results.append(that.el("div", "sa-result", card.name + "  \u00b7  " + card.id + "  \u00b7  site " + card.site).click(function () {
                                done(card.id);
                            }));
                        });
                        if (cards.length == 0)
                            results.append(that.el("div", "sa-note", "No site by that name."));
                    });
                }, 300);
            });
            query.focus();
        }));
        return holder;
    },

    putSites: function (freeps, shadow) {
        var that = this;
        var paths = (this.state && this.state.board && this.state.board.sites) || {};
        //Sites that have been given names keep them, place by place; the path is written with its names if any has one
        var path = function (seat, ids) {
            var named = false, object = {};
            $.each(ids, function (at, id) {
                var alias = ((paths[seat] || [])[at] || {}).alias || that.defaultSiteAlias(seat, at + 1);
                named = named || alias != that.defaultSiteAlias(seat, at + 1);
                object[alias] = id;
            });
            return named ? object : ids.join(", ");
        };
        this.edit({top: {sites: {freeps: path("freeps", freeps), shadow: path("shadow", shadow)}}}, null);
    },

    /** The seat's Ring-bearer and its Ring, chosen from its cards. */
    ringRow: function (seat, board) {
        var that = this;
        var mine = $.grep(board.cards, function (card) {
            return card.owner == seat;
        });
        var choices = function (type) {
            return $.map($.grep(mine, function (card) {
                return card.type == type;
            }), function (card) {
                return [[card.alias, card.alias + " (" + card.name + ")"]];
            });
        };
        var put = function (part, value) {
            var now = that.clone(that.draft[part]) || {};
            if (value == "")
                delete now[seat];
            else
                now[seat] = value;
            var top = {};
            top[part] = $.isEmptyObject(now) ? null : now;
            that.edit({top: top}, null);
        };
        var row = this.el("div", "sa-row sa-wrap sa-ring");
        row.append(this.el("span", "sa-label", "Ring-bearer"));
        row.append(this.select(choices("COMPANION"), (this.draft.ringBearer || {})[seat] || "", function (value) {
            put("ringBearer", value);
        }).attr("title", "The companion who carries the Ring"));
        row.append(this.el("span", "sa-label sa-inline", "Ring"));
        row.append(this.select([["", "none"]].concat(choices("THE_ONE_RING")), (this.draft.ring || {})[seat] || "", function (value) {
            put("ring", value);
        }).attr("title", "The One Ring the Ring-bearer bears (Add a card: The One Ring, to have another one to choose)"));
        return row;
    },

    /**
     * A card's name in the scenario, changed where it stands in the Board tab: Enter (or Rename) to change it, Esc
     * (or Cancel) to leave it.  The server puts the new name wherever the scenario said the old one.
     */
    renameCard: function (card, holder) {
        var that = this;
        var input = $("<input type='text'/>").addClass("sa-input sa-short").attr("maxlength", 40).val(card.alias);
        var over = false;
        var leave = function () {
            if (over)
                return;
            over = true;
            holder.empty();
            that.renderBoard();
        };
        var rename = function () {
            var to = $.trim(input.val());
            if (over)
                return;
            if (to == card.alias) {
                leave();
                return;
            }
            if (!/^[A-Za-z][A-Za-z0-9_-]*$/.test(to) || $.inArray(to, that.boardAliases()) > -1) {
                input.addClass("sa-bad").attr("title", $.inArray(to, that.boardAliases()) > -1 ? "The scenario already has a card called that"
                    : "Letters, digits, dashes and underscores, starting with a letter");
                return;
            }
            that.flush(function () {
                over = true;
                //(what the Board tab has gathered about the card goes on being about it)
                that.renameGathered(card.alias, to);
                that.call("POST", "alias", {from: card.alias, to: to}, function () {
                    holder.empty();
                    that.render();
                }, function () {
                    that.renameGathered(to, card.alias);
                    holder.empty();
                    that.renderBoard();
                });
            });
        };
        holder.empty().append(input);
        holder.append(this.button("Rename", "A fresh game is played by the script back to here", rename, "sa-primary"));
        holder.append(this.button("Cancel", "", leave));
        input.keydown(function (event) {
            if (event.keyCode == 13)
                rename();
            else if (event.keyCode == 27)
                leave();
        });
        input.focus().select();
    },

    /**
     * Gathers a card to be added to the scenario (a card of the library, as the search found it).  If the scenario
     * already has a card by that name, the author is asked whether the new card is to take its place.
     * @param zone a zone of the seat's; "" for the deck; or "attach" / "stack", with the card it goes on as host
     */
    addCard: function (found, alias, seat, zone, host) {
        var that = this;
        if (!/^[A-Za-z][A-Za-z0-9_]*$/.test(alias)) {
            this.problem = "A card's name in the scenario is made of letters, digits and underscores, starting with a letter.";
            this.renderNotices();
            return;
        }
        if ($.inArray(alias, this.siteAliases()) > -1) {
            this.problem = alias + " is the name of a site of the scenario's: give the card another.";
            this.renderNotices();
            return;
        }
        var gathered = this.gathered();
        var existing = null;
        $.each(this.boardView().cards, function (i, each) {
            if (each.alias == alias && !each.removing)
                existing = each;
        });
        var put = function () {
            //(whatever was to be done to a card of that name was to be done to the one that is replaced)
            that.forgetCard(alias);
            gathered.removed = $.grep(gathered.removed, function (each) {
                return each != alias;
            });
            gathered.added = $.grep(gathered.added, function (each) {
                return each.alias != alias;
            });
            gathered.added.push({alias: alias, blueprintId: found.id, name: found.name, type: found.type, seat: seat, zone: zone, host: host});
            that.remember();
            that.renderBoard();
        };
        if (existing == null) {
            put();
            return;
        }
        ScenarioConfirm.ask({
            title: "Replace " + alias + "?",
            text: ["The scenario already has a card called " + alias + ": " + existing.name + " (" + existing.blueprintId + "), in the "
                + (existing.owner == "freeps" ? "Free Peoples" : "Shadow") + " seat" + (existing.added ? ", which is waiting to be added" : "") + ".",
                "Adding " + found.name + " under that name replaces it: everything in the scenario that says " + alias + " is then about the new card, "
                + "and it goes where you chose, not where " + existing.name + " was."],
            yes: "Replace " + alias,
            no: "Cancel"
        }, put);
    },

    /** A card's \u00d7: one that was to be added is not added after all; one the scenario has is gathered to be taken out. */
    dropCard: function (card) {
        var gathered = this.gathered();
        this.forgetCard(card.alias);
        if (card.added)
            gathered.added = $.grep(gathered.added, function (add) {
                return add.alias != card.alias;
            });
        else if ($.inArray(card.alias, gathered.removed) < 0)
            gathered.removed.push(card.alias);
        this.remember();
        this.renderBoard();
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
        //A scenario of the author's own can be submitted for review from here (see ScenarioSubmit): it is saved
        // first if it needs to be, and then locked, so the draft is closed and this tab goes back to the hall
        if (state.own)
            buttons.append(this.button("Submit for review" + "\u2026", "Submit it for review, to become a public scenario"
                + (state.dirty || !state.onDisk ? ". It is saved first." : "."), function () {
                that.submit();
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

        this.renderRules(box);
        this.renderOutcome(box);

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

    /**
     * The rules a scenario plays by, as far as they are the usual ones: each seat's hand size, phases it skips, The
     * One Ring's text, the minions' roaming penalty, and whether the player takes every turn.  Anything else the file
     * has under 'rules' is kept as it is (and is changed under The rest of the file).
     */
    renderRules: function (box) {
        var that = this;
        var rules = this.clone(this.draft.rules) || {};
        var section = this.el("div", "sa-section sa-rules").appendTo(box);
        section.append(this.el("div", "sa-heading", "Rules"));
        var send = function () {
            var empty = !rules.oneSided && $.grep(["freeps", "shadow"], function (seat) {
                return (rules[seat] || []).length > 0;
            }).length == 0;
            that.edit({top: {rules: empty ? null : rules}}, null);
        };
        section.append(this.el("label", "sa-check sa-block").attr("title", "The Shadow player never takes a turn of their own")
            .append($("<input type='checkbox'/>").prop("checked", !!rules.oneSided).change(function () {
                if (this.checked)
                    rules.oneSided = true;
                else
                    delete rules.oneSided;
                send();
            })).append(document.createTextNode(" one-sided: the Free Peoples take every turn")));

        //What one of a seat's effects is, if it is one of the usual ones
        var only = function (object, names) {
            if (object == null || typeof object != "object")
                return false;
            for (var name in object)
                if (object.hasOwnProperty(name) && $.inArray(name, names) < 0)
                    return false;
            return true;
        };
        var kindOf = function (effect) {
            if (!only(effect, ["type", "modifier"]) || effect.type != "modifier")
                return null;
            var m = effect.modifier;
            if (m.type == "modifyHandSize" && only(m, ["type", "amount"]))
                return "hand";
            if (m.type == "skipPhase" && only(m, ["type", "phase"]))
                return "skip";
            if (m.type == "disableGameText" && only(m, ["type", "filter"]) && m.filter == "name(The One Ring)")
                return "ring";
            if (m.type == "modifyRoamingPenalty" && only(m, ["type", "filter", "amount"]) && m.filter == "minion")
                return "roaming";
            return null;
        };
        $.each(["freeps", "shadow"], function (index, seat) {
            var effects = rules[seat] || [];
            var known = {hand: 0, skip: [], ring: false, roaming: 0}, others = [];
            $.each(effects, function (i, effect) {
                var kind = kindOf(effect);
                if (kind == "hand")
                    known.hand += parseInt(effect.modifier.amount) || 0;
                else if (kind == "skip")
                    known.skip.push(("" + effect.modifier.phase).toLowerCase());
                else if (kind == "ring")
                    known.ring = true;
                else if (kind == "roaming")
                    known.roaming += parseInt(effect.modifier.amount) || 0;
                else
                    others.push(effect);
            });
            var rebuild = function () {
                var out = [];
                if (known.hand != 0)
                    out.push({type: "modifier", modifier: {type: "modifyHandSize", amount: known.hand}});
                if (known.ring)
                    out.push({type: "modifier", modifier: {type: "disableGameText", filter: "name(The One Ring)"}});
                $.each(that.PHASES, function (i, phase) {
                    if ($.inArray(phase, known.skip) > -1)
                        out.push({type: "modifier", modifier: {type: "skipPhase", phase: phase}});
                });
                if (known.roaming != 0)
                    out.push({type: "modifier", modifier: {type: "modifyRoamingPenalty", filter: "minion", amount: known.roaming}});
                out = out.concat(others);
                if (out.length == 0)
                    delete rules[seat];
                else
                    rules[seat] = out;
                send();
            };
            var block = that.el("div", "sa-seatrules").appendTo(section);
            block.append(that.el("div", "sa-zonehead", seat == "freeps" ? "Free Peoples" : "Shadow"));
            var number = function (label, value, hint, changed) {
                var input = $("<input type='number'/>").addClass("sa-input sa-short").val(value).change(function () {
                    changed(parseInt($(this).val()) || 0);
                    rebuild();
                });
                block.append(that.field(label, input, hint));
            };
            number("Hand size \u00b1", known.hand, "Added to the usual 8: -3 makes it 5", function (value) {
                known.hand = value;
            });
            var skipRow = that.el("div", "sa-row sa-wrap").appendTo(block);
            skipRow.append(that.el("span", "sa-label", "Skips").attr("title", "Phases this seat's turns go without"));
            $.each(that.PHASES, function (i, phase) {
                skipRow.append(that.el("label", "sa-check").append($("<input type='checkbox'/>").prop("checked", $.inArray(phase, known.skip) > -1).change(function () {
                    known.skip = $.grep(known.skip, function (name) {
                        return name != phase;
                    });
                    if (this.checked)
                        known.skip.push(phase);
                    rebuild();
                })).append(document.createTextNode(" " + phase)));
            });
            block.append(that.el("label", "sa-check sa-block").append($("<input type='checkbox'/>").prop("checked", known.ring).change(function () {
                known.ring = this.checked;
                rebuild();
            })).append(document.createTextNode(" The One Ring's game text is switched off")));
            number("Roaming \u00b1", known.roaming, "Added to the roaming penalty of this seat's minions (the twilight for playing one whose site number is above the fellowship's), -2 takes 2 off it", function (value) {
                known.roaming = value;
            });
            if (others.length > 0)
                block.append(that.el("div", "sa-note", others.length + " other effect" + (others.length == 1 ? "" : "s")
                    + " (under 'rules' in The rest of the file, below), kept as " + (others.length == 1 ? "it is" : "they are") + "."));
        });
    },

    /** How the player loses, as far as the usual ways go, and what they are told when they do. */
    renderOutcome: function (box) {
        var that = this;
        var outcome = this.clone(this.draft.outcome) || {};
        var section = this.el("div", "sa-section sa-outcome").appendTo(box);
        section.append(this.el("div", "sa-heading", "How it is lost"));
        var send = function () {
            that.edit({top: {outcome: $.isEmptyObject(outcome) ? null : outcome}}, null);
        };
        var companion = {type: "hasCardInDeadPile", filter: "companion"};
        var lose = outcome.lose;
        var which = lose == null ? "" : JSON.stringify(lose) == JSON.stringify(companion) ? "companion" : "other";
        var options = [["", "only as the game is lost (the Ring-bearer killed or corrupted)"], ["companion", "also when any companion is killed"]];
        if (which == "other")
            options.push(["other", "as The rest of the file says (under 'outcome')"]);
        section.append(this.field("The player loses", this.select(options, which, function (value) {
            if (value == "")
                delete outcome.lose;
            else if (value == "companion")
                outcome.lose = companion;
            else
                return;
            send();
        })));
        var message = this.el("textarea", "sa-input").attr("rows", 3).val(outcome.loseMessage || "").change(function () {
            if ($.trim($(this).val()) == "")
                delete outcome.loseMessage;
            else
                outcome.loseMessage = $(this).val();
            send();
        });
        section.append(this.field("They are told", message, "What the player reads when they lose, with the way back to the last checkpoint"));
    },

    /** Submits the draft's file for review: saved first if it has changes; the draft is then closed, and the tab with it. */
    submit: function () {
        var that = this;
        ScenarioSubmit.ask(this.draft.title || this.draft.id, function () {
            var send = function () {
                that.call("POST", "submit", {}, function () {
                    that.gone = true;
                    that.overlay.goToHall("#account/scenarios");
                });
            };
            if (!that.state.dirty && that.state.onDisk) {
                send();
                return;
            }
            if (!that.mayOverwrite())
                return;
            that.flushForSave(function (unsent) {
                that.call("POST", "save", {}, function (result) {
                    var saved = result.saved || {};
                    if ((saved.errors || []).length > 0 || !saved.verified || unsent != null) {
                        //(the server would refuse it: what is wrong is said as a save says it.  A step that was not
                        // taken is not in the file that would be submitted: that is said, and nothing is submitted)
                        that.afterSave(result, unsent);
                        return;
                    }
                    send();
                });
            });
        });
    },

    save: function () {
        var that = this;
        var data = {};
        if (!this.mayOverwrite())
            return;
        this.flushForSave(function (unsent) {
            that.sendSave(data, unsent);
        });
    },

    /**
     * Sends what is in the form, as flush does, for something that writes the draft to its file.  That is done
     * whether or not the server took the step: it takes the draft as it stands, and a step that is wrong stays in the
     * form (with what is wrong with it said) for the author to put right.
     * @param then given a sentence to say with the result when the form's step was not taken, and otherwise null
     */
    flushForSave: function (then) {
        var that = this;
        var form = this.form;
        this.flush(function (went) {
            if (went === false)
                that.unsentProblem = that.problem;
            then.call(that, went === false ? that.unsentNote(form) : null);
        }, true);
    },

    /** What is said of a step whose last change is in the form and not in the draft, because the server would not take it. */
    unsentNote: function (form) {
        return "Step " + this.numberOf(form.index, form.sub) + "'s last change is not in the file: the server would not take it (see the error), "
            + "and it is still in the Step tab to be put right.";
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
        //A file stays among the public scenarios if it is one (an admin opened it there), and goes in the author's own
        // folder otherwise: a scenario becomes public by being submitted and approved (My Scenarios), not from here
        var chooses = false;
        var canonical = $("<input type='checkbox'/>").prop("checked", !state.canSaveAnywhere || !!state.canonical);
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
            that.flushForSave(function (unsent) {
                that.call("POST", "save", {as: how, path: tidy, canonical: canonical.prop("checked")}, function (result) {
                    that.filing = null;
                    if (copy) {
                        that.afterSave(result, unsent);
                        return;
                    }
                    that.saved = {good: unsent == null, stays: true, text: "The file is now " + result.renamed + "." + (unsent == null ? "" : " " + unsent)};
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

    sendSave: function (data, unsent) {
        var that = this;
        this.call("POST", "save", data, function (result) {
            that.afterSave(result, unsent);
        });
    },

    /**
     * What is said about a save that went through: at the top of the panel, and in full under Scenario.
     * @param unsent what to say of the step in the form that the server would not take, if there was one: the error it
     *     was refused with stays up, and the form keeps the step
     */
    afterSave: function (result, unsent) {
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
            if (unsent != null) {
                that.saved.good = false;
                that.saved.text += " " + unsent;
                that.report += "\n\n" + unsent;
                //(a save that goes through clears what was said to have gone wrong, which here is still so)
                that.problem = that.unsentProblem;
            }
            if (that.saved.good && warnings == 0 && that.ui.tab != "scenario")
                that.report = null;
            that.render();
        })();
    }
});
