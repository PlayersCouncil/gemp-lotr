package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("iorlas", "7_107");

					// Garrison of Osgiliath: unique [gondor] fortification condition; its tokens come as it is played (cheated
					// here), its skirmish ability is used only on purpose
					put("garrison", "6_52");
					// A Path Appointed: [gondor] support condition that takes [gondor] tokens; its abilities are used only on purpose
					put("path", "8_42");
					put("walls", "7_111");          // Man the Walls: [gondor] support condition that never holds a token
					// Stout and Strong: [dwarven] support condition that takes [dwarven] tokens as Dwarves win skirmishes
					put("stout", "4_57");
					put("sword", "4_134");          // Sword of Gondor: [gondor] possession, bearer must be a [gondor] Man; a standing damage bonus in skirmishes

					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, cost 2; a skirmish ability used only on purpose
					put("pike", "7_265");           // Besieging Pike: [sauron] possession (no engine), cost 0, on a [sauron] Orc; a standing strength bonus
					put("towers", "7_281");         // Great Siege-towers: [sauron] ENGINE condition (no possession), cost 1, support area
					put("fear", "1_278");           // Strength Born of Fear: [sauron] support condition, cost 1, no engine
					put("ithil", "9_47");           // Ithil Stone: [sauron] ARTIFACT (no possession, no engine), cost 0, support area; to play, exert a [sauron] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IorlasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Iorlas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Response:</b> If a possession or engine is played by your opponent, remove a [gondor] token from your condition to make that opponent remove (1). If he or she cannot, discard that possession or engine.
		*/

		var scn = GetScenario();

		var iorlas = scn.GetFreepsCard("iorlas");

		assertEquals("Iorlas", iorlas.getBlueprint().getTitle());
		assertNull(iorlas.getBlueprint().getSubtitle());
		assertTrue(iorlas.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, iorlas.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, iorlas.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, iorlas.getBlueprint().getCardType());
		assertEquals(Race.MAN, iorlas.getBlueprint().getRace());
		assertEquals(2, iorlas.getBlueprint().getTwilightCost());
		assertEquals(6, iorlas.getBlueprint().getStrength());
		assertEquals(3, iorlas.getBlueprint().getVitality());
		assertEquals(6, iorlas.getBlueprint().getResistance());
		assertNull(iorlas.getBlueprint().getSignet());
	}

	@Test
	public void ResponseToAPossessionPlayedRemovesATokenFromYourConditionAndTheOpponentRemovesOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iorlas = scn.GetFreepsCard("iorlas");
		var garrison = scn.GetFreepsCard("garrison");
		var path = scn.GetFreepsCard("path");
		var walls = scn.GetFreepsCard("walls");
		var stout = scn.GetFreepsCard("stout");
		var soldier = scn.GetShadowCard("soldier");
		var pike = scn.GetShadowCard("pike");
		scn.MoveCompanionsToTable(iorlas);
		scn.MoveCardsToSupportArea(garrison, path, walls, stout);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(stout, 1);
		scn.MoveCardsToHand(pike);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(walls));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInHand(pike);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());

		// The Pike costs 0, and goes on the one [sauron] Orc unasked.
		scn.ShadowPlayCard(pike);
		assertAttachedTo(pike, soldier);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(iorlas));

		scn.FreepsChooseOptionalTrigger(iorlas);
		// A [gondor] token from a condition of his: either [gondor] condition holding one; not the one with none, nor
		// the condition holding a [dwarven] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, path));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walls, stout));
		scn.FreepsChooseCard(garrison);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		// The opponent can remove (1), so he does, unasked, and only 1; the possession stays.
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(pike, soldier);

		// A response may be used again on the same play while its cost can be paid (CR 4.2 "response"). With exactly
		// (1) left the opponent can still remove it, so the possession stays.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(iorlas));
		scn.FreepsChooseOptionalTrigger(iorlas);
		scn.FreepsChooseCard(path);
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(pike, soldier);

		// Offered a third time (a token is left on the Garrison): declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(iorlas));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertAttachedTo(pike, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedForAMinionAConditionOrAnArtifactAndAnEngineLeftWithNoTwilightIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iorlas = scn.GetFreepsCard("iorlas");
		var path = scn.GetFreepsCard("path");
		var soldier = scn.GetShadowCard("soldier");
		var fear = scn.GetShadowCard("fear");
		var towers = scn.GetShadowCard("towers");
		var ithil = scn.GetShadowCard("ithil");
		scn.MoveCompanionsToTable(iorlas);
		scn.MoveCardsToSupportArea(path);
		scn.AddTokensToCard(path, 2);
		scn.MoveCardsToHand(soldier, fear, towers, ithil);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInHand(soldier, fear, towers, ithil);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// A minion played: no possession or engine, nothing offered.
		scn.ShadowPlayCard(soldier);
		assertInPlay(soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow condition that is no engine: nothing offered.
		scn.ShadowPlayCard(fear);
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(path));

		// A Shadow artifact, which is neither a possession nor an engine: nothing offered (its cost exerts the Soldier).
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.ShadowPlayCard(ithil);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SUPPORT, ithil);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(path));

		// An engine that is no possession, played with exactly its cost of 1 in the pool, so none is left.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetTwilight());
		scn.ShadowPlayCard(towers);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(iorlas));

		// The one condition holding a token pays unasked; the opponent cannot remove (1), so the engine is discarded.
		scn.FreepsChooseOptionalTrigger(iorlas);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInDiscard(towers);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fear);

		// With a token still on the condition the response is offered again for the same play (CR 4.2 "response";
		// a response with a cost stays on offer): declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(iorlas));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGondorTokenOnAConditionOfYours() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iorlas = scn.GetFreepsCard("iorlas");
		var walls = scn.GetFreepsCard("walls");
		var stout = scn.GetFreepsCard("stout");
		var soldier = scn.GetShadowCard("soldier");
		var pike = scn.GetShadowCard("pike");
		scn.MoveCompanionsToTable(iorlas);
		// A [gondor] condition with no token, and a condition holding a token of another culture.
		scn.MoveCardsToSupportArea(walls, stout);
		scn.AddTokensToCard(stout, 2);
		scn.MoveCardsToHand(pike);
		assertEquals(0, scn.GetCultureTokensOn(walls));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertInHand(pike);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(pike);
		assertAttachedTo(pike, soldier);
		// Nothing asked of the Free Peoples player: the Shadow player goes on.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(stout));
	}

	@Test
	public void NotOfferedForAPossessionTheFreePeoplesPlayerPlaysHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iorlas = scn.GetFreepsCard("iorlas");
		var path = scn.GetFreepsCard("path");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(iorlas);
		scn.MoveCardsToSupportArea(path);
		scn.AddTokensToCard(path, 1);
		scn.MoveCardsToHand(sword);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertInHand(sword);

		scn.StartGame();

		// His own possession, on the one [gondor] Man unasked: not played by an opponent.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, iorlas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(path));
	}
}
