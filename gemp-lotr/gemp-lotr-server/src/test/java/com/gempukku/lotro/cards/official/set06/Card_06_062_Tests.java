package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fumes", "6_62");

					// [isengard] Orcs of five titles, each of site number 4. Their abilities are never used.
					put("forger", "3_56");      // Isengard Forger: "Shadow: Exert this minion to add (1)."
					put("worker", "3_62");      // Isengard Worker: a regroup ability that exerts it
					put("servant", "3_58");     // Isengard Servant: a regroup ability that exerts it
					put("retainer", "3_57");    // Isengard Retainer: a regroup ability that exerts it
					put("wargrider", "5_67");   // Warg-rider: fierce while not exhausted; nobody is assigned to him
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is an Uruk-hai, no other text
					put("runner", "1_178");     // Goblin Runner: an Orc of [moria]; its play trigger is not set off by a cheat

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability only
					// The Saga of Elendil: vitality +1 to its [gondor] bearer; its skirmish ability is never used.
					// Aragorn bearing it has 5 vitality, so four wounds leave him alive and a fifth would not.
					put("saga", "1_114");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; texts used only on purpose
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2; a fellowship ability only
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresandFoulFumesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Fires and Foul Fumes
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Discard 4 [isengard] Orcs to wound a companion 4 times (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var fumes = scn.GetFreepsCard("fumes");

		assertEquals("Fires and Foul Fumes", fumes.getBlueprint().getTitle());
		assertNull(fumes.getBlueprint().getSubtitle());
		assertFalse(fumes.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fumes.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, fumes.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fumes.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fumes, Timeword.REGROUP));
		assertEquals(4, fumes.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsFourIsengardOrcsToWoundACompanionFourTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(aragorn, saga);
		// Gimli, vitality 3, is exhausted: he can still be wounded.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(saga, aragorn);

		var fumes = scn.GetShadowCard("fumes");
		var forger = scn.GetShadowCard("forger");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		var wargrider = scn.GetShadowCard("wargrider");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(fumes);

		scn.StartGame();
		assertInZone(Zone.HAND, fumes);
		scn.MoveMinionsToTable(forger, worker, servant, retainer, wargrider, savage, runner);
		// Nobody is assigned to them, so all seven are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, forger, worker, servant, retainer, wargrider, savage, runner);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(fumes));
		scn.ShadowPlayCard(fumes);

		// Exactly four of the five [isengard] Orcs; not the [isengard] Uruk-hai nor the [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(forger, worker, servant, retainer, wargrider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(forger, worker, servant, retainer);

		assertInZone(Zone.DISCARD, forger, worker, servant, retainer);
		assertInZone(Zone.SHADOW_CHARACTERS, wargrider, savage, runner);

		// Any companion but the Ring-bearer, an exhausted one included; not an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, wargrider));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// Four wounds, all on the one companion chosen.
		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInZone(Zone.DISCARD, fumes);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionChosenIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		var fumes = scn.GetShadowCard("fumes");
		var forger = scn.GetShadowCard("forger");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(fumes);

		scn.StartGame();
		assertInZone(Zone.HAND, fumes);
		scn.MoveMinionsToTable(forger, worker, servant, retainer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, forger, worker, servant, retainer);
		assertTrue(scn.ShadowPlayAvailable(fumes));
		scn.ShadowPlayCard(fumes);

		// Four Orcs, the only four: nothing is asked of them.
		assertInZone(Zone.DISCARD, forger, worker, servant, retainer);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		// A wound and not an exertion: the exhausted Gimli is killed.
		scn.ShadowChooseCard(gimli);
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, fumes);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerAndAnAllyItIsPlayedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);

		var fumes = scn.GetShadowCard("fumes");
		var forger = scn.GetShadowCard("forger");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(fumes);

		scn.StartGame();
		assertInZone(Zone.HAND, fumes);
		scn.MoveMinionsToTable(forger, worker, servant, retainer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, forger, worker, servant, retainer);
		assertTrue(scn.ShadowPlayAvailable(fumes));
		scn.ShadowPlayCard(fumes);

		assertInZone(Zone.DISCARD, forger, worker, servant, retainer);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInZone(Zone.DISCARD, fumes);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithOnlyThreeIsengardOrcs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fumes = scn.GetShadowCard("fumes");
		var forger = scn.GetShadowCard("forger");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var retainer = scn.GetShadowCard("retainer");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(fumes);

		scn.StartGame();
		assertInZone(Zone.HAND, fumes);
		// Three [isengard] Orcs, with an [isengard] Uruk-hai and a [moria] Orc beside them.
		scn.MoveMinionsToTable(forger, worker, servant, savage, runner);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertInZone(Zone.HAND, fumes);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fumes));

		// A fourth [isengard] Orc.
		scn.MoveMinionsToTable(retainer);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(fumes));
	}
}
