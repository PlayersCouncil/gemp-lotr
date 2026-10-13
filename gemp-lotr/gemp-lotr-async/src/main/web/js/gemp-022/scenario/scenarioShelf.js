/**
 * My Account > My Scenarios (MyScenariosUI) and Admin > Scenario Admin (ScenarioAdminUI): what a player does with
 * their own scenarios, and how the admins review what is submitted and keep the public ones.  The server's side is
 * ScenarioShelf (GET/POST /scenarios/mine, /scenarios/public, /scenarios/admin).
 *
 * Playing or editing a scenario opens a game in a tab of its own, as everything else does: the tab is opened on the
 * click itself and pointed at the game once the server has answered (a tab opened from the answer has no click behind
 * it, and pop-up blockers stop it).
 */
var ScenarioShelfCommon = {
    KINDS: [
        {kind: "tutorial", heading: "Tutorials"},
        {kind: "puzzle", heading: "Puzzles"},
        {kind: "sandbox", heading: "Sandboxes"}
    ],

    heading: function (kind) {
        for (var i = 0; i < this.KINDS.length; i++)
            if (this.KINDS[i].kind == kind)
                return this.KINDS[i].heading;
        return kind ? String(kind) : "Unreadable";
    },

    /** [{kind, heading, campaigns: [{title, items}]}]: what the server sent (already in order), divided up. */
    group: function (items) {
        var kinds = [];
        var kindOf = {};
        $.each(items, function (i, item) {
            var kind = item.kind || "";
            var group = kindOf[kind];
            if (group == null) {
                group = kindOf[kind] = {kind: kind, heading: ScenarioShelfCommon.heading(item.kind), campaigns: [], campaignOf: {}};
                kinds.push(group);
            }
            var title = item.campaign ? item.campaign.title : "";
            var campaign = group.campaignOf[title];
            if (campaign == null) {
                campaign = group.campaignOf[title] = {title: title, items: []};
                group.campaigns.push(campaign);
            }
            campaign.items.push(item);
        });
        return kinds;
    },

    /** The message a failed request came back with. */
    errorOf: function (xhr, otherwise) {
        try {
            var message = JSON.parse(xhr.responseText).error;
            if (message)
                return message;
        } catch (e) {
            // not one of ours
        }
        var status = (xhr && xhr.status != null) ? xhr.status : 0;
        if (status == 0)
            return "Could not reach the server.";
        if (status == 403)
            return "You may not do that.";
        return otherwise + " (HTTP " + status + ").";
    },

    when: function (ms) {
        if (ms == null)
            return "";
        var date = new Date(ms);
        return date.toLocaleDateString() + " " + date.toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
    },

    /** A tab opened while the click is still a user gesture; null if the browser will not give one. */
    reserveWindow: function () {
        var win = null;
        try {
            win = window.open("", "_blank");
        } catch (e) {
            win = null;
        }
        if (win == null || win.closed)
            return null;
        try {
            win.document.title = "Gemp-LotR";
            win.document.body.style.background = "#111";
        } catch (e) {
            // only cosmetic
        }
        return win;
    },

    openGame: function (win, gameId) {
        var url = "/gemp-lotr/game.html?gameId=" + encodeURIComponent(gameId) + hall.participantIdParam();
        if (win != null && !win.closed) {
            win.location.href = url;
            try {
                win.focus();
            } catch (e) {
                // best effort
            }
            return;
        }
        hall.showReadyModal("game:" + gameId, url, "Your game is ready",
            "Your game has started, but your browser blocked the new tab.", "Open game");
    },

    /**
     * Plays a scenario (by its full id) in a new tab.
     * @param said Told how it went: said(text, kind), kind being "pending", "success" or "error".
     */
    play: function (id, said) {
        var win = this.reserveWindow();
        said("Setting up the table...", "pending");
        hall.comm.startScenario(id, false, function (result) {
            if (result == null || result.gameId == null) {
                if (win != null)
                    win.close();
                said((result && result.error) || "The scenario could not be started.", "error");
                return;
            }
            said("Started in a new tab.", "success");
            ScenarioShelfCommon.openGame(win, result.gameId);
        }, function (xhr) {
            if (win != null)
                win.close();
            said(ScenarioShelfCommon.errorOf(xhr, "The scenario could not be started"), "error");
        });
    },

    /**
     * Opens a scenario in the authoring tool, in a new tab: {id} to open one, {new: true, id, title} for a new one.
     * The author's draft that is open already is given up (they are asked first if it has unsaved changes).
     * @param draft The draft they have open ({dirty, title, id}), or null.
     */
    edit: function (what, draft, said, done) {
        if (draft != null && draft.dirty !== false
            && !confirm("You have a draft open (" + (draft.title || draft.id) + ") with changes that have not been saved. Opening another drops it, and they are lost."))
            return;
        var win = this.reserveWindow();
        said("Opening the authoring tool...", "pending");
        hall.comm.scenarioAuthor("POST", "open", what, function (result) {
            said("Opened in a new tab.", "success");
            ScenarioShelfCommon.openGame(win, result.gameId);
            if (done)
                done();
        }, function (xhr) {
            if (win != null)
                win.close();
            said(ScenarioShelfCommon.errorOf(xhr, "It could not be opened for writing"), "error");
        });
    },

    button: function (text, hint, click, cls) {
        var button = $("<button type='button'></button>").text(text).button().click(click);
        if (hint)
            button.attr("title", hint);
        if (cls)
            button.addClass(cls);
        return button;
    },

    tag: function (text, kind, hint) {
        var tag = $("<span class='shelf-tag'></span>").addClass("shelf-tag-" + kind).text(text);
        if (hint)
            tag.attr("title", hint);
        return tag;
    }
};

