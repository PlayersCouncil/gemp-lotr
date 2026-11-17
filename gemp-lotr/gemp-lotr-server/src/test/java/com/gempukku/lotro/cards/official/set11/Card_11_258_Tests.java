package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_258_Tests
{

	// The Fellowship-block path with Slag Mounds in place of site 9, which no test reaches: the Shadow player's
	// copy is put on the path as site 2 and the fellowship moves to it, so that a regroup phase follows the
	// skirmishes there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability for Hobbits
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow him
					// to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; overwhelms anyone of strength 6 or less
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its play trigger is not reached by a cheat
					put("despair", "7_148");    // Fierce in Despair: a Shadow Regroup event, twilight 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man minion; his text needs 2 burdens
					put("axeman2", "4_224");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("mounds", "11_258");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlagMoundsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Slag Mounds
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. Each time a companion is killed, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("mounds");

		assertEquals("Slag Mounds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void EachCompanionKilledAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var mounds = scn.GetShadowSite("mounds");
		scn.MoveCompanionsToTable(guard, boromir);

		scn.StartGame();
		scn.MoveCardToAdventurePath(mounds);
		scn.FreepsPass();

		assertSame(mounds, scn.GetCurrentSite());
		scn.MoveMinionsToTable(troop, brigand);
		// Boromir has one vitality left, so the wound for losing kills him.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{boromir, brigand});

		assertEquals(0, scn.FreepsGetBurdens());

		// A Dwarf overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(1, scn.FreepsGetBurdens());

		// A Man killed by his last wound: 8 beats 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, boromir);
		// On the Free Peoples player's Ring-bearer, whose site this is not.
		assertEquals(2, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledAMinionKilledAndACompanionWoundedAddNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("mounds"));
		scn.FreepsPass();

		assertEquals("Slag Mounds", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(troop, brigand, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, troop}, new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{boromir, brigand});

		assertEquals(0, scn.FreepsGetBurdens());

		// An ally overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.FreepsGetBurdens());

		// A minion killed by the wound for losing: 8 beats 5, vitality 1.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertEquals(0, scn.FreepsGetBurdens());

		// A companion who loses and lives: 8 beats 7, and Boromir has three vitality.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionDiscardedFromPlayIsNotKilledAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("mounds"));
		scn.FreepsPass();

		assertEquals("Slag Mounds", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.MoveCardsToHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		scn.ShadowChooseCard(guard);

		// Discarded from play is not killed (CR 4.2 "killed"): the dead pile stays empty and no burden is added.
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
