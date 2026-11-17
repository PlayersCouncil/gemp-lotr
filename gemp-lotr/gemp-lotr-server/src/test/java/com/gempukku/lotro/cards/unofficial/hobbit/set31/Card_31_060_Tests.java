package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_060_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knife", "31_60");
					put("bert", "31_56");       // Bert, Troll of Ettenmoors: [troll] Troll, fierce, no damage bonus; his texts work only on his play or on discards
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll, no damage bonus; a skirmish ability not used
					put("yazneg", "30_41");     // Yazneg, Orkish Assassin: [gundabad] Orc, a minion who is no Troll; a maneuver ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TrollKnifeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Troll Knife
		 * Unique: False
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +3
		 * Game Text: Bearer must be a Troll.
		 * 	Bearer is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("knife");

		assertEquals("Troll Knife", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnATrollOfEitherCultureAndMakesThatTrollAloneDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knife = scn.GetShadowCard("knife");
		var bert = scn.GetShadowCard("bert");
		var demolition = scn.GetShadowCard("demolition");
		var yazneg = scn.GetShadowCard("yazneg");
		scn.MoveCardsToHand(knife);

		scn.StartGame();
		assertInHand(knife);
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(bert, demolition, yazneg);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetKeywordCount(bert, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(demolition, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(knife));
		scn.ShadowPlayCard(knife);
		// Trolls of two cultures; not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bert, demolition));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(yazneg));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(demolition);

		assertAttachedTo(knife, demolition);
		assertEquals(1, scn.GetKeywordCount(demolition, Keyword.DAMAGE));
		// The other Troll does not bear it.
		assertEquals(0, scn.GetKeywordCount(bert, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
