package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("look", "6_39");
					put("look2", "6_39");       // a second copy, held in hand as a condition to discard
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3; a skirmish ability used only on purpose
					put("smeagol2", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose

					// Cards to hold in hand and discard, none with text that acts there
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("deftness", "1_293");   // Halfling Deftness: an event
					put("sword1", "1_299");     // Hobbit Sword: a possession
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("sword4", "1_299");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text (also a Shadow card for the hand)
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] minion who is no Sméagol; a regroup ability only
					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: [wraith] Nazgûl, vitality 4, fierce; a maneuver ability only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3, fierce; his text needs burdens
					put("detachment", "7_191"); // Morgul Detachment: [wraith] Orc, vitality 4; fierce and damage +1 only while a Nazgûl is spotted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DontLookatThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Don't Look at Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Spot Sméagol and discard 3 cards from hand to wound a minion Sméagol is skirmishing once (or twice if that minion is a [wraith] minion).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("look");

		assertEquals("Don't Look at Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsThreeCardsToWoundOnceAMinionSmeagolIsSkirmishingAndNothingInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var look = scn.GetFreepsCard("look");
		var look2 = scn.GetFreepsCard("look2");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var deftness = scn.GetFreepsCard("deftness");
		var troopCard = scn.GetFreepsCard("troop");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToSupportArea(look);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, raiders);
		scn.MoveCardsToHand(legolas, armor, deftness, troopCard, look2, sword1, sword2);
		assertInZone(Zone.HAND, legolas, armor, deftness, troopCard, look2, sword1, sword2);
		assertInZone(Zone.SUPPORT, look);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{smeagol, savage});
		// The Goblin Patrol Troop is left over: the Shadow player gives it to Sméagol too.
		scn.ShadowAssignToMinions(smeagol, troop);

		// Aragorn's skirmish first: Sméagol can be spotted, but no minion is one he is skirmishing yet.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiders));
		assertTrue(scn.IsCharAssignedAgainst(smeagol, savage));
		assertEquals(7, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(look));
		scn.FreepsUseCardAction(look);
		// "3 cards", of any kind: a companion, a possession, an event, a condition and a Shadow card are all offered.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, armor, deftness, troopCard, look2, sword1, sword2));
		scn.FreepsChooseCards(legolas, deftness, troopCard);
		assertInZone(Zone.DISCARD, legolas, deftness, troopCard);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 8 against the Raiding Party's 9: Aragorn loses.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.MoveCardsToHand(sword3);
		assertInZone(Zone.HAND, sword3);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(5, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(look);
		scn.FreepsChooseCards(armor, look2, sword1);
		assertInZone(Zone.DISCARD, armor, look2, sword1);
		assertInZone(Zone.HAND, sword2, sword3);
		// Either minion Sméagol is skirmishing, of two cultures and races; not the minion that fought Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(raiders, smeagol, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);
		// Once: the Savage, vitality 3, has one wound.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertInZone(Zone.SUPPORT, look);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoSmeagolToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(look);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(scn.GetFreepsCard("sword1"), scn.GetFreepsCard("sword2"), scn.GetFreepsCard("sword3"));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.FreepsResolveSkirmish(aragorn);
		// Gollum, a [gollum] minion who is no Sméagol, is in the skirmish; three cards are in hand.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(look));
	}

	@Test
	public void IsNotOfferedWithTwoCardsInHandAndIsWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(look);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(sword1, sword2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(look));

		scn.MoveCardsToHand(sword3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(look));
	}

	@Test
	public void AWraithMinionChosenIsWoundedTwiceAndANazgulOfAnotherCultureOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(look);

		var enquea = scn.GetShadowCard("enquea");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea, khamul);
		scn.MoveCardsToHand(scn.GetFreepsCard("sword1"), scn.GetFreepsCard("sword2"), scn.GetFreepsCard("sword3"),
				scn.GetFreepsCard("sword4"), scn.GetFreepsCard("legolas"), scn.GetFreepsCard("armor"));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, enquea);
		scn.ShadowAssignToMinions(smeagol, khamul);
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, enquea));
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, khamul));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(khamul));

		// Khamûl is a Nazgûl of another culture, chosen beside a [wraith] minion: wounded once.
		scn.FreepsUseCardAction(look);
		scn.FreepsChooseCards(scn.GetFreepsCard("sword1"), scn.GetFreepsCard("sword2"), scn.GetFreepsCard("sword3"));
		scn.FreepsChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// Úlairë Enquëa, a [wraith] Nazgûl of vitality 4: wounded twice, with nothing asked.
		scn.ShadowPass();
		scn.FreepsUseCardAction(look);
		// The last three cards in hand are discarded with nothing asked.
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, scn.GetFreepsCard("sword4"), scn.GetFreepsCard("legolas"), scn.GetFreepsCard("armor"));
		scn.FreepsChooseCard(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedTooAndAWraithOrcHeIsSkirmishingIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var look = scn.GetFreepsCard("look");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var sword4 = scn.GetFreepsCard("sword4");
		scn.MoveCompanionsToTable(smeagol2);
		scn.MoveCardsToSupportArea(look);

		var detachment = scn.GetShadowCard("detachment");

		scn.StartGame();
		scn.MoveMinionsToTable(detachment);
		scn.MoveCardsToHand(sword1, sword2, sword3, sword4);
		assertInZone(Zone.HAND, sword1, sword2, sword3, sword4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol2, detachment);
		scn.FreepsResolveSkirmish(smeagol2);
		assertEquals(0, scn.GetWoundsOn(detachment));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(look));

		scn.FreepsUseCardAction(look);
		scn.FreepsChooseCards(sword1, sword2, sword3);
		// The one minion he is skirmishing is chosen with nothing asked: a [wraith] Orc of vitality 4.
		assertEquals(2, scn.GetWoundsOn(detachment));
		assertInZone(Zone.HAND, sword4);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
