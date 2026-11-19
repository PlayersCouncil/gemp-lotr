package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "15_70");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, no ranger, strength 6, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion, no ranger, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Ranger of Osgiliath: [gondor] Man companion and a ranger, vitality 3; a regroup ability used only on
					// purpose
					put("ranger", "7_116");
					// Aragorn, Ranger of the North: a unique [gondor] Man ranger, vitality 4; a maneuver ability used only
					// on purpose
					put("aragorn", "1_89");
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
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time it wins a skirmish its player may
					// exert it twice to wound a companion
					put("skirmisher", "18_90");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "15_70");
					// Gondorian Knight: a [gondor] Man of strength 6 and vitality 3; Knight is its whole text
					put("knight1", "5_35");
					put("knight2", "5_35");
					// Uruk-hai Raiding Party: strength 9, so it beats a Knight without doubling him; Damage +1 is
					// its whole text
					put("party1", "1_158");
					put("party2", "1_158");
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
					put("wall", "15_70");
					// Ranger of Osgiliath: [gondor] Man companion and a ranger, vitality 3; a regroup ability used only
					// on purpose
					put("ranger", "7_116");
					// Aragorn, Ranger of the North: a [gondor] Man ranger, vitality 4; a maneuver ability used only on
					// purpose
					put("aragorn", "1_89");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger; strength +3 against a Nazgul only
					// Gondorian Merchant: the [gondor] Man ally, strength 6, vitality 1, home site 3 of the Towers block
					put("merchant", "7_97");
					// Uruk-hai Raiding Party: strength 9, so it beats the Merchant without doubling him; Damage +1 is
					// its whole text
					put("party", "1_158");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TremendousWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Tremendous Wall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Fortification</b>. Characters skirmishing bearer cannot take wounds from losing a skirmish involving bearer.<br><b>Skirmish:</b> Exert a [gondor] Man or spot 3 rangers to transfer this condition from your support area to a minion skirmishing a [gondor] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall");

		assertEquals("Tremendous Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACharacterWhoLosesASkirmishToItsBearerTakesNoWoundForLosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight1, party1},
				new PhysicalCardImpl[]{knight2, party2});

		// The first Knight exerts to move the Wall onto the minion he is skirmishing.
		scn.FreepsResolveSkirmish(knight1);
		assertEquals(0, scn.GetWoundsOn(knight1));
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight1, knight2));
		scn.FreepsChooseCard(knight1);
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertAttachedTo(wall, party1);

		// 6 against 9 and damage +1: he loses, and takes neither of the two wounds.  He has the one he
		// exerted for, where three would have killed him.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertInZone(Zone.FREE_CHARACTERS, knight1);
		assertEquals(0, scn.GetWoundsOn(party1));

		// The control: the minion the second Knight loses to bears no Wall, and he takes both.
		scn.FreepsResolveSkirmish(knight2);
		assertEquals(0, scn.GetWoundsOn(knight2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(knight2));
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertAttachedTo(wall, party1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACharacterSkirmishingItsBearerWhoIsAnAllyTakesNoWoundForLosingEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wall = scn.GetFreepsCard("wall");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(ranger, aragorn, arwen);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.SkipToSite(2);
		// Both [gondor] companions exhausted and the Merchant of vitality 1: the price paid is the three rangers
		// spotted.
		scn.AddWoundsToChar(ranger, 2);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		// Site 3 of the Towers block: the Merchant is at his home.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(merchant, party);

		scn.FreepsResolveSkirmish(merchant);
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		assertAttachedTo(wall, party);

		// 6 against 9, damage +1: he loses, and takes neither of the two wounds, one of which would have killed him.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertInZone(Zone.SUPPORT, merchant);
		assertEquals(0, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItsBearerStillTakesItsWoundWhenItLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);

		scn.FreepsResolveSkirmish(knight);
		scn.FreepsUseCardAction(wall);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(wall, savage);
		assertEquals(0, scn.GetWoundsOn(savage));

		// 6 against 5: the Savage, the bearer, loses and is wounded as usual; it skirmishes no bearer.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundThatIsNotForLosingStillReachesACharacterSkirmishingItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var skirmisher = scn.GetShadowCard("skirmisher");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, skirmisher);

		scn.FreepsResolveSkirmish(knight);
		scn.FreepsUseCardAction(wall);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertAttachedTo(wall, skirmisher);
		assertEquals(0, scn.GetWoundsOn(skirmisher));

		// 6 against 9: the Knight loses and takes no wound for it.  The Skirmisher's own trigger on its win then
		// wounds him: that wound is not one for losing.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight, frodo));
		scn.ShadowChooseCard(knight);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithBothPricesOpenAGondorManExertsToTransferItToAMinionSkirmishingAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Three rangers: the Ranger of Osgiliath, Aragorn and Arwen.
		scn.MoveCompanionsToTable(knight, kog, ranger, aragorn, arwen, pippin, rider);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

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
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("rangers"));
		scn.FreepsChooseOption("Exert");

		// [gondor] Men, companions (the one skirmishing among them) and the ally; not a [gondor] Hobbit, a [rohan]
		// Man, an Elf or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, ranger, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, rider, arwen, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Both minions skirmishing the Knight; not the Troop assigned to the Knight of Gondor, whose skirmish has
		// not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		assertAttachedTo(wall, savage);
		assertNotAttachedTo(wall, troop1);

		// No longer in the support area, it is not offered again, though [gondor] Men can still exert and the Troop
		// is still skirmishing the Knight.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(wall));
		assertAttachedTo(wall, savage);
	}

	@Test
	public void SpottingThreeRangersTransfersItWithNobodyExerting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(knight, ranger, aragorn, arwen, pippin);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Every [gondor] Man exhausted (vitality 3, 3 and 4): none can exert. Pippin and Arwen can, but are no Men.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(ranger, 2);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(troop1, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);
		scn.ShadowAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		// Exactly three rangers: the exhausted Ranger of Osgiliath and Aragorn, and Arwen.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		scn.FreepsChooseCard(troop1);

		assertAttachedTo(wall, troop1);
		assertTrue(scn.GetAttachedCards(savage).isEmpty());
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorManAbleToExertAndTwoRangersItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// Two rangers, the Ranger of Osgiliath and Arwen; the Knight and the Ranger the [gondor] Men.
		scn.MoveCompanionsToTable(knight, ranger, arwen, pippin, rider);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Vitality 3: both exhausted.
		scn.AddWoundsToChar(knight, 2);
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(2, scn.GetWoundsOn(ranger));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop1);

		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// A [gondor] Hobbit, a [rohan] Man and an Elf could exert; no [gondor] Man can, and two rangers are one short.
		assertFalse(scn.FreepsActionAvailable(wall));
		assertInZone(Zone.SUPPORT, wall);
	}

	@Test
	public void InTheSkirmishOfAGondorCompanionWhoIsNoManOrAManOfAnotherCultureItIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetFreepsCard("wall");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		// No ranger: the exertion is the one price open.
		scn.MoveCompanionsToTable(knight, kog, pippin, rider);
		scn.MoveCardsToSupportArea(wall);
		assertInZone(Zone.SUPPORT, wall);

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
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		scn.FreepsChooseCard(knight);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, wall);
		assertNotAttachedTo(wall, troop1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// The Rider's skirmish: the same.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.FreepsActionAvailable(wall));
		scn.FreepsUseCardAction(wall);
		scn.FreepsChooseCard(kog);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertInZone(Zone.SUPPORT, wall);
		assertNotAttachedTo(wall, savage);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
