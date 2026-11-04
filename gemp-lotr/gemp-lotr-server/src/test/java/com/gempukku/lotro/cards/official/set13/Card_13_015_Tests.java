package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_015_Tests
{

	// Two forests on the Fellowship path: Breeland Forest 1_329 as site 2 ("Stealth events may not be played") and
	// Rivendell Waterfall 1_342 as site 3 (a sanctuary; its move limit +1 wants a ranger).  Bag End 1_319, Balin's Tomb
	// 1_343 and The Bridge of Khazad-dum 1_349 are no forests.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "13_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: [elven] Elf companion; "To play, spot an Elf" is its whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, for Rivendell Waterfall's move limit; a maneuver ability only

					// For the draw deck:
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("strands", "9_22");     // Strands of Elven Hair: an [elven] possession
					put("elf2", "1_53");        // Lórien Elf: an [elven] companion
					put("knight2", "5_35");     // Gondorian Knight: a [gondor] companion
					put("servant", "1_83");     // Servant of the Secret Fire: a [gandalf] event
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_329");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// One forest: Rivendell Waterfall 1_342 as site 3; Ettenmoors 1_331 stays site 2.
	protected VirtualTableScenario GetOneForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "13_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: [elven] Elf companion; "To play, spot an Elf" is its whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, for Rivendell Waterfall's move limit; a maneuver ability only

					// For the draw deck:
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("strands", "9_22");     // Strands of Elven Hair: an [elven] possession
					put("elf2", "1_53");        // Lórien Elf: an [elven] companion
					put("knight2", "5_35");     // Gondorian Knight: a [gondor] companion
					put("servant", "1_83");     // Servant of the Secret Fire: a [gandalf] event
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Three forests: Hobbiton Woods 2_116 as site 1 (its fellowship ability is used only
	// on purpose), Breeland Forest 1_329 and Rivendell Waterfall 1_342.
	protected VirtualTableScenario GetThreeForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "13_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: [elven] Elf companion; "To play, spot an Elf" is its whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, for Rivendell Waterfall's move limit; a maneuver ability only

					// For the draw deck:
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("strands", "9_22");     // Strands of Elven Hair: an [elven] possession
					put("elf2", "1_53");        // Lórien Elf: an [elven] companion
					put("knight2", "5_35");     // Gondorian Knight: a [gondor] companion
					put("servant", "1_83");     // Servant of the Secret Fire: a [gandalf] event
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "2_116");
					put("site2", "1_329");
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Fellowship path: no forest on it.
	protected VirtualTableScenario GetNoForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "13_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; +3 only against a Nazgûl
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: [elven] Elf companion; "To play, spot an Elf" is its whole text
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, for Rivendell Waterfall's move limit; a maneuver ability only

					// For the draw deck:
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("strands", "9_22");     // Strands of Elven Hair: an [elven] possession
					put("elf2", "1_53");        // Lórien Elf: an [elven] companion
					put("knight2", "5_35");     // Gondorian Knight: a [gondor] companion
					put("servant", "1_83");     // Servant of the Secret Fire: a [gandalf] event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Galadriel, Sorceress of the Hidden Land
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: To play, spot 3 Elves.<br>Each time the fellowship moves during the regroup phase, you may reveal a card from the top of your draw deck for each forest on the adventure path to heal an Elf for each [elven] card revealed.
		*/

		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals("Sorceress of the Hidden Land", galadriel.getBlueprint().getSubtitle());
		assertTrue(galadriel.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, galadriel.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, galadriel.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, galadriel.getBlueprint().getCardType());
		assertEquals(Race.ELF, galadriel.getBlueprint().getRace());
		assertEquals(0, galadriel.getBlueprint().getTwilightCost());
		assertEquals(3, galadriel.getBlueprint().getStrength());
		assertEquals(3, galadriel.getBlueprint().getVitality());
		assertEquals(7, galadriel.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilThreeElvesAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var lorien = scn.GetFreepsCard("lorien");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(arwen, knight);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(galadriel, lorien);
		assertInHand(galadriel, lorien);

		scn.StartGame();

		// Two Elves, a companion and an ally, and a Man beside them: not playable.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(galadriel));

		// A third Elf, a companion of another title, by legal play.
		scn.FreepsPlayCard(lorien);
		assertInZone(Zone.FREE_CHARACTERS, lorien);
		assertTrue(scn.FreepsPlayAvailable(galadriel));

		scn.FreepsPlayCard(galadriel);
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARegroupMoveRevealsACardForEachForestAndHealsAnElfForEachElvenCardAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var knight = scn.GetFreepsCard("knight");
		var defiance = scn.GetFreepsCard("defiance");
		var knight2 = scn.GetFreepsCard("knight2");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(galadriel, arwen, knight);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(knight));

		// To site 4, and no minion: on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance, knight2, strands);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		// The move to site 5.  On the path: Breeland Forest and Rivendell Waterfall, two forests among five sites.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);

		// The top two cards are shown to both players; the third is not.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight2, false));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands));
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(defiance, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight2, false));
		scn.DismissRevealedCards();

		// One [elven] card among them (the [gondor] Knight is none): one heal.  The wounded Elves, companions and the
		// ally, Galadriel herself among them; not the wounded Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(celeborn);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(knight));

		// The cards revealed stay where they were, in their order.
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneForestAndOneCardLeftInTheDrawDeckItRevealsThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOneForestScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var elf2 = scn.GetFreepsCard("elf2");
		scn.MoveCompanionsToTable(galadriel, arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(elf2);
		assertEquals(1, scn.GetFreepsDeckCount());

		// Rivendell Waterfall, the one forest: one card revealed, an [elven] companion; one heal, on the one wounded Elf.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf2, false));
		scn.DismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeForestsTheTopThreeCardsAreRevealedAndOneElfMayBeHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeForestScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		var elf2 = scn.GetFreepsCard("elf2");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(galadriel, arwen);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(celeborn, 1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(celeborn));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance, strands, elf2, servant);
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		// Hobbiton Woods, Breeland Forest and Rivendell Waterfall: the top three (CR 4.2's ruling on this card).
		scn.FreepsChooseToMove();
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(strands, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf2, false));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(servant));
		scn.DismissRevealedCards();

		// An event, a possession and a companion, all [elven]: three heals, Arwen twice (CR 4.2 "for each").
		scn.FreepsChooseCard(arwen);
		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, celeborn));
		scn.FreepsChooseCard(celeborn);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheDrawDeckAndTheWoundsAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(galadriel, arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance, strands);
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(defiance, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedElfItIsOfferedAndRevealsButHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(galadriel, arwen, knight);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance, strands);
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		// Two [elven] cards revealed, and only a Man to heal.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoForestOnTheAdventurePathItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoForestScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(galadriel, arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance, strands);
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		// A card from the top for each forest is none: nothing to reveal, nobody healed.
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(2, scn.GetWoundsOn(arwen));
	}

	@Test
	public void WithAnEmptyDrawDeckItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(galadriel, arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());

		// Two forests, a wounded Elf, and nothing to reveal (Q-0348).
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(2, scn.GetWoundsOn(arwen));
	}

	@Test
	public void WithTwoForestsAndOneCardInTheDrawDeckItIsOfferedAndRevealsThatCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		scn.MoveCompanionsToTable(galadriel, arwen);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetFreepsDrawDeck(defiance);
		assertEquals(1, scn.GetFreepsDeckCount());

		// Two forests and one card: the reveal is done only to count the [elven] cards, so it reveals what the deck
		// holds (Ketura's ruling, Q-0384: A, with Q-0378).  Defiance, an [elven] event: one heal, on the one
		// wounded Elf.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, false));
		scn.DismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnIsAnsweredAndTheFellowshipPhaseMoveIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		var servant = scn.GetFreepsCard("servant");
		scn.MoveCompanionsToTable(galadriel, arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(celeborn, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		scn.SetFreepsDrawDeck(defiance, strands, servant);
		assertEquals(defiance, scn.GetFreepsTopOfDeck());

		// The move at the end of the fellowship phase, to Breeland Forest: not answered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(2, scn.GetWoundsOn(arwen));

		// The first regroup move, to Rivendell Waterfall.  The next site is laid before "when the fellowship moves"
		// is answered (CR 4.2 "moving your fellowship"): Breeland Forest and the Waterfall, two forests.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		scn.FreepsChooseCard(arwen);
		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Aragorn, a ranger, at the Waterfall: the move limit is 3, and the second regroup move of the turn is
		// answered as well.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		// Arwen is unwounded now: the lone wounded Elf takes both heals unasked.
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
