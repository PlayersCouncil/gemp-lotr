var StatsUI = Class.extend({
    communication:null,
    paramsDiv:null,
    statsDiv:null,

    init:function (url, paramControl, statControl) {
        this.communication = new GempLotrCommunication(url,
            function (xhr, ajaxOptions, thrownError) {
            });

        this.paramsDiv = paramControl;
        this.statsDiv = statControl;

        var now = new Date();
        var d = now.getDate();
        now.setMonth(now.getMonth() - 1);
        if (now.getDate() != d) {
          now.setDate(0);
        }
        var nowStr = now.getFullYear() + "-" + (1 + now.getMonth()) + "-" + now.getDate();

        $(".startDay").val(nowStr);
        
        var that = this;

        $(".getStats", this.paramsDiv).click(
            function () {
                var startDay = $(".startDay", that.paramsDiv).prop("value");
                var period = $("option:selected", $(".period", that.paramsDiv)).prop("value");

                that.communication.getStats(startDay, period, function (json) {
                    that.shown = {startDay: startDay, period: period};
                    that.loadedStats(json);
                }, {
                    "400":function () {
                        alert("Invalid parameter entered");
                    }
                })
            });
    },

    shown: null,   // {startDay, period} of the stats on screen

    // A data refresh (Server Info calls it when the viewer comes back): the stats on screen are fetched again for the
    // same dates, keeping each table's sort; the date / period fields are left as the viewer has them.  A failure
    // keeps what is shown.
    refresh:function () {
        var that = this;
        if (this.shown == null)
            return;
        var sorts = {competitive: StatsUI.currentSort($("#competitiveStatsTable")),
            casual: StatsUI.currentSort($("#casualStatsTable"))};
        this.communication.getStats(this.shown.startDay, this.shown.period, function (json) {
            that.loadedStats(json, sorts);
        }, {"0": function () {}, "400": function () {}, "401": function () {}, "500": function () {}});
    },
    
    getPercentage:function (num1, num2) {
        return Number(num1 / num2).toLocaleString(undefined, {style: 'percent', minimumFractionDigits:2});
    },

    // sorts: optional {competitive, casual} sort to show each table in (default: by number of games, most first)
    loadedStats:function (json, sorts) {
        log(json);
        sorts = sorts || {};

        var getPercentage = (num1, num2) => num2 > 0
            ? Number(num1 / num2).toLocaleString(undefined, {style: 'percent', minimumFractionDigits:2})
            : "";

        $("#startDateSpan").text(json["StartDate"] == null ? "" : json["StartDate"]);
        $("#endDateSpan").text(json["EndDate"] == null ? "" : json["EndDate"]);
        $("#activePlayersStat").text(json["ActivePlayers"] == null ? "" : json["ActivePlayers"]);
        $("#gamesCountStat").text(json["GamesCount"] == null ? "" : json["GamesCount"]);
        $("#botGamesCountStat").text(json["BotGamesCount"] == null ? "" : json["BotGamesCount"]);

        var formatStats = json["Stats"] || [];
        var casuals = 0;
        var comps = 0;
        var total = 0;
        formatStats.forEach(item => {
            if (item.Casual)
                casuals += item.Count;
            else
                comps += item.Count;
            total += item.Count;
        });

        var columns = function (groupTotal, groupLabel) {
            return [
                {key: "Format", title: "Format name"},
                {key: "Count", title: "# of games"},
                {key: "share", title: "% of " + groupLabel, sortValue: row => row.Count,
                    render: row => getPercentage(row.Count, groupTotal)},
                {key: "ofTotal", title: "% of total", sortValue: row => row.Count,
                    render: row => getPercentage(row.Count, total)}
            ];
        };
        var options = function (emptyText, sort) {
            return {sortable: true, sort: sort || {key: "Count", dir: "desc"}, className: "tables", emptyText: emptyText};
        };

        $("#competitiveStatsTable").empty().append(renderTable(columns(comps, "competitive"),
            formatStats.filter(item => !item.Casual), options("No competitive games in this period.", sorts.competitive)));
        $("#casualStatsTable").empty().append(renderTable(columns(casuals, "casual"),
            formatStats.filter(item => item.Casual), options("No casual games in this period.", sorts.casual)));
    }
});

// The sort a stats table is shown in, as renderTable's options.sort ({key, dir}), or null.  The columns are the
// ones loadedStats builds, in that order.
StatsUI.SORT_KEYS = ["Format", "Count", "share", "ofTotal"];
StatsUI.currentSort = function (container) {
    var result = null;
    container.find("table > thead th").each(function (index) {
        var sort = $(this).attr("aria-sort");
        if ((sort === "ascending" || sort === "descending") && StatsUI.SORT_KEYS[index] != null)
            result = {key: StatsUI.SORT_KEYS[index], dir: sort === "ascending" ? "asc" : "desc"};
    });
    return result;
};