/* ====================================================================== My Scenarios */

var MyScenariosUI = function (container) {
    this.container = container;
    this.scenarios = [];
    this.authoring = null;
    this.limit = 0;
    this.busy = false;
    this.build();
    this.refresh();
};

MyScenariosUI.prototype = {
    build: function () {
        var that = this;
        var c = this.container.empty();
        this.draftDiv = $("<div class='shelf-draft' hidden></div>").appendTo(c);
        var tools = $("<div class='shelf-tools'></div>").appendTo(c);
        //A new one, by id and title (the id is what it is known by, and its file's name: made from the title if left empty)
        var id = $("<input type='text' maxlength='60'/>").attr("placeholder", "id (optional)").attr("aria-label", "The new scenario's id")
            .attr("title", "What the scenario is known by, and its file's name: small letters, digits and dashes. Empty: made from the title.");
        var title = $("<input type='text' maxlength='80'/>").attr("placeholder", "Title").attr("aria-label", "The new scenario's title");
        var slug = function (text) {
            return String(text).toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").substring(0, 60);
        };
        title.on("input", function () {
            id.attr("placeholder", slug(title.val()) != "" ? slug(title.val()) : "id (optional)");
        });
        var create = function () {
            var name = $.trim(id.val()) != "" ? $.trim(id.val()) : slug(title.val());
            if (name == "") {
                that.say("Give the new scenario a title (or an id).", "error");
                return;
            }
            ScenarioShelfCommon.edit({"new": true, id: name, title: $.trim(title.val())}, that.authoring, function (text, kind) {
                that.say(text, kind);
            }, function () {
                id.val("");
                title.val("");
                that.refresh();
            });
        };
        title.add(id).keydown(function (event) {
            if (event.keyCode == 13)
                create();
        });
        tools.append($("<span class='shelf-label'></span>").text("New scenario:")).append(title).append(id)
            .append(ScenarioShelfCommon.button("Create", "Start a new scenario in the authoring tool, in a new tab", create));
        tools.append($("<span class='shelf-gap'></span>"));
        tools.append(ScenarioShelfCommon.button("Copy Public...", "Take a copy of one of the public scenarios to change. Submitted and approved, your version replaces it.", function () {
            that.copyPublic();
        }));
        this.countSpan = $("<span class='shelf-count'></span>").appendTo(tools);
        this.status = $("<div class='shelf-status' role='status' aria-live='polite'></div>").appendTo(c);
        this.list = $("<div class='shelf-list'></div>").appendTo(c);
        //Coming back to it after a while (or from the authoring tool's tab), it is fetched again
        if (window.GempHallTabs)
            GempHallTabs.onReturn(this.container, function () {
                that.refresh();
            });
        $(document).off("gemp:scenarios-changed.mine").on("gemp:scenarios-changed.mine", function () {
            that.refresh();
        });
    },

    say: function (text, kind) {
        this.status.text(text || "").attr("data-kind", kind || "");
    },

    refresh: function () {
        var that = this;
        hall.comm.scenarioShelf("GET", "mine", {}, function (json) {
            that.scenarios = json.scenarios || [];
            that.authoring = json.authoring || null;
            that.limit = json.limit || 0;
            that.ownFolder = json.ownFolder !== false;
            that.render();
        }, function (xhr) {
            that.say(ScenarioShelfCommon.errorOf(xhr, "Your scenarios could not be loaded"), "error");
        });
    },

    renderDraft: function () {
        var that = this;
        var draft = this.authoring;
        this.draftDiv.empty().prop("hidden", draft == null);
        if (draft == null)
            return;
        this.draftDiv.append($("<span></span>").append(document.createTextNode("You have a draft open in the authoring tool: "))
            .append($("<b></b>").text(draft.title || draft.id))
            .append(document.createTextNode((draft.test ? " (being played as a player would)" : "")
                + (draft.dirty ? ", with changes that have not been saved." : "."))));
        this.draftDiv.append(ScenarioShelfCommon.button("Continue writing", "Back to the draft, in a new tab", function () {
            var win = ScenarioShelfCommon.reserveWindow();
            hall.comm.scenarioAuthor("POST", "resume", {}, function (result) {
                ScenarioShelfCommon.openGame(win, result.gameId);
            }, function (xhr) {
                if (win != null)
                    win.close();
                that.say(ScenarioShelfCommon.errorOf(xhr, "The draft could not be gone back to"), "error");
                that.refresh();
            });
        }));
        this.draftDiv.append(ScenarioShelfCommon.button("Close it", "Give the draft up. Anything not saved is lost.", function () {
            if (draft.dirty !== false && !confirm("Close the draft? It has changes that have not been saved, and they are lost."))
                return;
            hall.comm.scenarioAuthor("POST", "close", {}, function () {
                that.refresh();
            }, function () {
                that.refresh();
            });
        }));
    },

    render: function () {
        var that = this;
        this.renderDraft();
        this.countSpan.text(this.scenarios.length + " of " + this.limit + " scenarios");
        var list = this.list.empty();
        if (!this.ownFolder) {
            list.append($("<p class='account-help'></p>").text("This server has nowhere to keep players' own scenarios (scenarios.user.path is not set)."));
            return;
        }
        if (this.scenarios.length == 0) {
            list.append($("<p class='account-help'></p>").text("You have no scenarios yet. Create one above, or copy a public one to change."));
            return;
        }
        $.each(ScenarioShelfCommon.group(this.scenarios), function (i, kind) {
            list.append($("<h2 class='shelf-kind'></h2>").text(kind.heading));
            $.each(kind.campaigns, function (j, campaign) {
                if (campaign.title)
                    list.append($("<h3 class='shelf-campaign'></h3>").text(campaign.title));
                else if (kind.campaigns.length > 1)
                    list.append($("<h3 class='shelf-campaign'></h3>").text("Not in a campaign"));
                $.each(campaign.items, function (k, scenario) {
                    list.append(that.row(scenario));
                });
            });
        });
    },

    row: function (scenario) {
        var that = this;
        var row = $("<div class='shelf-row'></div>").attr("data-path", scenario.path);
        var text = $("<div class='shelf-text'></div>").appendTo(row);
        var title = $("<div class='shelf-title'></div>").appendTo(text);
        title.append($("<span class='shelf-title-text'></span>").text(scenario.title || scenario.id || scenario.path));
        if (scenario.campaign && scenario.campaign.position)
            title.append($("<span class='shelf-dim'></span>").text(" #" + scenario.campaign.position));
        var tags = $("<div class='shelf-tags'></div>").appendTo(text);
        if (scenario.status == "submitted")
            tags.append(ScenarioShelfCommon.tag("In review", "review", "Submitted " + ScenarioShelfCommon.when(scenario.submittedAt)
                + ". It cannot be changed until an admin has approved or rejected it."));
        else if (scenario.status == "rejected")
            tags.append(ScenarioShelfCommon.tag("Changes requested", "rejected", "Reviewed by " + (scenario.reviewedBy || "an admin") + " " + ScenarioShelfCommon.when(scenario.reviewedAt)));
        if (scenario.published == "current")
            tags.append(ScenarioShelfCommon.tag("\u2605 Published", "published", "Approved " + ScenarioShelfCommon.when(scenario.reviewedAt)
                + ": this is the public version of " + ((scenario.origin && scenario.origin.title) || "it") + "."));
        else if (scenario.published == "changed")
            tags.append(ScenarioShelfCommon.tag("\u2605 Published; changed since", "published-changed",
                "Approved " + ScenarioShelfCommon.when(scenario.reviewedAt) + ". You have changed it since: submit it again to update the public version."));
        if (!scenario.playable)
            tags.append(ScenarioShelfCommon.tag("Cannot be played", "broken", (scenario.errors || []).join("\n")));
        else if (!scenario.verified)
            tags.append(ScenarioShelfCommon.tag("Does not play through yet", "unverified", "The server plays it through and it does not finish as it says it should. It cannot be submitted until it does."));
        var notes = [];
        if (scenario.origin && scenario.published == null)
            notes.push(scenario.origin.exists ? "A version of the public \u201c" + (scenario.origin.title || scenario.origin.id) + "\u201d: approved, it replaces that one"
                : "Was a version of the public " + scenario.origin.id + ", which is no longer public");
        notes.push(scenario.path);
        text.append($("<div class='shelf-notes'></div>").text(notes.join(" \u00b7 ")));
        if (scenario.status == "rejected" && scenario.feedback)
            text.append($("<div class='shelf-feedback'></div>").append($("<b></b>").text("Changes requested: ")).append(document.createTextNode(scenario.feedback)));
        else if (scenario.status == "rejected")
            text.append($("<div class='shelf-feedback'></div>").text("It was sent back without a note."));

        var buttons = $("<div class='shelf-buttons'></div>").appendTo(row);
        var locked = !!scenario.locked;
        var said = function (text, kind) {
            that.say(text, kind);
        };
        buttons.append(ScenarioShelfCommon.button("Edit", locked ? "It is in review, and cannot be changed. Copy it to go on working on it." : "Open it in the authoring tool, in a new tab", function () {
            ScenarioShelfCommon.edit({id: scenario.fullId}, that.authoring, said, function () {
                that.refresh();
            });
        }).button("option", "disabled", locked || !scenario.openable));
        buttons.append(ScenarioShelfCommon.button("Play", "Play it as a player would, in a new tab", function () {
            ScenarioShelfCommon.play(scenario.fullId, said);
        }).button("option", "disabled", !scenario.playable));
        buttons.append(ScenarioShelfCommon.button("Copy", "Make a copy of it, \u201cCopy of " + (scenario.title || scenario.id) + "\u201d, to change", function () {
            that.post("mine/copy", {path: scenario.path}, "Copied.");
        }).button("option", "disabled", scenario.fullId == null));
        var current = scenario.published == "current";
        buttons.append(ScenarioShelfCommon.button("Submit...", locked ? "It has been submitted"
            : current ? "It is the public version as it stands: change it to submit it again"
            : !scenario.verified ? "It has to play through before it can be submitted" : "Submit it for review, to become a public scenario", function () {
            ScenarioSubmit.ask(scenario.title, function () {
                that.post("mine/submit", {path: scenario.path}, "Submitted for review. It is locked until an admin has approved or rejected it.");
            });
        }).button("option", "disabled", locked || current || !scenario.verified));
        buttons.append(ScenarioShelfCommon.button("Delete", locked ? "It is in review, and cannot be deleted" : "Delete it", function () {
            if (!confirm("Delete \u201c" + (scenario.title || scenario.path) + "\u201d? (If it was a mistake, an admin can put it back.)"))
                return;
            that.post("mine/delete", {path: scenario.path}, "Deleted.");
        }, "shelf-danger").button("option", "disabled", locked));
        return row;
    },

    post: function (what, data, done) {
        var that = this;
        if (this.busy)
            return;
        this.busy = true;
        hall.comm.scenarioShelf("POST", what, data, function (json) {
            that.busy = false;
            that.say(typeof done == "function" ? done(json) : done, "success");
            that.refresh();
        }, function (xhr) {
            that.busy = false;
            that.say(ScenarioShelfCommon.errorOf(xhr, "That could not be done"), "error");
            that.refresh();
        });
    },

    /** The public scenarios, to take a copy of one. */
    copyPublic: function () {
        var that = this;
        var box = $("<div class='shelf-popup'></div>").text("Loading...");
        box.dialog({title: "Copy a public scenario", modal: true, width: Math.min(720, $(window).width() - 40),
            maxHeight: $(window).height() - 80, close: function () {
                box.dialog("destroy").remove();
            }});
        hall.comm.scenarioShelf("GET", "public", {}, function (json) {
            box.empty();
            box.append($("<p class='account-help'></p>").text("A copy goes into your own scenarios, linked to the public one: "
                + "submitted and approved, your version replaces it."));
            var list = $("<div class='shelf-list'></div>").appendTo(box);
            $.each(ScenarioShelfCommon.group(json.scenarios || []), function (i, kind) {
                list.append($("<h2 class='shelf-kind'></h2>").text(kind.heading));
                $.each(kind.campaigns, function (j, campaign) {
                    if (campaign.title)
                        list.append($("<h3 class='shelf-campaign'></h3>").text(campaign.title));
                    $.each(campaign.items, function (k, scenario) {
                        var row = $("<div class='shelf-row'></div>").appendTo(list);
                        var text = $("<div class='shelf-text'></div>").appendTo(row);
                        text.append($("<div class='shelf-title'></div>").text(scenario.title || scenario.id));
                        var notes = [scenario.fullId];
                        if (scenario.author)
                            notes.push("by " + scenario.author);
                        if (scenario.disabled)
                            notes.push("disabled");
                        if (scenario.summary)
                            notes.push(scenario.summary);
                        text.append($("<div class='shelf-notes'></div>").text(notes.join(" \u00b7 ")));
                        $("<div class='shelf-buttons'></div>").appendTo(row).append(ScenarioShelfCommon.button("Copy", "Copy it into your own scenarios", function () {
                            box.dialog("close");
                            that.post("mine/copyPublic", {id: scenario.fullId}, function (result) {
                                return "Copied as " + result.path + ". Edit it from the list below.";
                            });
                        }));
                    });
                });
            });
        }, function (xhr) {
            box.text(ScenarioShelfCommon.errorOf(xhr, "The public scenarios could not be loaded"));
        });
    }
};

