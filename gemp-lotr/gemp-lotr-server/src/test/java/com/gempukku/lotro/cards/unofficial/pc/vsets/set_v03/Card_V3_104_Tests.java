package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dear", "103_104");       // Was He Dear to Thee?
					put("sauron", "9_48");        // Sauron - unique [Sauron] minion
					put("witchking", "1_237");    // The Witch-king - unique [Ringwraith] minion
					put("slayer", "3_93");        // Morgul Slayer - non-unique [Sauron]
					put("runner", "1_178");       // Goblin Runner - [Moria], non-unique

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WasHeDeartoTheeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Was He Dear to Thee?
		 * Unique: false
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a unique [sauron] or [ringwraith] minion and hinder the Ring-bearer to make each minion strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dear");

		assertEquals("Was He Dear to Thee?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}


	@Test
	public void DearRequiresUniqueSauronOrRingwraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(dear);
		scn.MoveMinionsToTable(slayer, runner);  // Non-unique minions only

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(dear));
	}

	@Test
	public void DearCannotPlayIfRingBearerAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToHand(dear);
		scn.MoveMinionsToTable(sauron);
		scn.HinderCard(frodo);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Can't hinder already-hindered Ring-bearer
		assertFalse(scn.ShadowPlayAvailable(dear));
	}

	@Test
	public void DearHindersRingBearerAndPumpsAllMinionsUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dear = scn.GetShadowCard("dear");
		var sauron = scn.GetShadowCard("sauron");
		var slayer = scn.GetShadowCard("slayer");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();

		scn.MoveCardsToHand(dear);
		scn.MoveMinionsToTable(sauron, slayer, runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int sauronStr = scn.GetStrength(sauron);
		int slayerStr = scn.GetStrength(slayer);
		int runnerStr = scn.GetStrength(runner);
		assertFalse(scn.IsHindered(frodo));

		assertTrue(scn.ShadowPlayAvailable(dear));
		scn.ShadowPlayCard(dear);
		// Sauron auto-selected as only unique [Sauron]/[Ringwraith]
		// Ring-bearer auto-selected

		assertTrue(scn.IsHindered(frodo));
		assertEquals(1, scn.GetWoundsOn(sauron));

		scn.SkipToAssignments();

		// ALL minions get +2, including [Moria] runner
		// Sauron is enduring so he gets +2 more
		assertEquals(sauronStr + 4, scn.GetStrength(sauron));
		assertEquals(slayerStr + 2, scn.GetStrength(slayer));
		assertEquals(runnerStr + 2, scn.GetStrength(runner));

		// Advance to Regroup - pump should expire
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		// Sauron is enduring so he gets +2 more
		assertEquals(sauronStr + 2, scn.GetStrength(sauron));
		assertEquals(slayerStr, scn.GetStrength(slayer));
		assertEquals(runnerStr, scn.GetStrength(runner));
	}

}
