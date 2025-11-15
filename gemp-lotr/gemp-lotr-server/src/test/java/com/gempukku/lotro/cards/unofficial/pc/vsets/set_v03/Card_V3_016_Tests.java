package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("two", "103_16");
					put("noser", "5_28");
					put("noser2", "5_28");
					put("slinker", "5_29");
					put("arb", "9_30");

					put("shelob", "10_23"); //Shelob, Her Ladyship
					put("gollum", "9_28");

					put("breath", "1_207"); //Black Breath
					put("follow", "5_23"); //Follow Smeagol
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}
	
	protected VirtualTableScenario GetARBScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("two", "103_16");
					put("slinker", "5_29");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.SmeagolRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TwoMindsofItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Two Minds of It
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Restore Smeagol.  Then you may spot Smeagol (if he is not the Ring-bearer) and add a threat to replace him with another Smeagol from your discard pile (keep all tokens and borne cards).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("two");

		assertEquals("Two Minds of It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TwoMindsofItRestoresHinderedSmeagolAndExchangesForADifferentOneInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		scn.HinderCard(noser);

		scn.StartGame();

		assertTrue(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = noser.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, noser.getCardId());
		assertNotEquals(newId, noser.getCardId());
	}

	@Test
	public void TwoMindsofItExchangesUnhinderedSmeagolForADifferentOneInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		scn.StartGame();

		assertFalse(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = noser.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, noser.getCardId());
		assertNotEquals(newId, noser.getCardId());
	}

	@Test
	public void TwoMindsofItExchangesDuringManeuver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		var shelob = scn.GetShadowCard("shelob");
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = noser.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, noser.getCardId());
		assertNotEquals(newId, noser.getCardId());
	}

	@Test
	public void TwoMindsofItExchangesDuringSkirmishWithoutCancelingSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		var shelob = scn.GetShadowCard("shelob");
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(noser, shelob);

		assertFalse(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.IsCharSkirmishing(noser));
		assertTrue(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.IsCharSkirmishingAgainst(noser, shelob));

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = noser.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, noser.getCardId());
		assertNotEquals(newId, noser.getCardId());

		assertFalse(scn.IsCharSkirmishing(noser));
		assertTrue(scn.IsCharSkirmishing(slinker));
		assertTrue(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.IsCharSkirmishingAgainst(slinker, shelob));
	}

	@Test
	public void TwoMindsofItExchangesDuringSkirmishWithoutCancelingAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveMinionsToTable(shelob, gollum);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { noser, shelob },
				new PhysicalCardImpl[] { frodo, gollum }
		);
		scn.FreepsResolveSkirmish(frodo);

		assertFalse(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		assertFalse(scn.IsCharSkirmishing(noser));
		assertFalse(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.IsCharAssignedAgainst(noser, shelob));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = noser.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, noser.getCardId());
		assertNotEquals(newId, noser.getCardId());

		assertFalse(scn.IsCharSkirmishing(noser));
		assertFalse(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.IsCharAssignedAgainst(slinker, shelob));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, gollum));
	}

	@Test
	public void TwoMindsofItDoesNotExchangeIfNoSmeagolOnTable() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCardsToDiscard(slinker, noser);


		scn.StartGame();

		assertInDiscard(slinker, noser);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
	}

	@Test
	public void TwoMindsofItDoesNotExchangeIfNoSmeagolInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two, slinker);
		scn.MoveCompanionsToTable(noser);

		scn.StartGame();
		
		assertInPlay(noser);
		assertInHand(slinker);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
	}

	@Test
	public void TwoMindsofItDoesNotExchangeIfOnlyDuplicateSmeagolInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var noser2 = scn.GetFreepsCard("noser2");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(noser2);

		scn.StartGame();

		assertInPlay(noser);
		assertInDiscard(noser2);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
	}

	@Test
	public void TwoMindsofItPreservesWoundsAndModifiers() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		scn.AddWoundsToChar(noser, 2);

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToHand(shelob);
		scn.MoveMinionsToTable(gollum);

		scn.StartGame();

		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		scn.ShadowPlayCard(shelob);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(noser);

		scn.SkipToAssignments();

		assertFalse(scn.FreepsCanAssign(noser)); //Her Ladyship's sting
		scn.FreepsAssignToMinions(frodo, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(2, scn.GetWoundsOn(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);
		assertEquals(2, scn.GetWoundsOn(slinker));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsDeclineOptionalTrigger(); //the one ring

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		assertFalse(scn.FreepsCanAssign(slinker)); //Her Ladyship's sting transferred to Slinker
	}

	@Test
	public void TwoMindsofItExchangesARBSmeagolWhoIsNotRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var arb = scn.GetFreepsCard("arb");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(arb);
		scn.MoveCardsToDiscard(slinker);

		scn.StartGame();

		assertFalse(scn.IsHindered(arb));
		assertInPlay(arb);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		//Replacement involves some trickery with IDs to preserve identity, so we will double-check
		int oldId = arb.getCardId();
		int newId = slinker.getCardId();

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(arb));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(arb);

		assertEquals(oldId, slinker.getCardId());
		assertNotEquals(oldId, arb.getCardId());
		assertNotEquals(newId, arb.getCardId());
	}

	@Test
	public void TwoMindsofItDoesNotExchangeARBSmeagolWhoIsRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetARBScenario(); //Smeagol is the Ring-bearer

		var two = scn.GetFreepsCard("two");
		var slinker = scn.GetFreepsCard("slinker");
		scn.MoveCardsToHand(two);
		scn.MoveCardsToDiscard(slinker);

		scn.StartGame();

		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
	}

	@Test
	public void TwoMindsofItPreservesAttachedCardsWhenExchanging() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var two = scn.GetFreepsCard("two");
		var noser = scn.GetFreepsCard("noser");
		var slinker = scn.GetFreepsCard("slinker");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCardsToHand(two);
		scn.MoveCompanionsToTable(noser);
		scn.MoveCardsToDiscard(slinker);

		var breath = scn.GetShadowCard("breath");

		scn.AttachCardsTo(noser, follow, breath);

		scn.StartGame();

		assertFalse(scn.IsHindered(noser));
		assertInPlay(noser);
		assertInDiscard(slinker);
		assertEquals(0, scn.GetThreats());

		assertAttachedTo(follow, noser);
		assertAttachedTo(breath, noser);

		assertTrue(scn.FreepsPlayAvailable(two));

		scn.FreepsPlayCard(two);
		assertFalse(scn.IsHindered(noser));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to add a threat to replace Smeagol with another in your discard pile?"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetThreats());

		assertInPlay(slinker);
		assertInDiscard(noser);

		assertAttachedTo(follow, slinker);
		assertAttachedTo(breath, slinker);
	}
}
