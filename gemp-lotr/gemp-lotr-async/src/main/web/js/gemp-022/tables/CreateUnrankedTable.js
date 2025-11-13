class CreateUnrankedTable {
	mainHall = null;
	comm = null;
	// Should be the create-bot-table div
	mainDiv = null;
	formatManager = null;
	deckManager = null;
	
	deckSelector = null;
	
	formatDropdown = null;
	timerDropdown = null;
	descText = null;
	inviteCheckbox = null;
	inviteeDropdown = null;
	privateCheckbox = null;
	keepopenCheckbox = null;
	defaultFlowCheckbox = null;
	createTableButton = null;
	
	resultDiv = null;
	
	constructor(mainHall, comm, div, formatManager, deckManager) {
		var that = this;
		
		this.mainHall = mainHall;
		this.comm = comm;
		this.mainDiv = div;
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		
		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "unranked-table");
		
		this.deckDropdown = this.deckSelector.playerDeckDropdown;
		this.formatDropdown = $("#unranked-format");
		this.timerDropdown = $("#unranked-timer");
		this.descText = $("#unranked-desc");
		this.inviteCheckbox = $("#unranked-invite-only");
		this.inviteeDropdown = $("#unranked-invitee");
		this.privateCheckbox = $("#unranked-private");
		this.keepopenCheckbox = $("#unranked-keep-open");
		this.defaultFlowCheckbox = $("#unranked-default-flow");
		
		this.inviteCheckbox.click(() => {
			if (that.inviteCheckbox.is(":checked")) {
				that.inviteeDropdown.prop("disabled", false);
			} else {
				that.inviteeDropdown.prop("disabled", true);  
			}
		});
		
		this.defaultFlowCheckbox.click(() => {
			if (that.defaultFlowCheckbox.is(":checked")) {
				saveToCookie("unranked-table-default-flow", "true");
			} else {
				saveToCookie("unranked-table-default-flow", "false");  
			}
		});
		
		this.resultDiv = $("#unranked-result");
		
		this.createTableButton = $("#submit-unranked-table-button").button().click(this.submitTable(this));
		
		this.formatManager.registerFormatDropdownUpdate(this.formatDropdown);
		
		
		
		this.deckDropdown.on('change',function(){
			var currentDeck = that.deckDropdown.val();
			var currentFormat = that.formatDropdown.val();
			//Turns out it feels better to just always switch
			// if(currentFormat !== null) 
			// 	return;

			that.deckManager.playerDecks.forEach((deck) => {
		    if(deck.name === currentDeck) {
		    	var formatCode = that.formatManager.lookupFormatByName(deck.targetFormat);
					that.formatDropdown.val(formatCode);
				}
			});
		});
	}
	
	hide() {
		this.mainDiv.hide();
	}
	
	show() {
		var that = this;
		
		this.deckDropdown.val(loadFromCookie("unranked-table-last-deck", ""));
		this.formatDropdown.val(loadFromCookie("unranked-table-last-format", ""));
		this.timerDropdown.val(loadFromCookie("unranked-table-last-timer", ""));
		
		this.defaultFlowCheckbox.checked = loadFromCookie("unranked-table-default-flow", "false") === "true";
		
		that.mainDiv.show();
	}
	
	updateResult(text) {
		this.resultDiv.html(text);
	}
	
	updatePlayers(rawPlayers, currentPlayer) {
		
		let players = rawPlayers.map(name => {
			return name.replace(/[^a-zA-Z0-9_]/g, '');
		});
		
		players.sort((a, b) => {
			return a.toLowerCase().localeCompare(b.toLowerCase());
		});
	
		let selectedPlayer = this.inviteeDropdown.val();
		this.inviteeDropdown.empty();
		for(const player of players) {
			if(player.endsWith(currentPlayer))
				continue;
			
			var option = $("<option/>")
				.attr("value", player)
				.text(player);
				
			this.inviteeDropdown.append(option);
		}
		
		this.inviteeDropdown.val(selectedPlayer);
		this.inviteeDropdown.change();
	}
	
	submitTable(that) {
		return () => {

			var format = that.formatDropdown.val();
			
			if(format == null || format === "") {
				that.updateResult("You must select a format.");
				return;
			}
			
			var deck = that.deckSelector.getSelectedDeck();
			var tableDesc = that.descText.val();
			var timer = that.timerDropdown.val();
			var isPrivate = that.privateCheckbox.is(':checked');
			var isInviteOnly = that.inviteCheckbox.is(':checked');
			var invitee = that.inviteeDropdown.val();
			var keepOpen = that.keepopenCheckbox.is(':checked');
			
			if(isInviteOnly) {
				if(invitee == null || invitee === "") {
					that.updateResult("If set to invite-only, you must select a player to invite (all others will be disallowed from joining your table).");
					return;
				}
				
				tableDesc = invitee;				
			}
			
			if(deck == null || deck === "") {
				that.updateResult("You must select a deck.");
				return;
			}

			saveToCookie("unranked-table-last-deck", deck);
			saveToCookie("unranked-table-last-format", format);
			saveToCookie("unranked-table-last-timer", timer);

			that.comm.createTable(format, deck, timer, tableDesc, isPrivate, isInviteOnly,
					CreateTable.getResponse(that.resultDiv, (success) => {
						
						if(success && !keepOpen) {
							that.mainHall.tableCreator.hideAll();
							that.mainHall.tableCreator.popup.dialog("close");
						}
					}),
					CreateTable.getCreateErrorMap(that.resultDiv)
				);
			
		};
	}
}