package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wildmen", "5_4");
					put("wildmen2", "5_4");

					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text, cost 2, strength 8, site number 3
					put("brigand2", "4_10");
					// Wild Man of Dunland: [dunland] Man, strength 9.  "Each time this minion wins a skirmish, you may
					// play a [dunland] Man from hand; his twilight cost is -2. That Man is fierce and damage +1 until
					// the regroup phase."  A legal play of a minion in the skirmish phase.
					put("wildman", "4_38");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a companion, to fight or to hold in hand
					put("armor", "1_8");        // Dwarven Armor: a possession, to hold in hand
					put("stout", "1_315");      // Stout and Sturdy: a Free Peoples event, to hold in hand
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion, to hold in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WildMenoftheHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Wild Men of the Hills
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 18
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: The twilight cost of this minion is -2 during the skirmish phase.<br>When you play this minion, the Free Peoples player may discard 4 cards from hand to discard it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wildmen");

		assertEquals("Wild Men of the Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(18, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsTwoLessInHandDuringTheSkirmishPhaseAndNotInTheAssignmentOrRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var wildmen2 = scn.GetShadowCard("wildmen2");
		var brigand = scn.GetShadowCard("brigand");
		var inHand = scn.GetShadowCard("brigand2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Two Wild Men, each with its own -2, and another [dunland] Man of cost 2 beside them.
		scn.MoveCardsToHand(wildmen, wildmen2, inHand);

		scn.StartGame();
		assertInHand(wildmen, wildmen2, inHand);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		assertEquals(6, scn.GetTwilightCost(wildmen));
		assertEquals(6, scn.GetTwilightCost(wildmen2));
		assertEquals(2, scn.GetTwilightCost(inHand));

		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 6 - 2 for each; not 6 - 4 for two copies, and the Brigand keeps his 2.
		assertEquals(4, scn.GetTwilightCost(wildmen));
		assertEquals(4, scn.GetTwilightCost(wildmen2));
		assertEquals(2, scn.GetTwilightCost(inHand));

		// The Brigand beats Aragorn on the tie, 8 to 8.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetTwilightCost(wildmen));
		assertEquals(6, scn.GetTwilightCost(wildmen2));
		assertEquals(2, scn.GetTwilightCost(inHand));
	}

	@Test
	public void PlayedInTheSkirmishPhaseByAWildManOfDunlandItCostsTwoLessBesidesHisDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var wildman = scn.GetShadowCard("wildman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(wildman);
		scn.SetShadowHand(wildmen);
		// Nothing in the Free Peoples hand, so the Wild Men's own "when you play" asks him nothing.
		scn.SetFreepsHand();
		assertInHand(wildmen);
		assertEquals(0, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		// Balin's Tomb, site 4: no minion here is roaming.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, wildman);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// The Wild Man beats Aragorn, 9 to 8, and plays the Wild Men from hand.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wildman));
		scn.ShadowChooseOptionalTrigger(wildman);

		assertInZone(Zone.SHADOW_CHARACTERS, wildmen);
		// 4 - (6 to play - 2 during the skirmish phase - 2 for the Wild Man)
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 4 cards from hand"));
		assertTrue(scn.InFierceRound());
	}

	@Test
	public void WildMenoftheHillsDoesNotOfferChoiceWhenNotEnoughCardsInFreepsHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(wildmen);
		// Three cards in hand: one short of the four the Free Peoples player would have to discard.
		scn.MoveCardsToHand(aragorn, armor, brigand);

		scn.StartGame();
		assertInHand(aragorn, armor, brigand);
		// 5 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertEquals(8, scn.GetTwilight());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(wildmen));

		// 6 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(wildmen);
		assertInZone(Zone.SHADOW_CHARACTERS, wildmen);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 4 cards from hand"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, brigand);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardsFourCardsOfHisChoiceFromHandAndTheWildMenJustPlayedIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("wildmen");
		var played = scn.GetShadowCard("wildmen2");
		// Two companions, a possession, an event and a Shadow minion of his own: any card in his hand will do.
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(played);
		scn.MoveCardsToHand(aragorn, legolas, armor, stout, brigand);

		scn.StartGame();
		assertInHand(aragorn, legolas, armor, stout, brigand);
		// 5 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.MoveMinionsToTable(inPlay);
		scn.ShadowRefreshDecision();
		assertEquals(8, scn.GetTwilight());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// 6 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(played);
		assertInZone(Zone.SHADOW_CHARACTERS, played);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 4 cards from hand to discard Wild Men of the Hills?"));

		scn.FreepsChooseYes();
		// Exactly four, of the five he holds.
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, legolas, armor, stout, brigand));

		scn.FreepsChooseCards(aragorn, armor, stout, brigand);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(legolas);
		assertInDiscard(aragorn, armor, stout, brigand);
		// "It": the Wild Men that was played, and not the one already on the table.
		assertInZone(Zone.DISCARD, played);
		assertInZone(Zone.SHADOW_CHARACTERS, inPlay);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDeclineAndTheWildMenStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(wildmen);
		scn.MoveCardsToHand(aragorn, legolas, armor, stout, brigand);

		scn.StartGame();
		assertInHand(aragorn, legolas, armor, stout, brigand);
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(wildmen);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to discard 4 cards from hand to discard Wild Men of the Hills?"));

		scn.FreepsChooseNo();
		assertEquals(5, scn.GetFreepsHandCount());
		assertInHand(aragorn, legolas, armor, stout, brigand);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.SHADOW_CHARACTERS, wildmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMinionPlayedBesideAWildMenAsksTheFreePeoplesPlayerNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wildmen = scn.GetShadowCard("wildmen");
		var played = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var brigand = scn.GetFreepsCard("brigand");
		scn.MoveCardsToHand(played);
		// Five cards: he could pay, were he asked.
		scn.MoveCardsToHand(aragorn, legolas, armor, stout, brigand);

		scn.StartGame();
		assertInHand(aragorn, legolas, armor, stout, brigand);
		// 1 set + 1 companion + 2 for Ettenmoors
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wildmen);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetTwilight());

		// 2 to play + 2 roaming (site number 3, at site 2)
		scn.ShadowPlayCard(played);
		assertInZone(Zone.SHADOW_CHARACTERS, played, wildmen);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to discard 4 cards from hand"));
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
