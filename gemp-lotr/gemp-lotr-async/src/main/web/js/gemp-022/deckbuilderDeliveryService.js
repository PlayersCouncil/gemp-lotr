var deckDeliveryDialogs = {};
var deckDeliveryGroups = {};

function deckbuilderDeliveryService(xml) {
    console.log("Delivered a package for deckbuilder:");
    console.log(xml);

    var root = xml.documentElement;
    if (root.tagName == "delivery") {
        var collections = root.getElementsByTagName("collectionType");
        for (var i = 0; i < collections.length; i++) {
            var collection = collections[i];

            var collectionName = collection.getAttribute("name");
            var deliveryDialogResize = (function (name) {
                return function () {
                    var width = deckDeliveryDialogs[name].width() + 10;
                    var height = deckDeliveryDialogs[name].height() + 10;
                    deckDeliveryGroups[name].setBounds(2, 2, width - 2 * 2, height - 2 * 2);
                };
            })(collectionName);

            if (deckDeliveryDialogs[collectionName] == null) {
                deckDeliveryDialogs[collectionName] = $("<div></div>").dialog({
                    title:"New items - " + collectionName,
                    autoOpen:false,
                    closeOnEscape:true,
                    resizable:true,
                    width:400,
                    height:200,
                    closeText: ''
                });

                deckDeliveryGroups[collectionName] = new NormalCardGroup(deckDeliveryDialogs[collectionName], function (card) {
                    return true;
                }, false);

                deckDeliveryDialogs[collectionName].bind("dialogresize", deliveryDialogResize);
                deckDeliveryDialogs[collectionName].bind("dialogclose",
                    function () {
                        deckDeliveryDialogs[collectionName].html("");
                    });
            }

            var packs = collection.getElementsByTagName("pack");
            for (var j = 0; j < packs.length; j++) {
                var packElem = packs[j];
                var blueprintId = packElem.getAttribute("blueprintId");
                var count = packElem.getAttribute("count");
                var card = new Card(blueprintId, "delivery", "deliveryPack" + i, "player");
                card.tokens = {"count":count};
                var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), true, false, false, false);
                cardDiv.data("card", card);
                deckDeliveryDialogs[collectionName].append(cardDiv);
            }

            var cards = collection.getElementsByTagName("card");
            for (var j = 0; j < cards.length; j++) {
                var cardElem = cards[j];
                var blueprintId = cardElem.getAttribute("blueprintId");
                var count = cardElem.getAttribute("count");
                var card = new Card(blueprintId, "delivery", "deliveryCard" + i, "player");
                card.tokens = {"count":count};
                var cardDiv = Card.CreateCardDiv(card.imageUrl, null, null, card.isFoil(), true, false, card.hasErrata(), false);
                cardDiv.data("card", card);
                deckDeliveryDialogs[collectionName].append(cardDiv);
            }

            openSizeDialog(deckDeliveryDialogs[collectionName]);
            deliveryDialogResize();
        }
    }
}