package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauding", "11_126");
					put("marauding2", "11_126");
					put("denizen", "11_116");     // Denizen of Moria: an [orc] Orc of vitality 1; strength +3 at an underground site, which site 2 is not
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc; its start-of-assignment trigger is never reached here
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");    // The Beckoning Shadow: an [orc] condition, no minion; its corruption needs 5 burdens
					put("defend", "4_115");       // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the [gondor] Man who exerts for it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MaraudingOrcsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Marauding Orcs
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you cannot spot another [orc] minion, discard this minion from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauding");

		assertEquals("Marauding Orcs", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithNoOtherOrcMinionItIsDiscardedThoughAnOrcConditionAndAMoriaOrcAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauding = scn.GetShadowCard("marauding");
		var beckoning = scn.GetShadowCard("beckoning");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(marauding);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);
		assertInHand(marauding);
		assertInPlay(beckoning, troop);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(marauding));
		scn.ShadowPlayCard(marauding);

		// "Another": the Orcs themselves do not count, and neither the [orc] condition nor the [moria] Orc does.
		assertInDiscard(marauding);
		assertInPlay(beckoning, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedBesideAnOrcMinionWhoIsNoOrcItStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauding = scn.GetShadowCard("marauding");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(marauding);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		scn.SetTwilight(10);
		assertInHand(marauding);
		assertInPlay(halftroll);
		scn.FreepsPass();

		scn.ShadowPlayCard(marauding);

		assertInZone(Zone.SHADOW_CHARACTERS, marauding, halftroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedWhenTheOtherOrcMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var marauding = scn.GetShadowCard("marauding");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(marauding, denizen);
		scn.SkipToPhase(Phase.MANEUVER);

		// While the Denizen is in play, the Marauding Orcs stay.
		assertInZone(Zone.SHADOW_CHARACTERS, marauding, denizen);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(marauding, denizen));
		scn.FreepsChooseCard(denizen);

		// The Denizen, vitality 1, is killed; with no other [orc] minion to spot, the Marauding Orcs are discarded from play.
		assertInDiscard(denizen, marauding);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoCopiesKeepEachOtherInPlayWhenTheOtherOrcMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var marauding = scn.GetShadowCard("marauding");
		var marauding2 = scn.GetShadowCard("marauding2");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(marauding, marauding2, denizen);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SHADOW_CHARACTERS, marauding, marauding2, denizen);

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(marauding, marauding2, denizen));
		scn.FreepsChooseCard(denizen);

		// The Denizen is killed; each copy is another [orc] minion to the other, so both stay.
		assertInDiscard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, marauding, marauding2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
