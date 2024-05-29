package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_170_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("pippin", "11_170");
					put("gimli", "11_8");
					put("legolas", "11_21");
					put("sam", "1_311");

					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_329"); // Breeland Forest
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pippin, Brave Decoy
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 9
		 * Game Text: <b>Skirmish:</b> If Pippin is not assigned to a skirmish, spot an unbound companion who has less resistance than Pippin to have Pippin replace him or her in a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Brave Decoy", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(9, card.getBlueprint().getResistance());
	}

	@Test
	public void PippinAbilityWorksOnLowerResCompanionButNotEqual() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.FreepsMoveCharToTable(pippin, gimli, legolas);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.ShadowMoveCharToTable(runner1, runner2);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   gimli, runner1 },
				new PhysicalCardImpl[] { legolas, runner2 }
		);

		//9 - 1 from the bid burden
		assertEquals(8, scn.GetResistance(pippin));
		//7 - 1; Gimli is an eligible target for Pippin's ability
		assertEquals(6, scn.GetResistance(gimli));
		//7 - 1 + 2 from Legolas' ability; Legolas is not an eligible target
		assertEquals(8, scn.GetResistance(legolas));

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(runner1));
		assertTrue(scn.IsCharSkirmishing(gimli));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.IsCharSkirmishing(runner1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharSkirmishing(pippin));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveSkirmish(legolas);
		//check that pippin didn't die lol
		assertEquals(Zone.FREE_CHARACTERS, pippin.getZone());
		assertTrue(scn.IsCharSkirmishing(runner2));
		assertTrue(scn.IsCharSkirmishing(legolas));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.FreepsActionAvailable(pippin));
	}

	@Test
	public void PippinAbilityDoesNotWorkIfPippinAssignedToSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(pippin, gimli);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.ShadowMoveCharToTable(runner1, runner2);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   gimli, runner1 },
				new PhysicalCardImpl[] { pippin, runner2 }
		);

		//9 - 1 from the bid burden
		assertEquals(8, scn.GetResistance(pippin));
		//7 - 1; Gimli is an eligible target for Pippin's ability
		assertEquals(6, scn.GetResistance(gimli));

		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishing(runner2));
		assertTrue(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertFalse(scn.FreepsActionAvailable(pippin));
	}

	@Test
	public void PippinAbilityDoesNotWorkOnRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(pippin, sam);

		var runner1 = scn.GetShadowCard("runner1");
		scn.ShadowMoveCharToTable(runner1);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   sam, runner1 }
		);

		//9 - 1 from the bid burden
		assertEquals(8, scn.GetResistance(pippin));
		//5 - 1; Sam is theoretically an eligible target for Pippin's ability
		assertEquals(4, scn.GetResistance(sam));

		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(runner1));
		assertTrue(scn.IsCharSkirmishing(sam));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.FreepsActionAvailable(pippin));
	}
}
