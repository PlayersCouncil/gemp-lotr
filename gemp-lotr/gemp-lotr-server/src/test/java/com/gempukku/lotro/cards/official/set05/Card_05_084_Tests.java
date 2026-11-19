package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("here", "5_84");

					// Théoden, Leader of Spears: a [rohan] Man of vitality 3, so able to exert twice, and himself of the
					// Théoden signet; his move trigger is optional and no move is made
					put("theoden", "7_254");
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a [rohan] Man, Théoden signet; a response used only on purpose
					put("merry", "7_321");      // Merry, Swordthain: a [shire] Hobbit of vitality 4, Théoden signet; a skirmish ability only
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion with no signet; Valiant and no other text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of the Gandalf signet; a maneuver ability only
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY (no signet); an ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IAmHereStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: I Am Here
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert Théoden twice to heal every other companion with the Théoden signet twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("here");

		assertEquals("I Am Here", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheodenExertsTwiceToHealEveryOtherCompanionOfHisSignetTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var here = scn.GetFreepsCard("here");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCardsToHand(here);
		scn.MoveCompanionsToTable(theoden, eowyn, merry, guard, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(eowyn, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(guma, 1);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertInHand(here);

		assertTrue(scn.FreepsPlayAvailable(here));
		scn.FreepsPlayCard(here);

		// Théoden exerts twice, and is not healed though he has the Théoden signet ("other").
		assertEquals(2, scn.GetWoundsOn(theoden));
		// Merry (a Hobbit) and Éowyn (a Man) of the Théoden signet are each healed twice: 3 to 1, 2 to 0.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		// No signet, the Gandalf signet, and an ally: not healed.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertInDiscard(here);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheodenCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var here = scn.GetFreepsCard("here");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(here);
		scn.MoveCompanionsToTable(theoden, eowyn, merry);
		// Vitality 3 with 1 wound: one exertion left, not two.
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(merry));

		// Éowyn, a [rohan] companion able to exert twice, does not pay for Théoden; Merry has wounds to heal.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(here));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInHand(here);
	}

	@Test
	public void WithNoWoundedCompanionOfTheTheodenSignetItIsStillPlayedAndOnlyTheodenExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var here = scn.GetFreepsCard("here");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(here);
		scn.MoveCompanionsToTable(theoden, eowyn, merry, guard);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(guard));

		assertTrue(scn.FreepsPlayAvailable(here));
		scn.FreepsPlayCard(here);

		assertEquals(2, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInDiscard(here);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
