package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pavise", "11_94");
					put("pavise2", "11_94");

					// Minions
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; makes the current site plains
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities only used on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					put("throng", "11_102");    // Throng of Harad: [men] Man, vitality 3; fierce only when assigned
					put("legion", "11_88");     // Legion of Harad: [men] Man, vitality 3; Toil 2 is its whole text

					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession borne by Aragorn
					put("leaf", "1_300");       // Longbottom Leaf: [shire] possession that plays to the support area
					put("barahir", "6_55");     // Ring of Barahir: [gondor] ARTIFACT borne by Aragorn
					put("lordofmoria", "1_21"); // Lord of Moria: [dwarven] CONDITION in the support area
					put("defend", "4_115");     // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					put("fell", "4_97");        // Long I Fell: Skirmish: the Shadow player may make you wound a minion of your choice

					// A Shadow possession
					put("poison", "8_28");      // Spider Poison: [gollum] possession in its owner's support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PaviseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pavise
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Shield
		 * Vitality: +2
		 * Game Text: Bearer must be a [men] minion.
		 * 	The twilight cost of this possession is -1 for each Free Peoples possession you spot.
		 * 	Bearer cannot take wounds except during a skirmish.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pavise");

		assertEquals("Pavise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnlyOnAMenMinionForOneLessForEachFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(leaf);

		var pavise = scn.GetShadowCard("pavise");
		var footman = scn.GetShadowCard("footman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(pavise);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(footman, ted, brigand);
		scn.ShadowRefreshDecision();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(pavise));
		scn.ShadowPlayCard(pavise);
		// [men] minions of two races are offered; a Man of another culture is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertAttachedTo(pavise, ted);
		// 3 printed - 1 for the sword - 1 for the pipeweed.
		assertEquals(pool - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostIsOneLessForEachFreePeoplesPossessionAndForNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var leaf = scn.GetFreepsCard("leaf");
		var barahir = scn.GetFreepsCard("barahir");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.MoveCardsToHand(leaf);

		var pavise = scn.GetShadowCard("pavise");
		var pavise2 = scn.GetShadowCard("pavise2");
		var footman = scn.GetShadowCard("footman");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToHand(pavise);
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		// The Ring of Barahir has a start-of-turn trigger: it goes on after the turn has begun.
		scn.AttachCardsTo(aragorn, barahir);
		// A second Pavise already in play lowers the cost of none but itself.
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, pavise2);
		scn.FreepsRefreshDecision();

		// A Free Peoples artifact, a Free Peoples condition and two Shadow possessions: none is counted.
		assertEquals(3, scn.GetTwilightCost(pavise));

		// A Free Peoples possession played to the support area.
		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertEquals(2, scn.GetTwilightCost(pavise));

		// A second, of another culture, borne by a companion.
		scn.AttachCardsTo(aragorn, sword);
		assertEquals(1, scn.GetTwilightCost(pavise));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var pavise = scn.GetShadowCard("pavise");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, throng, legion);
		scn.AttachCardsTo(footman, pavise);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(legion));

		scn.FreepsPlayCard(defend);
		// The other two [men] minions may be wounded; the bearer may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(throng, legion));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(footman));
		scn.FreepsChooseCard(throng);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void BearerTakesTheWoundOfASkirmishItLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pavise = scn.GetShadowCard("pavise");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, pavise);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(footman));

		// Aragorn, 8, beats the Footman, 7.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerNotInTheSkirmishTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		var pavise = scn.GetShadowCard("pavise");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.MoveMinionsToTable(footman, throng, legion);
		scn.AttachCardsTo(throng, pavise);
		scn.SkipToAssignments();
		// Aragorn skirmishes the Footman; the bearer, Throng of Harad, is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(legion));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound is placed during a skirmish, so the bearer is a candidate beside the other minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(throng, legion, footman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(throng);

		assertEquals(1, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
