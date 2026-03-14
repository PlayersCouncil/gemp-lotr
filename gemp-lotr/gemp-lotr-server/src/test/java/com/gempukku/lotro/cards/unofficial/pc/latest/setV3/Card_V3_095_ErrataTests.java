package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_095_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horror", "103_95");
					put("sky1", "103_97");        // Ominous Sky - Twilight condition
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("orc", "1_271");          // Orc Soldier - [Sauron] Orc
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoverofDarknessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cover of Darkness, Omen of Horror
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight.
		* 	To play, hinder 2 twilight conditions.
		* 	Skirmish: Exert an Orc or Troll and hinder a twilight condition to make that minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horror");

		assertEquals("Cover of Darkness", card.getBlueprint().getTitle());
		assertEquals("Omen of Horror", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OmenOfHorrorSkirmishAbilityHindersOnly1TwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var horror = scn.GetShadowCard("horror");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(horror, sky1, sky2);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPassCurrentPhaseAction();

		int orcStrengthBefore = scn.GetStrength(orc);
		int orcWoundsBefore = scn.GetWoundsOn(orc);
		assertFalse(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2));

		assertTrue(scn.ShadowActionAvailable(horror));
		scn.ShadowUseCardAction(horror);
		// Orc auto-selected as only Orc/Troll
		// Errata: only hinder 1 twilight condition (was 2 in original)
		// sky1 or sky2 chosen; with 2 choices one should be auto-picked or we pick
		scn.ShadowChooseCard(sky1);

		assertEquals(orcWoundsBefore + 1, scn.GetWoundsOn(orc));
		assertTrue(scn.IsHindered(sky1));
		assertFalse(scn.IsHindered(sky2)); // Only 1 hindered, not 2
		assertEquals(orcStrengthBefore + 2, scn.GetStrength(orc));
	}
}
