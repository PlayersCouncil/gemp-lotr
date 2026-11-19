package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("calaglin", "3_10");
					put("rumil", "1_57");         // Rúmil, Elven Protector: an Elf ally, home 6F; an archery ability
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion; a maneuver ability

					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CalaglinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Calaglin, Elf of Lórien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Archer</b>.<br><b>Regroup:</b> Exert Calaglin to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("calaglin");

		assertEquals("Calaglin", card.getBlueprint().getTitle());
		assertEquals("Elf of Lórien", card.getBlueprint().getSubtitle());
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
	public void RegroupActionExertsCalaglinToWoundOneMinionOfAnyKind() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(calaglin, rumil);
		scn.MoveCompanionsToTable(aragorn);

		var patrol = scn.GetShadowCard("patrol");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol, savage);
		scn.SkipToPhase(Phase.REGROUP);

		// At site 2, far from his home at site 6: an ally's special ability is used wherever the fellowship is.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(calaglin));

		scn.FreepsUseCardAction(calaglin);
		assertEquals(1, scn.GetWoundsOn(calaglin));
		// A minion of either culture and race; no companion and no ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, aragorn, calaglin, rumil));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// One wound, on the one chosen: vitality 3 leaves room for more.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExhaustedCalaglinCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.AddWoundsToChar(calaglin, 2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 3 with 2 wounds: he cannot exert, and a minion is there to wound.
		assertEquals(2, scn.GetWoundsOn(calaglin));
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(calaglin));
	}
}
