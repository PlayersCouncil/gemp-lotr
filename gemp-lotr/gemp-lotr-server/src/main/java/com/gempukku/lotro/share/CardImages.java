package com.gempukku.lotro.share;

import com.gempukku.lotro.common.CardInfo;
import com.gempukku.lotro.game.LotroCardBlueprint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The image URL of a card, for a share link's preview.  The browser's rule (js/gemp-022/cards/Card.js getImageUrl)
 * is: a fixed image from the web root's lookup tables (PC_Cards.js for the PC errata and V-sets, set40.js, hobbit.js,
 * cards/CardImages.js), else the Decipher scan.  This reads the same tables (the {@code 'id': 'https://...'} lines,
 * re-read when a file changes) and falls back to the card definition's Decipher image; anything else is null (the
 * caller uses the site's default picture).
 */
public class CardImages {
    private static final Logger _log = LogManager.getLogger(CardImages.class);

    public static final String IMAGE_HOST = "https://i.lotrtcgpc.net/";
    /** the lookup tables, relative to the web root */
    public static final List<String> TABLES = List.of("js/gemp-022/PC_Cards.js", "js/gemp-022/set40.js",
            "js/gemp-022/hobbit.js", "js/gemp-022/cards/CardImages.js");

    // 'id': 'https://...' or "id": "https://..." at the start of a line (commented-out lines start with //)
    private static final Pattern ENTRY = Pattern.compile("^\\s*[\"']([-A-Za-z0-9_*]+)[\"']\\s*:\\s*[\"'](https://[^\"'\\s]+)[\"']",
            Pattern.MULTILINE);
    private static final Pattern DECIPHER = Pattern.compile("^decipher/[A-Za-z0-9_.-]+\\.jpg$");

    private final File _webRoot;
    private Map<String, String> _table = Map.of();
    private String _fingerprint = null;

    /** @param webRoot the web root the tables are read from; null for none (only the Decipher fallback) */
    public CardImages(File webRoot) {
        _webRoot = webRoot;
    }

    /** @return an absolute https URL of the card's picture, or null when there is none to point at */
    public synchronized String imageUrl(String blueprintId, LotroCardBlueprint card) {
        String fixed = table().get(blueprintId);
        if (fixed != null)
            return fixed;
        if (card != null) {
            CardInfo info = card.getCardInfo();
            if (info != null && info.image != null && DECIPHER.matcher(info.image.trim()).matches())
                return IMAGE_HOST + info.image.trim();
        }
        return null;
    }

    private Map<String, String> table() {
        if (_webRoot == null)
            return _table;
        StringBuilder fingerprint = new StringBuilder();
        for (String name : TABLES) {
            File file = new File(_webRoot, name);
            fingerprint.append(name).append(file.lastModified()).append('/').append(file.length()).append(';');
        }
        if (fingerprint.toString().equals(_fingerprint))
            return _table;
        Map<String, String> table = new HashMap<>();
        for (String name : TABLES) {
            File file = new File(_webRoot, name);
            if (!file.isFile())
                continue;
            try {
                Matcher matcher = ENTRY.matcher(Files.readString(file.toPath(), StandardCharsets.UTF_8));
                while (matcher.find())
                    table.putIfAbsent(matcher.group(1), matcher.group(2));
            } catch (IOException exp) {
                _log.warn("Could not read card image table " + file.getAbsolutePath() + ": " + exp.getMessage());
            }
        }
        _table = table;
        _fingerprint = fingerprint.toString();
        return _table;
    }
}
