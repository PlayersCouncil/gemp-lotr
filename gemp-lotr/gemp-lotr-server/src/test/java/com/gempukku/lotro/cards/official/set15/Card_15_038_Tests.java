package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treebeard", "15_38");

					put("orc1", "1_272"); //strength 10
					put("orc2", "1_262");
					put("orc3", "1_266");
					put("orc4", "1_267");
					put("enduring", "10_68");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreebeardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Treebeard, Enraged Shepherd
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Ent
		 * Strength: 12
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 3 [gandalf] companions.<br>Each time Treebeard wins a skirmish, the first Shadow
		 * player must exert X minions, where X is the difference between Treebeard's strength and the losing
		 * character's strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("treebeard");

		assertEquals("Treebeard", card.getBlueprint().getTitle());
		assertEquals("Enraged Shepherd", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ENT, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void TreebeardExerts2MinionsAgainstStrength10Minion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var orc1 = scn.GetShadowCard("orc1");
		var orc2 = scn.GetShadowCard("orc2");
		var orc3 = scn.GetShadowCard("orc3");
		var orc4 = scn.GetShadowCard("orc4");
		scn.MoveMinionsToTable(orc1, orc2, orc3, orc4);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, orc1); //10 strength orc
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(treebeard);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		//Treebeard is strength 12, the orc is 10, so 2 minions must be exerted
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(0, scn.GetWoundsOn(orc2));
		assertEquals(0, scn.GetWoundsOn(orc3));

		scn.ShadowChooseCards(orc2, orc3);
		assertEquals(1, scn.GetWoundsOn(orc2));
		assertEquals(1, scn.GetWoundsOn(orc3));
	}

	@Test
	public void TreebeardDoesNotCrashIfAgainstEnduringMinionOf11Strength() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.MoveCompanionsToTable(treebeard);

		var enduring = scn.GetShadowCard("enduring");
		var orc1 = scn.GetShadowCard("orc1"); //only here to slow down choices
		scn.MoveMinionsToTable(enduring, orc1);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, enduring); //11 strength enduring nazgul
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(treebeard);
		scn.PassCurrentPhaseActions();
		assertEquals(11, scn.GetStrength(enduring));
		assertEquals(0, scn.GetWoundsOn((enduring)));
		scn.FreepsResolveRuleFirst();
		assertEquals(13, scn.GetStrength(enduring));
		assertEquals(1, scn.GetWoundsOn((enduring))); // Skirmish loss

		//Treebeard is strength 12, the nazgul is 11.  However, 1 wound makes it 13
		// at the time this is evaluated, so it should be an absolute calculation.
		//With only 1 minion in play, it gets the exertion
		assertNotEquals(-1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(enduring);
		assertEquals(2, scn.GetWoundsOn((enduring)));
	}
}
