var GameAnimations = Class.extend({
    game:null,
    replaySpeed:1,
    playEventDuration:1500,
    putCardIntoPlayDuration:1500,
    cardAffectsCardDuration:1200,
    cardActivatedDuration:1200,
    decisionDuration:1200,
    removeCardFromPlayDuration:600,

    init:function (gameUI) {
        this.game = gameUI;
    },

    getAnimationLength:function (origValue) {
        if (this.game.replayMode)
            return origValue * this.replaySpeed;
        return origValue;
    },

    cardActivated:function (element, animate) {
        if (animate) {
            var that = this;

            var participantId = element.getAttribute("participantId");
            var cardId = element.getAttribute("cardId");

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        var cardDiv = $(".card:cardId(" + cardId + ")");
                        if (cardDiv.length > 0) {
                            $(".borderOverlay", cardDiv)
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("borderOverlay", "highlightBorderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6))
                                .switchClass("highlightBorderOverlay", "borderOverlay", that.getAnimationLength(that.cardActivatedDuration / 6), next);
                        }
                        else {
                            next();
                        }
                    });
            }
        }
    },

    eventPlayed:function (element, animate) {
        if (animate) {
            var that = this;

            var participantId = element.getAttribute("participantId");
            var blueprintId = element.getAttribute("blueprintId");
            var testingText = element.getAttribute("testingText");
            var backSideTestingText = element.getAttribute("backSideTestingText");

            // Play-out game event animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                var card = new Card(blueprintId, testingText, backSideTestingText, "ANIMATION", "anim", participantId);
                var cardDiv = Card.CreateSimpleCardDiv(card.imageUrl, card.testingText, card.foil, card.incomplete, 16);
                
                // var display = new CardDisplay(card, $("#main").width() / 2, $("#main").width() / 2)
                // var cardDiv = display.baseDiv;

                $("#main").queue(
                    function (next) {
                        cardDiv.data("card", card);
                        $("#main").append(cardDiv);

                        var gameWidth = $("#main").width();
                        var gameHeight = $("#main").height();

                        var cardHeight = (gameHeight / 2);
                        var cardWidth = card.getWidthForHeight(cardHeight);
                        
                        that.handleCardAnimatedStart(cardDiv);

                        $(cardDiv).css(
                            {
                                position:"absolute",
                                left:(gameWidth / 2 - cardWidth / 4),
                                top:gameHeight * (3 / 8),
                                width:cardWidth / 2,
                                height:cardHeight / 2,
                                "z-index":100,
                                opacity:0});

                        $(cardDiv).animate(
                            {
                                left:"-=" + cardWidth / 4,
                                top:"-=" + (gameHeight / 8),
                                width:"+=" + (cardWidth / 2),
                                height:"+=" + (cardHeight / 2),
                                opacity:1},
                            {
                                duration:that.getAnimationLength(that.playEventDuration / 8),
                                easing:"linear",
                                queue:false,
                                complete:next});
                    }).queue(
                    function (next) {
                        setTimeout(next, that.getAnimationLength(that.playEventDuration * (5 / 8)));
                    }).queue(
                    function (next) {
                        $(cardDiv).animate(
                            {
                                opacity:0},
                            {
                                duration:that.getAnimationLength(that.playEventDuration / 4),
                                easing:"easeOutQuart",
                                queue:false,
                                complete:next});
                    }).queue(
                    function (next) {
                        $(cardDiv).remove();
                        next();
                    });
            }
        }
    },

    cardAffectsCard:function (element, animate) {
        if (animate) {
            var that = this;

            var participantId = element.getAttribute("participantId");
            var blueprintId = element.getAttribute("blueprintId");
            var targetCardIds = element.getAttribute("otherCardIds").split(",");
            var testingText = element.getAttribute("testingText");
            var backSideTestingText = element.getAttribute("backSideTestingText");

            // Play-out card affects card animation only if it's not the player who initiated it
            if (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId)) {
                $("#main").queue(
                    function (next) {
                        for (var i = 0; i < targetCardIds.length; i++) {
                            var targetCardId = targetCardIds[i];

                            var card = new Card(blueprintId, testingText, backSideTestingText, "ANIMATION", "anim" + i, participantId);
                            var cardDiv = Card.CreateSimpleCardDiv(card.imageUrl, card.testingText, card.foil, card.incomplete, 16);
                
                            // var display = new CardDisplay(card, $("#main").width() / 2, $("#main").width() / 2)
                            // var cardDiv = display.baseDiv;
                                                        
                            var targetCard = $(".card:cardId(" + targetCardId + ")");
                            if (targetCard.length > 0) {
                                cardDiv.data("card", card);
                                $("#main").append(cardDiv);

                                targetCard = targetCard[0];
                                var targetCardWidth = $(targetCard).width();
                                var targetCardHeight = $(targetCard).height();

                                var shadowStartPosX;
                                var shadowStartPosY;
                                var shadowWidth;
                                var shadowHeight;
                                if (card.horizontal != $(targetCard).data("card").horizontal) {
                                    shadowWidth = targetCardHeight;
                                    shadowHeight = targetCardWidth;
                                    shadowStartPosX = $(targetCard).position().left - (shadowWidth - targetCardWidth) / 2;
                                    shadowStartPosY = $(targetCard).position().top - (shadowHeight - targetCardHeight) / 2;
                                } else {
                                    shadowWidth = targetCardWidth;
                                    shadowHeight = targetCardHeight;
                                    shadowStartPosX = $(targetCard).position().left;
                                    shadowStartPosY = $(targetCard).position().top;
                                }
                                
                                that.handleCardAnimatedStart(cardDiv);

                                $(cardDiv).css(
                                    {
                                        position:"absolute",
                                        left:shadowStartPosX,
                                        top:shadowStartPosY,
                                        width:shadowWidth,
                                        height:shadowHeight,
                                        "z-index":100,
                                        opacity:1});
                                $(cardDiv).animate(
                                    {
                                        opacity:0,
                                        left:"-=" + (shadowWidth / 2),
                                        top:"-=" + (shadowHeight / 2),
                                        width:"+=" + shadowWidth,
                                        height:"+=" + shadowHeight},
                                    {
                                        duration:that.getAnimationLength(that.cardAffectsCardDuration),
                                        easing:"easeInQuart",
                                        queue:false,
                                        complete:null});
                            }
                        }

                        setTimeout(next, that.getAnimationLength(that.cardAffectsCardDuration));
                    }).queue(
                    function (next) {
                        $(".card").each(
                            function () {
                                var cardData = $(this).data("card");
                                if (cardData.zone == "ANIMATION") {
                                    $(this).remove();
                                } else {
                                    that.handleCardAnimatedEnd(this);
                                }
                            }
                        );
                        next();
                    });
            }
        }
    },

    putCardInPlay:function (element, animate) {
        var participantId = element.getAttribute("participantId");
        var cardId = element.getAttribute("cardId");
        var zone = element.getAttribute("zone");

        var that = this;
        $("#main").queue(
            function (next) {
                var blueprintId = element.getAttribute("blueprintId");
                var targetCardId = element.getAttribute("targetCardId");
                var controllerId = element.getAttribute("controllerId");
                var testingText = element.getAttribute("testingText");
                var backSideTestingText = element.getAttribute("backSideTestingText");

                if (controllerId != null)
                    participantId = controllerId;

                var card;
                if (zone == "ADVENTURE_PATH")
                    card = new Card(blueprintId, testingText, backSideTestingText, zone, cardId, participantId, element.getAttribute("index"));
                else
                    card = new Card(blueprintId, testingText, backSideTestingText, zone, cardId, participantId);

                var cardDiv = that.game.createCardDiv(card, null, card.isFoil(), card.hasErrata());
                if (zone == "DISCARD")
                    that.game.discardPileDialogs[participantId].append(cardDiv);
                else if (zone == "DEAD")
                    that.game.deadPileDialogs[participantId].append(cardDiv);
                else if (zone == "ADVENTURE_DECK")
                    that.game.adventureDeckDialogs[participantId].append(cardDiv);
                else if (zone == "REMOVED")
                    that.game.removedPileDialogs[participantId].append(cardDiv);
                else if (zone == "DECK") {
                    that.game.miscPileDialogs[participantId].append(cardDiv);
                    animate = false;
                }
                else
                    $("#main").append(cardDiv);

                if (targetCardId != null) {
                    var targetCardData = $(".card:cardId(" + targetCardId + ")").data("card");
                    if(targetCardData != null) {
                        targetCardData.attachedCards.push(cardDiv);    
                    }
                }

                next();
            });

        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutGroupWithCard(cardId);
                    next();
                });
        }

        if (animate && (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId))
            && zone != "DISCARD" && zone != "DEAD" && zone != "HAND" && zone != "ADVENTURE_DECK" && zone != "DECK") {
            var oldValues = {};

            $("#main").queue(
                function (next) {
                    var cardDiv = $(".card:cardId(" + cardId + ")");
                    var card = cardDiv.data("card");
                    var pos = cardDiv.position();

                    oldValues["zIndex"] = cardDiv.css("zIndex");
                    oldValues["left"] = pos.left;
                    oldValues["top"] = pos.top;
                    oldValues["width"] = cardDiv.width();
                    oldValues["height"] = cardDiv.height();

                    // Now we begin the animation
                    var gameWidth = $("#main").width();
                    var gameHeight = $("#main").height();

                    var cardHeight = (gameHeight / 2);
                    var cardWidth = card.getWidthForHeight(cardHeight);
                    
                    that.handleCardAnimatedStart(cardDiv);

                    $(cardDiv).css(
                        {
                            position:"absolute",
                            left:(gameWidth / 2 - cardWidth / 4),
                            top:gameHeight * (3 / 8),
                            width:cardWidth / 2,
                            height:cardHeight / 2,
                            "z-index":100,
                            opacity:0});

                    $(cardDiv).animate(
                        {
                            opacity:1
                        },
                        {
                            duration:that.getAnimationLength(that.putCardIntoPlayDuration / 8),
                            easing:"linear",
                            step:function (now, fx) {
                                layoutCardElem(cardDiv,
                                    (gameWidth / 2 - cardWidth / 4) - now * (cardWidth / 4),
                                    gameHeight * (3 / 8) - now * (gameHeight / 8),
                                    cardWidth / 2 + now * (cardWidth / 2),
                                    cardHeight / 2 + now * (cardHeight / 2), 100);
                            },
                            complete:next
                        });
                }).queue(
                function (next) {
                    setTimeout(next, that.getAnimationLength(that.putCardIntoPlayDuration * (5 / 8)));
                }).queue(
                function (next) {
                    var cardDiv = $(".card:cardId(" + cardId + ")");
                    var pos = cardDiv.position();

                    var startLeft = pos.left;
                    var startTop = pos.top;
                    var startWidth = cardDiv.width();
                    var startHeight = cardDiv.height();

                    $(cardDiv).animate(
                        {
                            left:oldValues["left"]},
                        {
                            duration:that.getAnimationLength(that.putCardIntoPlayDuration / 4),
                            easing:"linear",
                            step:function (now, fx) {
                                var state = fx.state;
                                layoutCardElem(cardDiv,
                                    startLeft + (oldValues["left"] - startLeft) * state,
                                    startTop + (oldValues["top"] - startTop) * state,
                                    startWidth + (oldValues["width"] - startWidth) * state,
                                    startHeight + (oldValues["height"] - startHeight) * state, 100);
                            },
                            complete:next});
                }).queue(
                function (next) {
                    var cardDiv = $(".card:cardId(" + cardId + ")");
                    $(cardDiv).css({zIndex:oldValues["zIndex"]});
                    that.handleCardAnimatedEnd(cardDiv);
                    next();
                });
        }
    },

    moveCardInPlay:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");
                var zone = element.getAttribute("zone");
                var targetCardId = element.getAttribute("targetCardId");
                var participantId = element.getAttribute("participantId");
                var controllerId = element.getAttribute("controllerId");

                if (controllerId != null)
                    participantId = controllerId;

                // Remove from where it was already attached
                $(".card").each(
                    function () {
                        var cardData = $(this).data("card");
                        var index = -1;
                        for (var i = 0; i < cardData.attachedCards.length; i++)
                            if (cardData.attachedCards[i].data("card").cardId == cardId) {
                                index = i;
                                break;
                            }
                        if (index != -1)
                            cardData.attachedCards.splice(index, 1);
                    }
                );

                var card = $(".card:cardId(" + cardId + ")");
                var cardData = card.data("card");
                // move to new zone
                cardData.zone = zone;
                cardData.owner = participantId;

                if (targetCardId != null) {
                    // attach to new card if it's attached
                    var targetCardData = $(".card:cardId(" + targetCardId + ")").data("card");
                    targetCardData.attachedCards.push(card);
                }

                next();
            });

        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    removeCardFromPlay:function (element, animate) {
        var that = this;
        var cardRemovedIds = element.getAttribute("otherCardIds").split(",");
        var participantId = element.getAttribute("participantId");

        if (animate && (this.game.spectatorMode || this.game.replayMode || (participantId != this.game.bottomPlayerId))) {
            $("#main").queue(
                function (next) {
                    const cardDiv = $(".card:cardId(" + cardRemovedIds + ")");
                    that.handleCardAnimatedStart(cardDiv);
                    cardDiv
                        .animate(
                        {
                            opacity:0},
                        {
                            duration:that.getAnimationLength(that.removeCardFromPlayDuration),
                            easing:"easeOutQuart",
                            queue:false});
                    setTimeout(next, that.getAnimationLength(that.removeCardFromPlayDuration));
                });
        }
        $("#main").queue(
            function (next) {
                for (var i = 0; i < cardRemovedIds.length; i++) {
                    var cardId = cardRemovedIds[i];
                    var card = $(".card:cardId(" + cardId + ")");

                    if (card.length > 0) {
                        var cardData = card.data("card");
                        
                        if (cardData.zone == "ATTACHED" || cardData.zone == "STACKED") {
                            $(".card").each(
                                function () {
                                    var cardData = $(this).data("card");
                                    if(!cardData || !cardData.attachedCards)
                                        return;
                                    var index = -1;
                                    for (var i = 0; i < cardData.attachedCards.length; i++)
                                        if (cardData.attachedCards[i].data("card").cardId == cardId) {
                                            index = i;
                                            break;
                                        }
                                    if (index != -1)
                                        cardData.attachedCards.splice(index, 1);
                                }
                            );
                        }

                        card.remove();
                    }
                }

                next();
            });

        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    gamePhaseChange:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var phase = element.getAttribute("phase");

                $(".phase").text(phase);

                next();
            });
    },

    twilightPool:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var count = element.getAttribute("count");

                $(".twilightPool").html("" + count);

                next();
            });
    },

    turnChange:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var inactiveCardIds = element.getAttribute("otherCardIds");
                var playerId = element.getAttribute("participantId");
                var playerIndex = that.game.getPlayerIndex(playerId);

                that.game.currentPlayerId = playerId;

                $(".player").each(function (index) {
                    if (index == playerIndex)
                        $(this).addClass("current");
                    else
                        $(this).removeClass("current");
                });
                that.game.advPathGroup.setCurrentPlayerIndex(playerIndex);
                
                if(inactiveCardIds) {
                    var ids = inactiveCardIds.split(",");
                    
                    $(".card").each(function () {
                        var cardData = $(this).data("card");
                        if ($.inArray(cardData.cardId, ids) > -1) {
                            $(this).addClass("inactive");
                        }
                        else {
                            $(this).removeClass("inactive");
                        }
                    });
                }

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.advPathGroup.layoutCards();
                    next();
                });
        }
    },

    addAssignment:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");
                var opposingCardIds = element.getAttribute("otherCardIds").split(",");

                for (var i = 0; i < opposingCardIds.length; i++) {
                    if ($(".card:cardId(" + opposingCardIds[i] + ")").data("card").assign != cardId)
                        that.game.assignMinion(opposingCardIds[i], cardId);
                }

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    removeAssignment:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");

                $(".card").each(function () {
                    var cardData = $(this).data("card");
                    if (cardData.assign == cardId)
                        that.game.unassignMinion(cardData.cardId);
                });

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    startSkirmish:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");
                var opposingCardIds = element.getAttribute("otherCardIds");

                if (opposingCardIds != null && opposingCardIds != "")
                    $(".card:cardId(" + opposingCardIds + ")").each(function () {
                        $(this).data("card").skirmish = true;
                    });

                if (cardId != null)
                    $(".card:cardId(" + cardId + ")").each(function () {
                        $(this).data("card").skirmish = true;
                    });

                that.game.fpStrengthDiv = $("<div class='fpStrength'></div>");
                that.game.fpDamageBonusDiv = $("<div class='fpDamageBonus'></div>");
                that.game.shadowStrengthDiv = $("<div class='shadowStrength'></div>");
                that.game.shadowDamageBonusDiv = $("<div class='shadowDamageBonus'></div>");

                that.game.skirmishGroupDiv = $("<div class='ui-widget-content skirmish'></div>");
                that.game.skirmishGroupDiv.css({"border-radius":"7px", "border-color":"#ff0000"});
                that.game.skirmishGroupDiv.append(that.game.fpStrengthDiv);
                that.game.skirmishGroupDiv.append(that.game.fpDamageBonusDiv);
                that.game.skirmishGroupDiv.append(that.game.shadowStrengthDiv);
                that.game.skirmishGroupDiv.append(that.game.shadowDamageBonusDiv);
                $("#main").append(that.game.skirmishGroupDiv);

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    addToSkirmish:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");

                $(".card:cardId(" + cardId + ")").each(function () {
                    $(this).data("card").skirmish = true;
                });

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    removeFromSkirmish:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var cardId = element.getAttribute("cardId");

                $(".card:cardId(" + cardId + ")").each(function () {
                    var cardData = $(this).data("card");
                    delete cardData.skirmish;
                });

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    endSkirmish:function (animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                that.game.skirmishGroupDiv.remove();
                that.game.skirmishGroupDiv = null;
                that.game.fpStrengthDiv = null;
                that.game.shadowStrengthDiv = null;

                $(".card").each(function () {
                    var cardData = $(this).data("card");
                    if (cardData && cardData.skirmish == true) {
                        delete cardData.skirmish;
                    }
                });

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.layoutUI(false);
                    next();
                });
        }
    },

    addTokens:function (element, animate) {
        var cardId = element.getAttribute("cardId");
        var that = this;
        $("#main").queue(
            function (next) {
                var zone = element.getAttribute("zone");
                var token = element.getAttribute("token");
                var count = parseInt(element.getAttribute("count"));

                var cardData = $(".card:cardId(" + cardId + ")").data("card");
                if (cardData.tokens == null)
                    cardData.tokens = {};
                if (cardData.tokens[token] == null)
                    cardData.tokens[token] = 0;
                cardData.tokens[token] += count;

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    layoutTokens($(".card:cardId(" + cardId + ")"));
                    next();
                });
        }
    },

    removeTokens:function (element, animate) {
        var cardId = element.getAttribute("cardId");
        var that = this;
        $("#main").queue(
            function (next) {
                var zone = element.getAttribute("zone");
                var token = element.getAttribute("token");
                var count = parseInt(element.getAttribute("count"));

                var cardData = $(".card:cardId(" + cardId + ")").data("card");
                if (cardData.tokens == null)
                    cardData.tokens = {};
                if (cardData.tokens[token] == null)
                    cardData.tokens[token] = 0;
                cardData.tokens[token] -= count;

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    layoutTokens($(".card:cardId(" + cardId + ")"));
                    next();
                });
        }
    },

    playerPosition:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var participantId = element.getAttribute("participantId");
                var position = element.getAttribute("index");

                if (that.game.playerPositions == null)
                    that.game.playerPositions = new Array();

                var index = that.game.getPlayerIndex(participantId);
                that.game.playerPositions[index] = position;

                that.game.advPathGroup.setPositions(that.game.playerPositions);

                next();
            });
        if (animate) {
            $("#main").queue(
                function (next) {
                    that.game.advPathGroup.layoutCards();
                    next();
                });
        }
    },

    gameStats:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var charStats = element.getAttribute("charStats");
                if (charStats != null) {
                    var charStatsArr = charStats.split(",");
                    for (var i = 0; i < charStatsArr.length; i++) {
                        var cardStats = charStatsArr[i].split("=");
                        var cardDiv = $(".card:cardId(" + cardStats[0] + ")");
                        that.game.ensureCardHasBoxes(cardDiv);
                        var cardStatArr = cardStats[1].split("|");
                        $(".cardStrength", cardDiv).html(cardStatArr[0]);
                        $(".cardVitality", cardDiv).html(cardStatArr[1]);
                        if (cardStatArr.length > 2) {
                            if (cardStatArr[2].indexOf("R") == 0) {
                                var resistanceDiv = $(".cardResistance", cardDiv);
                                var resistance = cardStatArr[2].substring(1);
                                if (resistance.indexOf("A") == 0) {
                                    resistanceDiv.addClass("aragorn");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("F") == 0) {
                                    resistanceDiv.addClass("frodo");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("G") == 0) {
                                    resistanceDiv.addClass("gandalf");
                                    resistance = resistance.substring(1);
                                } else if (resistance.indexOf("T") == 0) {
                                    resistanceDiv.addClass("theoden");
                                    resistance = resistance.substring(1);
                                } else {
                                    $(".cardResistanceBg", cardDiv).css({display:""});
                                }
                                resistanceDiv.html(resistance).css({display:""});
                            } else {
                                $(".cardSiteNumber", cardDiv).html(cardStatArr[2]).css({display:""});
                                $(".cardSiteNumberBg", cardDiv).css({display:""});
                            }
                        }
                    }
                }

                var fellowshipArchery = element.getAttribute("fellowshipArchery");
                var shadowArchery = element.getAttribute("shadowArchery");
                var moveCount = element.getAttribute("moveCount");
                var moveLimit = element.getAttribute("moveLimit");

                if (fellowshipArchery == "null")
                    $(".fpArchery").html("");
                else if (fellowshipArchery != null)
                    $(".fpArchery").html(fellowshipArchery);
                if (shadowArchery == "null")
                    $(".shadowArchery").html("");
                else if (shadowArchery != null)
                    $(".shadowArchery").html(shadowArchery);

                $(".move").html(moveCount + "/" + moveLimit);
                
                var initiative = element.getAttribute("initiative");
                var ruleof4 = element.getAttribute("ruleof4");
                
                if(initiative == "FREE_PEOPLE") {
                    $("#initiative-player").html("Free Peoples");
                }
                else {
                    $("#initiative-player").html("<b>Shadow</b>");
                }
                
                if(ruleof4 == null || ruleof4 == "-1") {
                    $("#ruleof4").hide();
                }
                else {
                    $("#ruleof4").show();
                    $("#ruleof4-count").html(ruleof4);
                    if(ruleof4 >= "4") {
                        $("#ruleof4-status").html(" cards.<br/><b>(Rule of 4 limit reached!)</b>");
                    }
                    else if(ruleof4 == "1"){
                        $("#ruleof4-status").html(" card");
                    }
                    else {
                        $("#ruleof4-status").html(" card");
                    }
                }
                

                var playerZones = element.getElementsByTagName("playerZones");
                for (var i = 0; i < playerZones.length; i++) {
                    var playerZone = playerZones[i];

                    var playerId = playerZone.getAttribute("name");
                    var hand = playerZone.getAttribute("HAND");
                    var discard = playerZone.getAttribute("DISCARD");
                    var adventureDeck = playerZone.getAttribute("ADVENTURE_DECK");
                    var dead = playerZone.getAttribute("DEAD");
                    var deck = playerZone.getAttribute("DECK");
                    var removed = playerZone.getAttribute("REMOVED");

                    $("#deck" + that.game.getPlayerIndex(playerId)).text(deck);
                    $("#hand" + that.game.getPlayerIndex(playerId)).text(hand);
                    $("#discard" + that.game.getPlayerIndex(playerId)).text(discard);
                    $("#deadPile" + that.game.getPlayerIndex(playerId)).text(dead);
                    $("#adventureDeck" + that.game.getPlayerIndex(playerId)).text(adventureDeck);
                    $("#removedPile" + that.game.getPlayerIndex(playerId)).text(removed);
                }

                var playerThreats = element.getElementsByTagName("threats")
                for (var i = 0; i < playerThreats.length; i++) {
                    var playerThreat = playerThreats[i];

                    var playerId = playerThreat.getAttribute("name");
                    var value = playerThreat.getAttribute("value");
                    $("#threats" + that.game.getPlayerIndex(playerId)).text(value);
                }

                if (that.game.fpStrengthDiv != null) {
                    that.game.fpStrengthDiv.text(element.getAttribute("fellowshipStrength"));
                    var fpOverwhelmed = element.getAttribute("fpOverwhelmed");
                    if (fpOverwhelmed != null) {
                        if (fpOverwhelmed == "true") {
                            that.game.fpStrengthDiv.addClass("overwhelmed");
                        } else {
                            that.game.fpStrengthDiv.removeClass("overwhelmed");
                        }
                    }

                    var damageBonus = element.getAttribute("fellowshipDamageBonus");
                    if (damageBonus != null) {
                        that.game.fpDamageBonusDiv.text("+" + damageBonus);
                        if (damageBonus == 0)
                            that.game.fpDamageBonusDiv.css({visibility:"hidden"});
                        else
                            that.game.fpDamageBonusDiv.css({visibility:"visible"});
                    }
                }
                if (that.game.shadowStrengthDiv != null) {
                    that.game.shadowStrengthDiv.text(element.getAttribute("shadowStrength"));

                    var damageBonus = element.getAttribute("shadowDamageBonus");
                    if (damageBonus != null) {
                        that.game.shadowDamageBonusDiv.text("+" + damageBonus);
                        if (damageBonus == 0)
                            that.game.shadowDamageBonusDiv.css({visibility:"hidden"});
                        else
                            that.game.shadowDamageBonusDiv.css({visibility:"visible"});
                    }
                }

                next();
            });
    },

    message:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var message = element.getAttribute("message");
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "gameMessage");

                next();
            });
    },

    warning:function (element, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                var message = element.getAttribute("message");
                if (that.game.chatBox != null)
                    that.game.chatBox.appendMessage(message, "warningMessage");

                next();
            });
    },

    processDecision:function (decision, animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                if(!that.game.replayMode) {
                    that.game.countdownIntervalId = window.setInterval(function(){
                        that.game.totalTime -= 1;
                        that.game.decisionTime += 1;
                        
                        if(that.game.allPlayerIds == null)
                            return;
                        
                        var index = that.game.getPlayerIndex(that.game.currentPlayerId);
                        $("#clock-1").text(that.game.parseTime(that.game.decisionTime));
                        $("#clock" + index).text(that.game.parseTime(that.game.totalTime));
                    }, 1000);
                }
                
                var decisionType = decision.getAttribute("decisionType");
                if (decisionType == "INTEGER") {
                    that.game.integerDecision(decision);
                } else if (decisionType == "MULTIPLE_CHOICE") {
                    that.game.multipleChoiceDecision(decision);
                } else if (decisionType == "ARBITRARY_CARDS") {
                    that.game.arbitraryCardsDecision(decision);
                } else if (decisionType == "ACTION_CHOICE") {
                    that.game.actionChoiceDecision(decision);
                } else if (decisionType == "CARD_ACTION_CHOICE") {
                    that.game.cardActionChoiceDecision(decision);
                } else if (decisionType == "CARD_SELECTION") {
                    that.game.cardSelectionDecision(decision);
                } else if (decisionType == "ASSIGN_MINIONS") {
                    that.game.assignMinionsDecision(decision);
                }
                
                if (!animate)
                    that.game.layoutUI(false);

                next();
            });
        if (that.game.replayMode) {
            $("#main").queue(
                function (next) {
                    setTimeout(next, that.getAnimationLength(that.decisionDuration));
                });
        }
    },

    updateGameState:function (animate) {
        var that = this;
        $("#main").queue(
            function (next) {
                setTimeout(
                    function () {
                        that.game.updateGameState();
                    }, 100);

                if (!animate)
                    that.game.layoutUI(false);

                next();
            });
    },

    windowResized:function () {
        var that = this;
        $("#main").queue(
            function (next) {
                that.game.layoutUI(true);
                next();
            });
    },

    handleCardAnimatedStart: function (cardDiv) {
        cardDiv && cardDiv[0] && $(cardDiv[0]).addClass('card-animating')
    },

    handleCardAnimatedEnd: function (cardDiv) {
        cardDiv && cardDiv[0] && $(cardDiv[0]).removeClass('card-animating')
    }
});
