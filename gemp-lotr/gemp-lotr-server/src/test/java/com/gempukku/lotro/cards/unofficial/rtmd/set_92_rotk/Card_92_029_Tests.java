package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_029_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea: unique Nazgul minion, twilight 6. The minion that gives a turn its maneuver phase
					put("enquea", "1_231");
					// Ulaire Cantea: unique Nazgul minion, twilight 5 — in test booster, for uniqueness test
					put("cantea", "1_230");
					// Goblin Patrol Troop: [moria] Orc, vitality 3, no text. The Orc an event's own cost would exert
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_29"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Ulaire Enquea: unique Nazgul minion, twilight 6. The minion that gives a turn its maneuver phase
					put("enquea", "1_231");
					// Ulaire Cantea: unique Nazgul minion, twilight 5 — in test booster, for uniqueness test
					put("cantea", "1_230");
					// Goblin Patrol Troop: [moria] Orc, vitality 3, no text. The Orc an event's own cost would exert
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_29", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_29
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Shadow: Open and reveal a sealed booster pack (limit once per turn).
		 *   You may play one shadow card revealed, ignoring all costs.
		 */

		var scn = GetShadowScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_29", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void BasicFlowPlaysAShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		// Step 3: Shadow player picks a shadow card to play — pick Goblin Backstabber (1_174)
		scn.ChooseCardBPFromSelection(scn.P2, "1_174");

		// Goblin Backstabber should be in play (shadow characters zone)
		var backstabber = scn.GetLastCreatedCard();
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		scn.StopCardCapture();

		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FellowshipBoosterOpenTest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.FreepsPass(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("FotR - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		assertEquals(11, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowDecisionAvailable("Booster pack contents"));
	}

	@Test
	public void ReflectionsBoosterOpenTest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.StartGame();
		scn.FreepsPass(); // fellowship phase pass → auto-move

		// Now in shadow phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Step 1: Choose a booster pack
		scn.ShadowChooseCardBPFromSelection("REF - Booster");

		// Step 2: Opponent dismisses the revealed cards
		scn.FreepsDismissRevealedCards();

		assertEquals(18, scn.ShadowGetBPChoiceCount());
		assertTrue(scn.ShadowDecisionAvailable("Booster pack contents"));
	}

	@Test
	public void LimitOncePerTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// First use
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		// Decline to play any card
		scn.ShadowDeclineChoosing();

		// Second use in same turn — should NOT be available
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheLimitIsForTheTurnAndNotThePhaseAndALaterTurnHasItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// The first Shadow phase of player 1's turn
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ShadowDeclineChoosing();
		assertFalse(scn.ShadowActionAvailable(mod));

		// The fellowship moves again: a second Shadow phase in the same turn, and no second pack
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));

		// Player 2's own turn, and then player 1's next: player 2 is the Shadow player again, on a new turn
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var enquea = scn.GetShadowCard("enquea");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// Offered in the Shadow phase, and not used: the turn's one use is still to be had
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(mod));

		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingFreepsCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		// When Freeps owns the mod, Shadow player cannot use it
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		scn.StartGame();
		scn.FreepsPass();

		// Shadow phase — the mod is owned by Freeps, so Shadow can't use it
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOwnerInTheFirstSeatOpensAPackWhenHeIsTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();

		// Player 2's turn: player 1 is the Shadow player, and the modifier is his
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertTrue(scn.FreepsActionAvailable(mod));

		scn.StartCardCapture();
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCardBPFromSelection("Test - Booster");
		// The other player is the one shown the pack
		assertEquals(8, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection("1_174");

		var backstabber = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertEquals("Goblin Backstabber", backstabber.getBlueprint().getTitle());
		assertEquals(scn.P1, backstabber.getOwner());
		assertInZone(Zone.SHADOW_CHARACTERS, backstabber);
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void OptionalPlayCanDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// "May play one": none need be chosen, and no more than one can be
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		// Decline to play (select 0 cards)
		scn.ShadowDeclineChoosing();

		// Nothing was played and nothing paid, and the cards of the pack have gone to nobody's piles
		assertEquals(0, scn.GetCreatedCards().size());
		scn.StopCardCapture();
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// The play was the owner's to make or not: the other player is asked nothing
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// Should return to normal shadow phase actions
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IgnoresToPlayRequirements() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll of Moria (1_165) requires spotting a Moria Orc to play.
		// With no Moria Orcs on the table, it should still be playable via booster ignoring all costs.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Cave Troll requires a Moria Orc — none on the table, but should still be selectable
		scn.ChooseCardBPFromSelection(scn.P2, "1_165");

		var caveTroll = scn.GetLastCreatedCard();
		assertNotNull(caveTroll);
		assertEquals("Cave Troll of Moria", caveTroll.getBlueprint().getTitle());
		assertInZone(Zone.SHADOW_CHARACTERS, caveTroll);
		scn.StopCardCapture();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IgnoresExtraCosts() throws DecisionResultInvalidException, CardNotFoundException {
		// Alive and Unspoiled (1_120) has ExtraCost: exert an Uruk-hai.
		// With no Uruk-hai on the table, it should still be playable via booster ignoring all costs.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Alive and Unspoiled requires exerting an Uruk-hai — none present, but should still work
		scn.ChooseCardBPFromSelection(scn.P2, "1_120");

		var aliveAndUnspoiled = scn.GetLastCreatedCard();
		assertNotNull(aliveAndUnspoiled);
		assertEquals("Alive and Unspoiled", aliveAndUnspoiled.getBlueprint().getTitle());
		assertInZone(Zone.SUPPORT, aliveAndUnspoiled);
		scn.StopCardCapture();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IgnoresTheCostInAnEventsOwnText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var troop = scn.GetShadowCard("troop");

		// A pack of one card. Threat of the Unknown (1_197): [moria] event of the Shadow phase, cost 0,
		// "Exert your [moria] Orc to draw 2 cards."
		scn.UseBoosterOf("1_197");
		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		// The Shadow player's [moria] Orc is on the table and able to exert
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetShadowHandCount());
		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_197"));
		scn.ShadowChooseCardBPFromSelection("1_197");

		var threat = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertEquals("Threat of the Unknown", threat.getBlueprint().getTitle());
		assertInZone(Zone.DISCARD, threat);
		// The event did what it says, and the exertion it asks for first was not made
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventWhoseOwnCostCannotBePaidIsStillPlayedAndDoesWhatItSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		// Threat of the Unknown again, and no minion on the table: nobody who could exert for it
		scn.UseBoosterOf("1_197");
		scn.StartGame();
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_197"));
		scn.ShadowChooseCardBPFromSelection("1_197");

		var threat = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertInZone(Zone.DISCARD, threat);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IgnoresTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		// Cave Troll of Moria (1_165) costs 10 twilight. With 0 in the pool,
		// it should still be playable ignoring all costs.
		// (This test also validates ignoring ToPlay, but the focus is twilight.)
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// Verify twilight pool is low: 1 companion + 2 for the site
		assertTrue(scn.GetTwilight() < 10);
		assertEquals(3, scn.GetTwilight());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();
		scn.ChooseCardBPFromSelection(scn.P2, "1_165");

		var caveTroll = scn.GetLastCreatedCard();
		assertNotNull(caveTroll);
		assertInZone(Zone.SHADOW_CHARACTERS, caveTroll);
		scn.StopCardCapture();
		// Neither its cost of 10 nor the 2 for roaming at site 2 was taken from the pool
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RespectsUniqueness() throws DecisionResultInvalidException, CardNotFoundException {
		// Ulaire Cantea (1_230) is unique and already on the table.
		// The booster also contains 1_230, but it should NOT be selectable.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var cantea = scn.GetShadowCard("cantea");

		scn.MoveMinionsToTable(cantea);

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Cantea (1_230) should be visible but NOT selectable since it's already in play.
		// The selectable shadow cards should be: 1_174 (Backstabber), 1_165 (Cave Troll), 1_120 (Alive and Unspoiled)
		// but NOT 1_230 (Cantea, unique duplicate) and NOT 1_286 (Bounder, FP card).
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_230"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_230", false));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174"));
		assertTrue(scn.ShadowDecisionAvailable("Booster pack contents"));
	}

	@Test
	public void FPCardsNotSelectable() throws DecisionResultInvalidException, CardNotFoundException {
		// Bounder (1_286) is a Free Peoples card in the test booster.
		// It should be visible but NOT selectable since only shadow cards can be played.
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Bounder (1_286) is FP — should not be selectable
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_286"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_286", false));
		// But shadow cards should be
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_174"));
		assertTrue(scn.ShadowDecisionAvailable("Booster pack contents"));
	}

	@Test
	public void OfTheEventsRevealedOnlyTheShadowPhaseEventMayBeChosenAndItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		// 1 companion + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.StartCardCapture();
		scn.ShadowUseCardAction(mod);
		scn.ShadowChooseCardBPFromSelection("Test - Booster");
		scn.FreepsDismissRevealedCards();

		// Enheartened Foe (1_247), a Shadow response event, and Drums in the Deep (1_168), a Shadow skirmish
		// event, are shown and cannot be chosen. Foul Things (2_59), a Shadow event of the Shadow phase, can.
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_247", false));
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_247"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_168", false));
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_168"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("2_59"));

		scn.ShadowChooseCardBPFromSelection("2_59");

		// Foul Things was played ("Play a [moria] minion from your discard pile", and there is none) and lies
		// in its player's discard pile. Its cost of 2 was not paid: paid, the pool would read 1.
		var foulThings = scn.GetLastCreatedCard();
		scn.StopCardCapture();
		assertEquals("Foul Things", foulThings.getBlueprint().getTitle());
		assertInZone(Zone.DISCARD, foulThings);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOpenedPackIsShownToTheOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");

		scn.UseTestBoosters();
		scn.StartGame();
		scn.FreepsPass();

		scn.ShadowUseCardAction(mod);
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowChooseCardBPFromSelection("Test - Booster");

		// A revealed card is shown to all players (F-0039: the pack used to be shown to the Shadow player alone).
		assertTrue(scn.FreepsGetTotalRevealedCardCount() > 0);
		// All eight cards of the pack, Shadow and Free Peoples, and none of them his to choose
		assertEquals(8, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasBPChoiceAvailable("1_174", false));
		assertTrue(scn.FreepsHasBPChoiceAvailable("1_286", false));
		assertFalse(scn.FreepsHasBPChoiceAvailable("1_174"));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.ShadowGetTotalRevealedCardCount() > 0);
		assertEquals(8, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowDecisionAvailable("Booster pack contents"));
	}
}
