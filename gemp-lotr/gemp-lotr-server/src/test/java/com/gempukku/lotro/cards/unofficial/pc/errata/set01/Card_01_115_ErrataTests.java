package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_115_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kings", "51_115");
					put("kings2", "51_115");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, a maneuver ability used only on purpose
					put("dead", "8_45");        // Sleepless Dead: [gondor] Wraith companion (cheated in, so no threat); his trigger needs a won skirmish
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man. Skirmish: exert Boromir to make a Hobbit strength +3
					put("sentinels", "2_37");   // Sentinels of Numenor: [gondor] Skirmish event, (0). Make a [gondor] companion strength +2

					put("savage1", "1_151");    // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
					put("cry1", "1_128");       // Lurtz's Battle Cry: [isengard] Skirmish event, (1). Make an Uruk-hai strength -1 and damage +1
					put("cry2", "1_128");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kings", "51_115");

					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home site 3 of the Towers block; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("cry", "1_128");        // Lurtz's Battle Cry: [isengard] Skirmish event, (1). Make an Uruk-hai strength -1 and damage +1
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofKingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Strength of Kings
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a skirmish event is played during a skirmish involving a [gondor] companion, cancel that event.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("kings");

		assertEquals("Strength of Kings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CancelsAShadowSkirmishEventInAGondorCompanionsSkirmishWithItsCostStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var kings2 = scn.GetFreepsCard("kings2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage1");
		var cry = scn.GetShadowCard("cry1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(kings, kings2);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		var pool = scn.GetTwilight();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.ShadowPlayCard(cry);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));
		scn.FreepsChooseOptionalTrigger(kings);
		// Strength of Kings is itself an event played in Aragorn's skirmish, but a response event, not a
		// skirmish event: the second copy is not offered against it.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings2));

		// Cancelled: neither strength -1 nor damage +1.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		// The Battle Cry's (1) stays paid and the event is discarded (CR 4.2 "cancel").
		assertEquals(pool - 1, scn.GetTwilight());
		assertInDiscard(cry, kings);
		assertInHand(kings2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedInTheSkirmishOfACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var cry = scn.GetShadowCard("cry1");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(kings);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		// The Rohan Man's skirmish first, with Aragorn assigned and waiting.
		scn.FreepsResolveSkirmish(guard);

		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(savage1));
		scn.ShadowPlayCard(cry);
		scn.ShadowChooseCard(savage1);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings));
		assertEquals(4, scn.GetStrength(savage1));
		assertEquals(2, scn.GetKeywordCount(savage1, Keyword.DAMAGE));
		assertInHand(kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CancelsAFreePeoplesSkirmishEventTooButNotASkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var dead = scn.GetFreepsCard("dead");
		var boromir = scn.GetFreepsCard("boromir");
		var sentinels = scn.GetFreepsCard("sentinels");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(dead, boromir);
		scn.MoveCardsToHand(kings, sentinels);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(dead, savage);
		scn.FreepsResolveSkirmish(dead);

		// A skirmish special ability is not a skirmish event.
		var frodoStrength = scn.GetStrength(frodo);
		scn.FreepsUseCardAction(boromir);
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings));

		scn.ShadowPass();
		assertEquals(7, scn.GetStrength(dead));
		scn.FreepsPlayCard(sentinels);

		// The Free Peoples player's own skirmish event is answered as well, before its target is chosen.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));
		scn.FreepsChooseOptionalTrigger(kings);

		assertEquals(7, scn.GetStrength(dead));
		assertInDiscard(sentinels, kings);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedInTheSkirmishOfAGondorAllyWithNoGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var kings = scn.GetFreepsCard("kings");
		var merchant = scn.GetFreepsCard("merchant");
		var savage = scn.GetShadowCard("savage");
		var cry = scn.GetShadowCard("cry");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(kings);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// The fellowship has moved on to site 3, the Merchant's home. The [gondor] ally fights at its home site; the Ring-bearer is the only companion, and not [gondor].
		scn.FreepsAssignToMinions(merchant, savage);
		scn.FreepsResolveSkirmish(merchant);

		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(cry);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings));
		assertEquals(4, scn.GetStrength(savage));
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertInHand(kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
