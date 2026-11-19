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

public class Card_13_025_Tests
{

	// The Fellowship path: site 3 is Rivendell Valley 1_341, a sanctuary and no forest, and the home of Elrond.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "13_25");
					put("tall2", "13_25");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, no archer, strength 6, resistance 6; +3 only against a Nazgul
					// Elrond, Lord of Rivendell: Elf ally, home site 3, strength 8; heals allies of site 3 at the start
					// of his owner's turns and has a fellowship ability used only on purpose
					put("elrond", "1_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8; a maneuver ability used only on purpose
					// Garrison of Gondor: an archer who is no Elf ([gondor] Man companion); defender only beside a
					// fortification (none)
					put("garrison", "8_36");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Rivendell Waterfall 1_342 (forest, sanctuary) as site 3: with a ranger there (Arwen)
	// its move limit +1 changes nothing a test reads.
	protected VirtualTableScenario GetForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "13_25");
					put("tall2", "13_25");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, resistance 6; +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					// Rumil, Elven Protector: an Elf archer (ally, home 6F, so he adds nothing to the archery total
					// here); an archery ability used only on purpose
					put("rumil", "1_57");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf archer companion; an archery ability (no archery phase here)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Derndingle 4_330 (forest, no sanctuary) as site 2: its move limit needs an unbound
	// Hobbit, and none is laid.
	protected VirtualTableScenario GetDerndingleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tall", "13_25");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5, resistance 6; +2 only at sites 6 to 8
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "4_330");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StandingTallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Standing Tall
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition from play to make a minion skirmishing an Elf strength -1 for each of the following that is true: it is skirmishing a companion who has resistance 4 or more; it is wounded; you can spot an Elf archer; the fellowship is at a forest site.
		*/

		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");

		assertEquals("Standing Tall", tall.getBlueprint().getTitle());
		assertNull(tall.getBlueprint().getSubtitle());
		assertFalse(tall.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, tall.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, tall.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, tall.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(tall, Keyword.SUPPORT_AREA));
		assertEquals(1, tall.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAForestWithResistance4AWoundAndAnElfArcherTheMinionIsMinus4ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetForestScenario();

		var tall = scn.GetFreepsCard("tall");
		var tall2 = scn.GetFreepsCard("tall2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var rumil = scn.GetFreepsCard("rumil");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToSupportArea(tall, tall2);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tall, tall2);
		scn.SkipToSite(2);
		// Each burden takes 1 from every companion's resistance: Arwen's 6 is exactly 4.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(arwen));
		scn.MoveMinionsToTable(troop, soldier, savage);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.SkipToAssignments();
		// The archer goes down once the archery phase is over (pitfalls, "Archers get in the way").
		scn.MoveAlliesToTable(rumil);
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, troop}, new PhysicalCardImpl[]{haldir, savage});
		// The Shadow player puts the Orc Soldier on Arwen too: two minions skirmishing one Elf.
		scn.ShadowAssignToMinions(arwen, soldier);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(tall));
		scn.FreepsUseCardAction(tall);
		// Either minion skirmishing Arwen; not the Uruk Savage, assigned to Haldir with his skirmish still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, arwen, haldir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// 13 - 1 resistance 4 - 1 wounded - 1 Rumil - 1 forest
		assertEquals(9, scn.GetStrength(troop));
		assertInDiscard(tall);
		assertInZone(Zone.SUPPORT, tall2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The second copy on the unwounded Orc Soldier in the same skirmish: the wound is the chosen minion's own.
		scn.ShadowPass();
		// Rumil goes and Legolas takes his place: the Elf archer spotted is now a companion, not an ally.
		scn.MoveCardsToDiscard(rumil);
		scn.MoveCompanionsToTable(legolas);
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(tall2);
		scn.FreepsChooseCard(soldier);
		// 7 - 1 resistance 4 - 1 Legolas - 1 forest
		assertEquals(4, scn.GetStrength(soldier));
		assertEquals(9, scn.GetStrength(troop));
		assertInDiscard(tall2);

		// The penalties lasted Arwen's skirmish only: at the start of Haldir's they are gone.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnElfAllyAndACompanionAtResistance3WithNoElfArcherAndNoForestCountOnlyTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var tall2 = scn.GetFreepsCard("tall2");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(tall, tall2);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tall, tall2);
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(elrond);
		// Arwen's 6 under three burdens is exactly 3.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(arwen));
		scn.MoveMinionsToTable(savage, soldier);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(soldier));
		scn.SkipToAssignments();
		// An archer who is no Elf, down once the archery phase is over; Arwen is an Elf who is no archer.
		scn.MoveCompanionsToTable(garrison);
		assertTrue(scn.HasKeyword(garrison, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, soldier}, new PhysicalCardImpl[]{elrond, savage});

		// Resistance 3 does not count; the wound does.
		scn.FreepsResolveSkirmish(arwen);
		scn.FreepsUseCardAction(tall);
		// 7 - 1 wounded
		assertEquals(6, scn.GetStrength(soldier));
		assertInDiscard(tall);
		scn.PassSkirmishActions();

		// An Elf ally is an Elf, and no companion with resistance: again only the wound.
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsUseCardAction(tall2);
		// 5 - 1 wounded
		assertEquals(4, scn.GetStrength(savage));
		assertInDiscard(tall2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtDerndingleAForestThatIsNoSanctuaryCountsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDerndingleScenario();

		var tall = scn.GetFreepsCard("tall");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToSupportArea(tall);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tall);
		// Haldir's 6 under three burdens is exactly 3: not 4 or more.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(haldir));
		scn.MoveMinionsToTable(soldier);
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.FOREST));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));

		scn.FreepsAssignToMinions(haldir, soldier);
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(7, scn.GetStrength(soldier));
		scn.FreepsUseCardAction(tall);
		// 7 - 1 forest
		assertEquals(6, scn.GetStrength(soldier));
		assertInDiscard(tall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhaseAndInTheSkirmishOfAManIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tall = scn.GetFreepsCard("tall");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToSupportArea(tall);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, tall);
		scn.MoveMinionsToTable(troop, savage);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tall));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{arwen, savage});

		// Aragorn is no Elf, and the Savage waits for Arwen's skirmish: nobody to choose, the condition goes for nothing.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsActionAvailable(tall));
		scn.FreepsUseCardAction(tall);
		assertInDiscard(tall);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
