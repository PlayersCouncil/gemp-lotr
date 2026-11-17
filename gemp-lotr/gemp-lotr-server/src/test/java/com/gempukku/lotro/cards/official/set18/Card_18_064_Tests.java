package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "18_64");
					put("scimitar2", "18_64");

					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10; fierce only at a battleground (none on this path)
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; "When you play this, add a [men] token here"
					put("howdah", "13_94");     // Howdah: [men] support-area condition that carries [men] tokens; its archery text is not reached
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession that carries [raider] tokens; abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be a [men] minion.
		 * 	Bearer is strength +1 for each [men] token you can spot (limit +5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Corsair Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeAMenMinionAndIsStrengthPlusOneForEachMenTokenSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var stragglers = scn.GetShadowCard("stragglers");
		var howdah = scn.GetShadowCard("howdah");
		var ships = scn.GetShadowCard("ships");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(scimitar, stragglers);
		scn.MoveMinionsToTable(corps, ted, brigand);
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);
		scn.SetTwilight(10);
		assertInZone(Zone.HAND, scimitar, stragglers);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(corps));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);
		// [men] minions of two races are offered; a Man minion of another culture is not, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(corps);

		assertAttachedTo(scimitar, corps);
		// No [men] token in play: no bonus. The 2 [raider] tokens on the Ships are not counted (a count of any
		// culture's tokens would read 12).
		assertEquals(10, scn.GetStrength(corps));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Stragglers played adds a [men] token to itself.
		scn.ShadowPlayCard(stragglers);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		// 10 + 1 token
		assertEquals(11, scn.GetStrength(corps));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 2 [men] tokens on Stragglers and 1 on Howdah: 3 tokens on two cards, counted by the token.
		scn.AddTokensToCard(stragglers, 1);
		scn.MoveCardsToSupportArea(howdah);
		scn.AddTokensToCard(howdah, 1);
		assertEquals(2, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		// 10 + 3 tokens (a count of cards with tokens would read 12)
		assertEquals(13, scn.GetStrength(corps));
		// Ted Sandyman, another [men] minion, bears nothing and gains nothing.
		assertEquals(3, scn.GetStrength(ted));
	}

	@Test
	public void StrengthBonusStopsAtPlusFiveOnEachCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var stragglers = scn.GetShadowCard("stragglers");
		var howdah = scn.GetShadowCard("howdah");

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted);
		scn.AttachCardsTo(corps, scimitar);
		scn.AttachCardsTo(ted, scimitar2);
		scn.MoveCardsToSupportArea(stragglers, howdah);
		scn.AddTokensToCard(stragglers, 4);

		assertAttachedTo(scimitar, corps);
		assertAttachedTo(scimitar2, ted);
		assertEquals(4, scn.GetCultureTokensOn(stragglers));
		assertEquals(0, scn.GetCultureTokensOn(howdah));
		// 4 tokens: 10 + 4, and 3 + 4
		assertEquals(14, scn.GetStrength(corps));
		assertEquals(7, scn.GetStrength(ted));

		// 5 tokens: the limit reached
		scn.AddTokensToCard(howdah, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(15, scn.GetStrength(corps));
		assertEquals(8, scn.GetStrength(ted));

		// 6 tokens: still +5 on each bearer, each copy having a limit of its own
		scn.AddTokensToCard(howdah, 1);
		assertEquals(2, scn.GetCultureTokensOn(howdah));
		assertEquals(15, scn.GetStrength(corps));
		assertEquals(8, scn.GetStrength(ted));
	}
}
