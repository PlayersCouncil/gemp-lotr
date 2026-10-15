package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wulf", "4_40");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("footmen", "6_3");      // Dunlending Footmen: [dunland] Man, strength 11, vitality 2; a cost text only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4; may not be assigned to a skirmish
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1; its maneuver ability lets him be assigned

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WulfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Wulf, Dunlending Chieftain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Response:</b> If another [dunland] Man wins a skirmish, exert Wulf to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wulf");

		assertEquals("Wulf", card.getBlueprint().getTitle());
		assertEquals("Dunlending Chieftain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenAnotherDunlandManWinsExertsWulfToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wulf = scn.GetShadowCard("wulf");
		var brigand = scn.GetShadowCard("brigand");
		var footmen = scn.GetShadowCard("footmen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Player 1's second turn: both markers are past site 1, which can now be taken.
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wulf, brigand, footmen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, footmen}, new PhysicalCardImpl[]{gimli, brigand});
		// Wulf fights nobody.
		scn.ShadowDeclineAssignments();

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(wulf));

		// 11 against 8: the Footmen win.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wulf));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 8 against 6: the Brigand, a second title, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wulf));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(wulf));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		// Wulf is fierce and was not assigned: the game goes on to the fierce assignments.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenWulfHimselfOrAManOfAnotherCultureWinsNorWhenADunlandManLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wulf = scn.GetShadowCard("wulf");
		var footmen = scn.GetShadowCard("footmen");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, gimli, guard);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Aragorn is given the strength to beat the Footmen: 8 + 5 against 11.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.MoveMinionsToTable(wulf, footmen, axeman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, footmen}, new PhysicalCardImpl[]{guard, axeman},
				new PhysicalCardImpl[]{gimli, wulf});

		assertEquals(0, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 13 against 11: the Footmen, a [dunland] Man, lose and live (vitality 2).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(footmen));
		assertInZone(Zone.SHADOW_CHARACTERS, footmen);
		// The Shadow player is asked nothing: where Wulf's response is offered, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 6 against 4: the Axeman, a Man who is not [dunland], wins.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 10 against 6: Wulf himself wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertEquals(0, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandMinionWhoIsNoManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wulf = scn.GetShadowCard("wulf");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// Laid after the move, so that Saruman's own move trigger has nothing to count.
		scn.FreepsPass();
		scn.MoveMinionsToTable(wulf, saruman);
		scn.AttachCardsTo(saruman, staff);
		// The Staff's maneuver ability makes "Saruman may not be assigned to a skirmish" not apply until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman);
		scn.ShadowDeclineAssignments();

		assertEquals(0, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 8 + 2 for the Staff against 8: Saruman, a [dunland] minion who is no Man, wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertEquals(0, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileWulfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wulf = scn.GetShadowCard("wulf");
		var brigand = scn.GetShadowCard("brigand");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wulf, brigand);
		// Vitality 2 with 1 wound: Wulf cannot exert.
		scn.AddWoundsToChar(wulf, 1);
		assertEquals(1, scn.GetWoundsOn(wulf));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, brigand);
		scn.ShadowDeclineAssignments();

		assertEquals(1, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// 8 against 6: the Brigand wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		assertEquals(1, scn.GetWoundsOn(wulf));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// Wulf is exhausted but still fierce: the game goes on to the fierce assignments.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
