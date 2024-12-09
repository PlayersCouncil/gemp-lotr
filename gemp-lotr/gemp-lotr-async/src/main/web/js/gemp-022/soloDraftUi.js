var GempLotrSoloDraftUI = Class.extend({
    comm:null,

    topDiv:null,
    bottomDiv:null,

    messageDiv:null,
    picksDiv:null,
    draftedDiv:null,

    picksCardGroup:null,
    draftedCardGroup:null,

    leagueType:null,

    init:function () {
        var that = this;

        this.comm = new GempLotrCommunication("/gemp-lotr-server", that.processError);

        this.leagueType = getUrlParam("leagueType");

        this.topDiv = $("#topDiv");
        this.bottomDiv = $("#bottomDiv");

        this.messageDiv = $("#messageDiv");
        this.picksDiv = $("#picksDiv");
        this.draftedDiv = $("#draftedDiv");

        this.picksCardGroup = new NormalCardGroup(this.picksDiv, function (card) {
            return true;
        });
        this.picksCardGroup.maxCardHeight = 200;

        this.draftedCardGroup = new NormalCardGroup(this.draftedDiv, function (card) {
            return true;
        });
        this.draftedCardGroup.maxCardHeight = 200;

        this.selectionFunc = this.addCardToDeckAndLayout;

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

        this.getDraftState();
    },

    getDraftState:function () {
        var that = this;
        this.comm.getDraft(this.leagueType,
            function (xml) {
                var root = xml.documentElement;
                if (root.tagName == "availablePicks") {
                    var availablePicks = root.getElementsByTagName("availablePick");
                    for (var i = 0; i < availablePicks.length; i++) {
                        var availablePick = availablePicks[i];
                        var id = availablePick.getAttribute("id");
                        var url = availablePick.getAttribute("url");
                        var blueprintId = availablePick.getAttribute("blueprintId");

                        if (blueprintId != null) {
                            var card = new Card(blueprintId, null, null, "picks", "deck", "player");
                            var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                            cardDiv.data("card", card);
                            cardDiv.data("choiceId", id);
                            that.picksDiv.append(cardDiv);
                        } else {
                            var card = new Card("rules", null, null, "picks", "deck", "player");
                            var cardDiv = Card.CreateCardDiv(url, null, null, false, false, true, false, false);
                            cardDiv.data("card", card);
                            cardDiv.data("choiceId", id);
                            that.picksDiv.append(cardDiv);
                        }
                    }
                    that.picksCardGroup.layoutCards();
                    if (availablePicks.length > 0)
                        that.messageDiv.text("Make a pick");
                    else
                        that.messageDiv.text("Draft is finished");
                }
            });

        this.comm.getCollection(this.leagueType, "sort:cardType,culture,name", 0, 1000,
            function (xml) {
                var root = xml.documentElement;
                if (root.tagName == "collection") {
                    var cards = root.getElementsByTagName("card");
                    for (var i=0; i<cards.length; i++) {
                        var card = cards[i];
                        var count = card.getAttribute("count");
                        var blueprintId = card.getAttribute("blueprintId");
                        for (var no = 0; no < count; no++) {
                            var card = new Card(blueprintId, null, null, "drafted", "deck", "player");
                            var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                            cardDiv.data("card", card);
                            that.draftedDiv.append(cardDiv);
                        }
                    }
                    that.draftedCardGroup.layoutCards();
                }
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
            debugger;
            if (event.which >= 1) {
                if (!this.successfulDrag) {
                    if (event.shiftKey || event.which > 1) {
                        this.displayCardInfo(selectedCardElem.data("card"));
                        return false;
                    } else {
                        if (selectedCardElem.data("card").zone == "picks") {
                            var choiceId = selectedCardElem.data("choiceId");
                            that.comm.makeDraftPick(that.leagueType, choiceId, function (xml) {
                                var root = xml.documentElement;
                                if (root.tagName == "pickResult") {
                                    var pickedCards = root.getElementsByTagName("pickedCard");
                                    for (var i = 0; i < pickedCards.length; i++) {
                                        var pickedCard = pickedCards[i];
                                        var blueprintId = pickedCard.getAttribute("blueprintId");
                                        var count = pickedCard.getAttribute("count");
                                        for (var no = 0; no < count; no++) {
                                            var card = new Card(blueprintId, null, null, "drafted", "deck", "player");
                                            var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                                            cardDiv.data("card", card);
                                            that.draftedDiv.append(cardDiv);
                                        }
                                    }
                                    that.draftedCardGroup.layoutCards();

                                    var availablePicks = root.getElementsByTagName("availablePick");
                                    for (var i = 0; i < availablePicks.length; i++) {
                                        var availablePick = availablePicks[i];
                                        var id = availablePick.getAttribute("id");
                                        var url = availablePick.getAttribute("url");
                                        var blueprintId = availablePick.getAttribute("blueprintId");

                                        if (blueprintId != null) {
                                            var card = new Card(blueprintId, null, null, "picks", "deck", "player");
                                            var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), false, false, card.hasErrata(), false);
                                            cardDiv.data("card", card);
                                            cardDiv.data("choiceId", id);
                                            that.picksDiv.append(cardDiv);
                                        } else {
                                            var card = new Card("rules", null, null, "picks", "deck", "player");
                                            var cardDiv = Card.CreateCardDiv(url, null, null, false, false, true, false, false);
                                            cardDiv.data("card", card);
                                            cardDiv.data("choiceId", id);
                                            that.picksDiv.append(cardDiv);
                                        }
                                    }
                                    that.picksCardGroup.layoutCards();
                                    if (availablePicks.length > 0)
                                        that.messageDiv.text("Make a pick");
                                    else
                                        that.messageDiv.text("Draft is finished");
                                }
                            });
                            $(".card", that.picksDiv).remove();
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
            var padding = 5;

            var topWidth = this.topDiv.width();
            var topHeight = this.topDiv.height();

            var bottomWidth = this.bottomDiv.width();
            var bottomHeight = this.bottomDiv.height();

            this.picksDiv.css({position:"absolute", left:padding, top:messageHeight+padding, width:topWidth-padding*2, height:topHeight-messageHeight-padding*2});
            this.picksCardGroup.setBounds(0, 0, topWidth-padding*2, topHeight-messageHeight-padding*2);

            this.draftedDiv.css({position:"absolute", left:padding, top:padding, width:bottomWidth-padding*2, height:bottomHeight-padding*2});
            this.draftedCardGroup.setBounds(0, 0, bottomWidth-padding*2, bottomHeight-padding*2);
        } else {
            this.picksCardGroup.layoutCards();
            this.draftedCardGroup.layoutCards();
        }
    },

    processError:function (xhr, ajaxOptions, thrownError) {
        if (thrownError != "abort")
            alert("There was a problem during communication with server");
    }
});
