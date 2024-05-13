var CardGroup = Class.extend({
    container:null,
    x:null,
    y:null,
    width:null,
    height:null,
    belongTestFunc:null,
    padding:5,
    maxCardHeight:497,
    descDiv:null,

    init:function (container, belongTest, createDiv, existingDiv) {
        this.container = container;
        this.belongTestFunc = belongTest;

        if (createDiv === undefined || createDiv) {
            if(existingDiv !== undefined) {
                this.descDiv = existingDiv;
            }
            else {
                this.descDiv = $("<div class='ui-widget-content'></div>");
                container.append(this.descDiv);
            }
            
            this.descDiv.css({"border-radius":"7px"});
        }
    },

    getCardElems:function () {
        var cardsToLayout = new Array();
        var that = this;
        $(".card", this.container).each(function (index) {
            var card = $(this).data("card");
            if (that.belongTestFunc(card)) {
                cardsToLayout.push($(this));
            }
        });
        return cardsToLayout;
    },

    cardBelongs:function (cardData) {
        return this.belongTestFunc(cardData);
    },

    setBounds:function (x, y, width, height) {
        this.x = x + 3;
        this.y = y + 3;
        this.width = width - 6;
        this.height = height - 6;
        if (this.descDiv != null)
            this.descDiv.css({left:x + "px", top:y + "px", width:width, height:height, position:"absolute"});
        this.layoutCards();
    },

    layoutCards:function () {
        alert("This should be overriden by the extending classes");
    },

    layoutCard:function (cardElem, x, y, width, height, index) {
        layoutCardElem(cardElem, x, y, width, height, index);

        layoutTokens(cardElem);
    }
});

