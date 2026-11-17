package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_055_Tests
{

	// The Hobbit table: the Hobbit path, with Erebor as site 7, Bilbo, Reliable Companion 30_46 as the
	// Ring-bearer and no card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Five [dwarven] Dwarf companions, with Bilbo six.  Balin and Dwalin have abilities of the
					// fellowship and maneuver phases, Fili a response to a wound, Glóin a skirmish ability and Kili
					// an archery one: none of them is used.
					put("balin", "30_2");
					put("dwalin", "30_9");
					put("fili", "30_10");
					put("gloin", "30_11");
					put("kili", "30_12");
					put("bard", "30_22");        // Bard, The Bowman: [esgaroth] ally, a character who is no companion
					// Three minions of two cultures and two races.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability, not used
					put("troll", "32_31");       // Demolition Troll: [moria] Troll, vitality 3; a Skirmish ability, not used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void EreborStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Erebor
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>River</b>. <b>Shadow:</b> Exert 2 minions and spot 6 companions to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Erebor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void ShadowActionExertsTwoMinionsWithSixCompanionsToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		scn.MoveMinionsToTable(aggressor, troll, troop);
		// The turns on the way drew the Shadow player's small deck into his hand: it goes back on the deck.
		scn.SetShadowHand();
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troll, troop);

		scn.FreepsPass();

		assertEquals("30_55", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		int deck = scn.GetShadowDeckCount();
		assertTrue(deck >= 3);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.ShadowUseCardAction(scn.GetCurrentSite());

		// Any two of the three minions; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, troll, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, scn.GetRingBearer()));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(aggressor, troll);

		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FiveCompanionsAndAnAllyAreTooFewForTheAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var aggressor = scn.GetShadowCard("aggressor");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin");
		scn.MoveAlliesToTable(bard);
		scn.MoveMinionsToTable(aggressor, troll);
		assertInZone(Zone.SUPPORT, bard);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troll);

		scn.FreepsPass();

		assertEquals("30_55", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void ItIsNotOfferedWithOnlyOneMinionAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggressor = scn.GetShadowCard("aggressor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		scn.MoveMinionsToTable(aggressor, troop);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPass();

		assertEquals("30_55", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}
}
