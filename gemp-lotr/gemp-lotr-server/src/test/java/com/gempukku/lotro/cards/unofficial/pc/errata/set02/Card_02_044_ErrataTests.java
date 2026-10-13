package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_044_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("business", "52_44");
					put("business2", "52_44");

					put("uruk", "1_151");       // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc; a regroup ability only used on purpose
					put("whu", "18_131");       // White Hand Uruk: [uruk-hai] Uruk-hai; its play requirement does not matter when cheated in
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text, to fill the Shadow player's hand
					put("troop2", "1_177");

					put("mirror", "1_55");      // The Mirror of Galadriel: "Maneuver: If an opponent has at least 7 cards in hand, exert Galadriel to look at 2 of those cards at random. Discard one ..."
					put("curse1", "1_36");      // Curse Their Foul Feet!: "Fellowship: Exert an Elf to reveal an opponent's hand. That player discards a card from hand for each Orc revealed."
					put("curse2", "1_36");
					put("galadriel", "6_18");   // Galadriel, Keeper of Nenya: Elf ally, vitality 3; heals herself at the start of each turn; a skirmish ability

					put("advances", "53_87");   // The Dark Lord Advances: Shadow regroup event; exert a [sauron] minion to reveal a card at random from the Free Peoples player's hand
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoBusinessofOursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: No Business of Ours
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot an [isengard] minion, this card is twilight cost -3.
		 * 	The Free Peoples player may not look at or reveal cards in any Shadow player's hand.
		 */

		var scn = GetScenario();

		var business = scn.GetFreepsCard("business");

		assertEquals("No Business of Ours", business.getBlueprint().getTitle());
		assertNull(business.getBlueprint().getSubtitle());
		assertFalse(business.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, business.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, business.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, business.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(business, Keyword.SUPPORT_AREA));
		assertEquals(3, business.getBlueprint().getTwilightCost());
	}

	@Test
	public void CostsThreeToPlayToTheSupportAreaWithoutAnIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var business2 = scn.GetShadowCard("business2");
		var whu = scn.GetShadowCard("whu");
		scn.MoveCardsToHand(business);

		scn.StartGame();
		// An [uruk-hai] Uruk-hai and an [isengard] card that is not a minion: neither is an [isengard] minion.
		scn.MoveMinionsToTable(whu);
		scn.MoveCardsToSupportArea(business2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(business));
		assertTrue(scn.ShadowPlayAvailable(business));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(business);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, business);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsNothingOnceAnIsengardUrukhaiCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(business, uruk);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilightCost(business));
		scn.ShadowPlayCard(uruk);
		assertEquals(0, scn.GetTwilightCost(business));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(business);
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, business);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcAlsoMakesItCostNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(business);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetTwilightCost(business));
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(business);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheReductionIsExactlyThreeWhateverElseChangesTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(business);

		scn.StartGame();
		// Something else makes every condition cost 1 more. A cost does not go below 0, so without it
		// a reduction of 3 and a reduction of 4 would read the same on this card.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.CONDITION, null, new ConstantEvaluator(1)));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 3 printed + 1 more, no [isengard] minion to spot.
		assertEquals(4, scn.GetTwilightCost(business));
		scn.MoveMinionsToTable(servant);
		// 3 printed + 1 more - 3.
		assertEquals(1, scn.GetTwilightCost(business));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(business);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, business);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerCannotRevealTheShadowPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var galadriel = scn.GetFreepsCard("galadriel");
		var curse1 = scn.GetFreepsCard("curse1");
		var curse2 = scn.GetFreepsCard("curse2");
		scn.MoveCardsToHand(curse1, curse2);
		scn.MoveAlliesToTable(galadriel);
		// No Orc in the Shadow player's hand, so the curse makes nobody discard.
		scn.MoveCardsToHand(business, uruk);

		scn.StartGame();

		// Without the card, the curse reveals the hand.
		scn.FreepsPlayCard(curse1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);

		// With it, the curse is still played and its cost paid, and nothing is shown.
		assertTrue(scn.FreepsPlayAvailable(curse2));
		scn.FreepsPlayCard(curse2);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertInDiscard(curse2);
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerCannotLookAtCardsInTheShadowPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");
		scn.MoveCardsToSupportArea(mirror);
		scn.MoveAlliesToTable(galadriel);
		// The card under test waits in the discard pile, where the Mirror's random look and discard cannot reach
		// it (F-0264); it is put into play from there below.
		scn.MoveCardsToDiscard(business);
		// Eight cards in hand, so that after one is discarded the Mirror's 7 are still there.
		scn.MoveCardsToHand(scn.GetShadowCard("business2"), scn.GetShadowCard("servant"), scn.GetShadowCard("whu"),
				scn.GetShadowCard("troop1"), scn.GetShadowCard("troop2"), scn.GetShadowCard("mirror"),
				scn.GetShadowCard("curse1"), scn.GetShadowCard("curse2"));

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(8, scn.GetShadowHandCount());
		assertInDiscard(business);

		// Without the card, the Mirror looks at 2 cards and discards one.
		scn.FreepsUseCardAction(mirror);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseAny();
		assertEquals(7, scn.GetShadowHandCount());
		scn.ShadowPass();

		scn.MoveCardsToSupportArea(business);
		assertInZone(Zone.SUPPORT, business);

		// With it, the Mirror is still used and its cost paid, and nothing is looked at or discarded.
		assertEquals(7, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(mirror));
		scn.FreepsUseCardAction(mirror);
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertFalse(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertEquals(7, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillRevealTheFreePeoplesPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var advances = scn.GetShadowCard("advances");
		var sentry = scn.GetShadowCard("sentry");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(advances);
		scn.MoveCardsToHand(galadriel);
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The restriction is the Free Peoples player's: the Shadow player is shown the one card in that hand.
		scn.ShadowPlayCard(advances);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		scn.ShadowDeclineChoosing();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
