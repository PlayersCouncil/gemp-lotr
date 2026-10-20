package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keeping", "8_24");
					put("gollum", "7_58");        // Gollum, Plotting Deceiver: [gollum] minion, no race, strength 5; his text is about the Shadow phase
					put("darkness", "9_28");      // Gollum, Dark as Darkness: [gollum] minion, strength 5, vitality 4; "Regroup: Exert Gollum twice to wound the Ring-bearer"
					put("shelob", "8_26");        // Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8, enduring, fierce
					put("skirmisher", "18_90");   // Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; "Each time this minion wins a skirmish, you may exert him twice to wound a companion"
					put("hunters", "1_267");      // Orc Hunters: [sauron] Orc, strength 9; "Skirmish: Exert this minion to wound a character he is skirmishing"
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1; its when-played text is not used here
					put("sam", "1_311");          // Sam, Son of Hamfast: strength 3, vitality 4
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");          // Dwarf Guard: companion, vitality 2; no text but its play requirement
					put("smeagol", "6_45");       // Smeagol, Poor Creature: a [gollum] companion, strength 3; a skirmish ability used only on purpose
					put("barliman", "1_70");      // Barliman Butterbur: an ally, with an ability used only on purpose
					put("bounder", "1_286");      // Bounder: [shire] Hobbit ally, home 2; a skirmish ability that is never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keeping", "8_24");
					put("gollum", "7_58");      // Gollum, Plotting Deceiver: [gollum] minion, strength 5, vitality 4; his text is about the Shadow phase
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4: he loses to Gollum and is not overwhelmed
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion in no skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PromiseKeepingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Promise Keeping
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time a companion takes a wound during a skirmish that involves a [gollum] minion, exert a companion.
		*/

		var scn = GetScenario();

		var keeping = scn.GetFreepsCard("keeping");

		assertEquals("Promise Keeping", keeping.getBlueprint().getTitle());
		assertNull(keeping.getBlueprint().getSubtitle());
		assertTrue(keeping.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, keeping.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, keeping.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, keeping.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(keeping, Keyword.SUPPORT_AREA));
		assertEquals(4, keeping.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionWoundedInASkirmishInvolvingAGollumMinionMakesTheShadowPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassSkirmishActions();

		// Sam, strength 3, loses to Gollum, strength 5, and takes his wound for losing.
		assertEquals(1, scn.GetWoundsOn(sam));
		// The exertion is the Shadow player's to place, on any of the three companions.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Another companion than the one wounded: an exertion, and not a second wound on the loser.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundToACompanionOutsideTheSkirmishAndOneInASkirmishWithShelobEachExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, guard);
		scn.MoveAlliesToTable(barliman);
		// Exhausted: the Dwarf Guard cannot exert.
		scn.AddWoundsToChar(guard, 1);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum, skirmisher, shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, gollum},
				new PhysicalCardImpl[]{gimli, shelob});
		scn.ShadowAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn, 8, loses to Gollum and the Skirmisher, 14.  The win is answered before he takes his wound
		// (engine pass 54).  The Skirmisher won: it exerts twice to wound Sam, who is in no skirmish, while
		// Gollum's skirmish goes on.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(skirmisher));

		// Any companion who can exert; not the exhausted Guard, not the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Then Aragorn takes his wound for losing, and a companion exerts again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn, gimli));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli, 6, loses to Shelob, 8, a [gollum] Spider.
		assertEquals(2, scn.GetWoundsOn(gimli));
		// Gimli, vitality 3, is now exhausted himself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, guard));
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachOfTwoWoundsOnOneCompanionInOneSkirmishExertsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");
		var hunters = scn.GetShadowCard("hunters");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum, hunters);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowAssignToMinions(aragorn, hunters);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Hunters exert to wound Aragorn, in the middle of the skirmish.
		scn.ShadowUseCardAction(hunters);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.PassSkirmishActions();
		// Aragorn, 8, loses to 14: a second wound on him, and a second exertion.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WoundsInASkirmishWithNoGollumMinionOrOutsideASkirmishOrOnAMinionExertNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, aragorn, smeagol);

		var keeping = scn.GetShadowCard("keeping");
		var darkness = scn.GetShadowCard("darkness");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(darkness, troop, runner);
		// Sam dies of his next wound, and one threat waits on the dead pile.
		scn.AddWoundsToChar(sam, 3);
		scn.AddThreats(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, troop},
				new PhysicalCardImpl[]{aragorn, darkness},
				new PhysicalCardImpl[]{smeagol, runner});
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		// Smeagol, a [gollum] companion and no minion, loses to the Runner, a [moria] Orc, and takes a wound,
		// with Gollum in play and in a skirmish of his own to come: no exertion.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Sam takes a wound skirmishing an Orc, too: he dies, and the threat becomes a wound, which the Free
		// Peoples player places on Aragorn, assigned to Gollum but not yet skirmishing.
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetThreats());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn beats Gollum: the minion's wound is no companion's.
		assertEquals(1, scn.GetWoundsOn(darkness));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// In the regroup phase Gollum himself wounds the Ring-bearer: no skirmish is going on.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(darkness, "Wound the Ring-bearer"));
		scn.ShadowUseCardAction(darkness, "Wound the Ring-bearer");

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(darkness));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyWoundedSkirmishingGollumAtHisHomeSiteExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(bounder);
		// Strength 3, so that Gollum's 5 wounds him without overwhelming him (strength 2 would be killed outright).
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 1));

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum);

		// The fellowship moves to site 2, Bounder's home.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(bounder));
		scn.FreepsAssignAndResolve(bounder, gollum);
		scn.PassSkirmishActions();

		// Bounder, strength 3, loses to Gollum, strength 5, and takes a wound: an ally, no companion.
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
