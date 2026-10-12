package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_014_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior1", "65_14");
					put("warrior2", "65_14");

					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion of strength 6; an archery ability never used here
					// Possessions with no strength of their own.
					put("bow", "1_41");         // Elven Bow: [elven] ranged weapon, cost 1; bearer must be an Elf; bearer is an archer
					put("bow2", "1_41");
					put("cloak", "1_42");       // Elven Cloak: [elven] cloak, cost 1; to play, spot an Elf; bearer must be a companion; minion archery total -1
					put("poison", "8_28");      // Spider Poison: a Shadow possession a companion bears; bearer exerts when assigned to a skirmish
					// Cards an Elf may bear that are no possessions.
					put("aiglos", "9_12");      // Aiglos: [elven] artifact, vitality +1; bearer must be an Elf; a regroup ability never used here
					put("tale", "1_66");        // The Tale of Gil-galad: [elven] condition; bearer must be an Elf; a skirmish ability never used here
					// For the discard pile.
					put("defiance", "1_37");    // Defiance: an [elven] Skirmish event
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion with no text but a requirement to play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion with damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Elven Warrior
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant.</b>
		 * 	While this companion is bearing a possession, he is strength +2.
		 * 	<b>Maneuver:</b> Exert this companion to place an [elven] card from your discard pile on the bottom
		 * 	of your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior1");

		assertEquals("Elven Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedAndIsStrengthPlusTwoWhileHeBearsAPossessionHoweverManyHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		// A second Elven Warrior bearing nothing, and another Elf who already bears a possession.
		scn.MoveCompanionsToTable(warrior2, legolas);
		scn.AttachCardsTo(legolas, scn.GetFreepsCard("bow2"));
		scn.MoveCardsToHand(warrior1, bow, cloak);

		scn.StartGame();

		scn.FreepsPlayCard(warrior1);
		assertInZone(Zone.FREE_CHARACTERS, warrior1);

		assertEquals(5, scn.GetStrength(warrior1));
		assertEquals(5, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(legolas));

		// Legolas has a ranged weapon already, so the bow goes on one of the Warriors.
		scn.FreepsPlayCard(bow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior1, warrior2));
		scn.FreepsChooseCard(warrior1);

		assertAttachedTo(bow, warrior1);
		// 5 + 2; the bow adds no strength of its own
		assertEquals(7, scn.GetStrength(warrior1));
		assertEquals(5, scn.GetStrength(warrior2));
		assertEquals(6, scn.GetStrength(legolas));

		scn.FreepsPlayCard(cloak);
		scn.FreepsChooseCard(warrior1);

		assertAttachedTo(cloak, warrior1);
		// Two possessions are still +2, not +2 for each.
		assertEquals(7, scn.GetStrength(warrior1));
		assertEquals(5, scn.GetStrength(warrior2));

		// With his possessions gone he is as he was.
		scn.MoveCardsToDiscard(bow, cloak);
		assertEquals(5, scn.GetStrength(warrior1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactOrAConditionIsNoPossessionAndAShadowPossessionIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var aiglos = scn.GetFreepsCard("aiglos");
		var tale = scn.GetFreepsCard("tale");
		// The opponent's Spider Poison: a Shadow card, and a possession.
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(warrior1, warrior2);
		scn.AttachCardsTo(warrior1, aiglos);
		scn.AttachCardsTo(warrior2, tale);

		scn.StartGame();

		assertAttachedTo(aiglos, warrior1);
		assertAttachedTo(tale, warrior2);
		// An artifact alone, and a condition alone.
		assertEquals(5, scn.GetStrength(warrior1));
		assertEquals(5, scn.GetStrength(warrior2));

		scn.AttachCardsTo(warrior2, poison);

		assertAttachedTo(poison, warrior2);
		assertEquals(7, scn.GetStrength(warrior2));
		assertEquals(5, scn.GetStrength(warrior1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsThisWarriorToPlaceAnElvenCardFromHisOwnersDiscardPileBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		var defiance = scn.GetFreepsCard("defiance");
		var lorienElf = scn.GetFreepsCard("lorienelf");
		var cloak = scn.GetFreepsCard("cloak");
		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsSavage = scn.GetFreepsCard("savage");
		var bow = scn.GetFreepsCard("bow");
		var shadowBow = scn.GetShadowCard("bow");
		scn.MoveCompanionsToTable(warrior1, warrior2);
		// [elven] cards of three types, a [gondor] card and a Shadow card in the discard pile; an [elven] card
		// in hand and one in the other player's discard pile, neither of them a card that is in this pile.
		scn.MoveCardsToDiscard(defiance, lorienElf, cloak, aragorn, freepsSavage);
		scn.MoveCardsToDiscard(shadowBow);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		// Not a fellowship ability.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(warrior1));

		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(warrior1));
		assertTrue(scn.FreepsActionAvailable(warrior2));
		assertEquals(0, scn.GetWoundsOn(warrior1));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertNotSame(lorienElf, scn.GetFreepsBottomOfDeck());
		var deckCount = scn.GetFreepsDeckCount();
		var shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(warrior1);

		assertEquals(1, scn.GetWoundsOn(warrior1));
		assertEquals(0, scn.GetWoundsOn(warrior2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, lorienElf, cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, freepsSavage, bow));
		scn.FreepsChooseCards(lorienElf);

		assertInZone(Zone.DECK, lorienElf);
		assertSame(lorienElf, scn.GetFreepsBottomOfDeck());
		assertEquals(deckCount + 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, defiance, cloak, aragorn, freepsSavage, shadowBow);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, bow);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The other Warrior's ability exerts him and not the first, and its card goes beneath the first one.
		scn.ShadowPass();
		scn.FreepsUseCardAction(warrior2);

		assertEquals(1, scn.GetWoundsOn(warrior1));
		assertEquals(1, scn.GetWoundsOn(warrior2));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, cloak));
		scn.FreepsChooseCards(defiance);

		assertSame(defiance, scn.GetFreepsBottomOfDeck());
		assertSame(lorienElf, scn.GetFromBottomOfFreepsDeck(2));
		assertEquals(deckCount + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, cloak, aragorn, freepsSavage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedWarriorCannotUseTheAbilityWhileAnotherCopyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var warrior2 = scn.GetFreepsCard("warrior2");
		scn.MoveCompanionsToTable(warrior1, warrior2);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("defiance"), scn.GetFreepsCard("lorienelf"));
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(warrior1, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(warrior1));
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertFalse(scn.FreepsActionAvailable(warrior1));
		assertTrue(scn.FreepsActionAvailable(warrior2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoElvenCardInTheDiscardPileTheAbilityIsStillOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warrior1 = scn.GetFreepsCard("warrior1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsSavage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(warrior1);
		scn.MoveCardsToDiscard(aragorn, freepsSavage);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		scn.SkipToPhase(Phase.MANEUVER);

		// The ability has a cost, so it is offered whatever its effect will find.
		assertTrue(scn.FreepsActionAvailable(warrior1));
		assertEquals(0, scn.GetWoundsOn(warrior1));
		assertEquals(2, scn.GetFreepsDiscardCount());
		var deckCount = scn.GetFreepsDeckCount();
		var bottom = scn.GetFreepsBottomOfDeck();

		scn.FreepsUseCardAction(warrior1);

		assertEquals(1, scn.GetWoundsOn(warrior1));
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DISCARD, aragorn, freepsSavage);
		assertEquals(deckCount, scn.GetFreepsDeckCount());
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
