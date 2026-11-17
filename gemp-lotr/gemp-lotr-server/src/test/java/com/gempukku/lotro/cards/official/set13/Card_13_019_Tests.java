package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("letfly", "13_19");
					put("elf", "1_53");         // Lorien Elf: Elf companion; "To play, spot an Elf" is its whole text
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; +2 only at sites 6 to 8
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally; a skirmish ability never used here
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability never used here
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetFlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Let Fly
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot 3 Elves, spot a Shadow race, and choose a Shadow player to wound each of that player's minions which is not of that race.
		*/

		var scn = GetScenario();

		var letfly = scn.GetFreepsCard("letfly");

		assertEquals("Let Fly", letfly.getBlueprint().getTitle());
		assertNull(letfly.getBlueprint().getSubtitle());
		assertFalse(letfly.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, letfly.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, letfly.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, letfly.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(letfly, Timeword.ARCHERY));
		assertEquals(4, letfly.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeElvesSpottedEachMinionNotOfTheRaceSpottedIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var letfly = scn.GetFreepsCard("letfly");
		var elf = scn.GetFreepsCard("elf");
		var haldir = scn.GetFreepsCard("haldir");
		var golradir = scn.GetFreepsCard("golradir");
		var knight = scn.GetFreepsCard("knight");
		// Two Elf companions and an Elf ally: three Elves.
		scn.MoveCompanionsToTable(elf, haldir, knight);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(letfly);

		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertInZone(Zone.HAND, letfly);
		scn.MoveMinionsToTable(troop, soldier, savage, brigand);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(letfly));
		scn.FreepsPlayCard(letfly);

		// The race is spotted on a minion, any of them.  Not on a companion, though the Knight is a Man, a race
		// Shadow cards have too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, soldier, savage, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elf, knight, frodo, golradir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(soldier);

		// Orc spotted on the [sauron] Orc: both Orcs are spared, the [moria] one too.  The Uruk-hai is wounded and
		// the Man, of vitality 1, killed.
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInDiscard(letfly);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOnlyTwoElvesItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var letfly = scn.GetFreepsCard("letfly");
		var elf = scn.GetFreepsCard("elf");
		var haldir = scn.GetFreepsCard("haldir");
		var golradir = scn.GetFreepsCard("golradir");
		var knight = scn.GetFreepsCard("knight");
		// An Elf companion and an Elf ally, and a companion who is no Elf.
		scn.MoveCompanionsToTable(elf, knight);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(letfly);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInZone(Zone.HAND, letfly);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(letfly));

		// A third Elf.
		scn.MoveCompanionsToTable(haldir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(letfly));
	}
}
