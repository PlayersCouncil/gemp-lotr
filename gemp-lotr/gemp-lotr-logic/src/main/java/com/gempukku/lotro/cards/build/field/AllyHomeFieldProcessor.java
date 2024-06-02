package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.AllyHome;
import com.gempukku.lotro.common.Names;
import com.gempukku.lotro.common.SitesBlock;
import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.regex.Pattern;

public class AllyHomeFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {

        if(value instanceof String) {
            processShortenedAllyHomeString((String)value, blueprint, environment);
        }
        else if(value instanceof JSONObject) {
            processFullAllyHome((JSONObject)value, blueprint, environment);
        }
        else if(value instanceof final JSONArray array) {
            for(var entry : array) {
                if(entry instanceof String) {
                    processShortenedAllyHomeString((String)entry, blueprint, environment);
                }
                else if(entry instanceof JSONObject) {
                    processFullAllyHome((JSONObject)entry, blueprint, environment);
                }
            }
        }
    }

    // Matching shortened form that looks like 1F / 2T / 3K / 4S / 5H for Fellowship/Towers/King/Shadows/Hobbit
    private Pattern shortAllyHome = Pattern.compile("^\\s*(\\d)([\\w+])?\\s*$");
    private void processShortenedAllyHomeString(String value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        var matches = shortAllyHome.matcher(value);
        if(matches.matches()) {
            SitesBlock block;
            int siteNum;

            if(!Strings.isNullOrEmpty(matches.group(1))) {
                siteNum = Integer.parseInt(matches.group(1));
            }
            else
                throw new InvalidCardDefinitionException("Ally home '" + value + "' is missing site number.");

            if(!Strings.isNullOrEmpty(matches.group(2))) {
                block = SitesBlock.findBlock(matches.group(2));
            }
            else
                throw new InvalidCardDefinitionException("Ally home '" + value + "' is missing block.");

            blueprint.setAllyHomeSites(new AllyHome(block, siteNum));
        }
        else {
            throw new InvalidCardDefinitionException("Ally home '" + value + "' does not match supported shortened ally home pattern.");
        }

    }

    private void processFullAllyHome(JSONObject value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        SitesBlock block = FieldUtils.getBlock(value.get("block"), "block");
        int siteNum = FieldUtils.getInteger(value.get("site"), "site");

        blueprint.setAllyHomeSites(new AllyHome(block, siteNum));
    }
}
