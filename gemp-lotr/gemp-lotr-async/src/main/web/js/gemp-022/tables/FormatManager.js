class FormatManager {
	formats = [];
	sealed = [];
	draft = [];
	tableDraft = [];
	tableDraftTimers = [];
	leagues = [];
	
	updateCallbacks = [];
	
	constructor(comm) {
		this.comm = comm;
	}
	
	registerUpdate(callback) {
		this.updateCallbacks.push(callback);
	}
	
	registerFormatDropdownUpdate(dropdown) {
		var that = this;
		
		this.updateCallbacks.push(() => {
			var currentFormat = dropdown.val();

			dropdown.empty();

			let max = 0;
			let options = {};
			for (const [code, format] of Object.entries(that.formats)) {
				if(!format.hall)
					continue;
				
				var option = $("<option/>")
					.attr("value", code)
					.text(format.name);
				options[format.order] = option;
				if(format.order > max) {
					max = format.order;
				}
			}
			
			for(let i = -1; i <= max; i++) {
				if(Object.hasOwn(options, i)) {
					dropdown.append(options[i]);
				}
			}

			dropdown.val(currentFormat);
			dropdown.change();
		});
	}
	
	registerLeagueDropdownUpdate(dropdown) {
		var that = this;
		
		this.updateCallbacks.push(() => {
			var currentFormat = dropdown.val();

			dropdown.empty();

			let max = 0;
			let options = {};
			for (const league of that.leagues) {
				if(!league.member)
					continue;
				
				var option = $("<option/>")
					.attr("value", league.code)
					.text(league.name);
				dropdown.append(option);
				max++;
			}
			
			if(max == 0) {
				var option = $("<option/>")
					.attr("disabled", "disabled")
					.attr("value", "")
					.text("Register for a League below");
				dropdown.append(option);
			}
			
			dropdown.val("");
			dropdown.change();
		});
	}
	
	updateFormats(mainCallback) {
		var that = this;
		
		that.comm.getFormats(true,
			function (json) 
			{
				that.formats = json.Formats;
				that.sealed = json.SealedTemplates;
				that.draft = json.DraftTemplates;
				that.tableDraft = json.TableDraftTemplates;
				that.tableDraftTimers = json.TableDraftTimerTypes;
				
				that.comm.getLeagues(function(xml) {
					
					that.leagues = [];
					
					var leagues = xml.getElementsByTagName("league");
					for (var i = 0; i < leagues.length; i++) {
							var xleague = leagues[i];
							var league = {};
							league.name = xleague.getAttribute("name");
							league.code = xleague.getAttribute("code");
							league.start = xleague.getAttribute("start");
							league.end = xleague.getAttribute("end");
							league.desc = xleague.getAttribute("desc");
							league.inviteOnly = xleague.getAttribute("inviteOnly") === "true";
							league.member = xleague.getAttribute("member") === "true";
							league.joinable = xleague.getAttribute("joinable") === "true";
							league.draftable = xleague.getAttribute("draftable") === "true";
							
							that.leagues.push(league);
					}
					
					that.updateCallbacks.forEach((callback) => callback());
					
					if(mainCallback !== undefined) {
						mainCallback();	
					}
				}, 
				{
					"400":function () 
					{
						alert("Could not retrieve leagues.");
					}
				});
				
				
			}, 
			{
				"400":function () 
				{
					alert("Could not retrieve formats.");
				}
			});
	}
	
	lookupFormatByName(formatName) {
		var that = this;
		
		var ret = null;
		
		Object.keys(this.formats).forEach(function(code, index) {
			
			if(that.formats[code].name === formatName) {
				ret = code;
				return;
			}
		});
		
		return ret;
	}
	
	
	static processFormatsFromJson(json) {
		var that = this;
		var currentFormat = $("#formatSelect").val();
		
		that.comm.getFormats(false,
			function (json) 
			{
				that.formats = json.Formats;
				that.sealed = jsonSealedTemplates;
				that.draft = json.DraftTemplates;
				that.tableDraft = json.TableDraftTemplates;
				that.tableDraftTimers = json.TableDraftTimerTypes;
				// let max = 0;
				// let options = {};
				// for (const [code, format] of Object.entries(that.formats)) {
				// 	if(!format.hall)
				// 		continue;
					
				// 	var option = $("<option/>")
				// 		.attr("value", code)
				// 		.attr("data-type", "default") // default collection for non-limited formats
				// 		.text(format.name);
				// 	options[format.order] = option;
				// 	if(format.order > max) {
				// 		max = format.order;
				// 	}
				// }
				
				// for(let i = -1; i <= max; i++) {
				// 	if(Object.hasOwn(options, i)) {
				// 		that.formatSelect.append(options[i]);
				// 	}
				// }
				
				
			}, 
			{
				"400":function () 
				{
					alert("Could not retrieve formats.");
				}
			});
	}
	
	
}