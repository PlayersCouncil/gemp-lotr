package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_057_Tests
{

	// The Hobbit table: the Hobbit path, with Battle of the Five Armies as site 9, Bilbo, Reliable Companion 30_46
	// as the Ring-bearer and no card in the Ring slot.  A doubt is the HDG's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Minions of two cultures and two races.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability, not used
					put("troll", "32_31");       // Demolition Troll: [moria] Troll, cost 5; a Skirmish ability, not used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					// Smaug, The Golden: [smaug] Dragon minion, cost 16 (-1 for each Dwarf, Man and Elf spotted:
					// only Bilbo is in play), at home at a mountain, as site 9 is.
					put("smaug", "30_66");
					put("pinned", "1_192");      // Pinned Down: a [moria] condition
					put("scimitar", "1_180");    // Goblin Scimitar: a [moria] possession
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BattleoftheFiveArmiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Battle of the Five Armies
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Battleground</b>. <b>Shadow:</b> Remove 2 doubts to play a minion from your discard pile (except Smaug).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Battle of the Five Armies", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void RemovesTwoDoubtsToPlayAMinionOtherThanSmaugFromTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var smaug = scn.GetShadowCard("smaug");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");
		var freepsTroop = scn.GetFreepsCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		// The turns skipped have drawn cards: every pile is laid now.  A pile's choice is read by blueprint, so the
		// other player's minion in his own discard pile is of a title that lies in no discard pile of this player.
		scn.SetShadowHand(troop);
		scn.MoveCardsToDiscard(aggressor, troll, smaug, pinned, scimitar, freepsTroop);
		scn.AddBurdens(3);
		assertInZone(Zone.DISCARD, aggressor, troll, smaug, pinned, scimitar, freepsTroop);
		assertInHand(troop);
		assertEquals(3, scn.FreepsGetBurdens());
		scn.FreepsPass();

		var battle = scn.GetCurrentSite();
		assertEquals("30_57", battle.getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Enough for Smaug too, so that only the text keeps him out.
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(battle));

		scn.ShadowUseCardAction(battle);
		// Two doubts, not all of them.
		assertEquals(1, scn.FreepsGetBurdens());

		// Minions of two cultures and races; not Smaug, a Shadow condition or possession in the same pile, a minion
		// in hand, or the other player's minion in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(smaug, pinned, scimitar, troop, freepsTroop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertInZone(Zone.DISCARD, aggressor, smaug, pinned, scimitar, freepsTroop);
		assertInHand(troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneDoubtAndIsOnceThereAreTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(aggressor);
		scn.AddBurdens(1);
		assertInZone(Zone.DISCARD, aggressor);
		assertEquals(1, scn.FreepsGetBurdens());
		scn.FreepsPass();

		var battle = scn.GetCurrentSite();
		assertEquals("30_57", battle.getBlueprintId());
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(battle));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(battle));
	}

	@Test
	public void IsNotOfferedWithOnlySmaugOrNoMinionThePoolCanPayForInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var smaug = scn.GetShadowCard("smaug");
		var pinned = scn.GetShadowCard("pinned");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowHand(troop);
		scn.MoveCardsToDiscard(smaug, pinned, scimitar);
		scn.AddBurdens(3);
		assertInZone(Zone.DISCARD, smaug, pinned, scimitar);
		assertInHand(troop);
		assertEquals(3, scn.FreepsGetBurdens());
		scn.FreepsPass();

		var battle = scn.GetCurrentSite();
		assertEquals("30_57", battle.getBlueprintId());
		// Smaug, a condition and a possession in the pile, and twilight enough for Smaug.
		scn.SetTwilight(30);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(battle));

		// The Troop, cost 6, in the pile with one twilight too few.
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, troop);
		assertFalse(scn.ShadowActionAvailable(battle));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(battle));
		assertEquals(3, scn.FreepsGetBurdens());
	}
}
