package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("draws", "3_83");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, vitality 2; weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, vitality 3; his bonuses need 3 burdens
					put("nelya", "2_84");       // Úlairë Nelya: [wraith] Nazgûl, vitality 3; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2; its bonus needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheRingDrawsThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Ring Draws Them
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * 	Each time the fellowship moves, add (1) for each burden you can spot.
		 * 	At the end of each of your Shadow phases, exert a Nazgûl or discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("draws");

		assertEquals("The Ring Draws Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheMoveAddsOneForTheBurdenAndAsTheShadowPhaseEndsTheNazgulExertsToKeepIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(draws);
		assertInZone(Zone.SUPPORT, draws);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPass();
		// To Ettenmoors: 2 for its Shadow number + 1 for Frodo + 1 for the burden.
		assertEquals(4, scn.GetTwilight());
		// The Shadow phase has begun, and nothing is exerted or discarded yet.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SUPPORT, draws);

		scn.ShadowPass();
		assertEquals(List.of("Exert a Nazgul", "Discard The Ring Draws Them"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Exert a Nazgul");

		// Lemenya is the one Nazgûl and exerts unasked; the condition stays.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SUPPORT, draws);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ANazgulOfEitherCultureWhoCanExertIsOfferedAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(draws);
		assertInZone(Zone.SUPPORT, draws);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, nelya, hound);
		// Nelya, vitality 3 with 2 wounds, is exhausted.
		scn.AddWoundsToChar(nelya, 2);
		assertEquals(2, scn.GetWoundsOn(nelya));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowChoose("Exert a Nazgul");
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(nelya, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertInZone(Zone.SUPPORT, draws);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DiscardingItIsTheOtherChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(draws);
		assertInZone(Zone.SUPPORT, draws);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowChoose("Discard The Ring Draws Them");

		assertInDiscard(draws);
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulAbleToExertItIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(draws);
		assertInZone(Zone.SUPPORT, draws);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		// Nelya is exhausted; the [wraith] Orc beside her could exert, and is no Nazgûl.
		scn.AddWoundsToChar(nelya, 2);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		assertInDiscard(draws);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASecondMoveInTheTurnAddsAgainForTheBurdensThenAndTheSecondShadowPhaseAsksAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var khamul = scn.GetShadowCard("khamul");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(draws);
		assertInZone(Zone.SUPPORT, draws);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(khamul);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// To Ettenmoors: 2 + 1 for Frodo + 1 for the burden.
		assertEquals(4, scn.GetTwilight());
		scn.ShadowPass();
		scn.ShadowChoose("Exert a Nazgul");
		assertEquals(1, scn.GetWoundsOn(khamul));

		// Khamûl is put out of the way, so that no skirmish is fought (his strength would overwhelm Frodo).
		scn.MoveCardsToDiscard(khamul);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(lemenya);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		int pool = scn.GetTwilight();

		scn.FreepsChooseToMove();
		// To Rivendell Valley: 0 for its Shadow number + 1 for Frodo + 3 for the burdens.
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		scn.ShadowChoose("Exert a Nazgul");
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SUPPORT, draws);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void BurdensOnTheShadowPlayersOwnRingBearerAreNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var draws = scn.GetShadowCard("draws");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		// On his own turn the Shadow player's Ring-bearer takes 3 burdens.
		scn.AddBurdens(3);
		scn.SkipToOtherPlayersTurn();

		scn.MoveCardsToSupportArea(draws);
		scn.MoveMinionsToTable(lemenya);
		scn.AddBurdens(1);
		assertInZone(Zone.SUPPORT, draws);
		assertEquals(3, scn.ShadowGetBurdens());
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		int pool = scn.GetTwilight();

		scn.FreepsPass();
		// From Ettenmoors to Rivendell Valley: 0 + 1 for Frodo + 1 for the Free Peoples player's one burden.
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
