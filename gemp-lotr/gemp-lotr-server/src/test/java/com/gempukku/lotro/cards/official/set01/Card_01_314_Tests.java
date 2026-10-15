package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_314_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trolls", "1_314");
					put("trolls2", "1_314");
					put("trolls3", "1_314");
					put("trolls4", "1_314");
					// Bilbo, Retired Adventurer: [shire] Hobbit ally, home 3F; a fellowship ability used only on purpose
					put("bilbo", "1_284");
					// Bilbo, Melancholy Hobbit: [shire] Hobbit companion; his trigger needs a skirmish he is in
					put("bilbo2", "12_119");
					// Bilbo, Bearer of Things Burgled: [shire] Hobbit companion; "Regroup: Add a burden to play a [shire] tale"
					// is the one way a Free Peoples condition is played in a phase with minions; his move trigger is
					// avoided by laying him after the move
					put("bilbo3", "9_49");
					// Bounder: [shire] Hobbit ally of another title, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Hobbit Sword: [shire] possession (a hand weapon) a Hobbit bears, no other text; a card in play that is no character
					put("sword", "1_299");
					// There and Back Again: a [shire] tale condition borne by a Hobbit companion; a maneuver ability used only on purpose
					put("tale", "1_317");
					// Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Merry, From O'er the Brandywine: [shire] Hobbit, strength 3; his text needs a weapon
					put("merry", "1_303");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					// Pippin, Trollslayer: [gondor] Hobbit, strength 3; enduring, read here before he has a wound
					put("pippin", "103_31");

					// Cave Troll of Moria: unique [moria] Troll, strength 15, damage +1, fierce; its texts are about its play
					put("cavetroll", "1_165");
					// Beastly Olog-hai: [orc] Troll, strength 13, damage +1, fierce; an assignment ability used only on purpose
					put("olog", "11_108");
					put("olog2", "11_108");
					// Half-troll of Far Harad: [orc] Half-troll, no Troll, strength 8; its optional trigger needs an [orc]
					// Orc in the discard pile
					put("halftroll", "14_12");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1
					put("cirith", "10_81");     // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, damage +1
					// Isengard Worker: [isengard] Orc, no Uruk-hai, strength 5; a regroup ability used only on purpose
					put("worker", "3_62");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: [shire] Hobbit companion
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoneTrollsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Stone Trolls
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Tale</b>. To play, spot Bilbo. Plays on any character. Limit 1 per character.<br>Each time bearer skirmishes a Troll or Uruk-hai, bearer is strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trolls");

		assertEquals("Stone Trolls", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilBilboTheAllyIsSpottedAndThenPlaysOnAnyCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var trolls = scn.GetFreepsCard("trolls");
		var bilbo = scn.GetFreepsCard("bilbo");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToHand(trolls, bilbo);

		assertAttachedTo(sword, frodo);
		scn.StartGame();

		// A [shire] Hobbit ally of another title is no Bilbo.
		assertInPlay(bounder);
		assertFalse(scn.FreepsPlayAvailable(trolls));

		scn.FreepsPlayCard(bilbo);
		assertInPlay(bilbo);
		assertTrue(scn.FreepsPlayAvailable(trolls));

		scn.FreepsPlayCard(trolls);
		// Any character: companions of two cultures, and allies.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, bilbo, bounder));
		// A possession is borne by a character, and is none.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bounder);

		assertAttachedTo(trolls, bounder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedInTheRegroupPhaseByBilbosAbilityItMayGoOnAMinionToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var trolls = scn.GetFreepsCard("trolls");
		var bilbo = scn.GetFreepsCard("bilbo3");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(trolls);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCompanionsToTable(bilbo, aragorn);
		scn.FreepsRefreshDecision();

		assertInHand(trolls);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		// Bilbo's cost was paid; Stone Trolls is the one tale in hand, so it is played unasked.
		assertEquals(1, scn.GetBurdens());
		// Any character: the Shadow player's minion as well as the companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo, aragorn, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertAttachedTo(trolls, troop);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CanBePlayedWhileBilboTheCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var bilbo = scn.GetFreepsCard("bilbo2");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToHand(trolls);

		scn.StartGame();

		assertInPlay(bilbo);
		assertTrue(scn.FreepsPlayAvailable(trolls));

		scn.FreepsPlayCard(trolls);
		scn.FreepsChooseCard(bilbo);

		assertAttachedTo(trolls, bilbo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACharacterBearingOneIsNotOfferedAnotherAndWithEveryCharacterBearingOneItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var trolls = scn.GetFreepsCard("trolls");
		var trolls2 = scn.GetFreepsCard("trolls2");
		var trolls3 = scn.GetFreepsCard("trolls3");
		var trolls4 = scn.GetFreepsCard("trolls4");
		var tale = scn.GetFreepsCard("tale");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(aragorn, trolls);
		// Another [shire] tale, which does not count against the limit.
		scn.AttachCardsTo(frodo, tale);
		scn.MoveCardsToHand(trolls2, trolls3, trolls4);

		assertAttachedTo(trolls, aragorn);
		assertAttachedTo(tale, frodo);
		assertInHand(trolls2, trolls3, trolls4);
		scn.StartGame();

		scn.FreepsPlayCard(trolls2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(trolls2, frodo);

		// Bilbo, the one character left with none, is chosen unasked.
		scn.FreepsPlayCard(trolls3);
		assertAttachedTo(trolls3, bilbo);

		// Every character bears one; Bilbo is still spotted.
		assertInHand(trolls4);
		assertFalse(scn.FreepsPlayAvailable(trolls4));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3OnlyWhileSkirmishingATroll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var trolls2 = scn.GetFreepsCard("trolls2");
		var trolls3 = scn.GetFreepsCard("trolls3");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var olog = scn.GetShadowCard("olog");
		var olog2 = scn.GetShadowCard("olog2");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(merry, guard, pippin, aragorn);
		scn.AttachCardsTo(merry, trolls);
		scn.AttachCardsTo(guard, trolls2);
		scn.AttachCardsTo(pippin, trolls3);

		assertAttachedTo(trolls, merry);
		assertAttachedTo(trolls2, guard);
		assertAttachedTo(trolls3, pippin);
		scn.StartGame();

		scn.MoveMinionsToTable(cavetroll, olog, olog2, halftroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, cavetroll},
				new PhysicalCardImpl[]{guard, olog},
				new PhysicalCardImpl[]{pippin, halftroll},
				// Aragorn bears none.
				new PhysicalCardImpl[]{aragorn, olog2});

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, olog));
		// 4 + 3 against an [orc] Troll.
		assertEquals(7, scn.GetStrength(guard));
		// Merry is assigned to a Troll, but his skirmish has not begun.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, cavetroll));
		// 3 + 3 against a [moria] Troll.
		assertEquals(6, scn.GetStrength(merry));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, halftroll));
		// A Half-troll is no Troll.
		assertEquals(3, scn.GetStrength(pippin));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, olog2));
		// Skirmishing a Troll, but he is no bearer.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3WhileSkirmishingAnUrukHaiOfEitherCultureAndNotAnOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var trolls2 = scn.GetFreepsCard("trolls2");
		var trolls3 = scn.GetFreepsCard("trolls3");
		var trolls4 = scn.GetFreepsCard("trolls4");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var cirith = scn.GetShadowCard("cirith");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, guard, pippin, aragorn);
		scn.AttachCardsTo(merry, trolls);
		scn.AttachCardsTo(guard, trolls2);
		scn.AttachCardsTo(pippin, trolls3);
		scn.AttachCardsTo(aragorn, trolls4);

		assertAttachedTo(trolls, merry);
		assertAttachedTo(trolls2, guard);
		assertAttachedTo(trolls3, pippin);
		assertAttachedTo(trolls4, aragorn);
		scn.StartGame();

		scn.MoveMinionsToTable(savage, cirith, worker, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, savage},
				new PhysicalCardImpl[]{guard, cirith},
				new PhysicalCardImpl[]{pippin, worker},
				new PhysicalCardImpl[]{aragorn, troop});

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		// 3 + 3 against an [isengard] Uruk-hai.
		assertEquals(6, scn.GetStrength(merry));
		// The Dwarf Guard is assigned to an Uruk-hai, but his skirmish has not begun.
		assertEquals(4, scn.GetStrength(guard));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, cirith));
		// 4 + 3 against a [sauron] Uruk-hai.
		assertEquals(7, scn.GetStrength(guard));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, worker));
		// An [isengard] Orc is no Uruk-hai.
		assertEquals(3, scn.GetStrength(pippin));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		// A [moria] Orc is no Troll.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3AgainInTheFierceSkirmishAndNotBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cavetroll = scn.GetShadowCard("cavetroll");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, trolls);

		assertAttachedTo(trolls, aragorn);
		scn.StartGame();

		scn.MoveMinionsToTable(cavetroll);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cavetroll);
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.InFierceRound());
		// 8 + 3
		assertEquals(11, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();
		// 15 against 11: two wounds with the Troll's damage +1, and not overwhelmed.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, cavetroll);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.InFierceRound());
		// +3 again, and once: 8 + 3, not 8 + 6.
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
