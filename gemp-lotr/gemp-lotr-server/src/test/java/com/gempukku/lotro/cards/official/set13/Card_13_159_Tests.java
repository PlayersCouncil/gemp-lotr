package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_159_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denizen", "13_159");
					put("denizen2", "13_159");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, strength 5; its text is about being played
					// Free Peoples conditions that give themselves tokens of their culture
					put("kept", "6_111");       // Kept Safe: [shire], two tokens as it is played; its Response needs an unbound Hobbit about to be wounded
					put("path", "8_42");        // A Path Appointed: [gondor], adds a token to itself by its Fellowship ability (never used here)
					put("palaces", "6_10");     // Suspended Palaces: [dwarven], two tokens as it is played beside a Dwarf
					put("ale", "18_108");       // Golden Perch Ale: [shire] support-area POSSESSION, a token for each [shire] companion's win (never reached here)
					// Shadow cards that hold tokens of their culture
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession; its abilities are never used
					put("threatening", "13_103"); // Always Threatening: [orc] support-area condition; its skirmish ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AssaultDenizenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Assault Denizen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	This minion is strength +1 for each Free Peoples culture token you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("denizen");

		assertEquals("Assault Denizen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusTwoOnceKeptSafeIsPlayedWithTwoShireTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("kept");
		scn.MoveCardsToHand(kept);

		var denizen = scn.GetShadowCard("denizen");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// Minions that will stand in play later in the turn, laid in the fellowship phase to read the count change.
		scn.MoveMinionsToTable(denizen, runner);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.HAND, kept);
		assertEquals(9, scn.GetStrength(denizen));

		scn.FreepsPlayCard(kept);
		// Two tokens on one card: +1 for each token, not for the card.
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertEquals(11, scn.GetStrength(denizen));
		// The bonus is this minion's alone.
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsFreePeoplesTokensOfEachCultureAndNoShadowToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		var palaces = scn.GetFreepsCard("palaces");
		var ale = scn.GetFreepsCard("ale");
		scn.MoveCardsToSupportArea(path, palaces, ale);

		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var runner = scn.GetShadowCard("runner");
		var ships = scn.GetShadowCard("ships");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(ships, threatening);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, denizen2, runner);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertEquals(9, scn.GetStrength(denizen));

		// Shadow culture tokens, and burdens, are no Free Peoples culture tokens.
		scn.AddTokensToCard(ships, 1);
		scn.AddTokensToCard(threatening, 2);
		scn.AddBurdens(2);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(denizen));

		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(10, scn.GetStrength(denizen));

		scn.AddTokensToCard(palaces, 2);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		// 9 + 1 [gondor] + 2 [dwarven]
		assertEquals(12, scn.GetStrength(denizen));

		// A token on a possession counts as well as one on a condition.
		assertEquals(0, scn.GetCultureTokensOn(ale));
		scn.AddTokensToCard(ale, 1);
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(13, scn.GetStrength(denizen));
		// Each copy gives the bonus to itself alone: the second reads 13 as well, not 17.
		assertEquals(13, scn.GetStrength(denizen2));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
