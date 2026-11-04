package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "17_18");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: +1 strength for each of Hobbit, Dwarf, Elf, Man companion spotted
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; +2 only at 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; his maneuver ability is never used
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion, only ever in hand here
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally (home 1F, vitality 2); his ability is never used

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and no other text
					put("assassin", "2_93");    // Tower Assassin: strength 11; "Maneuver: Exert Tower Assassin to wound an ally"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Glamdring, Orc Beater
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf.<br>Bearer is <b>damage +1</b>.<br>Each time a companion is about to take a wound, you may discard a companion from hand to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertEquals("Orc Beater", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayableOnlyOnGandalfAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(glamdring, gandalf);
		assertInHand(glamdring, gandalf);

		scn.StartGame();

		// A [gandalf] companion who is no Gandalf cannot bear it.
		assertFalse(scn.FreepsPlayAvailable(glamdring));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(glamdring));

		// Gandalf is the one bearer it can have, so it goes on him unasked.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachWoundOnACompanionMayBePreventedByDiscardingACompanionFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(avenger, boromir, barliman);
		assertAttachedTo(glamdring, gandalf);
		assertInHand(avenger, boromir, barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetFreepsHandCount());
		// Aragorn 8 against the Raiding Party's 9: he loses, and its damage +1 makes two wounds.
		scn.PassSkirmishActions();

		// The first wound.  Only the companions in hand can pay; the ally cannot.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);
		assertInDiscard(boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The second wound is asked again, and declined: it lands, and the hand keeps its cards.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInHand(avenger, barliman);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItProtectsGandalfAndIsNotOfferedForAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(avenger);
		assertAttachedTo(glamdring, gandalf);
		assertInHand(avenger);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(barliman));
		scn.FreepsPass();
		// The Assassin wounds the one ally: no companion is about to take a wound, so nothing is offered.
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertInHand(avenger);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, assassin);
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// Gandalf 6 + 1 (Frodo, a Hobbit) + 2 = 9 against 11: he loses one wound.
		scn.PassSkirmishActions();

		// One companion in hand: it pays unasked, and Gandalf's wound is prevented.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		assertInDiscard(avenger);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionInHandTheWoundIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, glamdring);
		// An ally in hand, and no companion.
		scn.MoveCardsToHand(barliman);
		assertAttachedTo(glamdring, gandalf);
		assertInHand(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Both wounds land, and nothing is asked.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInHand(barliman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
