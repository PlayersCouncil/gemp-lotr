package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counsel", "15_85");
					put("counsel2", "15_85");
					put("hillman1", "15_82");   // Grousing Hillman: a [men] Man, vitality 2; his text needs a controlled site
					put("hillman2", "15_82");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is no Man (a Hobbit)
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man; his text needs 2 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, a Man
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound companion, a Man
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion, a Dwarf
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally
					// Free Peoples cards for the companions to bear, each on a bearer its text allows
					put("sword", "1_112");      // Ranger's Sword: Aragorn's hand weapon
					put("armor1", "1_92");      // Armor: a Man's armor
					put("armor2", "1_92");
					put("cloak1", "1_42");      // Elven Cloak: a companion's cloak; the minion archery total -1
					put("cloak2", "1_42");
					put("worker1", "15_135");   // Rohan Worker: a follower; cheated on, so its transfer trigger is not set off
					put("axe", "1_9");          // Dwarven Axe: a Dwarf's hand weapon
					put("darmor", "1_8");       // Dwarven Armor: a Dwarf's armor
					put("endurance", "2_4");    // Endurance of Dwarves: a Free Peoples condition a Dwarf bears
					put("hsword1", "1_299");    // Hobbit Sword: a Hobbit's hand weapon
					put("hsword2", "1_299");
					put("hsword3", "1_299");
					put("oarmor1", "10_113");   // Orc Armor: a Hobbit's armor
					put("oarmor2", "10_113");
					put("oarmor3", "10_113");
					put("salt1", "11_171");     // Salt from the Shire: a Hobbit's box; its abilities are used only on purpose
					put("salt2", "11_171");
					put("salt3", "11_171");
					put("poison1", "8_28");     // Spider Poison: a Shadow card that a companion bears
					put("poison2", "8_28");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LyingCounselStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Lying Counsel
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert your [men] Man and spot an unbound companion bearing 3 or more cards to return each Free Peoples card that companion bears to its owner's hand. The Free Peoples player may add a burden to discard those cards instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("counsel");

		assertEquals("Lying Counsel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AHinderedFreePeoplesCardThatCompanionBearsIsReturnedOrDiscardedWithTheOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("counsel");
		var card2 = scn.GetShadowCard("counsel2");
		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");
		var poison1 = scn.GetShadowCard("poison1");
		var poison2 = scn.GetShadowCard("poison2");
		scn.MoveCardsToHand(card, card2);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var armor1 = scn.GetFreepsCard("armor1");
		var cloak1 = scn.GetFreepsCard("cloak1");
		var hindered1 = scn.GetFreepsCard("worker1");
		var axe = scn.GetFreepsCard("axe");
		var darmor = scn.GetFreepsCard("darmor");
		var endurance = scn.GetFreepsCard("endurance");
		var hindered2 = scn.GetFreepsCard("cloak2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		// Each bears three Free Peoples cards that are not hindered (a possession, a condition or a follower among
		// them), a hindered Free Peoples card and a Shadow card.
		scn.AttachCardsTo(aragorn, sword, armor1, hindered1, cloak1);
		scn.AttachCardsTo(gimli, axe, darmor, endurance, hindered2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, poison1);
		scn.AttachCardsTo(gimli, poison2);
		// A hindered card has no type or culture, and is a Free Peoples card still (CR 5.0 "hinder"; Ketura on
		// Q-0098: "each Free Peoples card" reaches it).
		scn.HinderCard(hindered1, hindered2);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor1, aragorn);
		assertAttachedTo(cloak1, aragorn);
		assertAttachedTo(hindered1, aragorn);
		assertAttachedTo(poison1, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(darmor, gimli);
		assertAttachedTo(endurance, gimli);
		assertAttachedTo(hindered2, gimli);
		assertAttachedTo(poison2, gimli);
		scn.MoveMinionsToTable(hillman1, hillman2);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(hindered1));
		assertTrue(scn.IsHindered(hindered2));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(poison1, aragorn);
		assertAttachedTo(poison2, gimli);
		assertEquals(0, scn.GetFreepsHandCount());
		int burdens = scn.GetBurdens();

		scn.ShadowPlayCard(card);
		scn.ShadowChooseCard(hillman1);
		scn.ShadowChooseCard(aragorn);
		// The Free Peoples player does not add a burden: the cards go back to hand, the hindered one with them.
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseNo();

		assertEquals(burdens, scn.GetBurdens());
		assertInZone(Zone.HAND, sword, armor1, cloak1, hindered1);
		assertFalse(scn.IsHindered(hindered1));
		assertAttachedTo(poison1, aragorn);
		assertAttachedTo(axe, gimli);

		// The second copy: the other Hillman exerts, Gimli is the one companion left bearing 3 cards, and the
		// Free Peoples player adds a burden: the cards are discarded, the hindered one with them.
		scn.FreepsPass();
		scn.ShadowPlayCard(card2);
		scn.FreepsChooseYes();

		assertEquals(burdens + 1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, axe, darmor, endurance, hindered2);
		assertAttachedTo(poison2, gimli);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACompanionBearingTwoCardsAndAHinderedThirdIsBearingThreeAndOneBearingTwoIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("counsel");
		var hillman = scn.GetShadowCard("hillman1");
		scn.MoveCardsToHand(card);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var armor1 = scn.GetFreepsCard("armor1");
		var hindered = scn.GetFreepsCard("cloak1");
		var axe = scn.GetFreepsCard("axe");
		var darmor = scn.GetFreepsCard("darmor");
		var poison = scn.GetShadowCard("poison1");
		var armor2 = scn.GetFreepsCard("armor2");
		var cloak2 = scn.GetFreepsCard("cloak2");
		var hsword = scn.GetFreepsCard("hsword1");
		var oarmor = scn.GetFreepsCard("oarmor1");
		var salt = scn.GetFreepsCard("salt1");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		// Aragorn bears three cards, one of them hindered; Gimli three, none hindered, one of them a Shadow card; Boromir two; the Ring-bearer,
		// who is not unbound, three and The One Ring.
		scn.AttachCardsTo(aragorn, sword, armor1, hindered);
		scn.AttachCardsTo(gimli, axe, darmor);
		scn.AttachCardsTo(boromir, armor2, cloak2);
		scn.AttachCardsTo(frodo, hsword, oarmor, salt);

		scn.StartGame();
		scn.AttachCardsTo(gimli, poison);
		// A hindered card is a card still, and still borne
		scn.HinderCard(hindered);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor1, aragorn);
		assertAttachedTo(hindered, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(darmor, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(armor2, boromir);
		assertAttachedTo(cloak2, boromir);
		assertAttachedTo(hsword, frodo);
		assertAttachedTo(oarmor, frodo);
		assertAttachedTo(salt, frodo);
		scn.MoveMinionsToTable(hillman);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.IsHindered(hindered));
		assertAttachedTo(armor2, boromir);
		assertAttachedTo(hsword, frodo);
		assertEquals(0, scn.GetWoundsOn(hillman));
		assertTrue(scn.ShadowPlayAvailable(card));

		scn.ShadowPlayCard(card);
		// The one [men] Man exerts
		assertEquals(1, scn.GetWoundsOn(hillman));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, frodo));
		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseNo();

		assertInZone(Zone.HAND, sword, armor1, hindered);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor2, boromir);
		assertAttachedTo(hsword, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExertsAMenManOfThePlayersChoiceAndSpotsNeitherARingBoundCompanionNorAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("counsel");
		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");
		var ted = scn.GetShadowCard("ted");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(card);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		var armor1 = scn.GetFreepsCard("armor1");
		var cloak1 = scn.GetFreepsCard("cloak1");
		var axe = scn.GetFreepsCard("axe");
		var darmor = scn.GetFreepsCard("darmor");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(maggot);
		// Each bears three cards: two unbound companions of different races, a Ring-bound companion and an ally.
		scn.AttachCardsTo(aragorn, sword, armor1, cloak1);
		scn.AttachCardsTo(gimli, axe, darmor, endurance);
		scn.AttachCardsTo(sam, scn.GetFreepsCard("hsword1"), scn.GetFreepsCard("oarmor1"), scn.GetFreepsCard("salt1"));
		scn.AttachCardsTo(maggot, scn.GetFreepsCard("hsword2"), scn.GetFreepsCard("oarmor2"), scn.GetFreepsCard("salt2"));

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor1, aragorn);
		assertAttachedTo(cloak1, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(darmor, gimli);
		assertAttachedTo(endurance, gimli);
		scn.MoveMinionsToTable(hillman1, hillman2, ted, axeman);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetAttachedCards(maggot).size());
		assertEquals(0, scn.GetWoundsOn(hillman1));
		assertEquals(0, scn.GetWoundsOn(hillman2));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(axeman));

		scn.ShadowPlayCard(card);
		// Either [men] Man; neither the [men] Hobbit nor the [raider] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillman1, hillman2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, axeman));
		scn.ShadowChooseCard(hillman2);

		assertEquals(0, scn.GetWoundsOn(hillman1));
		assertEquals(1, scn.GetWoundsOn(hillman2));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(axeman));

		// Either unbound companion; neither Sam, who is Ring-bound, nor the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, maggot));
		scn.ShadowChooseCard(gimli);
		scn.FreepsChooseNo();

		assertInZone(Zone.HAND, axe, darmor, endurance);
		assertAttachedTo(sword, aragorn);
		assertEquals(3, scn.GetAttachedCards(sam).size());
		assertEquals(3, scn.GetAttachedCards(maggot).size());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithItsOnlyMenManExhaustedThoughAMenHobbitAndARaiderManCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("counsel");
		var hillman = scn.GetShadowCard("hillman1");
		var ted = scn.GetShadowCard("ted");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(card);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("axe"), scn.GetFreepsCard("darmor"), scn.GetFreepsCard("endurance"));

		scn.StartGame();
		assertInZone(Zone.HAND, card);
		scn.MoveMinionsToTable(hillman, ted, axeman);
		// Vitality 2: exhausted
		scn.AddWoundsToChar(hillman, 1);
		assertEquals(1, scn.GetWoundsOn(hillman));
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(hillman));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(3, scn.GetAttachedCards(gimli).size());
		assertInZone(Zone.HAND, card);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));
	}

	@Test
	public void CannotBePlayedWithNoUnboundCompanionBearingThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetShadowCard("counsel");
		var hillman = scn.GetShadowCard("hillman1");
		scn.MoveCardsToHand(card);

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(maggot);
		// Boromir, unbound, bears two; the Ring-bearer and the ally bear three.
		scn.AttachCardsTo(boromir, scn.GetFreepsCard("armor1"), scn.GetFreepsCard("cloak1"));
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("hsword1"), scn.GetFreepsCard("oarmor1"), scn.GetFreepsCard("salt1"));
		scn.AttachCardsTo(maggot, scn.GetFreepsCard("hsword2"), scn.GetFreepsCard("oarmor2"), scn.GetFreepsCard("salt2"));

		scn.StartGame();
		assertInZone(Zone.HAND, card);
		scn.MoveMinionsToTable(hillman);
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(2, scn.GetAttachedCards(boromir).size());
		assertEquals(3, scn.GetAttachedCards(maggot).size());
		assertEquals(0, scn.GetWoundsOn(hillman));
		assertInZone(Zone.HAND, card);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(card));
	}
}
