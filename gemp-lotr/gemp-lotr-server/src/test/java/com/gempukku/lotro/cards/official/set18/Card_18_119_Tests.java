package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_119");

					put("lurtz", "18_118");     // Lurtz, Halfling Hunter: [uruk-hai] Uruk-hai, archer; abilities used only on purpose
					put("isenlurtz", "1_127");  // Lurtz, Servant of Isengard: [isengard] Uruk-hai, archer; a maneuver ability used only on purpose
					put("uruk", "18_131");      // White Hand Uruk: [uruk-hai] Uruk-hai; a requirement to play, cheated in
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and nothing else
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai; a skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("sword", "15_163");     // Lurtz's Sword: [uruk-hai] hand weapon on an [uruk-hai] Uruk-hai; hunter 1, and fierce on Lurtz

					// [uruk-hai] cards to hold in hand, with no text that reaches the hand
					put("roar", "17_113");      // Deathly Roar: an [uruk-hai] condition
					put("spear", "17_117");     // Spear of the White Hand: an [uruk-hai] possession, no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] unbound companion; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] unbound companion; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] unbound companion, vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: an ally; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Lurtz's Bow, Black-fletch Bow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: 1
		 * Vitality: 1
		 * Game Text: Bearer must be an Uruk-hai.<br>When you play this possession on Lurtz, you may draw a card.<br><b>Archery:</b> If bearer is Lurtz, discard 2 [uruk-hai] cards from hand to exert an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Lurtz's Bow", card.getBlueprint().getTitle());
		assertEquals("Black-fletch Bow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnyUrukHaiAndOnOneWhoIsNotLurtzDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var uruk = scn.GetShadowCard("uruk");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var roar = scn.GetShadowCard("roar");
		scn.MoveCardsToHand(bow);
		assertInHand(bow);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, uruk, guard, saruman, troop);
		scn.MoveCardsToTopOfDeck(roar);
		assertEquals(roar, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(bow));
		scn.ShadowPlayCard(bow);

		// An Uruk-hai of any culture: Lurtz, the [uruk-hai] White Hand Uruk and the [sauron] Guard; not the
		// [uruk-hai] Wizard, nor the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lurtz, uruk, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowChooseCard(uruk);

		assertAttachedTo(bow, uruk);
		// Not played on Lurtz, though Lurtz is on the table: no draw is offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(roar, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedOnLurtzDrawsACardAndAPossessionPlayedOnHimAfterDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var sword = scn.GetShadowCard("sword");
		var roar = scn.GetShadowCard("roar");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToHand(bow, sword);
		assertInHand(bow, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.MoveCardsToTopOfDeck(spear, roar);
		assertInZone(Zone.DECK, spear, roar);
		assertEquals(roar, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, lurtz);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowAcceptOptionalTrigger();

		assertInHand(roar);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(spear, scn.GetShadowTopOfDeck());

		// Another possession played on Lurtz, the bow already on him: nothing more is drawn.
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, lurtz);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(spear, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheIsengardLurtzTheDrawMayBeDeclinedAndTheArcheryAbilityIsHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var isenlurtz = scn.GetShadowCard("isenlurtz");
		var roar = scn.GetShadowCard("roar");
		var spear = scn.GetShadowCard("spear");
		var uruk = scn.GetShadowCard("uruk");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(bow, spear, uruk);
		assertInHand(bow, spear, uruk);

		scn.StartGame();
		scn.MoveMinionsToTable(isenlurtz);
		scn.MoveCardsToTopOfDeck(roar);
		assertEquals(roar, scn.GetShadowTopOfDeck());
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());
		scn.FreepsPass();

		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, isenlurtz);

		// Lurtz of the [isengard] culture is Lurtz too: offered, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertEquals(2, scn.GetShadowHandCount());
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(roar, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(bow));
		scn.ShadowUseCardAction(bow);
		// The two [uruk-hai] cards in hand are the two discarded; Gimli is the only one to exert.
		assertInDiscard(spear, uruk);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscards2UrukHaiCardsToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var uruk = scn.GetShadowCard("uruk");
		var roar = scn.GetShadowCard("roar");
		var spear = scn.GetShadowCard("spear");
		var guard = scn.GetShadowCard("guard");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, arwen, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(uruk, roar, spear, guard);
		assertInHand(uruk, roar, spear, guard);
		// Vitality 3 with 2 wounds: exhausted, and so cannot be exerted.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, bow);
		assertAttachedTo(bow, lurtz);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		assertTrue(scn.ShadowActionAvailable(bow));
		scn.ShadowUseCardAction(bow);

		// The cost: two of the three [uruk-hai] cards; not the [sauron] one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, roar, spear));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(uruk, roar);
		assertInDiscard(uruk, roar);
		assertInHand(spear, guard);
		assertEquals(2, scn.GetShadowHandCount());

		// The unbound companions of two cultures; not the exhausted one, the Ring-bound ones or the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(arwen, sam, frodo, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhenTheBearerIsNotLurtz() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var uruk = scn.GetShadowCard("uruk");
		var roar = scn.GetShadowCard("roar");
		var spear = scn.GetShadowCard("spear");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(roar, spear);
		assertInHand(roar, spear);

		scn.StartGame();
		// Lurtz is on the table, but the bow is borne by another Uruk-hai.
		scn.MoveMinionsToTable(lurtz, uruk);
		scn.AttachCardsTo(uruk, bow);
		assertAttachedTo(bow, uruk);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithOneUrukHaiCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var roar = scn.GetShadowCard("roar");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(roar, guard);
		assertInHand(roar, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, bow);
		assertAttachedTo(bow, lurtz);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Two cards in hand, but only one of them [uruk-hai].
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bow));
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void WithNoUnboundCompanionAbleToExertTheArcheryAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow");
		var lurtz = scn.GetShadowCard("lurtz");
		var roar = scn.GetShadowCard("roar");
		var spear = scn.GetShadowCard("spear");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(arwen, sam);
		scn.MoveCardsToHand(roar, spear);
		assertInHand(roar, spear);
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, bow);
		assertAttachedTo(bow, lurtz);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		assertTrue(scn.ShadowActionAvailable(bow));
		scn.ShadowUseCardAction(bow);

		assertInDiscard(roar, spear);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
