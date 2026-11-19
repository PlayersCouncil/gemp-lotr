package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_161_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("southron", "7_161");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man, 5/3; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's; strength 8, vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronBrigandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Brigand
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.
		 * <b>Shadow:</b> Remove (3) and spot 3 [raider] Men to make one of those Men <b>damage +1</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var southron = scn.GetFreepsCard("southron");

		assertEquals("Southron Brigand", southron.getBlueprint().getTitle());
		assertNull(southron.getBlueprint().getSubtitle());
		assertFalse(southron.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, southron.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, southron.getBlueprint().getCulture());
		assertEquals(CardType.MINION, southron.getBlueprint().getCardType());
		assertEquals(Race.MAN, southron.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(southron, Keyword.SOUTHRON));
		assertEquals(4, southron.getBlueprint().getTwilightCost());
		assertEquals(9, southron.getBlueprint().getStrength());
		assertEquals(2, southron.getBlueprint().getVitality());
		assertEquals(4, southron.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityRemoves3ToMakeOneOfThreeRaiderMenDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var southron = scn.GetShadowCard("southron");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Three [raider] Men: the Southron Brigand himself, a Southron and an Easterling.  Beside them a [raider]
		// minion who is no Man, a Man who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(southron, explorer, axeman, khamul, brigand);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(southron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(axeman, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(southron));

		scn.ShadowUseCardAction(southron);
		assertEquals(1, scn.GetTwilight()); // 4 - 3
		assertTrue(scn.ShadowHasCardChoiceAvailable(southron, explorer, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axeman);

		assertEquals(1, scn.GetKeywordCount(axeman, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(southron, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(khamul, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(brigand, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithTwoRaiderMenAndCardsThatAreNotBesideThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		var southron = scn.GetShadowCard("southron");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Two [raider] Men; beside them a [raider] minion who is no Man, a Man who is not [raider], and the Free
		// Peoples player's own Man.
		scn.MoveMinionsToTable(southron, explorer, khamul, brigand);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(southron));
	}

	@Test
	public void IsNotOfferedWithOnly2ToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var southron = scn.GetShadowCard("southron");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(southron, explorer, axeman);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(southron));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(southron));
	}

	@Test
	public void TheBonusLastsIntoALaterSkirmishAndIsGoneInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var southron = scn.GetShadowCard("southron");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveMinionsToTable(southron, explorer, axeman);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		scn.ShadowUseCardAction(southron);
		scn.ShadowChooseCard(explorer);
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));

		// The Brigand to Aragorn, the Explorer to Boromir; the Axeman stays unassigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, southron}, new PhysicalCardImpl[]{boromir, explorer});
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish first: the Brigand (9) beats him (8).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// In the Explorer's own skirmish, after one skirmish is over, the bonus is still on.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(1, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(explorer));

		// Boromir (7) beats the Explorer (5), who lives through the wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(boromir));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertEquals(0, scn.GetKeywordCount(explorer, Keyword.DAMAGE));
	}
}
