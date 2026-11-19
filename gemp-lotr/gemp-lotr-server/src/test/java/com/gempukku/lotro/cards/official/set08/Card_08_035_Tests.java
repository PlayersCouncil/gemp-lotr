package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fourth1", "8_35");
					put("fourth2", "8_35");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, knight, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, knight, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion who is no knight, vitality 3; a regroup ability used
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

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					// Siege Troop: [sauron] Orc, vitality 4, so that exhausting it is 3 wounds and not 2; its regroup
					// ability and Besieger are never reached
					put("siege", "8_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path, on which the Gondorian Merchant (home site 3) skirmishes at site 3 as an ally
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fourth1", "8_35");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, knight; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, knight; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Pippin, Trollslayer: a knight who is no Man; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] Man ally, vitality 1, home site 3 of the Towers block
					put("merchant", "7_97");
					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FourthLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Fourth Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Strength: -1
		 * Game Text: <b>Fortification</b>.<br><b>Skirmish:</b> Exert 2 [gondor] Men or spot 3 knights to transfer this condition from your support area to a minion skirmishing a [gondor] Man. Exhaust that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fourth1");

		assertEquals("Fourth Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
	}

	@Test
	public void WithBothPricesOpenTwoGondorMenExertToTransferItToAMinionSkirmishingAGondorManWhoIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fourth1 = scn.GetFreepsCard("fourth1");
		var fourth2 = scn.GetFreepsCard("fourth2");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Three knights: the Gondorian Knight, the Knight of Gondor and Pippin.
		scn.MoveCompanionsToTable(knight, kog, ranger, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(fourth1, fourth2);
		assertInZone(Zone.SUPPORT, fourth1, fourth2);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop1}, new PhysicalCardImpl[]{kog, troop2});
		// The Savage, left over, is put on the Knight beside the first Troop.
		scn.ShadowAssignToMinions(knight, savage);

		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsActionAvailable(fourth1));
		scn.FreepsUseCardAction(fourth1);

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert 2"));
		assertTrue(scn.FreepsChoiceAvailable("Spot 3 knights"));
		scn.FreepsChooseOption("Exert 2");

		// [gondor] Men, companions (the one skirmishing among them) and the ally; not a [gondor] Hobbit, a [rohan]
		// Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(kog, merchant);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));

		// Both minions skirmishing the Knight; not the Troop assigned to the Knight of Gondor, whose skirmish has
		// not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// The copy used goes to the Savage, and the Savage (vitality 3) is exhausted; the other copy and the Troop
		// beside him are untouched.
		assertAttachedTo(fourth1, savage);
		assertNotAttachedTo(fourth1, troop1);
		assertInZone(Zone.SUPPORT, fourth2);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));

		// No longer in the support area, the copy used is not offered again, though [gondor] Men can still exert
		// and the Troop is still skirmishing the Knight; the other copy is.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fourth1));
		assertTrue(scn.FreepsActionAvailable(fourth2));
		assertAttachedTo(fourth1, savage);
	}

	@Test
	public void SpottingThreeKnightsTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fourth1 = scn.GetFreepsCard("fourth1");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(knight, kog, pippin);
		scn.MoveCardsToSupportArea(fourth1);
		assertInZone(Zone.SUPPORT, fourth1);

		var savage = scn.GetShadowCard("savage");
		var siege = scn.GetShadowCard("siege");

		scn.StartGame();
		// Both [gondor] Men exhausted (vitality 3): no two can exert. Pippin can, but is no Man.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(kog, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		scn.MoveMinionsToTable(savage, siege);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.ShadowAssignToMinions(knight, siege);
		scn.FreepsResolveSkirmish(knight);

		// Three knights: two [gondor] Men and a Hobbit.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(siege));
		assertTrue(scn.FreepsActionAvailable(fourth1));
		scn.FreepsUseCardAction(fourth1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, siege));
		scn.FreepsChooseCard(siege);

		assertAttachedTo(fourth1, siege);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		// Vitality 4, so exhausted is 3 wounds, which exerting it twice would not reach.
		assertEquals(3, scn.GetWoundsOn(siege));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneGondorManAbleToExertAndTwoKnightsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fourth1 = scn.GetFreepsCard("fourth1");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two knights (the Gondorian Knight and Pippin), and the Ranger a [gondor] Man who is no knight.
		scn.MoveCompanionsToTable(knight, ranger, pippin, rider);
		scn.MoveCardsToSupportArea(fourth1);
		assertInZone(Zone.SUPPORT, fourth1);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);

		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The Knight can exert, and so can a [gondor] Hobbit and a [rohan] Man; the Ranger cannot.
		assertFalse(scn.FreepsActionAvailable(fourth1));
		assertInZone(Zone.SUPPORT, fourth1);
	}

	@Test
	public void InTheSkirmishOfAGondorCompanionWhoIsNoManOrAManOfAnotherCultureItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fourth1 = scn.GetFreepsCard("fourth1");
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
		scn.MoveCardsToSupportArea(fourth1);
		assertInZone(Zone.SUPPORT, fourth1);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});

		// Pippin's skirmish: the action has a cost, and is offered; its cost is paid and nothing is moved or
		// exhausted.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsActionAvailable(fourth1));
		scn.FreepsUseCardAction(fourth1);
		scn.FreepsChooseCards(knight, merchant);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertInZone(Zone.SUPPORT, fourth1);
		assertNotAttachedTo(fourth1, troop1);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: the same. The Merchant is exhausted, so the Knight and the Ranger pay unasked.
		scn.FreepsResolveSkirmish(rider);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(fourth1));
		scn.FreepsUseCardAction(fourth1);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, fourth1);
		assertNotAttachedTo(fourth1, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAGondorManWhoIsAnAllyCanBeChosenToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var fourth1 = scn.GetFreepsCard("fourth1");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, kog, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(fourth1);
		assertInZone(Zone.SUPPORT, fourth1);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Both [gondor] Men exhausted (vitality 3), so the price paid is the three knights spotted.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(kog, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 3 of the Towers block: the Merchant is at his home, and the only Man skirmishing here.
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);

		scn.FreepsResolveSkirmish(merchant);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsActionAvailable(fourth1));
		scn.FreepsUseCardAction(fourth1);

		assertAttachedTo(fourth1, troop1);
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
