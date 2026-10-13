package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_118");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

					// Lurtz, Servant of Isengard: archer, damage +1, strength 13 (under twice Aragorn's 8); his
					// maneuver ability needs another Uruk-hai
					put("lurtz", "1_127");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValiantManoftheWestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Valiant Man of the West
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Make Aragorn <b>defender +1</b> and he takes no more than 1 wound during each skirmish phase until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Valiant Man of the West", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AragornTakesOneWoundInASkirmishHeLosesToADamageMinionAndAnArcheryWoundIsNoPartOfThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valiant));

		scn.FreepsPlayCard(valiant);
		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Lurtz's archery wound is no wound of a skirmish phase: it is taken, and it does not use up the one
		// wound the skirmish allows.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 8 against 13: Aragorn loses, and of the two wounds a damage +1 minion deals he takes one.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
