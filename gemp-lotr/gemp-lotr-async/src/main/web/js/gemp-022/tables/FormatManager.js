/**
 * Formats, leagues and Casual timers for the Play flows, fetched on every Play open (/deck/formats and /league).
 * Dropdowns register for a refill; a value wanted before the options arrive (a remembered format, say) is applied by
 * the refill (TableFlow.want), so nothing comes up blank.  Load failures are reported inline through the element
 * given to setErrorOutput, never with alert().
 */
class FormatManager {
	formats = {};
	sealed = [];
	draft = [];
	tableDraft = [];
	tableDraftTimers = [];
	hallTimers = null;       // [{code, name, minutesPerPlayer, minutesPerDecision, description}] from the server
	leagues = [];
	loaded = false;

	updateCallbacks = [];
	errorOutputs = [];

	constructor(comm) {
		this.comm = comm;
	}

	registerUpdate(callback) {
		this.updateCallbacks.push(callback);
	}

	// Where a load failure is reported (a jQuery element; several may be registered, e.g. one per form).
	setErrorOutput(element) {
		this.errorOutputs.push($(element));
	}

	reportError(message) {
		for (const output of this.errorOutputs)
			output.text(message).prop("hidden", false);
	}

	clearError() {
		for (const output of this.errorOutputs)
			output.text("").prop("hidden", true);
	}

	registerFormatDropdownUpdate(dropdown) {
		var that = this;
		TableFlow.keepWanted(dropdown);

		this.updateCallbacks.push(() => {
			var currentFormat = dropdown.val();

			dropdown.empty();

			let max = 0;
			let options = {};
			for (const [code, format] of Object.entries(that.formats || {})) {
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

			TableFlow.applyWanted(dropdown, currentFormat);
			dropdown.change();
		});
	}

	registerLeagueDropdownUpdate(dropdown) {
		var that = this;
		TableFlow.keepWanted(dropdown);

		this.updateCallbacks.push(() => {
			var currentLeague = dropdown.val();

			dropdown.empty();

			let count = 0;
			for (const league of that.leagues) {
				if(!league.member)
					continue;

				var option = $("<option/>")
					.attr("value", league.code)
					.text(league.name);
				dropdown.append(option);
				count++;
			}

			if(count == 0) {
				var option = $("<option/>")
					.attr("disabled", "disabled")
					.attr("value", "")
					.text("You have not joined a league yet; join one below");
				dropdown.append(option);
				dropdown.val("");
			} else {
				// the wanted / previous league, else the first (so a member of one league has it selected)
				TableFlow.applyWanted(dropdown, currentLeague);
			}
			dropdown.change();
		});
	}

	// Rebuilds a timer dropdown from the server's list (keeps the page's options if the server sent none).
	registerTimerDropdownUpdate(dropdown) {
		var that = this;
		TableFlow.keepWanted(dropdown);

		this.updateCallbacks.push(() => {
			if (!Array.isArray(that.hallTimers) || that.hallTimers.length == 0)
				return;
			var current = dropdown.val();
			dropdown.empty();
			for (const timer of that.hallTimers) {
				dropdown.append($("<option/>")
					.attr("value", timer.code)
					.text(FormatManager.timerLabel(timer)));
			}
			TableFlow.applyWanted(dropdown, current || "default");
			dropdown.change();
		});
	}

	// "Default (45m/6m)", "Glacial (1d/1d)"
	static timerLabel(timer) {
		var name = timer.name == "WC" ? "Championship" : timer.name;
		return name + " (" + FormatManager.minutes(timer.minutesPerPlayer) + "/" + FormatManager.minutes(timer.minutesPerDecision) + ")";
	}

	static minutes(total) {
		total = Number(total) || 0;
		if (total >= 1440 && total % 1440 == 0)
			return (total / 1440) + "d";
		if (total >= 60 && total % 60 == 0 && total > 60)
			return (total / 60) + "h";
		return total + "m";
	}

	findTimer(code) {
		if (!Array.isArray(this.hallTimers))
			return null;
		for (const timer of this.hallTimers) {
			if (String(timer.code).toLowerCase() === String(code).toLowerCase())
				return timer;
		}
		return null;
	}

	getFormat(code) {
		return (this.formats && code != null) ? (this.formats[code] || null) : null;
	}

	getLeague(code) {
		for (const league of this.leagues) {
			if (league.code === code)
				return league;
		}
		return null;
	}

	updateFormats(mainCallback) {
		var that = this;

		that.comm.getFormats(true,
			function (json)
			{
				that.formats = json.Formats || {};
				that.sealed = json.SealedTemplates;
				that.draft = json.DraftTemplates;
				that.tableDraft = json.TableDraftTemplates;
				that.tableDraftTimers = json.TableDraftTimerTypes;
				that.hallTimers = json.HallTimers || null;

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

					that.loaded = true;
					that.clearError();
					that.updateCallbacks.forEach((callback) => callback());

					if(mainCallback !== undefined) {
						mainCallback();
					}
				},
				TableFlow.errorMap(function (message) {
					that.reportError("Could not load your leagues: " + message);
					// the formats did arrive: fill their dropdowns anyway
					that.updateCallbacks.forEach((callback) => callback());
				}));
			},
			TableFlow.errorMap(function (message) {
				that.reportError("Could not load the list of formats: " + message);
			}));
	}

	// The format code whose display name is formatName (decks carry the display name), or null.
	lookupFormatByName(formatName) {
		var that = this;

		var ret = null;

		Object.keys(this.formats || {}).forEach(function(code, index) {

			if(that.formats[code].name === formatName) {
				ret = code;
				return;
			}
		});

		return ret;
	}
}
