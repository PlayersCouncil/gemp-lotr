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

public class Card_07_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "7_38");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: +1 for each race spotted (Frodo's Hobbit: 7)
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("merry", "1_302");      // Merry, Friend to Sam: a companion of cost 1, a Hobbit like Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gandalf's Staff, Focus of Power
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be Gandalf.
		 * While you can spot 4 twilight tokens, Gandalf is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Focus of Power", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedOnGandalfAndNotOnAnotherGandalfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(staff, gandalf);

		scn.StartGame();

		// A [gandalf] companion of another title is no bearer.
		assertFalse(scn.FreepsPlayAvailable(staff));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(staff));
		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		assertNotAttachedTo(staff, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileFourTwilightTokensCanBeSpottedGandalfIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(merry);

		scn.StartGame();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(gandalf));    // 6 + 1 for Frodo's Hobbit
		int frodoStrength = scn.GetStrength(frodo);

		// Merry's cost of 1 makes the pool 4.
		scn.FreepsPlayCard(merry);
		assertEquals(8, scn.GetStrength(gandalf));    // 6 + 1 + 1
		// The bonus is Gandalf's alone.
		assertEquals(frodoStrength, scn.GetStrength(frodo));

		scn.SetTwilight(5);
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
