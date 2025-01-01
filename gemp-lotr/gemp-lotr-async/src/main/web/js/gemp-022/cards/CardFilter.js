var CardFilter = Class.extend({
	clearCollectionFunc: null,
	addCardFunc: null,
	finishCollectionFunc: null,
	getCollectionFunc: null,

	collectionType: null,

	filter: null,
	start: 0,
	count: 12,

	pageDiv: null,
	navigationDiv: null,
	advancedFilterDiv: null,
	filtersDiv: null,
	fullFilterDiv: null,
	filterDiv: null,
	collectionDiv: null,

	previousPageBut: null,
	nextPageBut: null,
	countSlider: null,

	sideLabel:null,
	sideSelect:null,
	blockLabel:null,
	blockSelect: null,
	setLabel:null,
	setSelect: null,
	cardTypeLabel:null,
	cardTypeSelect:null,
	keywordLabel:null,
	keywordSelect:null,
	raceLabel:null,
	raceSelect:null,
	itemClassLabel:null,
	itemClassSelect:null,
	phaseLabel:null,
	phaseSelect:null,
	rarityLabel:null,
	raritySelect:null,
	
	nameLabel: null,
	nameInput: null,
	gametextLabel: null,
	gametextInput: null,
	
	productLabel:null,
	productSelect:null,
	sortLabel:null,
	sortSelect:null,
	
	twilightLabel:null,
	twilightCompareSelect:null,
	twilightValueInput:null,
	strengthLabel:null,
	strengthCompareSelect:null,
	strengthValueInput:null,
	vitalityLabel:null,
	vitalityCompareSelect:null,
	vitalityValueInput:null,
	signetLabel:null,
	signetSelect:null,
	
	siteNumberLabel:null,
	siteNumberCompareSelect:null,
	siteNumberValueInput:null,
	resistanceLabel:null,
	resistanceCompareSelect:null,
	resistanceValueInput:null,
	
	initialStartup: false,
	pauseUpdates: false,
	
	currentFormat: null,
	blocks: null,
	overrideFilter: "",
	

	init: function (pageElem, getCollectionFunc, clearCollectionFunc, addCardFunc, finishCollectionFunc) {
		this.initialStartup = false;
		
		this.getCollectionFunc = getCollectionFunc;
		this.clearCollectionFunc = clearCollectionFunc;
		this.addCardFunc = addCardFunc;
		this.finishCollectionFunc = finishCollectionFunc;

		this.filter = "";

		this.buildUi(pageElem);
		this.filter = this.calculateDeckFilter();
	},
	
	setFilterOverride(override) {
		console.log("Override from deckbuilder");
		this.initialStartup = true;
		this.overrideFilter = override; 
		this.start = 0;
		$("#resetAllFiltersButton").click();
		this.filterChanged(); 
	},

	setType: function (typeValue) {
		$("#productSelect").val(typeValue);
	},

	buildUi: function (pageElem) {
		var that = this;

		this.pageDiv = $("<div id='filter-main' style='display:flex;flex-direction:column;align-items:stretch;'></div>");
		this.navigationDiv = $("<div id='card-navigation' style='display: flex; flex-direction: row; gap: 2px; align-items: center;'></div>");
		this.pageDiv.append(this.navigationDiv);
		pageElem.append(this.pageDiv);

		this.previousPageBut = $("<button id='previousPage' class='navigation-butt'></button>").button({
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

		this.nextPageBut = $("<button id='nextPage' class='navigation-butt'></button>").button({
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

		this.countSlider = $("<div id='countSlider' style='flex-grow:1;'></div>").slider({
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

		this.navigationDiv.append(this.previousPageBut);
		this.navigationDiv.append(this.countSlider);
		this.navigationDiv.append(this.nextPageBut);
		
		this.fullFilterDiv = $("<div id='filter-inputs' style='display:flex;flex-wrap:wrap;'></div>");
		
		this.sideLabel = $("<label for='sideSelect' class='filterLabel'>Side:</label>");
		this.sideSelect = $("<select id='sideSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='SHADOW'>Shadow</option>"
			+ "<option value='FREE_PEOPLES'>Free Peoples</option>"
			+ "<option value='NONE'>Neither</option>"
			+ "</select>");
		
		this.blockLabel = $("<label for='blockSelect' class='filterLabel'>Block:</label>");
		this.blockSelect = $("<select id='blockSelect' class='filterInput'>"
			+ "<option value='0-33,50-200' selected='selected'>Any</option>"
			+ "</select>");
		
		this.setLabel = $("<label for='setSelect' class='filterLabel'>Set:</label>");
		this.setSelect = $("<select id='setSelect' class='filterInput'>"
			+ "<option value='0-34,50-200' selected='selected'>Any</option>"
			+ "</select>");

		this.cardTypeLabel = $("<label for='cardTypeSelect' class='filterLabel'>Card&nbsp;Type:</label>");
		this.cardTypeSelect = $("<select id='cardTypeSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
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
		
		this.keywordLabel = $("<label for='keywordSelect' class='filterLabel'>Keyword:</label>");
		this.keywordSelect = $("<select id='keywordSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
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
		
		this.raceLabel = $("<label for='raceSelect' class='filterLabel'>Race:</label>");
		this.raceSelect = $("<select id='raceSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='BALROG'>Balrog</option>"
			+ "<option value='CREATURE'>Creature</option>"
			+ "<option value='CROW'>Crow</option>"
			+ "<option value='DRAGON'>Dragon</option>"
			+ "<option value='DWARF'>Dwarf</option>"
			+ "<option value='EAGLE'>Eagle</option>"
			+ "<option value='ELF'>Elf</option>"
			+ "<option value='ENT'>Ent</option>"
			+ "<option value='GIANT'>Giant</option>"
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
			+ "<option value='WARG'>Warg</option>"
			+ "<option value='WIZARD'>Wizard</option>"
			+ "<option value='WRAITH'>Wraith</option>"
			+ "</select>");
		
		this.itemClassLabel = $("<label for='itemClassSelect' class='filterLabel'>Item Class:</label>");
		this.itemClassSelect = $("<select id='itemClassSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='CLASSLESS'>Classless</option>"
			+ "<option value='SUPPORT_AREA'>Support Area</option>"
			+ "<option value='ARMOR'>Armor</option>"
			+ "<option value='BOX'>Box</option>"
			+ "<option value='BRACERS'>Bracers</option>"
			+ "<option value='BROOCH'>Brooch</option>"
			+ "<option value='CLOAK'>Cloak</option>"
			+ "<option value='GAUNTLETS'>Gauntlets</option>"
			+ "<option value='HAND_WEAPON'>Hand Weapon</option>"
			+ "<option value='HELM'>Helm</option>"
			+ "<option value='MOUNT'>Mount</option>"
			+ "<option value='PALANTIR'>Palantir</option>"
			+ "<option value='PHIAL'>Phial</option>"
			+ "<option value='PIPE'>Pipe</option>"
			+ "<option value='PONY'>Pony</option>"
			+ "<option value='RANGED_WEAPON'>Ranged Weapon</option>"
			+ "<option value='RING'>Ring</option>"
			+ "<option value='SHIELD'>Shield</option>"
			+ "<option value='STAFF'>Staff</option>"
			
			+ "</select>");
		
		
		this.phaseLabel = $("<label for='phaseSelect' class='filterLabel'>Phase:</label>");
		this.phaseSelect = $("<select id='phaseSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='FELLOWSHIP'>Fellowship</option>"
			+ "<option value='SHADOW'>Shadow</option>"
			+ "<option value='MANEUVER'>Maneuver</option>"
			+ "<option value='ARCHERY'>Archery</option>"
			+ "<option value='ASSIGNMENT'>Assignment</option>"
			+ "<option value='SKIRMISH'>Skirmish</option>"
			+ "<option value='REGROUP'>Regroup</option>"
			+ "<option value='RESPONSE'>Response</option>"
			+ "</select>");
	   

		this.nameLabel = $("<label for='nameInput' class='filterLabel'>Name&nbsp;contains:</label>");
		this.nameInput = $("<input id='nameInput' type='text' maxlength='100' class='filterInput' style='width: 250px;'>");
		
		this.gametextLabel = $("<label for='gametextInput' class='filterLabel'>Game&nbsp;Text&nbsp;contains:</label>");
		this.gametextInput = $("<input id='gametextInput' type='text' maxlength='100' class='filterInput' style='width: 250px;'>");
		
		this.generateNumericComparator("twilight", "Twilight");
		this.generateNumericComparator("strength", "Strength");
		this.generateNumericComparator("vitality", "Vitality");
		this.generateNumericComparator("siteNumber", "Site Number");
		this.generateNumericComparator("resistance", "Resistance");
		
		this.signetLabel = $("<label for='signetSelect' class='filterLabel'>Signet:</label>");
		this.signetSelect = $("<select id='signetSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='ARAGORN'>Aragorn</option>"
			+ "<option value='FRODO'>Frodo</option>"
			+ "<option value='GANDALF'>Gandalf</option>"
			+ "<option value='THEODEN'>Theoden</option>"
			+ "</select>");
		
		this.productLabel = $("<label for='productSelect' class='filterLabel'>Product:</label>");
		this.productSelect = $("<select id='productSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='pack'>Packs</option>"
			+ "<option value='card'>Cards</option>"
			+ "<option value='foil'>Foils</option>"
			+ "<option value='nonFoil'>Non-foils</option>"
			+ "<option value='tengwar'>Tengwar</option>"
			+ "</select>");
		
		this.sortLabel = $("<label for='sortSelect' class='filterLabel'>Sort by:</label>");
		this.sortSelect = $("<select id='sortSelect' class='filterInput'>"
			+ "<option value='name' selected='selected'>Name</option>"
			+ "<option value='collinfo,set' >Collector's Info</option>"
			+ "<option value='twilight,name'>Twilight</option>"
			+ "<option value='siteNumber,name'>Site Number</option>"
			+ "<option value='strength,name'>Strength</option>"
			+ "<option value='vitality,name'>Vitality</option>"
			+ "<option value='cardType,name'>Card Type</option>"
			+ "<option value='culture,name'>Culture</option>"
			+ "</select>");
		
		this.rarityLabel = $("<label for='raritySelect' class='filterLabel'>Rarity:</label>");
		this.raritySelect = $("<select id='raritySelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='R'>Rare</option>"
			+ "<option value='U'>Uncommon</option>"
			+ "<option value='C'>Common</option>"
			+ "<option value='A'>Alternate Image</option>"
			+ "<option value='P'>Promo</option>"
			+ "<option value='X'>Rare+</option>"
			+ "<option value='S'>Fixed</option>"
			+ "<option value='C,U,P,S'>Poorman's</option>"
			+ "</select>");
		
		this.resetAdvancedFiltersButton = $("<button id='resetAdvancedFiltersButton' class='ui-button-icon-primary'>Reset advanced filters only</button>").button();
		this.resetAllFiltersButton = $("<button id='resetAllFiltersButton' class='ui-button-icon-primary'>Reset all filters</button>").button();
		
		var genFilterTD = function(element, params) {
			if(params !== undefined) 
				return '<td ' + params + '>' + element[0].outerHTML + '</td>';
			return '<td>' + element[0].outerHTML + '</td>';
		};
		
		var genFilterTDPair = function(...elements) {
			let cell = '<td>';
			elements.forEach(function(item) {
			   cell += item[0].outerHTML; 
			});
			return cell + "</td>"
		};
		
		var genFilterTR = function(...elements) {
			let row = '<tr>';
			elements.forEach(function(item) {
			   row += genFilterTD(item); 
			});
			return row + "</tr>"
		};
		
		var genFilterTRPair = function(label, field) {
			let row = '<tr>';
			row += genFilterTD(label);
			row += genFilterTD(field, "colspan='3'");
			return row + "</tr>"
		};
		
		var genFilterTRSolo = function(message, wrapperClass) {
			return '<tr><td colspan="4"><div class="' + wrapperClass + '">' + message + '</div></td></tr>'
		};
		
		var filterTable = $("<table id='filterTable' />");
		filterTable.append(genFilterTR(this.productLabel, this.productSelect, this.rarityLabel, this.raritySelect));
		filterTable.append(genFilterTR(this.blockLabel, this.blockSelect, this.setLabel, this.setSelect));
		filterTable.append(genFilterTR(this.sideLabel, this.sideSelect, this.sortLabel, this.sortSelect));
		filterTable.append(genFilterTRPair(this.cardTypeLabel, this.cardTypeSelect));
		
		filterTable.append(genFilterTRSolo("Advanced filters (<a target='_blank' href='https://wiki.lotrtcgpc.net/wiki/Special:RunQuery/CardSearch'>See more on the Wiki.</a>)", "cardFilterText"));
		filterTable.append(genFilterTRPair(this.nameLabel, this.nameInput));
		filterTable.append(genFilterTRPair(this.gametextLabel, this.gametextInput));

		filterTable.append(genFilterTR(this.keywordLabel, this.keywordSelect, this.phaseLabel, this.phaseSelect));
		filterTable.append(genFilterTR(this.raceLabel, this.raceSelect, this.itemClassLabel, this.itemClassSelect));
		
		filterTable.append("<tr>" + genFilterTD(this.twilightLabel)
								  + genFilterTDPair(this.twilightCompareSelect, this.twilightValueInput)
								  + genFilterTD(this.siteNumberLabel)
								  + genFilterTDPair(this.siteNumberCompareSelect, this.siteNumberValueInput)
								  + "</tr>");
		
		filterTable.append("<tr>" + genFilterTD(this.strengthLabel)
								  + genFilterTDPair(this.strengthCompareSelect, this.strengthValueInput)
								  + genFilterTD(this.signetLabel)
								  + genFilterTD(this.signetSelect)
								  + "</tr>");
		
		filterTable.append("<tr>" + genFilterTD(this.vitalityLabel)
								  + genFilterTDPair(this.vitalityCompareSelect, this.vitalityValueInput)
								  + genFilterTD(this.resistanceLabel)
								  + genFilterTDPair(this.resistanceCompareSelect, this.resistanceValueInput)
								  + "</tr>");

		filterTable.append(genFilterTRSolo(this.resetAdvancedFiltersButton[0].outerHTML + this.resetAllFiltersButton[0].outerHTML, "cardFilterText flex-horiz"));


		this.fullFilterDiv.append(filterTable);
		this.pageDiv.append(this.fullFilterDiv);

		this.filterDiv = $("<div id='culture-buttons'></div>");

		this.filterDiv.append("<div id='culture_freeps_base1'>"
			+ "<input type='checkbox' id='DWARVEN'/><label for='DWARVEN' id='labelDWARVEN' class='culture'><img src='images/cultures/dwarven.svg'/></label>"
			+ "<input type='checkbox' id='ELVEN'/><label for='ELVEN' id='labelELVEN' class='culture'><img src='images/cultures/elven.svg'/></label>"
			+ "<input type='checkbox' id='GANDALF'/><label for='GANDALF' id='labelGANDALF' class='culture'><img src='images/cultures/gandalf.svg'/></label>"
			+ "<input type='checkbox' id='GONDOR'/><label for='GONDOR' id='labelGONDOR' class='culture'><img src='images/cultures/gondor.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_freeps_movie1'>"
			+ "<input type='checkbox' id='ROHAN'/><label for='ROHAN' id='labelROHAN' class='culture'><img src='images/cultures/rohan.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_freeps_base2'>"
			+ "<input type='checkbox' id='SHIRE'/><label for='SHIRE' id='labelSHIRE' class='culture'><img src='images/cultures/shire.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_freeps_movie2'>"
			+ "<input type='checkbox' id='GOLLUM'/><label for='GOLLUM' id='labelGOLLUM' class='culture'><img src='images/cultures/gollum.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_shadow_movie1'>"
			+ "<input type='checkbox' id='DUNLAND'/><label for='DUNLAND' id='labelDUNLAND' class='culture'><img src='images/cultures/dunland.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_shadow_base1'>"
      + "<input type='checkbox' id='ISENGARD'/><label for='ISENGARD' id='labelISENGARD' class='culture'><img src='images/cultures/isengard.svg'/></label>"
			+ "<input type='checkbox' id='MORIA'/><label for='MORIA' id='labelMORIA' class='culture'><img src='images/cultures/moria.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_shadow_movie2'>"
			+ "<input type='checkbox' id='RAIDER'/><label for='RAIDER' id='labelRAIDER' class='culture'><img src='images/cultures/raider.svg'/></label>"		                      
		);
		
		this.filterDiv.append("<div id='culture_shadow_base2'>"
		  + "<input type='checkbox' id='WRAITH'/><label for='WRAITH' id='labelWRAITH' class='culture'><img src='images/cultures/ringwraith.svg'/></label>"
			+ "<input type='checkbox' id='SAURON'/><label for='SAURON' id='labelSAURON' class='culture'><img src='images/cultures/sauron.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_shadows'>"
			+ "<input type='checkbox' id='MEN'/><label for='MEN' id='labelMEN' class='culture'><img src='images/cultures/men.svg'/></label>"
			+ "<input type='checkbox' id='ORC'/><label for='ORC' id='labelORC' class='culture'><img src='images/cultures/orc.svg'/></label>"
			+ "<input type='checkbox' id='URUK_HAI'/><label for='URUK_HAI' id='labelURUK_HAI' class='culture'><img src='images/cultures/uruk.svg'/></label>"
		);
		
		this.filterDiv.append("<div id='culture_hobbit'>"
			+ "<input type='checkbox' id='ESGAROTH'/><label for='ESGAROTH' id='labelESGAROTH' ><img src='images/cultures/esgaroth.png'/></label>"
			+ "<input type='checkbox' id='GUNDABAD'/><label for='GUNDABAD' id='labelGUNDABAD' ><img src='images/cultures/gundabad.png'/></label>"
			+ "<input type='checkbox' id='MIRKWOOD'/><label for='MIRKWOOD' id='labelMIRKWOOD' ><img src='images/cultures/mirkwood.png'/></label>"
			+ "<input type='checkbox' id='SMAUG'/><label for='SMAUG' id='labelSMAUG' ><img src='images/cultures/smaug.png'/></label>"
			+ "<input type='checkbox' id='SPIDER'/><label for='SPIDER' id='labelSPIDER' ><img src='images/cultures/spider.png'/></label>"
			+ "<input type='checkbox' id='TROLL'/><label for='TROLL' id='labelTROLL' ><img src='images/cultures/troll.png'/></label>"
		);

		this.pageDiv.append(this.filterDiv);
		

		$("#culture-buttons").buttonset();

		//Hide Hobbit cultures by default
		$("#culture2").hide();

		this.setSelect.change(() => this.setFilterChanged(that));
		this.nameInput.change(this.filterChanged);
		this.gametextInput.change(this.filterChanged);
		this.sortSelect.change(this.filterChanged);
		this.raritySelect.change(this.filterChanged);

		// Hide initially hidden fields
		$("#twilightValueInput").prop("hidden", true);
		$("#strengthValueInput").prop("hidden", true);
		$("#vitalityValueInput").prop("hidden", true);
		$("#siteNumberValueInput").prop("hidden", true);
		$("#resistanceValueInput").prop("hidden", true);
		

		// Reset buttons
		$("#resetAdvancedFiltersButton").click(
				function (event) {
					that.pauseUpdates = true;
					$("#nameInput, #gametextInput" +
					  ", #keywordSelect, #raceSelect, #itemClassSelect, #phaseSelect, " +
					  ", #twilightCompareSelect, #twilightValueInput" +
					  ", #strengthCompareSelect, #strengthValueInput" +
					  ", #vitalityCompareSelect, #vitalityValueInput" + 
					  ", #signetSelect" +
					  ", #siteNumberCompareSelect, #siteNumberValueInput" +
					  ", #resistanceCompareSelect, #resistanceValueInput").val('').trigger('change');
					that.pauseUpdates = false;
					that.filterChanged();
					
					// $("#labelDWARVEN, #labelELVEN, #labelGANDALF, #labelGONDOR, #labelROHAN, #labelSHIRE" +
					//   ", #labelGOLLUM, #labelDUNLAND, #labelISENGARD, #labelMORIA, #labelRAIDER, #labelSAURON" +
					//   ", #labelMEN, #labelORC, #labelURUK_HAI, #labelWRAITH" +
					//   ", #labelESGAROTH, #labelGUNDABAD, #labelMIRKWOOD, #labelSMAUG, #labelSPIDER, #labelTROLL").removeClass("ui-state-active");
				});
		$("#resetAllFiltersButton").click(
				function (event) {
					that.pauseUpdates = true;
					$("#productSelect, #raritySelect, #blockSelect, #sideSelect, #sortSelect, #setSelect, #cardTypeSelect" +
					  ", #nameInput, #gametextInput" +
					  ", #keywordSelect, #raceSelect, #itemClassSelect, #phaseSelect, " +
					  ", #twilightCompareSelect, #twilightValueInput" +
					  ", #strengthCompareSelect, #strengthValueInput" +
					  ", #vitalityCompareSelect, #vitalityValueInput" + 
					  ", #signetSelect" +
					  ", #siteNumberCompareSelect, #siteNumberValueInput" +
					  ", #resistanceCompareSelect, #resistanceValueInput").val('').trigger('change');
					
					$("#DWARVEN, #ELVEN, #GANDALF, #GONDOR, #ROHAN, #SHIRE" +
					  ", #GOLLUM, #DUNLAND, #ISENGARD, #MORIA, #RAIDER, #SAURON" +
					  ", #MEN, #ORC, #URUK_HAI, #WRAITH" +
					  ", #ESGAROTH, #GUNDABAD, #MIRKWOOD, #SMAUG, #SPIDER, #TROLL").prop("checked", false).change();
					that.pauseUpdates = false;
					that.filterChanged();
				});

		// Triggers for filter fields changed
		$("#productSelect").change(() => this.productChanged(that));
		
		$("#blockSelect, #sideSelect, #setSelect, #cardTypeSelect, #raritySelect, #sortSelect, #nameInput, #gametextInput" +
		  ", #keywordSelect, #raceSelect, #itemClassSelect, #phaseSelect, " +
		  ", #twilightValueInput, #strengthValueInput, #vitalityValueInput, #signetSelect" +
		  ", #siteNumberValueInput, #resistanceValueInput").change(() => this.setFilterChanged(that));

		$("#twilightCompareSelect, #strengthCompareSelect, #vitalityCompareSelect" +
		  ", #siteNumberCompareSelect, #resistanceCompareSelect").change(() => this.compareTypeChanged(that));
		
		$("#DWARVEN, #ELVEN, #GANDALF, #GONDOR, #ROHAN, #SHIRE" +
					  ", #GOLLUM, #DUNLAND, #ISENGARD, #MORIA, #RAIDER, #SAURON" +
					  ", #MEN, #ORC, #URUK_HAI, #WRAITH" +
					  ", #ESGAROTH, #GUNDABAD, #MIRKWOOD, #SMAUG, #SPIDER, #TROLL").change(() => this.setFilterChanged(that));
		
		this.collectionDiv = $("<div id='collection-display' style='display:flex;flex-direction:column;position:relative;'></div>");
		//collection-display
		pageElem.append(this.collectionDiv);
	},
	
	generateNumericComparator: function(id, labelText) {
		this[id + "Label"] = $("<label for='" + id + "CompareSelect' class='filterLabel'>" + labelText + ":</label>");
		this[id + "CompareSelect"] = $("<select id='" + id + "CompareSelect' class='filterInput'>"
			+ "<option value='' selected='selected'>Any</option>"
			+ "<option value='EQUALS'>=</option>"
			+ "<option value='GREATER_THAN'>></option>"
			+ "<option value='GREATER_THAN_OR_EQUAL_TO'>>=</option>"
			+ "<option value='LESS_THAN'><</option>"
			+ "<option value='LESS_THAN_OR_EQUAL_TO'><=</option>"
			+ "</select>");
		this[id + "ValueInput"] = $("<input id='" + id + "ValueInput' type='number' min = '0' max='12' class='filterInput' style='width: 35px;'>");
	},
	
	movieBlocks: ["ttt_block", "king_block"],
	expandedBlocks: ["war_block", "hunter_block"],
	hobbitBlocks: ["hobbit"],
	
	setFilterChanged: function(that) {
		
		//Show/hide Rohan
		if (that.blocks.find((x) => that.movieBlocks.includes(x.filter)
		                     || that.expandedBlocks.includes(x.filter))) {
			$("#culture_freeps_movie1").show();
		} 
		else {
			$("#labelROHAN-movie").removeClass("ui-state-active");
			$("#culture_freeps_movie1").hide();
		}
		
		//Show/hide Gollum
		if (that.blocks.find((x) => that.movieBlocks.includes(x.filter)
		                     || that.expandedBlocks.includes(x.filter)
		                     || that.hobbitBlocks.includes(x.filter))) {
			$("#culture_freeps_movie2").show();
		} 
		else {
			$("#labelGOLLUM-movie").removeClass("ui-state-active");
			$("#culture_freeps_movie2").hide();
		}
		
		//Show/hide Movie-era Shadows
		if (that.blocks.find((x) => that.movieBlocks.includes(x.filter))) {
			$("#culture_shadow_movie1").show();
			$("#culture_shadow_movie2").show();
		} 
		else {
			$("#labelDUNLAND-movie").removeClass("ui-state-active");
			$("#labelRAIDER-movie").removeClass("ui-state-active");
			$("#culture_shadow_movie1").hide();
			$("#culture_shadow_movie2").hide();
		}
		
		//Show/hide Expanded-era Shadows
		if (that.blocks.find((x) => that.expandedBlocks.includes(x.filter))) {
			$("#culture_shadows").show();
		} 
		else {
			$("#labelMEN-movie").removeClass("ui-state-active");
			$("#labelORC-movie").removeClass("ui-state-active");
			$("#labelURUK_HAI-movie").removeClass("ui-state-active");
			$("#culture_shadows").hide();
		}
		
		
		//Show/hide Hobbit-only cultures
		
		if (that.blocks.find((x) => that.hobbitBlocks.includes(x.filter))) {
			$("#culture_hobbit").show();
		} 
		else {
			$("#labelESGAROTH").removeClass("ui-state-active");
			$("#labelGUNDABAD").removeClass("ui-state-active");
			$("#labelMIRKWOOD").removeClass("ui-state-active");
			$("#labelSMAUG").removeClass("ui-state-active");
			$("#labelSPIDER").removeClass("ui-state-active");
			$("#labelTROLL").removeClass("ui-state-active");
			$("#culture_hobbit").hide();
		}
		
		
		return that.filterChanged();
	},
	
	// Callback function for when the product filter is changed
	productChanged: function(that) {
		var isPack = ($("#productSelect option:selected").prop("value") == "pack");

		$("#blockSelect").prop("disabled", isPack);
		$("#sideSelect").prop("disabled", isPack);
		$("#setSelect").prop("disabled", isPack);
		$("#cardTypeSelect").prop("disabled", isPack);
		$("#raritySelect").prop("disabled", isPack);
		$("#sortSelect").prop("disabled", isPack);
		
		$("#gametextInput").prop("disabled", isPack);
		$("#keywordSelect").prop("disabled", isPack);
		$("#raceSelect").prop("disabled", isPack);
		$("#itemClassSelect").prop("disabled", isPack);
		$("#phaseSelect").prop("disabled", isPack);
		
		$("#twilightCompareSelect").prop("disabled", isPack);
		$("#twilightValueInput").prop("hidden", isPack);
		$("#strengthCompareSelect").prop("disabled", isPack);
		$("#strengthValueInput").prop("hidden", isPack);
		$("#vitalityCompareSelect").prop("disabled", isPack);
		$("#vitalityValueInput").prop("hidden", isPack);
		$("#signetSelect").prop("disabled", isPack);
		$("#siteNumberCompareSelect").prop("disabled", isPack);
		$("#siteNumberValueInput").prop("hidden", isPack);
		$("#resistanceCompareSelect").prop("disabled", isPack);
		$("#resistanceValueInput").prop("hidden", isPack);

		return that.compareTypeChanged(that);
	},
	
	resetFields: function(name) {
		if ($("#" + name + "CompareSelect option:selected").prop("value") == "") {
			$("#" + name + "ValueInput").prop("hidden", true);
			$("#" + name + "ValueInput").prop("value", '');
		}
		else {
			$("#" + name + "ValueInput").prop("hidden", false);
		}
	},

	// Callback function for when a filter compare is changed, to switch
	// all of the numeric comparison dropdowns to a default state
	compareTypeChanged: function(that) {
		that.resetFields("twilight");
		that.resetFields("strength");
		that.resetFields("vitality");
		that.resetFields("siteNumber");
		that.resetFields("resistance");
		return that.filterChanged();
	},

	// Callback function for when a filter is changed
	filterChanged: function() {
		var newFilterString = this.calculateDeckFilter();
		// Only re-gather collection if filter actually changed from previous filter
		if (this.filter != newFilterString) {
			this.filter = newFilterString
			this.start = 0;
			this.getCollection();
		}
		return true;
	},
	
	updateFormat: function(formatCode, blocks) {
		var that = this;
		this.currentFormat = formatCode;
		this.blocks = blocks;
		
		this.blockSelect = $("#blockSelect");
		this.blockSelect.empty();
		let defaultOption = $("<option/>")
						.attr("value", "")
						.attr("selected", "selected")
						.text("Any");
		this.blockSelect.append(defaultOption);
		
		for(const block of blocks) {
			var option = $("<option/>")
						.attr("value", block.filter)
						.text(block.name);
			this.blockSelect.append(option)
		}
		
		
		if(blocks.length == 1) {
			$("#blockSelect").prop('selectedIndex', 1);
		} 
		else {
			$("#blockSelect").prop('selectedIndex', 0);
		}
			
		
		this.blockSelect.change(function () {  
				that.setSelect = $("#setSelect");
				that.setSelect.empty();
				let defaultOption = $("<option/>")
								.attr("value", "")
								.attr("selected", "selected")
								.text("Any");
				that.setSelect.append(defaultOption);
				
				if($("#blockSelect").val() == "") {
					
					for(const block of that.blocks) {
						let setEntries = Object.entries(block.setFilters);
						for(const [name, code] of setEntries) {
							var option = $("<option/>")
										.attr("value", code)
										.text(name);
							that.setSelect.append(option)
						}
					}
					
				}
				else {
					let currentBlock = that.blocks.find((x) => x.filter == that.blockSelect.val());
					
					if(currentBlock) {
						let setEntries = Object.entries(currentBlock.setFilters);
						for(const [name, code] of setEntries) {
							var option = $("<option/>")
										.attr("value", code)
										.text(name);
							that.setSelect.append(option)
						}
					}
				}
				
				$("#setSelect").html($("#setSelect option").sort(function (a, b) {
					let av = a.value == "" ? 0 : parseInt(a.value, 10);
					let bv = b.value == "" ? 0 : parseInt(b.value, 10);
					if(a.value == "pc_errata")
						av = 1000;
					if(b.value == "pc_errata")
						bv = 1000;
					return av == bv ? 0 : av < bv ? -1 : 1
				}));
				
				// "Any" + the 1 actual entry
				if($("#setSelect option").length == 2) {
					$("#setSelect").prop('selectedIndex', 1);
				} 
				else {
					$("#setSelect").prop('selectedIndex', 0);
				}

				that.setFilterChanged(that);
			});
		
		this.blockSelect.change();
	},

	layoutUi: function (x, y, width, height) {
		//this.pageDiv.css({position: "absolute", left: x, top: y, width: width, height: 34});
		//this.countSlider.css({width: width - 100});
		//this.fullFilterDiv.css({position: "absolute", left: x, top: y + 34, width: width, height: 34});
		//this.filterDiv.css({position: "absolute", left: x, top: y + 68, width: width, height: 80});
	},

	layoutPageUi: function (x, y, width) {
		//this.pageDiv.css({left: x, top: y, width: width, height: 36});
		//this.countSlider.css({width: width - 100});
	},

	disableNavigation: function () {
		this.previousPageBut.button("option", "disabled", true);
		this.nextPageBut.button("option", "disabled", true);
		this.countSlider.button("option", "disabled", true);
	},

	calculateDeckFilter: function (predefFilter) {
		
		if(predefFilter === undefined)
			predefFilter = this.overrideFilter
		
		// Generates the filter string based on the current filter selections
		
		var cultures = new Array();
		$("label", $("#culture-buttons")).each(
			function () {
				if ($(this).hasClass("ui-state-active"))
					cultures.push($(this).prop("for"));
			});
		var culture = "";
		if(cultures.length > 0 && !predefFilter.includes("culture:")) {
			culture = " culture:" + cultures;
		}
		
		var product = $("#productSelect option:selected").prop("value");
		if (product != "" && !predefFilter.includes("product:"))
			product = " product:" + product; 
			
		var rarity = $("#raritySelect option:selected").prop("value");
		if (rarity != "" && !predefFilter.includes("rarity:"))
			rarity = " rarity:" + rarity;       
		
		var side = $("#sideSelect option:selected").prop("value");
		if (side != "" && !predefFilter.includes("side:"))
			side = " side:" + side;
		
		var sort = $("#sortSelect option:selected").prop("value");
		if (sort != "" && !predefFilter.includes("sort:"))
			sort = " sort:" + sort;
		
		var format = this.currentFormat;
		if (format) {
			format = " format:" + format;
		}
		else {
			format = "";
		}

		var block = $("#blockSelect option:selected").prop("value");
		if (block) {
			block = " block:" + block;
		}
		else {
			block = "";
		}
		
		var set = $("#setSelect option:selected").prop("value");
		if (set) {
			set = " set:" + set;
		}
		else {
			set = "";
		}

		var cardType = $("#cardTypeSelect option:selected").prop("value");
		if (cardType != "")
			cardType = " cardType:" + cardType;
		
		var name = $("#nameInput").prop("value");
		if (name.trim() != "" && !predefFilter.includes("name:")) {
			var nameElems = name.split(" ").filter(Boolean);
			name = "";
			for (var i = 0; i < nameElems.length; i++)
				name += " name:" + nameElems[i];
		}

		var gametext = $("#gametextInput").prop("value");
		if (gametext != "" && !predefFilter.includes("gametext:")) {
			gametext = " gametext:" + gametext.replace(" ", "_");
		}
		
		var keyword = $("#keywordSelect option:selected").prop("value");
		if (keyword != "" && !predefFilter.includes("keyword:"))
			keyword = " keyword:" + keyword;
		
		var phase = $("#phaseSelect option:selected").prop("value");
		if (phase != "" && !predefFilter.includes("phase:"))
			phase = " phase:" + phase;   

		var race = $("#raceSelect option:selected").prop("value");
		if (race != "" && !predefFilter.includes("race:"))
			race = " race:" + race;

		var itemClass = $("#itemClassSelect option:selected").prop("value");
		if (itemClass != "" && !predefFilter.includes("itemClass:"))
			itemClass = " itemClass:" + itemClass

		var twilightCompare = $("#twilightCompareSelect option:selected").prop("value");
		var twilight = $("#twilightValueInput").prop("value");
		if (twilightCompare != "" && twilight != ""  && !predefFilter.includes("twilight:")) {
			twilight = " twilightCompare:" + twilightCompare + " twilight:" + twilight;
		}
		else {
			twilight = "";
		}
		
		var siteNumberCompare = $("#siteNumberCompareSelect option:selected").prop("value");
		var siteNumber = $("#siteNumberValueInput").prop("value");
		if (siteNumberCompare != "" && siteNumber != "" && !predefFilter.includes("siteNumber:")) {
			siteNumber = " siteNumberCompare:" + siteNumberCompare + " siteNumber:" + siteNumber;
		}
		else {
			siteNumber = "";
		}

		var strengthCompare = $("#strengthCompareSelect option:selected").prop("value");
		var strength = $("#strengthValueInput").prop("value");
		if (strengthCompare != "" && strength != ""  && !predefFilter.includes("strength:")) {
			strength = " strengthCompare:" + strengthCompare + " strength:" + strength;
		}
		else {
			strength = "";
		}
		
		var vitalityCompare = $("#vitalityCompareSelect option:selected").prop("value");
		var vitality = $("#vitalityValueInput").prop("value");
		if (vitalityCompare != "" && vitality != "" && !predefFilter.includes("vitality:")) {
			vitality = " vitalityCompare:" + vitalityCompare + " vitality:" + vitality;
		}
		else {
			vitality = "";
		}
		
		var signet = $("#signetSelect option:selected").prop("value");
		if (signet != "")
			signet = " signet:" + signet;
		
		var resistanceCompare = $("#resistanceCompareSelect option:selected").prop("value");
		var resistance = $("#resistanceValueInput").prop("value");
		if (resistanceCompare != "" && resistance != "" && !predefFilter.includes("resistance:")) {
			resistance = " resistanceCompare:" + resistanceCompare + " resistance:" + resistance;
		}
		else {
			resistance = "";
		}

		var filterString = side + format + block + set + cardType + rarity + sort + product + culture 
				+ name + gametext
				+ keyword + phase + race + itemClass
				+ twilight + strength + vitality + resistance + siteNumber + signet
				+ " " + predefFilter;

		console.log("Regenerating filter: " + filterString);
		return filterString.trim();
	},

	getCollection: function () {
		var that = this;
		console.log("Updating collection");
		this.getCollectionFunc(this.filter, this.start, this.count, function (xml) {
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
