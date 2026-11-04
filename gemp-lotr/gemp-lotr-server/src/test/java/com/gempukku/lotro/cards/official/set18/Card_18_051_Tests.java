package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("forgondor", "18_51");
					put("forgondor2", "18_51");

					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man knight, strength 6, vitality 3; Knight is its whole text
					// Pippin, Trollslayer: a [gondor] Hobbit knight, strength 3, vitality 4; enduring (+2 strength for each
					// wound on him); a skirmish ability used only on purpose
					put("pippin", "103_31");
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man companion who is no knight; a regroup ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: For Gondor!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a knight to make him or her strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("forgondor");

		assertEquals("For Gondor!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheKnightChosenToMakeHimOrHerStrengthPlus3ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forgondor = scn.GetFreepsCard("forgondor");
		var forgondor2 = scn.GetFreepsCard("forgondor2");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, pippin, ranger);
		scn.MoveCardsToHand(forgondor, forgondor2);

		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");

		assertInHand(forgondor, forgondor2);
		scn.StartGame();
		scn.MoveMinionsToTable(savage, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{pippin, explorer});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));

		assertTrue(scn.FreepsPlayAvailable(forgondor));
		scn.FreepsPlayCard(forgondor);
		assertEquals(1, scn.FreepsGetChoiceMin());
		// A knight in this skirmish, and one of another race assigned to a skirmish still to come; not a [gondor]
		// Man who is no knight, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, frodo));
		scn.FreepsChooseCard(pippin);

		// Pippin, exerted: 3 + 2 (enduring, one wound) + 3.  The Knight beside him is unchanged.
		assertInDiscard(forgondor);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(3 + 2 + 3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(forgondor2);
		scn.FreepsChooseCard(knight);

		assertInDiscard(forgondor2);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(6 + 3, scn.GetStrength(knight));
		assertEquals(3 + 2 + 3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 9 against 5: the Knight wins.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(knight));

		// Pippin's +3 was laid in the Knight's skirmish and is gone in his own, a skirmish phase of its own:
		// 3 + 2 (enduring, his one wound).
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(3 + 2, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithItsOnlyKnightExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var forgondor = scn.GetFreepsCard("forgondor");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(forgondor);

		var savage = scn.GetShadowCard("savage");

		assertInHand(forgondor);
		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.  The Ranger, a [gondor] Man who is no knight, could exert.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(forgondor));
	}
}
