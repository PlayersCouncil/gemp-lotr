package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beast", "6_83");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, not fierce; her text reaches only
					                            // a character bearing a [wraith] condition
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl (printed fierce; his texts want 3 and 5 burdens)
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, not fierce (a play trigger, never set off: cheated in)
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl (fierce only at 2 wounds on the Ring-bearer)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; in the Shadow hand, a Free Peoples card to discard
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai minion whose whole text is Damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FellBeastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Fell Beast
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Game Text: To play, discard 2 cards from hand. Bearer must be a Nazgûl.
		 * Bearer is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beast");

		assertEquals("Fell Beast", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnANazgulOfAnyCultureByDiscardingTwoCardsFromHandAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var beast = scn.GetShadowCard("beast");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		// Three cards beside the beast, of three cultures and both sides: two are to be chosen.
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(beast, guard, troop, shadowAragorn);
		assertInHand(beast, guard, troop, shadowAragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, nertea, hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(beast));

		scn.ShadowPlayCard(beast);
		// A [wraith] Nazgûl, a second one, and a [raider] one; not the [wraith] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, nertea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, frodo, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		// Every other card in hand may pay; the beast itself is not in hand to be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, troop, shadowAragorn));
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(guard, shadowAragorn);

		assertInDiscard(guard, shadowAragorn);
		assertInHand(troop);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertAttachedTo(beast, lemenya);
		assertTrue(scn.HasKeyword(lemenya, Keyword.FIERCE));
		// Another Nazgûl, offered as a bearer and not chosen.
		assertFalse(scn.HasKeyword(nertea, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneOtherCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beast = scn.GetShadowCard("beast");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(beast, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.FreepsPass();

		// The beast and one card: the beast cannot pay for itself, so one card short.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowPlayAvailable(beast));

		// A second card beside the first, and it can be played.
		scn.MoveCardsToHand(guard);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(beast));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
