package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_206_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bent", "1_206");
					put("bent2", "1_206");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, vitality 2; her text weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, vitality 3; his bonuses need 3 burdens
					put("nelya", "2_84");       // Úlairë Nelya: [wraith] Nazgûl, vitality 3; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2; its bonus needs 2 wounds on the Ring-bearer

					put("deck1", "1_177");      // Goblin Patrol Troop, no text: cards left in the draw deck to be drawn
					put("deck2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BentonDiscoveryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bent on Discovery
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. Plays to your support area.
		 * 	At the beginning of each of your Shadow phases, draw 1 card.
		 * 	At the end of each of your Shadow phases, exert a Nazgûl or discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bent");

		assertEquals("Bent on Discovery", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DrawsACardAsTheShadowPhaseBeginsAndAsItEndsExertsTheNazgulToStay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bent = scn.GetShadowCard("bent");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(bent);
		assertInZone(Zone.SUPPORT, bent);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPass();
		// The Shadow phase has begun: one card drawn, unasked, and nothing exerted or discarded yet.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SUPPORT, bent);

		scn.ShadowPass();
		// The Shadow phase ends: exert a Nazgûl or discard this condition.
		assertEquals(List.of("Exert a Nazgul", "Discard this condition"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert a Nazgul");

		// Lemenya is the one Nazgûl and exerts unasked; the condition stays, and no card is drawn as the phase ends
		// or as the maneuver phase begins.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SUPPORT, bent);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachCopyDrawsAndAsksForItselfAndOnlyANazgulWhoCanExertKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bent = scn.GetShadowCard("bent");
		var bent2 = scn.GetShadowCard("bent2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, nelya, hound);
		// Nelya, vitality 3 with 2 wounds, is exhausted.
		scn.AddWoundsToChar(nelya, 2);
		scn.MoveCardsToSupportArea(bent, bent2);
		assertInZone(Zone.SUPPORT, bent, bent2);
		assertEquals(2, scn.GetWoundsOn(nelya));
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsPass();
		// One card for each copy.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());

		scn.ShadowPass();
		// The first copy's question: discarding it leaves the other copy where it is.
		assertEquals(List.of("Exert a Nazgul", "Discard this condition"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Discard this condition");
		assertInDiscard(bent);
		assertInZone(Zone.SUPPORT, bent2);

		// The second copy's question: a Nazgûl of either culture may exert, not the exhausted one nor the [wraith] Orc.
		assertEquals(List.of("Exert a Nazgul", "Discard this condition"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert a Nazgul");
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertInDiscard(bent);
		assertInZone(Zone.SUPPORT, bent2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulAbleToExertItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bent = scn.GetShadowCard("bent");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		// Nelya, vitality 3 with 2 wounds, is exhausted; the [wraith] Orc beside her could exert, and is no Nazgûl.
		scn.AddWoundsToChar(nelya, 2);
		scn.MoveCardsToSupportArea(bent);
		assertInZone(Zone.SUPPORT, bent);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		assertInDiscard(bent);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
