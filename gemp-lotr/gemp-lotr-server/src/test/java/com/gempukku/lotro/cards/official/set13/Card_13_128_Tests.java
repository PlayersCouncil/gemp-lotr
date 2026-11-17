package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hama", "13_128");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Heavy Chain: a Free Peoples possession (armor) on a Man; it changes no number
					put("chain", "4_278");

					// Spider Poison: a [gollum] possession in the Shadow player's support area; its texts need Shelob
					// or an assignment of its bearer
					put("poison", "8_28");
					// Moria Axe: a [moria] hand weapon on a [moria] Orc, strength +2 and damage +1; its skirmish
					// ability is used only on purpose
					put("axe", "1_190");
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; its only text is on its play
					// Ithil Stone: a [sauron] ARTIFACT in the support area; its optional draw at the start of the
					// Shadow phase is declined by the skippers, its Shadow ability used only on purpose
					put("stone", "9_47");
					// Relics of Moria: a [moria] CONDITION in the support area; its Shadow ability is used only on purpose
					put("relics", "1_195");

					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("pursuer", "4_190");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HamaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Háma, Captain of the King's Guard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Háma is strength +1 for each Shadow possession you can spot.<br>Each time Háma wins a skirmish, you may discard a possession from play.
		*/

		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");

		assertEquals("Háma", hama.getBlueprint().getTitle());
		assertEquals("Captain of the King's Guard", hama.getBlueprint().getSubtitle());
		assertTrue(hama.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, hama.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, hama.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, hama.getBlueprint().getCardType());
		assertEquals(Race.MAN, hama.getBlueprint().getRace());
		assertEquals(2, hama.getBlueprint().getTwilightCost());
		assertEquals(6, hama.getBlueprint().getStrength());
		assertEquals(3, hama.getBlueprint().getVitality());
		assertEquals(6, hama.getBlueprint().getResistance());
	}

	@Test
	public void AShadowPossessionPlayedMakesHamaStrongerByOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		scn.MoveCompanionsToTable(hama);

		var axe = scn.GetShadowCard("axe");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCardsToHand(axe);

		scn.StartGame();

		// No Shadow possession in play.
		assertInHand(axe);
		assertEquals(6, scn.GetStrength(hama));

		scn.MoveMinionsToTable(scout);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(axe);
		assertEquals(6, scn.GetStrength(hama));

		// The Scout is the one [moria] Orc: the Axe goes on him unasked.
		scn.ShadowPlayCard(axe);
		assertAttachedTo(axe, scout);
		assertEquals(7, scn.GetStrength(hama));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HamaCountsEachShadowPossessionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(hama, guard);
		scn.AttachCardsTo(guard, chain);

		var poison = scn.GetShadowCard("poison");
		var axe = scn.GetShadowCard("axe");
		var scout = scn.GetShadowCard("scout");
		var stone = scn.GetShadowCard("stone");
		var relics = scn.GetShadowCard("relics");
		scn.MoveMinionsToTable(scout);
		scn.AttachCardsTo(scout, axe);
		scn.MoveCardsToSupportArea(poison, stone, relics);

		scn.StartGame();

		assertAttachedTo(chain, guard);
		assertAttachedTo(axe, scout);
		assertInZone(Zone.SUPPORT, poison, stone, relics);

		// 6 + 1 for the [gollum] possession in the support area + 1 for the borne [moria] one.  Not the Shadow
		// artifact, the Shadow condition, or the Free Peoples possession; and only Háma is stronger.
		assertEquals(8, scn.GetStrength(hama));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeHamaWinsHeMayDiscardAPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(hama, guard);
		scn.AttachCardsTo(guard, chain);

		var poison = scn.GetShadowCard("poison");
		var axe = scn.GetShadowCard("axe");
		var scout = scn.GetShadowCard("scout");
		var stone = scn.GetShadowCard("stone");
		var relics = scn.GetShadowCard("relics");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();

		scn.MoveMinionsToTable(scout, pursuer);
		scn.AttachCardsTo(scout, axe);
		scn.MoveCardsToSupportArea(poison, stone, relics);
		scn.SkipToAssignments();
		assertAttachedTo(chain, guard);
		assertAttachedTo(axe, scout);
		assertInZone(Zone.SUPPORT, poison, stone, relics);

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{hama, pursuer},
				new PhysicalCardImpl[]{guard, scout});
		scn.FreepsResolveSkirmish(hama);
		// 6 + 2 Shadow possessions against 5.
		assertEquals(8, scn.GetStrength(hama));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hama));
		scn.FreepsAcceptOptionalTrigger();
		// A possession of either side, borne or in the support area; not the artifact or the condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, poison, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stone, relics));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);
		assertInDiscard(axe);
		assertInZone(Zone.SUPPORT, poison, stone, relics);
		assertAttachedTo(chain, guard);
		assertEquals(7, scn.GetStrength(hama));

		// The Guard (6) and the Scout (6, the Axe gone) tie: the Guard loses and takes a wound.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));

		// The fierce Pursuer comes back for Háma, who wins again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(hama, pursuer);
		scn.FreepsResolveSkirmish(hama);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hama));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(poison, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stone, relics));
		scn.FreepsChooseCard(chain);
		assertInDiscard(axe, chain);
		assertInZone(Zone.SUPPORT, poison, stone, relics);
		assertEquals(7, scn.GetStrength(hama));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheDiscardMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		scn.MoveCompanionsToTable(hama);

		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(poison);
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, poison);

		scn.FreepsAssignToMinions(hama, savage);
		scn.FreepsResolveSkirmish(hama);
		assertEquals(7, scn.GetStrength(hama));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hama));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, poison);
		assertEquals(7, scn.GetStrength(hama));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherCompanionWinsNorWhenHamaLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(hama, guard);

		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToSupportArea(poison);
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, poison);

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{hama, troop});

		// The Guard (6) beats the Savage (5).
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hama));
		assertInZone(Zone.SUPPORT, poison);

		// Háma (7) loses to the Troop (13), is not overwhelmed, and lives.
		scn.FreepsResolveSkirmish(hama);
		assertEquals(7, scn.GetStrength(hama));
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hama));
		assertEquals(1, scn.GetWoundsOn(hama));
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoPossessionInPlayHisWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		scn.MoveCompanionsToTable(hama);

		var savage = scn.GetShadowCard("savage");
		var relics = scn.GetShadowCard("relics");

		scn.StartGame();

		// A Shadow condition, and no possession of either side.
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(relics);
		scn.SkipToAssignments();
		assertInZone(Zone.SUPPORT, relics);

		scn.FreepsAssignToMinions(hama, savage);
		scn.FreepsResolveSkirmish(hama);
		assertEquals(6, scn.GetStrength(hama));
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hama));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SUPPORT, relics);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
