package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dominator", "12_152");
					put("tracking", "15_171");  // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; Hunter 1 is its whole text
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, strength 5; Hunter 6 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; Damage +1 is its whole text
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] Wizard, strength 8; each companion is resistance -1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4, resistance 6
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf, resistance 6; a skirmish ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound [shire] Hobbit, resistance 5
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally, resistance 0; a fellowship ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukDominatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Uruk Dominator
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Exert this minion to make it strength +1 for each [uruk-hai] Uruk-hai you spot (or +2 for each if you can spot a companion who has resistance 3 or less).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dominator");

		assertEquals("Uruk Dominator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsHimToBeStrengthPlusOneForEachUrukHaiUrukHaiSpottedForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dominator = scn.GetShadowCard("dominator");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(dominator, tracking, butcher, guard, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, dominator}, new PhysicalCardImpl[]{gimli, tracking});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Under Saruman every companion is resistance -1: none is down to 3.
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(9, scn.GetResistance(scn.GetRingBearer()));
		assertEquals(0, scn.GetWoundsOn(dominator));
		assertEquals(8, scn.GetStrength(dominator));

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(dominator));
		scn.ShadowUseCardAction(dominator);
		assertEquals(1, scn.GetWoundsOn(dominator));
		// The player's count: himself, Tracking Uruk and the Butcher, and not the [sauron] Uruk-hai or the Wizard.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(3, (int) scn.ShadowGetChoiceDefault());
		assertEquals(8, scn.GetStrength(dominator));
		scn.ShadowChoose("3");

		assertEquals(11, scn.GetStrength(dominator));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(saruman));

		// 11 beats Aragorn's 8; he takes 2 wounds (damage +1).
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The next skirmish is a phase of its own: the bonus is gone, and the ability is his to use here too.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(dominator));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(dominator));
		scn.ShadowUseCardAction(dominator);
		assertEquals(2, scn.GetWoundsOn(dominator));
		scn.ShadowChoose("2");
		assertEquals(10, scn.GetStrength(dominator));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoForEachWhileARingBoundHobbitHasResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dominator = scn.GetShadowCard("dominator");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(dominator, tracking);

		assertEquals(2, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(4, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, dominator);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(dominator));
		scn.FreepsPass();
		scn.ShadowUseCardAction(dominator);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// 8 + 2 for each of 2.
		assertEquals(12, scn.GetStrength(dominator));
		assertEquals(14, scn.GetStrength(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusTwoForEachWhileAnUnboundManHasResistance3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dominator = scn.GetShadowCard("dominator");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(dominator, tracking);

		assertEquals(3, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(scn.GetRingBearer()));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, dominator);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(dominator));
		scn.FreepsPass();
		scn.ShadowUseCardAction(dominator);
		scn.ShadowChoose("2");

		assertEquals(12, scn.GetStrength(dominator));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsStrengthPlusOneForEachWithEveryCompanionAtResistance4OrMoreBesideAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dominator = scn.GetShadowCard("dominator");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(dominator, tracking);

		assertEquals(2, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetResistance(scn.GetRingBearer()));
		// The ally's resistance, and the minions', is 0.
		assertEquals(0, scn.GetResistance(elrond));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, dominator);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(dominator));
		scn.FreepsPass();
		scn.ShadowUseCardAction(dominator);
		scn.ShadowChoose("2");

		assertEquals(10, scn.GetStrength(dominator));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dominator = scn.GetShadowCard("dominator");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(dominator, tracking);
		scn.AddWoundsToChar(dominator, 2);

		assertEquals(2, scn.GetWoundsOn(dominator));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, dominator);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dominator));
		assertEquals(2, scn.GetWoundsOn(dominator));
		assertEquals(8, scn.GetStrength(dominator));
	}
}
