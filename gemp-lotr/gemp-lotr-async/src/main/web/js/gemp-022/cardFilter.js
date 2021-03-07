var CardFilter = Class.extend({
    clearCollectionFunc: null,
    addCardFunc: null,
    finishCollectionFunc: null,
    getCollectionFunc: null,

    collectionType: null,

    filter: null,
    start: 0,
    count: 18,

    pageDiv: null,
    fullFilterDiv: null,
    filterDiv: null,

    previousPageBut: null,
    nextPageBut: null,
    countSlider: null,

    setSelect: null,
    nameInput: null,
    sortSelect: null,
    raritySelect: null,

    init: function (elem, pageElem, getCollectionFunc, clearCollectionFunc, addCardFunc, finishCollectionFunc) {
        this.getCollectionFunc = getCollectionFunc;
        this.clearCollectionFunc = clearCollectionFunc;
        this.addCardFunc = addCardFunc;
        this.finishCollectionFunc = finishCollectionFunc;

        this.filter = "";

        this.buildUi(elem, pageElem);
    },

    enableDetailFilters: function (enable) {
        $("#culture1").buttonset("option", "disabled", !enable);
        $("#culture2").buttonset("option", "disabled", !enable);
        $("#cardType").prop("disabled", !enable);
        $("#keyword").prop("disabled", !enable);
        $("#race").prop("disabled", !enable);
        $("#itemClass").prop("disabled", !enable);
        $("#phase").prop("disabled", !enable);
    },

    setFilter: function (filter) {
        this.filter = filter;

        this.start = 0;
        this.getCollection();
    },

    setType: function (typeValue) {
        $("#type").val(typeValue);
    },

    buildUi: function (elem, pageElem) {
        var that = this;

        this.pageDiv = $("<div></div>");

        this.previousPageBut = $("<button id='previousPage' style='float: left;'>Previous page</button>").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-w"
            },
            disabled: true
        }).click(
            function () {
                that.disableNavigation();
                that.start -= that.count;
                that.getCollection();
            });

        this.nextPageBut = $("<button id='nextPage' style='float: right;'>Next page</button>").button({
            text: false,
            icons: {
                primary: "ui-icon-circle-triangle-e"
            },
            disabled: true
        }).click(
            function () {
                that.disableNavigation();
                that.start += that.count;
                that.getCollection();
            });

        this.countSlider = $("<div id='countSlider' style='left: 50px; top: 10px; width: 200px;'></div>").slider({
            value: 18,
            min: 4,
            max: 40,
            step: 1,
            disabled: true,
            slide: function (event, ui) {
                that.start = 0;
                that.count = ui.value;
                that.getCollection();
            }
        });

        this.pageDiv.append(this.previousPageBut);
        this.pageDiv.append(this.nextPageBut);
        this.pageDiv.append(this.countSlider);

        pageElem.append(this.pageDiv);

        this.fullFilterDiv = $("<div></div>");
        this.setSelect = $("<select style='width: 130px; font-size: 80%;'>"
            + "<option value='0-32'>All Sets</option>"
            + "<option value='0-19'>Official Sets</option>"
            + "<option value='30-32'>The Hobbit Sets</option>"
            + "<option value='50-69,101-120'>Player's Council Sets</option>"
            + "<option disabled>----------</option>"
            + "<option value='fotr_block'>Fellowship Block</option>"
            + "<option value='ttt_block'>Towers Block</option>"
            + "<option value='king_block'>King Block</option>"
            + "<option value='war_block'>War of the Ring Block</option>"
            + "<option value='hunter_block'>Hunters Block</option>"
            + "<option value='towers_standard'>Towers Standard</option>"
            + "<option value='rotk_sta'>King Standard</option>"
            + "<option value='movie'>Movie Block</option>"
            + "<option value='war_standard'>War of the Ring standard</option>"
            + "<option value='standard'>Standard</option>"
            + "<option value='hobbit_tsr'> The Hobbit: The Short Rest</option>"
            + "<option value='hobbit_tcb'> The Hobbit: The Clouds Burst</option>"
            + "<option disabled>----------</option>"
            + "<option value='pc_fotr_block'>PC Fellowship Block</option>"
            + "<option value='pc_movie'>PC Movie Block</option>"
            + "<option value='pc_expanded'>PC Expanded</option>"
            + "<option disabled>----------</option>"
            + "<option value='0'>00 - Promo</option>"
            + "<option value='1'>01 - The Fellowship of the Ring</option>"
            + "<option value='51'>01E - The Fellowship of the Ring PC Errata</option>"
            + "<option value='2'>02 - Mines of Moria</option>"
            + "<option value='3'>03 - Realms of the Elf-lords</option>"
            + "<option value='4'>04 - The Two Towers</option>"
            + "<option value='5'>05 - Battle of Helm's Deep</option>"
            + "<option value='6'>06 - Ents of Fangorn</option>"
            + "<option value='7'>07 - The Return of the King</option>"
            + "<option value='8'>08 - Siege of Gondor</option>"
            + "<option value='9'>09 - Reflections</option>"
            + "<option value='10'>10 - Mount Doom</option>"
            + "<option value='11'>11 - Shadows</option>"
            + "<option value='12'>12 - Black Rider</option>"
            + "<option value='13'>13 - Bloodlines</option>"
            + "<option value='14'>14 - Expanded Middle-earth</option>"
            + "<option value='15'>15 - The Hunters</option>"
            + "<option value='16'>16 - The Wraith Collection</option>"
            + "<option value='17'>17 - Rise of Saruman</option>"
            + "<option value='18'>18 - Treachery & Deceit</option>"
            + "<option value='19'>19 - Ages End</option>"
            + "<option value='101'>1+ - PC Fellowship Supplement</option>"
            + "<option value='30'>30 - The Hobbit: Main Deck</option>"
            + "<option value='31'>31 - The Hobbit: Expansion 1</option>"
            + "<option value='32'>32 - The Hobbit: Expansion 2</option>"
            + "</select>");
        this.nameInput = $("<input type='text' placeholder='Card name' value='' style='width: 110px; font-size: 70%;'>");
        this.sortSelect = $("<select style='width: 80px; font-size: 80%;'>"
            + "<option value=''>Sort by:</option>"
            + "<option value='name'>Name</option>"
            + "<option value='twilight,name'>Twilight</option>"
            + "<option value='siteNumber,name'>Site number</option>"
            + "<option value='strength,name'>Strength</option>"
            + "<option value='vitality,name'>Vitality</option>"
            + "<option value='cardType,name'>Card type</option>"
            + "<option value='culture,name'>Culture</option>"
            + "</select>");
        this.raritySelect = $("<select style='width: 40px; font-size: 80%;'>"
            + "<option value=''>Rarity:</option>"
            + "<option value='R'>Rare</option>"
            + "<option value='U'>Uncommon</option>"
            + "<option value='C'>Common</option>"
            + "<option value='A'>Alternate Image</option>"
            + "<option value='P'>Promo</option>"
            + "<option value='X'>Rare+</option>"
            + "<option value='S'>Fixed</option>"
            + "<option value='C,U,P,S'>Poorman's</option>"
            + "</select>");

        this.fullFilterDiv.append(this.setSelect);
        this.fullFilterDiv.append(this.nameInput);
        this.fullFilterDiv.append(this.sortSelect);
        this.fullFilterDiv.append(this.raritySelect);

        elem.append(this.fullFilterDiv);

        this.filterDiv = $("<div></div>");

        this.filterDiv.append("<div id='culture1'>"
            + "<input type='checkbox' id='DWARVEN'/><label for='DWARVEN' id='labelDWARVEN'><img src='images/cultures/dwarven.png'/></label>"
            + "<input type='checkbox' id='ELVEN'/><label for='ELVEN' id='labelELVEN'><img src='images/cultures/elven.png'/></label>"
            + "<input type='checkbox' id='GANDALF'/><label for='GANDALF' id='labelGANDALF'><img src='images/cultures/gandalf.png'/></label>"
            + "<input type='checkbox' id='GONDOR'/><label for='GONDOR' id='labelGONDOR'><img src='images/cultures/gondor.png'/></label>"
            + "<input type='checkbox' id='ROHAN'/><label for='ROHAN' id='labelROHAN'><img src='images/cultures/rohan.png'/></label>"
            + "<input type='checkbox' id='SHIRE'/><label for='SHIRE' id='labelSHIRE'><img src='images/cultures/shire.png'/></label>"
            + "<input type='checkbox' id='GOLLUM'/><label for='GOLLUM' id='labelGOLLUM'><img src='images/cultures/gollum.png'/></label>"
            + "</div>");
        this.filterDiv.append("<div id='culture2'>"
            + "<input type='checkbox' id='DUNLAND'/><label for='DUNLAND' id='labelDUNLAND'><img src='images/cultures/dunland.png'/></label>"
            + "<input type='checkbox' id='ISENGARD'/><label for='ISENGARD' id='labelISENGARD'><img src='images/cultures/isengard.png'/></label>"
            + "<input type='checkbox' id='MEN'/><label for='MEN' id='labelMEN'><img src='images/cultures/men.png'/></label>"
            + "<input type='checkbox' id='MORIA'/><label for='MORIA' id='labelMORIA'><img src='images/cultures/moria.png'/></label>"
            + "<input type='checkbox' id='ORC'/><label for='ORC' id='labelORC'><img src='images/cultures/orc.png'/></label>"
            + "<input type='checkbox' id='RAIDER'/><label for='RAIDER' id='labelRAIDER'><img src='images/cultures/raider.png'/></label>"
            + "<input type='checkbox' id='SAURON'/><label for='SAURON' id='labelSAURON'><img src='images/cultures/sauron.png'/></label>"
            + "<input type='checkbox' id='URUK_HAI'/><label for='URUK_HAI' id='labelURUK_HAI'><img src='images/cultures/uruk_hai.png'/></label>"
            + "<input type='checkbox' id='WRAITH'/><label for='WRAITH' id='labelWRAITH'><img src='images/cultures/wraith.png'/></label>"
            //Additional Hobbit Draft cultures
            + "<input type='checkbox' id='ESGAROTH'/><label for='ESGAROTH' id='labelESGAROTH'><img src='images/cultures/esgaroth.png'/></label>"
            + "<input type='checkbox' id='GUNDABAD'/><label for='GUNDABAD' id='labelGUNDABAD'><img src='images/cultures/gundabad.png'/></label>"
            //+ "<input type='checkbox' id='MIRKWOOD'/><label for='MIRKWOOD' id='labelMIRKWOOD'><img src='images/cultures/mirkwood.png'/></label>"
            //+ "<input type='checkbox' id='SMAUG'/><label for='SMAUG' id='labelSMAUG'><img src='images/cultures/smaug.png'/></label>"
            //+ "<input type='checkbox' id='SPIDER'/><label for='SPIDER' id='labelSPIDER'><img src='images/cultures/spider.png'/></label>"
            //+ "<input type='checkbox' id='TROLL'/><label for='TROLL' id='labelTROLL'><img src='images/cultures/troll.png'/></label>"
            + "</div>");

        var combos = $("<div></div>");

        combos.append(" <select id='cardType' style='font-size: 80%;'>"
            + "<option value=''>All Card Types</option>"
            + "<option value='COMPANION,ALLY,MINION'>Characters</option>"
            + "<option value='POSSESSION,ARTIFACT'>Items</option>"
            + "<option value='SITE'>Sites</option>"
            + "<option value='ALLY'>Allies</option>"
            + "<option value='ARTIFACT'>Artifacts</option>"
            + "<option value='COMPANION'>Companions</option>"
            + "<option value='CONDITION'>Conditions</option>"
            + "<option value='EVENT'>Events</option>"
            + "<option value='FOLLOWER'>Followers</option>"
            + "<option value='MINION'>Minions</option>"
            + "<option value='POSSESSION'>Possessions</option>"
            + "</select>");
        combos.append(" <select id='keyword' style='font-size: 80%;'>"
            + "<option value=''>No keyword filtering</option>"
            + "<option value='ARCHER'>Archer</option>"
            + "<option value='BATTLEGROUND'>Battleground</option>"
            + "<option value='BESIEGER'>Besieger</option>"
            + "<option value='CORSAIR'>Corsair</option>"
            + "<option value='DWELLING'>Dwelling</option>"
            + "<option value='EASTERLING'>Easterling</option>"
            + "<option value='ENDURING'>Enduring</option>"
            + "<option value='ENGINE'>Engine</option>"
            + "<option value='FIERCE'>Fierce</option>"
            + "<option value='FOREST'>Forest</option>"
            + "<option value='FORTIFICATION'>Fortification</option>"
            + "<option value='HUNTER'>Hunter</option>"
            + "<option value='KNIGHT'>Knight</option>"
            + "<option value='LURKER'>Lurker</option>"
            + "<option value='MACHINE'>Machine</option>"
            + "<option value='MARSH'>Marsh</option>"
            + "<option value='MOUNTAIN'>Mountain</option>"
            + "<option value='MUSTER'>Muster</option>"
            + "<option value='PIPEWEED'>Pipeweed</option>"
            + "<option value='PLAINS'>Plains</option>"
            + "<option value='RANGER'>Ranger</option>"
            + "<option value='RING_BOUND'>Ring-bound</option>"
            + "<option value='RIVER'>River</option>"
            + "<option value='SEARCH'>Search</option>"
            + "<option value='SOUTHRON'>Southron</option>"
            + "<option value='SPELL'>Spell</option>"
            + "<option value='STEALTH'>Stealth</option>"
            + "<option value='TALE'>Tale</option>"
            + "<option value='TENTACLE'>Tentacle</option>"
            + "<option value='TRACKER'>Tracker</option>"
            + "<option value='TWILIGHT'>Twilight</option>"
            + "<option value='UNDERGROUND'>Underground</option>"
            + "<option value='UNHASTY'>Unhasty</option>"
            + "<option value='VALIANT'>Valiant</option>"
            + "<option value='VILLAGER'>Villager</option>"
            + "<option value='WARG_RIDER'>Warg-rider</option>"
            + "<option value='WEATHER'>Weather</option>"
            //Additional Hobbit Draft keyword
            + "<option value='WISE'>Wise</option>"
            + "</select>");
        combos.append(" <select id='type' style='font-size: 80%'>"
            + "<option value=''>All types</option>"
            + "<option value='pack'>Packs</option>"
            + "<option value='card'>Cards</option>"
            + "<option value='foil'>Foils</option>"
            + "<option value='nonFoil'>Non-foils</option>"
            + "<option value='tengwar'>Tengwar</option>"
            + "</select>");

        combos.append(" <select id ='race' style='font-size: 80%'>"
            + "<option value=''>All races</option>"
            + "<option value='BALROG'>Balrog</option>"
            + "<option value='CREATURE'>Creature</option>"
            + "<option value='DWARF'>Dwarf</option>"
            + "<option value='ELF'>Elf</option>"
            + "<option value='ENT'>Ent</option>"
            + "<option value='HALF_TROLL'>Half-troll</option>"
            + "<option value='HOBBIT'>Hobbit</option>"
            + "<option value='MAIA'>Maia</option>"
            + "<option value='MAN'>Man</option>"
            + "<option value='NAZGUL'>Nazgul</option>"
            + "<option value='ORC'>Orc</option>"
            + "<option value='SPIDER'>Spider</option>"
            + "<option value='TREE'>Tree</option>"
            + "<option value='TROLL'>Troll</option>"
            + "<option value='URUK_HAI'>Uruk-hai</option>"
            + "<option value='WIZARD'>Wizard</option>"
            + "<option value='WRAITH'>Wraith</option>"
            + "<option value='DRAGON'>Dragon</option>"
            + "<option value='EAGLE'>Eagle</option>"
            + "</select>");
        combos.append(" <select id ='itemClass' style='font-size: 80%'>"
            + "<option value=''>All classes</option>"
            + "<option value='ARMOR'>Armor</option>"
            + "<option value='BOX'>Box</option>"
            + "<option value='BRACERS'>Bracers</option>"
            + "<option value='BROOCH'>Brooch</option>"
            + "<option value='CLOAK'>Cloak</option>"
            + "<option value='GAUNTLETS'>Gauntlets</option>"
            + "<option value='HAND_WEAPON'>Hand Weapon</option>"
            + "<option value='HELM'>Helm</option>"
            + "<option value='HORN'>Horn</option>"
            + "<option value='MOUNT'>Mount</option>"
            + "<option value='PALANTIR'>Palantir</option>"
            + "<option value='PHIAL'>Phial</option>"
            + "<option value='PIPE'>Pipe</option>"
            + "<option value='RANGED_WEAPON'>Ranged Weapon</option>"
            + "<option value='RING'>Ring</option>"
            + "<option value='SHIELD'>Shield</option>"
            + "<option value='STAFF'>Staff</option>"
            + "<option value='CLASSLESS'>Classless</option>"
            + "</select>");
        combos.append(" <select id ='phase' style='font-size: 80%'>"
            + "<option value=''>All Phases</option>"
            + "<option value='FELLOWSHIP'>Fellowship</option>"
            + "<option value='SHADOW'>Shadow</option>"
            + "<option value='MANEUVER'>Maneuver</option>"
            + "<option value='ARCHERY'>Archery</option>"
            + "<option value='ASSIGNMENT'>Assignment</option>"
            + "<option value='SKIRMISH'>Skirmish</option>"
            + "<option value='REGROUP'>Regroup</option>"
            + "<option value='RESPONSE'>Response</option>"
            + "</select>");
        this.filterDiv.append(combos);

        elem.append(this.filterDiv);

        $("#culture1").buttonset();
        $("#culture2").buttonset();

        var fullFilterChanged = function () {
            that.start = 0;
            that.getCollection();
            return true;
        };

        this.setSelect.change(fullFilterChanged);
        this.nameInput.change(fullFilterChanged);
        this.sortSelect.change(fullFilterChanged);
        this.raritySelect.change(fullFilterChanged);

        var filterOut = function () {
            that.filter = that.calculateNormalFilter();
            that.start = 0;
            that.getCollection();
            return true;
        };
        
        //Hide dynamic filters by default
        $("#race").hide();
        $("#itemClass").hide();
        $("#phase").hide();
        
        var changeDynamicFilters = function () {
            var cardType = $("#cardType option:selected").prop("value");
            if (cardType.includes("COMPANION") || cardType.includes("ALLY") || cardType.includes("MINION")) {
                $("#race").show();
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").hide();
                $("#phase").val("");
            } else if (cardType.includes("POSSESSION") || cardType.includes("ARTIFACT")) {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").show();
                $("#phase").hide();
                $("#phase").val("");
            } else if (cardType.includes("EVENT")) {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").show();
            } else {
                $("#race").hide();
                $("#race").val("");
                $("#itemClass").hide();
                $("#itemClass").val("");
                $("#phase").hide();
                $("#phase").val("")
            }
            that.filter = that.calculateNormalFilter();
            that.start = 0;
            that.getCollection();
            return true;
            
        };

        $("#cardType").change(changeDynamicFilters);
        $("#keyword").change(filterOut);
        $("#type").change(filterOut);
        $("#race").change(filterOut);
        $("#itemClass").change(filterOut);
        $("#phase").change(filterOut);

        //Additional Hobbit Draft labels
        $("#labelDWARVEN,#labelELVEN,#labelGANDALF,#labelGONDOR,#labelROHAN,#labelSHIRE,#labelGOLLUM,#labelDUNLAND,#labelISENGARD,#labelMEN,#labelMORIA,#labelORC,#labelRAIDER,#labelSAURON,#labelURUK_HAI,#labelWRAITH,#labelESGAROTH,#labelGUNDABAD").click(filterOut);
    },

    layoutUi: function (x, y, width, height) {
        this.pageDiv.css({position: "absolute", left: x, top: y, width: width, height: 34});
        this.countSlider.css({width: width - 100});
        this.fullFilterDiv.css({position: "absolute", left: x, top: y + 34, width: width, height: 34});
        this.filterDiv.css({position: "absolute", left: x, top: y + 68, width: width, height: 80});
    },

    layoutPageUi: function (x, y, width) {
        this.pageDiv.css({left: x, top: y, width: width, height: 36});
        this.countSlider.css({width: width - 100});
    },

    disableNavigation: function () {
        this.previousPageBut.button("option", "disabled", true);
        this.nextPageBut.button("option", "disabled", true);
        this.countSlider.button("option", "disabled", true);
    },

    calculateNormalFilter: function () {
        var cultures = new Array();
        $("label", $("#culture1")).each(
            function () {
                if ($(this).hasClass("ui-state-active"))
                    cultures.push($(this).prop("id").substring(5));
            });
        $("label", $("#culture2")).each(
            function () {
                if ($(this).hasClass("ui-state-active"))
                    cultures.push($(this).prop("id").substring(5));
            });

        var cardType = $("#cardType option:selected").prop("value");
        if (cardType == "")
            cardType = "";
        else
            cardType = "cardType:" + cardType + " ";

        var keyword = $("#keyword option:selected").prop("value");
        if (keyword != "")
            keyword = " keyword:" + keyword;

        var type = $("#type option:selected").prop("value");
        if (type != "")
            type = " type:" + type;

        var race = $("#race option:selected").prop("value");
        if (race != "")
            race = " race:" + race;

        var itemClass = $("#itemClass option:selected").prop("value");
        if (itemClass != "")
            itemClass = " itemClass:" + itemClass

        var phase = $("#phase option:selected").prop("value");
        if (phase != "")
            phase = " phase:" + phase;

        if (cultures.length > 0)
            return cardType + " culture:" + cultures + keyword + type + race + itemClass + phase;
        else
            return cardType + keyword + type + race + itemClass + phase;
    },

    calculateFullFilterPostfix: function () {
        var setNo = $("option:selected", this.setSelect).prop("value");
        if (setNo != "")
            setNo = " set:" + setNo;

        var sort = $("option:selected", this.sortSelect).prop("value");
        if (sort != "")
            sort = " sort:" + sort;

        var cardName = this.nameInput.val();
        var cardNameElems = cardName.split(" ");
        cardName = "";
        for (var i = 0; i < cardNameElems.length; i++)
            cardName += " name:" + cardNameElems[i];

        var rarity = $("option:selected", this.raritySelect).prop("value");
        if (rarity != "")
            rarity = " rarity:" + rarity;

        return setNo + sort + cardName + rarity;
    },

    getCollection: function () {
        var that = this;
        this.getCollectionFunc((this.filter + this.calculateFullFilterPostfix()).trim(), this.start, this.count, function (xml) {
            that.displayCollection(xml);
        });
    },

    displayCollection: function (xml) {
        log(xml);
        var root = xml.documentElement;

        this.clearCollectionFunc(root);

        var packs = root.getElementsByTagName("pack");
        for (var i = 0; i < packs.length; i++) {
            var packElem = packs[i];
            var blueprintId = packElem.getAttribute("blueprintId");
            var count = packElem.getAttribute("count");
            this.addCardFunc(packElem, "pack", blueprintId, count);
        }

        var cards = root.getElementsByTagName("card");
        for (var i = 0; i < cards.length; i++) {
            var cardElem = cards[i];
            var blueprintId = cardElem.getAttribute("blueprintId");
            var count = cardElem.getAttribute("count");
            this.addCardFunc(cardElem, "card", blueprintId, count);
        }

        this.finishCollectionFunc();

        $("#previousPage").button("option", "disabled", this.start == 0);
        var cnt = parseInt(root.getAttribute("count"));
        $("#nextPage").button("option", "disabled", (this.start + this.count) >= cnt);
        $("#countSlider").slider("option", "disabled", false);
    }
});
