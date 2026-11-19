package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "19_35");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, strength 9; her text reaches only a
					// companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Nazgul Scimitar: a [wraith] possession on a [wraith] minion; damage +1 only at 5 threats
					put("scimitar", "7_203");
					put("scimitar2", "7_203");
					// Black Breath: a [wraith] condition that goes on a character skirmishing a Nazgul; burdens and
					// wounds may not be removed from its bearer
					put("breath", "1_207");
					put("breath2", "1_207");
					put("runner", "1_178");     // Goblin Runner: a [moria] minion; its text answers its own play
					// Merry, From O'er the Brandywine: a companion of cost 1; his text needs a weapon
					put("merry", "1_303");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Úlairë Attëa, Dark Predator
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.
		 * 	While you can spot 5 twilight tokens in the twilight pool and another [wraith] card, Úlairë Attëa is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Dark Predator", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus3FromTheFifthTwilightTokenWithAnotherWraithCardInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var lemenya = scn.GetShadowCard("lemenya");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(merry);
		assertInZone(Zone.HAND, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, lemenya);
		// Tokens that are not twilight tokens: a burden and a wound.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(frodo, 1);
		scn.SetTwilight(4);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(4, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));

		// Merry's play adds the fifth twilight token.
		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(5, scn.GetTwilight());
		// 12 + 3; the bonus is hers alone.
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));

		scn.SetTwilight(6);
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherWraithCardInPlayOfAnyTypeIsSpottedAndSheAndCardsOutOfPlayAreNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var lemenya = scn.GetShadowCard("lemenya");
		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var breath = scn.GetShadowCard("breath");
		var breath2 = scn.GetShadowCard("breath2");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		// [wraith] cards that are not in play: one in the discard pile, one in hand.
		scn.MoveCardsToDiscard(scimitar2);
		scn.MoveCardsToHand(breath2);
		assertInZone(Zone.DISCARD, scimitar2);
		assertInZone(Zone.HAND, breath2);

		scn.StartGame();
		// A minion that is not [wraith].
		scn.MoveMinionsToTable(attea, runner);
		scn.SetTwilight(5);

		assertEquals(5, scn.GetTwilight());
		// No other [wraith] card in play: she does not spot herself.
		assertEquals(12, scn.GetStrength(attea));

		// A [wraith] minion.
		scn.MoveMinionsToTable(lemenya);
		assertEquals(15, scn.GetStrength(attea));
		scn.MoveCardsToDiscard(lemenya);
		assertInZone(Zone.DISCARD, lemenya);
		assertEquals(12, scn.GetStrength(attea));

		// A [wraith] possession, borne by Attea herself.
		scn.AttachCardsTo(attea, scimitar);
		assertAttachedTo(scimitar, attea);
		// 12 + 1 for the Scimitar + 3
		assertEquals(16, scn.GetStrength(attea));
		scn.MoveCardsToDiscard(scimitar);
		assertInZone(Zone.DISCARD, scimitar);
		assertEquals(12, scn.GetStrength(attea));

		// A [wraith] condition, borne by a companion.
		scn.AttachCardsTo(frodo, breath);
		assertAttachedTo(breath, frodo);
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
