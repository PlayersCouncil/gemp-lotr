package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "15_184");

					// Úlairë Lemenya, Lieutenant of Morgul: another Nazgûl, strength 9; her text lowers a character who
					// bears a [wraith] condition, and none does
					put("lemenya", "1_232");
					// Black Breath: [wraith] condition, cost 1, plays to the support area with nothing asked; its
					// Skirmish transfer is never used
					put("breath", "1_207");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Úlairë Attëa, Desirous of Power
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Úlairë Attëa is strength +1 for every 3 twilight tokens you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Desirous of Power", card.getBlueprint().getSubtitle());
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
	public void IsStrengthPlusOneForEveryThreeTwilightTokensAndNoOtherNazgulIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, lemenya);

		scn.SetTwilight(0);
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));

		// One token short of the first 3.
		scn.SetTwilight(2);
		assertEquals(12, scn.GetStrength(attea));

		scn.SetTwilight(3);
		assertEquals(13, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));

		// A part of 3 counts for nothing.
		scn.SetTwilight(5);
		assertEquals(13, scn.GetStrength(attea));

		scn.SetTwilight(6);
		assertEquals(14, scn.GetStrength(attea));

		scn.SetTwilight(9);
		assertEquals(15, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));
	}

	@Test
	public void TheBonusFollowsThePoolAsTheMoveAddsToItAndAPlayTakesFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToHand(breath);

		scn.StartGame();
		// Laid in the fellowship phase, where she will stand in the Shadow phase (a shortening, Q-0228).
		scn.MoveMinionsToTable(attea);
		assertEquals(0, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(attea));

		scn.FreepsPass();
		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(attea));

		scn.ShadowPlayCard(breath);
		assertInZone(Zone.SUPPORT, breath);
		// 3 - 1 for Black Breath
		assertEquals(2, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
