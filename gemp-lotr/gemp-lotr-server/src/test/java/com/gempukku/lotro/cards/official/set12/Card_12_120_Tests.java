package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_120_Tests
{

	// The Towers path: site 2 is Eastfold 4_331 (plains, no battleground), site 4 White Rocks 4_346 (a
	// battleground and nothing else; its Shadow number counts mounted companions).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("diversion", "12_120");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; strength +2 while he bears a weapon
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on being played
					put("runner2", "1_178");
					put("axe", "1_190");        // Moria Axe: a Shadow possession on a [moria] Orc; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion whose race is Hobbit; abilities used only on purpose
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Uruk Camp 4_335 (battleground and plains; each Uruk-hai played exerts) at site 2.
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("diversion", "12_120");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ALLY; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5
					put("runner2", "1_178");
					put("whelp", "7_202");      // Morgul Whelp: [wraith] Orc, strength 3 while the Ring-bearer has no wound
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DiversionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Diversion
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard a Hobbit not assigned to a skirmish from play to discard a minion from play.<br>If the fellowship is at a battleground site, you may add 2 burdens to play this event during a skirmish involving a Hobbit.
		*/

		var scn = GetScenario();

		var diversion = scn.GetFreepsCard("diversion");

		assertEquals("Diversion", diversion.getBlueprint().getTitle());
		assertNull(diversion.getBlueprint().getSubtitle());
		assertFalse(diversion.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, diversion.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, diversion.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, diversion.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(diversion, Timeword.REGROUP));
		assertEquals(1, diversion.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheRegroupPhaseAnyUnassignedHobbitButTheRingBearerPaysAndAnyMinionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diversion = scn.GetFreepsCard("diversion");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var axe = scn.GetShadowCard("axe");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(diversion);
		scn.MoveCompanionsToTable(merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, ted);
		scn.AttachCardsTo(runner, axe);
		assertAttachedTo(axe, runner);
		// Nobody is assigned: the minions live on into the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, runner, ted);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(diversion));
		scn.FreepsPlayCard(diversion);

		// Hobbits of each side, culture and kind; not the Ring-bearer (never discarded), a [shire] companion who
		// is no Hobbit, nor a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);
		// A companion discarded goes to the discard pile, not the dead pile (CR 4.2 "discard").
		assertInDiscard(merry);

		// Minions of two cultures and races; not a Shadow possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner, axe, diversion);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertInZone(Zone.SUPPORT, bounder);
		// Played in its own phase, it adds no burdens.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheRingBearerTheOnlyHobbitItCannotBePlayedAndWithNoMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diversion = scn.GetFreepsCard("diversion");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(diversion);
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The Ring-bearer cannot be discarded, and neither of the others is a Hobbit.
		assertInZone(Zone.FREE_CHARACTERS, frodo, smeagol, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(diversion));

		scn.MoveAlliesToTable(bounder);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.FreepsPlayAvailable(diversion));
		scn.FreepsPlayCard(diversion);

		// Bounder, the one Hobbit who can pay, is discarded unasked; no minion is there to discard.
		assertInDiscard(bounder, diversion);
		assertInZone(Zone.FREE_CHARACTERS, frodo, smeagol, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtUrukCampItIsPlayedForTwoBurdensInAHobbitsSkirmishAndNotInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var diversion = scn.GetFreepsCard("diversion");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToHand(diversion);
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2, whelp);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{frodo, runner2},
				new PhysicalCardImpl[]{merry, whelp});

		// At the battleground, Aragorn's skirmish involves no Hobbit, though Hobbits wait for theirs.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(diversion));
		// Aragorn, 8, beats the Runner, 5.
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(diversion));
		scn.FreepsPlayCard(diversion);

		// The Hobbits not assigned; not Frodo, who is fighting, nor Merry, assigned to a skirmish still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, merry));
		scn.FreepsChooseCard(pippin);
		assertInDiscard(pippin);

		// The minion fighting and the one waiting are both minions in play.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner2, whelp));
		scn.FreepsChooseCard(runner2);

		assertEquals(2, scn.GetBurdens());
		assertInDiscard(runner2, diversion);
		assertInZone(Zone.SHADOW_CHARACTERS, whelp);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.SUPPORT, bounder);
		// With its one minion gone, Frodo's skirmish is over: Merry's is next.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AtEastfoldWhichIsNoBattlegroundItCannotBePlayedInAHobbitsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diversion = scn.GetFreepsCard("diversion");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(diversion);
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// Eastfold is plains and no battleground; Merry could pay and the burdens could be added.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.IsCharSkirmishing(frodo));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(diversion));
	}

	@Test
	public void AtWhiteRocksItIsPlayedInTheSkirmishOfAHobbitMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var diversion = scn.GetFreepsCard("diversion");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(merry, aragorn);
		// Kept out of the deck the turns draw from.
		scn.MoveCardsToDiscard(diversion);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(diversion);
		scn.MoveMinionsToTable(ted);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, ted);

		// White Rocks, site 4, is a battleground; the Hobbit in the skirmish is the Shadow player's.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(diversion));
		scn.FreepsPlayCard(diversion);

		// Merry, the one Hobbit not assigned who can be discarded, and Ted, the one minion, go unasked.
		assertEquals(2, scn.GetBurdens());
		assertInDiscard(merry, ted, diversion);
		// With its one minion gone, the skirmish, the only one, is over.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
