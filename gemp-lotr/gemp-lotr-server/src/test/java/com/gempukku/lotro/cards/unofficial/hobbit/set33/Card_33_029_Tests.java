package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_029_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Turn 1's skirmishes
	// are at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grudge", "33_29");

					put("jails", "33_32");      // Elven Jails: [mirkwood] condition; a Regroup ability not used
					put("jails2", "33_32");
					put("hatred", "30_34");     // Hatred Rekindled: a [gundabad] condition, no [mirkwood] one; a Shadow ability not used

					put("marauder", "30_38");   // Orkish Marauder: [gundabad] Orc, strength 8; abilities not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; an Assignment ability not used
					put("watchful", "30_40");   // Watchful Orc: [gundabad] Orc, strength 8; a Shadow ability not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, strength 8
					put("ferny", "33_22");      // Bill Ferny Sr., Servant of Azog: a [gundabad] Man, strength 4; only the Shadow player may assign him

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, strength 7; damage +1
					put("gandalf", "30_25");    // Gandalf, Leader of Dwarves: [gandalf] companion, strength 7; skirmish abilities not used
					// Thrarin, Dwarven Smith: a [dwarven] ally, strength 4, vitality 2, whose own Maneuver ability lets him
					// skirmish (no Hobbit-set card does it with as little in the way).
					put("thrarin", "1_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AncientGrudgeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Ancient Grudge
		 * Unique: True
		 * Side: Shadow
		 * Culture: Mirkwood
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot 3 [mirkwood] conditions, each [dwarven] character skirmishing a [gundabad] Orc is strength -2.
		 * 	Skirmish: Remove (3) to make a [gundabad] Orc strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grudge");

		assertEquals("Ancient Grudge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MIRKWOOD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void With3MirkwoodConditionsEachDwarvenCharacterSkirmishingAGundabadOrcIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveAlliesToTable(thrarin);

		var grudge = scn.GetShadowCard("grudge");
		var jails = scn.GetShadowCard("jails");
		var jails2 = scn.GetShadowCard("jails2");
		var marauder = scn.GetShadowCard("marauder");
		var aggressor = scn.GetShadowCard("aggressor");
		var watchful = scn.GetShadowCard("watchful");
		scn.MoveCardsToSupportArea(grudge, jails, jails2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grudge, jails, jails2);
		scn.MoveMinionsToTable(marauder, aggressor, watchful);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin's own ability lets him skirmish.
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, marauder}, new PhysicalCardImpl[]{dwalin, aggressor},
				new PhysicalCardImpl[]{thrarin, watchful});

		// Assigned, but no skirmish begun.
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(4, scn.GetStrength(thrarin));

		scn.FreepsResolveSkirmish(balin);
		// Exactly 3: the Grudge and two Elven Jails.  Dwalin's skirmish is still to come.
		assertEquals(5, scn.GetStrength(balin));
		assertEquals(7, scn.GetStrength(dwalin));
		assertEquals(4, scn.GetStrength(thrarin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(5, scn.GetStrength(dwalin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		// The ally too: a [dwarven] character.
		assertEquals(2, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void With2MirkwoodConditionsAndAnotherConditionDwarvesAreNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var grudge = scn.GetShadowCard("grudge");
		var jails = scn.GetShadowCard("jails");
		var hatred = scn.GetShadowCard("hatred");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToSupportArea(grudge, jails, hatred);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grudge, jails, hatred);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, marauder);
		scn.FreepsResolveSkirmish(balin);

		// Two [mirkwood] conditions and a [gundabad] one: one short.
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NoOtherCultureAgainstAGundabadOrcAndNoDwarfAgainstAnotherOrcOrAGundabadMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(balin, dwalin, gandalf);

		var grudge = scn.GetShadowCard("grudge");
		var jails = scn.GetShadowCard("jails");
		var jails2 = scn.GetShadowCard("jails2");
		var watchful = scn.GetShadowCard("watchful");
		var footman = scn.GetShadowCard("footman");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToSupportArea(grudge, jails, jails2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grudge, jails, jails2);
		scn.MoveMinionsToTable(watchful, footman, ferny);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, watchful}, new PhysicalCardImpl[]{balin, footman});
		// Only the Shadow player may assign Bill Ferny Sr.
		scn.ShadowAssignToMinions(dwalin, ferny);

		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(7, scn.GetStrength(gandalf));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(balin);
		// A [moria] Orc.
		assertEquals(7, scn.GetStrength(balin));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		// A [gundabad] Man.
		assertEquals(7, scn.GetStrength(dwalin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishRemoves3ToMakeAGundabadOrcStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		scn.MoveCompanionsToTable(balin, dwalin);

		var grudge = scn.GetShadowCard("grudge");
		var marauder = scn.GetShadowCard("marauder");
		var aggressor = scn.GetShadowCard("aggressor");
		var footman = scn.GetShadowCard("footman");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCardsToSupportArea(grudge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grudge);
		scn.MoveMinionsToTable(marauder, aggressor, footman, ferny);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, marauder}, new PhysicalCardImpl[]{dwalin, aggressor});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(grudge));
		assertEquals(5, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(marauder));
		assertEquals(6, scn.GetStrength(aggressor));
		scn.ShadowUseCardAction(grudge);

		// A [gundabad] Orc in this skirmish or in one still to come; not a [moria] Orc or a [gundabad] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(marauder, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman, ferny));
		scn.ShadowChooseCard(aggressor);

		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aggressor));
		assertEquals(8, scn.GetStrength(marauder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gone in the Aggressor's own skirmish: each skirmish is a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(dwalin);
		assertEquals(6, scn.GetStrength(aggressor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishIsNotOfferedWithOnly2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var grudge = scn.GetShadowCard("grudge");
		var marauder = scn.GetShadowCard("marauder");
		scn.MoveCardsToSupportArea(grudge);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, grudge);
		scn.MoveMinionsToTable(marauder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, marauder);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grudge));
	}
}
