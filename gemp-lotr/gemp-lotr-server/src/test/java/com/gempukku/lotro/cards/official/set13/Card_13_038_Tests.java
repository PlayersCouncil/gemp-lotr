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

public class Card_13_038_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "13_38");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; Radagast's bearer
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a [gandalf] companion, strength 7
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion of another title and race, strength 6
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, the [gandalf] card who is no companion

					// Úlairë Lemenya: [wraith] Nazgûl, strength 9; her own text reaches only a companion or ally
					// bearing a [wraith] condition, and nobody does here.
					put("lemenya", "1_232");
					put("steed", "1_208");      // Black Steed: [wraith] possession, a mount; +2, and +2 more at a plains site
					put("asperity", "9_42");    // Ring of Asperity: [wraith] artifact, +1
					put("twilight", "19_34");   // In Twilight: [wraith] condition on a [wraith] minion, +2
					put("chief1", "1_266");     // Orc Chieftain: [sauron] Orc, strength 7
					put("chief2", "1_266");
					put("banner1", "6_98");     // Banner of the Eye: [sauron] possession, a hand weapon, +1
					put("banner2", "6_98");
					put("rider", "12_161");     // Black Rider: fierce [wraith] Nazgûl, strength 8, +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Radagast, Tender of Beasts
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Follower
		 * Subtype:
		 * Resistance: +2
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper>
		 * 		While you can spot 2 [gandalf] companions, minions skirmishing bearer do not gain strength bonuses from possessions.
		 */

		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");

		assertEquals("Radagast", radagast.getBlueprint().getTitle());
		assertEquals("Tender of Beasts", radagast.getBlueprint().getSubtitle());
		assertTrue(radagast.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, radagast.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, radagast.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, radagast.getBlueprint().getCardType());
		assertEquals(2, radagast.getBlueprint().getTwilightCost());
		assertEquals(2, radagast.getBlueprint().getResistance());
		assertEquals(2, scn.GetAidTwilightCost(radagast));
	}

	@Test
	public void WithTwoGandalfCompanionsMinionsSkirmishingBearerLoseOnlyWhatTheirPossessionsGive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(aragorn, gandalf, avenger);
		scn.MoveCardsToSupportArea(radagast);

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var asperity = scn.GetShadowCard("asperity");
		var twilight = scn.GetShadowCard("twilight");
		var chief1 = scn.GetShadowCard("chief1");
		var chief2 = scn.GetShadowCard("chief2");
		var banner1 = scn.GetShadowCard("banner1");
		var banner2 = scn.GetShadowCard("banner2");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, chief1, chief2);
		scn.AttachCardsTo(lemenya, steed, asperity, twilight);
		scn.AttachCardsTo(chief1, banner1);
		scn.AttachCardsTo(chief2, banner2);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		// Site 2, Ettenmoors, is a plains site.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 9 + 2 + 2 (the Steed) + 1 (the Ring) + 2 (In Twilight)
		assertEquals(16, scn.GetStrength(lemenya));
		// 7 + 1 (the Banner)
		assertEquals(8, scn.GetStrength(chief1));
		assertEquals(8, scn.GetStrength(chief2));

		// The Aid: (2) added, and Radagast goes to a companion.
		var pool = scn.GetTwilight();
		assertInZone(Zone.SUPPORT, radagast);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(radagast));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, radagast);
		assertEquals(aragorn, radagast.getAttachedTo());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gandalf, chief2});
		// The Shadow player puts the second minion on Aragorn.
		scn.ShadowAssignToMinions(aragorn, chief1);

		// Gandalf's skirmish first.  The minion fighting Gandalf keeps its Banner, and the two assigned to
		// Radagast's bearer are not skirmishing him yet and keep everything.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, chief2));
		assertEquals(8, scn.GetStrength(chief2));
		assertEquals(16, scn.GetStrength(lemenya));
		assertEquals(8, scn.GetStrength(chief1));
		scn.PassSkirmishActions();
		// The Banner's bearer won: its optional trigger.
		scn.ShadowDeclineOptionalTrigger();

		// Aragorn's skirmish.  Lemenya loses the Steed's +2 and +2 and keeps the artifact's +1 and the condition's
		// +2; the Chieftain loses the Banner's +1.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chief1));
		assertEquals(12, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chief1));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceTheSecondGandalfCompanionIsKilledAFierceMinionKeepsItsPossessionsBonusInItsSecondSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gandalf, avenger);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(aragorn, radagast);

		var rider = scn.GetShadowCard("rider");
		var steed = scn.GetShadowCard("steed");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(rider, troop);
		scn.AttachCardsTo(rider, steed);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{avenger, troop});

		// Gandalf and the Ent Avenger: two [gandalf] companions.  The Rider is 8 + 4 for the four companions,
		// and the Steed's +2 and +2 at this plains site are not gained.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, rider));
		assertEquals(12, scn.GetStrength(rider));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Troop's 13 is double the Ent Avenger's 6: he is killed.
		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, avenger);

		// The fierce round.  Gandalf is the one [gandalf] companion left, beside Barliman, a [gandalf] ally, and
		// Aragorn, a companion of another culture: the Rider, 8 + 3 for the companions, gains the Steed's 4.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, rider));
		assertInZone(Zone.SUPPORT, barliman);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(15, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
