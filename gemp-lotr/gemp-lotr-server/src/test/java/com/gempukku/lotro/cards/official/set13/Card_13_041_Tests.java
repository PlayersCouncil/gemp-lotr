package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("meeting", "13_41");
					// Gandalf, The Grey Wizard: a [gandalf] Wizard companion, strength 7; his Fellowship ability is
					// used only on purpose
					put("gandalf", "1_364");
					// Ent Avenger: a [gandalf] Ent companion, strength 6; +2 only while 3 Ents can be spotted
					put("avenger1", "6_27");
					put("avenger2", "6_27");
					// Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; his Maneuver ability is used
					// only on purpose
					put("aragorn", "1_89");
					// Rohan Worker: a [rohan] follower with no strength or resistance; its texts answer a transfer
					// (cheated on, it is not transferred) and a skirmish won
					put("worker1", "15_135");
					put("worker2", "15_135");
					// Elendil's Army: a [gondor] follower, strength +2; Aid: exert a [gondor] companion
					put("army", "18_47");
					// Saruman, Servant of Sauron: a Shadow follower a minion bears; strength +2, and a trigger on a
					// skirmish won
					put("saruman", "17_38");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrangeMeetingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Strange Meeting
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each companion bearing a follower gains <b>muster</b> (and is strength +1 if that companion is a [gandalf] companion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("meeting");

		assertEquals("Strange Meeting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionGainsMusterOnceAFollowerIsTransferredToHimAndAGondorCompanionIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meeting = scn.GetFreepsCard("meeting");
		var aragorn = scn.GetFreepsCard("aragorn");
		var army = scn.GetFreepsCard("army");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(meeting, army);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, meeting, army);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// The follower lies in the support area: nobody bears it.
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		scn.FreepsAcceptOptionalTrigger();
		// Aragorn is the only [gondor] companion, so he exerts for the Aid unasked; then the companion it goes to.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(army, aragorn);

		assertTrue(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		// 8 + 2 for the Army: Aragorn is no [gandalf] companion, so nothing more.
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachCompanionBearingAFollowerHasMusterAndEachGandalfOneIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var meeting = scn.GetFreepsCard("meeting");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger1 = scn.GetFreepsCard("avenger1");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var worker1 = scn.GetFreepsCard("worker1");
		var worker2 = scn.GetFreepsCard("worker2");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(gandalf, avenger1, avenger2);
		scn.MoveCardsToSupportArea(meeting);
		scn.AttachCardsTo(gandalf, worker1);
		scn.AttachCardsTo(avenger1, worker2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, meeting);
		assertAttachedTo(worker1, gandalf);
		assertAttachedTo(worker2, avenger1);
		// A minion that bears a follower of its own player's, as Saruman's Aid puts him on one.
		scn.MoveMinionsToTable(troop);
		scn.AttachCardsTo(troop, saruman);
		assertAttachedTo(saruman, troop);

		// A Wizard and an Ent, both [gandalf] companions bearing a follower: muster, and strength +1.
		assertTrue(scn.HasKeyword(gandalf, Keyword.MUSTER));
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.HasKeyword(avenger1, Keyword.MUSTER));
		assertEquals(7, scn.GetStrength(avenger1));
		// A [gandalf] companion bearing no follower, and the Ring-bearer: neither.
		assertFalse(scn.HasKeyword(avenger2, Keyword.MUSTER));
		assertEquals(6, scn.GetStrength(avenger2));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		// A minion bearing a follower is no companion.
		assertFalse(scn.HasKeyword(troop, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
