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
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition, cost 1; its text is about archery
					put("keyward", "12_105");   // Troll's Keyward: [orc] Orc lurker. "Shadow: Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed."

					put("curse1", "1_36");      // Curse Their Foul Feet!: "Fellowship: Exert an Elf to reveal an opponent's hand. That player discards a card from hand for each Orc revealed."
					put("curse2", "1_36");
					put("galadriel", "6_18");   // Galadriel, Keeper of Nenya: Elf ally, vitality 3; heals herself at the start of each turn; a skirmish ability
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; her text is about skirmishing a Nazgul
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; his text is about his own strength
					put("inspiration", "11_36");// Inspiration: Skirmish event. "Spot a [gandalf] Wizard to reveal your hand and make a minion strength -3 for each companion in your hand ..."
				}},
				// Site 6 is Galadriel's Glade: "Sanctuary. Fellowship: Exert an Elf to look at an opponent's hand."
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
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(business, armory);

		scn.StartGame();
		// Something else makes every condition cost 1 more. A cost does not go below 0, so without it
		// a reduction of 3 and a reduction of 4 would read the same on this card.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.CONDITION, null, new ConstantEvaluator(1)));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 3 printed + 1 more, no [isengard] minion to spot.
		assertEquals(4, scn.GetTwilightCost(business));
		assertEquals(2, scn.GetTwilightCost(armory));
		scn.MoveMinionsToTable(servant);
		scn.ShadowRefreshDecision();
		// 3 printed + 1 more - 3.
		assertEquals(1, scn.GetTwilightCost(business));
		// The discount is this card's own: another condition in the same hand is not -3.
		assertEquals(2, scn.GetTwilightCost(armory));
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
		scn.FreepsRefreshDecision();

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
	public void TheFreePeoplesPlayerCannotLookAtTheShadowHandByASitesTextEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		// The card under test waits in the discard pile until it is cheated into play below.
		scn.MoveCardsToDiscard(business);
		assertInDiscard(business);
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(uruk, armory);
		var glade = scn.GetCurrentSite();

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(glade));

		// The bar is the player's, not his cards': without the card the site shows the whole hand.
		scn.FreepsUseCardAction(glade);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(business);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, business);

		// With it, Arwen, the only Elf, still exerts and nothing is shown.
		assertTrue(scn.FreepsActionAvailable(glade));
		scn.FreepsUseCardAction(glade);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var keyward = scn.GetShadowCard("keyward");
		var uruk = scn.GetShadowCard("uruk");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetShadowHand(uruk, armory);
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, business);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(keyward));
		int twilight = scn.GetTwilight();

		// The bar is on the Free Peoples player: a Shadow hand its own owner reveals is shown to him.
		scn.ShadowUseCardAction(keyward);
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertTrue(scn.FreepsHasCardChoiceAvailable(uruk, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();
		// No [orc] Troll among them, so nothing is added.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayStillRevealHisOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		var gandalf = scn.GetFreepsCard("gandalf");
		var arwen = scn.GetFreepsCard("arwen");
		var inspiration = scn.GetFreepsCard("inspiration");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(business);

		scn.StartGame();
		scn.SetFreepsHand(inspiration, arwen);
		scn.MoveMinionsToTable(uruk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, uruk);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, business);
		assertEquals(2, scn.GetFreepsHandCount());
		int strength = scn.GetStrength(uruk);

		// The bar is on a Shadow player's hand: the Free Peoples player's own, revealed by him, is shown to the Shadow player.
		scn.FreepsPlayCard(inspiration);
		assertTrue(scn.ShadowHasCardChoiceAvailable(arwen, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		// One companion in the hand revealed: the only minion is strength -3.
		assertEquals(strength - 3, scn.GetStrength(uruk));
		assertInHand(arwen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
