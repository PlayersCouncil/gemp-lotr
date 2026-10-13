package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("web", "8_30");
					put("web2", "8_30");
					put("gollum", "7_59");      // Gollum, Vile Creature: [gollum] minion with no race, cost 2, site 3; a skirmish ability only
					put("gollum2", "5_24");     // Gollum, Nasty Treacherous Creature: [gollum] minion with no race, cost 2, site 3
					// Shelob, Eater of Light: [gollum] Spider, cost 6, site 8, fierce.
					// "When you play Shelob, you may play a [gollum] possession from your draw deck."
					put("shelob", "8_25");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4; no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1, site 4; adds (2) unasked when played
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, no Orc; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WebStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Web
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> If there are fewer than 3 cards stacked here, spot your Orc or [gollum] minion to stack that minion here.<br><b>Shadow:</b> Play a [gollum] minion stacked here as if played from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("web");

		assertEquals("Web", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityStacksYourOrcOrGollumMinionHereUntilThreeCardsAreStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var web = scn.GetShadowCard("web");
		var web2 = scn.GetShadowCard("web2");
		var gollum = scn.GetShadowCard("gollum");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(web, web2);
		scn.StackCardsOn(web, runner, runner2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, soldier, troop, savage);

		// Nobody is assigned, so every minion is still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(2, scn.GetStackedCards(web).size());
		assertTrue(scn.ShadowActionAvailable(web));
		scn.ShadowUseCardAction(web);

		assertEquals(1, scn.ShadowGetChoiceMin());
		// Gollum is a [gollum] minion with no race; the Soldier and the Troop are Orcs of two other cultures.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, soldier, troop));
		// An Uruk-hai is no Orc; the other Web is a [gollum] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, web2));
		scn.ShadowChooseCard(gollum);

		assertInZone(Zone.STACKED, gollum);
		assertTrue(scn.GetStackedCards(web).contains(gollum));
		assertEquals(3, scn.GetStackedCards(web).size());
		assertEquals(0, scn.GetStackedCards(web2).size());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier, troop, savage);

		// Three cards are stacked on this Web now; the other, with none, still offers its ability.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(web));
		assertTrue(scn.ShadowActionAvailable(web2));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoOrcOrGollumMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var web = scn.GetShadowCard("web");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(web);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetStackedCards(web).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(web));
	}

	@Test
	public void ShadowAbilityPlaysAGollumMinionStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var web = scn.GetShadowCard("web");
		var web2 = scn.GetShadowCard("web2");
		var gollum = scn.GetShadowCard("gollum");
		var gollum2 = scn.GetShadowCard("gollum2");
		var shelob = scn.GetShadowCard("shelob");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(web, web2);
		scn.StackCardsOn(web, gollum, shelob, troop);
		scn.StackCardsOn(web2, gollum2);

		scn.StartGame();
		scn.FreepsPass();
		// Enough for any of them: Shelob is 6 + 2 roaming at Ettenmoors.
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowActionAvailable(web));
		scn.ShadowUseCardAction(web);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		// An Orc stacked here is no [gollum] minion; the Gollum stacked on the other Web is not stacked here.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, gollum2));
		scn.ShadowChooseCard(shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		// Played as if from hand, so her own "when you play" trigger answers.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.STACKED, gollum, troop, gollum2);
		assertEquals(2, scn.GetStackedCards(web).size());
		assertTrue(scn.GetStackedCards(web2).contains(gollum2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolCanPayForAGollumMinionStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var web = scn.GetShadowCard("web");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(web);
		scn.StackCardsOn(web, gollum, runner);

		scn.StartGame();
		scn.FreepsPass();

		// Gollum is 2 + 2 roaming at Ettenmoors; the Runner, which is no [gollum] minion, 1 + 2.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(web));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(web));
		scn.ShadowUseCardAction(web);

		// Gollum is the only one that can be chosen, and is played.
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.STACKED, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
