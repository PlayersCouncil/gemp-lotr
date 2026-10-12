package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orophin", "1_56");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2, Damage +1 and no other text
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai; each companion resistance -1,
					// and a skirmish ability never used
					put("saruman", "17_116");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, the minion of another race; its play trigger is not set off by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrophinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orophin, Lórien Bowman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Archer.
		 * Archery: Exert Orophin to wound an Uruk-hai.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orophin");

		assertEquals("Orophin", card.getBlueprint().getTitle());
		assertEquals("Lórien Bowman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void ArcheryAbilityExertsOrophinToWoundOneUrukHaiOfEitherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, saruman, runner);
		// Away from his home site 6 he adds nothing to the archery total, but his ability is his to use anywhere.
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsActionAvailable(orophin));

		scn.FreepsUseCardAction(orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman, runner, orophin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileOrophinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(orophin, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(orophin));
	}
}
