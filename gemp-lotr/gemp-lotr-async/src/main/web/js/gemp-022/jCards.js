var cardCache = {};
var cardScale = 357 / 497;
var fixedImages = {
    // "Forth the Three Hunters!" cards, separate special images
    "15_204": "http://lotrtcgwiki.com/images/LOTR15060D.jpg",
    "15_205": "http://lotrtcgwiki.com/images/LOTR15060E.jpg",
    "15_206": "http://lotrtcgwiki.com/images/LOTR15060G.jpg",
    // Holidays Gandalf
    "15_207": "http://lotrtcgwiki.com/images/LOTR15029H.jpg",
    // Gemp-LotR promos
    "gl_theOneRing": "/gemp-lotr/images/cards/gl_theOneRing.png"
};

var packBlueprints = {
    "(S)FotR - Starter": "/gemp-lotr/images/boosters/fotr_starter_selection.png",
    "(S)MoM - Starter": "/gemp-lotr/images/boosters/mom_starter_selection.png",
    "(S)RotEL - Starter": "/gemp-lotr/images/boosters/rotel_starter_selection.png",

    "(S)TTT - Starter": "/gemp-lotr/images/boosters/ttt_starter_selection.png",
    "(S)BoHD - Starter": "/gemp-lotr/images/boosters/bohd_starter_selection.png",
    "(S)EoF - Starter": "/gemp-lotr/images/boosters/eof_starter_selection.png",

    "(S)RotK - Starter": "/gemp-lotr/images/boosters/rotk_starter_selection.png",
    "(S)SoG - Starter": "/gemp-lotr/images/boosters/sog_starter_selection.png",
    "(S)MD - Starter": "/gemp-lotr/images/boosters/md_starter_selection.png",

    "(S)SH - Starter": "/gemp-lotr/images/boosters/sh_starter_selection.png",
    "(S)BR - Starter": "/gemp-lotr/images/boosters/br_starter_selection.png",
    "(S)BL - Starter": "/gemp-lotr/images/boosters/bl_starter_selection.png",
    
    "(S)HU - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)RoS - Starter": "/gemp-lotr/images/boosters/starter_selection.png",

    "(S)FotR - Tengwar": "/gemp-lotr/images/boosters/fotr_tengwar_selection.png",
    "(S)TTT - Tengwar": "/gemp-lotr/images/boosters/ttt_tengwar_selection.png",
    "(S)RotK - Tengwar": "/gemp-lotr/images/boosters/rotk_tengwar_selection.png",
    "(S)SH - Tengwar": "/gemp-lotr/images/boosters/sh_tengwar_selection.png",
    "(S)Tengwar": "/gemp-lotr/images/boosters/tengwar_selection.png",

    "(S)Booster Choice": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)Movie Booster Choice": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)TSBoosterChoice": "/gemp-lotr/images/boosters/booster_selection.png",

    "FotR - League Starter": "/gemp-lotr/images/boosters/fotr_league_starter.png",
    "Random FotR Foil Common": "/gemp-lotr/images/boosters/random_foil.png",
    "Random FotR Foil Uncommon": "/gemp-lotr/images/boosters/random_foil.png",

    "FotR - Gandalf Starter": "/gemp-lotr/images/boosters/fotr_gandalf_starter.png",
    "FotR - Aragorn Starter": "/gemp-lotr/images/boosters/fotr_aragorn_starter.png",
    "FotR - Booster": "/gemp-lotr/images/boosters/fotr_booster.png",

    "MoM - Gandalf Starter": "/gemp-lotr/images/boosters/mom_gandalf_starter.png",
    "MoM - Gimli Starter": "/gemp-lotr/images/boosters/mom_gimli_starter.png",
    "MoM - Booster": "/gemp-lotr/images/boosters/mom_booster.png",

    "RotEL - Boromir Starter": "/gemp-lotr/images/boosters/rotel_boromir_starter.png",
    "RotEL - Legolas Starter": "/gemp-lotr/images/boosters/rotel_legolas_starter.png",
    "RotEL - Booster": "/gemp-lotr/images/boosters/rotel_booster.png",

    "TTT - Aragorn Starter": "/gemp-lotr/images/boosters/ttt_aragorn_starter.png",
    "TTT - Theoden Starter": "/gemp-lotr/images/boosters/ttt_theoden_starter.png",
    "TTT - Booster": "/gemp-lotr/images/boosters/ttt_booster.png",

    "BoHD - Eowyn Starter": "/gemp-lotr/images/boosters/bohd_eowyn_starter.png",
    "BoHD - Legolas Starter": "/gemp-lotr/images/boosters/bohd_legolas_starter.png",
    "BoHD - Booster": "/gemp-lotr/images/boosters/bohd_booster.png",

    "EoF - Faramir Starter": "/gemp-lotr/images/boosters/eof_faramir_starter.png",
    "EoF - Witch-king Starter": "/gemp-lotr/images/boosters/eof_witch_king_starter.png",
    "EoF - Booster": "/gemp-lotr/images/boosters/eof_booster.png",

    "RotK - Aragorn Starter": "/gemp-lotr/images/boosters/rotk_aragorn_starter.png",
    "RotK - Eomer Starter": "/gemp-lotr/images/boosters/rotk_eomer_starter.png",
    "RotK - Booster": "/gemp-lotr/images/boosters/rotk_booster.png",

    "SoG - Merry Starter": "/gemp-lotr/images/boosters/sog_merry_starter.png",
    "SoG - Pippin Starter": "/gemp-lotr/images/boosters/sog_pippin_starter.png",
    "SoG - Booster": "/gemp-lotr/images/boosters/sog_booster.png",

    "MD - Frodo Starter": "/gemp-lotr/images/boosters/md_frodo_starter.png",
    "MD - Sam Starter": "/gemp-lotr/images/boosters/md_sam_starter.png",
    "MD - Booster": "/gemp-lotr/images/boosters/md_booster.png",

    "SH - Aragorn Starter": "/gemp-lotr/images/boosters/sh_aragorn_starter.png",
    "SH - Eowyn Starter": "/gemp-lotr/images/boosters/sh_eowyn_starter.png",
    "SH - Gandalf Starter": "/gemp-lotr/images/boosters/sh_gandalf_starter.png",
    "SH - Legolas Starter": "/gemp-lotr/images/boosters/sh_legolas_starter.png",
    "SH - Booster": "/gemp-lotr/images/boosters/sh_booster.png",

    "BR - Mouth Starter": "/gemp-lotr/images/boosters/br_mouth_starter.png",
    "BR - Saruman Starter": "/gemp-lotr/images/boosters/br_saruman_starter.png",
    "BR - Booster": "/gemp-lotr/images/boosters/br_booster.png",

    "BL - Arwen Starter": "/gemp-lotr/images/boosters/bl_arwen_starter.png",
    "BL - Boromir Starter": "/gemp-lotr/images/boosters/bl_boromir_starter.png",
    "BL - Booster": "/gemp-lotr/images/boosters/bl_booster.png",

    "HU - Aragorn Starter": "/gemp-lotr/images/boosters/hu_aragorn_starter.png",
    "HU - Mauhur Starter": "/gemp-lotr/images/boosters/hu_mauhur_starter.png",
    "HU - Booster": "/gemp-lotr/images/boosters/hu_booster.png",

    "RoS - Uruk Rampage Starter": "/gemp-lotr/images/boosters/ros_uruk_rampage_starter.png",
    "RoS - Evil Man Starter": "/gemp-lotr/images/boosters/ros_evil_man_starter.png",
    "RoS - Booster": "/gemp-lotr/images/boosters/ros_booster.png",

    "TaD - Faramir Starter": "/gemp-lotr/images/boosters/eof_faramir_starter.png",
    "TaD - Witch-king Starter": "/gemp-lotr/images/boosters/eof_witch_king_starter.png",
    "TaD - Booster": "/gemp-lotr/images/boosters/tad_booster.png",

    "REF - Booster": "/gemp-lotr/images/boosters/ref_booster.png",

    "Special-01": "/gemp-lotr/images/boosters/special-01.png",
    "Special-02": "/gemp-lotr/images/boosters/special-02.png",
    "Special-03": "/gemp-lotr/images/boosters/special-03.png",
    "Special-04": "/gemp-lotr/images/boosters/special-04.png",
    "Special-05": "/gemp-lotr/images/boosters/special-05.png",
    "Special-06": "/gemp-lotr/images/boosters/special-06.png",
    "Special-07": "/gemp-lotr/images/boosters/special-07.png",
    "Special-08": "/gemp-lotr/images/boosters/special-08.png",
    "Special-09": "/gemp-lotr/images/boosters/special-09.png",

    "(S)Special-1-3": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Special-4-6": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Special-7-9": "/gemp-lotr/images/boosters/starter_selection.png",

    "TSSealedS1D1": "/gemp-lotr/images/boosters/TSS1D1.png",
    "TSSealedS1D2": "/gemp-lotr/images/boosters/TSS1D2.png",
    "TSSealedS1D3": "/gemp-lotr/images/boosters/TSS1D3.png",
    "TSSealedS2D1": "/gemp-lotr/images/boosters/TSS2D1.png",
    "TSSealedS2D2": "/gemp-lotr/images/boosters/TSS2D2.png",
    "TSSealedS2D3": "/gemp-lotr/images/boosters/TSS2D3.png",
    "TSSealedS3D1": "/gemp-lotr/images/boosters/TSS3D1.png",
    "TSSealedS3D2": "/gemp-lotr/images/boosters/TSS3D2.png",
    "TSSealedS3D3": "/gemp-lotr/images/boosters/TSS3D3.png",

    "(S)TSSealed-S1": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TSSealed-S2": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TSSealed-S3": "/gemp-lotr/images/boosters/starter_selection.png",

    "Expanded": "/gemp-lotr/images/boosters/expanded.png",
    "Wraith": "/gemp-lotr/images/boosters/wraith.png",
    "AgesEnd": "/gemp-lotr/images/boosters/ages_end.png"
};

