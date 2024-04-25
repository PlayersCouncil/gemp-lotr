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

        $("#startDateSpan").html(json["startDate"]);
        $("#endDateSpan").html(json["endDate"]);
        $("#activePlayersStat").html(json["activePlayers"]);
        $("#gamesCountStat").html(json["gamesCount"]);

        var formatStats = json["stats"];
        if (formatStats.length > 0) {
            
            var casualStats = $("#casualStatsTable");
            var compStats = $("#competitiveStatsTable");
            $("#casualStatsTable > tbody").empty();
            $("#competitiveStatsTable > tbody").empty();
            
            var casuals = 0;
            var comps = 0;
            var total = 0;
            
            json.stats.sort((a,b) => { return b.count - a.count; })
            
            json["stats"].forEach(item => {
                if(item.casual) {
                    casuals += item.count;
                }
                else {
                    comps += item.count;
                }
                total += item.count;
            });
            
            json["stats"].forEach(item => {
                
                var test = getPercentage(item.count, total);
                
                if(item.casual) {
                    casualStats.append("<tr>" 
                    + "<td>" + item.format + "</td>"
                    + "<td>" + item.count + "</td>"
                    + "<td>" + getPercentage(item.count, casuals) + "</td>"
                    + "<td>" + getPercentage(item.count, total) + "</td>"
                    + "</tr>");
                }
                else {
                    compStats.append("<tr>" 
                    + "<td>" + item.format + "</td>"
                    + "<td>" + item.count + "</td>"
                    + "<td>" + getPercentage(item.count, comps) + "</td>"
                    + "<td>" + getPercentage(item.count, total) + "</td>"
                    + "</tr>");
                } 
            });

        }
    }
});