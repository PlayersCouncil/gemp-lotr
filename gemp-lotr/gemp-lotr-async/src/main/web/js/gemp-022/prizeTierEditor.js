/**
 * Editor for the prize tiers of an event (league or tournament): a list of tiers, each saying who qualifies
 * (a range of places, or a minimum number of games played) and what they get (cards / packs by id, or promises:
 * prizes whose card does not exist yet and are handed out as "to be announced" placeholders).
 *
 * Usage:
 *   var editor = new PrizeTierEditor($("#prize-tiers"), {
 *       comm: hall.comm,                        // for the live card-name lookup (getCardName)
 *       campaignInput: $("#league-campaign")    // optional; when given, participation tiers can count games
 *   });                                          // across the campaign named in that input
 *   editor.setTiers(tiers);    // array of PrizeTier-shaped objects ({kind, from, to, games, scope, label, items:[{blueprintId | promise, count}]}); [] clears
 *   editor.getTiers();         // -> the same shape, ready to JSON.stringify for the "prizeTiers" parameter
 *   editor.validate();         // -> null when everything is filled in, else a message for the admin (the offending row is highlighted)
 *   editor.clear();
 *
 * Nothing here is type specific: the tier shape is what the server's PrizeTier / PrizeItem classes deserialise.
 */
