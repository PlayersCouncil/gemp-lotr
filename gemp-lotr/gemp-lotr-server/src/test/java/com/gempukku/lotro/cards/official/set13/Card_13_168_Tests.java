package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggressor", "13_168");
					put("aggressor2", "13_168");
					put("sentry", "10_84");      // Cirith Ungol Sentry: [sauron] Uruk-hai minion, damage +1 only
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: resistance 6; a maneuver ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: resistance 5; a fellowship ability and a response when Frodo dies
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: ally, so of resistance 0; a start-of-turn heal and a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukAggressorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Aggressor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this, you may spot a companion who has resistance 0 to make this minion twilight cost -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aggressor");

		assertEquals("Uruk Aggressor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithACompanionOfResistance0ToSpotItCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToHand(aggressor);

		scn.StartGame();
		// Five burdens: Sam 5 - 5 = 0, Aragorn 6 - 5 = 1.
		scn.AddBurdens(5);
		assertEquals(0, scn.GetResistance(sam));
		assertEquals(1, scn.GetResistance(aragorn));
		assertInHand(aggressor);
		scn.FreepsPass();

		// At site 2 a site 5 minion is roaming: 3 to play - 2 for Sam + 2 roaming = 3; with no discount, 5.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(aggressor));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(aggressor));
		scn.ShadowPlayCard(aggressor);

		// Nothing is asked: the discount is taken.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnAllyOrAMinionOrACompanionOfResistance1GivesNoDiscountAndACompanionAtResistance0Does() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		var aggressor = scn.GetShadowCard("aggressor");
		var aggressor2 = scn.GetShadowCard("aggressor2");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(aggressor, aggressor2);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(1, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(elrond));
		scn.MoveMinionsToTable(sentry);
		assertInHand(aggressor, aggressor2);
		scn.FreepsPass();

		// Aragorn at resistance 1, an ally and a minion beside him: 3 to play + 2 roaming = 5.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(aggressor));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(aggressor));
		scn.ShadowPlayCard(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertEquals(0, scn.GetTwilight());

		// A sixth burden puts Aragorn at 0: the second copy costs 3 - 2 + 2 roaming = 3.
		scn.AddBurdens(1);
		assertEquals(0, scn.GetResistance(aragorn));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(aggressor2));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(aggressor2));
		scn.ShadowPlayCard(aggressor2);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor2);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
