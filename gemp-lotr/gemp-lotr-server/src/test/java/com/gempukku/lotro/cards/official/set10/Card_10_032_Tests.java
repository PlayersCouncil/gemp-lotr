package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_10_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fifth1", "10_32");
					put("fifth2", "10_32");
					// First Level: a [gondor] fortification condition; its maneuver ability is used only on purpose
					put("first", "7_92");
					// Stronghold of My Fathers: a [rohan] fortification POSSESSION in the support area; its response
					// answers a wound to a [rohan] companion, and none is wounded here
					put("stronghold", "102_48");
					// Rally Point: a [gondor] support-area condition that is no fortification; its regroup ability is
					// never reached
					put("rally", "5_38");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, knight, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, knight, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: a [gondor] Man companion who is no knight, vitality 3; a regroup ability used
					// only on purpose
					put("ranger", "7_116");
					// Gondorian Merchant: the [gondor] Man ally, vitality 1; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert; its
					// response answers a wound to its bearer, who takes none here
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), and a knight; a skirmish
					// ability used only on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FifthLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Fifth Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Bearer is strength -1 for each fortification you can spot.<br><b>Maneuver:</b> Exert 2 [gondor] Men or spot 3 knights to transfer this condition to a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fifth1");

		assertEquals("Fifth Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOnlyTheExertionOpenTwoGondorMenExertToTransferItToAMinionWhoIsOneWeakerForEachFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fifth1 = scn.GetFreepsCard("fifth1");
		var fifth2 = scn.GetFreepsCard("fifth2");
		var rally = scn.GetFreepsCard("rally");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two knights only (the Gondorian Knight and Pippin): the exertion is the one price open.
		scn.MoveCompanionsToTable(knight, ranger, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(fifth1, fifth2, rally);
		assertInZone(Zone.SUPPORT, fifth1, fifth2, rally);
		assertAttachedTo(narsil, merchant);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// In the support area it has no bearer: nobody is weakened.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(rider));

		assertTrue(scn.FreepsActionAvailable(fifth1));
		scn.FreepsUseCardAction(fifth1);

		// [gondor] Men, companions and the ally; not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(ranger, merchant);
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(rider));

		// Minions of two cultures and races; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// The copy used goes to the Troop; the other copy stays in the support area.
		assertAttachedTo(fifth1, troop);
		assertInZone(Zone.SUPPORT, fifth2, rally);
		// 13 - 2: the two copies are the fortifications spotted, the one borne included; Rally Point is none.
		assertEquals(11, scn.GetStrength(troop));
		// The minion beside it, and the companions, bear nothing and are unchanged.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithBothPricesOpenThePlayerIsAskedAndSpottingThreeKnightsTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fifth1 = scn.GetFreepsCard("fifth1");
		var first = scn.GetFreepsCard("first");
		var stronghold = scn.GetFreepsCard("stronghold");
		var rally = scn.GetFreepsCard("rally");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		// Three knights (two [gondor] Men and a Hobbit), and three [gondor] Men able to exert.
		scn.MoveCompanionsToTable(knight, kog, ranger, pippin);
		scn.MoveCardsToSupportArea(fifth1, first, stronghold, rally);
		assertInZone(Zone.SUPPORT, fifth1, first, stronghold, rally);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(pippin));

		assertTrue(scn.FreepsActionAvailable(fifth1));
		scn.FreepsUseCardAction(fifth1);

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert 2"));
		assertTrue(scn.FreepsChoiceAvailable("Spot 3 knights"));
		scn.FreepsChooseOption("Spot 3 knights");

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertAttachedTo(fifth1, savage);
		assertInZone(Zone.SUPPORT, first, stronghold, rally);
		// Nobody exerts.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(pippin));
		// 5 - 3: Fifth Level itself, First Level (a condition) and Stronghold of My Fathers (a possession); Rally
		// Point is no fortification.
		assertEquals(2, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithEveryGondorManExhaustedThreeKnightsPayAndNobodyIsAskedOrExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fifth1 = scn.GetFreepsCard("fifth1");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(knight, kog, pippin);
		scn.MoveCardsToSupportArea(fifth1);
		assertInZone(Zone.SUPPORT, fifth1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Both [gondor] Men exhausted (vitality 3): no two can exert. Pippin can, but is no Man.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(kog, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(fifth1));
		scn.FreepsUseCardAction(fifth1);

		// No question of which price: the minion is asked for at once.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);

		assertAttachedTo(fifth1, troop);
		// 13 - 1: itself the one fortification.
		assertEquals(12, scn.GetStrength(troop));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneGondorManAbleToExertAndTwoKnightsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fifth1 = scn.GetFreepsCard("fifth1");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two knights (the Gondorian Knight and Pippin), and the Ranger a [gondor] Man who is no knight.
		scn.MoveCompanionsToTable(knight, ranger, pippin, rider);
		scn.MoveCardsToSupportArea(fifth1);
		assertInZone(Zone.SUPPORT, fifth1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Knight can exert, and so can a [gondor] Hobbit and a [rohan] Man; the Ranger cannot.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fifth1));
		assertInZone(Zone.SUPPORT, fifth1);
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void BorneByAMinionItCanBeTransferredAgainToAnotherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fifth1 = scn.GetFreepsCard("fifth1");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, kog, ranger);
		scn.MoveCardsToSupportArea(fifth1);
		assertInZone(Zone.SUPPORT, fifth1);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		scn.FreepsUseCardAction(fifth1);
		scn.FreepsChooseCards(knight, kog);
		scn.FreepsChooseCard(troop);
		assertAttachedTo(fifth1, troop);
		assertEquals(12, scn.GetStrength(troop));
		scn.ShadowPass();

		// The text does not say "from your support area": on the Troop it is offered again.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(fifth1));
		scn.FreepsUseCardAction(fifth1);
		// The Knight and the Knight of Gondor have one wound each and can exert again, as the Ranger can.
		scn.FreepsChooseCards(knight, ranger);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(ranger));

		// The Troop already bears it, so the Savage is the one minion it can go to, and is taken unasked.
		assertAttachedTo(fifth1, savage);
		assertNotAttachedTo(fifth1, troop);
		assertEquals(13, scn.GetStrength(troop));
		// 5 - 1
		assertEquals(4, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
