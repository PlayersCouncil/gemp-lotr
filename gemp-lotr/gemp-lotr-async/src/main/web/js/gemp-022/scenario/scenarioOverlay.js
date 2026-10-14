/*
 * The scenario overlay: what a tutorial, puzzle or sandbox adds to the game screen.
 *
 * The server sends "S" game events to the person playing a scenario (see LiveScenario on the server):
 *
 *     info        once: which scenario this is, and what its opponent is called
 *     step        before the decision it is about: messages to show, and which answers the step allows.  A step can
 *                 also be one that is only read (an opponent's, or a "pause" before a decision of the player's own,
 *                 which the server holds back meanwhile): the server is told when it has been read, and which
 *                 button was pressed if it ended in a choice
 *     offscript   once: the game has left its script and carries on as an ordinary game
 *     end         once: how the scenario ended
 *
 * This file turns those into speech bubbles that point at things on the screen, outlines around the things being
 * talked about, and a limit on what the player may click.  It changes nothing about how the game screen itself is
 * built.  It finds things on the screen by asking the game UI object for them (see ScenarioAnchors below).
 *
 * It limits what the player may click in two ways.  A click on a card is put to it by the game screen itself, at
 * the one place where the game screen is about to act on it (cardClicked), so the overlay hears of exactly the
 * clicks the game acts on: not the click that only closes a card's enlarged picture, not the end of a drag, and
 * also the tap on a touch screen, which is not a click event at all.  Buttons and menu items are ordinary clicks,
 * and those it listens for before the game screen does and stops the ones the step does not allow.
 *
 * The game screen calls in at four points only:
 *
 *     handleEvent(json)        an "S" event has arrived (in order with every other game event)
 *     decisionShown(element)   the screen has just been set up for a decision
 *     cardClicked(cardElem)    a card that is part of the decision has been clicked; false means "do nothing"
 *     decisionAnswered()       the player's answer is on its way to the server
 */

/**
 * Finds things on the game screen.  An anchor is how a scenario names something without knowing where it is drawn:
 * "card:frodo", "zone:self.hand", "ui:twilightPool".  The server has already checked that each one is well formed.
 */
var ScenarioAnchors = {
    /**
     * @param cards alias -> card ID, as the current step gave them
     * @param onCards true to have a zone be the cards that are in it, rather than the whole of the place they are
     *                laid out in: what a bubble points at.  (An outline goes round the place; and a zone with no
     *                cards in it is the place either way.)
     * @return {left, top, width, height} in window coordinates, or null if the thing is not on the screen
     */
    resolve: function (game, anchor, cards, onCards) {
        if (anchor == null || game == null)
            return null;
        var parts = ("" + anchor).split(":");
        var kind = parts[0].toLowerCase();
        var elem = null;

        if (kind == "card") {
            elem = ScenarioAnchors.cardElem(cards[parts[1]]);
            if (parts.length > 2)
                return ScenarioAnchors.cardPart(elem, parts[1], parts[2].toLowerCase());
        } else if (kind == "zone") {
            var zone = parts[1].toLowerCase();
            var group = {
                "self.hand": game.hand,
                "self.characters": game.charactersPlayer,
                "self.support": game.supportPlayer,
                "opp.characters": game.charactersOpponent,
                "opp.support": game.supportOpponent,
                "minions": game.shadow
            }[zone];
            if (onCards && group != null) {
                var held = ScenarioAnchors.rectOf(ScenarioAnchors.cardsIn(group));
                if (held != null)
                    return held;
            }
            elem = group != null ? group.descDiv : null;
        } else if (kind == "pile") {
            var pile = parts[1].split(".");
            var self = pile[0].toLowerCase() == "self";
            var index = -1;
            if (game.allPlayerIds != null)
                for (var i = 0; i < game.allPlayerIds.length; i++)
                    if ((game.allPlayerIds[i] == game.bottomPlayerId) == self)
                        index = i;
            var prefix = {
                "deck": "deck", "discard": "discard", "deadpile": "deadPile", "adventuredeck": "adventureDeck",
                "removed": "removedPile", "hand": "hand"
            }[pile[1].toLowerCase()];
            elem = (index < 0 || prefix == null) ? null : $("#" + prefix + index);
        } else if (kind == "ui") {
            var name = parts[1].toLowerCase();
            var sub = parts.length > 2 ? parts[2] : null;
            if (name == "site") {
                elem = $(".card").filter(function () {
                    var card = $(this).data("card");
                    return card != null && card.zone == "ADVENTURE_PATH" && card.siteNumber == parseInt(sub);
                });
            } else if (name == "button") {
                elem = ScenarioAnchors.button(sub.toLowerCase());
            } else if (name == "assignment") {
                return ScenarioAnchors.groupRect(game.freePeopleAssignGroups[cards[sub]], game.shadowAssignGroups[cards[sub]]);
            } else if (name == "skirmish") {
                elem = sub == null ? game.skirmishGroupDiv
                    : sub.toLowerCase() == "fpstrength" ? game.fpStrengthDiv : game.shadowStrengthDiv;
            } else if (name == "stats") {
                elem = sub == null ? game.statsDiv
                    : sub.toLowerCase() == "archery" ? $(".fpArchery, .shadowArchery") : $(".stats .move");
            } else {
                elem = {
                    "twilightpool": $(".twilightPool"),
                    "phase": $(".phase"),
                    "initiative": $("#initiative"),
                    "ruleof4": $("#ruleof4"),
                    "clock": $("#clock-1"),
                    "advpath": game.advPathGroup != null ? game.advPathGroup.descDiv : null,
                    "alertbox": game.alertBox,
                    "chat": $("#bottomLeftTabs"),
                    "gamelog": $("#bottomLeftTabs"),
                    "settings": $("#settingsTab"),
                    "dialog": $(".ui-dialog:visible").last(),
                    "cardinfo": game.cardInfoDialog != null ? game.cardInfoDialog.infoDialog.closest(".ui-dialog") : null
                }[name];
            }
        }
        return ScenarioAnchors.rectOf(elem);
    },

    /** Where things are printed on the cards the current step points at: "frodo:strength" -> [left, top, width, height] in percent. */
    parts: {},

    /**
     * Part of a card on the table.  The figures the game screen writes over a card in play (its strength and vitality
     * as they stand) are found as what they are; the wounds are the tokens lying on it; anything else is where the
     * server says that thing is printed on a card of that kind.
     */
    cardPart: function (elem, alias, part) {
        if (elem == null || elem.length == 0)
            return null;
        var card = elem.first();
        var whole = ScenarioAnchors.rectOf(card);
        if (part == "wounds") {
            var wounds = ScenarioAnchors.rectOf(card.find(".token[src*='wound']"));
            return wounds != null ? wounds : whole;
        }
        var written = {
            strength: ".cardStrengthBg, .cardStrength",
            vitality: ".cardVitalityBg, .cardVitality",
            resistance: ".cardResistanceBg, .cardResistance",
            sitenumber: ".cardSiteNumberBg, .cardSiteNumber"
        }[part];
        if (written != null) {
            var figure = ScenarioAnchors.rectOf(card.find(written));
            if (figure != null)
                return figure;
        }
        var printed = (ScenarioAnchors.parts || {})[alias + ":" + part];
        if (printed == null || whole == null)
            return whole;
        return {
            left: whole.left + whole.width * printed[0] / 100, top: whole.top + whole.height * printed[1] / 100,
            width: whole.width * printed[2] / 100, height: whole.height * printed[3] / 100
        };
    },

    /** The cards laid out in a group, and whatever is attached to them (which shows from under them). */
    cardsIn: function (group) {
        if (group == null || typeof group.getCardElems != "function")
            return null;
        var found = [];
        $.each(group.getCardElems(), function (index, elem) {
            found.push(elem[0]);
            var card = elem.data("card");
            $.each((card && card.attachedCards) || [], function (i, attached) {
                if (attached != null && attached[0] != null)
                    found.push(attached[0]);
            });
        });
        return $(found);
    },

    /** The card with this ID that is on show; a card can be drawn more than once, and one copy may be hidden. */
    cardElem: function (cardId) {
        if (cardId == null)
            return null;
        var wanted = "" + cardId;
        return $(".card").filter(function () {
            var card = $(this).data("card");
            return card != null && ("" + card.cardId) == wanted && $(this).is(":visible");
        });
    },

    button: function (name) {
        var id = {pass: "#Pass", done: "#Done", reset: "#ClearSelection"}[name];
        var inAlert = $(id);
        if (inAlert.length > 0)
            return inAlert;
        var label = {done: "Done", reset: "Clear selection"}[name];
        return $(".ui-dialog:visible .ui-dialog-buttonpane button").filter(function () {
            return $.trim($(this).text()) == label;
        });
    },

    rectOf: function (elem) {
        if (elem == null || elem.length == 0)
            return null;
        var left = Infinity, top = Infinity, right = -Infinity, bottom = -Infinity;
        elem.each(function () {
            if (!$(this).is(":visible"))
                return;
            var rect = this.getBoundingClientRect();
            if (rect.width == 0 && rect.height == 0)
                return;
            left = Math.min(left, rect.left);
            top = Math.min(top, rect.top);
            right = Math.max(right, rect.right);
            bottom = Math.max(bottom, rect.bottom);
        });
        if (left == Infinity)
            return null;
        return {left: left, top: top, width: right - left, height: bottom - top};
    },

    /** Card groups that have no element of their own are known by the bounds they were laid out in. */
    groupRect: function () {
        var left = Infinity, top = Infinity, right = -Infinity, bottom = -Infinity;
        for (var i = 0; i < arguments.length; i++) {
            var group = arguments[i];
            if (group == null || group.x == null)
                continue;
            left = Math.min(left, group.x);
            top = Math.min(top, group.y);
            right = Math.max(right, group.x + group.width);
            bottom = Math.max(bottom, group.y + group.height);
        }
        if (left == Infinity)
            return null;
        return {left: left - window.pageXOffset, top: top - window.pageYOffset, width: right - left, height: bottom - top};
    }
};

