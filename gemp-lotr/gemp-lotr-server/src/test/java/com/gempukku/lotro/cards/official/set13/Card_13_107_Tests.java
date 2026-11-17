package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "13_107");
					// Always Threatening: [orc] support-area condition; its play trigger and skirmish ability reach no test.
					put("threatening", "13_103");
					// Great Cost: [orc] support-area condition; a start-of-skirmish offer and a Shadow ability only.
					put("greatcost", "12_88");
					// Spurred to Battle: [orc] support-area condition; adds (1) at the start of the Shadow phase.
					put("spurred", "11_139");
					put("relics", "1_195");      // Relics of Moria: [moria] support-area condition; a Shadow ability only
					put("prowling", "11_136");   // Prowling Orc: [orc] Orc, a skirmish ability only
					put("spear", "12_96");       // Orc Spear: [orc] possession, borne by an [orc] minion; text on its own play only
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1, site 4; text on its own play only
					// Half-troll of Far Harad: [orc] Half-troll, cost 3, site 4: an [orc] minion who is no Orc.
					put("halftroll", "14_12");
					put("runner", "1_178");      // Goblin Runner: [moria] Orc, cost 1, site 4; adds (2) as it is played
					put("skulker", "12_95");     // Orc Skulker: [orc] Orc, cost 2; text reaches no test
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, site 4; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExpendableServantsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Expendable Servants
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: If you can spot more [orc] conditions than [orc] minions, you may discard your [orc] condition from play to play an [orc] minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("Expendable Servants", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithMoreOrcConditionsThanOrcMinionsDiscardsYourOrcConditionToPlayAnOrcMinionFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var threatening = scn.GetShadowCard("threatening");
		var greatcost = scn.GetShadowCard("greatcost");
		var spurred = scn.GetShadowCard("spurred");
		var relics = scn.GetShadowCard("relics");
		var prowling = scn.GetShadowCard("prowling");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var skulker = scn.GetFreepsCard("skulker");
		scn.MoveCardsToSupportArea(threatening, greatcost, relics);
		scn.MoveCardsToHand(servants);
		scn.MoveCardsToDiscard(spurred, goblin, halftroll, troop);
		// The Free Peoples player's own [orc] minion in his own discard pile.
		scn.MoveCardsToDiscard(skulker);

		scn.StartGame();
		// Two [orc] conditions against one [orc] minion; the [moria] Runner beside it is no [orc] minion.
		scn.MoveMinionsToTable(prowling, runner);
		scn.AttachCardsTo(prowling, spear);
		assertInHand(servants);
		assertInZone(Zone.SUPPORT, threatening, greatcost, relics);
		assertAttachedTo(spear, prowling);
		assertInDiscard(spurred, goblin, halftroll, troop, skulker);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(servants));
		scn.ShadowPlayCard(servants);

		// The cost first: one of the player's own [orc] conditions.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(threatening, greatcost));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(relics, spear));
		scn.ShadowChooseCard(greatcost);
		assertInDiscard(greatcost);
		assertInZone(Zone.SUPPORT, threatening);

		// Then the play: an [orc] minion of either race from his own discard pile. The pool pays for any of them:
		// the Troop, a [moria] Orc, is 6 + 2 roaming.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(spurred, troop, skulker));
		scn.ShadowChooseCard(halftroll);

		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertInDiscard(servants, goblin, spurred, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWithAsManyOrcMinionsAsOrcConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var threatening = scn.GetShadowCard("threatening");
		var relics = scn.GetShadowCard("relics");
		var prowling = scn.GetShadowCard("prowling");
		var spear = scn.GetShadowCard("spear");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(threatening, relics);
		scn.MoveCardsToHand(servants);
		scn.MoveCardsToDiscard(goblin);

		scn.StartGame();
		// One [orc] condition and one [orc] minion; beside them a [moria] condition and an [orc] possession, which
		// are no [orc] conditions.
		scn.MoveMinionsToTable(prowling);
		scn.AttachCardsTo(prowling, spear);
		assertInHand(servants);
		assertInZone(Zone.SUPPORT, threatening, relics);
		assertAttachedTo(spear, prowling);
		assertInDiscard(goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(servants));
	}

	@Test
	public void IsNotPlayableUntilTheDiscardPileHoldsAnOrcMinionThePoolCanPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var threatening = scn.GetShadowCard("threatening");
		var greatcost = scn.GetShadowCard("greatcost");
		var prowling = scn.GetShadowCard("prowling");
		var runner = scn.GetShadowCard("runner");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToSupportArea(threatening, greatcost);
		scn.MoveCardsToHand(servants);
		scn.MoveCardsToDiscard(runner, halftroll);

		scn.StartGame();
		scn.MoveMinionsToTable(prowling);
		assertInHand(servants);
		assertInZone(Zone.SUPPORT, threatening, greatcost);
		assertInDiscard(runner, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// At Ettenmoors, site 2: the Half-troll is 3 + 2 roaming; the Runner, which is no [orc] minion, 1 + 2.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(servants));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(servants));
	}
}
