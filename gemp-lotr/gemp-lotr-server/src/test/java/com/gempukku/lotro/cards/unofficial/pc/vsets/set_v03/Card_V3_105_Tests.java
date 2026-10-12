package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_105_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("driven", "103_105");
					put("driven2", "103_105");
					put("driven3", "103_105");

					put("sam", "1_311");          // Sam, Son of Hamfast: Hobbit, strength 3, vitality 4
					put("merry", "1_302");        // Merry, Friend to Sam: Hobbit, strength 3, vitality 4; his ability needs him unassigned
					put("pippin", "1_306");       // Pippin, Friend to Frodo: Hobbit, strength 3, vitality 4
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3, vitality 4; knight, enduring, and a
					// skirmish ability used only on purpose
					put("trollslayer", "103_31");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: Man, strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: a fifth companion
					put("elrond", "1_40");        // Elrond, Lord of Rivendell: an ally, who is no companion
					put("bounder", "1_286");      // Bounder: Hobbit ally, home site 2, strength 2, vitality 2; a skirmish ability used only on purpose
					put("sword", "1_299");        // Hobbit Sword: hand weapon, strength +2, no text
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("ranger", "1_112");       // Ranger's Sword: hand weapon for Aragorn, strength +2, damage +1
					put("battlebow", "11_89");    // Long Battle Bow: a ranged weapon for a [men] minion, strength +1; bearer is an archer
					// Henchman's Dagger: a hand weapon for a [men] minion, cost 0, strength +1; it may be borne beside 1 other hand weapon
					put("dagger", "18_69");

					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3, vitality 4, site 2; a maneuver
					// ability that needs a site he controls and a regroup ability used only on purpose
					put("ted", "18_75");

					put("troop", "1_177");        // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text at all
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron], strength 8, vitality 3; a Shadow-phase ability only
					put("taskmaster2", "2_90");
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; its only text is for when it is played
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrivenByHerOwnCruelWillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Driven By Her Own Cruel Will
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a skirmishing Hobbit bearing a hand weapon (except the Ring-bearer) strength +X, where X is the total vitality of all characters in that skirmish.  At the end of that skirmish, hinder a minion involved in that skirmish if you cannot spot more than 4 companions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("driven");

		assertEquals("Driven By Her Own Cruel Will", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheSkirmishingHobbitStrongerByTheVitalityLeftToEveryoneInThatSkirmishUntilItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var taskmaster = scn.GetShadowCard("taskmaster");
		// Five companions with Frodo, so that nothing is hindered when the skirmish ends.
		scn.MoveCompanionsToTable(sam, merry, aragorn, gimli);
		scn.AttachCardsTo(sam, sword);
		scn.AttachCardsTo(merry, sword2);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner, taskmaster);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(troop, 1);
		scn.SkipToAssignments();
		// Merry, with a sword of his own, waits for a later skirmish; the Taskmaster fights nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop}, new PhysicalCardImpl[]{merry, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);

		// 3 + 2 for the sword
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(merry));
		assertInZone(Zone.HAND, driven);
		assertTrue(scn.FreepsPlayAvailable(driven));

		scn.FreepsPlayCard(driven);

		// X: Sam has 4 - 1 = 3 vitality left and the Troop 3 - 1 = 2. Nobody outside the skirmish is counted.
		assertEquals(5 + 5, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(merry));
		assertInZone(Zone.DISCARD, driven);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// X was worked out when the event was played: a later wound in the skirmish does not change it.
		scn.AddWoundsToChar(troop, 1);
		assertEquals(10, scn.GetStrength(sam));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(sam));

		// 10 against 13: Sam loses. There are five companions, so the Troop is not hindered.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.IsHindered(troop));

		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(merry));
		assertFalse(scn.IsHindered(troop));
	}

	@Test
	public void XCountsEachMinionInTheSkirmishAndWithFourCompanionsOneOfThoseMinionsIsHinderedWhenItEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var driven2 = scn.GetFreepsCard("driven2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var taskmaster2 = scn.GetShadowCard("taskmaster2");
		// Four companions with Frodo. Elrond is a fifth character and no companion.
		scn.MoveCompanionsToTable(sam, aragorn, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(driven, driven2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, taskmaster, taskmaster2);
		// This Taskmaster made fierce, so that Sam fights it a second time in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, taskmaster, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop}, new PhysicalCardImpl[]{aragorn, taskmaster2});
		scn.ShadowAssignToMinions(sam, taskmaster);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(5, scn.GetStrength(sam));

		scn.FreepsPlayCard(driven);

		// X: Sam 4 + the Troop 3 + the Taskmaster 3. The other Taskmaster is not in this skirmish.
		assertEquals(5 + 10, scn.GetStrength(sam));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(taskmaster));

		// 15 against 13 + 8: Sam loses and both minions live.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, taskmaster));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(taskmaster2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(taskmaster));
		assertFalse(scn.IsHindered(taskmaster2));

		// The next skirmish is not "that skirmish": 8 against 8, Aragorn loses, and nothing is hindered at its end.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(taskmaster2));
		assertFalse(scn.IsHindered(taskmaster));

		// Nor is a later skirmish of the same Hobbit: Sam fights the fierce Taskmaster again, with no bonus left.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, taskmaster);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishing(sam));
		assertEquals(5, scn.GetStrength(sam));

		// 5 against 8: Sam loses again, and nothing is hindered at the end of this skirmish either.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.IsHindered(taskmaster));
		assertFalse(scn.IsHindered(taskmaster2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// A skirmish event: not for the regroup phase.
		assertInZone(Zone.HAND, driven2);
		assertFalse(scn.FreepsPlayAvailable(driven2));
	}

	@Test
	public void HobbitKilledInThatSkirmishStillLeavesAMinionHinderedAtItsEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4 with 3 wounds: the wound for losing kills Merry.
		scn.AddWoundsToChar(merry, 3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(5, scn.GetStrength(merry));

		scn.FreepsPlayCard(driven);

		// X: Merry has 1 vitality left and the Troop 3.
		assertEquals(5 + 4, scn.GetStrength(merry));
		assertFalse(scn.IsHindered(troop));

		// 9 against 13: Merry loses and dies. The Troop was involved in that skirmish all the same.
		scn.ShadowPass();
		scn.FreepsPass();

		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CompanionsAreCountedWhenTheSkirmishEndsAndNotWhenTheEventIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		// Five companions with Frodo when the event is played.
		scn.MoveCompanionsToTable(sam, merry, aragorn, gimli);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, troop);
		scn.FreepsResolveSkirmish(sam);

		scn.FreepsPlayCard(driven);

		// X: Sam 4 + the Troop 3
		assertEquals(5 + 7, scn.GetStrength(sam));
		assertFalse(scn.IsHindered(troop));

		// A hindered companion cannot be spotted: four are left to count when the skirmish ends.
		scn.HinderCard(gimli);

		// 12 against 13: Sam loses.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void HobbitAllyFightingAtHisHomeSiteIsStrengthenedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(bounder, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		// Site 2 is the Bounder's home, where an ally may be assigned to a skirmish.
		scn.FreepsAssignToMinions(bounder, troop);
		scn.FreepsResolveSkirmish(bounder);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.IsCharSkirmishing(bounder));
		// 2 + 2 for the sword
		assertEquals(4, scn.GetStrength(bounder));
		assertTrue(scn.FreepsPlayAvailable(driven));

		scn.FreepsPlayCard(driven);

		// X: the Bounder 2 + the Troop 3. The text says "Hobbit", not "Hobbit companion".
		assertEquals(4 + 5, scn.GetStrength(bounder));
		assertInZone(Zone.DISCARD, driven);
		assertFalse(scn.IsHindered(troop));

		// 9 against 13: the Bounder loses. Frodo is the only companion, so the Troop is hindered.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHobbitWhoIsNoShireCardIsStrengthenedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		// Pippin, Trollslayer: a Hobbit of the [gondor] culture.
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(trollslayer);
		scn.AttachCardsTo(trollslayer, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(trollslayer, troop);
		scn.FreepsResolveSkirmish(trollslayer);

		assertTrue(scn.IsRace(trollslayer, Race.HOBBIT));
		// 3 + 2 for the sword
		assertEquals(5, scn.GetStrength(trollslayer));
		assertTrue(scn.FreepsPlayAvailable(driven));

		scn.FreepsPlayCard(driven);

		// X: Pippin 4 + the Troop 3. The text says "Hobbit": it names the race and no culture.
		assertEquals(5 + 7, scn.GetStrength(trollslayer));
		assertInZone(Zone.DISCARD, driven);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOpponentsHobbitBearingAHandWeaponIsAHobbitTheEventMayStrengthen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		// Ted Sandyman is a Hobbit and a [men] minion, and the Dagger is the [men] minions' hand weapon.
		var ted = scn.GetShadowCard("ted");
		var dagger = scn.GetShadowCard("dagger");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.AttachCardsTo(ted, dagger);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, ted);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsRace(ted, Race.HOBBIT));
		assertAttachedTo(dagger, ted);
		// 3 + 2 for the Hobbit Sword
		assertEquals(5, scn.GetStrength(sam));
		// 3 + 1 for the Dagger
		assertEquals(4, scn.GetStrength(ted));
		assertTrue(scn.FreepsPlayAvailable(driven));

		// "A skirmishing Hobbit bearing a hand weapon": the text names a race and no side, and two such Hobbits are
		// in this skirmish. One of them must be chosen.
		scn.FreepsPlayCard(driven);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(ted);

		// X: Sam 4 + Ted Sandyman 4. The Hobbit chosen is stronger, and the other is not.
		assertEquals(4 + 8, scn.GetStrength(ted));
		assertEquals(5, scn.GetStrength(sam));
		assertInZone(Zone.DISCARD, driven);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DoesNothingForAManWithAHandWeaponAHobbitWithARangedWeaponOrTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var driven2 = scn.GetFreepsCard("driven2");
		var driven3 = scn.GetFreepsCard("driven3");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var ranger = scn.GetFreepsCard("ranger");
		var sword3 = scn.GetFreepsCard("sword3");
		var runner = scn.GetShadowCard("runner");
		var ted = scn.GetShadowCard("ted");
		var battlebow = scn.GetShadowCard("battlebow");
		var runner3 = scn.GetShadowCard("runner3");
		// Five companions with Frodo, so that no skirmish here could end with a minion hindered.
		scn.MoveCompanionsToTable(aragorn, pippin, sam, gimli);
		scn.AttachCardsTo(aragorn, ranger);
		scn.AttachCardsTo(frodo, sword3);
		scn.MoveCardsToHand(driven, driven2, driven3);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, ted, runner3);
		scn.SkipToAssignments();
		// No ranged weapon goes on a Free Peoples Hobbit. Ted Sandyman is a Hobbit and a [men] minion, and bears
		// the [men] minions' bow. It makes its bearer an archer, so he is handed it once the archery phase is over.
		scn.AttachCardsTo(ted, battlebow);
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{pippin, ted}, new PhysicalCardImpl[]{frodo, runner3});

		// A Man bearing a hand weapon.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(driven));
		scn.FreepsPlayCard(driven);
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, driven);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();

		// A Hobbit bearing a weapon that is not a hand weapon, and the Hobbit he fights, who bears nothing.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsRace(ted, Race.HOBBIT));
		assertAttachedTo(battlebow, ted);
		assertEquals(3, scn.GetStrength(pippin));
		// 3 + 1 for the Long Battle Bow
		assertEquals(4, scn.GetStrength(ted));
		scn.FreepsPlayCard(driven2);
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(ted));
		assertInZone(Zone.DISCARD, driven2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(pippin));

		// A Hobbit bearing a hand weapon who is the Ring-bearer.
		scn.FreepsResolveSkirmish(frodo);
		int frodoStrength = scn.GetStrength(frodo);
		scn.FreepsPlayCard(driven3);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, driven3);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedWhereItStrengthensNobodyItHindersNoMinionEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var driven = scn.GetFreepsCard("driven");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ranger = scn.GetFreepsCard("ranger");
		var troop = scn.GetShadowCard("troop");
		// Two companions, Frodo and Aragorn.
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, ranger);
		scn.MoveCardsToHand(driven);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(10, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(driven);
		assertEquals(10, scn.GetStrength(aragorn));
		assertInZone(Zone.DISCARD, driven);

		// 10 against 13: Aragorn loses. No Hobbit was strengthened, so there is no "that skirmish" to end.
		scn.ShadowPass();
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
