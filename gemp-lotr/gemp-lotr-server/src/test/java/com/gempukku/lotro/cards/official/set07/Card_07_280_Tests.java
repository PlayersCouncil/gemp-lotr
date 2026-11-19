package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_280_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peril", "7_280");
					put("towers", "7_281");     // Great Siege-towers: an engine; a [sauron] condition in the support area
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary", "4_212");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
					put("companies", "8_107");  // Their Marching Companies: an engine that is a condition; abilities used only on purpose
					put("grond", "8_103");      // Grond: an engine that is a possession; abilities used only on purpose
					// Some Secret Art of Flame: a [sauron] condition that is no engine; a regroup ability, and initiative at 3 tokens
					put("art", "7_313");
					put("engineer", "7_272");   // Gorgoroth Engineer: a besieger Orc; his text answers only his play from a stacked site
					put("troll", "8_102");      // Great Hill Troll: a besieger who is a Troll; his trigger is for King-block sites only
					// Uruk Besieger: [isengard] Uruk-hai with "Besieger" in the title and not the keyword; damage +1, and a
					// regroup ability that stacks himself
					put("uruk", "4_180");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger; his text answers a weapon played on him

					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability
					put("promise", "2_112");    // A Promise: Free Peoples condition, support area; needs Sam to do anything
					// Endurance of Dwarves: Free Peoples condition, bearer must be a Dwarf; a skirmish ability.  Not unique,
					// so one on an ally and one on a companion is a board in play.
					put("endurance1", "2_4");
					put("endurance2", "2_4");
					// Mallorn-trees: [elven] condition, support area; a fellowship ability
					put("mallorn", "1_54");
					// Old Toby: a Free Peoples possession in the support area; its draw answers its own play only
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatPerilofFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Great Peril of Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a besieger or a site you control.<br><b>Maneuver:</b> Discard an engine to discard a condition from the Free Peoples support area.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("peril");

		assertEquals("Great Peril of Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilABesiegerIsSpottedAndASiteTheOtherPlayerControlsDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var engineer = scn.GetShadowCard("engineer");
		var uruk = scn.GetShadowCard("uruk");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		// The one site controlled is the Free Peoples player's.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.MoveMinionsToTable(uruk, chieftain);
		scn.SetShadowHand(peril, engineer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Neither the Uruk-hai with "Besieger" in his title nor the [sauron] Orc who is no besieger is spotted.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(peril));

		scn.ShadowPlayCard(engineer);
		assertInZone(Zone.SHADOW_CHARACTERS, engineer);

		assertTrue(scn.ShadowPlayAvailable(peril));
		scn.ShadowPlayCard(peril);
		assertInZone(Zone.SUPPORT, peril);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithABesiegerWhoIsATrollAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SetShadowHand(peril);
		scn.SetTwilight(10);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(peril));
		scn.ShadowPlayCard(peril);
		assertInZone(Zone.SUPPORT, peril);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithASiteHeControlsAndNoBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.MoveMinionsToTable(chieftain);
		scn.SetShadowHand(peril);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(peril));
		scn.ShadowPlayCard(peril);
		assertInZone(Zone.SUPPORT, peril);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsAnEngineOfEitherItemTypeToDiscardAFreePeoplesConditionAndNotAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var companies = scn.GetShadowCard("companies");
		var grond = scn.GetShadowCard("grond");
		var art = scn.GetShadowCard("art");
		var runner = scn.GetShadowCard("runner");
		var promise = scn.GetFreepsCard("promise");
		var mallorn = scn.GetFreepsCard("mallorn");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCardsToSupportArea(peril, companies, grond, art, promise, mallorn, toby);
		assertInZone(Zone.SUPPORT, peril, companies, grond, art, promise, mallorn, toby);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);

		// An engine that is a condition and one that is a possession; not the [sauron] condition that is no engine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(companies, grond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(art, peril));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(grond);
		assertInDiscard(grond);

		// The two Free Peoples conditions, of two cultures; not the Free Peoples possession beside them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, mallorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby, art, companies));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(mallorn);

		assertInDiscard(mallorn);
		assertInZone(Zone.SUPPORT, promise, toby, companies, art, peril);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoConditionInTheFreePeoplesSupportAreaTheEngineIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var companies = scn.GetShadowCard("companies");
		var runner = scn.GetShadowCard("runner");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCardsToSupportArea(peril, companies, toby);
		assertInZone(Zone.SUPPORT, peril, companies, toby);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);

		assertInDiscard(companies);
		assertInZone(Zone.SUPPORT, toby, peril);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoEngineTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var art = scn.GetShadowCard("art");
		var runner = scn.GetShadowCard("runner");
		var mallorn = scn.GetFreepsCard("mallorn");

		scn.MoveCardsToSupportArea(peril, art, mallorn);
		assertInZone(Zone.SUPPORT, peril, art, mallorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [sauron] condition that is no engine, and a Free Peoples condition to discard.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(peril));
		assertInZone(Zone.SUPPORT, art, mallorn);
	}

	@Test
	public void ManeuverAbilityReachesAConditionOfEitherPlayersThatAnAllyBearsAndNotOneOnACompanionOrInTheShadowSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var peril = scn.GetShadowCard("peril");
		var towers = scn.GetShadowCard("towers");
		var pinned = scn.GetShadowCard("pinned");
		var weary = scn.GetShadowCard("weary");
		var runner = scn.GetShadowCard("runner");
		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var promise = scn.GetFreepsCard("promise");
		var endurance1 = scn.GetFreepsCard("endurance1");
		var endurance2 = scn.GetFreepsCard("endurance2");

		scn.MoveAlliesToTable(grimir);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(promise, peril, towers, pinned);
		// In the Free Peoples support area: a condition lying there, and what an ally there bears, the Shadow
		// player's own condition included.
		scn.AttachCardsTo(grimir, endurance1, weary);
		// Not in it: a Free Peoples condition on a companion, and the conditions in the Shadow player's support
		// area (Pinned Down, and this card and the engine themselves).
		scn.AttachCardsTo(gimli, endurance2);
		assertInZone(Zone.SUPPORT, promise, peril, towers, pinned);
		assertAttachedTo(endurance1, grimir);
		assertAttachedTo(weary, grimir);
		assertAttachedTo(endurance2, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(peril));
		scn.ShadowUseCardAction(peril);
		assertInDiscard(towers);
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, endurance1, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(endurance2, pinned, peril));
		scn.ShadowChooseCard(endurance1);

		assertInDiscard(endurance1);
		assertInZone(Zone.SUPPORT, promise, pinned, peril);
		assertAttachedTo(weary, grimir);
		assertAttachedTo(endurance2, gimli);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
