var cardCache = {};
var cardScale = 357 / 497;
var fixedImages = {
    // "Forth the Three Hunters!" cards, separate special images
    "15_204": "https://i.lotrtcgpc.net/decipher/LOTR15060D.jpg",
    "15_205": "https://i.lotrtcgpc.net/decipher/LOTR15060E.jpg",
    "15_206": "https://i.lotrtcgpc.net/decipher/LOTR15060G.jpg",
    // Holidays Gandalf
    "15_207": "https://i.lotrtcgpc.net/decipher/LOTR15029H.jpg",
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
    "Random Tengwar": "/gemp-lotr/images/boosters/random_tengwar.png",

    "(S)All Decipher Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)Movie Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)TS Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",

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
    "AgesEnd": "/gemp-lotr/images/boosters/ages_end.png",
    
    "(S)FotR Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)TTT Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)RotK Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)SoG/MD Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)WotR Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)HU Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)Expanded Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)BR/HU/RoS Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",

    "(S)Movie Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Reflections Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Hobbits Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Expanded Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Evil Characters Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)FotR Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TTT Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)RotK Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TS Special Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Movie Special Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)WotR Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)HU Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    
    "(S)PC Promo Art Selection": "/gemp-lotr/images/boosters/pc_promo_selection.png",
    "(S)Masterwork Selection": "/gemp-lotr/images/boosters/masterwork_selection.png",
    
    
    "(S)SotP - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    
    "SotP - Tales & Weather Starter": "/gemp-lotr/images/boosters/V1-starter-tales_weather.png",
    "SotP - Aragorn Signet & Twilight Starter": "/gemp-lotr/images/boosters/V1-starter-aragorn_twilight.png",
    "SotP - Gandalf's Allies & Tentacles Starter": "/gemp-lotr/images/boosters/V1-starter-gandalf_tentacles.png",
    "SotP - Frodo Signet & Sauron Starter": "/gemp-lotr/images/boosters/V1-starter-frodo_sauron.png",
    
    "(R)SotP - Rare": "/gemp-lotr/images/boosters/V1-random-rare.png",
    "(R)SotP - Uncommon": "/gemp-lotr/images/boosters/V1-random-uncommon.png",
    "(R)SotP - Common": "/gemp-lotr/images/boosters/V1-random-common.png",
    
    "(S)SARU - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "SARU - Rohan & Dunland Starter": "/gemp-lotr/images/boosters/saru-rohan-dunland.png",
    "SARU - Dwarf & Evil Men Starter": "/gemp-lotr/images/boosters/saru-dwarf-men.png",
    "SARU - Gondor & Isengard Orc Starter": "/gemp-lotr/images/boosters/saru-gondor-isenorc.png",
    "SARU - Pipeweed & Isengard Uruk Starter": "/gemp-lotr/images/boosters/saru-pipeweed-isenuruks.png",
    "SARU - Smeagol & Urukhai Starter": "/gemp-lotr/images/boosters/saru-smeagol-uruks.png",
    
    "Event Chase Booster": "/gemp-lotr/images/boosters/event_award_booster.png",
    "Placement Random Chase Card Selector": "/gemp-lotr/images/boosters/event_high_placement.png",
    "Countdown to the King Collection": "/gemp-lotr/images/boosters/countdown_collection.png",
    "The Nine Riders Collection": "/gemp-lotr/images/boosters/nine_riders_collection.png",
    "(S)Alt Image Promo Selection": "/gemp-lotr/images/boosters/promo_selection.png",
    
    "Random Common Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Uncommon Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Rare Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Ultra Rare Foil": "/gemp-lotr/images/boosters/random_foil.png",
    
    "(S)SHELBI - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "SHELBI - Aragorn Starter":"/gemp-lotr/images/boosters/shelbi-aragorn.jpg",
    "SHELBI - Gandalf Starter":"/gemp-lotr/images/boosters/shelbi-gandalf.jpg",
    "SHELBI - Gimli Starter":"/gemp-lotr/images/boosters/shelbi-gimli.jpg",
    "SHELBI - Treebeard Starter":"/gemp-lotr/images/boosters/shelbi-treebeard.jpg"
};

class Card {
    blueprintId = null;
    foil = null;
    tengwar = null;
    hasWiki = null;
    horizontal = null;
    imageUrl = null;
    zone = null;
    cardId = null;
    owner = null;
    siteNumber = null;
    attachedCards = null;
    errata = null;

    constructor(blueprintId, zone, cardId, owner, siteNumber) {
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

        this.hasWiki = Card.getFixedImage(imageBlueprint) == null
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
                this.imageUrl = Card.getImageUrl(bareBlueprint, this.tengwar);
                this.horizontal = Card.isHorizontal(bareBlueprint);

                var separator = bareBlueprint.indexOf("_");
                var setNo = parseInt(bareBlueprint.substr(0, separator));
                var cardNo = parseInt(bareBlueprint.substr(separator + 1));

                this.errata = Card.getErrata(setNo, cardNo) != null;
                cardCache[imageBlueprint] = {
                    imageUrl: this.imageUrl,
                    horizontal: this.horizontal,
                    errata: this.errata
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

    isTengwar() {
        return this.tengwar;
    }

    isFoil() {
        return this.foil;
    }

    hasErrata() {
        var separator = this.blueprintId.indexOf("_");
        var setNo = parseInt(this.blueprintId.substr(0, separator));
        
        if(setNo >= 50 && setNo <= 89)
            return true;
        
        return this.errata;
    }

    isPack() {
        return packBlueprints[this.blueprintId] != null;
    }

    static isHorizontal(blueprintId) {
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
            return Math.floor(width * cardScale);
        else
            return Math.floor(width / cardScale);
    }

    getWidthForHeight(height) {
        if (this.horizontal)
            return Math.floor(height / cardScale);
        else
            return Math.floor(height * cardScale);
    }

    getWidthForMaxDimension(maxDimension) {
        if (this.horizontal)
            return maxDimension;
        else
            return Math.floor(maxDimension * cardScale);
    }
}

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
