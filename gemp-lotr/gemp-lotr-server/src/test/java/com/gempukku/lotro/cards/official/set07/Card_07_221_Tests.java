package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_221_Tests
{

	// The Great Ring answers no wound while it is not worn, so a wound on the Ring-bearer asks nothing of it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "7_221");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; a fellowship ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2; his text is about being played

					// Úlairë Cantëa, Lieutenant of Dol Guldur: a [wraith] minion; a skirmish ability, never in play here
					put("cantea", "1_230");
					// The Nine Servants of Sauron: a [wraith] support-area condition; never in play here
					put("servants", "1_219");
					// Orc Chieftain: a [sauron] minion, the card of another culture; never in play here
					put("chieftain", "1_266");
					// Fear: a [wraith] Maneuver event; laid in the Free Peoples player's discard pile only
					put("fear", "1_212");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, cost 4; a standing text on companions
					// bearing a [wraith] condition, of whom there are none
					put("lemenya", "1_232");

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("marksman", "1_176");   // Goblin Marksman: strength 7, vitality 1; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: The Witch-king, Morgul King
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play The Witch-king, you may remove a threat to take a [wraith] card into hand from your discard pile.<br>The Ring-bearer cannot take threat wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Morgul King", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedHeMayRemoveAThreatToTakeAWraithCardFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var fear = scn.GetFreepsCard("fear");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToDiscard(fear);

		var witchking = scn.GetShadowCard("witchking");
		var cantea = scn.GetShadowCard("cantea");
		var servants = scn.GetShadowCard("servants");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(cantea, servants, chieftain);

		scn.StartGame();
		// With the Ring-bearer, two companions: room for two threats.
		scn.AddThreats(2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());
		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		// A [wraith] minion and a [wraith] condition are offered; a [sauron] card in his pile and a [wraith] card in
		// the other player's pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(cantea, servants));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cantea);

		assertInHand(cantea);
		assertInZone(Zone.DISCARD, servants, chieftain, fear);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingChangesAndAnotherNazgulPlayedIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var cantea = scn.GetShadowCard("cantea");
		var servants = scn.GetShadowCard("servants");
		scn.MoveCardsToHand(witchking, lemenya);
		scn.MoveCardsToDiscard(cantea, servants);

		scn.StartGame();
		scn.AddThreats(2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, cantea, servants);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgûl played with The Witch-king in play, a threat to remove and [wraith] cards in the pile.
		scn.ShadowPlayCard(lemenya);
		assertInPlay(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.DISCARD, cantea, servants);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatToRemoveNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var witchking = scn.GetShadowCard("witchking");
		var cantea = scn.GetShadowCard("cantea");
		var servants = scn.GetShadowCard("servants");
		scn.MoveCardsToHand(witchking);
		scn.MoveCardsToDiscard(cantea, servants);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		scn.ShadowPlayCard(witchking);
		assertInPlay(witchking);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, cantea, servants);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheRingBearerTakesNoThreatWoundsButStillTakesAnArcheryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, sam, guard);

		var witchking = scn.GetShadowCard("witchking");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, troop, marksman);
		scn.AddThreats(2);

		// A wound that is no threat wound: the minion archery total of 1 may be placed on the Ring-bearer.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, knight, sam, guard));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.ShadowDeclineAssignments();

		// The Troop, 13, overwhelms the Guard, 4: he is killed, with The Witch-king in play but not in that skirmish.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);

		// One wound asked for each of the two threats: Sam, Ring-bound but not the Ring-bearer, and the Knight;
		// never Frodo.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(knight);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		// The Witch-king is fierce: the fierce assignment follows.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithoutHimTheRingBearerTakesAThreatWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(knight, sam, guard);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddThreats(2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, knight, sam));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, knight, sam));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