var PrizeTierEditor = Class.extend({
    container: null,
    comm: null,
    campaignInput: null,
    list: null,
    lookupDelay: 400,
    lookupSerial: 0,

    init: function (container, options) {
        var that = this;
        options = options || {};
        this.container = container;
        this.comm = options.comm || null;
        this.campaignInput = options.campaignInput || null;
        if (options.lookupDelay != null)
            this.lookupDelay = options.lookupDelay;

        container.empty().addClass("prize-tier-editor");
        this.list = $("<div class='prize-tier-list'></div>");
        container.append(this.list);
        container.append($("<div class='prize-tier-empty'>No extra prizes. Players still get the automatic prizes of the event.</div>"));
        container.append($("<button type='button' class='prize-tier-add'>Add prize</button>").button().click(function () {
            that._addTierRow(null);
            that._refresh();
        }));

        // the campaign scope option is labelled with the campaign the admin typed
        if (this.campaignInput != null) {
            this.campaignInput.on("input change", function () { that._refreshScopeLabels(); });
        }
        this._refresh();
    },

    // ---- public API ----

    setTiers: function (tiers) {
        this.list.empty();
        tiers = tiers || [];
        for (var i = 0; i < tiers.length; i++)
            this._addTierRow(tiers[i]);
        this._refresh();
    },

    getTiers: function () {
        var that = this;
        var tiers = [];
        this.list.find(".prize-tier").each(function () {
            tiers.push(that._readTier($(this)));
        });
        return tiers;
    },

    clear: function () {
        this.setTiers([]);
    },

    /**
     * @return null when the tiers are complete, otherwise a message; the first offending row is marked with the
     * class "prize-invalid" (cleared again on the next validate or edit)
     */
    validate: function () {
        var that = this;
        var problem = null;
        this.list.find(".prize-invalid").removeClass("prize-invalid");
        this.list.find(".prize-tier").each(function (index) {
            if (problem != null)
                return;
            var row = $(this);
            var tier = that._readTier(row);
            var n = "Prize tier " + (index + 1);
            if (tier.kind == "PLACEMENT") {
                if (tier.from < 1) {
                    problem = {row: row, message: n + ": the first place must be 1 or more."};
                    return;
                }
                if (tier.to < tier.from) {
                    problem = {row: row, message: n + ": the last place must not be before the first place."};
                    return;
                }
            } else {
                if (tier.games < 0) {
                    problem = {row: row, message: n + ": the number of games cannot be negative."};
                    return;
                }
                if (tier.scope == "CAMPAIGN" && that.campaignInput != null && !$.trim(that.campaignInput.val())) {
                    problem = {row: row, message: n + " counts games across the campaign, so the campaign needs a name."};
                    return;
                }
            }
            if (tier.label != null && tier.label.length > 255) {
                problem = {row: row, message: n + ": the label must be 255 characters or less."};
                return;
            }
            var itemRows = row.find(".prize-item");
            if (itemRows.length == 0) {
                problem = {row: row, message: n + " awards nothing; add at least one item or remove the tier."};
                return;
            }
            itemRows.each(function (itemIndex) {
                if (problem != null)
                    return;
                var itemRow = $(this);
                var item = that._readItem(itemRow);
                var m = n + ", item " + (itemIndex + 1);
                if (item.count < 1) {
                    problem = {row: itemRow, message: m + ": the count must be at least 1."};
                } else if (item.promise != null) {
                    if (item.promise.length > 255)
                        problem = {row: itemRow, message: m + ": the description must be 255 characters or less."};
                    else if (!item.promise)
                        problem = {row: itemRow, message: m + ": say what will be awarded (the promise needs a description)."};
                } else if (!item.blueprintId) {
                    problem = {row: itemRow, message: m + ": enter a card or pack id, or mark it as to be announced."};
                } else if (itemRow.data("lookupKind") == "unknown" && itemRow.data("lookupId") == item.blueprintId) {
                    problem = {row: itemRow, message: m + ": '" + item.blueprintId + "' is not a known card or pack."};
                }
            });
        });
        if (problem == null)
            return null;
        problem.row.addClass("prize-invalid");
        return problem.message;
    },

    // ---- rows ----

    _addTierRow: function (tier) {
        var that = this;
        tier = tier || {};
        var kind = tier.kind == "PARTICIPATION" ? "PARTICIPATION" : "PLACEMENT";

        var row = $("<div class='prize-tier'></div>");
        var head = $("<div class='prize-tier-head flex-horiz'></div>");
        head.append("<span class='prize-tier-index'></span>");

        var kindSelect = $("<select class='prize-tier-kind'></select>")
            .append($("<option value='PLACEMENT'>Placement</option>"))
            .append($("<option value='PARTICIPATION'>Participation</option>"))
            .val(kind);
        head.append(kindSelect);

        var placement = $("<span class='prize-tier-placement field-inline'></span>");
        placement.append("places ");
        placement.append($("<input type='number' min='1' class='prize-tier-from'>").val(tier.from != null ? tier.from : 1));
        placement.append(" to ");
        placement.append($("<input type='number' min='1' class='prize-tier-to'>").val(tier.to != null ? tier.to : (tier.from != null ? tier.from : 1)));
        head.append(placement);

        var participation = $("<span class='prize-tier-participation field-inline'></span>");
        participation.append("at least ");
        participation.append($("<input type='number' min='0' class='prize-tier-games'>").val(tier.games != null ? tier.games : 1));
        participation.append(" games ");
        if (this.campaignInput != null) {
            var scope = $("<select class='prize-tier-scope'></select>")
                .append($("<option value='EVENT'>in this league</option>"))
                .append($("<option value='CAMPAIGN'>across the campaign</option>"))
                .val(tier.scope == "CAMPAIGN" ? "CAMPAIGN" : "EVENT");
            participation.append(scope);
        }
        head.append(participation);

        head.append($("<input type='text' class='prize-tier-label flex-fill' maxlength='255' placeholder='label (optional)'>").val(tier.label || ""));

        var buttons = $("<span class='prize-tier-buttons'></span>");
        buttons.append($("<button type='button' class='prize-tier-up'>Up</button>").button().click(function () {
            var prev = row.prev(".prize-tier");
            if (prev.length > 0) prev.before(row);
            that._refresh();
        }));
        buttons.append($("<button type='button' class='prize-tier-down'>Down</button>").button().click(function () {
            var next = row.next(".prize-tier");
            if (next.length > 0) next.after(row);
            that._refresh();
        }));
        buttons.append($("<button type='button' class='prize-tier-remove'>Remove</button>").button().click(function () {
            row.remove();
            that._refresh();
        }));
        head.append(buttons);
        row.append(head);

        var items = $("<div class='prize-items'></div>");
        row.append(items);
        var itemList = tier.items || [];
        for (var i = 0; i < itemList.length; i++)
            this._addItemRow(items, itemList[i]);
        if (itemList.length == 0)
            this._addItemRow(items, null);

        row.append($("<button type='button' class='prize-item-add'>Add item</button>").button().click(function () {
            that._addItemRow(items, null);
            that._refresh();
        }));

        kindSelect.on("change", function () { that._applyKind(row); });
        row.on("input change", "input, select", function () {
            row.removeClass("prize-invalid").find(".prize-invalid").removeClass("prize-invalid");
        });
        this._applyKind(row);
        this.list.append(row);
        return row;
    },

    _applyKind: function (row) {
        var kind = row.find(".prize-tier-kind").val();
        row.find(".prize-tier-placement").toggle(kind == "PLACEMENT");
        row.find(".prize-tier-participation").toggle(kind == "PARTICIPATION");
    },

    _addItemRow: function (items, item) {
        var that = this;
        item = item || {};
        var isPromise = item.promise != null && item.promise !== "";

        var row = $("<div class='prize-item flex-horiz'></div>");
        row.append($("<input type='number' min='1' class='prize-item-count'>").val(item.count != null ? item.count : 1));
        row.append("<span class='prize-item-x'>x</span>");

        var typeSelect = $("<select class='prize-item-type'></select>")
            .append($("<option value='card'>Card / pack id</option>"))
            .append($("<option value='promise'>To be announced</option>"))
            .val(isPromise ? "promise" : "card");
        row.append(typeSelect);

        var cardPart = $("<span class='prize-item-card field-inline'></span>");
        var idInput = $("<input type='text' class='prize-item-id' placeholder='e.g. 9_1* or a pack name'>").val(item.blueprintId || "");
        cardPart.append(idInput);
        cardPart.append("<span class='prize-item-name'></span>");
        row.append(cardPart);

        var promisePart = $("<span class='prize-item-promise-part field-inline'></span>");
        promisePart.append($("<input type='text' class='prize-item-promise flex-fill' maxlength='255' placeholder='what will be awarded, e.g. 2026 WC Champion promo'>").val(isPromise ? item.promise : ""));
        row.append(promisePart);

        row.append($("<button type='button' class='prize-item-remove'>Remove</button>").button().click(function () {
            row.remove();
            that._refresh();
        }));

        var lookupTimer = null;
        idInput.on("input", function () {
            clearTimeout(lookupTimer);
            var value = $.trim(idInput.val());
            row.removeData("lookupKind").removeData("lookupId");
            if (!value) {
                row.find(".prize-item-name").empty();
                return;
            }
            row.find(".prize-item-name").text("...");
            lookupTimer = setTimeout(function () { that._lookupName(row, value); }, that.lookupDelay);
        });
        typeSelect.on("change", function () { that._applyItemType(row); });
        this._applyItemType(row);
        items.append(row);
        if (!isPromise && item.blueprintId)
            this._lookupName(row, $.trim(item.blueprintId));
        return row;
    },

    _applyItemType: function (row) {
        var promise = row.find(".prize-item-type").val() == "promise";
        row.find(".prize-item-card").toggle(!promise);
        row.find(".prize-item-promise-part").toggle(promise);
    },

    /**
     * Asks the server what an id stands for and shows the answer next to the input: the name as a card hint (so
     * clicking it shows the card), or "unknown".
     */
    _lookupName: function (row, blueprintId) {
        var that = this;
        var nameSpan = row.find(".prize-item-name");
        if (this.comm == null || typeof this.comm.getCardName != "function") {
            nameSpan.empty();
            return;
        }
        var serial = ++this.lookupSerial;
        row.data("lookupSerial", serial);
        this.comm.getCardName(blueprintId, function (json) {
            if (row.data("lookupSerial") != serial)
                return;
            if (json == null || json.kind == null || json.kind == "unknown" || json.kind == "placeholder") {
                row.data("lookupKind", "unknown").data("lookupId", blueprintId);
                nameSpan.html("<span class='prize-item-unknown'>unknown</span>");
                return;
            }
            row.data("lookupKind", json.kind).data("lookupId", blueprintId);
            var name = json.name != null ? json.name : blueprintId;
            var hint = $("<div class='cardHint'></div>").attr("value", blueprintId).text(name);
            nameSpan.empty().append(hint);
            if (json.kind == "pack")
                nameSpan.append(" <span class='prize-item-kind'>(pack)</span>");
        }, {
            "0": function () { nameSpan.empty(); },
            "400": function () { nameSpan.empty(); },
            "401": function () { nameSpan.empty(); },
            "403": function () { nameSpan.empty(); },
            "404": function () { nameSpan.empty(); },
            "500": function () { nameSpan.empty(); }
        });
    },

    _readTier: function (row) {
        var kind = row.find(".prize-tier-kind").val() == "PARTICIPATION" ? "PARTICIPATION" : "PLACEMENT";
        var tier = {
            kind: kind,
            from: 1,
            to: 1,
            games: 0,
            scope: "EVENT",
            label: $.trim(row.find(".prize-tier-label").val()) || null,
            items: []
        };
        if (kind == "PLACEMENT") {
            tier.from = this._toInt(row.find(".prize-tier-from").val(), 0);
            tier.to = this._toInt(row.find(".prize-tier-to").val(), 0);
        } else {
            tier.games = this._toInt(row.find(".prize-tier-games").val(), -1);
            var scope = row.find(".prize-tier-scope");
            tier.scope = (scope.length > 0 && scope.val() == "CAMPAIGN") ? "CAMPAIGN" : "EVENT";
        }
        var that = this;
        row.find(".prize-item").each(function () {
            tier.items.push(that._readItem($(this)));
        });
        return tier;
    },

    _readItem: function (row) {
        var promise = row.find(".prize-item-type").val() == "promise";
        return {
            blueprintId: promise ? null : $.trim(row.find(".prize-item-id").val()),
            promise: promise ? $.trim(row.find(".prize-item-promise").val()) : null,
            count: this._toInt(row.find(".prize-item-count").val(), 0)
        };
    },

    _toInt: function (value, fallback) {
        var n = parseInt(value, 10);
        return isNaN(n) ? fallback : n;
    },

    _refresh: function () {
        var rows = this.list.find(".prize-tier");
        rows.each(function (index) {
            var row = $(this);
            row.find(".prize-tier-index").text((index + 1) + ".");
            row.find(".prize-tier-up").button("option", "disabled", index == 0);
            row.find(".prize-tier-down").button("option", "disabled", index == rows.length - 1);
        });
        this.container.find(".prize-tier-empty").toggle(rows.length == 0);
        this._refreshScopeLabels();
    },

    _refreshScopeLabels: function () {
        if (this.campaignInput == null)
            return;
        var campaign = $.trim(this.campaignInput.val());
        var text = campaign ? "across the \"" + campaign + "\" campaign" : "across the campaign";
        this.list.find(".prize-tier-scope option[value='CAMPAIGN']").text(text);
    }
});
