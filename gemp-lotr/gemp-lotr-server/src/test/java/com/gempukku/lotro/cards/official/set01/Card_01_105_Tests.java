package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_105_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes", "1_105");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, vitality 4; a skirmish ability only
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so the Merchant can exert; a response to a wound on him only
					put("eowyn", "4_270");      // Éowyn, Lady of Rohan: a [rohan] Man companion, vitality 3; a response to a wound on an unbound companion only

					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, vitality 2; its text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 only
					put("servant", "3_58");     // Isengard Servant: an [isengard] Orc, vitality 3; a regroup ability only
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] support-area condition, no minion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoesofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Foes of Mordor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 		<b>Maneuver:</b> Exert a [gondor] character to wound a [sauron] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foes");

		assertEquals("Foes of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorCharacterToWoundASauronMinionEachTimeItIsUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetFreepsCard("foes");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, pippin, eowyn);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(foes);

		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, guard, servant);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(foes));

		scn.FreepsUseCardAction(foes);
		// The [gondor] characters: two companions of two races, and the ally.  Not the [rohan] Man or the [shire] Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eowyn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// A [sauron] Orc and a [sauron] Uruk-hai; not an [isengard] Orc, and not the [sauron] condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(servant, fear, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// No limit: used again, and this time the ally pays (vitality 1 + 1 with Narsil).
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(foes));
		scn.FreepsUseCardAction(foes);
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyGondorCharacterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetFreepsCard("foes");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(aragorn, eowyn);
		scn.MoveCardsToSupportArea(foes);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  The [rohan] Man beside him could.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(foes));
	}

	@Test
	public void WithNoSauronMinionTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetFreepsCard("foes");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);

		var servant = scn.GetShadowCard("servant");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(servant);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(servant));
		// An ability with a cost is offered whatever its effect can reach.
		assertTrue(scn.FreepsActionAvailable(foes));

		scn.FreepsUseCardAction(foes);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
