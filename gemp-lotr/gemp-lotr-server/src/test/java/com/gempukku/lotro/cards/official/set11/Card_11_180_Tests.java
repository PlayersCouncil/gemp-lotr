package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_180_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brutality", "11_180");

					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, damage +1; toil only as it is played
					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, strength 5, damage +1; +3 while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard, strength 8; companions resistance -1,
					                             // and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1, no other text
					put("endless", "13_161");    // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");      // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
				}},
				// White Rocks 4_346 (site 4) and Deeping Wall 4_348 (site 5) are battlegrounds; sites 1 to 3
				// (Eastemnet Gullies 4_325, Eastfold 4_331, a plains, and Barrows of Edoras 4_337) are not.
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brutality", "11_180");

					put("tyrannical", "11_204"); // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, damage +1; toil only as it is played

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				// Site 2 is Uruk Camp 4_335 (a battleground and a plains), site 4 White Rocks 4_346 (a battleground only).
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrutalityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Brutality
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +1 for each battleground site you can spot.
		*/

		var scn = GetScenario();

		var brutality = scn.GetFreepsCard("brutality");

		assertEquals("Brutality", brutality.getBlueprint().getTitle());
		assertNull(brutality.getBlueprint().getSubtitle());
		assertFalse(brutality.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, brutality.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, brutality.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, brutality.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(brutality, Timeword.SKIRMISH));
		assertEquals(2, brutality.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite5AnUrukHaiMinionIsStrengthPlusTwoUntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brutality = scn.GetShadowCard("brutality");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var hounding = scn.GetShadowCard("hounding");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(brutality);
		scn.MoveMinionsToTable(tyrannical, hounding, saruman, guard);
		scn.MoveCardsToSupportArea(endless);
		assertInHand(brutality);
		assertInZone(Zone.SUPPORT, endless);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hounding}, new PhysicalCardImpl[]{boromir, saruman});
		// The Shadow player puts the Guard on Aragorn too and leaves the Tyrannical Uruk unassigned.
		scn.ShadowAssignToMinions(aragorn, guard);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(hounding));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(14, scn.GetStrength(tyrannical));
		assertTrue(scn.ShadowPlayAvailable(brutality));

		scn.ShadowPlayCard(brutality);
		// An Uruk-hai in this skirmish, an [uruk-hai] Wizard waiting for his own and an Uruk-hai assigned to none are
		// offered; the [sauron] Uruk-hai in this skirmish and the [uruk-hai] condition are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hounding, saruman, tyrannical));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, endless));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		// Two battlegrounds among the five sites on the path: 8 + 1 + 1.
		assertEquals(10, scn.GetStrength(saruman));
		assertEquals(5, scn.GetStrength(hounding));
		assertEquals(14, scn.GetStrength(tyrannical));
		assertInDiscard(brutality);

		// Still +2 at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(saruman));

		scn.PassSkirmishActions();

		// Gone in his own skirmish, the next: each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPlayedInTheAssignmentPhaseAndMakesAnUrukHaiMinionStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brutality = scn.GetShadowCard("brutality");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(brutality);
		scn.MoveMinionsToTable(tyrannical);
		assertInHand(brutality);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		// The phase next door: the Shadow player's assignment actions.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(brutality));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, tyrannical);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(14, scn.GetStrength(tyrannical));
		assertTrue(scn.ShadowPlayAvailable(brutality));
		scn.ShadowPlayCard(brutality);

		// One battleground, White Rocks, among the four sites on the path: 14 + 1.
		assertEquals(15, scn.GetStrength(tyrannical));
		assertInDiscard(brutality);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ABattlegroundThatIsAPlainsIsCountedThoughTheShadowPlayerControlsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var brutality = scn.GetShadowCard("brutality");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		// Both markers are at site 3, so Uruk Camp, site 2, can be controlled: it goes to the Shadow player's support area.
		var camp = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(camp);
		assertInZone(Zone.SUPPORT, camp);
		scn.SetShadowHand(brutality);
		scn.MoveMinionsToTable(tyrannical);
		assertInHand(brutality);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, tyrannical);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(14, scn.GetStrength(tyrannical));
		scn.ShadowPlayCard(brutality);

		// Uruk Camp, controlled (a battleground and a plains), and White Rocks: 14 + 2.
		assertEquals(16, scn.GetStrength(tyrannical));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionOnTheTableItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brutality = scn.GetShadowCard("brutality");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(brutality);
		// A [sauron] Uruk-hai is the only minion, and an [uruk-hai] condition the only [uruk-hai] card.
		scn.MoveMinionsToTable(guard);
		scn.MoveCardsToSupportArea(endless);
		assertInHand(brutality);
		assertInZone(Zone.SUPPORT, endless);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Two battlegrounds could be counted, but no [uruk-hai] minion can be made stronger.
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowPlayAvailable(brutality));
		scn.ShadowPlayCard(brutality);

		assertInDiscard(brutality);
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
