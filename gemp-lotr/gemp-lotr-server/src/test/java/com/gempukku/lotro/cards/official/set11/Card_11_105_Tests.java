package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wielding", "11_105");

					// Axeman of Harad: a [men] Man minion, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, cost 1, site 2.  His maneuver
					// and regroup abilities are never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion, no text.
					put("brigand", "4_10");
					// Harmless: a [men] condition in the support area.  Its response is to a lost skirmish, never reached.
					put("harmless", "13_93");

					// Aragorn, Ranger of the North: a [gondor] Man companion.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: a Ring-bound [shire] Hobbit companion who is not the Ring-bearer.
					// A fellowship ability and a response when Frodo dies, neither reached.
					put("sam", "1_311");
					// Barliman Butterbur: a [gandalf] Man ally.  His fellowship ability is never used.
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WieldingtheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Wielding the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Resistance: -2
		 * Game Text: To play, spot a [men] minion.
		 * 		Bearer must be a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wielding");

		assertEquals("Wielding the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAMenMinionIsSpottedAndThenGoesOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);

		var wielding = scn.GetShadowCard("wielding");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var harmless = scn.GetShadowCard("harmless");

		scn.StartGame();
		scn.SetShadowHand(wielding, ted);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToSupportArea(harmless);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInZone(Zone.SUPPORT, harmless);
		assertInHand(wielding);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [dunland] Man minion and a [men] condition: no [men] minion to spot.
		assertFalse(scn.ShadowPlayAvailable(wielding));

		// Ted Sandyman, a [men] minion who is a Hobbit, played from hand at his own site 2.
		scn.ShadowPlayCard(ted);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertTrue(scn.ShadowPlayAvailable(wielding));

		scn.ShadowPlayCard(wielding);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		// Frodo is the Ring-bearer; Barliman is an ally; the minions are no companions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, barliman, ted, brigand));

		scn.ShadowChooseCard(sam);
		assertAttachedTo(wielding, sam);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsPlayedWithAMenManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var wielding = scn.GetShadowCard("wielding");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SetShadowHand(wielding);
		scn.MoveMinionsToTable(axeman);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(wielding));

		scn.ShadowPlayCard(wielding);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, axeman));

		scn.ShadowChooseCard(aragorn);
		assertAttachedTo(wielding, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