var VerticalBarGroup = CardGroup.extend({
    init:function (container, belongTest, createDiv, existingDiv) {
        this._super(container, belongTest, createDiv, existingDiv);
    },

    layoutCards:function () {
        var cardsToLayout = this.getCardElems();

        var cardCount = cardsToLayout.length;
        var totalHeight = 0;

        for (var cardIndex in cardsToLayout)
            totalHeight += cardsToLayout[cardIndex].data("card").getHeightForWidth(this.width);

        var topGap = 20;

        var resultPadding = Math.min(this.padding, (this.height - totalHeight - topGap) / (cardCount - 1));

        var x = this.x;
        var y = this.y + topGap;
        var index = 10;
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = (cardElem.data("card").getHeightForWidth(this.width));

            if (cardData.attachedCards.length == 1) {
                this.layoutCard(cardData.attachedCards[0], x + (this.width - cardHeight) / 2, y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                index++;
            } else {
                for (var i = 0; i < cardData.attachedCards.length; i++) {
                    this.layoutCard(cardData.attachedCards[i], x + i * (this.width - cardHeight) / (cardData.attachedCards.length - 1), y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                    index++;
                }
            }

            this.layoutCard(cardElem, x, y, this.width, cardHeight, index);

            y += cardHeight + resultPadding;
            index++;
        }
    }
});

var VerticalFlexGroup = CardGroup.extend({
    init:function (container, belongTest, createDiv, existingDiv) {
        this._super(container, belongTest, createDiv, existingDiv);
    },
    
    setBounds:function (x, y, width, height) {
        this.layoutCards();
    },

    layoutCards:function () {
        var cardsToLayout = this.getCardElems();

        var cardCount = cardsToLayout.length;
        var totalHeight = 0;

        for (var cardIndex in cardsToLayout)
            totalHeight += cardsToLayout[cardIndex].data("card").getHeightForWidth(this.width);

        var topGap = 20;

        var resultPadding = Math.min(this.padding, (this.height - totalHeight - topGap) / (cardCount - 1));

        var x = this.x;
        var y = this.y + topGap;
        var index = 10;
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = (cardElem.data("card").getHeightForWidth(this.width));

            if (cardData.attachedCards.length == 1) {
                this.layoutCard(cardData.attachedCards[0], x + (this.width - cardHeight) / 2, y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                index++;
            } else {
                for (var i = 0; i < cardData.attachedCards.length; i++) {
                    this.layoutCard(cardData.attachedCards[i], x + i * (this.width - cardHeight) / (cardData.attachedCards.length - 1), y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                    index++;
                }
            }

            this.layoutCard(cardElem, x, y, this.width, cardHeight, index);

            y += cardHeight + resultPadding;
            index++;
        }
    }
});

var AdvPathCardGroup = CardGroup.extend({
    positions:null,
    currentPlayerIndex:null,

    init:function (container) {
        this._super(container,
            function (card) {
                return (card.zone == "ADVENTURE_PATH");
            });
    },

    setPositions:function (positions) {
        this.positions = positions;
    },

    setCurrentPlayerIndex:function (index) {
        this.currentPlayedIndex = index;
    },

    layoutCards:function () {
        var cardsToLayout = this.getCardElems();

        cardsToLayout.sort(
            function (first, second) {
                return (first.data("card").siteNumber - second.data("card").siteNumber);
            }
        );

        var cardCount = cardsToLayout.length;
        var totalHeight = 0;

        for (var cardIndex in cardsToLayout) {
            var cardData = cardsToLayout[cardIndex].data("card");
            var cardHeight = cardData.getHeightForWidth(this.width);
            totalHeight += cardHeight;
            if (cardData.attachedCards.length > 0)
                totalHeight += cardHeight * 0.2;
        }

        var resultPadding = Math.min(this.padding, (this.height - totalHeight) / (cardCount - 1));

        var x = this.x;
        var y = this.y;
        var index = 10;
        for (var cardIndex in cardsToLayout) {
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardHeight = (cardElem.data("card").getHeightForWidth(this.width));

            if (cardData.attachedCards.length > 0)
                y += cardHeight * 0.1;

            cardData.tokens = {};
            if (this.positions != null) {
                for (var i = 0; i < this.positions.length; i++)
                    if (this.positions[i] == cardData.siteNumber) {
                        if (i == this.currentPlayedIndex)
                            cardData.tokens["" + (i + 1) + "-a"] = 1;
                        else
                            cardData.tokens["" + (i + 1) + "-i"] = 1;
                    }
            }

            if (cardData.attachedCards.length == 1) {
                this.layoutCard(cardData.attachedCards[0], x + (this.width - cardHeight) / 2, y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                index++;
            } else {
                for (var i = 0; i < cardData.attachedCards.length; i++) {
                    this.layoutCard(cardData.attachedCards[i], x + i * (this.width - cardHeight) / (cardData.attachedCards.length - 1), y - (this.width - cardHeight) / 2, cardHeight, this.width, index);
                    index++;
                }
            }

            this.layoutCard(cardElem, x, y, this.width, cardHeight, index);

            if (cardData.attachedCards.length > 0)
                y += cardHeight * 0.1;

            y += cardHeight + resultPadding;
            index++;
        }
    }
});

var NormalCardGroup = CardGroup.extend({

    init:function (container, belongTest, createDiv, existingDiv) {
        this._super(container, belongTest, createDiv, existingDiv);
    },

    layoutCards:function () {
        var cardsToLayout = this.getCardElems();

        var proportionsArray = this.getCardsWithAttachmentWidthProportion(cardsToLayout);

        var rows = 0;
        var result = false;
        do {
            rows++;
            result = this.layoutInRowsIfPossible(cardsToLayout, proportionsArray, rows);
        } while (!result);
    },

    getAttachedCardsWidth:function (maxDimension, cardData) {
        var result = 0;
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            result += attachedCardData.getWidthForMaxDimension(maxDimension);
            result += this.getAttachedCardsWidth(maxDimension, attachedCardData);
        }
        return result;
    },

    getCardsWithAttachmentWidthProportion:function (cardsToLayout) {
        var proportionsArray = new Array();
        for (var cardIndex in cardsToLayout) {
            var cardData = cardsToLayout[cardIndex].data("card");
            var cardWithAttachmentWidth = cardData.getWidthForMaxDimension(1000);
            cardWithAttachmentWidth += this.getAttachedCardsWidth(1000, cardData) * 0.2;
            proportionsArray.push(cardWithAttachmentWidth / 1000);
        }
        return proportionsArray;
    },

    layoutInRowsIfPossible:function (cardsToLayout, proportionsArray, rowCount) {
        if (rowCount == 1) {
            var oneRowHeight = this.getHeightForLayoutInOneRow(proportionsArray);
            if (oneRowHeight * 2 + this.padding > this.height) {
                this.layoutInRow(cardsToLayout, oneRowHeight);
                return true;
            } else {
                return false;
            }
        } else {
            if (this.tryIfCanLayoutInRows(rowCount, proportionsArray)) {
                this.layoutInRows(rowCount, cardsToLayout);
                return true;
            } else {
                return false;
            }
        }
    },

    getHeightForLayoutInOneRow:function (proportionsArray) {
        var totalWidth = 0;
        for (var cardIndex in proportionsArray)
            totalWidth += proportionsArray[cardIndex] * this.height;

        var widthWithoutPadding = this.width - (this.padding * (proportionsArray.length - 1));
        if (totalWidth > widthWithoutPadding) {
            return Math.floor(this.height / (totalWidth / widthWithoutPadding));
        } else {
            return this.height;
        }
    },

    tryIfCanLayoutInRows:function (rowCount, proportionsArray) {
        var rowHeight = (this.height - (this.padding * (rowCount - 1))) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var totalWidth = 0;
        var row = 0;
        for (var cardIndex in proportionsArray) {
            var cardWidthWithAttachments = proportionsArray[cardIndex] * rowHeight;
            totalWidth += cardWidthWithAttachments;
            if (totalWidth > this.width) {
                row++;
                if (row >= rowCount)
                    return false;
                totalWidth = cardWidthWithAttachments;
            }
            totalWidth += this.padding;
        }
        return true;
    },

    layoutAttached:function (cardData, y, height, layoutVars) {
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            var attachedCardWidth = attachedCardData.getWidthForMaxDimension(height);
            this.layoutAttached(attachedCardData, y, height, layoutVars);
            this.layoutCard(cardData.attachedCards[i], this.x + layoutVars.x, this.y + y, attachedCardWidth, attachedCardData.getHeightForWidth(attachedCardWidth), layoutVars.index);
            layoutVars.x += Math.floor(attachedCardWidth * 0.2);
            layoutVars.index++;
        }
    },

    layoutInRow:function (cardsToLayout, height) {
        if (this.maxCardHeight != null)
            height = Math.min(this.maxCardHeight, height);
        var layoutVars = {};
        layoutVars.x = 0;
        var y = Math.floor((this.height - height) / 2);

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(height);

            this.layoutAttached(cardData, y, height, layoutVars)

            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            layoutVars.x += this.padding;
        }
    },

    layoutInRows:function (rowCount, cardsToLayout) {
        var rowHeight = (this.height - ((rowCount - 1) * this.padding)) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var yBias = Math.floor((this.height - (rowHeight * rowCount) - (this.padding * (rowCount - 1))) / 2);
        var layoutVars = {};
        layoutVars.x = 0;
        var row = 0;
        var y = yBias;

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(rowHeight);

            var attachmentWidths = this.getAttachedCardsWidth(rowHeight, cardData) * 0.2;
            var cardWidthWithAttachments = cardWidth + attachmentWidths;
            if (layoutVars.x + cardWidthWithAttachments > this.width) {
                row++;
                layoutVars.x = 0;
                y = yBias + row * (rowHeight + this.padding);
            }

            this.layoutAttached(cardData, y, rowHeight, layoutVars);
            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            if (layoutVars.x > this.width)
                return false;
            layoutVars.x += this.padding;
        }

        return true;
    }
});

