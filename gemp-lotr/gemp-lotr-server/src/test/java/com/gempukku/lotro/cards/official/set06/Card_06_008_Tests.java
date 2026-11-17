package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peasants", "6_8");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6, vitality 2; stronger and fierce only at 2 burdens
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4; may not be assigned to a skirmish
					put("staff", "4_174");      // Saruman's Staff: on Saruman, strength +2, fierce, damage +1; its maneuver ability lets him be assigned
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc, strength 5; stronger and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, vitality 3; stronger only against a Nazgul
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text in play

					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] Free Peoples condition, support area
					put("saga", "1_114");       // The Saga of Elendil: [gondor] Free Peoples condition on a [gondor] companion
					put("arms", "7_252");       // Strong Arms: [rohan] Free Peoples condition that plays on a site
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area
					put("legacy", "1_282");     // The Weight of a Legacy: [sauron] Shadow condition, plays on Aragorn
					put("twilight", "19_34");   // In Twilight: [wraith] Shadow condition borne by a [wraith] minion
					put("spies", "1_140");      // Spies of Saruman: [isengard] Shadow condition that plays on a site; skips archery there
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion; he exerts when assigned
					put("coat", "2_105");       // Mithril-coat: [shire] artifact borne by Frodo
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooLongHaveThesePeasantsStoodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Too Long Have These Peasants Stood
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a [dunland] Man wins a skirmish, discard all conditions.
		*/

		var scn = GetScenario();

		var peasants = scn.GetFreepsCard("peasants");

		assertEquals("Too Long Have These Peasants Stood", peasants.getBlueprint().getTitle());
		assertNull(peasants.getBlueprint().getSubtitle());
		assertFalse(peasants.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, peasants.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, peasants.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, peasants.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(peasants, Timeword.RESPONSE));
		assertEquals(2, peasants.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenADunlandManWinsItDiscardsEveryActiveConditionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peasants = scn.GetShadowCard("peasants");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var stairs = scn.GetFreepsCard("stairs");
		var saga = scn.GetFreepsCard("saga");
		var arms = scn.GetFreepsCard("arms");
		var coat = scn.GetFreepsCard("coat");
		var pinned = scn.GetShadowCard("pinned");
		var legacy = scn.GetShadowCard("legacy");
		var twilight = scn.GetShadowCard("twilight");
		var spies = scn.GetShadowCard("spies");
		var poison = scn.GetShadowCard("poison");
		// Inactive conditions on the Free Peoples player's turn: the Shadow player's own Free Peoples condition,
		// and the Free Peoples player's own Shadow condition.
		var shadowStairs = scn.GetShadowCard("stairs");
		var freepsPinned = scn.GetFreepsCard("pinned");

		scn.MoveCardsToHand(peasants);
		scn.MoveCompanionsToTable(aragorn, gimli, arwen);
		scn.MoveCardsToSupportArea(stairs, pinned, shadowStairs, freepsPinned);
		// Aragorn is assigned to nobody: Spider Poison's exertion never comes.
		scn.AttachCardsTo(aragorn, saga, legacy, poison);
		scn.AttachCardsTo(frodo, coat);

		scn.StartGame();
		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs, pinned, shadowStairs, freepsPinned);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(legacy, aragorn);
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(coat, frodo);
		scn.MoveMinionsToTable(brigand, savage, hound);
		scn.AttachCardsTo(hound, twilight);
		assertAttachedTo(twilight, hound);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		scn.AttachCardsTo(site, arms, spies);
		assertAttachedTo(arms, site);
		assertAttachedTo(spies, site);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{gimli, brigand});
		// The Hound is left over and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// 11 against 6: the Savage, a [dunland] Man of one title, wins, and the event is offered; it is declined.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(peasants));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs, pinned);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 8 against 6: the Brigand, a [dunland] Man of another title, wins, and the event is played.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(peasants));
		scn.ShadowChooseOptionalTrigger(peasants);

		// Conditions of both sides, in a support area, on a companion, on a minion and on a site.
		assertInDiscard(peasants, stairs, saga, arms, pinned, legacy, twilight, spies);
		// A Shadow possession and a Free Peoples artifact are no conditions.
		assertAttachedTo(poison, aragorn);
		assertAttachedTo(coat, frodo);
		// Inactive conditions are not discarded.
		assertInZone(Zone.SUPPORT, shadowStairs, freepsPinned);
		assertInPlay(brigand, savage, hound);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandManLosesNorWhenAManOfAnotherCultureWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peasants = scn.GetShadowCard("peasants");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(peasants);
		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs);
		// Aragorn made strong enough to win; the Brigand given the vitality to live through the loss.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertEquals(10, scn.GetStrength(aragorn));

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, axeman);
		scn.ApplyAdHocModifier(new VitalityModifier(null, brigand, null, 1));
		assertEquals(2, scn.GetVitality(brigand));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, axeman}, new PhysicalCardImpl[]{aragorn, brigand});
		scn.SetTwilight(5);
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

		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenADunlandMinionWhoIsNoManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);

		var peasants = scn.GetShadowCard("peasants");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToHand(peasants);
		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs);

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
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// 8 + 2 for the Staff against 8: Saruman, a [dunland] minion who is no Man, wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 1 for losing + 1 for the Staff's damage +1
		assertEquals(2, scn.GetWoundsOn(aragorn));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.  The Staff makes
		// Saruman fierce, so the game goes on to the fierce assignments.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		assertInHand(peasants);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(10, scn.GetStrength(saruman));
	}
}
