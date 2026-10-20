package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_138");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteTakenByPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pain", "17_138");
					// White Hand Intruder: [uruk-hai] Uruk-hai, strength 9, vitality 2, damage +1. "Each time this
					// minion wins a skirmish, if you do not control a site, you may take control of a site."
					put("intruder", "17_127");
					// White Hand Aggressor: [uruk-hai] Uruk-hai, strength 13, vitality 1, damage +1; while its
					// owner controls a site it takes wounds only in the skirmish phase
					put("aggressor1", "17_119");
					put("aggressor2", "17_119");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound, strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void YouDoNotKnowPainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: You Do Not Know Pain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time you take control of a site, you may spot 3 [uruk-hai] minions to exert an unbound companion.<br><b>Skirmish:</b> Liberate a site to make an [uruk-hai] minion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("You Do Not Know Pain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TakingControlOfASiteWithThreeUrukHaiMinionsSpottedMayExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteTakenByPlayScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);

		var pain = scn.GetShadowCard("pain");
		var intruder = scn.GetShadowCard("intruder");
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		scn.MoveCardsToSupportArea(pain);

		// Both markers are at site 2 when the fellowship moves on to site 3: site 1 can be controlled.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(intruder, aggressor1, aggressor2);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// The Intruder, 9, beats Aragorn, 8: a wound and one more for damage +1.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pain));

		// The control is taken by the Intruder's own text, and that is what this condition answers.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pain));

		// The unbound companions may be chosen, the one already wounded among them; the Ring-bearer and Sam,
		// who are Ring-bound, may not.
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered, and
	// a spot pays nothing.  With no unbound companion who can exert, a yes would do nothing.  The game used to
	// offer it all the same (F-0377).
	@Test
	public void WithNoUnboundCompanionAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteTakenByPlayScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var pain = scn.GetShadowCard("pain");
		var intruder = scn.GetShadowCard("intruder");
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		scn.MoveCardsToSupportArea(pain);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(intruder, aggressor1, aggressor2);
		// Aragorn, the only unbound companion, is exhausted.  (The win is answered before his wounds for losing,
		// engine pass 54; until then one wound and those two exhausted him.)
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetVitality(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));

		// A site is taken and three [uruk-hai] minions can be spotted, but the exertion could reach nobody: the
		// one unbound companion cannot exert, and Frodo and Sam are Ring-bound.
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pain));
		// Then his wounds for losing kill him.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