var NormalFlexGroup = CardGroup.extend({

    init:function (container, belongTest, createDiv, existingDiv) {
        this._super(container, belongTest, createDiv, existingDiv);
    },
    
    setBounds:function (x, y, width, height) {

        this.layoutCards();
    },

    layoutCards:function () {
        var cardsToLayout = this.getCardElems();

        var proportionsArray = this.getCardsWithAttachmentWidthProportion(cardsToLayout);

        var rows = 0;
        var result = false;
        do {
            rows++;
            result = this.layoutInRowsIfPossible(cardsToLayout, proportionsArray, rows);
        } while (!result);
    },

    getAttachedCardsWidth:function (maxDimension, cardData) {
        var result = 0;
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            result += attachedCardData.getWidthForMaxDimension(maxDimension);
            result += this.getAttachedCardsWidth(maxDimension, attachedCardData);
        }
        return result;
    },

    getCardsWithAttachmentWidthProportion:function (cardsToLayout) {
        var proportionsArray = new Array();
        for (var cardIndex in cardsToLayout) {
            var cardData = cardsToLayout[cardIndex].data("card");
            var cardWithAttachmentWidth = cardData.getWidthForMaxDimension(1000);
            cardWithAttachmentWidth += this.getAttachedCardsWidth(1000, cardData) * 0.2;
            proportionsArray.push(cardWithAttachmentWidth / 1000);
        }
        return proportionsArray;
    },

    layoutInRowsIfPossible:function (cardsToLayout, proportionsArray, rowCount) {
        if (rowCount == 1) {
            var oneRowHeight = this.getHeightForLayoutInOneRow(proportionsArray);
            if (oneRowHeight * 2 + this.padding > this.height) {
                this.layoutInRow(cardsToLayout, oneRowHeight);
                return true;
            } else {
                return false;
            }
        } else {
            if (this.tryIfCanLayoutInRows(rowCount, proportionsArray)) {
                this.layoutInRows(rowCount, cardsToLayout);
                return true;
            } else {
                return false;
            }
        }
    },

    getHeightForLayoutInOneRow:function (proportionsArray) {
        var totalWidth = 0;
        for (var cardIndex in proportionsArray)
            totalWidth += proportionsArray[cardIndex] * this.height;

        var widthWithoutPadding = this.width - (this.padding * (proportionsArray.length - 1));
        if (totalWidth > widthWithoutPadding) {
            return Math.floor(this.height / (totalWidth / widthWithoutPadding));
        } else {
            return this.height;
        }
    },

    tryIfCanLayoutInRows:function (rowCount, proportionsArray) {
        var rowHeight = (this.height - (this.padding * (rowCount - 1))) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var totalWidth = 0;
        var row = 0;
        for (var cardIndex in proportionsArray) {
            var cardWidthWithAttachments = proportionsArray[cardIndex] * rowHeight;
            totalWidth += cardWidthWithAttachments;
            if (totalWidth > this.width) {
                row++;
                if (row >= rowCount)
                    return false;
                totalWidth = cardWidthWithAttachments;
            }
            totalWidth += this.padding;
        }
        return true;
    },

    layoutAttached:function (cardData, y, height, layoutVars) {
        for (var i = 0; i < cardData.attachedCards.length; i++) {
            var attachedCardData = cardData.attachedCards[i].data("card");
            var attachedCardWidth = attachedCardData.getWidthForMaxDimension(height);
            this.layoutAttached(attachedCardData, y, height, layoutVars);
            this.layoutCard(cardData.attachedCards[i], this.x + layoutVars.x, this.y + y, attachedCardWidth, attachedCardData.getHeightForWidth(attachedCardWidth), layoutVars.index);
            layoutVars.x += Math.floor(attachedCardWidth * 0.2);
            layoutVars.index++;
        }
    },

    layoutInRow:function (cardsToLayout, height) {
        if (this.maxCardHeight != null)
            height = Math.min(this.maxCardHeight, height);
        var layoutVars = {};
        layoutVars.x = 0;
        var y = Math.floor((this.height - height) / 2);

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(height);

            this.layoutAttached(cardData, y, height, layoutVars)

            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            layoutVars.x += this.padding;
        }
    },

    layoutInRows:function (rowCount, cardsToLayout) {
        var rowHeight = (this.height - ((rowCount - 1) * this.padding)) / rowCount;
        if (this.maxCardHeight != null)
            rowHeight = Math.min(this.maxCardHeight, rowHeight);
        var yBias = Math.floor((this.height - (rowHeight * rowCount) - (this.padding * (rowCount - 1))) / 2);
        var layoutVars = {};
        layoutVars.x = 0;
        var row = 0;
        var y = yBias;

        for (var cardIndex in cardsToLayout) {
            layoutVars.index = 10;
            var cardElem = cardsToLayout[cardIndex];
            var cardData = cardElem.data("card");
            var cardWidth = cardData.getWidthForMaxDimension(rowHeight);

            var attachmentWidths = this.getAttachedCardsWidth(rowHeight, cardData) * 0.2;
            var cardWidthWithAttachments = cardWidth + attachmentWidths;
            if (layoutVars.x + cardWidthWithAttachments > this.width) {
                row++;
                layoutVars.x = 0;
                y = yBias + row * (rowHeight + this.padding);
            }

            this.layoutAttached(cardData, y, rowHeight, layoutVars);
            this.layoutCard(cardElem, this.x + layoutVars.x, this.y + y, cardWidth, cardData.getHeightForWidth(cardWidth), layoutVars.index);
            layoutVars.x += cardWidth;
            if (layoutVars.x > this.width)
                return false;
            layoutVars.x += this.padding;
        }

        return true;
    }
});

