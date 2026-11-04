package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_136");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// White Hand Warrior, wounded, and a companion who is no hunter.
	protected VirtualTableScenario GetWoundedWarriorScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_136");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Hunters and characters who are none, on both sides, and an ally who can be made to skirmish.
	protected VirtualTableScenario GetHuntersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "17_136");
					put("warrior2", "17_136");
					put("tracker", "15_171");   // Tracking Uruk: an [uruk-hai] hunter minion, vitality 3; hunter 1 is its whole text
					put("covetous", "15_158");  // Covetous Uruk: an [uruk-hai] minion who is no hunter, vitality 2; damage +1, +1 for each possession
					put("aragorn", "15_54");    // Aragorn, Swift Hunter: a hunter companion, vitality 4; his Skirmish ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion who is no hunter; his Skirmish ability is not used
					put("knight", "5_35");      // Gondorian Knight: a Man companion who is no hunter; Knight is his whole text
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally who is no hunter; Maneuver: exert him to let him skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 4</b>. <helper>(While skirmishing a non-hunter character, this character is strength +4.)</helper><br>Each time this minion is assigned to a non-hunter companion, you may heal a hunter.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior");

		assertEquals("White Hand Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(4, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "Each time this minion is assigned to a non-hunter companion, you may heal a hunter": a "you may" on a
	// trigger is asked and may be declined (Q-0021; Q-0220: G).  The definition healed a hunter unasked.
	// F-0462.
	@Test
	public void HealingAHunterIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoundedWarriorScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.AddWoundsToChar(warrior, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, warrior);

		// The Warrior is himself the wounded hunter: the offer is made, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(warrior));

		// Hunter 4 against a non-hunter: 10 to Aragorn's 8.  He is fierce, and is assigned to Aragorn again.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, warrior);

		// The offer is made again, and taken: he is the only wounded hunter, and nothing more is asked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToANonHunterCompanionItMayHealAHunterOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);

		var warrior = scn.GetShadowCard("warrior");
		var tracker = scn.GetShadowCard("tracker");
		var covetous = scn.GetShadowCard("covetous");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, tracker, covetous);
		scn.AddWoundsToChar(warrior, 1);
		scn.AddWoundsToChar(tracker, 2);
		scn.AddWoundsToChar(covetous, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(knight, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(2, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(covetous));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, warrior},
				new PhysicalCardImpl[]{aragorn, tracker},
				new PhysicalCardImpl[]{knight, covetous});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();
		// The Warrior himself, the Tracking Uruk and the Free Peoples player's Aragorn are wounded hunters;
		// the Covetous Uruk and the Knight are wounded and no hunters.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, tracker, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(covetous, knight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracker);

		// One wound of two.
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(covetous));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedByTheShadowPlayerToTheRingBearerItIsOfferedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);

		var warrior = scn.GetShadowCard("warrior");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, tracker);
		scn.AddWoundsToChar(tracker, 2);
		assertEquals(2, scn.GetWoundsOn(tracker));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, tracker);
		// The Warrior is left over, and the Shadow player assigns it to Frodo, a Hobbit and no hunter.
		scn.ShadowAssignToMinions(frodo, warrior);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();
		// The Tracking Uruk is the one wounded hunter, and is healed unasked.
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAHunterCompanionNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var warrior = scn.GetShadowCard("warrior");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, tracker);
		scn.AddWoundsToChar(warrior, 1);
		scn.AddWoundsToChar(tracker, 2);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(2, scn.GetWoundsOn(tracker));
		scn.SkipToAssignments();
		// The Warrior to a hunter; another minion to Gimli, who is none.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, warrior},
				new PhysicalCardImpl[]{gimli, tracker});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, warrior));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(2, scn.GetWoundsOn(tracker));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToAnAllyNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.AddWoundsToChar(warrior, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, warrior);

		// Thrarin is a character who is no hunter, but no companion.
		assertTrue(scn.IsCharAssignedAgainst(thrarin, warrior));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoWoundedHunterNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gimli, knight);

		var warrior = scn.GetShadowCard("warrior");
		var covetous = scn.GetShadowCard("covetous");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, covetous);
		// Wounded, but no hunter.
		scn.AddWoundsToChar(covetous, 1);
		assertEquals(1, scn.GetWoundsOn(covetous));
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, warrior},
				new PhysicalCardImpl[]{knight, covetous});

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		assertEquals(1, scn.GetWoundsOn(covetous));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ASecondWarriorAnswersOnlyItsOwnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.AddWoundsToChar(warrior, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));
		scn.SkipToAssignments();
		// The first Warrior to a hunter, the second to Gimli, who is none.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, warrior},
				new PhysicalCardImpl[]{gimli, warrior2});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior));
		scn.ShadowAcceptOptionalTrigger();
		// The first Warrior is the one wounded hunter.
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
