package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_036_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("club", "4_36");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Hill Chief: a [dunland] Man, strength 10, vitality 2, so a bearer who can lose and live; his Shadow
					// ability needs a site the Shadow player controls, and he controls none
					put("chief", "4_20");
					// Wulf, Dunlending Chieftain: a fierce [dunland] Man, strength 10, vitality 2; a response to ANOTHER
					// [dunland] Man winning, used only on purpose
					put("wulf", "4_40");
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  Laid after the move, so his
					// move trigger does not fire.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man, strength 6; +3 and fierce only at 2 burdens
					put("axeman", "4_224");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6; a skirmish ability used only on purpose
					// Ranger's Cloak: a [gondor] possession on a [gondor] Man; its minion text needs a ranger or a forest
					put("cloak", "15_68");
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession on a Dwarf
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession on a Hobbit
					put("mithril", "2_105");    // Mithril-coat: a Free Peoples ARTIFACT on Frodo
					// The Saga of Elendil: a Free Peoples CONDITION on a [gondor] companion; an ability used only on purpose
					put("saga", "1_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarClubStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: War Club
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [dunland] Man.
		 * 		Each time bearer wins a skirmish, you may discard 2 Free Peoples possessions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("club");

		assertEquals("War Club", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADunlandManOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(club);
		assertInHand(club);

		scn.StartGame();
		scn.FreepsPass();
		// The minions are laid after the move, so that Saruman's move trigger does not fire.
		scn.MoveMinionsToTable(brigand, savage, saruman, axeman);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(club);
		assertTrue(scn.ShadowPlayAvailable(club));

		scn.ShadowPlayCard(club);

		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		// A [dunland] minion who is no Man, a Man of another culture, and a Free Peoples Man.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(savage);

		assertAttachedTo(club, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBearerWinsTheShadowPlayerMayDiscardTwoFreePeoplesPossessionsAndNotWhenAnotherManWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.AttachCardsTo(boromir, cloak);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(frodo, sword, mithril);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage);
		scn.AttachCardsTo(brigand, club);

		assertAttachedTo(club, brigand);
		assertInZone(Zone.ATTACHED, cloak, armor, sword, mithril, saga);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{boromir, brigand});

		// The [dunland] Man who does not bear the club wins first: 11 against 8.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer wins: 10 against 7.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(club));

		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(cloak, armor, sword));
		// An artifact, a condition and a Shadow possession.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mithril, saga, club));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(cloak, armor);

		assertInDiscard(cloak, armor);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(mithril, frodo);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(club, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedOnceItIsOfferedAgainWhenBearerWinsHisFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var wulf = scn.GetShadowCard("wulf");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var cloak = scn.GetFreepsCard("cloak");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.AttachCardsTo(boromir, cloak);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(frodo, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(wulf);
		scn.AttachCardsTo(wulf, club);

		assertInZone(Zone.ATTACHED, club, cloak, armor, sword);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, wulf);
		scn.FreepsResolveSkirmish(boromir);
		// 12 against 7.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(club));

		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(cloak, boromir);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, wulf);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(club));

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(cloak, sword);

		assertInDiscard(cloak, sword);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var chief = scn.GetShadowCard("chief");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var cloak = scn.GetFreepsCard("cloak");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.AttachCardsTo(scn.GetRingBearer(), sword);

		scn.StartGame();
		// Aragorn is given the strength to beat the bearer: 8 + 5 against 10 + 2 for the club.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.MoveMinionsToTable(chief);
		scn.AttachCardsTo(chief, club);

		assertAttachedTo(club, chief);
		assertInZone(Zone.ATTACHED, cloak, sword);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chief);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(13, scn.GetStrength(aragorn));
		assertEquals(12, scn.GetStrength(chief));
		assertEquals(0, scn.GetWoundsOn(chief));

		// 12 against 13: the bearer loses, and lives through it with one of his two wounds.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(chief));
		assertAttachedTo(club, chief);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.ATTACHED, cloak, sword);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerWinsWithNoFreePeoplesPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var mithril = scn.GetFreepsCard("mithril");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(scn.GetRingBearer(), mithril);
		scn.AttachCardsTo(aragorn, saga);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, club);

		assertInZone(Zone.ATTACHED, club, mithril, saga);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// It won, with only an artifact, a condition and the club itself to discard.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.ATTACHED, club, mithril, saga);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneFreePeoplesPossessionInPlayThatOneIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var club = scn.GetShadowCard("club");
		var brigand = scn.GetShadowCard("brigand");
		var boromir = scn.GetFreepsCard("boromir");
		var cloak = scn.GetFreepsCard("cloak");
		var mithril = scn.GetFreepsCard("mithril");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, cloak);
		scn.AttachCardsTo(scn.GetRingBearer(), mithril);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, club);

		assertInZone(Zone.ATTACHED, club, cloak, mithril);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(club));
		scn.ShadowAcceptOptionalTrigger();

		// CR 4.2 "effect": as much as can be done is done.
		assertInDiscard(cloak);
		assertInZone(Zone.ATTACHED, club, mithril);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
