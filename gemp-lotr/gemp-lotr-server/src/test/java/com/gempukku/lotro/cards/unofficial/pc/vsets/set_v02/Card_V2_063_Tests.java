package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_063_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lugburz", "102_63");

					// What lowers site numbers.  Each of these is a [sauron] Orc and a tracker, printed site number 6.
					put("grishnakh", "5_100");  // Grishnakh, Orc Captain: "The site number of each [sauron] Orc is -3."  Strength 11, vitality 3; a Shadow-phase ability
					put("runner1", "5_109");    // Orc Runner: "The site number of each [sauron] Orc is -1."  Strength 6, vitality 2; a trigger on being played
					put("runner2", "5_109");
					// I See You: a [sauron] condition.  "The site number of each [Sauron] minion is -1 for each
					// burden you can spot", Orc or not, and each of them at 1 or less is strength +1.
					put("isee", "101_46");

					put("gorgoroth", "7_278");  // Gorgoroth Soldier: a [sauron] Orc, no tracker, printed site number 5; strength 9, vitality 2; a response that needs 2 cards in hand
					put("gothmog", "10_88");    // Gothmog, Lieutenant of Morgul: a [sauron] Orc, printed site number 4; strength 13, vitality 3; his text needs a controlled site
					put("mouth", "12_118");     // The Mouth of Sauron, Lieutenant of Barad-dur: a [sauron] Man, printed site number 5; vitality 3; an assignment ability
					put("goblin", "1_178");     // Goblin Runner: a [moria] Orc, printed site number 4; strength 5, vitality 1
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger, strength 8, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is not used
					// Eowyn, Lady of Ithilien: strength 6, vitality 3.  "Skirmish: Exert Eowyn to choose an opponent.
					// That opponent must wound a minion for each wound on each minion skirmishing Eowyn."
					put("eowyn", "10_72");

					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if that
					// minion is an Uruk-hai)."
					put("defend1", "4_115");
					put("defend2", "4_115");
					put("defend3", "4_115");
					// Pursuit Just Behind: "Regroup: Exert a ranger companion to wound every minion."
					put("pursuit", "1_111");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcofLugburzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Orc of Lugburz
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Tracker.
		 * 	Each [sauron] orc with a site number of 1 or less cannot take wounds except during the skirmish phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lugburz");

		assertEquals("Orc of Lugburz", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SauronOrcsAtSiteNumber1OrLessCannotBeChosenForAWoundInTheManeuverPhaseWhileOrcOfLugburzIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCardsToHand(defend1, defend2, defend3);

		var lugburz = scn.GetShadowCard("lugburz");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var runner = scn.GetShadowCard("runner1");
		var gorgoroth = scn.GetShadowCard("gorgoroth");
		var gothmog = scn.GetShadowCard("gothmog");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.MoveMinionsToTable(lugburz, grishnakh, runner, gorgoroth, gothmog, goblin);
		// Nothing in the game lowers the site number of an Orc of another culture.
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, Culture.MORIA, null, -3));
		// One more wound kills the Orc Runner, and one more the Orc of Lugburz.
		scn.AddWoundsToChar(runner, 1);
		scn.AddWoundsToChar(lugburz, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Grishnakh's -3 and the Runner's -1.
		assertEquals(2, scn.GetMinionSiteNumber(lugburz));
		assertEquals(2, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(2, scn.GetMinionSiteNumber(runner));
		assertEquals(1, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(0, scn.GetMinionSiteNumber(gothmog));
		assertEquals(1, scn.GetMinionSiteNumber(goblin));

		scn.FreepsPlayCard(defend1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The [sauron] Orcs at 2 can be wounded, and so can the [moria] Orc at 1.  The [sauron] Orcs at 1
		// and at 0 cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lugburz, grishnakh, runner, goblin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gorgoroth, gothmog));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);

		// With the Runner dead every [sauron] Orc's number is 1 higher: the Gorgoroth Soldier, now at 2,
		// can be wounded, and Gothmog, now at 1, still cannot.
		assertEquals(2, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(1, scn.GetMinionSiteNumber(gothmog));
		scn.ShadowPass();
		scn.FreepsPlayCard(defend2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lugburz, grishnakh, goblin, gorgoroth));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gothmog));
		scn.FreepsChooseCard(lugburz);
		assertInDiscard(lugburz);

		// With the Orc of Lugburz dead Gothmog can be wounded where he stands.
		assertEquals(1, scn.GetMinionSiteNumber(gothmog));
		assertEquals(0, scn.GetWoundsOn(gothmog));
		scn.ShadowPass();
		scn.FreepsPlayCard(defend3);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gothmog, grishnakh, goblin, gorgoroth));
		scn.FreepsChooseCard(gothmog);
		assertEquals(1, scn.GetWoundsOn(gothmog));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ASauronMinionThatIsNotAnOrcCanBeWoundedAtSiteNumber1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCardsToHand(defend1);

		scn.MoveCardsToSupportArea(scn.GetShadowCard("isee"));

		var lugburz = scn.GetShadowCard("lugburz");
		var gorgoroth = scn.GetShadowCard("gorgoroth");
		var mouth = scn.GetShadowCard("mouth");

		scn.StartGame();
		scn.MoveMinionsToTable(lugburz, gorgoroth, mouth);
		scn.AddBurdens(4);
		scn.SkipToPhase(Phase.MANEUVER);

		// I See You: -1 for each of 4 burdens, on every [sauron] minion.
		assertEquals(2, scn.GetMinionSiteNumber(lugburz));
		assertEquals(1, scn.GetMinionSiteNumber(gorgoroth));
		assertEquals(1, scn.GetMinionSiteNumber(mouth));

		scn.FreepsPlayCard(defend1);
		// The Gorgoroth Soldier is a [sauron] Orc at 1.  The Mouth of Sauron is a [sauron] Man at 1.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gorgoroth));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lugburz, mouth));
		scn.FreepsChooseCard(mouth);
		assertEquals(1, scn.GetWoundsOn(mouth));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ProtectedOrcsTakeNoArcheryWoundAreWoundedDuringSkirmishPhasesAndAreProtectedAgainInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, legolas, eowyn);
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCardsToHand(pursuit);

		var lugburz = scn.GetShadowCard("lugburz");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var goblin = scn.GetShadowCard("goblin");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(lugburz, grishnakh, runner1, runner2, goblin, savage);
		scn.SkipToArcheryWounds();

		// Grishnakh's -3 and two Runners' -1 each: every [sauron] Orc is at 1, the Orc of Lugburz included.
		assertEquals(1, scn.GetMinionSiteNumber(lugburz));
		assertEquals(1, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(1, scn.GetMinionSiteNumber(runner1));
		assertEquals(1, scn.GetMinionSiteNumber(runner2));

		// Archery: Legolas's one wound can only go on a minion who can take it.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lugburz, grishnakh, runner1, runner2));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, savage},
				new PhysicalCardImpl[]{aragorn, runner1});
		scn.ShadowDeclineAssignments();

		// Eowyn's skirmish with the Savage, who has a wound on him: the Shadow player must wound a minion.
		// It is a skirmish phase, so the Orc of Lugburz, who is in no skirmish, can be the one.
		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(0, scn.GetWoundsOn(lugburz));
		scn.FreepsUseCardAction(eowyn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(lugburz, grishnakh, runner1, runner2, goblin, savage));
		scn.ShadowChooseCard(lugburz);
		assertEquals(1, scn.GetWoundsOn(lugburz));

		// Eowyn's 6 beats the Savage's 5.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		// Aragorn's 8 beats the Orc Runner's 6, and the Runner takes the wound for losing.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(runner1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(runner1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Regroup: every minion is wounded but the [sauron] Orcs.  The Goblin Runner and the Savage die of it.
		scn.FreepsPlayCard(pursuit);
		assertEquals(1, scn.GetWoundsOn(lugburz));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(1, scn.GetWoundsOn(runner1));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertInDiscard(goblin, savage);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
