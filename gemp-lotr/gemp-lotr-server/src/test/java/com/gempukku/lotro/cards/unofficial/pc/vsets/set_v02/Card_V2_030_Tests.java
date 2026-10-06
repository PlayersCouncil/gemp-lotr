package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_030_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pack", "102_30");

					// Warg-rider, Isengard Rider and Isengard Flanker: [isengard] Orcs and warg-riders, of
					// strength 5, 8 and 7.  Each is fierce while not exhausted and has no other text.
					put("wargrider", "5_67");
					put("isenrider", "5_53");
					put("flanker", "5_52");
					// Isengard Worker: an [isengard] Orc of strength 5 that is no warg-rider; a regroup ability.
					put("worker", "3_62");
					// Uruk Savage: an [isengard] minion that is no Orc.  Strength 5; Damage +1 is its whole text.
					put("savage", "1_151");
					// Goblin Runner: an Orc that is not [isengard].  Strength 5.  Cheated in, so its text never happens.
					put("runner", "1_178");

					// Warg (cost 3, strength +3, vitality +1) and War-warg (cost 4, strength +4, vitality +2):
					// [isengard] mounts.  "Bearer must be a warg-rider", and a response to skirmish special
					// abilities, which no test here comes to.
					put("warg1", "5_65");
					put("warg2", "5_65");
					put("warg3", "5_65");
					put("warg4", "5_65");
					put("warg5", "5_65");
					put("warwarg", "5_64");
					put("warwarg2", "5_64");
					// Isengard Axe: an [isengard] possession that is no mount, strength +2.  Cheated on, so its
					// "when you play" never happens.
					put("axe", "3_55");
					// Uruk-hai Sword: an [isengard] possession that is no mount, cost 1, borne by an Uruk-hai.
					put("sword", "1_160");
					// Gothmog's Warg: a mount that is not [isengard], cost 1.  Its bearer must be a [sauron] Orc.
					put("gothmog", "10_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofthePackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Strength of the Pack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Mounted [Isengard] Orcs are strength +1.
		 * While you can spot 2 mounted [isengard] Orcs, your [isengard] mounts are twilight cost -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pack");

		assertEquals("Strength of the Pack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneToTheSupportAreaItMakesMountedIsengardOrcsStrengthPlusOneAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pack = scn.GetShadowCard("pack");
		scn.MoveCardsToHand(pack);

		var wargrider = scn.GetShadowCard("wargrider");
		var isenrider = scn.GetShadowCard("isenrider");
		var flanker = scn.GetShadowCard("flanker");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(wargrider, isenrider, flanker, worker, savage, runner);
		// Two warg-riders on different mounts.
		scn.AttachCardsTo(wargrider, scn.GetShadowCard("warg1"));
		scn.AttachCardsTo(isenrider, scn.GetShadowCard("warwarg"));
		// An [isengard] Orc bearing a possession that is no mount.
		scn.AttachCardsTo(worker, scn.GetShadowCard("axe"));
		// A mounted [isengard] minion that is no Orc, and a mounted Orc that is not [isengard].  Neither could
		// be given a Warg by legal play.
		scn.AttachCardsTo(savage, scn.GetShadowCard("warg2"));
		scn.AttachCardsTo(runner, scn.GetShadowCard("warg3"));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		assertEquals(5 + 3, scn.GetStrength(wargrider));
		assertEquals(8 + 4, scn.GetStrength(isenrider));
		assertEquals(7, scn.GetStrength(flanker));
		assertEquals(5 + 2, scn.GetStrength(worker));
		assertEquals(5 + 3, scn.GetStrength(savage));
		assertEquals(5 + 3, scn.GetStrength(runner));
		assertTrue(scn.ShadowPlayAvailable(pack));

		scn.ShadowPlayCard(pack);
		assertEquals(13 - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pack);

		assertEquals(5 + 3 + 1, scn.GetStrength(wargrider));
		assertEquals(8 + 4 + 1, scn.GetStrength(isenrider));
		// A warg-rider with no mount.
		assertEquals(7, scn.GetStrength(flanker));
		assertEquals(5 + 2, scn.GetStrength(worker));
		assertEquals(5 + 3, scn.GetStrength(savage));
		assertEquals(5 + 3, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsengardMountsCostOneLessOnceASecondIsengardOrcIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pack = scn.GetShadowCard("pack");
		scn.MoveCardsToSupportArea(pack);

		var wargrider = scn.GetShadowCard("wargrider");
		var isenrider = scn.GetShadowCard("isenrider");
		var flanker = scn.GetShadowCard("flanker");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		var warwarg = scn.GetShadowCard("warwarg");
		var warwarg2 = scn.GetShadowCard("warwarg2");
		var warg4 = scn.GetShadowCard("warg4");
		var warg5 = scn.GetShadowCard("warg5");
		var sword = scn.GetShadowCard("sword");
		var gothmog = scn.GetShadowCard("gothmog");
		scn.MoveCardsToHand(warwarg, warwarg2, warg4, warg5, sword, gothmog);

		scn.StartGame();
		scn.MoveMinionsToTable(wargrider, isenrider, flanker, worker, savage, runner);
		// One mounted [isengard] Orc, and beside him everything that nearly counts as a second: two
		// warg-riders with no mount, an [isengard] Orc with a weapon, a mounted Uruk-hai and a mounted
		// [moria] Orc.
		scn.AttachCardsTo(wargrider, scn.GetShadowCard("warg1"));
		scn.AttachCardsTo(worker, scn.GetShadowCard("axe"));
		scn.AttachCardsTo(savage, scn.GetShadowCard("warg2"));
		scn.AttachCardsTo(runner, scn.GetShadowCard("warg3"));
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 20 set + 1 companion + 2 for the site.
		assertEquals(23, scn.GetTwilight());
		assertEquals(4, scn.GetTwilightCost(warwarg));
		assertEquals(4, scn.GetTwilightCost(warwarg2));
		assertEquals(3, scn.GetTwilightCost(warg4));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(gothmog));
		assertEquals(8, scn.GetStrength(isenrider));

		// The second Orc is mounted by legal play.  His mount is paid for in full: he is not mounted yet.
		scn.ShadowPlayCard(warwarg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(isenrider, flanker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wargrider, worker, savage, runner));
		scn.ShadowChooseCard(isenrider);
		assertAttachedTo(warwarg, isenrider);
		assertEquals(23 - 4, scn.GetTwilight());
		// 8 + 4 from the War-warg + 1 from Strength of the Pack.
		assertEquals(13, scn.GetStrength(isenrider));

		// Two mounted [isengard] Orcs: [isengard] mounts cost 1 less, and nothing else does.
		assertEquals(4 - 1, scn.GetTwilightCost(warwarg2));
		assertEquals(3 - 1, scn.GetTwilightCost(warg4));
		assertEquals(3 - 1, scn.GetTwilightCost(warg5));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(gothmog));

		// The Sword, an [isengard] possession that is no mount, goes on the Savage for its whole cost.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, savage);
		assertEquals(19 - 1, scn.GetTwilight());

		// The Flanker is the one warg-rider left without a mount.
		assertEquals(7, scn.GetStrength(flanker));
		scn.ShadowPlayCard(warg4);
		assertAttachedTo(warg4, flanker);
		assertEquals(18 - 2, scn.GetTwilight());
		assertEquals(7 + 3 + 1, scn.GetStrength(flanker));

		// Three mounted [isengard] Orcs: still 1 less, not 2.
		assertEquals(3 - 1, scn.GetTwilightCost(warg5));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersIsengardMountsAreNotMadeCheaper() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pack = scn.GetShadowCard("pack");
		scn.MoveCardsToSupportArea(pack);

		var wargrider = scn.GetShadowCard("wargrider");
		var isenrider = scn.GetShadowCard("isenrider");

		// A Warg in each player's hand.  The Free Peoples player's is his own Shadow card, held on his own turn:
		// he cannot play it this turn, so its cost can only be read.
		var warg = scn.GetShadowCard("warg4");
		var othersWarg = scn.GetFreepsCard("warg4");
		scn.MoveCardsToHand(warg, othersWarg);

		scn.StartGame();
		scn.MoveMinionsToTable(wargrider, isenrider);
		scn.AttachCardsTo(wargrider, scn.GetShadowCard("warg1"));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(warg));
		assertEquals(3, scn.GetTwilightCost(othersWarg));

		// A second [isengard] Orc is mounted: the mounts of the player whose condition it is cost 1 less.
		scn.AttachCardsTo(isenrider, scn.GetShadowCard("warg2"));
		assertEquals(3 - 1, scn.GetTwilightCost(warg));
		assertEquals(3, scn.GetTwilightCost(othersWarg));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
