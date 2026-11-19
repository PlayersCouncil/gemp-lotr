package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "6_86");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, no archer; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Rohirrim Herdsman: a Man ally; a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Cards for the Shadow hand, of three cultures: Goblin Patrol Troop ([moria], no text), Morgul Hound
					// ([wraith] minion) and Orc Banner ([sauron] condition).
					put("troop", "1_177");
					put("troop2", "1_177");
					put("hound", "7_192");
					put("banner", "1_263");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Úlairë Lemenya, Winged Hunter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Regroup:</b> Exert Úlairë Lemenya and discard 3 cards from hand to return a companion (except the Ring-bearer) to owner's hand.
		*/

		var scn = GetScenario();

		var lemenya = scn.GetFreepsCard("lemenya");

		assertEquals("Úlairë Lemenya", lemenya.getBlueprint().getTitle());
		assertEquals("Winged Hunter", lemenya.getBlueprint().getSubtitle());
		assertTrue(lemenya.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lemenya.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, lemenya.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lemenya.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, lemenya.getBlueprint().getRace());
		assertEquals(4, lemenya.getBlueprint().getTwilightCost());
		assertEquals(9, lemenya.getBlueprint().getStrength());
		assertEquals(2, lemenya.getBlueprint().getVitality());
		assertEquals(3, lemenya.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsHerAndDiscards3CardsToReturnACompanionWhoIsNotTheRingBearerToHisHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var hound = scn.GetShadowCard("hound");
		var banner = scn.GetShadowCard("banner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(troop, troop2, hound, banner);
		assertInHand(troop, troop2, hound, banner);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowActionAvailable(lemenya));
		scn.ShadowUseCardAction(lemenya);

		// Any 3 of the 4 cards in hand, whatever their culture or type.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troop2, hound, banner));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, hound, banner);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(troop, hound, banner);
		assertInHand(troop2);

		// Companions of three cultures and races, a Ring-bound one among them; not the Ring-bearer, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, herdsman, lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertInHand(sam);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.FREE_CHARACTERS, frodo, aragorn, gimli);
		assertInZone(Zone.SUPPORT, herdsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerAsACompanionItIsPaidForAndReturnsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(troop, troop2, hound);
		assertInHand(troop, troop2, hound);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(lemenya));
		scn.ShadowUseCardAction(lemenya);

		// The cost is paid with nothing to return: three cards, the whole hand, go with nobody asked.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(troop, troop2, hound);
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertInZone(Zone.SUPPORT, herdsman);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoCardsInHandOrWithHerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(troop, troop2);
		assertInHand(troop, troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Two cards in hand: the discard of 3 cannot be paid.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(2, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(lemenya));

		scn.MoveCardsToHand(hound);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(lemenya));

		// Vitality 2 with a wound: she is exhausted and cannot exert.
		scn.AddWoundsToChar(lemenya, 1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowActionAvailable(lemenya));
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
