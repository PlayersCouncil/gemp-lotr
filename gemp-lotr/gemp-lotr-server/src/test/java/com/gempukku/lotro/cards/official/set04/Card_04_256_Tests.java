package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_256_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop1", "4_256");
					put("troop2", "4_256");     // a second copy: not unique
					put("patrol", "1_177");     // Goblin Patrol Troop: a minion with no text, so that there is an assignment phase

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound [shire] Hobbit; a modifier while he bears a weapon
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; a strength modifier only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally; a fellowship ability only
					put("pride", "7_13");       // Reckless Pride: on a [dwarven] companion, each minion gains "Assignment: Assign this minion to bearer of Reckless Pride"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop1", "4_256");
					put("soldier", "4_254");    // Southron Soldier: a [raider] Southron Man; "Southron. Ambush (2)" is its whole text
					put("lancers", "4_217");    // Desert Lancers: a unique [raider] Southron Man; a regroup ability only
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man who is no Southron; a modifier at 2 burdens only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>. <b>Ambush (1)</b>. To play, spot a Southron.<br><b>Assignment:</b> Spot 7 companions to assign this minion to the Ring-bearer. The Free Peoples player may make you discard a companion (except the Ring-bearer) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troop1");

		assertEquals("Southron Troop", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.AMBUSH));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedBesideARaiderManWhoIsNoSouthronUntilASouthronIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var soldier = scn.GetShadowCard("soldier");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(troop1);

		scn.StartGame();
		assertInZone(Zone.HAND, troop1);
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.HAND, troop1);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troop1));

		// A Southron.
		scn.MoveMinionsToTable(soldier);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troop1));
		scn.ShadowPlayCard(troop1);

		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASouthronOfAnotherTitleLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var lancers = scn.GetShadowCard("lancers");
		scn.MoveCardsToHand(troop1);

		scn.StartGame();
		assertInZone(Zone.HAND, troop1);
		scn.MoveMinionsToTable(lancers);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInZone(Zone.HAND, troop1);
		assertTrue(scn.ShadowPlayAvailable(troop1));
		scn.ShadowPlayCard(troop1);

		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSevenCompanionsTheAssignmentAbilityAssignsThisMinionToTheRingBearerWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var patrol = scn.GetShadowCard("patrol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		// Seven companions of five cultures, Ring-bound and unbound, the Ring-bearer among them.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The Troops are archers: laid once the archery phase is over.
		scn.MoveMinionsToTable(troop1, troop2);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(troop1));

		scn.ShadowUseCardAction(troop1);
		assertTrue(scn.FreepsDecisionAvailable("discard a companion"));
		scn.FreepsChooseNo();

		// The copy used is assigned to the Ring-bearer; the other copy, and Sam, are not assigned.
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop1));
		assertFalse(scn.IsCharAssigned(troop2));
		assertFalse(scn.IsCharAssigned(sam));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInZone(Zone.FREE_CHARACTERS, sam, aragorn, gimli, merry, gandalf, boromir);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayMakeTheShadowPlayerDiscardACompanionToPreventTheAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var patrol = scn.GetShadowCard("patrol");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(sam, aragorn, gimli, merry, gandalf, boromir);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertFalse(scn.IsCharAssigned(troop1));
		assertTrue(scn.ShadowActionAvailable(troop1));

		scn.ShadowUseCardAction(troop1);
		assertTrue(scn.FreepsDecisionAvailable("discard a companion"));
		scn.FreepsChooseYes();

		// The Shadow player ("you") chooses: every companion but the Ring-bearer, Ring-bound or unbound, of five
		// cultures; not the Ring-bearer, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, gimli, merry, gandalf, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, celeborn, troop1, troop2, patrol));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertInZone(Zone.DISCARD, gimli);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInZone(Zone.FREE_CHARACTERS, frodo, sam, aragorn, merry, gandalf, boromir);
		// The assignment is prevented.
		assertFalse(scn.IsCharAssigned(troop1));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithSixCompanionsAndAnAllyTheAssignmentAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var patrol = scn.GetShadowCard("patrol");
		var boromir = scn.GetFreepsCard("boromir");
		// Six companions, and an ally, a character who is no companion.
		scn.MoveCompanionsToTable("sam", "aragorn", "gimli", "merry", "gandalf");
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(troop1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop1));

		// A seventh companion.
		scn.MoveCompanionsToTable(boromir);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(troop1));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void OnceThisMinionIsAssignedElsewhereItsAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var patrol = scn.GetShadowCard("patrol");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("sam", "aragorn", "merry", "gandalf", "boromir");
		scn.AttachCardsTo(gimli, pride);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, pride);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troop1, "Use Southron Troop"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Use Southron Troop"));

		// The first copy is assigned to Gimli by the ability Reckless Pride gives it.
		scn.ShadowUseCardAction(troop1, "Reckless Pride");
		assertTrue(scn.IsCharAssignedAgainst(gimli, troop1));
		scn.FreepsPass();

		// Already assigned, with the Ring-bearer still unassigned: its own ability is not offered; the other copy's is.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.ShadowActionAvailable(troop1, "Use Southron Troop"));
		assertTrue(scn.ShadowActionAvailable(troop2, "Use Southron Troop"));
	}

	@Test
	public void OnceTheRingBearerIsAssignedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var patrol = scn.GetShadowCard("patrol");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var pride = scn.GetFreepsCard("pride");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCompanionsToTable("sam", "aragorn", "merry", "gandalf", "boromir");
		scn.AttachCardsTo(gimli, pride);

		scn.StartGame();
		assertInZone(Zone.ATTACHED, pride);
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troop1, "Use Southron Troop"));
		scn.ShadowUseCardAction(troop2, "Use Southron Troop");
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop2));
		scn.FreepsPass();

		// The Ring-bearer has a minion: the unassigned copy is not offered its own ability, though it is still
		// offered the one Reckless Pride gives it.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(troop1));
		assertFalse(scn.ShadowActionAvailable(troop1, "Use Southron Troop"));
		assertTrue(scn.ShadowActionAvailable(troop1, "Reckless Pride"));
	}
}
