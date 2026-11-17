package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_131_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("miscreant", "11_131");
					put("miscreant2", "11_131");
					// Scurrying Goblin: an [orc] Orc, cost 1.  When played at an underground site beside another [orc]
					// minion, may add (2); played here away from one.
					put("goblin", "11_137");
					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its start-of-assignment trigger is
					// never reached.
					put("halftroll", "14_12");
					// The Beckoning Shadow: an [orc] condition, no minion.  Cheated into play (to play, exert an [orc]
					// minion); its other text needs 5 burdens.
					put("beckoning", "12_83");
					// Goblin Runner: a [moria] Orc, cheated into play so that its "may add (2)" is not asked
					put("runner", "1_178");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcMiscreantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Orc Miscreant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] minion.<br><b>Regroup:</b> Exert this minion to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("miscreant");

		assertEquals("Orc Miscreant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miscreant = scn.GetShadowCard("miscreant");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(miscreant, goblin);

		scn.StartGame();
		// An [orc] card that is no minion, and an Orc minion of another culture.
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(miscreant));

		// The Scurrying Goblin, an [orc] Orc, is played, and the Miscreant can be.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertTrue(scn.ShadowPlayAvailable(miscreant));

		scn.ShadowPlayCard(miscreant);
		assertInZone(Zone.SHADOW_CHARACTERS, miscreant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(miscreant);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(miscreant));

		scn.ShadowPlayCard(miscreant);
		assertInZone(Zone.SHADOW_CHARACTERS, miscreant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsThisMinionToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miscreant = scn.GetShadowCard("miscreant");
		var miscreant2 = scn.GetShadowCard("miscreant2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, miscreant2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetWoundsOn(miscreant2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(miscreant));

		scn.ShadowUseCardAction(miscreant);

		// This copy exerts, and not the other; one burden on the Ring-bearer, and no wound.
		assertEquals(1, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetWoundsOn(miscreant2));
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var miscreant = scn.GetShadowCard("miscreant");
		var miscreant2 = scn.GetShadowCard("miscreant2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, miscreant2);
		// Vitality 3 with 2 wounds: exhausted.  The other copy is fresh.
		scn.AddWoundsToChar(miscreant, 2);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetWoundsOn(miscreant2));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(miscreant));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(miscreant));
		assertTrue(scn.ShadowActionAvailable(miscreant2));
		assertEquals(0, scn.FreepsGetBurdens());
	}
}
