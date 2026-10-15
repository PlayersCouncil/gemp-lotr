package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_265_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("butchery", "51_265");

					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, strength 11, vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; "Regroup: Exert this minion to make the Free Peoples player wound a companion."
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai (not an Orc), strength 9, vitality 2, damage +1
					put("scout", "15_116");     // Scouting Orc: [orc] Orc, strength 8, vitality 2; "Skirmish: Exert this minion to wound a companion it is skirmishing."

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

	protected VirtualTableScenario GetCourteousHalflingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("butchery", "51_265");
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2

					put("sam", "2_114");        // Sam, Proper Poet: companion, strength 3, vitality 4; Ring-bound
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("troop", "1_177");      // these two stay in the draw deck, so that four cards could be discarded from it
					put("cuguard", "10_81");
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",                        // Frodo, Courteous Halfling: with 3 unbound companions spotted, Shadow cards may not discard cards from the top of your draw deck
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
		 * Game Text: If a companion is killed by a [sauron] Orc, then for each card in the dead pile the Free Peoples
		 * player must choose to add a burden or discard 4 cards from the top of their draw deck.
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
	public void ACompanionOverwhelmedByASauronOrcLetsItBePlayedAndTheFreePeoplesPlayerMayAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetBurdens());

		scn.PassCurrentPhaseActions();
		// Strength 7 against Sam's 3: overwhelmed.
		assertInZone(Zone.DEAD, sam);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		// One card in the dead pile: one choice, and the Free Peoples player makes it.
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));

		scn.FreepsChooseOption("burden");
		assertEquals(1, scn.GetBurdens());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, butchery);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledByASauronOrcsSkirmishWoundLetsTheFreePeoplesPlayerDiscardTheTopFourCardsOfTheirDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		var top1 = scn.GetFromTopOfFreepsDeck(1);
		var top2 = scn.GetFromTopOfFreepsDeck(2);
		var top3 = scn.GetFromTopOfFreepsDeck(3);
		var top4 = scn.GetFromTopOfFreepsDeck(4);
		var top5 = scn.GetFromTopOfFreepsDeck(5);
		int deck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.PassCurrentPhaseActions();
		// Strength 5 against 7 is no overwhelm: the wound for losing kills Sam, who had 3 of his 4.
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("Discard");

		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, top1, top2, top3, top4);
		assertInZone(Zone.DECK, top5);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInTheOrcsSkirmishByAnotherMinionsWoundDoesNotLetItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var scout = scn.GetShadowCard("scout");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
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
		// The [orc] Orc's own wound kills Sam, before he has lost to the [sauron] Orc: its text did not kill him, nor did
		// his losing (CR 4.2 Section Three, The Ring's Oppression 1_273; Q-0328: B).
		assertInZone(Zone.DEAD, sam);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));
		assertInHand(butchery);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledByASauronOrcsAbilityOutsideASkirmishLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		// Sam is the only companion who is not the Ring-bearer.
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.ShadowChooseOptionalTrigger(butchery);
		scn.FreepsChooseOption("burden");
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, butchery);
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
		scn.MoveMinionsToTable(slayer, troop, cuguard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard1, troop},
				new PhysicalCardImpl[]{guard2, cuguard},
				new PhysicalCardImpl[]{sam, slayer});

		// Each loser below is overwhelmed: 13 against 4, 9 against 4, 7 against 3.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));

		scn.FreepsResolveSkirmish(guard2);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, guard2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(butchery));

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
	public void ACompanionKilledOutsideASkirmishByAnIsengardOrcDoesNotLetItBePlayedButOneKilledByASauronOrcDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var servant = scn.GetShadowCard("servant");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam, guard1);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(guard1, 1);
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
		// Both allies' home is site 3, where Silinde can be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(slayer, assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AddWoundsToChar(elrond, 3);
		scn.AddWoundsToChar(silinde, 1);

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
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerChoosesOnceForEachCardInTheirDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var silinde = scn.GetFreepsCard("silinde");
		var elrond = scn.GetFreepsCard("elrond");
		var shadowGuard = scn.GetShadowCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		// A dead ally is a card in the dead pile as much as a dead companion is.
		scn.MoveCardsToDeadPile(guard1, guard2, elrond);
		// Neither of these is in the Free Peoples player's dead pile.
		scn.MoveCardsToDeadPile(shadowGuard);
		scn.MoveCardsToFreepsDiscard("silinde");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		int deck = scn.GetFreepsDeckCount();
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, shadowGuard);
		assertInZone(Zone.DISCARD, silinde);
		assertEquals(4, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetBurdens());

		scn.ShadowChooseOptionalTrigger(butchery);
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("burden");
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("burden");
		assertEquals(2, scn.GetBurdens());

		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("burden");
		assertEquals(3, scn.GetBurdens());

		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("burden");
		assertEquals(4, scn.GetBurdens());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnceFewerThanFourCardsAreLeftInTheDrawDeckABurdenIsAddedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDeadPile(guard1);
		scn.MoveCardsToFreepsDiscard("troop", "cuguard", "assassin", "silinde", "servant", "scout");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		// The butchery, slayer, sword, guard2 and elrond of the Free Peoples player's own copies.
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetBurdens());

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetFreepsDeadCount());
		scn.ShadowChooseOptionalTrigger(butchery);
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));
		scn.FreepsChooseOption("Discard");
		assertEquals(1, scn.GetFreepsDeckCount());

		// The second card in the dead pile: four cards cannot be discarded from one, so the burden is all that is left (CR 4.2 "effect").
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhereShadowCardsMayNotDiscardFromTheTopOfTheDrawDeckTheDiscardIsStillOfferedAndTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourteousHalflingScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		// The Free Peoples player's own copies of the four cards left in the draw deck.
		var top1 = scn.GetFreepsCard("butchery");
		var top2 = scn.GetFreepsCard("slayer");
		var top3 = scn.GetFreepsCard("troop");
		var top4 = scn.GetFreepsCard("cuguard");
		// Frodo and Sam are Ring-bound; the three Guards are three unbound companions.
		scn.MoveCardsToHand(butchery);
		scn.MoveCompanionsToTable(sam, guard1, guard2, guard3);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.MoveCardsToTopOfDeck(top4, top3, top2, top1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		int deck = scn.GetFreepsDeckCount();
		// The butchery, slayer, troop and cuguard of the Free Peoples player's own copies.
		assertEquals(4, deck);
		assertEquals(0, scn.GetBurdens());

		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DEAD, sam);
		scn.ShadowChooseOptionalTrigger(butchery);
		// Frodo stops Shadow cards that make the Free Peoples player discard, but the event gives him a choice
		// and he may choose to discard (F-0205: not a fault): the discard is offered beside the burden, and taken.
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard"));

		scn.FreepsChooseOption("Discard");
		assertInDiscard(top1, top2, top3, top4);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
