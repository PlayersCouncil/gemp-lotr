package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_030_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("wizardlate", "1_87"); // A Wizard Is Never Late: Fellowship event, plays a [gandalf] character straight from the draw deck
		put("gandalf", "1_72"); // Gandalf, Friend of the Shirefolk: the [gandalf] character to fetch, and the "spot Gandalf" for Risk a Little Light
		put("risk", "1_82"); // Risk a Little Light: looks at the top 2 cards and takes one into hand, discarding the other
		put("knocked", "4_308"); // Knocked on the Head: Regroup event, plays an unbound Hobbit from the discard pile
		put("merry", "1_302"); // Merry, Friend to Sam: unbound Hobbit, target for Knocked on the Head and for the deck searches
		put("gimli", "1_13"); // Gimli, Son of Glóin: the Dwarf that bears Ring of Guile
		put("guile", "9_8"); // Ring of Guile: Maneuver action takes a [dwarven] event into hand from the discard pile
		put("shaft", "1_22"); // Mithril Shaft: the [dwarven] event sitting in the discard pile
		put("troop", "1_177"); // Goblin Patrol Troop: a minion so the maneuver and regroup phases are actually reached
		put("host", "1_187"); // Host of Thousands: Shadow event, plays a [moria] Orc from the discard pile
		put("backstabber", "1_174"); // Goblin Backstabber: the [moria] Orc sitting in the discard pile
		put("delving", "1_6"); // Delving: Fellowship event, exert a Dwarf companion to draw 3 cards (ordinary draw)
		put("library", "9_39"); // Library of Orthanc: takes a card stacked on it into hand -- not deck or discard
		put("stackedminion", "1_143"); // Troop of Uruk-hai: the [isengard] card stacked and taken into hand
		put("stackedaxe", "1_9"); // filler card stacked on Library of Orthanc
		put("stackedextra", "1_178"); // filler card stacked on Library of Orthanc
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_30", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_30"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
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
		 * Game Text: You may not play or take into hand cards from your discard pile or draw deck (except when reconciling).
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
	}

	@Test
	public void ReconciliationDrawsAreUnaffected() throws DecisionResultInvalidException, CardNotFoundException {
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
	}

	@Test
	public void OrdinaryDrawEffectIsNotBlocked() throws DecisionResultInvalidException, CardNotFoundException {
		// Delving's "exert a Dwarf to draw 3 cards" is an ordinary top-of-deck draw, unrelated to 94_30's
		// discard-pile/draw-deck play-and-take restriction.
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving);
		scn.StartGame();

		int deckBefore = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsPlayAvailable(delving));
		scn.FreepsPlayCard(delving);

		assertEquals(deckBefore - 3, scn.GetFreepsDeckCount());
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

		scn.FreepsPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(library));
		scn.ShadowUseCardAction(library);

		assertInHand(stackedminion);
		assertEquals(2, scn.GetStackedCards(library).size());
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
	}
}
