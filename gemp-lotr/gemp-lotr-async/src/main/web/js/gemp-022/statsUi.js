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

                that.communication.getStats(startDay, period, that.loadedStats, {
                    "400":function () {
                        alert("Invalid parameter entered");
                    }
                })
            });
    },
    
    getPercentage:function (num1, num2) {
        return Number(num1 / num2).toLocaleString(undefined, {style: 'percent', minimumFractionDigits:2});
    },

    loadedStats:function (json) {
        var that = this;
        log(json);
        
        var getPercentage = (num1, num2) => Number(num1 / num2).toLocaleString(undefined, {style: 'percent', minimumFractionDigits:2});

        $("#startDateSpan").html(json["StartDate"]);
        $("#endDateSpan").html(json["EndDate"]);
        $("#activePlayersStat").html(json["ActivePlayers"]);
        $("#gamesCountStat").html(json["GamesCount"]);

        var formatStats = json["Stats"];
        if (formatStats.length > 0) {
            
            var casualStats = $("#casualStatsTable");
            var compStats = $("#competitiveStatsTable");
            $("#casualStatsTable > tbody").empty();
            $("#competitiveStatsTable > tbody").empty();
            
            var casuals = 0;
            var comps = 0;
            var total = 0;
            
            json.Stats.sort((a,b) => { return b.Count - a.Count; })
            
            json["Stats"].forEach(item => {
                if(item.Casual) {
                    casuals += item.Count;
                }
                else {
                    comps += item.Count;
                }
                total += item.Count;
            });
            
            json["Stats"].forEach(item => {
                
                var test = getPercentage(item.Count, total);
                
                if(item.Casual) {
                    casualStats.append("<tr>" 
                    + "<td>" + item.Format + "</td>"
                    + "<td>" + item.Count + "</td>"
                    + "<td>" + getPercentage(item.Count, casuals) + "</td>"
                    + "<td>" + getPercentage(item.Count, total) + "</td>"
                    + "</tr>");
                }
                else {
                    compStats.append("<tr>" 
                    + "<td>" + item.Format + "</td>"
                    + "<td>" + item.Count + "</td>"
                    + "<td>" + getPercentage(item.Count, comps) + "</td>"
                    + "<td>" + getPercentage(item.Count, total) + "</td>"
                    + "</tr>");
                } 
            });

        }
    }
});