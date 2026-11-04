package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destroyers", "18_78");
					// Denizen of Moria: an [orc] Orc, strength 7, vitality 1, cost 2; a bonus only at an underground site.
					put("denizen", "11_116");
					// Orkish Smith: an [orc] Orc, strength 6, vitality 1; an optional trigger on his own play only
					// (cheated in here).
					put("smith", "11_132");
					// Beastly Olog-hai: an [orc] Troll, damage +1 and fierce; an assignment ability never used.
					put("olog", "11_108");
					// Goblin Runner: a [moria] Orc, cost 1; when played, adds (2) unasked.
					put("runner1", "1_178");
					put("runner2", "1_178");

					// Aragorn, Heir of Elendil: a [gondor] Man, printed Defender +1 and no other text.
					put("aragorn", "4_109");
					// Arwen, Lady of Rivendell: an Elf ally.  "Aragorn is strength +1.  While Arwen is exhausted,
					// Aragorn is defender +1."  Her prevention of his wounds never comes up here.
					put("arwen", "102_6");
					// Gimli, Son of Glóin: a [dwarven] Dwarf, damage +1; a skirmish ability never used.
					put("gimli", "1_13");
					// Sam, Proper Poet: a [shire] Hobbit, vitality 4.  "Maneuver: Exert Sam twice to make him
					// defender +1 until the regroup phase."
					put("sam", "2_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DestroyersandUsurpersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Destroyers and Usurpers
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot an [orc] minion and fewer minions than companions, each companion loses all <b>defender</b> bonuses and cannot gain defender bonuses.<br><b>Maneuver:</b> Spot 2 [orc] minions to make the fellowship's current site gain <b>battleground</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var destroyers = scn.GetFreepsCard("destroyers");

		assertEquals("Destroyers and Usurpers", destroyers.getBlueprint().getTitle());
		assertNull(destroyers.getBlueprint().getSubtitle());
		assertFalse(destroyers.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, destroyers.getBlueprint().getSide());
		assertEquals(Culture.ORC, destroyers.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, destroyers.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(destroyers, Keyword.SUPPORT_AREA));
		assertEquals(1, destroyers.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCompanionLosesAllDefenderBonusesWhileAnOrcMinionIsSpottedAndThereAreFewerMinionsThanCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var destroyers = scn.GetShadowCard("destroyers");
		var denizen = scn.GetShadowCard("denizen");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToSupportArea(destroyers);
		scn.MoveCardsToHand(denizen, runner2);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Arwen is exhausted, and Aragorn has her defender +1 beside his own.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [moria] minion alone: no [orc] minion to spot (this condition is the [orc] card that is no minion).
		scn.MoveMinionsToTable(runner1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// An [orc] minion played: 2 minions against 3 companions (Frodo, Aragorn, Gimli), and both bonuses are gone.
		scn.ShadowPlayCard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen);
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// A third minion: 3 minions against 3 companions, Arwen being an ally and not counted, and both are back.
		scn.ShadowPlayCard(runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionCannotGainADefenderBonusWhileTheConditionHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var destroyers = scn.GetShadowCard("destroyers");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCompanionsToTable(sam, gimli);
		scn.MoveCardsToSupportArea(destroyers);

		scn.StartGame();
		// One [orc] minion, a Troll, against 3 companions.
		scn.MoveMinionsToTable(olog);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);

		// The two exertions are paid, and the bonus is not gained.
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAsManyMinionsAsCompanionsADefenderBonusIsGained() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var destroyers = scn.GetShadowCard("destroyers");
		var olog = scn.GetShadowCard("olog");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(destroyers);

		scn.StartGame();
		// The [orc] Troll and a [moria] Orc against 2 companions (Frodo and Sam): not fewer.
		scn.MoveMinionsToTable(olog, runner1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		scn.FreepsUseCardAction(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetKeywordCount(sam, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityMakesTheCurrentSiteABattlegroundUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var destroyers = scn.GetShadowCard("destroyers");
		var denizen = scn.GetShadowCard("denizen");
		var smith = scn.GetShadowCard("smith");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(destroyers);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, smith);
		scn.SkipToPhase(Phase.MANEUVER);

		// Ettenmoors, site 2, is a plains and no battleground; Bag End behind it is neither.
		PhysicalCardImpl site2 = scn.GetCurrentSite();
		PhysicalCardImpl site1 = scn.GetSite(1);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(destroyers));
		scn.ShadowUseCardAction(destroyers);
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, smith});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The second skirmish, after one has resolved: still a battleground.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		scn.PassSkirmishActions();

		// The first regroup decision: no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
	}

	@Test
	public void ManeuverAbilityIsOfferedWithAnOrcAndATrollAndAgainOnceTheSiteIsABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyers = scn.GetShadowCard("destroyers");
		var denizen = scn.GetShadowCard("denizen");
		var olog = scn.GetShadowCard("olog");
		scn.MoveCardsToSupportArea(destroyers);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, olog);
		scn.SkipToPhase(Phase.MANEUVER);
		PhysicalCardImpl site2 = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(destroyers));
		scn.ShadowUseCardAction(destroyers);
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));

		// A keyword offered to a site that has it already is still an offer (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(destroyers));
		scn.ShadowUseCardAction(destroyers);
		assertTrue(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOneOrcMinionAndAMinionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var destroyers = scn.GetShadowCard("destroyers");
		var denizen = scn.GetShadowCard("denizen");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(destroyers);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(destroyers));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
	}
}
