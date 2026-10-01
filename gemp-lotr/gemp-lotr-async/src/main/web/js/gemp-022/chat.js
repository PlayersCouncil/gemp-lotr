var ChatBoxUI = Class.extend({
	name:null,
	userInfo:null,
	userName:null,
	pingRegex:null,
	mentionRegex:null,
	everyoneRegex:/(@everyone|@anyone)/,
	div:null,
	comm:null,

	chatMessagesDiv:null,
	chatTalkDiv:null,
	chatListDiv:null,

	showTimestamps:false,
	maxMessageCount:500,
	talkBoxHeight:25,

	chatUpdateInterval:100,

	playerListener:null,
	hiddenClasses:null,

	hideSystemButton:null,

	lockChat:false,
	stopUpdates: false,
	
	dialogListener: null,
	
	enableDiscord: false,
	discordDiv:null,
	discordWidget:null,
	chatEmbed:null,
	displayDiscord:true,
	
	tournamentCallback:null,
	
	toggleChatButton:null,


	// deferStart: build the chat but leave the player info and (Game Hall) the chat's first request to start(); the
	// hall calls it through GempHallSession once the viewer is known to be logged in or opens a tab that needs it.
	init:function (name, div, url, showList, playerListener, showHideSystemButton, displayChatListener, allowDiscord=false, deferStart=false) {
		var that = this;
			 
		this.hiddenClasses = new Array();
		this.playerListener = playerListener;
		this.dialogListener = displayChatListener;
		this.name = name;
		this.div = div;
		
		//This needs to be done before the comm object is instantiated, as otherwise it's too slow for immediate errors

		if(this.name == "Game Hall")
		{
			this.chatMessagesDiv = $("#chatMessages");
		}
		else
		{
			this.chatMessagesDiv = $("<div class='chatMessages'></div>");
			this.div.append(this.chatMessagesDiv);
		}

		
		this.comm = new GempLotrCommunication(url, function (xhr, ajaxOptions, thrownError) {
			that.appendMessage("Unknown chat problem occured (error=" + xhr.status + ")", "warningMessage");
		});
		this.enableDiscord = allowDiscord;

		// "Click here to register or log in." (the "You are not logged in." warning): the login page is given the
		// current hall link, so logging in comes back to what was showing
		// (delegate: the game and deck-builder pages still run jQuery 1.6, which has no .on)
		this.chatMessagesDiv.delegate("a.chat-login-link", "click", function () {
			this.href = ChatBoxUI.loginUrl();
		});

		if (this.name != null) {
			
			if(this.name == "Game Hall")
			{
				this.discordDiv = $("#discordChat");

				this.chatTalkDiv = $("#chatTalk");

				this.hideSystemButton = $("#showSystemButton");
				if (showHideSystemButton) {
					this.hideSystemButton.attr("type", "button").attr("title", "Toggle system messages")
						.button({icons:{
							primary:"ui-icon-zoomin"
						}, text:false});

					this.hideSystemButton.click(
							function () {
								if (that.isShowingMessageClass("systemMessage")) {
									that.hideSystemButton.button("option", "icons", {primary:'ui-icon-zoomin'});
									that.hideMessageClass("systemMessage");
								} else {
									that.hideSystemButton.button("option", "icons", {primary:'ui-icon-zoomout'});
									that.showMessageClass("systemMessage");
								}
							});
					this.hideMessageClass("systemMessage");
				}
				else
				{
					this.hideSystemButton.hide();
					this.hideSystemButton = null;
				}

				this.chatTalkDiv.keydown(function (e) {
					if (e.keyCode == 13) {
						if(!e.shiftKey)
						{
							e.preventDefault();
							var value = $(this).val();
							if (value != "")
								that.sendMessage(value);
							$(this).val("").trigger("input");
							that.scrollChatToBottom();
						}
					}
				});

				
				if (showList) {
					this.chatListDiv = $("#userList");
					this.toggleChatButton = $("#toggleChatButt");

					this.toggleChatButton.button();
					this.toggleChatButton.click( function() {
						that.toggleChat();
					});
				}
				
				this.setDiscordVisible(false);
			}
			else
			{
				this.chatTalkDiv = $("<input type='text' class='chatTalk'>");

				if (showHideSystemButton) {
					this.hideSystemButton = $("<button id='showSystemMessages'>Toggle system messages</button>").button(
					{icons:{
						primary:"ui-icon-zoomin"
					}, text:false});
					this.hideSystemButton.click(
							function () {
								if (that.isShowingMessageClass("systemMessage")) {
									$('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomin'});
									that.hideMessageClass("systemMessage");
								} else {
									$('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomout'});
									that.showMessageClass("systemMessage");
								}
							});
					this.hideMessageClass("systemMessage");
				}

				if (showList) {
					this.chatListDiv = $("<div class='userList'></div>");
					this.div.append(this.chatListDiv);
				}
				if (this.hideSystemButton != null)
					this.div.append(this.hideSystemButton);

				this.div.append(this.chatTalkDiv);

				this.chatTalkDiv.bind("keypress", function (e) {
					var code = (e.keyCode ? e.keyCode : e.which);
					if (code == 13) {
						var value = $(this).val();
						if (value != "")
							that.sendMessage(value);
						$(this).val("");
					}
				});
			}
			
		} else {
			this.talkBoxHeight = 0;
		}

		if (!deferStart)
			this.start();
	},

	started: false,

	// Goes live: the player info and, for the Game Hall, the chat's first request (which starts its updates).
	start:function () {
		var that = this;
		if (this.started)
			return;
		this.started = true;

		// the same /player request the hall makes (one per page)
		this.comm.getPlayerInfoShared(function(json)
		{ 
			that.initPlayerInfo(json);
			// Discord or the built-in chat, as the player last chose (Discord until they choose)
			setTimeout(() => {  that.setDiscordVisible(that.loadChatChoice()); }, 10);
		}, this.chatErrorMap());

		if (this.name == "Game Hall") {
			if (this.chatTalkDiv != null)
				this.chatTalkDiv.prop("disabled", false).attr("placeholder", "Message the game hall...");
			this.comm.startChat(this.name,
					function (xml) {
						that.processMessages(xml, true);
						that.scrollChatToBottom();
					}, this.chatErrorMap());
		}
	},

	// A visitor who is not logged in (before start()): the warning with its login link, and no talking.
	showLoggedOut:function () {
		this.appendWarningOnce(ChatBoxUI.NOT_LOGGED_IN, "warningMessage");
		if (this.chatTalkDiv != null)
			this.chatTalkDiv.prop("disabled", true).attr("placeholder", "Log in to chat");
	},
	
	updatePlayerListener:function(listener) {
		this.playerListener = listener;
	},
	
	beginGameChat:function() {
		var that = this;
		this.comm.startChat(this.name,
				function (xml) {
					that.processMessages(xml, true);
				}, this.chatErrorMap());
	},
	
	initPlayerInfo:function (playerInfo) {
		this.userInfo = playerInfo;
		this.userName = this.userInfo.name;
		this.pingRegex = new RegExp("@" + this.userName + "\\b");
		this.mentionRegex = new RegExp("(?<!<b>)\\b" + this.userName + "\\b");
	},


	hideMessageClass:function (msgClass) {
		this.hiddenClasses.push(msgClass);
		$("div.message." + msgClass, this.chatMessagesDiv).hide();
	},

	isShowingMessageClass:function (msgClass) {
		var index = $.inArray(msgClass, this.hiddenClasses);
		return index == -1;
	},

	showMessageClass:function (msgClass) {
		var index = $.inArray(msgClass, this.hiddenClasses);
		if (index > -1) {
			this.hiddenClasses.splice(index, 1);
			$("div.message." + msgClass, this.chatMessagesDiv).show();
		}
	},

	setBounds:function (x, y, width, height) {
		
		if(this.name != "Game Hall")
		{
			var talkBoxPadding = 3;

			var userListWidth = 150;
			if (this.chatListDiv == null)
			   userListWidth = 0;

			if (this.chatListDiv != null)
			   this.chatListDiv.css({ position:"absolute", left:x + width - userListWidth + "px", top:y + "px", width:userListWidth, height:height - this.talkBoxHeight - 3 * talkBoxPadding, overflow:"auto" });
		   
			if(this.chatMessagesDiv != null)
				this.chatMessagesDiv.css({ position:"absolute", left:x + "px", top:y + "px", width:width - userListWidth, height:height - this.talkBoxHeight - 3 * talkBoxPadding, overflow:"auto" });
			
			if (this.chatTalkDiv != null) {
			   var leftTextBoxPadding = 0;

			   if (this.hideSystemButton != null) {
				   this.hideSystemButton.css({position:"absolute", left:x + width - talkBoxPadding - this.talkBoxHeight + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:this.talkBoxHeight, height:this.talkBoxHeight});
				   leftTextBoxPadding += this.talkBoxHeight + talkBoxPadding;
			   }
			   // if (this.lockButton != null) {
			   //     this.lockButton.css({position:"absolute", left:x + width - talkBoxPadding - this.talkBoxHeight - leftTextBoxPadding + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:this.talkBoxHeight, height:this.talkBoxHeight});
			   //     leftTextBoxPadding += this.talkBoxHeight + talkBoxPadding;
			   // }

			   this.chatTalkDiv.css({ position:"absolute", left:x + talkBoxPadding + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:width - 3 * talkBoxPadding - leftTextBoxPadding, height:this.talkBoxHeight });
			}
		}

		this.handleChatVisibility();       
	},
	
	handleChatVisibility:function() {
		
		if(this.enableDiscord)
		{
			if(this.displayDiscord)
			{
				this.toggleChatButton.text("Switch to Legacy");
				
				if(this.chatEmbed == null)
				{
					this.discordDiv.show();
					this.chatEmbed = $("<widgetbot server='699957633121255515' channel='873065954609881140' width='100%' height='100%' shard='https://emerald.widgetbot.io' username='" + this.userName + "'></widgetbot>");
					var script = $("<script src='https://cdn.jsdelivr.net/npm/@widgetbot/html-embed'></script>");
					this.discordDiv.append(script);
					this.discordDiv.append(this.chatEmbed);
				}
			}
			else
			{
				this.toggleChatButton.text("Switch to Discord");
			} 
		}
		
		if(this.enableDiscord && this.displayDiscord)
		{
			if(this.discordDiv != null)
				this.discordDiv.show();
			
			if(this.chatMessagesDiv != null)
				this.chatMessagesDiv.hide();
			if(this.chatTalkDiv != null)
				this.chatTalkDiv.hide();
			if(this.hideSystemButton != null)
				this.hideSystemButton.hide();
			// if(this.lockButton != null)
			//     this.lockButton.hide();
		}
		else
		{
			if(this.discordDiv != null)
				this.discordDiv.hide();
			
			if(this.chatMessagesDiv != null)
				this.chatMessagesDiv.show();
			if(this.chatTalkDiv != null)
				this.chatTalkDiv.show();
			if(this.hideSystemButton != null)
				this.hideSystemButton.show();
			// if(this.lockButton != null)
			//     this.lockButton.show(); 
		}
		
	},
	
	toggleChat:function() {
		this.setDiscordVisible(!this.displayDiscord);
		this.saveChatChoice(this.displayDiscord);
	},

	CHAT_CHOICE_KEY: "gemp.hallChat",

	// true = Discord, false = the built-in chat.  Kept in localStorage; storage can be unavailable (private mode,
	// blocked site data), in which case the default applies.
	loadChatChoice:function() {
		try {
			return window.localStorage.getItem(this.CHAT_CHOICE_KEY) !== "builtin";
		} catch (e) {
			return true;
		}
	},

	saveChatChoice:function(discord) {
		try {
			window.localStorage.setItem(this.CHAT_CHOICE_KEY, discord ? "discord" : "builtin");
		} catch (e) {
			// not remembered; nothing else to do
		}
	},
	
	setDiscordVisible:function(visible)
	{
	  this.displayDiscord = visible;
	  
	  this.handleChatVisibility();
		
	},
	
	checkForEnd:function (message, msgClass) {
		// if(msgClass != "systemMessage")
		// {
		//     return;
		// }
		
		if(message.includes("Thank you for playing!")) {
			if (this.dialogListener != null) {
				this.dialogListener("Give us feedback!", message);
			}
		}
	},
	

	appendMessage:function (message, msgClass) {
		if (msgClass == undefined)
			msgClass = "chatMessage";
		
		var locked = false;
		var scroll = this.chatMessagesDiv.scrollTop();
		var maxScroll = this.chatMessagesDiv[0].scrollHeight - this.chatMessagesDiv.outerHeight();
		var noScrollBars = maxScroll <= 0;
		var ratio = scroll / maxScroll;
		
		if(msgClass === "warningMessage" || noScrollBars || maxScroll <= 30 || ratio >= 0.999)
			locked = true;
		
		if(this.pingRegex != null && this.pingRegex.test(message))
		{
			msgClass += " user-ping";
		}
		else if((this.mentionRegex != null && this.mentionRegex.test(message)) || this.everyoneRegex.test(message))
		{
			msgClass += " user-mention";
		}
		
		if(msgClass == "gameMessage")
		{
			message = "<div class='msg-content'>" + message + "</div>";
		}
		
		var messageDiv = $("<div class='message " + msgClass + "'>" + message + "</div>");

		this.chatMessagesDiv.append(messageDiv);
		var index = $.inArray(msgClass, this.hiddenClasses);
		if (!this.isShowingMessageClass(msgClass)) {
			messageDiv.hide();
		}

		if ($("div.message", this.chatMessagesDiv).length > this.maxMessageCount) {
			$("div.message", this.chatMessagesDiv).first().remove();
		}
		
		if(locked)
			this.scrollChatToBottom();
		
		this.checkForEnd(message, msgClass);
	},

	monthNames:["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],

	scrollChatToBottom:function() {
		this.chatMessagesDiv.prop({ scrollTop:this.chatMessagesDiv.prop("scrollHeight") })
	},
	
	formatToTwoDigits:function (no) {
		if (no < 10)
			return "0" + no;
		else
			return no;
	},

	processMessages:function (xml, processAgain) {
		var root = xml.documentElement;
		if (root.tagName == 'chat') {
			this.retryCount = 0;
			var messages = root.getElementsByTagName("message");
			for (var i = 0; i < messages.length; i++) {
				var message = messages[i];
				var from = message.getAttribute("from");
				var text = message.childNodes[0].nodeValue;

				var msgClass = "chatMessage";
				if (from == "System") {
					msgClass = "systemMessage";
				}
				else if(from.startsWith("TournamentSystem")) {
					if(this.tournamentCallback) {
						this.tournamentCallback(from, text);
					}
					from = "TournamentSystem";
				}
				var prefix = "<div class='msg-identifier'>";
				if (this.showTimestamps) {
					var date = new Date(parseInt(message.getAttribute("date")));
					var dateStr = this.monthNames[date.getMonth()] + " " + date.getDate() + " " + this.formatToTwoDigits(date.getHours()) + ":" + this.formatToTwoDigits(date.getMinutes()) + ":" + this.formatToTwoDigits(date.getSeconds());
					prefix += "<span class='timestamp'>[" + dateStr + "]</span>";
				}
				
				prefix += "<span> <b>" + from + ": </b></span></div>";
				var postfix = "<div class='msg-content'>" + text + "</div>";
					
				this.appendMessage(prefix + postfix, msgClass);
			}

			var users = root.getElementsByTagName("user");
			// ==== tabs-account: user list (bare names; role / self / incognito arrive as attributes) ====
			var userEntries = this.readUsers(users);
			if (this.playerListener != null) {
				var players = new Array();
				for (var i = 0; i < userEntries.length; i++) {
					var entry = userEntries[i];
					// Rooms without the hall's rendered list (game, draft) show these names as they come, so keep the
					// old "* " / "+ " role markers for them; the hall's listener gets bare names (contract C2).
					var marker = (this.chatListDiv == null && entry.role === "admin") ? "* "
						: (this.chatListDiv == null && entry.role === "leagueAdmin") ? "+ " : "";
					players.push(marker + entry.name);
				}
				this.playerListener(players);
			}

			if (this.chatListDiv != null)
				this.renderUserList(userEntries);
			// ==== end tabs-account ====

			var that = this;

			if (processAgain)
				setTimeout(function () {
					that.updateChatMessages();
				}, that.chatUpdateInterval);
		}
	},

	// ==== tabs-account: user list rendering and its context menu ====
	userListSignature:null,
	lastUserEntries:null,       // the entries last rendered, for re-rendering when the ignore list changes
	ignoredPlayers:null,        // names this player ignores (null until loaded)
	ignoresRequested:false,
	userMenu:null,              // {element, name, opener} while a user menu is open
	userListStatus:null,

	// <user> elements -> [{name, role, self, incognito}].  Tolerates the old "* name" / "+ name" prefixes too.
	readUsers:function (users) {
		var result = [];
		for (var i = 0; i < users.length; i++) {
			var user = users[i];
			var text = user.textContent != null ? user.textContent : (user.childNodes[0] ? user.childNodes[0].nodeValue : "");
			var name = String(text || "");
			var role = user.getAttribute("role");
			var prefix = /^([*+]) (.+)$/.exec(name);
			if (prefix) {
				name = prefix[2];
				if (!role)
					role = prefix[1] === "*" ? "admin" : "leagueAdmin";
			}
			result.push({
				name: name,
				role: role || null,
				self: user.getAttribute("self") === "true",
				incognito: user.getAttribute("incognito") === "true"
			});
		}
		return result;
	},

	// Re-renders only when something shown changed, and keeps keyboard focus on the same name.
	renderUserList:function (entries) {
		var that = this;
		this.lastUserEntries = entries;
		if (this.ignoredPlayers == null && !this.ignoresRequested && this.comm.getIgnoredPlayers) {
			this.ignoresRequested = true;
			this.comm.getIgnoredPlayers(function (json) {
				that.setIgnoredPlayers(json && json.ignored ? json.ignored : []);
			}, {"0": function () {}, "401": function () {}, "404": function () {}, "500": function () {}});
			$(document).on("gemp:ignores-changed", function (e, list) {
				that.setIgnoredPlayers(list);
			});
		}

		var ordered = entries.filter(function (u) { return u.self; })
			.concat(entries.filter(function (u) { return !u.self; }));
		this.lockUserListWidth(ordered);
		var ignored = this.ignoredPlayers || [];
		var signature = JSON.stringify([ordered, ignored]);
		if (signature === this.userListSignature)
			return;
		this.userListSignature = signature;

		var focusedName = null;
		var active = document.activeElement;
		if (active && $(active).closest(this.chatListDiv).length > 0)
			focusedName = $(active).closest(".chatUser").attr("data-name");

		this.chatListDiv.empty().attr("role", "list").attr("aria-label", "Players in the hall");
		for (var i = 0; i < ordered.length; i++)
			this.chatListDiv.append(this.createUserEntry(ordered[i]));

		if (focusedName != null) {
			this.chatListDiv.find(".chatUser").each(function () {
				if ($(this).attr("data-name") === focusedName) {
					var target = $(this).find(".chatUser-visibility");
					(target.length > 0 ? target : $(this)).trigger("focus");
				}
			});
		}
	},

	isIgnored:function (name) {
		var lower = String(name).toLowerCase();
		return (this.ignoredPlayers || []).some(function (n) { return String(n).toLowerCase() === lower; });
	},

	setIgnoredPlayers:function (list) {
		this.ignoredPlayers = (list || []).slice();
		this.userListSignature = null;
		if (this.lastUserEntries && this.chatListDiv != null)
			this.renderUserList(this.lastUserEntries);
	},

	// The list's column keeps one width whatever the list holds: exactly wide enough for your own entry at its widest
	// (your name, your role badge if any, and the chip reading "incognito" or "online", whichever is wider).  It is
	// measured once and again only if your name or role changes, so going incognito, ignoring someone, players
	// coming and going, the status line or the Discord button never move it.  Longer names are cut with "…".
	userListWidthKey:null,

	lockUserListWidth:function (ordered) {
		// only the hall's list has a column of its own (#userListColumn in hall.html)
		var column = this.chatListDiv != null ? this.chatListDiv.parent("#userListColumn") : $();
		if (column.length === 0)
			return;
		var own = ordered.length > 0 && ordered[0].self ? ordered[0] : null;
		var name = own ? own.name : this.userName;
		if (name == null || name === "")
			return;
		var role = own ? own.role : null;
		var key = JSON.stringify([name, role]);
		if (key === this.userListWidthKey)
			return;

		var list = this.chatListDiv[0];
		var probe = this.createUserEntry({name: name, role: role, self: true, incognito: true});
		probe.attr("aria-hidden", "true").removeAttr("role")
			.css({position: "absolute", visibility: "hidden", left: 0, top: 0, width: "max-content", flexWrap: "nowrap"});
		this.chatListDiv.append(probe);
		var chip = probe.find(".chatUser-visibility");
		var widest = 0;
		$.each(["incognito", "online"], function (i, text) {
			chip.text(text);
			widest = Math.max(widest, probe[0].getBoundingClientRect().width);
		});
		probe.remove();
		if (!(widest > 0))
			return;       // not laid out (hidden): measured on a later update

		var style = window.getComputedStyle(list);
		var px = function (value) { return parseFloat(value) || 0; };
		// the list's own padding, borders and (always shown) scrollbar sit around the entries
		var chrome = (list.offsetWidth - list.clientWidth) + px(style.paddingLeft) + px(style.paddingRight)
			+ px(style.marginLeft) + px(style.marginRight);
		var columnStyle = window.getComputedStyle(column[0]);
		chrome += px(columnStyle.paddingLeft) + px(columnStyle.paddingRight)
			+ px(columnStyle.borderLeftWidth) + px(columnStyle.borderRightWidth);
		// (+1: sub-pixel text widths round the other way in some browsers, which would cut your own name)
		var width = Math.ceil(widest) + 1 + Math.ceil(chrome) + "px";
		column.css({boxSizing: "border-box", width: width, minWidth: width, maxWidth: width, flex: "0 0 auto"});
		this.userListWidthKey = key;
	},

	createUserEntry:function (user) {
		var that = this;
		var entry = $("<div class='chatUser' role='listitem'></div>").attr("data-name", user.name);
		// a name wider than the column is cut with "…"; the tooltip has it in full
		entry.append($("<span class='chatUser-name'></span>").text(user.name).attr("title", user.name));
		if (user.role === "admin")
			entry.addClass("chatUser-admin").append($("<span class='chatUser-badge chatUser-role'></span>").text("admin").attr("title", "Administrator"));
		else if (user.role === "leagueAdmin")
			entry.addClass("chatUser-leagueAdmin").append($("<span class='chatUser-badge chatUser-role'></span>").text("league admin").attr("title", "League administrator"));

		if (user.self) {
			// your own entry: listed first, no badge; the chip shows and toggles online / incognito
			entry.addClass("chatUser-self");
			var incognito = user.incognito === true;
			var visibility = $("<button type='button' class='chatUser-visibility'></button>")
				.addClass(incognito ? "is-incognito" : "is-online")
				.text(incognito ? "incognito" : "online")
				.attr("aria-pressed", incognito ? "true" : "false")
				.attr("title", incognito
					? "Hidden from the user list (admins still see you). Click to show as online. Resets when you re-enter the hall."
					: "Shown in the user list. Click to go incognito (hidden from everyone but admins).");
			visibility.on("click", function (e) {
				e.stopPropagation();
				that.toggleIncognito(!incognito);
			});
			entry.append(visibility);
			return entry;
		}

		// ignored players are struck through (CSS), with no visible badge; screen readers still hear it
		if (this.isIgnored(user.name))
			entry.addClass("chatUser-ignored").append($("<span class='visually-hidden'> (ignored)</span>"));
		entry.attr("tabindex", "0").attr("aria-haspopup", "menu")
			.attr("title", "Actions for " + user.name);
		entry.on("click", function (e) {
			e.preventDefault();
			that.openUserMenu(user.name, entry);
		});
		entry.on("keydown", function (e) {
			var key = e.key;
			if (key === "Enter" || key === " " || key === "Spacebar" || key === "ContextMenu" || (key === "F10" && e.shiftKey)) {
				e.preventDefault();
				that.openUserMenu(user.name, entry);
			} else if (key === "ArrowDown" || key === "ArrowUp") {
				e.preventDefault();
				var all = that.chatListDiv.find(".chatUser[tabindex]");
				var index = all.index(entry) + (key === "ArrowDown" ? 1 : -1);
				if (index >= 0 && index < all.length)
					all.eq(index).trigger("focus");
			}
		});
		entry.on("contextmenu", function (e) {
			e.preventDefault();
			that.openUserMenu(user.name, entry);
		});
		return entry;
	},

	openUserMenu:function (name, opener) {
		var that = this;
		this.closeUserMenu(false);

		var menu = $("<ul class='chatUserMenu' role='menu'></ul>").attr("aria-label", "Actions for " + name);
		menu.append($("<li class='chatUserMenu-title' role='presentation'></li>").text(name));
		var items = [];
		if (typeof window.gempOpenCasualInvite === "function") {
			items.push({label: "Invite to table", action: function () {
				that.closeUserMenu(false);
				window.gempOpenCasualInvite(name);
			}});
		}
		var ignored = this.isIgnored(name);
		items.push({label: ignored ? "Unignore" : "Ignore", action: function () {
			that.closeUserMenu(true);
			that.changeIgnore(name, !ignored);
		}});
		$.each(items, function (i, item) {
			var li = $("<li role='menuitem' tabindex='-1' class='chatUserMenu-item'></li>").text(item.label);
			li.on("click", function (e) {
				e.preventDefault();
				e.stopPropagation();
				item.action();
			});
			menu.append(li);
		});

		menu.on("keydown", function (e) {
			var all = menu.find("[role=menuitem]");
			var index = all.index(document.activeElement);
			if (e.key === "ArrowDown") {
				e.preventDefault();
				all.eq((index + 1) % all.length).trigger("focus");
			} else if (e.key === "ArrowUp") {
				e.preventDefault();
				all.eq((index - 1 + all.length) % all.length).trigger("focus");
			} else if (e.key === "Home") {
				e.preventDefault();
				all.first().trigger("focus");
			} else if (e.key === "End") {
				e.preventDefault();
				all.last().trigger("focus");
			} else if (e.key === "Enter" || e.key === " " || e.key === "Spacebar") {
				e.preventDefault();
				if (index >= 0)
					all.eq(index).trigger("click");
			} else if (e.key === "Escape") {
				e.preventDefault();
				that.closeUserMenu(true);
			} else if (e.key === "Tab") {
				that.closeUserMenu(false);
			}
		});

		// Anchor the menu to the clicked name: beside it on the left (the list is the rightmost column), flipped to the
		// other side or up when it would leave the window.  It must be absolutely positioned before it is measured or
		// placed: as a static block in <body> it spans the whole page width, which is what used to push it to the
		// left edge of the screen.
		menu.css({position: "absolute", top: 0, left: 0, zIndex: 2000, visibility: "hidden"});
		$("body").append(menu);
		this.positionUserMenu(menu, opener);
		menu.css("visibility", "");
		opener.attr("aria-expanded", "true");

		this.userMenu = {element: menu, name: name, opener: opener};
		menu.find("[role=menuitem]").first().trigger("focus");

		// a click anywhere else closes it
		setTimeout(function () {
			$(document).on("mousedown.chatUserMenu", function (e) {
				if ($(e.target).closest(".chatUserMenu").length === 0)
					that.closeUserMenu(false);
			});
		}, 0);
		// scrolling the list would leave the menu beside the wrong name
		if (this.chatListDiv != null)
			this.chatListDiv.on("scroll.chatUserMenu", function () { that.closeUserMenu(false); });
	},

	positionUserMenu:function (menu, opener) {
		if ($.ui && $.ui.position && menu.position) {
			menu.position({my: "right top", at: "left-4 top", of: opener, collision: "flipfit flipfit", within: window});
			return;
		}
		var offset = opener.offset() || {top: 0, left: 0};
		menu.css({top: offset.top, left: Math.max(4, offset.left - (menu.outerWidth() || 150) - 4)});
	},

	closeUserMenu:function (returnFocus) {
		$(document).off("mousedown.chatUserMenu");
		if (this.chatListDiv != null)
			this.chatListDiv.off("scroll.chatUserMenu");
		if (this.userMenu == null)
			return;
		var menu = this.userMenu;
		this.userMenu = null;
		menu.element.remove();
		menu.opener.removeAttr("aria-expanded");
		if (returnFocus) {
			// the list may have been re-rendered meanwhile: focus the current entry for that name
			var current = this.chatListDiv ? this.chatListDiv.find(".chatUser").filter(function () {
				return $(this).attr("data-name") === menu.name;
			}) : $();
			(current.length > 0 ? current : menu.opener).trigger("focus");
		}
	},

	changeIgnore:function (name, ignore) {
		var that = this;
		var call = ignore ? this.comm.ignorePlayer : this.comm.unignorePlayer;
		call.call(this.comm, name, function (json) {
			if (json && json.ignored) {
				that.setIgnoredPlayers(json.ignored);
				$(document).trigger("gemp:ignores-changed", [json.ignored]);
			}
			that.showUserListStatus(json && json.message ? json.message : (ignore ? "Ignored " : "Unignored ") + name + ".");
		}, {
			"0": function () { that.showUserListStatus("Could not reach the server."); },
			"401": function () { that.showUserListStatus("You are not logged in."); },
			"500": function () { that.showUserListStatus("Could not change your ignore list. Try again later."); }
		});
	},

	toggleIncognito:function (incognito) {
		var that = this;
		this.comm.setIncognito(incognito, function (json) {
			var now = json ? json.incognito === true : incognito;
			// show the new state straight away; the next chat update confirms it
			if (that.lastUserEntries) {
				$.each(that.lastUserEntries, function (i, u) {
					if (u.self)
						u.incognito = now;
				});
				that.userListSignature = null;
				that.renderUserList(that.lastUserEntries);
			}
			that.showUserListStatus(now ? "No longer showing as online" : "Now showing as online");
		}, {
			"0": function () { that.showUserListStatus("Could not reach the server."); },
			"401": function () { that.showUserListStatus("You are not logged in."); },
			"409": function () { that.showUserListStatus("You are not in the hall chat. Reload the page and try again."); },
			"500": function () { that.showUserListStatus("Could not change your visibility. Try again later."); }
		});
	},

	showUserListStatus:function (text) {
		if (this.chatListDiv == null)
			return;
		if (this.userListStatus == null) {
			this.userListStatus = $("<div class='chatUserStatus' role='status' aria-live='polite'></div>");
			this.chatListDiv.after(this.userListStatus);
		}
		var status = this.userListStatus;
		status.text(text).show();
		clearTimeout(this.userListStatusTimer);
		this.userListStatusTimer = setTimeout(function () { status.fadeOut(400); }, 6000);
	},
	// ==== end tabs-account ====

	updateChatMessages:function () {
		var that = this;

		this.comm.updateChat(this.name, function (xml) {
			that.processMessages(xml, true);
		}, this.chatErrorMap());
	},

	sendMessage:function (message) {
		var that = this;
		this.comm.sendChatMessage(this.name, message, this.chatErrorMap());
		
		//this.chatEmbed.emit("sendMessage", message);
	},

	chatMalfunction: function() {
		this.stopUpdates = true;
		this.chatTalkDiv.prop('disabled', true);
		this.chatTalkDiv.css({"background-color": "#ff9999"});
		
		if(this.discordDiv)
		{
			this.discordDiv.prop('disabled', true);
			this.discordDiv.css({"background-color": "#ff9999"});
		}
	},

	// The same chat failure can be reported by more than one request (start and update both get a 401, say);
	// show each warning once in a row rather than repeating it.
	lastWarningText:null,
	appendWarningOnce:function (text, msgClass) {
		if (text === this.lastWarningText)
			return;
		this.lastWarningText = text;
		this.appendMessage(text, msgClass);
	},

	chatErrorMap:function() {
		var that = this;
		return {
			"0":function() {
				that.chatMalfunction();
				that.appendWarningOnce("Chat server has been closed or there was a problem with your internet connection.", "warningMessage");
			},
			"401":function() {
				that.chatMalfunction();
				that.appendWarningOnce(ChatBoxUI.NOT_LOGGED_IN, "warningMessage");
			},
			"403": function() {
				that.chatMalfunction();
				that.appendWarningOnce("You have no permission to participate in this chat.", "warningMessage");
			},
			"404": function() {
				that.chatMalfunction();
				that.appendWarningOnce("Chat room is closed.", "warningMessage");
			},
			"410": function() {
				that.chatMalfunction();
				that.appendWarningOnce("You have been inactive for too long and were removed from the chat room. Refresh the page if you wish to re-enter.", "warningMessage");
			}
		};
	},
	
	
});

// The chat's "not logged in" warning (markup: fixed text; the link's address is set when it is clicked).
ChatBoxUI.NOT_LOGGED_IN = "You are not logged in. "
	+ "<a class='chat-login-link' href='/gemp-lotr/'>Click here to register or log in.</a>";

// The login page; in the hall it comes back to the current hall link once logged in (GempLinks).
ChatBoxUI.loginUrl = function () {
	return window.GempLinks ? GempLinks.loginUrl() : "/gemp-lotr/";
};
