package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "1_4");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no damage bonus
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; his skirmish ability is used only on purpose
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, strength 3, vitality 3; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man, strength 8, vitality 4; a maneuver ability only

					put("savage1", "1_151");      // Uruk Savage: strength 5, vitality 3, damage +1
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattleFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Battle Fury
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert a Dwarf to make that Dwarf strength +3 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Battle Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsADwarfToMakeHimStrengthPlus3AndDamagePlus1ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToHand(fury);
		scn.MoveCompanionsToTable(guard, gimli);

		scn.StartGame();
		assertInHand(fury);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage1}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(fury));

		scn.FreepsPlayCard(fury);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		scn.FreepsChooseCard(guard);

		// The Dwarf who exerted, and he alone, is strength +3 and damage +1.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertInDiscard(fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The last decision of this skirmish: the bonuses are still on him.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));

		// 7 against 5: the Guard wins, where at 4 he would have lost. 1 wound for losing + 1 for damage +1.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(guard));

		// A new skirmish phase: the bonuses went with the last one.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage2));
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
	}

	@Test
	public void AnyDwarfWhoCanExertPaysAndOnlyThatDwarfIsStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToHand(fury);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInHand(fury);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage1}, new PhysicalCardImpl[]{guard, savage2});
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharAssignedAgainst(guard, savage2));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(3, scn.GetStrength(grimir));
		assertFalse(scn.HasKeyword(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPlayCard(fury);
		// The Dwarf in the skirmish, the Dwarf waiting for his own and the Dwarf ally can each exert;
		// Aragorn and Frodo could exert too, but are no Dwarves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(6, scn.GetStrength(grimir));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		// The others neither pay nor gain.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(4, scn.GetStrength(guard));
		assertFalse(scn.HasKeyword(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCardsToHand(fury);
		scn.MoveCompanionsToTable(guard, aragorn);
		// Vitality 2 with 1 wound: exhausted, so the only Dwarf cannot exert.
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		assertInHand(fury);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, savage1);

		// Aragorn, unwounded, could exert, but is no Dwarf.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsExhausted(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(fury);
		assertFalse(scn.FreepsPlayAvailable(fury));
	}
}
