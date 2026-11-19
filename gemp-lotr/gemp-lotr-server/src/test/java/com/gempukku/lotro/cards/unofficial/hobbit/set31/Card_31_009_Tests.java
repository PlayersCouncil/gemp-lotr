package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_31_009_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  Legolas's home is
	// site 5, Mirkwood 30_53 (a forest), where he is an archer who fires; at every other site he adds nothing.
	// The path's other forest is The Troll Hoard 30_50 (site 2, also underground), its rivers Esgaroth 30_54
	// (site 6, a sanctuary) and Erebor 30_55 (site 7, a mountain); Goblin Town 30_52 (site 4, underground) is neither.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "31_9");
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability, not used
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7, vitality 2; archer only at site 9, a Regroup ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, a minion that is no Orc; a Shadow ability, not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Legolas, Prince of Mirkwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 5H
		 * Strength: 6
		 * Vitality: 3
		 * Game Text: <b>Archer</b>.<br><b>Archery:</b> Spot an Orc and exert Legolas to make the fellowship archery total +1 (or +2 if at a river or forest).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Prince of Mirkwood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 5)));
	}

	@Test
	public void ArcheryAbilityExertsLegolasToMakeTheArcheryTotalPlus1ForThePhaseAwayFromARiverOrForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(4, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		// Away from home Legolas adds nothing to the total himself.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);

		// The Orc is spotted, not exerted.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// The fellowship's one archery wound goes on the only minion; once the archery phase is over the +1 is gone.
		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void AtMirkwoodAForestTheArcheryTotalIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(5, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(legolas));
		// At his home site Legolas, an archer, adds 1 himself.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		// 1 + 2
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AtTheTrollHoardAForestTheArcheryTotalIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AtEsgarothARiverTheArcheryTotalIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(6, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		// Spotted on a [sauron] Orc alone.
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AtEreborARiverTheArcheryTotalIsPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(7, scn.GetCurrentSiteNumber());

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutAnOrcToSpotAndIsOnceOneIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveAlliesToTable(legolas);

		scn.StartGame();
		// A minion that is no Orc.
		scn.MoveMinionsToTable(bats);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));

		scn.MoveMinionsToTable(aggressor);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(legolas));
	}

	@Test
	public void IsNotOfferedWhenLegolasIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveAlliesToTable(legolas);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}
}
