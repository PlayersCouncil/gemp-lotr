package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kings", "1_115");
					put("kings2", "1_115");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, a maneuver ability used only on purpose
					put("dead", "8_45");        // Sleepless Dead: [gondor] Wraith companion (cheated in, so no threat); his trigger needs a won skirmish
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man. Skirmish: exert Boromir to make a Hobbit strength +3
					put("sentinels", "2_37");   // Sentinels of Numenor: [gondor] Skirmish event, (0). Make a [gondor] companion strength +2 (or +4 in a fierce skirmish)

					put("savage1", "1_151");    // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
					put("cry1", "1_128");       // Lurtz's Battle Cry: [isengard] Skirmish event, (1). Make an Uruk-hai strength -1 and damage +1
					put("cry2", "1_128");
					put("army", "5_70");        // Army of Haradrim: [raider] Southron Man, strength 14. Skirmish: Remove (3) to play a [raider] mount from your discard pile
					put("mumak", "5_78");       // War Mumak: [raider] mount on a Southron, (3); bearer is fierce; an optional trigger only when bearer overwhelms
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
					put("kings", "1_115");

					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home site 3 of the Towers block; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("cry", "1_128");        // Lurtz's Battle Cry: [isengard] Skirmish event, (1). Make an Uruk-hai strength -1 and damage +1
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCancelScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("kings", "1_115");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: the [gondor] Man, strength 8, no archer

					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5
					put("territory1", "1_201");     // Unfamiliar Territory: [moria] Skirmish event, cost 0, "Make a [moria] Orc strength +2 (or +4 if skirmishing an archer)"
					put("territory2", "1_201");
				}},
				VirtualTableScenario.FellowshipSites,
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
		 * Game Text: <b>Response:</b> If a skirmish event is played during a skirmish involving a [gondor] Man, cancel that event.
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
	public void CancelsASkirmishEventPlayedInAGondorMansSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCancelScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var territory1 = scn.GetShadowCard("territory1");
		var territory2 = scn.GetShadowCard("territory2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(kings);
		assertInHand(kings);
		scn.MoveCardsToHand(territory1, territory2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPlayCard(territory1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));

		scn.FreepsChooseOptionalTrigger(kings);
		// Cancelled: the Runner is still 5.  Aragorn is no archer, so the event would have made it 7.
		assertEquals(5, scn.GetStrength(runner));
		// Both events are spent (CR 4.2 "cancel": a cancelled event is discarded), and the skirmish goes on.
		assertInZone(Zone.DISCARD, territory1, kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The same event again, with no Strength of Kings left to answer it, does what it says.
		scn.FreepsPass();
		scn.ShadowPlayCard(territory2);
		assertEquals(7, scn.GetStrength(runner));
		assertInZone(Zone.DISCARD, territory2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CancelsAShadowSkirmishEventInAGondorMansSkirmishWithItsCostStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var kings2 = scn.GetFreepsCard("kings2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage1");
		var cry = scn.GetShadowCard("cry1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(kings, kings2);
		assertInHand(kings, kings2);
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
	public void IsNotOfferedInTheSkirmishOfAManOfAnotherCultureWhileAGondorManWaits() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var cry = scn.GetShadowCard("cry1");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(kings);
		assertInHand(kings);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		// The [rohan] Man's skirmish first, with Aragorn, the [gondor] Man, assigned and waiting.
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
	public void IsNotOfferedInTheSkirmishOfAGondorCompanionWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dead = scn.GetFreepsCard("dead");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var cry = scn.GetShadowCard("cry1");
		scn.MoveCompanionsToTable(aragorn, dead);
		scn.MoveCardsToHand(kings);
		assertInHand(kings);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{dead, savage1}, new PhysicalCardImpl[]{aragorn, savage2});
		// The skirmish of the [gondor] Wraith first, with Aragorn assigned and waiting.
		scn.FreepsResolveSkirmish(dead);

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
		var boromir = scn.GetFreepsCard("boromir");
		var sentinels = scn.GetFreepsCard("sentinels");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(kings, sentinels);
		assertInHand(kings, sentinels);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		// A skirmish special ability is not a skirmish event.
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsUseCardAction(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(frodoStrength + 3, scn.GetStrength(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings));

		scn.ShadowPass();
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPlayCard(sentinels);

		// The Free Peoples player's own skirmish event is answered as well, before its target is chosen.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));
		scn.FreepsChooseOptionalTrigger(kings);

		assertEquals(7, scn.GetStrength(boromir));
		assertInDiscard(sentinels, kings);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAShadowCardThatIsNoEventIsPlayedInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kings = scn.GetFreepsCard("kings");
		var aragorn = scn.GetFreepsCard("aragorn");
		var army = scn.GetShadowCard("army");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(kings);
		assertInHand(kings);
		scn.MoveCardsToDiscard(mumak);
		assertInDiscard(mumak);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.MoveMinionsToTable(army);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertInDiscard(mumak);
		assertFalse(scn.HasKeyword(army, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(army));
		scn.ShadowUseCardAction(army);

		// A possession played in Aragorn's skirmish is no skirmish event.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(kings));
		assertAttachedTo(mumak, army);
		assertTrue(scn.HasKeyword(army, Keyword.FIERCE));
		assertInHand(kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CancelsASkirmishEventInTheSkirmishOfAGondorManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var kings = scn.GetFreepsCard("kings");
		var merchant = scn.GetFreepsCard("merchant");
		var savage = scn.GetShadowCard("savage");
		var cry = scn.GetShadowCard("cry");
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(kings);
		assertInHand(kings);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// The fellowship has moved on to site 3, the Merchant's home.  The [gondor] Man ally fights there; the
		// Ring-bearer, the only companion, is no [gondor] Man.
		scn.FreepsAssignToMinions(merchant, savage);
		scn.FreepsResolveSkirmish(merchant);

		scn.FreepsPass();
		var pool = scn.GetTwilight();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		scn.ShadowPlayCard(cry);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(kings));
		scn.FreepsChooseOptionalTrigger(kings);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(pool - 1, scn.GetTwilight());
		assertInDiscard(cry, kings);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
