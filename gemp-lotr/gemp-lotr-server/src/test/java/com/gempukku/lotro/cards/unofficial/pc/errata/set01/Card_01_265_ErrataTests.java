package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_265_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("butchery", "51_265");
					put("slayer", "3_93");
					put("scavengers", "1_179");

					put("sam", "2_114");
					put("sword", "1_299");
					put("guard1", "1_7");
					put("guard2", "1_7");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void OrcButcheryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Orc Butchery
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 0
		* Type: event
		* Subtype: Response
		* Game Text: If a companion is killed by a [sauron] Orc, then for each card in the dead pile the Free Peoples
		 * player must choose to add a burden or discard 4 cards from the top of their draw deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetFreepsCard("butchery");

		assertFalse(butchery.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, butchery.getBlueprint().getSide());
		assertEquals(Culture.SAURON, butchery.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, butchery.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(butchery, Timeword.RESPONSE));
		assertEquals(0, butchery.getBlueprint().getTwilightCost());
	}

	@Test
	public void ButcheryIsTriggeredBySkirmishOverwhelmKill() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void ButcheryIsTriggeredBySkirmishWoundKill() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsAttachCardsTo(sam, "sword");

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void ButcheryIsTriggeredByDirectWoundAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 3);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(slayer);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void ButcheryForcesBurdenIfLessThan4TopDeckCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardToDiscard("sword", "guard1", "guard2");

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.HAND, butchery.getZone());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertEquals(Zone.DISCARD, butchery.getZone());
	}

	@Test
	public void ButcheryOffersChoiceOfBurdenOr4TopDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		assertTrue(scn.FreepsChoiceAvailable("Discard the top 4 cards of your draw deck"));

		assertEquals(6, scn.GetFreepsDeckCount());
		scn.FreepsChooseMultipleChoiceOption("Discard");
		assertEquals(2, scn.GetFreepsDeckCount());
	}

	@Test
	public void ButcheryForcesChoicePerDeadPileDenizen() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var butchery = scn.GetShadowCard("butchery");
		var slayer = scn.GetShadowCard("slayer");
		scn.ShadowMoveCardToHand(butchery);
		scn.ShadowMoveCharToTable(slayer);

		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardToDeadPile("guard1", "guard2");

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, slayer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseMultipleChoiceOption("Burden");

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseMultipleChoiceOption("Burden");

		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseMultipleChoiceOption("Burden");
		assertEquals(4, scn.GetBurdens());
	}
}
