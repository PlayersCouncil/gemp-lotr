package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_029_Tests
{

	// The Fellowship path with Pass of Caradhras 1_348 as site 4, a mountain.  Site 2 is Ettenmoors 1_331 (plains),
	// site 5 The Bridge of Khazad-dum 1_349 (underground), site 7 Anduin Confluence 1_353 (river).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slippery", "9_29");
					put("slippery2", "9_29");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability used only on purpose
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: a [wraith] Gollum, strength 5; his abilities are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, strength 8
					put("smeagol", "6_45");     // Smeagol, Poor Creature: a [gollum] companion who is no Gollum; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] minion; its text is about being played
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples card
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlipperyasFishesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Slippery as Fishes
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While Gollum is at a mountain, river, or underground site, he is strength +2.
		 * 	<b>Regroup:</b> Spot Gollum to reveal your hand. Place all Shadow cards revealed beneath your draw deck. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("slippery");

		assertEquals("Slippery as Fishes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAMountainGollumIsStrengthPlus2AndAnotherGollumMinionIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(gollum, shelob);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));

		scn.MoveCardsToSupportArea(slippery);

		assertInZone(Zone.SUPPORT, slippery);
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
	}

	@Test
	public void AtAnUndergroundSiteAGollumOfAnotherCultureIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var wraith = scn.GetShadowCard("wraith");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(wraith);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertEquals(5, scn.GetStrength(wraith));

		scn.MoveCardsToSupportArea(slippery);

		assertInZone(Zone.SUPPORT, slippery);
		assertEquals(7, scn.GetStrength(wraith));
	}

	@Test
	public void AtARiverGollumIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(gollum);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.RIVER));
		assertEquals(5, scn.GetStrength(gollum));

		scn.MoveCardsToSupportArea(slippery);

		assertInZone(Zone.SUPPORT, slippery);
		assertEquals(7, scn.GetStrength(gollum));
	}

	@Test
	public void AtAPlainsSiteGollumKeepsHisPrintedStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToSupportArea(slippery);

		// Ettenmoors is plains: no mountain, river or underground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertInZone(Zone.SUPPORT, slippery);
		assertEquals(5, scn.GetStrength(gollum));
	}

	@Test
	public void RegroupAbilityRevealsTheHandPutsItsShadowCardsBeneathTheDeckAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var slippery2 = scn.GetShadowCard("slippery2");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var fear = scn.GetShadowCard("fear");
		var aragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToSupportArea(slippery, slippery2);
		// A minion, a condition of another culture, and a Free Peoples card.
		scn.MoveCardsToHand(runner, fear, aragorn);

		assertInZone(Zone.SUPPORT, slippery, slippery2);
		assertInHand(runner, fear, aragorn);
		assertEquals(3, scn.GetShadowHandCount());
		var deckSize = scn.GetShadowDeckCount();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slippery));
		scn.ShadowUseCardAction(slippery);

		// The hand is shown to the other player, not to its owner.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The two Shadow cards are placed one at a time; the Free Peoples card is not among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, fear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(runner);

		assertEquals(fear, scn.GetShadowBottomOfDeck());
		assertEquals(runner, scn.GetFromBottomOfShadowDeck(2));
		assertEquals(deckSize + 2, scn.GetShadowDeckCount());
		assertInHand(aragorn);
		assertEquals(1, scn.GetShadowHandCount());

		// This copy is discarded, and not the other.
		assertInDiscard(slippery);
		assertInZone(Zone.SUPPORT, slippery2);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsOfferedWithAGollumOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var wraith = scn.GetShadowCard("wraith");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(wraith);
		scn.MoveCardsToSupportArea(slippery);

		assertInZone(Zone.SUPPORT, slippery);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slippery));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithoutGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slippery = scn.GetShadowCard("slippery");
		var shelob = scn.GetShadowCard("shelob");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A [gollum] minion who is no Gollum, and the other player's Smeagol.
		scn.MoveMinionsToTable(shelob);
		scn.MoveCardsToSupportArea(slippery);
		scn.MoveCardsToHand(runner);

		assertInZone(Zone.SUPPORT, slippery);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInHand(runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slippery));
		assertInZone(Zone.SUPPORT, slippery);
		assertInHand(runner);
	}
}
