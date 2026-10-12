package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_008_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "53_8");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3
					put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: [elven] Elf ally, home site 3
					put("galdor", "3_18");      // Galdor, Councilor From the West: [elven] Elf ally, home site 3, cost 2; to play, spot an Elf
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: [elven] Elf ally, home site 3
					put("saelbeth", "3_25");    // Saelbeth, Elven Councilor: [elven] Elf ally, home site 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, home site 6
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, home site 3
					put("galadriel", "32_13");  // Galadriel, Elven Lady: [elven] Elf ally, home site 3 of the Hobbit block
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5; its play trigger is not met when cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Arwen, Lady Undomiel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: Each minion skirmishing Arwen is strength -1 for each home 3 [elven] ally you can spot (limit -4).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Lady Undomiel", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void EachMinionSkirmishingArwenIsStrengthMinus1ForEachElvenAllyOfSite3AndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var galdor = scn.GetFreepsCard("galdor");
		var orophin = scn.GetFreepsCard("orophin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var galadriel = scn.GetFreepsCard("galadriel");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(silinde, orophin, thrarin);
		scn.MoveCardsToHand(galdor);

		scn.StartGame();
		// Galadriel's own start-of-turn trigger is kept out of the way by putting her down after it.
		scn.MoveAlliesToTable(galadriel);

		// The second [elven] ally of home site 3 is played.
		scn.FreepsPlayCard(galdor);
		assertInZone(Zone.SUPPORT, galdor);

		scn.MoveMinionsToTable(savage, runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.ShadowAssignToMinions(arwen, runner1);

		// Assigned to Arwen, but her skirmish has not begun.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));

		scn.FreepsResolveSkirmish(arwen);
		// Silinde and Galdor: 2.  Not Orophin (home site 6), Thrarin (an ally of home site 3 who is not [elven])
		// or Galadriel (home site 3 of the Hobbit block).
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(3, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(runner2, aragorn));
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void FiveElvenAlliesOfSite3MakeEachMinionStrengthMinus4AndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var erestor = scn.GetFreepsCard("erestor");
		var galdor = scn.GetFreepsCard("galdor");
		var golradir = scn.GetFreepsCard("golradir");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde, erestor, galdor, golradir, saelbeth);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.ShadowAssignToMinions(arwen, runner1);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner1));

		scn.FreepsResolveSkirmish(arwen);
		// Five allies, limit -4, for each of the two minions.
		assertEquals(1, scn.GetStrength(savage));
		assertEquals(1, scn.GetStrength(runner1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
