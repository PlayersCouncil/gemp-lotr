package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_125_Tests
{

	// Frodo 1_290 is the Ring-bearer: a Hobbit, resistance 10, Ring-bound by rule.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("measure", "12_125");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, resistance 5, Ring-bound by rule; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally (resistance 0); a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 is the Ring-bearer: a Dwarf, so no Hobbit is in play unless a test lays one.
	// Resistance 4, +1 for his damage +1: 5.  His other text acts at the start of a skirmish he is in, and no
	// skirmish is fought here.
	protected VirtualTableScenario GetDwarfRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("measure", "12_125");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6; a maneuver ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, resistance 6, unbound; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, Ring-bound, resistance 6 (cheated in); an assignment ability only
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, unbound, resistance 6 (cheated in); a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MeasureofComfortStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Measure of Comfort
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot a Hobbit, each unbound companion is resistance +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("measure");

		assertEquals("Measure of Comfort", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoHobbitNothingChangesUntilAHobbitCompanionIsPlayedAndThenEachUnboundCompanionIsResistancePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarfRingBearerScenario();

		var measure = scn.GetFreepsCard("measure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToSupportArea(measure);
		scn.MoveCardsToHand(merry);

		scn.StartGame();

		// No Hobbit in play: Smeagol is a [shire] companion but no Hobbit.
		assertInZone(Zone.SUPPORT, measure);
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(smeagol));

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		// Aragorn, a [gondor] Man, and Merry, a [shire] Hobbit, are unbound: 6 + 1.
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(merry));
		// The Ring-bearer and Smeagol are Ring-bound.
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(6, scn.GetResistance(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedBesideHobbitsItReachesNoRingBoundCompanionAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var measure = scn.GetFreepsCard("measure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(measure);

		scn.StartGame();

		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(bounder));

		scn.FreepsPlayCard(measure);
		assertInZone(Zone.SUPPORT, measure);
		assertEquals(7, scn.GetResistance(aragorn));
		// Frodo, the Ring-bearer, and Sam are Ring-bound; Bounder is an ally, and only companions are unbound.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetResistance(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitAllyIsAHobbitToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarfRingBearerScenario();

		var measure = scn.GetFreepsCard("measure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(measure);

		scn.StartGame();
		assertEquals(6, scn.GetResistance(aragorn));

		scn.MoveAlliesToTable(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetResistance(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitOfAnotherCultureIsAHobbitToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarfRingBearerScenario();

		var measure = scn.GetFreepsCard("measure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(measure);

		scn.StartGame();
		assertEquals(6, scn.GetResistance(aragorn));

		// Pippin, Trollslayer is a [gondor] Hobbit, and unbound himself.
		scn.MoveCompanionsToTable(pippin);
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(pippin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayersHobbitMinionIsAHobbitToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarfRingBearerScenario();

		var measure = scn.GetFreepsCard("measure");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetRingBearer();
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(measure);

		scn.StartGame();
		assertEquals(6, scn.GetResistance(aragorn));

		// A race word with no "your" names no side (Q-0058): the opponent's Hobbit is spotted too.
		scn.MoveMinionsToTable(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertEquals(7, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
