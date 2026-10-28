package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keyward", "12_105");
					// Denizen of Khazad-dum: an [orc] Orc minion; strength +2 only at an underground site.  On the
					// table, a second minion able to exert; in hand, an [orc] card that is no Troll.
					put("denizen", "11_115");
					put("denizen2", "11_115");

					// In hand only, where their texts do nothing:
					// Porter Troll and Beastly Olog-hai: [orc] Trolls of two titles.
					put("porter", "11_135");
					put("olog", "11_108");
					// A second Porter Troll, for the other player's hand.
					put("porter2", "11_135");
					// Troll of Udun: a [sauron] Troll.
					put("udun", "6_106");
					// Half-troll of Far Harad: an [orc] Half-troll, no Troll.
					put("halftroll", "14_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollsKeywardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Troll's Keyward, Keeper of the Beast
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Shadow:</b> Exert Troll's Keyward and reveal your hand to add (1) for each [orc] Troll revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("keyward");

		assertEquals("Troll's Keyward", card.getBlueprint().getTitle());
		assertEquals("Keeper of the Beast", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHimRevealsTheHandAndAdds1ForEachOfTheTwoOrcTrollsInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");
		var denizen = scn.GetShadowCard("denizen");
		var porter = scn.GetShadowCard("porter");
		var olog = scn.GetShadowCard("olog");
		var udun = scn.GetShadowCard("udun");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen2 = scn.GetShadowCard("denizen2");
		// [orc] Trolls in the other player's hand are not "your hand": three of them, so that a count of his hand
		// would read +3 and of both hands +5.
		scn.MoveCardsToHand(scn.GetFreepsCard("porter"), scn.GetFreepsCard("olog"), scn.GetFreepsCard("porter2"));

		scn.StartGame();
		scn.SetShadowHand(porter, olog, udun, halftroll, denizen2);
		scn.MoveMinionsToTable(keyward, denizen);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(keyward));
		assertEquals(0, scn.GetWoundsOn(denizen));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(keyward));
		scn.ShadowUseCardAction(keyward);

		// The hand is shown to the other player, each card and not to be chosen; its owner is shown nothing.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(porter, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(olog, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(udun, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(halftroll, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(denizen2, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(keyward));
		assertEquals(0, scn.GetWoundsOn(denizen));
		// Porter Troll and Beastly Olog-hai: the [sauron] Troll, the [orc] Half-troll and the [orc] Orc add nothing.
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneOrcTrollInHandItAdds1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");
		var porter = scn.GetShadowCard("porter");
		var udun = scn.GetShadowCard("udun");

		scn.StartGame();
		scn.SetShadowHand(porter, udun);
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(keyward));
		int pool = scn.GetTwilight();
		scn.ShadowUseCardAction(keyward);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetWoundsOn(keyward));
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyHandItIsStillOfferedAndHeExertsToAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");

		scn.StartGame();
		scn.SetShadowHand();
		scn.MoveMinionsToTable(keyward);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(keyward));
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(keyward));
		scn.ShadowUseCardAction(keyward);

		// Nothing to show (Q-0144): nobody is shown anything.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.GetWoundsOn(keyward));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keyward = scn.GetShadowCard("keyward");
		var denizen = scn.GetShadowCard("denizen");
		var porter = scn.GetShadowCard("porter");

		scn.StartGame();
		scn.SetShadowHand(porter);
		scn.MoveMinionsToTable(keyward, denizen);
		// Vitality 3 with two wounds: exhausted.  The other minion could exert, and does not count.
		scn.AddWoundsToChar(keyward, 2);
		assertEquals(2, scn.GetWoundsOn(keyward));
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(keyward));

		// One wound off: he can exert, and it is offered.
		scn.RemoveWoundsFromChar(keyward, 1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(keyward));
	}
}