function layoutCardElem(cardElem, x, y, width, height, index) {
    x = Math.floor(x);
    y = Math.floor(y);
    width = Math.floor(width);
    height = Math.floor(height);
    if (cardElem.css("left") == (x + "px") && cardElem.css("top") == (y + "px")
        && cardElem.css("width") == (width + "px") && cardElem.css("height") == (height + "px")
        && cardElem.css("zIndex") == index)
        return;
    cardElem.css({position:"absolute", left:x + "px", top:y + "px", width:width, height:height, zIndex:index });

    var tokenOverlay = $(".tokenOverlay", cardElem);
    if (tokenOverlay.length > 0)
        tokenOverlay.css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});

    $(".errataOverlay", cardElem).css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});
    $(".foilOverlay", cardElem).css({position:"absolute", left:0 + "px", top:0 + "px", width:width, height:height});

    var maxDimension = Math.max(width, height);
    var borderWidth = Math.floor(maxDimension / 30);

    var borderOverlay = $(".borderOverlay", cardElem);
    if (borderOverlay.hasClass("noBorder"))
        borderWidth = 0;
    borderOverlay.css({position:"absolute", left:0 + "px", top:0 + "px", width:width - 2 * borderWidth, height:height - 2 * borderWidth, "border-width":borderWidth + "px"});

    var sizeListeners = cardElem.data("sizeListeners");
    if (sizeListeners != null)
        for (var i = 0; i < sizeListeners.length; i++)
            sizeListeners[i].sizeChanged(cardElem, width, height);
}