/* ====================================================================== Scenario Admin */

var ScenarioAdminUI = function (container) {
    this.container = container;
    this.state = null;
    this.limit = 50;
    this.busy = false;
    this.build();
    this.refresh();
};

ScenarioAdminUI.prototype = {
    build: function () {
        var that = this;
        var c = this.container.empty();
        this.status = $("<div class='shelf-status' role='status' aria-live='polite'></div>").appendTo(c);
        var top = $("<section class='shelf-pane'></section>").appendTo(c);
        top.append($("<h2></h2>").text("Public scenarios"));
        top.append($("<p class='account-help'></p>").text("What players are offered on the Play screen. A disabled scenario is not offered, and cannot be started; "
            + "a game of it that is under way carries on. Deleted ones go to the archive below."));
        this.conflicts = $("<div class='shelf-conflicts'></div>").appendTo(top);
        this.publicList = $("<div class='shelf-list'></div>").appendTo(top);
        var lower = $("<section class='shelf-pane'></section>").appendTo(c);
        lower.append($("<h2></h2>").text("Submitted for review"));
        this.submissions = $("<div class='shelf-list'></div>").appendTo(lower);
        var archive = $("<section class='shelf-pane'></section>").appendTo(c);
        archive.append($("<h2></h2>").text("Archive"));
        archive.append($("<p class='account-help'></p>").text("Every public scenario that was replaced or deleted, and every player's scenario that was deleted. "
            + "Restoring one puts it back where it was; whatever is there now is archived in its turn."));
        this.archive = $("<div class='shelf-list'></div>").appendTo(archive);
        if (window.GempHallTabs)
            GempHallTabs.onReturn(this.container, function () {
                that.refresh();
            });
    },

    say: function (text, kind) {
        this.status.text(text || "").attr("data-kind", kind || "");
    },

    refresh: function () {
        var that = this;
        hall.comm.scenarioShelf("GET", "admin", {limit: this.limit}, function (json) {
            that.state = json;
            that.render();
        }, function (xhr) {
            that.say(ScenarioShelfCommon.errorOf(xhr, "Scenario Admin could not be loaded"), "error");
        });
    },

    post: function (what, data, done, after) {
        var that = this;
        if (this.busy)
            return;
        this.busy = true;
        hall.comm.scenarioShelf("POST", what, data, function (json) {
            that.busy = false;
            that.say(typeof done == "function" ? done(json) : done, "success");
            if (after)
                after(json);
            that.refresh();
        }, function (xhr) {
            that.busy = false;
            that.say(ScenarioShelfCommon.errorOf(xhr, "That could not be done"), "error");
            that.refresh();
        });
    },

    said: function () {
        var that = this;
        return function (text, kind) {
            that.say(text, kind);
        };
    },

    render: function () {
        this.renderConflicts();
        this.renderPublic();
        this.renderSubmissions();
        this.renderArchive();
    },

    renderConflicts: function () {
        var that = this;
        var box = this.conflicts.empty();
        var conflicts = this.state.conflicts || [];
        if (conflicts.length == 0)
            return;
        box.append($("<p class='shelf-warning'></p>").text("These public scenarios and the repository's disagree, and the server has left them as they are:"));
        var why = {
            changed: "changed here and in the repository",
            deleted: "deleted here, and changed in the repository since",
            different: "already here, different from the repository's, before the server kept track"
        };
        $.each(conflicts, function (i, conflict) {
            var row = $("<div class='shelf-row'></div>").appendTo(box);
            $("<div class='shelf-text'></div>").appendTo(row).append($("<div class='shelf-title'></div>").text(conflict.path))
                .append($("<div class='shelf-notes'></div>").text(why[conflict.kind] || conflict.kind));
            $("<div class='shelf-buttons'></div>").appendTo(row)
                .append(ScenarioShelfCommon.button("Take the repository's", "Put the repository's version in its place (what is here now is archived)", function () {
                    if (confirm("Replace " + conflict.path + " with the repository's version? What is here now goes to the archive."))
                        that.post("admin/settle", {path: conflict.path, take: "repository"}, "Took the repository's " + conflict.path + ".");
                }))
                .append(ScenarioShelfCommon.button("Keep ours", "Keep what is here, until the repository changes it again", function () {
                    that.post("admin/settle", {path: conflict.path, take: "ours"}, "Kept the server's " + conflict.path + ".");
                }));
        });
    },

    renderPublic: function () {
        var that = this;
        var list = this.publicList.empty();
        var publics = this.state["public"] || [];
        if (publics.length == 0) {
            list.append($("<p class='account-help'></p>").text("There are no public scenarios."));
            return;
        }
        $.each(ScenarioShelfCommon.group(publics), function (i, kind) {
            list.append($("<h3 class='shelf-kind'></h3>").text(kind.heading));
            $.each(kind.campaigns, function (j, campaign) {
                if (campaign.title)
                    list.append($("<h4 class='shelf-campaign'></h4>").text(campaign.title));
                $.each(campaign.items, function (k, scenario) {
                    list.append(that.publicRow(scenario));
                });
            });
        });
    },

    publicRow: function (scenario) {
        var that = this;
        var row = $("<div class='shelf-row'></div>").toggleClass("shelf-disabled", !!scenario.disabled);
        var text = $("<div class='shelf-text'></div>").appendTo(row);
        var title = $("<div class='shelf-title'></div>").appendTo(text).append($("<span class='shelf-title-text'></span>").text(scenario.title || scenario.path));
        if (scenario.campaign && scenario.campaign.position)
            title.append($("<span class='shelf-dim'></span>").text(" #" + scenario.campaign.position));
        var tags = $("<div class='shelf-tags'></div>").appendTo(text);
        if (scenario.disabled)
            tags.append(ScenarioShelfCommon.tag("Disabled", "disabled", "Not offered to players"));
        if (!scenario.playable)
            tags.append(ScenarioShelfCommon.tag("Cannot be played", "broken", (scenario.errors || []).join("\n")));
        else if (!scenario.verified)
            tags.append(ScenarioShelfCommon.tag("Does not play through", "unverified", "Only admins are offered it"));
        var notes = [scenario.fullId || "(no id)", scenario.path];
        if (scenario.author)
            notes.push("by " + scenario.author);
        if (scenario.approvedFrom)
            notes.push("approved from " + scenario.approvedFrom + " by " + scenario.approvedBy + ", " + ScenarioShelfCommon.when(scenario.approvedAt));
        text.append($("<div class='shelf-notes'></div>").text(notes.join(" \u00b7 ")));

        var buttons = $("<div class='shelf-buttons'></div>").appendTo(row);
        var enabled = $("<input type='checkbox'/>").prop("checked", !scenario.disabled).prop("disabled", scenario.fullId == null).change(function () {
            that.post("admin/disable", {id: scenario.fullId, disabled: !$(this).prop("checked")},
                ($(this).prop("checked") ? "Enabled " : "Disabled ") + (scenario.title || scenario.fullId) + ".");
        });
        buttons.append($("<label class='shelf-toggle'></label>").attr("title", "Whether players are offered it").append(enabled).append(document.createTextNode(" Enabled")));
        buttons.append(ScenarioShelfCommon.button("Play", "Play it, in a new tab", function () {
            ScenarioShelfCommon.play(scenario.fullId, that.said());
        }).button("option", "disabled", !scenario.playable));
        buttons.append(ScenarioShelfCommon.button("Edit public scenario", "Copy it into your own scenarios and open the copy in the authoring tool, in a new tab. "
            + "Submit and approve the copy to replace this one.", function () {
            that.editPublic(scenario);
        }).button("option", "disabled", !scenario.openable));
        buttons.append(ScenarioShelfCommon.button("Delete", "Take it down (it goes to the archive)", function () {
            if (confirm("Delete the public scenario \u201c" + (scenario.title || scenario.path) + "\u201d? Players are no longer offered it. It goes to the archive, from which it can be restored."))
                that.post("admin/delete", {path: scenario.path}, "Deleted " + (scenario.title || scenario.path) + ".");
        }, "shelf-danger"));
        return row;
    },

    /** Copies a public scenario into the admin's own folder, and opens the copy in the authoring tool. */
    editPublic: function (scenario) {
        var that = this;
        if (this.busy)
            return;
        var draft = this.state.authoring || null;
        if (draft != null && draft.dirty !== false
            && !confirm("You have a draft open (" + (draft.title || draft.id) + ") with changes that have not been saved. Opening the copy drops it, and they are lost."))
            return;
        //(the tab is opened now, on the click, and pointed at the tool once the copy and the draft are made)
        var win = ScenarioShelfCommon.reserveWindow();
        this.busy = true;
        this.say("Copying...", "pending");
        var failed = function (xhr) {
            that.busy = false;
            if (win != null)
                win.close();
            that.say(ScenarioShelfCommon.errorOf(xhr, "It could not be copied"), "error");
            that.refresh();
        };
        hall.comm.scenarioShelf("POST", "admin/edit", {id: scenario.fullId}, function (copied) {
            $(document).trigger("gemp:scenarios-changed");
            hall.comm.scenarioAuthor("POST", "open", {id: copied.id}, function (result) {
                that.busy = false;
                that.say("Copied into your scenarios as " + copied.path + ", and opened in a new tab.", "success");
                ScenarioShelfCommon.openGame(win, result.gameId);
                that.refresh();
            }, failed);
        }, failed);
    },

    renderSubmissions: function () {
        var that = this;
        var list = this.submissions.empty();
        var submissions = this.state.submissions || [];
        if (submissions.length == 0) {
            list.append($("<p class='account-help'></p>").text("Nothing is waiting for review."));
            return;
        }
        $.each(submissions, function (i, scenario) {
            list.append(that.submissionRow(scenario));
        });
    },

    submissionRow: function (scenario) {
        var that = this;
        var row = $("<div class='shelf-row shelf-submission'></div>");
        var text = $("<div class='shelf-text'></div>").appendTo(row);
        var title = $("<div class='shelf-title'></div>").appendTo(text);
        title.append($("<span class='shelf-title-text'></span>").text(scenario.title || scenario.path));
        title.append($("<span class='shelf-dim'></span>").text(" (" + ScenarioShelfCommon.heading(scenario.kind).replace(/s$/, "").toLowerCase() + ") by " + scenario.owner));
        var links = $("<span class='shelf-links'></span>").appendTo(title);
        links.append($("<a href='#'></a>").text("Play").attr("title", "Play it as a player would, in a new tab").click(function (event) {
            event.preventDefault();
            ScenarioShelfCommon.play(scenario.fullId, that.said());
        }));
        links.append(document.createTextNode(" \u00b7 "));
        links.append($("<a href='#'></a>").text("Edit").attr("title", "Open it in the authoring tool, in a new tab. What you save goes into " + scenario.owner + "'s file.").click(function (event) {
            event.preventDefault();
            //(asked first, so that the tab can be opened on the click: a draft with unsaved changes is asked about)
            ScenarioShelfCommon.edit({id: scenario.fullId}, that.state.authoring || null, that.said());
        }));
        var tags = $("<div class='shelf-tags'></div>").appendTo(text);
        if (scenario.replaces)
            tags.append(ScenarioShelfCommon.tag("Replaces \u201c" + (scenario.replaces.title || scenario.replaces.id) + "\u201d", "replaces",
                "A version of the public " + scenario.replaces.id + " (" + scenario.replaces.path + ")"));
        else if (scenario.clash)
            tags.append(ScenarioShelfCommon.tag("Id taken", "broken", "A public scenario is called " + scenario.clash.id + " already (" + scenario.clash.path
                + "), and this is not a version of it. Reject it, asking for another id."));
        else
            tags.append(ScenarioShelfCommon.tag("New", "new", "Approving it adds a public scenario"));
        if (!scenario.verified)
            tags.append(ScenarioShelfCommon.tag("Does not play through", "unverified", "It did when it was submitted; the cards or the server may have changed since"));
        var notes = [scenario.fullId, "submitted " + ScenarioShelfCommon.when(scenario.submittedAt)];
        if (scenario.summary)
            notes.push(scenario.summary);
        text.append($("<div class='shelf-notes'></div>").text(notes.join(" \u00b7 ")));

        var buttons = $("<div class='shelf-buttons'></div>").appendTo(row);
        var replacing = scenario.replaces != null;
        buttons.append(ScenarioShelfCommon.button(replacing ? "Replace" : "Approve", replacing
            ? "Make it public in place of \u201c" + (scenario.replaces.title || scenario.replaces.id) + "\u201d" : "Make it a public scenario", function () {
            var question = replacing
                ? "Accepting this replaces the public scenario \u201c" + (scenario.replaces.title || scenario.replaces.id) + "\u201d (" + scenario.replaces.path + ") with "
                    + scenario.owner + "'s submission. The one it replaces goes to the archive.\n\nReplace it?"
                : "Make \u201c" + (scenario.title || scenario.path) + "\u201d a public scenario, offered to every player?";
            if (confirm(question))
                that.post("admin/approve", {owner: scenario.owner, path: scenario.path}, function (result) {
                    return (replacing ? "Replaced: " : "Approved: ") + "it is public as " + result.path + ".";
                });
        }).button("option", "disabled", scenario.clash != null || !scenario.playable));
        var feedback = $("<div class='shelf-reject' hidden></div>");
        var area = $("<textarea rows='4' maxlength='4000'></textarea>").attr("placeholder", "What should be changed? " + scenario.owner + " sees this in My Scenarios.")
            .appendTo(feedback);
        feedback.append($("<div class='shelf-buttons'></div>")
            .append(ScenarioShelfCommon.button("Send it back", "Reject it, with this note", function () {
                that.post("admin/reject", {owner: scenario.owner, path: scenario.path, feedback: area.val()}, "Sent back to " + scenario.owner + ".");
            }))
            .append(ScenarioShelfCommon.button("Cancel", "", function () {
                feedback.prop("hidden", true);
            })));
        buttons.append(ScenarioShelfCommon.button("Reject...", "Send it back, saying what should be changed", function () {
            feedback.prop("hidden", false);
            area.focus();
        }));
        return row.add(feedback);
    },

    renderArchive: function () {
        var that = this;
        var list = this.archive.empty();
        if (!this.state.hasArchive) {
            list.append($("<p class='account-help'></p>").text("This server keeps no archive (scenarios.user.path is not set)."));
            return;
        }
        var records = this.state.archive || [];
        if (records.length == 0) {
            list.append($("<p class='account-help'></p>").text("Nothing has been archived."));
            return;
        }
        $.each(records, function (i, record) {
            var row = $("<div class='shelf-row'></div>").appendTo(list);
            var text = $("<div class='shelf-text'></div>").appendTo(row);
            text.append($("<div class='shelf-title'></div>").text((record.title || record.id || record.path)
                + (record.owner ? " (" + record.owner + "'s)" : " (public)")));
            var notes = [ScenarioShelfCommon.when(record.at), record.reason + (record.by ? " by " + record.by : ""), record.path];
            if (record.restoredAt)
                notes.push("restored " + ScenarioShelfCommon.when(record.restoredAt) + " by " + record.restoredBy);
            text.append($("<div class='shelf-notes'></div>").text(notes.join(" \u00b7 ")));
            $("<div class='shelf-buttons'></div>").appendTo(row).append(ScenarioShelfCommon.button("Restore", "Put it back where it was", function () {
                if (confirm("Put this version of " + (record.owner ? record.owner + "'s " : "the public ") + record.path + " back? Whatever is there now goes to the archive."))
                    that.post("admin/restore", {key: record.key}, "Restored " + record.path + ".", function () {
                        $(document).trigger("gemp:scenarios-changed");
                    });
            }));
        });
        if ((this.state.archived || 0) > records.length)
            list.append(ScenarioShelfCommon.button("Show more", "", function () {
                that.limit += 50;
                that.refresh();
            }));
    }
};
