package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_304_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager", "7_304");
					put("rager2", "7_304");     // a second copy: another [sauron] Orc, and not the one discarded
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon PLAYED on him only
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5; its text needs 2 wounds on the Ring-bearer
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai (no Orc), strength 9; damage +1 only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcRagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Rager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Discard this minion to make another [sauron] Orc strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rager");

		assertEquals("Orc Rager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ItIsDiscardedToMakeAnotherSauronOrcStrengthPlus4ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var rager = scn.GetShadowCard("rager");
		var rager2 = scn.GetShadowCard("rager2");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(rager, rager2, chieftain, soldier, hound, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{boromir, soldier},
				new PhysicalCardImpl[]{gimli, guard});
		// The Hound joins the Chieftain against Aragorn; the two Ragers are left unassigned.
		scn.ShadowAssignToMinions(aragorn, hound);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, hound));
		assertTrue(scn.IsCharAssignedAgainst(boromir, soldier));
		assertFalse(scn.IsCharAssigned(rager));
		assertFalse(scn.IsCharAssigned(rager2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.ShadowActionAvailable(rager));

		scn.ShadowUseCardAction(rager);
		// This copy is discarded, not the other.
		assertInZone(Zone.DISCARD, rager);
		assertInZone(Zone.SHADOW_CHARACTERS, rager2);
		// Another [sauron] Orc, in this skirmish or waiting for his own, the other copy among them; not the
		// [wraith] Orc, not the [sauron] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier, rager2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, guard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		// 7 + 4
		assertEquals(11, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(rager2));

		// Still so at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(soldier));

		// Chieftain and Hound, 7 + 5 against 8, win.  The Soldier's own skirmish is another phase: the bonus is gone.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItIsNotOfferedInTheShadowPlayersAssignmentActionsButIsInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rager = scn.GetShadowCard("rager");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(rager, chieftain);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The phase next to its own in which the Shadow player is asked for actions.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rager));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rager));
		assertInZone(Zone.SHADOW_CHARACTERS, rager);
	}

	@Test
	public void WithNoOtherSauronOrcItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rager = scn.GetShadowCard("rager");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(rager, hound, guard);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, rager);
		assertTrue(scn.ShadowActionAvailable(rager));
		scn.ShadowUseCardAction(rager);

		// Only a [wraith] Orc and a [sauron] Uruk-hai beside it: the Rager is discarded and nobody is made stronger.
		assertInZone(Zone.DISCARD, rager);
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
