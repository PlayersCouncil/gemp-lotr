package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_084_Tests
{

	// Frodo, Son of Drogo 1_290 is the Ring-bearer: vitality 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "13_84");
					put("champion2", "13_84");
					put("runner", "13_90");     // Easterling Runner: [men] Man minion; a Shadow ability used only on purpose
					put("stragglers", "13_99"); // Stragglers: [men] support-area condition; its skirmish ability is used only on purpose
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support-area possession; its own play trigger is not set off when cheated in
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition, a skirmish ability only
					put("poleaxe", "11_95");    // Poleaxe: [men] possession on a [men] minion; its skirmish ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; "Maneuver: Exert Aragorn to make him defender +1"
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, vitality 4; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability only
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, borne by a Dwarf
					put("cart", "1_73");        // Gandalf's Cart: [gandalf] possession, plays to the support area; no play trigger
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition, cost 0
					put("phial", "103_1");      // Phial of Galadriel, Bane of Darkness: [elven] artifact on a Ring-bound Hobbit
					put("eowyn", "5_122");      // Eowyn, Daughter of Eomund: "Maneuver: Play a [rohan] possession on Eowyn to heal her"
					put("spear", "4_288");      // Rider's Spear: [rohan] possession on a [rohan] Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairChampionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Corsair Champion
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each time a Free Peoples possession is played, you may spot a [men] card and reveal this minion from hand to make the Free Peoples player exert a companion.<br>While each companion is wounded, this minion is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("champion");

		assertEquals("Corsair Champion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeAFreePeoplesPossessionIsPlayedItMayBeRevealedToMakeTheFreePeoplesPlayerExertACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var axe = scn.GetFreepsCard("axe");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable(gimli, aragorn, sam);
		scn.MoveAlliesToTable(bounder);

		var champion = scn.GetShadowCard("champion");
		var runner = scn.GetShadowCard("runner");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveCardsToHand(axe, cart);
		scn.MoveCardsToHand(champion, runner);
		// Sam, vitality 4, is exhausted.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInHand(champion, runner);
		assertInZone(Zone.SUPPORT, stragglers);
		scn.FreepsRefreshDecision();

		// A [dwarven] possession borne by Gimli; the [men] card spotted is a condition.
		scn.FreepsPlayCard(axe);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowAcceptOptionalTrigger();

		// The Champion is shown to the Free Peoples player alone, and nothing else in the hand is.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(champion, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// The Free Peoples player chooses the companion: not the exhausted Sam, nor the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInHand(champion, runner);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second possession, of another culture and in the support area: again.
		scn.FreepsPlayCard(cart);
		assertInZone(Zone.SUPPORT, cart);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInHand(champion);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFreePeoplesConditionOrArtifactPlayedDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var stairs = scn.GetFreepsCard("stairs");
		var phial = scn.GetFreepsCard("phial");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);

		var champion = scn.GetShadowCard("champion");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveCardsToHand(stairs, phial, axe);
		scn.MoveCardsToHand(champion);
		assertInHand(champion);
		assertInZone(Zone.SUPPORT, stragglers);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsRefreshDecision();

		// A Free Peoples condition, and a Free Peoples artifact: neither is a possession.
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(phial);
		assertInZone(Zone.ATTACHED, phial);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// On the same board a possession does.
		scn.FreepsPlayCard(axe);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
	}

	@Test
	public void IsOfferedOnlyWhileAMenCardCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var cart = scn.GetFreepsCard("cart");
		scn.MoveCompanionsToTable(gimli);

		var champion = scn.GetShadowCard("champion");
		var fear = scn.GetShadowCard("fear");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveCardsToHand(axe, cart);
		scn.MoveCardsToHand(champion);
		assertInHand(champion);
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsRefreshDecision();

		// A [sauron] condition is the only Shadow card in play, and the Champion in hand is not in play.
		scn.FreepsPlayCard(axe);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [men] possession in the support area is a [men] card to spot.
		scn.MoveCardsToSupportArea(fleet);
		assertInZone(Zone.SUPPORT, fleet);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(cart);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMenMinionInPlayIsSpottedAndOnlyTheCopyInHandIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(eowyn);

		var champion = scn.GetShadowCard("champion");
		var champion2 = scn.GetShadowCard("champion2");

		scn.StartGame();
		scn.MoveCardsToHand(spear);
		assertInHand(spear);
		scn.AddWoundsToChar(eowyn, 1);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The copy in play is the only [men] card in play.
		scn.MoveMinionsToTable(champion);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToHand(champion2);
		assertInHand(champion2);
		assertInZone(Zone.SHADOW_CHARACTERS, champion);
		scn.FreepsRefreshDecision();

		// Eowyn plays the [rohan] possession on herself as her ability's cost.
		scn.FreepsUseCardAction(eowyn);
		assertInZone(Zone.ATTACHED, spear);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(champion2, false));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(champion2);
		assertInZone(Zone.SHADOW_CHARACTERS, champion);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningShowsNothingAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);

		var champion = scn.GetShadowCard("champion");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveCardsToHand(axe);
		scn.MoveCardsToHand(champion);
		assertInHand(champion);
		assertInZone(Zone.SUPPORT, stragglers);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(axe);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(champion);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedItIsStillOfferedAndRevealedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);

		var champion = scn.GetShadowCard("champion");
		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveCardsToHand(axe);
		scn.MoveCardsToHand(champion);
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInHand(champion);
		assertInZone(Zone.SUPPORT, stragglers);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(axe);
		// The reveal is a cost, and can be paid (Q-0034).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(champion));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(champion, false));
		scn.FreepsDismissRevealedCards();

		// Nobody can exert; the ally, who could, is no companion.
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInHand(champion);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowPossessionOrMinionPlayedDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var champion = scn.GetShadowCard("champion");
		var runner = scn.GetShadowCard("runner");
		var champion2 = scn.GetShadowCard("champion2");
		var poleaxe = scn.GetShadowCard("poleaxe");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();
		scn.MoveCardsToHand(champion, champion2, poleaxe);
		assertInHand(champion, champion2, poleaxe);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.ShadowRefreshDecision();

		// A [men] possession on the [men] Runner, played by the Shadow player: the Runner is a [men] card to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(poleaxe);
		assertInZone(Zone.ATTACHED, poleaxe);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(champion);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A Shadow minion played, the other copy itself: no possession either.
		scn.ShadowPlayCard(champion2);
		assertInZone(Zone.SHADOW_CHARACTERS, champion2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(champion);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItsOwnersOwnPossessionPlayedOnHisOwnTurnDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var champion = scn.GetFreepsCard("champion");
		scn.MoveCompanionsToTable(gimli);

		var stragglers = scn.GetShadowCard("stragglers");
		scn.MoveCardsToSupportArea(stragglers);

		scn.StartGame();
		scn.MoveCardsToHand(axe, champion);
		assertInHand(champion);
		assertInZone(Zone.SUPPORT, stragglers);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsRefreshDecision();

		// On his own turn he is the Free Peoples player, and a Shadow card in his hand is not his to use then.
		scn.FreepsPlayCard(axe);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(champion));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInHand(champion);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsFierceWhileEachCompanionIsWoundedAndNotOtherwise() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var champion = scn.GetShadowCard("champion");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(champion, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Aragorn is not wounded.
		assertFalse(scn.HasKeyword(champion, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Every companion is wounded; the ally is not, and is no companion.  Only the Champion is fierce.
		assertTrue(scn.HasKeyword(champion, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
