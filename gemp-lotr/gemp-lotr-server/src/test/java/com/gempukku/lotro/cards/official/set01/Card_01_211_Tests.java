package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_211_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drawn", "1_211");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2; her text weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; his bonuses need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his response needs Frodo to die
					put("smeagol", "7_71");     // Sméagol, Always Helps: [gollum] companion, strength 3; his assignment ability is used only on purpose
					put("deadcity", "7_56");    // The Dead City: on Sméagol, "if Sméagol is about to be killed in a skirmish, he is discarded instead"
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrawntoItsPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Drawn to Its Power
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Each time a companion is killed in a skirmish involving a Nazgûl, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("drawn");

		assertEquals("Drawn to Its Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCompanionKilledInASkirmishInvolvingANazgulAddsABurdenWhetherByTheWoundOrByOverwhelming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetShadowCard("drawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// Aragorn has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(lemenya, khamul);
		scn.MoveCardsToSupportArea(drawn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, drawn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{sam, khamul});
		assertEquals(0, scn.GetBurdens());

		// A [wraith] Nazgûl beats a [gondor] Man, 9 against 8, without overwhelming him: the wound kills him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());

		// A [raider] Nazgûl overwhelms a [shire] Hobbit, 12 against 3: a second kill, a second burden.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, drawn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionKilledInAnotherMinionsSkirmishWhileANazgulWaitsForHisAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetShadowCard("drawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// Each companion has one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(lemenya, hound);
		scn.MoveCardsToSupportArea(drawn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.SUPPORT, drawn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{sam, hound});
		assertEquals(0, scn.GetBurdens());

		// The [wraith] Orc beats Sam, 5 against 3, while the Nazgûl is assigned and waiting: Sam dies, and no Nazgûl is in that skirmish.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetBurdens());

		// The control on the same board: the Nazgûl's own skirmish kills Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionDiscardedInsteadOfKilledInASkirmishInvolvingANazgulAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetShadowCard("drawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deadcity = scn.GetFreepsCard("deadcity");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, deadcity);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(drawn);
		assertAttachedTo(deadcity, smeagol);
		assertInZone(Zone.SUPPORT, drawn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, lemenya);
		assertEquals(0, scn.GetBurdens());

		// 9 against 3 overwhelms Sméagol, and The Dead City has him discarded instead of killed.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertInDiscard(smeagol);
		assertNotInZone(Zone.DEAD, smeagol);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledInASkirmishInvolvingANazgulAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawn = scn.GetShadowCard("drawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(drawn);
		assertInZone(Zone.SUPPORT, drawn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, lemenya);
		assertEquals(0, scn.GetBurdens());

		// 9 against 4: the ally is overwhelmed and killed in the Nazgûl's skirmish, and he is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
