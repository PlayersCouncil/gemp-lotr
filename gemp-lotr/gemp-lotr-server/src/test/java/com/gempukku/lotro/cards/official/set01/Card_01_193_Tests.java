package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_193_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armories", "1_193");

					// [moria] weapons
					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon (possession), cost 1, on a [moria] minion
					put("spear2", "1_182");     // a second Spear, already in the discard pile
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon (possession), cost 0, on a [moria] Orc only
					put("balrogsword", "2_50"); // The Balrog's Sword: [moria] artifact hand weapon, cost 1, on The Balrog, beside another hand weapon
					// Not [moria] weapons
					put("chain", "2_53");       // Cave Troll's Chain: a [moria] possession that is no weapon, cost 1, on Cave Troll of Moria
					put("banner", "6_98");      // Banner of the Eye: a [sauron] hand weapon, cost 0, on a [sauron] Orc

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger never fires, as it is cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, Archer and nothing else
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is no Orc
					put("balrog", "12_79");     // The Balrog, The Terror of Khazad-dum: his text acts only at an underground site
					put("durinsbane", "2_51");  // The Balrog, Durin's Bane: "While you can spot The Balrog, discard all other minions"
					put("soldier1", "1_271");   // Orc Soldier: a [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("soldier2", "1_271");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					put("defend", "4_115");     // Defend It and Hope: exert a [gondor] Man to wound a minion once (twice an Uruk-hai)
					put("wall", "5_33");        // City Wall: a [gondor] fortification, borne by the minion for Fall Back
					put("fallback", "5_34");    // Fall Back: "Discard all Shadow cards borne by a minion bearing a [gondor] fortification"
					put("taurnil", "5_13");     // Taurnil: while he bears a ranged weapon, each Shadow possession is twilight cost +2
					put("elvenbow", "1_41");    // Elven Bow: a ranged weapon for an Elf
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, damage +1; a skirmish ability used only on purpose
					put("gimliaxe", "1_14");    // Gimli's Battle Axe: +2, damage +1; "Each time Gimli wins a skirmish, you may wound an Orc"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlunderedArmoriesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Plundered Armories
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Response:</b> If your [moria] weapon is discarded, play it from your discard pile (that weapon's twilight cost is -1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armories");

		assertEquals("Plundered Armories", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWeaponDiscardedWithItsBearerIsPlayedBackOnAnotherMinionAtOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var spear2 = scn.GetShadowCard("spear2");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var taurnil = scn.GetFreepsCard("taurnil");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, taurnil);
		scn.AttachCardsTo(taurnil, elvenbow);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(runner, troop, troll);
		scn.AttachCardsTo(runner, spear);
		scn.MoveCardsToDiscard(spear2);
		assertAttachedTo(spear, runner);
		assertAttachedTo(elvenbow, taurnil);
		assertInDiscard(spear2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Under Taurnil the Spear costs 1 + 2 = 3, and 2 with the discount.
		scn.SetTwilight(2);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner, spear);
		assertEquals(2, scn.GetTwilight());

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.ShadowChooseOptionalTrigger(armories);
		// The Spear goes on a [moria] minion of the Shadow player's choice.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, troll));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertAttachedTo(spear, troll);
		assertEquals(0, scn.GetTwilight());
		// The discarded Spear is the one played; the other Spear stays in the discard pile.
		assertInDiscard(spear2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCannotPayTheWeaponLessOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var taurnil = scn.GetFreepsCard("taurnil");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, taurnil);
		scn.AttachCardsTo(taurnil, elvenbow);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(runner, troop);
		scn.AttachCardsTo(runner, spear);
		assertAttachedTo(spear, runner);
		assertAttachedTo(elvenbow, taurnil);
		scn.SkipToPhase(Phase.MANEUVER);
		// Under Taurnil the Spear costs 1 + 2 = 3, and 2 with the discount: one short.
		scn.SetTwilight(1);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner, spear);

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		assertInDiscard(spear);
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void NotOfferedWhenNoMinionLeftCanBearTheWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var soldier2 = scn.GetShadowCard("soldier2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		// The Runner is the only [moria] minion; the Spear cannot go on a [sauron] Orc.
		scn.MoveMinionsToTable(runner, soldier2);
		scn.AttachCardsTo(runner, spear);
		assertAttachedTo(spear, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner, spear);

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		assertInDiscard(spear);
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void APossessionAndAnArtifactDiscardedTogetherAreEachPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var balrogsword = scn.GetShadowCard("balrogsword");
		var balrog = scn.GetShadowCard("balrog");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var fallback = scn.GetFreepsCard("fallback");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fallback);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, balrogsword, spear); // lint-ok: The Balrog's Sword "may be borne in addition to 1 other hand weapon"
		assertAttachedTo(spear, balrog);
		assertAttachedTo(balrogsword, balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, balrog);
		scn.AttachCardsTo(balrog, wall);
		scn.FreepsRefreshDecision();
		assertAttachedTo(wall, balrog);
		// Each costs 1, and 0 with the discount.
		scn.SetTwilight(0);

		scn.FreepsPlayCard(fallback);
		assertInDiscard(spear, balrogsword);

		// One offer for each weapon.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories, "Goblin Spear"));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories, "The Balrog's Sword"));
		scn.ShadowChooseOptionalTrigger(armories, "Goblin Spear");
		assertAttachedTo(spear, balrog);
		assertInDiscard(balrogsword);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.ShadowChooseOptionalTrigger(armories);
		assertAttachedTo(balrogsword, balrog);
		assertAttachedTo(spear, balrog);
		assertAttachedTo(wall, balrog);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMoriaPossessionThatIsNoWeaponDiscardedWithTheSpearIsNotPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var chain = scn.GetShadowCard("chain");
		var troll = scn.GetShadowCard("troll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var wall = scn.GetFreepsCard("wall");
		var fallback = scn.GetFreepsCard("fallback");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fallback);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, spear);
		assertAttachedTo(spear, troll);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troll);
		// The Chain goes down after the maneuver phase, whose end it answers.
		scn.AttachCardsTo(troll, chain, wall);
		scn.FreepsRefreshDecision();
		assertAttachedTo(chain, troll);
		assertAttachedTo(wall, troll);
		// Enough to play either card back at full cost.
		scn.SetTwilight(2);

		scn.FreepsPlayCard(fallback);
		assertInDiscard(spear, chain);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.ShadowChooseOptionalTrigger(armories);
		assertAttachedTo(spear, troll);
		// Nothing is offered for the Chain, though the Troll could bear it and the pool could pay for it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		assertInDiscard(chain);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void AWeaponOfAnotherCultureIsNotPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var banner = scn.GetShadowCard("banner");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AttachCardsTo(soldier1, banner);
		// Vitality 2 with 1 wound: the event's wound kills him.
		scn.AddWoundsToChar(soldier1, 1);
		assertAttachedTo(banner, soldier1);
		assertEquals(1, scn.GetWoundsOn(soldier1));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(soldier1);
		assertInDiscard(soldier1, banner);

		// The other [sauron] Orc could bear the Banner, and the pool could pay for it.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		assertInDiscard(banner);
		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void TheWinnersOptionalTriggerComesBeforeTheLoserDiesAndHisWeaponIsPlayedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var gimliaxe = scn.GetFreepsCard("gimliaxe");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, gimliaxe);

		scn.StartGame();
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(runner, troop);
		scn.AttachCardsTo(runner, spear);
		assertAttachedTo(spear, runner);
		assertAttachedTo(gimliaxe, gimli);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(2);
		scn.PassSkirmishActions();

		// Gimli (6 + 2) beats the Runner (5 + 2).  The winner's optional trigger comes first, before the Runner
		// loses (engine pass 54; until then the Runner had died with the Spear and the Armories came first).
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertAttachedTo(spear, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimliaxe));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(troop));

		// Then the Runner dies of its wound for losing, with the Spear, and Plundered Armories answers.
		assertInDiscard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.ShadowChooseOptionalTrigger(armories);
		// The Troop is the one [moria] minion left: the Spear goes on him unasked.
		assertAttachedTo(spear, troop);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void WeaponsDiscardedWithAllTheOtherMinionsGoOnlyOnAMinionStillInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armories = scn.GetShadowCard("armories");
		var spear = scn.GetShadowCard("spear");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");
		var durinsbane = scn.GetShadowCard("durinsbane");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(armories);
		scn.MoveMinionsToTable(runner, marksman);
		scn.AttachCardsTo(runner, spear);
		scn.AttachCardsTo(marksman, scimitar);
		scn.MoveCardsToHand(durinsbane);
		scn.SetTwilight(20);
		assertAttachedTo(spear, runner);
		assertAttachedTo(scimitar, marksman);
		assertInHand(durinsbane);
		// To site 4, Balin's Tomb: underground, so The Balrog stays.
		scn.FreepsPass();

		scn.ShadowPlayCard(durinsbane);
		// Every other minion is discarded at once, with what they bear.
		assertInDiscard(runner, marksman, spear, scimitar);

		// The Spear may go on The Balrog (a [moria] minion), unasked; the Scimitar wants a [moria] Orc, and none is left.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(armories));
		scn.ShadowChooseOptionalTrigger(armories);
		assertAttachedTo(spear, durinsbane);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(armories));
		assertInDiscard(scimitar, runner, marksman);
	}
}