/**
 * Works out, for the decision on the screen and what the step allows, which click comes next.
 *
 * A step allows whole answers ("assign the Bowman to Frodo"), but several kinds of decision take more than one
 * click before anything is sent to the server.  The plan keeps track of what the player has built so far and says,
 * for any click, whether it leads towards an allowed answer.  Only those clicks are let through, so a restricted
 * step cannot be got wrong; every other legal click is "not now".
 *
 * click = {kind: "card", id} | {kind: "menu", actionId} | {kind: "pass" | "done" | "reset" | "ok"}
 *       | {kind: "option", index}
 */
var ScenarioPlan = Class.extend({
    type: null,
    params: null,
    allow: null,

    init: function (decision, allow) {
        this.type = decision.type;
        this.params = decision.params;
        this.allow = allow;
        this.answers = allow.answers || [];
        this.reset();
    },

    reset: function () {
        this.selected = [];        // cards (or actions) chosen so far
        this.chosen = false;       // an action has been chosen and only confirming remains
        this.picked = null;        // assignment: the minion that has been picked up
        this.assigned = {};        // assignment: minion -> character
        this.menuCard = null;      // a card whose menu of actions is open
    },

    param: function (name) {
        return this.params[name] || [];
    },

    /**
     * The actions the game screen has put on each card, as {cardId: [actionId]}.  Read off the screen rather than
     * worked out from the decision: an action on a card that is not in play is put on a stand-in card that the game
     * screen makes up an ID for, and makes up again if the choice is reset.
     */
    actionsOnScreen: function () {
        var result = {};
        $(".actionableCard").each(function () {
            var card = $(this).data("card");
            var actions = $(this).data("action");
            if (card == null || actions == null)
                return;
            var ids = result["" + card.cardId] || [];
            for (var i = 0; i < actions.length; i++)
                ids.push("" + actions[i].actionId);
            result["" + card.cardId] = ids;
        });
        return result;
    },

    actionsOn: function (cardId) {
        return (this.actionsOnScreen()["" + cardId] || []).length;
    },

    /** The allowed answers as lists of card IDs.  Passing, where allowed, is the empty list. */
    allowedSets: function () {
        var sets = [];
        for (var i = 0; i < this.answers.length; i++)
            sets.push(this.answers[i] == "" ? [] : this.answers[i].split(","));
        if (this.allow.pass)
            sets.push([]);
        return sets;
    },

    /** The allowed answers to an assignment as maps of minion -> character. */
    allowedAssignments: function () {
        var result = [];
        for (var i = 0; i < this.answers.length; i++) {
            var map = {};
            var groups = this.answers[i] == "" ? [] : this.answers[i].split(",");
            for (var g = 0; g < groups.length; g++) {
                var ids = $.trim(groups[g]).split(" ");
                for (var m = 1; m < ids.length; m++)
                    map[ids[m]] = ids[0];
            }
            result.push(map);
        }
        if (this.allow.pass)
            result.push({});
        return result;
    },

    /**
     * The allowed answers to an assignment as their pairings in the order the scenario wrote them:
     * [{minion, character}].  That order is the order they are to be clicked in.
     */
    orderedAssignments: function () {
        var result = [];
        for (var i = 0; i < this.answers.length; i++) {
            var pairs = [];
            var groups = this.answers[i] == "" ? [] : this.answers[i].split(",");
            for (var g = 0; g < groups.length; g++) {
                var ids = $.trim(groups[g]).split(" ");
                for (var m = 1; m < ids.length; m++)
                    pairs.push({minion: ids[m], character: ids[0]});
            }
            result.push(pairs);
        }
        if (this.allow.pass)
            result.push([]);
        return result;
    },

    /** The same, of the assignments that what has been assigned so far could still become. */
    possibleOrders: function () {
        var that = this;
        return $.grep(this.orderedAssignments(), function (pairs) {
            var gives = {};
            for (var p = 0; p < pairs.length; p++)
                gives[pairs[p].minion] = pairs[p].character;
            for (var minion in that.assigned)
                if (that.assigned.hasOwnProperty(minion) && gives[minion] != that.assigned[minion])
                    return false;
            return true;
        });
    },

    /** The allowed assignments that what has been assigned so far could still become. */
    possibleAssignments: function () {
        var that = this;
        return $.grep(this.allowedAssignments(), function (target) {
            for (var minion in that.assigned)
                if (that.assigned.hasOwnProperty(minion) && target[minion] != that.assigned[minion])
                    return false;
            return true;
        });
    },

    sameSet: function (a, b) {
        if (a.length != b.length)
            return false;
        for (var i = 0; i < a.length; i++)
            if ($.inArray(a[i], b) < 0)
                return false;
        return true;
    },

    /** Whether what has been built is a complete allowed answer, with only the confirming click left. */
    isReady: function () {
        var that = this;
        switch (this.type) {
            case "CARD_ACTION_CHOICE":
            case "ACTION_CHOICE":
                return this.chosen;
            case "CARD_SELECTION":
            case "ARBITRARY_CARDS":
                return $.grep(this.allowedSets(), function (set) {
                    return that.sameSet(set, that.selected);
                }).length > 0;
            case "ASSIGN_MINIONS":
                if (this.picked != null)
                    return false;
                return $.grep(this.possibleAssignments(), function (target) {
                    for (var minion in target)
                        if (target.hasOwnProperty(minion) && that.assigned[minion] != target[minion])
                            return false;
                    return true;
                }).length > 0;
            default:
                return false;
        }
    },

    /** The cards that may be clicked next. */
    liveCards: function () {
        var that = this;
        var live = [];
        var add = function (id) {
            if ($.inArray(id, live) < 0)
                live.push(id);
        };
        switch (this.type) {
            case "CARD_ACTION_CHOICE":
                if (!this.chosen) {
                    var onScreen = this.actionsOnScreen();
                    for (var cardId in onScreen)
                        if (onScreen.hasOwnProperty(cardId))
                            for (var i = 0; i < onScreen[cardId].length; i++)
                                if ($.inArray(onScreen[cardId][i], this.answers) > -1)
                                    add(cardId);
                }
                break;
            case "ACTION_CHOICE":
                if (!this.chosen)
                    for (var a = 0; a < this.param("actionId").length; a++)
                        if ($.inArray(this.param("actionId")[a], this.answers) > -1)
                            add("temp" + a);
                break;
            case "CARD_SELECTION":
            case "ARBITRARY_CARDS":
                $.each(this.allowedSets(), function (index, set) {
                    for (var s = 0; s < that.selected.length; s++)
                        if ($.inArray(that.selected[s], set) < 0)
                            return;
                    for (var c = 0; c < set.length; c++)
                        if ($.inArray(set[c], that.selected) < 0)
                            add(set[c]);
                });
                break;
            case "ASSIGN_MINIONS":
                //One pairing at a time, in the order the step gives them: what a message says about "this one"
                // and "now the other" is only true if the player cannot take them the other way round
                $.each(this.possibleOrders(), function (index, pairs) {
                    for (var p = 0; p < pairs.length; p++) {
                        if (that.picked != null) {
                            if (pairs[p].minion == that.picked)
                                add(pairs[p].character);
                        } else if (that.assigned[pairs[p].minion] != pairs[p].character) {
                            add(pairs[p].minion);
                            break;
                        }
                    }
                });
                break;
        }
        return live;
    },

    /** The option buttons of a multiple choice that may be clicked. */
    liveOptions: function () {
        var result = [];
        for (var i = 0; i < this.answers.length; i++)
            result.push(parseInt(this.answers[i]));
        return result;
    },

    isLive: function (click) {
        switch (click.kind) {
            case "card":
                return $.inArray(click.id, this.liveCards()) > -1;
            case "menu":
                return $.inArray(click.actionId, this.answers) > -1;
            case "pass":
                return this.allow.pass && !this.chosen;
            case "done":
                return this.isReady();
            case "reset":
                return true;
            case "option":
                return $.inArray(click.index, this.liveOptions()) > -1;
            case "ok":
                if (this.type == "MULTIPLE_CHOICE")
                    return $.inArray("" + $("#multipleChoiceDecision").val(), this.answers) > -1;
                if (this.type == "INTEGER")
                    return $.inArray("" + parseInt($("#integerDecision").val()), this.answers) > -1;
                return true;
            default:
                return true;
        }
    },

    /**
     * Follows a click that was let through.
     * @return the moments it brought about, as [{moment, cardId}]
     */
    apply: function (click) {
        var moments = [];
        var wasReady = this.isReady();
        if (click.kind == "reset") {
            this.reset();
            return moments;
        }
        if (click.kind == "menu") {
            this.chosen = true;
            if (this.menuCard != null)
                moments.push({moment: "picked", cardId: this.menuCard});
        } else if (click.kind == "card") {
            switch (this.type) {
                case "CARD_ACTION_CHOICE":
                    //A card with one action is chosen by the click; a card with several opens a menu of them
                    if (this.actionsOn(click.id) == 1) {
                        this.chosen = true;
                        moments.push({moment: "picked", cardId: click.id});
                    } else {
                        this.menuCard = click.id;
                    }
                    break;
                case "ACTION_CHOICE":
                    this.chosen = true;
                    moments.push({moment: "picked", cardId: click.id});
                    break;
                case "CARD_SELECTION":
                case "ARBITRARY_CARDS":
                    this.selected.push(click.id);
                    moments.push({moment: "picked", cardId: click.id});
                    break;
                case "ASSIGN_MINIONS":
                    if (this.picked == null) {
                        this.picked = click.id;
                        moments.push({moment: "picked", cardId: click.id});
                    } else {
                        this.assigned[this.picked] = click.id;
                        this.picked = null;
                        if (this.characterComplete(click.id))
                            moments.push({moment: "assigned", cardId: click.id});
                    }
                    break;
            }
        }
        if (!wasReady && this.isReady())
            moments.push({moment: "ready", cardId: null});
        return moments;
    },

    /** Whether a character now has every minion that every still-possible answer gives it. */
    characterComplete: function (characterId) {
        var that = this;
        var possible = this.possibleAssignments();
        if (possible.length == 0)
            return false;
        return $.grep(possible, function (target) {
            for (var minion in target)
                if (target.hasOwnProperty(minion) && target[minion] == characterId && that.assigned[minion] != characterId)
                    return true;
            return false;
        }).length == 0;
    }
});

