package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "3_8");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3; has site 3's text only while that site is on the path
					put("erestor", "3_14");     // Erestor, Chief Advisor to Elrond: [elven] Elf ally, home site 3; answers only [sauron] wounds
					put("galdor", "3_18");      // Galdor, Councilor From the West: [elven] Elf ally, home site 3, cost 2; to play, spot an Elf
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
		 * Name: Arwen, Lady Undómiel
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
		 * Game Text: While you can spot 3 [elven] allies whose home is site 3, each minion skirmishing Arwen is strength -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Lady Undómiel", card.getBlueprint().getSubtitle());
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
	public void WithAThirdElvenAllyOfSite3PlayedEachMinionSkirmishingArwenIsStrengthMinus3AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var erestor = scn.GetFreepsCard("erestor");
		var galdor = scn.GetFreepsCard("galdor");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(silinde, erestor);
		scn.MoveCardsToHand(galdor);

		scn.StartGame();
		assertInHand(galdor);

		// The third [elven] ally of home site 3 is played.
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
		// Both minions skirmishing her, an Uruk-hai and an Orc, one assigned by each player.
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(2, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(6, scn.GetStrength(arwen));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		// A minion skirmishing another companion.
		assertTrue(scn.IsCharSkirmishingAgainst(runner2, aragorn));
		assertEquals(5, scn.GetStrength(runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TwoElvenAlliesOfSite3AndTheNearMissesBesideThemDoNotWeakenAMinionSkirmishingArwen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var erestor = scn.GetFreepsCard("erestor");
		var orophin = scn.GetFreepsCard("orophin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var galadriel = scn.GetFreepsCard("galadriel");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde, erestor, orophin, thrarin);

		scn.StartGame();
		// Galadriel's own start-of-turn trigger is kept out of the way by putting her down after it.
		scn.MoveAlliesToTable(galadriel);
		assertInZone(Zone.SUPPORT, galadriel);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// Silinde and Erestor: 2.  Not Orophin (home site 6), Thrarin (an ally of home site 3 who is not [elven]),
		// Galadriel (home site 3 of the Hobbit block) or Arwen herself (an [elven] companion, no ally).
		assertTrue(scn.IsCharSkirmishingAgainst(savage, arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
