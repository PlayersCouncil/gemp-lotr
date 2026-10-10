package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_63");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCompanionsInTheDeadPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halberd", "18_63");
					// Ruffian: a [men] minion to bear it; his text is about his own strength
					put("ruffian", "18_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion for the dead pile
					put("gimli", "1_13");       // Gimli, Son of Gloin: another
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairHalberdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Halberd
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [men] minion.<br>Bearer is <b>damage +1</b>.<br><b>Shadow:</b> Remove a companion in the dead pile from the game to control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Corsair Halberd", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void RemovesACompanionInTheDeadPileFromTheGameToControlASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCompanionsInTheDeadPileScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToDeadPile(aragorn, gimli);

		var halberd = scn.GetShadowCard("halberd");
		var ruffian = scn.GetShadowCard("ruffian");

		// Both markers are at site 2 when the fellowship moves on to site 3: site 1 can be controlled.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(ruffian);
		scn.AttachCardsTo(ruffian, halberd);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, aragorn, gimli);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.ShadowActionAvailable(halberd));

		// One of the two dead companions is the price.
		scn.ShadowUseCardAction(halberd);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(gimli);
		assertInZone(Zone.REMOVED, gimli);
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.IsSiteControlled(site1));
		assertAttachedTo(halberd, ruffian);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
