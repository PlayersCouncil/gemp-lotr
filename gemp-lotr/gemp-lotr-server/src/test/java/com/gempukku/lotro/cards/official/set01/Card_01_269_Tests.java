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

public class Card_01_269_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "1_269");
					put("orc", "1_271");        // Orc Soldier: a [sauron] Orc, strength 7
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar1", "1_269");
					put("scimitar2", "1_269");  // not unique: "bearer" is read beside the bearer of another copy
					put("inquisitor1", "1_268"); // Orc Inquisitor: [sauron] Orc, strength 9, vitality 3; text only as he is played
					put("inquisitor2", "1_268");
					put("hunter", "1_256");     // Morgul Hunter: [sauron] Orc, cost 5, vitality 4; strength +1 for each companion
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text only on a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is an Uruk-hai; Damage +1 is its whole text
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; his text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability used only on purpose
					put("dwarfguard", "1_7");   // Dwarf Guard: companion, strength 4, vitality 2, no text in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [sauron] Orc.<br><b>Skirmish:</b> Remove (1) to make bearer strength +1 (limit +3).
		*/

		var scn = GetScenario();

		var scimitar = scn.GetFreepsCard("scimitar");

		assertEquals("Orc Scimitar", scimitar.getBlueprint().getTitle());
		assertNull(scimitar.getBlueprint().getSubtitle());
		assertFalse(scimitar.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, scimitar.getBlueprint().getSide());
		assertEquals(Culture.SAURON, scimitar.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, scimitar.getBlueprint().getCardType());
		assertTrue(scimitar.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, scimitar.getBlueprint().getTwilightCost());
		assertEquals(2, scimitar.getBlueprint().getStrength());
	}

	@Test
	public void SkirmishAbilityAtItsLimitIsStillOfferedAndPaidForAndAddsNoStrength() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(orc);
		scn.AttachCardsTo(orc, scimitar);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, orc);
		scn.FreepsPass();

		// 7, +2 for the scimitar itself
		assertEquals(9, scn.GetStrength(orc));
		for (int used = 0; used < 3; used++) {
			assertEquals("false", scn.ShadowActionNoEffect(scimitar));
			scn.ShadowUseCardAction(scimitar);
			assertEquals(9 + used + 1, scn.GetStrength(orc));
			scn.FreepsPass();
		}

		// Limit +3: (1) can still be removed, so the ability is still offered, flagged as doing nothing
		// (Ketura on Q-0041).
		var twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(scimitar));
		assertEquals("limit", scn.ShadowActionNoEffect(scimitar));

		scn.ShadowUseCardAction(scimitar);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(orc));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerMustBeASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var scimitar1 = scn.GetShadowCard("scimitar1");
		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var hunter = scn.GetShadowCard("hunter");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(scimitar1);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor1, hunter, guard, hound);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(scimitar1));
		scn.ShadowPlayCard(scimitar1);

		// Two [sauron] Orcs; not the [sauron] Uruk-hai nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(inquisitor1, hunter));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(inquisitor1);

		assertAttachedTo(scimitar1, inquisitor1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishRemovesOneTwilightToMakeBearerAndNoOtherMinionStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var scimitar1 = scn.GetShadowCard("scimitar1");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor1, inquisitor2, chieftain);
		scn.AttachCardsTo(inquisitor1, scimitar1);
		scn.AttachCardsTo(inquisitor2, scimitar2);
		assertAttachedTo(scimitar1, inquisitor1);
		assertAttachedTo(scimitar2, inquisitor2);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, inquisitor1}, new PhysicalCardImpl[]{boromir, inquisitor2});
		// A minion who bears no Scimitar in the same skirmish, put there by the Shadow player.
		scn.ShadowAssignToMinions(aragorn, chieftain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		// 9, +2 for the Scimitar itself
		assertEquals(11, scn.GetStrength(inquisitor1));
		assertEquals(11, scn.GetStrength(inquisitor2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.ShadowActionAvailable(scimitar1));
		scn.ShadowUseCardAction(scimitar1);

		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(inquisitor1));
		assertEquals(11, scn.GetStrength(inquisitor2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The text names no skirmish: the bearer whose skirmish is still to come is offered it too, and is the only
		// one it strengthens.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(scimitar2));
		scn.ShadowUseCardAction(scimitar2);
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(inquisitor2));
		assertEquals(12, scn.GetStrength(inquisitor1));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var scimitar1 = scn.GetShadowCard("scimitar1");
		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor1);
		scn.AttachCardsTo(inquisitor1, scimitar1);
		assertAttachedTo(scimitar1, inquisitor1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, inquisitor1);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scimitar1));
		assertEquals(11, scn.GetStrength(inquisitor1));
	}

	@Test
	public void TheBonusAndTheLimitLastOneSkirmishPhaseAndAnotherCopyHasItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBearerScenario();

		var scimitar1 = scn.GetShadowCard("scimitar1");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(aragorn, dwarfguard);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor1, inquisitor2);
		scn.AttachCardsTo(inquisitor1, scimitar1);
		scn.AttachCardsTo(inquisitor2, scimitar2);
		assertAttachedTo(scimitar1, inquisitor1);
		assertAttachedTo(scimitar2, inquisitor2);
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, inquisitor1}, new PhysicalCardImpl[]{dwarfguard, inquisitor2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The second bearer, waiting for its own skirmish, takes its +3 in the first.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(11, scn.GetStrength(inquisitor2));
		scn.ShadowUseCardAction(scimitar2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(scimitar2);
		scn.FreepsPass();
		scn.ShadowUseCardAction(scimitar2);
		assertEquals(14, scn.GetStrength(inquisitor2));
		scn.FreepsPass();

		// The limit is each copy's own (CR 4.2 "limit"): the other Scimitar still gives its bearer +1.
		assertEquals(11, scn.GetStrength(inquisitor1));
		scn.ShadowUseCardAction(scimitar1);
		assertEquals(12, scn.GetStrength(inquisitor1));
		assertEquals(14, scn.GetStrength(inquisitor2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();
		scn.ShadowPass();

		// The first bearer, 12, beats Aragorn, 8: one wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(dwarfguard);

		// A skirmish is a phase of its own: the +3 is gone, and the limit is fresh.
		assertEquals(11, scn.GetStrength(inquisitor2));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(scimitar2));
		scn.ShadowUseCardAction(scimitar2);
		assertEquals(12, scn.GetStrength(inquisitor2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
