package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_015_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("focus", "65_15");

					put("legolas", "15_18");    // Legolas, Fleet-footed Hunter: [elven] Elf, strength 6, hunter 1; a regroup ability never used here
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven] Dwarf, hunter 3, damage +1 and nothing else
					put("aragorn", "18_38");    // Aragorn, Heir to the Throne of Gondor: [gondor] Man, strength 8, hunter 2; defender +1 at times
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] Dwarf who is no hunter
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no hunter; a skirmish ability never used here
					put("gamling", "15_126");   // Gamling, The Old: a [rohan] Man with hunter 1; stronger while 3 hunters can be spotted
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, strength 6; stronger only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] Elf ally; a fellowship ability never used here
					put("songs", "1_61");       // Songs of the Blessed Realm: another [elven] support-area condition; answers only an [elven] tale played

					put("uruk", "15_168");      // Searching Uruk: fierce, hunter 1, strength 5, vitality 2; stronger only beside other [uruk-hai] hunters
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FocusStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Focus
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time Legolas wins a skirmish, you may heal a hunter [dwarven] Dwarf or hunter [gondor] Man.
		 * 	<b>Skirmish:</b> Discard this condition to make an [elven] Elf strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("focus");

		assertEquals("Focus", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeLegolasWinsASkirmishHisOwnerMayHealAHunterDwarvenDwarfOrAHunterGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var gamling = scn.GetFreepsCard("gamling");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(legolas, gimli, aragorn, guard, boromir, gamling);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("focus"));
		// Legolas is a hunter too, of another culture and another race, and wounded like the rest.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(gamling, 1);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, uruk);
		scn.FreepsResolveSkirmish(legolas);

		// Hunter against hunter, neither has a bonus: 6 against 5
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(5, scn.GetStrength(uruk));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(uruk));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn));
		// No hunter; no hunter; a hunter Man of another culture; a hunter Elf of another culture.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, boromir, gamling, legolas));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gamling));
		assertEquals(1, scn.GetWoundsOn(legolas));

		// The Uruk is fierce and still alive: Legolas wins a second skirmish and is asked a second time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(legolas, uruk);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DISCARD, uruk);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheHealLeavesEveryWoundWhereItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, gimli, aragorn);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("focus"));
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		// 6 + 1 for a hunter against a minion that is none, against 5
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAnotherElfWinsASkirmishOrWhenLegolasLosesOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen, gimli, aragorn);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("focus"));
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{legolas, troop});

		// Arwen, another [elven] Elf, wins: 6 against 5.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// Legolas loses: 6 + 1 for a hunter against a minion that is none, against 13.
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenLegolasWinsAndNoHunterDwarvenDwarfOrHunterGondorManIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var gamling = scn.GetFreepsCard("gamling");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, gimli, aragorn, guard, gamling);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("focus"));
		// The two the text could heal are unwounded; a Dwarf who is no hunter and a hunter of another
		// culture are wounded.
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(gamling, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, savage);
		scn.FreepsResolveSkirmish(legolas);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gamling));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsFocusToMakeAnElvenElfStrengthPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var focus = scn.GetFreepsCard("focus");
		var songs = scn.GetFreepsCard("songs");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		// Songs of the Blessed Realm is an [elven] card that is no Elf; Aragorn is a companion who is none.
		scn.MoveCardsToSupportArea(focus, songs);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment ability.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(focus));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{legolas, savage}, new PhysicalCardImpl[]{arwen, savage2});
		scn.FreepsResolveSkirmish(legolas);

		assertTrue(scn.FreepsActionAvailable(focus));
		// Legolas: 6 + 1 for a hunter against a minion that is none
		assertEquals(7, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(celeborn));

		scn.FreepsUseCardAction(focus);

		assertInZone(Zone.DISCARD, focus);
		assertInZone(Zone.SUPPORT, songs);
		// Legolas in the skirmish, Arwen assigned to the next one, and an ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(songs, aragorn, frodo, savage, savage2));
		scn.FreepsChooseCard(arwen);

		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(celeborn));

		// Still +2 when the skirmish it was given in is about to be resolved,
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(legolas));
		assertEquals(8, scn.GetStrength(arwen));

		// and gone in the next skirmish, Arwen's own.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.IsCharSkirmishing(arwen));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElfInPlayTheSkirmishAbilityIsStillOfferedAndDiscardsFocusForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var focus = scn.GetFreepsCard("focus");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(focus);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// The ability has a cost, so it is offered whatever its effect will find.
		assertTrue(scn.FreepsActionAvailable(focus));
		// 8 + 2 for a hunter against a minion that is none
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(focus);

		assertInZone(Zone.DISCARD, focus);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
