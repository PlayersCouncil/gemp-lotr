package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_119");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path of the rig, with Steward's Tomb as its site 5.
	protected VirtualTableScenario GetTombScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					// Gondor Will See It Done: "Response: If a companion is about to take a wound, exert a
					// [gondor] companion to prevent that wound."
					put("seeitdone", "2_34");
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if
					// that minion is an Uruk-hai)."
					put("defendit", "4_115");

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					// Goblin Armory, a [moria] support-area condition: "Response: If a [moria] Orc is about to
					// take a wound, discard this condition to prevent that wound."
					put("armory", "1_173");
					put("marksman", "1_176");     // Goblin Marksman: archer, and nothing else
					// Lost to the Goblins, a Shadow event: "Response: If the Ring-bearer exerts or takes a wound,
					// ..."  Never played: it is here to be offered when he takes a wound.
					put("lost", "1_189");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "10_119");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StewardsTombStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Steward's Tomb
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 8
		 * Type: Site
		 * Subtype: 
		 * Site Number: 5K
		 * Game Text: Wounds cannot be prevented or healed. Burdens cannot be removed.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(5);
		var card = scn.GetFreepsCard("card");

		assertEquals("Steward's Tomb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AWoundCannotBePreventedHereAndWhatIsPaidToPreventItIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTombScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var defendit = scn.GetFreepsCard("defendit");

		var troop = scn.GetShadowCard("troop");
		var armory = scn.GetShadowCard("armory");
		var marksman = scn.GetShadowCard("marksman");
		var lost = scn.GetShadowCard("lost");

		scn.StartGame();
		scn.SkipToSite(4);
		// The turns played on the way drew both decks into the hands: the board is set out now.
		scn.MoveCompanionsToTable(aragorn);
		scn.SetFreepsHand(seeitdone, defendit);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, marksman);
		scn.MoveCardsToSupportArea(armory);
		scn.SetShadowHand(lost);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals("Steward's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());

		// Defend It and Hope wounds the Goblin Patrol Troop, Aragorn exerting for it.  The Goblin Armory's
		// response is offered and taken: the condition is discarded, and the wound lands all the same.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(defendit);
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armory));
		scn.ShadowChooseOptionalTrigger(armory);
		assertInZone(Zone.DISCARD, armory);
		assertEquals(1, scn.GetWoundsOn(troop));

		// The Goblin Marksman's archery wound goes to Frodo.  Gondor Will See It Done is offered and played:
		// Aragorn exerts again, and the wound lands all the same.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));
		scn.FreepsPlayCard(seeitdone);
		assertInDiscard(seeitdone);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));

		// It was a wound taken like any other: the event that answers a wound the Ring-bearer takes is offered.
		assertTrue(scn.ShadowPlayAvailable(lost));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
