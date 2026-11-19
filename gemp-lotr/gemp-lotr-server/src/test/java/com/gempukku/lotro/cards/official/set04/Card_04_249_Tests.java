package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_249_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "4_249");
					put("commander2", "4_249");   // a second copy: not unique

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit; a modifier while he bears a weapon
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; a strength modifier only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
					put("pride", "7_13");       // Reckless Pride: on a [dwarven] companion, each minion gains "Assignment: Assign this minion to bearer of Reckless Pride"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBoundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("commander1", "4_249");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, keywords only
					put("faramir", "4_117");    // Faramir, Son of Denethor: Ring-bound; a bar on the opponent's skirmish actions in his skirmishes
					put("ranger1", "4_130");    // Ranger of Ithilien: Ring-bound, not unique; a strength bonus against roaming minions only in a skirmish
					put("ranger2", "4_130");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronCommanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Commander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Ambush (1)</b>.<br><b>Assignment:</b> Spot 6 companions to assign this minion to the Ring-bearer. The Free Peoples player may discard an unbound companion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("commander1");

		assertEquals("Southron Commander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithSixCompanionsTheAssignmentAbilityAssignsThisMinionToTheRingBearerWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		// Six companions of five cultures, Ring-bound and unbound, the Ring-bearer among them.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(commander1));
		assertFalse(scn.IsCharAssigned(commander2));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(commander1));

		scn.ShadowUseCardAction(commander1);
		assertTrue(scn.FreepsDecisionAvailable("discard an unbound companion"));
		scn.FreepsChooseNo();

		// The copy used is assigned to the Ring-bearer; the other copy is not assigned.
		assertTrue(scn.IsCharAssignedAgainst(frodo, commander1));
		assertFalse(scn.IsCharAssigned(commander2));
		assertInZone(Zone.FREE_CHARACTERS, sam, aragorn, gimli, merry, gandalf);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardAnUnboundCompanionToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertFalse(scn.IsCharAssigned(commander1));
		assertTrue(scn.ShadowActionAvailable(commander1));

		scn.ShadowUseCardAction(commander1);
		assertTrue(scn.FreepsDecisionAvailable("discard an unbound companion"));
		scn.FreepsChooseYes();

		// Unbound companions of four cultures; not the Ring-bearer, a Ring-bound companion, an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, merry, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, celeborn, commander1, commander2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertInZone(Zone.DISCARD, merry);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.FREE_CHARACTERS, sam, aragorn, gimli, gandalf);
		// The assignment is prevented.
		assertFalse(scn.IsCharAssigned(commander1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyTheAssignmentAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var gandalf = scn.GetFreepsCard("gandalf");
		var celeborn = scn.GetFreepsCard("celeborn");
		// Five companions, and an ally, a character who is no companion.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry");
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(commander1));

		// A sixth companion.
		scn.MoveCompanionsToTable(gandalf);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(commander1));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithEveryCompanionRingBoundThePreventionIsNotAskedAndThisMinionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBoundScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable("sam", "boromir", "faramir", "ranger1", "ranger2");

		scn.StartGame();
		scn.MoveMinionsToTable(commander1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(commander1));
		assertTrue(scn.ShadowActionAvailable(commander1));

		scn.ShadowUseCardAction(commander1);
		// Six companions, none of them unbound: the price cannot be paid and is not offered.
		assertFalse(scn.FreepsDecisionAvailable("discard an unbound companion"));
		assertTrue(scn.IsCharAssignedAgainst(frodo, commander1));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceThisMinionIsAssignedElsewhereItsAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("sam", "aragorn", "merry", "gandalf");
		scn.AttachCardsTo(gimli, pride);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(commander1, "Use Southron Commander"));
		assertTrue(scn.ShadowActionAvailable(commander2, "Use Southron Commander"));

		// The first copy is assigned to Gimli by the ability Reckless Pride gives it.
		scn.ShadowUseCardAction(commander1, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, commander1));
		scn.FreepsPass();

		// Already assigned, with the Ring-bearer still unassigned: its own ability is not offered; the other copy's is.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.ShadowActionAvailable(commander1, "Use Southron Commander"));
		assertTrue(scn.ShadowActionAvailable(commander2, "Use Southron Commander"));
	}

	@Test
	public void OnceTheRingBearerIsAssignedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var commander1 = scn.GetShadowCard("commander1");
		var commander2 = scn.GetShadowCard("commander2");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("sam", "aragorn", "merry", "gandalf");
		scn.AttachCardsTo(gimli, pride);

		scn.StartGame();
		scn.MoveMinionsToTable(commander1, commander2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(commander1, "Use Southron Commander"));
		scn.ShadowUseCardAction(commander2, "Use Southron Commander");
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(frodo, commander2));
		scn.FreepsPass();

		// The Ring-bearer has a minion: the unassigned copy is not offered its own ability, though it is still
		// offered the one Reckless Pride gives it.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(commander1));
		assertFalse(scn.ShadowActionAvailable(commander1, "Use Southron Commander"));
		assertTrue(scn.ShadowActionAvailable(commander1, "Reckless Pride"));
	}
}