var Card = Class.extend({
    blueprintId: null,
    foil: null,
    tengwar: null,
    hasWiki: null,
    horizontal: null,
    imageUrl: null,
    zone: null,
    cardId: null,
    owner: null,
    siteNumber: null,
    attachedCards: null,
    errata: null,

    init: function (blueprintId, zone, cardId, owner, siteNumber) {
        this.blueprintId = blueprintId;

        var imageBlueprint = blueprintId;
        var len = imageBlueprint.length;
        this.foil = imageBlueprint.substring(len - 1, len) == "*";
        if (this.foil)
            imageBlueprint = imageBlueprint.substring(0, len - 1);

        var bareBlueprint = imageBlueprint;
        len = bareBlueprint.length;
        this.tengwar = bareBlueprint.substring(len - 1, len) == "T";
        if (this.tengwar)
            bareBlueprint = bareBlueprint.substring(0, len - 1);

        this.hasWiki = this.getFixedImage(imageBlueprint) == null
            && packBlueprints[imageBlueprint] == null;

        this.zone = zone;
        this.cardId = cardId;
        this.owner = owner;
        if (siteNumber)
            this.siteNumber = parseInt(siteNumber);
        this.attachedCards = new Array();
        if (imageBlueprint == "rules") {
            this.imageUrl = "/gemp-lotr/images/rules.png";
        } else {
            if (cardCache[imageBlueprint] != null) {
                var cardFromCache = cardCache[imageBlueprint];
                this.horizontal = cardFromCache.horizontal;
                this.imageUrl = cardFromCache.imageUrl;
                this.errata = cardFromCache.errata;
            } else {
                this.imageUrl = this.getUrlByBlueprintId(bareBlueprint);
                this.horizontal = this.isHorizontal(bareBlueprint);

                var separator = bareBlueprint.indexOf("_");
                var setNo = parseInt(bareBlueprint.substr(0, separator));
                var cardNo = parseInt(bareBlueprint.substr(separator + 1));

                this.errata = this.getErrata(setNo, cardNo) != null;
                cardCache[imageBlueprint] = {
                    imageUrl: this.imageUrl,
                    horizontal: this.horizontal,
                    errata: this.errata
                };
            }
        }
    },

    getFixedImage: function (blueprintId) {
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
    },

    isTengwar: function () {
        return this.tengwar;
    },

    isFoil: function () {
        return this.foil;
    },

    hasErrata: function () {
        var separator = this.blueprintId.indexOf("_");
        var setNo = parseInt(this.blueprintId.substr(0, separator));
        
        if(setNo >= 50 && setNo <= 69)
            return true;
        
        return this.errata;
    },

    isPack: function () {
        return packBlueprints[this.blueprintId] != null;
    },

    isHorizontal: function (blueprintId) {
        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));

        if (setNo == 0)
            return (cardNo == 1 || cardNo == 4 || cardNo == 6 || cardNo == 8);
        if (setNo == 1)
            return (cardNo >= 319 && cardNo <= 363);
        if (setNo == 2)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 3)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 4)
            return (cardNo >= 323 && cardNo <= 363);
        if (setNo == 5)
            return (cardNo >= 118 && cardNo <= 120);
        if (setNo == 6)
            return (cardNo >= 115 && cardNo <= 120);
        if (setNo == 7)
            return (cardNo >= 329 && cardNo <= 363);
        if (setNo == 8)
            return (cardNo >= 117 && cardNo <= 120);
        if (setNo == 10)
            return (cardNo >= 117 && cardNo <= 120);
        if (setNo == 11)
            return (cardNo >= 227 && cardNo <= 266);
        if (setNo == 12)
            return (cardNo >= 185 && cardNo <= 194);
        if (setNo == 13)
            return (cardNo >= 185 && cardNo <= 194);
        if (setNo == 15)
            return (cardNo >= 187 && cardNo <= 194);
        if (setNo == 17)
            return (cardNo >= 145 && cardNo <= 148);
        if (setNo == 18)
            return (cardNo >= 134 && cardNo <= 140);
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

        return false;
    },

    getUrlByBlueprintId: function (blueprintId, ignoreErrata) {
        if (this.getFixedImage(blueprintId) != null)
            return this.getFixedImage(blueprintId);

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

        if (this.isMasterworks(setNo, cardNo))
            cardStr = this.formatSetNo(setNo) + "O0" + (cardNo - this.getMasterworksOffset(setNo));
        else
            cardStr = this.formatCardNo(setNo, cardNo);

        return mainLocation + "LOTR" + cardStr + (this.isTengwar() ? "T" : "") + ".jpg";
    },

    getWikiLink: function () {
        var imageUrl = this.getUrlByBlueprintId(this.blueprintId, true);
        var afterLastSlash = imageUrl.lastIndexOf("/") + 1;
        var countAfterLastSlash = imageUrl.length - 4 - afterLastSlash;
        return "http://lotrtcgwiki.com/wiki/" + imageUrl.substr(afterLastSlash, countAfterLastSlash);
    },

    hasWikiInfo: function () {
        return this.hasWiki;
    },

    formatSetNo: function (setNo) {
        var setNoStr;
        if (setNo < 10)
            setNoStr = "0" + setNo;
        else
            setNoStr = setNo;
        return setNoStr;
    },

    formatCardNo: function (setNo, cardNo) {
        var setNoStr = this.formatSetNo(setNo);

        var cardStr;
        if (cardNo < 10)
            cardStr = setNoStr + "00" + cardNo;
        else if (cardNo < 100)
            cardStr = setNoStr + "0" + cardNo;
        else
            cardStr = setNoStr + "" + cardNo;

        return cardStr;
    },

    getMainLocation: function (setNo, cardNo) {
        return "http://lotrtcgwiki.com/images/";
    },

    getMasterworksOffset: function (setNo) {
        if (setNo == 17)
            return 148;
        if (setNo == 18)
            return 140;
        return 194;
    },

    isMasterworks: function (setNo, cardNo) {
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
    },

    remadeErratas: {
        "0": [7],
        "1": [3, 12, 43, 46, 55, 113, 235, 318],
        "3": [48],
        "4": [236, 237],
        "18": [8, 12, 25, 35, 48, 50, 77, 78, 79, 80, 94, 97]
    },

    getErrata: function (setNo, cardNo) {
        if (this.remadeErratas["" + setNo] != null && $.inArray(cardNo, this.remadeErratas["" + setNo]) != -1)
            return "/gemp-lotr/images/erratas/LOTR" + this.formatCardNo(setNo, cardNo) + ".jpg";
        return null;
    },

    getHeightForWidth: function (width) {
        if (this.horizontal)
            return Math.floor(width * cardScale);
        else
            return Math.floor(width / cardScale);
    },

    getWidthForHeight: function (height) {
        if (this.horizontal)
            return Math.floor(height / cardScale);
        else
            return Math.floor(height * cardScale);
    },

    getWidthForMaxDimension: function (maxDimension) {
        if (this.horizontal)
            return maxDimension;
        else
            return Math.floor(maxDimension * cardScale);
    }
});

