package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seventh", "7_119");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
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

	// The Towers path, on which the Gondorian Merchant (home site 3) skirmishes at site 3 as an ally
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seventh", "7_119");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion; a regroup ability used only on purpose
					put("ranger", "7_116");
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
	public void SeventhLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Seventh Level
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Condition
		 * Subtype: Support Area
		 * Strength: -2
		 * Vitality: -2
		 * Game Text: <b>Fortification</b>.<br><b>Skirmish:</b> Exert 3 [gondor] Men to transfer this condition from your support area to a minion skirmishing a [gondor] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seventh");

		assertEquals("Seventh Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getStrength());
		assertEquals(-2, card.getBlueprint().getVitality());
	}

	@Test
	public void SkirmishActionExertsThreeGondorMenToTransferItToAMinionSkirmishingAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var seventh = scn.GetFreepsCard("seventh");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, kog, ranger, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(seventh);
		assertAttachedTo(narsil, merchant);
		assertInZone(Zone.SUPPORT, seventh);

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
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(seventh));
		scn.FreepsUseCardAction(seventh);

		// [gondor] Men, companions (the one skirmishing among them) and the ally; not a [gondor] Hobbit, a [rohan]
		// Man or Frodo.  Exactly three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, frodo));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(kog, ranger, merchant);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(rider));

		// Both minions skirmishing the Knight; not the Troop assigned to the Knight of Gondor, whose skirmish has
		// not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertAttachedTo(seventh, savage);
		assertNotAttachedTo(seventh, troop1);
		assertTrue(scn.GetAttachedCards(troop2).isEmpty());

		// No longer in the support area, it is not offered again, though the Knight, the Knight of Gondor and the
		// Ranger can still exert and the Troop is still skirmishing the Knight.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(seventh));
		assertAttachedTo(seventh, savage);
	}

	@Test
	public void WithOnlyTwoGondorMenAbleToExertItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seventh = scn.GetFreepsCard("seventh");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, kog, ranger, pippin, rider);
		scn.MoveCardsToSupportArea(seventh);
		assertInZone(Zone.SUPPORT, seventh);

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
		// The Knight and the Knight of Gondor can exert, and so can a [gondor] Hobbit and a [rohan] Man; the Ranger
		// cannot.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertFalse(scn.FreepsActionAvailable(seventh));
		assertInZone(Zone.SUPPORT, seventh);
	}

	@Test
	public void InTheSkirmishOfAGondorCompanionWhoIsNoManOrAManOfAnotherCultureItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seventh = scn.GetFreepsCard("seventh");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(knight, kog, ranger, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(seventh);
		assertInZone(Zone.SUPPORT, seventh);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});

		// Pippin's skirmish: the action has a cost, and is offered; its cost is paid and nothing is moved.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.FreepsActionAvailable(seventh));
		scn.FreepsUseCardAction(seventh);
		scn.FreepsChooseCards(knight, kog, merchant);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, seventh);
		assertNotAttachedTo(seventh, troop1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: the same. The Merchant is exhausted, so the three companions pay unasked.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.FreepsActionAvailable(seventh));
		scn.FreepsUseCardAction(seventh);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertInZone(Zone.SUPPORT, seventh);
		assertNotAttachedTo(seventh, savage);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAGondorManWhoIsAnAllyCanBeChosenToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var seventh = scn.GetFreepsCard("seventh");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, kog, ranger);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(seventh);
		assertInZone(Zone.SUPPORT, seventh);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 3 of the Towers block: the Merchant is at his home, and the only Man skirmishing here.
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);

		scn.FreepsResolveSkirmish(merchant);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertTrue(scn.FreepsActionAvailable(seventh));
		// The Merchant (vitality 1) cannot exert, so the three companions pay unasked; the Troop is the one target.
		scn.FreepsUseCardAction(seventh);

		assertAttachedTo(seventh, troop1);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(kog));
		assertEquals(1, scn.GetWoundsOn(ranger));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
