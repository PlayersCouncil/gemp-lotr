package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "1_38");
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, printed archer; his archery ability is used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, no archer; strength +3 against a Nazgul only
					put("elvenbow", "1_41");    // Elven Bow: bearer must be an Elf; bearer is an archer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragornsbow", "1_90"); // Aragorn's Bow: on Aragorn, "He is an archer"; its archery ability is used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, printed archer, home 6; his archery ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DoubleShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Double Shot
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot an Elf archer companion to make the fellowship archery total +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shot");

		assertEquals("Double Shot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingLegolasMakesTheFellowshipArcheryTotalPlusOneForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(shot);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Legolas, the one archer, adds 1.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shot));

		scn.FreepsPlayCard(shot);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(shot);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// In force as the archery fire is placed (the one minion takes both wounds unasked), and gone with the
		// phase: the assignment phase reads Legolas's 1 again.
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.PassArcheryActions();
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ArwenBearingAnElvenBowIsAnElfArcherCompanionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var arwen = scn.GetFreepsCard("arwen");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, elvenbow);
		scn.MoveCardsToHand(shot);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Arwen is an archer by her bow, and adds 1.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shot));

		scn.FreepsPlayCard(shot);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(shot);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnElfWhoIsNoArcherAnArcherWhoIsNoElfOrAnElfArcherWhoIsNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornsbow = scn.GetFreepsCard("aragornsbow");
		var orophin = scn.GetFreepsCard("orophin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(aragorn, aragornsbow);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(shot);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Arwen is an Elf companion and no archer; Aragorn is an archer companion by his bow and no Elf;
		// Orophin is an Elf archer and an ally.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertFalse(scn.FreepsPlayAvailable(shot));
		assertInHand(shot);
	}
}
