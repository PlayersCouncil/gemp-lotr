package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_024_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keeping", "58_24");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, no race, strength 5; a regroup ability only
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] Spider minion, strength 8, enduring, fierce
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, strength 13, no text
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: an ally, with an ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2; no text but its play requirement
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2; a skirmish ability that is never used here
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, strength 5, vitality 1; its when-played text is not used here
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
		 * Game Text: Each time a companion skirmishing a [Gollum] minion takes a wound, exert a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("keeping");

		assertEquals("Promise Keeping", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CompanionsWoundedSkirmishingGollumOrShelobEachMakeTheShadowPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(barliman);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum, shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, gollum}, new PhysicalCardImpl[]{aragorn, shelob});
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Sam, strength 3, loses to Gollum, strength 5, and takes a wound.
		assertEquals(1, scn.GetWoundsOn(sam));
		// The Shadow player, whose card it is, exerts a companion: any companion, the wounded one and the
		// Ring-bearer among them; not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn, strength 8, ties Shelob, strength 8, and loses: a Spider of the [gollum] culture counts as well.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, aragorn));
		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void EachOfTwoWoundsOnOneCompanionExertsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.DAMAGE, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, gollum);
		scn.PassSkirmishActions();

		// Gollum is damage +1: two wounds, each its own trigger.
		scn.ShadowChooseCard(aragorn);
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WoundsWhileSkirmishingAnotherMinionOrBeforeTheSkirmishWithGollumBeginsExertNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(sam, aragorn, guard);

		var keeping = scn.GetShadowCard("keeping");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveCardsToSupportArea(keeping);
		scn.MoveMinionsToTable(gollum, troop, runner);
		// Sam dies of his next wound, and one threat waits on the dead pile.
		scn.AddWoundsToChar(sam, 3);
		scn.AddThreats(1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop}, new PhysicalCardImpl[]{aragorn, gollum}, new PhysicalCardImpl[]{guard, runner});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		// Dwarf Guard, strength 4, loses to the Runner, strength 5, and takes a wound (not overwhelmed, so he lives)
		// skirmishing an Orc, with Gollum in play and in a skirmish of his own to come: no exertion.
		assertEquals(1, scn.GetWoundsOn(guard));
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
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn beats Gollum: the minion's wound is no companion's.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
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
