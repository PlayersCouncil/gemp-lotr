package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stablemaster", "15_134");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6; Valiant is his whole text
					put("guard2", "5_83");
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit (a Man only beside 3 [rohan]
					// companions or Éowyn); his abilities are used only on purpose
					put("merry", "103_85");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is his whole text
					// Rider's Spear: a [rohan] hand weapon on a [rohan] Man, and no mount: "If bearer is mounted, bearer is
					// damage +1".  It reads whether its bearer is mounted.
					put("spear", "4_288");
					put("spear2", "4_288");
					put("troop", "1_177");      // Goblin Patrol Troop: no text; there so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohanStableMasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohan Stable Master
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype:
		 * Strength: 1
		 * Game Text: <b>Aid</b> – (1). <helper>(At the start of the maneuver phase, you may add (1) to transfer this to a companion.)</helper> To play, spot a [rohan] Man.<br>Bearer is considered to be mounted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stablemaster");

		assertEquals("Rohan Stable Master", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, scn.GetAidTwilightCost(card));
	}

	@Test
	public void CannotBePlayedUntilARohanManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stablemaster = scn.GetFreepsCard("stablemaster");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(merry, knight);
		scn.MoveCardsToHand(stablemaster, guard);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stablemaster));

		scn.FreepsPlayCard(guard);
		assertTrue(scn.FreepsPlayAvailable(stablemaster));

		scn.FreepsPlayCard(stablemaster);
		assertInZone(Zone.SUPPORT, stablemaster);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingARohanManWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stablemaster = scn.GetFreepsCard("stablemaster");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(stablemaster);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(stablemaster));
		scn.FreepsPlayCard(stablemaster);

		assertInZone(Zone.SUPPORT, stablemaster);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakenByItsAidToACompanionHeIsMountedAndNobodyElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stablemaster = scn.GetFreepsCard("stablemaster");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var spear = scn.GetFreepsCard("spear");
		var spear2 = scn.GetFreepsCard("spear2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, guard2);
		scn.MoveCardsToSupportArea(stablemaster);
		// Each Guard bears a Spear: a possession that is no mount.
		scn.AttachCardsTo(guard, spear);
		scn.AttachCardsTo(guard2, spear2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stablemaster);
		assertAttachedTo(spear, guard);
		assertAttachedTo(spear2, guard2);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		// In the support area the follower has no bearer, and a Spear alone mounts nobody.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stablemaster, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// The Aid: (1) added.
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guard2, frodo));
		scn.FreepsChooseCard(guard);

		// The bearer is mounted, so his Spear makes him damage +1; the other Guard bears no follower and is not.
		assertAttachedTo(stablemaster, guard);
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
