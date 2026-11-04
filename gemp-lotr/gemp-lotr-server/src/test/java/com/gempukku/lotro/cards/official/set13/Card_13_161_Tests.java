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

public class Card_13_161_Tests
{

	// The Towers path: site 2 Eastfold (plains, no battleground), site 4 White Rocks (battleground and nothing else).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("endless", "13_161");
					put("endless2", "13_161");
					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14; damage +1 and toil only
					put("murderous", "11_195");  // Murderous Uruk: [uruk-hai] Uruk-hai, strength 8; damage +1 and muster (regroup) only
					put("saruman", "17_116");    // Saruman, Master of the White Hand: [uruk-hai] Wizard minion, strength 8; each companion resistance -1
					put("guard", "10_81");       // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1 only
					put("spear", "17_117");      // Spear of the White Hand: [uruk-hai] possession, strength +1, no text
					put("fury", "18_116");       // Fury of the Evil Army: [uruk-hai] CONDITION on an [uruk-hai] minion, strength +1; a trigger on a win
					put("knight", "5_35");       // Gondorian Knight: [gondor] companion, resistance 6, Knight only
					put("faramir", "12_47");     // Faramir, Dunadan of Gondor: [gondor] companion, resistance 7; a skirmish ability never used
					put("thrarin", "1_27");      // Thrarin, Dwarven Smith: [dwarven] ally (resistance 0); Maneuver: exert him to let him skirmish at any site
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp (battleground and plains) at site 2.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("endless", "13_161");
					put("endless2", "13_161");
					put("tyrannical", "11_204");
					put("saruman", "17_116");
					put("guard", "10_81");
					put("spear", "17_117");
					put("knight", "5_35");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground, Plains; each Uruk-hai PLAYED must exert, and none is played
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EndlessAssaultStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Endless Assault
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make an [uruk-hai] minion strength +1 for each of the following that is true: it is at a battleground site; it is skirmishing a companion who has resistance 4 or less; it is bearing a possession; you can spot an [uruk-hai] minion not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("endless");

		assertEquals("Endless Assault", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAllFourTrueTheMinionChosenIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var endless = scn.GetShadowCard("endless");
		var endless2 = scn.GetShadowCard("endless2");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToSupportArea(endless, endless2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, endless, endless2);
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(tyrannical, saruman, guard);
		scn.AttachCardsTo(tyrannical, spear);
		assertAttachedTo(spear, tyrannical);
		// 6, -1 for Saruman, -1 for the burden.
		assertEquals(4, scn.GetResistance(knight));

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(knight, tyrannical);
		// Saruman and the Guard stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();

		// 14, +1 for the Spear.
		assertEquals(15, scn.GetStrength(tyrannical));
		assertTrue(scn.ShadowActionAvailable(endless));
		scn.ShadowUseCardAction(endless);
		assertInDiscard(endless);
		assertInZone(Zone.SUPPORT, endless2);
		// [uruk-hai] minions, in the skirmish or not; not a [sauron] Uruk-hai, nor [uruk-hai] cards that are no minions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tyrannical, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, spear, endless2));
		scn.ShadowChooseCard(tyrannical);
		// At a battleground, skirmishing a companion of resistance 4, bearing a possession, and Saruman unassigned.
		assertEquals(19, scn.GetStrength(tyrannical));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var endless = scn.GetShadowCard("endless");
		var tyrannical = scn.GetShadowCard("tyrannical");
		scn.MoveCardsToSupportArea(endless);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, endless);
		scn.MoveMinionsToTable(tyrannical);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(endless));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(knight, tyrannical);
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(endless));
	}

	@Test
	public void NearMissesCountNothingAndTheBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(knight, faramir);

		var endless = scn.GetShadowCard("endless");
		var endless2 = scn.GetShadowCard("endless2");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var murderous = scn.GetShadowCard("murderous");
		var guard = scn.GetShadowCard("guard");
		var spear = scn.GetShadowCard("spear");
		var fury = scn.GetShadowCard("fury");
		scn.MoveCardsToSupportArea(endless, endless2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, endless, endless2);
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(tyrannical, murderous, guard);
		scn.AttachCardsTo(tyrannical, fury);
		scn.AttachCardsTo(murderous, spear);
		assertAttachedTo(fury, tyrannical);
		assertAttachedTo(spear, murderous);
		assertEquals(5, scn.GetResistance(faramir));
		assertEquals(4, scn.GetResistance(knight));

		scn.SkipToAssignments();
		// Eastfold: plains, no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, tyrannical}, new PhysicalCardImpl[]{knight, murderous});
		// The Guard, a [sauron] Uruk-hai, stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsPass();

		// 14, +1 for Fury of the Evil Army.
		assertEquals(15, scn.GetStrength(tyrannical));
		scn.ShadowUseCardAction(endless);
		assertTrue(scn.ShadowHasCardChoiceAvailable(tyrannical, murderous));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		scn.ShadowChooseCard(tyrannical);
		// Skirmishing resistance 5; bearing a condition, no possession; the only cards not assigned are a [sauron]
		// Uruk-hai and an [uruk-hai] condition; the other minion bears a possession and is assigned to resistance 4.
		assertEquals(15, scn.GetStrength(tyrannical));
		assertInDiscard(endless);

		scn.FreepsPass();
		// 8, +1 for the Spear.
		assertEquals(9, scn.GetStrength(murderous));
		scn.ShadowUseCardAction(endless2);
		scn.ShadowChooseCard(murderous);
		// Bearing a possession; assigned to a companion of resistance 4 whose skirmish has not begun.
		assertEquals(10, scn.GetStrength(murderous));
		assertInDiscard(endless2);

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		// The bonus lasted the skirmish it was given in.
		assertEquals(9, scn.GetStrength(murderous));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUnassignedMinionChosenAtABattlegroundCountsItselfAsTheMinionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var endless = scn.GetShadowCard("endless");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(endless);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, endless);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(tyrannical, saruman);

		scn.SkipToAssignments();
		// White Rocks: a battleground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(knight, tyrannical);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowUseCardAction(endless);
		assertTrue(scn.ShadowHasCardChoiceAvailable(tyrannical, saruman));
		scn.ShadowChooseCard(saruman);
		// +1 at a battleground, +1 for an [uruk-hai] minion not assigned: Saruman himself, as the text has no "another".
		assertEquals(10, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWithResistanceZeroSkirmishingTheMinionCountsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var endless = scn.GetShadowCard("endless");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var murderous = scn.GetShadowCard("murderous");
		scn.MoveCardsToSupportArea(endless);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, endless);
		scn.MoveMinionsToTable(tyrannical, murderous);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.PassManeuverActions();
		scn.SkipToAssignments();
		// Eastfold: plains, no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(thrarin, tyrannical);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(14, scn.GetStrength(tyrannical));
		scn.ShadowUseCardAction(endless);
		assertTrue(scn.ShadowHasCardChoiceAvailable(tyrannical, murderous));
		scn.ShadowChooseCard(tyrannical);
		// An ally has resistance 0 but is no companion: only the unassigned Murderous Uruk counts.
		assertEquals(15, scn.GetStrength(tyrannical));
		assertInDiscard(endless);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
