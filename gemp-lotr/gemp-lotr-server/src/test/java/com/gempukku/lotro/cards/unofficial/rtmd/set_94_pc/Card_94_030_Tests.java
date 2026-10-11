package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_030_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// A Wizard Is Never Late: Fellowship event, plays a [gandalf] character straight from
					// the draw deck
					put("wizardlate", "1_87");
					// Gandalf, Friend of the Shirefolk: the [gandalf] character to fetch, and the "spot
					// Gandalf" for Risk a Little Light
					put("gandalf", "1_72");
					// Risk a Little Light: looks at the top 2 cards and takes one into hand, discarding
					// the other
					put("risk", "1_82");
					// Knocked on the Head: Regroup event, plays an unbound Hobbit from the discard pile
					put("knocked", "4_308");
					// Merry, Friend to Sam: unbound Hobbit, target for Knocked on the Head and for the
					// looks at the deck
					put("merry", "1_302");
					put("gimli", "1_13");  // Gimli, Son of Gloin: the Dwarf that bears Ring of Guile
					// Ring of Guile: Maneuver action takes a [dwarven] event into hand from the discard pile
					put("guile", "9_8");
					put("shaft", "1_22");  // Mithril Shaft: the [dwarven] event sitting in the discard pile
					// Goblin Patrol Troop: a minion so the maneuver and regroup phases are actually reached
					put("troop", "1_177");
					// Host of Thousands: Shadow event, plays a [moria] Orc from the discard pile
					put("host", "1_187");
					put("backstabber", "1_174"); // Goblin Backstabber: the [moria] Orc sitting in the discard pile
					// Delving: Fellowship event, exert a Dwarf companion to draw 3 cards: a card drawn is
					// a card taken into hand from the draw deck
					put("delving", "1_6");
					// Library of Orthanc: takes a card stacked on it into hand -- not deck or discard
					put("library", "9_39");
					// Troop of Uruk-hai: the [isengard] card stacked and taken into hand
					put("stackedminion", "1_143");
					put("stackedaxe", "1_9");     // Dwarven Axe: filler card stacked on Library of Orthanc
					put("stackedextra", "1_178"); // Goblin Runner: filler card stacked on Library of Orthanc
					// A New Light: Fellowship event; a Free Peoples player returns a minion from a Shadow
					// player's discard pile to that player's hand, then discards a minion from that hand
					put("light", "17_15");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_30", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The same deck as the scenario with the modifier in player 1's seat
					put("wizardlate", "1_87");    // A Wizard Is Never Late
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk
					put("risk", "1_82");          // Risk a Little Light
					put("knocked", "4_308");      // Knocked on the Head
					put("merry", "1_302");        // Merry, Friend to Sam
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("guile", "9_8");          // Ring of Guile
					put("shaft", "1_22");         // Mithril Shaft
					put("troop", "1_177");        // Goblin Patrol Troop
					put("host", "1_187");         // Host of Thousands
					put("backstabber", "1_174");  // Goblin Backstabber
					put("delving", "1_6");        // Delving
					put("library", "9_39");       // Library of Orthanc
					put("stackedminion", "1_143"); // Troop of Uruk-hai
					put("stackedaxe", "1_9");     // Dwarven Axe
					put("stackedextra", "1_178"); // Goblin Runner
					put("light", "17_15");        // A New Light
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_30"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The same deck as the scenario with the modifier in player 1's seat
					put("wizardlate", "1_87");    // A Wizard Is Never Late
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk
					put("risk", "1_82");          // Risk a Little Light
					put("knocked", "4_308");      // Knocked on the Head
					put("merry", "1_302");        // Merry, Friend to Sam
					put("gimli", "1_13");         // Gimli, Son of Gloin
					put("guile", "9_8");          // Ring of Guile
					put("shaft", "1_22");         // Mithril Shaft
					put("troop", "1_177");        // Goblin Patrol Troop
					put("host", "1_187");         // Host of Thousands
					put("backstabber", "1_174");  // Goblin Backstabber
					put("delving", "1_6");        // Delving
					put("library", "9_39");       // Library of Orthanc
					put("stackedminion", "1_143"); // Troop of Uruk-hai
					put("stackedaxe", "1_9");     // Dwarven Axe
					put("stackedextra", "1_178"); // Goblin Runner
					put("light", "17_15");        // A New Light
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_30
		 * Type: MetaSite
		 * Intensity: 5
		 * Game Text: You may not play or take into hand cards from your discard pile or draw deck (except
		 * when reconciling or starting fellowship).
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_30", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(5, mod.getBlueprint().getIntensity());
	}

	@Test
	public void CannotPlayFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var wizardlate = scn.GetFreepsCard("wizardlate");

		scn.MoveCardsToHand(wizardlate);
		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(wizardlate));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModPlayingFromDrawDeckWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var wizardlate = scn.GetFreepsCard("wizardlate");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToHand(wizardlate);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(wizardlate));
		scn.FreepsPlayCard(wizardlate);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(gandalf);

		assertInPlay(gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotPlayFromDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var knocked = scn.GetFreepsCard("knocked");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");

		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToHand(knocked);
		scn.MoveCardsToDiscard(merry);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.FreepsPlayAvailable(knocked));
		assertInDiscard(merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutTheModPlayingFromDiscardPileWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var knocked = scn.GetFreepsCard("knocked");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");

		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToHand(knocked);
		scn.MoveCardsToDiscard(merry);
		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(knocked));
		scn.FreepsPlayCard(knocked);

		assertInPlay(merry);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotTakeIntoHandFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var risk = scn.GetFreepsCard("risk");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);
		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry);

		scn.FreepsPlayCard(risk);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(merry);

		// Looking and choosing still happen; only the trip to hand is prevented, so the card
		// stays in the deck and is swept up by the card's own "discard the other" clause.
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModTakingIntoHandFromDrawDeckWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var risk = scn.GetFreepsCard("risk");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(risk);
		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry);

		scn.FreepsPlayCard(risk);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(merry);

		assertInHand(merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotTakeIntoHandFromDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guile = scn.GetFreepsCard("guile");
		var shaft = scn.GetFreepsCard("shaft");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(gimli, guile);
		scn.MoveCardsToDiscard(shaft);
		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(guile));
		scn.FreepsUseCardAction(guile);

		assertInDiscard(shaft);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithoutTheModTakingIntoHandFromDiscardPileWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var guile = scn.GetFreepsCard("guile");
		var shaft = scn.GetFreepsCard("shaft");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(gimli, guile);
		scn.MoveCardsToDiscard(shaft);
		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(guile);

		assertInHand(shaft);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ReconciliationDrawsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// "(except when reconciling ...)": every other draw of the owner's draws nothing, and the reconcile at
		// the end of his own turn still fills his hand to eight.
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var shaft = scn.GetFreepsCard("shaft");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(shaft);
		scn.StartGame();

		int deckBefore = scn.GetFreepsDeckCount();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		assertEquals(1 + Math.min(7, deckBefore), scn.GetFreepsHandCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ADrawOutsideAReconcileTakesNoCardFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		// A card drawn is a card taken into hand from the draw deck, and the text's one exception in play is
		// reconciling: Delving's "exert a Dwarf companion to draw 3 cards" is played, paid for, and draws nothing.
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving);
		scn.StartGame();

		int deckBefore = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(delving));
		scn.FreepsPlayCard(delving);

		// The event is played and its exertion paid, and no card has left the draw deck
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInDiscard(delving);
		assertEquals(deckBefore, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModADrawTakesCardsFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving);
		scn.StartGame();

		int deckBefore = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(delving));
		scn.FreepsPlayCard(delving);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(deckBefore - 3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingACardFromTheOpponentsDiscardPileIsUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// The restriction is on "your" piles: the owner's A New Light returns a Shadow minion from the Shadow
		// player's discard pile to the Shadow player's hand. Two minions lie there so that one is chosen, and
		// a second minion is in his hand for the card's own "discard a minion" to take, as the returned one
		// would otherwise be the only choice and be discarded in turn.
		var scn = GetFreepsScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var light = scn.GetFreepsCard("light");
		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("stackedextra");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(backstabber, troop);
		scn.MoveCardsToHand(runner);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		assertEquals(1, scn.GetShadowHandCount());
		scn.FreepsPlayCard(light);

		assertTrue(scn.FreepsDecisionAvailable("choose card from discard"));
		scn.FreepsChooseCardBPFromSelection(backstabber);
		assertInHand(backstabber);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(troop);

		// "You may reveal that Shadow player's hand"
		scn.FreepsChooseYes();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(runner);

		assertInHand(backstabber);
		assertInDiscard(runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentsCardCanReturnTheOwnersMinionFromHisDiscardPileToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		// The same board with the modifier in the Shadow player's seat: "You may not ... take into hand cards
		// from your discard pile" is about the owner's own taking, and here it is the Free Peoples player's
		// A New Light that puts the Shadow player's minion into his hand.
		var scn = GetShadowScenario();
		var gandalf = scn.GetFreepsCard("gandalf");
		var light = scn.GetFreepsCard("light");
		var backstabber = scn.GetShadowCard("backstabber");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("stackedextra");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(light);
		scn.MoveCardsToDiscard(backstabber, troop);
		scn.MoveCardsToHand(runner);
		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(light));
		assertEquals(1, scn.GetShadowHandCount());
		scn.FreepsPlayCard(light);

		assertTrue(scn.FreepsDecisionAvailable("choose card from discard"));
		scn.FreepsChooseCardBPFromSelection(backstabber);
		assertInHand(backstabber);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(troop);

		// "You may reveal that Shadow player's hand"
		scn.FreepsChooseYes();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(runner);

		assertInHand(backstabber);
		assertInDiscard(runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingAStackedCardIntoHandIsUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Library of Orthanc's "Regroup: take an [isengard] card stacked here into hand" reaches into the
		// stacked zone, not the discard pile or draw deck, so 94_30 (owned here by the Shadow player) must
		// not block it -- confirms both that stacking is unaffected and that other zones are unaffected.
		var scn = GetShadowScenario();
		var library = scn.GetShadowCard("library");
		var stackedminion = scn.GetShadowCard("stackedminion");
		var stackedaxe = scn.GetShadowCard("stackedaxe");
		var stackedextra = scn.GetShadowCard("stackedextra");

		scn.MoveCardsToSupportArea(library);
		scn.StackCardsOn(library, stackedaxe, stackedextra, stackedminion);
		scn.StartGame();
		scn.SetTwilight(5);

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(library));
		assertEquals(3, scn.GetStackedCards(library).size());
		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowUseCardAction(library);

		assertInHand(stackedminion);
		assertEquals(2, scn.GetStackedCards(library).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheStartingFellowshipIsPlayedAndTheOpeningHandDealtAsEver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var merry = scn.GetFreepsCard("merry");

		// "(except when ... starting fellowship)": the modifier is in play from set-up, and a starting
		// fellowship is played from the draw deck.  The opening hand is set-up as well, and is dealt.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, merry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		scn.FreepsChooseCard(merry);
		assertInPlay(merry);

		scn.FreepsDeclineChoosing();
		scn.ShadowDeclineChoosing();
		// This start of the game leaves the starting fellowships and the opening hands as they were dealt
		scn.StartGame(false, false);

		assertInPlay(merry);
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsIsUnrestricted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var wizardlate = scn.GetFreepsCard("wizardlate");
		var gandalf = scn.GetFreepsCard("gandalf");
		var risk = scn.GetFreepsCard("risk");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wizardlate, risk);
		scn.StartGame();
		scn.MoveCardsToTopOfDeck(merry);

		assertTrue(scn.FreepsPlayAvailable(wizardlate));
		scn.FreepsPlayCard(risk);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(merry);

		assertInHand(merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OwnerIsBlockedWhileActingAsShadow() throws DecisionResultInvalidException, CardNotFoundException {
		// The mod belongs to a player, not to a side: here the owner is the Shadow player.
		var scn = GetShadowScenario();
		var host = scn.GetShadowCard("host");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCardsToShadowHand("host");
		scn.MoveCardsToDiscard(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(5);

		assertFalse(scn.ShadowPlayAvailable(host));
		assertInDiscard(backstabber);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutTheModTheShadowPlayerCanPlayFromDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var host = scn.GetShadowCard("host");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCardsToShadowHand("host");
		scn.MoveCardsToDiscard(backstabber);
		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(5);

		assertTrue(scn.ShadowPlayAvailable(host));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
