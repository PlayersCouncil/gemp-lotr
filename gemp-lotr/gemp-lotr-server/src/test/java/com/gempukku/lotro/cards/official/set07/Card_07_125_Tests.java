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

public class Card_07_125_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("third1", "7_125");
					put("third2", "7_125");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, no ranger, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion and a ranger, vitality 3; a regroup ability used only on
					// purpose
					put("ranger", "7_116");
					// Arwen, Daughter of Elrond: an [elven] Elf ranger, so a ranger who is no [gondor] Man; strength +3
					// against a Nazgul only
					put("arwen", "1_30");
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
					put("third1", "7_125");
					// Ranger of Osgiliath: [gondor] Man companion and a ranger; a regroup ability used only on purpose
					put("ranger", "7_116");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger; strength +3 against a Nazgul only
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
	public void ThirdLevelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Third Level
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Site Number: 4
		 * Game Text: <b>Fortification</b>.<br><b>Skirmish:</b> Exert a [gondor] Man or spot 2 rangers to transfer this condition from your support area to a minion skirmishing a [gondor] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("third1");

		assertEquals("Third Level", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithBothPricesOpenAGondorManExertsToTransferItToAMinionSkirmishingAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var third1 = scn.GetFreepsCard("third1");
		var third2 = scn.GetFreepsCard("third2");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two rangers: the Ranger of Osgiliath and Arwen.
		scn.MoveCompanionsToTable(knight, kog, ranger, arwen, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(third1, third2);
		assertInZone(Zone.SUPPORT, third1, third2);

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
		assertTrue(scn.FreepsActionAvailable(third1));
		scn.FreepsUseCardAction(third1);

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("rangers"));
		scn.FreepsChooseOption("Exert");

		// [gondor] Men, companions (the one skirmishing among them) and the ally; not a [gondor] Hobbit, a [rohan]
		// Man, an Elf or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, ranger, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));

		// Both minions skirmishing the Knight; not the Troop assigned to the Knight of Gondor, whose skirmish has
		// not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// The copy used goes to the Savage; the other copy stays in the support area.
		assertAttachedTo(third1, savage);
		assertNotAttachedTo(third1, troop1);
		assertInZone(Zone.SUPPORT, third2);

		// No longer in the support area, the copy used is not offered again, though [gondor] Men can still exert
		// and the Troop is still skirmishing the Knight; the other copy is.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(third1));
		assertTrue(scn.FreepsActionAvailable(third2));
		assertAttachedTo(third1, savage);
	}

	@Test
	public void SpottingTwoRangersTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var third1 = scn.GetFreepsCard("third1");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(knight, ranger, arwen, pippin);
		scn.MoveCardsToSupportArea(third1);
		assertInZone(Zone.SUPPORT, third1);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Both [gondor] Men exhausted (vitality 3): neither can exert. Pippin and Arwen can, but are no Men.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);
		scn.ShadowAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// Exactly two rangers: the exhausted Ranger of Osgiliath and Arwen.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(third1));
		scn.FreepsUseCardAction(third1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		scn.FreepsChooseCard(troop1);

		assertAttachedTo(third1, troop1);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorManAbleToExertAndOneRangerItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var third1 = scn.GetFreepsCard("third1");
		var knight = scn.GetFreepsCard("knight");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Arwen the one ranger; the Knight the one [gondor] Man.
		scn.MoveCompanionsToTable(knight, arwen, pippin, rider);
		scn.MoveCardsToSupportArea(third1);
		assertInZone(Zone.SUPPORT, third1);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);

		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// A [gondor] Hobbit, a [rohan] Man and an Elf could exert; the Knight cannot.
		assertFalse(scn.FreepsActionAvailable(third1));
		assertInZone(Zone.SUPPORT, third1);
	}

	@Test
	public void InTheSkirmishOfAGondorCompanionWhoIsNoManOrAManOfAnotherCultureItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var third1 = scn.GetFreepsCard("third1");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// No ranger: the exertion is the one price open.
		scn.MoveCompanionsToTable(knight, kog, pippin, rider);
		scn.MoveCardsToSupportArea(third1);
		assertInZone(Zone.SUPPORT, third1);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{rider, savage});

		// Pippin's skirmish: the action has a cost, and is offered; its cost is paid and nothing is moved.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertTrue(scn.FreepsActionAvailable(third1));
		scn.FreepsUseCardAction(third1);
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, third1);
		assertNotAttachedTo(third1, troop1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: the same.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.FreepsActionAvailable(third1));
		scn.FreepsUseCardAction(third1);
		scn.FreepsChooseCard(kog);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertInZone(Zone.SUPPORT, third1);
		assertNotAttachedTo(third1, savage);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAGondorManWhoIsAnAllyCanBeChosenToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var third1 = scn.GetFreepsCard("third1");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(ranger, arwen);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(third1);
		assertInZone(Zone.SUPPORT, third1);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.SkipToSite(2);
		// The Ranger exhausted (vitality 3) and the Merchant of vitality 1: the price paid is the two rangers spotted.
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 3 of the Towers block: the Merchant is at his home, and the only Man skirmishing here.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(merchant));
		scn.FreepsAssignToMinions(merchant, troop1);

		scn.FreepsResolveSkirmish(merchant);
		assertTrue(scn.FreepsActionAvailable(third1));
		scn.FreepsUseCardAction(third1);

		// The one target, unasked.
		assertAttachedTo(third1, troop1);
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
