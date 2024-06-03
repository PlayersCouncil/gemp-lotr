package com.gempukku.lotro.cards.build.field;

import com.gempukku.lotro.cards.build.BuiltLotroCardBlueprint;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.FieldProcessor;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.effect.*;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectFieldProcessor implements FieldProcessor {
    private final Map<String, EffectProcessor> effectProcessors = new HashMap<>();

    public EffectFieldProcessor() {
        effectProcessors.put("activated", new ActivatedEffectProcessor());
        effectProcessors.put("activatedfromstacked", new ActivatedFromStackedEffectProcessor());
        effectProcessors.put("activatedindiscard", new ActivatedInDiscardEffectProcessor());
        effectProcessors.put("activatedindrawdeck", new ActivatedInDrawDeckEffectProcessor());
        effectProcessors.put("aidcost", new AidCost());
        effectProcessors.put("assignmentcost", new AssignmentCost());
        effectProcessors.put("controlledsitemodifier", new ControlledSiteModifier());
        effectProcessors.put("copycard", new CopyCard());
        effectProcessors.put("deckbuildingrestriction", new DeckBuildingRestrictionGameTextProcessor());
        effectProcessors.put("discardedfromplaytrigger", new DiscardedFromPlayTriggerEffectProcessor());
        effectProcessors.put("displayableinformation", new DisplayableInformationEffectProcessor());
        effectProcessors.put("discount", new PotentialDiscount());
        effectProcessors.put("event", new EventEffectProcessor());
        effectProcessors.put("exerttargetextracost", new ExertTargetExtraCost());
        effectProcessors.put("extracost", new ExtraCost());
        effectProcessors.put("extrapossessionclass", new ExtraPossessionClassEffectProcessor());
        effectProcessors.put("inhandtrigger", new InHandTriggerEffectProcessor());
        effectProcessors.put("killedtrigger", new KilledTriggerEffectProcessor());
        effectProcessors.put("modifier", new Modifier());
        effectProcessors.put("modifierindiscard", new ModifierInDiscard());
        effectProcessors.put("modifyowncost", new ModifyOwnCost());
        effectProcessors.put("permanentsitemodifier", new PermanentSiteModifier());
        effectProcessors.put("playedinotherphase", new PlayedInOtherPhase());
        effectProcessors.put("response", new ResponseEffectProcessor());
        effectProcessors.put("responseevent", new ResponseEventEffectProcessor());
        effectProcessors.put("stackedonmodifier", new StackedOnModifier());
        effectProcessors.put("trigger", new TriggerEffectProcessor());
        effectProcessors.put("toplay", new ToPlay());
    }

    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final JSONObject[] effectsArray = FieldUtils.getObjectArray(value, key);
        for (JSONObject effect : effectsArray) {
            final String effectType = FieldUtils.getString(effect.get("type"), "type");
            final EffectProcessor effectProcessor = effectProcessors.get(effectType.toLowerCase());
            if (effectProcessor == null)
                throw new InvalidCardDefinitionException("Unable to find top-level game text of type: " + effectType);
            effectProcessor.processEffect(effect, blueprint, environment);
        }
    }
}
