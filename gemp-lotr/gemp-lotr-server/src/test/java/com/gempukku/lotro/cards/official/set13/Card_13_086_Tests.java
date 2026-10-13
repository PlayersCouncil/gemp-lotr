package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_86");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTokensScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "13_86");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion to take an archery wound
					// Garrison of Osgiliath: a [gondor] condition of the support area, played with 2 [gondor] tokens;
					// its ability is a skirmish one
					put("garrison", "6_52");
					// Heirs of Gondor: a [gondor] condition of the support area, played with 1 [gondor] token; its
					// ability is a skirmish one
					put("heirs", "13_69");
					// Ships of Great Draught: a [raider] possession of the Shadow support area.  "Shadow: Remove a
					// threat or discard a [raider] card from hand to add a [raider] token here."  Two more copies
					// are the [raider] cards discarded from hand.
					put("ships1", "8_65");
					put("ships2", "8_65");
					put("ships3", "8_65");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Desert Wind
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot a [men] minion.<br>While you can spot 5 or more culture tokens, the minion archery total is +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Desert Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheMinionArcheryTotalIsPlus2OnceFiveCultureTokensOfAnyCultureCanBeSpottedAndNotAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokensScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(garrison);

		var wind = scn.GetShadowCard("wind");
		var ships = scn.GetShadowCard("ships1");
		var spare1 = scn.GetShadowCard("ships2");
		var spare2 = scn.GetShadowCard("ships3");
		// The Shadow player's own Free Peoples condition: in play, and not active on this turn.
		var theirHeirs = scn.GetShadowCard("heirs");
		scn.MoveCardsToSupportArea(ships, theirHeirs);
		scn.MoveCardsToHand(spare1, spare2);

		scn.StartGame();
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(ships, 2);
		scn.AddTokensToCard(theirHeirs, 1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(wind);

		// 2 [gondor] tokens on a Free Peoples card and 2 [raider] tokens on a Shadow card: four.  The token on
		// the card that is not active would make five.  No minion is an archer.
		assertEquals(0, scn.GetShadowArcheryTotal());

		// A fifth token, of either culture.
		scn.ShadowUseCardAction(ships);
		scn.ShadowChooseCard(spare1);
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertEquals(2, scn.GetShadowArcheryTotal());

		// A sixth adds nothing more: "5 or more".
		scn.ShadowUseCardAction(ships);
		assertEquals(4, scn.GetCultureTokensOn(ships));
		assertInDiscard(spare1, spare2);
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The two wounds are the Free Peoples player's to place.
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(wind));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
