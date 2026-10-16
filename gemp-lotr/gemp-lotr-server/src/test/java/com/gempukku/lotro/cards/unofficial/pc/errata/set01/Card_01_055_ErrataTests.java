package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_055_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "51_55");

					put("galadriel", "1_45");     // Galadriel, Lady of Light: Elf ally, home 6, strength 3, vitality 3; heals the allies of home 6 at the start of her owner's turn
					put("lady", "9_14");          // Galadriel, Bearer of Wisdom: a Galadriel who is a companion, vitality 3; her text is about resistance only
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally, home 6, strength 6; a fellowship ability only
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Elf ally, home 6, strength 3
					put("silinde", "1_60");       // Silinde, Elf of Mirkwood: Elf ally, home 3, strength 5
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6
					put("aldor", "4_262");        // Aldor, Soldier of Edoras: a Man, ally of homes 3 and 6 of the Towers block, strength 3; an archery ability only

					put("runner", "1_178");       // Goblin Runner: a minion on the table, so that there is a maneuver phase
					put("marksman", "1_176");     // Goblin Marksman, Uruk Savage, Uruk-hai Raiding Party, Goblin Patrol Troop:
					put("savage", "1_151");       // minions that are only ever cards in a hand here
					put("raiders", "1_158");
					put("troop", "1_177");
					put("nobusiness", "2_44");    // No Business of Ours: Shadow condition; the Free Peoples player may not look at or reveal cards in any Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMirrorofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Mirror of Galadriel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each Elf ally whose home is site 6 is strength +1.
		 * <b>Maneuver:</b> Exert Galadriel to reveal 3 cards at random from an opponent's hand. Place one on
		 * top of that player's draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mirror");

		assertEquals("The Mirror of Galadriel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedToTheSupportAreaAndMakesEachElfAllyOfSite6StrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var orophin = scn.GetFreepsCard("orophin");
		var celeborn = scn.GetFreepsCard("celeborn");
		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var aldor = scn.GetFreepsCard("aldor");
		scn.MoveAlliesToTable(galadriel, orophin, silinde, aldor);
		scn.MoveCompanionsToTable(arwen);
		// No Elf ally is printed with a home of 6 on another path. Aldor, a Man, is the ally of another path's
		// site 6 there is.
		scn.MoveCardsToHand(mirror, celeborn);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(orophin));
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(3, scn.GetStrength(aldor));
		assertTrue(scn.FreepsPlayAvailable(mirror));

		scn.FreepsPlayCard(mirror);
		assertInZone(Zone.SUPPORT, mirror);
		assertNotAttachedTo(mirror, galadriel);

		// The two Elf allies of site 6...
		assertEquals(4, scn.GetStrength(galadriel));
		assertEquals(4, scn.GetStrength(orophin));
		// ...and not the Elf ally of site 3, the Elf companion, or the Man who is an ally of another path's site 6.
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(3, scn.GetStrength(aldor));

		// An Elf ally of site 6 who arrives later is strengthened too: Celeborn's printed 6 reads 7.
		assertEquals(6, celeborn.getBlueprint().getStrength());
		scn.FreepsPlayCard(celeborn);
		assertEquals(7, scn.GetStrength(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsGaladrielToRevealAHandOfThreeAndPutTheChosenCardOnTopOfItsOwnersDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.MoveCardsToSupportArea(mirror);

		// A hand of exactly three, so that which cards are revealed is not left to chance: the Shadow player's
		// own copy of a Free Peoples card, a minion and a condition.
		var silinde = scn.GetShadowCard("silinde");
		var marksman = scn.GetShadowCard("marksman");
		var nobusiness = scn.GetShadowCard("nobusiness");
		var inDeck = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(silinde, marksman, nobusiness);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(mirror));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, silinde, marksman, nobusiness);
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.MarkGameLog();
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		// The Free Peoples player is shown the three cards, which are still in the hand.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksman, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(nobusiness, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(inDeck, false));
		assertEquals(3, scn.GetShadowHandCount());
		// The owner of the hand is shown nothing: the game log is what tells him which cards were revealed.
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(silinde, marksman, nobusiness));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(inDeck));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(silinde, marksman, nobusiness));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(silinde);
		assertInZone(Zone.DECK, silinde);
		assertSame(silinde, scn.GetShadowTopOfDeck());
		assertInZone(Zone.HAND, marksman, nobusiness);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RevealsThreeOfFiveCardsAtRandomAndOnlyThoseThreeMayBePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		// The Free Peoples player holds cards too, of other kinds than the Shadow player's five.
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(arwen, silinde);

		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		var nobusiness = scn.GetShadowCard("nobusiness");
		scn.MoveCardsToHand(marksman, savage, raiders, troop, nobusiness);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// Five cards in the opponent's hand are enough: nothing asks for seven.
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(mirror);
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());

		// Which three is chance. Whichever they are, all three are from the Shadow player's hand...
		var shown = new ArrayList<PhysicalCardImpl>();
		var notShown = new ArrayList<PhysicalCardImpl>();
		for (var card : List.of(marksman, savage, raiders, troop, nobusiness)) {
			if (scn.FreepsHasCardChoiceAvailable(card, false)) {
				shown.add(card);
			} else {
				notShown.add(card);
			}
		}
		assertEquals(3, shown.size());
		assertEquals(2, notShown.size());
		// ...and none is from the Free Peoples player's own.
		assertFalse(scn.FreepsHasCardChoiceAvailable(arwen, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(silinde, false));
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(shown.get(0), shown.get(1), shown.get(2)));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(notShown.get(0)));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(notShown.get(1)));
		assertEquals(5, scn.GetShadowHandCount());

		scn.FreepsDismissRevealedCards();
		// One of the three revealed, and exactly one: the two cards that were not revealed cannot be placed.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(shown.get(0), shown.get(1), shown.get(2)));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(notShown.get(0), notShown.get(1)));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(shown.get(1));
		assertSame(shown.get(1), scn.GetShadowTopOfDeck());
		assertInZone(Zone.HAND, shown.get(0), shown.get(2), notShown.get(0), notShown.get(1));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, arwen, silinde);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AGaladrielWhoIsACompanionExertsAndNoOtherElfAllyOfSite6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var lady = scn.GetFreepsCard("lady");
		var celeborn = scn.GetFreepsCard("celeborn");
		// Galadriel is unique, so one Galadriel to a table: the companion here, as the ally in the other tests.
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCompanionsToTable(lady);
		scn.MoveCardsToSupportArea(mirror);

		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(marksman);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(lady));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(mirror));

		// She is the only one who can pay: nothing is asked, with an unwounded Elf ally of site 6 beside her.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(lady));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		// A hand of one card: it is revealed, and as the only card revealed it is the one placed.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(marksman, false));
		assertInZone(Zone.HAND, marksman);

		scn.FreepsDismissRevealedCards();
		assertSame(marksman, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck + 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsUsedAgainUntilGaladrielIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.MoveCardsToSupportArea(mirror);

		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(marksman, savage, raiders);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(marksman);
		assertSame(marksman, scn.GetShadowTopOfDeck());
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(mirror));

		// Two cards are left in the hand: both are revealed, and one of the two is placed above the first.
		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(raiders, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(marksman, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(raiders);
		assertSame(raiders, scn.GetShadowTopOfDeck());
		assertSame(marksman, scn.GetFromTopOfShadowDeck(2));
		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPass();
		// Vitality 3 with 2 wounds: Galadriel is exhausted, and Celeborn, an Elf ally of site 6 with no wound
		// on him, cannot exert in her place, though a card is still there to reveal.
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsOfferedInTheManeuverPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveCardsToHand(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilGaladrielIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveCardsToHand(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// Celeborn, an Elf ally of site 6 able to exert, is not Galadriel.
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.FreepsActionAvailable(mirror));

		scn.MoveAlliesToTable(galadriel);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandGaladrielExertsAndNothingIsRevealedOrPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		int shadowDeck = scn.GetShadowDeckCount();
		// The exertion is a cost and can be paid, so the ability is on offer with nothing to reveal.
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheShadowHandMayNotBeRevealedNoCardIsRevealedAndSoNoneIsPlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		// No Business of Ours: "The Free Peoples player may not look at or reveal cards in any Shadow
		// player's hand."
		scn.MoveCardsToSupportArea(scn.GetShadowCard("nobusiness"));
		var marksman = scn.GetShadowCard("marksman");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(marksman, savage, raiders, troop);

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsActionAvailable(mirror));

		// The exertion is paid. Nothing can be revealed, and the card to place is one of those revealed.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(4, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, marksman, savage, raiders, troop);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
