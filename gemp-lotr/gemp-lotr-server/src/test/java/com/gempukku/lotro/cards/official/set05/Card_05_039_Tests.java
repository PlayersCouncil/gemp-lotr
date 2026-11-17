package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tower", "5_39");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, vitality 3; a fellowship ability used only on purpose
					put("knightofgondor", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion, vitality 3; a regroup ability used only on purpose
					put("ranger", "7_116");
					// Gondorian Merchant: the [gondor] Man ally, vitality 1; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert; its
					// response answers a wound to its bearer, who takes none here
					put("narsil", "9_34");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only
					// on purpose
					put("pippin", "103_31");
					// Rider of Rohan: a Man companion of another culture; his skirmish ability needs him mounted
					put("rider", "4_286");

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoneTowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Stone Tower
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Vitality: -2
		 * Game Text: <b>Fortification</b>. Plays to your support area.<br><b>Skirmish:</b> Exert 2 [gondor] Men to transfer this condition from your support area to a minion skirmishing a [gondor] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tower");

		assertEquals("Stone Tower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getVitality());
	}

	@Test
	public void SkirmishActionExertsTwoGondorMenToTransferItToAMinionSkirmishingAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var tower = scn.GetFreepsCard("tower");
		var knight = scn.GetFreepsCard("knight");
		var knightofgondor = scn.GetFreepsCard("knightofgondor");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, knightofgondor, ranger, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(tower);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop1}, new PhysicalCardImpl[]{knightofgondor, troop2});
		// The Savage, left over, is put on the Knight beside the first Troop.
		scn.ShadowAssignToMinions(knight, savage);

		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knightofgondor));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.FreepsActionAvailable(tower));
		scn.FreepsUseCardAction(tower);

		// [gondor] Men, companions (the one skirmishing among them) and the ally; not a [gondor] Hobbit, a [rohan]
		// Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, knightofgondor, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(knightofgondor, merchant);
		assertEquals(1, scn.GetWoundsOn(knightofgondor));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));

		// Both minions skirmishing the Knight; not the Troop assigned to the Knight of Gondor, whose skirmish has
		// not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertAttachedTo(tower, savage);
		assertNotAttachedTo(tower, troop1);

		// No longer in the support area, it is not offered again, though two [gondor] Men can still exert and the
		// Troop is still skirmishing the Knight.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tower));
		assertAttachedTo(tower, savage);
	}

	@Test
	public void WithOnlyOneGondorManAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetFreepsCard("tower");
		var knight = scn.GetFreepsCard("knight");
		var knightofgondor = scn.GetFreepsCard("knightofgondor");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, knightofgondor, pippin, rider);
		scn.MoveCardsToSupportArea(tower);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(knightofgondor, 2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);

		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The Knight can exert, and so can a [gondor] Hobbit and a [rohan] Man; the Knight of Gondor cannot.
		assertFalse(scn.FreepsActionAvailable(tower));
		assertInZone(Zone.SUPPORT, tower);
	}

	@Test
	public void InTheSkirmishOfAGondorCompanionWhoIsNoManOrAManOfAnotherCultureItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetFreepsCard("tower");
		var knight = scn.GetFreepsCard("knight");
		var knightofgondor = scn.GetFreepsCard("knightofgondor");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, knightofgondor, ranger, pippin, rider);
		scn.MoveCardsToSupportArea(tower);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});

		// Pippin's skirmish: the action has a cost, and is offered; its cost is paid and nothing is moved.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.FreepsActionAvailable(tower));
		scn.FreepsUseCardAction(tower);
		scn.FreepsChooseCards(knight, knightofgondor);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(knightofgondor));
		assertInZone(Zone.SUPPORT, tower);
		assertNotAttachedTo(tower, troop1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: the same.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.FreepsActionAvailable(tower));
		scn.FreepsUseCardAction(tower);
		scn.FreepsChooseCards(ranger, knight);
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, tower);
		assertNotAttachedTo(tower, savage);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
