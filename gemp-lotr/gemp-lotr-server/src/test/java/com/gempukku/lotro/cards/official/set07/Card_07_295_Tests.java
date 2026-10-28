package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_295_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "7_295");
					put("band2", "7_295");      // a second copy: another [sauron] Orc, and not the one that exerts
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon PLAYED on him only
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5; its text needs 2 wounds on the Ring-bearer
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai (no Orc), strength 9; damage +1 only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength +2 only while he bears a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: an ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcAssaultBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Assault Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Spot 6 companions and exert this minion to make another [sauron] Orc strength +6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("band");

		assertEquals("Orc Assault Band", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithSixCompanionsItExertsToMakeAnotherSauronOrcStrengthPlus6ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		// Frodo and five more: 6 companions.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, scn.GetFreepsCard("merry"), scn.GetFreepsCard("sam"));

		var band = scn.GetShadowCard("band");
		var band2 = scn.GetShadowCard("band2");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(band, band2, chieftain, soldier, hound, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, soldier},
				new PhysicalCardImpl[]{gimli, guard});
		// The Hound joins the Chieftain against Aragorn; the two Assault Bands are left unassigned.
		scn.ShadowAssignToMinions(aragorn, hound);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, hound));
		assertTrue(scn.IsCharAssignedAgainst(boromir, soldier));
		assertFalse(scn.IsCharAssigned(band));
		assertFalse(scn.IsCharAssigned(band2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(band2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(band));

		scn.ShadowUseCardAction(band);
		// This copy exerts, not the other.
		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(band2));
		// Another [sauron] Orc, in this skirmish or waiting for his own, the other copy among them; not the Band
		// itself, not the [wraith] Orc, not the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier, band2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(band, hound, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		// 7 + 6
		assertEquals(13, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(band));
		assertEquals(10, scn.GetStrength(band2));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(soldier));

		// Chieftain and Hound, 7 + 5 against 8, win.  The Soldier's own skirmish is another phase: the bonus is gone.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItIsNotOfferedUntilASixthCompanionComes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		// Frodo and four more: 5 companions, and an ally beside them.
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("merry"));
		scn.MoveAlliesToTable(barliman);

		var band = scn.GetShadowCard("band");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(band, chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(band));
		assertFalse(scn.ShadowActionAvailable(band));

		// A sixth companion: now it is offered.
		scn.MoveCompanionsToTable(sam);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(band));
		assertEquals(7, scn.GetStrength(chieftain));
	}

	@Test
	public void WhileTheBandIsExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("merry"), scn.GetFreepsCard("sam"));

		var band = scn.GetShadowCard("band");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(band, chieftain);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(band, 2);
		assertEquals(2, scn.GetWoundsOn(band));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(band));
		assertEquals(2, scn.GetWoundsOn(band));
	}

	@Test
	public void WithNoOtherSauronOrcItIsOfferedAndTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn, scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("merry"), scn.GetFreepsCard("sam"));

		var band = scn.GetShadowCard("band");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(band, hound, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(band));
		assertTrue(scn.ShadowActionAvailable(band));
		scn.ShadowUseCardAction(band);

		// Only a [wraith] Orc and a [sauron] Uruk-hai beside it: the exertion is paid and nobody is made stronger.
		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(10, scn.GetStrength(band));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
