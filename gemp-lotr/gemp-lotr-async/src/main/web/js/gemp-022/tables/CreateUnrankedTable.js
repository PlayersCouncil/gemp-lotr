class CreateUnrankedTable {
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
	createTableButton = null;
	
	resultDiv = null;
	
	constructor(comm, div, formatManager, deckManager) {
		var that = this;
		
		this.comm = comm;
		this.mainDiv = div;
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		
		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager)
		
		this.formatDropdown = $("#unranked-format");
		this.timerDropdown = $("#unranked-timer");
		this.descText = $("#unranked-desc");
		this.inviteCheckbox = $("#unranked-invite-only");
		this.inviteeDropdown = $("#unranked-invitee");
		this.privateCheckbox = $("#unranked-private");
		
		this.inviteCheckbox.click(() => {
			if (that.inviteCheckbox.is(":checked")) {
				that.inviteeDropdown.prop("disabled", false);
			} else {
				that.inviteeDropdown.prop("disabled", true);  
			}
		});
		
		this.resultDiv = $("#unranked-result");
		
		this.createTableButton = $("#submit-unranked-table-button").button().click(this.submitTable(this));
		
		this.formatManager.registerFormatDropdownUpdate(this.formatDropdown);
	}
	
	hide() {
		this.mainDiv.hide();
	}
	
	show() {
		var that = this;
		
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

			that.comm.createTable(format, deck, timer, tableDesc, isPrivate, isInviteOnly,
					CreateTable.getResponse(that.resultDiv),
					CreateTable.getCreateErrorMap(that.resultDiv)
				);
			
		};
	}
}