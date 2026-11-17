package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hama", "17_99");

					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8

					put("runner1", "1_178");      // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("axe1", "1_190");         // Moria Axe: possession, strength +2 and damage +1 on a [moria] Orc; a skirmish ability used only on purpose
					put("axe2", "1_190");
					put("haradrim", "11_86");     // Invading Haradrim: [men] Man, strength 8; its trigger answers the Free Peoples player assigning it
					put("halberd", "18_63");      // Corsair Halberd: possession, strength +2 and damage +1 on a [men] minion; a Shadow ability used only on purpose
					put("balrog", "12_79");       // The Balrog, The Terror of Khazad-dum: strength 17, printed damage +1; its other text is for underground sites
					put("sword", "2_50");         // The Balrog's Sword: an ARTIFACT, strength +3 and damage +1 on The Balrog
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound: Frodo is strength 3 and takes his wounds
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void HamaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Háma, Northman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While the Ring-bearer is assigned to a skirmish, each minion skirmishing Háma loses strength bonuses
		 * 	and <b>damage</b> bonuses from possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hama");

		assertEquals("Háma", card.getBlueprint().getTitle());
		assertEquals("Northman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileTheRingBearerIsAssignedEachMinionSkirmishingHamaLosesTheBonusesOfItsPossessionsAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hama = scn.GetFreepsCard("hama");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");
		var haradrim = scn.GetShadowCard("haradrim");
		var halberd = scn.GetShadowCard("halberd");
		var balrog = scn.GetShadowCard("balrog");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCompanionsToTable(hama, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3, haradrim, balrog);
		scn.AttachCardsTo(runner1, axe1);
		scn.AttachCardsTo(runner2, axe2);
		scn.AttachCardsTo(haradrim, halberd);
		scn.AttachCardsTo(balrog, sword);
		assertEquals(5 + 2, scn.GetStrength(runner1));
		assertEquals(1, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertEquals(5 + 2, scn.GetStrength(runner2));
		assertEquals(8 + 2, scn.GetStrength(haradrim));
		assertEquals(1, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		assertEquals(17 + 3, scn.GetStrength(balrog));
		assertEquals(1 + 1, scn.GetKeywordCount(balrog, Keyword.DAMAGE));
		scn.SkipToAssignments();

		// Two minions of two cultures and races, and The Balrog, all on Háma; a minion with a possession on Aragorn.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hama, runner1}, new PhysicalCardImpl[]{aragorn, runner2},
				new PhysicalCardImpl[]{frodo, runner3});
		scn.ShadowAssignToMinions(hama, haradrim, balrog);

		// Aragorn's skirmish first, Frodo's waiting: the minion skirmishing Aragorn keeps its axe's bonuses, and
		// those assigned to Háma, not yet skirmishing him, keep theirs.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(5 + 2, scn.GetStrength(runner2));
		assertEquals(1, scn.GetKeywordCount(runner2, Keyword.DAMAGE));
		assertEquals(5 + 2, scn.GetStrength(runner1));
		assertEquals(1, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertEquals(8 + 2, scn.GetStrength(haradrim));
		assertEquals(1, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		// 8 against 7
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(hama);

		// Frodo's skirmish still waits.  What possessions give the minions skirmishing Háma is gone.
		assertTrue(scn.IsCharAssigned(frodo));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(0, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(haradrim));
		assertEquals(0, scn.GetKeywordCount(haradrim, Keyword.DAMAGE));
		// The Balrog keeps what its artifact gives it, and its own printed damage +1.
		assertEquals(17 + 3, scn.GetStrength(balrog));
		assertEquals(1 + 1, scn.GetKeywordCount(balrog, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceTheRingBearersSkirmishIsOverTheMinionSkirmishingHamaKeepsItsBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hama = scn.GetFreepsCard("hama");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var axe1 = scn.GetShadowCard("axe1");
		scn.MoveCompanionsToTable(hama);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.AttachCardsTo(runner1, axe1);
		assertEquals(5 + 2, scn.GetStrength(runner1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hama, runner1}, new PhysicalCardImpl[]{frodo, runner2});

		// Frodo's skirmish first: 3 against 5.
		scn.FreepsResolveSkirmish(frodo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsResolveSkirmish(hama);
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(5 + 2, scn.GetStrength(runner1));
		assertEquals(1, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundCompanionWhoIsNotTheRingBearerBeingAssignedLeavesTheMinionItsBonuses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hama = scn.GetFreepsCard("hama");
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var axe1 = scn.GetShadowCard("axe1");
		scn.MoveCompanionsToTable(hama, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.AttachCardsTo(runner1, axe1);
		assertEquals(5 + 2, scn.GetStrength(runner1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hama, runner1}, new PhysicalCardImpl[]{sam, runner2});

		scn.FreepsResolveSkirmish(hama);
		assertTrue(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(5 + 2, scn.GetStrength(runner1));
		assertEquals(1, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
