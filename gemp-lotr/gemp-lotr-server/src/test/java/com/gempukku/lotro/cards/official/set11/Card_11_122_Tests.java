package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frenzied", "11_122");
					put("frenzied2", "11_122");

					// Denizen of Khazad-dum: an [orc] Orc, strength 9 (+2 at an underground site).
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8.  Its start-of-assignment trigger
					// plays an [orc] Orc from the discard pile, which holds none.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, strength 13, no text.
					put("troop", "1_177");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: a [gondor] Man, strength 7.  A skirmish ability, never used.
					put("boromir", "1_97");
					// Sam, Son of Hamfast: a [shire] Hobbit.  His abilities are never used.
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: a [shire] Hobbit, cost 1; strength +2 only with a weapon.
					put("merry", "1_303");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4.  "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frenzied", "11_122");

					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4.  "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.KingSites,   // site 3, The Dimholt 8_117: underground, and a sanctuary
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenziedOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Frenzied Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is at an underground site, each character skirmishing it is strength -2.
		 * 	While you can spot 6 companions, each character skirmishing an [orc] minion is strength -2.
		*/

		var scn = GetScenario();

		var frenzied = scn.GetFreepsCard("frenzied");

		assertEquals("Frenzied Orc", frenzied.getBlueprint().getTitle());
		assertNull(frenzied.getBlueprint().getSubtitle());
		assertFalse(frenzied.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, frenzied.getBlueprint().getSide());
		assertEquals(Culture.ORC, frenzied.getBlueprint().getCulture());
		assertEquals(CardType.MINION, frenzied.getBlueprint().getCardType());
		assertEquals(Race.ORC, frenzied.getBlueprint().getRace());
		assertEquals(4, frenzied.getBlueprint().getTwilightCost());
		assertEquals(8, frenzied.getBlueprint().getStrength());
		assertEquals(3, frenzied.getBlueprint().getVitality());
		assertEquals(4, frenzied.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtAnUndergroundSiteTheCharacterSkirmishingItIsStrengthMinus2AndNoOtherIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);

		var frenzied = scn.GetShadowCard("frenzied");
		var frenzied2 = scn.GetShadowCard("frenzied2");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(frenzied, frenzied2, denizen);

		// The fellowship moves on to Balin's Tomb, site 4: underground.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, frenzied},
				new PhysicalCardImpl[]{gimli, denizen},
				new PhysicalCardImpl[]{boromir, frenzied2});
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(7, scn.GetStrength(boromir));

		scn.FreepsResolveSkirmish(gimli);
		// Gimli skirmishes another Orc at the same site; Aragorn is assigned to the Frenzied Orc and is not yet
		// skirmishing it.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		// 8 - 2: the second copy, in Boromir's skirmish, does not reach him.
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(boromir);
		// 7 - 2, by the second copy's own text
		assertEquals(5, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AwayFromAnUndergroundSiteTheCharacterSkirmishingItKeepsHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var frenzied = scn.GetShadowCard("frenzied");

		scn.StartGame();
		scn.MoveMinionsToTable(frenzied);

		// Ettenmoors, site 2: plains, no underground site.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, frenzied);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtTheDimholtAnAllySkirmishingItIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var frenzied = scn.GetShadowCard("frenzied");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(frenzied);

		// The fellowship moves on to The Dimholt, site 3: underground, and a sanctuary.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, frenzied);
		assertEquals(4, scn.GetStrength(thrarin));
		scn.FreepsResolveSkirmish(thrarin);

		// 4 - 2
		assertEquals(2, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSixCompanionsEachCharacterSkirmishingAnOrcMinionIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveCardsToHand(merry);

		var frenzied = scn.GetShadowCard("frenzied");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Five companions with Frodo; Merry, played, is the sixth.
		assertInHand(merry);
		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		scn.MoveMinionsToTable(frenzied, denizen, troop);

		// Ettenmoors, site 2: the first sentence does nothing here.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, denizen},
				new PhysicalCardImpl[]{gimli, frenzied},
				new PhysicalCardImpl[]{boromir, troop});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(boromir);
		// Boromir skirmishes a [moria] Orc; Aragorn is assigned to an [orc] Orc and not yet skirmishing it.
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		// 8 - 2, skirmishing the Denizen
		assertEquals(6, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(gimli);
		// 6 - 2, skirmishing the Frenzied Orc itself, an [orc] minion as well
		assertEquals(4, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyTheCharacterSkirmishingAnOrcMinionKeepsHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Five companions with Frodo, and an ally, who is no companion.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(thrarin);

		var frenzied = scn.GetShadowCard("frenzied");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(frenzied, denizen);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSixCompanionsAnAllySkirmishingAnOrcMinionWhoIsNoOrcIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam, merry);
		scn.MoveAlliesToTable(thrarin);

		var frenzied = scn.GetShadowCard("frenzied");
		var halftroll = scn.GetShadowCard("halftroll");

		scn.StartGame();
		scn.MoveMinionsToTable(frenzied, halftroll);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(thrarin, halftroll);
		scn.ShadowDeclineAssignments();
		assertEquals(4, scn.GetStrength(thrarin));
		scn.FreepsResolveSkirmish(thrarin);

		// 4 - 2
		assertEquals(2, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
