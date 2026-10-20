package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warcry", "4_37");
					put("warcry2", "4_37");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man, strength 9; an assignment ability that needs an ally, and there is none
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4; may not be assigned to a skirmish
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1; its maneuver ability lets him be assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarCryofDunlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: War Cry of Dunland
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a [dunland] Man wins a skirmish, make him <b>fierce</b> and strength +4 until the regroup phase.
		*/

		var scn = GetScenario();

		var warcry = scn.GetFreepsCard("warcry");

		assertEquals("War Cry of Dunland", warcry.getBlueprint().getTitle());
		assertNull(warcry.getBlueprint().getSubtitle());
		assertFalse(warcry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, warcry.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, warcry.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, warcry.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(warcry, Timeword.RESPONSE));
		assertEquals(1, warcry.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADunlandManWhoWinsIsMadeFierceAndStrengthPlus4UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warcry = scn.GetShadowCard("warcry");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(warcry);
		assertInHand(warcry);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage);
		scn.SkipToAssignments();
		// The Savage is a second [dunland] Man, in a skirmish of his own that has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, brigand}, new PhysicalCardImpl[]{aragorn, savage});

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(warcry));
		assertFalse(scn.ShadowActionAvailable("Fierce and +4 Strength"));

		// 8 against 6: the Brigand wins.
		scn.ShadowPass();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(gimli));
		// The play is worded with the winner's name, and read by it.
		assertTrue(scn.ShadowActionAvailable(warcry, "Brigand"));
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.ShadowUseCardAction(warcry, "Brigand");
		// 8 + 4
		assertEquals(12, scn.GetStrength(brigand));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		// The Man who won, and not the other [dunland] Man.
		assertEquals(11, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertInDiscard(warcry);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 11 against 8: the Savage beats Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Brigand alone is fierce, and fights again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(brigand));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsAssignToMinions(aragorn, brigand);

		// The fierce skirmish is the last thing before the regroup phase: he is fierce and +4 through it.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(12, scn.GetStrength(brigand));
		// 12 against 8: Aragorn loses again.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(brigand));
	}

	@Test
	public void WithTwoDunlandMenWinningOneSkirmishEachIsOfferedAndOnlyTheOneNamedIsMadeFierceAndStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warcry = scn.GetShadowCard("warcry");
		var warcry2 = scn.GetShadowCard("warcry2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// 8 + 5 = 13, so that the three minions together (25) beat him without being double him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		assertEquals(13, scn.GetStrength(aragorn));
		scn.MoveCardsToHand(warcry, warcry2);
		assertInHand(warcry, warcry2);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage, axeman, warrior);
		scn.SkipToAssignments();
		// Aragorn takes the Brigand, and the Shadow player gives him the Savage and the Axeman as well; the Warrior is
		// a third [dunland] Man, assigned to Gimli, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, brigand}, new PhysicalCardImpl[]{gimli, warrior});
		scn.ShadowAssignToMinions(aragorn, savage, axeman);

		// 8 + 11 + 6 against 13: all three win.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// An offer for each [dunland] Man who won, from each copy; none for the [raider] Man who won beside them, or
		// for the [dunland] Man still waiting.
		assertTrue(scn.ShadowActionAvailable(warcry, "Brigand"));
		assertTrue(scn.ShadowActionAvailable(warcry, "Savage"));
		assertTrue(scn.ShadowActionAvailable(warcry2, "Brigand"));
		assertTrue(scn.ShadowActionAvailable(warcry2, "Savage"));
		assertFalse(scn.ShadowActionAvailable("Axeman"));
		assertFalse(scn.ShadowActionAvailable("Warrior"));
		assertEquals(11, scn.GetStrength(savage));
		assertEquals(8, scn.GetStrength(brigand));

		scn.ShadowUseCardAction(warcry, "Savage");
		// 11 + 4
		assertEquals(15, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertInDiscard(warcry);

		// The second copy answers the same win, for the Brigand.
		assertTrue(scn.ShadowActionAvailable(warcry2, "Brigand"));
		scn.ShadowUseCardAction(warcry2, "Brigand");
		// 8 + 4
		assertEquals(12, scn.GetStrength(brigand));
		assertTrue(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(15, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(axeman));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(warrior));
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertInDiscard(warcry2);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void IsNotOfferedWhenADunlandManLosesNorWhenAManOfAnotherCultureWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warcry = scn.GetShadowCard("warcry");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToHand(warcry);
		assertInHand(warcry);
		// Aragorn made strong enough to win; the Brigand given the vitality to live through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertEquals(10, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman);
		scn.ApplyAdHocModifier(new VitalityModifier(null, brigand, null, 1));
		assertEquals(2, scn.GetVitality(brigand));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, axeman}, new PhysicalCardImpl[]{aragorn, brigand});

		// 3 companions + 2 for the site: the event could be paid for.
		assertEquals(5, scn.GetTwilight());

		// 6 against 4: the Axeman, a Man who is not [dunland], wins.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 10: the Brigand, a [dunland] Man, loses and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(brigand));
		assertInPlay(brigand);
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertInHand(warcry);
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman, Keyword.FIERCE));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandMinionWhoIsNoManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warcry = scn.GetShadowCard("warcry");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToHand(warcry);
		assertInHand(warcry);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		// The Staff's maneuver ability makes "Saruman may not be assigned to a skirmish" not apply until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);

		// 2 companions + 2 for the site: the event could be paid for.
		assertEquals(4, scn.GetTwilight());

		// 8 + 2 for the Staff against 8: Saruman, a [dunland] minion who is no Man, wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.  The Staff makes
		// Saruman fierce, so the game goes on to the fierce assignments.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertInHand(warcry);
		assertEquals(10, scn.GetStrength(saruman));
	}
}
