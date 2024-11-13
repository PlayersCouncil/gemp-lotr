class Card {
    blueprintId = null;
    bareBlueprint = null;
    foil = null;
    alternateImage = null;
    tengwar = false;
    horizontal = null;
    imageUrl = null;
    backSideImageUrl = null;
    testingText = null;
    backSideTestingText = null;
    
    tengwar = null;
    hasWiki = null;
    siteNumber = null;
    errata = null;

    zone = null;
    cardId = null;
    owner = null;
    attachedCards = null;
    locationIndex = null;
    inverted = false;
    upsideDown = false;
    sideways = false;
    onSide = false;
    inBattle = false;
    attackingInAttack = false;
    defendingInAttack = false;
    inDuelOrLightsaberCombat = false;
    incomplete = null;
    frozen = null;
    suspended = null;
    collapsed = null;
    
    static CardCache = {};
    static CardScale = 350 / 490;
    
    static StripBlueprintId(blueprintId) {
        var stripped = blueprintId;
        
        stripped = stripped.replace("*", "");
        stripped = stripped.replace("^", "");
        stripped = stripped.replace(/(_\d+)T/, "$1");

        return stripped;
    }
    
    static GetFoil(bpid) {
       //At the very smallest, a card id can be 3 characters, i.e. 1_1
        //Thus we start searching at the 2nd character
        return bpid.includes("*", 2); 
    }
    
    static GetAlternateImage(bpid) {
        return bpid.includes("^", 2);
    }

    //blueprintId, zone, cardId, owner, siteNumber
    constructor (blueprintId, testingText, backSideTestingText, zone, cardId, owner, siteNumber, upsideDown, onSide) {
        this.blueprintId = blueprintId;
        this.bareBlueprint = Card.StripBlueprintId(this.blueprintId);
        
        this.foil = Card.GetFoil(blueprintId);
        this.alternateImage = Card.GetAlternateImage(blueprintId);
        
        if (this.alternateImage) {
            if (fixedImages[this.bareBlueprint + "ai"] != null)
                this.bareBlueprint = this.bareBlueprint + "ai";
        }
        
        this.tengwar = this.blueprintId.includes("T");
        
        this.hasWiki = Card.getFixedImage(this.bareBlueprint) == null
            && packBlueprints[this.bareBlueprint] == null;


        this.testingText = null;
        if (testingText !== undefined) {
            this.testingText = testingText;
        }
        this.backSideTestingText = null;
        if (backSideTestingText !== undefined) {
            this.backSideTestingText = backSideTestingText;
        }

        this.zone = zone;
        this.cardId = cardId;
        this.owner = owner;
        if (siteNumber !== undefined) {
            this.siteNumber = parseInt(siteNumber);
        }
        this.inverted = false;
        if (upsideDown !== undefined) {
            this.upsideDown = upsideDown;
        }
        this.sideways = false;
        if (onSide !== undefined) {
            this.onSide = onSide;
        }
        this.attachedCards = new Array();
        if (this.bareBlueprint == "rules") {
            this.imageUrl = "/gemp-lotr/images/rules.png";
            return;
        }

        this.horizontal = Card.isHorizontal(this.bareBlueprint, this.zone);

        if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2" && Card.CardCache[this.bareBlueprint] != null) {
            var cardFromCache = Card.CardCache[this.bareBlueprint];
            this.imageUrl = cardFromCache.imageUrl;
            this.backSideImageUrl = cardFromCache.backSideImageUrl;
            this.incomplete = cardFromCache.incomplete;
            this.errata = cardFromCache.errata;
        } else {
            this.imageUrl = Card.getImageUrl(this.bareBlueprint, this.tengwar);
            this.backSideImageUrl = Card.getBackSideUrl(this.bareBlueprint);
            this.incomplete = Card.isIncomplete(this.bareBlueprint);
            
            var separator = this.bareBlueprint.indexOf("_");
            var setNo = parseInt(this.bareBlueprint.substr(0, separator));
            var cardNo = parseInt(this.bareBlueprint.substr(separator + 1));

            this.errata = Card.getErrata(setNo, cardNo) != null;

            if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2") {
                Card.CardCache[this.bareBlueprint] = {
                    imageUrl:this.imageUrl,
                    backSideImageUrl:this.backSideImageUrl,
                    incomplete:this.incomplete,
                    errata:this.errata
                };
            }
        }
    }
    
    static getFixedImage (blueprintId) {
        var img = fixedImages[blueprintId];
        if (img != null)
            return img;
        img = set40[blueprintId];
        if (img != null)
            return img;
        img = hobbit[blueprintId];
        if (img != null)
            return img;
        img = PCCards[blueprintId];
        if (img != null)
            return img;
        return null;
    }

    flipOverCard() {
        this.bareBlueprint = Card.getBackSideBlueprintId(this.bareBlueprint);
        this.horizontal = Card.isHorizontal(this.bareBlueprint, this.zone);
        var tempText = this.testingText;
        this.testingText = this.backSideTestingText;
        this.backSideTestingText = tempText;

        if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2" && Card.CardCache[this.bareBlueprint] != null) {
            var cardFromCache = Card.CardCache[this.bareBlueprint];
            this.imageUrl = cardFromCache.imageUrl;
            this.backSideImageUrl = cardFromCache.backSideImageUrl;
            this.incomplete = cardFromCache.incomplete;
        } else {
            this.imageUrl = Card.getImageUrl(this.bareBlueprint);
            this.backSideImageUrl = Card.getBackSideUrl(this.bareBlueprint);
            this.incomplete = Card.isIncomplete(this.bareBlueprint);

            if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2") {
                Card.CardCache[this.bareBlueprint] = {
                    imageUrl:this.imageUrl,
                    backSideImageUrl:this.backSideImageUrl,
                    incomplete:this.incomplete
                };
            }
        }
        $(".card:cardId(" + this.cardId + ") > img").attr('src', this.imageUrl);
    }

    turnCardOver(tempBlueprintId) {
        this.bareBlueprint = tempBlueprintId;
        this.horizontal = Card.isHorizontal(this.bareBlueprint, this.zone);
        var tempText = this.testingText;
        this.testingText = this.backSideTestingText;
        this.backSideTestingText = tempText;

        if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2" && Card.CardCache[this.bareBlueprint] != null) {
            var cardFromCache = Card.CardCache[this.bareBlueprint];
            this.imageUrl = cardFromCache.imageUrl;
            this.backSideImageUrl = cardFromCache.backSideImageUrl;
            this.incomplete = cardFromCache.incomplete;
        } else {
            this.imageUrl = Card.getImageUrl(this.bareBlueprint);
            this.backSideImageUrl = Card.getBackSideUrl(this.bareBlueprint);
            this.incomplete = Card.isIncomplete(this.bareBlueprint);

            if (this.bareBlueprint != "-1_1" && this.bareBlueprint != "-1_2") {
                Card.CardCache[this.bareBlueprint] = {
                    imageUrl:this.imageUrl,
                    backSideImageUrl:this.backSideImageUrl,
                    incomplete:this.incomplete
                };
            }
        }
        $(".card:cardId(" + this.cardId + ") > img").attr('src', this.imageUrl);
    }

    isFoil() {
        return this.foil;
    }

    isPack() {
        return packBlueprints[this.blueprintId] != null;
    }
    
    isTengwar() {
        return this.tengwar;
    }

    hasErrata() {
        var separator = this.blueprintId.indexOf("_");
        var setNo = parseInt(this.blueprintId.substr(0, separator));
        
        if(setNo >= 50 && setNo <= 89)
            return true;
        
        return this.errata;
    }
    
    //Checks for whether the card is on a pile that is always vertical
    effectivelyHorizontal() {
        if(!this.zone)
            return false;
        
        return (this.zone.startsWith("TOP_OF") &&
            Card.isBlueprintHorizontal(this.blueprintId));
    }

    static isZoneNeverHorizontal(zone) {
        if (zone == "DECK" 
                || zone == "TOP_OF_DISCARD" || zone == "TOP_OF_REMOVED"
                || zone == "STACKED" || zone == "STACKED_FACE_DOWN") {
            return true;
        }

        return false;
    }

    static isBlueprintHorizontal(blueprintId) {
        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));

        if (setNo == 0)
            return (cardNo == 1 || cardNo == 4 || cardNo == 6 || cardNo == 8);
        if (setNo == 1 || setNo == 51 || setNo == 71)
            return ((cardNo >= 319 && cardNo <= 363) || cardNo == 367);
        if (setNo == 2 || setNo == 52 || setNo == 72)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 3 || setNo == 53 || setNo == 73)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 4 || setNo == 54 || setNo == 74)
            return (cardNo >= 323 && cardNo <= 363);
        if (setNo == 5 || setNo == 55 || setNo == 75)
            return (cardNo >= 118 && cardNo <= 120);
        if (setNo == 6 || setNo == 56 || setNo == 76)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 7 || setNo == 57 || setNo == 77)
            return (cardNo >= 329 && cardNo <= 363);
        if (setNo == 8 || setNo == 58 || setNo == 78)
            return (cardNo >= 117 && cardNo <= 120);
        if (setNo == 10 || setNo == 60 || setNo == 80)
            return (cardNo >= 117 && cardNo <= 120);
        if (setNo == 11 || setNo == 61 || setNo == 81)
            return (cardNo >= 227 && cardNo <= 266);
        if (setNo == 12 || setNo == 62 || setNo == 82)
            return (cardNo >= 185 && cardNo <= 194);
        if (setNo == 13 || setNo == 63 || setNo == 83)
            return (cardNo >= 185 && cardNo <= 194);
        if (setNo == 15 || setNo == 65 || setNo == 85)
            return (cardNo >= 187 && cardNo <= 194) || cardNo == 210;
        if (setNo == 17 || setNo == 67 || setNo == 87)
            return (cardNo >= 145 && cardNo <= 148);
        if (setNo == 18 || setNo == 68 || setNo == 88)
            return (cardNo >= 134 && cardNo <= 140) || cardNo == 151;
        if (setNo == 20)
            return (cardNo >= 416 && cardNo <= 469);
        if (setNo == 30)
            return (cardNo >= 49 && cardNo <= 65);
        if (setNo == 31)
            return (cardNo >= 44 && cardNo <= 47);
        if (setNo == 32)
            return (cardNo >= 46 && cardNo <= 49);
        if (setNo == 33)
            return (cardNo >= 55 && cardNo <= 58);
        if (setNo == 40)
            return (cardNo >= 273 && cardNo <= 309);
        if (setNo == 100 || setNo == 150)
            return (cardNo == 1);
        if (setNo == 101 || setNo == 151)
            return (cardNo >= 57 && cardNo <= 64);
        if (setNo == 102)
            return (cardNo >= 67 && cardNo <= 73);

        return false;
    }

    static isHorizontal(blueprintId, zone) {

        // For some zones, never show the card as horizontal
        if (Card.isZoneNeverHorizontal(zone)) {
            return false;
        }

        return Card.isBlueprintHorizontal(blueprintId);
    }

    static isIncomplete(blueprintId) {
        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));
        
        if (setNo >= 400 && setNo < 600) {
            return true;
        }

        return false;
    }
    
    static getImageUrl(blueprintId, tengwar, ignoreErrata) {
        let image = Card.getFixedImage(blueprintId)
        if (image != null)
            return image;

        if (packBlueprints[blueprintId] != null)
            return packBlueprints[blueprintId];

        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));

        var errata = this.getErrata(setNo, cardNo);
        if (errata != null && (ignoreErrata === undefined || !ignoreErrata))
            return errata;

        var mainLocation = this.getMainLocation(setNo, cardNo);

        var cardStr;

        if (Card.isMasterworks(setNo, cardNo))
            cardStr = Card.formatSetNo(setNo) + "O0" + (cardNo - this.getMasterworksOffset(setNo));
        else
            cardStr = Card.formatCardNo(setNo, cardNo);

        return mainLocation + "LOTR" + cardStr + (tengwar ? "T" : "") + ".jpg";
    }
    
    getWikiLink() {
        var imageUrl = Card.getImageUrl(this.blueprintId, false, true);
        var afterLastSlash = imageUrl.lastIndexOf("/") + 1;
        var countAfterLastSlash = imageUrl.length - 4 - afterLastSlash;
        return "http://wiki.lotrtcgpc.net/wiki/" + imageUrl.substr(afterLastSlash, countAfterLastSlash);
    }

    hasWikiInfo() {
        return this.hasWiki;
    }

    static getBackSideBlueprintId(blueprintId) {
        if (blueprintId.endsWith("_BACK")) {
            return blueprintId.substring(0, blueprintId.length - 5);
        }
        var backSideUrl = Card.getImageUrl(blueprintId.concat("_BACK"));
        if (backSideUrl != null) {
            return blueprintId.concat("_BACK");
        }
        var genericBackUrl = Card.getImageUrl(blueprintId);
        if (genericBackUrl != null) {
            if (Card.getImageUrl(blueprintId).includes("-Dark/"))
                    return "-1_2";
                else
                    return "-1_1";
        }
    }

    static getBackSideUrl(blueprintId) {
        return Card.getImageUrl(Card.getBackSideBlueprintId(blueprintId));
    }
    
    static formatSetNo(setNo) {
        var setNoStr;
        if (setNo < 10)
            setNoStr = "0" + setNo;
        else
            setNoStr = setNo;
        return setNoStr;
    }

    static formatCardNo(setNo, cardNo) {
        var setNoStr = Card.formatSetNo(setNo);

        var cardStr;
        if (cardNo < 10)
            cardStr = setNoStr + "00" + cardNo;
        else if (cardNo < 100)
            cardStr = setNoStr + "0" + cardNo;
        else
            cardStr = setNoStr + "" + cardNo;

        return cardStr;
    }

    static getMainLocation(setNo, cardNo) {
        return "https://i.lotrtcgpc.net/decipher/";
    }

    static getMasterworksOffset(setNo) {
        if (setNo == 17)
            return 148;
        if (setNo == 18)
            return 140;
        return 194;
    }

    static isMasterworks(setNo, cardNo) {
        if (setNo == 12)
            return cardNo > 194;
        if (setNo == 13)
            return cardNo > 194;
        if (setNo == 15)
            return cardNo > 194 && cardNo < 204;
        if (setNo == 17)
            return cardNo > 148;
        if (setNo == 18)
            return cardNo > 140;
        return false;
    }

    static remadeErratas = {
        "0": [7],
        "1": [3, 12, 43, 46, 55, 109, 113, 138, 162, 211, 235, 263, 309, 318, 331, 338, 343, 360],
        "3": [48, 110],
        "4": [63, 236, 237, 352],
        "6": [39, 46, 85],
        "7": [10, 14, 66, 114, 133, 134, 135, 182, 284, 285, 289, 302, 357],
        "8": [20, 33, 69],
        "17": [15, 87, 96, 118],
        "18": [8, 12, 20, 25, 35, 48, 50, 55, 77, 78, 79, 80, 82, 94, 97, 133]
    }

    static getErrata(setNo, cardNo) {
        if (this.remadeErratas["" + setNo] != null && $.inArray(cardNo, Card.remadeErratas["" + setNo]) != -1)
            return "/gemp-lotr/images/erratas/LOTR" + Card.formatCardNo(setNo, cardNo) + ".jpg";
        return null;
    }

    getHeightForWidth(width) {
        if (this.horizontal)
            return Math.floor(width * Card.CardScale);
        else
            return Math.floor(width / Card.CardScale);
    }

    getHeightForColumnWidth(columnWidth) {
        if (this.horizontal)
            return columnWidth;
        else
            return Math.floor(columnWidth / Card.CardScale);
    }

    getWidthForHeight(height) {
        if (this.horizontal)
            return Math.floor(height / Card.CardScale);
        else
            return Math.floor(height * Card.CardScale);
    }

    getWidthForMaxDimension(maxDimension) {
        if (this.horizontal)
            return maxDimension;
        else
            return Math.floor(maxDimension * Card.CardScale);
    }

    getHeightForMaxDimension(maxDimension) {
        if (this.horizontal)
            return Math.floor(maxDimension * Card.CardScale);
        else
            return maxDimension;
    }
    
    static getFoilPresentation() {
        let foil = loadFromCookie("foilPresentation", "none");
        //Handling of old cookies. This should be removable late 2025.
        if(foil === "true") {
            saveToCookie("foilPresentation", "animated");
            return "animated"
        }
        
        if(foil === "false") {
            saveToCookie("foilPresentation", "static");
            return "static"
        }
        return foil;
    }
    
    static CreateCardDiv(image, testingText, text, foil, tokens, noBorder, errata, incomplete) {
        var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'>" + ((text != null) ? text : "") + "</div>");

        // if (incomplete) {
        //     var incompleteDiv = $("<div class='incompleteOverlay'><img src='https://res.starwarsccg.org/gemp/incompleteCard.png' width='100%' height='100%'></div>");
        //     cardDiv.append(incompleteDiv);
        // }
        
        if (errata) {
            var errataDiv = $("<div class='errataOverlay'><img src='/gemp-lotr/images/errata-vertical.png' width='100%' height='100%'></div>");
            cardDiv.append(errataDiv);
        }

        var foilPresentation = Card.getFoilPresentation();

        if (foil && foilPresentation !== 'none') {
            var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
            var foilDiv = $("<div class='foilOverlay'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
            cardDiv.append(foilDiv);
        }

        // var frozenDiv = $("<div class='frozenOverlay'><img src='https://res.starwarsccg.org/cards/carbonite.gif' width='100%' height='100%'></div>");
        // cardDiv.append(frozenDiv);

        // var suspendedDiv = $("<div class='suspendedOverlay'><img src='https://res.starwarsccg.org/gemp/gray.jpg' width='100%' height='100%'></div>");
        // cardDiv.append(suspendedDiv);

        // var collapsedDiv = $("<div class='collapsedOverlay'><img src='https://res.starwarsccg.org/gemp/collapsed.jpg' width='100%' height='100%'></div>");
        // cardDiv.append(collapsedDiv);

        if (tokens === undefined || tokens) {
            var overlayDiv = $("<div class='tokenOverlay'></div>");
            cardDiv.append(overlayDiv);
        }

        if (testingText != null) {
            var testingTextDiv = $("<div class='testingTextOverlay'></div>");
            var firstPipe = testingText.indexOf('|');
            if (firstPipe !== -1) {
                testingTextDiv.html(testingText.substring(0, firstPipe));
            }
            else {
                testingTextDiv.html(testingText);
            }
            cardDiv.append(testingTextDiv);
        }

        var borderDiv = $("<div class='borderOverlay'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
        if (noBorder)
            borderDiv.addClass("noBorder");
        cardDiv.append(borderDiv);
        
        var cardPileCountDiv = $("<div class='cardPileCount'></div>");
        cardDiv.append(cardPileCountDiv);

        return cardDiv;
    }

    static CreateFullCardDiv(image, testingText, foil, horizontal, noBorder) {
        var foilPresentation = Card.getFoilPresentation();
        
        if (horizontal) {
            var cardDiv = $("<div style='position: relative;width:497px;height:357px;'></div>");
            cardDiv.append("<div class='fullcard' style='position:absolute'><img src='" + image + "' width='497' height='357'></div>");

            if (foil && foilPresentation !== 'none') {
                var foilDiv = $("<div class='foilOverlay' style='position:absolute;width:497px;height:357px'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
                cardDiv.append(foilDiv);
            }

            if (testingText != null) {
                var testingTextDiv = $("<div class='testingTextOverlay' style='position:absolute;left:25px;width:449px;top:18px;height:321px'></div>");
                testingTextDiv.html(testingText.replace(/\|/g, "<br/>"));
                cardDiv.append(testingTextDiv);
            }

            var borderDiv = $("<div class='borderOverlay' style='position:absolute;width:465px;height:325px;border-width:16px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            if (noBorder)
                borderDiv.addClass("noBorder");
            cardDiv.append(borderDiv);

        } else {
            var cardDiv = $("<div style='position: relative;width:357px;height:497px;'></div>");
            cardDiv.append("<div class='fullcard' style='position:absolute'><img src='" + image + "' width='357' height='497'></div>");
            
            if (foil && foilPresentation !== 'none') {
                var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
                var foilDiv = $("<div class='foilOverlay' style='position:absolute;width:357px;height:497px'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
                cardDiv.append(foilDiv);
            }

            if (testingText != null) {
                var testingTextDiv = $("<div class='testingTextOverlay' style='position:absolute;left:36px;width:285px;top:50px;height:398px'></div>");
                testingTextDiv.html(testingText.replace(/\|/g, "<br/>"));
                cardDiv.append(testingTextDiv);
            }

            var borderDiv = $("<div class='borderOverlay' style='position:absolute;width:325px;height:465px;border-width:16px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            if (noBorder)
                borderDiv.addClass("noBorder");
            cardDiv.append(borderDiv);
        }

        return cardDiv;
    }

    static CreateSimpleCardDiv(image, testingText, foil, incomplete, borderWidth) {
        var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'></div>");

        if (incomplete) {
            var incompleteDiv = $("<div class='incompleteOverlay' style='position:absolute;left:0px;top:0px;width:100%;height:100%'><img src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            cardDiv.append(incompleteDiv);
        }

        if (foil && foilPresentation !== 'none') {
                var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
                var foilDiv = $("<div class='foilOverlay' style='position:absolute;width:357px;height:497px'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
                cardDiv.append(foilDiv);
            }

        if (testingText != null) {
            var testingTextDiv = $("<div class='testingTextOverlay' style='position:absolute;left:5%;top:5%;width:90%;height:90%'></div>");
            testingTextDiv.html(testingText.replace(/\|/g, "<br/>"));
            cardDiv.append(testingTextDiv);
        }

        var borderDiv = $("<div class='borderOverlay' style='position:absolute;left:0px;top:0px;width:100%;height:100%;border-width:" + borderWidth + "px;box-sizing:border-box'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
        cardDiv.append(borderDiv);

        return cardDiv;
    }
}
