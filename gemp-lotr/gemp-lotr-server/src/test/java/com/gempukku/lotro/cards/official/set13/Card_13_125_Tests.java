package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ferthu", "13_125");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, resistance 6; Valiant is its whole text
					// Éomer, Heir to Meduseld: [rohan] Man companion, resistance 7; two texts about damage and strength that
					// these tests do not reach
					put("eomer", "13_123");
					// Merry, From O'er the Brandywine: [shire] Hobbit companion, resistance 6; strength +2 while he bears a weapon
					put("merryshire", "1_303");
					// Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Guma, Plains Farmer: [rohan] Man ally (resistance 0, as every ally), home 4T (no such site on this
					// path); a fellowship ability used only on purpose
					put("guma", "4_277");

					// Cards to discard, and cards that are no possession.
					put("coat", "1_101");       // Coat of Mail: [gondor] possession, borne by a Man; an overwhelm rule only
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; an optional draw as it is played
					// Do Not Trust to Hope: [rohan] condition on a [rohan] companion; Valiant to its bearer, and a
					// start-of-regroup wound or discard
					put("hope", "102_45");
					// Phial of Galadriel, The Light of Eärendil: [shire] artifact on a Ring-bound Hobbit; muster texts only
					put("phial", "13_155");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability only
					// Banner of the Eye: [sauron] possession on a [sauron] Orc; an optional trigger on its bearer's win
					put("banner", "6_98");
					// Ithil Stone: [sauron] artifact in the support area; an optional draw at the start of its owner's Shadow
					// phases and a Shadow ability used only on purpose
					put("ithil", "9_47");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FerthuTheodenHalStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Ferthu Théoden Hal
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: To play, spot 2 [rohan] Men.<br>Unless you can spot a companion who has resistance 3 or less, discard a possession from play.
		*/

		var scn = GetScenario();

		var ferthu = scn.GetFreepsCard("ferthu");

		assertEquals("Ferthu Théoden Hal", ferthu.getBlueprint().getTitle());
		assertNull(ferthu.getBlueprint().getSubtitle());
		assertFalse(ferthu.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ferthu.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, ferthu.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, ferthu.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(ferthu, Timeword.MANEUVER));
		assertEquals(1, ferthu.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoRohanMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferthu = scn.GetFreepsCard("ferthu");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveCardsToHand(ferthu);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertInHand(ferthu);
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);

		// One [rohan] Man; Merry is a [rohan] Hobbit (two [rohan] companions make him no Man) and Boromir a
		// [gondor] Man.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ferthu));

		// A [rohan] Man ally makes two.
		scn.MoveAlliesToTable(guma);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.FreepsPlayAvailable(ferthu));

		// No possession in play: the event is played for nothing.
		scn.FreepsPlayCard(ferthu);
		assertInDiscard(ferthu);
		assertInZone(Zone.FREE_CHARACTERS, guard, merry, boromir);
		assertInZone(Zone.SUPPORT, guma);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DiscardsAPossessionOfEitherSideWhileNoCompanionHasResistance3OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferthu = scn.GetFreepsCard("ferthu");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var coat = scn.GetFreepsCard("coat");
		var toby = scn.GetFreepsCard("toby");
		var hope = scn.GetFreepsCard("hope");
		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, coat, hope);
		scn.AttachCardsTo(frodo, phial);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(ferthu);

		var soldier = scn.GetShadowCard("soldier");
		var banner = scn.GetShadowCard("banner");
		var ithil = scn.GetShadowCard("ithil");

		scn.StartGame();
		assertInHand(ferthu);
		assertAttachedTo(coat, guard);
		assertAttachedTo(hope, guard);
		assertAttachedTo(phial, frodo);
		assertInZone(Zone.SUPPORT, toby);
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		scn.MoveCardsToSupportArea(ithil);
		assertAttachedTo(banner, soldier);
		assertInZone(Zone.SUPPORT, ithil);
		// 2 burdens: the Guard's resistance 6 is 4, one more than the text allows. Guma, an ally, has resistance 0,
		// and the minion none: neither is a companion.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(guard));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(ferthu));
		scn.FreepsPlayCard(ferthu);

		// A Free Peoples possession borne, one in the support area and a Shadow possession; not a condition, nor an
		// artifact of either side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(coat, toby, banner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hope, phial, ithil));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(banner);

		assertInDiscard(banner, ferthu);
		assertAttachedTo(coat, guard);
		assertInZone(Zone.SUPPORT, toby, ithil);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWhileACompanionHasResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferthu = scn.GetFreepsCard("ferthu");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, coat);
		scn.MoveCardsToHand(ferthu);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertInHand(ferthu);
		assertAttachedTo(coat, guard);
		scn.MoveMinionsToTable(soldier);
		// 3 burdens: the Guard's resistance 6 is exactly 3.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(guard));
		scn.SkipToPhase(Phase.MANEUVER);

		// Ketura (Q-0390: B): the "unless" is a condition inside the effect, not a requirement to play. The event
		// is played, leaves the hand, and discards nothing.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ferthu));
		scn.FreepsPlayCard(ferthu);
		assertInDiscard(ferthu);
		assertAttachedTo(coat, guard);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWhileOnlyAShireHobbitCompanionHasResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ferthu = scn.GetFreepsCard("ferthu");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merryshire");
		var coat = scn.GetFreepsCard("coat");
		scn.MoveCompanionsToTable(eomer, merry);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(eomer, coat);
		scn.MoveCardsToHand(ferthu);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertInHand(ferthu);
		assertAttachedTo(coat, eomer);
		scn.MoveMinionsToTable(soldier);
		// 3 burdens: Merry's resistance 6 is exactly 3, Éomer's 7 is 4. The [rohan] Man is not the one who stops the discard.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(merry));
		assertEquals(4, scn.GetResistance(eomer));
		scn.SkipToPhase(Phase.MANEUVER);

		// Ketura (Q-0390: B): the "unless" is a condition inside the effect, not a requirement to play. The event
		// is played, leaves the hand, and discards nothing.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(ferthu));
		scn.FreepsPlayCard(ferthu);
		assertInDiscard(ferthu);
		assertAttachedTo(coat, eomer);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
