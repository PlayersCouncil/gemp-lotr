package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("honed", "8_6");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6, damage +1; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, strength 3, no damage bonus of his own; a fellowship ability only
					put("dwarfguard", "1_7");   // Dwarf Guard: Dwarf companion, strength 4, no damage bonus; "To play, spot a Dwarf"
					put("lord", "1_21");        // Lord of Moria: support-area condition, "Each Dwarf is damage +1."
					put("ring", "9_8");         // Ring of Guile: artifact on a Dwarf, strength +1, bearer is damage +1; a maneuver ability used only on purpose
					put("huorn", "9_25");       // Huorn: a [gandalf] companion, strength 6, who is damage +1 and no Dwarf; a response that needs an Ent

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HonedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Honed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a Dwarf who is damage +X to make him strength +X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("honed");

		assertEquals("Honed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoIsMadeStrengthPlusTwoForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honed = scn.GetFreepsCard("honed");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var lord = scn.GetFreepsCard("lord");
		var huorn = scn.GetFreepsCard("huorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, huorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(honed);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, savage1},
				new PhysicalCardImpl[]{huorn, savage2});
		scn.FreepsResolveSkirmish(gimli);

		// Gimli: printed +1 and Lord of Moria's +1.  Grimir: Lord of Moria's +1.  Huorn: his own +1, and no Dwarf.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsPlayAvailable(honed));
		scn.FreepsPlayCard(honed);

		// A Dwarf with a damage bonus, companion or ally, in the skirmish or not; not Huorn, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(huorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// 6 + 2.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(huorn));
		assertInDiscard(honed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +2 when the skirmish resolves: 8 beats 5, and the Savage takes 1 wound and 2 more for damage +2.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInDiscard(savage1);

		// No duration is printed: the bonus is gone in the next skirmish phase.
		scn.FreepsResolveSkirmish(huorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void XIsTheDamageBonusOfTheDwarfChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honed = scn.GetFreepsCard("honed");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(honed);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(grimir));
		scn.FreepsPlayCard(honed);
		scn.FreepsChooseCard(grimir);

		// Grimir is damage +1, not the +2 Gimli has: 3 + 1.
		assertEquals(4, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(honed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoDwarfThoughACompanionHasADamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honed = scn.GetFreepsCard("honed");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveCardsToHand(honed);
		assertInHand(honed);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(huorn, savage1);
		scn.FreepsResolveSkirmish(huorn);

		// Huorn is damage +1, and a Tree.
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertInHand(honed);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(honed));
	}

	@Test
	public void CannotBePlayedWithADwarfWhoHasNoDamageBonusUntilHeHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honed = scn.GetFreepsCard("honed");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var huorn = scn.GetFreepsCard("huorn");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(honed);
		assertInHand(honed);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(huorn, savage1);
		scn.FreepsResolveSkirmish(huorn);

		// Grimir is a Dwarf of no damage bonus; Huorn has the bonus and is no Dwarf.
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		assertInHand(honed);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(honed));

		// The same Dwarf with a damage bonus of his own is the Dwarf the event wants.
		scn.AttachCardsTo(grimir, ring);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(honed));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ADwarfWithNoDamageBonusIsNotOfferedBesideDwarvesWhoHaveOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honed = scn.GetFreepsCard("honed");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var ring = scn.GetFreepsCard("ring");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(gimli, dwarfguard);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, ring);
		scn.MoveCardsToHand(honed);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(grimir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(dwarfguard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(dwarfguard));
		scn.FreepsPlayCard(honed);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarfguard));
		scn.FreepsChooseCard(gimli);

		// 6 + 1.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(dwarfguard));
		assertInDiscard(honed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
