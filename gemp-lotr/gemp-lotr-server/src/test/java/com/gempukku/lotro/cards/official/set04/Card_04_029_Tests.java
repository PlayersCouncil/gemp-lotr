package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("refuge", "4_29");
					// Dunlending Looter: [dunland] Man, strength 9, vitality 1.  "When this minion wins a skirmish, you
					// may stack him on a site you control.  Shadow: If stacked on a site you control, play this minion.
					// His twilight cost is -2."
					put("looter", "4_11");
					// Hillman Rabble: [dunland] Man, strength 10; the same two abilities as the Looter.
					put("rabble", "4_24");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
					// The Mouth of Sauron, Lieutenant of Barad-dûr: a [sauron] Man.  Their Marching Companies 8_107
					// ("Regroup: Remove a threat to stack your [sauron] minion on a site you control") puts him there.
					put("mouth", "12_118");
					// Pass of the Spider: a [gollum] possession in the support area; its skirmish ability stacks a
					// minion from play on it, which is how a [dunland] Man comes to be stacked on a card that is no site.
					put("pass", "103_12");

					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoRefugeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: No Refuge
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>While a [dunland] Man is stacked on a site, the Shadow number of each site is +5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("refuge");

		assertEquals("No Refuge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ALooterStackedOnASiteByHisOwnWinMakesEachShadowNumberPlus5UntilHeIsPlayedFromThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var refuge = scn.GetShadowCard("refuge");
		var looter = scn.GetShadowCard("looter");

		scn.StartGame();
		// Player 1's second turn: both markers are off site 1, which the Shadow player controls.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(refuge);
		scn.SetTwilight(0);

		// Nothing is stacked: Bag End 0, Ettenmoors 2.
		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));

		scn.FreepsPass();
		// 2 companions + 0 for Rivendell Valley
		assertEquals(2, scn.GetTwilight());
		var site3 = scn.GetSite(3);

		// A [dunland] Man in play and not stacked.
		scn.MoveMinionsToTable(looter);
		assertEquals(2, scn.GetTwilightCost(site2));
		assertEquals(0, scn.GetTwilightCost(site3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, looter);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// 9 against 6: the Looter wins, and his owner stacks him on the one site he controls.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.STACKED, looter);
		assertEquals(1, scn.GetStackedCards(site1).size());

		assertEquals(7, scn.GetTwilightCost(site2));
		assertEquals(5, scn.GetTwilightCost(site3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// 2 before + 3 + 5 for Balin's Tomb + 2 companions
		assertEquals(4, scn.GetCurrentSiteNumber());
		var site4 = scn.GetSite(4);
		assertEquals(8, scn.GetTwilightCost(site4));
		assertEquals(12, scn.GetTwilight());

		// The Looter's own Shadow ability plays him from the site, and nothing is stacked there any more.
		assertTrue(scn.ShadowActionAvailable(looter));
		scn.ShadowUseCardAction(looter);
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(2, scn.GetTwilightCost(site2));
		assertEquals(0, scn.GetTwilightCost(site3));
		assertEquals(3, scn.GetTwilightCost(site4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHillmanRabbleStackedOnASiteMakesEachShadowNumberPlus5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var refuge = scn.GetShadowCard("refuge");
		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(refuge);

		// Bag End 0, under the Shadow player's control; Ettenmoors 2, where the fellowship is.
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));

		scn.StackCardsOn(site1, rabble);
		assertInZone(Zone.STACKED, rabble);
		assertEquals(5, scn.GetTwilightCost(site1));
		assertEquals(7, scn.GetTwilightCost(site2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOtherPlayersDunlendingLooterStackedOnASiteHeControlsMakesEachShadowNumberPlus5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The Free Peoples player's own Looter, stacked as the Shadow player of an earlier turn on a site he controls.
		var looter = scn.GetFreepsCard("looter");
		var refuge = scn.GetShadowCard("refuge");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		scn.MoveCardsToSupportArea(refuge);

		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));

		scn.StackCardsOn(site1, looter);
		assertInZone(Zone.STACKED, looter);
		assertEquals(5, scn.GetTwilightCost(site1));
		assertEquals(7, scn.GetTwilightCost(site2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AManOfAnotherCultureOnASiteAndADunlandManStackedOnAPossessionChangeNoShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var refuge = scn.GetShadowCard("refuge");
		var mouth = scn.GetShadowCard("mouth");
		var pass = scn.GetShadowCard("pass");
		var brigand = scn.GetShadowCard("brigand");
		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToSupportArea(refuge, pass);

		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));

		// A [sauron] Man on the site the Shadow player controls, and a [dunland] Man on a Shadow card that is no site.
		scn.StackCardsOn(site1, mouth);
		scn.StackCardsOn(pass, brigand);
		assertInZone(Zone.STACKED, mouth, brigand);
		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));

		// A [dunland] Man put on the same site beside the Mouth of Sauron is what it takes.
		scn.StackCardsOn(site1, rabble);
		assertEquals(5, scn.GetTwilightCost(site1));
		assertEquals(7, scn.GetTwilightCost(site2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