function createCardDiv(image, text, foil, tokens, noBorder, errata) {
    var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'>" + ((text != null) ? text : "") + "</div>");

    if (errata) {
        var errataDiv = $("<div class='errataOverlay'><img src='/gemp-lotr/images/errata-vertical.png' width='100%' height='100%'></div>");
        cardDiv.append(errataDiv);
    }

    var foilPresentation = getFoilPresentation();

    if (foil && foilPresentation !== 'none') {
        var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
        var foilDiv = $("<div class='foilOverlay'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
        cardDiv.append(foilDiv);
    }

    if (tokens === undefined || tokens) {
        var overlayDiv = $("<div class='tokenOverlay'></div>");
        cardDiv.append(overlayDiv);
    }
    var borderDiv = $("<div class='borderOverlay'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
    if (noBorder)
        borderDiv.addClass("noBorder");
    cardDiv.append(borderDiv);

    return cardDiv;
}

function getFoilPresentation() {
    var result = $.cookie("foilPresentation");
    if (result === null)
        result = "static";
    if (result === "true")
        result = "animated";
    if (result === "false")
        result = "static";
    return result;
}

function createFullCardDiv(image, foil, horizontal, noBorder) {
    var foilPresentation = getFoilPresentation();

    if (horizontal) {
        var cardDiv = $("<div style='position: relative;width:497px;height:357px;'></div>");
        cardDiv.append("<div style='position:absolute'><img src='" + image + "' width='497' height='357'></div>");

        if (noBorder) {
            var borderDiv = $("<div class='borderOverlay,noBorder' style='position:absolute;width:497px;height:357px;border-width:0px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            cardDiv.append(borderDiv);
        } else {
            var borderDiv = $("<div class='borderOverlay' style='position:absolute;width:465px;height:325px;border-width:16px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            cardDiv.append(borderDiv);
        }

        if (foil && foilPresentation !== 'none') {
            var foilDiv = $("<div class='foilOverlay' style='position:absolute;width:497px;height:357px'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
            cardDiv.append(foilDiv);
        }
    } else {
        var cardDiv = $("<div style='position: relative;width:357px;height:497px;'></div>");
        cardDiv.append("<div style='position:absolute'><img src='" + image + "' width='357' height='497'></div>");

        if (noBorder) {
            var borderDiv = $("<div class='borderOverlay,noBorder' style='position:absolute;width:357px;height:497px;border-width:0px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            cardDiv.append(borderDiv);
        } else {
            var borderDiv = $("<div class='borderOverlay' style='position:absolute;width:325px;height:465px;border-width:16px'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
            cardDiv.append(borderDiv);
        }

        if (foil && foilPresentation !== 'none') {
            var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
            var foilDiv = $("<div class='foilOverlay' style='position:absolute;width:357px;height:497px'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
            cardDiv.append(foilDiv);
        }
    }

    return cardDiv;
}

function createSimpleCardDiv(image) {
    var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'></div>");

    return cardDiv;
}
