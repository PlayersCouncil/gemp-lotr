package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_203_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "7_203");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, no damage bonus; her text is about
					// companions and allies bearing [wraith] conditions
					put("lemenya", "1_232");
					// Morgul Hound: a [wraith] Orc; strength +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// Orc Chieftain: a [sauron] Orc; his text answers only a weapon played on him
					put("chieftain", "1_266");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], not [wraith]; fierce
					put("khamul", "103_61");

					// Four companions beside Frodo, so that five threats can lie on the dead pile
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("guard1", "5_83");      // Household Guard: Valiant and nothing else
					put("guard2", "5_83");
					// Oathbreaker: a [gondor] Wraith companion; "To play, add a threat"
					put("oathbreaker", "8_41");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NazgulScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Nazgûl Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [wraith] minion.<br>While you can spot 5 threats, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Nazgûl Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void CanBeBorneByAWraithMinionOfEachRaceAndNotByAMinionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var chieftain = scn.GetShadowCard("chieftain");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(scimitar);
		assertInHand(scimitar);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound, chieftain, khamul);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertInHand(scimitar);
		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);

		// A Nazgûl and an Orc of [wraith]; a [sauron] Orc and a [raider] Nazgûl are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, khamul));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertAttachedTo(scimitar, hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnceFiveThreatsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("sam"), scn.GetFreepsCard("aragorn"),
				scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToHand(oathbreaker);

		scn.StartGame();
		scn.AddThreats(4);
		scn.MoveMinionsToTable(lemenya, hound);
		scn.AttachCardsTo(lemenya, scimitar);
		scn.FreepsRefreshDecision();

		// Five companions and four threats: one short.
		assertAttachedTo(scimitar, lemenya);
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));

		// Oathbreaker's own cost adds the fifth threat.
		scn.FreepsPlayCard(oathbreaker);
		assertEquals(5, scn.GetThreats());
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		// The [wraith] minion who does not bear it gains nothing.
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
