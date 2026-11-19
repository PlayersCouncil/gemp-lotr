package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_212_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "7_212");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: another Nazgûl; damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, no text; overwhelms anyone of strength 6 or less
					put("troop2", "1_177");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a companion
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion, strength 6, vitality 3
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion, strength 6 (+3 only against a Nazgûl)
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer, cost 2, no requirement to play
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally; heals allies whose home is site 3 at the start of the turn
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow him
					// to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enquea", "7_212");
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, no text but its requirement to play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("despair", "7_148");    // Fierce in Despair: a Shadow Regroup event, twilight 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man minion; his text needs 2 burdens
					put("axeman2", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireEnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Enquëa, Faster Than Winds
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>While you can spot 6 companions, Úlairë Enquëa is <b>damage +1</b>.<br>Each time a companion is killed, you may exert Úlairë Enquëa to exert the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Faster Than Winds", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SheIsDamagePlusOneOnceASixthCompanionIsPlayedAndAnotherNazgulIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var nelya = scn.GetShadowCard("nelya");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"),
				scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("elrond"));
		scn.MoveCardsToHand(sam);
		assertInHand(sam);

		scn.StartGame();
		// Laid early in the turn for the reading (Q-0228).
		scn.MoveMinionsToTable(enquea, nelya);
		scn.FreepsRefreshDecision();

		// Frodo and four companions, and Elrond, an ally: five companions.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetKeywordCount(enquea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));

		scn.FreepsPlayCard(sam);

		// The sixth companion.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetKeywordCount(enquea, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(nelya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeACompanionIsKilledSheMayExertToExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, arwen, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{arwen, troop2},
				new PhysicalCardImpl[]{aragorn, enquea});

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		// A Dwarf overwhelmed: 13 is more than twice 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();

		// She exerts, and so does the Ring-bearer; Sam, Ring-bound but not the Ring-bearer, does not.
		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		// An Elf overwhelmed the same way: offered again.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, arwen);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DecliningTheTriggerExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop1 = scn.GetShadowCard("troop1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{aragorn, enquea});

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithTheRingBearerExhaustedSheExertsAndTheRingBearerTakesNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop1 = scn.GetShadowCard("troop1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, troop1);
		// Vitality 4 with 3 wounds: Frodo cannot exert, and one wound more would kill him.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{aragorn, enquea});

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(enquea));
		// The trigger has a cost, so it is offered though its effect will do nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(enquea));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheTriggerIsNotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop1 = scn.GetShadowCard("troop1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, troop1);
		// Vitality 4 with 3 wounds: she cannot exert.
		scn.AddWoundsToChar(enquea, 3);
		assertEquals(3, scn.GetWoundsOn(enquea));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{aragorn, enquea});

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(3, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllyKilledDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var troop1 = scn.GetShadowCard("troop1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, troop1}, new PhysicalCardImpl[]{aragorn, enquea});

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// An ally overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ACompanionDiscardedFromPlayIsNotKilledAndDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var enquea = scn.GetShadowCard("enquea");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.MoveCardsToHand(despair);
		assertInHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Laid after the skirmishes, so that nothing on the way answers them.
		scn.MoveMinionsToTable(enquea);
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(enquea));
		int frodoWounds = scn.GetWoundsOn(frodo);
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		scn.ShadowChooseCard(guard);

		// Discarded from play is not killed (CR 4.2 "killed"): the dead pile stays empty and nothing is offered.
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(enquea));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(frodoWounds, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
