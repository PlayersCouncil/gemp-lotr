package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beetles", "10_65");

					put("hound", "7_192");          // Morgul Hound: [wraith] Orc; its own text needs 2 wounds on the Ring-bearer
					put("mongrel", "7_194");        // Morgul Mongrel: [wraith] Orc of another title; its own text needs 3 wounds on the Ring-bearer
					put("lemenya", "1_232");        // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] minion who is no Orc; his text needs a [wraith] condition on a companion
					put("chieftain", "1_266");      // Orc Chieftain: an Orc who is not [wraith] ([sauron]); a trigger on a weapon played on him only

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("guard", "1_7");            // Dwarf Guard: [dwarven] Dwarf companion, vitality 2; a requirement to play only
					put("cleaved", "18_114");       // Cleaved: [uruk-hai] condition, vitality -1, which its own ability transfers to an unbound companion
					put("maggot", "1_288");         // Farmer Maggot, Chaser of Rascals: a Hobbit ally; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwarmingLikeBeetlesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Swarming Like Beetles
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Maneuver:</b> Spot a [wraith] Orc and remove a threat to wound an unwounded companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beetles");

		assertEquals("Swarming Like Beetles", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverActionRemovesAThreatToWoundAnUnwoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(maggot);

		var beetles = scn.GetShadowCard("beetles");
		var hound = scn.GetShadowCard("hound");
		var cleaved = scn.GetShadowCard("cleaved");

		scn.StartGame();
		scn.MoveCardsToSupportArea(beetles);
		scn.MoveMinionsToTable(hound);
		// Vitality 2 - 1: the Dwarf Guard is unwounded and exhausted, so a wound kills him where an exertion
		// could not be put on him.
		scn.AttachCardsTo(guard, cleaved);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddThreats(2);
		assertAttachedTo(cleaved, guard);
		assertEquals(1, scn.GetVitality(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetThreats());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertTrue(scn.ShadowActionAvailable(beetles));
		scn.ShadowUseCardAction(beetles);

		// One threat of two removed.
		assertEquals(1, scn.GetThreats());
		// Unwounded companions of two cultures and races; not the wounded companion, nor the unwounded ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, maggot));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(maggot));
		// The threat left is removed for the Guard's death and asked for as a wound.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsDecisionAvailable("Choose characters to wound"));
	}

	@Test
	public void WithEveryCompanionWoundedTheThreatIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beetles = scn.GetShadowCard("beetles");
		var mongrel = scn.GetShadowCard("mongrel");

		scn.StartGame();
		scn.MoveCardsToSupportArea(beetles);
		// The Morgul Mongrel is the only [wraith] Orc to spot.
		scn.MoveMinionsToTable(mongrel);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddThreats(1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(beetles));
		scn.ShadowUseCardAction(beetles);

		assertEquals(0, scn.GetThreats());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAWraithOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beetles = scn.GetShadowCard("beetles");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveCardsToSupportArea(beetles);
		// A [wraith] minion who is no Orc and an Orc who is not [wraith].
		scn.MoveMinionsToTable(lemenya, chieftain);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(beetles));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void NotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beetles = scn.GetShadowCard("beetles");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(beetles);
		scn.MoveMinionsToTable(hound);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(beetles));
	}
}
