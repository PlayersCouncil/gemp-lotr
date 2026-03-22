package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_014_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Desert Lord (4_246): Raider Man minion, twilight 4
		put("desert", "4_246");
		// Southron Troop (7_140): Raider Man minion, twilight 4
		put("troop", "7_140");
		// Corsair Lookout (8_55): Raider Man minion, twilight 3
		put("corsair", "8_55");
		// Goblin Runner (1_178): Moria Orc minion, twilight 0 — NOT a raider
		put("runner", "1_178");
		// Sam, Son of Hamfast (1_311): Shire companion
		put("sam", "1_311");
	}};

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_14"
		);
	}

	protected VirtualTableScenario GetSamScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SamRB,
				VirtualTableScenario.RulingRing,
				null, "92_14"
		);
	}

	protected VirtualTableScenario GetOpponentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_14", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("mod");

		assertEquals("Race Text 92_14", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void DiscardsSamAfterThreeRaidersPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var desert = scn.GetShadowCard("desert");
		var troop = scn.GetShadowCard("troop");
		var corsair = scn.GetShadowCard("corsair");
		var runner = scn.GetShadowCard("runner");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(desert, troop, corsair, runner);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// Not yet available — no raiders played this phase
		assertFalse(scn.ShadowActionAvailable(mod));

		// 1 Raider played
		scn.ShadowPlayCard(desert);
		assertFalse(scn.ShadowActionAvailable(mod));

		// 2 Raiders played
		scn.ShadowPlayCard(troop);
		assertFalse(scn.ShadowActionAvailable(mod));

		// 2 Raiders + another minion played
		scn.ShadowPlayCard(runner);
		assertFalse(scn.ShadowActionAvailable(mod));

		// Now 3 raiders played this phase — ability should be available
		scn.ShadowPlayCard(corsair);
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		assertInZone(Zone.DISCARD, sam);
	}

	@Test
	public void CountResetsAcrossShadowPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mod = scn.GetShadowCard("mod");
		var desert = scn.GetShadowCard("desert");
		var troop = scn.GetShadowCard("troop");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(desert, troop);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToFreepsDiscard("runner");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// Play 2 raiders this phase
		scn.ShadowPlayCard(desert);
		scn.ShadowPlayCard(troop);
		assertFalse(scn.ShadowActionAvailable(mod));

		// Move to next turn's shadow phase
		scn.ShadowPassCurrentPhaseAction();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();

		// New shadow phase — count should have reset, and we have no new raiders to play
		scn.MoveMinionsToTable(desert, troop);
		scn.SkipToPhase(Phase.SHADOW);
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void DoesNotDiscardRingBearerSam() throws DecisionResultInvalidException, CardNotFoundException {
		// If Sam is the ring-bearer, he should not be a valid target
		var scn = GetSamScenario();

		var mod = scn.GetShadowCard("mod");
		var desert = scn.GetShadowCard("desert");
		var troop = scn.GetShadowCard("troop");
		var corsair = scn.GetShadowCard("corsair");
		var sam = scn.GetRingBearer();

		scn.MoveCardsToHand(desert, troop, corsair);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(desert);
		scn.ShadowPlayCard(troop);
		scn.ShadowPlayCard(corsair);

		// Sam is ring-bearer — ability should not be available
		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OpponentCanUseAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOpponentScenario();

		var mod = scn.GetFreepsCard("mod");
		var desert = scn.GetShadowCard("desert");
		var troop = scn.GetShadowCard("troop");
		var corsair = scn.GetShadowCard("corsair");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCardsToHand(desert, troop, corsair);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(desert);
		scn.ShadowPlayCard(troop);
		scn.ShadowPlayCard(corsair);

		assertTrue(scn.ShadowActionAvailable(mod));
	}
}
