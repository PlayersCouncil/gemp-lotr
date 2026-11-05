package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_316_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion of another title; +2 only with a weapon
					put("merry", "1_303");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					// Orc Miscreant: [orc] Orc minion, vitality 3. "Regroup: Exert this minion to add a burden."
					put("miscreant", "11_131");
					// Goblin Spear: [moria] hand weapon. "If bearer kills a companion in a skirmish, add 1 burden (or 2
					// burdens if that companion was a Hobbit)."
					put("spear", "1_182");
					// Light Shining Faintly: [shire] event. "Fellowship: Add a burden to heal a Ring-bound companion twice."
					put("light", "4_309");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");
					// Frodo, Frenzied Fighter: the one Frodo who cannot bear the Ring, so the one who may be in play
					// beside another Ring-bearer; strength 3, and his texts want more minions than companions, or a
					// skirmish he is not in
					put("frodo", "13_149");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion of another title; +2 only with a weapon
					put("merry", "1_303");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand."
					put("glamdring", "1_75");
					// Eye of Barad-dûr: [sauron] event.  "Response: If a Free Peoples player reveals this card from
					// your hand, discard this card to add 2 burdens."
					put("eye", "5_96");
					put("runner", "1_178");     // Goblin Runner: a second card in the Shadow hand
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");
					// Desperate Defense of the Ring: [sauron] condition borne by a companion.  "Each time bearer is
					// assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or
					// add a burden."
					put("defense", "1_244");
					put("runner", "1_178");     // Goblin Runner: the minion the bearer is assigned to
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he bears Glamdring
					// Glamdring: "Fellowship or Regroup: Exert Gandalf to reveal an opponent's hand."
					put("glamdring", "1_75");
					// Eye of Barad-dûr: [sauron] event.  "Response: If a Free Peoples player reveals this card from
					// your hand, discard this card to add 2 burdens."  A Shadow card that adds two burdens at once.
					put("eye", "5_96");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Sam, Samwise the Brave
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bound</b>.<br><b>Response:</b> If a Shadow card is about to add any number of burdens, spot Frodo and exert Sam to prevent this.<br><b>Response:</b> If Frodo is killed, make Sam the <b>Ring-bearer (resistance 5)</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Samwise the Brave", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PreventsTheBurdenOfAShadowCardThatTheFreePeoplesPlayerChoseToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		// Four cards in hand, his own copies of cards that stay out of play here: "discard 3 cards from hand"
		// can be done, so the choice between it and the burden is really put to the player.
		scn.MoveCardsToFreepsHand("gandalf", "glamdring", "runner", "eye");

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		assertAttachedTo(defense, frodo);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.FreepsAssignToMinions(frodo, runner);

		assertEquals(List.of("Discard 3 cards from hand", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		// The Free Peoples player picked the burden, but the card that adds it is the Shadow player's: Sam may
		// answer it (CR 4.2 "effect": "The source of an effect is the card on which that effect is printed").
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		// The burden that was chosen and prevented is not paid for with the discard after all.
		assertEquals(4, scn.GetFreepsHandCount());
		assertAttachedTo(defense, frodo);
		// The assignment stands and the game goes on with it.
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithTheResponseDeclinedTheBurdenOfTheShadowCardIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToFreepsHand("gandalf", "glamdring", "runner", "eye");

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsAssignToMinions(frodo, runner);
		assertEquals(List.of("Discard 3 cards from hand", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Add a burden");

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		// The same board with Sam holding back: the burden he could have stopped is there.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void OneExertionPreventsBothBurdensOfAShadowCardThatAddsTwoAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		// A second card in the Shadow hand, so that the Eye is one of the cards revealed and not the hand.
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowPlayAvailable(eye));
		scn.ShadowPlayCard(eye);

		// "Any number of burdens": the Eye is about to add 2.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		// One exertion, and neither burden is added (the Eye's own file reads the 2 it adds unanswered).
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(eye);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PreventsTheBurdenAMinionsOwnAbilityIsAboutToAddAndOnlyWhenUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var miscreant = scn.GetShadowCard("miscreant");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion that lived through the skirmishes stands in the regroup phase.
		scn.MoveMinionsToTable(miscreant);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		scn.ShadowUseCardAction(miscreant);
		assertEquals(1, scn.GetWoundsOn(miscreant));
		// A character's ability is about to add the burden: offered, and declined it is added.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsPass();
		scn.ShadowUseCardAction(miscreant);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsAcceptOptionalTrigger();

		// Sam exerts, and the second burden is not added.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsTheBurdensAShadowPossessionIsAboutToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, spear);
		assertAttachedTo(spear, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		// 3 against 13 + 2: Merry is overwhelmed, and the Spear is about to add 2 burdens for the Hobbit it killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFreePeoplesCardAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(light));

		// Its cost is a burden, and the card that adds it is a Free Peoples one.
		scn.FreepsPlayCard(light);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sam));

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithSamExhaustedTheResponseIsNotOfferedAndBothBurdensAreAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(sam, 3);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		scn.ShadowPlayCard(eye);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAFrodoWhoIsNotTheRingBearerToSpotTheResponseIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, frodo, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();

		assertEquals("Galadriel", scn.GetRingBearer().getBlueprint().getTitle());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		scn.ShadowPlayCard(eye);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFrodoToSpotTheResponseIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var eye = scn.GetShadowCard("eye");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, merry, gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(eye, runner);

		scn.StartGame();

		assertEquals("Galadriel", scn.GetRingBearer().getBlueprint().getTitle());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(glamdring);
		scn.FreepsDismissRevealedCards();
		scn.ShadowPlayCard(eye);

		// A [shire] Hobbit companion of another title is no Frodo: the burdens go on.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenFrodoIsOverwhelmedSamCanBecomeTheRingBearerWithNoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertSame(frodo, scn.GetRingBearer());
		assertAttachedTo(ring, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(sam));
		// 5 less 2 burdens
		assertEquals(3, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		// The burdens went with Frodo.
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenFrodoIsKilledByWoundsSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertSame(frodo, scn.GetRingBearer());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Vitality 4: he is killed when the game next checks.
		scn.AddWoundsToChar(frodo, 4);
		assertEquals(4, scn.GetWoundsOn(frodo));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoIsKilledLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, merry);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, troop);
		assertInZone(Zone.FREE_CHARACTERS, merry);

		// 3 against 13: Merry is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: Galadriel is overwhelmed, and Sam may not take her place.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertInZone(Zone.FREE_CHARACTERS, frodo);

		// 3 against 13: this Frodo is overwhelmed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
