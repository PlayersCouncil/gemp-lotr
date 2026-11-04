package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("murderer", "13_98");
					// Token holders of three cultures on both sides, each a card that gives itself tokens in play:
					// A Path Appointed ([gondor] Free Peoples condition) and Heirs of Gondor ([gondor], one token as
					// it is played); Ships of Great Draught ([raider] Shadow possession) and Fleet of Corsair Ships
					// ([men] Shadow possession). None of their abilities is used.
					put("path", "8_42");
					put("heirs", "13_69");
					put("ships", "8_65");
					put("fleet", "18_66");
					// Minions with no text that reaches a test
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7; makes the current site plains
					put("throng", "11_102");    // Throng of Harad: [men] Man, vitality 3; fierce only against resistance 5 or less
					put("legion", "11_88");     // Legion of Harad: [men] Man, vitality 3; Toil 2 is its whole text
					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("sword", "1_112");      // Ranger's Sword: strength +2 for Aragorn
					put("defend", "4_115");     // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					put("fell", "4_97");        // Long I Fell: Skirmish: the Shadow player may make you wound a minion of your choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronMurdererStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Southron Murderer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.
		 * 	While you can spot 4 or more culture tokens, this minion cannot take wounds except during skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("murderer");

		assertEquals("Southron Murderer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAFourthCultureTokenPlayedItCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var path = scn.GetFreepsCard("path");
		var heirs = scn.GetFreepsCard("heirs");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(heirs, defend);

		var murderer = scn.GetShadowCard("murderer");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var throng = scn.GetShadowCard("throng");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToSupportArea(ships, fleet);

		scn.StartGame();
		// Three tokens of three cultures, on both players' cards.
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(fleet, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		scn.MoveMinionsToTable(murderer, throng, legion);
		scn.FreepsRefreshDecision();

		// The fourth, by legal play.
		scn.FreepsPlayCard(heirs);
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(murderer));
		assertEquals(0, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(defend);
		// The other two minions may be wounded; this one may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(throng, legion));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(murderer));
		scn.FreepsChooseCard(throng);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(murderer));
		assertEquals(0, scn.GetWoundsOn(legion));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithThreeCultureTokensItCanBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var path = scn.GetFreepsCard("path");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(defend);

		var murderer = scn.GetShadowCard("murderer");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var throng = scn.GetShadowCard("throng");
		scn.MoveCardsToSupportArea(ships, fleet);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(fleet, 1);
		// Burdens and a threat are tokens too, but no culture tokens.
		scn.AddBurdens(2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(murderer, throng);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(murderer));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(murderer, throng));
		scn.FreepsChooseCard(murderer);
		// Vitality 1: the wound kills it.
		assertInDiscard(murderer);
		assertEquals(0, scn.GetWoundsOn(throng));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithFourCultureTokensItTakesTheWoundOfASkirmishItLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var path = scn.GetFreepsCard("path");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(path, heirs);

		var murderer = scn.GetShadowCard("murderer");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToSupportArea(ships, fleet);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(fleet, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertAttachedTo(sword, aragorn);
		scn.MoveMinionsToTable(footman);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// An archer: laid after the archery phase, so that its archery wound is not in the way.
		scn.MoveMinionsToTable(murderer);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, murderer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(murderer));

		// Aragorn, 10 with his sword, beats the Murderer, 8.
		scn.PassSkirmishActions();
		assertInDiscard(murderer);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCultureTokensItTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var path = scn.GetFreepsCard("path");
		var heirs = scn.GetFreepsCard("heirs");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(path, heirs);
		scn.MoveCardsToHand(fell);

		var murderer = scn.GetShadowCard("murderer");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");
		scn.MoveCardsToSupportArea(ships, fleet);

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(fleet, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		scn.MoveMinionsToTable(footman, throng);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(murderer);
		scn.PassAssignmentActions();
		// Aragorn skirmishes the Footman; the Murderer is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(murderer));
		assertEquals(0, scn.GetWoundsOn(throng));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound is placed during a skirmish, so the Murderer is a candidate beside the other minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(murderer, throng, footman));
		scn.FreepsChooseCard(murderer);
		assertInDiscard(murderer);
		assertEquals(0, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
