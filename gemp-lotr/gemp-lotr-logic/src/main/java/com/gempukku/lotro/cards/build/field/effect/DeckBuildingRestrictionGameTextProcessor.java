package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.*;
import com.gempukku.lotro.cards.build.field.EffectProcessor;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.common.PileType;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Result;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.GameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeckBuildingRestrictionGameTextProcessor implements EffectProcessor {
    @Override
    public void processEffect(JSONObject effectObj, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObj, "filter", "pile", "min", "max", "text");

        final int max = FieldUtils.getInteger(effectObj.get("max"), "max", 1000);
        final int min = FieldUtils.getInteger(effectObj.get("max"), "min", 0);
        final String pileStr = FieldUtils.getString(effectObj.get("pile"), "pile");
        final PileType pileType = PileType.Parse(pileStr);
        final String failureMsg = FieldUtils.getString(effectObj.get("text"), "text");

        if(StringUtils.isBlank(failureMsg))
            throw new InvalidCardDefinitionException("'text' field is required for deck validation operations to communicate to the player what validation they are faling in the deckbuilder.  Ensure it is user friendly.");
        if(max < min)
            throw new InvalidCardDefinitionException("'max' field may not be less than 'min' field for deck validations.");
        if(min < 0)
            throw new InvalidCardDefinitionException("'min' field may not be less than zero for deck validations.");

        final String filterStr = FieldUtils.getString(effectObj.get("filter"), "attachedTo", "any");
        final var filterSource = environment.getFilterFactory().generateFilter(filterStr, environment);



        blueprint.setDeckValidation(
                (freeps, shadow, sites, rb, ring, map) -> {

                    var filter = Filters.and(filterSource.getFilterable(null));
                    var found = new ArrayList<LotroCardBlueprint>();

                    if(pileType == null || pileType == PileType.FREE_PEOPLES) {
                        found.addAll(validate(freeps, filter));
                    }
                    if(pileType == null || pileType == PileType.SHADOW) {
                        found.addAll(validate(shadow, filter));
                    }
                    if(pileType == null || pileType == PileType.ADVENTURE) {
                        found.addAll(validate(sites, filter));
                    }
                    if(pileType == null || pileType == PileType.RING_BEARER) {
                        found.addAll(validate(Collections.singletonList(rb), filter));
                    }
                    if(pileType == null || pileType == PileType.RING) {
                        found.addAll(validate(Collections.singletonList(ring), filter));
                    }
                    if(pileType == null || pileType == PileType.MAP) {
                        found.addAll(validate(Collections.singletonList(map), filter));
                    }

                    final String successMsg = "Deck meets restrictions placed by " + GameUtils.getFullName(blueprint) + ".";
                    final String fullFailureMsg = "Deck does not meet restrictions placed by " + GameUtils.getFullName(blueprint) + ": "
                            + failureMsg + ".  ";

                    if(found.size() > max || found.size() < min)
                        return new Result(false, fullFailureMsg + "Found " + found.size() + " issues: "
                                + found.stream().map(GameUtils::getFullName).collect(Collectors.joining(", ")));

                    return new Result(true, successMsg);
                });
    }

    protected List<LotroCardBlueprint> validate(List<PhysicalCardImpl> cards, Filter filter) {
        var found = new ArrayList<LotroCardBlueprint>();
        for(var card : cards) {
            if(filter.accepts(null, card)) {
                found.add(card.getBlueprint());
            }
        }

        return found;
    }
}