function layoutTokens(cardElem) {
    var tokenOverlay = $(".tokenOverlay", cardElem);

    if (tokenOverlay.length > 0) {
        var width = cardElem.width();
        var height = cardElem.height();
        var maxDimension = Math.max(width, height);

        var tokenSize = Math.floor(maxDimension / 13) * 2;

        // Remove all existing tokens
        $(".token", tokenOverlay).remove();

        var tokens = cardElem.data("card").tokens;
        if (tokens != null) {
            var tokenInColumnMax = 10;
            var tokenColumns = 0;

            for (var token in tokens)
                if (tokens.hasOwnProperty(token) && tokens[token] > 0) {
                    tokenColumns += (1 + Math.floor((tokens[token] - 1) / tokenInColumnMax));
                }

            var tokenIndex = 1;
            for (var token in tokens)
                if (tokens.hasOwnProperty(token)) {
                    if (token == "count") {
                        var tokenElem = $("<div class='cardCount token'>" + tokens[token] + "</div>").css({position:"absolute", left:((width - 20) / 2) + "px", top:((height - 18) / 2) + "px"});
                        tokenOverlay.append(tokenElem);
                    } else if (tokens[token] > 0) {
                        var tokenCount = tokens[token];

                        var tokenX = (tokenIndex * (width / (tokenColumns + 1))) - (tokenSize / 2);
                        var tokenInColumnIndex = 0;
                        for (var i = 0; i < tokenCount; i++) {
                            if (tokenInColumnIndex == tokenInColumnMax) {
                                tokenInColumnIndex = 0;
                                tokenIndex++;
                                tokenX = (tokenIndex * (width / (tokenColumns + 1))) - (tokenSize / 2);
                            }
                            var tokenY = Math.floor((maxDimension / 13) * (1 + tokenInColumnIndex));

                            if (i%2 == 1)
                                tokenX += Math.floor(tokenSize / 5);

                            var tokenElem = $("<img class='token' src='images/tokens/" + token.toLowerCase() + ".png' width='" + tokenSize + "' height='" + tokenSize + "'></img>").css({position:"absolute", left:tokenX + "px", top:tokenY + "px"});
                            tokenOverlay.append(tokenElem);
                            tokenInColumnIndex++;
                        }
                        tokenIndex++;
                    }
                }
        }
    }
}
