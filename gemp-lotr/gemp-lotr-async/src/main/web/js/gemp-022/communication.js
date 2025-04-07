var GempLotrCommunication = Class.extend({
    url:null,
    failure:null,

    init:function (url, failure) {
        this.url = serverDomain + url;
        this.failure = failure;
    },

    errorCheck:function (errorMap) {
        var that = this;
        return function (xhr, status, request) {
            var errorStatus = "" + xhr.status;
            if (errorMap != null && errorMap[errorStatus] != null)
                errorMap[errorStatus](xhr, status, request);
            else if (""+xhr.status != "200")
                that.failure(xhr, status, request);
        };
    },

    logout:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/logout",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap)
        });
    },

    getDelivery:function (callback) {
        $.ajax({
            type:"GET",
            url:this.url + "/delivery/misc",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"xml"
        });
    },
    
    forcePackDelivery:function() {
        if (window.deckbuilderDeliveryService != null) {
            this.getPackDelivery(window.deckbuilderDeliveryService);
        }
    },
    
    getPackDelivery:function (callback) {
        $.ajax({
            type:"GET",
            url:this.url + "/delivery/packContents",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"xml"
        });
    },
    
    getAnnouncementDelivery:function (callback) {
        $.ajax({
            type:"GET",
            url:this.url + "/delivery/announcements",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"json"
        });
    },
    
    snoozeAnnouncement:function (callback) {
        $.ajax({
            type:"POST",
            url:this.url + "/delivery/announcements/snooze",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"html"
        });
    },
    
    dismissAnnouncement:function (callback) {
        $.ajax({
            type:"POST",
            url:this.url + "/delivery/announcements/dismiss",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:callback,
            error:null,
            dataType:"html"
        });
    },
    
    addAnnouncement:function (title, content, start, until, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/delivery/announcements/add",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                title:title,
                content:content,
                start:start,
                until:until
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    previewAnnouncement:function (title, content, start, until, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/delivery/announcements/preview",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                title:title,
                content:content,
                start:start,
                until:until
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },

    deliveryCheck:function (callback) {
        var that = this;
        return function (xml, status, request) {
            var delivery = request.getResponseHeader("Delivery-Service-Package");
            if (delivery == "true" && window.deliveryService != null) {
                that.getDelivery(window.deliveryService);
            }
            var announceDelivery = request.getResponseHeader("Delivery-Service-Announcement");
            if (announceDelivery == "true" && window.announcementDeliveryService != null) {
                that.getAnnouncementDelivery(function(json) {
                    window.announcementDeliveryService(that, json);
                });
            }
            callback(xml);
        };
    },
    
    deckbuilderDeliveryCheck:function (callback) {
        var that = this;
        return function (xml, status, request) {
            var delivery = request.getResponseHeader("Delivery-Service-Opened-Pack");
            if (delivery == "true") {
                that.forcePackDelivery();
            }
            callback(xml);
        };
    },
    
    extractStatus:function (callback) {
        var that = this;
        return function (xml, status, request) {
            callback(xml, request.status);
        };
    },

    getGameHistory:function (start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/gameHistory",
            cache:false,
            data:{
                start:start,
                count:count,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getStats:function (startDay, length, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/stats",
            cache:false,
            data:{
                startDay:startDay,
                length:length,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },

    getPlayerStats:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/playerStats",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLiveTournaments:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getHistoryTournaments:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/history",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getTournament:function (tournamentId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tournament/" + tournamentId,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLeagues:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league",
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getLeague:function (type, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/league/" + type,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    joinLeague:function (code, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/league/" + code,
            cache:false,
            data:{
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },

    getReplay:function (replayId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/replay/" + replayId,
            cache:false,
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    startGameSession:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{ participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateGameState:function (channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getGameCardModifiers:function (cardId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cardInfo",
            cache:false,
            data:{
                cardId:cardId,
                participantId:getUrlParam("participantId") },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    gameDecisionMade:function (decisionId, response, channelNumber, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId"),
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId"),
                decisionId:decisionId,
                decisionValue:response },
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    concede:function (errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/concede",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    cancel:function (errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/game/" + getUrlParam("gameId") + "/cancel",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    shareDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/share",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getLibraryDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/library",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDecks:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/list",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getLibraryDecks:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/deck/libraryList",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getCollectionTypes:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getMerchant:function (filter, ownedMin, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/merchant",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                ownedMin:ownedMin,
                start:start,
                count:count},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    buyItem:function (blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/buy",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    sellItem:function (blueprintId, price, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/sell",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId,
                price:price},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    tradeInFoil:function (blueprintId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/merchant/tradeFoil",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                blueprintId:blueprintId},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getCollection:function (collectionType, filter, start, count, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                filter:filter,
                start:start,
                count:count},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    importCollection:function (decklist, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/collection/import/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                decklist:decklist},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    openPack:function (collectionType, pack, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack
            },
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    openSelectionPack:function (collectionType, pack, selection, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/" + collectionType,
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                pack:pack,
                selection:selection
            },
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    saveDeck:function (deckName, targetFormat, notes, contents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName,
                targetFormat:targetFormat,
                notes:notes,
                deckContents:contents},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    renameDeck:function (oldDeckName, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/rename",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                oldDeckName:oldDeckName,
                deckName:deckName},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    deleteDeck:function (deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/delete",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getDeckStats:function (contents, targetFormat, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/stats",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                targetFormat:targetFormat,
                deckContents:contents},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getFormats:function (includeEvents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/formats",
            cache:true,
            data:{ 
                includeEvents:includeEvents
            },
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    convertErrata:function (targetFormat, contents, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/deck/convert",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                targetFormat:targetFormat,
                deckContents:contents},
            success:this.deckbuilderDeliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    startChat:function (room, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateChat:function (room, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    sendChatMessage:function (room, messages, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/chat/" + room,
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId"),
                message:messages},
            traditional:true,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getHall:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    updateHall:function (callback, channelNumber, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/update",
            cache:false,
            data:{
                channelNumber:channelNumber,
                participantId:getUrlParam("participantId") },
            success:this.deliveryCheck(callback),
            timeout: 20000,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    joinQueue:function (queueId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    leaveQueue:function (queueId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    startQueue:function (queueId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId + "/start",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    confirmReadyCheckQueue:function (queueId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/queue/" + queueId + "/ready",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    joinTournamentLate:function(tournamentId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/tournament/" + tournamentId + "/join",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    registerLimitedTournamentDeck:function(tournamentId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/tournament/" + tournamentId + "/registerdeck",
            cache:false,
            data:{
                participantId:getUrlParam("participantId"),
                deckName:deckName
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    dropFromTournament:function(tournamentId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/tournament/" + tournamentId + "/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    joinTable:function (tableId, deckName, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/" + tableId,
            cache:false,
            data:{
                deckName:deckName,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    leaveTable:function (tableId, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall/"+tableId+"/leave",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    createTable:function (format, deckName, timer, desc, isPrivate, isInviteOnly, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/hall",
            cache:false,
            data:{
                format:format,
                deckName:deckName,
                timer:timer,
                desc:desc,
                isPrivate:isPrivate,
                isInviteOnly:isInviteOnly,
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getFormat:function (formatCode, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/format/" + formatCode,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getFormatRules:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/formats/html",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    getErrata:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/hall/errata/json",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    addTesterFlag:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/addTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    removeTesterFlag:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/removeTesterFlag",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    migrateTrophies:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/collection/migrate",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    getRecentReplays:function (format, count, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/playtesting/getRecentReplays",
            cache:false,
            data:{
                format:format,
                count:count
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    setShutdownMode:function (shutdown, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/shutdown",
            cache:false,
            data:{
                shutdown:shutdown
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    clearServerCache:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/clearCache",
            cache:false,
            data:{},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    reloadCardDefinitions:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/reloadCards",
            cache:false,
            data:{},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    getMOTD:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/admin/getMOTD",
            cache:false,
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    setMOTD:function (motd, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/setMOTD",
            cache:false,
            data:{
                motd:motd
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    addItems:function (collectionType, product, players, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addItems",
            cache:false,
            data:{
                collectionType:collectionType,
                product:product,
                players:players
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    resetUserPassword:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/resetUserPassword",
            cache:false,
            data:{
                login:login
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    permabanUser:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUser",
            cache:false,
            data:{
                login:login
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    tempbanUser:function (login, duration, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banUserTemp",
            cache:false,
            data:{
                login:login,
                duration:duration
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    unbanUser:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/unBanUser",
            cache:false,
            data:{
                login:login
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    susUserSearch:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/findMultipleAccounts",
            cache:false,
            data:{
                login:login
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    banMultiple:function (login, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/banMultiple",
            cache:false,
            data:{
                login:login
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    processSealedLeague:function (preview, start, name, cost, format, serieDuration, maxMatches,
                                       maxRepeatMatches, inviteOnly, description,
                                       topPrize, topCutoff, participationPrize, participationGames,
                                       callback, errorMap) {
        let url = this.url + "/admin/addSealedLeague";
        
        if(preview) {
            url = this.url + "/admin/previewSealedLeague";
        }
        $.ajax({
            type:"POST",
            url:url,
            cache:false,
            data:{
                start:start,
                name:name,
                cost:cost,
                format:format,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                maxRepeatMatches:maxRepeatMatches,
                inviteOnly:inviteOnly,
                description:description,
                topPrize:topPrize,
                topCutoff:topCutoff, 
                participationPrize:participationPrize, 
                participationGames:participationGames,
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    
    processSoloDraftLeague:function (preview, start, name, cost, format, serieDuration, maxMatches,
                                       maxRepeatMatches, inviteOnly, description,
                                       topPrize, topCutoff, participationPrize, participationGames,
                                       callback, errorMap) {
        let url = this.url + "/admin/addSoloDraftLeague";
        
        if(preview) {
            url = this.url + "/admin/previewSoloDraftLeague";
        }
        
        $.ajax({
            type:"POST",
            url:url,
            cache:false,
            data:{
                start:start,
                name:name,
                cost:cost,
                format:format,
                serieDuration:serieDuration,
                maxMatches:maxMatches,
                maxRepeatMatches:maxRepeatMatches,
                inviteOnly:inviteOnly,
                description:description,
                topPrize:topPrize,
                topCutoff:topCutoff, 
                participationPrize:participationPrize, 
                participationGames:participationGames,
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
        
    processConstructedLeague:function (preview, start, collectionType, name, cost, maxRepeatMatches, 
                                       inviteOnly, description,
                                       topPrize, topCutoff, participationPrize, participationGames,
                                       formats, serieDurations, maxMatches,
                                       callback, errorMap) {
        let url = this.url + "/admin/addConstructedLeague";
        
        if(preview) {
            url = this.url + "/admin/previewConstructedLeague";
        }
        
        $.ajax({
            type:"POST",
            url:url,
            cache:false,
            data:{
                start:start,
                collectionType:collectionType,
                name:name,
                cost:cost,
                maxRepeatMatches:maxRepeatMatches,
                topPrize:topPrize,
                topCutoff:topCutoff, 
                participationPrize:participationPrize, 
                participationGames:participationGames,
                format:formats,
                serieDuration:serieDurations,
                maxMatches:maxMatches,
                inviteOnly:inviteOnly,
                description:description
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    
    
    processScheduledTournament:function (preview, name, type, wc, tournamentId, 
                                         formatCode, sealedFormatCode, deckbuildingDuration, turnInDuration,
                                         soloDraftFormatCode, soloDraftDeckbuildingDuration, soloDraftTurnInDuration,
                                         soloTableDraftFormatCode, soloTableDraftDeckbuildingDuration, soloTableDraftTurnInDuration,
                                         tableDraftFormatCode, tableDraftTimer, tableDraftDeckbuildingDuration, tableDraftTurnInDuration,
                                         start, cost, playoff, tiebreaker, prizeStructure, minPlayers, manualKickoff,
                                       callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/processScheduledTournament",
            cache:false,
            data:{
                preview:preview,
                name:name,
                type:type,
                wc:wc,
                tournamentId:tournamentId,
                formatCode:formatCode,
                sealedFormatCode:sealedFormatCode, 
                deckbuildingDuration:deckbuildingDuration, 
                turnInDuration:turnInDuration,
                soloDraftFormatCode:soloDraftFormatCode,
                soloDraftDeckbuildingDuration:soloDraftDeckbuildingDuration,
                soloDraftTurnInDuration:soloDraftTurnInDuration,
                soloTableDraftFormatCode:soloTableDraftFormatCode,
                soloTableDraftDeckbuildingDuration:soloTableDraftDeckbuildingDuration,
                soloTableDraftTurnInDuration:soloTableDraftTurnInDuration,
                tableDraftFormatCode:tableDraftFormatCode,
                tableDraftTimer:tableDraftTimer,
                tableDraftDeckbuildingDuration:tableDraftDeckbuildingDuration,
                tableDraftTurnInDuration:tableDraftTurnInDuration,
                start:start,
                cost:cost,
                playoff:playoff,
                tiebreaker:tiebreaker,
                prizeStructure:prizeStructure,
                minPlayers:minPlayers,
                manualKickoff:manualKickoff
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    setTournamentStage:function(tournamentId, stage, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/setTournamentStage",
            cache:false,
            data:{
                tournamentId:tournamentId,
                stage:stage
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    addLeaguePlayers:function(code, players, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addLeaguePlayers",
            cache:false,
            data:{
                code:code,
                players:players
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    addTables:function (name, tournament, format, timer, playerones, playertwos, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/admin/addTables",
            cache:false,
            data:{
                name:name,
                tournament:tournament,
                format:format,
                timer:timer,
                playerones:playerones,
                playertwos:playertwos
            },
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    
    
    
    //NEVER EVER EVER use this for actual authentication
    // This is strictly to simplify things like auto-hiding
    // of the admin panel.  If you actually need functionality
    // gated behind authorization, it goes on the server
    // and not in here.
    
    getPlayerInfo:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/player",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")
            },
            success:this.deliveryCheck(callback),
            error:this.errorCheck(errorMap),
            dataType:"json"
        });
    },
    
    getStatus:function (callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/",
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    login:function (login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/login",
            cache:false,
            async:false,
            data:{
                login:login,
                password:password,
                participantId:getUrlParam("participantId")},
            success:this.extractStatus(callback),
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    register:function (login, password, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/register",
            cache:false,
            data:{
                login:login,
                password:password,
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getRegistrationForm:function (callback, errorMap) {
        $.ajax({
            type:"POST",
            url:"/gemp-lotr/includes/registrationForm.html",
            cache:false,
            async:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"html"
        });
    },
    getDraft:function (eventId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/soloDraft/" + eventId,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    makeDraftPick:function (eventId, choiceId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/soloDraft/" + eventId,
            cache:false,
            data:{
                choiceId:choiceId,
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    getTableDraft:function (eventId, callback, errorMap) {
        $.ajax({
            type:"GET",
            url:this.url + "/tableDraft/" + eventId,
            cache:false,
            data:{
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    },
    makeTableDraftPick:function (eventId, choiceId, callback, errorMap) {
        $.ajax({
            type:"POST",
            url:this.url + "/tableDraft/" + eventId,
            cache:false,
            data:{
                choiceId:choiceId,
                participantId:getUrlParam("participantId")},
            success:callback,
            error:this.errorCheck(errorMap),
            dataType:"xml"
        });
    }
});
