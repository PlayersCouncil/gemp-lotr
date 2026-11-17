package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("field", "4_72");
					put("field2", "4_72");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					// Legolas, Companion of the Ring: Elf companion; stronger only at a battleground or forest site
					put("legolas", "11_21");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("stairs", "1_24");      // Stairs of Khazad-dum: a Free Peoples support condition; +1 only to a Dwarf against a [moria] minion

					// Black Rider: [wraith] Nazgul, printed fierce, not unique; strength +1 for each companion
					put("rider1", "12_161");
					put("rider2", "12_161");
					// White Hand Destroyer: [uruk-hai] Uruk-hai, printed fierce; hunter only for sites its player controls
					put("destroyer", "17_124");
					// Uruk Pursuer: printed fierce, strength 5, vitality 3; its bar and its response reach no Hobbit here
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KillingFieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Killing Field
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 Elves. Plays to your support area.
		 * Each wounded minion is prevented from being <b>fierce</b>. Discard this condition during the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("field");

		assertEquals("Killing Field", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithTwoElvesButCanOnceAnElfAllyIsPlayedAsTheThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		// Two Elf companions, and Gimli and Frodo, who are no Elves.
		scn.MoveCompanionsToTable(arwen, legolas, gimli);
		scn.MoveCardsToHand(field, celeborn);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(field, celeborn);
		assertFalse(scn.FreepsPlayAvailable(field));

		// An Elf ally is the third Elf.
		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(field));
		scn.FreepsPlayCard(field);
		assertInZone(Zone.SUPPORT, field);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWoundedMinionIsNotFierceAndAnUnwoundedOneStillIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(field);

		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var destroyer = scn.GetShadowCard("destroyer");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, field);
		scn.MoveMinionsToTable(rider1, rider2, destroyer);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Unwounded, each is fierce as printed.
		assertTrue(scn.HasKeyword(rider1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider2, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(destroyer, Keyword.FIERCE));

		// A [wraith] Nazgul and an [uruk-hai] Uruk-hai, wounded: neither is fierce.  The other Black Rider, with
		// no wound, still is.
		scn.AddWoundsToChar(rider1, 1);
		scn.AddWoundsToChar(destroyer, 1);
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertEquals(1, scn.GetWoundsOn(destroyer));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertFalse(scn.HasKeyword(rider1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(destroyer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider2, Keyword.FIERCE));
	}

	@Test
	public void AFierceMinionWoundedInItsSkirmishHasNoFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(field);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, field);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));

		// Gimli, 6, beats the Pursuer, 5: 2 wounds (damage +1) of its 3.  Wounded, it is no longer fierce, so no
		// fierce assignment phase follows and the turn goes on to the regroup phase.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// The condition has gone as the regroup phase began, and the Pursuer is fierce again.
		assertInDiscard(field);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
	}

	@Test
	public void StaysThroughTheSkirmishAndIsDiscardedAsTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var field = scn.GetFreepsCard("field");
		var field2 = scn.GetFreepsCard("field2");
		var stairs = scn.GetFreepsCard("stairs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(field, field2, stairs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, field, field2, stairs);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// In the skirmish, the phase before regroup: both copies still in play.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, field, field2);
		scn.PassSkirmishActions();

		// The Troop (13) beats Aragorn (8).  As the regroup phase starts each copy is discarded; the other Free
		// Peoples support condition stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(field, field2);
		assertInZone(Zone.SUPPORT, stairs);
	}
}