var GempScenarioOverlay = Class.extend({
    STOCK_NOT_NOW: "You could do that in a real game, but for this lesson let's hold off for now.",
    /** How far a bubble sits from what it points at when nobody has said: in hundredths of the window's shorter side. */
    STOCK_GAP: "1.8",
    /** Below this the game screen has no room left for bubbles. */
    MIN_WIDTH: 1000,
    /** Where a bug is reported: the form behind the Game Hall's "Report a Bug" button (hall.html). */
    BUG_REPORT_URL: "https://forms.gle/pks6CHMWcpBsTcwv9",
    MIN_HEIGHT: 600,

    game: null,
    info: null,
    step: null,
    decision: null,
    plan: null,
    /**
     * idle        nothing of the scenario's is on the screen
     * reading     the step's opening messages are being read; the decision cannot be answered yet
     * answering   the last opening message is up (or there were none) and the decision can be answered
     * opponent    a step that is only read (an opponent's, or a pause): its messages are being read, and the server
     *             waits to be told they have been
     * ended       the scenario is over
     */
    phase: "idle",
    startMessages: null,
    messageIndex: 0,
    currentMessage: null,
    bubblePlaced: null,
    frame: null,

    init: function (game) {
        this.game = game;
        var that = this;

        this.layer = $("<div id='scenarioLayer'></div>").appendTo("body");
        this.highlights = $("<div class='scenario-highlights'></div>").appendTo(this.layer);
        //A card shown large (see showZoom): a shade over the table, a frame the card is seen through, and over the
        // card a darkening with holes in it where the message's subject is
        this.zoomBox = $("<div class='scenario-zoom' style='display:none'><div class='scenario-zoom-shade'></div>"
            + "<div class='scenario-zoom-frame'><img class='scenario-zoom-card' alt=''/>"
            + "<svg class='scenario-zoom-dim' xmlns='http://www.w3.org/2000/svg'><path fill-rule='evenodd'/></svg>"
            + "<div class='scenario-zoom-marks'></div></div></div>").appendTo(this.layer);
        //The line from a bubble to what it is about, when the two are apart
        // (one line for each thing it is about: see pointerLines)
        this.pointer = $("<svg class='scenario-pointer' xmlns='http://www.w3.org/2000/svg' style='display:none'></svg>").appendTo(this.layer);
        this.bubble = $("<div class='scenario-bubble' style='display:none'>"
            + "<div class='scenario-arrow'></div>"
            + "<div class='scenario-head'><span class='scenario-title'></span><span class='scenario-count'></span>"
            + "<a class='scenario-collapse' href='#' title='Fold this away'>&ndash;</a></div>"
            + "<div class='scenario-body'></div>"
            + "<div class='scenario-foot'><span class='scenario-choices'></span><button class='scenario-next'>Next</button></div>"
            + "</div>").appendTo(this.layer);
        this.hint = $("<div class='scenario-hint' style='display:none'></div>").appendTo(this.layer);

        this.bubble.find(".scenario-next").click(function () {
            that.next();
            return false;
        });
        this.bubble.find(".scenario-collapse").click(function () {
            that.bubble.toggleClass("scenario-collapsed");
            $(this).html(that.bubble.hasClass("scenario-collapsed") ? "+" : "&ndash;");
            return false;
        });
        this.makeDraggable();

        //Clicks on buttons and menu items are heard here before the game screen hears them, so that the ones a step
        // does not allow can be stopped.  Clicks on cards come through cardClicked instead.
        document.addEventListener("click", function (event) {
            that.clickCaptured(event);
        }, true);
        document.addEventListener("keydown", function (event) {
            that.keyCaptured(event);
        }, true);
    },

    /* ---------------------------------------------------------------- what the game screen tells the overlay */

    handleEvent: function (json) {
        var event;
        try {
            event = JSON.parse(json);
        } catch (e) {
            log("Not a scenario event: " + json);
            return;
        }
        if (event.type == "info")
            this.applyInfo(event);
        else if (event.type == "step")
            this.startStep(event);
        else if (event.type == "offscript")
            this.showNotice(event);
        else if (event.type == "end")
            this.showEnd(event);
    },

    /**
     * In a replay nobody is there to read and press Next, so a step's opening messages are each held on the screen
     * for a while and then the replay moves on.  Nothing is limited and nothing is sent anywhere.
     * @param lengthOf turns a length of time into what it is at the replay's current speed
     * @param done to be called when the replay may carry on
     */
    replayEvent: function (json, lengthOf, done) {
        var event;
        try {
            event = JSON.parse(json);
        } catch (e) {
            done();
            return;
        }
        if (event.type == "info") {
            this.applyInfo(event);
            done();
            return;
        }
        if (event.type != "step") {
            this.clearStep();
            done();
            return;
        }
        var that = this;
        this.step = event;
        this.phase = "idle";
        this.startMessages = $.grep(event.messages, function (message) {
            return message.on == null || message.on.moment == "start";
        });
        var showFrom = function (index) {
            if (index >= that.startMessages.length) {
                //The last one stays up over whatever it was about, until the next step replaces it
                done();
                return;
            }
            var message = that.startMessages[index];
            that.showMessage(message);
            that.bubble.find(".scenario-foot").hide();
            var words = $("<div></div>").html(message.html).text().split(/\s+/).length;
            setTimeout(function () {
                showFrom(index + 1);
            }, lengthOf(1500 + words * 280));
        };
        if (this.startMessages.length == 0) {
            this.clearStep();
            done();
        } else {
            showFrom(0);
        }
    },

    decisionShown: function (element) {
        var params = {};
        var parameters = element.getElementsByTagName("parameter");
        for (var i = 0; i < parameters.length; i++) {
            var name = parameters[i].getAttribute("name");
            if (params[name] == null)
                params[name] = [];
            params[name].push(parameters[i].getAttribute("value"));
        }
        this.decision = {
            id: element.getAttribute("id"),
            type: element.getAttribute("decisionType"),
            text: element.getAttribute("text"),
            params: params
        };
        this.plan = null;
        if (this.step != null && this.step.seat == "human" && this.step.allow != null)
            this.plan = new ScenarioPlan(this.decision, this.step.allow);
        this.markCards();
    },

    decisionAnswered: function () {
        this.decision = null;
        this.plan = null;
        this.hideHint();
        $(".scenario-live, .scenario-notnow").removeClass("scenario-live scenario-notnow");
        if (this.step != null && this.step.seat == "human")
            this.clearStep();
    },

    /* ---------------------------------------------------------------- info */

    applyInfo: function (info) {
        this.info = info;
        var game = this.game;
        $("body").addClass("scenario-game scenario-" + info.kind);

        //A scenario's text talks about confirming choices, so a click must not be sent off by itself; and the server
        // has already settled where the player is asked to pass.  Neither changes the player's own saved settings.
        game.settingsAutoAccept = info.autoAccept;
        game.settingsAutoDismissDeckReveal = false;
        $("#autoAccept, #autoDismissDeckReveal").attr("disabled", "disabled");
        $("#settingsBox .setting-group").first().find("input").attr("disabled", "disabled");

        this.renameOpponent();
        this.addMenu();
        this.takeOverFeedback();

        //Somebody writing the scenario as they play it gets the authoring panel beside the table (it is also what
        // shows the way back from a player's game of a draft)
        if (!game.replayMode && this.author == null && typeof GempScenarioAuthor != "undefined"
            && (info.author || info.draftTest))
            this.author = new GempScenarioAuthor(game, this, info.author);

        if (!game.replayMode && ($(window).width() < this.MIN_WIDTH || $(window).height() < this.MIN_HEIGHT))
            this.showTooSmall();
    },

    /**
     * Every finished game ends with a chat message asking for feedback, with a link that fills in the replay, and
     * the game screen puts it in a dialog of its own.  A scenario already has an end panel, so the link goes there
     * instead.  The message arrives after the end panel has been shown (the replay is saved once the game is over).
     */
    takeOverFeedback: function () {
        var that = this;
        var chatBox = this.game.chatBox;
        if (chatBox == null || this.feedbackTaken)
            return;
        this.feedbackTaken = true;
        chatBox.dialogListener = function (title, message) {
            var link = $("<div></div>").html(message).find("a").first().attr("href");
            if (link == null)
                return;
            that.reportUrl = link;
            that.addReportLink();
        };
    },

    addReportLink: function () {
        var panel = this.layer.find(".scenario-end").not(".scenario-small");
        if (this.reportUrl == null || panel.length == 0 || panel.find(".scenario-end-report").length > 0)
            return;
        $("<div class='scenario-end-report'></div>")
            .append(document.createTextNode("Something not right with this scenario? "))
            .append($("<a target='_blank' rel='noopener'></a>").attr("href", this.reportUrl).text("Report it"))
            .append(document.createTextNode(" (the replay is filled in for you)."))
            .appendTo(panel);
    },

    /** The opponent plays as one shared account; on screen it goes by the name the scenario gives it. */
    renameOpponent: function () {
        var info = this.info;
        if (info.opponentName == null || info.opponentName == info.opponent)
            return;
        var replaceIn = function (root) {
            $(root).find("*").andSelf().contents().each(function () {
                if (this.nodeType == 3 && this.nodeValue.indexOf(info.opponent) > -1)
                    this.nodeValue = this.nodeValue.split(info.opponent).join(info.opponentName);
            });
        };
        replaceIn(this.game.gameStateElem);
        replaceIn($("#chatBox"));
        replaceIn($(".ui-dialog-title"));
        replaceIn($("#pregame-info"));

        var chatBox = this.game.chatBox;
        if (chatBox != null && !chatBox.scenarioRenamed) {
            chatBox.scenarioRenamed = true;
            var append = chatBox.appendMessage;
            //The log takes its messages as markup, and the name is whatever the scenario's author typed
            var shownAs = $("<span></span>").text(info.opponentName).html();
            chatBox.appendMessage = function (message, msgClass) {
                if (typeof message == "string")
                    message = message.split(info.opponent).join(shownAs);
                return append.call(chatBox, message, msgClass);
            };
        }
    },

    addMenu: function () {
        if (this.menu != null || this.game.replayMode)
            return;
        var that = this;
        this.menu = $("<div class='scenario-menu'><span class='scenario-menu-title'></span>"
            + "<a href='#' class='scenario-menu-restart'>Start again</a>"
            + "<a href='#' class='scenario-menu-leave'>Leave</a>"
            + "<a class='scenario-menu-bug' target='_blank' rel='noopener'>Report a Bug</a></div>").appendTo(this.layer);
        this.menu.find(".scenario-menu-title").text(this.info.title);
        //(the form the Game Hall's own Report a Bug button opens)
        this.menu.find(".scenario-menu-bug").attr("href", this.BUG_REPORT_URL)
            .attr("title", "Something wrong with this scenario, or with the game? Tell us (opens in a new tab).");
        this.menu.find(".scenario-menu-restart").click(function () {
            if (that.phase == "ended" || that.game.gameEnded || confirm("Start this scenario again from the beginning?"))
                that.restart(false);
            return false;
        });
        this.menu.find(".scenario-menu-leave").click(function () {
            that.leave();
            return false;
        });
    },

    /* ---------------------------------------------------------------- steps */

    startStep: function (step) {
        this.step = step;
        this.hideHint();
        ScenarioAnchors.parts = step.parts || {};
        this.startMessages = $.grep(step.messages, function (message) {
            return message.on == null || message.on.moment == "start";
        });
        this.messageIndex = 0;

        if (step.seat == "opponent" || step.pause) {
            //There is nothing to answer while it is up: the server sends the decision once it has been read
            this.plan = null;
            this.phase = "opponent";
            if (this.startMessages.length == 0)
                this.acknowledge();
            else
                this.showMessage(this.startMessages[0]);
            this.markCards();
            if (this.author != null)
                this.author.stepShown(step);
            return;
        }

        this.answeringMessage = null;
        if (this.startMessages.length == 0) {
            this.phase = "answering";
            this.hideBubble();
        } else {
            this.phase = this.startMessages[0].advance == "decision" ? "answering" : "reading";
            if (this.phase == "answering")
                this.answeringMessage = this.startMessages[0];
            this.showMessage(this.startMessages[0]);
        }
        //A step can arrive again for a decision already on the screen (its answer was rejected).  For an author it
        // arrives again whenever they change it, and what it allows may be what changed.
        if (this.info != null && this.info.author && this.decision != null)
            this.plan = null;
        if (this.decision != null && step.allow != null && this.plan == null)
            this.plan = new ScenarioPlan(this.decision, step.allow);
        this.markCards();
        if (this.author != null)
            this.author.stepShown(step);
    },

    /** The Next button. */
    next: function () {
        if (this.step == null || this.currentMessage == null)
            return;
        this.messageIndex++;
        if (this.messageIndex < this.startMessages.length) {
            var message = this.startMessages[this.messageIndex];
            if (this.phase == "reading" && message.advance == "decision") {
                this.phase = "answering";
                this.answeringMessage = message;
            }
            this.showMessage(message);
            this.markCards();
        } else if (this.phase == "opponent") {
            this.acknowledge();
        } else {
            this.phase = "answering";
            this.hideBubble();
            this.markCards();
        }
    },

    /**
     * Tells the server that a step which is only read has been read.  The step stays up until the server has heard:
     * if the message is lost the game would wait for ever, with nothing on the screen to say why.
     * @param choice the name of the button pressed, if the step ended in a choice
     */
    acknowledge: function (choice) {
        var that = this;
        var step = this.step;
        if (step == null || this.acknowledging === step)
            return;
        this.acknowledging = step;
        var button = this.bubble.find(".scenario-foot button").attr("disabled", "disabled");
        this.game.communication.scenarioAcknowledge(step.step, choice, function () {
            that.acknowledging = null;
            button.removeAttr("disabled");
            //The game may already have moved on to its next step
            if (that.step === step)
                that.clearStep();
        }, function () {
            that.acknowledging = null;
            button.removeAttr("disabled");
            if (that.step !== step)
                return;
            if (that.currentMessage != null) {
                //(shown even on a message that had nothing to press: there has to be a way to say it again, and
                // watchInfoWindow must not go on saying it by itself)
                that.ackFailedFor = step;
                that.bubble.find(".scenario-foot").css("display", "");
                that.bubble.find(".scenario-next").css("display", "").text("Try again");
                that.nudge();
            } else {
                //Nothing is on the screen to press, so it is tried again without being asked
                setTimeout(function () {
                    if (that.step === step)
                        that.acknowledge();
                }, 2000);
            }
        });
    },

    clearStep: function () {
        this.step = null;
        this.phase = this.phase == "ended" ? "ended" : "idle";
        this.currentMessage = null;
        this.hideBubble();
        this.setHighlights([]);
    },

    /** Something has happened inside the answering of the decision; a message may have been waiting for it. */
    raise: function (moment, cardId) {
        if (this.step == null)
            return;
        var cards = this.step.cards || {};
        for (var i = 0; i < this.step.messages.length; i++) {
            var message = this.step.messages[i];
            if (message.on == null || message.on.moment != moment)
                continue;
            if (message.on.card != null && ("" + cards[message.on.card]) != ("" + cardId))
                continue;
            this.showMessage(message);
            return;
        }
    },

    /* ---------------------------------------------------------------- limiting clicks */

    /**
     * What a click on a button or a menu item is, as far as a decision is concerned; null if it has nothing to do
     * with one.  Cards are not looked at here: see cardClicked.
     */
    classify: function (target) {
        var tar = $(target);
        if (tar.hasClass("actionArea"))
            return null;
        var menuItem = tar.closest(".contextMenu a");
        if (menuItem.length > 0)
            return {kind: "menu", actionId: menuItem.attr("href").substr(1), elem: menuItem};

        var button = tar.closest("button");
        if (button.length == 0 || button.closest("#scenarioLayer").length > 0)
            return null;
        var id = button.attr("id");
        if (id == "Pass")
            return {kind: "pass", elem: button};
        if (id == "Done")
            return {kind: "done", elem: button};
        if (id == "ClearSelection")
            return {kind: "reset", elem: button};
        if (button.closest(".ui-dialog-buttonpane").length > 0) {
            var label = $.trim(button.text());
            if (label == "Done")
                return {kind: "done", elem: button};
            if (label == "Clear selection")
                return {kind: "reset", elem: button};
            if (label == "OK")
                return {kind: "ok", elem: button};
            return null;
        }
        var smallDialog = this.game.smallDialog;
        if (smallDialog != null && $.contains(smallDialog[0], button[0]))
            return {kind: "option", index: smallDialog.find("button").index(button), elem: button};
        return null;
    },

    clickCaptured: function (event) {
        if (this.info == null || this.game.replayMode)
            return;
        //An author picking something on the table for a message to point at is not playing
        if ($("body").hasClass("sa-picking"))
            return;
        //Whatever was last said about a click has been read by the time of the next one
        if (this.hintShownAt == null || Date.now() - this.hintShownAt > 400)
            this.hideHint();
        if (event.button > 0)
            return;
        var click = this.classify(event.target);
        if (click != null && !this.letThrough(click))
            this.stop(event);
    },

    /**
     * The game screen is about to act on a click on a card that is part of the decision (see clickCardFunction).
     * @return false if it should do nothing: the step does not allow the card, or not yet
     */
    cardClicked: function (cardElem) {
        if (this.info == null || this.game.replayMode)
            return true;
        this.hideHint();
        var card = cardElem.data("card");
        if (card == null)
            return true;
        return this.letThrough({kind: "card", id: "" + card.cardId, elem: cardElem});
    },

    /**
     * Decides about one click, and follows it if it is let through.
     * @return whether the game screen may act on it
     */
    letThrough: function (click) {
        if (this.phase == "reading" || this.phase == "opponent" || this.phase == "ended") {
            this.nudge();
            return false;
        }
        if (this.plan == null)
            return true;
        if (!this.plan.isLive(click)) {
            this.showHint(click);
            return false;
        }

        var that = this;
        var moments = this.plan.apply(click);
        //The game screen acts on the click after this; what is live next is worked out once it has
        setTimeout(function () {
            //Starting the choice over also takes back whatever was said along the way
            if (click.kind == "reset" && that.currentMessage != null && that.currentMessage.on != null) {
                if (that.answeringMessage != null)
                    that.showMessage(that.answeringMessage);
                else
                    that.hideBubble();
            }
            for (var i = 0; i < moments.length; i++)
                that.raise(moments[i].moment, moments[i].cardId);
            that.markCards();
        }, 0);
        return true;
    },

    /**
     * Space or Escape reads on: it presses the Next on the message that is up.  Not while something is being typed
     * (the chat box, most of all), not for a key that is being held down, and not when the key is for something
     * else (a card's information window to be closed, an author picking a thing on the table).
     * @return whether the key was taken
     */
    pressesNext: function (event) {
        if ((event.keyCode != 32 && event.keyCode != 27) || event.repeat || event.ctrlKey || event.altKey || event.metaKey
            || this.info == null || this.game.replayMode)
            return false;
        var active = document.activeElement;
        if (active != null && (active.tagName == "INPUT" || active.tagName == "TEXTAREA" || active.tagName == "SELECT" || active.isContentEditable))
            return false;
        if ($(event.target).closest("#scenarioAuthor").length > 0 || $("body").hasClass("sa-picking"))
            return false;
        var dialog = this.game.cardInfoDialog;
        if (event.keyCode == 27 && dialog != null && dialog.isOpen())
            return false;
        var next = this.bubble.find(".scenario-next");
        if (this.currentMessage == null || !this.bubble.is(":visible") || !next.is(":visible") || next.is(":disabled"))
            return false;
        //(a button that has the keyboard would be pressed by the space as well)
        if (active != null && active.tagName == "BUTTON")
            active.blur();
        next.click();
        return true;
    },

    keyCaptured: function (event) {
        if (this.pressesNext(event)) {
            this.stop(event);
            return;
        }
        //Escape confirms a choice from a list of cards, the same as its Done button
        if (event.keyCode != 27 || this.info == null || this.game.escFunction == null)
            return;
        if (this.phase == "reading" || this.phase == "opponent" || (this.plan != null && !this.plan.isLive({kind: "done"}))) {
            this.stop(event);
            //...but it still puts a card's information window away, which is all it would have been pressed for
            var dialog = this.game.cardInfoDialog;
            if (dialog != null && dialog.isOpen())
                dialog.infoDialog.dialog("close");
        }
    },

    stop: function (event) {
        event.stopPropagation();
        event.preventDefault();
    },

    /** Draws attention to the Next button when the player tries to get on with the game before reading. */
    nudge: function () {
        //Whatever the message is waiting for: its Next button, its choices, or (with neither) the message itself
        var next = this.bubble.find(".scenario-next:visible, .scenario-choices button:visible");
        if (next.length == 0)
            next = this.bubble.find(".scenario-body");
        if (next.length == 0 || this.currentMessage == null)
            return;
        next.removeClass("scenario-nudge");
        void next[0].offsetWidth;
        next.addClass("scenario-nudge");
    },

    /**
     * Marks every card the game is offering as either the next click or "not now", and outlines what comes next.
     * The game screen sets its own marks again whenever it redraws a choice, so this is redone after every click.
     */
    markCards: function () {
        $(".scenario-live, .scenario-notnow").removeClass("scenario-live scenario-notnow");
        this.nextTargets = [];
        //What a step allows holds for the whole of the step.  While its first messages are being read nothing can
        // be clicked yet, but the cards it does not allow are already marked "not now": a table lit up with
        // everything the game offers, and then with one card, reads as if the step had changed its mind.
        var reading = this.phase == "reading";
        if (this.plan == null || (this.phase != "answering" && !reading)) {
            this.refresh();
            return;
        }
        var live = this.plan.liveCards();
        var that = this;
        $(".selectableCard, .actionableCard").each(function () {
            var card = $(this).data("card");
            if (card == null)
                return;
            if ($.inArray("" + card.cardId, live) > -1) {
                $(this).addClass("scenario-live");
                if (!reading)
                    that.nextTargets.push($(this));
            } else {
                $(this).addClass("scenario-notnow");
            }
        });
        if (reading) {
            //(what to click next is pointed out once there is something to click)
            this.refresh();
            return;
        }
        if (this.plan.isReady()) {
            var done = ScenarioAnchors.button("done");
            if (done != null && done.length > 0)
                this.nextTargets.push(done);
        } else if (live.length == 0 && this.plan.isLive({kind: "pass"})) {
            var pass = ScenarioAnchors.button("pass");
            if (pass != null && pass.length > 0)
                this.nextTargets.push(pass);
        }
        if (this.plan.type == "MULTIPLE_CHOICE") {
            var options = this.plan.liveOptions();
            this.game.smallDialog.find("button").each(function (index) {
                if ($.inArray(index, options) > -1)
                    that.nextTargets.push($(this));
            });
        }
        this.refresh();
    },

    showHint: function (click) {
        var allow = this.step != null ? this.step.allow : null;
        var html = null;
        if (allow != null && click.kind == "card" && allow.hints != null) {
            var cards = this.step.cards || {};
            for (var alias in allow.hints)
                if (allow.hints.hasOwnProperty(alias) && ("" + cards[alias]) == click.id)
                    html = allow.hints[alias];
        }
        if (html == null && allow != null && allow.otherwise != null)
            html = allow.otherwise;
        if (html == null)
            html = "<p>" + this.STOCK_NOT_NOW + "</p>";

        var rect = ScenarioAnchors.rectOf(click.elem);
        this.hintShownAt = Date.now();
        this.hint.html(html).css({display: "block", left: 0, top: 0});
        var width = this.hint.outerWidth(), height = this.hint.outerHeight();
        var left = rect == null ? ($(window).width() - width) / 2 : rect.left + rect.width / 2 - width / 2;
        var top = rect == null ? 80 : rect.top - height - 10;
        if (top < 6 && rect != null)
            top = rect.top + rect.height + 10;
        this.hint.css({
            left: Math.max(6, Math.min(left, $(window).width() - width - 6)),
            top: Math.max(6, Math.min(top, $(window).height() - height - 6))
        });

        var that = this;
        clearTimeout(this.hintTimer);
        this.hintTimer = setTimeout(function () {
            that.hideHint();
        }, 7000);
    },

    hideHint: function () {
        clearTimeout(this.hintTimer);
        this.hint.hide();
    },

    /* ---------------------------------------------------------------- bubbles and outlines */

    /**
     * Everything a bubble says goes into the game log as well, once, as chat from the scenario's opponent: a bubble
     * is gone as soon as it has been read, and the log is where a player looks for what they missed.
     */
    logMessage: function (message) {
        var chatBox = this.game.chatBox;
        if (chatBox == null || this.step == null || message.html == null)
            return;
        var position = $.inArray(message, this.step.messages || []);
        var key = (this.step.step != null ? this.step.step : this.step.seat) + ":" + position;
        this.logged = this.logged || {};
        if (position > -1 && this.logged[key]) {
            //(an author's message is put up again each time they change it, and the log keeps up with it)
            if (this.info != null && this.info.author)
                $(".msg-content[data-scenario-says='" + key + "']").html(message.html);
            return;
        }
        this.logged[key] = true;
        var name = this.info != null && this.info.opponentName != null ? this.info.opponentName : "Scenario";
        var from = $("<span></span>").text(name).html();
        chatBox.appendMessage("<div class='msg-identifier'><span> <b>" + from + ": </b></span></div>"
            + "<div class='msg-content' data-scenario-says='" + key + "'>" + message.html + "</div>", "chatMessage scenario-says");
    },

    showMessage: function (message) {
        this.currentMessage = message;
        this.bubblePlaced = null;
        this.bubbleSituation = null;
        this.logMessage(message);
        var count = "";
        var position = $.inArray(message, this.startMessages);
        if (position > -1 && this.startMessages.length > 1)
            count = (position + 1) + " / " + this.startMessages.length;

        this.bubble.removeClass("scenario-collapsed scenario-dragged");
        this.bubble.find(".scenario-collapse").html("&ndash;");
        //A heading is the author's to give; most messages have none
        this.bubble.find(".scenario-title").text(message.header || "");
        this.bubble.toggleClass("scenario-headed", !!message.header);
        this.bubble.find(".scenario-count").text(count);
        this.bubble.find(".scenario-body").html(message.html);
        //What takes the message away: Next; one of its own buttons; the player doing what it asks (answering the
        // decision, opening or closing a card's information window), for which there is nothing to press
        var that = this;
        var choices = this.bubble.find(".scenario-choices").empty();
        $.each(message.choices || [], function (index, choice) {
            $("<button class='scenario-choice'></button>").text(choice.label).click(function () {
                that.acknowledge(choice.name);
                return false;
            }).appendTo(choices);
        });
        var chooses = (message.choices || []).length > 0;
        this.bubble.find(".scenario-foot").css("display", message.advance == "next" || chooses ? "" : "none");
        this.bubble.find(".scenario-next").css("display", chooses ? "none" : "").text(
            this.phase == "opponent" && position == this.startMessages.length - 1 ? "Continue" : "Next");
        this.infoWasOpen = this.infoIsOpen();
        this.bubble.css({display: "block", visibility: "hidden"});

        this.showZoom(message.zoom);
        //A card shown large covers the table: the pointer does not reach what is under it (a card under it would
        // otherwise come up enlarged over it).  Unless the message is waiting on something done on the table, which
        // has to be reachable through it.
        this.zoomBox.toggleClass("scenario-zoom-blocks", message.zoom != null && (message.advance == "next" || chooses));
        this.setHighlights(message.highlight || [], message.highlightStyle);
        this.refresh();
        this.bubble.css({visibility: "visible"});
    },

    hideBubble: function () {
        this.currentMessage = null;
        this.bubble.hide();
        this.pointer.hide();
        this.showZoom(null);
        this.setHighlights([]);
        this.refresh();
    },

    /* ---------------------------------------------------------------- a card shown large */

    /**
     * @param zoom {blueprint, magnify, focus: [{name, rect: [left, top, width, height]}]} with the rectangles in
     *             percent of the card; null to put the card away
     */
    showZoom: function (zoom) {
        this.zoom = zoom || null;
        this.zoomFrame = null;
        if (this.zoom == null) {
            this.zoomBox.hide();
            return;
        }
        var that = this;
        var image = this.zoomBox.find(".scenario-zoom-card");
        //A card, or a picture that is not one (a file in images/scenarios, by the address the server gives for it)
        var url = zoom.image != null ? zoom.image : Card.getImageUrl(zoom.blueprint);
        //Until the picture has arrived its shape is taken from the kind of card it is (and a picture's for a card's)
        this.zoomShape = zoom.image == null && Card.isHorizontal(Card.StripBlueprintId(zoom.blueprint), "SPECIAL") ? 497 / 355 : 357 / 497;
        image.unbind("load").bind("load", function () {
            if (this.naturalWidth > 0 && this.naturalHeight > 0)
                that.zoomShape = this.naturalWidth / this.naturalHeight;
            that.bubbleSituation = null;
        });
        if (image.attr("src") != url)
            image.attr("src", url);
        var marks = this.zoomBox.find(".scenario-zoom-marks").empty();
        for (var i = 0; i < zoom.focus.length; i++)
            marks.append("<div class='scenario-zoom-mark'></div>");
        this.zoomBox.show();
    },

    /**
     * Sizes the large card to the window and puts the bubble's space beside it.
     * @return the frame the card is seen through, and where on the screen each part in focus is
     */
    layoutZoom: function (bubbleWidth) {
        var zoom = this.zoom;
        var viewWidth = $(window).width() - (this.game.reservedRight || 0), viewHeight = $(window).height();
        var gap = 28;
        //As large as the window allows, with room left beside it for the bubble
        var frameHeight = Math.max(120, viewHeight - 56);
        var frameWidth = frameHeight * this.zoomShape;
        var room = Math.max(160, viewWidth - bubbleWidth - gap - 40);
        if (frameWidth > room) {
            frameWidth = room;
            frameHeight = frameWidth / this.zoomShape;
        }
        var frame = {
            left: Math.max(12, (viewWidth - (frameWidth + gap + bubbleWidth)) / 2),
            top: (viewHeight - frameHeight) / 2,
            width: frameWidth, height: frameHeight
        };

        //Magnified, the card is larger than its frame, and is slid so that what is in focus sits in the middle
        var scale = Math.max(1, zoom.magnify || 1);
        var cardWidth = frameWidth * scale, cardHeight = frameHeight * scale;
        var focusBox = null;
        for (var i = 0; i < zoom.focus.length; i++) {
            var rect = zoom.focus[i].rect;
            focusBox = focusBox == null ? {left: rect[0], top: rect[1], right: rect[0] + rect[2], bottom: rect[1] + rect[3]} : {
                left: Math.min(focusBox.left, rect[0]), top: Math.min(focusBox.top, rect[1]),
                right: Math.max(focusBox.right, rect[0] + rect[2]), bottom: Math.max(focusBox.bottom, rect[1] + rect[3])
            };
        }
        var centreX = focusBox == null ? 50 : (focusBox.left + focusBox.right) / 2;
        var centreY = focusBox == null ? 50 : (focusBox.top + focusBox.bottom) / 2;
        var cardLeft = Math.min(0, Math.max(frameWidth - cardWidth, frameWidth / 2 - cardWidth * centreX / 100));
        var cardTop = Math.min(0, Math.max(frameHeight - cardHeight, frameHeight / 2 - cardHeight * centreY / 100));

        this.zoomBox.find(".scenario-zoom-frame").css(frame);
        this.zoomBox.find(".scenario-zoom-card").css({left: cardLeft, top: cardTop, width: cardWidth, height: cardHeight});

        //Everything on the card but what is in focus is darkened; each part in focus gets an outline
        var path = "M0 0H" + frameWidth + "V" + frameHeight + "H0Z";
        var marks = this.zoomBox.find(".scenario-zoom-mark");
        var onScreen = [];
        for (var f = 0; f < zoom.focus.length; f++) {
            var part = zoom.focus[f].rect;
            var left = cardLeft + cardWidth * part[0] / 100, top = cardTop + cardHeight * part[1] / 100;
            var width = cardWidth * part[2] / 100, height = cardHeight * part[3] / 100;
            //Only as much of it as shows through the frame
            var right = Math.min(frameWidth, left + width), bottom = Math.min(frameHeight, top + height);
            left = Math.max(0, left);
            top = Math.max(0, top);
            if (right - left < 2 || bottom - top < 2) {
                $(marks[f]).hide();
                continue;
            }
            path += "M" + left + " " + top + "H" + right + "V" + bottom + "H" + left + "Z";
            $(marks[f]).css({display: "block", left: left - 3, top: top - 3, width: right - left + 6, height: bottom - top + 6});
            onScreen.push({left: frame.left + left, top: frame.top + top, width: right - left, height: bottom - top});
        }
        var dim = this.zoomBox.find(".scenario-zoom-dim");
        dim.attr({width: frameWidth, height: frameHeight}).css("display", zoom.focus.length > 0 ? "block" : "none");
        dim.find("path").attr("d", path);
        return {frame: frame, focus: onScreen};
    },

    setHighlights: function (anchors, style) {
        this.highlightAnchors = anchors || [];
        this.highlightStyle = style || "gold";
    },

    /** Starts (or keeps going) the loop that keeps bubbles and outlines where the things they are about are. */
    refresh: function () {
        var that = this;
        if (this.frame != null)
            return;
        var tick = function () {
            that.frame = null;
            if (that.draw())
                that.frame = window.requestAnimationFrame(tick);
        };
        tick();
    },

    infoIsOpen: function () {
        var dialog = this.game.cardInfoDialog;
        return dialog != null && dialog.isOpen();
    },

    /**
     * A message can wait for the player to open a card's information window, or to close it again.  Nothing tells
     * anybody when that happens, so it is looked for each time the screen is drawn.
     */
    watchInfoWindow: function () {
        var message = this.currentMessage;
        if (message == null || this.step == null || this.game.replayMode || this.ackFailedFor === this.step)
            return;
        var open = this.infoIsOpen();
        var was = this.infoWasOpen;
        this.infoWasOpen = open;
        if (message.advance == "inspect" && open && !was) {
            var wanted = message.advanceCard == null ? null : (this.step.cards || {})[message.advanceCard];
            var shown = this.game.cardInfoDialog.card;
            if (wanted == null || (shown != null && ("" + shown.cardId) == ("" + wanted)))
                this.next();
        } else if (message.advance == "close" && !open) {
            this.next();
        }
    },

    /** @return whether there is anything on the screen that needs keeping in place */
    draw: function () {
        this.watchInfoWindow();
        var cards = this.step != null && this.step.cards != null ? this.step.cards : {};
        var boxes = [];
        for (var i = 0; i < (this.highlightAnchors || []).length; i++) {
            var rect = ScenarioAnchors.resolve(this.game, this.highlightAnchors[i], cards);
            if (rect != null)
                boxes.push({rect: rect, style: "scenario-highlight scenario-highlight-" + this.highlightStyle});
        }
        for (var n = 0; n < (this.nextTargets || []).length; n++) {
            var nextRect = ScenarioAnchors.rectOf(this.nextTargets[n]);
            if (nextRect != null)
                boxes.push({rect: nextRect, style: "scenario-highlight scenario-next-click"});
        }

        var drawn = this.highlights.children();
        for (var b = 0; b < boxes.length; b++) {
            var box = b < drawn.length ? $(drawn[b]) : $("<div></div>").appendTo(this.highlights);
            var pad = 4;
            box.attr("class", boxes[b].style).css({
                left: boxes[b].rect.left - pad, top: boxes[b].rect.top - pad,
                width: boxes[b].rect.width + pad * 2, height: boxes[b].rect.height + pad * 2
            });
        }
        for (var extra = boxes.length; extra < drawn.length; extra++)
            $(drawn[extra]).remove();

        if (this.currentMessage != null)
            this.placeBubble(cards, boxes);
        return this.currentMessage != null || boxes.length > 0;
    },

    /** The things a message points at that are on the screen, as rectangles. */
    anchorsOf: function (message, cards) {
        var names = message.anchors != null ? message.anchors : (message.anchor != null ? [message.anchor] : []);
        var rects = [];
        for (var i = 0; i < names.length; i++) {
            //(a zone is pointed at where its cards are)
            var rect = ScenarioAnchors.resolve(this.game, names[i], cards, true);
            if (rect != null)
                rects.push(rect);
        }
        return rects;
    },

    /** The rectangle around several; of one, that one itself; of none, null. */
    around: function (rects) {
        if (rects.length < 2)
            return rects.length == 0 ? null : rects[0];
        var left = Infinity, top = Infinity, right = -Infinity, bottom = -Infinity;
        for (var i = 0; i < rects.length; i++) {
            left = Math.min(left, rects[i].left);
            top = Math.min(top, rects[i].top);
            right = Math.max(right, rects[i].left + rects[i].width);
            bottom = Math.max(bottom, rects[i].top + rects[i].height);
        }
        return {left: left, top: top, width: right - left, height: bottom - top};
    },

    /**
     * How far from what it points at a message's bubble sits, in pixels.  Written as a plain number it is in
     * hundredths of the window's shorter side, which keeps its proportion to the cards whatever the screen; "20px"
     * is pixels.  A message that does not say takes the scenario's, and a scenario that does not say the stock one.
     */
    gapOf: function (message) {
        var written = message.gap;
        if (written == null || written === "")
            written = this.info != null ? this.info.bubbleGap : null;
        if (written == null || written === "" || isNaN(parseFloat(written)))
            written = this.STOCK_GAP;
        written = ("" + written).toLowerCase();
        var amount = parseFloat(written);
        return written.indexOf("px") > -1 ? amount : amount * Math.min($(window).width(), $(window).height()) / 100;
    },

    placeBubble: function (cards, boxes) {
        var message = this.currentMessage;
        var anchors = this.anchorsOf(message, cards);
        var anchor = this.around(anchors);
        var width = this.bubble.outerWidth(), height = this.bubble.outerHeight();
        var viewWidth = $(window).width() - (this.game.reservedRight || 0), viewHeight = $(window).height();
        var margin = 8, gap = this.gapOf(message);

        //What the bubble points at is usually what it sits beside.  With a card shown large it sits beside the card
        // and points at the part of it in focus.
        var targets = anchors;
        var side = message.side;
        if (this.zoom != null) {
            var zoomed = this.layoutZoom(width);
            anchor = zoomed.frame;
            anchors = [anchor];
            targets = zoomed.focus.length > 0 ? [zoomed.focus[0]] : [];
            side = "right";
            boxes = [];
        }

        //A bubble that has been moved stays where it was put, and keeps its line to what it is about
        if (this.bubble.hasClass("scenario-dragged")) {
            var moved = this.bubble[0].getBoundingClientRect();
            this.drawPointer({left: moved.left, top: moved.top, width: moved.width, height: moved.height}, targets, null);
            return;
        }

        //What the bubble must not cover: the things it points at, everything outlined (which includes whatever is
        // to be clicked next), whatever the message says to avoid, and the buttons that answer the decision
        var keepClear = anchors.slice(0);
        for (var b = 0; b < boxes.length; b++)
            keepClear.push(boxes[b].rect);
        for (var a = 0; a < (message.avoid || []).length; a++) {
            var avoid = ScenarioAnchors.resolve(this.game, message.avoid[a], cards);
            if (avoid != null)
                keepClear.push(avoid);
        }
        var buttons = ScenarioAnchors.rectOf(this.game.alertButtons != null ? this.game.alertButtons.children() : null);
        if (buttons != null)
            keepClear.push(buttons);
        //A dialog that is open is either the decision itself or something the player has just asked to see.  A
        // bubble about something inside a dialog has to sit over part of it, though.
        var that = this;
        $(".ui-dialog:visible").each(function () {
            var rect = ScenarioAnchors.rectOf($(this));
            if (rect != null && (anchor == null || that.overlap(anchor, rect) == 0))
                keepClear.push(rect);
        });

        //A card shown large has the screen to itself: nothing on the table underneath matters
        if (this.zoom != null)
            keepClear = [anchor];

        //Nothing to do unless something it depends on has moved
        var situation = [width, height, viewWidth, viewHeight, Math.round(gap)];
        for (var r = 0; r < keepClear.length; r++)
            situation.push(Math.round(keepClear[r].left), Math.round(keepClear[r].top),
                Math.round(keepClear[r].width), Math.round(keepClear[r].height));
        situation = situation.join("|");
        if (situation == this.bubbleSituation)
            return;
        this.bubbleSituation = situation;

        //Cards it would be a pity to cover, though not a mistake: the bubble goes where there are fewest
        var otherCards = [];
        $(this.zoom != null ? [] : ".card:visible").each(function () {
            var rect = this.getBoundingClientRect();
            if (rect.width > 0 && rect.height > 0)
                otherCards.push({left: rect.left, top: rect.top, width: rect.width, height: rect.height});
        });

        var place;
        if (anchor == null) {
            //About nothing in particular: as near the middle of the screen as leaves clear what must be
            var wantLeft = (viewWidth - width) / 2, wantTop = Math.max(margin, viewHeight * 0.38 - height / 2);
            var across = 6, down = 6;
            var free = null;
            for (var col = 0; col <= across; col++) {
                for (var row = 0; row <= down; row++) {
                    var spot = {
                        left: margin + (viewWidth - width - margin * 2) * col / across,
                        top: margin + (viewHeight - height - margin * 2) * row / down,
                        width: width, height: height
                    };
                    var price = (Math.abs(spot.left - wantLeft) + Math.abs(spot.top - wantTop)) / 20;
                    for (var kc = 0; kc < keepClear.length; kc++)
                        price += this.overlap(spot, keepClear[kc]) / 50;
                    for (var oc = 0; oc < otherCards.length; oc++)
                        price += this.overlap(spot, otherCards[oc]) / 400;
                    if (free == null || price < free.cost)
                        free = {side: "none", left: spot.left, top: spot.top, cost: price};
                }
            }
            //Where it would go by itself, if that is as good as anywhere
            var middle = {left: wantLeft, top: wantTop, width: width, height: height};
            var middlePrice = 0;
            for (var mk = 0; mk < keepClear.length; mk++)
                middlePrice += this.overlap(middle, keepClear[mk]) / 50;
            for (var mc = 0; mc < otherCards.length; mc++)
                middlePrice += this.overlap(middle, otherCards[mc]) / 400;
            place = (free == null || middlePrice <= free.cost) ? {side: "none", left: wantLeft, top: wantTop} : free;
        } else {
            //A side the scenario names is the side it goes on, and level with the middle of the thing, whatever
            // cards that covers: the author has seen what it covers.  Only what it must not cover (what is outlined,
            // what is to be clicked, what the message says to keep clear of) still slides it along that side.  Left
            // to itself it goes on whichever side costs least, and, where they cost much the same, above first: a
            // bubble over what it points at reads before the thing, and keeps clear of the hand along the bottom.
            var order = ["above", "right", "left", "below"];
            var named = side != null && side != "auto" && $.inArray(side, order) > -1;
            if (named)
                order = [side];
            //Beside all of what it points at taken together; or, pointing at several things, beside any one of them
            var beside = anchors.length > 1 ? [anchor].concat(anchors) : [anchor];
            var best = null;
            for (var w = 0; w < beside.length; w++) {
                var box = beside[w];
                var centreX = box.left + box.width / 2, centreY = box.top + box.height / 2;
                var right = box.left + box.width, bottom = box.top + box.height;
                //On each side in turn: level with its middle if there is room, otherwise slid along towards one
                // end or the other, as far as still leaves the pointer something to point from
                var lap = 30;
                var slideDown = [centreY - height / 2, box.top, bottom - height, bottom - lap, box.top + lap - height];
                var slideAcross = [centreX - width / 2, box.left, right - width, right - lap, box.left + lap - width];
                for (var s = 0; s < order.length; s++) {
                    for (var slide = 0; slide < 5; slide++) {
                        var candidate = order[s] == "right" ? {left: right + gap, top: slideDown[slide]}
                            : order[s] == "left" ? {left: box.left - gap - width, top: slideDown[slide]}
                            : order[s] == "below" ? {left: slideAcross[slide], top: bottom + gap}
                            : {left: slideAcross[slide], top: box.top - gap - height};
                        var left = Math.max(margin, Math.min(candidate.left, viewWidth - width - margin));
                        var top = Math.max(margin, Math.min(candidate.top, viewHeight - height - margin));
                        //How far it had to be pushed back onto the screen, and how much it covers of what it must not
                        var cost = Math.abs(left - candidate.left) + Math.abs(top - candidate.top);
                        var where = {left: left, top: top, width: width, height: height};
                        for (var k = 0; k < keepClear.length; k++)
                            cost += this.overlap(where, keepClear[k]) / 50;
                        for (var c = 0; !named && c < otherCards.length; c++)
                            cost += this.overlap(where, otherCards[c]) / 400;
                        //Earlier sides win ties, and a little more, so the bubble does not hop about; level with
                        // the middle beats slid along; and beside the lot beats beside one of them
                        cost += s * 4 + (slide == 0 ? 0 : 3 + slide) + (w == 0 ? 0 : 2);
                        if (best == null || cost < best.cost)
                            best = {side: order[s], left: left, top: top, cost: cost};
                    }
                }
            }
            place = best;
        }

        var key = Math.round(place.left) + "," + Math.round(place.top) + "," + place.side;
        if (key != this.bubblePlaced) {
            this.bubblePlaced = key;
            this.bubble.css({left: Math.round(place.left), top: Math.round(place.top)});
        }

        this.drawPointer({left: place.left, top: place.top, width: width, height: height}, targets,
            targets.length == 1 && targets[0] === anchor ? place.side : null);
    },

    /** The lines of the pointer, one (with its dark edge and the dot at its end) for each thing pointed at. */
    pointerLines: function (count) {
        var svg = this.pointer[0];
        var space = "http://www.w3.org/2000/svg";
        var groups = svg.getElementsByTagName("g");
        while (groups.length > count)
            svg.removeChild(groups[groups.length - 1]);
        while (groups.length < count) {
            var group = document.createElementNS(space, "g");
            $.each([["line", "scenario-pointer-edge"], ["line", "scenario-pointer-line"], ["circle", "scenario-pointer-end"]], function (index, part) {
                var node = document.createElementNS(space, part[0]);
                node.setAttribute("class", part[1]);
                if (part[0] == "circle")
                    node.setAttribute("r", "4");
                group.appendChild(node);
            });
            svg.appendChild(group);
        }
        return groups;
    },

    /**
     * Joins the bubble to what it is about.  Sitting right beside the one thing, a small arrow on the bubble's edge
     * does that.  Anywhere else (moved by hand, set further off, beside a large card and pointing into it) a thin
     * line runs from one to the other; and about several things, a line to each.
     * @param targets  the things it is about, as rectangles
     * @param besideOn the side of the one target the bubble was placed on ("right", "left", "below", "above"); null
     *                 if it was not placed beside it
     */
    drawPointer: function (bubble, targets, besideOn) {
        var that = this;
        var arrow = this.bubble.find(".scenario-arrow");
        //(nothing points at a thing the bubble is lying on)
        targets = this.bubble.hasClass("scenario-collapsed") ? [] : $.grep(targets || [], function (each) {
            return that.overlap(bubble, each) == 0;
        });
        if (targets.length == 0) {
            arrow.hide();
            this.pointer.hide();
            return;
        }
        var target = targets[0];
        if (targets.length > 1)
            besideOn = null;

        //Beside it means close against that side and level with some part of it: as close as a bubble is put when
        // nobody says how close.  Set further off than that, an arrow would point at nothing in particular.
        var reach = Math.max(22, parseFloat(this.STOCK_GAP) * Math.min($(window).width(), $(window).height()) / 100 + 6);
        var beside = false;
        if (besideOn == "right")
            beside = Math.abs(bubble.left - (target.left + target.width)) <= reach;
        else if (besideOn == "left")
            beside = Math.abs(target.left - (bubble.left + bubble.width)) <= reach;
        else if (besideOn == "below")
            beside = Math.abs(bubble.top - (target.top + target.height)) <= reach;
        else if (besideOn == "above")
            beside = Math.abs(target.top - (bubble.top + bubble.height)) <= reach;
        if (besideOn == "right" || besideOn == "left")
            beside = beside && target.top < bubble.top + bubble.height && target.top + target.height > bubble.top;
        else if (besideOn != null)
            beside = beside && target.left < bubble.left + bubble.width && target.left + target.width > bubble.left;

        if (beside) {
            //The arrow sits on the edge nearest the thing and slides along it to line up with the thing's middle
            var along;
            if (besideOn == "right" || besideOn == "left") {
                along = Math.max(14, Math.min(target.top + target.height / 2 - bubble.top, bubble.height - 14));
                arrow.attr("class", "scenario-arrow scenario-arrow-" + (besideOn == "right" ? "left" : "right")).css({top: along - 9, left: ""});
            } else {
                along = Math.max(14, Math.min(target.left + target.width / 2 - bubble.left, bubble.width - 14));
                arrow.attr("class", "scenario-arrow scenario-arrow-" + (besideOn == "below" ? "up" : "down")).css({left: along - 9, top: ""});
            }
            arrow.show();
            this.pointer.hide();
            return;
        }

        //From the edge of the bubble to the edge of each thing, along the line between their middles
        arrow.hide();
        this.pointer.attr({width: $(window).width(), height: $(window).height()}).show();
        var lines = this.pointerLines(targets.length);
        for (var t = 0; t < targets.length; t++) {
            var from = this.edgePoint(bubble, targets[t]);
            var to = this.edgePoint(targets[t], bubble);
            $(lines[t]).find("line").attr({x1: from.x, y1: from.y, x2: to.x, y2: to.y});
            $(lines[t]).find("circle").attr({cx: to.x, cy: to.y});
        }
    },

    /** Where the line from the middle of one rectangle to the middle of another leaves the first. */
    edgePoint: function (rect, towards) {
        var cx = rect.left + rect.width / 2, cy = rect.top + rect.height / 2;
        var dx = towards.left + towards.width / 2 - cx, dy = towards.top + towards.height / 2 - cy;
        if (dx == 0 && dy == 0)
            return {x: cx, y: cy};
        var scale = Math.min(dx == 0 ? Infinity : (rect.width / 2) / Math.abs(dx), dy == 0 ? Infinity : (rect.height / 2) / Math.abs(dy));
        return {x: cx + dx * scale, y: cy + dy * scale};
    },

    overlap: function (a, b) {
        var x = Math.min(a.left + a.width, b.left + b.width) - Math.max(a.left, b.left);
        var y = Math.min(a.top + a.height, b.top + b.height) - Math.max(a.top, b.top);
        return x > 0 && y > 0 ? x * y : 0;
    },

    /** A bubble can be dragged out of the way by its heading; it then stays where it was put. */
    makeDraggable: function () {
        var that = this;
        var start = null;
        this.bubble.find(".scenario-head").mousedown(function (event) {
            if ($(event.target).hasClass("scenario-collapse"))
                return;
            var offset = that.bubble.offset();
            start = {x: event.clientX, y: event.clientY, left: offset.left - window.pageXOffset, top: offset.top - window.pageYOffset};
            event.preventDefault();
            event.stopPropagation();
        });
        $(document).mousemove(function (event) {
            if (start == null)
                return;
            that.bubble.addClass("scenario-dragged").css({
                left: start.left + event.clientX - start.x,
                top: start.top + event.clientY - start.y
            });
            that.refresh();
        }).mouseup(function () {
            start = null;
        });
    },

    /* ---------------------------------------------------------------- notices and endings */

    showNotice: function (event) {
        this.clearStep();
        this.phase = "answering";
        this.startMessages = [];
        this.step = {seat: "notice", messages: [], cards: {}, allow: null};
        this.showMessage({
            title: this.info != null ? this.info.title : "", html: event.message || "<p>You have left the script.</p>",
            anchor: null, highlight: [], avoid: [], side: "auto", advance: "next", on: null
        });
        this.bubble.find(".scenario-next").text("OK");
    },

    showEnd: function (event) {
        this.clearStep();
        this.phase = "ended";
        this.hideHint();
        //Nothing to say in a replay, nor to somebody who is already on their way out
        if (this.game.replayMode || this.leaving)
            return;

        var that = this;
        var headings = {
            abandoned: "This game has been closed",
            complete: "Complete",
            failed: "Not this time",
            off_script: "That's not how this one goes",
            error: "Something went wrong"
        };
        var body = "";
        for (var i = 0; i < (event.messages || []).length; i++) {
            if (event.messages[i].title)
                body += "<h3>" + $("<span></span>").text(event.messages[i].title).html() + "</h3>";
            body += event.messages[i].html;
        }
        if (event.message)
            body += event.message;
        if (body == "") {
            body = {
                complete: "<p>You have finished <b></b>.</p>",
                failed: "<p>That didn't work out. Have another go.</p>",
                off_script: "<p>The game went somewhere this scenario wasn't written for.</p>",
                abandoned: "<p>Your game of <b></b> is no longer running. That happens when a scenario is left alone "
                    + "for too long, or when you start another one.</p>",
                error: "<p>The scenario could not carry on. What happened has been recorded.</p>"
            }[event.result] || "";
        }

        var panel = $("<div class='scenario-end scenario-end-" + event.result + "'><h2></h2><div class='scenario-end-body'></div>"
            + "<div class='scenario-end-buttons'></div></div>");
        panel.find("h2").text(headings[event.result] || "The scenario is over");
        panel.find(".scenario-end-body").html(body);
        panel.find(".scenario-end-body b:empty").text(this.info != null ? this.info.title : "this scenario");

        var buttons = panel.find(".scenario-end-buttons");
        var addButton = function (label, action, primary) {
            return $("<button></button>").text(label).addClass(primary ? "scenario-primary" : "").click(function () {
                action();
                return false;
            }).appendTo(buttons);
        };
        //One of a campaign, and done: the ways back on the left, and on to the next one on the right
        var campaign = this.info != null ? this.info.campaign : null;
        if (event.result == "complete" && campaign != null) {
            buttons.addClass("scenario-end-campaign");
            addButton("Back to the Game Hall", function () {
                that.backToHall();
            }, false);
            addButton("Play it Again", function () {
                that.restart(false);
            }, false);
            if (campaign.next != null) {
                addButton("Next Scenario", function () {
                    that.startOther(campaign.next.id);
                }, true).addClass("scenario-end-onward").attr("title", campaign.next.title);
            } else {
                panel.find(".scenario-end-body").append($("<p class='scenario-end-last'></p>").text(
                    "That was the last of " + campaign.title + "."));
                buttons.find("button").first().addClass("scenario-primary");
            }
        } else {
            if (event.result != "complete" && event.result != "abandoned" && event.checkpoint != null)
                addButton("Back to the last checkpoint", function () {
                    that.restart(true);
                }, true);
            if (event.result != "error")
                addButton(event.result == "complete" ? "Play it again" : "Start again", function () {
                    that.restart(false);
                }, event.result != "complete" && event.checkpoint == null);
            addButton("Back to the Game Hall", function () {
                that.backToHall();
            }, event.result == "complete" || event.result == "error");
        }

        this.layer.find(".scenario-end, .scenario-shade").remove();
        this.layer.append("<div class='scenario-shade'></div>").append(panel);
        this.addReportLink();
    },

    showTooSmall: function () {
        if (this.layer.find(".scenario-small").length > 0)
            return;
        var that = this;
        var panel = $("<div class='scenario-end scenario-small'><h2>This needs a bigger screen</h2>"
            + "<div class='scenario-end-body'><p>Tutorials point at things on the game table, and there isn't room for that here. "
            + "Try a desktop or laptop, or make this window larger.</p></div>"
            + "<div class='scenario-end-buttons'><button class='scenario-primary'>Back to the Game Hall</button>"
            + "<button>Carry on anyway</button></div></div>");
        panel.find("button").first().click(function () {
            that.backToHall();
        });
        panel.find("button").last().click(function () {
            that.layer.find(".scenario-small, .scenario-shade").remove();
        });
        this.layer.append("<div class='scenario-shade'></div>").append(panel);
    },

    restart: function (fromCheckpoint) {
        if (this.info == null)
            return;
        //An author's game starts again by the script playing itself, not from the scenario's file
        if (this.author != null && this.author.restart(fromCheckpoint))
            return;
        this.startOther(this.info.id, fromCheckpoint);
    },

    /** Starts a game of a scenario (this one again, or the next of its campaign) in this tab. */
    startOther: function (id, fromCheckpoint) {
        var that = this;
        //Starting another gives this game up, and its ending is not news
        this.leaving = true;
        this.game.communication.startScenario(id, fromCheckpoint === true, function (result) {
            location.href = "/gemp-lotr/game.html?gameId=" + result.gameId;
        }, function () {
            that.leaving = false;
            alert("The scenario could not be started. Go back to the Game Hall and try from there.");
        });
    },

    leave: function () {
        if (this.phase == "ended" || this.game.gameEnded) {
            this.backToHall();
            return;
        }
        //An author's draft, and the game it is being written in, stay as they are to be come back to (unless there
        // is something in it that has not been saved: then they are asked what is to become of it)
        if (this.author != null && this.author.authoring) {
            this.backToHall();
            return;
        }
        if (!confirm("Leave this scenario? You can start it again from the Game Hall."))
            return;
        var that = this;
        this.leaving = true;
        this.game.communication.concede();
        setTimeout(function () {
            that.backToHall();
        }, 300);
    },

    /**
     * A game opens in a tab of its own, and the Game Hall it was started from is usually still open in the tab it
     * came from.  Opening the hall again here would make two of them, which the server takes for somebody else
     * logging in.  So: ask whether a hall is open (the hall answers on a channel between this site's tabs, see
     * hallUi.js).  If one is, this tab closes and the browser goes back to it.  If none answers, the hall opens here.
     */
    backToHall: function () {
        //Somebody writing a scenario, with changes that are in no file, says first what is to become of them
        if (this.author != null && this.author.unsaved()) {
            this.author.askBeforeLeaving();
            return;
        }
        this.goToHall();
    },

    /**
     * Back to the Game Hall: this tab closes itself if there is a hall open in another (which is shown 'hash', a hall
     * link such as "#account/scenarios", if one is given), and goes to the hall itself if there is not.
     */
    goToHall: function (hash) {
        var that = this;
        this.leaving = true;
        var here = function () {
            location.href = "/gemp-lotr/hall.html" + (hash || "");
        };
        var channel;
        try {
            channel = new BroadcastChannel("gemp-lotr-hall");
        } catch (e) {
            here();
            return;
        }
        var answered = false;
        channel.onmessage = function (event) {
            if (answered || event.data == null || event.data.type != "here")
                return;
            answered = true;
            if (hash)
                channel.postMessage({type: "go", hash: hash});
            channel.close();
            window.close();
            //A tab the player opened by hand is not a script's to close.  Still here a moment later means that.
            setTimeout(function () {
                that.showHallIsOpen(here);
            }, 400);
        };
        channel.postMessage({type: "anyone"});
        setTimeout(function () {
            if (answered)
                return;
            channel.close();
            here();
        }, 350);
    },

    showHallIsOpen: function (openHere) {
        this.layer.find(".scenario-end, .scenario-shade").remove();
        var panel = $("<div class='scenario-end'><h2>The Game Hall is already open</h2>"
            + "<div class='scenario-end-body'><p>It is in another tab of this browser. You can close this tab and go back to it.</p></div>"
            + "<div class='scenario-end-buttons'><button>Open it here instead</button></div></div>");
        panel.find("button").click(function () {
            openHere();
            return false;
        });
        this.layer.append("<div class='scenario-shade'></div>").append(panel);
    }
});
