package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_195_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("relics", "51_195");
					put("axe", "1_190");        // Moria Axe: [moria] possession, cost 1, bearer must be a [moria] Orc
					put("spear", "1_182");      // Goblin Spear: [moria] possession, cost 1, bearer must be a [moria] minion
					put("hammer", "1_166");     // Cave Troll's Hammer: [moria] possession, bearer must be Cave Troll of Moria
					put("orcscimitar", "1_269");// Orc Scimitar: [sauron] possession, cost 1, bearer must be a [sauron] Orc
					put("goblinscimitar", "1_180"); // Goblin Scimitar: [moria] possession, cost 0; may draw when played
					put("marksman", "1_176");   // Goblin Marksman: [moria] minion, archer, no other text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) only when played
					put("warden", "1_259");     // Morgul Warden: [sauron] Orc; strength +1 for each other [sauron] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelicsofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Relics of Moria
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	<b>Shadow:</b> Remove (2) to play a [moria] possession from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("relics");

		assertEquals("Relics of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemoves2ToPlayAMoriaPossessionFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relics = scn.GetShadowCard("relics");
		var axe = scn.GetShadowCard("axe");
		var spear = scn.GetShadowCard("spear");
		var hammer = scn.GetShadowCard("hammer");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		var marksman = scn.GetShadowCard("marksman");
		var runner = scn.GetShadowCard("runner");
		var warden = scn.GetShadowCard("warden");
		// A [moria] possession in the other player's discard pile.
		var freepsScimitar = scn.GetFreepsCard("goblinscimitar");
		scn.MoveCardsToHand(relics);
		scn.MoveCardsToDiscard(axe, spear, hammer, orcscimitar, marksman, freepsScimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, warden);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		scn.ShadowPlayCard(relics);
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, relics);
		assertNull(relics.getAttachedTo());

		assertTrue(scn.ShadowActionAvailable(relics));
		scn.ShadowUseCardAction(relics);

		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, spear));
		// Not [moria]; not a possession; no Cave Troll to bear it; not in your discard pile.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orcscimitar, marksman, hammer, freepsScimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(axe);

		// 2 removed + 1 to play Moria Axe; Goblin Runner is the only [moria] Orc to bear it.
		assertEquals(9, scn.GetTwilight());
		assertAttachedTo(axe, runner);
		assertInDiscard(spear, hammer, orcscimitar, marksman, freepsScimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedUntilAMoriaPossessionAndItsTwilightCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relics = scn.GetShadowCard("relics");
		var axe = scn.GetShadowCard("axe");
		var orcscimitar = scn.GetShadowCard("orcscimitar");
		var marksman = scn.GetShadowCard("marksman");
		var runner = scn.GetShadowCard("runner");
		var warden = scn.GetShadowCard("warden");
		scn.MoveCardsToSupportArea(relics);
		scn.MoveCardsToDiscard(orcscimitar, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, warden);
		scn.FreepsPass();

		// 1 companion + 2 for the site: enough to remove (2), but nothing in the discard pile can be played.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(relics));

		// Moria Axe costs 1: (2) in the pool pays the removal but not the axe.
		scn.MoveCardsToDiscard(axe);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(relics));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(relics));
		scn.ShadowUseCardAction(relics);
		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(axe, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
