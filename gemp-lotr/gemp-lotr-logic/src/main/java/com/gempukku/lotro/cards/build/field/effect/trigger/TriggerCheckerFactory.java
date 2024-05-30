package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TriggerCheckerFactory {
    private final Map<String, TriggerCheckerProducer> triggerCheckers = new HashMap<>();

    public TriggerCheckerFactory() {

        //Movement triggers
        triggerCheckers.put("movesfrom", new MovesFrom());
        triggerCheckers.put("moves", new Moves());
        triggerCheckers.put("abouttomoveto", new AboutToMoveTo());
        triggerCheckers.put("movesto", new MovesTo());

        //Timing Triggers
        triggerCheckers.put("startofturn", new StartOfTurn());
        triggerCheckers.put("startofphase", new StartOfPhase());
        triggerCheckers.put("endofphase", new EndOfPhase());
        triggerCheckers.put("endofturn", new EndOfTurn());

        triggerCheckers.put("abouttoaddburden", new AboutToAddBurden());
        triggerCheckers.put("abouttoaddtwilight", new AboutToAddTwilight());
        triggerCheckers.put("abouttobekilled", new AboutToBeKilled());
        triggerCheckers.put("abouttobeoverwhelmed", new AboutToBeOverwhelmed());
        triggerCheckers.put("abouttodiscard", new AboutToDiscard());
        triggerCheckers.put("abouttodrawcard", new AboutToDrawCard());
        triggerCheckers.put("abouttoexert", new AboutToExert());
        triggerCheckers.put("abouttoheal", new AboutToHeal());
        triggerCheckers.put("abouttotakecontrolofsite", new AboutToTakeControlOfSite());
        triggerCheckers.put("abouttotakewound", new AboutToTakeWound());
        triggerCheckers.put("addsburden", new AddsBurden());
        triggerCheckers.put("addsthreat", new AddsThreat());
        triggerCheckers.put("afterallskirmishes", new AfterAllSkirmishes());
        triggerCheckers.put("assignedagainst", new AssignedAgainst());
        triggerCheckers.put("assignedtoskirmish", new AssignedToSkirmish());
        triggerCheckers.put("beforethreatwounds", new BeforeThreatWounds());
        triggerCheckers.put("beforetoil", new BeforeToil());
        triggerCheckers.put("cancelledskirmish", new CancelledSkirmish());
        triggerCheckers.put("constantlycheck", new ConstantlyCheckTrigger());
        triggerCheckers.put("discarded", new Discarded());
        triggerCheckers.put("exerted", new Exerted());
        triggerCheckers.put("exertsforspecialability", new ExertsForSpecialAbility());
        triggerCheckers.put("exertstoplay", new ExertsToPlay());
        triggerCheckers.put("fpdecidedifmoving", new FPDecidedIfMoving());
        triggerCheckers.put("fpdecidedtomove", new FPDecidedToMove());
        triggerCheckers.put("fpdecidedtostay", new FPDecidedToStay());
        triggerCheckers.put("fpstartedassigning", new FPStartedAssigning());
        triggerCheckers.put("heals", new Heals());
        triggerCheckers.put("killed", new Killed());
        triggerCheckers.put("killedinskirmish", new KilledInSkirmish());
        triggerCheckers.put("losesinitiative", new LosesInitiative());
        triggerCheckers.put("losesskirmish", new LosesSkirmish());
        triggerCheckers.put("played", new PlayedTriggerCheckerProducer());
        triggerCheckers.put("playedfromstacked", new PlayedFromStacked());
        triggerCheckers.put("playerdrawscard", new PlayerDrawsCard());
        triggerCheckers.put("putsonring", new PutsOnRing());
        triggerCheckers.put("reconciles", new Reconciles());
        triggerCheckers.put("removedfromplay", new RemovedFromPlay());
        triggerCheckers.put("removesburden", new RemovesBurden());
        triggerCheckers.put("replacessite", new ReplacesSite());
        triggerCheckers.put("revealscardfromtopofdrawdeck", new RevealsCardFromTopOfDrawDeck());
        triggerCheckers.put("revealedcardfromhand", new RevealedCardFromHand());
        triggerCheckers.put("skirmishabouttoend", new SkirmishAboutToEnd());
        triggerCheckers.put("startofskirmishinvolving", new StartOfSkirmishInvolving());
        triggerCheckers.put("takencontrolofsite", new TakenControlOfSite());
        triggerCheckers.put("takesoffring", new TakesOffRing());
        triggerCheckers.put("takeswound", new TakesWound());
        triggerCheckers.put("transferred", new Transferred());
        triggerCheckers.put("usesspecialability", new UsesSpecialAbility());
        triggerCheckers.put("winsskirmish", new WinsSkirmish());
    }

    public TriggerChecker getTriggerChecker(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String triggerType = FieldUtils.getString(object.get("type"), "type");
        if (triggerType == null)
            throw new InvalidCardDefinitionException("Trigger type not defined");
        final TriggerCheckerProducer triggerCheckerProducer = triggerCheckers.get(triggerType.toLowerCase());
        if (triggerCheckerProducer == null)
            throw new InvalidCardDefinitionException("Unable to find trigger of type: " + triggerType);
        return triggerCheckerProducer.getTriggerChecker(object, environment);
    }
}
