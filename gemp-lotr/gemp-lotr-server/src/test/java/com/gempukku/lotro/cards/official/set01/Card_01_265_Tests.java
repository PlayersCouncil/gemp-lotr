package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_265_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("butchery", "1_265");

					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, strength 11, vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					put("scout", "15_116");     // Scouting Orc: [orc] Orc, strength 8, vitality 2; "Skirmish: Exert this minion to wound a companion it is skirmishing."
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; "Regroup: Exert this minion to make the Free Peoples player wound a companion."
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai (not an Orc), strength 9, vitality 2, damage +1

					put("sam", "2_114");        // Sam, Proper Poet: companion, strength 3, vitality 4; his texts are used only on purpose or answer Frodo's death
					put("sword", "1_299");      // Hobbit Sword: strength +2, no other text
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: ally, home site 3, strength 5, vitality 2; at site 3 she has its "play an Elf to draw a card"
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, home site 3, vitality 4; a start-of-turn heal and a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcButcheryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Butchery
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a companion is killed by a [sauron] Orc, the Free Peoples player must discard 3 cards
		 * from the top of his draw deck for each card in the dead pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("butchery");

		assertEquals("Orc Butchery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionOverwhelmedByASauronOrcLetsItBePlayedAndThreeCardsAreDiscardedForTheOneInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var silinde = scn.GetFreepsCard("silinde");
		var shadowGuard = scn.GetShadowCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		// Neither of these is in the Free Peoples player's dead pile.
		scn.MoveCardsToDeadPile(shadowGuard);
		scn.MoveCardsToFreepsDiscard("silinde");

		scn.StartGame();
		assertInHand(butchery);
		scn.MoveMinionsToTable(slayer);
		assertInZone(Zone.DEAD, shadowGuard);
		assertInZone(Zone.DISCARD, silinde);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		var top1 = scn.GetFromTopOfFreepsDeck(1);
		var top2 = scn.GetFromTopOfFreepsDeck(2);
		var top3 = scn.GetFromTopOfFreepsDeck(3);
		var top4 = scn.GetFromTopOfFreepsDeck(4);
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.PassCurrentPhaseActions();
		// Strength 7 against Sam's 3: overwhelmed.
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInZone(Zone.DEAD, shadowGuard);
		assertInZone(Zone.DISCARD, silinde);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		// One card in the Free Peoples dead pile: 3 cards, and the top 3, with nothing asked.
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, top1, top2, top3);
		assertInZone(Zone.DECK, top4);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, butchery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledByTheWoundForLosingWithTwoCardsInTheDeadPileDiscardsSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToDeadPile(guard1);

		scn.StartGame();
		assertInHand(butchery);
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertAttachedTo(sword, sam);
		assertInZone(Zone.DEAD, guard1);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		var top6 = scn.GetFromTopOfFreepsDeck(6);
		var top7 = scn.GetFromTopOfFreepsDeck(7);
		int deck = scn.GetFreepsDeckCount();

		scn.PassCurrentPhaseActions();
		// Strength 5 against 7 is no overwhelm: the wound for losing kills Sam, who had 3 of his 4.
		assertInZone(Zone.DEAD, sam);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		// Two cards in the dead pile: 3 for each.
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, top6);
		assertInZone(Zone.DECK, top7);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var scout = scn.GetShadowCard("scout");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		assertInHand(butchery);
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(slayer, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(sam, scout);
		scn.FreepsResolveSkirmish(sam);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The [orc] Orc's own wound kills Sam in the [sauron] Orc's skirmish (Q-0123: A, a kill in its skirmish counts).
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, butchery);
		// With Sam gone the skirmish is over, and the only one: the regroup phase.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	@Ignore("Q-0328: CR 4.2 Section Three (filed under The Ring's Oppression 1_273) says a [Sauron] Orc kills a companion by the game text of its own card or by the companion losing a skirmish involving it, so another minion's wound in its skirmish would not count; Q-0123 A, which this definition keeps, was answered on the footing that no such definition exists")
	public void ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var scout = scn.GetShadowCard("scout");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		assertInHand(butchery);
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(slayer, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		// The Shadow player puts the [orc] Orc in the same skirmish.
		scn.ShadowAssignToMinions(sam, scout);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPass();
		scn.ShadowUseCardAction(scout);
		// The [orc] Orc's own wound kills Sam, before he has lost to the [sauron] Orc: its text did not kill him, nor did his losing.
		assertInZone(Zone.DEAD, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertInHand(butchery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CompanionsKilledByAMoriaOrcOrASauronUrukHaiDoNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var troop = scn.GetShadowCard("troop");
		var cuguard = scn.GetShadowCard("cuguard");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam, guard1, guard2);

		scn.StartGame();
		assertInHand(butchery);
		scn.MoveMinionsToTable(slayer, troop, cuguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, troop},
				new PhysicalCardImpl[]{guard2, cuguard},
				new PhysicalCardImpl[]{sam, slayer});
		int deck = scn.GetFreepsDeckCount();

		// Each loser below is overwhelmed: 13 against 4, 9 against 4, 7 against 3.  The [sauron] Orc waits in a
		// skirmish of its own while the first two are fought.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));
	}

	@Test
	public void ACompanionWoundedByASauronOrcButNotKilledDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		assertInHand(butchery);
		assertAttachedTo(sword, sam);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.PassCurrentPhaseActions();
		// Strength 5 against 7 is no overwhelm: the wound for losing leaves Sam at 1 of his 4.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		// The Slayer's wound outside a skirmish leaves him at 2 of 4.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertInZone(Zone.HAND, butchery);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledOutsideASkirmishBySauronOrcsWoundLetsItBePlayedButOneKilledByAnIsengardOrcsDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var servant = scn.GetShadowCard("servant");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam, guard1);

		scn.StartGame();
		assertInHand(butchery);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(guard1, 1);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard1));
		scn.MoveMinionsToTable(slayer, servant);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetFreepsDeadCount());

		scn.FreepsPass();
		scn.ShadowUseCardAction(servant);
		// The Free Peoples player makes the Servant's wound; Sam, at 3 of 4, is the one killed. A [sauron] Orc stands by, but did not do it.
		scn.FreepsChooseCard(sam);
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		// The Dwarf Guard is the only companion left whom the Slayer may wound: the same board, with a [sauron] Orc doing it.
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, guard1);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		int deck = scn.GetFreepsDeckCount();
		scn.ShadowChooseOptionalTrigger(butchery);
		// Two cards in the dead pile: 6.
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, butchery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AlliesKilledByASauronOrcDoNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var assassin = scn.GetShadowCard("assassin");
		var silinde = scn.GetFreepsCard("silinde");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToHand(butchery);
		scn.MoveAlliesToTable(silinde, elrond);

		scn.StartGame();
		assertInHand(butchery);
		// Both allies' home is site 3, where Silinde can be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(slayer, assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AddWoundsToChar(elrond, 3);
		scn.AddWoundsToChar(silinde, 1);
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(silinde));

		scn.FreepsPass();
		// Elrond is not the only ally, so the Assassin's wound is chosen.
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(elrond);
		assertInZone(Zone.DEAD, elrond);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(silinde, slayer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(silinde);
		scn.PassCurrentPhaseActions();
		// Strength 5 against 7: the wound for losing kills her.
		assertInZone(Zone.DEAD, silinde);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertInZone(Zone.HAND, butchery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
