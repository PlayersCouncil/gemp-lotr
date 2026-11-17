package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_049_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("company", "18_49");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, vitality 3; Knight is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession borne by Aragorn; damage +1
					// Catapult: a [gondor] possession in the support area; a maneuver ability that costs 2 cards from hand,
					// used only on purpose
					put("catapult", "8_32");
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: a [gondor] artifact on a [gondor] Man; a response to its bearer's wound only
					put("sting", "1_313");      // Sting: a [shire] possession on Frodo; a fellowship or regroup ability used only on purpose
					// Gwaihir, The Windlord: a [gandalf] follower, Aid - Add a threat; his trigger needs a [gandalf]
					// Wizard bearer who wins a skirmish
					put("gwaihir", "17_20");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirsCompanyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Faramir's Company
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Follower
		 * Aid: Add a threat
		 * Game Text: <b>Aid</b> – Add a threat. <helper>(At the start of the maneuver phase, you may add a threat to transfer this to a companion.)</helper><br>When you attach this card to a companion, spot a [gondor] possession to heal that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("company");

		assertEquals("Faramir's Company", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeItsAidAttachesItToACompanionWhileABorneGondorPossessionIsSpottedThatCompanionIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(company);

		scn.StartGame();
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(aragorn, 1);
		// Only there so that there is a maneuver phase; nobody is assigned to it.
		scn.MoveMinionsToTable("troop");
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, company);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(company, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, aragorn, frodo));
		scn.FreepsChooseCard(knight);

		// Aid - add a threat: a threat, and no twilight.  Then the heal, which asks nothing: one wound off the
		// Knight, and none off Aragorn beside him.
		assertEquals(1, scn.GetThreats());
		assertEquals(pool, scn.GetTwilight());
		assertAttachedTo(company, knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second maneuver phase in the same turn: the Aid taken again gives it to Aragorn, and that attaching
		// heals him in turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(company, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetThreats());
		assertAttachedTo(company, aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AGondorPossessionInTheSupportAreaIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");
		var knight = scn.GetFreepsCard("knight");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(company, catapult);

		scn.StartGame();
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.SUPPORT, company, catapult);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(knight);

		assertAttachedTo(company, knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGondorPossessionToSpotNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var narsil = scn.GetFreepsCard("narsil");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(knight);
		// A [gondor] artifact, and a possession of another culture.
		scn.AttachCardsTo(knight, narsil);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(company);

		scn.StartGame();
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable("troop");
		assertAttachedTo(narsil, knight);
		assertAttachedTo(sting, frodo);
		assertInZone(Zone.SUPPORT, company);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetThreats());
		assertAttachedTo(company, knight);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherFollowerAttachedToAWoundedCompanionHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(company, gwaihir);

		scn.StartGame();
		scn.AddWoundsToChar(knight, 2);
		scn.MoveMinionsToTable("troop");
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, company, gwaihir);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(company, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir, "Aid"));
		scn.FreepsChooseOptionalTrigger(gwaihir);
		scn.FreepsChooseCard(knight);
		assertAttachedTo(gwaihir, knight);
		assertEquals(1, scn.GetThreats());

		// Gwaihir on the Knight heals nothing; what is still offered is this card's own Aid, declined.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(company, "Aid"));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, company);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
