var leagueDeliveryDialog = null;

function leagueDeliveryService(notifications) {
    if (!notifications || notifications.length === 0)
        return;

    for (var i = 0; i < notifications.length; i++) {
        var notification = notifications[i];
        showLeagueNotification(notification);
    }
}

function showLeagueNotification(notification) {
    if (leagueDeliveryDialog == null) {
        leagueDeliveryDialog = $("<div></div>").dialog({
            title: "Race to Mount Doom",
            autoOpen: false,
            closeOnEscape: true,
            resizable: false,
            width: "auto",
            height: "auto",
            modal: true,
            closeText: ''
        });
    }

    leagueDeliveryDialog.html("");

    var container = $("<div style='text-align:center; padding:10px;'></div>");

    // Message text
    var message = notification.message || "You have advanced!";
    container.append("<div style='font-size:18px; font-weight:bold; margin-bottom:15px;'>" + message + "</div>");

    // Parse contents: "oldVisual|oldMod|newVisual|newMod"
    var parts = parseLeagueContents(notification.contents);

    var cardHeight = 600;
    var cardWidth = Math.round(cardHeight * (357 / 497)); // maintain card aspect ratio

    if (parts.oldVisual && parts.newVisual) {
        var cardRow = $("<div style='display:flex; align-items:center; justify-content:center; gap:20px;'></div>");

        // Old position card (greyed out)
        var oldDiv = createMetaSiteCardDiv(parts.oldVisual, parts.oldMod, cardWidth, cardHeight);
        oldDiv.css({"opacity": "0.5"});
        cardRow.append(oldDiv);

        // Arrow
        cardRow.append("<div style='font-size:48px; font-weight:bold; color:#c0a000;'>&rarr;</div>");

        // New position card
        var newDiv = createMetaSiteCardDiv(parts.newVisual, parts.newMod, cardWidth, cardHeight);
        cardRow.append(newDiv);

        container.append(cardRow);
    } else if (parts.newVisual) {
        // Only new card (e.g., starting from position 0)
        var newDiv = createMetaSiteCardDiv(parts.newVisual, parts.newMod, cardWidth, cardHeight);
        container.append(newDiv);
    }

    leagueDeliveryDialog.append(container);
    leagueDeliveryDialog.dialog("open");
}

/**
 * Creates a split-rendered meta-site card div with the visual base card and modifier overlay.
 * Matches the in-game and league results rendering pattern.
 */
function createMetaSiteCardDiv(visualBpId, modBpId, width, height) {
    var visualUrl = Card.getImageUrl(visualBpId);
    var wrapper = $("<div style='position:relative;width:" + width + "px;height:" + height + "px;border-radius:8px;overflow:hidden;'></div>");
    wrapper.append("<img src='" + visualUrl + "' style='width:100%;height:100%;object-fit:cover;'>");

    if (modBpId) {
        var modUrl = Card.getImageUrl(modBpId);
        var overlayHeight = Card.MetaSiteOverlayHeight;
        wrapper.append("<div style='position:absolute;bottom:0;width:100%;height:" + overlayHeight
            + "%;overflow:hidden;'><img src='" + modUrl
            + "' style='width:100%;height:100%;object-fit:cover;object-position:bottom;'></div>");
    }

    return wrapper;
}

/**
 * Parses pipe-delimited contents: "oldVisual|oldMod|newVisual|newMod"
 * Returns an object with oldVisual, oldMod, newVisual, newMod (any may be empty string/null).
 */
function parseLeagueContents(contents) {
    var result = { oldVisual: null, oldMod: null, newVisual: null, newMod: null };
    if (!contents)
        return result;

    var parts = contents.split("|");
    if (parts.length >= 4) {
        result.oldVisual = parts[0] || null;
        result.oldMod = parts[1] || null;
        result.newVisual = parts[2] || null;
        result.newMod = parts[3] || null;
    }
    return result;
}

window.leagueDeliveryService = leagueDeliveryService;
