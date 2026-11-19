package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_119_Tests
{

	// House of Elrond is site 3; the rest is the usual Fellowship path.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion; strength +3 against a Nazgul only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an Elf ally, home 6F; an archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability only
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "3_119");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HouseofElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: House of Elrond
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. When the fellowship moves to House of Elrond, the Free Peoples player may spot 2 Elves to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("House of Elrond", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void MovingToHouseOfElrondWithAnElfCompanionAndAnElfAllyRemovesABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(2);
		// The two Elves are a companion and an ally; Gimli is no Elf.
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPass();
		// The site on the path is whichever player's House of Elrond was laid there.
		var house = scn.GetCurrentSite();
		assertEquals("House of Elrond", house.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(house));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItLeavesTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(orophin);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPass();
		// The site on the path is whichever player's House of Elrond was laid there.
		var house = scn.GetCurrentSite();
		assertEquals("House of Elrond", house.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(house));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneElfBesideACompanionWhoIsNoElfNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPass();
		var house = scn.GetCurrentSite();
		assertEquals("House of Elrond", house.getBlueprint().getTitle());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(house));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(orophin);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		var house = scn.GetCurrentSite();
		assertEquals("House of Elrond", house.getBlueprint().getTitle());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(house));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromHouseOfElrondOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");

		scn.StartGame();
		scn.SkipToSite(3);
		var house = scn.GetCurrentSite();
		assertEquals("House of Elrond", house.getBlueprint().getTitle());
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(orophin);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(house));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
