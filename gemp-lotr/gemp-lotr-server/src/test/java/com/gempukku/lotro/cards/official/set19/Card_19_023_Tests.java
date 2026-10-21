package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "19_23");

					// Goblin Aggressor: an [orc] Orc; its bonus needs a possession on it.
					put("aggressor", "12_87");
					// Scurrying Goblin: an [orc] Orc of another title, strength 5; its play trigger needs an
					// underground site.
					put("scurrying", "11_137");
					// Half-troll of Far Harad: an [orc] minion who is no Orc (a Half-troll), strength 8; its only
					// text is a start-of-assignment trigger that needs more companions than minions.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: an Orc who is not [orc] ([moria]), with no text.
					put("troop", "1_177");
					// Goblin Runner: a minion who is not [orc] ([moria] Orc), strength 5.  "When you play this minion,
					// you may add (2)": it is never played.
					put("runner", "1_178");
					// The Beckoning Shadow: an [orc] card that is no minion (a support-area condition); its text
					// needs 5 burdens.
					put("beckoning", "12_83");

					// Aragorn, Ranger of the North: a companion of strength 8; his ability is used only on purpose.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrolloftheDeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Troll of the Deep, Cave Troll
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] Orc.<br><b>Skirmish:</b> Remove a threat to make an [orc] minion strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Troll of the Deep", card.getBlueprint().getTitle());
		assertEquals("Cave Troll", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var aggressor = scn.GetShadowCard("aggressor");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// An [orc] minion who is no Orc, and an Orc who is not [orc].
		scn.MoveMinionsToTable(halftroll, troop);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));

		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));
		scn.ShadowPlayCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesAThreatToMakeAnOrcMinionStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var troll = scn.GetShadowCard("troll");
		var scurrying = scn.GetShadowCard("scurrying");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(troll);
		assertInZone(Zone.HAND, troll);

		scn.StartGame();
		// Two companions: room for two threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Another [orc] Orc, alone, is enough to play it.
		scn.MoveMinionsToTable(scurrying);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));
		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		scn.MoveMinionsToTable(halftroll, runner);
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll, runner);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troll}, new PhysicalCardImpl[]{frodo, scurrying});
		// A minion who is not [orc] in the troll's own skirmish; the Half-troll is left unassigned.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(troll));
		assertEquals(5, scn.GetStrength(scurrying));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);

		// The troll himself, an [orc] Orc in another skirmish and an unassigned [orc] Half-troll are offered; the
		// [moria] Orc in this skirmish and the [orc] condition are not.  The threat is removed as the cost.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll, scurrying, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(scurrying);

		assertEquals(7, scn.GetStrength(scurrying));
		assertEquals(13, scn.GetStrength(troll));
		assertEquals(8, scn.GetStrength(halftroll));

		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(scurrying));
		scn.ShadowUseCardAction(troll);
		scn.ShadowChooseCard(troll);
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(0, scn.GetThreats());

		// No threat left to remove.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(scurrying));
		scn.ShadowPass();

		// 15 and 5 against Aragorn's 8: more than double his strength, so he is killed.
		assertInZone(Zone.DEAD, aragorn);

		// The next skirmish is another skirmish phase: both bonuses have gone.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(scurrying));
		assertEquals(13, scn.GetStrength(troll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
