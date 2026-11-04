package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_160_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "13_160");
					// Free Peoples conditions that give themselves tokens of their culture
					put("heirs", "13_69");      // Heirs of Gondor: [gondor], one token as it is played; its skirmish ability is never used
					put("ale", "18_108");       // Golden Perch Ale: [shire] support-area POSSESSION, a token for each [shire] companion's win (never reached here)
					// Shadow cards that hold tokens of their culture
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession; its abilities are never used
					put("threatening", "13_103"); // Always Threatening: [orc] support-area condition; its skirmish ability is never used
					// Minions with no text that reaches a test
					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7; makes the current site plains
					put("throng", "11_102");    // Throng of Harad: [men] Man, vitality 3; fierce only against resistance 5 or less
					// Free Peoples cards
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8
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
	public void CavernDenizenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Cavern Denizen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	While you can spot a Free Peoples culture token, this minion cannot take wounds except during skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denizen");

		assertEquals("Cavern Denizen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAGondorTokenPlayedItCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var heirs = scn.GetFreepsCard("heirs");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(heirs, defend);

		var denizen = scn.GetShadowCard("denizen");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, footman, throng);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.HAND, heirs);

		// The Free Peoples culture token, by legal play.
		scn.FreepsPlayCard(heirs);
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(throng));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(defend);
		// The other two minions may be wounded; this one may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(footman, throng));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denizen));
		scn.FreepsChooseCard(throng);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyShireTokensItCannotBeWoundedInTheManeuverPhaseEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ale = scn.GetFreepsCard("ale");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ale);
		scn.MoveCardsToHand(defend);

		var denizen = scn.GetShadowCard("denizen");
		var throng = scn.GetShadowCard("throng");
		var footman = scn.GetShadowCard("footman");

		scn.StartGame();
		// A [shire] token on a possession, not a condition.
		scn.AddTokensToCard(ale, 1);
		assertEquals(1, scn.GetCultureTokensOn(ale));
		scn.MoveMinionsToTable(denizen, throng, footman);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(throng, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denizen));
		scn.FreepsChooseCard(throng);
		assertEquals(1, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyShadowCultureTokensItCanBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var denizen = scn.GetShadowCard("denizen");
		var throng = scn.GetShadowCard("throng");
		var ships = scn.GetShadowCard("ships");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(ships, threatening);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(threatening, 2);
		// Burdens are tokens too, but no culture tokens.
		scn.AddBurdens(2);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen, throng);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(denizen, throng));
		scn.FreepsChooseCard(denizen);
		// Vitality 1: the wound kills it.
		assertInDiscard(denizen);
		assertEquals(0, scn.GetWoundsOn(throng));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAFreePeoplesTokenItTakesTheWoundOfASkirmishItLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(heirs);

		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		scn.MoveMinionsToTable(denizen);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(denizen));

		// Aragorn, 8, beats the Denizen, 6.
		scn.PassSkirmishActions();
		assertInDiscard(denizen);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAFreePeoplesTokenItTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var heirs = scn.GetFreepsCard("heirs");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(heirs);
		scn.MoveCardsToHand(fell);

		var denizen = scn.GetShadowCard("denizen");
		var footman = scn.GetShadowCard("footman");
		var throng = scn.GetShadowCard("throng");

		scn.StartGame();
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		scn.MoveMinionsToTable(denizen, footman, throng);

		scn.SkipToAssignments();
		// Aragorn skirmishes the Footman; the Denizen is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, footman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(throng));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound is placed during a skirmish, so the Denizen is a candidate beside the other minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(denizen, throng, footman));
		scn.FreepsChooseCard(denizen);
		assertInDiscard(denizen);
		assertEquals(0, scn.GetWoundsOn(throng));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
