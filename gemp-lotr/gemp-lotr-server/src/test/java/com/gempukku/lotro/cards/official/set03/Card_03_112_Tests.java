package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seek", "3_112");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his texts are not used here
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3, vitality 4; an assignment ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor]; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion with damage +1; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; "Damage +1" is its whole text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9; "Damage +1" is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("axe", "1_190");        // Moria Axe: on a [moria] Orc, "Bearer is damage +1"; its skirmish ability is used only on purpose
					put("cry1", "1_128");       // Lurtz's Battle Cry: [isengard] Skirmish event, "Make an Uruk-hai strength -1 and damage +1"
					put("cry2", "1_128");
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
					put("seek", "3_112");
					// Merry, Friend to Sam: a [shire] companion, as the Ring-bearer is; strength 3, vitality 4.  His
					// ability is for when he is not assigned to a skirmish.
					put("merry", "1_302");
					// Uruk Savage: strength 5; Damage +1 is its whole text.  It beats Merry without overwhelming him.
					put("savage", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SeekandHideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Seek and Hide
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Stealth</b>.<br><b>Skirmish:</b> Spot 2 [shire] companions to make a minion lose all <b>damage</b> bonuses.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seek");

		assertEquals("Seek and Hide", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenMinionLoseItsDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var seek = scn.GetFreepsCard("seek");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(seek);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(seek));

		scn.FreepsPlayCard(seek);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, raiders));
		scn.FreepsChooseCard(savage);
		assertInDiscard(seek);
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));

		// Merry, 3 against 5, loses: the one wound of the loss, and no second for damage +1.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionOfAnyKindAssignedToNoSkirmishMayBeChosenAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var seek = scn.GetFreepsCard("seek");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var axe = scn.GetShadowCard("axe");
		// Frodo and Smeagol, who is no Hobbit, are the two [shire] companions spotted.
		scn.MoveCompanionsToTable(smeagol, gimli);
		scn.MoveCardsToHand(seek);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.AttachCardsTo(troop, axe);
		assertInZone(Zone.ATTACHED, axe);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, savage);
		// The Troop is assigned to no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(smeagol);

		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(seek));

		scn.FreepsPlayCard(seek);
		// A [moria] Orc in no skirmish and an [isengard] Uruk-hai in this one are offered; a companion with a
		// damage bonus, and the other companions, are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, smeagol, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// The bonus the Axe gives the Troop is lost; the Savage and Gimli keep theirs.
		assertInDiscard(seek);
		assertInZone(Zone.ATTACHED, axe);
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Smeagol, 3 against 5, loses: one wound for the loss and one for the Savage's damage +1.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AllItsDamageBonusesAreLostAndNoneGainedUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seek = scn.GetFreepsCard("seek");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		var cry1 = scn.GetShadowCard("cry1");
		var cry2 = scn.GetShadowCard("cry2");
		// Frodo and Sam are the two [shire] companions.
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(seek, cry1, cry2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{aragorn, raiders});
		scn.FreepsResolveSkirmish(sam);

		// The Savage's own bonus, and one given to it for the skirmish.
		scn.FreepsPass();
		scn.ShadowPlayCard(cry1);
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(savage));

		scn.FreepsPlayCard(seek);
		scn.FreepsChooseCard(savage);
		// Both are lost; the Raiding Party keeps its own.
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiders, Keyword.DAMAGE));

		// A bonus given after it does not land: the second Battle Cry takes its strength and gives it nothing.
		scn.ShadowPlayCard(cry2);
		scn.ShadowChooseCard(savage);
		assertInDiscard(cry2);
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(0, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// Sam, 3 against 3, loses: the one wound of the loss.  Either Battle Cry's bonus would have added more.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// In the next skirmish the Savage has its own damage +1 back.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
	}

	@Test
	public void CannotBePlayedWithOneShireCompanionBesideAGondorHobbitAndAShireAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seek = scn.GetFreepsCard("seek");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		// Frodo is the one [shire] companion; Pippin is a Hobbit companion of another culture and Bounder a
		// [shire] Hobbit who is no companion.
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(seek);
		assertInHand(seek);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, savage);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(seek);
		assertFalse(scn.FreepsPlayAvailable(seek));
	}
}
