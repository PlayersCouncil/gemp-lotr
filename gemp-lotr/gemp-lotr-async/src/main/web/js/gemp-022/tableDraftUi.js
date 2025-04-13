var GempLotrTableDraftUI = Class.extend({
    comm:null,

    topDiv:null,
    bottomDiv:null,

    messageDiv:null,
    picksDiv:null,
    draftedDiv:null,

    picksCardGroup:null,
    draftedCardGroup:null,

    eventId:null,

    refreshInterval:null,

    chatBoxDiv: null,
    chatBox: null,

    init:function () {
        var that = this;

        this.comm = new GempLotrCommunication("/gemp-lotr-server", that.processError.bind(that));

        this.eventId = getUrlParam("eventId");

        this.topDiv = $("#topDiv");
        this.bottomDiv = $("#bottomDiv");

        this.messageDiv = $("#messageDiv");
        this.tableStatusDiv = $("#tableStatusDiv");
        this.picksDiv = $("#picksDiv");
        this.draftedDiv = $("#draftedDiv");
        this.sideDiv = $("#sideDiv");

        this.picksCardGroup = new NormalCardGroup(this.picksDiv, function (card) {
            return true;
        });
        this.picksCardGroup.maxCardHeight = 200;

        this.draftedCardGroup = new NormalCardGroup(this.draftedDiv, function (card) {
            return true;
        });
        this.draftedCardGroup.maxCardHeight = 200;

        this.selectionFunc = this.addCardToDeckAndLayout;

        this.addBottomLeftTabPane();

        $("body").click(
                function (event) {
                    return that.clickCardFunction(event);
                });
        $("body").mousedown(
                function (event) {
                    return that.dragStartCardFunction(event);
                });
        $("body").mouseup(
                function (event) {
                    return that.dragStopCardFunction(event);
                });
        $("body")[0].addEventListener("contextmenu",
            function (event) {
                if(!that.clickCardFunction(event))
                {
                    event.preventDefault();
                    return false;
                }
                return true;
            });

        var width = $(window).width();
        var height = $(window).height();

        this.infoDialog = $("<div></div>")
                .dialog({
            autoOpen:false,
            closeOnEscape:true,
            resizable:false,
            title:"Card information"
        });

        var swipeOptions = {
            threshold:20,
            swipeUp:function (event) {
                that.infoDialog.prop({ scrollTop:that.infoDialog.prop("scrollHeight") });
                return false;
            },
            swipeDown:function (event) {
                that.infoDialog.prop({ scrollTop:0 });
                return false;
            }
        };
        this.infoDialog.swipe(swipeOptions);

        this.refreshInterval = setInterval(function () {
            that.getDraftState();
        }, 500);
        this.getDraftState();

        this.chatBox.beginGameChat();
    },

    addBottomLeftTabPane: function () {
        var that = this;
        var tabsLabels = "<li><a href='#chatBox' class='slimTab'>Chat</a></li><li><a href='#playersInRoomBox' class='slimTab'>Players</a></li>";
        var tabsBodies = "<div id='chatBox' class='slimPanel'></div><div id='playersInRoomBox' class='slimPanel'></div>";

        var tabsStr = "<div id='bottomLeftTabs'><ul>" + tabsLabels + "</ul>" + tabsBodies + "</div>";

        this.tabPane = $(tabsStr).tabs();

        $("#sideDiv").append(this.tabPane);

        this.chatBoxDiv = $("#chatBox");

        var playerListener = function (players) {
            var val = "";
            for (var i = 0; i < players.length; i++)
                val += players[i] + "<br/>";
            $("a[href='#playersInRoomBox']").html("Players(" + players.length + ")");
            $("#playersInRoomBox").html(val);
        };

        var displayChatListener = function(title, message) {

            var dialog = $("<div></div>").dialog({
                title: title,
                resizable: true,
                height: 200,
                modal: true,
                buttons: {}
            }).html(message);
        }

        var chatRoomName = ("Draft-" + getUrlParam("eventId"));
        this.chatBox = new ChatBoxUI(chatRoomName, $("#chatBox"), this.comm.url, false, playerListener, false, displayChatListener);
        this.chatBox.chatUpdateInterval = 3000;

    },

    processDraftStatus:function (xml, callUpdate) {
        var that = this;
        var root = xml.documentElement;
        if (root.tagName == "draftStatus") {
            var pickedCards = root.getElementsByTagName("pickedCard");
            var availablePicks = root.getElementsByTagName("availablePick");
            var timeRemainingElements = root.getElementsByTagName("timeRemaining");
            var roundsInfoElements = root.getElementsByTagName("roundsInfo");
            var playerElements = root.getElementsByTagName("player");
            var pickOrderAscendingElements = root.getElementsByTagName("pickOrderAscending");

            // Get time remaining
            var timeRemaining = null; // Default to null
            if (timeRemainingElements.length > 0) {
                timeRemaining = timeRemainingElements[0].getAttribute("value");
            }

            // Get round info
            var currentRound = null; // Default to null
            var roundsTotal = null; // Default to null
            if (roundsInfoElements.length > 0) {
                currentRound = roundsInfoElements[0].getAttribute("currentRound");
                roundsTotal = roundsInfoElements[0].getAttribute("roundsTotal");
            }


            // Check if picked cards changed and we should update
            let pickedCardsChanged = false;
            let newDraftedIds = new Map();

            // Count occurrences of picked cards
            for (var i = 0; i < pickedCards.length; i++) {
                let blueprintId = pickedCards[i].getAttribute("blueprintId");
                newDraftedIds.set(blueprintId, pickedCards[i].getAttribute("count"));
            }

            // Count occurrences of existing drafted cards
            let existingDraftedIds = new Map();
            $(".card", that.draftedDiv).each(function () {
                let blueprintId = $(this).data("blueprintId");
                existingDraftedIds.set(blueprintId, (existingDraftedIds.get(blueprintId) || 0) + 1);
            });

            // Compare the two maps
            if (newDraftedIds.size !== existingDraftedIds.size) {
                pickedCardsChanged = true;
            } else {
                for (let [id, count] of newDraftedIds) {
                    if (existingDraftedIds.get(id) != count) {
                        pickedCardsChanged = true;
                        break;
                    }
                }
            }

            // Check if we can append just one card
            let singleNewCard = null;
            let totalDifference = 0;

            for (let [id, newCount] of newDraftedIds) {
                let existingCount = existingDraftedIds.get(id) || 0;
                let difference = newCount - existingCount;

                if (difference > 0) {
                    singleNewCard = singleNewCard === null ? id : null; // Ensure only one new card
                    totalDifference += difference;
                }
            }

            // There is exactly one new card if totalDifference is 1 and singleNewCard is set
            let isSingleNewCard = totalDifference === 1 && singleNewCard !== null;

            if (isSingleNewCard) {
                var card = new Card(singleNewCard, null, null, "drafted", "deck", "player");
                var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                cardDiv.data("card", card);
                cardDiv.data("blueprintId", singleNewCard);
                that.draftedDiv.append(cardDiv);
                that.draftedCardGroup.layoutCards();
            } else if (pickedCardsChanged) {
                $(".card", that.draftedDiv).remove();
                for (var i = 0; i < pickedCards.length; i++) {
                    var pickedCard = pickedCards[i];
                    var blueprintId = pickedCard.getAttribute("blueprintId");
                    var count = pickedCard.getAttribute("count");
                    for (var no = 0; no < count; no++) {
                        var card = new Card(blueprintId, null, null, "drafted", "deck", "player");
                        var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                        cardDiv.data("card", card);
                        cardDiv.data("blueprintId", blueprintId);
                        that.draftedDiv.append(cardDiv);
                    }
                }
                that.draftedCardGroup.layoutCards();
            }

            // Check if cards to pick from changed and we should update
            let availablePicksChanged = false;
            let newPickIds = new Map();

            // Count occurrences of available picks
            for (var i = 0; i < availablePicks.length; i++) {
                let id = availablePicks[i].getAttribute("id");
                newPickIds.set(id, (newPickIds.get(id) || 0) + 1);
            }

            // Count occurrences of existing picks
            let existingPickIds = new Map();
            $(".card", that.picksDiv).each(function () {
                let id = $(this).data("choiceId");
                existingPickIds.set(id, (existingPickIds.get(id) || 0) + 1);
            });

            // Compare the two maps
            if (newPickIds.size !== existingPickIds.size) {
                availablePicksChanged = true;
            } else {
                for (let [id, count] of newPickIds) {
                    if (existingPickIds.get(id) !== count) {
                        availablePicksChanged = true;
                        break;
                    }
                }
            }

            // Check if pre picked card changed and we should update (highlight)
            let chosenCardChanged = false;
            let newChosenIds = new Set();
            for (var i = 0; i < availablePicks.length; i++) {
                let chosen = availablePicks[i].getAttribute("chosen")
                if (chosen === "true") {
                    newChosenIds.add(availablePicks[i].getAttribute("id"))
                }
            }
            let existingChosenIds = new Set();
            $(".card", that.picksDiv).each(function () {
                if ($(this).hasClass("draft-highlight")) {
                    existingChosenIds.add($(this).data("choiceId")); // Store the chosen card's choiceId
                }
            });
            if (existingChosenIds.size !== newChosenIds.size || [...existingChosenIds].some(id => !newChosenIds.has(id))) {
                chosenCardChanged = true;
            }

            if (availablePicksChanged) {
                $(".card", that.picksDiv).remove();
                for (var i = 0; i < availablePicks.length; i++) {
                    var availablePick = availablePicks[i];
                    var id = availablePick.getAttribute("id");
                    var url = availablePick.getAttribute("url");
                    var blueprintId = availablePick.getAttribute("blueprintId");
                    var chosen = availablePick.getAttribute("chosen"); // Declared card for pick

                    var card;
                    var cardDiv;

                    if (blueprintId != null) {
                        card = new Card(blueprintId, null, null, "picks", "deck", "player");
                        cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                        cardDiv.data("card", card);
                        cardDiv.data("choiceId", id);
                    } else {
                        card = new Card("rules", null, null, "picks", "deck", "player");
                        cardDiv = Card.CreateCardDiv(url, null, null, false, false, true, false, false);
                        cardDiv.data("card", card);
                        cardDiv.data("choiceId", id);
                    }

                    // Check if the card is chosen and apply the highlight class
                    if (chosen === "true") {
                        cardDiv.get(0).classList.add("draft-highlight"); // Add highlight CSS class
                    }

                    that.picksDiv.append(cardDiv);
                }
                that.picksCardGroup.layoutCards();
            } else if (chosenCardChanged) {
                $(".card", that.picksDiv).removeClass("draft-highlight");
                for (var i = 0; i < availablePicks.length; i++) {
                    let chosen = availablePicks[i].getAttribute("chosen");
                    if (chosen === "true") {
                        $(".card", that.picksDiv).eq(i).addClass("draft-highlight");
                    }
                }
            }

            // Read player statuses
            let players = [];
            for (let i = 0; i < playerElements.length; i++) {
                let name = playerElements[i].getAttribute("name");
                let hasChosen = playerElements[i].getAttribute("hasChosen") === "true";
                players.push({ name, hasChosen });
            }

            // Read pick order direction
            let pickOrderAscending = pickOrderAscendingElements.length == 0 ||
                                     pickOrderAscendingElements[0].getAttribute("value") === "true";

            // Clear previous table status
            that.tableStatusDiv.empty();

            // Generate player order display
            let separator = pickOrderAscending ? " > " : " < ";
            let tableStatusText = players.map(player => {
                let nameSpan = $("<span>").text(player.name);
                if (player.hasChosen) {
                    nameSpan.addClass("player-chosen"); // Add class to indicate a chosen card
                }
                return nameSpan.prop("outerHTML");
            }).join(separator);

            // Append to the table status div
            that.tableStatusDiv.html(tableStatusText);

            // Function to format time in m:ss
            function formatTime(seconds) {
                let minutes = Math.floor(seconds / 60);
                let secs = seconds % 60;
                return minutes + ":" + (secs < 10 ? "0" : "") + secs; // Adds leading zero if needed
            }

            if (newChosenIds.size != 0) {
                let message = "Waiting for others to pick a card";
                if (currentRound !== null && roundsTotal !== null) {
                    message += " - Pack " + currentRound + "/" + roundsTotal;
                }
                if (timeRemaining !== null) {
                    message += " (" + formatTime(timeRemaining) + ")";
                }
                that.messageDiv.text(message);
            } else if (availablePicks.length > 0) {
                let message = "Make a pick";
                if (currentRound !== null && roundsTotal !== null) {
                    message += " - Pack " + currentRound + "/" + roundsTotal;
                }
                if (timeRemaining !== null) {
                    message += " (" + formatTime(timeRemaining) + ")";
                }
                that.messageDiv.text(message);
            } else {
                that.messageDiv.text("Draft is finished");
                clearInterval(that.refreshInterval);
            }
        }
    },

    getDraftState:function () {
        var that = this;
        this.comm.getTableDraft(this.eventId,
            function (xml) {
                that.processDraftStatus(xml, true);
            });
    },

    clickCardFunction:function (event) {
        var that = this;

        var tar = $(event.target);
        if (tar.length == 1 && tar[0].tagName == "A")
            return true;

        if (!this.successfulDrag && this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            var card = selectedCardElem.data("card");
            if (event.which >= 1) {
                if (!this.successfulDrag) {
                    if (event.shiftKey || event.which > 1) {
                        this.displayCardInfo(selectedCardElem.data("card"));
                        return false;
                    } else {
                        if (selectedCardElem.data("card").zone == "picks") {
                            var choiceId = selectedCardElem.data("choiceId");
                            that.comm.makeTableDraftPick(that.eventId, choiceId, function (xml) {
                                that.processDraftStatus(xml, false)
                            });
                        }
                    }

                    event.stopPropagation();
                }
            }
            return false;
        }
        return true;
    },

    dragCardData:null,
    dragStartX:null,
    dragStartY:null,
    successfulDrag:null,

    dragStartCardFunction:function (event) {
        this.successfulDrag = false;
        var tar = $(event.target);
        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which == 1) {
                this.dragCardData = selectedCardElem.data("card");
                this.dragStartX = event.clientX;
                this.dragStartY = event.clientY;
                return false;
            }
        }
        return true;
    },

    dragStopCardFunction:function (event) {
        if (this.dragCardData != null) {
            if (this.dragStartY - event.clientY >= 20) {
                this.displayCardInfo(this.dragCardData);
                this.successfulDrag = true;
            }
            this.dragCardData = null;
            this.dragStartX = null;
            this.dragStartY = null;
            return false;
        }
        return true;
    },

    displayCardInfo:function (card) {
        this.infoDialog.html("");
        this.infoDialog.html("<div style='scroll: auto'></div>");
        this.infoDialog.append(Card.CreateFullCardDiv(card.imageUrl, null, card.foil, card.horizontal, card.isPack()));
        if (card.hasWikiInfo())
            this.infoDialog.append("<div><a href='" + card.getWikiLink() + "' target='_blank'>Wiki</a></div>");
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var horSpace = 30;
        var vertSpace = 45;

        if (card.horizontal) {
            // 500x360
            this.infoDialog.dialog({width:Math.min(500 + horSpace, windowWidth), height:Math.min(380 + vertSpace, windowHeight)});
        } else {
            // 360x500
            this.infoDialog.dialog({width:Math.min(360 + horSpace, windowWidth), height:Math.min(520 + vertSpace, windowHeight)});
        }
        this.infoDialog.dialog("open");
    },

    layoutUI:function (layoutDivs) {
        if (layoutDivs) {
            var messageHeight = 40;
            var statusHeight = 20;
            var padding = 5;
            var chatHeight = 200;
            var chatWidth = 200;

            var topWidth = this.topDiv.width();
            var topHeight = this.topDiv.height();

            var bottomWidth = this.bottomDiv.width();
            var bottomHeight = this.bottomDiv.height();

            this.tableStatusDiv.css({
                position: "absolute",
                top: messageHeight + padding,
                left: padding,
                width: topWidth - padding * 2,
                height: topHeight - messageHeight - statusHeight - padding * 2,
            });

            this.picksDiv.css({position:"absolute", left:padding, top:messageHeight+statusHeight+padding*2, width:topWidth-padding*2, height:topHeight-messageHeight-statusHeight-padding*2});
            this.picksCardGroup.setBounds(0, 0, topWidth-padding*2, topHeight-messageHeight-statusHeight-padding*2);

            this.draftedDiv.css({position:"absolute", left:padding*2+chatWidth, top:padding, width:bottomWidth-padding*2-chatWidth, height:bottomHeight-padding*2});
            this.draftedCardGroup.setBounds(0, 0, bottomWidth-padding*2-chatWidth, bottomHeight-padding*2);

            this.sideDiv.css({position:"absolute", left:padding, top:padding, width:chatWidth, height:bottomHeight-padding*2});

            this.tabPane.css({
                position: "absolute",
                left: 0,
                top: bottomHeight - chatHeight - padding,
                width: chatWidth - padding,
                height: chatHeight - padding
            });
            this.chatBox.setBounds(4, 4 + 25, chatWidth - 8, chatHeight - 8 - 25);
        } else {
            this.picksCardGroup.layoutCards();
            this.draftedCardGroup.layoutCards();
        }
    },

    processError:function (xhr, ajaxOptions, thrownError) {
        if (thrownError != "abort") {
            clearInterval(this.refreshInterval);
            this.chatBox.appendMessage("There was a problem communicating with the server, if the draft is finished, table has been removed, otherwise you have lost connection to the server.", "warningMessage");
            this.chatBox.appendMessage("Refresh the page (press F5) to resume the draft, or press back on your browser to get back to the Game Hall.", "warningMessage");
        }
    }
});
