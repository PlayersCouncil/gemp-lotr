package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_050_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horde", "55_50");
					put("horde2", "55_50");     // a second copy: the erratum is not unique, and each copy reads its own vitality

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text that matters here
					put("troop", "1_177");      // Goblin Patrol Troop: another minion, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulHordeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Foul Horde
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Warg-rider</b>.
		 * While Foul Horde is not exhausted, it is <b>fierce</b>.
		 * While Foul Horde has at least 3 vitality, it is <b>ambush</b> (1).
		 * While Foul Horde has at least 4 vitality, it is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horde");

		assertEquals("Foul Horde", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WARG_RIDER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysForFourPlusRoamingAndUnwoundedIsFierceAndAmbushButNotDamage() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		scn.MoveCardsToHand(horde);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 for Frodo + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(horde));

		scn.ShadowPlayCard(horde);
		// 4 to play + 2 roaming (site 4, played at site 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, horde);

		// No wounds: 3 vitality left.
		assertEquals(0, scn.GetWoundsOn(horde));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(horde, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyIsFierceUntilExhaustedAndAmbushOnlyAtThreeVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var horde2 = scn.GetShadowCard("horde2");

		scn.StartGame();
		scn.MoveMinionsToTable(horde, horde2);

		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(horde2, Keyword.AMBUSH));

		// 2 vitality left: still fierce, no longer ambush.
		scn.AddWoundsToChar(horde2, 1);
		assertFalse(scn.IsExhausted(horde2));
		assertTrue(scn.HasKeyword(horde2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(horde2, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(horde2, Keyword.DAMAGE));

		// 1 vitality left: exhausted, so not fierce either.
		scn.AddWoundsToChar(horde2, 1);
		assertTrue(scn.IsExhausted(horde2));
		assertFalse(scn.HasKeyword(horde2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(horde2, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(horde2, Keyword.DAMAGE));

		// The other copy, unwounded, is untouched by its twin's wounds.
		assertEquals(0, scn.GetWoundsOn(horde));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(horde, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DamagePlusOneOnlyWithFourVitalityLeftCountingWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var horde2 = scn.GetShadowCard("horde2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(horde, horde2, troop);
		// Only the first copy is made vitality 4, and another minion beside it.
		scn.ApplyAdHocModifier(new VitalityModifier(null, horde, null, 1));
		scn.ApplyAdHocModifier(new VitalityModifier(null, troop, null, 1));

		// 4 vitality left: damage +1, ambush (1) and fierce.
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		// The unboosted copy has 3 and no damage bonus; the other minion, at 4, gets nothing from either copy.
		assertFalse(scn.HasKeyword(horde2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(troop, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(troop, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// Vitality 4 with 1 wound: 3 left, so no damage bonus, but still ambush (1).
		scn.AddWoundsToChar(horde, 1);
		assertFalse(scn.HasKeyword(horde, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));

		// 2 left: fierce alone.
		scn.AddWoundsToChar(horde, 1);
		assertFalse(scn.HasKeyword(horde, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(horde, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));

		// 1 left: exhausted, nothing.
		scn.AddWoundsToChar(horde, 1);
		assertTrue(scn.IsExhausted(horde));
		assertFalse(scn.HasKeyword(horde, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(horde, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(horde, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AmbushIsOfferedAtThreeVitalityAndAWoundThatExhaustsItKeepsItOutOfTheFierceSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horde = scn.GetShadowCard("horde");
		var horde2 = scn.GetShadowCard("horde2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(horde, horde2);
		scn.AddWoundsToChar(horde2, 1);
		// Both companions beat a strength 9 minion: Aragorn 8 + 4, the Guard 4 + 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 8));

		scn.SkipToAssignments();
		int twilight = scn.GetTwilight();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, horde}, new PhysicalCardImpl[]{guard, horde2});

		// Only the unwounded copy has 3 vitality and ambush.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(horde));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(horde2));
		scn.ShadowChooseOptionalTrigger(horde);
		assertEquals(twilight + 1, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();

		// The first is on 1 wound and still fierce; the second, on 2, is exhausted and is not.
		assertEquals(1, scn.GetWoundsOn(horde));
		assertEquals(2, scn.GetWoundsOn(horde2));
		assertTrue(scn.HasKeyword(horde, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(horde2, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(horde));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(horde2));
		scn.FreepsAssignToMinions(aragorn, horde);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, horde));
		assertFalse(scn.IsCharAssigned(horde2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
