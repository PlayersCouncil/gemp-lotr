package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "9_8");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, no damage bonus; his text is about being played
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, no damage bonus; +2 only against an Orc
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability used only on purpose

					put("axestrike", "1_3");    // Axe Strike: [dwarven] Skirmish event
					put("shaft", "1_22");       // Mithril Shaft: [dwarven] Maneuver event
					put("heart", "1_10");       // Dwarven Heart: a [dwarven] card that is no event (a condition)
					put("defiance", "1_37");    // Defiance: an event of another culture ([elven] Skirmish)
					put("fury", "1_4");         // Battle Fury: [dwarven] Skirmish event; laid in the Shadow player's discard pile only

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; a minion so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RingofGuileStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Ring of Guile
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Strength: 1
		 * Game Text: Bearer must be a Dwarf.<br>Bearer is <b>damage +1</b>.<br><b>Maneuver:</b> Take a [dwarven] event into hand from your discard pile. Discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("Ring of Guile", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarfCompanionOrAllyAndMakesThatBearerAloneDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(ring);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(ring));
		scn.FreepsPlayCard(ring);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertAttachedTo(ring, farin);
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionTakesADwarvenEventFromTheDiscardPileAndDiscardsTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var axestrike = scn.GetFreepsCard("axestrike");
		var shaft = scn.GetFreepsCard("shaft");
		var heart = scn.GetFreepsCard("heart");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);
		scn.MoveCardsToDiscard(axestrike, shaft, heart, defiance);

		var fury = scn.GetShadowCard("fury");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		// Two [dwarven] events of different phases; not a [dwarven] condition, an [elven] event, or a [dwarven]
		// event in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axestrike, shaft));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heart, defiance, fury));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(shaft);

		assertInHand(shaft);
		assertInZone(Zone.DISCARD, axestrike, heart, defiance, fury);
		assertInDiscard(ring);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheActionIsOfferedInTheManeuverPhaseAndNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var shaft = scn.GetFreepsCard("shaft");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);
		scn.MoveCardsToDiscard(shaft);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		assertAttachedTo(ring, guard);
		assertInZone(Zone.DISCARD, shaft);
	}

	@Test
	public void WithNoDwarvenEventInTheDiscardPileTheActionStillDiscardsTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var heart = scn.GetFreepsCard("heart");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ring);
		scn.MoveCardsToDiscard(heart, defiance);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertInDiscard(ring);
		assertInZone(Zone.DISCARD, heart, defiance);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
