package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "11_177");

					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an archer companion, resistance 6; +3 only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an archer ally, resistance 0, home 6F; an archery ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Fellowship: exert Sam to remove a burden
					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer, and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmyofUrukhaiStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Army of Uruk-hai
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each Free Peoples archer who has resistance 5 or less does not add to the Free Peoples archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Army of Uruk-hai", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void ArchersOfResistance5OrLessAddNothingToTheFellowshipArcheryTotalAndTheMinionArcheryTotalIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		// Site 6 is Orophin's home, where an ally takes part in archery fire.
		scn.SkipToSite(5);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetCurrentSiteNumber());

		// One burden: Legolas is resistance 6 - 1 = 5; an ally has resistance 0.
		assertEquals(5, scn.GetResistance(legolas));
		assertEquals(0, scn.GetResistance(orophin));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.MoveMinionsToTable(army);
		// Neither archer adds; the Shadow archer, who has no resistance either, still adds to the minion total.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnArcherOfResistance6AddsToTheFellowshipArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetShadowCard("army");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(legolas, sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(army, marksman);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Sam removes the burden: Legolas is back at resistance 6, one more than the limit.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(legolas));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
