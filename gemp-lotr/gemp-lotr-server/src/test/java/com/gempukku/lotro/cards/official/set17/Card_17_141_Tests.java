package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "17_141");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4, site 3.  Her text reads [wraith]
					// conditions on companions and allies, and there are none in play.
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl of [raider].  His text needs 3 burdens.
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl.  Its text needs 3 burdens.
					put("creeper", "8_75");
					// Black Breath: a [wraith] condition, kept in hand
					put("breath", "1_207");
					// Strength Born of Fear: a [sauron] condition, kept in hand
					put("fear", "1_278");
					// Aragorn, Ranger of the North and Gimli, Son of Glóin: companions.  Neither ability is used.
					put("aragorn", "1_89");
					put("gimli", "1_13");
					// Calaglin, Elf of Lórien: an ally.  His regroup ability is not used.
					put("calaglin", "3_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Úlairë Otsëa, Duplicitous Specter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>When you play Úlairë Otsëa, you may spot another Nazgûl to draw a card for each wounded companion you can spot.<br><b>Regroup:</b> Exert Úlairë Otsëa and discard a [wraith] card from hand to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Duplicitous Specter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAWraithNazgulSpottedSheDrawsACardForEachWoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(calaglin);
		// Two wounded companions.  An unwounded companion, a wounded ally and a wounded minion do not count.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(calaglin, 1);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.AddWoundsToChar(lemenya, 1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(otsea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowAcceptOptionalTrigger();

		// 2 for Frodo and Aragorn, and nothing is asked about what to count.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureIsSpottedAndThreeWoundedCompanionsDrawThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(gimli, 2);

		var otsea = scn.GetShadowCard("otsea");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(otsea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowAcceptOptionalTrigger();

		// One card for each wounded companion, whatever his wounds: 3.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithMinionWhoIsNoNazgulIsNotSpottedAndSheDoesNotSpotHerself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);

		var otsea = scn.GetShadowCard("otsea");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(creeper);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(otsea);

		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedCompanionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(calaglin);
		// A wounded ally and a wounded minion; no companion is wounded.
		scn.AddWoundsToChar(calaglin, 1);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.AddWoundsToChar(lemenya, 1);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(otsea);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AddWoundsToChar(frodo, 1);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.ShadowRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(otsea);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherNazgulPlayedAfterHerIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(otsea, lemenya);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowDeclineOptionalTrigger();

		// Lemenya is played with Otsëa and Khamûl in play and two companions wounded: Otsëa's text does not
		// answer a play that is not her own.
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(lemenya);

		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsHerAndDiscardsAWraithCardToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var breath = scn.GetShadowCard("breath");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(lemenya, breath, fear);

		scn.StartGame();
		// With no minion in play the game goes from the Shadow phase straight to regroup; Otsëa is the minion
		// still standing after the skirmishes.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowActionAvailable(otsea));
		scn.ShadowUseCardAction(otsea);

		// A [wraith] minion and a [wraith] condition may be discarded; a [sauron] card may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fear));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(breath);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertInDiscard(breath);
		assertInHand(lemenya, fear);
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToHand(breath);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		// Vitality 2: one wound exhausts her.
		scn.AddWoundsToChar(otsea, 1);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(otsea));
		assertInHand(breath);
		assertFalse(scn.ShadowActionAvailable(otsea));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoWraithCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(fear);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(otsea);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInHand(fear);
		assertFalse(scn.ShadowActionAvailable(otsea));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToHand(breath);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInHand(breath);
		assertFalse(scn.ShadowActionAvailable(otsea));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
