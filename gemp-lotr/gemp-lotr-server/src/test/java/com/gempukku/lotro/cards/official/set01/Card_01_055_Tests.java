package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mirror", "1_55");

					put("galadriel", "1_45");     // Galadriel, Lady of Light: Elf ally, home 6, strength 3, vitality 3; heals the allies of home 6 at the start of her owner's turn
					put("lady", "9_14");          // Galadriel, Bearer of Wisdom: a Galadriel who is a companion, vitality 3; her text is about resistance only
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally, home 6, strength 6, vitality 3; a fellowship ability only
					put("orophin", "1_56");       // Orophin, Lorien Bowman: Elf ally, home 6, strength 3; an archery ability only
					put("silinde", "1_60");       // Silinde, Elf of Mirkwood: Elf ally, home 3, strength 5; no text while his owner's site 3 is not on the path
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6
					put("aldor", "4_262");        // Aldor, Soldier of Edoras: a Man, ally of homes 3 and 6 of the Towers block, strength 3; an archery ability only

					put("runner", "1_178");       // Goblin Runner: a minion on the table, so that there is a maneuver phase
					// Eight cards of eight kinds for a hand: the game lists a hand's cards by blueprint.
					put("marksman", "1_176");     // Goblin Marksman
					put("savage", "1_151");       // Uruk Savage
					put("raiders", "1_158");      // Uruk-hai Raiding Party
					put("troop", "1_177");        // Goblin Patrol Troop
					put("scavengers", "1_179");   // Goblin Scavengers
					put("sneak", "1_181");        // Goblin Sneak
					put("wallcrawler", "1_184");  // Goblin Wallcrawler
					put("scimitar", "1_180");     // Goblin Scimitar
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
		 * 	Each Elf ally whose home is site 6 is strength +1.
		 * 	Maneuver: If an opponent has at least 7 cards in hand, exert Galadriel to look at
		 * 	2 of those cards at random. Discard one and replace the other.
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
		// ...and not the Elf ally of site 3, the Elf companion, or the ally of the Towers block's site 6, who
		// is a Man. (No Elf ally is printed with a home of 6 on another path.)
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
	public void ExertsGaladrielToLookAtTwoOfSevenCardsAndDiscardsTheOneChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.MoveCardsToSupportArea(mirror);

		// The Free Peoples player holds cards too, of other kinds than the Shadow player's seven.
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(arwen, silinde);

		var hand = List.of(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"),
				scn.GetShadowCard("troop"), scn.GetShadowCard("scavengers"), scn.GetShadowCard("sneak"),
				scn.GetShadowCard("wallcrawler"));
		scn.MoveCardsToHand(hand.toArray(new PhysicalCardImpl[0]));
		var inDeck = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		// The Free Peoples player is shown two cards. Which two is chance; whichever they are, both are from
		// the Shadow player's hand...
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		var seen = new ArrayList<PhysicalCardImpl>();
		var notSeen = new ArrayList<PhysicalCardImpl>();
		for (var card : hand) {
			if (scn.FreepsHasCardChoiceAvailable(card, false)) {
				seen.add(card);
			} else {
				notSeen.add(card);
			}
		}
		assertEquals(2, seen.size());
		assertEquals(5, notSeen.size());
		// ...and neither is from his draw deck or from the Free Peoples player's own hand.
		assertFalse(scn.FreepsHasCardChoiceAvailable(inDeck, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(arwen, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(silinde, false));
		// Nothing has left the hand yet, and its owner is shown nothing.
		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsDismissRevealedCards();
		// One of the two looked at, and exactly one: the five cards not looked at cannot be discarded.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(seen.get(0), seen.get(1)));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(notSeen.toArray(new PhysicalCardImpl[0])));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(seen.get(1));
		// The chosen card is in its owner's discard pile; the other is back in his hand with the five.
		assertInZone(Zone.DISCARD, seen.get(1));
		assertInZone(Zone.HAND, seen.get(0));
		assertInZone(Zone.HAND, notSeen.toArray(new PhysicalCardImpl[0]));
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, arwen, silinde);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTheOpponentHoldsSevenCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		// The Free Peoples player's own hand of seven is no opponent's hand.
		scn.MoveCardsToHand(scn.GetFreepsCard("marksman"), scn.GetFreepsCard("savage"), scn.GetFreepsCard("raiders"),
				scn.GetFreepsCard("troop"), scn.GetFreepsCard("scavengers"), scn.GetFreepsCard("sneak"),
				scn.GetFreepsCard("wallcrawler"));
		scn.MoveCardsToHand(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"),
				scn.GetShadowCard("troop"), scn.GetShadowCard("scavengers"), scn.GetShadowCard("sneak"));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		// Six cards in the opponent's hand, and Galadriel able to exert.
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertFalse(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A seventh card, and the ability is on offer.
		scn.MoveCardsToHand(scn.GetShadowCard("wallcrawler"));
		scn.FreepsRefreshDecision();
		assertEquals(7, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void GaladrielTheCompanionExertsForItAgainstAHandOfEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var lady = scn.GetFreepsCard("lady");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(lady);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(mirror);

		var hand = List.of(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"),
				scn.GetShadowCard("troop"), scn.GetShadowCard("scavengers"), scn.GetShadowCard("sneak"),
				scn.GetShadowCard("wallcrawler"), scn.GetShadowCard("scimitar"));
		scn.MoveCardsToHand(hand.toArray(new PhysicalCardImpl[0]));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(lady));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(lady));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		var seen = new ArrayList<PhysicalCardImpl>();
		for (var card : hand) {
			if (scn.FreepsHasCardChoiceAvailable(card, false)) {
				seen.add(card);
			}
		}
		assertEquals(2, seen.size());

		scn.FreepsDismissRevealedCards();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCardBPFromSelection(seen.get(0));
		assertInZone(Zone.DISCARD, seen.get(0));
		assertInZone(Zone.HAND, seen.get(1));
		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileGaladrielIsExhaustedWhoeverElseCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.MoveCardsToSupportArea(mirror);

		scn.MoveCardsToHand(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"),
				scn.GetShadowCard("troop"), scn.GetShadowCard("scavengers"), scn.GetShadowCard("sneak"),
				scn.GetShadowCard("wallcrawler"));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		// Her own text heals her at the start of the turn, so the wounds go on after it.
		scn.AddWoundsToChar(galadriel, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 3 with 2 wounds: Galadriel is exhausted, and Celeborn, an Elf ally of site 6 with no wound
		// on him, cannot exert in her place, though seven cards are there to look at.
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(7, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(mirror));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// One wound fewer and she can exert again: the ability is back, and it is she who pays.
		scn.RemoveWoundsFromChar(galadriel, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(mirror));

		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseAny();
		assertEquals(6, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WhenTheShadowHandMayNotBeLookedAtGaladrielExertsAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mirror = scn.GetFreepsCard("mirror");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(mirror);

		// No Business of Ours: "The Free Peoples player may not look at or reveal cards in any Shadow
		// player's hand."
		scn.MoveCardsToSupportArea(scn.GetShadowCard("nobusiness"));
		var hand = List.of(scn.GetShadowCard("marksman"), scn.GetShadowCard("savage"), scn.GetShadowCard("raiders"),
				scn.GetShadowCard("troop"), scn.GetShadowCard("scavengers"), scn.GetShadowCard("sneak"),
				scn.GetShadowCard("wallcrawler"));
		scn.MoveCardsToHand(hand.toArray(new PhysicalCardImpl[0]));

		scn.StartGame();
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(7, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		// The exertion is a cost and can be paid, so the ability is on offer.
		assertTrue(scn.FreepsActionAvailable(mirror));

		// The exertion is paid. Nothing can be looked at, and so nothing is discarded.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(7, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, hand.toArray(new PhysicalCardImpl[0]));
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
