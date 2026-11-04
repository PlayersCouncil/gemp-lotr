package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_194_Tests
{

	// A Shadows-block path with Redhorn Pass in it.  A test starts at Redhorn Pass, or at Fangorn Glade 11_239
	// ("site1"), a forest whose only text answers the fellowship moving to it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; his skirmish ability is never used
					// Suspended Palaces: [dwarven] support-area condition.  "When you play this condition, you may spot a
					// Dwarf to place 2 [dwarven] tokens here."  Its fellowship ability is never used here.
					put("palaces", "6_10");
					// Arod, Rohirrim Steed: [dwarven] mount on a Dwarf.  "When you play Arod, you may reinforce a
					// [dwarven] token."  Its Response needs a skirmish.
					put("arod", "13_1");
					// Golden Perch Ale: [shire] support-area possession.  "Fellowship: Remove a [shire] token here to
					// remove a burden."  It gets its tokens when a [shire] companion wins a skirmish.
					put("ale", "18_108");
					// Ships of Great Draught: [raider] support-area possession.  "Shadow: Remove a threat or discard a
					// [raider] card from hand to add a [raider] token here."  Its regroup ability is never used.
					put("ships", "8_65");
					put("ships2", "8_65");      // a [raider] card for the Shadow player's hand
					put("ships3", "8_65");      // and another
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void RedhornPassStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Redhorn Pass
		 * Unique: False
		 * Side:
		 * Culture:
		 * Shadow Number: 2
		 * Type: Site
		 * Subtype:
		 * Site Number: *
		 * Block: Shadows
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. Culture tokens cannot be added, removed, or reinforced.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("redhorn");

		assertEquals("Redhorn Pass", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtRedhornPassNoTokenIsPlacedOnACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var redhorn = scn.GetFreepsSite("redhorn");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable("gimli");
		scn.MoveCardsToHand(palaces);

		scn.StartGame(redhorn);

		assertSame(redhorn, scn.GetCurrentSite());

		scn.FreepsPlayCard(palaces);

		// A Dwarf is there to spot, but no token can be placed: the offer would do nothing, and is not made.
		assertInZone(Zone.SUPPORT, palaces);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(palaces));
		assertEquals(0, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtRedhornPassNoTokenCanBeRemovedToPayACost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var redhorn = scn.GetFreepsSite("redhorn");
		var ale = scn.GetFreepsCard("ale");
		scn.MoveCardsToSupportArea(ale);

		scn.StartGame(redhorn);
		scn.AddTokensToCard(ale, 1);
		scn.AddBurdens(1);
		// The token and the burden were laid under a decision already sent: ask it again.
		scn.FreepsRefreshDecision();

		assertSame(redhorn, scn.GetCurrentSite());
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ale));
	}

	@Test
	public void AtRedhornPassNoTokenIsReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var redhorn = scn.GetFreepsSite("redhorn");
		var gimli = scn.GetFreepsCard("gimli");
		var palaces = scn.GetFreepsCard("palaces");
		var arod = scn.GetFreepsCard("arod");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces);
		scn.MoveCardsToHand(arod);

		scn.StartGame(redhorn);
		scn.AddTokensToCard(palaces, 2);

		assertSame(redhorn, scn.GetCurrentSite());
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		scn.FreepsPlayCard(arod);

		assertInZone(Zone.ATTACHED, arod);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(arod));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AwayFromRedhornPassTokensArePlacedReinforcedAndRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glade = scn.GetFreepsSite("site1");
		var gimli = scn.GetFreepsCard("gimli");
		var palaces = scn.GetFreepsCard("palaces");
		var arod = scn.GetFreepsCard("arod");
		var ale = scn.GetFreepsCard("ale");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(ale);
		scn.MoveCardsToHand(palaces, arod);

		scn.StartGame(glade);
		scn.AddTokensToCard(ale, 1);
		scn.AddBurdens(1);

		assertSame(glade, scn.GetCurrentSite());
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetBurdens());

		// Placed.
		scn.FreepsPlayCard(palaces);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(palaces));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		// Reinforced: the Palaces' token is the only [dwarven] one.
		scn.FreepsPlayCard(arod);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(arod));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetCultureTokensOn(palaces));

		// Removed, to pay a cost.
		assertTrue(scn.FreepsActionAvailable(ale));
		scn.FreepsUseCardAction(ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowTokenIsNotAddedAtRedhornPassAndIsOnceTheFellowshipHasLeftIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glade = scn.GetFreepsSite("site1");
		var redhorn = scn.GetShadowSite("redhorn");
		var ships = scn.GetShadowCard("ships");
		var ships2 = scn.GetShadowCard("ships2");
		var ships3 = scn.GetShadowCard("ships3");
		scn.MoveCardsToSupportArea(ships);
		scn.MoveCardsToHand(ships2);

		scn.StartGame(glade);
		// Redhorn Pass lies next, as where the Shadow player has laid it.
		scn.MoveCardToAdventurePath(redhorn);
		scn.FreepsPass();

		assertSame(redhorn, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.HAND, ships2);

		// The ability has a cost, so it is offered; the cost is paid and no token is added.
		assertTrue(scn.ShadowActionAvailable(ships));
		scn.ShadowUseCardAction(ships);
		assertInZone(Zone.DISCARD, ships2);
		assertEquals(0, scn.GetCultureTokensOn(ships));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// The Shadow player lays his Abandoned Mine Shaft as the next site; Redhorn Pass is left behind.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.MoveCardsToHand(ships3);
		scn.ShadowRefreshDecision();

		assertSame(scn.GetShadowSite("site2"), scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, redhorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, ships3);

		scn.ShadowUseCardAction(ships);
		assertInZone(Zone.DISCARD, ships3);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
