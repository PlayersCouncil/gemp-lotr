package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_011_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The event is played
	// in the archery phase at The Troll Hoard 30_50 (site 2), where none of the allies below is at home (3H, 5H, 6H),
	// so none takes part in anything unless a card allows it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "31_11");
					put("army", "31_8");        // Elf Army: [elven] Elf ally, archer, home 5H, strength 7, vitality 4; a Maneuver ability, not used
					put("scout", "33_16");      // Rivendell Scout: [elven] Elf ally, archer, home 3H, strength 6; a trigger on a companion healed
					put("lindir", "33_14");     // Lindir, Elven Minstrel: [elven] Elf ally who is no archer; a heal trigger on a move (nobody wounded)
					put("bard", "30_22");       // Bard, The Bowman: [esgaroth] Man ally, an archer of another culture; a Regroup ability, not used
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] archer COMPANION, not an ally; an archer, his archery ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, strength 7, damage +1; his ability not used
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability, not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, strength 4, vitality 2; a Shadow ability, not used
					put("footman", "30_42");    // Goblin Footman: [moria] Orc of vitality 1, exhausted from the start; a "when you play" trigger only
					put("spider", "31_51");     // Fat Spider: fierce [spider] minion, strength 8, vitality 2; a Regroup ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheEvilBecomesStrongerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: The Evil Becomes Stronger
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an [elven] archer ally to exert a minion. Until the regroup phase, that ally is strength +2 and participates in archery fire and skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil");

		assertEquals("The Evil Becomes Stronger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElvenArcherAllyExertsAMinionAndThatAllyIsStrengthPlus2AndTakesPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetFreepsCard("evil");
		var army = scn.GetFreepsCard("army");
		var scout = scn.GetFreepsCard("scout");
		var lindir = scn.GetFreepsCard("lindir");
		var bard = scn.GetFreepsCard("bard");
		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(army, scout, lindir, bard);
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, bats, footman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(7, scn.GetStrength(army));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(bats));
		// Legolas, an archer companion, adds 1; none of the allies does.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertFalse(scn.FreepsCanAssign(army));
		assertTrue(scn.FreepsPlayAvailable(evil));
		scn.FreepsPlayCard(evil);

		// [elven] archer allies of two homes; not an [elven] ally who is no archer, an archer ally of another
		// culture, nor an [elven] archer companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(army, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lindir, bard, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(army);

		// Minions of two cultures and races; not the exhausted Goblin Footman.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aggressor, bats));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(footman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bats);

		assertInDiscard(evil);
		assertEquals(1, scn.GetWoundsOn(bats));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(footman));
		// 7 + 2; the Scout, spotted by nobody, is untouched.
		assertEquals(9, scn.GetStrength(army));
		assertEquals(6, scn.GetStrength(scout));
		// The Army, an archer, takes part in archery fire: 1 + 1.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsCanAssign(army));
		assertFalse(scn.FreepsCanAssign(scout));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutAnElvenArcherAllyAndIsOnceOneIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetFreepsCard("evil");
		var army = scn.GetFreepsCard("army");
		var lindir = scn.GetFreepsCard("lindir");
		var bard = scn.GetFreepsCard("bard");
		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(lindir, bard);
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(evil));

		scn.MoveAlliesToTable(army);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(evil));
	}

	@Test
	public void WithEveryMinionExhaustedItExertsNothingAndTheAllyIsStillStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetFreepsCard("evil");
		var army = scn.GetFreepsCard("army");
		var footman = scn.GetShadowCard("footman");
		scn.MoveAlliesToTable(army);
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(7, scn.GetStrength(army));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(evil));
		scn.FreepsPlayCard(evil);

		assertInDiscard(evil);
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(9, scn.GetStrength(army));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ThatAllyIsStrongerAndTakesPartUntilTheRegroupPhaseAndNotAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetFreepsCard("evil");
		var army = scn.GetFreepsCard("army");
		var thorin = scn.GetFreepsCard("thorin");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(army);
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, bats, spider);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(7, scn.GetStrength(army));
		scn.FreepsPlayCard(evil);
		// The one [elven] archer ally is spotted unasked.
		scn.FreepsChooseCard(aggressor);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(9, scn.GetStrength(army));

		// The Army's archery wound, placed by the Shadow player, goes on the Bats.
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(bats);
		assertEquals(1, scn.GetWoundsOn(bats));

		// The fierce Fat Spider is left out of the first assignment.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{thorin, aggressor},
				new PhysicalCardImpl[]{army, bats});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();

		// The second skirmish of the turn, after one already resolved.
		scn.FreepsResolveSkirmish(army);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(army));
		scn.PassSkirmishActions();

		// The fierce assignment phase, still ahead of the regroup phase.
		assertInZone(Zone.SHADOW_CHARACTERS, spider);
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsCanAssign(army));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(army));
		scn.FreepsAssignAndResolve(army, spider);
		assertTrue(scn.IsCharSkirmishingAgainst(army, spider));
		assertEquals(9, scn.GetStrength(army));
		scn.PassSkirmishActions();

		// The regroup phase: all gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(army));
		assertFalse(scn.FreepsCanAssign(army));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
