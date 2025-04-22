var GempLotrDeckBuildingUI = Class.extend({
    comm:null,

    deckDiv:null,

    ringBearerDiv:null,
    ringBearerGroup:null,

    ringDiv:null,
    ringGroup:null,

    siteDiv:null,
    siteGroup:null,

    collectionDiv:null,
    formatSelect:null,
    currentFormat:null,
    notes:null,

    normalCollectionDiv:null,
    normalCollectionGroup:null,

    selectionFunc:null,
    drawDeckDiv:null,

    fpDeckGroup:null,
    shadowDeckGroup:null,

    start:0,
    count:18,
    filter:null,

    deckName:null,

    filterDirty:false,
    deckValidationDirty:true,
    deckContentsDirty:true,

    checkDirtyInterval:500,

    deckListDialog:null,
    selectionDialog:null,
    selectionGroup:null,
    packSelectionId:null,
    deckImportDialog:null,
    notesDialog:null,

    cardFilter:null,

    collectionType:null,

    specialSelection:false,

    init:function () {
        var that = this;

        this.comm = new GempLotrCommunication("/gemp-lotr-server", that.processError);

        this.cardFilter = new CardFilter($("#collectionDiv"),
                function (filter, start, count, callback) {
                    if (!that.specialSelection) {
                        filter = filter + " cardType:-THE_ONE_RING";
                    }
                    that.comm.getCollection(that.collectionType, filter, start, count, function (xml) {
                        callback(xml);
                    }, {
                        "404":function () {
                            alert("You don't have collection of that type.");
                        }
                    });
                },
                function () {
                    that.clearCollection();
                },
                function (elem, type, blueprintId, count) {
                    that.addCardToCollection(type, blueprintId, count, elem.getAttribute("side"), elem.getAttribute("contents"));
                },
                function () {
                    that.finishCollection();
                });
        this.collectionType = "default";

        this.deckDiv = $("#deckDiv");
        
        this.formatSelect = $("#formatSelect");
        $("#formatSelect").change(
                function () {
                    that.deckModified(true);
                });

        var collectionSelect = $("#collectionSelect");

        var newDeckBut = $("#newDeckBut").button();

        var saveDeckBut = $("#saveDeckBut").button();

        var renameDeckBut = $("#renameDeckBut").button();

        var copyDeckBut = $("#copyDeckBut").button();
        
        var importDeckBut = $("#importDeckBut").button();
        
        var libraryListBut = $("#libraryListBut").button();

        var deckListBut = $("#deckListBut").button();
        
        var notesBut = $("#notesBut").button();

        this.deckNameSpan = ("#editingDeck");

        newDeckBut.click(
                function () {
                    that.deckName = null;
                    $("#editingDeck").text("New deck");
                    that.clearDeck();
                });

        saveDeckBut.click(
                function () {
                    if (that.deckName == null) {
                        var newDeckName = prompt("Enter the name of the deck", "");
                        if (newDeckName == null)
                            return;
                        if (newDeckName.length < 3 || newDeckName.length > 100)
                            alert("Deck name has to have at least 3 characters and at most 100 characters.");
                        else {
                            that.deckName = newDeckName;
                            $("#editingDeck").text(newDeckName);
                            that.saveDeck(true);
                        }
                    } else {
                        that.saveDeck(false);
                    }
                });

        renameDeckBut.click(
                function () {
                    if (that.deckName == null) {
                        alert("You can't rename this deck, since it's not named (saved) yet.");
                        return;
                    }
                    that.renameCurrentDeck();
                });

        copyDeckBut.click(
                function () {
                    that.deckName = null;
                    $("#editingDeck").text("New deck");
                });

        deckListBut.click(
                function () {
                    that.loadDeckList();
                });
        
        libraryListBut.click(
                function () {
                    that.loadLibraryList();
                });

        importDeckBut.click(
                function () {
                    that.deckName = null;
                    that.importDecklist();
                });
        
        notesBut.click(
               function () {
                    that.editNotes();
               });

        this.collectionDiv = $("#collectionDiv");

        $("#collectionSelect").change(
                function () {
                    that.collectionType = that.getCollectionType();
                    that.cardFilter.getCollection();
                });

        this.normalCollectionDiv = $("#collection-display");
        this.normalCollectionGroup = new NormalCardGroup(this.normalCollectionDiv, function (card) {
            return true;
        });
        this.normalCollectionGroup.maxCardHeight = 200;

        this.ringBearerDiv = $("#ringBearerDiv");
        this.ringBearerDiv.click(
                function () {
                    if ($(".card", that.ringBearerDiv).length == 0) {
                        that.showPredefinedFilter("canStartWithRing:true type:card", that.ringBearerDiv);
                    }
                });
        this.ringBearerGroup = new NormalFlexGroup(this.ringBearerDiv, function (card) {
            return true;
        }, false);

        this.ringDiv = $("#ringDiv");
        this.ringDiv.click(
                function () {
                    if ($(".card", that.ringDiv).length == 0)
                        that.showPredefinedFilter("cardType:THE_ONE_RING type:card", that.ringDiv);
                });
        this.ringGroup = new NormalFlexGroup(this.ringDiv, function (card) {
            return true;
        }, false);

        this.siteDiv = $("#sitesDiv");
        this.siteGroup = new VerticalFlexGroup(this.siteDiv, function (card) {
            return true;
        }, false);

        this.drawDeckDiv = $("#decksRegion");
        this.fpDeckGroup = new NormalCardGroup($("#freeps-content"), function (card) {
            return (card.zone == "FREE_PEOPLE");
        }, false);
        this.fpDeckGroup.maxCardHeight = 200;
        this.shadowDeckGroup = new NormalCardGroup($("#shadow-content"), function (card) {
            return (card.zone == "SHADOW");
        }, false);
        this.shadowDeckGroup.maxCardHeight = 200;

        this.bottomBarDiv = $("#statsDiv");

        this.selectionFunc = this.addCardToDeckAndLayout;

        $("body").click(
                function (event) {
                    return that.clickCardFunction(event);
                });
        $("body")[0].addEventListener("contextmenu",
            function (event) {
                if(!that.clickCardFunction(event))
                {
                    event.preventDefault();
                    return false;
                }
                return true;
            });
        $("body").mousedown(
                function (event) {
                    return that.dragStartCardFunction(event);
                });
        $("body").mouseup(
                function (event) {
                    return that.dragStopCardFunction(event);
                });

        var width = $(window).width();
        var height = $(window).height();

        this.infoDialog = $("#cardInfoDiv")
                .dialog({
            autoOpen:false,
            closeOnEscape:true,
            resizable:false,
            title:"Card information"
        });

        var swipeOptions = {
            threshold:20,
            swipeUp:function (event) {
                that.infoDialog.prop({ scrollTop:that.infoDialog.prop("scrollHeight") });
                return false;
            },
            swipeDown:function (event) {
                that.infoDialog.prop({ scrollTop:0 });
                return false;
            }
        };
        this.infoDialog.swipe(swipeOptions);

        this.getCollectionTypes();

        this.cardFilter.setFilter("");
        this.cardFilter.getCollection();


        setInterval(() => {
            if (that.deckValidationDirty) {
                that.deckValidationDirty = false;
                that.updateDeckStats();
            }
        }, this.checkDirtyInterval);
        
        this.updateFormatOptions();
    },
    
    renameCurrentDeck:function() {
        var that = this;
        that.renameDeck(that.deckName, function (newDeckName) {
            
            if (that.deckContentsDirty && confirm("Do you wish to save this deck?"))
            {
                that.saveDeck(false);
            }
            that.deckName = newDeckName;
            that.deckModified(that.deckContentsDirty);
        });
    },
    
    renameDeck:function(oldName, callback) {
        var that = this;
        
        var newDeckName = prompt("Enter new name for the deck", oldName);
        if (newDeckName == null)
            return;
        
        if (newDeckName.length < 3 || newDeckName.length > 100)
            alert("Deck name has to have at least 3 characters and at most 100 characters.");
        else {
            that.comm.renameDeck(oldName, newDeckName, () => callback(newDeckName), 
                {
                    "404":function () {
                        alert("Couldn't find the deck to rename on the server.");
                    }
                });
        }
    },

    getCollectionType:function () {
        return $("#collectionSelect option:selected").prop("value");
    },

    getCollectionTypes:function () {
        var that = this;
        this.comm.getCollectionTypes(
                function (xml) {
                    var root = xml.documentElement;
                    if (root.tagName == "collections") {
                        var collections = root.getElementsByTagName("collection");
                        for (var i = 0; i < collections.length; i++) {
                            var collection = collections[i];
                            $("#collectionSelect").append("<option value='" + collection.getAttribute("type") + "'>" + collection.getAttribute("name") + "</option>");
                        }
                    }
                });
    },
    
    importDecklist:function () {
        var that = this;
        if (that.deckImportDialog == null) {
            that.deckImportDialog = $('<div></div>').dialog({
                closeOnEscape:true,
                resizable:true,
                title:"Import deck"
            });
        }
        that.deckImportDialog.html("");
        var deckImport = $("<textarea rows='5' cols='30' id='deckImport' decklist='decklist'></textarea>");
        var getDecklistTextBut = $("<button title='Import'>Import</button>").button();

        var importDialogDiv = $("<div></div>");
        importDialogDiv.append(deckImport);
        importDialogDiv.append(getDecklistTextBut);
        that.deckImportDialog.append(importDialogDiv);

        getDecklistTextBut.click(
             function () {
                var decklist = $('textarea[decklist="decklist"]').val()
                that.parseDecklist(decklist);
            }
        );
        that.deckImportDialog.dialog("open");
    },
    
    parseDecklist:function(rawText) {
        this.clearDeck();
        var that = this;
        var rawTextList = rawText.split("\n");
        var formattedText = "";
        for (var i = 0; i < rawTextList.length; i++) {
            if (rawTextList[i] != "") {
                var line = that.removeNotes(rawTextList[i]).toLowerCase();
                line = line.replace(/[\*•]/g,"").replace(/’/g,"'")
                        .replace(/starting|start|ring-bearer:|ring:/g,"")
                formattedText = formattedText + line.trim() + "~";
            }
        }
                
        this.importDeckCollection(formattedText, function (xml) {
            var cards = xml.documentElement.getElementsByTagName("card");
            for (var i = 0; i < cards.length; i++) {
                var cardElem = cards[i];
                var blueprintId = cardElem.getAttribute("blueprintId");
                var side = cardElem.getAttribute("side");
                var group = cardElem.getAttribute("group");
                var cardCount = parseInt(cardElem.getAttribute("count"));
                for (var j = 0; j < cardCount; j++) {
                    if (group == "ringBearer") {
                        that.addCardToContainer(blueprintId, "special", that.ringBearerDiv, false).addClass("cardInDeck");
                    } else if (group == "ring") {
                        that.addCardToContainer(blueprintId, "special", that.ringDiv, false).addClass("cardInDeck");
                    } else {
                        that.addCardToDeckDontLayout(blueprintId, side);
                    }
                }
            }
            that.deckModified(true);
            that.layoutDeck();
            $("#editingDeck").text("Imported Deck (unsaved)");
        });
    },

    removeNotes:function(line) {
        var processedLine = line;
        var hasNotes = false;
        var start = line.indexOf("(");
        var end = line.indexOf(")", start);
        if (start < 0 && end < 0) {
            start = line.indexOf("[");
            end = line.indexOf("]", start);
        }
        if (start > 0) {
            processedLine = line.slice(0,start)
            if (end > 0) {
                processedLine = processedLine + line.slice(end+1);
            }
        }
        else if (end > 0) {
            processedLine = line.slice(end+1);
        }
        if (processedLine.indexOf("(") > -1 || processedLine.indexOf(")") > -1 ||
            processedLine.indexOf("[") > -1 || processedLine.indexOf("]") > -1) {
                return this.removeNotes(processedLine);
            }
        return processedLine;
    },

    importDeckCollection:function (decklist, callback) {
        this.comm.importCollection(decklist, function (xml) {
            callback(xml);
        }, {
            "414":function () {
                alert("Deck too large to import.");
            }
        });
    },
    
    editNotes:function() {
        var that = this;
        that.notesDialog = $('<div class="notesDialog"></div>')
            .dialog({
                title:"Edit Deck Notes",
                autoOpen:false,
                closeOnEscape:true,
                resizable:true,
                width:700,
                height:400,
                modal:true
            });
            
        var notesElem = $("<textarea class='notesText'></textarea>");
            
        notesElem.val(that.notes);
        that.notesDialog.append(notesElem);
        
        notesElem.change(function() {
            that.notes = notesElem.val();
            that.deckModified(true);
        });
        
        that.notesDialog.dialog("open");
    },

    loadDeckList:function () {
        var that = this;
        this.comm.getDecks(function (xml) {
            if (that.deckListDialog == null) {
                that.deckListDialog = $("<div></div>")
                        .dialog({
                    title:"Your Saved Decks",
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:700,
                    height:400,
                    modal:true
                });
            }
            that.deckListDialog.html("");
            
            function formatDeckName(formatName, deckName)
            {
                return "<b>[" + formatName + "]</b> - " + deckName;
            }

            var root = xml.documentElement;
            if (root.tagName == "decks") {
                var decks = root.getElementsByTagName("deck");
                var deckNames = [];
                for (var i = 0; i < decks.length; i++) {
                    var deck = decks[i];
                    var deckName = deck.childNodes[0].nodeValue;
                    deckNames[i] = deckName;
                    var formatName = deck.getAttribute("targetFormat");
                    var openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                    var renameDeckBut = $("<button title='Rename deck'><span class='ui-icon ui-icon-pencil'></span></button>").button();
                    var deckListBut = $("<button title='Share deck list'><span class='ui-icon ui-icon-extlink'></span></button>").button();
                    var deleteDeckBut = $("<button title='Delete deck'><span class='ui-icon ui-icon-trash'></span></button>").button();

                    var deckElem = $("<div class='deckItem'></div>");
                    deckElem.append(openDeckBut);
                    deckElem.append(renameDeckBut);
                    deckElem.append(deckListBut);
                    deckElem.append(deleteDeckBut);
                    var deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                    deckElem.append(deckNameDiv);

                    that.deckListDialog.append(deckElem);

                    openDeckBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.getDeck(deckNames[i],
                                            function (xml) {
                                                that.setupDeck(xml, deckNames[i]);
                                            });
                                };
                            })(i));


                    deckListBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.shareDeck(deckNames[i],
                                        function(html) {
                                            window.open('/share/deck?id=' + html, "_blank");
                                        });
                                };
                            })(i));
                    
                    renameDeckBut.click(
                            (function (i, formatName, deckNameDiv) {
                                return function () {
                                    that.renameDeck(deckNames[i], function (newDeckName) {
                                        deckNameDiv.html(formatDeckName(formatName, newDeckName));
                                        
                                        if (that.deckName == deckNames[i]) 
                                        {
                                            that.deckName = newDeckName;
                                            that.deckModified(that.deckContentsDirty);
                                        }
                                        deckNames[i] = newDeckName;
                                    })
                                };
                            })(i, formatName, deckNameDiv));

                    deleteDeckBut.click(
                            (function (i) {
                                return function () {
                                    if (confirm("Are you sure you want to delete this deck?")) {
                                        that.comm.deleteDeck(deckNames[i],
                                                function () {
                                                    if (that.deckName == deckNames[i]) {
                                                        that.deckName = null;
                                                        $("#editingDeck").text("New deck");
                                                        that.clearDeck();
                                                    }

                                                    that.loadDeckList();
                                                });
                                    }
                                };
                            })(i));
                }
            }

            that.deckListDialog.dialog("open");
        });
    },
    
    loadLibraryList:function () {
        var that = this;
        this.comm.getLibraryDecks(function (xml) {
            if (that.deckListDialog == null) {
                that.deckListDialog = $("<div></div>")
                        .dialog({
                    title:"Library Decks",
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:700,
                    height:400,
                    modal:true
                });
            }
            that.deckListDialog.html("");
            
            function formatDeckName(formatName, deckName)
            {
                return "<b>[" + formatName + "]</b> - " + deckName;
            }

            var root = xml.documentElement;
            if (root.tagName == "decks") {
                var decks = root.getElementsByTagName("deck");
                var deckNames = [];
                for (var i = 0; i < decks.length; i++) {
                    var deck = decks[i];
                    var deckName = deck.childNodes[0].nodeValue;
                    deckNames[i] = deckName;
                    var formatName = deck.getAttribute("targetFormat");
                    var openDeckBut = $("<button title='Open deck'><span class='ui-icon ui-icon-folder-open'></span></button>").button();
                    var deckListBut = $("<button title='Deck list'><span class='ui-icon ui-icon-clipboard'></span></button>").button();

                    var deckElem = $("<div class='deckItem'></div>");
                    deckElem.append(openDeckBut);
                    deckElem.append(deckListBut);
                    var deckNameDiv = $("<span/>").html(formatDeckName(formatName, deckName));
                    deckElem.append(deckNameDiv);

                    that.deckListDialog.append(deckElem);

                    openDeckBut.click(
                            (function (i) {
                                return function () {
                                    that.comm.getLibraryDeck(deckNames[i],
                                        function (xml) {
                                            that.setupDeck(xml, deckNames[i]);
                                            that.deckModified(true);
                                        });
                                };
                            })(i));


                    deckListBut.click(
                            (function (i) {
                                return function () {
                                    window.open('/gemp-lotr-server/deck/libraryHtml?deckName=' + encodeURIComponent(deckNames[i]), "_blank");
                                };
                            })(i));
                }
            }

            that.deckListDialog.dialog("open");
        });
    },

    clickCardFunction:function (event) {
        var that = this;

        var tar = $(event.target);
        if (tar.length == 1 && tar[0].tagName == "A")
            return true;

        if (!this.successfulDrag && this.infoDialog.dialog("isOpen")) {
            this.infoDialog.dialog("close");
            event.stopPropagation();
            return false;
        }

        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which >= 1) {
                if (!this.successfulDrag) {
                    if (event.shiftKey || event.which > 1) {
                        this.displayCardInfo(selectedCardElem.data("card"));
                        return false;
                    } else if (selectedCardElem.hasClass("cardInCollection")) {
                        var cardData = selectedCardElem.data("card");
                        this.selectionFunc(cardData.blueprintId, cardData.zone);
                        cardData.tokens = {count:(parseInt(cardData.tokens["count"]) - 1)};
                        layoutTokens(selectedCardElem);
                    } else if (selectedCardElem.hasClass("packInCollection")) {
                        // if (confirm("Would you like to open this pack?")) {
                            this.comm.openPack(this.getCollectionType(), selectedCardElem.data("card").blueprintId, function () {
                                that.cardFilter.getCollection();
                            }, {
                                "404":function () {
                                    alert("You have no pack of this type in your collection.");
                                }
                            });
                        //}
                    } else if (selectedCardElem.hasClass("cardToSelect")) {
                        this.comm.openSelectionPack(this.getCollectionType(), this.packSelectionId, selectedCardElem.data("card").blueprintId, function () {
                            that.cardFilter.getCollection();
                        }, {
                            "404":function () {
                                alert("You have no pack of this type in your collection or that selection is not available for this pack.");
                            }
                        });
                        this.selectionDialog.dialog("close");
                    } else if (selectedCardElem.hasClass("selectionInCollection")) {
                        var selectionDialogResize = function () {
                            var width = that.selectionDialog.width() + 10;
                            var height = that.selectionDialog.height() + 10;
                            that.selectionGroup.setBounds(2, 2, width - 2 * 2, height - 2 * 2);
                        };

                        if (this.selectionDialog == null) {
                            this.selectionDialog = $("<div></div>")
                                    .dialog({
                                title:"Choose one",
                                autoOpen:false,
                                closeOnEscape:true,
                                resizable:true,
                                width:400,
                                height:200,
                                modal:true
                            });

                            this.selectionGroup = new NormalCardGroup(this.selectionDialog, function (card) {
                                return true;
                            }, false);

                            this.selectionDialog.bind("dialogresize", selectionDialogResize);
                        }
                        this.selectionDialog.html("");
                        var cardData = selectedCardElem.data("card");
                        this.packSelectionId = cardData.blueprintId;
                        var selection = selectedCardElem.data("selection");
                        var blueprintIds = selection.split("|");
                        for (var i = 0; i < blueprintIds.length; i++) {
                            var card = new Card(blueprintIds[i], "selection", "selection" + i, "player");
                            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), false, card.isPack(), card.hasErrata());
                            cardDiv.data("card", card);
                            cardDiv.addClass("cardToSelect");
                            this.selectionDialog.append(cardDiv);
                        }
                        openSizeDialog(that.selectionDialog);
                        selectionDialogResize();
                    } else if (selectedCardElem.hasClass("cardInDeck")) {
                        this.removeCardFromDeck(selectedCardElem);
                    }
                    event.stopPropagation();
                }
            }
            return false;
        }
        return true;
    },

    dragCardData:null,
    dragStartX:null,
    dragStartY:null,
    successfulDrag:null,

    dragStartCardFunction:function (event) {
        this.successfulDrag = false;
        var tar = $(event.target);
        if (tar.hasClass("actionArea")) {
            var selectedCardElem = tar.closest(".card");
            if (event.which == 1) {
                this.dragCardData = selectedCardElem.data("card");
                this.dragStartX = event.clientX;
                this.dragStartY = event.clientY;
                return false;
            }
        }
        return true;
    },

    dragStopCardFunction:function (event) {
        if (this.dragCardData != null) {
            if (this.dragStartY - event.clientY >= 20) {
                this.displayCardInfo(this.dragCardData);
                this.successfulDrag = true;
            }
            this.dragCardData = null;
            this.dragStartX = null;
            this.dragStartY = null;
            return false;
        }
        return true;
    },

    displayCardInfo:function (card) {
        this.infoDialog.html("");
        this.infoDialog.html("<div style='scroll: auto'></div>");
        this.infoDialog.append(createFullCardDiv(card.imageUrl, card.foil, card.horizontal, card.isPack()));
        if (card.hasWikiInfo())
            this.infoDialog.append("<div><a href='" + card.getWikiLink() + "' target='_blank'>Wiki</a></div>");
        var windowWidth = $(window).width();
        var windowHeight = $(window).height();

        var horSpace = 30;
        var vertSpace = 45;

        if (card.horizontal) {
            // 500x360
            this.infoDialog.dialog({width:Math.min(500 + horSpace, windowWidth), height:Math.min(380 + vertSpace, windowHeight)});
        } else {
            // 360x500
            this.infoDialog.dialog({width:Math.min(360 + horSpace, windowWidth), height:Math.min(520 + vertSpace, windowHeight)});
        }
        this.infoDialog.dialog("open");
    },

    getDeckContents:function () {
        var ringBearer = $(".card", this.ringBearerDiv);
        var ring = $(".card", this.ringDiv);

        var result = "";
        if (ringBearer.length > 0)
            result += ringBearer.data("card").blueprintId;
        result += "|";
        if (ring.length > 0)
            result += ring.data("card").blueprintId;
        result += "|";

        var sites = new Array();
        $(".card", this.siteDiv).each(
                function () {
                    sites.push($(this).data("card").blueprintId);
                });
        result += sites;
        result += "|";

        var cards = new Array();
        $(".card", this.drawDeckDiv).each(
                function () {
                    cards.push($(this).data("card").blueprintId);
                });
        result += cards;

        return result;
    },

    saveDeck:function (reloadList) {
        var that = this;

        var deckContents = this.getDeckContents();
        if (deckContents == null)
            alert("Deck must contain at least Ring-bearer, The One Ring and 9 sites");
        else
            this.comm.saveDeck(this.deckName, that.formatSelect.val(), this.notes, deckContents, function (xml) {
                that.deckModified(false);
                alert("Deck was saved.  Refresh the Game Hall to see it!");
            }, {
                "400":function () {
                    alert("Invalid deck format.");
                }
            });
    },

    addCardToContainer:function (blueprintId, zone, container, tokens) {
        var card = new Card(blueprintId, zone, "deck", "player");
        var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), tokens, card.isPack(), card.hasErrata());
        cardDiv.data("card", card);
        container.append(cardDiv);
        return cardDiv;
    },

    showPredefinedFilter:function (filter, container) {
        this.cardFilter.enableDetailFilters(false);
        this.specialSelection = true;
        this.cardFilter.setFilter(filter);

        var that = this;
        this.selectionFunc = function (blueprintId) {
            var cardDiv = this.addCardToContainer(blueprintId, "special", container, false);
            cardDiv.addClass("cardInDeck");
            that.showNormalFilter();
            that.layoutSpecialGroups();
            that.deckModified(true);
        };
    },

    showNormalFilter:function () {
        this.cardFilter.enableDetailFilters(true);
        this.specialSelection = false;
        this.cardFilter.setFilter("");

        this.selectionFunc = this.addCardToDeckAndLayout;
    },

    addCardToDeckDontLayout:function (blueprintId, side) {
        var that = this;
        if (side == "FREE_PEOPLE") {
            this.addCardToDeck(blueprintId, side);
        } else if (side == "SHADOW") {
            this.addCardToDeck(blueprintId, side);
        } else if (side == null) {
            var div = this.addCardToContainer(blueprintId, side, this.siteDiv, false)
            div.addClass("cardInDeck");
        }
    },

    addCardToDeckAndLayout:function (blueprintId, side) {
        var that = this;
        if (side == "FREE_PEOPLE") {
            this.addCardToDeck(blueprintId, side);
            that.fpDeckGroup.layoutCards();
        } else if (side == "SHADOW") {
            this.addCardToDeck(blueprintId, side);
            that.shadowDeckGroup.layoutCards();
        } else if (side == null) {
            var div = this.addCardToContainer(blueprintId, side, this.siteDiv, false)
            div.addClass("cardInDeck");
            that.siteGroup.layoutCards();
        }
        that.deckModified(true);
    },

    deckModified:function (value) {
        
        var name = (this.deckName == null) ? "New deck" : this.deckName;
        if (value)
        {
            this.deckValidationDirty = true;
            this.deckContentsDirty = true;
            $("#editingDeck").html("<font color='orange'>*" + name + " - modified</font>");
        }
        else
        {
            this.deckContentsDirty = false;
            $("#editingDeck").text(name);
        }
    },

    addCardToDeck:function (blueprintId, side) {
        var that = this;
        var added = false;
        $(".card.cardInDeck", this.drawDeckDiv).each(
                function () {
                    var cardData = $(this).data("card");
                    if (cardData.blueprintId == blueprintId) {
                        var attDiv = that.addCardToContainer(blueprintId, "attached", that.drawDeckDiv, false);
                        cardData.attachedCards.push(attDiv);
                        added = true;
                    }
                });
        if (!added) {
            var div = this.addCardToContainer(blueprintId, side, this.drawDeckDiv, false)
            div.addClass("cardInDeck");
        }

        this.deckModified(true);
    },

    updateDeckStats:function () {
        var that = this;
        var deckContents = this.getDeckContents();
        if (deckContents != null && deckContents != "") 
        {
            this.comm.getDeckStats(deckContents, 
                   $("#formatSelect").val(),
                   "default", // TODO - collection name for limited formats, now it does not check card availability
                    function (html) 
                    {
                        $("#deckStats").html(html);
                    }, 
                    {
                        "400":function () 
                        {
                            alert("Invalid deck for getting stats.");
                        }
                    });
        } else {
            $("#deckStats").html("Deck has no Ring, Ring-bearer or all 9 sites");
        }
    },
    
    updateFormatOptions:function() {
        var that = this;
        var currentFormat = $("#formatSelect").val();
        
        this.comm.getFormats(false,
            function (json) 
            {
                that.formatSelect.empty();
                //var formats = JSON.parse(json);
                $(json).each(function (index, o) {    
                    var $option = $("<option/>")
                        .attr("value", o.code)
                        .text(o.name);
                    that.formatSelect.append($option);
                });
                
                that.formatSelect.val(currentFormat);

            }, 
            {
                "400":function () 
                {
                    alert("Could not retrieve formats.");
                }
            });
    },

    removeCardFromDeck:function (cardDiv) {
        var cardData = cardDiv.data("card");
        if (cardData.attachedCards.length > 0) {
            cardData.attachedCards[0].remove();
            cardData.attachedCards.splice(0, 1);
        } else {
            cardDiv.remove();
        }
        var cardInCollectionElem = null;
        $(".card", this.normalCollectionDiv).each(
                function () {
                    var tempCardData = $(this).data("card");
                    if (tempCardData.blueprintId == cardData.blueprintId)
                        cardInCollectionElem = $(this);
                });
        if (cardInCollectionElem != null) {
            var cardInCollectionData = cardInCollectionElem.data("card");
            cardInCollectionData.tokens = {count:(parseInt(cardInCollectionData.tokens["count"]) + 1)};
            layoutTokens(cardInCollectionElem);
        }

        this.layoutDeck();
        this.deckModified(true);
    },

    clearDeck:function () {
        $(".cardInDeck").each(
                function () {
                    var cardData = $(this).data("card");
                    for (var i = 0; i < cardData.attachedCards.length; i++)
                        cardData.attachedCards[i].remove();
                });
        $(".cardInDeck").remove();

        this.layoutUI(false);

        this.deckValidationDirty = true;
    },

    setupDeck:function (xml, deckName) {
        var root = xml.documentElement;
        if (root.tagName == "deck") {
            this.clearDeck();
            this.deckName = deckName;
            $("#editingDeck").text(deckName);
            
            var targetFormat = root.getElementsByTagName("targetFormat");
            if (targetFormat.length > 0)
            {
                var formatName = targetFormat[0].getAttribute("formatName");
                var formatCode = targetFormat[0].getAttribute("formatCode");
                //$('#formatSelect option[value="' + formatName + '"]').prop('selected', true);
 
                this.formatSelect.val(formatCode);
            }
            
            var notes = root.getElementsByTagName("notes");
            this.notes = notes[0].innerHTML;

            var ringBearer = root.getElementsByTagName("ringBearer");
            if (ringBearer.length > 0)
                this.addCardToContainer(ringBearer[0].getAttribute("blueprintId"), "deck", this.ringBearerDiv, false).addClass("cardInDeck");

            var ring = root.getElementsByTagName("ring");
            if (ring.length > 0)
                this.addCardToContainer(ring[0].getAttribute("blueprintId"), "deck", this.ringDiv, false).addClass("cardInDeck");

            var sites = root.getElementsByTagName("site");
            for (var i = 0; i < sites.length; i++)
                this.addCardToContainer(sites[i].getAttribute("blueprintId"), "deck", this.siteDiv, false).addClass("cardInDeck");

            var cards = root.getElementsByTagName("card");
            for (var i = 0; i < cards.length; i++)
                this.addCardToDeck(cards[i].getAttribute("blueprintId"), cards[i].getAttribute("side"));

            this.layoutUI(false);

            this.cardFilter.getCollection();
        }
        this.deckModified(false);
    },

    clearCollection:function () {
        $(".card", this.normalCollectionDiv).remove();
    },

    addCardToCollection:function (type, blueprintId, count, side, contents) {
        if (type == "pack") {
            if (blueprintId.substr(0, 3) == "(S)") {
                var card = new Card(blueprintId, "pack", "collection", "player");
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv.data("card", card);
                cardDiv.data("selection", contents);
                cardDiv.addClass("selectionInCollection");
            } else {
                var card = new Card(blueprintId, "pack", "collection", "player");
                card.tokens = {"count":count};
                var cardDiv = createCardDiv(card.imageUrl, null, false, true, true, false);
                cardDiv.data("card", card);
                cardDiv.addClass("packInCollection");
            }
            this.normalCollectionDiv.append(cardDiv);
        } else if (type == "card") {
            var card = new Card(blueprintId, side, "collection", "player");
            var countInDeck = 0;
            $(".card", this.deckDiv).each(
                    function () {
                        var tempCardData = $(this).data("card");
                        if (blueprintId == tempCardData.blueprintId)
                            countInDeck++;
                    });
            card.tokens = {"count":count - countInDeck};
            var cardDiv = createCardDiv(card.imageUrl, null, card.isFoil(), true, false, card.hasErrata());
            cardDiv.data("card", card);
            cardDiv.addClass("cardInCollection");
            this.normalCollectionDiv.append(cardDiv);
        }
    },

    finishCollection:function () {
        this.normalCollectionGroup.layoutCards();
    },

    layoutUI:function (layoutDivs) {
        if (layoutDivs) {
            var manageHeight = 23;

            var padding = 5;
            var collectionWidth = this.collectionDiv.width();
            var collectionHeight = this.collectionDiv.height();

            var deckWidth = this.deckDiv.width();
            var deckHeight = this.deckDiv.height() - (manageHeight + padding);

            var rowHeight = Math.floor((deckHeight - 6 * padding) / 5);
            var sitesWidth = Math.floor(1.5 * deckHeight / 5);
            sitesWidth = Math.min(sitesWidth, 250);

            //this.ringBearerDiv.css({ position:"absolute", left:padding, top:manageHeight + 2 * padding, width:Math.floor((sitesWidth - padding) / 2), height:rowHeight });
            this.ringBearerGroup.setBounds(0, 0, Math.floor((sitesWidth - padding) / 2), rowHeight);
            //this.ringDiv.css({ position:"absolute", left:Math.floor((sitesWidth + 3 * padding) / 2), top:manageHeight + 2 * padding, width:Math.floor((sitesWidth - padding) / 2), height:rowHeight });
            this.ringGroup.setBounds(0, 0, Math.floor((sitesWidth - padding) / 2), rowHeight);

            //this.siteDiv.css({ position:"absolute", left:padding, top:manageHeight + 3 * padding + rowHeight, width:sitesWidth, height:deckHeight - rowHeight - 2 * padding});
            this.siteGroup.setBounds(0, 0, sitesWidth, deckHeight - rowHeight - 2 * padding);

            //this.drawDeckDiv.css({ position:"absolute", left:padding * 2 + sitesWidth, top:manageHeight + 2 * padding, width:deckWidth - (sitesWidth + padding) - padding, height:deckHeight - 2 * padding - 50 });
            this.fpDeckGroup.setBounds(0, 0, deckWidth - (sitesWidth + padding) - padding, (deckHeight - 2 * padding - 50) / 2);
            this.shadowDeckGroup.setBounds(0, (deckHeight - 2 * padding - 50) / 2, deckWidth - (sitesWidth + padding) - padding, (deckHeight - 2 * padding - 50) / 2);

            //this.bottomBarDiv.css({ position:"absolute", left:padding * 2 + sitesWidth, top:manageHeight + padding + deckHeight - 50, width:deckWidth - (sitesWidth + padding) - padding, height:70 });

            //this.cardFilter.layoutUi(padding, 0, collectionWidth - padding, 160);
            //this.normalCollectionDiv.css({ position:"absolute", left:padding, top:160, width:collectionWidth - padding * 2, height:collectionHeight - 160 });

            this.normalCollectionGroup.setBounds(0, 0, collectionWidth - padding * 2, collectionHeight - 160);
        } else {
            this.layoutDeck();
            this.normalCollectionGroup.layoutCards();
        }
    },

    layoutSpecialGroups:function () {
        this.ringBearerGroup.layoutCards();
        this.ringGroup.layoutCards();
        this.siteGroup.layoutCards();
    },

    layoutDeck:function () {
        this.layoutSpecialGroups();
        this.fpDeckGroup.layoutCards();
        this.shadowDeckGroup.layoutCards();
    },

    processError:function (xhr, ajaxOptions, thrownError) {
        if (thrownError != "abort")
        {
            alert("There was a problem during communication with server");
            console.log(xhr)
            console.log(ajaxOptions)
            console.log(thrownError)
        }
    }
});
