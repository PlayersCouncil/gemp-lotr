package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("servants", "32_58");
					// The Hobbit sets have no [wraith] Orc: these are Decipher cards.
					// For the hand: the Orc that pays the cost
					put("brute", "7_188");      // Morgul Brute: [wraith] Orc; its play's burden wants a Nazgul, and none is laid
					// For the discard pile.  A choice from a pile is read by blueprint, so neither shares one with the Brute.
					put("lackey", "7_193");     // Morgul Lackey: [wraith] Orc, cost 2, site 4; a skirmish ability
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, cost 1, site 4; strength +6 while the Ring-bearer is wounded
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HeIsSummoningHisServantsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: He Is Summoning His Servants
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you cannot spot 2 Wise characters, each [wraith] minion is strength +2.  <b>Shadow:</b>
		 * 	Discard an Orc from hand to play a [wraith] minion from your discard pile.  (No sheet has the Hobbit sets
		 * 	and the definition carries no game text: these are the definition's own fields.  It has no side line; the
		 * 	game takes Shadow from the culture.)
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("servants");

		assertEquals("He Is Summoning His Servants", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// From the definition, there being no text to go by: "Shadow: Discard an Orc from hand to play a [wraith] minion
	// from your discard pile."
	@Test
	public void TheMinionPlayedIsNotTheOrcDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var servants = scn.GetShadowCard("servants");
		var brute = scn.GetShadowCard("brute");
		var lackey = scn.GetShadowCard("lackey");
		var whelp = scn.GetShadowCard("whelp");

		scn.MoveCardsToSupportArea(servants);
		scn.MoveCardsToHand(brute);
		scn.MoveCardsToDiscard(lackey, whelp);

		scn.StartGame();
		assertInHand(brute);
		assertInDiscard(lackey, whelp);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(servants));
		scn.ShadowUseCardAction(servants);
		// The one Orc in hand pays the cost without a choice
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(brute);

		// The two [wraith] minions that lay in the discard pile before the cost was paid, and not the Morgul Brute that
		// lies there now because it paid for it (CR 4.2 "effect", the They Are Coming example; Q-0320; F-0545).
		assertTrue(scn.ShadowHasCardChoiceAvailable(lackey, whelp));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brute));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(whelp);

		assertInZone(Zone.SHADOW_CHARACTERS, whelp);
		assertInDiscard(lackey, brute);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
