package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_049_Tests
{

	// The Hobbit table, with Northern Slopes as site 9 in place of Battle of the Five Armies 30_57.  Each test skips
	// to site 8 and passes: the Shadow phase at site 9 is where the ability is used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, vitality 4; no follower, his ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, vitality 3; a Maneuver ability, not used
					// Dain Ironfoot: [dwarven] ally, vitality 3, home site 9; Thorin strength +1, a start-of-regroup trigger
					// not reached.
					put("dain", "32_2");
					put("bifur", "30_5");       // Bifur, Inarticulate: a [dwarven] follower (no character), in the support area
					// Orkish Aggressor: [gundabad] Orc minion; an Assignment ability, not used.  Two copies, one culture.
					put("aggressor1", "30_39");
					put("aggressor2", "30_39");
					put("jail", "32_39");       // Jail: a [sauron] condition in the support area; a Regroup ability, not used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site9", "32_49");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void NorthernSlopesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Northern Slopes
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Battleground</b>. <b>Shadow:</b> Exert a [dwarven] character for each Shadow culture you can spot (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Northern Slopes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void WithOneShadowCultureExertsOneDwarvenCharacterOnceAndOnlyOnceThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var bifur = scn.GetFreepsCard("bifur");
		var bilbo = scn.GetRingBearer();
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var aggressor2 = scn.GetShadowCard("aggressor2");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin, dwalin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(bifur);
		// Vitality 3 with 2 wounds: Dwalin cannot exert.
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		// Two minions of one Shadow culture; the Free Peoples cultures in play ([dwarven], [shire]) are not counted.
		scn.MoveMinionsToTable(aggressor1, aggressor2);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slopes));

		scn.ShadowUseCardAction(slopes);
		// A [dwarven] companion and a [dwarven] ally; not the exhausted Dwalin, Bilbo, nor a [dwarven] follower.
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, dain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dwalin, bilbo, bifur));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(dain);

		// One exertion, on Dain.
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// Once per turn: not offered again, though Thorin could still exert.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slopes));
	}

	@Test
	public void WithTwoShadowCulturesBothExertionsMayGoOnOneDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var bilbo = scn.GetRingBearer();
		var aggressor1 = scn.GetShadowCard("aggressor1");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin, dwalin);
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		// Two Shadow cultures: [gundabad] on a minion, [sauron] on a condition in the support area.
		scn.MoveMinionsToTable(aggressor1);
		scn.MoveCardsToSupportArea(jail);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.ShadowActionAvailable(slopes));

		scn.ShadowUseCardAction(slopes);

		// Thorin, vitality 4 and the one Dwarf able to exert, is exerted for each of the two cultures
		// (CR 4.2 "for each": one character may be affected more than once).
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var aggressor1 = scn.GetShadowCard("aggressor1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin);
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		scn.MoveMinionsToTable(aggressor1);
		scn.ShadowRefreshDecision();
		// Offered in the Shadow phase, and not used there.
		assertTrue(scn.ShadowActionAvailable(slopes));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slopes));
		assertEquals(0, scn.GetWoundsOn(thorin));
	}

	@Test
	public void AShadowConditionAloneIsAShadowCultureToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		// No minion: one [sauron] condition in the support area.
		scn.MoveCardsToSupportArea(jail);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, jail);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));

		scn.ShadowUseCardAction(slopes);
		assertTrue(scn.ShadowHasCardChoiceAvailable(thorin, dain));
		scn.ShadowChooseCard(thorin);

		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoShadowCultureToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(thorin);
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		// No Shadow card in play: the count is 0 and the ability would do nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slopes));

		// One Shadow card put in play, and the decision sent again: offered.
		scn.MoveCardsToSupportArea(jail);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(slopes));
		assertEquals(0, scn.GetWoundsOn(thorin));
	}

	@Test
	public void IsNotOfferedWithNoDwarvenCharacterAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thorin = scn.GetFreepsCard("thorin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var aggressor1 = scn.GetShadowCard("aggressor1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(dwalin);
		scn.AddWoundsToChar(dwalin, 2);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		scn.FreepsPass();

		var slopes = scn.GetCurrentSite();
		assertEquals("32_49", slopes.getBlueprintId());
		scn.MoveMinionsToTable(aggressor1);
		scn.ShadowRefreshDecision();
		// Only an exhausted Dwarf and Bilbo: the ability would do nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slopes));

		scn.MoveCompanionsToTable(thorin);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(slopes));
	}
}
